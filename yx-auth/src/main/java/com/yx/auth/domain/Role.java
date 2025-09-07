package com.yx.auth.domain; import jakarta.persistence.*; import lombok.*;
@Entity @Table(name="roles") @Getter @Setter public class Role{
 @Id @GeneratedValue(strategy=GenerationType.IDENTITY) private Long id;
 @Column(unique=true,nullable=false) private String code;
 @Column(nullable=false) private String name;
}