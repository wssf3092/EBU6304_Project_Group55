<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ include file="/WEB-INF/views/shared/app-start.jspf" %>

<section class="panel">
    <div class="panel-head">
        <h2>审核申请</h2>
        <c:url var="backApplicants" value="/mo/courses/applicants">
            <c:param name="courseId" value="${application.courseId}"/>
        </c:url>
        <a class="btn secondary" href="${backApplicants}">返回列表</a>
    </div>
    <p><strong>课程</strong>：<c:out value="${course.name}"/></p>
    <p><strong>申请人</strong>：<c:out value="${applicant != null ? applicant.name : application.applicantId}"/>
        (<c:out value="${application.applicantId}"/>)</p>
    <p><strong>申请时间</strong>：<c:out value="${application.applyDate}"/></p>
</section>

<section class="panel">
    <h3 style="margin-bottom:12px;">个人陈述</h3>
    <p style="white-space:pre-wrap;"><c:out value="${application.statement}"/></p>
</section>

<section class="panel">
    <h3 style="margin-bottom:12px;">审核决定</h3>
    <c:url var="reviewPost" value="/mo/applications/review"/>
    <form method="post" action="${reviewPost}" class="form-grid">
        <input type="hidden" name="applicationId" value="${application.applicationId}"/>
        <input type="hidden" name="courseId" value="${application.courseId}"/>
        <label class="field">
            <span>备注（可选）</span>
            <textarea name="note" rows="3" placeholder="写给申请人的说明，将随状态一并保存。"></textarea>
        </label>
        <div style="display:flex;gap:10px;flex-wrap:wrap;">
            <button class="btn primary" type="submit" name="action" value="accept">通过</button>
            <button class="btn secondary" type="submit" name="action" value="reject"
                    onclick="return confirm('确认拒绝该申请？');">拒绝</button>
        </div>
    </form>
</section>

<%@ include file="/WEB-INF/views/shared/app-end.jspf" %>
