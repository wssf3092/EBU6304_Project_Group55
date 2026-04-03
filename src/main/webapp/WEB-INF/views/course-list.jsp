<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<!DOCTYPE html>
<html lang="zh-CN">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>课程列表 - TA 申请管理系统</title>
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

<div class="container">
    <h2 class="mb-3">可选课程</h2>
    <div style="display: grid; grid-template-columns: repeat(auto-fill, minmax(300px, 1fr)); gap: 1.5rem;">
        <c:choose>
            <c:when test="${empty courses}">
                <p class="text-muted">暂无可申请课程。</p>
            </c:when>
            <c:otherwise>
                <c:forEach var="course" items="${courses}">
                    <div class="card">
                        <h3 style="margin-bottom: 0.5rem;">${course.name}</h3>
                        <p class="text-muted mb-2">${course.description}</p>
                        <p class="mb-2"><strong>授课教师：</strong>${course.teacherName}</p>
                        <p class="mb-2"><strong>需要 TA：</strong>${course.taNeedCount} 人</p>
                        <a href="${pageContext.request.contextPath}/ta/courses/apply?courseId=${course.id}" class="btn btn-primary" style="text-align: center;">申请 TA</a>
                    </div>
                </c:forEach>
            </c:otherwise>
        </c:choose>
    </div>
</div>

<script src="${pageContext.request.contextPath}/static/js/main.js"></script>
</body>
</html>
