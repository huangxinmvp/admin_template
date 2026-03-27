package com.hiking.treasure.domain.vo.system;

import lombok.Data;

@Data
public class MessageCenterChannelVO {
    private String code;
    private String name;
    private Boolean configured;
    private String summary;
    private String targetPath;
}
