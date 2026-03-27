import { Tag } from 'antd';
import dayjs from 'dayjs';
import type { GenericRecord } from '@/services/backend/types';

export const formatDateTime = (value?: string | null) =>
  value ? dayjs(value).format('YYYY-MM-DD HH:mm:ss') : '-';

export const renderWorkflowStatusTag = (finished?: boolean, suspended?: boolean) => {
  if (suspended) {
    return <Tag color="warning">已挂起</Tag>;
  }
  if (finished) {
    return <Tag color="success">已完成</Tag>;
  }
  return <Tag color="processing">进行中</Tag>;
};

export const parseWorkflowVariables = (raw?: string) => {
  const value = raw?.trim();
  if (!value) {
    return undefined;
  }

  try {
    const parsed = JSON.parse(value);
    if (!parsed || typeof parsed !== 'object' || Array.isArray(parsed)) {
      throw new Error('流程变量必须是 JSON 对象');
    }
    return parsed as GenericRecord;
  } catch (error) {
    const message =
      error instanceof Error ? error.message : '流程变量必须是合法的 JSON 对象';
    throw new Error(message);
  }
};

export const getWorkflowResourceName = (value?: string | null) => {
  if (!value) {
    return '-';
  }

  const normalized = value.replace(/\\/g, '/');
  return normalized.split('/').pop() || normalized;
};

export const stringifyWorkflowVariables = (value?: GenericRecord) => {
  if (!value || Object.keys(value).length === 0) {
    return '{}';
  }
  return JSON.stringify(value, null, 2);
};
