package com.yx.auth.service;

import com.yx.auth.dto.CaptchaResponse;

public interface CaptchaService {

  CaptchaResponse generateCaptcha();

  boolean validate(String token, String code);
}
