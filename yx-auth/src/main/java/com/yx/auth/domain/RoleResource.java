package com.yx.auth.domain; import jakarta.persistence.*; import lombok.*;
@Entity @Table(name="role_resource") @Getter @Setter @IdClass(RoleResource.PK.class)
public class RoleResource{ @Id private Long roleId; @Id private Long resourceId;
 @Data public static class PK implements java.io.Serializable{ private Long roleId; private Long resourceId; } }