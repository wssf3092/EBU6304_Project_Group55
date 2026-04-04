<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ include file="/WEB-INF/views/shared/auth-start.jspf" %>
<div class="auth-card">
    <div class="card-head">
        <h2>注册</h2>
    </div>
    <%@ include file="/WEB-INF/views/shared/flash.jspf" %>
    <form method="post" action="${pageContext.request.contextPath}/auth/register" class="form-grid" id="registerForm">
        <label class="field">
            <span>姓名</span>
            <input type="text" name="name" value="<c:out value='${name}'/>" required/>
        </label>
        <label class="field">
            <span>邮箱</span>
            <input type="email" name="email" value="<c:out value='${email}'/>" required/>
        </label>
        <label class="field">
            <span>角色</span>
            <select name="role">
                <option value="TA" <c:if test="${role == 'TA'}">selected</c:if>>TA（学生）</option>
                <option value="MO" <c:if test="${role == 'MO'}">selected</c:if>>MO（教师）</option>
                <option value="ADMIN" <c:if test="${role == 'ADMIN'}">selected</c:if>>管理员</option>
            </select>
        </label>
        <label class="field">
            <span>密码</span>
            <input type="password" name="password" required minlength="6"/>
        </label>
        <label class="field">
            <span>确认密码</span>
            <input type="password" name="confirmPassword" required/>
        </label>
        <button class="btn primary wide" type="submit">注册</button>
    </form>
    <div class="auth-footer">
        <a href="${pageContext.request.contextPath}/auth/login">已有账户？去登录</a>
    </div>
</div>
<script>
    document.getElementById("registerForm").addEventListener("submit", function (ev) {
        var p = this.password.value;
        var c = this.confirmPassword.value;
        if (p !== c) {
            ev.preventDefault();
            alert("两次输入的密码不一致");
        }
    });
</script>
<%@ include file="/WEB-INF/views/shared/auth-end.jspf" %>
