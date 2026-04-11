# 开发任务说明书：角色登录控制 + MO 发布岗位与选人

> **适用版本**：基于 `develop` 分支当前状态（已含 Controller / JSP / Filter 骨架，缺少 Model / DAO 实现层）  
> **开发顺序**：开发者 A → 开发者 B（B 在 A 合并后才开始）

---

## 背景与问题概述

当前代码库存在以下关键缺陷，需通过本次迭代修复并新增功能：

| # | 问题 | 影响 |
|---|------|------|
| 1 | `model/` 和 `dao/` 包为空，Servlet 通过反射调用 DAO 全部会失败 | 登录、注册、申请均无法运行 |
| 2 | Session key 不一致：`LoginServlet` 存 `currentUser`，但 Filter 和 JSP 读 `user` | 登录后仍被判定为未登录 |
| 3 | URL 路由不匹配：JSP 链接 `/courses`、`/apply`，`web.xml` 注册的是 `/course/list`、`/application` | 点击导航 404 |
| 4 | `AuthenticationFilter` 只检查是否登录，不检查角色 | Teacher 可以提交申请，Student 可以访问管理页 |
| 5 | MO 无法发布岗位、无法查看申请者、无法选人 | 核心业务场景缺失 |

---

## 开发者 A：Model/DAO 基础层 + 角色登录控制

### 分支命名

```
feature/<你的名字>/model-dao-role-login
```

从 `develop` 签出。完成后发起 PR 合并回 `develop`，B 等 PR 合并后再开始。

---

### 任务 1：实现 Model 层

**新建文件**（路径均在 `src/main/java/com/group55/ta/model/`）：

#### `User.java`

```java
package com.group55.ta.model;

public class User {
    public enum Role { STUDENT, TEACHER, ADMIN }

    private String username;
    private String password;
    private String email;
    private String fullName;
    private Role role;

    // 无参构造器 + 全参构造器 + 所有字段的 getter/setter
}
```

与 `data/users.txt` 格式对齐：`username,password,email,fullName,role`

---

#### `Course.java`

```java
package com.group55.ta.model;

public class Course {
    private String id;            // 如 "CS101"
    private String name;
    private String teacherUsername;
    private int taNeedCount;
    private String description;

    // 无参构造器 + 全参构造器 + 所有字段的 getter/setter
}
```

与 `data/courses.txt` 格式对齐：`courseId,courseName,teacherUsername,taRequired,description`

---

#### `Application.java`

```java
package com.group55.ta.model;

public class Application {
    public enum Status { PENDING, APPROVED, REJECTED }

    private String applicationId;
    private String studentUsername;
    private String courseId;
    private Status status;
    private String applyTime;
    private String statement;

    // 无参构造器 + 全参构造器 + 所有字段的 getter/setter
}
```

与 `data/applications.txt` 格式对齐：`applicationId,studentUsername,courseId,status,applyTime,statement`

---

### 任务 2：实现工具类与 DAO 层

**新建文件**（路径在 `src/main/java/com/group55/ta/`）：

#### `util/FileStorageUtil.java`

实现 4 个方法：

```java
public static String readFile(String filename)            // 读全文
public static boolean writeFile(String filename, String content) // 覆盖写
public static boolean appendToFile(String filename, String line) // 追加一行
public static List<String> readLines(String filename)    // 按行读，忽略空行和 # 注释行
```

- `filename` 是相对路径，拼接到 `System.getProperty("catalina.base") + "/webapps/ta-recruitment/data/"` 或 `System.getProperty("user.dir") + "/data/"`（选其一，写成常量 `DATA_DIR`）。
- 所有方法捕获 `IOException` 后打印堆栈并返回 `null` / `false`，**不向外抛出**。

---

#### `dao/UserDao.java`

```java
public class UserDao {
    // 可选：支持自定义文件路径（用于测试）
    public UserDao() { }
    public UserDao(String filePath) { }

    public User authenticate(String username, String password) // 找到且密码匹配则返回 User，否则 null
    public User findByUsername(String username)
    public void save(User user)                  // 追加写入，若用户名已存在则不写
    public List<User> findAll()
    public void clearAll()                       // 仅测试使用，清空文件
}
```

