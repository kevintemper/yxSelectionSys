// === Guard.tsx injected by fixer ===
import { Navigate, useLocation } from 'react-router-dom';

export default function Guard({ children }: { children: React.ReactNode }){
  const token = localStorage.getItem('accessToken');
  const loc = useLocation();
  if(!token){
    return <Navigate to="/login" state={{ from: loc }} replace />;
  }
  return <>{children}</>;
}
