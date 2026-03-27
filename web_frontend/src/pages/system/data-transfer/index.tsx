import { DownloadOutlined, UploadOutlined } from '@ant-design/icons';
import { PageContainer, ProCard } from '@ant-design/pro-components';
import { App, Button, Col, Empty, Row, Space, Statistic, Tag, Typography } from 'antd';
import React from 'react';
import {
  exportResourceExcel,
  importResourceExcel,
  pickLocalFile,
} from '@/services/backend/resources';

const transferResources = [
  { title: '用户管理', resourcePath: '/api/user', allowImport: true, description: '支持用户基础数据导入导出。' },
  { title: '角色管理', resourcePath: '/api/role', allowImport: true, description: '适合初始角色模板分发。' },
  { title: '权限菜单', resourcePath: '/api/permission', allowImport: true, description: '适合初始化菜单树与按钮权限模板。' },
  { title: '租户管理', resourcePath: '/api/tenant', allowImport: true, description: '用于租户初始化或迁移。' },
  { title: '字典管理', resourcePath: '/api/dict', allowImport: true, description: '用于业务字典初始化。' },
  { title: '字典项管理', resourcePath: '/api/dictItem', allowImport: true, description: '用于字典项批量导入。' },
  { title: '公告管理', resourcePath: '/api/announcement', allowImport: true, description: '支持公告模板导入导出。' },
  { title: '公告送达', resourcePath: '/api/announcementSend', allowImport: true, description: '可用于审计或历史数据迁移。' },
  { title: '文件中心', resourcePath: '/api/file', allowImport: true, description: '导出文件元数据，导入文件记录。' },
  { title: '数据权限', resourcePath: '/api/permissionDataRule', allowImport: true, description: '适合规则模板迁移。' },
  { title: '定时任务', resourcePath: '/api/quartzJob', allowImport: true, description: '批量同步任务定义。' },
  { title: '任务日志', resourcePath: '/api/quartzJobLog', allowImport: true, description: '适合历史执行结果留档。' },
  { title: '系统日志', resourcePath: '/api/log', allowImport: true, description: '支持日志快照导出。' },
] as const;

const DataTransferCenterPage: React.FC = () => {
  const { message } = App.useApp();

  const handleImport = async (resourcePath: string) => {
    const file = await pickLocalFile('.xlsx,.xls');
    if (!file) {
      return;
    }
    const response = await importResourceExcel(resourcePath, file);
    message.success(response.message || response.result || '导入成功');
  };

  return (
    <PageContainer
      className="saas-page-container"
      title="数据传输"
      content="统一管理各模块的导入导出能力，适合初始化数据、迁移数据和定期归档。"
      extra={[
        <Tag key="resource-count" color="processing">
          支持 {transferResources.length} 个模块
        </Tag>,
      ]}
    >
      {!transferResources.length ? (
        <Empty description="暂无可用的数据传输模块" />
      ) : (
        <>
          <Row gutter={[16, 16]}>
            <Col lg={8} xs={24}>
              <ProCard>
                <Statistic title="支持导出的模块" value={transferResources.length} />
              </ProCard>
            </Col>
            <Col lg={8} xs={24}>
              <ProCard>
                <Statistic
                  title="支持导入的模块"
                  value={transferResources.filter((item) => item.allowImport).length}
                />
              </ProCard>
            </Col>
            <Col lg={8} xs={24}>
              <ProCard>
                <Typography.Paragraph style={{ marginBottom: 0 }}>
                  导出会沿用各资源页当前控制器的 Excel 能力，导入则统一走 `importExcel`。
                </Typography.Paragraph>
                <Typography.Paragraph style={{ marginBottom: 0 }}>
                  流程草稿和流程定义属于独立页，导入导出入口请在对应流程页面中直接操作。
                </Typography.Paragraph>
              </ProCard>
            </Col>
          </Row>
          <Row gutter={[16, 16]} style={{ marginTop: 4 }}>
            {transferResources.map((item) => (
              <Col key={item.resourcePath} lg={8} md={12} xs={24}>
                <ProCard title={item.title}>
                  <Typography.Paragraph type="secondary">
                    {item.description}
                  </Typography.Paragraph>
                  <Typography.Paragraph type="secondary" style={{ wordBreak: 'break-all' }}>
                    {item.resourcePath}
                  </Typography.Paragraph>
                  <Space wrap>
                    <Button
                      icon={<DownloadOutlined />}
                      onClick={() =>
                        void exportResourceExcel(
                          item.resourcePath,
                          {},
                          item.title.replace(/\s+/g, '_'),
                        )
                      }
                    >
                      导出
                    </Button>
                    {item.allowImport ? (
                      <Button
                        icon={<UploadOutlined />}
                        type="primary"
                        onClick={() => void handleImport(item.resourcePath)}
                      >
                        导入
                      </Button>
                    ) : null}
                  </Space>
                </ProCard>
              </Col>
            ))}
          </Row>
        </>
      )}
    </PageContainer>
  );
};

export default DataTransferCenterPage;
