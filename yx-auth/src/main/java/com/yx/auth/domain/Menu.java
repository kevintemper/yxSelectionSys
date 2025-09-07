package com.yx.auth.domain; import jakarta.persistence.*; import lombok.*;
@Entity @Table(name="menus") @Getter @Setter public class Menu{
 @Id @GeneratedValue(strategy=GenerationType.IDENTITY) private Long id;
 private Long parentId; @Column(nullable=false) private String name;
 private String path; private String icon; private Integer sort=0;
 @Column(nullable=false) private Integer level;
}