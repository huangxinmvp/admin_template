package com.hiking.treasure.domain.vo.system;

import lombok.Data;

import java.util.ArrayList;
import java.util.List;

@Data
public class MessageCenterSummaryVO {
    private long announcementCount;
    private long publishedAnnouncementCount;
    private long draftAnnouncementCount;
    private long revokedAnnouncementCount;
    private long activeAnnouncementCount;
    private long deliveryCount;
    private long readDeliveryCount;
    private long unreadDeliveryCount;
    private long personalUnreadCount;
    private double readRate;
    private List<MessageCenterChannelVO> channels = new ArrayList<>();
}
