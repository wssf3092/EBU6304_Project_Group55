import fs from "node:fs/promises";
import path from "node:path";
import { chromium } from "playwright";

const repoRoot = process.cwd();
const artifactRoot = path.join(repoRoot, "artifacts", "qa");
const screenshotsDir = path.join(artifactRoot, "screenshots");
const resultPath = path.join(artifactRoot, "e2e-results.json");
const baseUrl = process.env.QA_BASE_URL || "http://127.0.0.1:18080";
const dataRoot = process.env.QA_DATA_ROOT || path.join(artifactRoot, "run-data");
const skipPersistence = process.env.QA_SKIP_PERSISTENCE === "1";

const timestamp = new Date().toISOString().replace(/[-:.TZ]/g, "").slice(0, 14);
const jobTitle = `Advanced Lab Support ${timestamp}`;

const summary = {
  feature: "TA Recruitment System browser regression",
  startedAt: new Date().toISOString(),
  finishedAt: null,
  baseUrl,
  dataRoot,
  browser: null,
  scenarios: [],
  persistenceChecks: [],
  accounts: {},
  entities: {
    moUserId: null,
    taUserId: null,
    adminUserId: null,
    jobId: null,
    applicationId: null,
    jobTitle
  }
};

const accounts = {
  mo: {
    name: `Morgan QA ${timestamp}`,
    email: `qa-mo-${timestamp}@example.com`,
    password: "Pass1234",
    role: "MO",
    homePath: "/mo/dashboard"
  },
  ta: {
    name: `Taylor QA ${timestamp}`,
    email: `qa-ta-${timestamp}@example.com`,
    password: "Pass1234",
    role: "TA",
    homePath: "/ta/dashboard"
  },
  admin: {
    name: `Alex QA ${timestamp}`,
    email: `qa-admin-${timestamp}@example.com`,
    password: "Pass1234",
    role: "ADMIN",
    homePath: "/admin/workload"
  }
};

let browser;
let context;
let page;

function toPosix(inputPath) {
  return inputPath.split(path.sep).join("/");
}

function relativePath(inputPath) {
  return toPosix(path.relative(repoRoot, inputPath));
}

async function ensureDirectories() {
  await fs.rm(screenshotsDir, { recursive: true, force: true });
  await fs.mkdir(screenshotsDir, { recursive: true });
}

async function writeSummary() {
  summary.finishedAt = new Date().toISOString();
  await fs.writeFile(resultPath, JSON.stringify(summary, null, 2), "utf8");
}

function invariant(condition, message) {
  if (!condition) {
    throw new Error(message);
  }
}

function getQueryParam(href, key) {
  const url = new URL(href, baseUrl);
  return url.searchParams.get(key);
}

async function launchBrowser() {
  const attempts = [
    { label: "msedge", options: { channel: "msedge", headless: true } },
    { label: "chromium", options: { headless: true } }
  ];

  let lastError;
  for (const attempt of attempts) {
    try {
      const instance = await chromium.launch(attempt.options);
      return { browser: instance, label: attempt.label };
    } catch (error) {
      lastError = error;
    }
  }
  throw new Error(`Unable to launch Playwright browser: ${lastError?.message || lastError}`);
}

async function captureScreenshot(filename) {
  const filePath = path.join(screenshotsDir, filename);
  await page.screenshot({ path: filePath, fullPage: true });
  return relativePath(filePath);
}

async function recordScenario(name, screenshotName, action) {
  try {
    const result = await action();
    const screenshot = screenshotName ? await captureScreenshot(screenshotName) : null;
    summary.scenarios.push({
      name,
      status: "PASS",
      screenshot,
      details: result?.details || null
    });
    return result;
  } catch (error) {
    let screenshot = null;
    if (page && screenshotName) {
      try {
        const failedName = screenshotName.replace(".png", "-failure.png");
        screenshot = await captureScreenshot(failedName);
      } catch {
        screenshot = null;
      }
    }
    summary.scenarios.push({
      name,
      status: "FAIL",
      screenshot,
      details: error.message
    });
    throw error;
  }
}

async function gotoPath(relativeUrl) {
  await page.goto(new URL(relativeUrl, baseUrl).toString(), { waitUntil: "domcontentloaded" });
}

async function waitForPath(expectedPath) {
  await page.waitForFunction(
    (pathName) => window.location.pathname.endsWith(pathName),
    expectedPath,
    { timeout: 15000 }
  );
}

