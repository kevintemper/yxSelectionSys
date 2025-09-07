// yx-admin/src/pages/Login.tsx
import { useEffect, useState } from 'react';
import { Button, Card, Checkbox, Form, Input, Typography, message } from 'antd';
import { Link, useNavigate } from 'react-router-dom';
import api from '../api/request';
import { motion } from 'framer-motion';
import { fadeUp } from '../motions';

type Captcha = { id: string; img: string } | null;

export default function Login() {
  const nav = useNavigate();
  const [cap, setCap] = useState<Captcha>(null);
  const [submitting, setSubmitting] = useState(false);

  // 获取验证码（保持你原来的逻辑）
  const loadCaptcha = async () => {
    try {
      const { data } = await api.get('/auth/captcha');
      setCap({ id: data.captchaId, img: data.imageBase64 || data.imgBase64 });
    } catch {
      message.error('获取验证码失败，请稍后重试');
    }
  };

  useEffect(() => { loadCaptcha(); }, []);

  // 提交登录（优化后的部分）
  const submit = async (v: any) => {
    if (!cap?.id) {
      message.warning('验证码加载中，请稍后再试');
      return;
    }
    setSubmitting(true);
    try {
      const { data } = await api.post('/auth/login', {
        username: v.username,
        password: v.password,
        captchaId: cap.id,
        captchaCode: v.captcha,
        rememberMe: v.remember,
      });
      // 存储 token
      localStorage.setItem('accessToken', data.accessToken);
      localStorage.setItem('refreshToken', data.refreshToken);
      message.success('登录成功');
      nav('/'); // 跳转到首页
    } catch (e: any) {
      const msg = e?.response?.data?.msg || e?.response?.data?.message || '登录失败';
      message.error(msg);
      await loadCaptcha(); // 登录失败时刷新验证码
    } finally {
      setSubmitting(false);
    }
  };

  return (
    <div style={{ minHeight: '100vh', display: 'grid', placeItems: 'center', position: 'relative', overflow: 'hidden' }}>
      <img src="/src/assets/hero-illustration.svg" style={{ position: 'absolute', inset: 0, objectFit: 'cover', filter: 'blur(2px) brightness(.9)' }} />
      <motion.div {...fadeUp(.05)}>
        <Card style={{ width: 420, boxShadow: '0 10px 30px rgba(0,0,0,.35)', backdropFilter: 'blur(6px)' }}>
          <div style={{ display: 'flex', gap: 12, alignItems: 'center', marginBottom: 8 }}>
            <img src="/src/assets/logo.svg" style={{ height: 36 }} />
            <Typography.Title level={4} style={{ margin: 0 }}>严选管理台</Typography.Title>
          </div>
          <Typography.Paragraph type="secondary">欢迎回来，请登录以继续</Typography.Paragraph>

          <Form onFinish={submit} layout="vertical" initialValues={{ remember: true }}>
            <Form.Item name="username" label="用户名" rules={[{ required: true, message: '请输入用户名' }]}>
              <Input size="large" placeholder="admin" autoComplete="username" />
            </Form.Item>

            <Form.Item name="password" label="密码" rules={[{ required: true, message: '请输入密码' }]}>
              <Input.Password size="large" placeholder="admin123" autoComplete="current-password" />
            </Form.Item>

            {/* 保持你的验证码逻辑不动 */}
            <Form.Item name="captcha" label="验证码" rules={[{ required: true, message: '请输入验证码' }]}>
              <Input
                size="large"
                addonAfter={
                  <img
                    src={cap?.img || ''}
                    onClick={loadCaptcha}
                    style={{ height: 36, cursor: 'pointer' }}
                    title="点击刷新验证码"
                    alt="captcha"
                  />
                }
              />
            </Form.Item>

            <Form.Item name="remember" valuePropName="checked">
              <Checkbox>记住登录（7天）</Checkbox>
            </Form.Item>

            <Button
              className="gradient-btn"
              size="large"
              htmlType="submit"
              block
              loading={submitting}
              disabled={submitting}
            >
              登 录
            </Button>
          </Form>

          <div style={{ marginTop: 12, textAlign: 'right' }}>
            <Link to="/register">没有账号？去注册</Link>
          </div>
        </Card>
      </motion.div>
    </div>
  );
}
