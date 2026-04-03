package com.hiking.treasure.common.util;

import com.hiking.treasure.common.web.RequestCorrelation;

import java.io.IOException;
import java.net.ConnectException;
import java.net.http.HttpTimeoutException;
import java.util.UUID;

public final class OutboundRequestSupport {

    public static final String REQUEST_ID_HEADER = RequestCorrelation.HEADER_NAME;

    private OutboundRequestSupport() {
    }

    public static String currentRequestIdOrGenerate() {
        String requestId = RequestCorrelation.currentRequestId();
        return requestId == null || requestId.isBlank() ? UUID.randomUUID().toString() : requestId;
    }

    public static boolean isRetryableStatus(int statusCode) {
        return statusCode == 408
                || statusCode == 429
                || statusCode == 502
                || statusCode == 503
                || statusCode == 504;
    }

    public static boolean isRetryableException(Throwable throwable) {
        return throwable instanceof HttpTimeoutException
                || throwable instanceof ConnectException
                || throwable instanceof IOException;
    }

    public static void backoff(int attempt) throws InterruptedException {
        Thread.sleep(150L * Math.max(1, attempt));
    }
}
