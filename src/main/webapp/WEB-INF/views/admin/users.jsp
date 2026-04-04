<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ include file="/WEB-INF/views/shared/app-start.jspf" %>

<section class="panel">
    <div class="panel-head">
        <div>
            <h2>用户列表</h2>
            <p class="cell-subtle">只读查看账号（Step 6 可扩展启用/禁用）</p>
        </div>
    </div>
    <div class="table-wrap">
        <table class="data-table">
            <thead>
            <tr>
                <th>用户 ID</th>
                <th>姓名</th>
                <th>邮箱</th>
                <th>角色</th>
                <th>状态</th>
            </tr>
            </thead>
            <tbody>
            <c:forEach items="${users}" var="u">
                <tr>
                    <td><c:out value="${u.userId}"/></td>
                    <td><c:out value="${u.name}"/></td>
                    <td><c:out value="${u.email}"/></td>
                    <td><c:out value="${u.navBadge}"/></td>
                    <td>
                        <c:choose>
                            <c:when test="${u.active}">
                                <span class="status-chip status-accepted">正常</span>
                            </c:when>
                            <c:otherwise>
                                <span class="status-chip status-rejected">禁用</span>
                            </c:otherwise>
                        </c:choose>
                    </td>
                </tr>
            </c:forEach>
            </tbody>
        </table>
    </div>
</section>

<%@ include file="/WEB-INF/views/shared/app-end.jspf" %>
