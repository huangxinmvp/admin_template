import { Link, useSearchParams } from '@umijs/max';
import { Button, Result } from 'antd';
import React from 'react';

const RegisterResult: React.FC = () => {
  const [params] = useSearchParams();
  const account = params?.get('account') || '-';

  return (
    <Result
      status="success"
      title={`账户 ${account} 注册成功`}
      subTitle="注册页已经切换为真实后端接口。你现在可以直接返回登录页完成登录。"
      extra={[
        <Link key="login" to="/user/login">
          <Button type="primary">去登录</Button>
        </Link>,
        <Link key="home" to="/">
          <Button>返回首页</Button>
        </Link>,
      ]}
    />
  );
};

export default RegisterResult;