CSV 解析：按逗号分割，跳过 `#` 开头的注释行。

---

#### `dao/CourseDao.java`

```java
public class CourseDao {
    public CourseDao() { }
    public CourseDao(String filePath) { }

    public List<Course> findAll()
    public Course findById(String courseId)
    public List<Course> findByTeacher(String teacherUsername) // B 需要此方法
    public void save(Course course)                           // B 需要此方法
    public void clearAll()
}
```

---

#### `dao/ApplicationDao.java`

```java
public class ApplicationDao {
    public ApplicationDao() { }

    public void save(Application application)
    public List<Application> findByStudentUsername(String username)
    public List<Application> findByCourseId(String courseId)  // B 需要此方法
    public Application findById(String applicationId)          // B 需要此方法
    public void updateStatus(String applicationId, Application.Status status) // B 需要此方法
    public List<Application> findAll()
}
```

`updateStatus` 的实现方式：把 `applications.txt` 全行读出，找到对应 `applicationId` 的那行，替换 `status` 字段后整体覆写文件。

---

### 任务 3：修复 Session Key 不一致问题

**修改文件**：`src/main/java/com/group55/ta/controller/LoginServlet.java`

将：
```java
session.setAttribute("currentUser", user);
```
改为：
```java
session.setAttribute("user", user);
```

确认 `AuthenticationFilter` 读的是 `session.getAttribute("user")`（已正确），JSP 用的是 `${sessionScope.user.xxx}`（已正确）。

---

### 任务 4：修复 URL 路由不匹配

**修改文件**：`src/main/webapp/WEB-INF/web.xml`

在已有的 Servlet 注册基础上，**补充以下 mapping**（Servlet 类不变，只加新的 `<servlet-mapping>`）：

```xml
<!-- 课程列表：JSP 和导航栏用 /courses，需额外注册 -->
<servlet-mapping>
    <servlet-name>CourseListServlet</servlet-name>
    <url-pattern>/courses</url-pattern>
</servlet-mapping>

<!-- 申请：JSP form action 用 /apply -->
<servlet-mapping>
    <servlet-name>ApplicationServlet</servlet-name>
    <url-pattern>/apply</url-pattern>
</servlet-mapping>
```

同时把 `AuthenticationFilter` 的 `url-pattern` 也补充 `/courses` 和 `/apply`：

```xml
<filter-mapping>
    <filter-name>AuthenticationFilter</filter-name>
    <url-pattern>/courses</url-pattern>
</filter-mapping>
<filter-mapping>
    <filter-name>AuthenticationFilter</filter-name>
    <url-pattern>/apply</url-pattern>
</filter-mapping>
```

---

### 任务 5：角色登录控制

#### 5a. 登录后按角色跳转

**修改文件**：`src/main/java/com/group55/ta/controller/LoginServlet.java`

登录成功后，根据用户角色跳转到不同页面：

```java
String role = user.getRole().name(); // "STUDENT" | "TEACHER" | "ADMIN"
if ("TEACHER".equals(role)) {
    response.sendRedirect(request.getContextPath() + "/dashboard");
} else if ("ADMIN".equals(role)) {
    response.sendRedirect(request.getContextPath() + "/dashboard");
} else {
    response.sendRedirect(request.getContextPath() + "/dashboard");
}
```

> 目前三个角色均跳转 `/dashboard`（dashboard.jsp 内部已按角色展示不同内容）。若后续需要独立页面，B 阶段再拆分。

#### 5b. 角色权限拦截

**修改文件**：`src/main/java/com/group55/ta/filter/AuthenticationFilter.java`

在已有的"是否登录"检查之后，**加入角色权限检查**：

