<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ include file="/WEB-INF/views/shared/app-start.jspf" %>

<section class="panel">
    <div class="panel-head">
        <div>
            <h2>全部课程</h2>
            <p class="cell-subtle">只读浏览</p>
        </div>
    </div>
    <c:choose>
        <c:when test="${empty courses}">
            <div class="empty-state"><h3>系统中暂无课程</h3></div>
        </c:when>
        <c:otherwise>
            <div class="metric-grid" style="grid-template-columns: repeat(auto-fill, minmax(280px, 1fr));">
                <c:forEach var="course" items="${courses}">
                    <article class="panel" style="margin:0;">
                        <h3 style="margin-bottom:8px;"><c:out value="${course.name}"/></h3>
                        <p class="cell-subtle" style="margin-bottom:12px;"><c:out value="${course.description}"/></p>
                        <p>申请 <strong><c:out value="${course.applicantCount}"/></strong> / TA 需求 <strong><c:out value="${course.taNeedCount}"/></strong></p>
                    </article>
                </c:forEach>
            </div>
        </c:otherwise>
    </c:choose>
</section>

<%@ include file="/WEB-INF/views/shared/app-end.jspf" %>
