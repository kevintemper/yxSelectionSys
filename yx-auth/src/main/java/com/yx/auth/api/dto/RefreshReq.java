package com.yx.auth.api.dto;

import jakarta.validation.constraints.NotBlank;

public record RefreshReq(@NotBlank String refreshToken) {}
