package com.yx.auth.security;

import com.yx.auth.repo.UserRepo;
import com.yx.auth.repo.UserRoleRepo;
import io.jsonwebtoken.Claims;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
public class JwtAuthFilter extends OncePerRequestFilter {

    private final JwtUtil jwt;
    private final UserRepo users;
    private final UserRoleRepo userRoleRepo;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {

        String header = request.getHeader("Authorization");
        if (header != null && header.startsWith("Bearer ")) {
            String token = header.substring(7);
            if (jwt.validate(token)) {
                Claims claims = (Claims) jwt.getClaims(token);
                Object uidObj = claims.get("uid");
                Long uid = null;
                if (uidObj instanceof Number) {
                    uid = ((Number) uidObj).longValue();
                } else if (uidObj instanceof String s) {
                    try { uid = Long.parseLong(s); } catch (Exception ignored) {}
                }
                String username = jwt.getUsername(token);
                if (uid != null && username != null) {
                    List<String> roles = userRoleRepo.findRoleCodesByUserId(uid);
                    var authorities = roles.stream().map(SimpleGrantedAuthority::new).collect(Collectors.toList());
                    var auth = new UsernamePasswordAuthenticationToken(username, null, authorities);
                    SecurityContextHolder.getContext().setAuthentication(auth);
                }
            }
        }
        filterChain.doFilter(request, response);
    }
}