```java
// 在 loggedIn 为 true 之后继续检查
User user = (User) session.getAttribute("user");
String role = (user != null && user.getRole() != null) ? user.getRole().name() : "";

// 教师专用路径：只有 TEACHER 可访问
if (path.startsWith("/courses/manage") || path.startsWith("/courses/new")) {
    if (!"TEACHER".equals(role)) {
        res.sendRedirect(req.getContextPath() + "/dashboard");
        return;
    }
}

// 学生专用路径：只有 STUDENT 可申请
if (path.equals("/apply") || path.startsWith("/apply")) {
    if (!"STUDENT".equals(role)) {
        res.sendRedirect(req.getContextPath() + "/dashboard");
        return;
    }
}
```

> **注意**：`AuthenticationFilter` 需要从 `session.getAttribute("user")` 直接强转为 `User`（不再用反射），前提是 Model 层已就绪（任务 1 完成）。

---

### A 的交付物清单

| 文件 | 状态 |
|------|------|
| `model/User.java` | 新建 |
| `model/Course.java` | 新建 |
| `model/Application.java` | 新建 |
| `util/FileStorageUtil.java` | 新建（替换已删除的占位版） |
| `dao/UserDao.java` | 新建 |
| `dao/CourseDao.java` | 新建（含 `findByTeacher`、`save`） |
| `dao/ApplicationDao.java` | 新建（含 `findByCourseId`、`updateStatus`） |
| `controller/LoginServlet.java` | 修改（session key、角色跳转） |
| `filter/AuthenticationFilter.java` | 修改（强转 User，加角色拦截） |
| `webapp/WEB-INF/web.xml` | 修改（补 `/courses`、`/apply` mapping） |

A 完成并合并 PR 后，通知 B 可以开始。

---

---

## 开发者 B：MO 发布岗位 + 选人功能

### 前提条件

- A 的 PR 已合并进 `develop`
- `CourseDao.findByTeacher()`、`CourseDao.save()`、`ApplicationDao.findByCourseId()`、`ApplicationDao.updateStatus()` 均已可用

### 分支命名

```
feature/<你的名字>/mo-post-and-select
```

从合并后的 `develop` 签出。

---

### 任务 1：MO 发布新岗位

#### 1a. 新建 Servlet

**新建文件**：`src/main/java/com/group55/ta/controller/CourseCreateServlet.java`

| HTTP 方法 | 行为 |
|-----------|------|
| GET | 转发到 `course-create.jsp`，显示空表单 |
| POST | 读取表单参数，创建 `Course` 对象，调用 `CourseDao.save()`，重定向到 `/dashboard` |

POST 参数：
- `courseName`（必填）
- `taNeedCount`（必填，整数，≥1）
- `description`（选填）

`Course.id` 生成规则：取当前毫秒时间戳前 8 位，如 `UUID.randomUUID().toString().substring(0, 8).toUpperCase()`。  
`teacherUsername` 从 `session.getAttribute("user")` 取得当前登录教师的用户名。

错误处理：
- 必填项为空 → 设 `errorMessage` 属性，forward 回表单页面，不丢失已填内容
- `taNeedCount` 不是正整数 → 同上

#### 1b. 注册 Servlet

**修改文件**：`src/main/webapp/WEB-INF/web.xml`，添加：

```xml
<servlet>
    <servlet-name>CourseCreateServlet</servlet-name>
    <servlet-class>com.group55.ta.controller.CourseCreateServlet</servlet-class>
</servlet>
<servlet-mapping>
    <servlet-name>CourseCreateServlet</servlet-name>
    <url-pattern>/courses/new</url-pattern>
</servlet-mapping>
```

> A 已在 `AuthenticationFilter` 中对 `/courses/new` 加了 TEACHER-only 拦截，无需 B 重复添加。

#### 1c. 新建 JSP

**新建文件**：`src/main/webapp/WEB-INF/views/course-create.jsp`

页面内容：
- 沿用 `common.css` 样式
- 导航栏与 `dashboard.jsp` 相同（直接复制导航区域代码）
- 表单字段：课程名称、所需 TA 人数、课程描述
- 提交按钮、取消按钮（返回 `/dashboard`）
- 显示 `${errorMessage}` 的错误提示区域
- 表单 action 为 `${pageContext.request.contextPath}/courses/new`，method 为 POST

