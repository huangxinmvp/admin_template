package com.hiking.treasure.domain.vo.system;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.util.List;

@Data
@Schema(description = "菜单整树保存结果")
public class PermissionTreeSaveResultVO {
    @Schema(description = "更新节点数")
    private Integer updatedCount;

    @Schema(description = "保存后的最新菜单树")
    private List<PermissionTreeVO> tree;
}
