package com.hiking.treasure.service;

import com.hiking.treasure.common.security.LoginUser;
import com.hiking.treasure.domain.convert.AnnouncementConvert;
import com.hiking.treasure.domain.vo.AnnouncementVO;
import com.hiking.treasure.entity.Announcement;
import com.hiking.treasure.entity.AnnouncementSend;
import com.hiking.treasure.entity.User;
import com.hiking.treasure.mapper.AnnouncementMapper;
import com.hiking.treasure.service.impl.AnnouncementServiceImpl;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AnnouncementServiceImplTest {

    @Mock
    private AnnouncementMapper announcementMapper;
    @Mock
    private AnnouncementSendService announcementSendService;
    @Mock
    private UserService userService;
    @Mock
    private AnnouncementConvert announcementConvert;

    private AnnouncementServiceImpl announcementService;

    @BeforeEach
    void setUp() {
        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken(new LoginUser("u-admin", "admin", "t1", null,List.of("ADMIN")), null, List.of())
        );
        announcementService = new AnnouncementServiceImpl(announcementSendService, userService, announcementConvert);
        ReflectionTestUtils.setField(announcementService, "baseMapper", announcementMapper);
    }

    @AfterEach
    void clearSecurityContext() {
        SecurityContextHolder.clearContext();
    }

    @Test
    void publishCreatesAnnouncementSendRecordsForTenantUsers() {
        Announcement announcement = new Announcement();
        announcement.setId("a1");
        announcement.setReceiverScope("tenant");
        announcement.setTargetTenantId("t1");
        announcement.setSendStatus(0);

        User userA = new User();
        userA.setId("u1");
        userA.setTenantId("t1");

        User userB = new User();
        userB.setId("u2");
        userB.setTenantId("t1");

        when(announcementMapper.selectById("a1")).thenReturn(announcement);
        when(userService.list(org.mockito.ArgumentMatchers.<com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<User>>any()))
                .thenReturn(List.of(userA, userB));
        when(announcementSendService.remove(any())).thenReturn(true);
        when(announcementSendService.save(any())).thenReturn(true);
        when(announcementMapper.updateById(any(Announcement.class))).thenReturn(1);

        boolean published = announcementService.publish("a1");

        assertTrue(published);
        ArgumentCaptor<AnnouncementSend> sendCaptor = ArgumentCaptor.forClass(AnnouncementSend.class);
        verify(announcementSendService, times(2)).save(sendCaptor.capture());
        List<AnnouncementSend> relations = sendCaptor.getAllValues();
        assertEquals(List.of("u1", "u2"), relations.stream().map(AnnouncementSend::getUserId).toList());
        assertEquals(List.of("t1", "t1"), relations.stream().map(AnnouncementSend::getTenantId).toList());

        verify(announcementMapper).updateById(eq(announcement));
        assertEquals(Integer.valueOf(1), announcement.getSendStatus());
        assertEquals("TENANT", announcement.getReceiverScope());
        assertEquals("admin", announcement.getSender());
    }

    @Test
    void getDetailAutoMarksReadForInboxUser() {
        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken(new LoginUser("u1", "alice", "t1",null, List.of("USER")), null, List.of())
        );
        Announcement announcement = new Announcement();
        announcement.setId("a1");
        announcement.setSendStatus(1);

        AnnouncementSend announcementSend = new AnnouncementSend();
        announcementSend.setAnntId("a1");
        announcementSend.setUserId("u1");
        announcementSend.setReadFlag(0);

        AnnouncementVO announcementVO = new AnnouncementVO();
        announcementVO.setId("a1");

        when(announcementMapper.selectById("a1")).thenReturn(announcement);
        when(announcementSendService.getOne(any())).thenReturn(announcementSend);
        when(announcementSendService.updateById(any())).thenReturn(true);
        when(announcementConvert.toVO(announcement)).thenReturn(announcementVO);

        AnnouncementVO detail = announcementService.getDetail("a1", true);

        assertEquals(Integer.valueOf(1), detail.getReadFlag());
        assertNotNull(detail.getReadTime());
        verify(announcementSendService).updateById(eq(announcementSend));
    }
}
