# QA fixtures (Playwright)

Shared building blocks for the browser regression suite.

## Files

| File | Purpose |
|---|---|
| `fixtures.mjs` | Account factory (`buildAccounts`), canonical role list, home-path map, run-id helper |

## Why this folder exists

Round 1 of the role-based test split: we extract account seeding here so subsequent rounds can
add per-role suites under `scripts/qa/ta/`, `scripts/qa/mo/`, `scripts/qa/admin/` without
copy-pasting credential conventions or password-policy-compliant defaults.

## Conventions

- All emails are namespaced with the run timestamp (`qa-<role>-<ts>@example.com`) so parallel
  runs cannot collide on persisted data.
- Default password `Pass1234` is the canonical fixture and satisfies `PasswordPolicy` (≥8 chars,
  letter + digit, no whitespace). Update in one place if the policy ever changes.
- Role tokens (`MO` / `TA` / `ADMIN`) match the values rendered by the registration `<select>`.
