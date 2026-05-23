/**
 * Per-role E2E orchestrator.
 *
 * Runs the MO suite first (creates the job), then the TA suite (applies), then the MO suite
 * review step, and finally the Admin suite — mirroring the complete recruitment workflow while
 * keeping each role's behaviour in its own module.
 *
 * Usage:
 *   node scripts/qa/index.mjs
 *
 * Environment variables (same as qa-e2e.mjs):
 *   QA_BASE_URL           default http://127.0.0.1:18080
 *   QA_DATA_ROOT          default artifacts/qa/run-data
 *   QA_SKIP_PERSISTENCE   set to "1" to skip JSON-file assertions
 */
import fs from "node:fs/promises";
import path from "node:path";
import { chromium } from "playwright";
import { buildAccounts, defaultTimestamp } from "./fixtures.mjs";
import { runMoSuite }    from "./mo-suite.mjs";
import { runTaSuite }    from "./ta-suite.mjs";
import { runAdminSuite } from "./admin-suite.mjs";
import { toPosix }       from "./suite-helpers.mjs";

const repoRoot       = process.cwd();
const artifactRoot   = path.join(repoRoot, "artifacts", "qa");
const screenshotsDir = path.join(artifactRoot, "screenshots");
const resultPath     = path.join(artifactRoot, "e2e-per-role-results.json");
const baseUrl        = process.env.QA_BASE_URL    || "http://127.0.0.1:18080";
const dataRoot       = process.env.QA_DATA_ROOT   || path.join(artifactRoot, "run-data");
const skipPersistence = process.env.QA_SKIP_PERSISTENCE === "1";

const timestamp = defaultTimestamp();
const accounts  = buildAccounts(timestamp);

const summary = {
    feature:     "TA Recruitment System — per-role browser regression",
    startedAt:   new Date().toISOString(),
    finishedAt:  null,
    baseUrl,
    dataRoot,
    browser:     null,
    suites:      { mo: [], ta: [], admin: [] },
    persistenceChecks: [],
    entities: {
        moUserId:      null,
        taUserId:      null,
        adminUserId:   null,
        jobId:         null,
        applicationId: null,
        jobTitle:      `Advanced Lab Support ${timestamp}`
    }
};

async function launchBrowser() {
    for (const attempt of [
        { label: "msedge",   options: { channel: "msedge", headless: true } },
        { label: "chromium", options: { headless: true } }
    ]) {
        try {
            return { browser: await chromium.launch(attempt.options), label: attempt.label };
        } catch {
        }
    }
    throw new Error("Unable to launch any Playwright browser.");
}

async function findJsonRecord(directory, predicate) {
    try {
        const entries = await fs.readdir(directory, { withFileTypes: true });
        for (const entry of entries) {
            if (!entry.isFile() || !entry.name.endsWith(".json")) continue;
            const filePath = path.join(directory, entry.name);
            const payload  = JSON.parse(await fs.readFile(filePath, "utf8"));
            if (predicate(payload)) return { filePath, payload };
        }
    } catch {
    }
    return null;
}

function addPersistenceCheck(name, passed, successDetail, failureDetail) {
    summary.persistenceChecks.push({
        name,
        status: passed ? "PASS" : "FAIL",
        detail: passed ? successDetail : failureDetail
    });
    if (!passed) throw new Error(failureDetail);
}

let browser, context, page;

async function main() {
    await fs.rm(screenshotsDir, { recursive: true, force: true });
    await fs.mkdir(screenshotsDir, { recursive: true });
    await fs.mkdir(artifactRoot, { recursive: true });

    const launched = await launchBrowser();
    browser = launched.browser;
    summary.browser = launched.label;

    context = await browser.newContext({ viewport: { width: 1440, height: 1200 } });
    page    = await context.newPage();

    const ctx = { page, baseUrl, screenshotsDir };

    summary.suites.mo = await runMoSuite(ctx, repoRoot, accounts, summary.entities);

    summary.suites.ta = await runTaSuite(ctx, repoRoot, accounts, summary.entities);

    summary.suites.admin = await runAdminSuite(ctx, repoRoot, accounts, summary.entities);

    if (skipPersistence) return;

    const moRecord = await findJsonRecord(path.join(dataRoot, "users", "mos"), (r) => r.email === accounts.mo.email);
    addPersistenceCheck("MO account persisted.", !!moRecord, moRecord ? toPosix(moRecord.filePath) : null, "MO user JSON record was not created.");

    const taRecord = await findJsonRecord(path.join(dataRoot, "users", "tas"), (r) => r.email === accounts.ta.email);
    addPersistenceCheck("TA account persisted.", !!taRecord, taRecord ? toPosix(taRecord.filePath) : null, "TA user JSON record was not created.");

    const adminRecord = await findJsonRecord(path.join(dataRoot, "users", "admins"), (r) => r.email === accounts.admin.email);
    addPersistenceCheck("Admin account persisted.", !!adminRecord, adminRecord ? toPosix(adminRecord.filePath) : null, "Admin user JSON record was not created.");

    summary.entities.moUserId    = moRecord?.payload?.userId    || null;
    summary.entities.taUserId    = taRecord?.payload?.userId    || null;
    summary.entities.adminUserId = adminRecord?.payload?.userId || null;

    const jobRecord = await findJsonRecord(path.join(dataRoot, "jobs"), (r) => r.title === summary.entities.jobTitle);
    addPersistenceCheck("Job posting persisted.", !!jobRecord, jobRecord ? toPosix(jobRecord.filePath) : null, "Job JSON record was not created.");

    const applicationRecord = await findJsonRecord(
        path.join(dataRoot, "applications"),
        (r) => r.applicantId === taRecord?.payload?.userId && r.jobId === jobRecord?.payload?.jobId && r.status === "accepted"
    );
    addPersistenceCheck(
        "Application persisted with status accepted.",
        !!applicationRecord,
        applicationRecord ? toPosix(applicationRecord.filePath) : null,
        "Application record was not found or status was not accepted."
    );
    summary.entities.applicationId = applicationRecord?.payload?.applicationId || null;
}

try {
    await main();
    summary.finishedAt = new Date().toISOString();
    await fs.writeFile(resultPath, JSON.stringify(summary, null, 2), "utf8");
    console.log("Per-role E2E run complete. Results →", toPosix(path.relative(repoRoot, resultPath)));
} catch (error) {
    summary.error      = error.message;
    summary.finishedAt = new Date().toISOString();
    await fs.writeFile(resultPath, JSON.stringify(summary, null, 2), "utf8").catch(() => {});
    console.error(error);
    process.exitCode = 1;
} finally {
    await context?.close().catch(() => {});
    await browser?.close().catch(() => {});
}
