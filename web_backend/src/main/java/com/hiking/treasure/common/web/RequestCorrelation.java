package com.hiking.treasure.common.web;

import org.slf4j.MDC;

import java.util.UUID;

public final class RequestCorrelation {

    public static final String HEADER_NAME = "X-Request-Id";
    public static final String ATTRIBUTE_NAME = RequestCorrelation.class.getName() + ".requestId";
    public static final String MDC_KEY = "requestId";

    private RequestCorrelation() {
    }

    public static String currentRequestId() {
        return MDC.get(MDC_KEY);
    }

    public static String resolveOrGenerate(String incomingRequestId) {
        if (incomingRequestId != null && !incomingRequestId.isBlank()) {
            return incomingRequestId.trim();
        }
        return UUID.randomUUID().toString();
    }
}
