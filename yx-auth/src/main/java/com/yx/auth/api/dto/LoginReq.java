package com.yx.auth.api.dto;

import jakarta.validation.constraints.NotBlank;

public record LoginReq(
        @NotBlank String username,
        @NotBlank String password,
        @NotBlank String captchaId,
        @NotBlank String captchaCode,
        boolean rememberMe
) {}
