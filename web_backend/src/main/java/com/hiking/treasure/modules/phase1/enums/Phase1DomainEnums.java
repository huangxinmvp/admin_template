package com.hiking.treasure.modules.phase1.enums;

public final class Phase1DomainEnums {

    private Phase1DomainEnums() {
    }

    public interface CodeEnum {
        String getCode();

        String getLabel();
    }

    public enum ProjectType implements CodeEnum {
        DELIVERY("delivery", "交付项目"),
        CONSULTING("consulting", "咨询项目"),
        INTERNAL("internal", "内部项目");

        private final String code;
        private final String label;

        ProjectType(String code, String label) {
            this.code = code;
            this.label = label;
        }

        @Override
        public String getCode() {
            return code;
        }

        @Override
        public String getLabel() {
            return label;
        }
    }

    public enum ProjectStatus implements CodeEnum {
        DRAFT("draft", "草稿"),
        ACTIVE("active", "进行中"),
        PAUSED("paused", "已暂停"),
        COMPLETED("completed", "已完成"),
        CANCELLED("cancelled", "已取消");

        private final String code;
        private final String label;

        ProjectStatus(String code, String label) {
            this.code = code;
            this.label = label;
        }

        @Override
        public String getCode() {
            return code;
        }

        @Override
        public String getLabel() {
            return label;
        }
    }

    public enum RiskLevel implements CodeEnum {
        LOW("low", "低"),
        MEDIUM("medium", "中"),
        HIGH("high", "高"),
        CRITICAL("critical", "关键");

        private final String code;
        private final String label;

        RiskLevel(String code, String label) {
            this.code = code;
            this.label = label;
        }

        @Override
        public String getCode() {
            return code;
        }

        @Override
        public String getLabel() {
            return label;
        }
    }

    public enum ProjectGovernanceStatus implements CodeEnum {
        HEALTHY("healthy", "正常"),
        AT_RISK("at_risk", "风险"),
        BLOCKED("blocked", "阻塞");

        private final String code;
        private final String label;

        ProjectGovernanceStatus(String code, String label) {
            this.code = code;
            this.label = label;
        }

        @Override
        public String getCode() {
            return code;
        }

        @Override
        public String getLabel() {
            return label;
        }
    }

    public enum ProjectLifecycleStage implements CodeEnum {
        INTAKE("intake", "需求接收"),
        CLARIFICATION("clarification", "需求澄清"),
        FEASIBILITY("feasibility", "可行性评估"),
        ESTIMATION("estimation", "预算估算"),
        APPROVAL("approval", "审批"),
        PLANNING("planning", "计划"),
        DESIGN("design", "设计"),
        DEVELOPMENT("development", "开发"),
        TESTING("testing", "测试"),
        RELEASE_APPROVAL("release_approval", "发布审批"),
        RELEASE("release", "发布"),
        RETROSPECTIVE("retrospective", "复盘");

        private final String code;
        private final String label;

        ProjectLifecycleStage(String code, String label) {
            this.code = code;
            this.label = label;
        }

        @Override
        public String getCode() {
            return code;
        }

        @Override
        public String getLabel() {
            return label;
        }
    }

    public enum ProjectStageStatus implements CodeEnum {
        PENDING("pending", "待开始"),
        ACTIVE("active", "进行中"),
        COMPLETED("completed", "已完成"),
        BLOCKED("blocked", "已阻塞");

        private final String code;
        private final String label;

        ProjectStageStatus(String code, String label) {
            this.code = code;
            this.label = label;
        }

        @Override
        public String getCode() {
            return code;
        }

        @Override
        public String getLabel() {
            return label;
        }
    }

    public enum GateStatus implements CodeEnum {
        NOT_REQUIRED("not_required", "无需门禁"),
        PENDING("pending", "待确认"),
        APPROVED("approved", "已通过"),
        REJECTED("rejected", "已拒绝");

        private final String code;
        private final String label;

        GateStatus(String code, String label) {
            this.code = code;
            this.label = label;
        }

