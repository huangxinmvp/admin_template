package com.hiking.treasure.service;

import com.hiking.treasure.common.exception.BusinessException;
import com.hiking.treasure.domain.vo.system.DeleteConflictResultVO;
import com.hiking.treasure.domain.vo.system.DepartTreeVO;
import com.hiking.treasure.entity.Depart;
import com.hiking.treasure.entity.User;
import com.hiking.treasure.entity.UserDepart;
import com.hiking.treasure.mapper.UserDepartMapper;
import com.hiking.treasure.mapper.DepartMapper;
import com.hiking.treasure.mapper.UserMapper;
import com.hiking.treasure.service.impl.DepartServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertIterableEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class DepartServiceImplTest {

    @Mock
    private DepartMapper departMapper;
    @Mock
    private UserDepartMapper userDepartMapper;
    @Mock
    private UserMapper userMapper;

    private DepartServiceImpl departService;

    @BeforeEach
    void setUp() {
        departService = new DepartServiceImpl(userDepartMapper, userMapper);
        ReflectionTestUtils.setField(departService, "baseMapper", departMapper);
    }

    @Test
    void listTreeBuildsSortedHierarchy() {
        Depart root = new Depart();
        root.setId("d-root");
        root.setDepartName("总部");
        root.setDepartOrder(1);

        Depart childB = new Depart();
        childB.setId("d-b");
        childB.setParentId("d-root");
        childB.setDepartName("运营部");
        childB.setDepartOrder(2);

        Depart childA = new Depart();
        childA.setId("d-a");
        childA.setParentId("d-root");
        childA.setDepartName("研发部");
        childA.setDepartOrder(1);

        when(departMapper.selectList(any())).thenReturn(List.of(childB, root, childA));

        List<DepartTreeVO> tree = departService.listTree();

        assertEquals(1, tree.size());
        assertEquals("总部", tree.get(0).getDepartName());
        assertIterableEquals(List.of("研发部", "运营部"),
                tree.get(0).getChildren().stream().map(DepartTreeVO::getDepartName).toList());
    }

    @Test
    void deleteDepartByIdRejectsWhenHasChildren() {
        Depart parent = new Depart();
        parent.setId("d1");
        parent.setDepartName("总部");
        Depart child = new Depart();
        child.setId("d2");
        child.setParentId("d1");
        child.setDepartName("研发部");

        when(departMapper.selectBatchIds(List.of("d1"))).thenReturn(List.of(parent));
        when(departMapper.selectList(any())).thenReturn(List.of(child));

        BusinessException exception = assertThrows(BusinessException.class, () -> departService.deleteDepartById("d1"));
        assertTrue(exception.getMessage().contains("总部"));
        assertTrue(exception.getMessage().contains("研发部"));
        assertInstanceOf(DeleteConflictResultVO.class, exception.getData());
    }

    @Test
    void deleteDepartByIdRejectsWhenAssignedToUsers() {
        Depart depart = new Depart();
        depart.setId("d1");
        depart.setDepartName("研发部");
        User user = new User();
        user.setId("u1");
        user.setUsername("admin");
        user.setRealname("管理员");
        UserDepart userDepart = new UserDepart();
        userDepart.setDepartId("d1");
        userDepart.setUserId("u1");

        when(departMapper.selectBatchIds(List.of("d1"))).thenReturn(List.of(depart));
        when(departMapper.selectList(any())).thenReturn(List.of());
        when(userDepartMapper.selectList(any())).thenReturn(List.of(userDepart));
        when(userMapper.selectBatchIds(List.of("u1"))).thenReturn(List.of(user));

        BusinessException exception = assertThrows(BusinessException.class, () -> departService.deleteDepartById("d1"));
        assertTrue(exception.getMessage().contains("研发部"));
        assertTrue(exception.getMessage().contains("管理员(admin)"));
        assertInstanceOf(DeleteConflictResultVO.class, exception.getData());
    }
}
