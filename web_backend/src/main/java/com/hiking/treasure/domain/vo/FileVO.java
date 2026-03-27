package com.hiking.treasure.domain.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@Schema(description = "文件存储记录 - VO" )
public class FileVO {
    @Schema(description = "主键ID" )
    private String id;
    @Schema(description = "原始文件名" )
    private String fileName;
    @Schema(description = "访问URL" )
    private String url;
    @Schema(description = "预览URL" )
    private String previewUrl;
    @Schema(description = "存储对象名" )
    private String objectName;
    @Schema(description = "Content-Type" )
    private String contentType;
    @Schema(description = "文件大小(字节)" )
    private Long fileSize;
    @Schema(description = "存储类型:local/minio/aliyun/qiniu" )
    private String storageType;
    @Schema(description = "存储提供方" )
    private String storageProvider;
    @Schema(description = "Bucket/目录" )
    private String bucketName;
    @Schema(description = "业务类型" )
    private String bizType;
    @Schema(description = "MD5" )
    private String md5;
    @Schema(description = "状态" )
    private Integer status;
    @Schema(description = "删除状态" )
    private Integer delFlag;
    @Schema(description = "创建人" )
    private String createBy;
    @Schema(description = "创建时间" )
    private LocalDateTime createTime;
    @Schema(description = "更新人" )
    private String updateBy;
    @Schema(description = "更新时间" )
    private LocalDateTime updateTime;
}
