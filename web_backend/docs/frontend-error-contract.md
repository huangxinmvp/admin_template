# 前端错误码与密码策略约定

前端如果需要直接复用错误码常量，可直接引用：

- [frontend-error-keys.ts](/Users/hx/photoquest/backend/springboot-api/photo-quest/docs/frontend-error-keys.ts)

该文件由后端导出器生成，来源于 `ErrorCode` 枚举，不建议手工维护。

## 通用返回结构

后端错误响应统一包含以下字段：

```json
{
  "success": false,
  "code": 400,
  "errorKey": "PASSWORD_TOO_WEAK",
  "message": "密码需为8-32位，且同时包含大写字母、小写字母、数字和特殊字符",
  "result": null,
  "timestamp": 1710000000000
}
```

- `code`：HTTP 语义对应的业务状态码
- `errorKey`：前端分支判断的稳定枚举
- `message`：直接展示给用户的文案
- `result`：删除冲突等场景下的结构化数据

## 删除冲突

删除菜单、角色、部门、租户时，如果存在占用关系：

- `errorKey`：通常为 `BAD_REQUEST`
- `result.entityType`：冲突主体，例如 `permission`、`role`、`depart`、`tenant`
- `result.details[].code`：冲突类型，例如 `assigned_role`、`assigned_user`
- `result.details[].items[]`：主冲突项

```json
{
  "success": false,
  "code": 400,
  "errorKey": "BAD_REQUEST",
  "message": "菜单删除失败，以下菜单仍被角色授权: 租户管理(平台管理员)",
  "result": {
    "entityType": "permission",
    "details": [
      {
        "code": "assigned_role",
        "label": "角色授权",
        "items": [
          {
            "id": "p1",
            "name": "租户管理(平台管理员)",
            "type": "permission",
            "meta": {
              "title": "关联对象",
              "summary": "共 1 个角色",
              "relatedType": "role",
              "relatedItems": [
                {
                  "id": "r1",
                  "name": "平台管理员",
                  "type": "role"
                }
              ]
            }
          }
        ]
      }
    ]
  }
}
```

前端建议：

- 优先用 `errorKey` 判断是否进入统一错误态
- 删除冲突弹窗直接读取 `result.details`
- `meta.title` 用作分组标题
- `meta.summary` 用作摘要说明
- `meta.relatedItems`、`meta.from`、`meta.to` 用于展开明细

## 认证失败

常见 `errorKey` 如下：

| errorKey | 说明 | 建议前端行为 |
|---|---|---|
| `UNAUTHORIZED` | 未登录或 token 无效 | 跳登录页 |
| `FORBIDDEN` | 无权限访问 | 显示 403 或无权限提示 |
| `INVALID_CREDENTIALS` | 用户名或密码错误 | 表单错误提示 |
| `INVALID_REFRESH_TOKEN` | 刷新令牌非法 | 清本地登录态并跳登录 |
| `TOKEN_RELOGIN_REQUIRED` | 密码变更或凭证失效 | 清本地登录态并提示重新登录 |
| `USER_LOCKED` | 用户被锁定 | 表单错误提示 |
| `USER_DISABLED` | 用户被禁用或不存在 | 表单错误提示 |
| `TENANT_DISABLED` | 租户停用 | 清登录态并提示联系管理员 |
| `TENANT_EXPIRED` | 租户过期 | 清登录态并提示续费/联系管理员 |
| `PASSWORD_TOO_WEAK` | 密码不符合策略 | 表单错误提示 |

## 改密码与重置密码

以下接口成功时统一返回：

- `PUT /api/auth/password`
- `PUT /api/user/{id}/password`

```json
{
  "success": true,
  "code": 200,
  "result": {
    "reloginRequired": true,
    "message": "密码已修改，请重新登录"
  }
}
```

前端建议：

- 当 `result.reloginRequired === true` 时，提示用户重新登录
- 如果是当前用户自助改密，清理本地 token 并回到登录页
- 如果是管理员重置他人密码，只提示“目标用户需重新登录”

## 密码策略配置

默认配置在 `application.yml`：

```yml
app:
  security:
    password-policy:
      min-length: 8
      max-length: 32
      require-uppercase: true
      require-lowercase: true
      require-digit: true
      require-special: true
```

对应环境变量：

- `APP_PASSWORD_MIN_LENGTH`
- `APP_PASSWORD_MAX_LENGTH`
- `APP_PASSWORD_REQUIRE_UPPERCASE`
- `APP_PASSWORD_REQUIRE_LOWERCASE`
- `APP_PASSWORD_REQUIRE_DIGIT`
- `APP_PASSWORD_REQUIRE_SPECIAL`

更多关于热更新与多环境差异配置的说明，见：

- [password-policy-config.md](/Users/hx/photoquest/backend/springboot-api/photo-quest/docs/password-policy-config.md)
