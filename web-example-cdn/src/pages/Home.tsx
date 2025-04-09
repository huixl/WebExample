import { Button, Layout } from 'antd';
import React from 'react';
import { useNavigate } from 'react-router-dom';

const { Header, Content } = Layout;

const Home: React.FC = () => {
  const navigate = useNavigate();

  const handleLogout = () => {
    // 清除token
    localStorage.removeItem('token');
    // 跳转到登录页
    navigate('/login');
  };

  return (
    <Layout className="layout" style={{ minHeight: '100vh' }}>
      <Header style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center' }}>
        <div style={{ color: 'white', fontSize: '20px' }}>Web Example</div>
        <Button type="primary" onClick={handleLogout}>
          退出登录
        </Button>
      </Header>
      <Content style={{ padding: '50px', background: '#fff' }}>
        <h1>欢迎来到首页</h1>
        <p>这里是首页内容</p>
      </Content>
    </Layout>
  );
};

export default Home; 