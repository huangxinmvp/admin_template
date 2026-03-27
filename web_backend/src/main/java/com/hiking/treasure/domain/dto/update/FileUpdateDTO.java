package com.hiking.treasure.domain.dto.update;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@Schema(description = "文件存储记录 - 更新DTO" )
public class FileUpdateDTO {
    @Schema(description = "原始文件名" )
    private String fileName;
    @Schema(description = "访问URL" )
    private String url;
    @Schema(description = "存储对象名" )
    private String objectName;
    @Schema(description = "Content-Type" )
    private String contentType;
    @Schema(description = "文件大小(字节)" )
    private Long fileSize;
    @Schema(description = "存储类型:local/minio/aliyun/qiniu" )
    private String storageType;
    @Schema(description = "MD5" )
    private String md5;
    @Schema(description = "状态" )
    private Integer status;
}
