<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<!DOCTYPE html>
<html lang="zh-CN">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>登录 - TA 申请管理系统</title>
    <link rel="stylesheet" href="${pageContext.request.contextPath}/static/css/common.css">
</head>
<body>
    <nav class="navbar">
        <a href="${pageContext.request.contextPath}/" class="nav-brand">TA System</a>
    </nav>

    <div class="container" style="max-width: 480px; margin-top: 3rem;">
        <div class="card">
            <h2 class="mb-2">登录</h2>
            <p class="text-muted mb-3">使用邮箱或用户 ID 登录</p>

            <c:if test="${not empty errorMessage}">
                <div class="alert alert-error">${errorMessage}</div>
            </c:if>

            <form action="${pageContext.request.contextPath}/auth/login" method="post">
                <div class="form-group">
                    <label for="username">邮箱 / 用户 ID</label>
                    <input type="text" id="username" name="username" class="form-control" value="${username}" required>
                </div>
                <div class="form-group">
                    <label for="password">密码</label>
                    <input type="password" id="password" name="password" class="form-control" required>
                </div>
                <button type="submit" class="btn btn-primary" style="width: 100%;">登录</button>
            </form>

            <p class="mt-3 text-muted" style="text-align: center;">
                没有账户？ <a href="${pageContext.request.contextPath}/auth/register">去注册</a>
            </p>
        </div>
    </div>
</body>
</html>
