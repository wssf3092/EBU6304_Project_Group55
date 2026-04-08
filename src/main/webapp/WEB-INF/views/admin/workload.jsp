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