        @Override
        public String getCode() {
            return code;
        }

        @Override
        public String getLabel() {
            return label;
        }
    }

    public enum DecisionItemType implements CodeEnum {
        CLARIFICATION("clarification", "澄清事项"),
        REQUIREMENT_CONFIRMATION("requirement_confirmation", "需求确认"),
        SOLUTION_DIRECTION("solution_direction", "方案确认"),
        TIMELINE_CONFIRMATION("timeline_confirmation", "排期确认"),
        SCOPE_CHANGE("scope_change", "范围变更"),
        BUDGET_CHANGE("budget_change", "预算变更"),
        RELEASE("release", "发布决策");

        private final String code;
        private final String label;

        DecisionItemType(String code, String label) {
            this.code = code;
            this.label = label;
        }

        @Override
        public String getCode() {
            return code;
        }

        @Override
        public String getLabel() {
            return label;
        }
    }

    public enum DecisionPriority implements CodeEnum {
        LOW("low", "低"),
        MEDIUM("medium", "中"),
        HIGH("high", "高"),
        CRITICAL("critical", "关键");

        private final String code;
        private final String label;

        DecisionPriority(String code, String label) {
            this.code = code;
            this.label = label;
        }

        @Override
        public String getCode() {
            return code;
        }

        @Override
        public String getLabel() {
            return label;
        }
    }

    public enum DecisionItemStatus implements CodeEnum {
        OPEN("open", "待确认"),
        PENDING_APPROVAL("pending_approval", "待审批"),
        CONFIRMED("confirmed", "已确认"),
        REJECTED("rejected", "已拒绝"),
        DEFERRED("deferred", "已暂缓"),
        RESOLVED("resolved", "已解决");

        private final String code;
        private final String label;

        DecisionItemStatus(String code, String label) {
            this.code = code;
            this.label = label;
        }

        @Override
        public String getCode() {
            return code;
        }

        @Override
        public String getLabel() {
            return label;
        }
    }

    public enum DecisionSourceType implements CodeEnum {
        MANUAL("manual", "人工创建"),
        CLARIFICATION("clarification", "来自澄清项"),
        INTAKE("intake", "来自需求接收"),
        PROJECT_CENTER("project_center", "来自项目中心");

        private final String code;
        private final String label;

        DecisionSourceType(String code, String label) {
            this.code = code;
            this.label = label;
        }

        @Override
        public String getCode() {
            return code;
        }

        @Override
        public String getLabel() {
            return label;
        }
    }

    public enum DecisionActionType implements CodeEnum {
        PROMOTED("promoted", "由澄清项提升"),
        CONFIRM("confirm", "确认"),
        REJECT("reject", "拒绝"),
        DEFER("defer", "暂缓");

        private final String code;
        private final String label;

        DecisionActionType(String code, String label) {
            this.code = code;
            this.label = label;
        }

        @Override
        public String getCode() {
            return code;
        }

        @Override
        public String getLabel() {
            return label;
        }
    }

    public enum ClarificationCategory implements CodeEnum {
        BUSINESS_GOAL("business_goal", "业务目标"),
        FEATURE_SCOPE("feature_scope", "功能范围"),
        REFERENCE_BENCHMARK("reference_benchmark", "参考产品"),
        TIMELINE("timeline", "时间预期"),
        BUDGET("budget", "预算约束"),
        TECHNICAL_CONSTRAINT("technical_constraint", "技术约束"),
        INTEGRATION("integration", "集成依赖"),
        ACCEPTANCE("acceptance", "验收标准");

        private final String code;
        private final String label;

        ClarificationCategory(String code, String label) {
            this.code = code;
            this.label = label;
        }

        @Override
        public String getCode() {
            return code;
        }

        @Override
        public String getLabel() {
            return label;
        }
    }

    public enum ClarificationSeverity implements CodeEnum {
        LOW("low", "低"),
        MEDIUM("medium", "中"),
        HIGH("high", "高"),
        BLOCKER("blocker", "阻塞");

