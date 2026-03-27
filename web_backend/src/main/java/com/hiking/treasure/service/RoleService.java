package com.hiking.treasure.service;

import com.hiking.treasure.domain.vo.system.OptionVO;
import com.hiking.treasure.domain.vo.system.RoleTreeVO;
import com.hiking.treasure.entity.Role;
import com.baomidou.mybatisplus.extension.service.IService;

import java.util.List;

/**
 * <p>
 * 角色表 服务类
 * </p>
 *
 * @author hx
 * @since 2025-09-04
 */
public interface RoleService extends IService<Role> {
    List<String> getPermissionIds(String roleId);
    boolean savePermissionIds(String roleId, List<String> permissionIds);
    List<RoleTreeVO> listTree();
    List<OptionVO> listOptions();
    boolean deleteRoleById(String roleId);
    boolean deleteRolesByIds(List<String> roleIds);
}
