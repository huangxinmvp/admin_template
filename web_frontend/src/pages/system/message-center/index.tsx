import { PageContainer, ProCard, StatisticCard } from '@ant-design/pro-components';
import { history } from '@umijs/max';
import {
  BellOutlined,
  MailOutlined,
  MessageOutlined,
  NotificationOutlined,
  SettingOutlined,
  UserOutlined,
} from '@ant-design/icons';
import { App, Button, Col, Empty, List, Progress, Row, Space, Spin, Tag, Typography } from 'antd';
import dayjs from 'dayjs';
import React, { useEffect, useMemo, useState } from 'react';
import { queryResourcePage } from '@/services/backend/resources';
import { getMessageCenterSummary } from '@/services/backend/system';
import type { GenericRecord, MessageCenterSummary } from '@/services/backend/types';

const formatTime = (value?: string | null) =>
  value ? dayjs(value).format('YYYY-MM-DD HH:mm:ss') : '-';

const sendStatusMap: Record<number, { label: string; color: string }> = {
  0: { label: '草稿', color: 'default' },
  1: { label: '已发布', color: 'success' },
  2: { label: '已撤销', color: 'warning' },
};

const readFlagMap: Record<number, { label: string; color: string }> = {
  0: { label: '未读', color: 'warning' },
  1: { label: '已读', color: 'success' },
};

const quickLinks = [
  { path: '/system/announcements', label: '公告管理', icon: <NotificationOutlined /> },
  { path: '/system/announcement-sends', label: '公告送达', icon: <MailOutlined /> },
  { path: '/account/inbox', label: '个人收件箱', icon: <UserOutlined /> },
  { path: '/system/config-center', label: '通知配置', icon: <SettingOutlined /> },
];