        private final String code;
        private final String label;

        ClarificationSeverity(String code, String label) {
            this.code = code;
            this.label = label;
        }

        @Override
        public String getCode() {
            return code;
        }

        @Override
        public String getLabel() {
            return label;
        }
    }

    public enum ClarificationStatus implements CodeEnum {
        OPEN("open", "待澄清"),
        AWAITING_RESPONSE("awaiting_response", "待用户回复"),
        ANSWERED("answered", "已回复待整理"),
        RESOLVED("resolved", "已解决");

        private final String code;
        private final String label;

        ClarificationStatus(String code, String label) {
            this.code = code;
            this.label = label;
        }

        @Override
        public String getCode() {
            return code;
        }

        @Override
        public String getLabel() {
            return label;
        }
    }

    public enum ApprovalType implements CodeEnum {
        REQUIREMENT("requirement", "需求审批"),
        DECISION("decision", "决策审批"),
        BUDGET("budget", "预算审批"),
        BUDGET_CHANGE("budget_change", "预算变更审批"),
        RISK_OPERATION("risk_operation", "风险操作审批"),
        GATE("gate", "关卡审批"),
        RELEASE("release", "发布审批");

        private final String code;
        private final String label;

        ApprovalType(String code, String label) {
            this.code = code;
            this.label = label;
        }

        @Override
        public String getCode() {
            return code;
        }

        @Override
        public String getLabel() {
            return label;
        }
    }

    public enum ApprovalStatus implements CodeEnum {
        DRAFT("draft", "草稿"),
        SUBMITTED("submitted", "已提交"),
        REQUEST_CHANGES("request_changes", "要求修改"),
        DEFERRED("deferred", "已暂缓"),
        APPROVED("approved", "已批准"),
        REJECTED("rejected", "已拒绝"),
        CANCELLED("cancelled", "已取消");

        private final String code;
        private final String label;

        ApprovalStatus(String code, String label) {
            this.code = code;
            this.label = label;
        }

        @Override
        public String getCode() {
            return code;
        }

        @Override
        public String getLabel() {
            return label;
        }
    }

    public enum ApprovalActionType implements CodeEnum {
        CREATED("created", "发起审批"),
        APPROVE("approve", "批准"),
        REJECT("reject", "拒绝"),
        REQUEST_CHANGES("request_changes", "要求修改"),
        DEFER("defer", "暂缓");

        private final String code;
        private final String label;

        ApprovalActionType(String code, String label) {
            this.code = code;
            this.label = label;
        }

        @Override
        public String getCode() {
            return code;
        }

        @Override
        public String getLabel() {
            return label;
        }
    }

    public enum ApprovalSourceObjectType implements CodeEnum {
        MANUAL("manual", "人工发起"),
        DECISION_ITEM("decision_item", "决策事项"),
        BUDGET_PLAN("budget_plan", "预算计划"),
        BUDGET_LEDGER("budget_ledger", "预算流水"),
        PROJECT("project", "项目"),
        PROJECT_STAGE("project_stage", "项目阶段");

        private final String code;
        private final String label;

        ApprovalSourceObjectType(String code, String label) {
            this.code = code;
            this.label = label;
        }

        @Override
        public String getCode() {
            return code;
        }

        @Override
        public String getLabel() {
            return label;
        }
    }

    public enum BudgetPlanStatus implements CodeEnum {
        DRAFT("draft", "草稿"),
        PENDING_APPROVAL("pending_approval", "待审批"),
        APPROVED("approved", "已批准"),
        CLOSED("closed", "已关闭");

        private final String code;
        private final String label;

        BudgetPlanStatus(String code, String label) {
            this.code = code;
            this.label = label;
        }

        @Override
        public String getCode() {
            return code;
        }

        @Override
        public String getLabel() {
            return label;
        }
    }

