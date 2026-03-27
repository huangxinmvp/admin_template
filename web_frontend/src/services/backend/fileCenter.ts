import { request } from '@umijs/max';
import type {
  BackendResponse,
  FileCenterStats,
} from './types';

export const getFileCenterStats = async () =>
  request<BackendResponse<FileCenterStats>>('/api/file/stats', {
    method: 'GET',
  });
