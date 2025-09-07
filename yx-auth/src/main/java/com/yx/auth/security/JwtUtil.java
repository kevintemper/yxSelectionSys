package com.yx.auth.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.Date;
import java.util.Map;

@Component
public class JwtUtil {

    private final SecretKey key;
    private final long accessTtlMillis;
    private final long refreshTtlMillis;

    public JwtUtil(
            @Value("${yx.jwt.secret}") String secret,
            @Value("${yx.jwt.access-ttl-minutes:30}") long accessTtlMinutes,
            @Value("${yx.jwt.refresh-ttl-days:7}") long refreshTtlDays
    ) {
        // make deterministic key from secret
        byte[] bytes = Base64.getEncoder().encode(secret.getBytes(StandardCharsets.UTF_8));
        this.key = Keys.hmacShaKeyFor(bytes);
        this.accessTtlMillis = accessTtlMinutes * 60_000L;
        this.refreshTtlMillis = refreshTtlDays * 24 * 3600_000L;
    }

    public String generateAccessToken(String username, Map<String,Object> claims) {
        long now = System.currentTimeMillis();
        return Jwts.builder()
                .setSubject(username)
                .addClaims(claims)
                .setIssuedAt(new Date(now))
                .setExpiration(new Date(now + accessTtlMillis))
                .signWith(key, SignatureAlgorithm.HS256)
                .compact();
    }

    public String generateRefreshToken(String username, Map<String,Object> claims) {
        long now = System.currentTimeMillis();
        return Jwts.builder()
                .setSubject(username)
                .addClaims(claims)
                .setIssuedAt(new Date(now))
                .setExpiration(new Date(now + refreshTtlMillis))
                .signWith(key, SignatureAlgorithm.HS256)
                .compact();
    }

    public boolean validate(String token) {
        try {
            parseAllClaims(token);
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    public boolean shouldRenew(String refreshToken) {
        Claims c = parseAllClaims(refreshToken);
        long now = System.currentTimeMillis();
        long exp = c.getExpiration().getTime();
        // renew if less than 10 minutes remaining
        return (exp - now) < 10 * 60_000L;
    }

    public String getUsername(String token) {
        return parseAllClaims(token).getSubject();
    }

    public Map<String,Object> getClaims(String token) {
        return parseAllClaims(token);
    }

    private Claims parseAllClaims(String token) {
        return Jwts.parserBuilder()
                .setSigningKey(key)
                .build()
                .parseClaimsJws(token)
                .getBody();
    }
}
