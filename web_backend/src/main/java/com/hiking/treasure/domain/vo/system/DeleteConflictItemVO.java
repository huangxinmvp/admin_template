package com.hiking.treasure.domain.vo.system;

import lombok.Data;

import java.util.Map;

@Data
public class DeleteConflictItemVO {
    private String id;
    private String name;
    private String type;
    private Map<String, Object> meta;

    public static DeleteConflictItemVO of(String id, String name, String type) {
        return of(id, name, type, null);
    }

    public static DeleteConflictItemVO of(String id, String name, String type, Map<String, Object> meta) {
        DeleteConflictItemVO item = new DeleteConflictItemVO();
        item.setId(id);
        item.setName(name);
        item.setType(type);
        item.setMeta(meta);
        return item;
    }
}
