package com.hiking.treasure.modules.phase1.domain.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Schema(description = "AICoOS 项目工具绑定")
public class ProjectToolBindingVO {

    private String id;

    private String projectId;

    private String toolType;

    private String bindingType;

    private String externalId;

    private String externalKey;

    private String externalName;

    private String externalUrl;

    private String bindingStatus;

    private Integer defaultFlag;

    private String metadataJson;

    private String remark;

    private LocalDateTime createTime;

    private LocalDateTime updateTime;
}
