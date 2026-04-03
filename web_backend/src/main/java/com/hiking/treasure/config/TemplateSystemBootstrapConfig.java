package com.hiking.treasure.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.ClassPathResource;
import org.springframework.jdbc.datasource.init.DatabasePopulatorUtils;
import org.springframework.jdbc.datasource.init.ResourceDatabasePopulator;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Objects;

@Slf4j
@Configuration
public class TemplateSystemBootstrapConfig {

    private static final String CONFIG_BOOTSTRAP_SQL = "static/sql/bootstrap-system-config.sql";
    private static final String SESSION_BOOTSTRAP_SQL = "static/sql/bootstrap-user-session.sql";
    private static final String PHASE1_DOMAIN_BOOTSTRAP_SQL = "static/sql/bootstrap-aicoos-phase1-domain.sql";
    private static final String PHASE1_DEMO_SEED_SQL = "static/sql/seed-aicoos-demo.sql";
    private static final String PERMISSION_BOOTSTRAP_SQL = "static/sql/bootstrap-template-permissions.sql";
    private static final String TENANT_TABLE = "sys_tenant";
    private static final String ROLE_TABLE = "sys_role";
    private static final String PERMISSION_TABLE = "sys_permission";
    private static final String PROJECT_TABLE = "ai_project";

    @Bean
    public ApplicationRunner templateSystemBootstrapRunner(DataSource dataSource) {
        return args -> bootstrap(dataSource);
    }

    private void bootstrap(DataSource dataSource) throws SQLException {
        runScript(dataSource, SESSION_BOOTSTRAP_SQL);
        runScript(dataSource, CONFIG_BOOTSTRAP_SQL);
        runScript(dataSource, PHASE1_DOMAIN_BOOTSTRAP_SQL);
        ensurePhase1DomainColumns(dataSource);
        if (tableExists(dataSource, TENANT_TABLE) && tableExists(dataSource, PROJECT_TABLE)) {
            runScript(dataSource, PHASE1_DEMO_SEED_SQL);
        } else {
            log.info("Skip AICoOS demo seed bootstrap because core tenant/project tables are not ready");
        }
        if (!tableExists(dataSource, TENANT_TABLE)
                || !tableExists(dataSource, ROLE_TABLE)
                || !tableExists(dataSource, PERMISSION_TABLE)) {
            log.info("Skip template permission bootstrap because core RBAC tables are not ready");
            return;
        }
        runScript(dataSource, PERMISSION_BOOTSTRAP_SQL);
    }

    private boolean tableExists(DataSource dataSource, String tableName) throws SQLException {
        try (Connection connection = dataSource.getConnection()) {
            DatabaseMetaData metaData = connection.getMetaData();
            String catalog = connection.getCatalog();
            return lookupTable(metaData, catalog, tableName)
                    || lookupTable(metaData, catalog, tableName.toLowerCase())
                    || lookupTable(metaData, catalog, tableName.toUpperCase());
        }
    }

    private boolean lookupTable(DatabaseMetaData metaData, String catalog, String tableName) throws SQLException {
        try (ResultSet resultSet = metaData.getTables(catalog, null, tableName, new String[]{"TABLE"})) {
            return resultSet.next();
        }
    }

