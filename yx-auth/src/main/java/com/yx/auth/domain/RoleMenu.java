package com.yx.auth.domain; import jakarta.persistence.*; import lombok.*;
@Entity @Table(name="role_menu") @Getter @Setter @IdClass(RoleMenu.PK.class)
public class RoleMenu{ @Id private Long roleId; @Id private Long menuId;
 @Data public static class PK implements java.io.Serializable{ private Long roleId; private Long menuId; } }