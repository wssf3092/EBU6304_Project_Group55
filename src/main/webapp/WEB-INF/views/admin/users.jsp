<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ include file="/WEB-INF/views/shared/app-start.jspf" %>

<section class="panel">
    <div class="panel-head">
        <div>
            <h2>用户列表</h2>
            <p class="cell-subtle">按角色排序；可启用或禁用账号（不能操作自己）</p>
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
                <th>操作</th>
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
                    <td>
                        <c:choose>
                            <c:when test="${u.userId == currentUser.userId}">
                                <span class="cell-subtle">当前账号</span>
                            </c:when>
                            <c:when test="${u.active}">
                                <form method="post" action="${pageContext.request.contextPath}/admin/users"
                                      data-confirm="确定禁用该用户？" style="display:inline;">
                                    <input type="hidden" name="userId" value="${u.userId}"/>
                                    <input type="hidden" name="active" value="false"/>
                                    <button type="submit" class="btn secondary">禁用</button>
                                </form>
                            </c:when>
                            <c:otherwise>
                                <form method="post" action="${pageContext.request.contextPath}/admin/users"
                                      style="display:inline;">
                                    <input type="hidden" name="userId" value="${u.userId}"/>
                                    <input type="hidden" name="active" value="true"/>
                                    <button type="submit" class="btn primary">启用</button>
                                </form>
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
