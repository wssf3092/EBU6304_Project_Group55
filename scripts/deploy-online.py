#!/usr/bin/env python3
from __future__ import annotations

import base64
import json
import os
import re
import shutil
import subprocess
import sys
import tarfile
import textwrap
import time
import urllib.request
from dataclasses import dataclass
from pathlib import Path

import paramiko


REPO_ROOT = Path(__file__).resolve().parents[1]
ARTIFACT_ROOT = REPO_ROOT / "artifacts" / "deploy"
LOCAL_BUNDLE = ARTIFACT_ROOT / "ta-recruitment-online-bundle.tar.gz"
LOCAL_SUMMARY = ARTIFACT_ROOT / "online-deployment-summary.json"
SERVER_DOC = REPO_ROOT / "服务器部署.txt"
SERVICE_NAME = "ta-recruitment-system"
REMOTE_APP_ROOT = "/opt/ta-recruitment-system"
REMOTE_RELEASES = f"{REMOTE_APP_ROOT}/releases"
REMOTE_CURRENT = f"{REMOTE_APP_ROOT}/current"
REMOTE_DATA = f"{REMOTE_APP_ROOT}/data"
REMOTE_LOGS = f"{REMOTE_APP_ROOT}/logs"
REMOTE_RUNTIME = f"{REMOTE_APP_ROOT}/runtime"
REMOTE_TMP_BUNDLE = "/tmp/ta-recruitment-online-bundle.tar.gz"
DATABASE_NAME = "ta_recruitment_system_500"
MAVEN_CMD = shutil.which("mvn.cmd") or shutil.which("mvn") or "mvn"
NODE_CMD = shutil.which("node.exe") or shutil.which("node") or "node"
AI_CONFIG_FILES = [
    REPO_ROOT / "src" / "main" / "resources" / "ai-config.properties",
    REPO_ROOT / "src" / "main" / "resources" / "ai-config.local.properties",
]


@dataclass
class DeploymentConfig:
    ssh_host: str
    ssh_user: str
    ssh_password: str
    mysql_user: str
    mysql_password: str
    backend_port: int
    frontend_port: int


@dataclass
class AiRuntimeConfig:
    base_url: str
    api_key: str
    model: str
    timeout_millis: str
    cache_minutes: str


def read_properties_file(path: Path) -> dict[str, str]:
    values: dict[str, str] = {}
    if not path.exists():
        return values
    for raw_line in path.read_text(encoding="utf-8").splitlines():
        line = raw_line.strip()
        if not line or line.startswith("#") or "=" not in line:
            continue
        key, value = line.split("=", 1)
        values[key.strip()] = value.strip()
    return values


def load_repo_ai_properties() -> dict[str, str]:
    merged: dict[str, str] = {}
    for path in AI_CONFIG_FILES:
        merged.update(read_properties_file(path))
    return merged


class RemoteClient:
    def __init__(self, host: str, username: str, password: str) -> None:
        self.host = host
        self.username = username
        self.password = password
        self.client = paramiko.SSHClient()
        self.client.set_missing_host_key_policy(paramiko.AutoAddPolicy())

    def __enter__(self) -> "RemoteClient":
        self.client.connect(
            hostname=self.host,
            username=self.username,
            password=self.password,
            timeout=30,
        )
        return self

    def __exit__(self, exc_type, exc, tb) -> None:
        self.client.close()

    def run(self, command: str, timeout: int = 120) -> str:
        stdin, stdout, stderr = self.client.exec_command(command, timeout=timeout)
        exit_code = stdout.channel.recv_exit_status()
        out = stdout.read().decode("utf-8", "replace")
        err = stderr.read().decode("utf-8", "replace")
        if exit_code != 0:
            raise RuntimeError(
                f"Remote command failed ({exit_code}): {command}\nSTDOUT:\n{out}\nSTDERR:\n{err}"
            )
        return out

    def upload(self, local_path: Path, remote_path: str) -> None:
        sftp = self.client.open_sftp()
        try:
            sftp.put(str(local_path), remote_path)
        finally:
            sftp.close()


def log(message: str) -> None:
    print(f"[deploy-online] {message}", flush=True)


def run_local(command: list[str], env: dict[str, str] | None = None) -> None:
    log("Running: " + " ".join(command))
    subprocess.run(command, cwd=REPO_ROOT, env=env, check=True)


