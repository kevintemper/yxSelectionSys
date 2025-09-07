
import { ReactNode } from 'react';
import { Typography } from 'antd';
import { motion } from 'framer-motion';
import { fadeUp } from '../motions';
export default function PageHeader({title,sub,extra}:{title:string; sub?:ReactNode; extra?:ReactNode}){
  return (
    <div style={{marginBottom:16, display:'flex', alignItems:'center', justifyContent:'space-between'}}>
      <motion.div {...fadeUp(.05)}>
        <div className="badge">Admin</div>
        <Typography.Title level={4} style={{marginTop:8, color:'var(--text)'}}>{title}</Typography.Title>
        <div style={{color:'var(--sub)'}}>{sub}</div>
      </motion.div>
      {extra && <motion.div {...fadeUp(.1)}>{extra}</motion.div>}
    </div>
  );
}
