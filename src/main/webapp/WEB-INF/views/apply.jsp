<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<!DOCTYPE html>
<html lang="zh-CN">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>提交申请 - TA 申请管理系统</title>
    <link rel="stylesheet" href="${pageContext.request.contextPath}/static/css/common.css">
</head>
<body>
<nav class="navbar">
    <a href="${pageContext.request.contextPath}${rolePrefix}/dashboard" class="nav-brand">TA System</a>
    <div class="nav-links">
        <a href="${pageContext.request.contextPath}/ta/dashboard" class="nav-link" data-page="dashboard">首页</a>
        <a href="${pageContext.request.contextPath}/ta/courses" class="nav-link" data-page="courses">课程列表</a>
        <a href="${pageContext.request.contextPath}/ta/dashboard" class="nav-link" data-page="applications">我的申请</a>
    </div>
    <div class="nav-user">
        <span class="user-badge">${currentUser.navBadge}</span>
        <span>欢迎, ${currentUser.name}</span>
        <a href="${pageContext.request.contextPath}/auth/logout" class="btn btn-secondary btn-sm"
           onclick="return confirmAction('确定要注销登录吗？')">登出</a>
    </div>
</nav>

<div class="container" style="max-width: 720px;">
    <h2 class="mb-3">课程 TA 申请</h2>

    <c:if test="${not empty errorMessage}">
        <div class="alert alert-error">${errorMessage}</div>
    </c:if>

    <c:if test="${not empty targetCourse}">
        <div class="card mb-3">
            <h3>${targetCourse.name}</h3>
            <p class="text-muted">${targetCourse.description}</p>
        </div>

        <form action="${pageContext.request.contextPath}/ta/courses/apply" method="post" id="applyForm"
              onsubmit="return validateAndConfirmSubmit('确定要提交这份申请吗？提交后不可修改。')">
            <input type="hidden" name="courseId" value="${courseId}">
            <div class="form-group">
                <label for="statement">个人陈述</label>
                <textarea id="statement" name="statement" class="form-control" rows="6" required
                          placeholder="请简述你的相关经历与申请动机。"></textarea>
            </div>
            <div class="d-flex gap-1">
                <button type="submit" class="btn btn-primary">提交申请</button>
                <a href="${pageContext.request.contextPath}/ta/courses" class="btn btn-secondary">取消</a>
            </div>
        </form>
    </c:if>
    <c:if test="${empty targetCourse}">
        <p class="text-muted">未找到课程，请从<a href="${pageContext.request.contextPath}/ta/courses">课程列表</a>进入。</p>
    </c:if>
</div>

<script src="${pageContext.request.contextPath}/static/js/main.js"></script>
</body>
</html>
