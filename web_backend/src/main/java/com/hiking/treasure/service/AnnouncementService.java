package com.hiking.treasure.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.hiking.treasure.domain.vo.AnnouncementVO;
import com.hiking.treasure.entity.Announcement;
import com.baomidou.mybatisplus.extension.service.IService;

/**
 * <p>
 * 系统通告 服务类
 * </p>
 *
 * @author hx
 * @since 2025-09-04
 */
public interface AnnouncementService extends IService<Announcement> {
    boolean publish(String announcementId);
    boolean revoke(String announcementId);
    Page<AnnouncementVO> pageInbox(long pageNo, long pageSize, Boolean unreadOnly);
    boolean markRead(String announcementId);
    long unreadCount();
    AnnouncementVO getDetail(String announcementId, boolean autoRead);
}