def parse_server_doc() -> DeploymentConfig:
    raw = SERVER_DOC.read_text(encoding="utf-8")

    def extract(pattern: str, label: str) -> str:
        match = re.search(pattern, raw)
        if not match:
            raise RuntimeError(f"Unable to find {label} in {SERVER_DOC}")
        return match.group(1).strip()

    def extract_any(patterns: list[str], label: str) -> str:
        for pattern in patterns:
            match = re.search(pattern, raw)
            if match:
                return match.group(1).strip()
        raise RuntimeError(f"Unable to find {label} in {SERVER_DOC}")

    return DeploymentConfig(
        ssh_host=extract(r"服务器 IP：([^\r\n]+)", "server ip"),
        ssh_user=extract(r"SSH 用户：([^\r\n]+)", "ssh user"),
        ssh_password=extract(r"SSH 密码：([^\r\n]+)", "ssh password"),
        mysql_user=extract(r"MySQL 账号：([^\r\n]+)", "mysql user"),
        mysql_password=extract(r"MySQL 密码：([^\r\n]+)", "mysql password"),
        backend_port=int(extract_any([r"后端部署在(\d+)", r"后端端口：(\d+)"], "backend port")),
        frontend_port=int(extract_any([r"前端部署在(\d+)", r"前端端口：(\d+)"], "frontend port")),
    )


def load_ai_runtime_config() -> AiRuntimeConfig:
    properties = load_repo_ai_properties()
    return AiRuntimeConfig(
        base_url=os.environ.get("TA_AI_BASE_URL", properties.get("ai.baseUrl", "")).strip(),
        api_key=os.environ.get("TA_AI_API_KEY", properties.get("ai.apiKey", "")).strip(),
        model=os.environ.get("TA_AI_MODEL", properties.get("ai.model", "")).strip(),
        timeout_millis=os.environ.get("TA_AI_TIMEOUT_MILLIS", properties.get("ai.timeoutMillis", "")).strip(),
        cache_minutes=os.environ.get("TA_AI_CACHE_MINUTES", properties.get("ai.cacheMinutes", "")).strip(),
    )


def quote_systemd_env(value: str) -> str:
    return value.replace("\\", "\\\\").replace('"', '\\"')


def build_systemd_environment(ai_cfg: AiRuntimeConfig) -> str:
    lines = []
    mappings = {
        "TA_AI_BASE_URL": ai_cfg.base_url,
        "TA_AI_API_KEY": ai_cfg.api_key,
        "TA_AI_MODEL": ai_cfg.model,
        "TA_AI_TIMEOUT_MILLIS": ai_cfg.timeout_millis,
        "TA_AI_CACHE_MINUTES": ai_cfg.cache_minutes,
    }
    for key, value in mappings.items():
        if value:
            lines.append(f'Environment="{key}={quote_systemd_env(value)}"')
    return "\n".join(lines)


def build_bundle() -> None:
    ARTIFACT_ROOT.mkdir(parents=True, exist_ok=True)
    run_local([MAVEN_CMD, "-q", "test"])
    run_local([MAVEN_CMD, "-q", "-DskipTests", "package", "dependency:copy-dependencies"])

    if LOCAL_BUNDLE.exists():
        LOCAL_BUNDLE.unlink()

    with tarfile.open(LOCAL_BUNDLE, "w:gz") as tar:
        tar.add(REPO_ROOT / "src" / "main" / "webapp", arcname="src/main/webapp")
        tar.add(REPO_ROOT / "target" / "classes", arcname="target/classes")
        tar.add(REPO_ROOT / "target" / "dependency", arcname="target/dependency")


