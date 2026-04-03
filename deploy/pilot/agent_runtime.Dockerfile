ARG RUNTIME_BASE_IMAGE=docker.m.daocloud.io/library/python:3.12-slim
FROM ${RUNTIME_BASE_IMAGE}

ENV PYTHONDONTWRITEBYTECODE=1
ENV PYTHONUNBUFFERED=1

WORKDIR /app

COPY agent_runtime/requirements.txt /tmp/requirements.txt

RUN pip install --no-cache-dir -r /tmp/requirements.txt

COPY agent_runtime /app/agent_runtime

EXPOSE 8091

CMD ["python", "-m", "uvicorn", "agent_runtime.app:app", "--host", "0.0.0.0", "--port", "8091"]
