package com.hiking.treasure.domain.dto.create;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Schema(description = "租户 - 新增DTO")
public class TenantCreateDTO {
    @NotBlank
    @Schema(description = "租户名称")
    private String tenantName;

    @NotBlank
    @Schema(description = "租户编码")
    private String tenantCode;

    @Schema(description = "联系人")
    private String contactName;

    @Schema(description = "联系电话")
    private String contactPhone;

    @Schema(description = "联系邮箱")
    private String contactEmail;

    @Schema(description = "套餐名称")
    private String packageName;

    @Schema(description = "到期时间")
    private LocalDateTime expireTime;

    @Schema(description = "状态: 1启用 0停用")
    private Integer status;

    @Schema(description = "备注")
    private String remark;
}
