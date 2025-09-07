package com.yx.auth.service;
import com.yx.auth.domain.*; import com.yx.auth.repo.*; import com.yx.common.api.PageResp; import org.springframework.data.domain.*; import org.springframework.security.crypto.password.PasswordEncoder; import org.springframework.stereotype.Service; import org.springframework.util.StringUtils; import java.time.Instant; import java.util.List;
@Service public class UserAdminService{
 private final UserRepo users; private final RoleRepo roles; private final UserRoleRepo ur; private final PasswordEncoder pe;
 public UserAdminService(UserRepo u,RoleRepo r,UserRoleRepo ur,PasswordEncoder pe){ users=u; roles=r; this.ur=ur; this.pe=pe; }
 public PageResp<User> page(String keyword,Integer userType,Integer status,int page,int size){
  Pageable p=PageRequest.of(Math.max(page,0),Math.min(Math.max(size,1),100),Sort.by(Sort.Direction.DESC,"id"));
  var spec=(org.springframework.data.jpa.domain.Specification<User>)(root,cq,cb)->{ var pred=cb.conjunction();
   if(StringUtils.hasText(keyword)) pred.getExpressions().add(cb.like(root.get("username"),"%"+keyword+"%"));
   if(userType!=null) pred.getExpressions().add(cb.equal(root.get("userType"),userType));
   if(status!=null) pred.getExpressions().add(cb.equal(root.get("status"),status)); return pred; };
  var data=users.findAll(spec,p); return new PageResp<>(data.getTotalElements(),data.getContent());
 }
 public Long create(User u){ if(users.findByUsername(u.getUsername()).isPresent()) throw new RuntimeException("用户名已存在");
  u.setPassword(pe.encode(u.getPassword())); u.setCreatedAt(Instant.now()); return users.save(u).getId(); }
 public void update(User req){ var u=users.findById(req.getId()).orElseThrow();
  if(req.getNickname()!=null) u.setNickname(req.getNickname()); if(req.getPhone()!=null) u.setPhone(req.getPhone());
  if(req.getEmail()!=null) u.setEmail(req.getEmail()); if(req.getStatus()!=null) u.setStatus(req.getStatus());
  if(req.getMerchantId()!=null) u.setMerchantId(req.getMerchantId()); if(req.getUserType()!=null) u.setUserType(req.getUserType());
  u.setUpdatedAt(Instant.now()); users.save(u); }
 public void delete(Long id){ users.deleteById(id); }
 public void resetPassword(Long id,String raw){ var u=users.findById(id).orElseThrow(); u.setPassword(pe.encode(raw)); users.save(u); }
 public void grantRoles(Long userId, List<String> roleCodes){
  var olds=ur.findAll().stream().filter(x->x.getUserId().equals(userId)).toList(); ur.deleteAll(olds);
  var map=roles.findAll().stream().collect(java.util.stream.Collectors.toMap(Role::getCode,Role::getId));
  roleCodes.forEach(code->{ var rid=map.get(code); if(rid!=null){ var x=new UserRole(); x.setUserId(userId); x.setRoleId(rid); ur.save(x); }});
 } }