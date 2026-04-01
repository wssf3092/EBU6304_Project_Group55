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

    <div class="auth-container">
        <div class="auth-card" style="max-width: 550px;">
            <h2>创建新账户</h2>
            
            <c:if test="${not empty errorMessage}">
                <div class="alert alert-error">
                    ${errorMessage}
                </div>
            </c:if>

            <form action="${pageContext.request.contextPath}/register" method="post" id="registerForm" onsubmit="return validateRegisterForm()">
                <div class="form-group">
                    <label for="role" class="form-label">注册角色</label>
                    <select id="role" name="role" class="form-control" required>
                        <option value="TA" ${role == 'TA' ? 'selected' : ''}>助教申请人 (TA)</option>
                        <option value="MO" ${role == 'MO' ? 'selected' : ''}>模块负责人 (MO)</option>
                    </select>
                </div>

                <div class="form-group">
                    <label for="name" class="form-label">真实姓名</label>
                    <input type="text" id="name" name="name" class="form-control" required placeholder="您的姓名" value="${name}">
                </div>
                
                <div class="form-group">
                    <label for="email" class="form-label">电子邮箱</label>
                    <input type="email" id="email" name="email" class="form-control" required placeholder="example@university.edu" value="${email}">
                </div>
                
                <div class="d-flex gap-2" style="margin-bottom: 0;">
                    <div class="form-group" style="flex: 1;">
                        <label for="password" class="form-label">密码</label>
                        <input type="password" id="password" name="password" class="form-control" required placeholder="设置密码">
                    </div>
                    
                    <div class="form-group" style="flex: 1;">
                        <label for="confirmPassword" class="form-label">确认密码</label>
                        <input type="password" id="confirmPassword" name="confirmPassword" class="form-control" required placeholder="再次输入密码">
                    </div>
                </div>
                
                <!-- Client-side error message placeholder -->
                <div id="jsErrorMsg" class="alert alert-error" style="display: none; padding: 0.5rem; margin-top: -0.5rem; margin-bottom: 1rem;"></div>
                
                <button type="submit" class="btn btn-primary mt-2">注 册</button>
            </form>
            
            <div class="auth-links">
                已有账户？ <a href="${pageContext.request.contextPath}/login">去登录</a>
            </div>
        </div>
    </div>

    <script>
        function validateRegisterForm() {
            var password = document.getElementById("password").value;
            var confirmPassword = document.getElementById("confirmPassword").value;
            var jsErrorMsg = document.getElementById("jsErrorMsg");
            
            if (password !== confirmPassword) {
                jsErrorMsg.innerText = "两次输入的密码不一致！";
                jsErrorMsg.style.display = "block";
                return false;
            }
            
            if (password.length < 6) {
                jsErrorMsg.innerText = "密码长度至少需要6位！";
                jsErrorMsg.style.display = "block";
                return false;
            }
            
            jsErrorMsg.style.display = "none";
            return true;
        }
    </script>
</body>
</html>
