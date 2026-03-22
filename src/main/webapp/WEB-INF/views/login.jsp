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

    <div class="auth-container">
        <div class="auth-card">
            <h2>欢迎登录</h2>
            
            <c:if test="${not empty errorMessage}">
                <div class="alert alert-error">
                    ${errorMessage}
                </div>
            </c:if>
            
            <c:if test="${not empty successMessage}">
                <div class="alert alert-success">
                    ${successMessage}
                </div>
            </c:if>

            <form action="${pageContext.request.contextPath}/login" method="post">
                <div class="form-group">
                    <label for="username" class="form-label">用户名</label>
                    <input type="text" id="username" name="username" class="form-control" required placeholder="请输入用户名" value="${username}">
                </div>
                
                <div class="form-group">
                    <label for="password" class="form-label">密码</label>
                    <input type="password" id="password" name="password" class="form-control" required placeholder="请输入密码">
                </div>
                
                <button type="submit" class="btn btn-primary mt-2">登 录</button>
            </form>
            
            <div class="auth-links">
                没有账户？ <a href="${pageContext.request.contextPath}/register">去注册</a>
            </div>
        </div>
    </div>
</body>
</html>
