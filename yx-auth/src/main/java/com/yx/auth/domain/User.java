package com.yx.auth.domain;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.Builder;

import java.time.Instant;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
@Table(name = "users")
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 64)
    private String username;

    @Column(nullable = false, length = 255)
    private String password;

    @Column(name = "nickname", length = 64)
    private String nickname;

    /**
     * 0=普通C端用户, 1=商家管理员
     */
    @Column(name = "user_type", nullable = false)
    private Integer userType;

    @Column(length = 32)
    private String phone;

    @Column(length = 128)
    private String email;

    @Column(name = "status", nullable = false)
    private Integer status;

    @Column(name = "merchant_id")
    private Long merchantId;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt;

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;
}
