package com.hiking.treasure.domain.vo.system;

import lombok.Data;

import java.util.List;

@Data
public class DeleteConflictDetailVO {
    private String code;
    private String label;
    private List<DeleteConflictItemVO> items;

    public static DeleteConflictDetailVO of(String code, String label, List<DeleteConflictItemVO> items) {
        DeleteConflictDetailVO detail = new DeleteConflictDetailVO();
        detail.setCode(code);
        detail.setLabel(label);
        detail.setItems(items);
        return detail;
    }
}
