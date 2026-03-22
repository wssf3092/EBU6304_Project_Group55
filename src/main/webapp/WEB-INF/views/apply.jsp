<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<!DOCTYPE html>
<html lang="zh-CN">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>提交申请 - TA 申请管理系统</title>
    <link rel="stylesheet" href="${pageContext.request.contextPath}/static/css/common.css">
</head>
<body>
    <nav class="navbar">
        <a href="${pageContext.request.contextPath}/dashboard" class="nav-brand">TA System</a>
        <div class="nav-links">
            <a href="${pageContext.request.contextPath}/dashboard" class="nav-link" data-page="dashboard">首页</a>
            <a href="${pageContext.request.contextPath}/courses" class="nav-link" data-page="courses">课程列表</a>
            <a href="${pageContext.request.contextPath}/applications" class="nav-link" data-page="applications">我的申请</a>
        </div>
        <div class="nav-user">
            <span>欢迎, ${sessionScope.user.name}</span>
            <a href="${pageContext.request.contextPath}/logout" class="btn btn-secondary btn-sm" onclick="return confirmAction('确定要注销登录吗？')">登出</a>
        </div>
    </nav>

    <div class="container d-flex justify-center" style="max-width: 800px; padding-top: 3rem;">
        <div class="card" style="width: 100%;">
            <div class="mb-3" style="border-bottom: 1px solid var(--border-color); padding-bottom: 1.5rem;">
                <h2>申请助教岗位</h2>
                <div class="mt-2" style="background: var(--bg-color); padding: 1rem; border-radius: var(--border-radius);">
                    <h4 style="color: var(--primary-color); margin-bottom: 0.5rem;">${targetCourse.name}</h4>
                    <p class="text-muted" style="font-size: 0.9rem;">授课教师: ${targetCourse.teacherName} | 需求人数: ${targetCourse.taNeedCount}</p>
                </div>
            </div>

            <c:if test="${not empty errorMessage}">
                <div class="alert alert-error">${errorMessage}</div>
            </c:if>

            <form action="${pageContext.request.contextPath}/apply" method="post" id="applyForm" onsubmit="return validateAndConfirmSubmit('确定要提交这份申请吗？提交后不可修改。')">
                <input type="hidden" name="courseId" value="${targetCourse.id}">
                
                <div class="form-group">
                    <label for="statement" class="form-label">个人陈述 (Personal Statement)</label>
                    <p class="text-muted mb-1" style="font-size: 0.85rem;">请说明你为什么适合担任这门课程的助教，包括你的相关成绩或经验。</p>
                    <textarea id="statement" name="statement" class="form-control" rows="8" required placeholder="在此输入你的申请理由..."></textarea>
                    <div id="statementError" class="text-muted" style="color: var(--error-color); font-size: 0.85rem; display: none; margin-top: 0.5rem;">
                        非常抱歉，个人陈述不能少于 50 个字符。
                    </div>
                </div>
                
                <div class="d-flex justify-between align-center mt-3">
                    <a href="${pageContext.request.contextPath}/courses" class="btn btn-secondary">取消</a>
                    <button type="submit" class="btn btn-primary" style="width: auto;">提交申请</button>
                </div>
            </form>
        </div>
    </div>
    
    <script src="${pageContext.request.contextPath}/static/js/main.js"></script>
    <script>
        document.getElementById('applyForm').addEventListener('submit', function(e) {
            var statement = document.getElementById('statement').value.trim();
            if(statement.length < 50) {
                e.preventDefault();
                document.getElementById('statementError').style.display = 'block';
            } else {
                document.getElementById('statementError').style.display = 'none';
            }
        });
    </script>
</body>
</html>
