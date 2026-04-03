package com.hiking.treasure.modules.phase1.domain.dto.command;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
@Schema(description = "生成产品与架构协作简报 DTO")
public class ProductArchitectureBriefGenerateDTO {

    @Schema(description = "操作员关注点说明")
    private String focusNotes;
}
