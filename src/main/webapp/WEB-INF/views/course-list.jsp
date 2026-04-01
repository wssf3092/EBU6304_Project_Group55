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
        <a href="${pageContext.request.contextPath}/dashboard" class="nav-brand">TA System</a>
        <div class="nav-links">
            <a href="${pageContext.request.contextPath}/dashboard" class="nav-link" data-page="dashboard">首页</a>
            <a href="${pageContext.request.contextPath}/courses" class="nav-link" data-page="courses">课程列表</a>
            <c:if test="${sessionScope.user.role == 'TA'}">
                <a href="${pageContext.request.contextPath}/applications" class="nav-link" data-page="applications">我的申请</a>
            </c:if>
        </div>
        <div class="nav-user">
            <span>欢迎, ${sessionScope.user.name}</span>
            <a href="${pageContext.request.contextPath}/logout" class="btn btn-secondary btn-sm" onclick="return confirmAction('确定要注销登录吗？')">登出</a>
        </div>
    </nav>

    <div class="container">
        <div class="mb-3">
            <h2>可申请岗位课程列表</h2>
            <p class="text-muted mt-1">浏览并申请您感兴趣的课程助教岗位。</p>
        </div>
        
        <c:if test="${not empty errorMessage}">
            <div class="alert alert-error">${errorMessage}</div>
        </c:if>

        <div style="display: grid; grid-template-columns: repeat(auto-fill, minmax(320px, 1fr)); gap: 1.5rem;">
            <c:forEach var="course" items="${courses}">
                <div class="card d-flex" style="flex-direction: column;">
                    <h3 style="font-size: 1.25rem; margin-bottom: 0.5rem; color: var(--primary-color)">${course.name}</h3>
                    <div class="text-muted" style="font-size: 0.9rem; margin-bottom: 1rem;">
                        <span>教师: <strong>${course.teacherName}</strong></span>
                        <span style="margin: 0 0.5rem;">|</span>
                        <span>需求: <strong>${course.taNeedCount}</strong> 人</span>
                    </div>
                    <p style="flex: 1; margin-bottom: 1.5rem; font-size: 0.95rem;">${course.description}</p>
                    
                    <c:if test="${sessionScope.user.role == 'TA'}">
                        <a href="${pageContext.request.contextPath}/apply?courseId=${course.id}" class="btn btn-primary" style="text-align: center;">申请 TA</a>
                    </c:if>
                    <c:if test="${sessionScope.user.role == 'MO'}">
                        <button class="btn btn-secondary" disabled style="opacity: 0.5; cursor: not-allowed; width: 100%;">教师不可申请</button>
                    </c:if>
                </div>
            </c:forEach>
            <c:if test="${empty courses}">
                <div style="grid-column: 1 / -1; text-align: center; padding: 3rem; background: var(--bg-card); border-radius: var(--border-radius);">
                    <p class="text-muted">目前没有开放申请的课程。</p>
                </div>
            </c:if>
        </div>
    </div>
    
    <script src="${pageContext.request.contextPath}/static/js/main.js"></script>
</body>
</html>
