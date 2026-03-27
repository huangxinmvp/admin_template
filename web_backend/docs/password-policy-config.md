# 密码策略热更新与多环境配置说明

## 当前实现结论

当前密码策略通过 `app.security.password-policy` 配置项读取，配置类见：

- `src/main/java/com/hiking/treasure/config/PasswordPolicyConfig.java`
- `src/main/java/com/hiking/treasure/common/util/PasswordPolicyValidator.java`

默认情况下：

- 应用启动时加载配置
- 运行中修改 `application.yml` 或环境变量后，不会自动热更新到正在运行的进程
- 变更密码策略后，需要重启应用才能生效

这意味着目前是“启动时生效配置”，不是“运行时动态热更新配置”。

## 为什么当前不支持热更新

项目当前使用的是 Spring Boot 原生 `@ConfigurationProperties`，但没有接入：

- Spring Cloud Config / Nacos / Apollo 等配置中心
- `@RefreshScope`
- Actuator `/refresh` 或消息总线刷新机制

因此生产环境里修改密码策略时，推荐走“更新配置 -> 重启应用”的发布流程。

## 多环境配置建议

### 方案 1：环境变量覆盖

当前 `application.yml` 已支持以下环境变量：

- `APP_PASSWORD_MIN_LENGTH`
- `APP_PASSWORD_MAX_LENGTH`
- `APP_PASSWORD_REQUIRE_UPPERCASE`
- `APP_PASSWORD_REQUIRE_LOWERCASE`
- `APP_PASSWORD_REQUIRE_DIGIT`
- `APP_PASSWORD_REQUIRE_SPECIAL`

适合：

- Docker / Kubernetes
- CI/CD 注入变量
- 不想维护多份 yml 文件的环境

示例：

```bash
APP_PASSWORD_MIN_LENGTH=10
APP_PASSWORD_MAX_LENGTH=24
APP_PASSWORD_REQUIRE_SPECIAL=true
```

### 方案 2：按环境拆分配置文件

可以增加：

- `application-dev.yml`
- `application-test.yml`
- `application-prod.yml`

然后在不同环境启用不同 profile。

示例：

```yml
# application-dev.yml
app:
  security:
    password-policy:
      min-length: 8
      max-length: 32
      require-uppercase: true
      require-lowercase: true
      require-digit: true
      require-special: false
```

```yml
# application-prod.yml
app:
  security:
    password-policy:
      min-length: 10
      max-length: 20
      require-uppercase: true
      require-lowercase: true
      require-digit: true
      require-special: true
```

适合：

- 明确区分开发、测试、生产密码规则
- 团队希望把环境差异纳入配置文件管理

## 推荐实践

开发环境建议：

- 长度要求不要过高
- 可以按团队习惯决定是否必须特殊字符
- 目标是减少本地调试阻力

测试/预发环境建议：

- 尽量与生产一致
- 至少保证长度、大小写、数字规则相同

生产环境建议：

- 长度建议 `10-20`
- 开启大写、小写、数字、特殊字符全部要求
- 通过环境变量或配置中心集中管理

## 如果后续要支持真正热更新

可以考虑以下演进路径：

1. 引入统一配置中心
2. 将密码策略配置接入动态刷新
3. 增加配置变更审计
4. 在管理后台提供“当前生效密码策略”只读接口

在没有配置中心前，不建议承诺“运行中即时生效”。
