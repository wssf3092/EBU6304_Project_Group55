<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ include file="/WEB-INF/views/shared/auth-start.jspf" %>
<div class="auth-card">
    <div class="card-head">
        <h2>登录</h2>
        <p class="cell-subtle">使用邮箱或用户 ID</p>
    </div>
    <%@ include file="/WEB-INF/views/shared/flash.jspf" %>
    <form method="post" action="${pageContext.request.contextPath}/auth/login" class="form-grid">
        <label class="field">
            <span>邮箱 / 用户 ID</span>
            <input type="text" name="username" value="<c:out value='${username}'/>" required autocomplete="username"/>
        </label>
        <label class="field">
            <span>密码</span>
            <input type="password" name="password" required autocomplete="current-password"/>
        </label>
        <button class="btn primary wide" type="submit">登录</button>
    </form>
    <div class="auth-footer">
        <a href="${pageContext.request.contextPath}/auth/register">注册新账户</a>
    </div>
</div>
<%@ include file="/WEB-INF/views/shared/auth-end.jspf" %>
