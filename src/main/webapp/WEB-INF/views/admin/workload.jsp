<%@ include file="/WEB-INF/views/shared/app-start.jspf" %>
<div class="metric-grid">
    <article class="metric-card">
        <span>Total TAs</span>
        <strong><c:out value="${metrics.totalTAs}"/></strong>
    </article>
    <article class="metric-card">
        <span>Overloaded</span>
        <strong><c:out value="${metrics.overloaded}"/></strong>
    </article>
    <article class="metric-card">
        <span>Open jobs</span>
        <strong><c:out value="${metrics.openJobs}"/></strong>
    </article>
    <article class="metric-card">
        <span>Accepted placements</span>
        <strong><c:out value="${metrics.acceptedPlacements}"/></strong>
    </article>
</div>

<div class="two-column">
    <section class="panel">
        <div class="panel-head">
            <div>
                <h2>Capacity</h2>
            </div>
        </div>
        <p class="cell-subtle">Each row totals weekly hours from accepted placements and compares them to the TA weekly limit stored in the applicant profile (status: underload / balanced / overload).</p>
        <div class="table-wrap">
            <table class="data-table">
                <thead>
                <tr>
                    <th>TA</th>
                    <th>Accepted jobs</th>
                    <th>Total hours</th>
                    <th>Limit</th>
                    <th>Status</th>
                </tr>
                </thead>
                <tbody>
                <c:forEach items="${workloads}" var="item">
                    <tr>
                        <td>
                            <strong><c:out value="${item.name}"/></strong>
                            <div class="cell-subtle"><c:out value="${item.userId}"/></div>
                        </td>
                        <td><c:forEach items="${item.acceptedJobs}" var="jobItem" varStatus="state"><c:out value="${jobItem}"/><c:if test="${not state.last}"><br/></c:if></c:forEach></td>
                        <td><c:out value="${item.totalHours}"/>h/week</td>
                        <td><c:out value="${item.maxHours}"/>h/week</td>
                        <td><span class="status-chip status-${item.loadStatus}"><c:out value="${item.loadStatus}"/></span></td>
                    </tr>
                </c:forEach>
                </tbody>
            </table>
        </div>
    </section>

    <section class="panel">
        <div class="panel-head">
            <div>
                <h2>Advice</h2>
            </div>
        </div>
        <div class="ai-box" data-ai-feedback="workload-balance"></div>
    </section>
</div>

<section class="panel">
    <div class="panel-head">
        <div>
            <h2>Workload distribution</h2>
            <p class="cell-subtle">Hours allocated per TA versus their declared weekly capacity. Bars exceeding the dashed limit indicate overload.</p>
        </div>
    </div>
    <div style="position:relative;height:320px;">
        <canvas id="workloadChart"></canvas>
    </div>
    <div class="panel-head" style="margin-top:1rem;">
        <div>
            <h3 style="margin:0;">Load status mix</h3>
        </div>
    </div>
    <div style="position:relative;height:240px;max-width:480px;">
        <canvas id="loadStatusChart"></canvas>
    </div>
    <script id="workload-data" type="application/json"><c:forEach items="${workloads}" var="w" varStatus="s">{"name":"<c:out value='${w.name}' escapeXml='true'/>","userId":"<c:out value='${w.userId}'/>","totalHours":<c:out value="${w.totalHours}"/>,"maxHours":<c:out value="${w.maxHours}"/>,"loadStatus":"<c:out value='${w.loadStatus}'/>"}<c:if test="${not s.last}">,</c:if></c:forEach></script>
    <script src="https://cdn.jsdelivr.net/npm/chart.js@4.4.1/dist/chart.umd.min.js"></script>
    <script src="${pageContext.request.contextPath}/assets/js/workload-charts.js"></script>
</section>

<section class="panel">
    <div class="panel-head">
        <div>
            <h2>Jobs</h2>
        </div>
        <a class="btn secondary" href="${pageContext.request.contextPath}/admin/jobs">Manage</a>
    </div>
    <div class="table-wrap">
        <table class="data-table">
            <thead>
            <tr>
                <th>Position</th>
                <th>Status</th>
                <th>Applications</th>
                <th>Accepted</th>
            </tr>
            </thead>
            <tbody>
            <c:forEach items="${jobs}" var="item">
                <tr>
                    <td><strong><c:out value="${item.job.title}"/></strong><div class="cell-subtle"><c:out value="${item.job.module}"/></div></td>
                    <td><span class="status-chip status-${item.job.status}"><c:out value="${item.job.status}"/></span></td>
                    <td><c:out value="${item.applicationCount}"/></td>
                    <td><c:out value="${item.acceptedCount}"/> / <c:out value="${item.job.quota}"/></td>
                </tr>
            </c:forEach>
            </tbody>
        </table>
    </div>
</section>
<%@ include file="/WEB-INF/views/shared/app-end.jspf" %>
