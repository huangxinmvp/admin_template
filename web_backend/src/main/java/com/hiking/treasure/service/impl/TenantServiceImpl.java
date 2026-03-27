package com.hiking.treasure.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.hiking.treasure.common.exception.BusinessException;
import com.hiking.treasure.domain.vo.system.DeleteConflictDetailVO;
import com.hiking.treasure.domain.vo.system.DeleteConflictItemVO;
import com.hiking.treasure.domain.vo.system.DeleteConflictResultVO;
import com.hiking.treasure.entity.Announcement;
import com.hiking.treasure.entity.AnnouncementSend;
import com.hiking.treasure.entity.Depart;
import com.hiking.treasure.entity.Dict;
import com.hiking.treasure.entity.DictItem;
import com.hiking.treasure.entity.File;
import com.hiking.treasure.entity.Log;
import com.hiking.treasure.entity.Permission;
import com.hiking.treasure.entity.PermissionDataRule;
import com.hiking.treasure.entity.QuartzJob;
import com.hiking.treasure.entity.QuartzJobLog;
import com.hiking.treasure.entity.Role;
import com.hiking.treasure.entity.RolePermission;
import com.hiking.treasure.entity.Tenant;
import com.hiking.treasure.entity.User;
import com.hiking.treasure.entity.UserDepart;
import com.hiking.treasure.entity.UserRole;
import com.hiking.treasure.mapper.AnnouncementSendMapper;
import com.hiking.treasure.mapper.PermissionDataRuleMapper;
import com.hiking.treasure.mapper.QuartzJobLogMapper;
import com.hiking.treasure.mapper.RolePermissionMapper;
import com.hiking.treasure.mapper.TenantMapper;
import com.hiking.treasure.mapper.UserDepartMapper;
import com.hiking.treasure.mapper.UserRoleMapper;
import com.hiking.treasure.service.AnnouncementService;
import com.hiking.treasure.service.DepartService;
import com.hiking.treasure.service.DictItemService;
import com.hiking.treasure.service.DictService;
import com.hiking.treasure.service.FileService;
import com.hiking.treasure.service.LogService;
import com.hiking.treasure.service.PermissionService;
import com.hiking.treasure.service.QuartzJobService;
import com.hiking.treasure.service.RoleService;
import com.hiking.treasure.service.TenantService;
import com.hiking.treasure.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Supplier;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class TenantServiceImpl extends ServiceImpl<TenantMapper, Tenant> implements TenantService {

    private final UserService userService;
    private final RoleService roleService;
    private final PermissionService permissionService;
    private final DepartService departService;
    private final AnnouncementService announcementService;
    private final FileService fileService;
    private final QuartzJobService quartzJobService;
    private final DictService dictService;
    private final DictItemService dictItemService;
    private final LogService logService;
    private final UserRoleMapper userRoleMapper;
    private final UserDepartMapper userDepartMapper;
    private final RolePermissionMapper rolePermissionMapper;
    private final AnnouncementSendMapper announcementSendMapper;
    private final QuartzJobLogMapper quartzJobLogMapper;
    private final PermissionDataRuleMapper permissionDataRuleMapper;

    @Override
    public Tenant getByCode(String tenantCode) {
        return baseMapper.selectByCode(tenantCode);
    }

    @Override
    public Tenant requireActiveTenant(String tenantId) {
        if (!StringUtils.hasText(tenantId)) {
            return null;
        }
        Tenant tenant = getById(tenantId);
        if (tenant == null || Integer.valueOf(0).equals(tenant.getStatus())) {
            throw new BusinessException(400, "租户不存在或已停用");
        }
        if (tenant.getExpireTime() != null && tenant.getExpireTime().isBefore(LocalDateTime.now())) {
            throw new BusinessException(400, "租户已过期");
        }
        return tenant;
    }

    @Override
    public boolean deleteTenantById(String tenantId) {
        validateDeletable(List.of(tenantId));
        return removeById(tenantId);
    }

    @Override
    public boolean deleteTenantsByIds(List<String> tenantIds) {
        if (tenantIds == null || tenantIds.isEmpty()) {
            return true;
        }
        validateDeletable(tenantIds);
        return removeByIds(tenantIds);
    }

    private void validateDeletable(List<String> tenantIds) {
        for (String tenantId : tenantIds) {
            Map<String, List<DeleteConflictItemVO>> occupiedResources = new LinkedHashMap<>();

            collectIfExists(occupiedResources, "用户",
                    userService.count(new LambdaQueryWrapper<User>().eq(User::getTenantId, tenantId)),
                    () -> userService.list(new LambdaQueryWrapper<User>().eq(User::getTenantId, tenantId)).stream()
                            .map(user -> DeleteConflictItemVO.of(user.getId(), getUserLabel(user), "user"))
                            .toList());
            collectIfExists(occupiedResources, "角色",
                    roleService.count(new LambdaQueryWrapper<Role>().eq(Role::getTenantId, tenantId)),
                    () -> roleService.list(new LambdaQueryWrapper<Role>().eq(Role::getTenantId, tenantId)).stream()
                            .map(role -> DeleteConflictItemVO.of(role.getId(), getRoleLabel(role), "role"))
                            .toList());
            collectIfExists(occupiedResources, "菜单权限",
                    permissionService.count(new LambdaQueryWrapper<Permission>().eq(Permission::getTenantId, tenantId)),
                    () -> permissionService.list(new LambdaQueryWrapper<Permission>().eq(Permission::getTenantId, tenantId)).stream()
                            .map(permission -> DeleteConflictItemVO.of(permission.getId(), getPermissionLabel(permission), "permission"))
                            .toList());
            collectIfExists(occupiedResources, "部门",
                    departService.count(new LambdaQueryWrapper<Depart>().eq(Depart::getTenantId, tenantId)),
                    () -> departService.list(new LambdaQueryWrapper<Depart>().eq(Depart::getTenantId, tenantId)).stream()
                            .map(depart -> DeleteConflictItemVO.of(depart.getId(), getDepartLabel(depart), "depart"))
                            .toList());
            collectIfExists(occupiedResources, "公告",
                    announcementService.count(new LambdaQueryWrapper<Announcement>().eq(Announcement::getTenantId, tenantId)),
                    () -> announcementService.list(new LambdaQueryWrapper<Announcement>().eq(Announcement::getTenantId, tenantId)).stream()
                            .map(announcement -> DeleteConflictItemVO.of(announcement.getId(), getAnnouncementLabel(announcement), "announcement"))
                            .toList());
            collectIfExists(occupiedResources, "文件",
                    fileService.count(new LambdaQueryWrapper<File>().eq(File::getTenantId, tenantId)),
                    () -> fileService.list(new LambdaQueryWrapper<File>().eq(File::getTenantId, tenantId)).stream()
                            .map(file -> DeleteConflictItemVO.of(file.getId(), getFileLabel(file), "file"))
                            .toList());
            collectIfExists(occupiedResources, "定时任务",
                    quartzJobService.count(new LambdaQueryWrapper<QuartzJob>().eq(QuartzJob::getTenantId, tenantId)),
                    () -> quartzJobService.list(new LambdaQueryWrapper<QuartzJob>().eq(QuartzJob::getTenantId, tenantId)).stream()
                            .map(job -> DeleteConflictItemVO.of(job.getId(), getQuartzJobLabel(job), "quartz_job"))
                            .toList());
            collectIfExists(occupiedResources, "字典",
                    dictService.count(new QueryWrapper<Dict>().eq("tenant_id", tenantId)),
                    () -> dictService.list(new QueryWrapper<Dict>().eq("tenant_id", tenantId)).stream()
                            .map(dict -> DeleteConflictItemVO.of(dict.getId(), getDictLabel(dict), "dict"))
                            .toList());
            collectIfExists(occupiedResources, "字典项",
                    dictItemService.count(new QueryWrapper<DictItem>().eq("tenant_id", tenantId)),
                    () -> dictItemService.list(new QueryWrapper<DictItem>().eq("tenant_id", tenantId)).stream()
                            .map(dictItem -> DeleteConflictItemVO.of(dictItem.getId(), getDictItemLabel(dictItem), "dict_item"))
                            .toList());
            collectIfExists(occupiedResources, "日志",
                    logService.count(new QueryWrapper<Log>().eq("tenant_id", tenantId)),
                    () -> logService.list(new QueryWrapper<Log>().eq("tenant_id", tenantId)).stream()
                            .map(log -> DeleteConflictItemVO.of(log.getId(), getLogLabel(log), "log"))
                            .toList());

            List<UserRole> userRoles = listUserRoles(tenantId);
            Map<String, String> relationUserLabelMap = loadUserLabelMap(userRoles.stream().map(UserRole::getUserId).toList());
            Map<String, String> relationRoleLabelMap = loadRoleLabelMap(userRoles.stream().map(UserRole::getRoleId).toList());
            collectIfExists(occupiedResources, "用户角色关系", userRoles.size(), () -> userRoles.stream()
                    .map(link -> DeleteConflictItemVO.of(
                            link.getId(),
                            relationUserLabelMap.getOrDefault(link.getUserId(), link.getUserId())
                                    + " -> " + relationRoleLabelMap.getOrDefault(link.getRoleId(), link.getRoleId()),
                            "user_role_relation",
                            buildRelationMeta(
                                    DeleteConflictItemVO.of(link.getUserId(), relationUserLabelMap.getOrDefault(link.getUserId(), link.getUserId()), "user"),
                                    DeleteConflictItemVO.of(link.getRoleId(), relationRoleLabelMap.getOrDefault(link.getRoleId(), link.getRoleId()), "role")
                            )
                    ))
                    .toList());
            List<UserDepart> userDeparts = listUserDeparts(tenantId);
            Map<String, String> departRelationUserLabelMap = loadUserLabelMap(userDeparts.stream().map(UserDepart::getUserId).toList());
            Map<String, String> relationDepartLabelMap = loadDepartLabelMap(userDeparts.stream().map(UserDepart::getDepartId).toList());
            collectIfExists(occupiedResources, "用户部门关系", userDeparts.size(), () -> userDeparts.stream()
                    .map(link -> DeleteConflictItemVO.of(
                            link.getId(),
                            departRelationUserLabelMap.getOrDefault(link.getUserId(), link.getUserId())
                                    + " -> " + relationDepartLabelMap.getOrDefault(link.getDepartId(), link.getDepartId()),
                            "user_depart_relation",
                            buildRelationMeta(
                                    DeleteConflictItemVO.of(link.getUserId(), departRelationUserLabelMap.getOrDefault(link.getUserId(), link.getUserId()), "user"),
                                    DeleteConflictItemVO.of(link.getDepartId(), relationDepartLabelMap.getOrDefault(link.getDepartId(), link.getDepartId()), "depart")
                            )
                    ))
                    .toList());
            List<RolePermission> rolePermissions = listRolePermissions(tenantId);
            Map<String, String> permissionRelationRoleLabelMap = loadRoleLabelMap(rolePermissions.stream().map(RolePermission::getRoleId).toList());
            Map<String, String> relationPermissionLabelMap = loadPermissionLabelMap(rolePermissions.stream().map(RolePermission::getPermissionId).toList());
            collectIfExists(occupiedResources, "角色权限关系", rolePermissions.size(), () -> rolePermissions.stream()
                    .map(link -> DeleteConflictItemVO.of(
                            link.getId(),
                            permissionRelationRoleLabelMap.getOrDefault(link.getRoleId(), link.getRoleId())
                                    + " -> " + relationPermissionLabelMap.getOrDefault(link.getPermissionId(), link.getPermissionId()),
                            "role_permission_relation",
                            buildRelationMeta(
                                    DeleteConflictItemVO.of(link.getRoleId(), permissionRelationRoleLabelMap.getOrDefault(link.getRoleId(), link.getRoleId()), "role"),
                                    DeleteConflictItemVO.of(link.getPermissionId(), relationPermissionLabelMap.getOrDefault(link.getPermissionId(), link.getPermissionId()), "permission")
                            )
                    ))
                    .toList());
            List<AnnouncementSend> announcementSends = listAnnouncementSends(tenantId);
            Map<String, String> relationAnnouncementLabelMap = loadAnnouncementLabelMap(announcementSends.stream().map(AnnouncementSend::getAnntId).toList());
            Map<String, String> announcementRelationUserLabelMap = loadUserLabelMap(announcementSends.stream().map(AnnouncementSend::getUserId).toList());
            collectIfExists(occupiedResources, "公告收件记录", announcementSends.size(), () -> announcementSends.stream()
                    .map(link -> DeleteConflictItemVO.of(
                            link.getId(),
                            relationAnnouncementLabelMap.getOrDefault(link.getAnntId(), link.getAnntId())
                                    + " -> " + announcementRelationUserLabelMap.getOrDefault(link.getUserId(), link.getUserId()),
                            "announcement_send_relation",
                            buildRelationMeta(
                                    DeleteConflictItemVO.of(link.getAnntId(), relationAnnouncementLabelMap.getOrDefault(link.getAnntId(), link.getAnntId()), "announcement"),
                                    DeleteConflictItemVO.of(link.getUserId(), announcementRelationUserLabelMap.getOrDefault(link.getUserId(), link.getUserId()), "user")
                            )
                    ))
                    .toList());
            List<QuartzJobLog> quartzJobLogs = listQuartzJobLogs(tenantId);
            collectIfExists(occupiedResources, "任务日志", quartzJobLogs.size(), () -> quartzJobLogs.stream()
                    .map(log -> DeleteConflictItemVO.of(log.getId(), getQuartzJobLogLabel(log), "quartz_job_log"))
                    .toList());
            List<PermissionDataRule> permissionDataRules = listPermissionDataRules(tenantId);
            Map<String, String> dataRulePermissionLabelMap = loadPermissionLabelMap(permissionDataRules.stream().map(PermissionDataRule::getPermissionId).toList());
            collectIfExists(occupiedResources, "数据权限规则", permissionDataRules.size(), () -> permissionDataRules.stream()
                    .map(rule -> DeleteConflictItemVO.of(
                            rule.getId(),
                            getPermissionDataRuleLabel(rule, dataRulePermissionLabelMap),
                            "permission_data_rule",
                            buildRuleMeta(rule, dataRulePermissionLabelMap)
                    ))
                    .toList());

            if (!occupiedResources.isEmpty()) {
                throw new BusinessException(400,
                        "租户删除失败，仍存在以下资源: " + formatResourceDetail(occupiedResources),
                        DeleteConflictResultVO.of("tenant", buildDeleteConflictDetails(occupiedResources)));
            }
        }
    }

    private void collectIfExists(Map<String, List<DeleteConflictItemVO>> occupiedResources,
                                 String resourceName,
                                 long count,
                                 Supplier<List<DeleteConflictItemVO>> detailsSupplier) {
        if (count > 0) {
            occupiedResources.put(resourceName, summarizeDetails(detailsSupplier.get()));
        }
    }

    private List<UserRole> listUserRoles(String tenantId) {
        return userRoleMapper.selectList(new LambdaQueryWrapper<UserRole>().eq(UserRole::getTenantId, tenantId));
    }

    private List<UserDepart> listUserDeparts(String tenantId) {
        return userDepartMapper.selectList(new LambdaQueryWrapper<UserDepart>().eq(UserDepart::getTenantId, tenantId));
    }

    private List<RolePermission> listRolePermissions(String tenantId) {
        return rolePermissionMapper.selectList(new LambdaQueryWrapper<RolePermission>().eq(RolePermission::getTenantId, tenantId));
    }

    private List<AnnouncementSend> listAnnouncementSends(String tenantId) {
        return announcementSendMapper.selectList(new LambdaQueryWrapper<AnnouncementSend>().eq(AnnouncementSend::getTenantId, tenantId));
    }

    private List<QuartzJobLog> listQuartzJobLogs(String tenantId) {
        return quartzJobLogMapper.selectList(new LambdaQueryWrapper<QuartzJobLog>().eq(QuartzJobLog::getTenantId, tenantId));
    }

    private List<PermissionDataRule> listPermissionDataRules(String tenantId) {
        return permissionDataRuleMapper.selectList(new QueryWrapper<PermissionDataRule>().eq("tenant_id", tenantId));
    }

    private Map<String, String> loadUserLabelMap(List<String> userIds) {
        if (userIds == null || userIds.isEmpty()) {
            return Map.of();
        }
        return userService.listByIds(userIds.stream().filter(StringUtils::hasText).distinct().toList()).stream()
                .collect(Collectors.toMap(User::getId, this::getUserLabel, (left, right) -> left, LinkedHashMap::new));
    }

    private Map<String, String> loadRoleLabelMap(List<String> roleIds) {
        if (roleIds == null || roleIds.isEmpty()) {
            return Map.of();
        }
        return roleService.listByIds(roleIds.stream().filter(StringUtils::hasText).distinct().toList()).stream()
                .collect(Collectors.toMap(Role::getId, this::getRoleLabel, (left, right) -> left, LinkedHashMap::new));
    }

    private Map<String, String> loadPermissionLabelMap(List<String> permissionIds) {
        if (permissionIds == null || permissionIds.isEmpty()) {
            return Map.of();
        }
        return permissionService.listByIds(permissionIds.stream().filter(StringUtils::hasText).distinct().toList()).stream()
                .collect(Collectors.toMap(Permission::getId, this::getPermissionLabel, (left, right) -> left, LinkedHashMap::new));
    }

    private Map<String, String> loadDepartLabelMap(List<String> departIds) {
        if (departIds == null || departIds.isEmpty()) {
            return Map.of();
        }
        return departService.listByIds(departIds.stream().filter(StringUtils::hasText).distinct().toList()).stream()
                .collect(Collectors.toMap(Depart::getId, this::getDepartLabel, (left, right) -> left, LinkedHashMap::new));
    }

    private Map<String, String> loadAnnouncementLabelMap(List<String> announcementIds) {
        if (announcementIds == null || announcementIds.isEmpty()) {
            return Map.of();
        }
        return announcementService.listByIds(announcementIds.stream().filter(StringUtils::hasText).distinct().toList()).stream()
                .collect(Collectors.toMap(Announcement::getId, this::getAnnouncementLabel, (left, right) -> left, LinkedHashMap::new));
    }

    private String formatResourceDetail(Map<String, List<DeleteConflictItemVO>> occupiedResources) {
        return occupiedResources.entrySet().stream()
                .map(entry -> entry.getKey() + ": " + entry.getValue().stream().map(DeleteConflictItemVO::getName).collect(Collectors.joining("、")))
                .collect(Collectors.joining("；"));
    }

    private List<DeleteConflictDetailVO> buildDeleteConflictDetails(Map<String, List<DeleteConflictItemVO>> occupiedResources) {
        return occupiedResources.entrySet().stream()
                .map(entry -> DeleteConflictDetailVO.of(toConflictCode(entry.getKey()), entry.getKey(), entry.getValue()))
                .toList();
    }

    private String toConflictCode(String resourceName) {
        return switch (resourceName) {
            case "用户" -> "user";
            case "角色" -> "role";
            case "菜单权限" -> "permission";
            case "部门" -> "depart";
            case "公告" -> "announcement";
            case "文件" -> "file";
            case "定时任务" -> "quartz_job";
            case "字典" -> "dict";
            case "字典项" -> "dict_item";
            case "分类" -> "category";
            case "日志" -> "log";
            case "用户角色关系" -> "user_role_relation";
            case "用户部门关系" -> "user_depart_relation";
            case "角色权限关系" -> "role_permission_relation";
            case "公告收件记录" -> "announcement_send_relation";
            case "任务日志" -> "quartz_job_log";
            case "数据权限规则" -> "permission_data_rule";
            default -> resourceName;
        };
    }

    private List<DeleteConflictItemVO> summarizeDetails(List<DeleteConflictItemVO> details) {
        if (details == null || details.isEmpty()) {
            return List.of(DeleteConflictItemVO.of(null, "存在关联数据", "unknown"));
        }
        List<DeleteConflictItemVO> sanitized = details.stream()
                .filter(item -> item != null && StringUtils.hasText(item.getName()))
                .collect(Collectors.toMap(
                        item -> StringUtils.hasText(item.getId()) ? item.getType() + ":" + item.getId() : item.getType() + ":" + item.getName(),
                        item -> item,
                        (left, right) -> left,
                        LinkedHashMap::new
                ))
                .values()
                .stream()
                .limit(5)
                .collect(Collectors.toCollection(ArrayList::new));
        long distinctCount = details.stream()
                .filter(item -> item != null && StringUtils.hasText(item.getName()))
                .map(item -> StringUtils.hasText(item.getId()) ? item.getType() + ":" + item.getId() : item.getType() + ":" + item.getName())
                .distinct()
                .count();
        if (distinctCount > sanitized.size()) {
            sanitized.add(DeleteConflictItemVO.of(null, "等", "summary"));
        }
        return sanitized;
    }

    private Map<String, Object> buildRelationMeta(DeleteConflictItemVO from, DeleteConflictItemVO to) {
        Map<String, Object> meta = new LinkedHashMap<>();
        meta.put("title", "关联关系");
        meta.put("summary", from.getName() + " -> " + to.getName());
        meta.put("from", from);
        meta.put("to", to);
        return meta;
    }

    private Map<String, Object> buildRuleMeta(PermissionDataRule permissionDataRule, Map<String, String> permissionLabelMap) {
        Map<String, Object> meta = new LinkedHashMap<>();
        meta.put("title", "规则摘要");
        if (StringUtils.hasText(permissionDataRule.getPermissionId())) {
            meta.put("permission", DeleteConflictItemVO.of(
                    permissionDataRule.getPermissionId(),
                    permissionLabelMap.getOrDefault(permissionDataRule.getPermissionId(), permissionDataRule.getPermissionId()),
                    "permission"
            ));
        }
        if (StringUtils.hasText(permissionDataRule.getRuleColumn())) {
            meta.put("ruleColumn", permissionDataRule.getRuleColumn());
        }
        if (StringUtils.hasText(permissionDataRule.getCondition())) {
            meta.put("condition", permissionDataRule.getCondition());
        }
        meta.put("summary", buildRuleSummary(permissionDataRule, permissionLabelMap));
        return meta.isEmpty() ? null : meta;
    }

    private String buildRuleSummary(PermissionDataRule permissionDataRule, Map<String, String> permissionLabelMap) {
        String permissionName = StringUtils.hasText(permissionDataRule.getPermissionId())
                ? permissionLabelMap.getOrDefault(permissionDataRule.getPermissionId(), permissionDataRule.getPermissionId())
                : "未关联菜单";
        String ruleName = StringUtils.hasText(permissionDataRule.getRuleName()) ? permissionDataRule.getRuleName() : "未命名规则";
        return permissionName + " / " + ruleName;
    }

    private String getUserLabel(User user) {
        if (user == null) {
            return "";
        }
        if (StringUtils.hasText(user.getRealname()) && StringUtils.hasText(user.getUsername())) {
            return user.getRealname() + "(" + user.getUsername() + ")";
        }
        if (StringUtils.hasText(user.getUsername())) {
            return user.getUsername();
        }
        return user.getId();
    }

    private String getRoleLabel(Role role) {
        if (role == null) {
            return "";
        }
        if (StringUtils.hasText(role.getRoleName())) {
            return role.getRoleName();
        }
        if (StringUtils.hasText(role.getRoleCode())) {
            return role.getRoleCode();
        }
        return role.getId();
    }

    private String getPermissionLabel(Permission permission) {
        if (permission == null) {
            return "";
        }
        if (StringUtils.hasText(permission.getName())) {
            return permission.getName();
        }
        if (StringUtils.hasText(permission.getPerms())) {
            return permission.getPerms();
        }
        if (StringUtils.hasText(permission.getUrl())) {
            return permission.getUrl();
        }
        return permission.getId();
    }

    private String getDepartLabel(Depart depart) {
        if (depart == null) {
            return "";
        }
        if (StringUtils.hasText(depart.getDepartName())) {
            return depart.getDepartName();
        }
        if (StringUtils.hasText(depart.getOrgCode())) {
            return depart.getOrgCode();
        }
        return depart.getId();
    }

    private String getAnnouncementLabel(Announcement announcement) {
        if (announcement == null) {
            return "";
        }
        if (StringUtils.hasText(announcement.getTitle())) {
            return announcement.getTitle();
        }
        return announcement.getId();
    }

    private String getFileLabel(File file) {
        if (file == null) {
            return "";
        }
        if (StringUtils.hasText(file.getFileName())) {
            return file.getFileName();
        }
        if (StringUtils.hasText(file.getObjectName())) {
            return file.getObjectName();
        }
        return file.getId();
    }

    private String getQuartzJobLabel(QuartzJob quartzJob) {
        if (quartzJob == null) {
            return "";
        }
        if (StringUtils.hasText(quartzJob.getJobName()) && StringUtils.hasText(quartzJob.getJobGroup())) {
            return quartzJob.getJobName() + "@" + quartzJob.getJobGroup();
        }
        if (StringUtils.hasText(quartzJob.getJobName())) {
            return quartzJob.getJobName();
        }
        return quartzJob.getId();
    }

    private String getDictLabel(Dict dict) {
        if (dict == null) {
            return "";
        }
        if (StringUtils.hasText(dict.getDictName())) {
            return dict.getDictName();
        }
        if (StringUtils.hasText(dict.getDictCode())) {
            return dict.getDictCode();
        }
        return dict.getId();
    }

    private String getDictItemLabel(DictItem dictItem) {
        if (dictItem == null) {
            return "";
        }
        if (StringUtils.hasText(dictItem.getItemText())) {
            return dictItem.getItemText();
        }
        if (StringUtils.hasText(dictItem.getItemValue())) {
            return dictItem.getItemValue();
        }
        return dictItem.getId();
    }

    private String getLogLabel(Log log) {
        if (log == null) {
            return "";
        }
        if (StringUtils.hasText(log.getUsername()) && StringUtils.hasText(log.getRequestUrl())) {
            return log.getUsername() + "(" + log.getRequestUrl() + ")";
        }
        if (StringUtils.hasText(log.getUsername())) {
            return log.getUsername();
        }
        if (StringUtils.hasText(log.getRequestUrl())) {
            return log.getRequestUrl();
        }
        return log.getId();
    }

    private String getQuartzJobLogLabel(QuartzJobLog quartzJobLog) {
        if (quartzJobLog == null) {
            return "";
        }
        if (StringUtils.hasText(quartzJobLog.getJobName()) && StringUtils.hasText(quartzJobLog.getJobGroup())) {
            return quartzJobLog.getJobName() + "@" + quartzJobLog.getJobGroup();
        }
        if (StringUtils.hasText(quartzJobLog.getJobName())) {
            return quartzJobLog.getJobName();
        }
        return quartzJobLog.getId();
    }

    private String getPermissionDataRuleLabel(PermissionDataRule permissionDataRule, Map<String, String> permissionLabelMap) {
        if (permissionDataRule == null) {
            return "";
        }
        if (StringUtils.hasText(permissionDataRule.getRuleName())) {
            if (StringUtils.hasText(permissionDataRule.getPermissionId())) {
                return permissionLabelMap.getOrDefault(permissionDataRule.getPermissionId(), permissionDataRule.getPermissionId())
                        + "/" + permissionDataRule.getRuleName();
            }
            return permissionDataRule.getRuleName();
        }
        if (StringUtils.hasText(permissionDataRule.getPermissionId())) {
            return permissionLabelMap.getOrDefault(permissionDataRule.getPermissionId(), permissionDataRule.getPermissionId());
        }
        return permissionDataRule.getId();
    }
}
