package com.yx.auth.dto;

import com.yx.auth.domain.MerchantUser;
import java.time.Instant;

public class LoginResponse {

  private String token;
  private Instant expiresAt;
  private MerchantUser user;

  public LoginResponse(String token, Instant expiresAt, MerchantUser user) {
    this.token = token;
    this.expiresAt = expiresAt;
    this.user = user;
  }

  public String getToken() {
    return token;
  }

  public Instant getExpiresAt() {
    return expiresAt;
  }

  public MerchantUser getUser() {
    return user;
  }
}
