import { Result } from 'antd';
import { useLocation } from '@umijs/max';
import React from 'react';
import { CrudPage } from '@/features/backend/CrudPage';
import { getResourceConfigByPath } from '@/features/backend/resourceMeta';

const ResourcePage: React.FC = () => {
  const location = useLocation();
  const config = getResourceConfigByPath(location.pathname);

  if (!config) {
    return (
      <Result
        status="404"
        title="未找到资源配置"
        subTitle={`当前路径 ${location.pathname} 没有对应的资源定义。`}
      />
    );
  }

  return <CrudPage resource={config} />;
};

export default ResourcePage;
