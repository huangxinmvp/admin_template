# M11 Governance Linkage Plan

M11 will add a lightweight `ProjectGovernanceState` snapshot layer and a small recomputation service that derives project-level governance signals from existing clarification, decision, approval, budget, gate, workflow-template, and agent-role records. Relevant write paths will call explicit recompute hooks after successful changes, while Project Center and the dashboard will read the shared project governance snapshot so blocked, at-risk, and bottleneck signals stay consistent without introducing a heavy event bus or workflow engine.
