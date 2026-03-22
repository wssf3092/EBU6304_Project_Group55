# Project Dependencies

## Build Tool Policy

This project **does NOT use Maven or Gradle**. All dependencies are managed as JAR files
placed directly in this `lib/` directory. Team members must share JAR files via Git
(this directory is tracked in version control).

## Runtime Environment

- **Apache Tomcat 9.x** — The servlet container provides the Servlet API at runtime.
  Therefore, `javax.servlet-api` is a **compile-time only** dependency and does **not**
  need to be placed in this `lib/` directory. Configure it as "provided" scope in your IDE.

## Current Dependencies

| JAR Name | Version | Purpose | Source URL |
|---|---|---|---|
| `junit-4.13.2.jar` | 4.13.2 | Unit testing framework | https://search.maven.org/artifact/junit/junit |
| `hamcrest-core-1.3.jar` | 1.3 | JUnit dependency | https://search.maven.org/artifact/org.hamcrest/hamcrest-core |

## Planned / Future Dependencies

| JAR Name | Version | Purpose | Source URL |
|---|---|---|---|
| `gson-x.x.x.jar` | 2.10.1 | JSON parsing and serialization | https://search.maven.org/artifact/com.google.code.gson/gson |
| `junit-x.x.jar` | 4.13.2 | Unit testing framework | https://search.maven.org/artifact/junit/junit |

## Adding a New Dependency

1. Download the JAR from the source URL above (or Maven Central).
2. Place the JAR file in this `lib/` directory.
3. Update this `DEPENDENCIES.md` with the JAR name, version, purpose, and URL.
4. Commit both the JAR file and the updated `DEPENDENCIES.md`.
5. Notify all team members to re-sync their local `lib/` directory.

## IDE Configuration

### IntelliJ IDEA
1. Open **File → Project Structure → Modules → Dependencies**.
2. Click **+** → **JARs or directories** → select all JARs in `lib/`.
3. Set `javax.servlet-api` scope to **Provided** (from Tomcat).

### Eclipse
1. Right-click project → **Properties → Java Build Path → Libraries**.
2. Click **Add External JARs** → select all JARs in `lib/`.
3. Add Tomcat server runtime for the Servlet API.
