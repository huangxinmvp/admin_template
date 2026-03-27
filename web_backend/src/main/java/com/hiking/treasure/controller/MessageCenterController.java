package com.hiking.treasure.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.hiking.treasure.common.api.vo.Result;
import com.hiking.treasure.common.security.SecurityUtils;
import com.hiking.treasure.domain.vo.system.MessageCenterChannelVO;
import com.hiking.treasure.domain.vo.system.MessageCenterSummaryVO;
import com.hiking.treasure.entity.Announcement;
import com.hiking.treasure.entity.AnnouncementSend;
import com.hiking.treasure.service.AnnouncementSendService;
import com.hiking.treasure.service.AnnouncementService;
import com.hiking.treasure.service.SystemConfigService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;

@Tag(name = "消息中心")
@RestController
@RequestMapping("/api/system/message-center")
@RequiredArgsConstructor
public class MessageCenterController {

    private final AnnouncementService announcementService;
    private final AnnouncementSendService announcementSendService;
    private final SystemConfigService systemConfigService;

    @Operation(summary = "消息中心概览")
    @PreAuthorize("hasRole('ADMIN') or hasAuthority('sys:messageCenter:view')")
    @GetMapping("/summary")
    public Result<MessageCenterSummaryVO> summary() {
        MessageCenterSummaryVO vo = new MessageCenterSummaryVO();
        LocalDateTime now = LocalDateTime.now();

        vo.setAnnouncementCount(announcementService.count());
        vo.setPublishedAnnouncementCount(announcementService.count(
                new LambdaQueryWrapper<Announcement>().eq(Announcement::getSendStatus, 1)
        ));
        vo.setDraftAnnouncementCount(announcementService.count(
                new LambdaQueryWrapper<Announcement>().eq(Announcement::getSendStatus, 0)
        ));
        vo.setRevokedAnnouncementCount(announcementService.count(
                new LambdaQueryWrapper<Announcement>().eq(Announcement::getSendStatus, 2)
        ));
        vo.setActiveAnnouncementCount(announcementService.count(
                new LambdaQueryWrapper<Announcement>()
                        .eq(Announcement::getSendStatus, 1)
                        .and(wrapper -> wrapper.isNull(Announcement::getEndTime)
                                .or()
                                .ge(Announcement::getEndTime, now))
        ));

        vo.setDeliveryCount(announcementSendService.count());
        vo.setReadDeliveryCount(announcementSendService.count(
                new LambdaQueryWrapper<AnnouncementSend>().eq(AnnouncementSend::getReadFlag, 1)
        ));
        vo.setUnreadDeliveryCount(announcementSendService.count(
                new LambdaQueryWrapper<AnnouncementSend>().eq(AnnouncementSend::getReadFlag, 0)
        ));

        String currentUserId = SecurityUtils.getUserId();
        if (StringUtils.hasText(currentUserId)) {
            vo.setPersonalUnreadCount(announcementSendService.count(
                    new LambdaQueryWrapper<AnnouncementSend>()
                            .eq(AnnouncementSend::getUserId, currentUserId)
                            .eq(AnnouncementSend::getReadFlag, 0)
            ));
        }

        vo.setReadRate(vo.getDeliveryCount() == 0
                ? 0D
                : (double) vo.getReadDeliveryCount() * 100D / (double) vo.getDeliveryCount());

        vo.getChannels().add(buildEmailChannel());
        vo.getChannels().add(buildSmsChannel());
        vo.getChannels().add(buildWebhookChannel());
        return Result.ok(vo);
    }

    private MessageCenterChannelVO buildEmailChannel() {
        String fromName = systemConfigService.getString("notification.email.fromName", "");
        String fromAddress = systemConfigService.getString("notification.email.fromAddress", "");
        boolean configured = StringUtils.hasText(fromName) && StringUtils.hasText(fromAddress);

        MessageCenterChannelVO channel = new MessageCenterChannelVO();
        channel.setCode("email");
        channel.setName("邮件通知");
        channel.setConfigured(configured);
        channel.setSummary(configured ? fromName + " <" + fromAddress + ">" : "未配置发送者名称或邮箱地址");
        channel.setTargetPath("/system/config-center");
        return channel;
    }

    private MessageCenterChannelVO buildSmsChannel() {
        String sign = systemConfigService.getString("notification.sms.sign", "");
        MessageCenterChannelVO channel = new MessageCenterChannelVO();
        channel.setCode("sms");
        channel.setName("短信通知");
        channel.setConfigured(StringUtils.hasText(sign));
        channel.setSummary(StringUtils.hasText(sign) ? "当前短信签名：" + sign : "未配置短信签名");
        channel.setTargetPath("/system/config-center");
        return channel;
    }

    private MessageCenterChannelVO buildWebhookChannel() {
        String webhookUrl = systemConfigService.getString("notification.webhook.url", "");
        MessageCenterChannelVO channel = new MessageCenterChannelVO();
        channel.setCode("webhook");
        channel.setName("Webhook");
        channel.setConfigured(StringUtils.hasText(webhookUrl));
        channel.setSummary(StringUtils.hasText(webhookUrl) ? webhookUrl : "未配置回调地址");
        channel.setTargetPath("/system/config-center");
        return channel;
    }
}
