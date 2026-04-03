package com.hiking.treasure.modules.phase1.domain.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
@Schema(description = "AICoOS 项目中心 - 当前门禁条件")
public class ProjectGateConditionVO {

    @Schema(description = "条件键")
    private String key;

    @Schema(description = "条件标题")
    private String title;

    @Schema(description = "状态")
    private String status;

    @Schema(description = "摘要")
    private String summary;
}
