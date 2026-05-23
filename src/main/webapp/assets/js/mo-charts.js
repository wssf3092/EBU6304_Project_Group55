/**
 * Renders the MO dashboard application-status doughnut chart using Chart.js.
 * Reads data from a script tag with id="moStatusData" (JSON object) so the JSP can
 * inject server-side counts without duplicating logic in client-side code.
 */
(function () {
    "use strict";

    function readData() {
        var node = document.getElementById("moStatusData");
        if (!node) return null;
        var raw = (node.textContent || "").trim();
        if (!raw) return null;
        try {
            return JSON.parse(raw);
        } catch (err) {
            console.warn("mo-charts: cannot parse status data", err);
            return null;
        }
    }

    function renderStatusChart(data) {
        var canvas = document.getElementById("moStatusChart");
        if (!canvas || !window.Chart) return;
        var pending  = Number(data.pending)  || 0;
        var accepted = Number(data.accepted) || 0;
        var rejected = Number(data.rejected) || 0;
        if (pending + accepted + rejected === 0) {
            canvas.parentNode.innerHTML = "<p class=\"chart-empty\">No applications yet.</p>";
            return;
        }
        new Chart(canvas.getContext("2d"), {
            type: "doughnut",
            data: {
                labels: ["Pending", "Accepted", "Rejected"],
                datasets: [{
                    data: [pending, accepted, rejected],
                    backgroundColor: ["#f59e0b", "#16a34a", "#dc2626"],
                    borderWidth: 2
                }]
            },
            options: {
                responsive: true,
                maintainAspectRatio: false,
                plugins: {
                    legend: { position: "bottom" },
                    tooltip: {
                        callbacks: {
                            label: function (ctx) {
                                var total = ctx.dataset.data.reduce(function (a, b) { return a + b; }, 0);
                                var pct = total > 0 ? Math.round(ctx.parsed / total * 100) : 0;
                                return " " + ctx.label + ": " + ctx.parsed + " (" + pct + "%)";
                            }
                        }
                    }
                }
            }
        });
    }

    document.addEventListener("DOMContentLoaded", function () {
        var data = readData();
        if (data) renderStatusChart(data);
    });
})();
