import { LockOutlined, MailOutlined, PhoneOutlined, UserOutlined } from '@ant-design/icons';
import { Button, Card, Form, Input, message } from 'antd';
import React, { useState } from 'react';
import { useNavigate } from 'react-router-dom';
import { authService } from '../services/auth';
import { RegisterRequest } from '../types/auth';

const Register: React.FC = () => {
    const [loading, setLoading] = useState(false);
    const navigate = useNavigate();
    const [form] = Form.useForm();

    const onFinish = async (values: any) => {
        try {
            setLoading(true);
            const registerRequest: RegisterRequest = {
                username: values.username,
                email: values.email,
                phone: values.phone,
                password: values.password,
                confirmPassword: values.confirmPassword,
                loginType: 'username_password'
            };
            
            const response = await authService.register(registerRequest);
            if (response.success) {
                message.success(response.message || '注册成功！');
                navigate('/login');
            } else {
                message.error(response.message || '注册失败，请重试');
            }
        } catch (error: any) {
            message.error(error.response?.data?.message || '注册失败，请重试');
        } finally {
            setLoading(false);
        }
    };

    const onLogin = () => {
        navigate('/login');
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
                <h2 style={{ textAlign: 'center', marginBottom: 30 }}>用户注册</h2>
                <Form
                    form={form}
                    name="register"
                    onFinish={onFinish}
                >
                    <Form.Item
                        name="username"
                        rules={[
                            { required: true, message: '请输入用户名！' },
                            { min: 3, max: 20, message: '用户名长度必须在3-20个字符之间！' }
                        ]}
                    >
                        <Input 
                            prefix={<UserOutlined />} 
                            placeholder="用户名" 
                            size="large"
                        />
                    </Form.Item>

                    <Form.Item
                        name="email"
                        rules={[
                            { required: true, message: '请输入邮箱！' },
                            { type: 'email', message: '请输入有效的邮箱地址！' }
                        ]}
                    >
                        <Input 
                            prefix={<MailOutlined />} 
                            placeholder="邮箱" 
                            size="large"
                        />
                    </Form.Item>

                    <Form.Item
                        name="phone"
                        rules={[
                            { required: true, message: '请输入手机号！' },
                            { min: 8, max: 11, message: '手机号长度必须在8-11个字符之间！' },
                            { pattern: /^[0-9]+$/, message: '手机号只能包含数字！' }
                        ]}
                    >
                        <Input 
                            prefix={<PhoneOutlined />} 
                            placeholder="手机号" 
                            size="large"
                        />
                    </Form.Item>

                    <Form.Item
                        name="password"
                        rules={[
                            { required: true, message: '请输入密码！' },
                            { min: 6, max: 20, message: '密码长度必须在6-20个字符之间！' }
                        ]}
                    >
                        <Input.Password
                            prefix={<LockOutlined />}
                            placeholder="密码"
                            size="large"
                        />
                    </Form.Item>

                    <Form.Item
                        name="confirmPassword"
                        dependencies={['password']}
                        rules={[
                            { required: true, message: '请确认密码！' },
                            ({ getFieldValue }) => ({
                                validator(_, value) {
                                    if (!value || getFieldValue('password') === value) {
                                        return Promise.resolve();
                                    }
                                    return Promise.reject(new Error('两次输入的密码不一致！'));
                                },
                            }),
                        ]}
                    >
                        <Input.Password
                            prefix={<LockOutlined />}
                            placeholder="确认密码"
                            size="large"
                        />
                    </Form.Item>

                    <Form.Item>
                        <Button type="primary" htmlType="submit" loading={loading} block size="large">
                            注册
                        </Button>
                    </Form.Item>

                    <div style={{ textAlign: 'center' }}>
                        已有账号？ <Button type="link" onClick={onLogin}>立即登录</Button>
                    </div>
                </Form>
            </Card>
        </div>
    );
};

export default Register; 