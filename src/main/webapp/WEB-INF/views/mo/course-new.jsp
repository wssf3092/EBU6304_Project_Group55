<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ include file="/WEB-INF/views/shared/app-start.jspf" %>

<section class="panel">
    <div class="panel-head">
        <h2>发布新课程</h2>
    </div>
    <form method="post" action="${pageContext.request.contextPath}/mo/courses/new" class="form-grid">
        <label class="field">
            <span>课程名称</span>
            <input type="text" name="courseName" value="<c:out value='${courseName}'/>" required/>
        </label>
        <label class="field">
            <span>需要 TA 人数</span>
            <input type="number" name="taNeedCount" min="1" value="<c:out value='${taNeedCount}'/>" required/>
        </label>
        <label class="field">
            <span>课程描述</span>
            <textarea name="description" rows="4"><c:out value="${description}"/></textarea>
        </label>
        <div style="display:flex;gap:10px;flex-wrap:wrap;">
            <button class="btn primary" type="submit">保存</button>
            <a class="btn secondary" href="${pageContext.request.contextPath}/mo/dashboard">取消</a>
        </div>
    </form>
</section>

<%@ include file="/WEB-INF/views/shared/app-end.jspf" %>
