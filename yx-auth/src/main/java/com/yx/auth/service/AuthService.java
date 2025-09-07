package com.yx.auth.service;

import com.yx.auth.api.dto.*;
import com.yx.auth.domain.Role;
import com.yx.auth.domain.User;
import com.yx.auth.domain.UserRole;
import com.yx.auth.repo.RoleRepo;
import com.yx.auth.repo.UserRepo;
import com.yx.auth.repo.UserRoleRepo;
import com.yx.auth.security.JwtUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.time.Instant;
import java.util.*;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepo users;
    private final RoleRepo roles;
    private final UserRoleRepo userRoleRepo;
    private final PasswordEncoder pe;
    private final JwtUtil jwt;
    private final StringRedisTemplate redis;

    public TokenResp login(LoginReq req) {
        // 1) 验证码
        checkCaptcha(req.captchaId(), req.captchaCode());

        // 2) 用户名/密码
        User u = users.findByUsername(req.username())
                .orElseThrow(() -> new RuntimeException("用户名或密码错误"));
        if (u.getStatus() != null && u.getStatus() == 0) {
            throw new RuntimeException("账号已禁用");
        }
        if (!pe.matches(req.password(), u.getPassword())) {
            throw new RuntimeException("用户名或密码错误");
        }

        // 3) 生成token
        Map<String,Object> claims = new HashMap<>();
        claims.put("uid", u.getId());
        claims.put("ut", Optional.ofNullable(u.getUserType()).orElse(0));

        String access = jwt.generateAccessToken(u.getUsername(), claims);
        String refresh = jwt.generateRefreshToken(u.getUsername(), claims);

        return new TokenResp(access, refresh);
    }

    public TokenResp refresh(RefreshReq req) {
        String refresh = req.refreshToken();
        if (refresh == null || refresh.isBlank()) {
            throw new RuntimeException("refreshToken不能为空");
        }
        if (!jwt.validate(refresh)) {
            throw new RuntimeException("refreshToken无效");
        }
        String username = jwt.getUsername(refresh);
        Map<String, Object> claims = jwt.getClaims(refresh);
        String access = jwt.generateAccessToken(username, claims);
        String newRefresh = jwt.shouldRenew(refresh) ? jwt.generateRefreshToken(username, claims) : refresh;
        return new TokenResp(access, newRefresh);
    }

    @Transactional
    public RegisterResp register(RegisterReq req) {
        // 1) 验证码
        checkCaptcha(req.captchaId(), req.captchaCode());

        // 2) 唯一性
        users.findByUsername(req.username()).ifPresent(x -> { throw new RuntimeException("用户名已存在"); });

        // 3) 保存用户
        Instant now = Instant.now();
        User u = new User();
        u.setUsername(req.username());
        u.setPassword(pe.encode(req.password()));
        u.setNickname(req.username());
        u.setUserType(Optional.ofNullable(req.userType()).orElse(0));
        u.setPhone(req.phone());
        u.setEmail(req.email());
        u.setStatus(1);
        u.setCreatedAt(now);
        u.setUpdatedAt(now);
        users.save(u);

        // 4) 绑定默认角色
        String code = (req.userType() != null && req.userType() == 1) ? "MERCHANT_ADMIN" : "C_USER";
        Role role = roles.findByCode(code).orElseGet(() -> {
            Role r = new Role();
            r.setCode(code);
            r.setName(code.equals("MERCHANT_ADMIN") ? "商家管理员" : "C端用户");
            return roles.save(r);
        });
        userRoleRepo.save(new UserRole(u.getId(), role.getId()));

        return new RegisterResp(u.getId(), u.getUsername());
    }

    private void checkCaptcha(String captchaId, String captchaCode) {
        if (captchaId == null || captchaCode == null) {
            throw new RuntimeException("验证码不能为空");
        }
        String key = "captcha:" + captchaId;
        String expect = redis.opsForValue().get(key);
        if (expect == null) {
            throw new RuntimeException("验证码已过期");
        }
        if (!expect.equalsIgnoreCase(captchaCode)) {
            throw new RuntimeException("验证码错误");
        }
        // 单次使用
        redis.delete(key);
    }
}
