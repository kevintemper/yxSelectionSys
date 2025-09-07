
import { Layout, Menu, Breadcrumb, Space, Avatar, Dropdown, Input } from 'antd';
import { useEffect, useState } from 'react';
import { useLocation, useNavigate, Outlet } from 'react-router-dom';
import api from '../api/request';
import { loadProfile } from '../store/perm';
import { UserOutlined, SearchOutlined, SunOutlined, MoonOutlined } from '@ant-design/icons';
import classNames from 'classnames';
import '../styles/theme.css';

export default function MainLayout() {
  const [items, setItems] = useState<any[]>([]);
  const [collapsed, setCollapsed] = useState(false);
  const [theme, setTheme] = useState<'dark' | 'light'>('dark');
  const nav = useNavigate(); const { pathname } = useLocation();

  useEffect(() => { document.documentElement.className = theme === 'light' ? 'light' : ''; }, [theme]);

  useEffect(() => {
    (async () => {
      try { await loadProfile(); } catch (e) { nav('/login'); return; }
      const { data } = await api.get('/menu/tree');
      const t = (nodes: any[]) => nodes.map(n => ({ key: n.id, label: n.title, onClick: () => n.path && nav(n.path), children: n.children ? t(n.children) : undefined }));
      setItems(t(data));
    })();
  }, []);

  const menu = (
    <Menu
      items={[
        { key: 'theme', label: (<div onClick={() => setTheme(theme === 'dark' ? 'light' : 'dark')}>{theme === 'dark' ? '切换到浅色' : '切换到深色'}</div>) },
        { type: 'divider' as any },
        { key: 'logout', label: '退出登录', onClick: () => { localStorage.removeItem('accessToken'); localStorage.removeItem('refreshToken'); nav('/login'); } },
      ]}
    />
  );

  return (
    <Layout style={{ minHeight: '100vh' }} className={classNames(theme)}>
      <Layout.Sider collapsible collapsed={collapsed} onCollapse={setCollapsed} theme="light" width={220}>
        <div style={{ padding: 16 }} className="brand">{collapsed ? 'YX' : '严选管理台'}</div>
        <Menu items={items} mode="inline" />
      </Layout.Sider>
      <Layout>
        <Layout.Header style={{ background: 'var(--panel)', display: 'flex', alignItems: 'center', justifyContent: 'space-between', padding: '8px 16px' }} className="nav-shadow">
          <Breadcrumb items={[{ title: '首页' }, { title: pathname }]} />
          <Space>
            <Input allowClear size="middle" placeholder="全局搜索" prefix={<SearchOutlined />} style={{ width: 260 }} />
            {theme === 'dark' ? <SunOutlined onClick={() => setTheme('light')} style={{ cursor: 'pointer' }} /> : <MoonOutlined onClick={() => setTheme('dark')} style={{ cursor: 'pointer' }} />}
            <Dropdown overlay={menu} placement="bottomRight" trigger={['click']}><Avatar size={32} icon={<UserOutlined />} style={{ cursor: 'pointer' }} /></Dropdown>
          </Space>
        </Layout.Header>
        <Layout.Content style={{ padding: 20 }}>
          <div className="app-panel"><Outlet /></div>
        </Layout.Content>
      </Layout>
    </Layout>
  );
}
