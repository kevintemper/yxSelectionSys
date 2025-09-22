package com.yx.auth.service.impl;

import com.google.code.kaptcha.Producer;
import com.yx.auth.dto.CaptchaResponse;
import com.yx.auth.service.CaptchaService;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.time.Duration;
import java.time.Instant;
import java.util.Base64;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import javax.imageio.ImageIO;
import org.springframework.stereotype.Service;

@Service
public class CaptchaServiceImpl implements CaptchaService {

  private final Producer captchaProducer;
  private final Map<String, CaptchaItem> captchaStorage = new ConcurrentHashMap<>();
  private final Duration ttl = Duration.ofMinutes(5);

  public CaptchaServiceImpl(Producer captchaProducer) {
    this.captchaProducer = captchaProducer;
  }

  @Override
  public CaptchaResponse generateCaptcha() {
    String text = captchaProducer.createText();
    String token = UUID.randomUUID().toString();
    captchaStorage.put(token, new CaptchaItem(text.toLowerCase(), Instant.now().plus(ttl)));
    ByteArrayOutputStream output = new ByteArrayOutputStream();
    try {
      ImageIO.write(captchaProducer.createImage(text), "png", output);
    } catch (IOException e) {
      throw new IllegalStateException("无法生成验证码", e);
    }
    String base64 = Base64.getEncoder().encodeToString(output.toByteArray());
    return new CaptchaResponse(token, "data:image/png;base64," + base64);
  }

  @Override
  public boolean validate(String token, String code) {
    if (token == null || code == null) {
      return false;
    }
    CaptchaItem item = captchaStorage.remove(token);
    if (item == null) {
      return false;
    }
    if (Instant.now().isAfter(item.expireAt())) {
      return false;
    }
    return item.value().equals(code.toLowerCase());
  }

  private record CaptchaItem(String value, Instant expireAt) {
  }
}
