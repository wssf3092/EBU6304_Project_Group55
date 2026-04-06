<%@ include file="/WEB-INF/views/shared/app-start.jspf" %>
<section class="panel">
    <div class="panel-head">
        <div>
            <h2><c:out value="${job.title}"/></h2>
            <p class="panel-subtle"><c:out value="${job.module}"/> / <c:out value="${job.activityType}"/> / <c:out value="${job.displayDeadline}"/></p>
        </div>
        <div class="inline-actions">
            <a class="btn secondary" href="${pageContext.request.contextPath}/mo/jobs">Back</a>
            <a class="btn secondary" href="${pageContext.request.contextPath}/mo/jobs/applicants?jobId=${job.jobId}&sort=recent">Recent</a>
            <a class="btn secondary" href="${pageContext.request.contextPath}/mo/jobs/applicants?jobId=${job.jobId}&sort=skill">Fit</a>
            <a class="btn secondary" href="${pageContext.request.contextPath}/mo/jobs/applicants?jobId=${job.jobId}&sort=status">Status</a>
        </div>
    </div>
</section>

<div class="stack-list">
    <c:forEach items="${applicants}" var="item">
        <section class="panel applicant-card">
            <div class="surface-link-head">
                <div>
                    <strong><c:out value="${item.applicant.name}"/></strong>
                    <span><c:out value="${item.profile.major}"/> / Year <c:out value="${item.profile.year}"/> / <c:out value="${item.applicant.email}"/></span>
                </div>
                <div class="inline-actions">
                    <span class="score-pill"><c:out value="${item.matchScore}"/>%</span>
                    <span class="status-chip status-${item.application.status}"><c:out value="${item.application.status}"/></span>
                </div>
            </div>

            <div class="two-column tight">
                <div>
                    <h3>Profile</h3>
                    <p class="rich-copy"><c:out value="${item.profile.bio}"/></p>
                    <div class="badge-row">
                        <c:forEach items="${item.profile.skills}" var="skillItem">
                            <span class="badge"><c:out value="${skillItem}"/></span>
                        </c:forEach>
                    </div>
                    <p class="note-line">Applied <c:out value="${item.application.displayAppliedAt}"/></p>
                    <p class="note-line">Cover letter: <c:out value="${empty item.application.coverLetter ? 'None' : item.application.coverLetter}"/></p>
                    <c:choose>
                        <c:when test="${item.cvAvailable}">
                            <a class="table-link" href="${pageContext.request.contextPath}/files/cv?userId=${item.applicant.userId}">Download CV</a>
                        </c:when>
                        <c:otherwise>
                            <span class="cell-subtle">Not uploaded</span>
                        </c:otherwise>
                    </c:choose>
                </div>
                <div>
                    <h3>Match</h3>
                    <div class="badge-row">
                        <c:forEach items="${item.matchedSkills}" var="skillItem">
                            <span class="badge success"><c:out value="${skillItem}"/></span>
                        </c:forEach>
                        <c:forEach items="${item.missingSkills}" var="skillItem">
                            <span class="badge warning"><c:out value="${skillItem}"/></span>
                        </c:forEach>
                    </div>
                    <div class="ai-box" data-ai-feedback="match-insight" data-application-id="${item.application.applicationId}"></div>
                </div>
            </div>

            <form method="post" action="${pageContext.request.contextPath}/mo/applications/review" class="form-inline-block">
                <input type="hidden" name="jobId" value="${job.jobId}"/>
                <input type="hidden" name="applicationId" value="${item.application.applicationId}"/>
                <select name="decision" required>
                    <option value="">Select decision</option>
                    <option value="accepted">Accept</option>
                    <option value="rejected">Reject</option>
                </select>
                <input type="text" name="note" placeholder="Note"/>
                <button class="btn primary" type="submit">Save decision</button>
            </form>
        </section>
    </c:forEach>
</div>
<%@ include file="/WEB-INF/views/shared/app-end.jspf" %>
