package com.yx.auth.service;

import com.yx.auth.domain.MerchantUser;
import java.time.Instant;
import java.util.Optional;

public interface JwtTokenService {

  String createToken(MerchantUser user, boolean rememberMe);

  Instant getExpiration(String token);

  Optional<String> refreshToken(String token);

  Optional<String> extractUsername(String token);

  boolean isRememberMe(String token);
}
