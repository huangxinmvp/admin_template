# Flowable Integration

## Version

- Spring Boot: `3.5.4`
- Flowable: `7.2.0`

## What Was Added

- `flowable-spring-boot-starter-process` 依赖
- Flowable MySQL schema 自举，首次启动自动补齐 `common / engine / history` 官方表
- 基础引擎配置
- 示例流程：`leaveApproval`
- 流程定义、部署、发起、待办、已办、任务办理、实例详情接口

## Auto Deployment

Flowable 会自动扫描 `classpath:/processes/` 目录。

当前已内置示例流程：

- `leaveApproval`：请假审批

## API Overview

- `GET /api/workflow/definitions`
- `POST /api/workflow/deployments`
- `DELETE /api/workflow/deployments/{deploymentId}`
- `POST /api/workflow/instances`
- `GET /api/workflow/instances/my-started`
- `GET /api/workflow/instances/{processInstanceId}`
- `GET /api/workflow/tasks/todo`
- `GET /api/workflow/tasks/done`
- `POST /api/workflow/tasks/{taskId}/complete`

## Quick Start

### 1. 启动服务

确保数据库可用，然后启动 Spring Boot。

如果当前库里还没有 Flowable 表，应用会在引擎初始化前自动执行官方 MySQL 建表脚本，补齐：

- `ACT_GE_*` common 表
- `ACT_RE_* / ACT_RU_*` engine 表
- `ACT_HI_*` history 表

### 2. 查看流程定义

```bash
curl -H "Authorization: Bearer <token>" \
  http://127.0.0.1:8081/api/workflow/definitions
```

### 3. 发起示例流程

```bash
curl -X POST http://127.0.0.1:8081/api/workflow/instances \
  -H "Authorization: Bearer <token>" \
  -H "Content-Type: application/json" \
  -d '{
    "definitionKey": "leaveApproval",
    "businessKey": "LEAVE-001",
    "title": "张三请假 2 天",
    "approver": "admin",
    "variables": {
      "days": 2,
      "reason": "病假"
    }
  }'
```

### 4. 查询待办

```bash
curl -H "Authorization: Bearer <token>" \
  http://127.0.0.1:8081/api/workflow/tasks/todo
```

### 5. 办理任务

```bash
curl -X POST http://127.0.0.1:8081/api/workflow/tasks/<taskId>/complete \
  -H "Authorization: Bearer <token>" \
  -H "Content-Type: application/json" \
  -d '{
    "approved": true,
    "comment": "同意"
  }'
```
