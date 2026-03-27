package com.hiking.treasure.domain.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@Schema(description = "用户-部门关系 - VO" )
public class UserDepartVO {
    @Schema(description = "主键ID" )
    private String id;
    @Schema(description = "用户ID" )
    private String userId;
    @Schema(description = "部门ID" )
    private String departId;
    @Schema(description = "关系类型:1主部门 2兼任" )
    private Integer relType;
    @Schema(description = "创建人" )
    private String createBy;
    @Schema(description = "创建时间" )
    private LocalDateTime createTime;
}
