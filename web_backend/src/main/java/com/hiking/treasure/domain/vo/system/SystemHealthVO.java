package com.hiking.treasure.domain.vo.system;

import lombok.Data;

@Data
public class SystemHealthVO {
    private String status;
    private String application;
    private Boolean databaseReady;
    private Long timestamp;
}