def install_java_if_needed(remote: RemoteClient) -> str:
    preferred_java = f"{REMOTE_RUNTIME}/temurin-17/bin/java"
    if remote.run(f"test -x {preferred_java} && echo yes || true").strip() == "yes":
        log(f"Using existing bundled Java runtime: {preferred_java}")
        return preferred_java

    version_output = remote.run("java -version 2>&1 || true")
    if 'version "17.' in version_output or 'version "21.' in version_output:
        java_bin = remote.run("readlink -f $(which java)").strip()
        log(f"Using existing Java runtime: {java_bin}")
        return java_bin

    log("Installing Java 17 runtime on the server")
    available_java17 = remote.run("yum list available java-17-openjdk-headless 2>/dev/null | grep java-17-openjdk-headless || true").strip()
    if available_java17:
        remote.run("yum install -y java-17-openjdk-headless", timeout=1800)
        java_bin = remote.run("rpm -ql java-17-openjdk-headless | grep '/bin/java$' | head -n 1").strip()
        if java_bin:
            return java_bin

    remote.run(f"mkdir -p {REMOTE_RUNTIME}", timeout=120)
    remote.run(
        textwrap.dedent(
            f"""\
            set -e
            mkdir -p {REMOTE_RUNTIME}
            rm -rf {REMOTE_RUNTIME}/temurin-17
            mkdir -p {REMOTE_RUNTIME}/temurin-17
            curl -L --retry 3 --connect-timeout 20 -o /tmp/temurin17.tar.gz "https://api.adoptium.net/v3/binary/latest/17/ga/linux/x64/jdk/hotspot/normal/eclipse"
            tar -xzf /tmp/temurin17.tar.gz --strip-components=1 -C {REMOTE_RUNTIME}/temurin-17
            rm -f /tmp/temurin17.tar.gz
            test -x {preferred_java}
            """
        ),
        timeout=1800,
    )
    return preferred_java


def prepare_remote_release(remote: RemoteClient, java_bin: str, cfg: DeploymentConfig, ai_cfg: AiRuntimeConfig) -> dict[str, str]:
    release_id = time.strftime("%Y%m%d%H%M%S")
    release_dir = f"{REMOTE_RELEASES}/{release_id}"
    log(f"Preparing remote release: {release_dir}")

    remote.run(
        "mkdir -p "
        + " ".join(
            [
                REMOTE_RELEASES,
                REMOTE_DATA,
                REMOTE_LOGS,
                REMOTE_RUNTIME,
                release_dir,
            ]
        )
    )
    remote.upload(LOCAL_BUNDLE, REMOTE_TMP_BUNDLE)
    remote.run(f"tar -xzf {REMOTE_TMP_BUNDLE} -C {release_dir}")
    remote.run(f"rm -f {REMOTE_TMP_BUNDLE}")
    remote.run(f"ln -sfn {release_dir} {REMOTE_CURRENT}")

    service_lines = [
        "[Unit]",
        "Description=TA Recruitment System",
        "After=network.target",
        "",
        "[Service]",
        "Type=simple",
        f"WorkingDirectory={REMOTE_CURRENT}",
    ]
    environment_lines = build_systemd_environment(ai_cfg)
    if environment_lines:
        service_lines.extend(environment_lines.splitlines())
    service_lines.extend(
        [
            f'ExecStart=/usr/bin/bash -lc \'exec "{java_bin}" -Dta.data.root={REMOTE_DATA} -cp "target/classes:target/dependency/*" com.group55.ta.DevServer {cfg.backend_port}\'',
            "Restart=always",
            "RestartSec=5",
            "User=root",
            "",
            "[Install]",
            "WantedBy=multi-user.target",
            "",
        ]
    )
    service_body = "\n".join(service_lines)
    service_cmd = "cat > /etc/systemd/system/{name}.service <<'EOF'\n{body}EOF".format(
        name=SERVICE_NAME,
        body=service_body,
    )
    remote.run(service_cmd)

    nginx_body = textwrap.dedent(
        f"""\
        server {{
            listen {cfg.frontend_port};
            server_name _;

            location / {{
                proxy_pass http://127.0.0.1:{cfg.backend_port};
                proxy_http_version 1.1;
                proxy_set_header Host $http_host;
                proxy_set_header X-Real-IP $remote_addr;
                proxy_set_header X-Forwarded-For $proxy_add_x_forwarded_for;
                proxy_set_header X-Forwarded-Proto $scheme;
            }}
        }}
        """
    )
    nginx_cmd = "cat > /etc/nginx/conf.d/ta-recruitment-{port}.conf <<'EOF'\n{body}EOF".format(
        port=cfg.frontend_port,
        body=nginx_body,
    )
    remote.run(nginx_cmd)

    remote.run(
        "mysql -u{user} -p'{password}' -e \"CREATE DATABASE IF NOT EXISTS {db} "
        "DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;\"".format(
            user=cfg.mysql_user,
            password=cfg.mysql_password,
            db=DATABASE_NAME,
        )
    )

    remote.run("systemctl daemon-reload")
    remote.run(f"systemctl enable {SERVICE_NAME}")
    remote.run(f"systemctl restart {SERVICE_NAME}", timeout=180)
    remote.run("nginx -t", timeout=120)
    remote.run("systemctl reload nginx", timeout=120)

    return {
        "release_id": release_id,
        "release_dir": release_dir,
    }


