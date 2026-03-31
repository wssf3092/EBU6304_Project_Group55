# TA 招募系统 — 代码级功能需求文档

> EBU6304 Software Engineering Group Project — Group 55
> 版本：1.0 | 日期：2026-04-05

---

## 目录

1. [开发技术约束（强制）](#1-开发技术约束强制)
2. [角色与用户体系](#2-角色与用户体系)
3. [功能需求 — TA 端](#3-功能需求--ta-端)
4. [功能需求 — MO 端（模块负责人）](#4-功能需求--mo-端模块负责人)
5. [功能需求 — Admin 端（管理员）](#5-功能需求--admin-端管理员)
6. [AI 功能需求](#6-ai-功能需求)
7. [数据存储规范](#7-数据存储规范)
8. [代码架构要求](#8-代码架构要求)
9. [非功能需求](#9-非功能需求)

---

## 1. 开发技术约束（强制）

以下约束来自课题 Handout 的 **2.2 Other Requirements (mandatory)**，必须严格遵守：

| 约束项  | 要求                                                         |
| ---- | ---------------------------------------------------------- |
| 开发语言 | Java（Java 11+）                                             |
| 应用类型 | 轻量级 Java Servlet / JSP Web 应用（**禁止**使用 Spring Boot 等重量级框架） |
| 数据存储 | 纯文本文件格式：`.txt`、`.csv`、`.json`、`.xml` — **禁止使用任何数据库**       |
| 服务器  | Apache Tomcat 9.x                                          |
| 构建方式 | 手动编译或 IDE 直接部署（不强制使用 Maven/Gradle）                         |

---

## 2. 角色与用户体系

系统共三类用户角色，均需通过账号/密码登录：

| 角色                             | 说明             |
| ------------------------------ | -------------- |
| **TA（Teaching Assistant 申请者）** | 在校学生，申请助教职位    |
| **MO（Module Organiser 模块负责人）** | 课程负责教师，发布和管理职位 |
| **Admin（系统管理员）**               | 管理全局数据与工作量监控   |

### 2.1 用户认证模块（所有角色共用）

- **注册**：用户填写基本信息（姓名、邮箱、密码、角色类型），数据写入对应文本文件。
- **登录**：凭邮箱+密码验证身份，登录成功后跳转对应角色主页（Servlet Session 管理）。
- **登出**：清除 Session，跳转登录页。
- **权限拦截**：通过 Servlet Filter 实现，未登录用户访问受保护页面时重定向至登录页。

---

## 3. 功能需求 — TA 端

### 3.1 申请人档案管理（Applicant Profile）

**功能描述**：TA 可创建并维护个人档案。

- 可填写/编辑的字段：
  - 姓名、学号、联系邮箱
  - 专业方向、年级
  - 技能标签列表（如：Java、Python、数学、英语等，支持多选/手动输入）
  - 个人简介（自由文本）
  - 可接受的工作量上限（每周最大小时数）
- 档案数据以 JSON 格式存储于 `data/applicants/` 目录，每位 TA 一个文件（`{userId}.json`）。
- 档案创建后可随时修改，修改覆写原文件。

### 3.2 CV 上传

**功能描述**：TA 可上传 CV 文件（PDF 或 DOCX）。

- 通过 HTML `<form enctype="multipart/form-data">` + Servlet 的 `getPart()` API 实现文件上传。
- 上传的文件保存至服务器本地目录 `data/cvs/{userId}/`。
- 页面显示已上传 CV 的文件名及上传时间；支持重新上传（覆盖旧版本）。
- 文件大小限制：最大 5MB（在 Servlet 中校验）。

### 3.3 查看可用职位（Browse Jobs）

**功能描述**：TA 可浏览所有 MO 发布的职位列表。

- 职位列表页显示字段：职位名称、所属模块/活动、所需技能标签、招募人数、截止日期、状态（开放/已关闭）。
- 支持按技能标签或关键词筛选职位。
- 点击职位可查看详情页（完整的职位描述）。
- 已申请的职位在列表中高亮标记。

### 3.4 申请职位（Apply for Jobs）

**功能描述**：TA 可向目标职位提交申请。

- 申请前系统校验：TA 档案是否完整、是否已申请该职位（防重复申请）。
- 申请时可附上一段简短的求职信（Cover Letter，自由文本，500字以内）。
- 申请数据写入 `data/applications/` 目录（JSON 格式），记录：申请人ID、职位ID、申请时间、求职信、状态（`pending` / `accepted` / `rejected`）。
- 申请成功后在页面给出确认提示。

### 3.5 查看申请状态（Application Status）

**功能描述**：TA 可查看自己所有申请的当前状态。

- 展示字段：职位名称、申请时间、当前状态（待审核 / 已接受 / 已拒绝）。
- 若状态变更（MO 审核后），页面自动反映最新状态（每次加载时实时读取文件）。
- 若申请被拒绝，可显示 MO 填写的拒绝原因（可选字段）。

---

## 4. 功能需求 — MO 端（模块负责人）

### 4.1 发布职位（Post Jobs）

**功能描述**：MO 可创建新的助教招募职位。

- 发布职位所需字段：
  - 职位名称
  - 所属模块/活动（如：模块编号、活动类型——监考/答疑等）
  - 职位描述（详细文本）
  - 所需技能标签（多选）
  - 招募人数
  - 每人预计工作量（小时/周）
  - 报名截止日期
- 数据写入 `data/jobs/` 目录（JSON 格式），每个职位一个文件（`{jobId}.json`），自动生成唯一 jobId。
- 职位发布后状态默认为 `open`；MO 可手动关闭职位（状态改为 `closed`）。

### 4.2 查看申请列表（View Applicants）

**功能描述**：MO 可查看某一职位下所有申请者的列表。

- 列表展示：申请人姓名、技能标签、申请时间、简历下载链接（指向已上传的 CV 文件）、求职信、当前状态。
- 提供筛选功能：按申请时间排序、按技能匹配度排序（结合 AI 功能）。

### 4.3 选择/审核申请人（Select Applicants）

**功能描述**：MO 可对申请者做出录用或拒绝决定。

- MO 可将申请状态改为 `accepted` 或 `rejected`，并可填写备注/拒绝原因。
- 接受申请后，该职位的已录用人数自动累加；当录用人数达到招募名额上限时，职位状态自动变更为 `closed`。
- 所有状态变更实时写回对应 JSON 文件。

---

## 5. 功能需求 — Admin 端（管理员）

### 5.1 查看 TA 工作量总览（Workload Overview）

**功能描述**：Admin 可查看所有 TA 的整体工作量情况。

- 汇总视图展示每位 TA：姓名、已获录用的职位列表、累计工作量（小时/周）、其自定义的工作量上限。
- 高亮标记工作量超过其个人上限的 TA（超负荷预警）。
- 数据来源：读取 `data/applications/`（状态为 `accepted`）+ `data/jobs/`（工作量字段）+ `data/applicants/`（工作量上限字段）进行聚合计算。

### 5.2 用户管理（User Management）

- Admin 可查看所有注册用户（TA、MO）列表。
- 可停用/启用某一账号（在用户 JSON 文件中设置 `active: true/false` 字段）；停用账号登录时提示"账号已被禁用"。

### 5.3 职位管理（Job Management）

- Admin 可强制关闭任意职位（无论 MO 操作与否）。
- Admin 可查看所有职位及其申请统计数据（申请人数、录用人数）。

---

## 6. AI 功能需求

> 根据 Handout 2.1 节的 AI 功能建议，**全部实现**以下三项 AI 功能。
> 根据 Handout 2.3 节要求，AI 输出**不可盲目采用**，必须结合结构化逻辑，且结果需可解释。

### 6.1 技能匹配（Skill Matching）

**功能入口**：MO 查看某职位申请列表时，每位申请人旁显示"匹配分"。

**实现方案**：

1. **结构化层（本地计算）**：比较申请人技能标签列表与职位所需技能标签列表，计算交集比例作为基础匹配分（0~100分）。
   
   ```
   基础匹配分 = (匹配的技能数 / 职位所需技能总数) × 100
   ```
2. **AI 增强层（调用 LLM API）**：将职位描述和申请人的个人简介、CV 文本发送给 AI（如 DeepSeek / 其他开放 API），要求 AI 返回语义匹配评估（结构化 JSON 格式，包含：综合评分、匹配优势、匹配不足）。
3. **融合展示**：将结构化分数与 AI 评估综合显示，不直接以 AI 评分替代结构化分数。
4. **可解释性**：在页面上展示匹配计算依据（如"匹配技能：Java、Python；缺失技能：数据库"），而不仅仅是一个分数。

**数据流**：

- 调用 AI API 的请求/响应通过后端 Servlet 中转（不暴露 API Key 给前端）。
- AI 响应结果缓存到临时文本文件（`data/ai_cache/match_{jobId}_{applicantId}.json`），避免重复调用。

### 6.2 缺失技能识别（Identifying Missing Skills）

**功能入口**：TA 查看某一职位详情时，页面显示"技能差距分析"模块。

**实现方案**：

1. **结构化层**：对比 TA 当前技能标签与职位所需技能标签，列出缺失的技能标签（集合差集）。
2. **AI 增强层**：将职位描述和 TA 档案（技能+简介）发送给 AI，要求 AI 返回：
   - 重要程度排序后的缺失技能列表
   - 每项缺失技能的简短学习建议（资源/方向）
3. **展示格式**：在职位详情页以列表或卡片形式展示，注明"此分析由 AI 生成，仅供参考"。
4. **降级处理**：若 AI API 调用失败，仅展示结构化层的缺失技能列表，页面提示"AI 分析暂时不可用"。

### 6.3 工作量均衡（Balancing Workload）

**功能入口**：Admin 工作量总览页面，提供"AI 工作量建议"按钮。

**实现方案**：

1. **结构化层**：计算每位 TA 当前累计工作量，识别工作量过高（超上限）和过低（当前为 0 或明显低于平均）的 TA。
2. **AI 增强层**：将全体 TA 工作量数据（匿名化处理后，仅保留 TA_ID 和数值）及所有开放职位列表发送给 AI，要求 AI 返回再分配建议（结构化 JSON：推荐哪位 TA 申请哪个职位）。
3. **呈现方式**：在 Admin 页面展示 AI 的建议理由，**Admin 仅查看建议，不会直接执行操作**（最终决策权仍在 MO 和 TA 手中，符合负责任 AI 要求）。
4. **可解释性**：展示 AI 给出建议的依据（如"TA_003 当前工作量为 2h/周，低于均值 8h/周，建议考虑职位 Job_012"）。

### 6.4 AI 集成技术要求

| 项目         | 要求                                                                                   |
| ---------- | ------------------------------------------------------------------------------------ |
| API 调用方式   | 后端 Servlet 通过 `HttpURLConnection` 调用外部 LLM REST API（不引入额外 HTTP 客户端库）                 |
| API Key 存储 | 存储在服务器端配置文件（`src/main/resources/ai-config.properties`），**不提交到 Git**（加入 `.gitignore`） |
| 响应格式       | 要求 AI 返回结构化 JSON，后端使用简单 JSON 解析（可引入 `org.json` 或 `Gson` JAR）                         |
| 错误处理       | 所有 AI 调用必须有 try-catch，超时/失败时降级到纯结构化逻辑，不影响主流程                                         |
| 结果缓存       | AI 结果缓存至本地文本文件，有效期内不重复调用                                                             |
| 隐私保护       | 发送给 AI 的数据不包含完整个人身份信息（PII），仅发送技能标签、工作量数值等非敏感数据                                       |

---

## 7. 数据存储规范

所有数据以文本文件形式存储于项目根目录下的 `data/` 文件夹，**禁止使用任何数据库**。

### 7.1 目录结构

```
data/
├── users/
│   ├── tas/            # 每个 TA 一个 JSON 文件，文件名为 {userId}.json
│   ├── mos/            # 每个 MO 一个 JSON 文件
│   └── admins/         # Admin 账号 JSON 文件
├── applicants/         # TA 申请人档案，{userId}.json
├── cvs/                # 上传的 CV 文件，按 userId 子目录存放
├── jobs/               # 职位信息，{jobId}.json
├── applications/       # 申请记录，{applicationId}.json
└── ai_cache/           # AI 响应缓存文件
```

### 7.2 关键数据结构（JSON 示例）

**用户（`data/users/tas/{userId}.json`）**

```json
{
  "userId": "TA_001",
  "name": "Zhang San",
  "email": "zhangsan@bupt.edu.cn",
  "passwordHash": "...",
  "role": "TA",
  "active": true,
  "createdAt": "2026-03-01T10:00:00"
}
```

**TA 档案（`data/applicants/{userId}.json`）**

```json
{
  "userId": "TA_001",
  "studentId": "2021213001",
  "major": "Computer Science",
  "year": 3,
  "skills": ["Java", "Python", "Mathematics", "English"],
  "bio": "...",
  "maxWorkloadHoursPerWeek": 10,
  "cvFileName": "zhangsan_cv.pdf",
  "cvUploadedAt": "2026-03-15T14:30:00"
}
```

**职位（`data/jobs/{jobId}.json`）**

```json
{
  "jobId": "JOB_001",
  "moId": "MO_001",
  "title": "EBU6304 Teaching Assistant",
  "module": "EBU6304",
  "activityType": "Lab Support",
  "description": "...",
  "requiredSkills": ["Java", "Software Engineering"],
  "quota": 3,
  "workloadHoursPerWeek": 4,
  "deadline": "2026-04-30",
  "status": "open",
  "acceptedCount": 1,
  "createdAt": "2026-03-10T09:00:00"
}
```

**申请记录（`data/applications/{applicationId}.json`）**

```json
{
  "applicationId": "APP_001",
  "applicantId": "TA_001",
  "jobId": "JOB_001",
  "coverLetter": "...",
  "appliedAt": "2026-03-20T11:00:00",
  "status": "pending",
  "reviewedAt": null,
  "reviewNote": null
}
```

### 7.3 DAO 层设计要求

- 每种实体对应一个 DAO 类（如 `UserDAO`、`JobDAO`、`ApplicationDAO`）。
- DAO 类封装所有文件读写逻辑，上层（Servlet）不直接操作文件。
- 所有文件读写操作使用 `synchronized` 关键字或文件锁（`FileLock`）保证并发安全（Tomcat 多线程环境）。
- JSON 解析统一使用一个工具类（如 `JsonUtil`），封装 `org.json` 或 `Gson` 的调用。

---

## 8. 代码架构要求

采用 **MVC 模式**：

| 层次                  | 职责                                   | 对应包/目录                      |
| ------------------- | ------------------------------------ | --------------------------- |
| **Controller（控制层）** | Servlet，处理 HTTP 请求/响应，调用 Service/DAO | `com.group55.ta.controller` |
| **Model（模型层）**      | POJO 数据类（User、Job、Application 等）     | `com.group55.ta.model`      |
| **View（视图层）**       | JSP 页面，展示数据，不含业务逻辑                   | `WEB-INF/views/`            |
| **DAO（数据访问层）**      | 读写文本文件，返回 Model 对象                   | `com.group55.ta.dao`        |
| **Service（服务层）**    | 业务逻辑（可选，也可直接在 Servlet 中实现）           | `com.group55.ta.service`    |
| **Util（工具层）**       | JSON 解析、文件操作、密码哈希、AI API 调用等         | `com.group55.ta.util`       |
| **Filter（过滤器）**     | 认证拦截、字符编码设置                          | `com.group55.ta.filter`     |

### 8.1 Servlet 路由规划

| URL Pattern               | Servlet 类               | 功能                 |
| ------------------------- | ----------------------- | ------------------ |
| `/auth/login`             | `LoginServlet`          | 登录处理               |
| `/auth/register`          | `RegisterServlet`       | 注册处理               |
| `/auth/logout`            | `LogoutServlet`         | 登出                 |
| `/ta/profile`             | `TAProfileServlet`      | TA 档案查看/编辑         |
| `/ta/cv/upload`           | `CVUploadServlet`       | CV 上传              |
| `/ta/jobs`                | `JobBrowseServlet`      | TA 浏览职位列表          |
| `/ta/jobs/apply`          | `ApplyServlet`          | 提交申请               |
| `/ta/applications`        | `MyApplicationsServlet` | 查看申请状态             |
| `/mo/jobs`                | `MOJobServlet`          | MO 职位管理（发布/关闭）     |
| `/mo/jobs/applicants`     | `ApplicantListServlet`  | 查看申请列表             |
| `/mo/applications/review` | `ReviewServlet`         | 审核申请（录用/拒绝）        |
| `/admin/workload`         | `WorkloadServlet`       | 工作量总览              |
| `/admin/users`            | `UserManagementServlet` | 用户管理               |
| `/ai/match`               | `AIMatchServlet`        | AI 技能匹配（AJAX 调用）   |
| `/ai/skills-gap`          | `AISkillsGapServlet`    | AI 缺失技能分析（AJAX 调用） |
| `/ai/workload-balance`    | `AIWorkloadServlet`     | AI 工作量均衡建议         |

### 8.2 安全要求

- 密码存储：使用 SHA-256 哈希（`MessageDigest`），不存储明文密码。
- XSS 防护：JSP 中所有输出变量使用 `<c:out value="${...}"/>` 或 `fn:escapeXml()`。
- 权限控制：`AuthFilter` 拦截所有 `/ta/*`、`/mo/*`、`/admin/*` 路径，根据 Session 中的 `role` 字段进行角色校验。
- 文件上传安全：校验文件后缀（白名单：`.pdf`、`.docx`）、文件大小，防止路径穿越（对文件名进行净化）。

---

## 9. 非功能需求

| 类别         | 要求                                                     |
| ---------- | ------------------------------------------------------ |
| **可用性**    | 主要页面加载时间不超过 3 秒（本地 Tomcat 环境）                          |
| **错误处理**   | 所有用户输入必须有服务端校验；无效操作返回清晰的错误提示页面，不暴露栈跟踪信息                |
| **代码文档**   | 所有公开类和方法必须有 JavaDoc 注释                                 |
| **测试**     | 核心功能（DAO 层、业务逻辑）必须有 JUnit 单元测试                         |
| **兼容性**    | 支持主流现代浏览器（Chrome、Edge、Firefox）                         |
| **编码**     | 全项目统一使用 UTF-8 编码；`CharacterEncodingFilter` 设置所有请求/响应编码 |
| **AI 透明度** | 所有 AI 生成内容必须在 UI 上注明"由 AI 生成，仅供参考"，且展示推理依据             |

---
