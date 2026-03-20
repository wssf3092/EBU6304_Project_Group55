<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<!DOCTYPE html>
<html>
<head>
    <meta charset="UTF-8">
    <title>TA 招聘系统</title>
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
</head>
<body>
<div style="max-width: 900px; margin: 40px auto; font-family: Arial, Helvetica, sans-serif;">
    <h1>TA 招聘系统（Group55）</h1>
    <p>这是一个用于课程 TA 申请与管理的 Web 系统原型。</p>

    <div style="margin-top: 20px;">
        <a href="<%=request.getContextPath()%>/login">登录</a>
        <span style="margin: 0 8px;">|</span>
        <a href="<%=request.getContextPath()%>/register">注册</a>
    </div>
</div>
</body>
</html>

