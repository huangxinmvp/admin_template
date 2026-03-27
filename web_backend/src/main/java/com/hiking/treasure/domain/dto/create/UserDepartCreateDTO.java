package com.hiking.treasure.domain.dto.create;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@Schema(description = "用户-部门关系 - 新增DTO" )
public class UserDepartCreateDTO {
    @Schema(description = "用户ID" )
    private String userId;
    @Schema(description = "部门ID" )
    private String departId;
    @Schema(description = "关系类型:1主部门 2兼任" )
    private Integer relType;
}
