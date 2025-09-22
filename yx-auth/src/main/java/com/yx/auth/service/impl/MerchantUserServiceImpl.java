package com.yx.auth.service.impl;

import com.yx.auth.domain.MerchantUser;
import com.yx.auth.domain.Role;
import com.yx.auth.domain.enums.UserStatus;
import com.yx.auth.dto.MerchantUserRequest;
import com.yx.auth.service.MerchantUserService;
import java.time.Clock;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class MerchantUserServiceImpl implements MerchantUserService {

  private final PasswordEncoder passwordEncoder;
  private final Clock clock;
  private final Map<UUID, MerchantUser> users = new ConcurrentHashMap<>();
  private final Map<UUID, Role> roles = new ConcurrentHashMap<>();

  public MerchantUserServiceImpl(PasswordEncoder passwordEncoder, Clock clock) {
    this.passwordEncoder = passwordEncoder;
    this.clock = clock;
    bootstrap();
  }

  private void bootstrap() {
    Role adminRole = createRole("ADMIN", "超级管理员");
    Role operatorRole = createRole("OPERATOR", "运营专员");
    MerchantUser admin = new MerchantUser(UUID.randomUUID(), "admin");
    admin.setDisplayName("平台管理员");
    admin.setEmail("admin@yxs.com");
    admin.setPasswordHash(passwordEncoder.encode("ChangeMe123!"));
    admin.getRoles().add(adminRole);
    admin.getRoles().add(operatorRole);
    Instant now = clock.instant();
    admin.setCreatedAt(now);
    admin.setUpdatedAt(now);
    users.put(admin.getId(), admin);
  }

  @Override
  public MerchantUser create(MerchantUserRequest request) {
    validateUsernameUnique(request.getUsername(), null);
    MerchantUser user = new MerchantUser(UUID.randomUUID(), request.getUsername());
    user.setDisplayName(request.getDisplayName());
    user.setEmail(request.getEmail());
    user.setPhone(request.getPhone());
    if (request.getPassword() != null) {
      user.setPasswordHash(passwordEncoder.encode(request.getPassword()));
    }
    assignRolesInternal(user, request.getRoleIds());
    Instant now = clock.instant();
    user.setCreatedAt(now);
    user.setUpdatedAt(now);
    users.put(user.getId(), user);
    return user;
  }

  @Override
  public MerchantUser update(MerchantUserRequest request) {
    if (request.getId() == null) {
      throw new IllegalArgumentException("用户ID不能为空");
    }
    MerchantUser existing = users.get(request.getId());
    if (existing == null) {
      throw new IllegalArgumentException("用户不存在");
    }
    validateUsernameUnique(request.getUsername(), existing.getId());
    existing.setUsername(request.getUsername());
    existing.setDisplayName(request.getDisplayName());
    existing.setEmail(request.getEmail());
    existing.setPhone(request.getPhone());
    if (request.getPassword() != null && !request.getPassword().isBlank()) {
      existing.setPasswordHash(passwordEncoder.encode(request.getPassword()));
    }
    assignRolesInternal(existing, request.getRoleIds());
    existing.setUpdatedAt(clock.instant());
    return existing;
  }

  @Override
  public void disable(UUID userId) {
    MerchantUser user = requireUser(userId);
    user.setStatus(UserStatus.DISABLED);
    user.setUpdatedAt(clock.instant());
  }

  @Override
  public void enable(UUID userId) {
    MerchantUser user = requireUser(userId);
    user.setStatus(UserStatus.ENABLED);
    user.setUpdatedAt(clock.instant());
  }

  @Override
  public Optional<MerchantUser> findByUsername(String username) {
    return users.values().stream()
        .filter(user -> user.getUsername().equalsIgnoreCase(username))
        .findFirst();
  }

  @Override
  public List<MerchantUser> list() {
    return new ArrayList<>(users.values());
  }

  @Override
  public Set<Role> listRoles() {
    return Set.copyOf(roles.values());
  }

  @Override
  public Role createRole(String code, String name) {
    Role role = new Role(UUID.randomUUID(), code, name);
    roles.put(role.getId(), role);
    return role;
  }

  @Override
  public void assignRoles(UUID userId, Set<UUID> roleIds) {
    MerchantUser user = requireUser(userId);
    assignRolesInternal(user, roleIds);
  }

  @Override
  public void updateStatus(UUID userId, UserStatus status) {
    MerchantUser user = requireUser(userId);
    user.setStatus(status);
    user.setUpdatedAt(clock.instant());
  }

  private void validateUsernameUnique(String username, UUID ignoreId) {
    if (username == null) {
      return;
    }
    boolean exists = users.values().stream()
        .anyMatch(user -> user.getUsername().equalsIgnoreCase(username)
            && (ignoreId == null || !user.getId().equals(ignoreId)));
    if (exists) {
      throw new IllegalArgumentException("用户名已存在");
    }
  }

  private void assignRolesInternal(MerchantUser user, Set<UUID> roleIds) {
    Set<Role> roleSet;
    if (roleIds == null || roleIds.isEmpty()) {
      roleSet = roles.values().stream()
          .filter(role -> "OPERATOR".equalsIgnoreCase(role.getCode()))
          .collect(Collectors.toSet());
    } else {
      roleSet = roleIds.stream()
          .map(roles::get)
          .filter(role -> role != null)
          .collect(Collectors.toSet());
    }
    user.setRoles(roleSet);
  }

  private MerchantUser requireUser(UUID userId) {
    MerchantUser user = users.get(userId);
    if (user == null) {
      throw new IllegalArgumentException("用户不存在");
    }
    return user;
  }
}
