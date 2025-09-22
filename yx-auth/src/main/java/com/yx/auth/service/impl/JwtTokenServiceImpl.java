package com.yx.auth.service.impl;

import com.yx.auth.domain.MerchantUser;
import com.yx.auth.service.JwtTokenService;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import java.security.Key;
import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.util.Date;
import java.util.Optional;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
public class JwtTokenServiceImpl implements JwtTokenService {

  private final Clock clock;
  private final Key signingKey;
  private final Duration rememberMeDuration;
  private final Duration sessionDuration;

  public JwtTokenServiceImpl(Clock clock,
      @Value("${yx.security.jwt.secret:YWxwaGEteHgtc2VjcmV0LWtleS0xMjM0NTY=}") String secret,
      @Value("${yx.security.jwt.remember-days:7}") long rememberDays,
      @Value("${yx.security.jwt.session-hours:12}") long sessionHours) {
    this.clock = clock;
    this.signingKey = Keys.hmacShaKeyFor(Decoders.BASE64.decode(secret));
    this.rememberMeDuration = Duration.ofDays(rememberDays);
    this.sessionDuration = Duration.ofHours(sessionHours);
  }

  @Override
  public String createToken(MerchantUser user, boolean rememberMe) {
    Instant now = clock.instant();
    Duration duration = rememberMe ? rememberMeDuration : sessionDuration;
    Instant expiry = now.plus(duration);
    return Jwts.builder()
        .setSubject(user.getUsername())
        .setIssuedAt(Date.from(now))
        .setExpiration(Date.from(expiry))
        .claim("remember", rememberMe)
        .signWith(signingKey, SignatureAlgorithm.HS256)
        .compact();
  }

  @Override
  public Instant getExpiration(String token) {
    return parseClaims(token).getExpiration().toInstant();
  }

  @Override
  public Optional<String> refreshToken(String token) {
    try {
      Claims claims = parseClaims(token);
      Instant expiration = claims.getExpiration().toInstant();
      Instant now = clock.instant();
      if (Duration.between(now, expiration).compareTo(Duration.ofHours(1)) <= 0) {
        String username = claims.getSubject();
        boolean remember = Boolean.TRUE.equals(claims.get("remember", Boolean.class));
        Duration extend = remember ? rememberMeDuration : sessionDuration;
        Instant newExpiration = now.plus(extend);
        String refreshed = Jwts.builder()
            .setSubject(username)
            .setIssuedAt(Date.from(now))
            .setExpiration(Date.from(newExpiration))
            .claim("remember", remember)
            .signWith(signingKey, SignatureAlgorithm.HS256)
            .compact();
        return Optional.of(refreshed);
      }
      return Optional.empty();
    } catch (Exception ex) {
      return Optional.empty();
    }
  }

  @Override
  public Optional<String> extractUsername(String token) {
    try {
      return Optional.of(parseClaims(token).getSubject());
    } catch (Exception ex) {
      return Optional.empty();
    }
  }

  @Override
  public boolean isRememberMe(String token) {
    try {
      Claims claims = parseClaims(token);
      return Boolean.TRUE.equals(claims.get("remember", Boolean.class));
    } catch (Exception ex) {
      return false;
    }
  }

  private Claims parseClaims(String token) {
    return Jwts.parserBuilder()
        .setSigningKey(signingKey)
        .build()
        .parseClaimsJws(token)
        .getBody();
  }
}
