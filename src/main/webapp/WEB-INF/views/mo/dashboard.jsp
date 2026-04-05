<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ include file="/WEB-INF/views/shared/app-start.jspf" %>

<section class="panel">
    <div class="panel-head">
        <div>
            <h2>我的课程</h2>
            <p class="cell-subtle">管理课程与 TA 申请</p>
        </div>
        <a class="btn primary" href="${pageContext.request.contextPath}/mo/courses/new">发布新课程</a>
    </div>
    <c:choose>
        <c:when test="${empty courses}">
            <div class="empty-state">
                <h3>尚未发布课程</h3>
                <p class="cell-subtle">点击右上角发布第一门课程。</p>
            </div>
        </c:when>
        <c:otherwise>
            <div class="metric-grid" style="grid-template-columns: repeat(auto-fill, minmax(280px, 1fr));">
                <c:forEach var="course" items="${courses}">
                    <article class="panel" style="margin:0;">
                        <h3 style="margin-bottom:8px;"><c:out value="${course.name}"/></h3>
                        <p class="cell-subtle" style="margin-bottom:12px;"><c:out value="${course.description}"/></p>
                        <p style="margin-bottom:16px;">
                            申请 <strong><c:out value="${course.applicantCount}"/></strong> / 名额 <strong><c:out value="${course.taNeedCount}"/></strong>
                        </p>
                        <a class="btn secondary wide" href="${pageContext.request.contextPath}/mo/courses/applicants?courseId=${course.id}">查看申请人</a>
                    </article>
                </c:forEach>
            </div>
        </c:otherwise>
    </c:choose>
</section>

<%@ include file="/WEB-INF/views/shared/app-end.jspf" %>
