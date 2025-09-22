package com.yx.auth.dto;

public class CaptchaResponse {

  private final String token;
  private final String base64Image;

  public CaptchaResponse(String token, String base64Image) {
    this.token = token;
    this.base64Image = base64Image;
  }

  public String getToken() {
    return token;
  }

  public String getBase64Image() {
    return base64Image;
  }
}
