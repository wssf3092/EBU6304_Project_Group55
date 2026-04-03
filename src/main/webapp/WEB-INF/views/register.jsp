<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<!DOCTYPE html>
<html lang="zh-CN">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>注册 - TA 申请管理系统</title>
    <link rel="stylesheet" href="${pageContext.request.contextPath}/static/css/common.css">
</head>
<body>
    <nav class="navbar">
        <a href="${pageContext.request.contextPath}/" class="nav-brand">TA System</a>
    </nav>

    <div class="container" style="max-width: 520px; margin-top: 2rem;">
        <div class="card">
            <h2 class="mb-2">注册</h2>

            <c:if test="${not empty errorMessage}">
                <div class="alert alert-error">${errorMessage}</div>
            </c:if>

            <form action="${pageContext.request.contextPath}/auth/register" method="post" id="registerForm" onsubmit="return validateRegisterForm()">
                <div class="form-group">
                    <label for="name">姓名</label>
                    <input type="text" id="name" name="name" class="form-control" value="${name}" required>
                </div>
                <div class="form-group">
                    <label for="email">邮箱</label>
                    <input type="email" id="email" name="email" class="form-control" value="${email}" required>
                </div>
                <div class="form-group">
                    <label for="role">角色</label>
                    <select id="role" name="role" class="form-control">
                        <option value="TA" ${role == 'TA' ? 'selected' : ''}>TA（学生）</option>
                        <option value="MO" ${role == 'MO' ? 'selected' : ''}>MO（教师）</option>
                        <option value="ADMIN" ${role == 'ADMIN' ? 'selected' : ''}>管理员</option>
                    </select>
                </div>
                <div class="form-group">
                    <label for="password">密码</label>
                    <input type="password" id="password" name="password" class="form-control" required minlength="6">
                </div>
                <div class="form-group">
                    <label for="confirmPassword">确认密码</label>
                    <input type="password" id="confirmPassword" name="confirmPassword" class="form-control" required>
                </div>
                <button type="submit" class="btn btn-primary" style="width: 100%;">注册</button>
            </form>

            <p class="mt-3 text-muted" style="text-align: center;">
                已有账户？ <a href="${pageContext.request.contextPath}/auth/login">去登录</a>
            </p>
        </div>
    </div>

    <script>
        function validateRegisterForm() {
            var p = document.getElementById('password').value;
            var c = document.getElementById('confirmPassword').value;
            if (p !== c) {
                alert('两次输入的密码不一致');
                return false;
            }
            return true;
        }
    </script>
</body>
</html>
