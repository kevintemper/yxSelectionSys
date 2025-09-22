package com.yx.common.core.model;

import java.io.Serial;
import java.io.Serializable;
import java.time.Instant;

import org.springframework.http.HttpStatus;

/**
 * Standard API response wrapper used across services.
 */
public record ApiResponse<T>(boolean success, String message, T data, Instant timestamp) implements Serializable {
  @Serial
  private static final long serialVersionUID = 1L;

  public static <T> ApiResponse<T> success(T data) {
    return new ApiResponse<>(true, HttpStatus.OK.getReasonPhrase(), data, Instant.now());
  }

  public static <T> ApiResponse<T> success(String message, T data) {
    return new ApiResponse<>(true, message, data, Instant.now());
  }

  public static <T> ApiResponse<T> failure(HttpStatus status, String message) {
    return new ApiResponse<>(false, message == null ? status.getReasonPhrase() : message, null, Instant.now());
  }

  public static <T> ApiResponse<T> failure(HttpStatus status, String message, T data) {
    return new ApiResponse<>(false, message == null ? status.getReasonPhrase() : message, data, Instant.now());
  }
}
