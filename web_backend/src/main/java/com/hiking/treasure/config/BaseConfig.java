package com.hiking.treasure.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

/**
 * 通用基础配置（替代 JeecgBaseConfig）
 */
@Data
@Configuration
@ConfigurationProperties(prefix = "app.base")
public class BaseConfig {

    /** 文件/图片等上传的根路径（用于导出含图片时定位资源根） */
    private Path path = new Path();

    @Data
    public static class Path {
        /** 例如：/data/upload 或者 D:/upload */
        private String upload;
    }
}

