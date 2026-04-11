<%@ include file="/WEB-INF/views/shared/app-start.jspf" %>
<section class="panel">
    <div class="panel-head">
        <div>
            <h2>Applications</h2>
        </div>
    </div>
    <c:choose>
        <c:when test="${empty applications}">
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
                        <th>Review note</th>
                    </tr>
                    </thead>
                    <tbody>
                    <c:forEach items="${applications}" var="item">
                        <tr>
                            <td>
                                <strong><c:out value="${item.job.title}"/></strong>
                                <div class="cell-subtle"><c:out value="${item.job.module}"/> / <c:out value="${item.job.activityType}"/></div>
                            </td>
                            <td><c:out value="${item.application.displayAppliedAt}"/></td>
                            <td><span class="status-chip status-${item.application.status}"><c:out value="${item.application.status}"/></span></td>
                            <td>
                                <div><c:out value="${item.matchScore}"/>%</div>
                                <div class="cell-subtle">Matched: <c:forEach items="${item.matchedSkills}" var="skillItem" varStatus="state"><c:out value="${skillItem}"/><c:if test="${not state.last}">, </c:if></c:forEach></div>
                            </td>
                            <td><c:out value="${empty item.application.reviewNote ? 'None' : item.application.reviewNote}"/></td>
                        </tr>
                    </c:forEach>
                    </tbody>
                </table>
            </div>
        </c:otherwise>
    </c:choose>
</section>
<%@ include file="/WEB-INF/views/shared/app-end.jspf" %>
