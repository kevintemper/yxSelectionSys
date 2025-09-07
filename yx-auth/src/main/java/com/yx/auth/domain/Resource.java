package com.yx.auth.domain; import jakarta.persistence.*; import lombok.*;
@Entity @Table(name="resources") @Getter @Setter public class Resource{
 @Id @GeneratedValue(strategy=GenerationType.IDENTITY) private Long id;
 @Column(unique=true,nullable=false) private String code;
 @Column(nullable=false) private String type; @Column(nullable=false) private String name;
 private String uri; private String method;
}