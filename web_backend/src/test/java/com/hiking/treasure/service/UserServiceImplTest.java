package com.hiking.treasure.service;

import com.hiking.treasure.domain.vo.UserVO;
import com.hiking.treasure.domain.vo.system.UserDetailVO;
import com.hiking.treasure.entity.Depart;
import com.hiking.treasure.entity.Role;
import com.hiking.treasure.entity.User;
import com.hiking.treasure.entity.UserDepart;
import com.hiking.treasure.entity.UserRole;
import com.hiking.treasure.mapper.RoleMapper;
import com.hiking.treasure.mapper.UserDepartMapper;
import com.hiking.treasure.mapper.UserMapper;
import com.hiking.treasure.mapper.UserRoleMapper;
import com.hiking.treasure.service.impl.UserServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertIterableEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class UserServiceImplTest {

    @Mock
    private UserMapper userMapper;
    @Mock
    private RoleMapper roleMapper;
    @Mock
    private UserDepartMapper userDepartMapper;
    @Mock
    private UserRoleMapper userRoleMapper;
    @Mock
    private DepartService departService;

    private UserServiceImpl userService;

    @BeforeEach
    void setUp() {
        userService = new UserServiceImpl(roleMapper, userDepartMapper, userRoleMapper, departService);
        ReflectionTestUtils.setField(userService, "baseMapper", userMapper);
    }

    @Test
    void getDetailAggregateReturnsRoleAndDepartInfo() {
        User user = new User();
        user.setId("u1");
        user.setTenantId("t1");
        user.setUsername("alice");
        user.setRealname("Alice");

        Role adminRole = new Role();
        adminRole.setId("r1");
        adminRole.setRoleCode("ADMIN");
        adminRole.setRoleName("管理员");

        Role opsRole = new Role();
        opsRole.setId("r2");
        opsRole.setRoleCode("OPS");
        opsRole.setRoleName("运营");

        UserDepart rel1 = new UserDepart();
        rel1.setDepartId("d1");
        UserDepart rel2 = new UserDepart();
        rel2.setDepartId("d2");

        Depart depart1 = new Depart();
        depart1.setId("d1");
        depart1.setDepartName("研发部");
        Depart depart2 = new Depart();
        depart2.setId("d2");
        depart2.setDepartName("运营部");

        when(userMapper.selectById("u1")).thenReturn(user);
        when(roleMapper.selectByUserId("u1")).thenReturn(List.of(adminRole, opsRole));
        when(userDepartMapper.selectList(any())).thenReturn(List.of(rel1, rel2));
        when(departService.listByIds(List.of("d1", "d2"))).thenReturn(List.of(depart1, depart2));

        UserDetailVO detailVO = userService.getDetailAggregate("u1");

        assertEquals("u1", detailVO.getId());
        assertIterableEquals(List.of("r1", "r2"), detailVO.getRoleIds());
        assertIterableEquals(List.of("ADMIN", "OPS"), detailVO.getRoleCodes());
        assertIterableEquals(List.of("管理员", "运营"), detailVO.getRoleNames());
        assertIterableEquals(List.of("d1", "d2"), detailVO.getDepartIds());
        assertIterableEquals(List.of("研发部", "运营部"), detailVO.getDepartNames());
    }

    @Test
    void fillUserSummaryAddsRoleAndDepartNames() {
        UserVO userVO = new UserVO();
        userVO.setId("u1");

        UserRole userRole1 = new UserRole();
        userRole1.setUserId("u1");
        userRole1.setRoleId("r1");
        UserRole userRole2 = new UserRole();
        userRole2.setUserId("u1");
        userRole2.setRoleId("r2");

        Role adminRole = new Role();
        adminRole.setId("r1");
        adminRole.setRoleName("管理员");
        Role opsRole = new Role();
        opsRole.setId("r2");
        opsRole.setRoleName("运营");

        UserDepart userDepart1 = new UserDepart();
        userDepart1.setUserId("u1");
        userDepart1.setDepartId("d1");
        UserDepart userDepart2 = new UserDepart();
        userDepart2.setUserId("u1");
        userDepart2.setDepartId("d2");

        Depart depart1 = new Depart();
        depart1.setId("d1");
        depart1.setDepartName("研发部");
        Depart depart2 = new Depart();
        depart2.setId("d2");
        depart2.setDepartName("运营部");

        when(userRoleMapper.selectList(any())).thenReturn(List.of(userRole1, userRole2));
        when(roleMapper.selectBatchIds(List.of("r1", "r2"))).thenReturn(List.of(adminRole, opsRole));
        when(userDepartMapper.selectList(any())).thenReturn(List.of(userDepart1, userDepart2));
        when(departService.listByIds(List.of("d1", "d2"))).thenReturn(List.of(depart1, depart2));

        List<UserVO> result = userService.fillUserSummary(List.of(userVO));

        assertIterableEquals(List.of("管理员", "运营"), result.get(0).getRoleNames());
        assertIterableEquals(List.of("研发部", "运营部"), result.get(0).getDepartNames());
        assertEquals("管理员 / 运营", result.get(0).getRoleSummary());
        assertEquals("研发部 / 运营部", result.get(0).getDepartSummary());
    }

    @Test
    void deleteUsersByIdsRemovesRelations() {
        UserServiceImpl spyService = spy(new UserServiceImpl(roleMapper, userDepartMapper, userRoleMapper, departService));
        ReflectionTestUtils.setField(spyService, "baseMapper", userMapper);
        doReturn(true).when(spyService).removeByIds(List.of("u1", "u2"));

        spyService.deleteUsersByIds(List.of("u1", "u2"));

        verify(userRoleMapper).delete(any());
        verify(userDepartMapper).delete(any());
    }

    @Test
    void resetPasswordUpdatesPasswordAndPwdUpdateTime() {
        User existing = new User();
        existing.setId("u1");
        when(userMapper.selectById("u1")).thenReturn(existing);
        when(userMapper.updateById(any(User.class))).thenReturn(1);

        userService.resetPassword("u1", "encoded-password");

        org.mockito.ArgumentCaptor<User> captor = org.mockito.ArgumentCaptor.forClass(User.class);
        verify(userMapper).updateById(captor.capture());
        assertEquals("u1", captor.getValue().getId());
        assertEquals("encoded-password", captor.getValue().getPassword());
        assertNotNull(captor.getValue().getPwdUpdateTime());
    }

    @Test
    void lockUserUpdatesLockUntil() {
        User existing = new User();
        existing.setId("u1");
        LocalDateTime lockUntil = LocalDateTime.now().plusHours(2);
        when(userMapper.selectById("u1")).thenReturn(existing);
        when(userMapper.updateById(any(User.class))).thenReturn(1);

        userService.lockUser("u1", lockUntil);

        org.mockito.ArgumentCaptor<User> captor = org.mockito.ArgumentCaptor.forClass(User.class);
        verify(userMapper).updateById(captor.capture());
        assertEquals("u1", captor.getValue().getId());
        assertEquals(lockUntil, captor.getValue().getLockUntil());
    }

    @Test
    void unlockUserClearsLockUntil() {
        User existing = new User();
        existing.setId("u1");
        existing.setLockUntil(LocalDateTime.now().plusHours(2));
        when(userMapper.selectById("u1")).thenReturn(existing);
        when(userMapper.updateById(any(User.class))).thenReturn(1);

        userService.unlockUser("u1");

        org.mockito.ArgumentCaptor<User> captor = org.mockito.ArgumentCaptor.forClass(User.class);
        verify(userMapper).updateById(captor.capture());
        assertEquals("u1", captor.getValue().getId());
        assertEquals(null, captor.getValue().getLockUntil());
    }
}
