from __future__ import annotations

from dataclasses import dataclass
from threading import Lock
import time


@dataclass
class RuntimeFailure:
    classification: str
    message: str
    request_id: str
    occurred_at: float


@dataclass
class RuntimeOperation:
    success_count: int = 0
    failure_count: int = 0
    total_duration_ms: int = 0
    last_duration_ms: int = 0
    last_success_at: float | None = None
    last_failure: RuntimeFailure | None = None

    def average_duration_ms(self) -> float:
        total_calls = self.success_count + self.failure_count
        if total_calls <= 0:
            return 0.0
        return self.total_duration_ms / total_calls


class RuntimeDiagnostics:
    def __init__(self) -> None:
        self._lock = Lock()
        self._operations: dict[str, RuntimeOperation] = {}

    def record(
        self,
        path: str,
        success: bool,
        duration_ms: int,
        classification: str,
        request_id: str,
        message: str | None = None,
    ) -> None:
        with self._lock:
            operation = self._operations.setdefault(path, RuntimeOperation())
            operation.total_duration_ms += max(0, duration_ms)
            operation.last_duration_ms = max(0, duration_ms)
            if success:
                operation.success_count += 1
                operation.last_success_at = time.time()
            else:
                operation.failure_count += 1
                operation.last_failure = RuntimeFailure(
                    classification=classification,
                    message=message or "",
                    request_id=request_id,
                    occurred_at=time.time(),
                )

    def snapshot(self, request_id: str | None) -> dict:
        with self._lock:
            operations = {
                path: {
                    "successCount": operation.success_count,
                    "failureCount": operation.failure_count,
                    "lastDurationMs": operation.last_duration_ms,
                    "averageDurationMs": operation.average_duration_ms(),
                    "lastSuccessAt": operation.last_success_at,
                    "lastFailure": None
                    if operation.last_failure is None
                    else {
                        "classification": operation.last_failure.classification,
                        "message": operation.last_failure.message,
                        "requestId": operation.last_failure.request_id,
                        "occurredAt": operation.last_failure.occurred_at,
                    },
                }
                for path, operation in sorted(self._operations.items())
            }
        return {
            "requestId": request_id,
            "capturedAt": time.time(),
            "operations": operations,
        }


runtime_diagnostics = RuntimeDiagnostics()