def wait_for_url(url: str, timeout_seconds: int = 180) -> None:
    deadline = time.time() + timeout_seconds
    last_error = None
    while time.time() < deadline:
        try:
            with urllib.request.urlopen(url, timeout=10) as response:
                if response.status == 200:
                    return
        except Exception as exc:  # noqa: BLE001
            last_error = exc
        time.sleep(3)
    raise RuntimeError(f"Timed out waiting for {url} to become ready: {last_error}")


def wait_for_remote_http(remote: RemoteClient, url: str, timeout_seconds: int = 180) -> None:
    command = textwrap.dedent(
        f"""\
        end=$((SECONDS+{timeout_seconds}))
        while [ $SECONDS -lt $end ]; do
          code=$(curl -s -o /dev/null -w "%{{http_code}}" "{url}" || true)
          if [ "$code" = "200" ]; then
            exit 0
          fi
          sleep 3
        done
        echo "Timed out waiting for {url} inside the server" >&2
        exit 1
        """
    )
    remote.run(command, timeout=timeout_seconds + 30)


def run_online_qa(base_url: str) -> dict:
    env = os.environ.copy()
    env["QA_BASE_URL"] = base_url
    env["QA_SKIP_PERSISTENCE"] = "1"
    run_local([NODE_CMD, str(REPO_ROOT / "scripts" / "qa-e2e.mjs")], env=env)
    return json.loads((REPO_ROOT / "artifacts" / "qa" / "e2e-results.json").read_text(encoding="utf-8"))


def verify_remote_persistence(remote: RemoteClient, qa_summary: dict) -> dict:
    payload = json.dumps(
        {
            "remote_data_root": REMOTE_DATA,
            "accounts": qa_summary["accounts"],
            "job_title": qa_summary["entities"]["jobTitle"],
        }
    )
    encoded_payload = base64.b64encode(payload.encode("utf-8")).decode("ascii")
    command = (
        "python3 - <<'PY'\n"
        "import base64\n"
        "import json\n"
        "from pathlib import Path\n"
        f"payload = json.loads(base64.b64decode('{encoded_payload}').decode('utf-8'))\n"
        "root = Path(payload['remote_data_root'])\n"
        "accounts = payload['accounts']\n"
        "job_title = payload['job_title']\n"
        "result = {'users': {}, 'job': None, 'applicant': None, 'application': None}\n"
        "def read_jsons(folder):\n"
        "    if not folder.exists():\n"
        "        return []\n"
        "    return [json.loads(path.read_text(encoding='utf-8')) for path in folder.glob('*.json')]\n"
        "mos = read_jsons(root / 'users' / 'mos')\n"
        "tas = read_jsons(root / 'users' / 'tas')\n"
        "admins = read_jsons(root / 'users' / 'admins')\n"
        "jobs = read_jsons(root / 'jobs')\n"
        "applicants = read_jsons(root / 'applicants')\n"
        "applications = read_jsons(root / 'applications')\n"
        "mo_user = next((item for item in mos if item.get('email') == accounts['mo']['email']), None)\n"
        "ta_user = next((item for item in tas if item.get('email') == accounts['ta']['email']), None)\n"
        "admin_user = next((item for item in admins if item.get('email') == accounts['admin']['email']), None)\n"
        "job = next((item for item in jobs if item.get('title') == job_title), None)\n"
        "applicant = next((item for item in applicants if item.get('contactEmail') == accounts['ta']['email']), None)\n"
        "application = None\n"
        "if ta_user and job:\n"
        "    application = next((item for item in applications if item.get('applicantId') == ta_user.get('userId') and item.get('jobId') == job.get('jobId')), None)\n"
        "result['users']['mo'] = mo_user\n"
        "result['users']['ta'] = ta_user\n"
        "result['users']['admin'] = admin_user\n"
        "result['job'] = job\n"
        "result['applicant'] = applicant\n"
        "result['application'] = application\n"
        "print(json.dumps(result, ensure_ascii=False))\n"
        "PY"
    )
    raw = remote.run(command, timeout=120)
    verification = json.loads(raw)

    def ensure(condition: bool, message: str) -> None:
        if not condition:
            raise RuntimeError(message)

    ensure(verification["users"]["mo"], "MO account was not persisted on the server.")
    ensure(verification["users"]["ta"], "TA account was not persisted on the server.")
    ensure(verification["users"]["admin"], "Admin account was not persisted on the server.")
    ensure(verification["job"], "Job posting was not persisted on the server.")
    ensure(verification["applicant"], "Applicant profile was not persisted on the server.")
    ensure(verification["application"], "Application record was not persisted on the server.")
    ensure(
        verification["application"]["status"] == "accepted",
        f"Expected accepted application status, received {verification['application']['status']!r}.",
    )
    return verification