    public enum BudgetLedgerEntryType implements CodeEnum {
        CREATE("create", "预算创建"),
        LOCK("lock", "预算锁定"),
        PENDING_INCREASE("pending_increase", "待增补预算"),
        MANUAL_CORRECTION("manual_correction", "人工修正"),
        RESERVE("reserve", "预算预留"),
        CONSUME("consume", "预算消耗"),
        ADJUST("adjust", "预算调整"),
        REFUND("refund", "预算回退");

        private final String code;
        private final String label;

        BudgetLedgerEntryType(String code, String label) {
            this.code = code;
            this.label = label;
        }

        @Override
        public String getCode() {
            return code;
        }

        @Override
        public String getLabel() {
            return label;
        }
    }

    public enum BudgetHealthStatus implements CodeEnum {
        UNPLANNED("unplanned", "未规划"),
        PENDING("pending", "待审批"),
        HEALTHY("healthy", "正常"),
        WARNING("warning", "预警"),
        OVERRUN("overrun", "超支");

        private final String code;
        private final String label;

        BudgetHealthStatus(String code, String label) {
            this.code = code;
            this.label = label;
        }

        @Override
        public String getCode() {
            return code;
        }

        @Override
        public String getLabel() {
            return label;
        }
    }

    public enum BudgetRoleCode implements CodeEnum {
        PRODUCT_ANALYSIS("product_analysis", "产品 / 分析"),
        ARCHITECT("architect", "架构"),
        UI_UX("ui_ux", "UI / UX"),
        FRONTEND("frontend", "前端"),
        BACKEND("backend", "后端"),
        QA("qa", "QA"),
        DEVOPS("devops", "DevOps"),
        PROJECT_COORDINATION("project_coordination", "项目协同");

        private final String code;
        private final String label;

        BudgetRoleCode(String code, String label) {
            this.code = code;
            this.label = label;
        }

        @Override
        public String getCode() {
            return code;
        }

        @Override
        public String getLabel() {
            return label;
        }
    }

    public enum AgentRoleCategory implements CodeEnum {
        GOVERNANCE("governance", "治理角色"),
        DELIVERY("delivery", "交付角色"),
        SUPPORT("support", "支持角色");

        private final String code;
        private final String label;

        AgentRoleCategory(String code, String label) {
            this.code = code;
            this.label = label;
        }

        @Override
        public String getCode() {
            return code;
        }

        @Override
        public String getLabel() {
            return label;
        }
    }

    public enum AgentRoleStatus implements CodeEnum {
        ACTIVE("active", "启用"),
        INACTIVE("inactive", "停用");

        private final String code;
        private final String label;

        AgentRoleStatus(String code, String label) {
            this.code = code;
            this.label = label;
        }

        @Override
        public String getCode() {
            return code;
        }

        @Override
        public String getLabel() {
            return label;
        }
    }

    public enum AgentStageParticipationType implements CodeEnum {
        REQUIRED("required", "必需"),
        OPTIONAL("optional", "可选"),
        NOT_INVOLVED("not_involved", "不参与");

        private final String code;
        private final String label;

        AgentStageParticipationType(String code, String label) {
            this.code = code;
            this.label = label;
        }

        @Override
        public String getCode() {
            return code;
        }

        @Override
        public String getLabel() {
            return label;
        }
    }

    public enum AgentAllowedActionCode implements CodeEnum {
        CLARIFICATION_MANAGE("clarification_manage", "创建/更新澄清项"),
        DECISION_CREATE("decision_create", "创建决策事项"),
        BUDGET_CHANGE_PROPOSE("budget_change_propose", "提出预算变更"),
        APPROVAL_REQUEST("approval_request", "发起审批请求"),
        STAGE_SUGGESTION_UPDATE("stage_suggestion_update", "更新阶段建议"),
        RELEASE_ITEM_PREPARE("release_item_prepare", "准备发布事项"),
        HIGH_RISK_ACTION("high_risk_action", "高风险动作");

        private final String code;
        private final String label;

        AgentAllowedActionCode(String code, String label) {
            this.code = code;
            this.label = label;
        }

