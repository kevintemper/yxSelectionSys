// src/pages/Dashboard.tsx
import { useState } from 'react';
import {
  Card,
  Col,
  Row,
  Statistic,
  Typography,
  Button,
  Space,
  Tag,
  message,
  notification,
} from 'antd';
import {
  LineChart,
  Line,
  XAxis,
  YAxis,
  CartesianGrid,
  Tooltip,
  BarChart,
  Bar,
  ResponsiveContainer,
} from 'recharts';
import { motion } from 'framer-motion';
import { Link } from 'react-router-dom';
import PageHeader from '../../components/PageHeader';

// 小动效
const fadeUp = (delay = 0.05) => ({
  initial: { opacity: 0, y: 10 },
  animate: { opacity: 1, y: 0 },
  transition: { delay, type: 'spring', stiffness: 380, damping: 28 },
});

// 假数据
const lineData = Array.from({ length: 12 }, (_, i) => ({
  m: `${i + 1}月`,
  pv: Math.round(500 + Math.random() * 400),
}));
const barData = Array.from({ length: 7 }, (_, i) => ({
  d: `周${i + 1}`,
  uv: Math.round(20 + Math.random() * 50),
}));

type Env = 'gray' | 'prd';
type LastEnv = Env | 'unknown' | '';

/** 动画通知（可关闭） */
function openNotice(title: string, url: string, payload: any) {
  notification.open({
    message: title,
    description: (
      <motion.div
        initial={{ opacity: 0, y: 8, scale: 0.98 }}
        animate={{ opacity: 1, y: 0, scale: 1 }}
        exit={{ opacity: 0, y: -8, scale: 0.98 }}
        transition={{ type: 'spring', stiffness: 380, damping: 28 }}
      >
        <pre style={{ margin: 0 }}>{JSON.stringify(payload, null, 2)}</pre>
      </motion.div>
    ),
    duration: 3,
  });
}

