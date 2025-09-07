package com.yx.auth.api;

import com.google.code.kaptcha.Producer;
import com.yx.auth.api.dto.*;
import com.yx.auth.service.AuthService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.http.MediaType;
import org.springframework.util.FastByteArrayOutputStream;
import org.springframework.web.bind.annotation.*;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.time.Duration;
import java.util.Base64;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final Producer kaptchaProducer;
    private final StringRedisTemplate redis;
    private final AuthService auth;

    @GetMapping("/captcha")
    public Map<String, String> captcha() throws IOException {
        String id = UUID.randomUUID().toString().replace("-", "");
        String text = kaptchaProducer.createText();
        BufferedImage img = kaptchaProducer.createImage(text);

        FastByteArrayOutputStream os = new FastByteArrayOutputStream();
        ImageIO.write(img, "png", os);
        String b64 = Base64.getEncoder().encodeToString(os.toByteArray());

        redis.opsForValue().set("captcha:"+id, text, Duration.ofMinutes(2));

        Map<String, String> ret = new HashMap<>();
        ret.put("captchaId", id);
        ret.put("imageBase64", "data:image/png;base64," + b64);
        System.out.println(ret);  // 添加打印日志查看返回的数据
        return ret;

    }

    @PostMapping("/login")
    public TokenResp login(@RequestBody LoginReq req) {
        return auth.login(req);
    }

    @PostMapping("/refresh")
    public TokenResp refresh(@RequestBody RefreshReq req) {
        return auth.refresh(req);
    }

    @PostMapping("/register")
    public RegisterResp register(@RequestBody RegisterReq req) {
        return auth.register(req);
    }

    // 读取 nacos 元数据里的 env（灰度/生产），没有就 unknown
    @Value("${spring.cloud.nacos.discovery.metadata.env:unknown}")
    private String env;

    @GetMapping("/api/auth/hello")
    public Map<String, Object> hello() {
        return Map.of(
                "msg", "ok",
                "from", "yx-auth",
                "env", env
        );
    }

}