def collect_remote_status(remote: RemoteClient, cfg: DeploymentConfig) -> dict:
    service_status = remote.run(f"systemctl is-active {SERVICE_NAME}").strip()
    backend_health = remote.run(
        f"curl -I -s http://127.0.0.1:{cfg.backend_port}/auth/login | head -n 1"
    ).strip()
    frontend_health = remote.run(
        f"curl -I -s http://127.0.0.1:{cfg.frontend_port}/auth/login | head -n 1"
    ).strip()
    java_path = remote.run(
        f"systemctl show -p ExecStart {SERVICE_NAME} --no-pager | sed 's/^ExecStart=//'"
    ).strip()
    return {
        "service_status": service_status,
        "backend_health": backend_health,
        "frontend_health": frontend_health,
        "service_execstart": java_path,
    }


def write_summary(summary: dict) -> None:
    LOCAL_SUMMARY.write_text(json.dumps(summary, ensure_ascii=False, indent=2), encoding="utf-8")


def main() -> int:
    cfg = parse_server_doc()
    ai_cfg = load_ai_runtime_config()
    build_bundle()
    base_url = f"http://{cfg.ssh_host}:{cfg.frontend_port}"

    with RemoteClient(cfg.ssh_host, cfg.ssh_user, cfg.ssh_password) as remote:
        java_bin = install_java_if_needed(remote)
        release_meta = prepare_remote_release(remote, java_bin, cfg, ai_cfg)
        wait_for_remote_http(remote, f"http://127.0.0.1:{cfg.backend_port}/auth/login")
        wait_for_remote_http(remote, f"http://127.0.0.1:{cfg.frontend_port}/auth/login")
        wait_for_url(f"{base_url}/auth/login")
        qa_summary = run_online_qa(base_url)
        persistence = verify_remote_persistence(remote, qa_summary)
        remote_status = collect_remote_status(remote, cfg)

    deployment_summary = {
        "deployed_at": time.strftime("%Y-%m-%d %H:%M:%S"),
        "server_ip": cfg.ssh_host,
        "frontend_url": f"{base_url}/auth/login",
        "backend_url": f"http://{cfg.ssh_host}:{cfg.backend_port}/auth/login",
        "database_name": DATABASE_NAME,
        "release": release_meta,
        "ai": {
            "configured": bool(ai_cfg.base_url and ai_cfg.api_key and ai_cfg.model),
            "base_url": ai_cfg.base_url,
            "model": ai_cfg.model,
            "api_key_present": bool(ai_cfg.api_key),
        },
        "qa": qa_summary,
        "persistence": persistence,
        "remote_status": remote_status,
        "screenshots": sorted(
            str(path.relative_to(REPO_ROOT)).replace("\\", "/")
            for path in (REPO_ROOT / "artifacts" / "qa" / "screenshots").glob("*.png")
        ),
    }
    write_summary(deployment_summary)
    log(f"Deployment summary written to {LOCAL_SUMMARY}")
    return 0


if __name__ == "__main__":
    try:
        raise SystemExit(main())
    except subprocess.CalledProcessError as exc:
        print(exc, file=sys.stderr)
        raise SystemExit(exc.returncode)
    except Exception as exc:  # noqa: BLE001
        print(exc, file=sys.stderr)
        raise SystemExit(1)
