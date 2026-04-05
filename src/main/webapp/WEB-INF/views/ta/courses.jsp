<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ include file="/WEB-INF/views/shared/app-start.jspf" %>

<section class="panel">
    <div class="panel-head">
        <div>
            <h2>可选课程</h2>
            <p class="cell-subtle">选择课程并提交 TA 申请</p>
        </div>
    </div>
    <c:choose>
        <c:when test="${empty courses}">
            <div class="empty-state">
                <h3>暂无可选课程</h3>
            </div>
        </c:when>
        <c:otherwise>
            <div class="metric-grid" style="grid-template-columns: repeat(auto-fill, minmax(280px, 1fr));">
                <c:forEach var="course" items="${courses}">
                    <article class="panel" style="margin:0;">
                        <h3 style="margin-bottom:8px;"><c:out value="${course.name}"/></h3>
                        <p class="cell-subtle" style="margin-bottom:12px;"><c:out value="${course.description}"/></p>
                        <p style="margin-bottom:6px;"><strong>教师</strong>：<c:out value="${course.teacherName}"/></p>
                        <p style="margin-bottom:16px;"><strong>需要 TA</strong>：<c:out value="${course.taNeedCount}"/> 人</p>
                        <a class="btn primary wide" href="${pageContext.request.contextPath}/ta/courses/apply?courseId=${course.id}">申请</a>
                    </article>
                </c:forEach>
            </div>
        </c:otherwise>
    </c:choose>
</section>

<%@ include file="/WEB-INF/views/shared/app-end.jspf" %>
