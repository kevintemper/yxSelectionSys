package com.yx.auth.api.dto;

import jakarta.validation.constraints.*;

public record RegisterReq(
        @NotBlank String username,
        @NotBlank String password,
        @NotBlank String captchaId,
        @NotBlank String captchaCode,
        @NotNull  Integer userType, // 0=C端，1=商家
        String phone,
        String email
) {}
