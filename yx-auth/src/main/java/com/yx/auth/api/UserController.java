package com.yx.auth.api;
import com.yx.auth.repo.*; import org.springframework.security.core.Authentication; import org.springframework.web.bind.annotation.*; import java.util.Map;
@RestController
@RequestMapping("/api/user")
public class UserController {
 @GetMapping("/profile")
 public Map<String, Object> profile(Authentication auth) {
  return Map.of("username", auth.getName(), "authorities", auth.getAuthorities());
 }
}
