// src/pages/Register.tsx
import { useEffect, useState } from 'react';
import { Button, Card, Form, Input, Radio, Typography, message } from 'antd';
import { Link, useNavigate } from 'react-router-dom';
import api from '../api/request';

export default function Register() {
    const nav = useNavigate();
    const [cap, setCap] = useState<{ id: string; img: string } | null>(null);

    const load = async () => {
        try {
            const { data } = await api.get('/auth/captcha');
            setCap({ id: data.captchaId, img: data.imageBase64 || data.imgBase64 });
        } catch {
            message.error('获取验证码失败');
        }
    };
    useEffect(() => { load(); }, []);

    const submit = async (v: any) => {
        const payload = {
            username: v.username,
            password: v.password,
            userType: v.userType, // 0=C端，1=商家
            phone: v.phone,
            email: v.email,
            captchaId: cap?.id,
            captchaCode: v.captcha
        };
        try {
            await api.post('/auth/register', payload);
            message.success('注册成功，请登录');
            nav('/login');
        } catch (e: any) {
            message.error(e?.response?.data?.message || '注册失败');
            load();
        }
    };

    return (
        <div style={{ minHeight: '100vh', display: 'grid', placeItems: 'center' }}>
            <Card style={{ width: 520 }}>
                <Typography.Title level={4}>注册新用户</Typography.Title>
                <Form layout="vertical" onFinish={submit} initialValues={{ userType: 0 }}>
                    <Form.Item name="username" label="用户名" rules={[{ required: true }]}><Input /></Form.Item>
                    <Form.Item name="password" label="密码" rules={[{ required: true, min: 6 }]}><Input.Password /></Form.Item>
                    <Form.Item name="userType" label="用户类型">
                        <Radio.Group>
                            <Radio value={0}>C端</Radio>
                            <Radio value={1}>商家</Radio>
                        </Radio.Group>
                    </Form.Item>
                    <Form.Item name="phone" label="手机"><Input /></Form.Item>
                    <Form.Item name="email" label="邮箱"><Input /></Form.Item>
                    <Form.Item name="captcha" label="验证码" rules={[{ required: true }]}>
                        <Input addonAfter={<img src={cap?.img} onClick={load} style={{ height: 32, cursor: 'pointer' }} title="点击刷新验证码" />} />
                    </Form.Item>
                    <Button type="primary" htmlType="submit" block>注 册</Button>
                </Form>
                <div style={{ marginTop: 12 }}><Link to="/login">已有账号？去登录</Link></div>
            </Card>
        </div>
    );
}
