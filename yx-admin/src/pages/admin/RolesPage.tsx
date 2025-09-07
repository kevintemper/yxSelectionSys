
import { useEffect, useMemo, useState } from 'react'; import { Button, Form, Input, Modal, Space, Table, Tree, Transfer, message, Card } from 'antd';
import { RoleApi } from '../../api/admin'; import PageHeader from '../../components/PageHeader';
type Role={ id:number; code:string; name:string; };
export default function RolesPage(){ const [data,setData]=useState<Role[]>([]); const [total,setTotal]=useState(0); const [q,setQ]=useState({page:0,size:10}); const [loading,setLoading]=useState(false);
  const fetch=async()=>{ setLoading(true); const res=await RoleApi.page(q); setData(res.records); setTotal(res.total); setLoading(false); }; useEffect(()=>{ fetch(); },[q.page,q.size]);
  const columns=useMemo(()=>[{title:'ID',dataIndex:'id',width:80},{title:'编码',dataIndex:'code'},{title:'名称',dataIndex:'name'},
    {title:'操作',render:(_:any,row:Role)=>(<Space><Button size="small" onClick={()=>openEdit(row)}>编辑</Button>
      <Button size="small" onClick={()=>openGrantMenus(row)}>授予菜单</Button><Button size="small" onClick={()=>openGrantResources(row)}>授予资源</Button>
      <Button size="small" danger onClick={()=>del(row)}>删除</Button></Space>)}],[]);
  const [open,setOpen]=useState(false); const [form]=Form.useForm(); const openCreate=()=>{ form.resetFields(); setOpen(true); }; const openEdit=(r:Role)=>{ form.setFieldsValue(r); setOpen(true); };
  const submit=async()=>{ const v=await form.validateFields(); if(v.id) await RoleApi.update(v.id,v.name); else await RoleApi.create(v); setOpen(false); fetch(); message.success('保存成功'); };
  const del=(r:Role)=>Modal.confirm({title:'确认删除？',onOk:async()=>{await RoleApi.del(r.id); fetch();}});
  const [grantMenuOpen,setGrantMenuOpen]=useState(false); const [menuTree,setMenuTree]=useState<any[]>([]); const [checkedMenu,setCheckedMenu]=useState<any[]>([]); const [curRole,setCurRole]=useState<Role|null>(null);
  const openGrantMenus=async(r:Role)=>{ setCurRole(r); setGrantMenuOpen(true); const tree=await RoleApi.allMenus(); setMenuTree(tree); };
  const doGrantMenus=async()=>{ if(!curRole) return; await RoleApi.grantMenus(curRole.id,checkedMenu as number[]); setGrantMenuOpen(false); message.success('已授权'); };
  const [grantResOpen,setGrantResOpen]=useState(false); const [resources,setResources]=useState<any[]>([]); const [selectedRes,setSelectedRes]=useState<string[]>([]);
  const openGrantResources=async(r:Role)=>{ setCurRole(r); setGrantResOpen(true); const all=await RoleApi.allResources(); setResources(all.map((x:any)=>({key:String(x.id),title:`${x.name} (${x.code})`}))); };
  const doGrantRes=async()=>{ if(!curRole) return; await RoleApi.grantResources(curRole.id,selectedRes.map(Number)); setGrantResOpen(false); message.success('已授权'); };
  return <div>
    <PageHeader title="角色管理" sub="维护角色信息，并为角色授予菜单与资源"/>
    <Card className="card-hover">
      <Space style={{marginBottom:12}}><Button onClick={openCreate} type="dashed">新建角色</Button></Space>
      <Table rowKey="id" loading={loading} dataSource={data} columns={columns}
             pagination={{current:q.page+1,pageSize:q.size,total,showSizeChanger:true,onChange:(p,s)=>setQ({page:p-1,size:s})}} />
    </Card>
    <Modal title={(form.getFieldValue('id')?'编辑':'新建')+'角色'} open={open} onOk={submit} onCancel={()=>setOpen(false)}>
      <Form form={form} labelCol={{span:5}} wrapperCol={{span:18}}>
        <Form.Item name="id" hidden><Input/></Form.Item>
        <Form.Item name="code" label="编码" rules={[{required:true}]}><Input disabled={!!form.getFieldValue('id')}/></Form.Item>
        <Form.Item name="name" label="名称" rules={[{required:true}]}><Input/></Form.Item>
      </Form>
    </Modal>
    <Modal title="授予菜单" open={grantMenuOpen} onOk={doGrantMenus} onCancel={()=>setGrantMenuOpen(false)} width={600}>
      <Tree checkable treeData={menuTree} onCheck={(k)=>setCheckedMenu(k as any[])} />
    </Modal>
    <Modal title="授予资源" open={grantResOpen} onOk={doGrantRes} onCancel={()=>setGrantResOpen(false)} width={720}>
      <Transfer dataSource={resources} titles={['未选','已选']} targetKeys={selectedRes} onChange={(keys)=>setSelectedRes(keys as string[])}
                showSearch render={item=>item.title} listStyle={{width:300,height:360}} />
    </Modal>
  </div>; }