---

### 任务 2：MO 查看申请者与选人

#### 2a. 新建 Servlet

**新建文件**：`src/main/java/com/group55/ta/controller/CourseManageServlet.java`

| HTTP 方法 | 参数 | 行为 |
|-----------|------|------|
| GET | `?id=courseId` | 查出该课程对象 + 该课程所有申请列表，转发到 `course-manage.jsp` |
| POST | `courseId`, `applicationId`, `action`（值为 `approve` 或 `reject`） | 更新该申请的状态，重定向回 GET 页面 |

GET 逻辑：
1. 从请求参数取 `courseId`
2. 验证当前登录用户为 TEACHER，且该课程的 `teacherUsername` 与当前用户一致（否则重定向 `/dashboard` 并设错误提示）
3. `CourseDao.findById(courseId)` → 放入 `request.setAttribute("course", course)`
4. `ApplicationDao.findByCourseId(courseId)` → 放入 `request.setAttribute("applications", list)`
5. 转发到 `course-manage.jsp`

POST 逻辑：
1. 取 `applicationId` 和 `action`
2. `action.equals("approve")` → `ApplicationDao.updateStatus(id, APPROVED)`
3. `action.equals("reject")` → `ApplicationDao.updateStatus(id, REJECTED)`
4. `response.sendRedirect(".../courses/manage?id=courseId")`（回到刚才的课程管理页）

> **一致性约束**：一个课程的 APPROVED 申请数不能超过 `course.taNeedCount`。若已满额，POST 时拒绝 approve 并设错误提示后重定向。  
> 实现方式：POST 前先调用 `ApplicationDao.findByCourseId()` 统计状态为 APPROVED 的数量。

#### 2b. 注册 Servlet

**修改文件**：`src/main/webapp/WEB-INF/web.xml`，添加：

```xml
<servlet>
    <servlet-name>CourseManageServlet</servlet-name>
    <servlet-class>com.group55.ta.controller.CourseManageServlet</servlet-class>
</servlet>
<servlet-mapping>
    <servlet-name>CourseManageServlet</servlet-name>
    <url-pattern>/courses/manage</url-pattern>
</servlet-mapping>
```

#### 2c. 新建 JSP

**新建文件**：`src/main/webapp/WEB-INF/views/course-manage.jsp`

页面内容：
- 顶部：课程名称、课程描述、需要 TA 人数、已通过人数
- 申请列表表格，列：学生用户名、申请时间、个人陈述（截断显示）、状态徽章、操作列
- 操作列：
  - 状态为 `PENDING` → 显示「通过」按钮（POST `action=approve`）和「拒绝」按钮（POST `action=reject`）
  - 状态为 `APPROVED` 或 `REJECTED` → 显示已定状态徽章，无按钮
- 表格为空时显示"暂无申请"提示
- 显示 `${errorMessage}` 错误提示（如满额时点通过的错误）
- 「返回主控台」按钮

按钮用 `<form>` 包裹，method POST，action `/courses/manage`：

```html
<form method="post" action="${pageContext.request.contextPath}/courses/manage">
    <input type="hidden" name="courseId" value="${course.id}">
    <input type="hidden" name="applicationId" value="${app.applicationId}">
    <button type="submit" name="action" value="approve" class="btn btn-primary btn-sm">通过</button>
    <button type="submit" name="action" value="reject" class="btn btn-secondary btn-sm"
            onclick="return confirmAction('确认拒绝这位申请者？')">拒绝</button>
</form>
```

---

### 任务 3：更新 Dashboard 中 Teacher 的跳转链接

**修改文件**：`src/main/webapp/WEB-INF/views/dashboard.jsp`

Teacher 视图中已有两个链接，确认其目标 URL 正确：

| 按钮 | 应跳转至 |
|------|---------|
| 发布新课程 | `/courses/new` |
| 管理申请 | `/courses/manage?id=${course.id}` |