    private void ensurePhase1DomainColumns(DataSource dataSource) throws SQLException {
        ensureColumnExists(
                dataSource,
                "ai_decision_item",
                "source_type",
                "ALTER TABLE `ai_decision_item` ADD COLUMN `source_type` varchar(64) COMMENT '来源类型' AFTER `item_type`"
        );
        ensureColumnExists(
                dataSource,
                "ai_decision_item",
                "source_id",
                "ALTER TABLE `ai_decision_item` ADD COLUMN `source_id` varchar(36) COMMENT '来源ID' AFTER `source_type`"
        );
        ensureColumnExists(
                dataSource,
                "ai_decision_item",
                "description",
                "ALTER TABLE `ai_decision_item` ADD COLUMN `description` text COMMENT '决策描述' AFTER `source_id`"
        );
        ensureColumnExists(
                dataSource,
                "ai_decision_item",
                "impact_summary",
                "ALTER TABLE `ai_decision_item` ADD COLUMN `impact_summary` text COMMENT '影响摘要' AFTER `description`"
        );
        ensureColumnExists(
                dataSource,
                "ai_decision_item",
                "suggested_options",
                "ALTER TABLE `ai_decision_item` ADD COLUMN `suggested_options` text COMMENT '建议选项' AFTER `impact_summary`"
        );
        ensureColumnExists(
                dataSource,
                "ai_decision_item",
                "recommended_option",
                "ALTER TABLE `ai_decision_item` ADD COLUMN `recommended_option` varchar(255) COMMENT '推荐选项' AFTER `suggested_options`"
        );
        ensureColumnExists(
                dataSource,
                "ai_decision_item",
                "budget_impact_summary",
                "ALTER TABLE `ai_decision_item` ADD COLUMN `budget_impact_summary` text COMMENT '预算影响摘要' AFTER `recommended_option`"
        );
        ensureColumnExists(
                dataSource,
                "ai_decision_item",
                "project_impact_summary",
                "ALTER TABLE `ai_decision_item` ADD COLUMN `project_impact_summary` text COMMENT '项目影响摘要' AFTER `budget_impact_summary`"
        );
        ensureColumnExists(
                dataSource,
                "ai_decision_item",
                "blocker_flag",
                "ALTER TABLE `ai_decision_item` ADD COLUMN `blocker_flag` tinyint(1) NOT NULL DEFAULT 0 COMMENT '是否阻塞' AFTER `project_impact_summary`"
        );

        ensureColumnExists(
                dataSource,
                "ai_clarification_item",
                "promoted_decision_item_id",
                "ALTER TABLE `ai_clarification_item` ADD COLUMN `promoted_decision_item_id` varchar(36) COMMENT '已转化的决策项ID' AFTER `generated_flag`"
        );

        ensureColumnExists(
                dataSource,
                "ai_budget_plan",
                "role_allocations_json",
                "ALTER TABLE `ai_budget_plan` ADD COLUMN `role_allocations_json` text COMMENT '角色预算分配JSON' AFTER `consumed_amount`"
        );

        ensureColumnExists(
                dataSource,
                "ai_agent_role",
                "description",
                "ALTER TABLE `ai_agent_role` ADD COLUMN `description` text COMMENT '角色描述' AFTER `role_category`"
        );
        ensureColumnExists(
                dataSource,
                "ai_agent_role",
                "default_flag",
                "ALTER TABLE `ai_agent_role` ADD COLUMN `default_flag` tinyint(1) NOT NULL DEFAULT 0 COMMENT '是否默认角色' AFTER `capability_summary`"
        );
        ensureColumnExists(
                dataSource,
                "ai_agent_role",
                "budget_factor",
                "ALTER TABLE `ai_agent_role` ADD COLUMN `budget_factor` decimal(10,2) NOT NULL DEFAULT 1.00 COMMENT '预算系数' AFTER `default_flag`"
        );
        ensureColumnExists(
                dataSource,
                "ai_agent_role",
                "approval_collaboration_flag",
                "ALTER TABLE `ai_agent_role` ADD COLUMN `approval_collaboration_flag` tinyint(1) NOT NULL DEFAULT 1 COMMENT '审批协作标记' AFTER `budget_factor`"
        );

        ensureColumnExists(
                dataSource,
                "ai_workflow_template",
                "project_type",
                "ALTER TABLE `ai_workflow_template` ADD COLUMN `project_type` varchar(64) COMMENT '项目类型' AFTER `template_name`"
        );
        ensureColumnExists(
                dataSource,
                "ai_workflow_template",
                "gate_checks_config",
                "ALTER TABLE `ai_workflow_template` ADD COLUMN `gate_checks_config` text COMMENT 'Gate检查配置' AFTER `description`"
        );
        ensureColumnExists(
                dataSource,
                "ai_workflow_template",
                "blocking_decision_config",
                "ALTER TABLE `ai_workflow_template` ADD COLUMN `blocking_decision_config` text COMMENT '阻塞决策条件' AFTER `gate_checks_config`"
        );
        ensureColumnExists(
                dataSource,
                "ai_workflow_template",
                "blocking_approval_config",
                "ALTER TABLE `ai_workflow_template` ADD COLUMN `blocking_approval_config` text COMMENT '阻塞审批条件' AFTER `blocking_decision_config`"
        );
        ensureColumnExists(
                dataSource,
                "ai_workflow_template",
                "budget_threshold_config",
                "ALTER TABLE `ai_workflow_template` ADD COLUMN `budget_threshold_config` text COMMENT '预算阈值条件' AFTER `blocking_approval_config`"
        );
        ensureColumnExists(
                dataSource,
                "ai_workflow_template",
                "high_risk_approval_config",
                "ALTER TABLE `ai_workflow_template` ADD COLUMN `high_risk_approval_config` text COMMENT '高风险动作审批要求' AFTER `budget_threshold_config`"
        );

        ensureColumnExists(
                dataSource,
                "ai_approval_record",
                "title",
                "ALTER TABLE `ai_approval_record` ADD COLUMN `title` varchar(200) COMMENT '审批标题' AFTER `project_id`"
        );
        ensureColumnExists(
                dataSource,
                "ai_approval_record",
                "source_object_type",
                "ALTER TABLE `ai_approval_record` ADD COLUMN `source_object_type` varchar(64) COMMENT '来源对象类型' AFTER `approval_type`"
        );
        ensureColumnExists(
                dataSource,
                "ai_approval_record",
                "source_object_id",
                "ALTER TABLE `ai_approval_record` ADD COLUMN `source_object_id` varchar(36) COMMENT '来源对象ID' AFTER `source_object_type`"
        );
        ensureColumnExists(
                dataSource,
                "ai_approval_record",
                "requester_user_id",
                "ALTER TABLE `ai_approval_record` ADD COLUMN `requester_user_id` varchar(36) COMMENT '发起人用户ID' AFTER `source_object_id`"
        );
        ensureColumnExists(
                dataSource,
                "ai_approval_record",
                "description",
                "ALTER TABLE `ai_approval_record` ADD COLUMN `description` text COMMENT '背景说明' AFTER `approver_user_id`"
        );
        ensureColumnExists(
                dataSource,
                "ai_approval_record",
                "risk_summary",
                "ALTER TABLE `ai_approval_record` ADD COLUMN `risk_summary` text COMMENT '风险摘要' AFTER `description`"
        );
        ensureColumnExists(
                dataSource,
                "ai_approval_record",
                "budget_impact_summary",
                "ALTER TABLE `ai_approval_record` ADD COLUMN `budget_impact_summary` text COMMENT '预算影响摘要' AFTER `risk_summary`"
        );
        ensureColumnExists(
                dataSource,
                "ai_approval_record",
                "recommended_action",
                "ALTER TABLE `ai_approval_record` ADD COLUMN `recommended_action` varchar(255) COMMENT '推荐动作' AFTER `budget_impact_summary`"
        );
        ensureColumnExists(
                dataSource,
                "ai_approval_record",
                "blocker_flag",
                "ALTER TABLE `ai_approval_record` ADD COLUMN `blocker_flag` tinyint(1) NOT NULL DEFAULT 0 COMMENT '是否阻塞' AFTER `recommended_action`"
        );
        ensureColumnExists(
                dataSource,
                "ai_approval_record",
                "operator_user_id",
                "ALTER TABLE `ai_approval_record` ADD COLUMN `operator_user_id` varchar(36) COMMENT '最近操作人用户ID' AFTER `approval_status`"
        );

        ensureColumnExists(
                dataSource,
                "ai_project_governance_state",
                "current_stage_code",
                "ALTER TABLE `ai_project_governance_state` ADD COLUMN `current_stage_code` varchar(64) COMMENT '当前阶段编码' AFTER `project_id`"
        );
        ensureColumnExists(
                dataSource,
                "ai_project_governance_state",
                "current_stage_name",
                "ALTER TABLE `ai_project_governance_state` ADD COLUMN `current_stage_name` varchar(128) COMMENT '当前阶段名称' AFTER `current_stage_code`"
        );
        ensureColumnExists(
                dataSource,
                "ai_project_governance_state",
                "current_stage_status",
                "ALTER TABLE `ai_project_governance_state` ADD COLUMN `current_stage_status` varchar(32) COMMENT '当前阶段状态' AFTER `current_stage_name`"
        );
        ensureColumnExists(
                dataSource,
                "ai_project_governance_state",
                "current_gate_status",
                "ALTER TABLE `ai_project_governance_state` ADD COLUMN `current_gate_status` varchar(32) COMMENT '当前门禁状态' AFTER `current_stage_status`"
        );
        ensureColumnExists(
                dataSource,
                "ai_project_governance_state",
                "governance_status",
                "ALTER TABLE `ai_project_governance_state` ADD COLUMN `governance_status` varchar(32) NOT NULL DEFAULT 'healthy' COMMENT '治理状态' AFTER `current_gate_status`"
        );
        ensureColumnExists(
                dataSource,
                "ai_project_governance_state",
                "blocked_flag",
                "ALTER TABLE `ai_project_governance_state` ADD COLUMN `blocked_flag` tinyint(1) NOT NULL DEFAULT 0 COMMENT '是否阻塞' AFTER `governance_status`"
        );
        ensureColumnExists(
                dataSource,
                "ai_project_governance_state",
                "at_risk_flag",
                "ALTER TABLE `ai_project_governance_state` ADD COLUMN `at_risk_flag` tinyint(1) NOT NULL DEFAULT 0 COMMENT '是否风险' AFTER `blocked_flag`"
        );
        ensureColumnExists(
                dataSource,
                "ai_project_governance_state",
                "pending_decision_count",
                "ALTER TABLE `ai_project_governance_state` ADD COLUMN `pending_decision_count` int NOT NULL DEFAULT 0 COMMENT '待处理决策数' AFTER `at_risk_flag`"
        );
        ensureColumnExists(
                dataSource,
                "ai_project_governance_state",
                "blocker_decision_count",
                "ALTER TABLE `ai_project_governance_state` ADD COLUMN `blocker_decision_count` int NOT NULL DEFAULT 0 COMMENT '阻塞决策数' AFTER `pending_decision_count`"
        );
        ensureColumnExists(
                dataSource,
                "ai_project_governance_state",
                "pending_approval_count",
                "ALTER TABLE `ai_project_governance_state` ADD COLUMN `pending_approval_count` int NOT NULL DEFAULT 0 COMMENT '待处理审批数' AFTER `blocker_decision_count`"
        );
        ensureColumnExists(
                dataSource,
                "ai_project_governance_state",
                "blocker_approval_count",
                "ALTER TABLE `ai_project_governance_state` ADD COLUMN `blocker_approval_count` int NOT NULL DEFAULT 0 COMMENT '阻塞审批数' AFTER `pending_approval_count`"
        );
        ensureColumnExists(
                dataSource,
                "ai_project_governance_state",
                "clarification_count",
                "ALTER TABLE `ai_project_governance_state` ADD COLUMN `clarification_count` int NOT NULL DEFAULT 0 COMMENT '澄清项总数' AFTER `blocker_approval_count`"
        );
        ensureColumnExists(
                dataSource,
                "ai_project_governance_state",
                "blocker_clarification_count",
                "ALTER TABLE `ai_project_governance_state` ADD COLUMN `blocker_clarification_count` int NOT NULL DEFAULT 0 COMMENT '阻塞澄清项数' AFTER `clarification_count`"
        );
        ensureColumnExists(
                dataSource,
                "ai_project_governance_state",
                "failed_gate_condition_count",
                "ALTER TABLE `ai_project_governance_state` ADD COLUMN `failed_gate_condition_count` int NOT NULL DEFAULT 0 COMMENT '失败门禁条件数' AFTER `blocker_clarification_count`"
        );
        ensureColumnExists(
                dataSource,
                "ai_project_governance_state",
                "blocking_gate_condition_count",
                "ALTER TABLE `ai_project_governance_state` ADD COLUMN `blocking_gate_condition_count` int NOT NULL DEFAULT 0 COMMENT '阻塞门禁条件数' AFTER `failed_gate_condition_count`"
        );
        ensureColumnExists(
                dataSource,
                "ai_project_governance_state",
                "missing_critical_role_count",
                "ALTER TABLE `ai_project_governance_state` ADD COLUMN `missing_critical_role_count` int NOT NULL DEFAULT 0 COMMENT '缺失关键角色数' AFTER `blocking_gate_condition_count`"
        );
        ensureColumnExists(
                dataSource,
                "ai_project_governance_state",
                "budget_status",
                "ALTER TABLE `ai_project_governance_state` ADD COLUMN `budget_status` varchar(32) COMMENT '预算健康状态' AFTER `missing_critical_role_count`"
        );
        ensureColumnExists(
                dataSource,
                "ai_project_governance_state",
                "blocker_reason_summary",
                "ALTER TABLE `ai_project_governance_state` ADD COLUMN `blocker_reason_summary` varchar(500) COMMENT '治理原因摘要' AFTER `budget_status`"
        );
        ensureColumnExists(
                dataSource,
                "ai_project_governance_state",
                "last_recomputed_at",
                "ALTER TABLE `ai_project_governance_state` ADD COLUMN `last_recomputed_at` datetime COMMENT '最近重算时间' AFTER `blocker_reason_summary`"
        );
    }

