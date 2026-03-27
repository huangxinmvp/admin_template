package com.hiking.treasure.domain.vo.system;

import lombok.Data;

@Data
public class UserSessionCountVO {
    private String userId;
    private Long activeCount;
}
