<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ include file="/WEB-INF/views/shared/app-start.jspf" %>

<section class="panel">
    <h2 style="margin-bottom:8px;"><c:out value="${course.name}"/></h2>
    <p class="cell-subtle" style="margin-bottom:12px;"><c:out value="${course.description}"/></p>
    <c:set var="approvedCount" value="0"/>
    <c:forEach var="a" items="${applications}">
        <c:if test="${a.status == 'ACCEPTED'}">
            <c:set var="approvedCount" value="${approvedCount + 1}"/>
        </c:if>
    </c:forEach>
    <p>需要 TA：<strong><c:out value="${course.taNeedCount}"/></strong>，已通过：<strong><c:out value="${approvedCount}"/></strong></p>
</section>

<section class="panel">
    <div class="panel-head">
        <h2>申请列表</h2>
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
                        <th>申请时间</th>
                        <th>陈述</th>
                        <th>状态</th>
                        <th>操作</th>
                    </tr>
                    </thead>
                    <tbody>
                    <c:forEach var="app" items="${applications}">
                        <tr>
                            <td><c:out value="${app.studentUsername}"/></td>
                            <td><c:out value="${app.applyTime}"/></td>
                            <td>
                                <c:choose>
                                    <c:when test="${not empty app.statement && app.statement.length() > 80}">
                                        <c:out value="${app.statement.substring(0, 80)}"/>…
                                    </c:when>
                                    <c:otherwise>
                                        <c:out value="${app.statement}"/>
                                    </c:otherwise>
                                </c:choose>
                            </td>
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
                                    <form method="post" action="${pageContext.request.contextPath}/mo/courses/manage" class="form-inline-block"
                                          style="display:flex;gap:8px;flex-wrap:wrap;">
                                        <input type="hidden" name="courseId" value="${course.id}"/>
                                        <input type="hidden" name="applicationId" value="${app.applicationId}"/>
                                        <button class="btn primary" type="submit" name="action" value="accept">通过</button>
                                        <button class="btn secondary" type="submit" name="action" value="reject"
                                                onclick="return confirm('确认拒绝该申请？');">拒绝</button>
                                    </form>
                                </c:if>
                                <c:if test="${app.status != 'PENDING'}">
                                    <span class="cell-subtle">已处理</span>
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

<section class="panel">
    <a class="btn secondary" href="${pageContext.request.contextPath}/mo/dashboard">返回仪表盘</a>
</section>

<%@ include file="/WEB-INF/views/shared/app-end.jspf" %>
