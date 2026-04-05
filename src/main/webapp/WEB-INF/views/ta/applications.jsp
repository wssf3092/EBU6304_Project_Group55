<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ include file="/WEB-INF/views/shared/app-start.jspf" %>

<section class="panel">
    <div class="panel-head">
        <div>
            <h2>我的申请</h2>
        </div>
        <a class="btn secondary" href="${pageContext.request.contextPath}/ta/courses">浏览课程</a>
    </div>
    <c:choose>
        <c:when test="${empty applications}">
            <div class="empty-state">
                <h3>暂无申请记录</h3>
                <p class="cell-subtle"><a href="${pageContext.request.contextPath}/ta/courses">去选课申请</a></p>
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
                        <th>审核备注</th>
                    </tr>
                    </thead>
                    <tbody>
                    <c:forEach items="${applications}" var="app">
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
                            <td class="cell-subtle">
                                <c:if test="${not empty app.reviewNote}">
                                    <c:out value="${app.reviewNote}"/>
                                </c:if>
                                <c:if test="${empty app.reviewNote}">—</c:if>
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
