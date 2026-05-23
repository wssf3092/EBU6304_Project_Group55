/**
 * Shared Playwright page helpers used by every per-role suite.
 * All helpers accept a `ctx` object: { page, baseUrl, screenshotsDir }.
 */
import path from "node:path";
import fs from "node:fs/promises";

export function toPosix(p) {
    return p.split(path.sep).join("/");
}

export async function gotoPath(ctx, relativeUrl) {
    await ctx.page.goto(new URL(relativeUrl, ctx.baseUrl).toString(), { waitUntil: "domcontentloaded" });
}

export async function waitForPath(ctx, expectedPath) {
    await ctx.page.waitForFunction(
        (p) => window.location.pathname.endsWith(p),
        expectedPath,
        { timeout: 15000 }
    );
}

export async function captureScreenshot(ctx, repoRoot, filename) {
    const filePath = path.join(ctx.screenshotsDir, filename);
    await ctx.page.screenshot({ path: filePath, fullPage: true });
    return toPosix(path.relative(repoRoot, filePath));
}

export async function registerAccount(ctx, account) {
    await gotoPath(ctx, "/auth/register");
    await ctx.page.locator('input[name="name"]').fill(account.name);
    await ctx.page.locator('input[name="email"]').fill(account.email);
    await ctx.page.locator('input[name="password"]').fill(account.password);
    await ctx.page.locator('select[name="role"]').selectOption(account.role);
    await ctx.page.getByRole("button", { name: "Create Account" }).click();
    await waitForPath(ctx, account.homePath);
    await ctx.page.locator(".topbar").waitFor({ state: "visible", timeout: 15000 });
}

export async function login(ctx, email, password, expectedPath) {
    await gotoPath(ctx, "/auth/login");
    await ctx.page.locator('input[name="email"]').fill(email);
    await ctx.page.locator('input[name="password"]').fill(password);
    await ctx.page.getByRole("button", { name: "Sign In" }).click();
    await waitForPath(ctx, expectedPath);
    await ctx.page.locator(".topbar").waitFor({ state: "visible", timeout: 15000 });
}

export async function logout(ctx) {
    await ctx.page.locator('form[action$="/auth/logout"] button').click();
    await waitForPath(ctx, "/auth/login");
}

export async function runAiFeedback(ctx, container, label) {
    const trigger = container.locator("[data-ai-trigger]");
    await trigger.waitFor({ state: "visible", timeout: 15000 });
    await trigger.click();
    const head = container.locator(".ai-content .ai-head, .ai-content .alert");
    await head.first().waitFor({ state: "visible", timeout: 15000 });
    const errorBox = container.locator(".ai-content .alert.error strong");
    if ((await errorBox.count()) > 0) {
        throw new Error(`${label} failed: ${(await errorBox.first().textContent())?.trim()}`);
    }
    const mode   = ((await container.locator(".ai-content .ai-head .status-chip").first().textContent()) || "").trim();
    const notice = ((await container.locator(".ai-content .cell-subtle").first().textContent())           || "").trim();
    return { mode, notice };
}

export function getQueryParam(href, key, baseUrl) {
    return new URL(href, baseUrl).searchParams.get(key);
}

export async function recordScenario(ctx, repoRoot, results, name, screenshotName, action) {
    try {
        const result = await action();
        const screenshot = screenshotName ? await captureScreenshot(ctx, repoRoot, screenshotName) : null;
        results.push({ name, status: "PASS", screenshot, details: result?.details || null });
        return result;
    } catch (error) {
        let screenshot = null;
        if (screenshotName) {
            try {
                screenshot = await captureScreenshot(ctx, repoRoot, screenshotName.replace(".png", "-failure.png"));
            } catch {
                screenshot = null;
            }
        }
        results.push({ name, status: "FAIL", screenshot, details: error.message });
        throw error;
    }
}
