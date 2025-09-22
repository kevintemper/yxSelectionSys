package com.yx.common.core.autoconfigure;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.yx.common.core.web.GlobalExceptionHandler;
import com.yx.common.core.web.GlobalResponseAdvice;
import java.time.Clock;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;
import org.springframework.context.annotation.Bean;

@AutoConfiguration
@ConfigurationPropertiesScan(basePackages = "com.yx")
public class CommonAutoConfiguration {

  @Bean
  @ConditionalOnMissingBean
  public Clock systemClock() {
    return Clock.systemUTC();
  }

  @Bean
  @ConditionalOnMissingBean
  public ObjectMapper objectMapper() {
    ObjectMapper objectMapper = new ObjectMapper();
    objectMapper.registerModule(new JavaTimeModule());
    objectMapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
    return objectMapper;
  }

  @Bean
  public GlobalExceptionHandler globalExceptionHandler() {
    return new GlobalExceptionHandler();
  }

  @Bean
  public GlobalResponseAdvice globalResponseAdvice() {
    return new GlobalResponseAdvice();
  }
}
