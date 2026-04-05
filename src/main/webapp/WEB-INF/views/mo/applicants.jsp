<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ include file="/WEB-INF/views/shared/app-start.jspf" %>

<section class="panel">
    <div class="panel-head">
        <div>
            <h2><c:out value="${course.name}"/></h2>
            <p class="cell-subtle"><c:out value="${course.description}"/></p>
        </div>
        <a class="btn secondary" href="${pageContext.request.contextPath}/mo/dashboard">返回仪表盘</a>
    </div>
</section>

<section class="panel">
    <div class="panel-head">
        <h2>申请人列表</h2>
    </div>
    <c:choose>
        <c:when test="${empty applications}">
            <div class="empty-state"><h3>暂无申请</h3></div>
        </c:when>
        <c:otherwise>
            <div class="table-wrap">
                <table class="data-table">
                    <thead>
                    <tr>
                        <th>申请人</th>
                        <th>用户 ID</th>
                        <th>申请时间</th>
                        <th>状态</th>
                        <th>操作</th>
                    </tr>
                    </thead>
                    <tbody>
                    <c:forEach var="app" items="${applications}">
                        <tr>
                            <td><strong><c:out value="${app.applicantName != null ? app.applicantName : app.applicantId}"/></strong></td>
                            <td><c:out value="${app.applicantId}"/></td>
                            <td><c:out value="${app.applyDate}"/></td>
                            <td>
                                <c:choose>
                                    <c:when test="${app.status == 'PENDING'}">
                                        <span class="status-chip status-pending">待审核</span>
                                    </c:when>
                                    <c:when test="${app.status == 'ACCEPTED'}">
                                        <span class="status-chip status-accepted">已通过</span>
                                    </c:when>
                                    <c:otherwise>
                                        <span class="status-chip status-rejected">已拒绝</span>
                                    </c:otherwise>
                                </c:choose>
                            </td>
                            <td>
                                <c:if test="${app.status == 'PENDING'}">
                                    <c:url var="reviewLink" value="/mo/applications/review">
                                        <c:param name="id" value="${app.applicationId}"/>
                                    </c:url>
                                    <a class="btn primary" style="display:inline-flex;" href="${reviewLink}">审核</a>
                                </c:if>
                                <c:if test="${app.status != 'PENDING' && not empty app.reviewNote}">
                                    <span class="cell-subtle"><c:out value="${app.reviewNote}"/></span>
                                </c:if>
                            </td>
                        </tr>
                    </c:forEach>
                    </tbody>
                </table>
            </div>
        </c:otherwise>
    </c:choose>
</section>

<%@ include file="/WEB-INF/views/shared/app-end.jspf" %>
