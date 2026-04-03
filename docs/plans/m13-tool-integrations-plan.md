# M13 Tool Integrations Plan

## Implementation Plan

M13 will extend the existing AICoOS Phase 1 modules with controlled external-tool actions instead of introducing a new integration platform. The backend will add a lightweight Linear/Figma client layer, project-level tool bindings, and auditable preview/apply records. The frontend will reuse current Project Center, Clarification Center, and Decision Center patterns so operators can preview and confirm external writes explicitly. Linear will be implemented first as a controlled issue/comment export path plus a project-level Linear representation link. Figma will be implemented as project-linked read/context linkage with previewed metadata fetch and saved context bindings. System config center will hold the minimal integration config keys, with password-style masking for API keys and clear documentation of current security limitations.
