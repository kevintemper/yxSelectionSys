import { ReactNode } from 'react'; import { hasPerm } from '../store/perm';
export default function Perm({code,children}:{code:string,children:ReactNode}){ return hasPerm(code)?<>{children}</>:null; }