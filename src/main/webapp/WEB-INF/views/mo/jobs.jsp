<%@ include file="/WEB-INF/views/shared/app-start.jspf" %>
<div class="two-column">
    <section class="panel">
        <div class="panel-head">
            <div>
                <h2>New Job</h2>
            </div>
        </div>
        <form method="post" action="${pageContext.request.contextPath}/mo/jobs" class="form-grid two">
            <label class="field">
                <span>Job title</span>
                <input type="text" name="title" required/>
            </label>
            <label class="field">
                <span>Module</span>
                <input type="text" name="module" required/>
            </label>
            <label class="field">
                <span>Activity type</span>
                <select name="activityType" required>
                    <option value="">Select activity</option>
                    <c:forEach items="${activityTypes}" var="item">
                        <option value="${item}"><c:out value="${item}"/></option>
                    </c:forEach>
                </select>
            </label>
            <label class="field">
                <span>Deadline</span>
                <input type="date" name="deadline" required/>
            </label>
            <label class="field">
                <span>Quota</span>
                <input type="number" min="1" max="20" name="quota" required/>
            </label>
            <label class="field">
                <span>Workload per week</span>
                <input type="number" min="1" max="20" name="workload" required/>
            </label>
            <label class="field span-two">
                <span>Required skills</span>
                <textarea name="requiredSkills" placeholder="Java, Communication, Marking" required></textarea>
            </label>
            <label class="field span-two">
                <span>Description</span>
                <textarea name="description" required></textarea>
            </label>
            <button class="btn primary wide span-two" type="submit">Create Position</button>
        </form>
    </section>

    <section class="panel">
        <div class="panel-head">
            <div>
                <h2>Jobs</h2>
            </div>
        </div>
        <div class="stack-list">
            <c:forEach items="${jobs}" var="item">
                <article class="surface-card">
                    <div class="surface-link-head">
                        <div>
                            <strong><c:out value="${item.job.title}"/></strong>
                            <span><c:out value="${item.job.module}"/> / <c:out value="${item.job.activityType}"/></span>
                        </div>
                        <span class="status-chip status-${item.job.status}"><c:out value="${item.job.status}"/></span>
                    </div>
                    <div class="inline-meta">
                        <span><c:out value="${item.applicationCount}"/> applications</span>
                        <span><c:out value="${item.acceptedCount}"/> accepted</span>
                        <span><c:out value="${item.job.remainingQuota}"/> seats left</span>
                    </div>
                    <div class="surface-actions">
                        <a class="btn secondary" href="${pageContext.request.contextPath}/mo/jobs/applicants?jobId=${item.job.jobId}">Review applicants</a>
                        <form method="post" action="${pageContext.request.contextPath}/mo/jobs">
                            <input type="hidden" name="action" value="close"/>
                            <input type="hidden" name="jobId" value="${item.job.jobId}"/>
                            <button class="btn danger" type="submit" ${item.job.closed ? 'disabled' : ''}>Close position</button>
                        </form>
                    </div>
                </article>
            </c:forEach>
        </div>
    </section>
</div>
<%@ include file="/WEB-INF/views/shared/app-end.jspf" %>
