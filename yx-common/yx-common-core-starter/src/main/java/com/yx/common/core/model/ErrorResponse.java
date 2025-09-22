package com.yx.common.core.model;

import java.io.Serial;
import java.io.Serializable;
import java.time.Instant;

public record ErrorResponse(String error, String message, Instant timestamp, String traceId) implements Serializable {
  @Serial
  private static final long serialVersionUID = 1L;

  public static ErrorResponse of(String error, String message, String traceId) {
    return new ErrorResponse(error, message, Instant.now(), traceId);
  }
}
