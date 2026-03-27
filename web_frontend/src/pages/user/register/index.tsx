import { LockOutlined, UserOutlined } from '@ant-design/icons';
import { LoginForm, ProFormText } from '@ant-design/pro-components';
import { Helmet, history } from '@umijs/max';
import { App } from 'antd';
import React from 'react';
import { Footer } from '@/components';
import {
  INLINE_FORM_LABEL_COL,
  INLINE_FORM_WRAPPER_COL,
} from '@/constants/formLayout';
import { registerUser } from '@/services/backend/auth';
import Settings from '../../../../config/defaultSettings';

const RegisterPage: React.FC = () => {
  const { message } = App.useApp();

  return (
    <div
      style={{
        minHeight: '100vh',
        display: 'flex',
        flexDirection: 'column',
        background: '#f8fafc',
      }}
    >
      <Helmet>
        <title>
          注册{Settings.title ? ` - ${Settings.title}` : ''}
        </title>
      </Helmet>
      <div
        style={{
          flex: 1,
          display: 'flex',
          alignItems: 'center',
          justifyContent: 'center',
          padding: 24,
        }}
      >
        <LoginForm<{
          tenantId?: string;
          username: string;
          realname?: string;
          password: string;
          confirmPassword: string;
        }>
          grid
          layout="horizontal"
          logo={<img alt="logo" src="/logo.svg" />}
          title="用户注册"
          subTitle="对接 /api/auth/register"
          className="saas-auth-form saas-inline-form"
          labelAlign="right"
          labelWrap={false}
          labelCol={INLINE_FORM_LABEL_COL}
          wrapperCol={INLINE_FORM_WRAPPER_COL}
          style={{
            width: 'min(860px, 100%)',
          }}
          rowProps={{
            gutter: [16, 0],
          }}
          onFinish={async (values) => {
            if (values.password !== values.confirmPassword) {
              message.error('两次输入的密码不一致');
              return;
            }
            await registerUser({
              tenantId: values.tenantId,
              username: values.username,
              realname: values.realname,
              password: values.password,
            });
            message.success('注册成功');
            history.push(`/user/register-result?account=${values.username}`);
          }}
        >
          <ProFormText
            name="tenantId"
            label="租户ID"
            colProps={{ xs: 24, md: 12 }}
            placeholder="选填，默认由后端决定"
          />
          <ProFormText
            name="username"
            label="用户名"
            colProps={{ xs: 24, md: 12 }}
            fieldProps={{
              prefix: <UserOutlined />,
            }}
            rules={[{ required: true, message: '请输入用户名' }]}
          />
          <ProFormText name="realname" label="姓名" colProps={{ xs: 24, md: 12 }} />
          <ProFormText.Password
            name="password"
            label="密码"
            colProps={{ xs: 24, md: 12 }}
            fieldProps={{
              prefix: <LockOutlined />,
            }}
            rules={[{ required: true, message: '请输入密码' }]}
          />
          <ProFormText.Password
            name="confirmPassword"
            label="确认密码"
            colProps={{ xs: 24, md: 12 }}
            rules={[{ required: true, message: '请再次输入密码' }]}
          />
        </LoginForm>
      </div>
      <Footer />
    </div>
  );
};

export default RegisterPage;
