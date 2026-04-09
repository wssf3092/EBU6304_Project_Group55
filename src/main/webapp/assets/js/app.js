window.App = {
    contextPath() {
        return document.body?.dataset?.contextPath || "";
    },

    escapeHtml(value) {
        return String(value ?? "")
            .replace(/&/g, "&amp;")
            .replace(/</g, "&lt;")
            .replace(/>/g, "&gt;")
            .replace(/"/g, "&quot;")
            .replace(/'/g, "&#39;");
    },

    async getJson(url) {
        const response = await fetch(url, {
            headers: { "X-Requested-With": "XMLHttpRequest" }
        });
        const payload = await response.json().catch(() => ({}));
        if (!response.ok) {
            throw new Error(payload.error || `Request failed: ${response.status}`);
        }
        return payload;
    },

    // Step 10: keys match data-ai-feedback on JSP; fetch URLs map to /ai/* servlets.
    aiDefinitions() {
        return {
            "skills-gap": {
                title: "Skills Gap",
                actionLabel: "Run Analysis",
                refreshLabel: "Refresh Analysis",
                idleCopy: "Run the analysis only when needed. Successful results are cached locally to reduce repeated traffic.",
                buildUrl: (container) => `${this.contextPath()}/ai/skills-gap?jobId=${encodeURIComponent(container.dataset.jobId || "")}`,
                render: (target, data) => this.renderSkillGap(target, data)
            },
            "match-insight": {
                title: "Match Insight",
                actionLabel: "Analyze Match",
                refreshLabel: "Refresh Match",
                idleCopy: "Run applicant-job analysis on demand. Successful results are cached locally to reduce repeated traffic.",
                buildUrl: (container) => `${this.contextPath()}/ai/match?applicationId=${encodeURIComponent(container.dataset.applicationId || "")}`,
                render: (target, data) => this.renderMatchInsight(target, data)
            },
            "workload-balance": {
                title: "Workload Advice",
                actionLabel: "Generate Advice",
                refreshLabel: "Refresh Advice",
                idleCopy: "Generate workload advice only when needed. Successful results are cached locally to reduce repeated traffic.",
                buildUrl: () => `${this.contextPath()}/ai/workload-balance`,
                render: (target, data) => this.renderWorkloadAdvice(target, data)
            }
        };
    },

    aiFeedbackConfig(kind) {
        return this.aiDefinitions()[kind] || null;
    },

    renderAiFeedbackShell(container, config) {
        container.innerHTML = `
            <div class="ai-control">
                <div class="ai-control-copy">
                    <strong>${this.escapeHtml(config.title)}</strong>
                    <div class="cell-subtle">${this.escapeHtml(config.idleCopy)}</div>
                </div>
                <button class="btn secondary ai-trigger" type="button" data-ai-trigger>${this.escapeHtml(config.actionLabel)}</button>
            </div>
            <div class="ai-content">
                <div class="ai-placeholder">Ready</div>
            </div>
        `;
    },

    renderLoading(target) {
        target.innerHTML = `<div class="ai-placeholder">Loading...</div>`;
    },

    updateAiTriggerLabel(container, config, loading) {
        const button = container.querySelector("[data-ai-trigger]");
        if (!button) {
            return;
        }
        button.disabled = !!loading;
        if (loading) {
            button.textContent = "Running...";
            return;
        }
        button.textContent = container.dataset.aiLoaded === "true" ? config.refreshLabel : config.actionLabel;
    },

    async loadAiFeedback(container) {
        const kind = container.dataset.aiFeedback;
        const config = this.aiFeedbackConfig(kind);
        if (!config) {
            return;
        }

        const target = container.querySelector(".ai-content");
        this.updateAiTriggerLabel(container, config, true);
        this.renderLoading(target);

        try {
            const data = await this.getJson(config.buildUrl(container));
            config.render(target, data);
            container.dataset.aiLoaded = "true";
        } catch (error) {
            this.renderError(target, error.message);
        } finally {
            this.updateAiTriggerLabel(container, config, false);
        }
    },

    mountAiFeedback(container) {
        const config = this.aiFeedbackConfig(container.dataset.aiFeedback);
        if (!config) {
            return;
        }

        this.renderAiFeedbackShell(container, config);
        const button = container.querySelector("[data-ai-trigger]");
        button?.addEventListener("click", () => {
            this.loadAiFeedback(container);
        });
    },

    renderSkillGap(target, data) {
        const items = Array.isArray(data.priorityGaps) ? data.priorityGaps : [];
        const matched = Array.isArray(data.matchedSkills) ? data.matchedSkills : [];
        const missing = Array.isArray(data.missingSkills) ? data.missingSkills : [];

        target.innerHTML = `
            <div class="ai-head">
                <strong>Gap</strong>
                <span class="status-chip status-${data.available ? "accepted" : "underload"}">${data.available ? "AI" : "Rules"}</span>
            </div>
            <p>${this.escapeHtml(data.summary || "No summary.")}</p>
            <div class="badge-row">
                ${matched.map((item) => `<span class="badge success">${this.escapeHtml(item)}</span>`).join("")}
                ${missing.map((item) => `<span class="badge warning">${this.escapeHtml(item)}</span>`).join("")}
            </div>
            <ul class="ai-list">
                ${items.length ? items.map((item) => `<li><strong>${this.escapeHtml(item.skill || "Gap")}</strong>: ${this.escapeHtml(item.suggestion || item.why || "")}</li>`).join("") : "<li>No gaps.</li>"}
            </ul>
            <div class="cell-subtle">${this.escapeHtml(data.notice || "")}</div>
        `;
    },

    renderMatchInsight(target, data) {
        const strengths = Array.isArray(data.strengths) ? data.strengths : [];
        const risks = Array.isArray(data.risks) ? data.risks : [];

        target.innerHTML = `
            <div class="ai-head">
                <strong>Match</strong>
                <span class="status-chip status-${data.available ? "accepted" : "underload"}">${data.available ? "AI" : "Rules"}</span>
            </div>
            <div class="ai-grid">
                <div class="ai-metric">
                    <span>Rules</span>
                    <strong>${this.escapeHtml(data.structuredScore)}</strong>
                </div>
                <div class="ai-metric">
                    <span>AI</span>
                    <strong>${this.escapeHtml(data.aiScore)}</strong>
                </div>
                <div class="ai-metric">
                    <span>Final</span>
                    <strong>${this.escapeHtml(data.combinedScore)}</strong>
                </div>
            </div>
            <p>${this.escapeHtml(data.summary || "No summary.")}</p>
            <div class="two-column tight">
                <div>
                    <strong>Strengths</strong>
                    <ul class="ai-list">
                        ${strengths.length ? strengths.map((item) => `<li>${this.escapeHtml(item)}</li>`).join("") : "<li>None.</li>"}
                    </ul>
                </div>
                <div>
                    <strong>Risks</strong>
                    <ul class="ai-list">
                        ${risks.length ? risks.map((item) => `<li>${this.escapeHtml(item)}</li>`).join("") : "<li>None.</li>"}
                    </ul>
                </div>
            </div>
            <div class="cell-subtle">${this.escapeHtml(data.notice || "")}</div>
        `;
    },

    renderWorkloadAdvice(target, data) {
        const recommendations = Array.isArray(data.recommendations) ? data.recommendations : [];
        target.innerHTML = `
            <div class="ai-head">
                <strong>Advice</strong>
                <span class="status-chip status-${data.available ? "accepted" : "underload"}">${data.available ? "AI" : "Rules"}</span>
            </div>
            <p>${this.escapeHtml(data.summary || "No summary.")}</p>
            <ul class="ai-list">
                ${recommendations.length ? recommendations.map((item) => `<li><strong>${this.escapeHtml(item.taId || "TA")}</strong> -> <strong>${this.escapeHtml(item.jobId || "Job")}</strong>: ${this.escapeHtml(item.reason || "")}</li>`).join("") : "<li>None.</li>"}
            </ul>
            <div class="cell-subtle">${this.escapeHtml(data.notice || "")}</div>
        `;
    },

    renderError(container, message) {
        container.innerHTML = `<div class="alert error"><span>Error</span><strong>${this.escapeHtml(message)}</strong></div>`;
    },

    init() {
        document.querySelectorAll("[data-ai-feedback]").forEach((container) => {
            this.mountAiFeedback(container);
        });
    }
};

document.addEventListener("DOMContentLoaded", () => window.App.init());
