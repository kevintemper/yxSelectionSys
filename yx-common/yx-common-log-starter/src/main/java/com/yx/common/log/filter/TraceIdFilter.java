package com.yx.common.log.filter;

import com.yx.common.core.util.TraceUtils;
import jakarta.servlet.Filter;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import jakarta.servlet.http.HttpServletRequest;
import java.io.IOException;
import org.slf4j.MDC;

public class TraceIdFilter implements Filter {

  @Override
  public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
      throws IOException, ServletException {
    try {
      if (request instanceof HttpServletRequest httpServletRequest) {
        String traceId = httpServletRequest.getHeader("X-Trace-Id");
        if (traceId != null && !traceId.isBlank()) {
          MDC.put(TraceUtils.TRACE_ID_KEY, traceId);
        } else {
          TraceUtils.ensureTraceId();
        }
      }
      chain.doFilter(request, response);
    } finally {
      MDC.remove(TraceUtils.TRACE_ID_KEY);
    }
  }
}