export default function Dashboard() {
  const [loading, setLoading] = useState(false);
  const [lastEnv, setLastEnv] = useState<LastEnv>('');
  const [lastPayload, setLastPayload] = useState<any>(null);

  /** 调用不同端口（通过 Vite 代理）的后端 */
  const callEnv = async (env: Env) => {
    // 你已在 vite.config.ts 配好：
    // /gray-api -> http://localhost:9101
    // /prd-api  -> http://localhost:9103
    const url =
      env === 'gray'
        ? '/gray-api/api/auth/hello'
        : '/prd-api/api/auth/hello';

    const hide = message.loading(`正在请求 ${env} …`, 0);
    setLoading(true);

    try {
      const res = await fetch(url, { method: 'GET' });
      // 处理 403 / 非 JSON 场景
      const text = await res.text();
      let data: any = null;
      try {
        data = JSON.parse(text);
      } catch {
        throw new Error('Unexpected token: 非 JSON 响应（可能 403 或 HTML 网关页）');
      }

      setLastEnv((data?.env as LastEnv) ?? 'unknown');
      setLastPayload({ url, data });
      message.success(`请求 ${env} 成功`);
      openNotice(
        `来自 ${env.toUpperCase()} 环境 (${url})`,
        url,
        data
      );
    } catch (e: any) {
      const err = e?.message || 'Failed to fetch';
      setLastEnv('unknown');
      setLastPayload({ url, error: err });
      message.error('请求失败');
      openNotice(`请求 ${env.toUpperCase()} 失败`, url, { error: err });
      console.error('callEnv error:', e);
    } finally {
      hide();
      setLoading(false);
    }
  };

  const EnvTag = () => {
    if (!lastEnv) return null;
    const color =
      lastEnv === 'gray' ? 'geekblue' : lastEnv === 'prd' ? 'green' : 'default';
    return (
      <Tag color={color} style={{ marginLeft: 8 }}>
        后端最近一次返回：{lastEnv || '-'}
      </Tag>
    );
  };

  return (
    <div>
      {/* 标题 */}
      <PageHeader title="首页" sub="数据一览 · 高效运营" />

      {/* 环境请求按钮（保留你的可用逻辑） */}
      <Card className="card-hover" style={{ marginBottom: 16 }}>
        <Space size="middle" wrap>
          <Button
            type="primary"
            loading={loading}
            onClick={() => callEnv('gray')}
          >
            请求 Gray（9101）
          </Button>
          <Button danger loading={loading} onClick={() => callEnv('prd')}>
            请求 PRD（9103）
          </Button>
          <EnvTag />
        </Space>

        {/* 内联结果展示（无论成功失败都能看到） */}
        {lastPayload && (
          <motion.pre
            style={{
              marginTop: 12,
              padding: 12,
              background: '#f6f6f6',
              borderRadius: 8,
              color: '#222',
              maxWidth: 880,
              overflow: 'auto',
              boxShadow: '0 4px 14px rgba(0,0,0,.08)',
            }}
            initial={{ opacity: 0, y: 8 }}
            animate={{ opacity: 1, y: 0 }}
            transition={{ type: 'spring', stiffness: 380, damping: 28 }}
          >
            {JSON.stringify(lastPayload, null, 2)}
          </motion.pre>
        )}
      </Card>

      {/* 指标卡片 */}
      <Row gutter={16}>
        <Col span={6}>
          <motion.div {...fadeUp(0.05)}>
            <Card className="card-hover">
              <Statistic title="活跃用户" value={1234} />
            </Card>
          </motion.div>
        </Col>
        <Col span={6}>
          <motion.div {...fadeUp(0.1)}>
            <Card className="card-hover">
              <Statistic title="新增用户" value={567} />
            </Card>
          </motion.div>
        </Col>
        <Col span={6}>
          <motion.div {...fadeUp(0.15)}>
            <Card className="card-hover">
              <Statistic title="订单数" value={890} />
            </Card>
          </motion.div>
        </Col>
        <Col span={6}>
          <motion.div {...fadeUp(0.2)}>
            <Card className="card-hover">
              <Statistic title="GMV(￥)" value={12345} precision={0} />
            </Card>
          </motion.div>
        </Col>
      </Row>

      {/* 快捷导航 */}
      <Row gutter={16} style={{ marginTop: 16 }}>
        <Col span={6}>
          <motion.div {...fadeUp(0.05)}>
            <Link to="/users">
              <Card className="card-hover" hoverable>
                <Typography.Title level={5}>用户管理</Typography.Title>
                <Typography.Paragraph type="secondary">
                  账号、角色、权限
                </Typography.Paragraph>
              </Card>
            </Link>
          </motion.div>
        </Col>
        <Col span={6}>
          <motion.div {...fadeUp(0.1)}>
            <Link to="/products">
              <Card className="card-hover" hoverable>
                <Typography.Title level={5}>商品管理</Typography.Title>
                <Typography.Paragraph type="secondary">
                  商品、类目、库存
                </Typography.Paragraph>
              </Card>
            </Link>
          </motion.div>
        </Col>
        <Col span={6}>
          <motion.div {...fadeUp(0.15)}>
            <Link to="/orders">
              <Card className="card-hover" hoverable>
                <Typography.Title level={5}>订单管理</Typography.Title>
                <Typography.Paragraph type="secondary">
                  订单、售后、对账
                </Typography.Paragraph>
              </Card>
            </Link>
          </motion.div>
        </Col>
        <Col span={6}>
          <motion.div {...fadeUp(0.2)}>
            <Link to="/perm">
              <Card className="card-hover" hoverable>
                <Typography.Title level={5}>权限中心</Typography.Title>
                <Typography.Paragraph type="secondary">
                  资源、菜单、按钮
                </Typography.Paragraph>
              </Card>
            </Link>
          </motion.div>
        </Col>
      </Row>

      {/* 图表 */}
      <Row gutter={16} style={{ marginTop: 16 }}>
        <Col span={14}>
          <motion.div {...fadeUp(0.05)}>
            <Card className="card-hover" title="访问趋势">
              <div style={{ width: '100%', height: 300 }}>
                <ResponsiveContainer>
                  <LineChart data={lineData}>
                    <CartesianGrid strokeDasharray="3 3" />
                    <XAxis dataKey="m" />
                    <YAxis />
                    <Tooltip />
                    <Line type="monotone" dataKey="pv" stroke="#7C87FF" />
                  </LineChart>
                </ResponsiveContainer>
              </div>
            </Card>
          </motion.div>
        </Col>
        <Col span={10}>
          <motion.div {...fadeUp(0.1)}>
            <Card className="card-hover" title="近7天新客">
              <div style={{ width: '100%', height: 300 }}>
                <ResponsiveContainer>
                  <BarChart data={barData}>
                    <CartesianGrid strokeDasharray="3 3" />
                    <XAxis dataKey="d" />
                    <YAxis />
                    <Tooltip />
                    <Bar dataKey="uv" fill="#3FD1FF" />
                  </BarChart>
                </ResponsiveContainer>
              </div>
            </Card>
          </motion.div>
        </Col>
      </Row>
    </div>
  );
}
