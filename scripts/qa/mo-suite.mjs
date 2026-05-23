/**
 * MO role scenario suite.
 *
 * Covers: registration → job creation → applicant review → match-insight AI.
 * Mutates {@code entities.jobId} so the TA and Admin suites can reference it.
 *
 * @param {object} ctx        - { page, baseUrl, screenshotsDir }
 * @param {string} repoRoot
 * @param {object} accounts   - { mo, ta, admin }
 * @param {object} entities   - shared mutable bag (jobId, etc.)
 * @returns {Promise<Array>}  - scenario result records
 */
import {
    gotoPath, waitForPath, login, logout, registerAccount,
    runAiFeedback, getQueryParam, recordScenario
} from "./suite-helpers.mjs";

export async function runMoSuite(ctx, repoRoot, accounts, entities) {
    const results = [];
    const rec = (name, shot, fn) => recordScenario(ctx, repoRoot, results, name, shot, fn);

    await rec("MO account can register and publish a new position.", null, async () => {
        await registerAccount(ctx, accounts.mo);
        await gotoPath(ctx, "/mo/jobs");

        await ctx.page.locator('input[name="title"]').fill(entities.jobTitle);
        await ctx.page.locator('input[name="module"]').fill("EBU6304");
        await ctx.page.locator('select[name="activityType"]').selectOption("Lab Support");
        await ctx.page.locator('input[name="deadline"]').fill("2099-12-31");
        await ctx.page.locator('input[name="quota"]').fill("2");
        await ctx.page.locator('input[name="workload"]').fill("4");
        await ctx.page.locator('textarea[name="requiredSkills"]').fill("Java, Communication, Teaching Support");
        await ctx.page
            .locator('textarea[name="description"]')
            .fill("Support weekly lab delivery, answer implementation questions, and help maintain marking consistency.");
        await ctx.page.getByRole("button", { name: "Create Position" }).click();
        await waitForPath(ctx, "/mo/jobs");
        await ctx.page.locator(".alert.success strong").filter({ hasText: "Position created." }).waitFor({ timeout: 15000 });

        const jobCard    = ctx.page.locator(".surface-card").filter({ hasText: entities.jobTitle }).first();
        const reviewLink = jobCard.getByRole("link", { name: "Review applicants" });
        await reviewLink.waitFor({ state: "visible", timeout: 15000 });
        const reviewHref = await reviewLink.getAttribute("href");
        if (!reviewHref) throw new Error("Unable to determine the created job review link.");
        entities.jobId = getQueryParam(reviewHref, "jobId", ctx.baseUrl);
        if (!entities.jobId) throw new Error("Unable to determine the created job ID.");
        await logout(ctx);
        return { details: `${entities.jobId} / ${entities.jobTitle}` };
    });

    await rec(
        "MO can review the applicant, load explainable match analysis, and save an accepted decision.",
        "mo-05-applicants.png",
        async () => {
            if (!entities.jobId) throw new Error("No jobId available — MO job creation must run first.");
            await login(ctx, accounts.mo.email, accounts.mo.password, accounts.mo.homePath);
            await gotoPath(ctx, `/mo/jobs/applicants?jobId=${entities.jobId}`);
            await ctx.page.locator(`text=${entities.jobTitle}`).first().waitFor({ state: "visible", timeout: 15000 });

            const applicantCard = ctx.page.locator(".applicant-card").filter({ hasText: accounts.ta.email }).first();
            await applicantCard.waitFor({ state: "visible", timeout: 15000 });

            const aiBox    = applicantCard.locator('[data-ai-feedback="match-insight"]').first();
            const aiState  = await runAiFeedback(ctx, aiBox, "Match analysis");

            await applicantCard.locator('select[name="decision"]').selectOption("accepted");
            await applicantCard.locator('input[name="note"]').fill("Strong structured fit for the required lab support tasks.");
            await applicantCard.locator('button[type="submit"]').filter({ hasText: "Save decision" }).click();
            await waitForPath(ctx, "/mo/jobs/applicants");
            await ctx.page.locator(".alert.success strong").filter({ hasText: "Application decision saved." }).waitFor({ timeout: 15000 });
            await ctx.page.locator(".applicant-card").filter({ hasText: accounts.ta.email }).locator(".status-chip.status-accepted").first().waitFor({ state: "visible", timeout: 15000 });
            await logout(ctx);
            return { details: `match mode=${aiState.mode}; notice=${aiState.notice}` };
        }
    );

    return results;
}
