package com.hiking.treasure.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.hiking.treasure.common.exception.BusinessException;
import com.hiking.treasure.common.security.SecurityUtils;
import com.hiking.treasure.domain.convert.AnnouncementConvert;
import com.hiking.treasure.domain.vo.AnnouncementVO;
import com.hiking.treasure.entity.Announcement;
import com.hiking.treasure.entity.AnnouncementSend;
import com.hiking.treasure.entity.User;
import com.hiking.treasure.mapper.AnnouncementMapper;
import com.hiking.treasure.service.AnnouncementSendService;
import com.hiking.treasure.service.AnnouncementService;
import com.hiking.treasure.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * <p>
 * 系统通告 服务实现类
 * </p>
 *
 * @author hx
 * @since 2025-09-04
 */
@Service
@RequiredArgsConstructor
public class AnnouncementServiceImpl extends ServiceImpl<AnnouncementMapper, Announcement> implements AnnouncementService {

    private final AnnouncementSendService announcementSendService;
    private final UserService userService;
    private final AnnouncementConvert announcementConvert;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean publish(String announcementId) {
        Announcement announcement = requireAnnouncement(announcementId);
        if (Integer.valueOf(1).equals(announcement.getSendStatus())) {
            return true;
        }
        String receiverScope = normalizeReceiverScope(announcement.getReceiverScope());
        if ("USER".equals(receiverScope)) {
            throw new BusinessException(400, "当前版本暂不支持按指定用户发布，请使用全部或租户范围");
        }

        List<User> targetUsers = resolveTargetUsers(announcement, receiverScope);
        if (targetUsers.isEmpty()) {
            throw new BusinessException(400, "当前通告没有可发布的接收人");
        }

        announcementSendService.remove(new LambdaQueryWrapper<AnnouncementSend>()
                .eq(AnnouncementSend::getAnntId, announcementId));
        LocalDateTime now = LocalDateTime.now();
        for (User user : targetUsers) {
            AnnouncementSend announcementSend = new AnnouncementSend();
            announcementSend.setTenantId(user.getTenantId());
            announcementSend.setAnntId(announcementId);
            announcementSend.setUserId(user.getId());
            announcementSend.setReadFlag(0);
            announcementSendService.save(announcementSend);
        }

        if (!StringUtils.hasText(announcement.getSender())) {
            announcement.setSender(Objects.requireNonNullElse(SecurityUtils.getUsername(), "system"));
        }
        announcement.setReceiverScope(receiverScope);
        announcement.setSendStatus(1);
        announcement.setSendTime(now);
        announcement.setCancelTime(null);
        return updateById(announcement);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean revoke(String announcementId) {
        Announcement announcement = requireAnnouncement(announcementId);
        announcement.setSendStatus(2);
        announcement.setCancelTime(LocalDateTime.now());
        return updateById(announcement);
    }

    @Override
    public Page<AnnouncementVO> pageInbox(long pageNo, long pageSize, Boolean unreadOnly) {
        String userId = SecurityUtils.getRequiredUserId();
        LambdaQueryWrapper<AnnouncementSend> wrapper = new LambdaQueryWrapper<AnnouncementSend>()
                .eq(AnnouncementSend::getUserId, userId)
                .orderByAsc(AnnouncementSend::getReadFlag)
                .orderByDesc(AnnouncementSend::getReadTime)
                .orderByDesc(AnnouncementSend::getAnntId);
        if (Boolean.TRUE.equals(unreadOnly)) {
            wrapper.eq(AnnouncementSend::getReadFlag, 0);
        }

        Page<AnnouncementSend> sendPage = announcementSendService.page(new Page<>(pageNo, pageSize), wrapper);
        Page<AnnouncementVO> voPage = new Page<>(pageNo, pageSize, sendPage.getTotal());
        if (sendPage.getRecords().isEmpty()) {
            return voPage;
        }

        List<String> announcementIds = sendPage.getRecords().stream()
                .map(AnnouncementSend::getAnntId)
                .distinct()
                .toList();
        Map<String, Announcement> announcementMap = list(new LambdaQueryWrapper<Announcement>()
                .in(Announcement::getId, announcementIds)
                .eq(Announcement::getSendStatus, 1))
                .stream()
                .collect(Collectors.toMap(Announcement::getId, Function.identity()));

        List<AnnouncementVO> records = sendPage.getRecords().stream()
                .map(send -> toInboxVO(announcementMap.get(send.getAnntId()), send))
                .filter(Objects::nonNull)
                .toList();
        voPage.setRecords(records);
        return voPage;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean markRead(String announcementId) {
        AnnouncementSend announcementSend = announcementSendService.getOne(new LambdaQueryWrapper<AnnouncementSend>()
                .eq(AnnouncementSend::getAnntId, announcementId)
                .eq(AnnouncementSend::getUserId, SecurityUtils.getRequiredUserId())
                .last("limit 1"));
        if (announcementSend == null) {
            throw new BusinessException(404, "通告不存在或当前用户无权阅读");
        }
        if (!Integer.valueOf(1).equals(announcementSend.getReadFlag())) {
            announcementSend.setReadFlag(1);
            announcementSend.setReadTime(LocalDateTime.now());
            return announcementSendService.updateById(announcementSend);
        }
        return true;
    }

    @Override
    public long unreadCount() {
        return announcementSendService.count(new LambdaQueryWrapper<AnnouncementSend>()
                .eq(AnnouncementSend::getUserId, SecurityUtils.getRequiredUserId())
                .eq(AnnouncementSend::getReadFlag, 0));
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public AnnouncementVO getDetail(String announcementId, boolean autoRead) {
        Announcement announcement = requireAnnouncement(announcementId);
        boolean privileged = SecurityUtils.hasRole("ADMIN") || SecurityUtils.hasAuthority("sys:announcement:view");
        AnnouncementSend announcementSend = null;
        if (!privileged) {
            announcementSend = announcementSendService.getOne(new LambdaQueryWrapper<AnnouncementSend>()
                    .eq(AnnouncementSend::getAnntId, announcementId)
                    .eq(AnnouncementSend::getUserId, SecurityUtils.getRequiredUserId())
                    .last("limit 1"));
            if (announcementSend == null || !Integer.valueOf(1).equals(announcement.getSendStatus())) {
                throw new BusinessException(403, "当前用户无权查看该通告");
            }
        } else {
            announcementSend = announcementSendService.getOne(new LambdaQueryWrapper<AnnouncementSend>()
                    .eq(AnnouncementSend::getAnntId, announcementId)
                    .eq(AnnouncementSend::getUserId, SecurityUtils.getRequiredUserId())
                    .last("limit 1"));
        }

        if (autoRead && announcementSend != null && !Integer.valueOf(1).equals(announcementSend.getReadFlag())) {
            announcementSend.setReadFlag(1);
            announcementSend.setReadTime(LocalDateTime.now());
            announcementSendService.updateById(announcementSend);
        }
        return toInboxVO(announcement, announcementSend);
    }

    private Announcement requireAnnouncement(String announcementId) {
        Announcement announcement = getById(announcementId);
        if (announcement == null) {
            throw new BusinessException(404, "通告不存在");
        }
        return announcement;
    }

    private String normalizeReceiverScope(String receiverScope) {
        return StringUtils.hasText(receiverScope) ? receiverScope.trim().toUpperCase() : "ALL";
    }

    private List<User> resolveTargetUsers(Announcement announcement, String receiverScope) {
        LambdaQueryWrapper<User> wrapper = new LambdaQueryWrapper<User>()
                .eq(User::getStatus, 1);
        String currentTenantId = SecurityUtils.getTenantId();
        String targetTenantId = StringUtils.hasText(announcement.getTargetTenantId())
                ? announcement.getTargetTenantId()
                : currentTenantId;
        if ("TENANT".equals(receiverScope)) {
            if (!StringUtils.hasText(targetTenantId)) {
                throw new BusinessException(400, "按租户发布时必须指定目标租户");
            }
            wrapper.eq(User::getTenantId, targetTenantId);
        } else if (StringUtils.hasText(currentTenantId)) {
            wrapper.eq(User::getTenantId, currentTenantId);
        }
        return userService.list(wrapper);
    }

    private AnnouncementVO toInboxVO(Announcement announcement, AnnouncementSend announcementSend) {
        if (announcement == null) {
            return null;
        }
        AnnouncementVO vo = announcementConvert.toVO(announcement);
        if (announcementSend != null) {
            vo.setReadFlag(announcementSend.getReadFlag());
            vo.setReadTime(announcementSend.getReadTime());
        }
        return vo;
    }
}
