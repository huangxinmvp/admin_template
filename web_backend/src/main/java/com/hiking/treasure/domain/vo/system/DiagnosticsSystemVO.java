package com.hiking.treasure.domain.vo.system;

import lombok.Data;

import java.util.Map;

@Data
public class DiagnosticsSystemVO {
    private Map<String, DiagnosticsOperationVO> operations;
}
