package com.yx.common.mybatis.autoconfigure;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "yx.mybatis")
public class MybatisProperties {

  /**
   * Base packages to scan for mapper interfaces.
   */
  private String[] basePackages = new String[0];

  public String[] getBasePackages() {
    return basePackages;
  }

  public void setBasePackages(String[] basePackages) {
    this.basePackages = basePackages;
  }
}
