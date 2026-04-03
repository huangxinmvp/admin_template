package com.hiking.treasure.common.diagnostics;

import com.hiking.treasure.domain.vo.system.DiagnosticsFailureVO;
import com.hiking.treasure.domain.vo.system.DiagnosticsOperationVO;
import com.hiking.treasure.domain.vo.system.DiagnosticsSnapshotVO;
import com.hiking.treasure.domain.vo.system.DiagnosticsSystemVO;

import java.time.Instant;
import java.util.Map;
import java.util.TreeMap;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

public final class IntegrationDiagnostics {

    private static final ConcurrentHashMap<String, ConcurrentHashMap<String, OperationStats>> STATS = new ConcurrentHashMap<>();

    private IntegrationDiagnostics() {
    }

    public static void recordSuccess(String system, String operation, long durationMs) {
        OperationStats stats = operationStats(system, operation);
        stats.successCount.incrementAndGet();
        stats.totalDurationMs.addAndGet(Math.max(0L, durationMs));
        stats.totalCallCount.incrementAndGet();
        stats.lastDurationMs.set(Math.max(0L, durationMs));
        stats.lastSuccessAt.set(Instant.now().toEpochMilli());
    }

    public static void recordFailure(
            String system,
            String operation,
            String classification,
            String message,
            String requestId,
            long durationMs) {
        OperationStats stats = operationStats(system, operation);
        stats.failureCount.incrementAndGet();
        stats.totalDurationMs.addAndGet(Math.max(0L, durationMs));
        stats.totalCallCount.incrementAndGet();
        stats.lastDurationMs.set(Math.max(0L, durationMs));
        DiagnosticsFailureVO failure = new DiagnosticsFailureVO();
        failure.setClassification(classification);
        failure.setMessage(message);
        failure.setRequestId(requestId);
        failure.setOccurredAt(Instant.now().toEpochMilli());
        stats.lastFailure = failure;
    }

    public static DiagnosticsSnapshotVO snapshot(String requestId) {
        DiagnosticsSnapshotVO snapshot = new DiagnosticsSnapshotVO();
        snapshot.setRequestId(requestId);
        snapshot.setCapturedAt(Instant.now().toEpochMilli());
        Map<String, DiagnosticsSystemVO> systems = new TreeMap<>();
        for (Map.Entry<String, ConcurrentHashMap<String, OperationStats>> systemEntry : STATS.entrySet()) {
            DiagnosticsSystemVO system = new DiagnosticsSystemVO();
            Map<String, DiagnosticsOperationVO> operations = new TreeMap<>();
            for (Map.Entry<String, OperationStats> operationEntry : systemEntry.getValue().entrySet()) {
                OperationStats stats = operationEntry.getValue();
                DiagnosticsOperationVO operation = new DiagnosticsOperationVO();
                operation.setSuccessCount(stats.successCount.get());
                operation.setFailureCount(stats.failureCount.get());
                operation.setLastDurationMs(stats.lastDurationMs.get());
                operation.setAverageDurationMs(stats.averageDurationMs());
                operation.setLastSuccessAt(stats.lastSuccessAt.get() == 0L ? null : stats.lastSuccessAt.get());
                operation.setLastFailure(stats.lastFailure);
                operations.put(operationEntry.getKey(), operation);
            }
            system.setOperations(operations);
            systems.put(systemEntry.getKey(), system);
        }
        snapshot.setSystems(systems);
        return snapshot;
    }

    private static OperationStats operationStats(String system, String operation) {
        return STATS.computeIfAbsent(system, ignored -> new ConcurrentHashMap<>())
                .computeIfAbsent(operation, ignored -> new OperationStats());
    }

    private static final class OperationStats {
        private final AtomicLong successCount = new AtomicLong();
        private final AtomicLong failureCount = new AtomicLong();
        private final AtomicLong totalDurationMs = new AtomicLong();
        private final AtomicLong totalCallCount = new AtomicLong();
        private final AtomicLong lastDurationMs = new AtomicLong();
        private final AtomicLong lastSuccessAt = new AtomicLong();
        private volatile DiagnosticsFailureVO lastFailure;

        private double averageDurationMs() {
            long callCount = totalCallCount.get();
            if (callCount <= 0) {
                return 0D;
            }
            return ((double) totalDurationMs.get()) / callCount;
        }
    }
}
