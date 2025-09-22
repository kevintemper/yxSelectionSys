package com.yx.auth.controller;

import com.yx.auth.domain.MerchantUser;
import com.yx.auth.dto.CaptchaResponse;
import com.yx.auth.dto.LoginRequest;
import com.yx.auth.dto.LoginResponse;
import com.yx.auth.service.CaptchaService;
import com.yx.auth.service.JwtTokenService;
import com.yx.auth.service.MerchantUserService;
import com.yx.common.core.model.ApiResponse;
import jakarta.validation.Valid;
import java.time.Instant;
import java.util.Optional;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

  private static final String AUTH_COOKIE = "YX_AUTH_TOKEN";

  private final AuthenticationManager authenticationManager;
  private final MerchantUserService merchantUserService;
  private final JwtTokenService jwtTokenService;
  private final CaptchaService captchaService;

  public AuthController(AuthenticationManager authenticationManager, MerchantUserService merchantUserService,
      JwtTokenService jwtTokenService, CaptchaService captchaService) {
    this.authenticationManager = authenticationManager;
    this.merchantUserService = merchantUserService;
    this.jwtTokenService = jwtTokenService;
    this.captchaService = captchaService;
  }

  @PostMapping("/login")
  public ResponseEntity<ApiResponse<LoginResponse>> login(@Valid @RequestBody LoginRequest request) {
    if (!captchaService.validate(request.getCaptchaToken(), request.getCaptchaCode())) {
      return ResponseEntity.badRequest().body(ApiResponse.failure(org.springframework.http.HttpStatus.BAD_REQUEST, "验证码错误"));
    }
    Authentication authentication = authenticationManager.authenticate(
        new UsernamePasswordAuthenticationToken(request.getUsername(), request.getPassword()));
    SecurityContextHolder.getContext().setAuthentication(authentication);
    MerchantUser user = merchantUserService.findByUsername(request.getUsername())
        .orElseThrow(() -> new IllegalArgumentException("用户不存在"));
    String token = jwtTokenService.createToken(user, request.isRememberMe());
    Instant expiresAt = jwtTokenService.getExpiration(token);
    ResponseCookie cookie = ResponseCookie.from(AUTH_COOKIE, token)
        .httpOnly(true)
        .path("/")
        .maxAge(request.isRememberMe() ? 7 * 24 * 3600 : 12 * 3600)
        .build();
    return ResponseEntity.ok()
        .header(HttpHeaders.SET_COOKIE, cookie.toString())
        .body(ApiResponse.success(new LoginResponse(token, expiresAt, user)));
  }

  @GetMapping("/me")
  public ResponseEntity<ApiResponse<MerchantUser>> me() {
    Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
    if (authentication == null) {
      return ResponseEntity.status(401).body(ApiResponse.failure(org.springframework.http.HttpStatus.UNAUTHORIZED, "未登录"));
    }
    return merchantUserService.findByUsername(authentication.getName())
        .map(user -> ResponseEntity.ok(ApiResponse.success(user)))
        .orElseGet(() -> ResponseEntity.status(404)
            .body(ApiResponse.failure(org.springframework.http.HttpStatus.NOT_FOUND, "用户不存在")));
  }

  @PostMapping("/refresh")
  public ResponseEntity<ApiResponse<LoginResponse>> refresh(@RequestHeader(value = "Authorization", required = false) String authorization,
      @RequestHeader(value = "X-Auth-Token", required = false) String directToken) {
    String token = null;
    if (authorization != null && authorization.startsWith("Bearer ")) {
      token = authorization.substring(7);
    }
    if (token == null) {
      token = directToken;
    }
    if (token == null) {
      return ResponseEntity.badRequest().body(ApiResponse.failure(org.springframework.http.HttpStatus.BAD_REQUEST, "缺少token"));
    }
    Optional<String> refreshed = jwtTokenService.refreshToken(token);
    if (refreshed.isEmpty()) {
      return ResponseEntity.status(400).body(ApiResponse.failure(org.springframework.http.HttpStatus.BAD_REQUEST, "无需刷新"));
    }
    String newToken = refreshed.get();
    Instant expiresAt = jwtTokenService.getExpiration(newToken);
    boolean remember = jwtTokenService.isRememberMe(newToken);
    ResponseCookie cookie = ResponseCookie.from(AUTH_COOKIE, newToken)
        .httpOnly(true)
        .path("/")
        .maxAge(remember ? 7 * 24 * 3600 : 12 * 3600)
        .build();
    Optional<String> username = jwtTokenService.extractUsername(newToken);
    if (username.isEmpty()) {
      return ResponseEntity.status(400)
          .body(ApiResponse.failure(org.springframework.http.HttpStatus.BAD_REQUEST, "token不合法"));
    }
    Optional<MerchantUser> user = merchantUserService.findByUsername(username.get());
    if (user.isEmpty()) {
      return ResponseEntity.status(404)
          .body(ApiResponse.failure(org.springframework.http.HttpStatus.NOT_FOUND, "用户不存在"));
    }
    return ResponseEntity.ok()
        .header(HttpHeaders.SET_COOKIE, cookie.toString())
        .body(ApiResponse.success(new LoginResponse(newToken, expiresAt, user.get())));
  }

  @GetMapping("/captcha")
  public ApiResponse<CaptchaResponse> captcha() {
    return ApiResponse.success(captchaService.generateCaptcha());
  }
}
