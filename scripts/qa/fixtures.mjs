/**
 * Shared QA fixtures for Playwright suites.
 *
 * Step 1 of the role-based test split: account factory + canonical role list extracted out of
 * the monolithic qa-e2e.mjs so future per-role suites (ta/, mo/, admin/) can import the same
 * seed without duplicating credentials or home-path conventions.
 *
 * Passwords satisfy the new PasswordPolicy (≥8 chars, letter + digit, no whitespace).
 */

export const ROLES = ["mo", "ta", "admin"];

export const HOME_PATH = {
    mo:    "/mo/dashboard",
    ta:    "/ta/dashboard",
    admin: "/admin/workload"
};

const ROLE_NAME = {
    mo:    "Morgan",
    ta:    "Taylor",
    admin: "Alex"
};

const ROLE_TOKEN = {
    mo:    "MO",
    ta:    "TA",
    admin: "ADMIN"
};

/**
 * Builds a fresh account triple keyed by role name. Email is namespaced with {@code timestamp}
 * so parallel runs (or repeated runs against persisted data) never collide.
 *
 * @param {string} timestamp - compact run id, e.g. "20260518T223301"
 * @returns {{mo: object, ta: object, admin: object}}
 */
export function buildAccounts(timestamp) {
    const accounts = {};
    for (const role of ROLES) {
        accounts[role] = {
            name:     `${ROLE_NAME[role]} QA ${timestamp}`,
            email:    `qa-${role}-${timestamp}@example.com`,
            password: "Pass1234",
            role:     ROLE_TOKEN[role],
            homePath: HOME_PATH[role]
        };
    }
    return accounts;
}

/** Default canonical run id used when callers do not supply one. */
export function defaultTimestamp(now = new Date()) {
    return now.toISOString().replace(/[-:.TZ]/g, "").slice(0, 14);
}
