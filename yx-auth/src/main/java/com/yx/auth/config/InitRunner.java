package com.yx.auth.config;

import com.yx.auth.domain.*;
import com.yx.auth.repo.*;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.password.PasswordEncoder;

@Configuration
@RequiredArgsConstructor
public class InitRunner {

 @Bean
 CommandLineRunner seed(RoleRepo roleRepo,
                        UserRepo userRepo,
                        UserRoleRepo userRoleRepo,
                        PasswordEncoder pe,
                        MenuRepo menuRepo) {
  return args -> {
   // 1) 角色：ADMIN（没有则创建）
   Role adminRole = roleRepo.findByCode("ADMIN").orElseGet(() -> {
    Role r = new Role();
    r.setCode("ADMIN");
    r.setName("系统管理员");
    return roleRepo.save(r);
   });

   // 2) 管理员账号：admin/admin123（没有则创建）
   User admin = userRepo.findByUsername("admin").orElseGet(() -> {
    User u = new User();
    u.setUsername("admin");
    u.setNickname("平台管理员");
    u.setPassword(pe.encode("admin123")); // 仅开发环境使用
    u.setStatus(1);
    u.setUserType(9);        // ★ 关键：显式设置，避免 user_type 为空
    return userRepo.save(u);
   });

   // 3) 绑定用户-角色（根据你的 UserRole 主键字段名二选一）

   // ===== A. 如果 UserRole 是 @EmbeddedId private PK id; =====
            /*
            UserRole.PK pkA = new UserRole.PK();
            pkA.setUserId(admin.getId());
            pkA.setRoleId(adminRole.getId());

            userRoleRepo.findById(pkA).orElseGet(() -> {
                UserRole ur = new UserRole();
                ur.setId(pkA);                     // ← 字段名是 id
                return userRoleRepo.save(ur);
            });
            */

   // 绑定管理员与角色（IdClass 方案）
   UserRole.PK key = new UserRole.PK(admin.getId(), adminRole.getId());
   if (!userRoleRepo.existsById(key)) {
    UserRole ur = new UserRole();
    ur.setUserId(admin.getId());
    ur.setRoleId(adminRole.getId());
    userRoleRepo.save(ur);
   }



   // 4) 简单菜单（若你有 Menu 实体）
   if (menuRepo.count() == 0) {
    Menu sys = new Menu();
    sys.setName("系统管理");
    sys.setLevel(1);
    menuRepo.save(sys);

    Menu um = new Menu();
    um.setName("用户管理");
    um.setLevel(2);
    um.setParentId(sys.getId());
    um.setPath("/admin/users");
    menuRepo.save(um);

    Menu rm = new Menu();
    rm.setName("角色管理");
    rm.setLevel(2);
    rm.setParentId(sys.getId());
    rm.setPath("/admin/roles");
    menuRepo.save(rm);
   }
  };
 }
}
