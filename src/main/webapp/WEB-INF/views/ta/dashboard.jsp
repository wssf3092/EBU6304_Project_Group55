<%@ include file="/WEB-INF/views/shared/app-start.jspf" %>
<div class="metric-grid">
    <article class="metric-card">
        <span>Open jobs</span>
        <strong><c:out value="${metrics.openJobs}"/></strong>
    </article>
    <article class="metric-card">
        <span>Applications</span>
        <strong><c:out value="${metrics.applications}"/></strong>
    </article>
    <article class="metric-card">
        <span>Accepted</span>
        <strong><c:out value="${metrics.accepted}"/></strong>
    </article>
    <article class="metric-card">
        <span>Capacity</span>
        <strong><c:out value="${metrics.capacity}"/>h/week</strong>
    </article>
</div>

<div class="two-column">
    <section class="panel">
        <div class="panel-head">
            <div>
                <h2>Recommended Jobs</h2>
            </div>
            <a class="btn secondary" href="${pageContext.request.contextPath}/ta/jobs">All jobs</a>
        </div>
        <c:choose>
            <c:when test="${empty recommendedJobs}">
                <div class="empty-state">
                    <h3>No recommendations</h3>
                </div>
            </c:when>
            <c:otherwise>
                <div class="stack-list">
                    <c:forEach items="${recommendedJobs}" var="jobView">
                        <a class="surface-link" href="${pageContext.request.contextPath}/ta/jobs?jobId=${jobView.job.jobId}">
                            <div class="surface-link-head">
                                <div>
                                    <strong><c:out value="${jobView.job.title}"/></strong>
                                    <span><c:out value="${jobView.job.module}"/> / <c:out value="${jobView.job.activityType}"/></span>
                                </div>
                                <span class="score-pill"><c:out value="${jobView.matchScore}"/>%</span>
                            </div>
                            <div class="badge-row">
                                <c:forEach items="${jobView.matchedSkills}" var="skill">
                                    <span class="badge success"><c:out value="${skill}"/></span>
                                </c:forEach>
                            </div>
                        </a>
                    </c:forEach>
                </div>
            </c:otherwise>
        </c:choose>
    </section>

    <section class="panel">
        <div class="panel-head">
            <div>
                <h2>Profile</h2>
            </div>
            <a class="btn secondary" href="${pageContext.request.contextPath}/ta/profile">Edit</a>
        </div>
        <div class="detail-stack">
            <div class="stat-line">
                <span>Profile completion</span>
                <strong>${profile.complete ? 'Ready' : 'Needs attention'}</strong>
            </div>
            <div class="stat-line">
                <span>CV status</span>
                <strong>${profile.hasCv ? 'Uploaded' : 'Missing'}</strong>
            </div>
            <div class="stat-line">
                <span>Skills listed</span>
                <strong><c:out value="${fn:length(profile.skills)}"/></strong>
            </div>
            <div class="stat-line">
                <span>Workload limit</span>
                <strong><c:out value="${profile.maxWorkloadHoursPerWeek}"/>h/week</strong>
            </div>
        </div>
    </section>
</div>

<section class="panel">
    <div class="panel-head">
        <div>
            <h2>Applications</h2>
        </div>
        <a class="btn secondary" href="${pageContext.request.contextPath}/ta/applications">All</a>
    </div>
    <c:choose>
        <c:when test="${empty recentApplications}">
            <div class="empty-state">
                <h3>No applications</h3>
            </div>
        </c:when>
        <c:otherwise>
            <div class="table-wrap">
                <table class="data-table">
                    <thead>
                    <tr>
                        <th>Position</th>
                        <th>Applied</th>
                        <th>Status</th>
                        <th>Match</th>
                    </tr>
                    </thead>
                    <tbody>
                    <c:forEach items="${recentApplications}" var="item">
                        <tr>
                            <td>
                                <strong><c:out value="${item.job.title}"/></strong>
                                <div class="cell-subtle"><c:out value="${item.job.module}"/></div>
                            </td>
                            <td><c:out value="${item.application.displayAppliedAt}"/></td>
                            <td><span class="status-chip status-${item.application.status}"><c:out value="${item.application.status}"/></span></td>
                            <td><c:out value="${item.matchScore}"/>%</td>
                        </tr>
                    </c:forEach>
                    </tbody>
                </table>
            </div>
        </c:otherwise>
    </c:choose>
</section>
<%@ include file="/WEB-INF/views/shared/app-end.jspf" %>
