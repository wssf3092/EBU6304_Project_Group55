/**
 * Admin role scenario suite.
 *
 * Covers: registration → workload overview → AI workload-balance advice.
 *
 * @param {object} ctx        - { page, baseUrl, screenshotsDir }
 * @param {string} repoRoot
 * @param {object} accounts   - { mo, ta, admin }
 * @param {object} entities   - shared mutable bag
 * @returns {Promise<Array>}  - scenario result records
 */
import {
    gotoPath, logout, registerAccount,
    runAiFeedback, recordScenario
} from "./suite-helpers.mjs";

export async function runAdminSuite(ctx, repoRoot, accounts) {
    const results = [];
    const rec = (name, shot, fn) => recordScenario(ctx, repoRoot, results, name, shot, fn);

    await rec(
        "Admin can open workload overview and generate workload advice with structured fallback output.",
        "admin-06-workload.png",
        async () => {
            await registerAccount(ctx, accounts.admin);
            await gotoPath(ctx, "/admin/workload");
            const aiBox    = ctx.page.locator('[data-ai-feedback="workload-balance"]');
            const aiState  = await runAiFeedback(ctx, aiBox, "Workload advice");
            await ctx.page.locator(".status-chip.status-underload, .status-chip.status-balanced, .status-chip.status-overload").first().waitFor({
                state: "visible",
                timeout: 15000
            });
            await logout(ctx);
            return { details: `workload mode=${aiState.mode}; notice=${aiState.notice}` };
        }
    );

    return results;
}
