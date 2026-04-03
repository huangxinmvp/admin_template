package com.hiking.treasure.domain.vo.system;

import lombok.Data;

@Data
public class DiagnosticsOperationVO {
    private long successCount;
    private long failureCount;
    private long lastDurationMs;
    private double averageDurationMs;
    private Long lastSuccessAt;
    private DiagnosticsFailureVO lastFailure;
}
