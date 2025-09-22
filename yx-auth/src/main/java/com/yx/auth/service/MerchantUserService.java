package com.yx.auth.service;

import com.yx.auth.domain.MerchantUser;
import com.yx.auth.domain.Role;
import com.yx.auth.domain.enums.UserStatus;
import com.yx.auth.dto.MerchantUserRequest;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

public interface MerchantUserService {

  MerchantUser create(MerchantUserRequest request);

  MerchantUser update(MerchantUserRequest request);

  void disable(UUID userId);

  void enable(UUID userId);

  Optional<MerchantUser> findByUsername(String username);

  List<MerchantUser> list();

  Set<Role> listRoles();

  Role createRole(String code, String name);

  void assignRoles(UUID userId, Set<UUID> roleIds);

  void updateStatus(UUID userId, UserStatus status);
}
