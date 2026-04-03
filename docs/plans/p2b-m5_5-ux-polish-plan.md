# P2B-M5.5 Governance Action Chain UX Polish Plan

## Summary

Keep P2B-M5 scope unchanged and focus only on light UX verification support plus small readability polish for the Project Center governance action chain.

## Focus

### 1. Detail drawer readability

- make the top-level recommendation and the primary action feel clearly related
- reduce the sense of repeated wording between the summary reason and the primary-action explanation
- keep the chain readable within a few seconds for an operator

### 2. Information density and ordering

- preserve the current blocker-first structure
- slightly tighten the card labels and section order if it helps scanning
- keep follow-up actions informative but lighter than the main action

### 3. Wording polish

- improve clarity of:
  - recommended reason
  - addressed risk
  - expected change
  - follow-up action framing
- stay Chinese-first and operator-facing

### 4. Verification support

- update the browser regression checklist for the new action-chain layer
- add explicit checks for:
  - top-level next-step vs primary-action consistency
  - primary-action clarity
  - follow-up action readability
  - copy density and ambiguity

## Expected Changes

- small copy/order refinements in `ProjectCenterDetailDrawer`
- no backend logic changes unless a tiny display-support tweak is strictly needed
- no data-model changes
- no new pages
- no new behavior
