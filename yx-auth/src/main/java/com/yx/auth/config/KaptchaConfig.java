package com.yx.auth.config;

import com.google.code.kaptcha.impl.DefaultKaptcha;
import com.google.code.kaptcha.util.Config;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.Properties;

@Configuration
public class KaptchaConfig {
    @Bean
    public DefaultKaptcha defaultKaptcha() {
        Properties p = new Properties();
        p.setProperty("kaptcha.border", "no");
        p.setProperty("kaptcha.char.length", "4");
        p.setProperty("kaptcha.image.width", "120");
        p.setProperty("kaptcha.image.height", "40");
        DefaultKaptcha k = new DefaultKaptcha();
        k.setConfig(new Config(p));
        return k;
    }
}
