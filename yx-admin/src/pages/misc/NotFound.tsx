
import { Result, Button } from 'antd'; import { useNavigate } from 'react-router-dom';
export default function NotFound(){ const nav=useNavigate(); return <Result status="404" title="404" subTitle="页面走丢了" extra={<Button type="primary" onClick={()=>nav('/')}>回到首页</Button>} /> }