    private void ensureColumnExists(DataSource dataSource, String tableName, String columnName, String alterSql)
            throws SQLException {
        if (!tableExists(dataSource, tableName) || columnExists(dataSource, tableName, columnName)) {
            return;
        }
        log.info("Bootstrap phase 1 domain column {}.{} using SQL", tableName, columnName);
        executeStatement(dataSource, alterSql);
    }

    private boolean columnExists(DataSource dataSource, String tableName, String columnName) throws SQLException {
        try (Connection connection = dataSource.getConnection()) {
            DatabaseMetaData metaData = connection.getMetaData();
            String catalog = connection.getCatalog();
            return lookupColumn(metaData, catalog, tableName, columnName)
                    || lookupColumn(metaData, catalog, tableName.toLowerCase(), columnName.toLowerCase())
                    || lookupColumn(metaData, catalog, tableName.toUpperCase(), columnName.toUpperCase());
        }
    }

    private boolean lookupColumn(DatabaseMetaData metaData, String catalog, String tableName, String columnName)
            throws SQLException {
        try (ResultSet resultSet = metaData.getColumns(catalog, null, tableName, columnName)) {
            return resultSet.next();
        }
    }

    private void executeStatement(DataSource dataSource, String sql) throws SQLException {
        try (Connection connection = dataSource.getConnection();
             Statement statement = connection.createStatement()) {
            statement.execute(sql);
        }
    }

    private void runScript(DataSource dataSource, String classpathLocation) {
        try {
            log.info("Bootstrap SaaS template enhancements using {}", classpathLocation);
            ResourceDatabasePopulator populator = new ResourceDatabasePopulator();
            populator.setContinueOnError(false);
            populator.setIgnoreFailedDrops(true);
            populator.addScript(new ClassPathResource(classpathLocation));
            DatabasePopulatorUtils.execute(populator, dataSource);
        } catch (Exception ex) {
            throw new IllegalStateException(
                    "Bootstrap SaaS template enhancements failed for " + Objects.toString(classpathLocation),
                    ex
            );
        }
    }
}
