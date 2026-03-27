package com.hiking.treasure.domain.vo.system;

import lombok.Data;

import java.util.ArrayList;
import java.util.List;

@Data
public class SystemConfigGroupVO {
    private String groupCode;
    private String groupName;
    private Integer groupSort;
    private List<SystemConfigItemVO> items = new ArrayList<>();
}
