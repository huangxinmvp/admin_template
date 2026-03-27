package com.hiking.treasure.service;

import com.hiking.treasure.domain.dto.PermissionSortDTO;
import com.hiking.treasure.domain.dto.PermissionTreeSaveDTO;
import com.hiking.treasure.domain.vo.system.PermissionTreeVO;
import com.hiking.treasure.domain.vo.system.PermissionTreeSaveResultVO;
import com.hiking.treasure.entity.Permission;
import com.baomidou.mybatisplus.extension.service.IService;

import java.util.List;

/**
 * <p>
 * 权限表(菜单/按钮) 服务类
 * </p>
 *
 * @author hx
 * @since 2025-09-04
 */
public interface PermissionService extends IService<Permission> {
    List<Permission> listByUserId(String userId);
    List<Permission> listMenuByUserId(String userId);
    List<String> listPermissionCodesByUserId(String userId);
    List<PermissionTreeVO> listTree();
    int getNextSortNo(String parentId);
    boolean savePermission(Permission permission);
    boolean updatePermission(Permission permission);
    boolean deletePermissionById(String permissionId);
    boolean deletePermissionsByIds(List<String> permissionIds);
    boolean saveSort(List<PermissionSortDTO> items);
    PermissionTreeSaveResultVO saveTree(List<PermissionTreeSaveDTO> tree);
}
