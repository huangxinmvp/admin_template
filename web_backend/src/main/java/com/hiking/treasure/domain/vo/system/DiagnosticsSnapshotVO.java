package com.hiking.treasure.domain.vo.system;

import lombok.Data;

import java.util.Map;

@Data
public class DiagnosticsSnapshotVO {
    private String requestId;
    private Long capturedAt;
    private Map<String, DiagnosticsSystemVO> systems;
}
