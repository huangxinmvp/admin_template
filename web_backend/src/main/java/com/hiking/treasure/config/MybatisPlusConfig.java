package com.hiking.treasure.config;

import com.baomidou.mybatisplus.annotation.DbType;
import com.baomidou.mybatisplus.extension.plugins.MybatisPlusInterceptor;
import com.baomidou.mybatisplus.extension.plugins.inner.BlockAttackInnerInterceptor;
import com.baomidou.mybatisplus.extension.plugins.inner.PaginationInnerInterceptor;
import com.baomidou.mybatisplus.extension.plugins.handler.TenantLineHandler;
import com.baomidou.mybatisplus.extension.plugins.inner.TenantLineInnerInterceptor;
import com.hiking.treasure.common.security.SecurityUtils;
import net.sf.jsqlparser.expression.Expression;
import net.sf.jsqlparser.expression.StringValue;
import org.mybatis.spring.annotation.MapperScan;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.Set;

/**
 * MyBatis 配置类
 */
@Configuration
@MapperScan({
        "com.hiking.treasure.mapper",
        "com.hiking.treasure.modules.phase1.mapper"
})
public class MybatisPlusConfig {
    @Bean
    public MybatisPlusInterceptor mybatisPlusInterceptor() {
        MybatisPlusInterceptor interceptor = new MybatisPlusInterceptor();
        interceptor.addInnerInterceptor(new TenantLineInnerInterceptor(new TenantLineHandler() {
            private final Set<String> tenantTables = Set.of(
                    "sys_user",
                    "sys_role",
                    "sys_permission",
                    "wf_model_draft",
                    "sys_user_role",
                    "sys_role_permission",
                    "sys_depart",
                    "sys_user_depart",
                    "sys_dict",
                    "sys_dict_item",
                    "sys_permission_data_rule",
                    "sys_log",
                    "sys_announcement",
                    "sys_announcement_send",
                    "sys_file",
                    "sys_quartz_job",
                    "sys_quartz_job_log",
                    "ai_project",
                    "ai_project_stage",
                    "ai_decision_item",
                    "ai_approval_record",
                    "ai_budget_plan",
                    "ai_budget_ledger",
                    "ai_agent_role",
                    "ai_agent_role_stage_participation",
                    "ai_agent_role_allowed_action",
                    "ai_workflow_template",
                    "ai_workflow_template_stage",
                    "ai_requirement_intake",
                    "ai_clarification_item",
                    "ai_decision_action_log",
                    "ai_approval_action_log",
                    "ai_project_governance_state",
                    "ai_meeting_record",
                    "ai_project_tool_binding",
                    "ai_tool_integration_audit"
            );

            @Override
            public Expression getTenantId() {
                return new StringValue(SecurityUtils.getTenantId());
            }

            @Override
            public boolean ignoreTable(String tableName) {
                return SecurityUtils.getTenantId() == null || !tenantTables.contains(tableName);
            }
        }));
        interceptor.addInnerInterceptor(new PaginationInnerInterceptor(DbType.MYSQL)); // 分页
        interceptor.addInnerInterceptor(new BlockAttackInnerInterceptor());            // 防全表更新/删除
        return interceptor;
    }
}