        @Override
        public String getCode() {
            return code;
        }

        @Override
        public String getLabel() {
            return label;
        }
    }

    public enum WorkflowTemplateStatus implements CodeEnum {
        DRAFT("draft", "草稿"),
        ACTIVE("active", "启用"),
        ARCHIVED("archived", "归档");

        private final String code;
        private final String label;

        WorkflowTemplateStatus(String code, String label) {
            this.code = code;
            this.label = label;
        }

        @Override
        public String getCode() {
            return code;
        }

        @Override
        public String getLabel() {
            return label;
        }
    }

    public enum ExternalToolType implements CodeEnum {
        LINEAR("linear", "Linear"),
        FIGMA("figma", "Figma");

        private final String code;
        private final String label;

        ExternalToolType(String code, String label) {
            this.code = code;
            this.label = label;
        }

        @Override
        public String getCode() {
            return code;
        }

        @Override
        public String getLabel() {
            return label;
        }
    }

    public enum ProjectToolBindingType implements CodeEnum {
        LINEAR_PRIMARY_ISSUE("linear_primary_issue", "Linear 主工作项"),
        FIGMA_FILE("figma_file", "Figma 文件"),
        FIGMA_NODE("figma_node", "Figma 节点");

        private final String code;
        private final String label;

        ProjectToolBindingType(String code, String label) {
            this.code = code;
            this.label = label;
        }

        @Override
        public String getCode() {
            return code;
        }

        @Override
        public String getLabel() {
            return label;
        }
    }

    public enum ProjectToolBindingStatus implements CodeEnum {
        LINKED("linked", "已关联"),
        PREVIEW_ONLY("preview_only", "仅预览"),
        ERROR("error", "异常");

        private final String code;
        private final String label;

        ProjectToolBindingStatus(String code, String label) {
            this.code = code;
            this.label = label;
        }

        @Override
        public String getCode() {
            return code;
        }

        @Override
        public String getLabel() {
            return label;
        }
    }

    public enum ToolIntegrationActionType implements CodeEnum {
        LINEAR_PROJECT_PREVIEW("linear_project_preview", "Linear 项目映射预览"),
        LINEAR_PROJECT_APPLY("linear_project_apply", "Linear 项目映射写入"),
        LINEAR_ISSUE_PREVIEW("linear_issue_preview", "Linear Issue 预览"),
        LINEAR_ISSUE_APPLY("linear_issue_apply", "Linear Issue 写入"),
        LINEAR_COMMENT_PREVIEW("linear_comment_preview", "Linear Comment 预览"),
        LINEAR_COMMENT_APPLY("linear_comment_apply", "Linear Comment 写入"),
        FIGMA_CONTEXT_PREVIEW("figma_context_preview", "Figma 上下文预览"),
        FIGMA_CONTEXT_APPLY("figma_context_apply", "Figma 上下文关联");

        private final String code;
        private final String label;

        ToolIntegrationActionType(String code, String label) {
            this.code = code;
            this.label = label;
        }

        @Override
        public String getCode() {
            return code;
        }

        @Override
        public String getLabel() {
            return label;
        }
    }

    public enum ToolIntegrationAuditStatus implements CodeEnum {
        PREVIEWED("previewed", "已预览"),
        SUCCEEDED("succeeded", "成功"),
        FAILED("failed", "失败");

        private final String code;
        private final String label;

        ToolIntegrationAuditStatus(String code, String label) {
            this.code = code;
            this.label = label;
        }

        @Override
        public String getCode() {
            return code;
        }

        @Override
        public String getLabel() {
            return label;
        }
    }

    public enum LinearWriteMode implements CodeEnum {
        ISSUE("issue", "Issue"),
        COMMENT("comment", "Comment");

        private final String code;
        private final String label;

        LinearWriteMode(String code, String label) {
            this.code = code;
            this.label = label;
        }

        @Override
        public String getCode() {
            return code;
        }

        @Override
        public String getLabel() {
            return label;
        }
    }
}
