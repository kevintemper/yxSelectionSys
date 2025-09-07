package com.yx.gateway;

import org.springframework.cloud.gateway.filter.GatewayFilter;
import org.springframework.cloud.gateway.filter.factory.AbstractGatewayFilterFactory;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.concurrent.ThreadLocalRandom;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Component
public class GrayRouteGatewayFilterFactory extends AbstractGatewayFilterFactory<GrayRouteGatewayFilterFactory.Config> {

    public GrayRouteGatewayFilterFactory() {
        super(Config.class);
    }

    /** 配置：percentage=10 表示 10% 灰度；grayUsers=1001,1002 表示命中这些用户必走 gray */
    public static class Config {
        private int percentage = 0;
        private List<String> grayUsers;

        public int getPercentage() { return percentage; }
        public void setPercentage(int p) { this.percentage = p; }

        public List<String> getGrayUsers() { return grayUsers; }
        public void setGrayUsers(String usersCsv) {
            if (usersCsv == null || usersCsv.isBlank()) this.grayUsers = List.of();
            else this.grayUsers = Stream.of(usersCsv.split(","))
                    .map(String::trim).filter(s->!s.isEmpty()).collect(Collectors.toList());
        }
    }

    @Override
    public List<String> shortcutFieldOrder() {
        return List.of("percentage","grayUsers"); // 允许 GrayRoute=10,1001,1002 这种写法
    }

    @Override
    public GatewayFilter apply(Config config) {
        return (exchange, chain) -> {
            String uid = exchange.getRequest().getHeaders().getFirst("X-User-Id"); // 你的 JwtVerify 已经加了
            boolean forceGray = uid != null && config.getGrayUsers()!=null && config.getGrayUsers().contains(uid);

            String env = "prd";
            if (forceGray) {
                env = "gray";
            } else {
                int p = Math.max(0, Math.min(config.getPercentage(), 100));
                int dice = ThreadLocalRandom.current().nextInt(100); // 0..99
                if (dice < p) env = "gray";
            }

            ServerHttpRequest mutated = exchange.getRequest().mutate()
                    .header("X-Route-Env", env)
                    .build();
            return chain.filter(exchange.mutate().request(mutated).build());
        };
    }
}