async function registerAccount(account) {
  await gotoPath("/auth/register");
  await page.locator('input[name="name"]').fill(account.name);
  await page.locator('input[name="email"]').fill(account.email);
  await page.locator('input[name="password"]').fill(account.password);
  await page.locator('select[name="role"]').selectOption(account.role);
  await page.getByRole("button", { name: "Create Account" }).click();
  await waitForPath(account.homePath);
  await page.locator(".topbar").waitFor({ state: "visible", timeout: 15000 });
}

async function login(email, password, expectedPath) {
  await gotoPath("/auth/login");
  await page.locator('input[name="email"]').fill(email);
  await page.locator('input[name="password"]').fill(password);
  await page.getByRole("button", { name: "Sign In" }).click();
  await waitForPath(expectedPath);
  await page.locator(".topbar").waitFor({ state: "visible", timeout: 15000 });
}

async function logout() {
  await page.locator('form[action$="/auth/logout"] button').click();
  await waitForPath("/auth/login");
}

async function waitForAiBox(container, label) {
  const head = container.locator(".ai-content .ai-head, .ai-content .alert");
  await head.first().waitFor({ state: "visible", timeout: 15000 });
  const errorBox = container.locator(".ai-content .alert.error strong");
  if ((await errorBox.count()) > 0) {
    throw new Error(`${label} failed: ${(await errorBox.first().textContent())?.trim()}`);
  }
  const mode = ((await container.locator(".ai-content .ai-head .status-chip").first().textContent()) || "").trim();
  const notice = ((await container.locator(".ai-content .cell-subtle").first().textContent()) || "").trim();
  const summaryText = ((await container.locator(".ai-content p").first().textContent()) || "").trim();
  return {
    mode,
    notice,
    summary: summaryText
  };
}

async function runAiFeedback(container, label) {
  const trigger = container.locator("[data-ai-trigger]");
  await trigger.waitFor({ state: "visible", timeout: 15000 });
  await trigger.click();
  return waitForAiBox(container, label);
}

async function readJsonFile(filePath) {
  const raw = await fs.readFile(filePath, "utf8");
  return JSON.parse(raw);
}

async function findJsonRecord(directory, predicate) {
  const entries = await fs.readdir(directory, { withFileTypes: true });
  for (const entry of entries) {
    if (!entry.isFile() || !entry.name.endsWith(".json")) {
      continue;
    }
    const filePath = path.join(directory, entry.name);
    const payload = await readJsonFile(filePath);
    if (predicate(payload)) {
      return { filePath, payload };
    }
  }
  return null;
}

function addPersistenceCheck(name, passed, successDetail, failureDetail) {
  summary.persistenceChecks.push({
    name,
    status: passed ? "PASS" : "FAIL",
    detail: passed ? successDetail : failureDetail
  });
  invariant(passed, failureDetail);
}

