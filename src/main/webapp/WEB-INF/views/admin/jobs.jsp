<%@ include file="/WEB-INF/views/shared/app-start.jspf" %>
<section class="panel">
    <div class="panel-head">
        <div>
            <h2>Jobs</h2>
        </div>
    </div>
    <div class="table-wrap">
        <table class="data-table">
            <thead>
            <tr>
                <th>Position</th>
                <th>Status</th>
                <th>Applications</th>
                <th>Accepted</th>
                <th>Remaining quota</th>
                <th>Action</th>
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
                    <td><c:out value="${item.acceptedCount}"/></td>
                    <td><c:out value="${item.job.remainingQuota}"/></td>
                    <td>
                        <form method="post" action="${pageContext.request.contextPath}/admin/jobs">
                            <input type="hidden" name="jobId" value="${item.job.jobId}"/>
                            <button class="btn danger" type="submit" ${item.job.closed ? 'disabled' : ''}>Force close</button>
                        </form>
                    </td>
                </tr>
            </c:forEach>
            </tbody>
        </table>
    </div>
</section>
<%@ include file="/WEB-INF/views/shared/app-end.jspf" %>
