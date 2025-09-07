package com.yx.auth.api;
import com.yx.auth.domain.*; import com.yx.auth.repo.*; import com.yx.common.api.PageResp; import org.springframework.data.domain.*; import org.springframework.web.bind.annotation.*; import java.util.*;
@RestController @RequestMapping("/api/admin/role") public class RoleAdminController{
 private final RoleRepo roles; private final RoleMenuRepo rm; private final RoleResourceRepo rr; private final MenuRepo menus; private final ResourceRepo res;
 public RoleAdminController(RoleRepo r,RoleMenuRepo rm,RoleResourceRepo rr,MenuRepo m,ResourceRepo res){ roles=r; this.rm=rm; this.rr=rr; menus=m; this.res=res; }
 @GetMapping("/page") public PageResp<Role> page(@RequestParam(defaultValue="0") int page,@RequestParam(defaultValue="10") int size){ var p=PageRequest.of(page,size,Sort.by(Sort.Direction.DESC,"id")); var data=roles.findAll(p); return new PageResp<>(data.getTotalElements(),data.getContent()); }
 @PostMapping public Long create(@RequestParam String code,@RequestParam String name){ if(roles.findByCode(code).isPresent()) throw new RuntimeException("角色编码已存在"); var r=new Role(); r.setCode(code); r.setName(name); return roles.save(r).getId(); }
 @PutMapping("/{id}") public void update(@PathVariable Long id,@RequestParam String name){ var r=roles.findById(id).orElseThrow(); r.setName(name); roles.save(r); }
 @DeleteMapping("/{id}") public void delete(@PathVariable Long id){ roles.deleteById(id); }
 @PostMapping("/{roleId}/grantMenus") public void grantMenus(@PathVariable Long roleId,@RequestBody List<Long> menuIds){ rm.deleteAll(rm.findAll().stream().filter(x->x.getRoleId().equals(roleId)).toList()); menuIds.forEach(mid->{ var x=new RoleMenu(); x.setRoleId(roleId); x.setMenuId(mid); rm.save(x); }); }
 @PostMapping("/{roleId}/grantResources") public void grantResources(@PathVariable Long roleId,@RequestBody List<Long> resourceIds){ rr.deleteAll(rr.findAll().stream().filter(x->x.getRoleId().equals(roleId)).toList()); resourceIds.forEach(rid->{ var x=new RoleResource(); x.setRoleId(roleId); x.setResourceId(rid); rr.save(x); }); }
 @GetMapping("/allMenus") public Object allMenus(){ return build(null); }
 private List<Map<String,Object>> build(Long pid){ var list=menus.findByParentIdOrderBySortAsc(pid); List<Map<String,Object>> ret=new ArrayList<>(); for(var m:list){ var node=new LinkedHashMap<String,Object>(); node.put("id",m.getId()); node.put("title",m.getName()); node.put("children",build(m.getId())); ret.add(node);} return ret; }
 @GetMapping("/allResources") public List<Resource> allResources(){ return res.findAll(); }
}