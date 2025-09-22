package com.yx.auth.config;

import com.google.code.kaptcha.Producer;
import com.google.code.kaptcha.impl.DefaultKaptcha;
import com.google.code.kaptcha.util.Config;
import java.util.Properties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class AuthApplicationConfig {

  @Bean
  public Producer kaptchaProducer() {
    Properties properties = new Properties();
    properties.put("kaptcha.border", "no");
    properties.put("kaptcha.textproducer.font.color", "black");
    properties.put("kaptcha.textproducer.char.space", "4");
    Config config = new Config(properties);
    DefaultKaptcha kaptcha = new DefaultKaptcha();
    kaptcha.setConfig(config);
    return kaptcha;
  }
}
