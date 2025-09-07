package com.yx.auth.api;
import com.yx.auth.domain.User; import com.yx.auth.service.UserAdminService; import com.yx.common.api.PageResp; import jakarta.validation.Valid; import org.springframework.web.bind.annotation.*; import java.util.List;
@RestController @RequestMapping("/api/admin/user") public class UserAdminController{
 private final UserAdminService svc; public UserAdminController(UserAdminService s){ svc=s; }
 @GetMapping("/page") public PageResp<User> page(@RequestParam(required=false) String keyword,@RequestParam(required=false) Integer userType,@RequestParam(required=false) Integer status,@RequestParam(defaultValue="0") Integer page,@RequestParam(defaultValue="10") Integer size){ return svc.page(keyword,userType,status,page,size); }
 @PostMapping public Long create(@RequestBody @Valid User req){ return svc.create(req); }
 @PutMapping public void update(@RequestBody @Valid User req){ svc.update(req); }
 @DeleteMapping("/{id}") public void delete(@PathVariable Long id){ svc.delete(id); }
 @PostMapping("/{id}/reset") public void reset(@PathVariable Long id,@RequestParam String password){ svc.resetPassword(id,password); }
 @PostMapping("/{id}/grantRoles") public void grantRoles(@PathVariable Long id,@RequestBody List<String> codes){ svc.grantRoles(id,codes); }
}