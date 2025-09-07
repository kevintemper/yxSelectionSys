
import { useEffect, useMemo, useState } from 'react'; import { Button, Form, Input, Modal, Radio, Space, Table, Tag, message, Select, Card } from 'antd';
import { UserApi, RoleApi } from '../../api/admin'; import PageHeader from '../../components/PageHeader';
type User={ id:number; username:string; nickname?:string; phone?:string; email?:string; userType:number; status:number; merchantId?:number; };
export default function UsersPage(){ const [data,setData]=useState<User[]>([]); const [total,setTotal]=useState(0); const [loading,setLoading]=useState(false);
  const [q,setQ]=useState<any>({keyword:'',userType:undefined,status:undefined,page:0,size:10}); const fetch=async()=>{ setLoading(true); const res=await UserApi.page(q); setData(res.records); setTotal(res.total); setLoading(false); };
  useEffect(()=>{ fetch(); },[q.page,q.size]); const columns=useMemo(()=>[
    {title:'ID',dataIndex:'id',width:80},{title:'用户名',dataIndex:'username'},{title:'昵称',dataIndex:'nickname'},
    {title:'类型',dataIndex:'userType',render:(v:number)=>({0:'C端',1:'商家',2:'运营'} as any)[v]||v},
    {title:'状态',dataIndex:'status',render:(v:number)=>v===1?<Tag color="green">启用</Tag>:<Tag color="red">停用</Tag>},
    {title:'操作',render:(_:any,row:User)=>(<Space>
      <Button onClick={()=>openEdit(row)} size="small">编辑</Button>
      <Button onClick={()=>openGrant(row)} size="small">授予角色</Button>
      <Button onClick={()=>resetPwd(row)} size="small">重置密码</Button>
      <Button danger onClick={()=>del(row)} size="small">删除</Button>
    </Space>)}],[]);
  const [open,setOpen]=useState(false); const [form]=Form.useForm<User>(); const openCreate=()=>{ form.resetFields(); form.setFieldsValue({userType:2,status:1} as any); setOpen(true); };
  const openEdit=(u:User)=>{ form.setFieldsValue(u as any); setOpen(true); }; const submit=async()=>{ const v=await form.validateFields(); if(v.id) await UserApi.update(v); else await UserApi.create(v); setOpen(false); fetch(); message.success('保存成功'); };
  const del=(u:User)=>Modal.confirm({title:'确认删除？',onOk:async()=>{await UserApi.del(u.id); fetch();}});
  const resetPwd=async(u:User)=>{ const pwd=prompt('输入新密码','admin123'); if(!pwd) return; await UserApi.reset(u.id,pwd); message.success('已重置'); };
  const [grantOpen,setGrantOpen]=useState(false); const [roles,setRoles]=useState<{label:string,value:string}[]>([]);
  const [grantUser,setGrantUser]=useState<User|null>(null); const [selectedRoles,setSelectedRoles]=useState<string[]>([]);
  const openGrant=async(u:User)=>{ setGrantUser(u); setGrantOpen(true); const page=await RoleApi.page({page:0,size:100}); setRoles(page.records.map((r:any)=>({label:`${r.name}(${r.code})`,value:r.code}))); };
  const doGrant=async()=>{ if(!grantUser) return; await UserApi.grantRoles(grantUser.id,selectedRoles); setGrantOpen(false); message.success('已授权'); };
  return <div>
    <PageHeader title="用户管理" sub="支持用户类型/状态筛选，提供 CRUD、重置密码与角色授权"/>
    <Card className="card-hover">
      <Space style={{marginBottom:12}}>
        <Input placeholder="搜索用户名" value={q.keyword} onChange={e=>setQ({...q,keyword:e.target.value})}/>
        <Select allowClear placeholder="类型" style={{width:120}} value={q.userType} onChange={v=>setQ({...q,userType:v})}
                options={[{value:0,label:'C端'},{value:1,label:'商家'},{value:2,label:'运营'}]} />
        <Select allowClear placeholder="状态" style={{width:120}} value={q.status} onChange={v=>setQ({...q,status:v})}
                options={[{value:1,label:'启用'},{value:0,label:'停用'}]} />
        <Button type="primary" onClick={()=>{setQ({...q,page:0}); fetch();}}>查询</Button>
        <Button onClick={openCreate} type="dashed">新建用户</Button>
      </Space>
      <Table rowKey="id" loading={loading} dataSource={data} columns={columns}
             pagination={{current:q.page+1,pageSize:q.size,total,showSizeChanger:true,onChange:(p,s)=>setQ({...q,page:p-1,size:s})}} />
    </Card>
    <Modal title={(form.getFieldValue('id')?'编辑':'新建')+'用户'} open={open} onOk={submit} onCancel={()=>setOpen(false)}>
      <Form form={form} labelCol={{span:5}} wrapperCol={{span:18}}>
        <Form.Item name="id" hidden><Input/></Form.Item>
        <Form.Item name="username" label="用户名" rules={[{required:!form.getFieldValue('id')}]}> <Input disabled={!!form.getFieldValue('id')}/> </Form.Item>
        {!form.getFieldValue('id') && <Form.Item name="password" label="密码" rules={[{required:true,min:6}]}><Input.Password/></Form.Item>}
        <Form.Item name="userType" label="类型" rules={[{required:true}]}><Radio.Group options={[{label:'C端',value:0},{label:'商家',value:1},{label:'运营',value:2}]}/></Form.Item>
        <Form.Item name="status" label="状态" rules={[{required:true}]}><Radio.Group options={[{label:'启用',value:1},{label:'停用',value:0}]}/></Form.Item>
        <Form.Item name="nickname" label="昵称"><Input/></Form.Item>
        <Form.Item name="phone" label="手机"><Input/></Form.Item>
        <Form.Item name="email" label="邮箱"><Input/></Form.Item>
        <Form.Item name="merchantId" label="商家ID"><Input/></Form.Item>
      </Form>
    </Modal>
    <Modal title="授予角色" open={grantOpen} onOk={doGrant} onCancel={()=>setGrantOpen(false)}>
      <Select mode="multiple" style={{width:'100%'}} options={roles} value={selectedRoles} onChange={setSelectedRoles}/>
    </Modal>
  </div>; }