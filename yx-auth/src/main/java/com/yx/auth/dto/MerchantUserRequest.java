package com.yx.auth.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import java.util.Set;
import java.util.UUID;

public class MerchantUserRequest {

  private UUID id;

  @NotBlank(message = "用户名必填")
  private String username;

  @Size(min = 6, message = "密码至少6位")
  private String password;

  @NotBlank(message = "昵称必填")
  private String displayName;

  @Email(message = "邮箱格式不正确")
  private String email;

  private String phone;

  private Set<UUID> roleIds;

  public UUID getId() {
    return id;
  }

  public void setId(UUID id) {
    this.id = id;
  }

  public String getUsername() {
    return username;
  }

  public void setUsername(String username) {
    this.username = username;
  }

  public String getPassword() {
    return password;
  }

  public void setPassword(String password) {
    this.password = password;
  }

  public String getDisplayName() {
    return displayName;
  }

  public void setDisplayName(String displayName) {
    this.displayName = displayName;
  }

  public String getEmail() {
    return email;
  }

  public void setEmail(String email) {
    this.email = email;
  }

  public String getPhone() {
    return phone;
  }

  public void setPhone(String phone) {
    this.phone = phone;
  }

  public Set<UUID> getRoleIds() {
    return roleIds;
  }

  public void setRoleIds(Set<UUID> roleIds) {
    this.roleIds = roleIds;
  }
}