async function run() {
  await ensureDirectories();

  summary.accounts = {
    mo: {
      name: accounts.mo.name,
      email: accounts.mo.email,
      password: accounts.mo.password,
      role: accounts.mo.role
    },
    ta: {
      name: accounts.ta.name,
      email: accounts.ta.email,
      password: accounts.ta.password,
      role: accounts.ta.role
    },
    admin: {
      name: accounts.admin.name,
      email: accounts.admin.email,
      password: accounts.admin.password,
      role: accounts.admin.role
    }
  };

  const launched = await launchBrowser();
  browser = launched.browser;
  summary.browser = launched.label;

  context = await browser.newContext({
    viewport: { width: 1440, height: 1200 }
  });
  page = await context.newPage();

  await recordScenario("Login page loads and renders the auth shell.", "01-login.png", async () => {
    await gotoPath("/auth/login");
    await page.locator(".auth-card").waitFor({ state: "visible", timeout: 15000 });
  });

  await recordScenario("MO account can register and publish a new position.", null, async () => {
    await registerAccount(accounts.mo);
    await gotoPath("/mo/jobs");

    await page.locator('input[name="title"]').fill(jobTitle);
    await page.locator('input[name="module"]').fill("EBU6304");
    await page.locator('select[name="activityType"]').selectOption("Lab Support");
    await page.locator('input[name="deadline"]').fill("2099-12-31");
    await page.locator('input[name="quota"]').fill("2");
    await page.locator('input[name="workload"]').fill("4");
    await page.locator('textarea[name="requiredSkills"]').fill("Java, Communication, Teaching Support");
    await page
      .locator('textarea[name="description"]')
      .fill("Support weekly lab delivery, answer implementation questions, and help maintain marking consistency.");
    await page.getByRole("button", { name: "Create Position" }).click();
    await waitForPath("/mo/jobs");
    await page.locator(".alert.success strong").filter({ hasText: "Position created." }).waitFor({ timeout: 15000 });

    const jobCard = page.locator(".surface-card").filter({ hasText: jobTitle }).first();
    const reviewLink = jobCard.getByRole("link", { name: "Review applicants" });
    await reviewLink.waitFor({ state: "visible", timeout: 15000 });
    const reviewHref = await reviewLink.getAttribute("href");
    invariant(reviewHref, "Unable to determine the created job review link.");
    summary.entities.jobId = getQueryParam(reviewHref, "jobId");
    invariant(summary.entities.jobId, "Unable to determine the created job ID.");
    await logout();

    return { details: `${summary.entities.jobId} / ${jobTitle}` };
  });

  await recordScenario("TA account can register and view the dashboard.", "02-ta-dashboard.png", async () => {
    await registerAccount(accounts.ta);
    await page.locator(".panel h2").filter({ hasText: "Recommended Jobs" }).waitFor({ state: "visible", timeout: 15000 });
  });

  await recordScenario(
    "TA profile can be completed, a job can be browsed, and structured skills-gap analysis loads.",
    "03-ta-job-detail.png",
    async () => {
      await gotoPath("/ta/profile");
      await page.locator('input[name="studentId"]').fill("2026213001");
      await page.locator('input[name="contactEmail"]').fill(accounts.ta.email);
      await page.locator('input[name="major"]').fill("Software Engineering");
      await page.locator('input[name="year"]').fill("3");
      await page.locator('textarea[name="skills"]').fill("Java, Communication, Teaching Support");
      await page
        .locator('textarea[name="bio"]')
        .fill("Hands-on Java experience with peer support, lab preparation, and consistent student-facing communication.");
      await page.locator('input[name="maxHours"]').fill("10");
      await page.getByRole("button", { name: "Save Changes" }).click();
      await waitForPath("/ta/profile");
      await page.locator('input[name="studentId"]').waitFor({ state: "visible", timeout: 15000 });

      await gotoPath(`/ta/jobs?jobId=${summary.entities.jobId}`);
      await page.locator(".detail-panel h2").filter({ hasText: jobTitle }).waitFor({ state: "visible", timeout: 15000 });
      await page.locator('button[type="submit"]').filter({ hasText: "Submit Application" }).waitFor({ state: "visible", timeout: 15000 });

      const aiBox = page.locator('[data-ai-feedback="skills-gap"]');
      const aiState = await runAiFeedback(aiBox, "Skills gap analysis");
      await aiBox.scrollIntoViewIfNeeded();
      return { details: `skills-gap mode=${aiState.mode}; notice=${aiState.notice}` };
    }
  );

  await recordScenario("TA can submit an application and view the updated tracker.", "04-ta-applications.png", async () => {
    await page.locator('textarea[name="coverLetter"]').fill("I can explain coding tasks clearly and support dependable weekly lab delivery.");
    await page.locator('button[type="submit"]').filter({ hasText: "Submit Application" }).click();
    await waitForPath("/ta/applications");
    await page.locator(".alert.success strong").filter({ hasText: "Application submitted." }).waitFor({ timeout: 15000 });
    await page.locator(".status-chip.status-pending").first().waitFor({ state: "visible", timeout: 15000 });
  });
  await logout();

  await recordScenario(
    "MO can review the applicant, load explainable match analysis, and save an accepted decision.",
    "05-mo-applicants.png",
    async () => {
      await login(accounts.mo.email, accounts.mo.password, accounts.mo.homePath);
      await gotoPath(`/mo/jobs/applicants?jobId=${summary.entities.jobId}`);
      await page.locator(`text=${jobTitle}`).first().waitFor({ state: "visible", timeout: 15000 });

      const applicantCard = page.locator(".applicant-card").filter({ hasText: accounts.ta.email }).first();
      await applicantCard.waitFor({ state: "visible", timeout: 15000 });

      const aiBox = applicantCard.locator('[data-ai-feedback="match-insight"]').first();
      const aiState = await runAiFeedback(aiBox, "Match analysis");

      await applicantCard.locator('select[name="decision"]').selectOption("accepted");
      await applicantCard.locator('input[name="note"]').fill("Strong structured fit for the required lab support tasks.");
      await applicantCard.locator('button[type="submit"]').filter({ hasText: "Save decision" }).click();
      await waitForPath("/mo/jobs/applicants");
      await page.locator(".alert.success strong").filter({ hasText: "Application decision saved." }).waitFor({ timeout: 15000 });
      await page.locator(".applicant-card").filter({ hasText: accounts.ta.email }).locator(".status-chip.status-accepted").first().waitFor({ state: "visible", timeout: 15000 });
      return { details: `match mode=${aiState.mode}; notice=${aiState.notice}` };
    }
  );
  await logout();

  await recordScenario(
    "Admin can open workload overview and generate workload advice with structured fallback output.",
    "06-admin-workload.png",
    async () => {
      await registerAccount(accounts.admin);
      await gotoPath("/admin/workload");
      const aiBox = page.locator('[data-ai-feedback="workload-balance"]');
      const aiState = await runAiFeedback(aiBox, "Workload advice");
      await page.locator(".status-chip.status-underload, .status-chip.status-balanced, .status-chip.status-overload").first().waitFor({
        state: "visible",
        timeout: 15000
      });
      return { details: `workload mode=${aiState.mode}; notice=${aiState.notice}` };
    }
  );
  await logout();

  await recordScenario("Invalid login shows a clear error state.", "07-login-error.png", async () => {
    await gotoPath("/auth/login");
    await page.locator('input[name="email"]').fill(accounts.ta.email);
    await page.locator('input[name="password"]').fill("wrong-pass");
    await page.getByRole("button", { name: "Sign In" }).click();
    await page.locator(".alert.error strong").filter({ hasText: "Invalid email or password." }).waitFor({ timeout: 15000 });
  });

  if (skipPersistence) {
    return;
  }

  const moRecord = await findJsonRecord(path.join(dataRoot, "users", "mos"), (record) => record.email === accounts.mo.email);
  addPersistenceCheck(
    "MO account persisted to the isolated QA data root.",
    !!moRecord,
    moRecord ? toPosix(moRecord.filePath) : null,
    "MO user JSON record was not created."
  );

  const taRecord = await findJsonRecord(path.join(dataRoot, "users", "tas"), (record) => record.email === accounts.ta.email);
  addPersistenceCheck(
    "TA account persisted to the isolated QA data root.",
    !!taRecord,
    taRecord ? toPosix(taRecord.filePath) : null,
    "TA user JSON record was not created."
  );

  const adminRecord = await findJsonRecord(path.join(dataRoot, "users", "admins"), (record) => record.email === accounts.admin.email);
  addPersistenceCheck(
    "Admin account persisted to the isolated QA data root.",
    !!adminRecord,
    adminRecord ? toPosix(adminRecord.filePath) : null,
    "Admin user JSON record was not created."
  );

  summary.entities.moUserId = moRecord?.payload?.userId || null;
  summary.entities.taUserId = taRecord?.payload?.userId || null;
  summary.entities.adminUserId = adminRecord?.payload?.userId || null;

  const applicantRecord = await findJsonRecord(
    path.join(dataRoot, "applicants"),
    (record) => record.contactEmail === accounts.ta.email && record.major === "Software Engineering"
  );
  addPersistenceCheck(
    "Applicant profile persisted after TA profile submission.",
    !!applicantRecord,
    applicantRecord ? toPosix(applicantRecord.filePath) : null,
    "Applicant profile JSON record was not created."
  );

  const jobRecord = await findJsonRecord(
    path.join(dataRoot, "jobs"),
    (record) => record.title === jobTitle
  );
  addPersistenceCheck(
    "MO job posting persisted after creation.",
    !!jobRecord,
    jobRecord ? toPosix(jobRecord.filePath) : null,
    "Job JSON record was not created."
  );

  summary.entities.jobId = jobRecord?.payload?.jobId || summary.entities.jobId;

  const applicationRecord = await findJsonRecord(
    path.join(dataRoot, "applications"),
    (record) =>
      record.applicantId === taRecord?.payload?.userId &&
      record.jobId === jobRecord?.payload?.jobId &&
      record.status === "accepted"
  );
  addPersistenceCheck(
    "Application persisted and review status updated to accepted.",
    !!applicationRecord,
    applicationRecord ? `${toPosix(applicationRecord.filePath)} (status=accepted)` : null,
    "Application JSON record was not created or the review status was not saved as accepted."
  );

  summary.entities.applicationId = applicationRecord?.payload?.applicationId || null;

  addPersistenceCheck(
    "Job acceptedCount synchronized after MO review.",
    jobRecord?.payload?.acceptedCount === 1,
    `acceptedCount=${jobRecord?.payload?.acceptedCount}`,
    `Expected job acceptedCount to be 1 but received ${jobRecord?.payload?.acceptedCount}.`
  );
}

try {
  await run();
  await writeSummary();
} catch (error) {
  summary.error = error.message;
  await writeSummary();
  console.error(error);
  process.exitCode = 1;
} finally {
  await context?.close().catch(() => {});
  await browser?.close().catch(() => {});
}
