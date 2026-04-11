<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<!DOCTYPE html>
<html lang="zh-CN">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>发布课程 - TA 申请管理系统</title>
    <link rel="stylesheet" href="${pageContext.request.contextPath}/static/css/common.css">
</head>
<body>
<nav class="navbar">
    <a href="${pageContext.request.contextPath}${rolePrefix}/dashboard" class="nav-brand">TA System</a>
    <div class="nav-links">
        <a href="${pageContext.request.contextPath}/mo/dashboard" class="nav-link" data-page="dashboard">首页</a>
        <a href="${pageContext.request.contextPath}/mo/dashboard" class="nav-link" data-page="courses">我的课程</a>
    </div>
    <div class="nav-user">
        <span class="user-badge">${currentUser.navBadge}</span>
        <span>欢迎, ${currentUser.name}</span>
        <a href="${pageContext.request.contextPath}/auth/logout" class="btn btn-secondary btn-sm"
           onclick="return confirmAction('确定要注销登录吗？')">登出</a>
    </div>
</nav>

<div class="container" style="max-width: 640px;">
    <h2 class="mb-3">发布新课程</h2>

    <c:if test="${not empty errorMessage}">
        <div class="alert alert-error">${errorMessage}</div>
    </c:if>

    <div class="card">
        <form method="post" action="${pageContext.request.contextPath}/mo/courses/new">
            <div class="form-group">
                <label for="courseName">课程名称</label>
                <input type="text" id="courseName" name="courseName" class="form-control" value="${courseName}" required>
            </div>
            <div class="form-group">
                <label for="taNeedCount">需要 TA 人数</label>
                <input type="number" id="taNeedCount" name="taNeedCount" class="form-control" min="1"
                       value="${taNeedCount}" required>
            </div>
            <div class="form-group">
                <label for="description">课程描述</label>
                <textarea id="description" name="description" class="form-control" rows="4">${description}</textarea>
            </div>
            <div class="d-flex gap-1">
                <button type="submit" class="btn btn-primary">保存</button>
                <a href="${pageContext.request.contextPath}/mo/dashboard" class="btn btn-secondary">取消并返回</a>
            </div>
        </form>
    </div>
</div>

<script src="${pageContext.request.contextPath}/static/js/main.js"></script>
</body>
</html>
