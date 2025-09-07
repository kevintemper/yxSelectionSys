
import { Routes, Route, Navigate } from 'react-router-dom';
import MainLayout from './layouts/MainLayout';
import Login from './pages/Login';
import UsersPage from './pages/admin/UsersPage';
import RolesPage from './pages/admin/RolesPage';
import Dashboard from './pages/dashboard/Dashboard';
import MenuPage from './pages/menu/MenuPage';
import ResourcesPage from './pages/resource/ResourcesPage';
import NotFound from './pages/misc/NotFound';
import Register from './pages/Register';

function Guard({ children }: { children: any }) {
  const at = localStorage.getItem('accessToken');
  if (!at) return <Navigate to="/login" replace />;
  return children;
}

export default function App() {
  return (
    <Routes>
      <Route path="/login" element={<Login />} />
      <Route path="/" element={<Guard><MainLayout /></Guard>}>
        <Route index element={<Dashboard />} />
        <Route path="admin/users" element={<UsersPage />} />
        <Route path="admin/roles" element={<RolesPage />} />
        <Route path="admin/menus" element={<MenuPage />} />
        <Route path="admin/resources" element={<ResourcesPage />} />
        <Route path="*" element={<NotFound />} />
      </Route>
      <Route path="/register" element={<Register />} />
    </Routes>
  );
}
