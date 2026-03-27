package com.hiking.treasure.domain.vo.system;

import lombok.Data;

import java.util.ArrayList;
import java.util.List;

@Data
public class MenuTreeVO {
    private String id;
    private String parentId;
    private String name;
    private String path;
    private String component;
    private String perms;
    private Integer type;
    private Integer sortNo;
    private Integer hidden;
    private Integer alwaysShow;
    private String icon;
    private List<MenuTreeVO> children = new ArrayList<>();
}
