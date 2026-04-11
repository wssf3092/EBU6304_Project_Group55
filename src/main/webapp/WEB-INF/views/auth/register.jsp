<%@ include file="/WEB-INF/views/shared/auth-start.jspf" %>
<div class="auth-card">
    <div class="card-head">
        <h2>Create Account</h2>
    </div>
    <%@ include file="/WEB-INF/views/shared/flash.jspf" %>
    <form method="post" action="${pageContext.request.contextPath}/auth/register" class="form-grid">
        <label class="field">
            <span>Full name</span>
            <input type="text" name="name" value="<c:out value='${name}'/>" required/>
        </label>
        <label class="field">
            <span>Email</span>
            <input type="email" name="email" value="<c:out value='${email}'/>" required/>
        </label>
        <label class="field">
            <span>Password</span>
            <input type="password" name="password" required/>
        </label>
        <label class="field">
            <span>Role</span>
            <select name="role" required>
                <option value="">Select role</option>
                <c:forEach items="${roles}" var="roleItem">
                    <option value="${roleItem}" ${role eq roleItem ? 'selected' : ''}>
                        <c:out value="${roleItem.label}"/>
                    </option>
                </c:forEach>
            </select>
        </label>
        <button class="btn primary wide" type="submit">Create Account</button>
    </form>
    <div class="auth-footer">
        <a href="${pageContext.request.contextPath}/auth/login">Back to sign in</a>
    </div>
</div>
<%@ include file="/WEB-INF/views/shared/auth-end.jspf" %>
