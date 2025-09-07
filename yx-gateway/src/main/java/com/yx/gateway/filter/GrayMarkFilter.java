package com.yx.gateway.filter;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.stereotype.Component;
import org.springframework.util.AntPathMatcher;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.security.SecureRandom;
import java.util.List;

@Component
public class GrayMarkFilter implements GlobalFilter, Ordered {

    @Value("${gray.enabled:true}")
    private boolean enabled;

    @Value("${gray.percent:0}")
    private int percent;

    @Value("${gray.user-ids:}")
    private List<Long> grayUserIds;

    @Value("${gray.include-paths:}")
    private List<String> includePaths;

    private final AntPathMatcher matcher = new AntPathMatcher();
    private final SecureRandom random = new SecureRandom();

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        if (!enabled) return chain.filter(exchange);

        String path = exchange.getRequest().getURI().getPath();
        // 若配置了参与灰度的接口名单且当前不在名单里，直接走 prd
        if (includePaths != null && !includePaths.isEmpty()) {
            boolean matched = includePaths.stream().anyMatch(p -> matcher.match(p, path));
            if (!matched) return chain.filter(withEnv(exchange, "prd"));
        }

        // 1) 用户灰度：从下游 JwtVerify 放到 request header 的 userId 里取（或从已有的JWT中解析）
        Long uid = parseUserId(exchange); // 你也可以从 JWT 中解析
        if (uid != null && grayUserIds != null && grayUserIds.contains(uid)) {
            return chain.filter(withEnv(exchange, "gray"));
        }

        // 2) 百分比灰度
        if (percent > 0) {
            int r = random.nextInt(100) + 1; // 1~100
            if (r <= percent) {
                return chain.filter(withEnv(exchange, "gray"));
            }
        }

        // 默认走 prd
        return chain.filter(withEnv(exchange, "prd"));
    }

    private ServerWebExchange withEnv(ServerWebExchange exchange, String env) {
        ServerHttpRequest mutated = exchange.getRequest().mutate()
                .header("X-Env", env)
                .build();
        return exchange.mutate().request(mutated).build();
    }

    // 示例：尝试从请求头拿 userId（可改成从 JWT 解析）
    private Long parseUserId(ServerWebExchange exchange) {
        String v = exchange.getRequest().getHeaders().getFirst("X-User-Id");
        if (v == null) return null;
        try { return Long.parseLong(v); } catch (Exception e) { return null; }
    }

    @Override
    public int getOrder() { return -100; } // 比 JwtVerify 更前或者更后都可，根据你的实现调整
}