当前 dashboard.jsp 第 91 行和 107 行已有这两个链接，**确认 URL 与 B 的 Servlet 注册一致即可，无需修改内容**。但需要补充：Dashboard Teacher 视图目前展示的是全部课程（`safeFindAllCourses()`），应改为**只展示当前教师自己的课程**。

**修改文件**：`src/main/java/com/group55/ta/controller/DashboardServlet.java`

将 Teacher 分支的数据查询从：
```java
request.setAttribute("courses", safeFindAllCourses());
```
改为：
```java
String teacherUsername = user.getUsername();
request.setAttribute("courses", safeFindCoursesByTeacher(teacherUsername));
```

并添加私有方法 `safeFindCoursesByTeacher`（仿照已有的 `safeFindAllCourses`，调用 `CourseDao.findByTeacher(username)`）。

同时将 Servlet 中使用反射加载 `currentUser` 的方式，改为直接从 session 读取 `User` 对象（A 已提供 Model 类，不再需要反射）：

```java
User user = (User) session.getAttribute("user");
```

> **注意**：这是 B 唯一需要修改的 Servlet Controller 文件（`DashboardServlet.java`），不涉及 A 已修改过的 `LoginServlet` 和 `AuthenticationFilter`。

---

### B 的交付物清单

| 文件 | 状态 |
|------|------|
| `controller/CourseCreateServlet.java` | 新建 |
| `controller/CourseManageServlet.java` | 新建 |
| `controller/DashboardServlet.java` | 修改（仅限 Teacher 查询方式与去反射） |
| `webapp/WEB-INF/views/course-create.jsp` | 新建 |
| `webapp/WEB-INF/views/course-manage.jsp` | 新建 |
| `webapp/WEB-INF/web.xml` | 修改（补 `/courses/new`、`/courses/manage` mapping） |

---

## 两人分工总结（无重叠）

```
A 负责：基础层 + 登录安全
  ├── model/                     (全新，B 只读不改)
  ├── dao/                       (全新，B 只调用不改)
  ├── util/FileStorageUtil       (全新)
  ├── LoginServlet               (修复 session key + 跳转)
  ├── AuthenticationFilter       (加角色拦截)
  └── web.xml                    (补 /courses、/apply)

B 负责：MO 功能
  ├── CourseCreateServlet        (全新)
  ├── CourseManageServlet        (全新)
  ├── DashboardServlet           (仅修改 Teacher 查询逻辑，去反射)
  ├── course-create.jsp          (全新)
  ├── course-manage.jsp          (全新)
  └── web.xml                    (补 /courses/new、/courses/manage)
```

---

## 验收标准（中期演示可用）

### A 完成后验收

- [ ] `student_li` / `teacher_wang` / `admin` 均能从 `data/users.txt` 登录，session 中 `user` 对象 role 字段正确
- [ ] 登录后页面导航栏显示正确角色名
- [ ] Teacher 直接访问 `/apply` 被重定向到 Dashboard
- [ ] Student 直接访问 `/courses/manage` 被重定向到 Dashboard
- [ ] 未登录访问 `/dashboard` 被重定向到 `/login`
- [ ] 注册后能以新账号登录

### B 完成后验收

- [ ] Teacher 登录后，Dashboard 只显示自己发布的课程
- [ ] Teacher 点击「发布新课程」，表单验证通过后，新课程写入 `data/courses.txt` 且在 Dashboard 可见
- [ ] Teacher 点击「管理申请」，能看到该课程所有学生申请（含申请时间与个人陈述）
- [ ] Teacher 点击「通过」后，对应申请状态变为 `APPROVED`，页面刷新后状态徽章更新
- [ ] Teacher 点击「拒绝」后，对应申请状态变为 `REJECTED`
- [ ] 当 APPROVED 数量达到 `taNeedCount` 上限后，再点「通过」提示满额，不更新状态
- [ ] Student 在课程列表能看到 Teacher 发布的新课程并申请
