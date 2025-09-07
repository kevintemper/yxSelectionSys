package com.yx.auth.config;

import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class EnvConfig {

    @Value("${server.port}")
    private int port;

    @Value("${ENVIRONMENT:default}")
    private String environment;

    @PostConstruct
    public void setEnvironment() {
        if (port == 9101) {
            environment = "gray";
        } else if (port == 9103) {
            environment = "prd";
        }

        System.setProperty("ENVIRONMENT", environment); // 动态设置环境变量
        System.out.println("Current environment: " + environment);
    }
}

