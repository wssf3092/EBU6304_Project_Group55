<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<!DOCTYPE html>
<html lang="zh-CN">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>发布新课程 - TA 申请管理系统</title>
    <link rel="stylesheet" href="${pageContext.request.contextPath}/static/css/common.css">
</head>
<body>
<nav class="navbar">
    <a href="${pageContext.request.contextPath}/dashboard" class="nav-brand">TA System</a>
    <div class="nav-links">
        <a href="${pageContext.request.contextPath}/dashboard" class="nav-link" data-page="dashboard">首页</a>
        <a href="${pageContext.request.contextPath}/courses" class="nav-link" data-page="courses">课程列表</a>
    </div>
    <div class="nav-user">
        <span class="user-badge">教师</span>
        <span>欢迎, ${sessionScope.user.name}</span>
        <a href="${pageContext.request.contextPath}/logout" class="btn btn-secondary btn-sm"
           onclick="return confirmAction('确定要注销登录吗？')">登出</a>
    </div>
</nav>

<div class="container">
    <div class="card" style="max-width: 720px; margin: 0 auto;">
        <h2 class="mb-2">发布新课程岗位</h2>
        <p class="text-muted mb-3">填写课程信息后提交，系统将创建新的 TA 招聘课程。</p>

        <c:if test="${not empty errorMessage}">
            <div class="alert alert-error">${errorMessage}</div>
        </c:if>

        <form method="post" action="${pageContext.request.contextPath}/courses/new">
            <div class="form-group">
                <label class="form-label" for="courseName">课程名称 <span style="color:#ef233c;">*</span></label>
                <input class="form-control" id="courseName" name="courseName"
                       value="${courseName}" required>
            </div>

            <div class="form-group">
                <label class="form-label" for="taNeedCount">所需 TA 人数 <span style="color:#ef233c;">*</span></label>
                <input class="form-control" id="taNeedCount" name="taNeedCount" type="number" min="1"
                       value="${taNeedCount}" required>
            </div>

            <div class="form-group">
                <label class="form-label" for="description">课程描述</label>
                <textarea class="form-control" id="description" name="description" rows="4"
                          style="resize: vertical;">${description}</textarea>
            </div>

            <div class="d-flex gap-2">
                <button type="submit" class="btn btn-primary" style="width: auto;">提交发布</button>
                <a href="${pageContext.request.contextPath}/dashboard" class="btn btn-secondary">取消并返回</a>
            </div>
        </form>
    </div>
</div>

<script src="${pageContext.request.contextPath}/static/js/main.js"></script>
</body>
</html>

