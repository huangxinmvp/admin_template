from __future__ import annotations

from typing import Annotated

from fastapi import Header, HTTPException, status


def require_runtime_key(
    x_agent_runtime_key: Annotated[str | None, Header(alias="X-Agent-Runtime-Key")] = None,
) -> str:
    from agent_runtime.config import load_settings

    settings = load_settings()
    if settings.api_key and x_agent_runtime_key != settings.api_key:
        raise HTTPException(
            status_code=status.HTTP_401_UNAUTHORIZED,
            detail="invalid runtime key",
        )
    return x_agent_runtime_key or ""
