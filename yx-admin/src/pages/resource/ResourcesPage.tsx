
import { useEffect, useState } from 'react';
import { Card, Table } from 'antd';
import { RoleApi } from '../../api/admin';

export default function ResourcesPage(){
  const [data,setData]=useState<any[]>([]);
  useEffect(()=>{ (async()=>{ const all = await RoleApi.allResources(); setData(all); })(); },[]);
  return (
    <Card title="资源列表（只读）">
      <Table rowKey="id" dataSource={data} columns={[
        {title:'ID',dataIndex:'id',width:80},
        {title:'编码',dataIndex:'code'},
        {title:'名称',dataIndex:'name'},
        {title:'类型',dataIndex:'type'},
        {title:'URI',dataIndex:'uri'},
        {title:'方法',dataIndex:'method'},
      ]}/>
    </Card>
  );
}
