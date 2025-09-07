import api from './request';
export const UserApi={ page:(params:any)=>api.get('/admin/user/page',{params}).then(r=>r.data),
  create:(data:any)=>api.post('/admin/user',data), update:(data:any)=>api.put('/admin/user',data),
  del:(id:number)=>api.delete(`/admin/user/${id}`), reset:(id:number,pwd:string)=>api.post(`/admin/user/${id}/reset`,null,{params:{password:pwd}}),
  grantRoles:(id:number,codes:string[])=>api.post(`/admin/user/${id}/grantRoles`,codes), };
export const RoleApi={ page:(p:any)=>api.get('/admin/role/page',{params:p}).then(r=>r.data),
  create:(data:{code:string,name:string})=>api.post('/admin/role',null,{params:data}), update:(id:number,name:string)=>api.put(`/admin/role/${id}`,null,{params:{name}}),
  del:(id:number)=>api.delete(`/admin/role/${id}`), allMenus:()=>api.get('/admin/role/allMenus').then(r=>r.data),
  allResources:()=>api.get('/admin/role/allResources').then(r=>r.data),
  grantMenus:(roleId:number,menuIds:number[])=>api.post(`/admin/role/${roleId}/grantMenus`,menuIds),
  grantResources:(roleId:number,resIds:number[])=>api.post(`/admin/role/${roleId}/grantResources`,resIds), };
export const UserProfile={ profile:()=>api.get('/user/profile').then(r=>r.data), };