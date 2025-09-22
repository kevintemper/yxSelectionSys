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
import org.springframework.stereotype.Component;
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
        String grayHeader = exchange.getRequest().getHeaders().getFirst("X-Gray");
        if (grayHeader != null) {
          GrayRequestContextHolder.setTag(grayHeader);
          exchange.getRequest().mutate().header("SERVICE-TAG", grayHeader).build();
        }
        return chain.filter(exchange).doFinally(signalType -> GrayRequestContextHolder.clear());
      };
    }

    @Override
    public int getOrder() {
      return -1;
    }

    public static class Config {
    }
  }

  @Component
  public static class SouthNorthGlobalFilter implements GlobalFilter, Ordered {

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
      HttpHeaders headers = exchange.getRequest().getHeaders();
      String userId = headers.getFirst("X-User-Id");
      if (userId != null && userId.endsWith("gray")) {
        GrayRequestContextHolder.setTag("gray");
        exchange.getRequest().mutate().header("SERVICE-TAG", "gray").build();
      }
      return chain.filter(exchange).doFinally(signalType -> GrayRequestContextHolder.clear());
    }

    @Override
    public int getOrder() {
      return Ordered.HIGHEST_PRECEDENCE;
    }
  }
}
