# EBU6304 - TA Recruitment System (Group 55)

## Project Description

BUPT International School Teaching Assistant Recruitment System.
Built as a lightweight Java Servlet/JSP web application.
This project is developed using Agile methodologies for EBU6304 Software Engineering.

## Tech Stack

- **Language**: Java 11+
- **Web Framework**: Java Servlet 4.0 / JSP 2.3 (NO Spring Boot)
- **Server**: Apache Tomcat 9.x
- **Data Storage**: Plain text files (.txt, .csv, .json) — NO database
- **Build**: Manual compilation (no Maven/Gradle)

## Project Structure

```
D:\college\SE\code\
├── src/
│   └── main/
│       ├── java/
│       │   └── com/
│       │       └── group55/
│       │           └── ta/
│       │               ├── controller/      # Servlet控制器 (MVC - C层)
│       │               ├── model/           # 数据模型/POJO (MVC - M层)
│       │               ├── dao/             # 数据访问层 (读写文本文件)
│       │               ├── util/            # 工具类
│       │               └── filter/          # Servlet过滤器 (编码/认证等)
│       ├── webapp/
│       │   ├── WEB-INF/
│       │   │   ├── web.xml                  # 部署描述符
│       │   │   └── views/                   # JSP视图文件 (MVC - V层)
│       │   ├── static/
│       │   │   ├── css/                     # 样式表
│       │   │   ├── js/                      # JavaScript
│       │   │   └── images/                  # 图片资源
│       │   └── index.jsp                    # 入口页面
│       └── resources/                       # 配置文件
├── src/
│   └── test/
│       └── java/
│           └── com/
│               └── group55/
│                   └── ta/                  # 单元测试
├── data/                                    # 运行时文本文件数据存储目录
├── lib/                                     # 第三方JAR依赖
│   └── DEPENDENCIES.md                      # 依赖说明文档
├── doc/                                     # 项目文档
├── build/                                   # 编译输出 (被gitignore)
├── .gitignore
└── README.md
```

## Getting Started (Setup Guide)

### Prerequisites

1. JDK 11 or higher installed, `JAVA_HOME` configured
2. Apache Tomcat 9.x downloaded and extracted
3. Git installed

### Clone the Repository

```bash
git clone https://github.com/wssf3092/EBU6304_Project_Group55.git
cd EBU6304_Project_Group55
```

### Configure Tomcat

1. Copy the entire project to Tomcat's `webapps/` directory,
   OR configure an IDE (IntelliJ/Eclipse) to deploy to Tomcat.

2. **IntelliJ IDEA** configuration steps:
   - Go to **Run → Edit Configurations → + → Tomcat Server → Local**
   - Under **Server** tab: set the Tomcat home directory
   - Under **Deployment** tab: click **+** → **Artifact** → select the project WAR/exploded artifact
   - Set the **Application context** to `/ta-recruitment`
   - Add all JARs in `lib/` to the project classpath (**File → Project Structure → Modules → Dependencies**)
   - Set `javax.servlet-api` scope to **Provided**

3. **Eclipse** configuration steps:
   - Go to **Window → Preferences → Server → Runtime Environments** → Add Tomcat 9.x
   - Right-click project → **Properties → Java Build Path → Libraries** → Add External JARs from `lib/`
   - Right-click project → **Run As → Run on Server** → select the configured Tomcat runtime

### Compile & Run

1. Ensure Tomcat is running
2. Access [http://localhost:8080/ta-recruitment/](http://localhost:8080/ta-recruitment/)
3. You should see the "System Initialized" page

## Git Branching Strategy

### Branch Structure

- `master` — 稳定发布分支，仅接受经过 Code Review 的 PR 合并
- `develop` — 开发集成分支，所有功能分支合并至此
- `feature/<member-name>/<feature-description>` — 个人功能开发分支

### Branch Naming Convention

| Branch Type | Pattern | Example |
|---|---|---|
| Master | `master` | `master` |
| Development | `develop` | `develop` |
| Feature | `feature/<name>/<desc>` | `feature/zhangsan/login-page` |
| Bugfix | `bugfix/<name>/<desc>` | `bugfix/lisi/fix-file-read` |
| Hotfix | `hotfix/<desc>` | `hotfix/critical-encoding` |

### Workflow Rules

1. **NEVER** commit directly to `master` or `develop`
2. Create a feature branch from `develop` for every task
3. Submit a Pull Request when the feature is complete
4. At least 1 team member must review and approve the PR
5. After PR is merged, delete the feature branch
6. Each team member must have visible commits on their own branches

### Commit Message Convention

```
<type>: <short description>

Types: feat, fix, docs, style, refactor, test, chore
```

## Team Members

| # | Name | GitHub Username | Role |
|---|---|---|---|
| 1 | [Name] | [username] | [Scrum Master / Developer / ...] |
| 2 | [Name] | [username] | [Developer] |
| 3 | [Name] | [username] | [Developer] |
| 4 | [Name] | [username] | [Developer] |
| 5 | [Name] | [username] | [Developer] |
| 6 | [Name] | [username] | [Developer] |

## License

This project is developed for academic purposes at BUPT & QMUL.
