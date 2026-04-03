# M13 Tool Integration Checklist

## Operator Expectations
- All external-tool actions are operator initiated
- All external writes must go through `preview -> confirm`
- AICoOS remains the source of truth
- Every preview/apply action should be visible in project audit context

## Browser Walkthrough Checklist

### Project Center
- Open one project detail drawer
- Verify `Linear 映射` and `Figma 上下文` buttons are visible in the header
- Verify `外部工具上下文` card exists
- Verify `最近集成动作` card exists

### Linear project mapping
- Open `Linear 映射`
- Confirm the modal explains create vs link behavior
- Confirm preview is required before apply
- Confirm a failed preview/apply shows one actionable error message

### Clarification export
- Open one clarification row `Linear`
- Confirm `Issue` and `Comment` modes are visible
- Confirm the modal explains that Comment mode depends on project mapping
- Confirm preview body is readable before apply

### Decision export
- Open one decision drawer
- Confirm `Linear 导出` is visible
- Confirm preview/apply flow matches clarification export

### Figma context
- Open `Figma 上下文`
- Confirm the modal explains that current behavior is read/link only
- Confirm file/node metadata is visible in preview
- Confirm wrong node/auth errors are actionable

## Audit Checklist
After each successful apply:
- one success audit is visible in project detail
- the audit contains tool type and action type
- the audit shows external link when the provider returns one
- recent activity shows an integration item

## Current Known Limitations
- No bidirectional sync
- No background polling
- No autonomous external writes
- No notification or workflow auto-advancement
- Secret masking exists, but current config storage is not a production-grade secret manager

For the broader M15 browser walkthrough, also use:

- [local-demo-runbook.md](/Users/hx/it_company/agent_company/docs/runbooks/local-demo-runbook.md)
- [m15-operator-verification-checklist.md](/Users/hx/it_company/agent_company/docs/runbooks/m15-operator-verification-checklist.md)
