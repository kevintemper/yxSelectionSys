package com.yx.common.log.autoconfigure;

import com.yx.common.log.filter.TraceIdFilter;
import jakarta.servlet.Filter;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;

@AutoConfiguration
public class LoggingAutoConfiguration {

  @Bean
  @ConditionalOnMissingBean
  public FilterRegistrationBean<Filter> traceIdFilterRegistration() {
    FilterRegistrationBean<Filter> registration = new FilterRegistrationBean<>();
    registration.setFilter(new TraceIdFilter());
    registration.addUrlPatterns("/*");
    registration.setOrder(0);
    return registration;
  }
}
