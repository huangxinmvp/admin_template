import type { ActionType, ProColumns } from '@ant-design/pro-components';
import { PageContainer, ProTable } from '@ant-design/pro-components';
import { history, useSearchParams } from '@umijs/max';
import { App, Button, Select, Space, Typography } from 'antd';
import React, { useEffect, useMemo, useRef, useState } from 'react';
import { BudgetCenterDetailDrawer } from './BudgetCenterDetailDrawer';
import {
  budgetStatusOptions,
  formatBudgetAmount,
  getOptionLabel,
  projectTypeOptions,
  renderBudgetStatusTag,
} from '@/features/aicoos/projectCenter';
import { formatDateTime } from '@/features/workflow/utils';
import { getBudgetCenterPage } from '@/services/backend/aicoos';
import { queryPageOptions } from '@/services/backend/system';
import type { BudgetCenterListItem, OptionItem } from '@/services/backend/types';

const PROJECT_RESOURCE_PATH = '/api/aicoos/project';

const BudgetCenterPage: React.FC = () => {
  const { message } = App.useApp();
  const actionRef = useRef<ActionType | null>(null);
  const [searchParams, setSearchParams] = useSearchParams();
  const [projectOptions, setProjectOptions] = useState<OptionItem[]>([]);
  const [selectedProjectId, setSelectedProjectId] = useState<string | undefined>(
    searchParams.get('projectId') || undefined,
  );
  const [detailProjectId, setDetailProjectId] = useState<string>();

  const selectedProjectLabel = useMemo(
    () => projectOptions.find((item) => String(item.value) === selectedProjectId)?.label,
    [projectOptions, selectedProjectId],
  );

  const syncProjectId = (projectId?: string) => {
    setSelectedProjectId(projectId);
    const nextParams = new URLSearchParams();
    if (projectId) {
      nextParams.set('projectId', projectId);
    }
    setSearchParams(nextParams);
    actionRef.current?.reload();
  };

  useEffect(() => {
    let active = true;
    const loadProjects = async () => {
      try {
        const options = await queryPageOptions(PROJECT_RESOURCE_PATH, 'projectName');
        if (active) {
          setProjectOptions(options);
        }
      } catch (_error) {
        if (active) {
          message.error('加载项目选项失败');
        }
      }
    };
    void loadProjects();
    return () => {
      active = false;
    };
  }, [message]);

  const columns: ProColumns<BudgetCenterListItem>[] = [
    {
      title: '项目',
      dataIndex: 'projectName',
      search: false,
      width: 260,
      render: (_, record) => (
        <Space direction="vertical" size={2}>
          <Typography.Text strong>{record.projectName || '-'}</Typography.Text>
          <Typography.Text type="secondary">{record.projectCode || '-'}</Typography.Text>
        </Space>
      ),
    },
    {
      title: '关键字',
      dataIndex: 'keyword',
      hideInTable: true,
    },
    {
      title: '项目类型',
      dataIndex: 'projectType',
      valueType: 'select',
      width: 132,
      valueEnum: Object.fromEntries(
        projectTypeOptions.map((item) => [item.value, { text: item.label }]),
      ),
      render: (value) => getOptionLabel(projectTypeOptions, value as string),
    },
    {
      title: '预算状态',
      dataIndex: 'budgetStatus',
      valueType: 'select',
      width: 132,
      valueEnum: Object.fromEntries(
        budgetStatusOptions.map((item) => [item.value, { text: item.label }]),
      ),
      render: (_, record) => renderBudgetStatusTag(record.budgetStatus),
    },
    {
      title: '总预算',
      dataIndex: 'totalBudgetAmount',
      hideInSearch: true,
      width: 138,
      render: (_, record) => formatBudgetAmount(record.totalBudgetAmount, record.currencyCode || 'TOKEN'),
    },
    {
      title: '锁定预算',
      dataIndex: 'lockedAmount',
      hideInSearch: true,
      width: 138,
      render: (_, record) => formatBudgetAmount(record.lockedAmount, record.currencyCode || 'TOKEN'),
    },
    {
      title: '已消耗',
      dataIndex: 'consumedAmount',
      hideInSearch: true,
      width: 138,
      render: (_, record) => formatBudgetAmount(record.consumedAmount, record.currencyCode || 'TOKEN'),
    },
    {
      title: '待增补',
      dataIndex: 'pendingIncreaseAmount',
      hideInSearch: true,
      width: 138,
      render: (_, record) => formatBudgetAmount(record.pendingIncreaseAmount, record.currencyCode || 'TOKEN'),
    },
    {
      title: '最后更新',
      dataIndex: 'lastUpdatedTime',
      hideInSearch: true,
      width: 180,
      valueType: 'dateTime',
      render: (value) => formatDateTime(value as string),
    },
    {
      title: '操作',
      key: 'action',
      valueType: 'option',
      width: 140,
      render: (_, record) => [
        <a
          key="detail"
          onClick={() => {
            setDetailProjectId(record.projectId);
          }}
        >
          详情
        </a>,
        <a
          key="project"
          onClick={() => {
            history.push(`/aicoos/projects?projectId=${record.projectId}`);
          }}
        >
          项目中心
        </a>,
      ],
    },
  ];

  return (
    <PageContainer
      className="saas-page-container"
      title="预算中心"
      subTitle={
        selectedProjectLabel
          ? `当前聚焦项目：${selectedProjectLabel}`
          : '按项目查看预算健康、角色分配与预算台账。'
      }
      extra={[
        <Select
          key="project-filter"
          allowClear
          options={projectOptions}
          placeholder="按项目筛选"
          style={{ width: 240 }}
          value={selectedProjectId}
          onChange={(value) => {
            syncProjectId(value ? String(value) : undefined);
          }}
        />,
        <Button
          key="clear-filter"
          disabled={!selectedProjectId}
          onClick={() => {
            syncProjectId(undefined);
          }}
        >
          清空项目筛选
        </Button>,
      ]}
    >
      <ProTable<BudgetCenterListItem>
        actionRef={actionRef}
        columns={columns}
        rowKey="projectId"
        request={async (params) => {
          const response = await getBudgetCenterPage({
            current: params.current,
            pageSize: params.pageSize,
            projectId: selectedProjectId,
            keyword: params.keyword as string | undefined,
            projectType: params.projectType as string | undefined,
            budgetStatus: params.budgetStatus as string | undefined,
          });
          const result = response.result;
          return {
            data: result.records || [],
            success: response.success,
            total: result.total || 0,
          };
        }}
        search={{
          labelWidth: 88,
          defaultCollapsed: false,
        }}
        scroll={{ x: 1200 }}
        toolBarRender={false}
      />

      <BudgetCenterDetailDrawer
        open={!!detailProjectId}
        projectId={detailProjectId}
        onChanged={() => {
          actionRef.current?.reload();
        }}
        onClose={() => {
          setDetailProjectId(undefined);
        }}
      />
    </PageContainer>
  );
};

export default BudgetCenterPage;
