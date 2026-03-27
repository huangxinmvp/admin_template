import { LockOutlined, UserOutlined } from '@ant-design/icons';
import { LoginForm, ProFormCheckbox, ProFormText } from '@ant-design/pro-components';
import { Helmet, SelectLang, useIntl, useModel } from '@umijs/max';
import { App } from 'antd';
import { createStyles } from 'antd-style';
import React from 'react';
import { flushSync } from 'react-dom';
import { Footer } from '@/components';
import { login, persistAuthSession } from '@/services/backend/auth';
import { normalizeBranding } from '@/utils/branding';
import Settings from '../../../../config/defaultSettings';

const useStyles = createStyles(({ token }) => ({
  lang: {
    width: 42,
    height: 42,
    lineHeight: '42px',
    position: 'fixed',
    right: 16,
    borderRadius: token.borderRadius,
    ':hover': {
      backgroundColor: token.colorBgTextHover,
    },
  },
  container: {
    display: 'flex',
    flexDirection: 'column',
    minHeight: '100vh',
    background:
      'linear-gradient(145deg, rgba(12,74,110,0.08), rgba(251,191,36,0.12)), #f8fafc',
  },
  content: {
    flex: 1,
    display: 'flex',
    alignItems: 'center',
    justifyContent: 'center',
    padding: '32px 16px',
  },
}));

const Lang = () => {
  const { styles } = useStyles();
  return (
    <div className={styles.lang} data-lang>
      {SelectLang && <SelectLang />}
    </div>
  );
};

const LoginPage: React.FC = () => {
  const { styles } = useStyles();
  const { message } = App.useApp();
  const intl = useIntl();
  const { initialState, setInitialState } = useModel('@@initialState');
  const branding = normalizeBranding(initialState?.branding);

  const fetchUserInfo = async () => {
    const userInfo = await initialState?.fetchUserInfo?.();
    if (!userInfo) {
      return;
    }
    flushSync(() => {
      setInitialState((state: any) => ({
        ...state,
        currentUser: userInfo,
      }));
    });
  };

  return (
    <div className={styles.container}>
      <Helmet>
        <title>
          登录{branding.siteName ? ` - ${branding.siteName}` : Settings.title ? ` - ${Settings.title}` : ''}
        </title>
      </Helmet>
      <Lang />
      <div className={styles.content}>
        <LoginForm<{
          username: string;
          password: string;
          autoLogin?: boolean;
        }>
          logo={<img alt="logo" src={branding.logoUrl || '/logo.svg'} />}
          title={branding.siteName}
          subTitle={branding.siteSubtitle || '前端已接入 Spring Boot 后端认证接口'}
          initialValues={{
            autoLogin: true,
          }}
          onFinish={async (values) => {
            const response = await login({
              username: values.username,
              password: values.password,
            });
            persistAuthSession(response.result);
            await fetchUserInfo();
            message.success(
              intl.formatMessage({
                id: 'pages.login.success',
                defaultMessage: '登录成功',
              }),
            );
            const urlParams = new URL(window.location.href).searchParams;
            window.location.href = urlParams.get('redirect') || '/';
          }}
        >
          <ProFormText
            name="username"
            fieldProps={{
              size: 'large',
              prefix: <UserOutlined />,
            }}
            placeholder="请输入用户名"
            rules={[{ required: true, message: '请输入用户名' }]}
          />
          <ProFormText.Password
            name="password"
            fieldProps={{
              size: 'large',
              prefix: <LockOutlined />,
            }}
            placeholder="请输入密码"
            rules={[{ required: true, message: '请输入密码' }]}
          />
          <div style={{ marginBottom: 24 }}>
            <ProFormCheckbox noStyle name="autoLogin">
              自动登录
            </ProFormCheckbox>
          </div>
        </LoginForm>
      </div>
      <Footer />
    </div>
  );
};

export default LoginPage;
