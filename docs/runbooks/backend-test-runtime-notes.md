# Backend Test Runtime Notes

## Purpose

This note explains the smallest local runtime fix added for backend Mockito-based service tests.

The goal is only to make the current collaboration/governance backend tests runnable under local Maven/Surefire on the current JDK environment. It does not change product behavior or test logic.

## Local Failure Mode

Some backend service tests use Mockito's inline mock maker. On the current local environment:

- JDK: Oracle JDK 21
- local platform: macOS
- test runner: Maven Surefire

Mockito fails while trying to self-attach a Java agent at runtime, which causes errors like:

- `Could not initialize inline Byte Buddy mock maker`
- `Could not self-attach to current VM using external process`

This means the test runtime fails before the actual service assertions run.

## Minimal Fix Applied

The local fix is now configured in [pom.xml](/Users/hx/it_company/agent_company/web_backend/pom.xml):

- Maven Surefire starts test JVMs with Mockito's premain agent
- the agent jar is resolved from the local Maven repository using `${settings.localRepository}`
- existing CLI-provided `argLine` values are still preserved

In practice, Maven now launches tests with a JVM option shaped like:

```text
-javaagent:~/.m2/repository/org/mockito/mockito-core/<version>/mockito-core-<version>.jar
```

This avoids the failing self-attach path and keeps the change scoped to test runtime only.

## Recommended Local Commands

From [web_backend](/Users/hx/it_company/agent_company/web_backend):

Run the affected governance/collaboration service tests:

```bash
./mvnw -q test -Dtest=AgentRuntimeSuggestionServiceImplTest,ProjectNextStepGuidanceResolverTest,ProjectServiceImplTest
```

Run a compile-only verification:

```bash
./mvnw -q -DskipTests compile
```

If you want the broader backend test suite:

```bash
./mvnw -q test
```

## What To Expect

- The Mockito inline mock-maker bootstrap issue should no longer block Maven test startup.
- Relevant service tests should now run under Maven if there are no separate assertion or fixture issues.
- This fix is local-Maven focused and intentionally minimal.
- The current governance-focused local set is expected to pass with:
  `AgentRuntimeSuggestionServiceImplTest`, `ProjectNextStepGuidanceResolverTest`, and `ProjectServiceImplTest`.

## IDE Notes

If you run tests directly from the IDE and the IDE does not delegate test execution to Maven, the same self-attach problem may still appear.

If that happens, use one of these approaches:

1. delegate test execution to Maven in the IDE
2. add the same Mockito `-javaagent` VM option to the IDE test run configuration

This repository does not currently include IDE-specific run configurations for that.

## Scope Boundaries

This change does not:

- alter product code
- change test assertions
- add new test dependencies
- solve unrelated backend test failures caused by stale fixtures, auth API drift, or environment data issues

## Current Limitation

The fix relies on the local Maven repository containing the Mockito jar version managed by Spring Boot. If the local repository is incomplete, Maven will still need to resolve dependencies first.
