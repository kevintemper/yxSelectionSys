
import { useEffect, useState } from 'react';
import { Button, Card, Space, Tree, Upload, message } from 'antd';
import { UploadOutlined, ReloadOutlined } from '@ant-design/icons';
import api from '../../api/request';
import PageHeader from '../../components/PageHeader';

export default function MenuPage(){
  const [tree,setTree]=useState<any[]>([]);
  const load=async()=>{ const {data}=await api.get('/menu/tree'); setTree(data); };
  useEffect(()=>{ load(); },[]);

  const props={
    name:'file', showUploadList:false,
    customRequest: async (opt:any)=>{
      const fd=new FormData(); fd.append('file',opt.file);
      await api.post('/menu/import', fd, {headers:{'Content-Type':'multipart/form-data'}});
      message.success('导入成功'); load(); opt.onSuccess({}, opt.file);
    }
  };

  return (
    <div>
      <PageHeader title="菜单管理" sub="支持通过 Excel 一键导入菜单树" />
      <Card className="card-hover" extra={<Space><Upload {...props}><Button icon={<UploadOutlined/>}>导入Excel</Button></Upload><Button icon={<ReloadOutlined/>} onClick={load}>刷新</Button></Space>}>
        <Tree treeData={tree} defaultExpandAll/>
      </Card>
    </div>
  );
}
