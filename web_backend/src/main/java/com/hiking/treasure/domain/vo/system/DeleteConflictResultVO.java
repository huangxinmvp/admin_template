package com.hiking.treasure.domain.vo.system;

import lombok.Data;

import java.util.List;

@Data
public class DeleteConflictResultVO {
    private String entityType;
    private List<DeleteConflictDetailVO> details;

    public static DeleteConflictResultVO of(String entityType, List<DeleteConflictDetailVO> details) {
        DeleteConflictResultVO result = new DeleteConflictResultVO();
        result.setEntityType(entityType);
        result.setDetails(details);
        return result;
    }
}
