<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ include file="/WEB-INF/views/shared/app-start.jspf" %>

<c:if test="${not empty targetCourse}">
    <section class="panel">
        <h2 style="margin-bottom:8px;"><c:out value="${targetCourse.name}"/></h2>
        <p class="cell-subtle"><c:out value="${targetCourse.description}"/></p>
    </section>

    <section class="panel">
        <div class="panel-head">
            <h2>填写申请</h2>
        </div>
        <form method="post" action="${pageContext.request.contextPath}/ta/courses/apply" class="form-grid"
              data-confirm="确定要提交这份申请吗？提交后不可修改。">
            <input type="hidden" name="courseId" value="${courseId}"/>
            <label class="field">
                <span>个人陈述</span>
                <textarea name="statement" rows="6" required placeholder="请简述相关经历与申请动机。"></textarea>
            </label>
            <div style="display:flex;gap:10px;flex-wrap:wrap;">
                <button class="btn primary" type="submit">提交申请</button>
                <a class="btn secondary" href="${pageContext.request.contextPath}/ta/courses">取消</a>
            </div>
        </form>
    </section>
</c:if>
<c:if test="${empty targetCourse}">
    <section class="panel">
        <div class="empty-state">
            <h3>未找到课程</h3>
            <p class="cell-subtle">请从<a href="${pageContext.request.contextPath}/ta/courses">课程列表</a>进入。</p>
        </div>
    </section>
</c:if>

<%@ include file="/WEB-INF/views/shared/app-end.jspf" %>
