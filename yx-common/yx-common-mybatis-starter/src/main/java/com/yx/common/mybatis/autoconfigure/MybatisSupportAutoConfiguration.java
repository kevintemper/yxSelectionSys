package com.yx.common.mybatis.autoconfigure;

import org.mybatis.spring.mapper.MapperScannerConfigurer;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;

@AutoConfiguration
@EnableConfigurationProperties(MybatisProperties.class)
public class MybatisSupportAutoConfiguration {

  @Bean
  @ConditionalOnProperty(prefix = "yx.mybatis", name = "base-packages")
  public MapperScannerConfigurer mapperScannerConfigurer(MybatisProperties properties) {
    MapperScannerConfigurer configurer = new MapperScannerConfigurer();
    configurer.setBasePackage(String.join(",", properties.getBasePackages()));
    return configurer;
  }
}
