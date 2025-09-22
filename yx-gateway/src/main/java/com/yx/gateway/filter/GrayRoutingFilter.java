package com.yx.gateway.filter;

import org.springframework.cloud.gateway.filter.GatewayFilter;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.cloud.gateway.filter.factory.AbstractGatewayFilterFactory;
import org.springframework.cloud.gateway.route.RouteLocator;
import org.springframework.cloud.gateway.route.builder.RouteLocatorBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.Ordered;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpCookie;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

@Configuration
public class GrayRoutingFilter {

    @Bean
    public RouteLocator yxRouteLocator(RouteLocatorBuilder builder) {
        return builder.routes()
                .route("auth", route -> route.path("/api/auth/**")
                        .filters(filter -> filter.filter(new GrayGatewayFilter()))
                        .uri("lb://yx-auth"))
                .route("store", route -> route.path("/api/stores/**")
                        .filters(filter -> filter.filter(new GrayGatewayFilter()))
                        .uri("lb://yx-store"))
                .route("proxy-auth", route -> route.path("/kaiwen-proxy/auth/**")
                        .filters(filter -> filter.stripPrefix(2).filter(new GrayGatewayFilter()))
                        .uri("lb://yx-auth"))
                .route("proxy-store", route -> route.path("/kaiwen-proxy/store/**")
                        .filters(filter -> filter.stripPrefix(2).filter(new GrayGatewayFilter()))
                        .uri("lb://yx-store"))
                .build();
    }

    @Component
    public static class GrayGatewayFilter extends AbstractGatewayFilterFactory<GrayGatewayFilter.Config> implements Ordered {

        public GrayGatewayFilter() {
            super(Config.class);
        }

        @Override
        public GatewayFilter apply(Config config) {
            return (exchange, chain) -> {
                String tag = resolveTag(exchange);
                if (tag != null) {
                    GrayRequestContextHolder.setTag(tag);
                    ServerHttpRequest mutatedRequest = exchange.getRequest().mutate()
                            .headers(httpHeaders -> {
                                httpHeaders.set("SERVICE-TAG", tag);
                                httpHeaders.set("X-Gray", tag);
                                httpHeaders.set("X-Env", tag);
                            })
                            .build();
                    ServerWebExchange mutatedExchange = exchange.mutate().request(mutatedRequest).build();
                    return chain.filter(mutatedExchange).doFinally(signalType -> GrayRequestContextHolder.clear());
                }
                return chain.filter(exchange).doFinally(signalType -> GrayRequestContextHolder.clear());
            };
        }

        @Override
        public int getOrder() {
            return -1;
        }

        public static class Config {
            // 配置类，可根据需要添加配置属性
        }

        static String resolveTag(ServerWebExchange exchange) {
            HttpHeaders headers = exchange.getRequest().getHeaders();
            String explicit = firstNonEmpty(
                    headers.getFirst("SERVICE-TAG"), 
                    headers.getFirst("X-Gray"), 
                    headers.getFirst("X-Env")
            );
            String normalized = normalizeTag(explicit);
            if (normalized != null) {
                return normalized;
            }
            
            HttpCookie cookie = exchange.getRequest().getCookies().getFirst("env");
            if (cookie != null) {
                normalized = normalizeTag(cookie.getValue());
                if (normalized != null) {
                    return normalized;
                }
            }
            return null;
        }

        private static String normalizeTag(String candidate) {
            if (!StringUtils.hasText(candidate)) {
                return null;
            }
            String lower = candidate.toLowerCase();
            if (lower.contains("gray")) {
                return "gray";
            }
            if (lower.contains("prd")) {
                return "prd";
            }
            return null;
        }

        private static String firstNonEmpty(String... values) {
            for (String value : values) {
                if (StringUtils.hasText(value)) {
                    return value;
                }
            }
            return null;
        }
    }

    @Component
    public static class SouthNorthGlobalFilter implements GlobalFilter, Ordered {

        @Override
        public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
            String tag = GrayGatewayFilter.resolveTag(exchange);
            if (tag == null) {
                HttpHeaders headers = exchange.getRequest().getHeaders();
                String userId = headers.getFirst("X-User-Id");
                if (userId != null && userId.toLowerCase().endsWith("gray")) {
                    tag = "gray";
                }
            }

            if (tag != null) {
                GrayRequestContextHolder.setTag(tag);
                ServerHttpRequest mutatedRequest = exchange.getRequest().mutate()
                        .headers(httpHeaders -> {
                            httpHeaders.set("SERVICE-TAG", tag);
                            httpHeaders.set("X-Gray", tag);
                            httpHeaders.set("X-Env", tag);
                        })
                        .build();
                ServerWebExchange mutatedExchange = exchange.mutate().request(mutatedRequest).build();
                return chain.filter(mutatedExchange).doFinally(signalType -> GrayRequestContextHolder.clear());
            }

            return chain.filter(exchange).doFinally(signalType -> GrayRequestContextHolder.clear());
        }

        @Override
        public int getOrder() {
            return Ordered.HIGHEST_PRECEDENCE;
        }
    }

    /**
     * 灰度请求上下文持有器
     */
    public static class GrayRequestContextHolder {
        private static final ThreadLocal<String> TAG_HOLDER = new ThreadLocal<>();

        public static void setTag(String tag) {
            TAG_HOLDER.set(tag);
        }

        public static String getTag() {
            return TAG_HOLDER.get();
        }

        public static void clear() {
            TAG_HOLDER.remove();
        }
    }
}