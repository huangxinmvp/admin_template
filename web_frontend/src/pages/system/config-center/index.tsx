import { PageContainer, ProCard } from '@ant-design/pro-components';
import { useModel } from '@umijs/max';
import { Alert, App, Button, Col, Empty, Form, Input, InputNumber, Row, Spin, Switch, Tabs, Tag, Typography } from 'antd';
import React, { useEffect, useMemo, useState } from 'react';
import {
  INLINE_FORM_LABEL_COL,
  INLINE_FORM_WRAPPER_COL,
} from '@/constants/formLayout';
import { getSystemConfigGroups, updateSystemConfigBatch } from '@/services/backend/system';
import type { SystemConfigGroup, SystemConfigItem } from '@/services/backend/types';
import { extractBrandingFromGroups } from '@/utils/branding';

const { TextArea } = Input;

const isFullRowType = (item: SystemConfigItem) =>
  item.valueType === 'textarea';

const toInitialValue = (item: SystemConfigItem) => {
  const value = item.configValue ?? item.defaultValue ?? '';
  switch (item.valueType) {
    case 'boolean':
      return value === 'true' || value === '1';
    case 'number':
      return value ? Number(value) : undefined;
    case 'password':
      return '';
    default:
      return value;
  }
};

const toSubmitValue = (item: SystemConfigItem, value: any) => {
  switch (item.valueType) {
    case 'boolean':
      return value ? 'true' : 'false';
    case 'number':
      return value === undefined || value === null || value === '' ? '' : String(value);
    default:
      return value ?? '';
  }
};

const renderField = (item: SystemConfigItem) => {
  switch (item.valueType) {
    case 'boolean':
      return <Switch checkedChildren="开" unCheckedChildren="关" />;
    case 'number':
      return <InputNumber min={0} precision={0} style={{ width: '100%' }} />;
    case 'textarea':
      return <TextArea placeholder={item.placeholder || undefined} rows={4} showCount maxLength={500} />;
    case 'password':
      return (
        <Input.Password
          autoComplete="new-password"
          placeholder={item.placeholder || undefined}
        />
      );
    default:
      return <Input placeholder={item.placeholder || undefined} />;
  }
};

const toItemExtra = (item: SystemConfigItem) => {
  const extras = [item.description].filter(Boolean);
  if (item.environmentOverride) {
    extras.push('当前值由环境变量覆盖；页面中的值仅用于说明有效来源。');
  }
  if (item.valueType === 'password' && item.configured) {
    extras.push('当前已配置，留空则保持原值。');
  }
  return extras.join(' ');
};

const getValueSourceTag = (item: SystemConfigItem) => {
  if (item.environmentOverride || item.valueSource === 'environment') {
    return <Tag color="gold">环境变量覆盖</Tag>;
  }
  if (item.valueSource === 'database') {
    return <Tag>数据库值</Tag>;
  }
  if (item.valueSource === 'default') {
    return <Tag color="blue">默认值</Tag>;
  }
  return null;
};

const SystemConfigCenterPage: React.FC = () => {
  const { message } = App.useApp();
  const { setInitialState } = useModel('@@initialState');
  const [form] = Form.useForm();
  const [loading, setLoading] = useState(true);
  const [saving, setSaving] = useState(false);
  const [groups, setGroups] = useState<SystemConfigGroup[]>([]);

  const loadGroups = async () => {
    setLoading(true);
    try {
      const response = await getSystemConfigGroups();
      const nextGroups = response.result || [];
      setGroups(nextGroups);
      setInitialState((state: any) => ({
        ...state,
        branding: extractBrandingFromGroups(nextGroups),
      }));

      const initialValues = nextGroups.reduce<Record<string, any>>((acc, group) => {
        group.items.forEach((item) => {
          acc[item.configKey] = toInitialValue(item);
        });
        return acc;
      }, {});

      form.setFieldsValue(initialValues);
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    void loadGroups();
  }, []);

  const tabs = useMemo(
    () =>
      groups.map((group) => ({
        key: group.groupCode,
        label: group.groupName,
        children: (
          <>
            {group.groupCode === 'toolIntegration' ? (
              <Alert
                description="工具集成 API Key 在当前版本会被掩码展示，但仍保存在系统配置表中。这适合开发、联调和受控演示，不等同生产级 secret manager。"
                showIcon
                style={{ marginBottom: 16 }}
                type="warning"
              />
            ) : null}
            <Row gutter={[16, 0]}>
              {group.items.map((item) => (
                <Col key={item.configKey} span={isFullRowType(item) ? 24 : 12}>
                  <Form.Item
                    extra={toItemExtra(item)}
                    label={
                      <Row align="middle" gutter={8} wrap={false}>
                        <Col flex="auto">{item.configName}</Col>
                        <Col>{getValueSourceTag(item)}</Col>
                      </Row>
                    }
                    name={item.configKey}
                    rules={
                      item.requiredFlag
                        ? [{ required: true, message: `请输入${item.configName}` }]
                        : undefined
                    }
                    valuePropName={item.valueType === 'boolean' ? 'checked' : 'value'}
                  >
                    {renderField(item)}
                  </Form.Item>
                </Col>
              ))}
            </Row>
          </>
        ),
      })),
    [groups],
  );

  const handleSave = async () => {
    const values = await form.validateFields();
    const allItems = groups.flatMap((group) => group.items);
    setSaving(true);
    try {
      await updateSystemConfigBatch(
        allItems.map((item) => ({
          configKey: item.configKey,
          configValue: toSubmitValue(item, values[item.configKey]),
        })),
      );
      message.success('系统配置已保存');
      await loadGroups();
    } finally {
      setSaving(false);
    }
  };

  return (
    <PageContainer
      className="saas-page-container"
      title="系统配置"
      content="统一维护品牌信息、安全策略、文件策略与通知配置。安全策略保存后会直接影响密码校验与文件上传限制。"
      extra={[
        <Tag key="config-count" color="processing">
          配置项 {groups.reduce((sum, group) => sum + group.items.length, 0)}
        </Tag>,
        <Button key="reload" onClick={() => void loadGroups()}>
          刷新
        </Button>,
        <Button key="save" loading={saving} type="primary" onClick={() => void handleSave()}>
          保存配置
        </Button>,
      ]}
    >
      <ProCard className="saas-workflow-panel-card">
        {loading ? (
          <div style={{ padding: '80px 0', textAlign: 'center' }}>
            <Spin />
          </div>
        ) : !groups.length ? (
          <Empty description="当前还没有可维护的系统配置项" />
        ) : (
          <Form
            className="saas-inline-form"
            form={form}
            labelAlign="right"
            labelCol={INLINE_FORM_LABEL_COL}
            layout="horizontal"
            wrapperCol={INLINE_FORM_WRAPPER_COL}
          >
            <Typography.Paragraph type="secondary">
              当前配置按“环境变量 → 数据库配置 → 系统默认值”的优先级生效。敏感配置建议优先通过环境变量注入；当前数据库配置仍不等同生产级 secret
              manager。
            </Typography.Paragraph>
            <Tabs items={tabs} />
          </Form>
        )}
      </ProCard>
    </PageContainer>
  );
};

export default SystemConfigCenterPage;
