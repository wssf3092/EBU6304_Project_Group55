<%@ include file="/WEB-INF/views/shared/app-start.jspf" %>
<div class="metric-grid">
    <article class="metric-card">
        <span>Active jobs</span>
        <strong><c:out value="${metrics.activeJobs}"/></strong>
    </article>
    <article class="metric-card">
        <span>Pending applications</span>
        <strong><c:out value="${metrics.pendingApplications}"/></strong>
    </article>
    <article class="metric-card">
        <span>Accepted placements</span>
        <strong><c:out value="${metrics.acceptedPlacements}"/></strong>
    </article>
    <article class="metric-card">
        <span>Remaining seats</span>
        <strong><c:out value="${metrics.positionsOpen}"/></strong>
    </article>
</div>

<section class="panel">
    <div class="panel-head">
        <div>
            <h2>Jobs</h2>
        </div>
        <a class="btn secondary" href="${pageContext.request.contextPath}/mo/jobs">Manage</a>
    </div>
    <div class="table-wrap">
        <table class="data-table">
            <thead>
            <tr>
                <th>Position</th>
                <th>Status</th>
                <th>Applications</th>
                <th>Accepted</th>
                <th>Next action</th>
            </tr>
            </thead>
            <tbody>
            <c:forEach items="${jobs}" var="item">
                <tr>
                    <td>
                        <strong><c:out value="${item.job.title}"/></strong>
                        <div class="cell-subtle"><c:out value="${item.job.module}"/> / <c:out value="${item.job.activityType}"/></div>
                    </td>
                    <td><span class="status-chip status-${item.job.status}"><c:out value="${item.job.status}"/></span></td>
                    <td><c:out value="${item.applicationCount}"/></td>
                    <td><c:out value="${item.acceptedCount}"/> / <c:out value="${item.job.quota}"/></td>
                    <td><a class="table-link" href="${pageContext.request.contextPath}/mo/jobs/applicants?jobId=${item.job.jobId}">Review applicants</a></td>
                </tr>
            </c:forEach>
            </tbody>
        </table>
    </div>
</section>
<section class="panel">
    <div class="panel-head">
        <div>
            <h2>Application Status</h2>
            <p class="panel-subtitle">Distribution across all your job postings</p>
        </div>
    </div>
    <div class="chart-row">
        <div class="chart-wrap chart-wrap--sm">
            <canvas id="moStatusChart"></canvas>
        </div>
    </div>
    <script id="moStatusData" type="application/json"><c:out value="${statusChartJson}" escapeXml="false"/></script>
    <script src="https://cdn.jsdelivr.net/npm/chart.js@4.4.1/dist/chart.umd.min.js"></script>
    <script src="${pageContext.request.contextPath}/assets/js/mo-charts.js"></script>
</section>
<%@ include file="/WEB-INF/views/shared/app-end.jspf" %>
