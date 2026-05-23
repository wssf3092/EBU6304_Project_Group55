/**
 * Renders the Admin workload dashboard charts using Chart.js.
 * Reads data from a script tag with id="workload-data" (JSON array) so the JSP can drive both
 * the table and the charts from the same WorkloadEntry DTO without duplicating server logic.
 */
(function () {
    "use strict";

    function readData() {
        var node = document.getElementById("workload-data");
        if (!node) return [];
        var raw = (node.textContent || "").trim();
        if (!raw) return [];
        try {
            return JSON.parse("[" + raw + "]");
        } catch (err) {
            console.warn("workload-charts: cannot parse data", err);
            return [];
        }
    }

    function statusColor(status) {
        switch ((status || "").toLowerCase()) {
            case "overload":  return "#dc2626";
            case "balanced":  return "#2563eb";
            case "underload": return "#16a34a";
            default:          return "#6b7280";
        }
    }

    function renderHoursChart(entries) {
        var canvas = document.getElementById("workloadChart");
        if (!canvas || !window.Chart) return;
        var labels  = entries.map(function (e) { return e.name || e.userId; });
        var totals  = entries.map(function (e) { return Number(e.totalHours) || 0; });
        var limits  = entries.map(function (e) { return Number(e.maxHours)   || 0; });
        var colors  = entries.map(function (e) { return statusColor(e.loadStatus); });

        new Chart(canvas.getContext("2d"), {
            type: "bar",
            data: {
                labels: labels,
                datasets: [
                    { label: "Total hours/week", data: totals, backgroundColor: colors, borderRadius: 4 },
                    { label: "Weekly limit",     data: limits, type: "line", borderColor: "#111827",
                      borderDash: [6, 4], pointRadius: 0, fill: false, tension: 0 }
                ]
            },
            options: {
                responsive: true, maintainAspectRatio: false,
                plugins: { legend: { position: "bottom" } },
                scales: { y: { beginAtZero: true, title: { display: true, text: "Hours / week" } } }
            }
        });
    }

    function renderStatusChart(entries) {
        var canvas = document.getElementById("loadStatusChart");
        if (!canvas || !window.Chart) return;
        var buckets = { underload: 0, balanced: 0, overload: 0 };
        entries.forEach(function (e) {
            var key = (e.loadStatus || "").toLowerCase();
            if (buckets.hasOwnProperty(key)) buckets[key] += 1;
        });
        new Chart(canvas.getContext("2d"), {
            type: "doughnut",
            data: {
                labels: ["Underload", "Balanced", "Overload"],
                datasets: [{
                    data: [buckets.underload, buckets.balanced, buckets.overload],
                    backgroundColor: ["#16a34a", "#2563eb", "#dc2626"]
                }]
            },
            options: { responsive: true, maintainAspectRatio: false,
                       plugins: { legend: { position: "bottom" } } }
        });
    }

    document.addEventListener("DOMContentLoaded", function () {
        var entries = readData();
        if (!entries.length) return;
        renderHoursChart(entries);
        renderStatusChart(entries);
    });
})();
