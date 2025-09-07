package com.yx.auth;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/api/auth")
public class PingController {

    // 通过nacos获取env环境变量值
    @Value("${spring.cloud.nacos.discovery.metadata.env:unknown}")
    private String env;

    @GetMapping("/hello")
    public Map<String, Object> hello() {
        return Map.of(
                "msg", "ok",
                "from", "yx-auth",
                "env", env
        );
    }
}
