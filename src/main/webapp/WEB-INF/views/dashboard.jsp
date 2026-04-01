<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<!DOCTYPE html>
<html lang="zh-CN">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>主控台 - TA 申请管理系统</title>
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
            <span class="user-badge">${sessionScope.user.role == 'MO' ? 'MO' : sessionScope.user.role == 'ADMIN' ? '管理员' : 'TA'}</span>
            <span>欢迎, ${sessionScope.user.name}</span>
            <a href="${pageContext.request.contextPath}/logout" class="btn btn-secondary btn-sm" onclick="return confirmAction('确定要注销登录吗？')">登出</a>
        </div>
    </nav>

    <div class="container">
        <div class="d-flex justify-between align-center mb-3">
            <h2>我的工作台</h2>
        </div>

        <c:if test="${not empty errorMessage}">
            <div class="alert alert-error">${errorMessage}</div>
        </c:if>
        <c:if test="${not empty successMessage}">
            <div class="alert alert-success">${successMessage}</div>
        </c:if>

        <!-- TA View -->
        <c:if test="${sessionScope.user.role == 'TA'}">
            <div class="card mb-3">
                <h3 class="mb-2">我的申请记录</h3>
                <c:choose>
                    <c:when test="${empty applications}">
                        <p class="text-muted">暂无申请记录，快去<a href="${pageContext.request.contextPath}/courses">申请一门课程</a>吧！</p>
                    </c:when>
                    <c:otherwise>
                        <table style="width: 100%; border-collapse: collapse;">
                            <thead>
                                <tr style="border-bottom: 2px solid var(--border-color); text-align: left;">
                                    <th style="padding: 1rem 0;">课程名称</th>
                                    <th style="padding: 1rem 0;">授课教师</th>
                                    <th style="padding: 1rem 0;">申请时间</th>
                                    <th style="padding: 1rem 0;">状态</th>
                                </tr>
                            </thead>
                            <tbody>
                                <c:forEach var="app" items="${applications}">
                                    <tr style="border-bottom: 1px solid var(--border-color);">
                                        <td style="padding: 1rem 0; font-weight: 500;">${app.courseName}</td>
                                        <td style="padding: 1rem 0;">${app.teacherName}</td>
                                        <td style="padding: 1rem 0; color: var(--text-secondary);">${app.applyDate}</td>
                                        <td style="padding: 1rem 0;">
                                            <c:choose>
                                                <c:when test="${app.status == 'PENDING'}">
                                                    <span class="status-badge status-pending">待审核</span>
                                                </c:when>
                                                <c:when test="${app.status == 'ACCEPTED'}">
                                                    <span class="status-badge status-approved">已通过</span>
                                                </c:when>
                                                <c:when test="${app.status == 'REJECTED'}">
                                                    <span class="status-badge status-rejected">已拒绝</span>
                                                </c:when>
                                            </c:choose>
                                        </td>
                                    </tr>
                                </c:forEach>
                            </tbody>
                        </table>
                    </c:otherwise>
                </c:choose>
            </div>
        </c:if>

        <!-- MO / Admin course overview -->
        <c:if test="${sessionScope.user.role == 'MO'}">
            <div class="card mb-3">
                <div class="d-flex justify-between align-center mb-2">
                    <h3>我创建的课程</h3>
                    <a href="${pageContext.request.contextPath}/courses/new" class="btn btn-primary btn-sm" style="width: auto;">发布新课程</a>
                </div>
                <c:choose>
                    <c:when test="${empty courses}">
                        <p class="text-muted">您还未发布任何课程。</p>
                    </c:when>
                    <c:otherwise>
                        <div style="display: grid; grid-template-columns: repeat(auto-fill, minmax(300px, 1fr)); gap: 1.5rem;">
                            <c:forEach var="course" items="${courses}">
                                <div class="card" style="border: 1px solid var(--border-color); padding: 1.5rem;">
                                    <h4 style="margin-bottom: 0.5rem;">${course.name}</h4>
                                    <p class="text-muted mb-2">${course.description}</p>
                                    <div class="d-flex justify-between align-center mt-2">
                                        <span style="font-size: 0.9rem;">
                                            申请人数: <strong>${course.applicantCount}</strong> / ${course.taNeedCount}
                                        </span>
                                        <a href="${pageContext.request.contextPath}/courses/manage?id=${course.id}" class="btn btn-secondary btn-sm">管理申请</a>
                                    </div>
                                </div>
                            </c:forEach>
                        </div>
                    </c:otherwise>
                </c:choose>
            </div>
        </c:if>

        <c:if test="${sessionScope.user.role == 'ADMIN'}">
            <div class="card mb-3">
                <div class="d-flex justify-between align-center mb-2">
                    <h3>全部课程</h3>
                </div>
                <c:choose>
                    <c:when test="${empty courses}">
                        <p class="text-muted">系统中暂无课程。</p>
                    </c:when>
                    <c:otherwise>
                        <div style="display: grid; grid-template-columns: repeat(auto-fill, minmax(300px, 1fr)); gap: 1.5rem;">
                            <c:forEach var="course" items="${courses}">
                                <div class="card" style="border: 1px solid var(--border-color); padding: 1.5rem;">
                                    <h4 style="margin-bottom: 0.5rem;">${course.name}</h4>
                                    <p class="text-muted mb-2">${course.description}</p>
                                    <div class="d-flex justify-between align-center mt-2">
                                        <span style="font-size: 0.9rem;">
                                            申请人数: <strong>${course.applicantCount}</strong> / ${course.taNeedCount}
                                        </span>
                                        <span class="text-muted" style="font-size:0.85rem;">仅查看</span>
                                    </div>
                                </div>
                            </c:forEach>
                        </div>
                    </c:otherwise>
                </c:choose>
            </div>
        </c:if>
    </div>

    <script src="${pageContext.request.contextPath}/static/js/main.js"></script>
</body>
</html>
