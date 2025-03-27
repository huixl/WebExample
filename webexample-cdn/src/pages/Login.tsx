import { LockOutlined, UserOutlined } from '@ant-design/icons';
import { Button, Card, Checkbox, Form, Input, message, Tabs } from 'antd';
import React, { useState } from 'react';
import { useNavigate } from 'react-router-dom';
import { authService } from '../services/auth';
import { LoginRequest } from '../types/auth';

const { TabPane } = Tabs;

const Login: React.FC = () => {
    const [loading, setLoading] = useState(false);
    const navigate = useNavigate();

    const onFinish = async (values: any) => {
        try {
            setLoading(true);
            const loginRequest: LoginRequest = {
                identity: values.username,
                credential: values.password,
                loginType: 'username_password',
                rememberMe: values.remember
            };
            
            await authService.getLoginInfo();
            const response = await authService.login(loginRequest);
            message.success('登录成功！');
            // 存储用户信息
            localStorage.setItem('user', JSON.stringify({
                userId: response.userId,
                username: response.username,
                nickname: response.nickname,
                avatarUrl: response.avatarUrl
            }));
            // 存储token
            localStorage.setItem('token', response.token);
            // 跳转到首页
            navigate('/');
        } catch (error: any) {
            message.error(error.response?.data?.message || '登录失败，请重试');
        } finally {
            setLoading(false);
        }
    };

    const onRegister = () => {
        navigate('/register');
    };

    return (
        <div style={{ 
            display: 'flex', 
            justifyContent: 'center', 
            alignItems: 'center', 
            minHeight: '100vh',
            background: '#f0f2f5'
        }}>
            <Card style={{ width: 400, boxShadow: '0 4px 8px rgba(0,0,0,0.1)' }}>
                <h2 style={{ textAlign: 'center', marginBottom: 30 }}>欢迎登录</h2>
                <Tabs defaultActiveKey="account" centered>
                    <TabPane tab="账号密码登录" key="account">
                        <Form
                            name="login"
                            onFinish={onFinish}
                            initialValues={{ remember: true }}
                        >
                            <Form.Item
                                name="username"
                                rules={[{ required: true, message: '请输入用户名！' }]}
                            >
                                <Input 
                                    prefix={<UserOutlined />} 
                                    placeholder="用户名" 
                                    size="large"
                                />
                            </Form.Item>

                            <Form.Item
                                name="password"
                                rules={[{ required: true, message: '请输入密码！' }]}
                            >
                                <Input.Password
                                    prefix={<LockOutlined />}
                                    placeholder="密码"
                                    size="large"
                                />
                            </Form.Item>

                            <Form.Item>
                                <Form.Item name="remember" valuePropName="checked" noStyle>
                                    <Checkbox>记住我</Checkbox>
                                </Form.Item>

                                <a style={{ float: 'right' }} href="#">
                                    忘记密码
                                </a>
                            </Form.Item>

                            <Form.Item>
                                <Button type="primary" htmlType="submit" loading={loading} block size="large">
                                    登录
                                </Button>
                            </Form.Item>

                            <div style={{ textAlign: 'center' }}>
                                还没有账号？ <Button type="link" onClick={onRegister}>立即注册</Button>
                            </div>
                        </Form>
                    </TabPane>
                </Tabs>
            </Card>
        </div>
    );
};

export default Login; 