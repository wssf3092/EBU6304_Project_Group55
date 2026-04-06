<%@ include file="/WEB-INF/views/shared/app-start.jspf" %>
<section class="panel">
    <form method="get" action="${pageContext.request.contextPath}/ta/jobs" class="filter-bar">
        <input type="text" name="q" value="<c:out value='${query}'/>" placeholder="Search jobs"/>
        <select name="skill">
            <option value="">All skills</option>
            <c:forEach items="${skillCatalog}" var="skillItem">
                <option value="${skillItem}" ${skill eq skillItem ? 'selected' : ''}><c:out value="${skillItem}"/></option>
            </c:forEach>
        </select>
        <select name="status">
            <option value="open" ${status eq 'open' ? 'selected' : ''}>Open</option>
            <option value="all" ${status eq 'all' ? 'selected' : ''}>All</option>
            <option value="closed" ${status eq 'closed' ? 'selected' : ''}>Closed</option>
        </select>
        <button class="btn secondary" type="submit">Filter</button>
    </form>
</section>

<div class="split-layout">
    <section class="panel">
        <div class="panel-head">
            <div>
                <h2>Jobs</h2>
            </div>
            <span class="subtle-pill"><c:out value="${fn:length(jobs)}"/> results</span>
        </div>
        <c:choose>
            <c:when test="${empty jobs}">
                <div class="empty-state">
                    <h3>No jobs</h3>
                </div>
            </c:when>
            <c:otherwise>
                <div class="stack-list">
                    <c:forEach items="${jobs}" var="jobView">
                        <a class="surface-link ${selectedJob ne null and selectedJob.job.jobId eq jobView.job.jobId ? 'selected' : ''}"
                           href="${pageContext.request.contextPath}/ta/jobs?jobId=${jobView.job.jobId}&q=${query}&skill=${skill}&status=${status}">
                            <div class="surface-link-head">
                                <div>
                                    <strong><c:out value="${jobView.job.title}"/></strong>
                                    <span><c:out value="${jobView.job.module}"/> / <c:out value="${jobView.job.activityType}"/></span>
                                </div>
                                <span class="status-chip status-${jobView.job.closed ? 'closed' : 'open'}">${jobView.job.closed ? 'closed' : 'open'}</span>
                            </div>
                            <div class="inline-meta">
                                <span>Match <strong><c:out value="${jobView.matchScore}"/>%</strong></span>
                                <span>Quota <strong><c:out value="${jobView.job.remainingQuota}"/></strong> left</span>
                                <span>Deadline <strong><c:out value="${jobView.job.displayDeadline}"/></strong></span>
                            </div>
                            <div class="badge-row">
                                <c:forEach items="${jobView.job.requiredSkills}" var="skillItem">
                                    <span class="badge"><c:out value="${skillItem}"/></span>
                                </c:forEach>
                            </div>
                        </a>
                    </c:forEach>
                </div>
            </c:otherwise>
        </c:choose>
    </section>

    <section class="panel detail-panel">
        <c:choose>
            <c:when test="${selectedJob eq null}">
                <div class="empty-state">
                    <h3>Select a job</h3>
                </div>
            </c:when>
            <c:otherwise>
                <div class="panel-head">
                    <div>
                        <h2><c:out value="${selectedJob.job.title}"/></h2>
                    </div>
                    <span class="score-pill"><c:out value="${selectedJob.matchScore}"/>% fit</span>
                </div>
                <div class="detail-stack">
                    <div class="stat-line">
                        <span>Module</span>
                        <strong><c:out value="${selectedJob.job.module}"/></strong>
                    </div>
                    <div class="stat-line">
                        <span>Activity</span>
                        <strong><c:out value="${selectedJob.job.activityType}"/></strong>
                    </div>
                    <div class="stat-line">
                        <span>Deadline</span>
                        <strong><c:out value="${selectedJob.job.displayDeadline}"/></strong>
                    </div>
                    <div class="stat-line">
                        <span>Weekly workload</span>
                        <strong><c:out value="${selectedJob.job.workloadHoursPerWeek}"/>h/week</strong>
                    </div>
                </div>

                <div class="rich-copy">
                    <h3>Description</h3>
                    <p><c:out value="${selectedJob.job.description}"/></p>
                </div>

                <div class="dual-chip-panel">
                    <div>
                        <h3>Matched skills</h3>
                        <div class="badge-row">
                            <c:forEach items="${selectedJob.matchedSkills}" var="skillItem">
                                <span class="badge success"><c:out value="${skillItem}"/></span>
                            </c:forEach>
                        </div>
                    </div>
                    <div>
                        <h3>Missing skills</h3>
                        <div class="badge-row">
                            <c:forEach items="${selectedJob.missingSkills}" var="skillItem">
                                <span class="badge warning"><c:out value="${skillItem}"/></span>
                            </c:forEach>
                        </div>
                    </div>
                </div>

                <div class="ai-box" data-ai-feedback="skills-gap" data-job-id="${selectedJob.job.jobId}"></div>

                <form method="post" action="${pageContext.request.contextPath}/ta/jobs/apply" class="stack-form">
                    <input type="hidden" name="jobId" value="${selectedJob.job.jobId}"/>
                    <label class="field">
                        <span>Cover letter</span>
                        <textarea name="coverLetter" maxlength="500" placeholder="Optional"></textarea>
                    </label>
                    <c:choose>
                        <c:when test="${selectedJob.actionEnabled}">
                            <button class="btn primary wide" type="submit">Submit Application</button>
                        </c:when>
                        <c:otherwise>
                            <div class="action-note"><c:out value="${selectedJob.applyDisabledReason}"/></div>
                            <button class="btn secondary wide" type="button" disabled>Submit Application</button>
                        </c:otherwise>
                    </c:choose>
                </form>
            </c:otherwise>
        </c:choose>
    </section>
</div>
<%@ include file="/WEB-INF/views/shared/app-end.jspf" %>
