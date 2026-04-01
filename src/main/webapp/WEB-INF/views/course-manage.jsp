<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<!DOCTYPE html>
<html lang="zh-CN">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>管理课程申请 - TA 申请管理系统</title>
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
    <div class="card mb-3">
        <h2 class="mb-1">${course.name}</h2>
        <p class="text-muted mb-2">${course.description}</p>
        <c:set var="approvedCount" value="0"/>
        <c:forEach var="app" items="${applications}">
            <c:if test="${app.status == 'ACCEPTED'}">
                <c:set var="approvedCount" value="${approvedCount + 1}"/>
            </c:if>
        </c:forEach>
        <p>需要 TA 人数：<strong>${course.taNeedCount}</strong>，已通过：<strong>${approvedCount}</strong></p>
    </div>

    <c:if test="${not empty errorMessage}">
        <div class="alert alert-error">${errorMessage}</div>
    </c:if>
    <c:if test="${not empty successMessage}">
        <div class="alert alert-success">${successMessage}</div>
    </c:if>

    <div class="card">
        <h3 class="mb-2">申请列表</h3>
        <c:choose>
            <c:when test="${empty applications}">
                <p class="text-muted">暂无申请</p>
            </c:when>
            <c:otherwise>
                <table style="width: 100%; border-collapse: collapse;">
                    <thead>
                    <tr style="border-bottom: 2px solid var(--border-color); text-align: left;">
                        <th style="padding: 1rem 0;">学生用户名</th>
                        <th style="padding: 1rem 0;">申请时间</th>
                        <th style="padding: 1rem 0;">个人陈述</th>
                        <th style="padding: 1rem 0;">状态</th>
                        <th style="padding: 1rem 0;">操作</th>
                    </tr>
                    </thead>
                    <tbody>
                    <c:forEach var="app" items="${applications}">
                        <tr style="border-bottom: 1px solid var(--border-color); vertical-align: top;">
                            <td style="padding: 1rem 0;">${app.studentUsername}</td>
                            <td style="padding: 1rem 0;">${app.applyTime}</td>
                            <td style="padding: 1rem 0; max-width: 360px;">
                                <c:choose>
                                    <c:when test="${not empty app.statement && app.statement.length() > 80}">
                                        ${app.statement.substring(0, 80)}...
                                    </c:when>
                                    <c:otherwise>
                                        ${app.statement}
                                    </c:otherwise>
                                </c:choose>
                            </td>
                            <td style="padding: 1rem 0;">
                                <c:choose>
                                    <c:when test="${app.status == 'PENDING'}">
                                        <span class="status-badge status-pending">待审核</span>
                                    </c:when>
                                    <c:when test="${app.status == 'ACCEPTED'}">
                                        <span class="status-badge status-approved">已通过</span>
                                    </c:when>
                                    <c:otherwise>
                                        <span class="status-badge status-rejected">已拒绝</span>
                                    </c:otherwise>
                                </c:choose>
                            </td>
                            <td style="padding: 1rem 0;">
                                <c:if test="${app.status == 'PENDING'}">
                                    <form method="post" action="${pageContext.request.contextPath}/courses/manage"
                                          class="d-flex gap-1">
                                        <input type="hidden" name="courseId" value="${course.id}">
                                        <input type="hidden" name="applicationId" value="${app.applicationId}">
                                        <button type="submit" name="action" value="approve"
                                                class="btn btn-primary btn-sm" style="width: auto;">通过</button>
                                        <button type="submit" name="action" value="reject"
                                                class="btn btn-secondary btn-sm"
                                                onclick="return confirmAction('确认拒绝这位申请者？')">拒绝</button>
                                    </form>
                                </c:if>
                                <c:if test="${app.status != 'PENDING'}">
                                    <span class="text-muted">已处理</span>
                                </c:if>
                            </td>
                        </tr>
                    </c:forEach>
                    </tbody>
                </table>
            </c:otherwise>
        </c:choose>
    </div>

    <div class="mt-3">
        <a href="${pageContext.request.contextPath}/dashboard" class="btn btn-secondary">返回主控台</a>
    </div>
</div>

<script src="${pageContext.request.contextPath}/static/js/main.js"></script>
</body>
</html>

