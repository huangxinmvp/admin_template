# M12 E2E Checklist

## Preflight

- [ ] `agent_runtime` is running on `127.0.0.1:8091`
- [ ] `curl http://127.0.0.1:8091/health` returns `status=ok`
- [ ] `defaultProviderReady=true`
- [ ] Spring Boot is running on `127.0.0.1:8081`
- [ ] Frontend dev server is running
- [ ] An admin user can log in
- [ ] At least one project exists in `项目中心`

## Runtime Health Notes

Use [runtime_smoke_test.sh](/Users/hx/it_company/agent_company/agent_runtime/scripts/runtime_smoke_test.sh) if you want a quick direct runtime check before opening the UI.

Health hints:

- `runtimeKeyConfigured=true`: runtime shared key is loaded
- `openaiCompatibleConfigured=true`: provider env is present
- `defaultProviderReady=true`: default provider can actually be used
- `mockFallbackEnabled=false`: real-provider verification is strict instead of silently falling back

## Flow 1: Requirement Intake -> Clarification Preview -> Apply

- [ ] Open `AICoOS -> 需求接收`
- [ ] Select a project with saved intake fields
- [ ] Click `生成澄清建议`
- [ ] A preview modal/drawer appears before any write happens
- [ ] Suggested rows include structured fields such as title, category, severity, blocker flag, and reason
- [ ] Select one or more suggestions and click apply
- [ ] Open `AICoOS -> 澄清中心`
- [ ] Newly applied clarification items are visible and linked to the same project

If this fails:

- check backend `agentRuntime.baseUrl`
- check shared key mismatch
- check that the project has intake content to send to the runtime

## Flow 2: Clarification -> Decision Suggestion -> Apply

- [ ] Open `AICoOS -> 澄清中心`
- [ ] Filter to a project with open clarification items
- [ ] Click `AI 分析转决策`
- [ ] A suggestion preview appears before any decision record is created
- [ ] Suggested rows include clarification linkage, suggested title, impact summary, recommended option, and blocker flag
- [ ] Apply one or more suggestions
- [ ] Open `AICoOS -> 决策中心`
- [ ] The applied records exist as `DecisionItem`

## Flow 3: Decision -> Budget Suggestion Preview -> Apply

- [ ] Open `AICoOS -> 决策中心`
- [ ] Open a decision detail drawer
- [ ] Click `预算影响建议`
- [ ] A preview appears before any write happens
- [ ] The suggestion includes budget impact summary, project impact summary, delta range, affected roles, and confidence
- [ ] Apply the suggestion
- [ ] Reopen the decision and confirm the summary fields were updated explicitly by the apply action

Important:

- M12 does not auto-recalculate budgets
- M12 only writes the selected budget-impact summary back into the governed main record

## Flow 4: Meeting Summary Preview -> Save

- [ ] Open `AICoOS -> 项目中心`
- [ ] Open a project detail drawer
- [ ] Click `会议总结`
- [ ] Paste meeting/discussion notes
- [ ] Generate summary
- [ ] A preview appears before anything is saved
- [ ] The preview includes summary, action items, open questions, and decision candidates
- [ ] Click save
- [ ] The new record appears in the project detail meeting-history section

## Project Governance Follow-Through

- [ ] Project Center still loads normally after M12 actions
- [ ] Applied clarification items affect clarification counts where expected
- [ ] Applied decision suggestions affect pending decision visibility where expected
- [ ] Recent meeting save appears in project recent activity or recent meeting records
- [ ] No action is persisted when the operator closes the preview without applying/saving

## Optional Real-Provider Verification

Run this only after mock mode passes cleanly.

- [ ] runtime started with `AGENT_RUNTIME_PROVIDER=openai_compatible`
- [ ] runtime started with `AGENT_RUNTIME_ALLOW_MOCK_FALLBACK=false`
- [ ] health returns `provider=openai_compatible`
- [ ] health returns `openaiCompatibleConfigured=true`
- [ ] backend config has `agentRuntime.mockMode=false`
- [ ] backend config has `agentRuntime.provider=openai_compatible`
- [ ] one preview flow completes without fallback symptoms

## Stop Conditions

Stop the demo and fix config before proceeding if any of the following occurs:

- runtime health returns `defaultProviderReady=false`
- preview requests return 401 or 500
- preview already writes data before explicit apply/save
- the UI cannot distinguish preview from confirmed write
