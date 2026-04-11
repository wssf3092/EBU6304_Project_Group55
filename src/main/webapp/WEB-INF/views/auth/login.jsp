<%@ include file="/WEB-INF/views/shared/auth-start.jspf" %>
<div class="auth-card">
    <div class="card-head">
        <h2>Sign In</h2>
    </div>
    <%@ include file="/WEB-INF/views/shared/flash.jspf" %>
    <form method="post" action="${pageContext.request.contextPath}/auth/login" class="form-grid">
        <label class="field">
            <span>Email</span>
            <input type="email" name="email" value="<c:out value='${email}'/>" required/>
        </label>
        <label class="field">
            <span>Password</span>
            <input type="password" name="password" required/>
        </label>
        <button class="btn primary wide" type="submit">Sign In</button>
    </form>
    <div class="auth-footer">
        <a href="${pageContext.request.contextPath}/auth/register">Create account</a>
    </div>
</div>
<%@ include file="/WEB-INF/views/shared/auth-end.jspf" %>
