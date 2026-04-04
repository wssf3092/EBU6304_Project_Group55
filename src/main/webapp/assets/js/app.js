/**
 * Step 5 frontend utilities (course TA baseline; no AI modules).
 */
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

    initSidebarToggle() {
        const shell = document.querySelector(".shell");
        const btn = document.querySelector("[data-sidebar-toggle]");
        if (!shell || !btn) {
            return;
        }
        btn.addEventListener("click", () => {
            shell.classList.toggle("shell--sidebar-open");
        });
    },

    initFormConfirm() {
        document.querySelectorAll("form[data-confirm]").forEach((form) => {
            form.addEventListener("submit", (ev) => {
                const msg = form.getAttribute("data-confirm") || "确定执行此操作？";
                if (!window.confirm(msg)) {
                    ev.preventDefault();
                }
            });
        });
    },

    init() {
        this.initSidebarToggle();
        this.initFormConfirm();
    }
};

document.addEventListener("DOMContentLoaded", () => window.App.init());
