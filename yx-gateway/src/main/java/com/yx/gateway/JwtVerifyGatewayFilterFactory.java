package com.yx.gateway;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.gateway.filter.GatewayFilter;
import org.springframework.cloud.gateway.filter.factory.AbstractGatewayFilterFactory;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Set;

/**
 * JWT 校验过滤器（网关）
 */
@Component
public class JwtVerifyGatewayFilterFactory
        extends AbstractGatewayFilterFactory<JwtVerifyGatewayFilterFactory.Config> {

 // 从配置读取密钥，默认给一个值，和 yx-auth 中保持一致
 @Value("${yx.jwt.secret:yx@secret!}")
 private String secret;

 // 免鉴权白名单
 private static final Set<String> WHITE_LIST = Set.of(
         "/api/auth/login", "/api/auth/captcha", "/api/auth/refresh",
         "/v3/api-docs", "/swagger-ui", "/swagger-ui.html",
         "/actuator/health"
 );

 private static final String TOKEN_HEADER = "Authorization";
 private static final String TOKEN_PREFIX = "Bearer ";

 public JwtVerifyGatewayFilterFactory() {
  super(Config.class);
 }

 @Override
 public GatewayFilter apply(Config config) {
  return (exchange, chain) -> {
   final String path = exchange.getRequest().getURI().getPath();
   if (isWhite(path)) {
    return chain.filter(exchange);
   }

   final List<String> hs = exchange.getRequest().getHeaders().getOrEmpty(TOKEN_HEADER);
   if (hs.isEmpty() || hs.get(0) == null || !hs.get(0).startsWith(TOKEN_PREFIX)) {
    return unAuth(exchange, "NO_TOKEN");
   }

   final String token = hs.get(0).substring(TOKEN_PREFIX.length()).trim();
   final Claims claims;
   try {
    claims = Jwts.parser()
            .setSigningKey(secret.getBytes(StandardCharsets.UTF_8))
            .parseClaimsJws(token)
            .getBody();
   } catch (Exception e) {
    return unAuth(exchange, "INVALID_TOKEN");
   }

   // 需要某些资源权限时可以在这里检查（示例：要求 product:create）
   // Object scopes = claims.get("scopes");
   // if (!(scopes instanceof List) || !((List<?>) scopes).contains("product:create")) {
   //     return forbid(exchange, "NO_PERMISSION");
   // }

   // 透传用户信息到下游
   String uid = String.valueOf(claims.get("uid"));
   var mutated = exchange.getRequest().mutate()
           .header("X-User-Id", uid)
           .build();

   return chain.filter(exchange.mutate().request(mutated).build());
  };
 }

 private boolean isWhite(String path) {
  for (String w : WHITE_LIST) {
   if (path.startsWith(w)) return true;
  }
  return false;
 }

 private Mono<Void> unAuth(ServerWebExchange ex, String msg) {
  return jsonWrite(ex, HttpStatus.UNAUTHORIZED, msg);
 }

 private Mono<Void> forbid(ServerWebExchange ex, String msg) {
  return jsonWrite(ex, HttpStatus.FORBIDDEN, msg);
 }

 private Mono<Void> jsonWrite(ServerWebExchange ex, HttpStatus status, String msg) {
  var res = ex.getResponse();
  res.setStatusCode(status);
  res.getHeaders().set(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE);
  String json = "{\"error\":\"" + msg + "\"}";
  byte[] bytes = json.getBytes(StandardCharsets.UTF_8);
  return res.writeWith(Mono.just(res.bufferFactory().wrap(bytes)));
 }

 public static class Config { }
}
