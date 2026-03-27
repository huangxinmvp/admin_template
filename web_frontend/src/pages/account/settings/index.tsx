import { PageContainer, ProCard, ProForm, ProFormText } from '@ant-design/pro-components';
import { history, useModel } from '@umijs/max';
import { App, Descriptions, Empty, Typography } from 'antd';
import React from 'react';
import {
  INLINE_FORM_LABEL_COL,
  INLINE_FORM_WRAPPER_COL,
} from '@/constants/formLayout';
import { changeCurrentPassword } from '@/services/backend/auth';

const AccountSettings: React.FC = () => {
  const { message } = App.useApp();
  const { initialState } = useModel('@@initialState');
  const currentUser = initialState?.currentUser;

  return (
    <PageContainer
      className="saas-page-container"
      title="安全设置"
      content="已接入 /api/auth/password，用于当前登录用户修改密码。"
      extra={[
        <a
          key="sessions"
          onClick={() => {
            history.push('/account/sessions');
          }}
        >
          前往会话管理
        </a>,
      ]}
    >
      {!currentUser ? (
        <Empty description="未获取到当前用户信息" />
      ) : (
        <ProCard split="vertical">
          <ProCard colSpan="40%" title="账户信息">
            <Descriptions column={1}>
              <Descriptions.Item label="用户名">
                {currentUser.username || '-'}
              </Descriptions.Item>
              <Descriptions.Item label="姓名">
                {currentUser.realname || currentUser.name || '-'}
              </Descriptions.Item>
              <Descriptions.Item label="邮箱">
                {currentUser.email || '-'}
              </Descriptions.Item>
              <Descriptions.Item label="手机号">
                {currentUser.phone || '-'}
              </Descriptions.Item>
            </Descriptions>
          </ProCard>
          <ProCard title="修改密码">
            <Typography.Paragraph type="secondary">
              后端密码策略在服务端校验，提交后若不符合复杂度要求，会直接返回真实错误信息。
            </Typography.Paragraph>
            <ProForm<{
              oldPassword: string;
              newPassword: string;
              confirmPassword: string;
            }>
              grid
              layout="horizontal"
              className="saas-inline-form"
              labelAlign="right"
              labelWrap={false}
              labelCol={INLINE_FORM_LABEL_COL}
              wrapperCol={INLINE_FORM_WRAPPER_COL}
              rowProps={{
                gutter: [16, 0],
              }}
              submitter={{
                searchConfig: {
                  submitText: '保存新密码',
                },
              }}
              onFinish={async (values) => {
                if (values.newPassword !== values.confirmPassword) {
                  message.error('两次输入的新密码不一致');
                  return false;
                }
                await changeCurrentPassword({
                  oldPassword: values.oldPassword,
                  newPassword: values.newPassword,
                });
                message.success('密码修改成功');
                return true;
              }}
            >
              <ProFormText.Password
                name="oldPassword"
                label="当前密码"
                colProps={{ xs: 24, md: 12 }}
                rules={[{ required: true, message: '请输入当前密码' }]}
              />
              <ProFormText.Password
                name="newPassword"
                label="新密码"
                colProps={{ xs: 24, md: 12 }}
                rules={[{ required: true, message: '请输入新密码' }]}
              />
              <ProFormText.Password
                name="confirmPassword"
                label="确认新密码"
                colProps={{ xs: 24, md: 12 }}
                rules={[{ required: true, message: '请再次输入新密码' }]}
              />
            </ProForm>
          </ProCard>
        </ProCard>
      )}
    </PageContainer>
  );
};

export default AccountSettings;
