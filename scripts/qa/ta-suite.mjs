/**
 * TA role scenario suite.
 *
 * Covers: login page smoke → registration → profile + job browse + skills-gap AI →
 *         application submission → invalid-login guard.
 *
 * Reads {@code entities.jobId} set by the MO suite.
 *
 * @param {object} ctx        - { page, baseUrl, screenshotsDir }
 * @param {string} repoRoot
 * @param {object} accounts   - { mo, ta, admin }
 * @param {object} entities   - shared mutable bag
 * @returns {Promise<Array>}  - scenario result records
 */
import {
    gotoPath, waitForPath, logout, registerAccount,
    runAiFeedback, recordScenario
} from "./suite-helpers.mjs";

export async function runTaSuite(ctx, repoRoot, accounts, entities) {
    const results = [];
    const rec = (name, shot, fn) => recordScenario(ctx, repoRoot, results, name, shot, fn);

    await rec("Login page loads and renders the auth shell.", "ta-01-login.png", async () => {
        await gotoPath(ctx, "/auth/login");
        await ctx.page.locator(".auth-card").waitFor({ state: "visible", timeout: 15000 });
    });

    await rec("TA account can register and view the dashboard.", "ta-02-dashboard.png", async () => {
        await registerAccount(ctx, accounts.ta);
        await ctx.page.locator(".panel h2").filter({ hasText: "Recommended Jobs" }).waitFor({ state: "visible", timeout: 15000 });
    });

    await rec(
        "TA profile can be completed, a job can be browsed, and structured skills-gap analysis loads.",
        "ta-03-job-detail.png",
        async () => {
            await gotoPath(ctx, "/ta/profile");
            await ctx.page.locator('input[name="studentId"]').fill("2026213001");
            await ctx.page.locator('input[name="contactEmail"]').fill(accounts.ta.email);
            await ctx.page.locator('input[name="major"]').fill("Software Engineering");
            await ctx.page.locator('input[name="year"]').fill("3");
            await ctx.page.locator('textarea[name="skills"]').fill("Java, Communication, Teaching Support");
            await ctx.page
                .locator('textarea[name="bio"]')
                .fill("Hands-on Java experience with peer support, lab preparation, and consistent student-facing communication.");
            await ctx.page.locator('input[name="maxHours"]').fill("10");
            await ctx.page.getByRole("button", { name: "Save Changes" }).click();
            await waitForPath(ctx, "/ta/profile");
            await ctx.page.locator('input[name="studentId"]').waitFor({ state: "visible", timeout: 15000 });

            if (!entities.jobId) throw new Error("No jobId available — MO suite must run before TA suite.");
            await gotoPath(ctx, `/ta/jobs?jobId=${entities.jobId}`);
            await ctx.page.locator(".detail-panel h2").filter({ hasText: entities.jobTitle }).waitFor({ state: "visible", timeout: 15000 });
            await ctx.page.locator('button[type="submit"]').filter({ hasText: "Submit Application" }).waitFor({ state: "visible", timeout: 15000 });

            const aiBox    = ctx.page.locator('[data-ai-feedback="skills-gap"]');
            const aiState  = await runAiFeedback(ctx, aiBox, "Skills gap analysis");
            await aiBox.scrollIntoViewIfNeeded();
            return { details: `skills-gap mode=${aiState.mode}; notice=${aiState.notice}` };
        }
    );

    await rec("TA can submit an application and view the updated tracker.", "ta-04-applications.png", async () => {
        await ctx.page.locator('textarea[name="coverLetter"]').fill("I can explain coding tasks clearly and support dependable weekly lab delivery.");
        await ctx.page.locator('button[type="submit"]').filter({ hasText: "Submit Application" }).click();
        await waitForPath(ctx, "/ta/applications");
        await ctx.page.locator(".alert.success strong").filter({ hasText: "Application submitted." }).waitFor({ timeout: 15000 });
        await ctx.page.locator(".status-chip.status-pending").first().waitFor({ state: "visible", timeout: 15000 });
    });
    await logout(ctx);

    await rec("Invalid login shows a clear error state.", "ta-07-login-error.png", async () => {
        await gotoPath(ctx, "/auth/login");
        await ctx.page.locator('input[name="email"]').fill(accounts.ta.email);
        await ctx.page.locator('input[name="password"]').fill("wrong-pass");
        await ctx.page.getByRole("button", { name: "Sign In" }).click();
        await ctx.page.locator(".alert.error strong").filter({ hasText: "Invalid email or password." }).waitFor({ timeout: 15000 });
    });

    return results;
}
