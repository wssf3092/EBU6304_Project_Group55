<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ include file="/WEB-INF/views/shared/app-start.jspf" %>

<div class="metric-grid">
    <article class="metric-card">
        <span>申请总数</span>
        <strong><c:out value="${metrics.total}"/></strong>
    </article>
    <article class="metric-card">
        <span>待审核</span>
        <strong><c:out value="${metrics.pending}"/></strong>
    </article>
    <article class="metric-card">
        <span>已通过</span>
        <strong><c:out value="${metrics.accepted}"/></strong>
    </article>
    <article class="metric-card">
        <span>已拒绝</span>
        <strong><c:out value="${metrics.rejected}"/></strong>
    </article>
</div>

<section class="panel">
    <div class="panel-head">
        <div>
            <h2>最近申请</h2>
        </div>
        <a class="btn secondary" href="${pageContext.request.contextPath}/ta/applications">全部申请</a>
    </div>
    <c:choose>
        <c:when test="${empty recentApplications}">
            <div class="empty-state">
                <h3>暂无申请</h3>
                <p class="cell-subtle">去<a href="${pageContext.request.contextPath}/ta/courses">浏览课程</a>提交第一份申请。</p>
            </div>
        </c:when>
        <c:otherwise>
            <div class="table-wrap">
                <table class="data-table">
                    <thead>
                    <tr>
                        <th>课程</th>
                        <th>教师</th>
                        <th>申请时间</th>
                        <th>状态</th>
                    </tr>
                    </thead>
                    <tbody>
                    <c:forEach items="${recentApplications}" var="app">
                        <tr>
                            <td><strong><c:out value="${app.courseName}"/></strong></td>
                            <td><c:out value="${app.teacherName}"/></td>
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
                        </tr>
                    </c:forEach>
                    </tbody>
                </table>
            </div>
        </c:otherwise>
    </c:choose>
</section>

<%@ include file="/WEB-INF/views/shared/app-end.jspf" %>
