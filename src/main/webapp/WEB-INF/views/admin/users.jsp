<%@ include file="/WEB-INF/views/shared/app-start.jspf" %>
<section class="panel">
    <div class="panel-head">
        <div>
            <h2>Users</h2>
        </div>
    </div>
    <div class="table-wrap">
        <table class="data-table">
            <thead>
            <tr>
                <th>User</th>
                <th>Role</th>
                <th>Profile</th>
                <th>CV</th>
                <th>Accepted hours</th>
                <th>Status</th>
                <th>Action</th>
            </tr>
            </thead>
            <tbody>
            <c:forEach items="${users}" var="item">
                <tr>
                    <td>
                        <strong><c:out value="${item.user.name}"/></strong>
                        <div class="cell-subtle"><c:out value="${item.user.email}"/></div>
                    </td>
                    <td><c:out value="${item.user.roleLabel}"/></td>
                    <td>${item.hasProfile ? 'Ready' : 'Missing'}</td>
                    <td>${item.hasCv ? 'Uploaded' : 'Missing'}</td>
                    <td><c:out value="${item.acceptedHours}"/>h/week</td>
                    <td><span class="status-chip status-${item.user.active ? 'accepted' : 'rejected'}">${item.user.active ? 'active' : 'disabled'}</span></td>
                    <td>
                        <form method="post" action="${pageContext.request.contextPath}/admin/users">
                            <input type="hidden" name="userId" value="${item.user.userId}"/>
                            <input type="hidden" name="active" value="${item.user.active ? 'false' : 'true'}"/>
                            <button class="btn ${item.user.active ? 'danger' : 'success'}" type="submit">
                                ${item.user.active ? 'Disable' : 'Enable'}
                            </button>
                        </form>
                    </td>
                </tr>
            </c:forEach>
            </tbody>
        </table>
    </div>
</section>
<%@ include file="/WEB-INF/views/shared/app-end.jspf" %>
