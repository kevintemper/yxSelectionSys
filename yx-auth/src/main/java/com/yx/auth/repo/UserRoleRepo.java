package com.yx.auth.repo;

import com.yx.auth.domain.UserRole;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface UserRoleRepo extends JpaRepository<UserRole, UserRole.PK> {

    @Query("select r.code from Role r where r.id in (select ur.roleId from UserRole ur where ur.userId = :uid)")
    List<String> findRoleCodesByUserId(@Param("uid") Long userId);

    @Query("""
            select distinct res.code
            from Resource res
            where res.id in (
              select rr.resourceId from RoleResource rr
              where rr.roleId in (select ur.roleId from UserRole ur where ur.userId = :uid)
            )
            """)
    List<String> findPermCodesByUserId(@Param("uid") Long userId);
}
