package com.hiking.treasure.domain.vo.system;

import lombok.Data;

@Data
public class DiagnosticsFailureVO {
    private String classification;
    private String message;
    private String requestId;
    private Long occurredAt;
}
