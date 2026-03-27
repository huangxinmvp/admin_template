import { PageContainer, ProCard } from '@ant-design/pro-components';
import { useModel } from '@umijs/max';
import { App, Button, Col, Empty, Form, Input, InputNumber, Row, Spin, Switch, Tabs, Tag, Typography } from 'antd';
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
    default:
      return <Input placeholder={item.placeholder || undefined} />;
  }
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
          <Row gutter={[16, 0]}>
            {group.items.map((item) => (
              <Col key={item.configKey} span={isFullRowType(item) ? 24 : 12}>
                <Form.Item
                  extra={item.description}
                  label={item.configName}
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
              当前配置会优先覆盖服务端默认值；未填写的配置会自动回退到系统内置默认值。
            </Typography.Paragraph>
            <Tabs items={tabs} />
          </Form>
        )}
      </ProCard>
    </PageContainer>
  );
};

export default SystemConfigCenterPage;
