package com.yx.auth.controller;

import com.yx.auth.domain.MerchantUser;
import com.yx.auth.domain.Role;
import com.yx.auth.domain.enums.UserStatus;
import com.yx.auth.dto.MerchantUserRequest;
import com.yx.auth.service.MerchantUserService;
import com.yx.common.core.model.ApiResponse;
import jakarta.validation.Valid;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/merchant/users")
public class MerchantUserController {

  private final MerchantUserService merchantUserService;

  public MerchantUserController(MerchantUserService merchantUserService) {
    this.merchantUserService = merchantUserService;
  }

  @GetMapping
  @PreAuthorize("hasRole('ADMIN')")
  public ApiResponse<List<MerchantUser>> list() {
    return ApiResponse.success(merchantUserService.list());
  }

  @PostMapping
  @PreAuthorize("hasRole('ADMIN')")
  public ApiResponse<MerchantUser> create(@Valid @RequestBody MerchantUserRequest request) {
    return ApiResponse.success(merchantUserService.create(request));
  }

  @PutMapping
  @PreAuthorize("hasRole('ADMIN')")
  public ApiResponse<MerchantUser> update(@Valid @RequestBody MerchantUserRequest request) {
    return ApiResponse.success(merchantUserService.update(request));
  }

  @PostMapping("/{id}/disable")
  @PreAuthorize("hasRole('ADMIN')")
  public ApiResponse<Void> disable(@PathVariable UUID id) {
    merchantUserService.disable(id);
    return ApiResponse.success(null);
  }

  @PostMapping("/{id}/enable")
  @PreAuthorize("hasRole('ADMIN')")
  public ApiResponse<Void> enable(@PathVariable UUID id) {
    merchantUserService.enable(id);
    return ApiResponse.success(null);
  }

  @PostMapping("/{id}/status")
  @PreAuthorize("hasRole('ADMIN')")
  public ApiResponse<Void> updateStatus(@PathVariable UUID id, @RequestParam UserStatus status) {
    merchantUserService.updateStatus(id, status);
    return ApiResponse.success(null);
  }

  @GetMapping("/roles")
  @PreAuthorize("hasRole('ADMIN')")
  public ApiResponse<Set<Role>> roles() {
    return ApiResponse.success(merchantUserService.listRoles());
  }
}
