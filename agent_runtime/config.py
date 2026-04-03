from __future__ import annotations

from dataclasses import dataclass
import os


def _as_bool(value: str | None, default: bool) -> bool:
    if value is None:
        return default
    return value.strip().lower() in {"1", "true", "yes", "on"}


def _as_float(value: str | None, default: float) -> float:
    if value is None:
        return default
    try:
        return float(value)
    except ValueError:
        return default


@dataclass(frozen=True)
class RuntimeSettings:
    api_key: str
    default_provider: str
    default_model: str
    openai_compatible_base_url: str
    openai_compatible_api_key: str
    timeout_seconds: float
    allow_mock_fallback: bool

    def runtime_key_configured(self) -> bool:
        return bool(self.api_key.strip())

    def openai_compatible_ready(self) -> bool:
        return bool(self.openai_compatible_base_url and self.openai_compatible_api_key)

    def default_provider_ready(self) -> bool:
        if self.default_provider.strip().lower() == "openai_compatible":
            return self.openai_compatible_ready()
        return True


def load_settings() -> RuntimeSettings:
    return RuntimeSettings(
        api_key=os.getenv("AGENT_RUNTIME_API_KEY", "agent-runtime-local-key"),
        default_provider=os.getenv("AGENT_RUNTIME_PROVIDER", "mock"),
        default_model=os.getenv("AGENT_RUNTIME_MODEL", "mock-suggestion-v1"),
        openai_compatible_base_url=(
            os.getenv("OPENAI_COMPATIBLE_BASE_URL")
            or os.getenv("OPENAI_BASE_URL")
            or ""
        ).rstrip("/"),
        openai_compatible_api_key=(
            os.getenv("OPENAI_COMPATIBLE_API_KEY")
            or os.getenv("OPENAI_API_KEY")
            or ""
        ),
        timeout_seconds=_as_float(os.getenv("AGENT_RUNTIME_TIMEOUT_SECONDS"), 20.0),
        allow_mock_fallback=_as_bool(os.getenv("AGENT_RUNTIME_ALLOW_MOCK_FALLBACK"), True),
    )
