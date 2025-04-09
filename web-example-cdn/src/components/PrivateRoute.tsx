import React from 'react';
import { Navigate } from 'react-router-dom';

interface PrivateRouteProps {
  children: React.ReactNode;
}

const PrivateRoute: React.FC<PrivateRouteProps> = ({ children }) => {
  const token = localStorage.getItem('token');

  if (!token) {
    // 如果没有token，重定向到登录页
    return <Navigate to="/login" replace />;
  }

  // 如果有token，渲染子组件
  return <>{children}</>;
};

export default PrivateRoute; 