package com.yx.common.core.util;

import java.util.Optional;
import java.util.UUID;

import org.slf4j.MDC;

public final class TraceUtils {
  public static final String TRACE_ID_KEY = "traceId";

  private TraceUtils() {
  }

  public static String ensureTraceId() {
    String traceId = MDC.get(TRACE_ID_KEY);
    if (traceId == null || traceId.isBlank()) {
      traceId = UUID.randomUUID().toString();
      MDC.put(TRACE_ID_KEY, traceId);
    }
    return traceId;
  }

  public static Optional<String> getTraceId() {
    return Optional.ofNullable(MDC.get(TRACE_ID_KEY));
  }
}