const MessageCenterPage: React.FC = () => {
  const { message } = App.useApp();
  const [loading, setLoading] = useState(true);
  const [summary, setSummary] = useState<MessageCenterSummary>();
  const [recentAnnouncements, setRecentAnnouncements] = useState<GenericRecord[]>([]);
  const [recentDeliveries, setRecentDeliveries] = useState<GenericRecord[]>([]);

  const loadData = async () => {
    setLoading(true);
    try {
      const [summaryResponse, announcementsResponse, deliveriesResponse] = await Promise.all([
        getMessageCenterSummary(),
        queryResourcePage('/api/announcement', {
          pageNo: 1,
          pageSize: 5,
        }),
        queryResourcePage('/api/announcementSend', {
          pageNo: 1,
          pageSize: 5,
        }),
      ]);

      setSummary(summaryResponse.result);
      setRecentAnnouncements(announcementsResponse.result.records || []);
      setRecentDeliveries(deliveriesResponse.result.records || []);
    } catch (error: any) {
      message.error(error?.message || '消息中心加载失败');
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    void loadData();
  }, []);

  const readRatePercent = useMemo(
    () => Math.max(0, Math.min(100, Number(summary?.readRate || 0))),
    [summary?.readRate],
  );

  return (
    <PageContainer
      className="saas-page-container saas-message-center"
      title="消息中心"
      content="统一查看公告投放情况、送达阅读表现，以及当前邮件、短信、Webhook 通知渠道的配置状态。"
      extra={[
        <Tag key="personal-unread" color="processing">
          我的未读 {summary?.personalUnreadCount ?? 0}
        </Tag>,
        <Button key="reload" onClick={() => void loadData()}>
          刷新
        </Button>,
      ]}
    >
      {loading ? (
        <div style={{ padding: '96px 0', textAlign: 'center' }}>
          <Spin />
        </div>
      ) : !summary ? (
        <Empty description="暂未加载到消息中心数据" />
      ) : (
        <Row gutter={[16, 16]}>
          <Col lg={6} md={12} xs={24}>
            <StatisticCard
              statistic={{
                title: '已发布公告',
                value: summary.publishedAnnouncementCount,
                description: `总公告 ${summary.announcementCount} / 当前生效 ${summary.activeAnnouncementCount}`,
                prefix: <BellOutlined />,
              }}
            />
          </Col>
          <Col lg={6} md={12} xs={24}>
            <StatisticCard
              statistic={{
                title: '草稿与撤销',
                value: `${summary.draftAnnouncementCount} / ${summary.revokedAnnouncementCount}`,
                description: '草稿数 / 已撤销数',
                prefix: <MessageOutlined />,
              }}
            />
          </Col>
          <Col lg={6} md={12} xs={24}>
            <StatisticCard
              statistic={{
                title: '送达总量',
                value: summary.deliveryCount,
                description: `已读 ${summary.readDeliveryCount} / 未读 ${summary.unreadDeliveryCount}`,
                prefix: <MailOutlined />,
              }}
            />
          </Col>
          <Col lg={6} md={12} xs={24}>
            <StatisticCard
              statistic={{
                title: '整体阅读率',
                value: `${readRatePercent.toFixed(readRatePercent >= 100 ? 0 : 1)}%`,
                description: '基于公告送达记录计算',
                prefix: <NotificationOutlined />,
              }}
            />
          </Col>

          <Col lg={14} xs={24}>
            <ProCard title="通知渠道状态" className="saas-workflow-panel-card">
              <div className="saas-message-center__channel-list">
                {(summary.channels || []).map((channel) => (
                  <div key={channel.code} className="saas-message-center__channel-item">
                    <div className="saas-message-center__channel-header">
                      <Space size={10}>
                        <Typography.Text strong>{channel.name}</Typography.Text>
                        <Tag color={channel.configured ? 'success' : 'warning'}>
                          {channel.configured ? '已配置' : '待配置'}
                        </Tag>
                      </Space>
                      {channel.targetPath ? (
                        <Button
                          size="small"
                          type="link"
                          onClick={() => {
                            history.push(channel.targetPath!);
                          }}
                        >
                          前往配置
                        </Button>
                      ) : null}
                    </div>
                    <Typography.Paragraph type="secondary" style={{ marginBottom: 0 }}>
                      {channel.summary}
                    </Typography.Paragraph>
                  </div>
                ))}
              </div>
              <div className="saas-message-center__read-rate">
                <div>
                  <Typography.Text strong>公告阅读完成度</Typography.Text>
                  <Typography.Paragraph type="secondary" style={{ marginBottom: 0 }}>
                    阅读率会随着个人收件箱打开和手动标记已读实时更新。
                  </Typography.Paragraph>
                </div>
                <Progress percent={Number(readRatePercent.toFixed(1))} strokeColor="#13c2c2" />
              </div>
            </ProCard>
          </Col>

          <Col lg={10} xs={24}>
            <ProCard title="快捷入口" className="saas-workflow-panel-card">
              <div className="saas-message-center__quick-links">
                {quickLinks.map((item) => (
                  <Button
                    key={item.path}
                    icon={item.icon}
                    onClick={() => {
                      history.push(item.path);
                    }}
                  >
                    {item.label}
                  </Button>
                ))}
              </div>
              <Typography.Paragraph type="secondary" style={{ marginBottom: 0 }}>
                公告的编辑、发布和送达详情仍在各自的独立资源页中维护；消息中心负责统一查看与跳转。
              </Typography.Paragraph>
            </ProCard>
          </Col>

          <Col lg={12} xs={24}>
            <ProCard
              title="最新公告"
              extra={
                <Button
                  size="small"
                  type="link"
                  onClick={() => {
                    history.push('/system/announcements');
                  }}
                >
                  查看全部
                </Button>
              }
            >
              <List<GenericRecord>
                dataSource={recentAnnouncements}
                locale={{ emptyText: '暂无公告记录' }}
                renderItem={(item) => {
                  const status = sendStatusMap[Number(item.sendStatus)] || {
                    label: String(item.sendStatus ?? '-'),
                    color: 'default',
                  };
                  return (
                    <List.Item
                      className="saas-message-center__list-item"
                      actions={[
                        <Button
                          key="detail"
                          size="small"
                          type="link"
                          onClick={() => {
                            history.push('/system/announcements');
                          }}
                        >
                          打开管理页
                        </Button>,
                      ]}
                    >
                      <List.Item.Meta
                        title={
                          <Space size={10}>
                            <Typography.Text strong>{item.title || '未命名公告'}</Typography.Text>
                            <Tag color={status.color}>{status.label}</Tag>
                          </Space>
                        }
                        description={`发布人：${item.sender || '-'} · 发布时间：${formatTime(item.sendTime || item.createTime)}`}
                      />
                    </List.Item>
                  );
                }}
              />
            </ProCard>
          </Col>

          <Col lg={12} xs={24}>
            <ProCard
              title="最新送达"
              extra={
                <Button
                  size="small"
                  type="link"
                  onClick={() => {
                    history.push('/system/announcement-sends');
                  }}
                >
                  查看全部
                </Button>
              }
            >
              <List<GenericRecord>
                dataSource={recentDeliveries}
                locale={{ emptyText: '暂无送达记录' }}
                renderItem={(item) => {
                  const status = readFlagMap[Number(item.readFlag)] || {
                    label: String(item.readFlag ?? '-'),
                    color: 'default',
                  };
                  return (
                    <List.Item
                      className="saas-message-center__list-item"
                      actions={[
                        <Button
                          key="delivery"
                          size="small"
                          type="link"
                          onClick={() => {
                            history.push('/system/announcement-sends');
                          }}
                        >
                          打开送达页
                        </Button>,
                      ]}
                    >
                      <List.Item.Meta
                        title={
                          <Space size={10}>
                            <Typography.Text strong>{item.anntId || '未知公告'}</Typography.Text>
                            <Tag color={status.color}>{status.label}</Tag>
                          </Space>
                        }
                        description={`接收人：${item.userId || '-'} · 阅读时间：${formatTime(item.readTime)}`}
                      />
                    </List.Item>
                  );
                }}
              />
            </ProCard>
          </Col>
        </Row>
      )}
    </PageContainer>
  );
};

export default MessageCenterPage;
