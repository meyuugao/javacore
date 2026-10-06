# Раздел 7. DevOps, Containers & Observability

> Цель раздела — провести Java-инженера от уровня «умею запустить `docker run`» до уровня Senior/Staff DevOps-инженера: контейнеры, оркестрация (Kubernetes), GitOps, observability (metrics/logs/traces), профилирование в production, SRE-практики, IaC, CI/CD и chaos engineering. Особое внимание — Java-специфике: JVM в контейнерах, GC logs, JFR, async-profiler, Spring Boot Actuator/Micrometer, OpenTelemetry Java agent.
>
> Связь с другими разделами:
> - **01 (Java Core)**: NIO/Process API, JMX, JFR; CLI-утилиты.
> - **02 (Java Advanced)**: JVM tuning в контейнерах (`-XX:MaxRAMPercentage`), GC logs, virtual threads + Kubernetes CPU limits, heap dumps.
> - **03 (Build/Test)**: JIB/Buildpacks, Testcontainers (как правило стоит в CI), Maven/Gradle в многоступенчатой сборке.
> - **04 (Spring)**: Actuator/Micrometer, Spring Boot layered jars, Spring Cloud Kubernetes, OTel Spring Boot starter.
> - **05 (Persistence)**: Testcontainers для PostgreSQL, миграции Flyway в K8s Job, операторы Patroni/CloudNativePG.
> - **06 (Distributed)**: Kafka на K8s (Strimzi), RabbitMQ Cluster Operator, трассировка через очереди.
> - **08 (Arch/Leadership)**: release management, on-call, postmortem-культура, SLO-driven roadmap.

## Содержание

1. Linux/Shell основы (bash, SSH, top/htop, systemd, journalctl, ss/netstat, ulimit)
2. Docker (Dockerfile instructions, multi-stage builds, layers cache, .dockerignore, BuildKit, distroless/slim, USER non-root, dive)
3. Docker Compose (services, volumes, networks, healthcheck, override, profiles)
4. Docker registry (Docker Hub, Harbor, ECR/GAR, Trivy scan, cosign, SBOM)
5. Kubernetes основы (Pod, Deployment, ReplicaSet, Service, ConfigMap, Secret, Labels, Probes liveness/readiness/startup, requests/limits, QoS)
6. Kubernetes продолжение (StatefulSet, DaemonSet, Job/CronJob, HPA/VPA, init containers, sidecar, affinity/taints/tolerations, PDB)
7. Kubernetes networking (Service types, Ingress, Ingress controllers, NetworkPolicy, DNS, CNI Calico/Cilium)
8. Kubernetes storage (PV/PVC, StorageClass, CSI, StatefulSet volumeClaimTemplates)
9. Helm (charts, templates, values, hooks, subcharts, Kustomize сравнение)
10. GitOps (ArgoCD, Flux, sync waves, drift, rollback)
11. Service Mesh на K8s (Istio, Linkerd; mTLS, canary)
12. Observability Metrics (Prometheus, PromQL, histograms/summaries, rules, Alertmanager, Micrometer, cardinality)
13. Observability Dashboards (Grafana datasources, panels, variables, dashboards as code, alerting)
14. Observability Logs (ELK/EFK, Loki+Promtail, structured JSON logging, MDC, log levels)
15. Observability Traces (OpenTelemetry SDK, Java agent auto-instrumentation, Jaeger/Tempo, sampling, baggage, W3C trace context)
16. Профилирование Java в production (JFR, async-profiler flame graphs, Arthas, JMC, heap dump MAT, GC logs, JMX)
17. SRE практики (SLI/SLO/SLA, error budget, toil, blameless postmortem, on-call)
18. IaC (Terraform state/modules/providers/workspaces/Atlantis; Pulumi; Crossplane; Ansible)
19. CI/CD для Java на K8s (GitLab CI/Jenkins/GitHub Actions: build→test→SAST/SCA→image→scan→push→deploy→canary; Argo Rollouts)
20. Chaos Engineering (Chaos Mesh, Litmus, game days, steady state, blast radius)

---

## 1. Linux/Shell основы (bash, SSH, top/htop, systemd, journalctl, ss/netstat, ulimit)

### Теория

- **POSIX-семантика**: каждый процесс имеет PID, PPID, UID/GID, file descriptors (stdin/stdout/stderr = fd 0/1/2), umask, env vars, exit code (0..255, 128+N — killed by signal N).
- **Bash**: builtins (`cd`, `export`, `set`, `source`); `set -euo pipefail` (errexit, nounset, pipefail) — стандарт «safe bash»; `trap '...' EXIT ERR INT`; `IFS`; `$?`, `$$`, `$!` (PID последнего фонового); `<<EOF` heredoc; `>(...)` process substitution; `[[ ]]` vs `[ ]`.
- **Пайпы и редиректы**: `cmd1 | cmd2` (stdout→stdin), `>` (truncate), `>>` (append), `2>` (stderr), `2>&1` (stderr→stdout), `&>` (оба потока), `/dev/null`, `tee`. FIFO через `mkfifo`.
- **SSH**: handshake (TCP → version exchange → KEX algorithm → server auth → user auth); config в `~/.ssh/config` (`Host`, `HostName`, `User`, `IdentityFile`, `ProxyJump`, `ControlMaster`); `~/.ssh/authorized_keys` форматы (ssh-rsa, ecdsa-sha2-nistp256, ssh-ed25519); `ssh-keygen -t ed25519 -a 100`; `ssh-agent` + `ssh-add`; port forwarding: `-L local:target` (local forward), `-R` (remote), `-D` (SOCKS); `ssh -J bastion target` (jump host); `sshd_config`: `PermitRootLogin no`, `PasswordAuthentication no`, `AllowUsers`, `ClientAliveInterval`.
- **top/htop**: load average (1/5/15 min) — сумма runnable + uninterruptible (D-state) процессов, нормируется на число CPU; `us/sy/ni/id/wa/si/hi/st` (%CPU по типам; `wa` = I/O wait, `st` = steal time на виртуалках); memory: `VIRT/RES/SHR` (VIRT — virtual address space, RES — resident RAM, SHR — shared); `i` (Irix/Solaris mode), `1` (expand CPUs), `H` (threads), `c` (full command), `P/M/T` (sort CPU/MEM/TIME).
- **systemd**: юниты `service/socket/timer/mount/target/path/slice/scope`; `systemctl start/stop/restart/status/enable/disable/is-enabled`; `journalctl -u unit -f --since "1h ago" -p err`; юнит-файл: `[Unit]`, `[Service]` (Type=simple/forking/oneshot/notify, ExecStart, ExecStop, Restart=on-failure, RestartSec, User/Group, Environment, LimitNOFILE, WorkingDirectory, PrivateTmp, NoNewPrivileges), `[Install]` (WantedBy=multi-user.target); `systemd-analyze blame`, `systemd-analyze critical-chain`; cgroups v2 — `systemd-cgls`, `systemd-cgtop`.
- **journalctl**: journald хранит логи в `/var/log/journal/` (persistent) или `/run/log/journal/` (volatile), бинарный формат; `-u`, `--since/--until`, `-p` (priority 0..7: emerg..debug), `-f` (follow), `--grep`, `-o json`, `-o cat`, `-k` (kernel), `_PID=`, `_SYSTEMD_UNIT=`. Persistence через `mkdir /var/log/journal && systemd-tmpfiles --create --prefix /var/log/journal`.
- **ss / netstat**: `ss -tlnp` (TCP listening, numeric, PID); `ss -tan state established`; `ss -s` (summary); `ss -m` (memory); `netstat` устарел, но полезен (`netstat -rn`, `netstat -i`). Состояния TCP: `LISTEN/SYN_SENT/SYN_RECV/ESTABLISHED/FIN_WAIT_1/FIN_WAIT_2/TIME_WAIT/CLOSE_WAIT/LAST_ACK/CLOSING/CLOSED`.
- **ulimit**: soft/hard limits через `ulimit -Sn` / `ulimit -Hn`; `ulimit -n` (open files), `-u` (processes), `-s` (stack), `-c` (core dump size), `-v` (virtual memory). Persistent через `/etc/security/limits.conf` (pam_limits) или systemd `LimitNOFILE=`, `LimitNPROC=`. Для Java-сервисов критично `LimitNOFILE=65536` (high load) и проверка через `cat /proc/<pid>/limits`.
- **/proc**: `/proc/<pid>/status` (VmRSS, VmSize, Threads), `/proc/<pid>/fd/` (open files), `/proc/<pid>/io` (rchar/wchar), `/proc/<pid>/cmdline`, `/proc/<pid>/smaps` (детальная память), `/proc/loadavg`, `/proc/cpuinfo`, `/proc/meminfo` (MemAvailable — лучше чем MemFree), `/proc/sys/` (sysctl — `vm.swappiness`, `net.core.somaxconn`, `net.ipv4.tcp_tw_reuse`).
- **strace/ltrace**: `strace -p <pid> -f -e trace=openat,read,write -s 256`; `strace -c` (statistics); `-T` (timing); `ltrace` для library calls. Полезно для «почему висит на I/O».
- **Основные CLI**: `grep` (`-r`, `-E`, `-A/-B/-C`, `-v`, `-i`, `--include`), `awk` (`'{print $1}'`), `sed` (`-i` in-place), `find` (`-name`, `-mtime`, `-exec`), `xargs` (`-0`, `-I{}`, `-P`), `curl` (`-sS`, `-w '%{http_code}'`, `--data-binary`, `-H`, `--resolve`, `-o`, `--retry`), `jq` (Turing-complete JSON), `tar`, `gzip/xz/zstd`, `rsync -avz --delete`, `nc/netcat`, `tcpdump -i eth0 -nn port 8080 -w cap.pcap`.
- **Файловые системы и права**: rwx для u/g/o, sticky bit (`/tmp`), setuid/setgid; `chmod 750`, `chown user:group`; ACL (`setfacl -m u:nobody:rx`); `stat`; `lsblk`, `df -h`, `du -sh *`, `inotifywait`; `mount`, `fstab`, `noatime`, `tmpfs`.
- **Сетевая диагностика**: `ip addr`, `ip route`, `ip neigh`, `ip link`, `traceroute`/`mtr` (better — показывает loss%), `dig` (`+short`, `+trace`, `@8.8.8.8`), `nslookup`, `ping -c 5`, `tcpdump`, `nmap -sS -p 1-65535`, `iperf3` (throughput), `ethtool`, `ss -i` (per-socket stats).
- **Locale/timezone/NTP**: `timedatectl set-timezone Europe/Moscow`, `timedatectl set-ntp true` (systemd-timesyncd); `localectl set-locale LANG=en_US.UTF-8`. Контейнеры часто запускаются в UTC — будьте внимательны с логами.

### Материалы

- **Книга**: "The Linux Command Line: A Complete Introduction", William Shotts (No Starch Press, 2nd ed., 2019) — бесплатный PDF: https://linuxcommand.org/tlcl.php
- **Книга**: "Linux Pocket Guide", Daniel J. Barrett (O'Reilly, 4th ed., 2020)
- **man pages**: `man bash`, `man systemd.service`, `man journalctl`, `man ss`, `man proc`
- **Статья**: "Safe Bash" — https://sipb.mit.edu/doc/safe-shell/
- **systemd docs**: https://www.freedesktop.org/software/systemd/man/systemd.service.html
- **Статья**: "Use the Unofficial Bash Strict Mode" — http://redsymbol.net/articles/unofficial-bash-strict-mode/
- **Дока**: "Linux Performance" Brendan Gregg — http://www.brendangregg.com/linuxperf.html

### Практика / Кейсы

1. **Java-сервис падает с `Too many open files` (EMFILE)**. Шаги: `cat /proc/$(pidof java)/limits | grep "open files"` — soft=1024; `ls /proc/$(pidof java)/fd | wc -l` — 1022; `lsof -p <pid> | awk '{print $NF}' | sort | uniq -c | sort -rn | head` — тысячи сокетов в `CLOSE_WAIT` к PostgreSQL; в коде — не закрыт `PreparedStatement` в цикле. Решение: фикс leak'а в коде + временно `prlimit --pid <pid> --nofile=65536:65536` (runtime) + persistent через systemd `LimitNOFILE=65536`. Документируй incident в postmortem.

2. **Поднять SSH bastion с MFA и audit-логированием**. Шаги: `sshd_config` — `PasswordAuthentication no`, `PubkeyAuthentication yes`, `AllowGroups ssh-users`, `PermitRootLogin no`, `AuthenticationMethods publickey,keyboard-interactive:pam` (pam_google_authenticator); `journalctl -u ssh -f` для аудита; настроить `ForceCommand` для логирования сессий через `tlog`; `ProxyJump bastion` в `~/.ssh/config` у разработчиков; ротация ключей через Vault SSH secrets engine.

3. **Поднимается high-load Java на bare metal — поднять лимиты через systemd**. Юнит `myapp.service`:
   ```ini
   [Service]
   Type=notify
   ExecStart=/opt/jdk/bin/java -XX:MaxRAMPercentage=75 -jar /opt/app/app.jar
   User=appuser
   Group=appgroup
   LimitNOFILE=200000
   LimitNPROC=4096
   TasksMax=4096
   Restart=on-failure
   RestartSec=5s
   WatchdogSec=60s
   AmbientCapabilities=CAP_NET_BIND_SERVICE
   NoNewPrivileges=true
   PrivateTmp=true
   ProtectSystem=strict
   ProtectHome=true
   ReadWritePaths=/var/lib/myapp /var/log/myapp
   ```
   + notify-socket через `sd_notify(0, "READY=1")` в Spring Boot Actuator (или `systemd-notify` wrapper). Измерить cold-start, watchdog перезапускает через 60s при зависании.

4. **Анализ TIME_WAIT storm на high-RPS API**. Симптом: `ss -tan state time-wait | wc -l` ~50k. Шаги: `cat /proc/sys/net/ipv4/ip_local_port_range` — узкий диапазон; поднять `net.ipv4.ip_local_port_range = 10000 65535`; включить `net.ipv4.tcp_tw_reuse = 1`; в Java-клиенте (OkHttp/Apache HttpClient) — pooled connection reuse + `keep-alive`; либо переход на HTTP/2 (один TCP, multiplexing). Контрольная проверка через `iperf3` и `ss -s`.

5. **Получить heap dump зависшего в production JVM без перезапуска**. Шаги: `top -p <pid> -H` найти hotspot thread; `jstack <pid> > thread-dump.txt`; если suspend безопасен — `jcmd <pid> GC.heap_dump /tmp/heap.hprof` (не останавливает JVM надолго, но создаёт паузу); альтернатива — `jmap -dump:live,format=b,file=/tmp/heap.hprof <pid>` (deprecated в JDK 18+, удаляется); скачать `sftp` → открыть в Eclipse MAT → `Leak Suspects Report` → найти доминатор (`HashMap$Node[]` с миллионом entries); исправить caching policy в Caffeine (`maximumSize`). Параллельно — `jcmd <pid> Thread.print` и `jcmd <pid> VM.native_memory` для off-heap.

6. **Логи systemd-юнита не persistent после reboot**. Симптом: `journalctl -u myapp --since yesterday` пуст. Шаги: `ls /var/log/journal/` — отсутствует; `mkdir -p /var/log/journal && systemd-tmpfiles --create --prefix /var/log/journal`; `systemctl restart systemd-journald`; проверить `journalctl --disk-usage`; настроить ротацию через `/etc/systemd/journald.conf`: `SystemMaxUse=2G`, `MaxRetentionSec=30day`. Документировать процедуру восстановления после инцидента.

7. **Башовый safe-mode скрипт деплоя с trap и rollback**. Скрипт `deploy.sh`:
   ```bash
   #!/usr/bin/env bash
   set -Eeuo pipefail
   IFS=$'\n\t'
   trap 'rm -rf "$TMP_DIR"; echo "FAILED at line $LINENO" >&2; exit 1' ERR
   TMP_DIR="$(mktemp -d)"
   # ... deploy steps ...
   trap - ERR
   rm -rf "$TMP_DIR"
   ```
   Покрыть скрипт `bats` тестами, прогонять в CI. Внедрить `shellcheck` в pre-commit hook.

---

## 2. Docker (Dockerfile instructions, multi-stage builds, layers cache, .dockerignore, BuildKit, distroless/slim, USER non-root, dive)

### Теория

- **Docker architecture**: client/server (`dockerd` daemon + `containerd` + `runc`); образ — набор read-only слоёв (layers), каждый слой = diff файловой системы (OverlayFS/Aufs); контейнер = образ + read-write слой (CoW — copy-on-write).
- **Dockerfile instructions**: `FROM` (базовый образ, `--platform=linux/amd64`), `LABEL` (метаданные OCI), `RUN` (выполнить в слое), `COPY`/`ADD` (COPY предпочтительнее, ADD умеет URL/tar auto-extract — обычно избегать), `WORKDIR`, `ENV`, `ARG` (build-time, не persist), `EXPOSE` (документация, не открывает порт), `USER` (переключение на non-root), `VOLUME` (точка монтирования), `ENTRYPOINT` vs `CMD` (ENTRYPOINT — fixed executable, CMD — default args, override-able), `HEALTHCHECK`, `SHELL`, `STOPSIGNAL` (default SIGTERM), `ONBUILD` (deprecated).
- **Shell vs exec form**: `RUN apt-get update` (shell — `/bin/sh -c "..."`), `RUN ["apt-get", "update"]` (exec — без shell, без signal-проблемы, но без переменных `&&`); для `ENTRYPOINT`/`CMD` всегда **exec form** (`["java", "-jar", "app.jar"]`) — иначе PID 1 = `/bin/sh`, который не пробрасывает SIGTERM к JVM → JVM не получает shutdown hook.
- **Multi-stage builds**: `FROM ... AS builder`, копирование артефакта через `COPY --from=builder /build/app.jar /app/app.jar`. Снижает финальный размер образа (build tools и JDK не попадают в runtime-образ), уменьшает attack surface.
- **Layer caching**: каждый `RUN`/`COPY` создаёт слой; docker кэширует слои по hash инструкции + содержимое COPY; изменение раннего слоя инвалидирует все последующие. Правило: «изменчивое — ниже, стабильное — выше» (сначала `pom.xml`/`build.gradle` + dependency resolution, потом `COPY src`). `--mount=type=cache,target=/root/.m2` (BuildKit) — maven/gradle cache между сборками без раздувания образа.
- **.dockerignore**: исключения для build context (например, `target/`, `.git`, `node_modules`, `*.log`, `.env`). Уменьшает context size → быстрее `docker build`, меньше шансов случайно закопировать секреты.
- **BuildKit**: новый backend (`DOCKER_BUILDKIT=1` или `docker buildx`), фичи: parallel multi-stage, `--mount=type=cache/secret/ssh`, `RUN --security=insecure` (sandboxed), inline `# syntax=docker/dockerfile:1.7` parser directive, `--platform` для multi-arch (`linux/amd64,linux/arm64`), `docker buildx build --push --platform ... -t registry/img:tag`.
- **Distroless / slim**:
  - `gcr.io/distroless/java17-debian12` — нет shell, package manager, только JRE + минимальный glibc; размер ~200MB; нельзя `docker exec sh`.
  - `eclipse-temurin:17-jre-alpine` — musl libc, ~80MB; могут быть проблемы с DNS (`musl` resolver vs glibc), `NativePRNG` в Java.
  - `amazoncorretto:17-alpine` — Amazon LTS JDK на Alpine.
  - Spring Boot layered jars (`spring-boot:layertools`) — каждый layer (dependencies/spring-boot-loader/snapshot-deps/application) отдельно для оптимального кэша.
- **USER non-root**: создавать пользователя в Dockerfile (`RUN groupadd -r app && useradd -r -g app app`), `USER 1001:1001` (числовой UID для совместимости с K8s `runAsUser`); для privileged port <1024 — `setcap` или `CAP_NET_BIND_SERVICE`.
- **dive** (github.com/wagoodman/dive): анализ layers образа — показывает что добавлено/удалено в каждом слое, размер, inefficiency score; помогает находить «почему образ 1.5GB».
- **Best practices**: один процесс на контейнер; `.dockerignore` всегда; `.env` не коммитить (secrets через Vault/Sealed Secrets); pin versions (`FROM eclipse-temurin:17.0.10_7-jre-alpine`, не `:latest`); `apt-get` cleanup в одном слое (`rm -rf /var/lib/apt/lists/*`); `dumb-init` или `tini` как PID 1 для signal forwarding; `HEALTHCHECK` для docker-compose; метки OCI (`org.opencontainers.image.source`, `.revision`, `.created`).
- **Image labels SBOM-relevant**: `org.opencontainers.image.{version,revision,created,license,title,description,documentation,source}` — стандартизировано в OCI Image Spec; используется для provenance (SLSA) и attestation.
- **Buildx / build cache backends**: inline cache (`--cache-from`), registry cache (`type=registry`), local cache (`type=local,dest=/tmp/cache`), GitHub Actions cache (`type=gha`).

### Материалы

- **Дока**: Dockerfile reference — https://docs.docker.com/engine/reference/builder/
- **Дока**: Multi-stage builds — https://docs.docker.com/build/building/multi-stage/
- **Дока**: BuildKit — https://docs.docker.com/build/buildkit/
- **Дока**: Best practices for writing Dockerfiles — https://docs.docker.com/develop/develop-images/dockerfile_best-practices/
- **Дока**: docker buildx — https://docs.docker.com/build/buildx/
- **Дока**: Spring Boot layered jars — https://docs.spring.io/spring-boot/reference/packaging/container-images.html#packaging.container-images.layered
- **Tool**: dive — https://github.com/wagoodman/dive
- **Статья**: Distroless — https://github.com/GoogleContainerTools/distroless
- **Книга**: "Docker Deep Dive", Nigel Poulton (napier.one, 2024)

### Практика / Кейсы

1. **Оптимизировать Spring Boot-образ с 1.2GB до 180MB через multi-stage + layered jars + distroless**. Шаги: декомпозировать build (`FROM maven:3.9-eclipse-temurin-17 AS build` → `mvn package`); `FROM eclipse-temurin:17-jre-jammy AS extract` → `java -Djarmode=layertools -jar app.jar extract`; `FROM gcr.io/distroless/java17-debian12` → `COPY --from=extract dependencies/ snapshot-dependencies/ spring-boot-loader/ application/ /app/` → `ENTRYPOINT ["java", "org.springframework.boot.loader.launch.JarLauncher"]`. Проверка через `dive` — inefficiency score <10%; тегирование по Git SHA + SemVer; подпись через cosign.

2. **Контейнер не получает SIGTERM при `docker stop` — JVM не завершает shutdown hooks**. Симптом: 10-секундная пауза, потом SIGKILL, данные не сохранены. Причина: `ENTRYPOINT java -jar app.jar` (shell form → PID 1 = sh). Фикс: `ENTRYPOINT ["java", "-jar", "app.jar"]` (exec form). Дополнительно: в K8s `terminationGracePeriodSeconds: 60` + Spring `server.shutdown.grace-period=30s`. Тест: `docker stop -t 0` должно сразу SIGTERM, проверить через `kill -SIGTERM $(docker inspect ...)`.

3. **BuildKit cache для Maven между сборками в CI (GitHub Actions)**. Шаги: `docker/build-push-action@v5` с `cache-from: type=gha` и `cache-to: type=gha,mode=max`; в Dockerfile — `RUN --mount=type=cache,target=/root/.m2 mvn -B package`; benchmark: build time 8min → 1.5min. Альтернатива для self-hosted runners — `type=local,dest=/tmp/.buildx-cache`. Включить attestation SBOM: `--provenance=true --sbom=true`.

4. **Multi-arch образы для ARM64 (Graviton/M1) и AMD64**. Шаги: `docker run --privileged --rm tonistiigi/binfmt --install all` (на build host); `docker buildx create --use --name multiarch --platform linux/amd64,linux/arm64`; `docker buildx build --platform linux/amd64,linux/arm64 -t registry/app:v1 --push .`; проверить через `docker manifest inspect registry/app:v1`. Для Java — `eclipse-temurin:17-jre` имеет оба arch; если есть native libs (RocksDB, jemalloc) — они должны быть multi-arch.

5. **Секреты в build — не закоммитить**. Антипаттерн: `ARG GITHUB_TOKEN` + `RUN git clone https://$GITHUB_TOKEN@github.com/...` — токен остаётся в истории слоёв (`docker history`). Правильно: BuildKit `RUN --mount=type=secret,id=github_token git clone https://$(cat /run/secrets/github_token)@github.com/...` — секрет не попадает в слой. В CI: `docker buildx build --secret id=github_token,env=GITHUB_TOKEN`. Дополнительно — `trivy fs --secret-config trivy-secret.yaml .` для скана на секреты в build context.

6. **Аудит образа через `dive` и slim down**. Шаги: `dive registry/app:v1` — видны слои, файлы; обнаружить что `RUN apt-get install -y curl wget vim` добавил 80MB; удалить; заменить на healthcheck через Spring Boot Actuator (HTTP); `RUN apt-get install --no-install-recommends -y ...` + cleanup; использовать `--squash` (experimental) для финального сжатия. Цель — <250MB для Spring Boot app.

7. **HEALTHCHECK и signal forwarding с tini/dumb-init**. Dockerfile:
   ```dockerfile
   FROM eclipse-temurin:17-jre-jammy
   RUN apt-get update && apt-get install -y --no-install-recommends tini && rm -rf /var/lib/apt/lists/*
   USER 1001:1001
   COPY --chown=1001:1001 app.jar /app/app.jar
   EXPOSE 8080
   HEALTHCHECK --interval=30s --timeout=3s --start-period=60s --retries=3 \
     CMD curl -fsS http://localhost:8080/actuator/health || exit 1
   ENTRYPOINT ["/usr/bin/tini", "--", "java", "-jar", "/app/app.jar"]
   ```
   tini пере forwards сигналы + reap zombies (если JVM спавнит подпроцессы). Проверить: `docker inspect --format '{{.State.Health.Status}}' container`.

---

## 3. Docker Compose (services, volumes, networks, healthcheck, override, profiles)

### Теория

- **compose.yaml** (Compose spec v2, предпочтительно `compose.yaml` вместо `docker-compose.yml`): top-level keys `services`, `volumes`, `networks`, `configs`, `secrets`, `include` (v2.20+).
- **services**: `image` / `build` (`context`, `dockerfile`, `args`, `target` для multi-stage), `container_name`, `hostname`, `environment` (list/map, `${VAR}`), `env_file`, `ports` (`"8080:80"` short, `target/published/protocol/mode` long, `mode: host` vs `ingress` в swarm), `expose` (only to linked services), `depends_on` (с `condition: service_healthy` / `service_started` / `service_completed_successfully` для init), `command`/`entrypoint`, `user`, `working_dir`, `restart` (no/always/unless-stopped/on-failure), `init` (tini), `tty`/`stdin_open`.
- **healthcheck**: `test: ["CMD", "curl", "-fsS", "http://localhost:8080/actuator/health"]`, `interval`, `timeout`, `retries`, `start_period`, `start_interval` (v2.16+). В `depends_on` можно ждать healthy.
- **volumes**: named (`mydata:` в `volumes:` top-level + `volumes: - mydata:/var/lib/postgresql/data`) vs bind mounts (`./config:/etc/app:ro`); `read_only: true` + `tmpfs: /tmp` для immutable. Volume drivers (`local`, `nfs`, `cifs`). `volume_driver:` deprecated в пользу top-level `volumes: driver: nfs`.
- **networks**: default bridge, custom networks (`driver: bridge` / `overlay` в swarm), `internal: true` (изолированная, без выхода в интернет), `enable_ipv6`, `driver_opts: com.docker.network.bridge.enable_icc`. Адресация: имена сервисов резолвятся через embedded DNS (`postgres:5432`).
- **override**: `docker compose -f compose.yaml -f compose.override.yaml up` — автоматическое слияние; override может переопределять/добавлять (для списка `ports` — заменяет, для `environment` — merge); для dev-окружения (`compose.override.yaml`) — добавляем hot reload, для prod — `compose.prod.yaml`.
- **profiles**: `profiles: [dev]` для сервисов, которые поднимаются только при `--profile dev`; удобно для тестовых/интеграционных баз, mailhog, mocks.
- **extends / include**: `extends:` (один сервис extends другой), `include:` (v2.20+, объединяет несколько compose-файлов) — модульность.
- **scale**: `docker compose up --scale worker=4` — поднимает 4 реплики; работает только если сервис не занимает фиксированный порт.
- **resources**: `deploy.resources.limits.cpus/memory` + `reservations` (в swarm — настоящие лимиты, в compose v2 — ограничения через cgroups).
- **secrets**: top-level `secrets:` (`file: ./secrets/db.txt` или `environment: MYSQL_PWD`), в сервисе `secrets: - db_password` → монтируется в `/run/secrets/db_password` read-only.
- **Команды**: `up -d`, `down -v` (удалить volumes), `restart`, `logs -f --tail=100`, `exec`, `run --rm` (one-off), `ps`, `top`, `events`, `config` (validate + render final config), `cp`, `stats`.

### Материалы

- **Дока**: Compose file reference — https://docs.docker.com/compose/compose-file/
- **Дока**: Compose CLI — https://docs.docker.com/compose/reference/
- **Дока**: Compose profiles — https://docs.docker.com/compose/how-tos/profiles/
- **Дока**: Compose override — https://docs.docker.com/compose/how-tos/multiple-compose-files/merge/
- **Spec**: Compose specification — https://github.com/compose-spec/compose-spec
- **Статья**: "Docker Compose for Java microservices" — https://www.baeldung.com/docker-compose
- **Книга**: "Docker in Action, Second Edition", Nickoloff/Liebowitz (Manning, 2019)

### Практика / Кейсы

1. **Полный dev-стек для Spring Boot + PostgreSQL + Redis + Kafka + LocalStack**. Структура `compose.yaml` с profiles: `dev` (только postgres + redis), `integration` (+kafka + zookeeper + localstack), `e2e` (+selenium chrome). Порядок через `depends_on` с `condition: service_healthy`. База инициализируется через `volumes: ./init:/docker-entrypoint-initdb.d:ro`. Kafka без Zookeeper — `confluentinc/cp-kafka:7.6` с KRaft. Hot reload для Spring Boot через `spring-boot-devtools` + bind mount `./target/classes:/app/BOOT-INF/classes`.

2. **Тестирование `depends_on: condition: service_healthy` + init-контейнер**. Сервис `app` ждёт `postgres` (healthcheck `pg_isready`), затем запускается `flyway-migrate` (`service_completed_successfully`), затем `app`. Проблема: Flyway должен выполниться до app, но это one-shot. Решение:
   ```yaml
   services:
     postgres:
       image: postgres:16
       healthcheck:
         test: ["CMD-SHELL", "pg_isready -U app"]
         interval: 3s
     migrate:
       image: myapp/migrate:latest
       depends_on:
         postgres: { condition: service_healthy }
       restart: "no"
     app:
       image: myapp:latest
       depends_on:
         migrate: { condition: service_completed_successfully }
   ```
   Для production — Flyway в init-container K8s, а не Compose.

3. **Compose override для dev с debug портом и горячим кодом**. `compose.yaml` (base) + `compose.override.yaml` (auto-loaded в dev):
   ```yaml
   services:
     app:
       environment:
         JAVA_TOOL_OPTIONS: "-agentlib:jdwp=transport=dt_socket,server=y,suspend=n,address=*:5005"
         SPRING_DEVTOOLS_RESTART_ENABLED: "true"
       ports: ["8080:8080", "5005:5005"]
       volumes:
         - ./target/classes:/app/BOOT-INF/classes:ro
   ```
   В prod — `compose.prod.yaml` без debug + `read_only: true` + `tmpfs: /tmp`.

4. **Multi-проектная сеть между Compose-стеками**. Два проекта (frontend + backend) в разных директориях с собственными `compose.yaml`. Подключить к общей external network:
   ```yaml
   networks:
     shared:
       name: platform-shared
       external: true
   ```
   Создать: `docker network create platform-shared`. Backend доступен из frontend как `http://backend:8080`. Альтернатива — `include:` для объединения.

5. **CI/CD pipeline с Compose для integration tests**. Шаги в GitLab CI: `docker compose -f compose.test.yaml up --abort-on-container-exit --exit-code-from=test` — `test` сервис запускает Testcontainers/JUnit, остальные зависимости поднимаются, exit code передаётся наружу; `docker compose down -v --remove-orphans` в `after_script`. Использовать named volumes с уникальным префиксом (`COMPOSE_PROJECT_NAME=ci-$CI_PIPELINE_ID`) для параллельных пайплайнов.

6. **Healthcheck с start_period и start_interval для медленного старта JVM**. Spring Boot с большим classpath — холодный старт 30-60s; healthcheck начинает проверять только через `start_period: 60s`, в течение которого `start_interval: 5s` (частые проверки) для быстрого определения готовности. После `start_period` — обычный `interval: 30s`. Без этого — restart loop если `retries: 3` срабатывает раньше старта.

7. **Секреты через `secrets:` (file-based) вместо ENV**. Compose:
   ```yaml
   secrets:
     db_password: { file: ./secrets/db.txt }
   services:
     app:
       secrets: [db_password]
       environment:
         SPRING_DATASOURCE_PASSWORD_FILE: /run/secrets/db_password
   ```
   В Spring Boot 3.1+ — `spring.config.import=optional:file:/run/secrets/`. Не попадает в `docker inspect` env, не логируется. В K8s — эквивалент через Secret + projected volume.

---

## 4. Docker registry (Docker Hub, Harbor, ECR/GAR, Trivy scan, cosign, SBOM)

### Теория

- **Registry**: сервер хранения Docker/OCI-образов; протокол OCI Distribution Spec v1.1 (referrers API для SBOM/attestations). Endpoints: `/v2/` (ping), `/v2/<name>/manifests/<tag>`, `/v2/<name>/blobs/<digest>`, `/v2/_catalog`.
- **Docker Hub**: публичный registry Docker Inc.; rate limits (anonymous 100/6h, authenticated 200/6h, Pro 5000/day); namespaces (`user/repo`); automated builds (deprecated, replace with GitHub Actions). Для production лучше self-hosted или cloud-managed (Rate Limit Hell).
- **Harbor** (CNCF graduated): self-hosted enterprise registry; фичи: RBAC, replication (между инстансами), vulnerability scanning (Trivy/Clair), policy (block on critical CVE), Helm chart repo (OCI), cosign signing verification, robot accounts, quota, garbage collection, OIDC auth.
- **ECR** (AWS): `aws ecr get-login-password | docker login --username AWS --password-stdin <acct>.dkr.ecr.<region>.amazonaws.com`; lifecycle policies (удалять untagged, old images); scan-on-push (Inspector/Trivy); immutable tags; cross-region replication; KMS encryption; cost: $0.10/GB-month storage + data transfer.
- **GAR** (Google Artifact Registry): замена GCR; multi-format (Docker, Maven, npm, Python, apt); regional repos; vulnerability scanning; IAM integration.
- **Trivy** (Aqua Security): сканер CVE для images, filesystem, repos, Kubernetes, IaC; `trivy image registry/app:v1`; severity filter (`--severity HIGH,CRITICAL`); `--ignore-unfixed` (только с патчем); `--exit-code 1` для CI fail; `trivy fs --scanners vuln,misconfig,secret .`; `.trivyignore` (exception list с обоснованием). Trivy Operator — сканer в K8s cluster.
- **Grype** (Anchore): альтернатива Trivy, быстрее на больших images; DB обновляется из NVD/OSV/GHSA.
- **Snyk**: commercial, глубокий reachability analysis (call graph), лучше для prioritization.
- **cosign** (Sigstore, CNCF): подпись образов через ECDSA P-256; ключи (`cosign sign --key cosign.key`) или keyless (`cosign sign --yes`, через OIDC + Rekor transparency log); верификация в K8s через admission controller (Kyverno/Cert-manager); supports `--attach sbom` для attestation. Transparency log (Rekor) — публичный аудит.
- **SBOM** (Software Bill of Materials): CycloneDX (XML/JSON, OWASP) или SPDX (Linux Foundation); генерация через `syft` (`syft registry/app:v1 -o cyclonedx-json`), `trivy image --format cyclonedx`; Maven plugin `org.cyclonedx:cyclonedx-maven-plugin`. Нужен для compliance (EO 14028 в США, EU CRA), vulnerability tracking, license audit.
- **SLSA** (Supply-chain Levels for Software Artifacts): уровни 1-4; L3 требует hermetic builds, provenance attestation; `slsa-github-generator` action для GitHub Actions.
- **Image signing policy**: через CUE policy (`cosign verify --certificate-identity-regexp ...`); в K8s — `kyverno` ClusterPolicy verifyImages с `mutateDigest: true`.
- **Garbage collection**: реестры накапливают untagged blobs; `docker registry garbage-collect --delete-untagged /etc/docker/registry/config.yml`. В Harbor — встроенный GC job. ECR/GAR — lifecycle policies.
- **Tagging strategy**: `:latest` запрещён в prod (mutating); использовать immutable tags `:v1.4.2`, `:1.4.2-${GIT_SHA}`, `:1.4-${BUILD_ID`; SemVer + Git SHA + build number. Multi-stage promotion: `dev` → `staging` → `prod` через retag с verification.

### Материалы

- **Дока**: Harbor — https://goharbor.io/docs/
- **Дока**: AWS ECR — https://docs.aws.amazon.com/AmazonECR/latest/userguide/what-is-ecr.html
- **Дока**: Google Artifact Registry — https://cloud.google.com/artifact-registry/docs
- **Дока**: OCI Distribution Spec — https://github.com/opencontainers/distribution-spec
- **Дока**: Trivy — https://aquasecurity.github.io/trivy/
- **Дока**: cosign — https://github.com/sigstore/cosign
- **Дока**: Syft (SBOM) — https://github.com/anchore/syft
- **Книга**: "Software Supply Chain Security", Cassie Crossley (O'Reilly, 2022)

### Практика / Кейсы

1. **Поднять Harbor в K8s с OIDC, replication и scanning**. Шаги: Helm chart `harbor/harbor`; configure OIDC (Keycloak); включить Trivy scanner; replication rule для pull-through cache из Docker Hub (обход rate limits); project policies: только signed images (cosign verification через Kyverno admission); lifecycle policy — auto-delete untagged через 7d;定期 GC через cronjob. Для Java-приложения: Maven artifacts тоже можно хранить как OCI artifacts в GAR/Harbor.

2. **Cosign signing в CI с keyless mode (Rekor)**. Шаги в GitHub Actions: `cosign sign --yes <registry/app@sha256:...>` (Git OIDC token); сохранение attestation SBOM: `cosign attach sbom --sbom sbom.cyclonedx.json <image>`; `cosign sign --yes --attachment sbom <image>`. В K8s deploy — Kyverno ClusterPolicy:
   ```yaml
   apiVersion: kyverno.io/v1
   kind: ClusterPolicy
   metadata: { name: verify-images }
   spec:
     validationFailureAction: Enforce
     rules:
     - name: verify-signature
       match: { resources: { kinds: [Pod] } }
       verifyImages:
       - imageReferences: ["registry.example.com/*"]
         attestors: [{ entries: [{ keyless: { issuer: "https://token.actions.githubusercontent.com", subject: "https://github.com/myorg/myrepo/.github/workflows/build.yml@refs/heads/main" } }] }]
   ```

3. **Trivy scan в CI с break build и exception process**. Pipeline: `trivy image --severity HIGH,CRITICAL --ignore-unfixed --exit-code 1 --format json -o scan.json registry/app:v1`; exceptions через `.trivyignore` (с комментарием-обоснованием + JIRA ticket ID + expiry date); weekly full scan (`trivy image --severity UNKNOWN,LOW,MEDIUM,HIGH,CRITICAL`) → отправка в DefectDojo; runtime scan через Trivy Operator в K8s. KPI: time-to-patch для critical CVE < 7 дней.

4. **Pull-through cache из Docker Hub → обход rate limits**. В Harbor: Registry → New Endpoint (Docker Hub) → Replication rule (Pull-based, filter `library/*`); в ECR — public registry cache через `PublicRegistryCatalog`. В docker daemon — `registry-mirror` в `/etc/docker/daemon.json`. Измерить: было 200 pulls/6h limit → unlimited через cache. Cache-TTL настраивать осторожно (security updates).

5. **SBOM generation с Maven plugin + CycloneDX + upload к image**. В `pom.xml`:
   ```xml
   <plugin>
     <groupId>org.cyclonedx</groupId>
     <artifactId>cyclonedx-maven-plugin</artifactId>
     <version>2.8.1</version>
     <executions><execution><phase>package</phase><goals><goal>makeBom</goal></goals></execution></executions>
   </plugin>
   ```
   В CI: `mvn package` → `target/bom.json`; `syft` для OS packages; объединить; `cosign attach sbom --sbom bom.json <image>`; в Dependency-Track (OWASP) — continuous analysis; alert на новые CVE для компонентов в BOM.

6. **Image promotion dev → staging → prod с подписью**. Шаги: build в CI → push в `registry/dev/app:${SHA}`; Trivy + cosign sign; promote в staging — `crane cp registry/dev/app@sha256:... registry/staging/app:v1.4.2-rc1` (digest-pinning); после UAT — promote в prod `registry/prod/app:v1.4.2` (immutable tag); верификация подписи перед каждым деплоем через ArgoCD + Kyverno. Никакого retagging — только copy по digest (гарантия идентичности).

7. **Аудит registry на устаревшие образы + cost optimization**. Скрипт: `crane ls registry/app | xargs -I{} crane config registry/app@{} | jq '.created'` — отсортировать по дате; ECR lifecycle policy:
   ```json
   {"rules":[{"rulePriority":1,"description":"keep last 30","selection":{"tagStatus":"tagged","countType":"imageCountMoreThan","countNumber":30},"action":{"type":"expire"}},{"rulePriority":2,"description":"expire untagged after 7d","selection":{"tagStatus":"untagged","countType":"sinceImagePushed","countUnit":"days","countNumber":7},"action":{"type":"expire"}}]}
   ```
   Quarterly review: какие CVE в старых образах, есть ли несанкционированные pushes (audit log в CloudTrail).

---

## 5. Kubernetes основы (Pod, Deployment, ReplicaSet, Service, ConfigMap, Secret, Labels, Probes liveness/readiness/startup, requests/limits, QoS)

### Теория

- **API architecture**: REST + JSON over HTTPS; resources (`apiVersion`, `kind`, `metadata`, `spec`, `status`); controllers (reconciliation loop — desired vs observed); controllers: deployment-controller, replicaset-controller, endpoint-controller, kube-scheduler, kubelet. etcd — single source of truth (Raft).
- **Pod**: минимальная unit деплоя; один или несколько контейнеров, разделяющих network namespace (`localhost`), IPC, volumes (не PID/filesystem по умолчанию); ephemeral IP; lifecycle: Pending → Running → Succeeded/Failed; init containers (run sequentially before main, must succeed), sidecar containers (K8s 1.28+ stable, restartPolicy: Always inside Pod), ephemeral containers (`kubectl debug`). Restart policy: `Always` (default, для Deployment), `OnFailure`, `Never` (для Job).
- **Deployment**: декларативное управление ReplicaSet; strategy `RollingUpdate` (maxSurge, maxUnavailable) / `Recreate`; `revisionHistoryLimit` (default 10); `progressDeadlineSeconds` (600s); `minReadySeconds`. Rollback: `kubectl rollout undo deployment/x --to-revision=3`. Pause/resume: `kubectl rollout pause`.
- **ReplicaSet**: поддерживает N реплик Pod; selector по labels (matchLabels/matchExpressions); Deployment создаёт/управляет ReplicaSet. В современных деплоях не используется напрямую.
- **Service**: стабильный IP + DNS поверх Pods; types: `ClusterIP` (default, internal), `NodePort` (30000-32767), `LoadBalancer` (cloud LB), `ExternalName` (CNAME DNS); `headless` (`clusterIP: None` — для StatefulSet, возвращает Pod IPs). Traffic через kube-proxy (iptables/IPVS mode). Endpoints/EndpointSlices — dynamic.
- **ConfigMap**: конфигурация как key-value; mount as `volume` (files) или `env`/`envFrom`; immutability (`immutable: true` в K8s 1.21+); размер до 1MiB (для большего — external store). Обновление ConfigMap: для mounted volumes — eventual (1min kubelet sync), для env — только после restart Pod.
- **Secret**: base64-encoded (не шифрование!); типы `Opaque`, `kubernetes.io/tls`, `kubernetes.io/dockerconfigjson`, `kubernetes.io/service-account-token`; encryption at rest через `EncryptionConfiguration` (KMS provider); RBAC для доступа; для prod — External Secrets Operator (sync из Vault/AWS Secrets Manager), Sealed Secrets (GitOps-friendly).
- **Labels & Annotations**: labels — key/value для selection (`app=api`, `tier=backend`), selector syntax `app=api,env!=prod,version in (v1,v2)`; annotations — неключевые метаданные (build SHA, runbook URL, `kubectl.kubernetes.io/last-applied-configuration`).
- **Probes**:
  - **liveness**: «жив ли контейнер?» — restart при failure; не должен проверять зависимости (PostgreSQL) — иначе cascade restart; endpoint `/actuator/health/liveness`.
  - **readiness**: «готов принимать трафик?» — убрать из Service endpoints при failure; может проверять зависимости.
  - **startup** (K8s 1.16+): «первый старт» — disable liveness/readiness пока не пройдёт; для медленных JVM (Spring Boot cold start 60s+). После первого success — liveness/readiness включаются.
  - Handlers: `httpGet` (path/port), `tcpSocket` (just connect), `exec` (command exit code). `initialDelaySeconds`, `periodSeconds`, `timeoutSeconds`, `successThreshold`, `failureThreshold`.
- **Requests & limits**: `resources.requests.{cpu,memory}` — гарантировано scheduler'ом (used for scheduling decisions); `resources.limits.{cpu,memory}` — максимум; CPU throttled (CFS quota), memory → OOMKill (`OOMKilled`) при превышении. CPU в millicores (`500m` = 0.5 CPU). Memory в bytes (`512Mi`).
- **QoS classes** (вычисляется автоматически):
  - **Guaranteed**: requests == limits (для всех контейнеров в Pod); последними evict'ятся при pressure.
  - **Burstable**: requests < limits; evict'ятся по priority.
  - **BestEffort**: без requests/limits; первыми evict'сятся.
- **CPU manager static policy** — позволяет pin whole CPUs для latency-sensitive workloads (`resources.requests.cpu` = integer + ` Guaranteed` QoS + `cpuManagerPolicy=static` на kubelet).
- **Pod lifecycle hooks**: `postStart` (sync, blocks container start), `preStop` (async, runs before SIGTERM). `terminationGracePeriodSeconds` (default 30s) — время между SIGTERM и SIGKILL.
- **Namespaces**: логическое разделение кластера; resourcequotas, limitranges, NetworkPolicies — namespace-scoped. `default`, `kube-system`, `kube-public`. Namespaces не дают security isolation — для этого separate clusters или vCluster/ Capsule.
- **kubectl**: `get/describe/apply/delete/edit/logs/exec/port-forward/label/annotate/rollout/scale/top/cordon/drain`. `kubectl explain pod.spec.containers.resources` — дока прямо из cluster. Plugins через krew.

### Материалы

- **Дока**: Pods — https://kubernetes.io/docs/concepts/workloads/pods/
- **Дока**: Deployments — https://kubernetes.io/docs/concepts/workloads/controllers/deployment/
- **Дока**: Services — https://kubernetes.io/docs/concepts/services-networking/service/
- **Дока**: ConfigMap — https://kubernetes.io/docs/concepts/configuration/configmap/
- **Дока**: Secrets — https://kubernetes.io/docs/concepts/configuration/secret/
- **Дока**: Configure Liveness, Readiness and Startup Probes — https://kubernetes.io/docs/tasks/configure-pod-container/configure-liveness-readiness-startup-probes/
- **Дока**: Managing Resources for Containers — https://kubernetes.io/docs/concepts/configuration/manage-resources-containers/
- **Дока**: Pod QoS — https://kubernetes.io/docs/concepts/workloads/pods/pod-qos/
- **Книга**: "Kubernetes: Up and Running, Third Edition", Burns/Hightower/Luedde (O'Reilly, 2022)
- **Книга**: "Kubernetes in Action", Marko Lukša (Manning, 2024, 2nd ed.)

### Практика / Кейсы

1. **Spring Boot deployment с правильными probes, resources и preStop**. Manifest:
   ```yaml
   apiVersion: apps/v1
   kind: Deployment
   metadata: { name: api, labels: { app: api, tier: backend } }
   spec:
     replicas: 3
     selector: { matchLabels: { app: api } }
     strategy:
       type: RollingUpdate
       rollingUpdate: { maxSurge: 1, maxUnavailable: 0 }
     template:
       metadata: { labels: { app: api, version: v1.4.2 } }
       spec:
         terminationGracePeriodSeconds: 60
         containers:
         - name: api
           image: registry/api@sha256:abc...
           ports: [{ containerPort: 8080 }]
           resources:
             requests: { cpu: 500m, memory: 768Mi }
             limits: { cpu: 1000m, memory: 1Gi }   # Burstable
           startupProbe:
             httpGet: { path: /actuator/health/liveness, port: 8080 }
             failureThreshold: 30
             periodSeconds: 5    # 150s на старт
           livenessProbe:
             httpGet: { path: /actuator/health/liveness, port: 8080 }
             periodSeconds: 10
             failureThreshold: 3
           readinessProbe:
             httpGet: { path: /actuator/health/readiness, port: 8080 }
             periodSeconds: 5
             failureThreshold: 2
           lifecycle:
             preStop:
               exec: { command: ["sh", "-c", "sleep 15"] }   # drain time for LB
           env:
           - name: SPRING_PROFILES_ACTIVE
             valueFrom: { configMapKeyRef: { name: api-config, key: profile } }
   ```
   Цель: zero-downtime rolling deploy. `preStop sleep 15` — даёт kube-proxy убрать Pod из endpoints до SIGTERM. Spring `server.shutdown.grace-period=30s`.

2. **JVM в контейнере: GC tuning под limits**. По умолчанию JVM видит cgroup limits (Java 10+, `+UseContainerSupport`); но `-Xmx` всё равно рекомендуется явно через `-XX:MaxRAMPercentage=75.0 -XX:InitialRAMPercentage=50.0 -XX:MinRAMPercentage=75.0`. Не использовать `-Xmx1g` в контейнере с `memory.limit: 1Gi` — оставь headroom для metaspace/threads/native memory, иначе OOMKilled при прямых аллокациях (Netty direct buffers, JNI). Для latency-sensitive — G1GC: `-XX:+UseG1GC -XX:MaxGCPauseMillis=200 -XX:+ParallelRefProcEnabled`. Включить GC logs: `-Xlog:gc*=info:file=/tmp/gc.log:time,uptime,level,tags:filecount=5,filesize=10M`.

3. **ConfigMap hot-reload без перезапуска Pod**. ConfigMap mounted as volume — обновляется автоматически (1min sync), но приложение должно читать файл. Spring Boot: `spring.config.import=optional:file:/etc/config/` + `spring.cloud.kubernetes.config` (Spring Cloud Kubernetes) или `Reloader` (Stakater) — auto-restart на ConfigMap change. Альтернатива — sidecar с inotify + `kill -HUP` на основной процесс.

4. **OOMKilled при burst нагрузке — анализ и фикс**. Симптом: Pod в статусе `OOMKilled`, restart. Шаги: `kubectl describe pod` → `Last State: OOMKilled, Exit Code 137`; `kubectl top pod` показывает что уперлись в limit 1Gi; heap dump перед смертью через `-XX:+HeapDumpOnOutOfMemoryError -XX:HeapDumpPath=/tmp/`; анализ в MAT — leak в `ConcurrentHashMap` cache. Решение: либо поднять limit, либо фиксить leak, либо добавить Caffeine `maximumSize`. Для off-heap (Netty direct) — `-Djdk.nio.maxCachedBufferSize=262144` и `MaxDirectMemorySize`.

5. **CPU throttling на latency-sensitive Java**. Симптом: p99 latency всплески при CPU utilization 70%. Причина: `limits.cpu: 1000m` + CFS throttling. Анализ: `kubectl top pod` + `crictl stats`; node-level `cat /sys/fs/cgroup/cpu/.../cpu.stat` — `nr_throttled` растёт. Решение: либо убрать CPU limit (оставить только request), либо поднять limit в 2-3 раза от request, либо `cpuManagerPolicy=static` с integer CPU request. Для latency-critical — Guaranteed QoS + static CPU manager.

6. **Multi-container Pod: app + filebeat sidecar для логов**. Когда нет stdout (например, файлы логов): app пишет в `/var/log/app/app.log`, sidecar filebeat читает и отправляет в ELK. Shared volume `emptyDir`. В K8s 1.28+ sidecar containers (`restartPolicy: Always`) запускаются перед main и останавливаются после — корректный логгинг при shutdown. Альтернатива — app пишет в stdout (рекомендация 12-factor), K8s log aggregator (Fluent Bit DaemonSet) собирает.

7. **Migration от InsecureSecret (base64) к External Secrets Operator**. Шаги: установить ESO; создать `SecretStore` (Vault backend) / `ClusterSecretStore`; `ExternalSecret` ссылается на ключ в Vault → ESO создаёт/обновляет native K8s Secret; в Deployment env ссылается на K8s Secret как обычно. GitOps: в Git только `ExternalSecret` (без значений), реальные секреты в Vault. RBAC: Vault policy на path `secret/data/myapp/*` для K8s service account через JWT auth.

---

## 6. Kubernetes продолжение (StatefulSet, DaemonSet, Job/CronJob, HPA/VPA, init containers, sidecar, affinity/taints/tolerations, PDB)

### Теория

- **StatefulSet**: для stateful workloads (базы, очереди); стабильные имена `app-0, app-1, ...` + stable DNS (`app-0.app-svc.ns.svc.cluster.local`); stable PVC (`volumeClaimTemplates`); ordered deploy/scale (parallel pod management через `podManagementPolicy: Parallel`); headless Service (`clusterIP: None`) для прямого доступа к Pod. `updateStrategy: RollingUpdate` (partition для canary) / `OnDelete`. Подходит для Cassandra, Kafka (через Strimzi), ZooKeeper, Elasticsearch, PostgreSQL (CloudNativePG/Patroni).
- **DaemonSet**: один Pod на node; use cases: logging agents (Fluent Bit), node exporters (node-exporter, Datadog), CNI plugins, storage CSI node plugins, security agents (Falco). `updateStrategy: RollingUpdate` (maxUnavailable, maxSurge K8s 1.21+) / `OnDelete`. Tolerations для запуска на master/ tainted nodes.
- **Job**: run-to-completion Pod; `completions` (parallel jobs), `parallelism`, `backoffLimit` (default 6), `activeDeadlineSeconds`, `ttlSecondsAfterFinished` (auto-cleanup). `suspend` для паузы. Patterns: 1) single task, 2) fixed-completion, 3) work queue (один Pod создаёт work items, рабочие забирают из очереди).
- **CronJob**: Job по расписанию (cron format `m h dom mon dow`, с секундами через `seconds=` если включено); `concurrencyPolicy: Allow/Forbid/Replace`; `startingDeadlineSeconds`; `successfulJobsHistoryLimit` / `failedJobsHistoryLimit` (default 3/1). Use cases: backups, batch reports, cleanup. Важно: таймзона — `timeZone` field (K8s 1.25+), иначе UTC.
- **HPA** (Horizontal Pod Autoscaler): масштабирование по метрикам; v2 (K8s 1.23+) — multiple metrics; metrics: Resource (`cpu`, `memory`), Pods (custom), Object (Ingress, external); `minReplicas`, `maxReplicas`, `target CPU utilization` (default 80%); требует Metrics Server (resource) или KEDA (custom/external). Cooldown: `--horizontal-pod-autoscaler-downscale-stabilization-window` (default 5min) — защищает от thrashing.
- **VPA** (Vertical Pod Autoscaler): рекомендует/применяет requests; modes: `Off` (рекомендации), `Initial` (только при создании), `Auto`/`Recreate` (restart Pod). Конфликт с HPA на CPU/memory — использовать VPA в `Off` режиме (для рекомендаций) + HPA для scaling. In-place vertical scaling (K8s 1.27 alpha, beta в 1.33) — без restart.
- **KEDA** (CNCF): event-driven autoscaling; scalers: Kafka, RabbitMQ, AWS SQS, Redis, Prometheus, cron; `ScaledObject` (Deployment) / `ScaledJob` (Job); scale-to-zero для Serverless. Для Java-консьюмеров Kafka — scale по lag.
- **init containers**: запускаются последовательно перед main контейнерами, должны успешно завершиться; use cases: DB migrations (Flyway), download config, wait for dependencies, init volumes (`chown`). Отличие от sidecar: init завершается, sidecar (1.28+) живёт вместе с main.
- **Sidecar containers** (K8s 1.28 stable): `restartPolicy: Always` в `initContainers` — стартует перед main, живёт всё время, останавливается после main; решает проблему порядка запуска/остановки. Use cases: service mesh proxy (Istio/Linkerd), log forwarding, vault agent.
- **Pod affinity/anti-affinity**:
  - `requiredDuringScheduling` / `preferredDuringScheduling` (hard/soft).
  - `podAntiAffinity` — spread Pods по nodes (HA: topologyKey `kubernetes.io/hostname` или `topology.kubernetes.io/zone`).
  - `podAffinity` — colocation (app + cache на одной node).
- **Node affinity** (заменяет `nodeSelector`): `requiredDuringSchedulingIgnoredDuringExecution` / `preferredDuringSchedulingIgnoredDuringExecution`; matchExpressions на labels (`disktype=ssd`, `gpu=true`).
- **Taints & tolerations**: `kubectl taint nodes node1 key=value:NoSchedule/NoExecute/PreferNoSchedule`; toleration позволяет Pod'у быть scheduled на tainted node. Use cases: dedicated GPU nodes, master nodes (`node-role.kubernetes.io/control-plane`), on-prem vs cloud nodes.
- **Topology Spread Constraints** (K8s 1.19+): более гибкая альтернатива anti-affinity; `maxSkew` — максимальный дисбаланс; `whenUnsatisfiable: DoNotSchedule/ScheduleAnyway`; `topologyKey` (zone/hostname); можно несколько constraints (zone spread + rack spread).
- **PDB** (PodDisruptionBudget): `minAvailable` или `maxUnavailable` — защита от добровольных evictions (drain); работает с `kubectl drain`, не с node failure. `kubectl drain node1 --ignore-daemonsets --delete-emptydir-data`.
- **PriorityClass**: приоритет Pod'а; `preemptionPolicy: PreemptLowerPriority`; `system-node-critical`, `system-cluster-critical` (K8s system), пользовательские. Pod'ы с высоким priority могут вытеснить низкоприоритетные при resource pressure.

### Материалы

- **Дока**: StatefulSets — https://kubernetes.io/docs/concepts/workloads/controllers/statefulset/
- **Дока**: DaemonSet — https://kubernetes.io/docs/concepts/workloads/controllers/daemonset/
- **Дока**: Jobs — https://kubernetes.io/docs/concepts/workloads/controllers/job/
- **Дока**: CronJob — https://kubernetes.io/docs/concepts/workloads/controllers/cron-jobs/
- **Дока**: HPA — https://kubernetes.io/docs/tasks/run-application/horizontal-pod-autoscale/
- **Дока**: VPA — https://github.com/kubernetes/autoscaler/tree/master/vertical-pod-autoscaler
- **Дока**: Taints and Tolerations — https://kubernetes.io/docs/concepts/scheduling-eviction/taint-and-toleration/
- **Дока**: Topology Spread Constraints — https://kubernetes.io/docs/concepts/scheduling-eviction/topology-spread-constraints/
- **Дока**: PodDisruptionBudget — https://kubernetes.io/docs/concepts/workloads/pods/disruptions/
- **Дока**: KEDA — https://keda.sh/docs/
- **Книга**: "Production Kubernetes", Josh Rosso et al. (O'Reilly, 2021)

### Практика / Кейсы

1. **HA deployment с topology spread + PDB + anti-affinity**. Гарантировать что 3 реплики распределены по 3 зонам, и при voluntary disruption остаётся минимум 2. Manifest:
   ```yaml
   spec:
     replicas: 3
     template:
       spec:
         topologySpreadConstraints:
         - maxSkew: 1
           topologyKey: topology.kubernetes.io/zone
           whenUnsatisfiable: DoNotSchedule
           labelSelector: { matchLabels: { app: api } }
         affinity:
           podAntiAffinity:
             preferredDuringSchedulingIgnoredDuringExecution:
             - weight: 100
               podAffinityTerm: { topologyKey: kubernetes.io/hostname, labelSelector: { matchLabels: { app: api } } }
   ---
   apiVersion: policy/v1
   kind: PodDisruptionBudget
   metadata: { name: api-pdb }
   spec: { minAvailable: 2, selector: { matchLabels: { app: api } } }
   ```
   При `kubectl drain` — PDB не даст evict 2-й Pod одновременно, drain зависнет; в `/var/log/kube-scheduler.log` видна причина.

2. **Kafka через Strimzi с anti-affinity по rack**. Strimzi Kafka operator: `Kafka` CR с `rack.topologyKey: topology.kubernetes.io/zone`; `podAntiAffinity` через `template.pod.antiAffinity`; `volumeClaimTemplates` для каждого broker; JBOD storage. Replication factor 3, `min.insync.replicas=2`. При zone failure — оставшиеся брокеры продолжают работать.

3. **HPA + KEDA для Kafka consumer (Java)**. Консьюмер с lag в Kafka топике. KEDA `ScaledObject`:
   ```yaml
   triggers:
   - type: kafka
     metadata:
       bootstrapServers: kafka:9092
       consumerGroup: my-app
       topic: events
       lagThreshold: "1000"
   ```
   HPA масштабирует от 1 до 20 реплик по lag. KEDA scale-to-zero ночью при отсутствии сообщений. Внимание: JVM cold start 30-60s — установить `cooldownPeriod` достаточно большим; рассмотреть GraalVM native image для sub-second startup.

4. **CronJob для nightly DB backup в S3**. `CronJob` с расписанием `0 2 * * *` (02:00 UTC), `timeZone: Europe/Moscow`; Job `pg_dump | gzip | aws s3 cp - s3://...`; `concurrencyPolicy: Forbid` (не запускать если предыдущий ещё идёт); `ttlSecondsAfterFinished: 86400` (clean через 24h); `successfulJobsHistoryLimit: 7`, `failedJobsHistoryLimit: 3`; `activeDeadlineSeconds: 3600` (kill через 1h). Алёрт при `failed > 0` через Prometheus.

5. **Init container с Flyway migrations + retry**. Pattern: init container `migrate` запускается перед main app; `flyway migrate`; если БД ещё не готова — retry с exponential backoff. K8s native: `restartPolicy: Always` (для Deployment), init container будет перезапущен. Для Idempotent migrations — `flyway.repair` + checksum validation. Внимание: при parallel deployment нескольких реплик — только один init должен мигрировать; через Lease lock (`coordination.k8s.io/v1 Lease`) или external lock (PostgreSQL advisory lock `pg_advisory_lock`).

6. **Sidecar vault-agent для секретов с auto-renewal**. HashiCorp Vault Agent Injector (mutating webhook) — аннотации на Pod: `vault.hashicorp.com/agent-inject: "true"`, `vault.hashicorp.com/role: "my-app"`, `vault.hashicorp.com/agent-inject-secret-db: "database/creds/my-app"`. Agent sidecar извлекает secret, пишет в `/vault/secrets/db`, periodic renewal. App читает файл. Для Java — Spring Cloud Vault интеграция напрямую (без sidecar).

7. **Migration StatefulSet из in-tree storage в CSI с zero downtime**. Шаги: 1) добавить новый CSI StorageClass, 2) развернуть новый StatefulSet с тем же образом, но новым storage, 3) синхронизировать данные (rsync/pitr для БД), 4) переключить Service на новый StatefulSet, 5) удалить старый. Для PostgreSQL — использовать CloudNativePG `Cluster` с replica promotion, для Kafka — Strimzi `KafkaRebalance`.

---

## 7. Kubernetes networking (Service types, Ingress, Ingress controllers, NetworkPolicy, DNS, CNI Calico/Cilium)

### Теория

- **Networking model K8s**: каждый Pod имеет свой IP (flat, routable); Pods могут общаться с Pods на любых nodes без NAT; nodes могут общаться с Pods без NAT; Pod IP нестабилен (жизнь Pod'а).
- **Service**: stable virtual IP (ClusterIP) + DNS; kube-proxy (iptables/IPVS) реализует load balancing между Pod endpoints; session affinity (`sessionAffinity: ClientIP`); `externalTrafficPolicy: Local/Cluster` (Local — preserve source IP, не балансирует между nodes).
- **Service types**: `ClusterIP` (internal only), `NodePort` (30000-32767), `LoadBalancer` (cloud LB, в AWS — Classic/Network LB), `ExternalName` (CNAME to external DNS). `LoadBalancer` с annotations для cloud-specific (AWS NLB, GCP, Azure).
- **Ingress**: L7 routing (HTTP/HTTPS); `Ingress` resource + Ingress controller (nginx-ingress, Traefik, HAProxy, Contour, ALB); path-based (`/api → api-svc`), name-based virtual host (`api.example.com → api-svc`); TLS termination (`tls: []` + cert-manager). IngressClass (`ingressClassName: nginx`).
- **Ingress controllers**:
  - **ingress-nginx** (community, kubernetes.github.io): nginx + Lua; ~80% рынка; annotations-based config.
  - **Traefik**: Go-native, auto-config from labels, Let's Encrypt integration.
  - **HAProxy Ingress**:高性能, granular config.
  - **Contour** (Project Contour, VMware): Envoy-based.
  - **AWS ALB Ingress Controller** (now `aws-load-balancer-controller`): native ALB.
- **NetworkPolicy**: firewall на L3/L4 для Pods; default allow-all (если нет policy); policy с `podSelector` → isolate Pod (must explicitly allow). Ingress/Egress rules с `from`/`to` (`podSelector`, `namespaceSelector`, `ipBlock`, `ports`). Требует CNI с поддержкой (Calico, Cilium, Weave Net).
- **DNS**: CoreDNS (`kube-dns` в старых версиях); A/AAAA records для Services (`my-svc.ns.svc.cluster.local`); SRV для named ports (`_port._tcp.my-svc...`); Pods — `pod-ip-dashed.ns.pod.cluster.local`. `ndots: 5` в `/etc/resolv.conf` Pod'а → множественные lookup'ы; production — `dnsPolicy: ClusterFirst` или `None` с custom config; NodeLocal DNSCache для уменьшения load.
- **CNI (Container Network Interface)**:
  - **Calico** (Tigera): BGP-based, L3 routing; eBPF dataplane для high performance; NetworkPolicy + GlobalNetworkPolicy; Istio integration.
  - **Cilium** (Isovalent, CNCF graduated): eBPF-based, kernel-level filtering без iptables; L7 policies (HTTP, Kafka); Hubble observability; service mesh (Cilium Service Mesh); cluster mesh.
  - **Flannel**: simple, L2 overlay (VXLAN), без NetworkPolicy (нужен Calico plugin).
  - **Antrea**: OVS-based, VMware.
  - **AWS VPC CNI**: native VPC IPs для Pods (no overlay); ограничение по числу ENI/IP; `WARM_ENI_TARGET`.
  - **Azure CNI**: similar.
- **eBPF**: kernel bytecode; позволяет Cilium/Calico заменять kube-proxy (Cilium kube-proxy replacement), Conntrack, NetworkPolicy в kernel space — без context switch; значительно выше throughput и ниже latency.
- **Gateway API** (CNCF, successor of Ingress): `GatewayClass`, `Gateway`, `HTTPRoute`, `TCPRoute`, `GRPCRoute`; role separation (infra provider vs app developer); namespace cross-refs; route delegation. GKE/EKS поддерживают нативно.
- **DNS resolution performance**: `ndots: 5` → 5 DNS lookups для single-label names (попытки append search domains); fix — FQDN (`postgres.ns.svc.cluster.local.`) или `dnsConfig.options: - name: ndots, value: "1"`. CoreDNS + NodeLocal DNSCache (DaemonSet) — устраняет single point of failure.
- **IPv4/IPv6 dual-stack** (K8s 1.21+ GA): Pod с двумя IP; Services с `ipFamilyPolicy: PreferDualStack`.
- **Service Mesh data plane**: Envoy (Istio, Consul), Linkerd2-proxy (Rust), Cilium. Sidecar intercept (iptables redirect) или Cilium sidecarless.

### Материалы

- **Дока**: Service — https://kubernetes.io/docs/concepts/services-networking/service/
- **Дока**: Ingress — https://kubernetes.io/docs/concepts/services-networking/ingress/
- **Дока**: Ingress Controllers — https://kubernetes.io/docs/concepts/services-networking/ingress-controllers/
- **Дока**: NetworkPolicies — https://kubernetes.io/docs/concepts/services-networking/network-policies/
- **Дока**: DNS for Services and Pods — https://kubernetes.io/docs/concepts/services-networking/dns-pod-service/
- **Дока**: Cluster Networking — https://kubernetes.io/docs/concepts/cluster-administration/networking/
- **Дока**: Calico — https://docs.tigera.io/calico
- **Дока**: Cilium — https://docs.cilium.io/
- **Дока**: Gateway API — https://gateway-api.sigs.k8s.io/
- **Книга**: "Kubernetes Networking and Service Mesh", Satyajit Pany, Rahul Jadhav (Apress, 2024)

### Практика / Кейсы

1. **Zero-trust NetworkPolicy для Spring Boot микросервисов**. Default deny all ingress/egress:
   ```yaml
   apiVersion: networking.k8s.io/v1
   kind: NetworkPolicy
   metadata: { name: default-deny-all, namespace: prod }
   spec: { podSelector: {}, policyTypes: [Ingress, Egress] }
   ```
   Затем explicit allow:
   ```yaml
   spec:
     podSelector: { matchLabels: { app: api } }
     policyTypes: [Ingress, Egress]
     ingress:
     - from: [{ namespaceSelector: { matchLabels: { name: ingress-nginx } } }]
       ports: [{ protocol: TCP, port: 8080 }]
     egress:
     - to: [{ podSelector: { matchLabels: { app: postgres } } }]
       ports: [{ port: 5432 }]
     - to: [{ namespaceSelector: {} }]  # DNS
       ports: [{ protocol: UDP, port: 53 }]
     - to: [{ ipBlock: { cidr: 0.0.0.0/0 } }]  # external APIs (временно)
       ports: [{ port: 443 }]
   ```
   Тестировать через `kubectl exec` + `curl`/`nc` — должны проходить только разрешённые соединения.

2. **Ingress-nginx + cert-manager + TLS для нескольких доменов**. cert-manager `ClusterIssuer` (Let's Encrypt); `Certificate` per domain; Ingress с `cert-manager.io/cluster-issuer: letsencrypt-prod` annotation → auto-issue cert. Rate limits: использовать staging issuer для тестов. HSTS, OCSP stapling. Для wildcard — DNS-01 challenge (Route53/CloudDNS). HTTP → 301 → HTTPS.

3. **Migration Ingress → Gateway API**. Шаги: установить Gateway API CRDs + контроллер (envoy-gateway, Cilium, Contour); создать `GatewayClass` + `Gateway` (listeners на 80/443); `HTTPRoute` для app routing; постепенный migration через фильтр Host header. Преимущества: role separation, richer routing (header matches, request mirroring для canary), cross-namespace route references.

4. **Cilium + Hubble observability для L7 visibility**. Установка Cilium как CNI (или secondary); enable Hubble Relay + UI (`cilium hubble enable --ui`); `hubble observe --pod prod/api --follow` — видеть все connections в реальном времени; L7 HTTP flows (если включено L7 policy); export в Prometheus (`hubble-metrics`) для dashboard Grafana. Замена service mesh (Cilium Service Mesh) — без sidecar, через eBPF.

5. **DNS performance issue: high `ndots` → 5x lookups**. Симптом: p99 latency на service-to-service calls spikes; `tcpdump port 53` показывает 5 DNS queries per call. Fix: FQDN (`postgres.ns.svc.cluster.local.`) или `dnsConfig: options: [{ name: ndots, value: "2" }]` в Pod spec; NodeLocal DNSCache DaemonSet для cache; CoreDNS `autopath` plugin (deprecated). Мониторить `coredns_dns_requests_total`.

6. **Calico BGP peering с on-prem router для Pod IP routability**. On-prem cluster, нужно напрямую обращаться к Pod IPs из сети. Calico BGP config: `BGPConfiguration` с `asNumber`, `BGPPeer` с router IP; router (Cisco/Juniper) — peer с Calico node; Pod IPs анонсируются в BGP; без overlay, без NAT. Внимание: Pod CIDR планирование (足够 IP для кластера).

7. **AWS VPC CNI: не хватает IPs в subnet**. Симптом: Pods Pending с `FailedScheduling: 0/5 nodes are available: insufficient IP addresses`. Шаги: `kubectl describe node` → `Allocated resources` показывают IP usage; увеличить subnet size или включить `prefix delegation` (assign /28 per ENI); `WARM_PREFIX_TARGET: 1`; для IPv6 — VPC CNI supports IPv6 (но требует dual-stack VPC). Monitor через `awscni_total_ip_addresses`.

---

## 8. Kubernetes storage (PV/PVC, StorageClass, CSI, StatefulSet volumeClaimTemplates)

### Теория

- **PV (PersistentVolume)**: cluster-scoped resource, представляет piece of storage (provisioned admin'ом statically или dynamically); `capacity`, `accessModes` (`ReadWriteOnce`, `ReadOnlyMany`, `ReadWriteMany`, `ReadWriteOncePod` K8s 1.22+), `persistentVolumeReclaimPolicy` (`Retain`, `Delete`, `Recycle` deprecated), `storageClassName`, `volumeMode` (`Filesystem` default, `Block`).
- **PVC (PersistentVolumeClaim)**: user request for storage; namespace-scoped; `resources.requests.storage`, `accessModes`, `storageClassName`, `selector`. PVC bound to PV matching criteria.
- **StorageClass**: dynamic provisioning; `provisioner` (CSI driver name, e.g. `ebs.csi.aws.com`); `parameters` (disk type, IOPS, encryption); `reclaimPolicy`, `volumeBindingMode` (`Immediate` vs `WaitForFirstConsumer` — важно для multi-AZ topology), `allowVolumeExpansion`, `mountOptions`.
- **CSI (Container Storage Interface)**: standard API для storage drivers; out-of-tree (раньше был in-tree в kubelet); drivers: AWS EBS, EFS, Azure Disk/File, GCE PD, Ceph RBD/CephFS, NFS, Longhorn, OpenEBS, Portworx, TopoLVM. CSI sidecars: `external-provisioner`, `external-attacher`, `external-snapshotter`, `external-resizer`, `node-driver-registrar`, `liveness-probe`.
- **Volume types** (in-tree, gradually replaced by CSI):
  - `emptyDir` — scratch space, исчезает с Pod, можно на tmpfs/ramdisk (`medium: Memory`).
  - `hostPath` — монтирует host path (security risk, только для DaemonSets).
  - `configMap`/`secret` — read-only mounts.
  - `nfs` — network file system.
  - `persistentVolumeClaim` — most common для stateful.
  - `projected` — combine multiple sources (ConfigMap + Secret + DownwardAPI).
  - `ephemeral` CSI volumes (inline CSI) — short-lived.
- **Access modes** (важно):
  - `RWO` (ReadWriteOnce) — один node (большинство block storage, EBS).
  - `ROX` (ReadOnlyMany) — много nodes только на чтение.
  - `RWX` (ReadWriteMany) — много nodes read/write (NFS, CephFS, EFS, GlusterFS) — нужно для shared storage.
  - `RWOP` (ReadWriteOncePod) — один Pod (K8s 1.22+) — для single-writer гарантий.
- **StatefulSet volumeClaimTemplates**: каждый Pod получает свой PVC (`data-app-0`, `data-app-1`); PVC persistent across Pod restart; stable PVC names + stable identity. Для Cassandra/Kafka/PostgreSQL — replica都有自己的 данные.
- **Volume snapshots**: `VolumeSnapshotClass`, `VolumeSnapshot`, `VolumeSnapshotContent`; CSI `external-snapshotter`; use для backups, clones. `kubectl get volumesnapshot`.
- **Volume expansion**: `allowVolumeExpansion: true` в StorageClass; `kubectl edit pvc` увеличить `requests.storage`; resize происходит online для некоторых CSI (EBS, Ceph RBD).
- **CSI drivers для Java workloads**:
  - PostgreSQL (CloudNativePG): WAL archive в S3 через WAL-G; PVC для data, snapshots для PITR.
  - Kafka (Strimzi): JBOD volumes, multiple PVCs per broker.
  - Elasticsearch: hot/warm/cold tiers с разными StorageClass (SSD для hot, HDD для cold).
- **Backup strategies**:
  - **Application-consistent**: Quiesce app (e.g. `pg_dump`, `xtrabackup`), затем snapshot.
  - **Crash-consistent**: Volume snapshot без quiesce — может быть inconsistent (для БД — восстановление через WAL replay).
  - **Velero**: cluster backup (resources + PV snapshots); restore в другой cluster для DR.
  - **Kasten K10**: enterprise K8s backup.
- **Topology-aware volume provisioning**: `WaitForFirstConsumer` binding — PV создаётся в той же AZ, где Pod scheduled; критично для multi-AZ cluster + zonal storage (EBS).

### Материалы

- **Дока**: Persistent Volumes — https://kubernetes.io/docs/concepts/storage/persistent-volumes/
- **Дока**: Storage Classes — https://kubernetes.io/docs/concepts/storage/storage-classes/
- **Дока**: CSI — https://kubernetes-csi.github.io/docs/
- **Дока**: Volume Snapshots — https://kubernetes.io/docs/concepts/storage/volume-snapshots/
- **Дока**: EBS CSI driver — https://github.com/kubernetes-sigs/aws-ebs-csi-driver
- **Дока**: CloudNativePG storage — https://cloudnative-pg.io/documentation/1.23/storage/
- **Дока**: Strimzi storage — https://strimzi.io/docs/operators/latest/deploying#storage-concepts-str
- **Дока**: Velero — https://velero.io/docs/
- **Книга**: "Kubernetes Patterns", Bilgin Ibryam & Roland Huß (O'Reilly, 2nd ed., 2024)

### Практика / Кейсы

1. **PostgreSQL через CloudNativePG с PVC, WAL archiving в S3, PITR**. `Cluster` CR:
   ```yaml
   spec:
     instances: 3
     storage:
       storageClass: ebs-gp3
       size: 100Gi
     wal:
       storage: storageClass: ebs-gp3; size: 20Gi
     backup:
       barmanObjectStore:
         destinationPath: s3://pg-backups/
         endpointURL: https://s3.eu-west-1.amazonaws.com
         s3Credentials: ...
         wal retention: { retentionValue: 30, retentionUnit: days }
   ```
   PITR: `kubectl cnpg backup cluster --type=full` ежедневно + continuous WAL; restore через `kubectl cnpg restore cluster-pitr --timestamp "2024-..."`. Multi-AZ topology: `WaitForFirstConsumer` binding + `topologySpreadConstraints`.

2. **Kafka Strimzi с JBOD + multiple PVC per broker**. `Kafka` CR:
   ```yaml
   spec:
     kafka:
       replicas: 3
       storage:
         type: jbod
         volumes:
         - id: 0
           type: persistent-claim
           size: 500Gi
           class: ebs-gp3
           deleteClaim: false
         - id: 1
           type: persistent-claim
           size: 500Gi
           class: ebs-gp3
           deleteClaim: false
   ```
   `deleteClaim: false` — данные сохраняются при удалении Kafka cluster (важно для миграций). Volume expansion: `kubectl edit kafka` → Strimzi rolling update.

3. **Velero backup всего кластера + restore в DR region**. Установить Velero с S3 backend; `BackupStorageLocation` + `VolumeSnapshotLocation` per region; `Schedule` nightly backup; `kubectl get backups`; restore test quarterly в DR cluster. Resources (Deployments, Services, ConfigMaps) → restore через YAML; PV → CSI snapshot → create new PVC в DR. Внимание: secrets не всегда можно restore (encryption key rotation).

4. **Volume expansion EBS PVC online**. StorageClass `allowVolumeExpansion: true`; `kubectl patch pvc data-app-0 -p '{"spec":{"resources":{"requests":{"storage":"200Gi"}}}}'`; для EBS — online resize (no downtime); watch `kubectl get pvc` → `Capacity: 200Gi`. Если PVC в StatefulSet — нужно изменить `volumeClaimTemplates` и rolling restart, либо manually resize each PVC.

5. **Read-only ConfigMap mount с subPath для отдельных файлов**. App ожидает `/etc/app/application.yml`, `/etc/app/logback.xml`. ConfigMap `app-config` с двумя keys; mount через subPath:
   ```yaml
   volumes:
   - name: config
     configMap: { name: app-config }
   containers:
   - volumeMounts:
     - name: config
       mountPath: /etc/app/application.yml
       subPath: application.yml
     - name: config
       mountPath: /etc/app/logback.xml
       subPath: logback.xml
   ```
   SubPath не обновляется автоматически при ConfigMap change — нужно restart Pod. Альтернатива — mount whole dir + symbolic links.

6. **NFS RWX для shared static assets между Pod'ами**. PV (NFS server) + PVC `accessModes: [ReadWriteMany]`; multiple Pods mount same PVC. Use case: WordPress uploads, shared cache. Внимание: NFS — single point of failure; latency; file locking issues (NFSv4 better). Лучше — S3 + CloudFront для static, или EFS (AWS-managed NFS).

7. **StatefulSet migration storage type (gp2 → gp3) без downtime**. Шаги: 1) создать новый StatefulSet `app-v2` с gp3 StorageClass, 2) постепенно migrate replicas с rsync данных (или PostgreSQL logical replication), 3) переключить Service, 4) decommission старый StatefulSet. Для Kafka — Strimzi `KafkaRebalance` с rebalance + storage migration. Для PostgreSQL — pg_repack или logical replication.

---

## 9. Helm (charts, templates, values, hooks, subcharts, Kustomize сравнение)

### Теория

- **Helm**: package manager для K8s; chart = коллекция templated YAML + `Chart.yaml` + `values.yaml`; v3 (2019) — без Tiller, чисто client-side; релизы хранятся как Secrets в namespace.
- **Chart structure**: `Chart.yaml` (metadata: `apiVersion: v2`, `name`, `version`, `appVersion`, `type: application/library`, `dependencies`), `values.yaml` (defaults), `templates/` (Go templates + Sprig functions), `templates/_helpers.tpl` (named templates), `charts/` (vendored dependencies), `crds/` (CRD installation before templates), `values.schema.json` (validation), `.helmignore`.
- **Template language**: Go text/template + Sprig functions (`{{ .Values.foo }}`, `{{ .Release.Name }}`, `{{ include "chart.name" . }}`, `{{ toYaml .Values.resources | nindent 12 }}`, `{{ range }}`, `{{ if }}`, `{{ with }}`); `{{- ... -}}` whitespace trimming. `lookup` function (query cluster at install time).
- **Values**: `values.yaml` (defaults); override через `--set key.sub=value`, `--set 'list[0].name=x'`, `-f myvalues.yaml`, `--values`; layered merge (deep). `values.schema.json` — JSON Schema validation.
- **Hooks**: annotations `helm.sh/hook: pre-install/post-install/pre-delete/post-delete/pre-upgrade/post-upgrade/test`; `helm.sh/hook-weight` (order, lower first); `helm.sh/hook-delete-policy`. Use cases: DB migrations, smoke tests, certificate generation. `helm test` — runs `helm.sh/hook: test` resources.
- **Subcharts / dependencies**: `Chart.yaml` `dependencies:`; values для subchart через `subchartName:`; parent может override subchart values; `condition` (enable/disable), `tags` (group enable); `import-values` (прокинуть values из subchart в parent).
- **Library charts** (`type: library`): только named templates, не инсталлируют resources; повторное использование через `dependencies`. Common library: `common` (Bitnami).
- **Release lifecycle**: `install` / `upgrade` (3-way merge: live state vs cached vs new values) / `rollback` / `uninstall`; `helm history`, `helm get values`, `helm get manifest`. Atomic upgrade: `--atomic` — rollback on failure. Wait: `--wait --timeout 5m`.
- **`helm template`** vs `helm install --dry-run`: `template` — pure local render (no cluster); `--dry-run=client/server` — validate against cluster. Для GitOps (ArgoCD) — `helm template` + ArgoCD применяет rendered YAML.
- **Helmfile / Helmwave / Argo CD ApplicationSet**: tools поверх Helm для multi-environment management.
- **OCI registry для charts** (Helm 3.8+): `helm push chart.tgz oci://registry.example.com/charts`; `helm install oci://registry/charts/api --version 1.4.2`; преимущества: единый registry для images и charts, cosign signing.
- **Kustomize** (built-in kubectl): overlay-based patching; `kustomization.yaml` с `resources`, `patches`, `configMapGenerator`, `secretGenerator`, `images`, `namePrefix`, `namespace`, `commonLabels`, `components` (reusable bundles). Без templating — pure YAML; declarative; no DSL.
- **Helm vs Kustomize**:
  - Helm: templating, packages (charts), versioning, easy parametrization; con: logic in templates может стать сложным, "Helm charts as code".
  - Kustomize: no templates, overlays, easier review, native kubectl; con: сложнее для distribution (нет chart package), повторяющиеся patches.
  - Часто комбинируют: Helm для upstream charts (Bitnami), Kustomize для env-specific overlays.
- **Best practices**: один chart per app; `Chart.yaml` semver; pinned `appVersion`; `values.yaml` минимальные defaults; production overrides в `values-prod.yaml`; `README.md` с `helm install` инструкцией; tests; lint через `helm lint`; CI через `ct` (chart-testing).

### Материалы

- **Дока**: Helm — https://helm.sh/docs/
- **Дока**: Chart template guide — https://helm.sh/docs/chart_template_guide/
- **Дока**: Helm hooks — https://helm.sh/docs/topics/charts_hooks/
- **Дока**: OCI registries — https://helm.sh/docs/topics/registries/
- **Дока**: Kustomize — https://kubectl.docs.kubernetes.io/
- **Дока**: Bitnami common library — https://github.com/bitnami/charts/tree/main/bitnami/common
- **Дока**: chart-testing (ct) — https://github.com/helm/chart-testing
- **Книга**: "Learning Helm", Oteemo/Litkas/Butcher (O'Reilly, 2020)

### Практика / Кейсы

1. **Создать production-grade chart для Spring Boot с probes, HPA, PDB, Service, Ingress**. Структура: `Chart.yaml`, `values.yaml` (image, resources, ingress, hpa, pdb), `templates/` (deployment, service, ingress, hpa, pdb, configmap, secret-placeholder, serviceaccount). Helpers `_helpers.tpl` для имен (`{{ include "api.fullname" . }}` = `release-name-chart`). `values.schema.json` для валидации. Subchart для PostgreSQL (Bitnami) с `condition: postgresql.enabled`. `helm template` в CI для regression tests.

2. **Helm hooks: pre-upgrade DB migration + post-install smoke test**. `templates/migrate-job.yaml` с `helm.sh/hook: pre-upgrade,pre-install`, `hookWeight: -10`, `hookDeletePolicy: before-hook-creation,hook-succeeded`; Flyway миграция. `templates/smoke-test.yaml` — `helm.sh/hook: test`, `helm test release` запускает Pod с curl-проверкой `/actuator/health`. `--wait` + `--timeout` для синхронных deployments.

3. **Multi-environment values через Helm + ArgoCD**. Структура: `values.yaml` (base), `values-dev.yaml`, `values-staging.yaml`, `values-prod.yaml`. ArgoCD `Application` с `helm.valueFiles: [values.yaml, values-prod.yaml]`. Или Helmfile для multi-release orchestration:
   ```yaml
   releases:
   - name: api
     chart: charts/api
     values: [values.yaml, {{ env "ENV" }}.yaml]
   - name: postgres
     chart: bitnami/postgresql
     values: [postgres-{{ env "ENV" }}.yaml]
   ```
   Secrets через `helm-secrets` plugin (SOPS-encrypted values).

4. **Kustomize overlay для dev/staging/prod**. Структура:
   ```
   base/
     deployment.yaml
     service.yaml
     kustomization.yaml
   overlays/
     dev/
       kustomization.yaml
       patch-replicas.yaml
     prod/
       kustomization.yaml
       patch-replicas.yaml
       hpa.yaml
   ```
   `images: - name: registry/api; newName: registry/api; newTag: v1.4.2` — version pinning per env. `configMapGenerator` с `behavior: merge` для env-specific config. ArgoCD нативно понимает Kustomize.

5. **Migration Helm → Kustomize (или наоборот)**. Шаги: `helm template release chart > rendered.yaml` → разделить на individual YAMLs → создать `kustomization.yaml`. Или reverse: Kustomize bases → Helm chart с subcharts. Внимание: Helm post-renderer (`helm install --post-renderer ./kustomize-renderer.sh`) — комбинация Helm + Kustomize для env-specific patches.

6. **Chart для Java с JVM tuning + layered jar + Spring Boot Actuator**. values:
   ```yaml
   java:
     opts: "-XX:MaxRAMPercentage=75 -XX:+UseG1GC -Xlog:gc*:file=/tmp/gc.log:time,uptime,level,tags"
     image: registry/api
     tag: 1.4.2
   resources:
     requests: { cpu: 500m, memory: 768Mi }
     limits: { cpu: 1000m, memory: 1Gi }
   probes:
     startup: { path: /actuator/health/liveness, failureThreshold: 30, periodSeconds: 5 }
     liveness: { path: /actuator/health/liveness }
     readiness: { path: /actuator/health/readiness }
   ```
   Template генерирует deployment с этими values; `{{- range $k, $v := .Values.java.opts | splitList " " }}` для проброса как array в command.

7. **CI/CD для Helm charts (chart-testing + chart-releaser)**. GitHub Actions: `helm/chart-testing-action` → `ct lint` (yamllint, helm lint, kubeval/kubeconform); `ct install` (Kind cluster + install chart + smoke test); `helm/chart-releaser-action` → publish в GitHub Pages как Helm repo или push в OCI registry. Chart version bump — conventional commits + automated changelog.

---

## 10. GitOps (ArgoCD, Flux, sync waves, drift, rollback)

### Теория

- **GitOps principles** (OpenGitOps 1.0): 1) declarative system description, 2) versioned in Git, 3) pulled automatically, 4) continuously reconciled (drift detection + correction). Git = single source of truth; not push from CI.
- **Argo CD** (CNCF graduated): UI + CLI; `Application` (one per app/env) или `ApplicationSet` (matrix of clusters x apps); sync modes: `manual` (click button), `auto` (auto-apply on commit), `auto-prune`, `self-heal` (correct drift). Multi-cluster (one ArgoCD manage many target clusters). RBAC, SSO. Sync hooks (`PreSync`, `Sync`, `PostSync`, `SyncFail`). Resource hooks для custom logic.
- **Flux** (CNCF graduated): CLI-first, no UI by default (Weave GitOps UI separately); controllers: `source-controller` (GitRepository, OCIRepository, HelmRepository), `kustomize-controller`, `helm-controller`, `notification-controller`, `image-reflector-controller`, `image-automation-controller`. CRDs: `Kustomization`, `HelmRelease`. Better for pure GitOps purists; Argo CD better for UI/visibility.
- **Sync waves** (ArgoCD): `sync-wave` annotation на resources (`-5`, `0`, `5`) — порядок применения; lower first. Use cases: CRDs (-5) → namespaces (-4) → operators (-3) → app (0) → tests (5). Within wave — apply in parallel; next wave waits for all previous to be healthy.
- **Sync hooks**: `argocd.argoproj.io/hook: PreSync/Sync/PostSync/SyncFail`, `hook-delete-policy`. Use cases: DB migrations (PreSync), smoke tests (PostSync), notifications (SyncFail).
- **Drift detection**: ArgoCD сравнивает Git state vs cluster live state каждые 3min (default); показывает `OutOfSync` resources; `selfHeal: true` — автоматически применяет. Внимание: HPA масштабирует replicas — Argo будет видеть drift; fix — `argocd.argoproj.io/sync-options: RespectIgnoreDifferences=true` + ignoreDifferences field for replicas.
- **Rollback**: `argocd app rollback app --revision 5` (history of syncs). Если Git rollback — `git revert` + Argo auto-sync. В GitOps purest подходе — rollback только через Git, не через `kubectl`/`argocd` directly.
- **Multi-cluster**:
  - **Hub-spoke**: один ArgoCD управляет N кластерами (registers cluster secrets). Simpler, single pane of glass.
  - **Per-cluster ArgoCD**: изоляция, но больший overhead.
  - **ApplicationSet** — генерация Application по матрице (clusters x apps), git-directory generator, list generator, matrix generator, pull-request generator.
- **Secret management в GitOps**: Git не должен содержать plaintext secrets. Tools:
  - **Sealed Secrets** (Bitnami): encrypt с private key, decrypt controller'ом в кластере.
  - **External Secrets Operator**: sync из Vault/SM/SSM в K8s Secret; в Git только ExternalSecret resource.
  - **SOPS** (Mozilla) + age/PGP keys: encrypt values, Helm secrets plugin / Flux SOPS integration.
  - **Vault Agent Injector**: secret injection через annotations, без K8s Secret.
- **Progressive delivery с ArgoCD**: интеграция с Argo Rollouts (canary, blue-green) — Rollout CR вместо Deployment; ArgoCD sync'ает Rollout, Rollout controller управляет rolling.
- **App-of-apps pattern**: один Application, который sync'ает другие Applications (recursive); удобно для bootstrapping cluster.
- **Kargo** (Akuity, новый CNCF sandbox): progressive delivery без DSL, на основе stages/promotions; complement to ArgoCD.
- **ArgoCD Notifications**: `AppProject` + `Application` с annotations для Slack/PagerDuty/Teams уведомлений о sync status, OutOfSync, health.

### Материалы

- **Дока**: Argo CD — https://argo-cd.readthedocs.io/
- **Дока**: Argo CD ApplicationSet — https://argo-cd.readthedocs.io/en/stable/operator-manual/applicationset/
- **Дока**: Argo CD Sync waves — https://argo-cd.readthedocs.io/en/stable/user-guide/sync-waves/
- **Дока**: Flux — https://fluxcd.io/flux/
- **Дока**: Sealed Secrets — https://github.com/bitnami-labs/sealed-secrets
- **Дока**: External Secrets Operator — https://external-secrets.io/
- **Дока**: OpenGitOps principles — https://opengitops.dev/
- **Книга**: "GitOps and Kubernetes", Botelho/McGraw (O'Reilly, 2022)

### Практика / Кейсы

1. **Bootstrapping multi-cluster ArgoCD с app-of-apps**. Root `Application` указывает на `git://infra/bootstrap` → там `ApplicationSet` генерирует Applications для каждого кластера (dev/staging/prod) × каждый сервис. Cluster registration через ArgoCD cluster secrets. Sync waves: CRDs → controllers → CRs → apps. SSO через Dex (GitHub OIDC). RBAC: `policy.csv` с role-based permissions.

2. **Flux multi-tenant: tenant isolation**. Tenant namespace; `GitRepository` source с branch/tag per tenant; `Kustomization` per app; `ServiceAccount` + RBAC per tenant; Flux notification-controller → Slack per tenant. Image automation: `ImageRepository` + `ImagePolicy` (semver, latest) + `ImageUpdateAutomation` — auto-bump image tag в Git при новом image.

3. **Sync waves: CRD → operator → CR → app**. Annotations:
   ```yaml
   metadata:
     annotations:
       argocd.argoproj.io/sync-wave: "-5"  # CRDs
   ```
   Operator subscription (wave -3), CR instances (wave -1), app (wave 0). ArgoCD ждёт health каждого ресурса before next wave. Для CRDs — health check custom lua: `health.lua` per CRD (например, проверить `status.conditions`).

4. **Drift management: HPA + Argo self-heal conflict**. HPA обновляет `spec.replicas` в Deployment → Argo видит drift → self-heal откатывает → HPA снова меняет. Fix: `ignoreDifferences` в Application:
   ```yaml
   ignoreDifferences:
   - group: apps
     kind: Deployment
     jsonPointers: [/spec/replicas]
   ```
   + `RespectIgnoreDifferences=true` sync option. Также для `status` fields (не должны сравниваться).

5. **Secret management migration: in-cluster secrets → ESO + Vault**. Шаги: установить ESO; `SecretStore` с Vault backend (JWT auth через K8s SA); `ExternalSecret` per app → ESO создаёт K8s Secret; в Git только ExternalSecret (no values); migrate existing secrets to Vault через `kubectl get secret -o yaml | vault kv put ...`. Для emergency access — break-glass procedure (manual K8s Secret creation, документировано).

6. **Disaster recovery: ArgoCD cluster failure**. Git как source of truth → новый cluster → install ArgoCD → app-of-apps bootstrap → все приложения восстанавливаются автоматически. RPO/RTO измеряются. Тестировать quarterly через chaos engineering (kill cluster, restore from scratch). Backup ArgoCD state (`argocd-util export`).

7. **Progressive delivery с Argo Rollouts + ArgoCD**. Заменить `Deployment` на `Rollout`:
   ```yaml
   strategy:
     canary:
       steps:
       - setWeight: 10
       - pause: { duration: 5m }
       - analysis: { templates: [{ templateName: success-rate }] }
       - setWeight: 50
       - pause: { duration: 10m }
       - setWeight: 100
   ```
   Analysis template проверяет Prometheus success rate; rollback автоматически если < 95%. ArgoCD sync'ает Rollout, Rollout controller управляет canary. Интеграция с Istio/SM for traffic shifting.

---

## 11. Service Mesh на K8s (Istio, Linkerd; mTLS, canary)

### Теория

- **Service Mesh purpose**: L7 observability (per-request metrics/traces), mTLS без кода, traffic control (canary, retries, timeouts, circuit breaker), policy enforcement. Sidecar (Istio, Linkerd) или sidecarless (Cilium, Istio Ambient).
- **Istio** (CNCF): data plane = Envoy sidecars; control plane = `istiod` (Citadel, Galley, Pilot merged). CRDs: `VirtualService` (routing rules), `DestinationRule` (LB, subsets, mTLS), `Gateway` (ingress traffic), `ServiceEntry` (external services), `PeerAuthentication` (mTLS mode), `RequestAuthentication` (JWT), `AuthorizationPolicy` (L7 policy), `Sidecar` (config scoping), `EnvoyFilter` (raw Envoy patches).
- **Linkerd** (CNCF, Buoyant): data plane = Linkerd2-proxy (Rust, lightweight); control plane simpler; CRDs: `HTTPRoute` (Gateway API), `ServiceProfile` (per-service retries/timeouts), `Server` (authorization). Less features than Istio, but simpler operation.
- **Ambient mesh** (Istio 1.18+): sidecarless; L4 (ztunnel) per node + L7 (waypoint) per namespace; reduces resource overhead, easier upgrade. CNCF going in this direction.
- **mTLS**: certificate rotation через SPIFFE/SPIRE; `PeerAuthentication` mode `DISABLE/PERMISSIVE/STRICT`; transition: PERMISSIVE (both plaintext + mTLS) → STRICT. Certificates per workload identity (K8s SA). Auto-rotation каждые 24h.
- **Traffic management**:
  - `VirtualService`: route by URI/headers/ports; weight-based splitting (canary 10/90); retries (`retries: attempts, perTryTimeout, retryOn`); timeouts; fault injection (`abort`, `delay` для testing); mirror (shadow traffic).
  - `DestinationRule`: load balancing (ROUND_ROBIN, LEAST_REQUEST, RANDOM, PASSTHROUGH); connection pool (`tcp.maxConnections`, `http.http1MaxPendingRequests`); outlier detection (circuit breaker — evict unhealthy endpoints); subsets (canary, stable).
- **Canary через Istio**: `VirtualService` с weighted routing — 5% на `v2`, monitor metrics, постепенно 25% → 50% → 100%. Integration с Argo Rollouts — auto-promote/rollback based on metrics.
- **Circuit breaker**: `outlierDetection.consecutiveErrors`, `interval`, `baseEjectionTime`, `maxEjectionPercent` — Envoy перестаёт слать трафик на unhealthy endpoints.
- **Retries & timeouts**: в `VirtualService`; для Java-приложений уменьшает необходимость Resilience4j (но дублирование полезно для defence in depth). Retry budget: `retryOn: 5xx,reset,connect-refused,refused-stream`.
- **Observability**: golden signals out-of-the-box (request rate, error rate, latency) per service/method; distributed tracing через OpenTelemetry (Envoy автоматически inject headers); access logs от Envoy.
- **Authorization**: `AuthorizationPolicy` — deny-by-default или allow-by-default; rules на principals (SPIFFE ID), namespaces, methods, paths. `RequestAuthentication` — JWT validation (issuer, audiences, jwksUri).
- **External services** (`ServiceEntry`): добавить external API (Stripe, Twilio) в mesh для unified observability и policy.
- **Multi-cluster mesh**: cross-cluster mTLS, shared root of trust (SPIRE); primary-remote или multi-primary topology. Сложно, требует careful planning.
- **Performance overhead**: sidecar добавляет 1-2ms latency, ~50-100MB memory per pod; Ambient mesh — значительно меньше. Для latency-critical — Cilium (eBPF, in-kernel) может быть лучше.

### Материалы

- **Дока**: Istio — https://istio.io/latest/docs/
- **Дока**: Istio traffic management — https://istio.io/latest/docs/concepts/traffic-management/
- **Дока**: Istio security — https://istio.io/latest/docs/concepts/security/
- **Дока**: Istio Ambient mesh — https://istio.io/latest/docs/ambient/
- **Дока**: Linkerd — https://linkerd.io/2/overview/
- **Дока**: Argo Rollouts + Istio — https://argo-rollouts.readthedocs.io/en/stable/features/traffic-management/istio/
- **Книга**: "Istio in Action", Christian Posta, Rinor Maloku (Manning, 2022)
- **Книга**: "Service Mesh in Practice", Lee Calcote (O'Reilly, 2024)

### Практика / Кейсы

1. **mTLS migration в prod: PERMISSIVE → STRICT с zero downtime**. Шаги: enable Istio sidecar injection (`istio-injection=enabled` namespace label); restart Pods → sidecars injected; `PeerAuthentication` mode `PERMISSIVE` (plaintext + mTLS both work); monitor metrics, validate traffic; switch to `STRICT` постепенно (per namespace или per workload). Внимание: external non-meshed services (legacy VMs) — `ServiceEntry` + plaintext, или migrate VMs в mesh.

2. **Canary deployment через Argo Rollouts + Istio**. `Rollout` с `trafficRouting.istio`:
   ```yaml
   strategy:
     canary:
       trafficRouting:
         istio:
           virtualService: { name: api-vs, routes: [primary] }
           destinationRule: { name: api-dr, canarySubsetName: canary, stableSubsetName: stable }
       steps:
       - setWeight: 5
       - analysis: { templates: [{ templateName: success-rate, args: [...] }] }
       - setWeight: 25
       - pause: { duration: 10m }
       - setWeight: 100
   ```
   Argo Rollouts автоматически обновляет `VirtualService` weights. Analysis template — Prometheus query `sum(rate(http_requests_total{status!~"5.."}[1m])) / sum(rate(http_requests_total[1m]))`.

3. **Circuit breaker для downstream service**. `DestinationRule`:
   ```yaml
   trafficPolicy:
     connectionPool:
       tcp: { maxConnections: 100 }
       http: { http1MaxPendingRequests: 50, maxRequestsPerConnection: 10 }
     outlierDetection:
       consecutive5xxErrors: 5
       interval: 30s
       baseEjectionTime: 30s
       maxEjectionPercent: 50
   ```
   Envoy перестаёт слать трафик на bad endpoints; в Java-коде — fallback через Resilience4j (когда все endpoints evicted). Дублирование не зло — defense in depth.

4. **Migration Istio sidecar → Ambient mesh**. Шаги: install Istio Ambient profile; enable ztunnel DaemonSet (L4 mTLS per node); постепенно remove sidecars через `istio.io/rev` label; enable waypoint proxies per namespace для L7 features (authorization, routing). Цель: меньше overhead (50MB sidecar → 5MB ztunnel shared), simpler upgrades.

5. **JWT authorization через Istio**. `RequestAuthentication`:
   ```yaml
   spec:
     jwtRules:
     - issuer: https://keycloak.example.com/realms/myrealm
       jwksUri: https://keycloak.example.com/realms/myrealm/protocol/openid-connect/certs
       audiences: [api]
   ```
   `AuthorizationPolicy`: require valid JWT for `/api/*`, allow `/health` без JWT. В Java-коде — Spring Security OAuth2 Resource Server дублирует проверку (defense in depth). Внимание: JWKS rotation — Istio кэширует, refresh периодический.

6. **External API integration через ServiceEntry**. `ServiceEntry` для Stripe API:
   ```yaml
   spec:
     hosts: [api.stripe.com]
     location: MESH_EXTERNAL
     ports: [{ number: 443, name: https, protocol: HTTPS }]
     resolution: DNS
   ```
   Теперь Stripe-запросы видны в Kiali, есть mTLS (на самом деле TLS termination to Stripe), retries/timeouts через `VirtualService`. Полезно для unified observability внешних вызовов.

7. **Multi-cluster Istio: shared trust + cross-cluster traffic**. Primary-remote topology: один `istiod` управляет несколькими clusters; shared root CA (self-signed или корпоративный); cross-cluster service discovery; `DestinationRule` с locality load balancing (prefer same cluster). Use case: active-active DR, regional traffic optimization. Сложность: сетевая связность (VPN/Direct Connect), etcd cgroups limits, certs management.

---

## 12. Observability Metrics (Prometheus, PromQL, histograms/summaries, rules, Alertmanager, Micrometer, cardinality)

### Теория

- **Metrics data model**: time series = `metric_name{label1=val1,label2=val2}` → samples `(timestamp, value)`. TSDB (Prometheus) — columnar по series, chunk encoding (gorilla XOR, delta-of-delta). Retention default 15d (или 100h для long-term storage через Thanos/Mimir).
- **Metric types**:
  - **Counter**: монотонно возрастает (`http_requests_total`); use `rate()` / `increase()`. Reset при restart — Prometheus детектит через counter reset detection.
  - **Gauge**:任意 value (`memory_usage_bytes`, `temperature`); `delta`, `avg_over_time`.
  - **Histogram**: bucketed (`http_request_duration_seconds_bucket`); buckets `le="0.1"`, `le="0.5"`, `le="+Inf"`; compute percentiles через `histogram_quantile()`. Cumulative buckets. Higher cardinality than summary.
  - **Summary**: pre-computed quantiles на client side (`http_request_duration_seconds{quantile="0.99"}`); cannot aggregate across instances. Mostly replaced by histograms в modern setups.
- **PromQL**:
  - Selectors: `http_requests_total`, `http_requests_total{method="POST"}`, `http_requests_total{method=~"POST|PUT"}` (regex), `http_requests_total{status!~"5.."}` (negative regex).
  - Range vectors: `[5m]`, `[1h]`; `rate(metric[5m])` (per-second rate), `increase(metric[1h])` (total over hour).
  - Aggregations: `sum by (status) (rate(http_requests_total[5m]))`, `avg by (instance) (...)`, `topk(5, ...)`, `count`, `min`/`max`/`stddev`.
  - Binary ops: `metric_a / metric_b` (vector matching by labels; `ignoring()`, `on()`, `group_left()`).
  - Functions: `histogram_quantile(0.99, rate(http_duration_bucket[5m]))`, `predict_linear`, `deriv`, `changes`, `absent`, `clamp_max`, `label_replace`.
  - Subqueries: `rate(http_requests_total[5m])[1h:1m]` — для computed rates over time.
- **Recording rules**: pre-compute expensive queries; `groups` с `interval`, `rules` (name, expr); хранятся как новые series (`job:http_requests:rate5m`); экономит CPU Prometheus на dashboard/ alerting queries.
- **Alerting rules**: `groups` с `rules` (alert, expr, for, labels, annotations); `for: 5m` — condition must hold for 5 min before firing. `labels` (severity, team, runbook) + `annotations` (description, dashboard URL).
- **Alertmanager**: routing tree (routes based on labels), grouping, inhibition (если critical fired — suppress warning), silences (maintenance windows), receivers (Slack, PagerDuty, Email, webhook). High availability: cluster mode (gossip).
- **Service discovery**: Prometheus-native SD for Kubernetes (`kubernetes_sd_configs` с `role: node/pod/service/endpoints/ingress`); scrape targets = Pod IPs with `prometheus.io/scrape: "true"` annotations (legacy) или `ServiceMonitor` CRD (Prometheus Operator, modern).
- **Prometheus Operator**: `Prometheus`, `ServiceMonitor`, `PodMonitor`, `PrometheusRule`, `Alertmanager` CRDs; manage Prometheus instances via K8s declarative.
- **Micrometer** (Spring Boot default): facade (like SLF4J for metrics); `MeterRegistry` (Prometheus, Datadog, NewRelic, etc.); `Counter`, `Gauge`, `Timer`, `DistributionSummary`, `LongTaskTimer`. Naming convention: `http.server.requests` → Prometheus `http_server_requests_seconds_count/sum/max` (+ bucket for histograms). Spring Boot Actuator exposes `/actuator/prometheus`. JVM metrics (GC, heap, threads), HTTP metrics, Tomcat, HikariCP, Kafka client.
- **Cardinality**: number of unique label combinations; explosion kills Prometheus. Anti-patterns: label with `user_id`, `session_id`, `request_id`, full URL (path with query params). Rule of thumb: <10 unique values per label, <100k total series per scrape. Monitor via `prometheus_tsdb_head_series`.
- **Histogram bucket design**: cover latency SLO (e.g., for p99 < 500ms — buckets `0.005, 0.01, 0.025, 0.05, 0.1, 0.25, 0.5, 1, 2.5, 5, 10`); `+Inf` always present. Custom buckets via Micrometer `@Timed(percentiles=...)` или `ServiceLevelObjective` configuration.
- **Long-term storage**: Thanos (sidecar + S3 + query frontend + compactor + store gateway), Cortex/Mimir (multi-tenant, horizontal scalable), VictoriaMetrics (drop-in, more performant). Global query view across clusters.
- **OTel metrics**: OpenTelemetry metrics specification; OTLP protocol; supports Prometheus exposition; dimensional metrics (vs Prometheus labels); exponential histograms (better dynamic range).

### Материалы

- **Дока**: Prometheus — https://prometheus.io/docs/
- **Дока**: PromQL — https://prometheus.io/docs/prometheus/latest/querying/basics/
- **Дока**: Histograms and summaries — https://prometheus.io/docs/practices/histograms/
- **Дока**: Recording rules — https://prometheus.io/docs/prometheus/latest/configuration/recording_rules/
- **Дока**: Alertmanager — https://prometheus.io/docs/alerting/latest/alertmanager/
- **Дока**: Prometheus Operator — https://prometheus-operator.dev/
- **Дока**: Micrometer — https://micrometer.io/docs
- **Дока**: Spring Boot Actuator metrics — https://docs.spring.io/spring-boot/reference/actuator/metrics.html
- **Дока**: Thanos — https://thanos.io/tip/thanos/getting-started.md/
- **Книга**: "Observability Engineering", Charity Majors, Liz Fong-Jones, George Miranda (O'Reilly, 2022)
- **Книга**: "Prometheus: Up & Running", Brian Brazil (O'Reilly, 2nd ed., 2024)

### Практика / Кейсы

1. **Настроить Spring Boot + Micrometer + Prometheus с JVM и HTTP metrics**. Maven: `micrometer-registry-prometheus`, `actuator`; `application.yml`: `management.endpoints.web.exposure.include: prometheus,health,metrics`; `management.metrics.distribution.percentiles-histogram.http.server.requests: true` + `slo: [50ms, 100ms, 250ms, 500ms, 1s, 5s]`. ServiceMonitor (Prometheus Operator):
   ```yaml
   spec:
     endpoints: [{ port: http, path: /actuator/prometheus, interval: 15s }]
     selector: { matchLabels: { app: api } }
   ```
   Dashboard Grafana: JVM, Hikari pool, HTTP latencies p50/p95/p99, throughput.

2. **Cardinality explosion: debug и fix**. Симптом: Prometheus OOM, `prometheus_tsdb_head_series` растёт экспоненциально. Шаги: query `topk(10, count by (__name__)({__name__=~".+"}))` — найти metric с наибольшим числом series; `http_server_requests_seconds_count{uri=...}` с тысячами урлов (с query params); fix в Spring Boot — `server.tomcat.accesslog` без query string, или interceptor для нормализации URI (Map `/{orderId}` → `/orders/{orderId}`); для restTemplate/WebClient — `MetricsTag` с templated URI. После fix — series count stable.

3. **Custom business metrics для电商平台**. Micrometer в коде:
   ```java
   @Autowired MeterRegistry registry;
   // Counter
   registry.counter("orders.created", "type", orderType).increment();
   // Timer
   registry.timer("payment.processing", "gateway", gateway).record(() -> processPayment());
   // Gauge (auto-refresh)
   registry.gauge("cart.items.active", activeCarts, c -> c.size());
   // LongTaskTimer для in-flight requests
   LongTaskTimer ltt = registry.more().longTaskTimer("ingestion.batch.active");
   ```
   Alerting: `rate(orders_created_total[5m]) < 1` (no orders — possible incident); `sum(payment_processing_seconds_sum) / sum(payment_processing_seconds_count) > 2` (latency degradation).

4. **Multi-cluster Prometheus с Thanos (global view)**. Each cluster: Prometheus + Thanos sidecar (uploads blocks to S3); central: Thanos Query (fan-in to all sidecars + store gateway) + Compactor (downsampling, retention) + Ruler (global alerting). Grafana datasource = Thanos Query. Long-term retention (years). Cardinality management per cluster. Cost: S3 storage, Querier memory.

5. **Alerting rules с runbook links + Alertmanager routing**. `PrometheusRule`:
   ```yaml
   groups:
   - name: api.rules
     interval: 30s
     rules:
     - alert: HighErrorRate
       expr: sum(rate(http_server_requests_seconds_count{status=~"5.."}[5m])) by (service) / sum(rate(http_server_requests_seconds_count[5m])) by (service) > 0.05
       for: 5m
       labels: { severity: critical, team: api }
       annotations:
         summary: "High 5xx rate for {{ $labels.service }}"
         runbook: "https://wiki/runbooks/high-error-rate"
         dashboard: "https://grafana/d/api-overview?var-service={{ $labels.service }}"
   ```
   Alertmanager routes: critical → PagerDuty, warning → Slack; inhibit LowDisk if HighDisk firing; group_by [alertname, service]; repeat_interval 4h.

6. **SLI/SLO через Prometheus + recording rules**. Определить SLO "99% of requests < 500ms over 28 days". Recording rules:
   ```yaml
   - record: request_latency:slo_error_rate:28d
     expr: |
       sum(rate(http_request_duration_seconds_bucket{le="0.5"}[28d])) by (service)
       / sum(rate(http_request_duration_seconds_count[28d])) by (service)
   ```
   Alerting on error budget burn rate (multi-window multi-burn-rate alerts from Google SRE workbook). 1h burn rate 14.4x → page; 6h burn rate 6x → ticket.

7. **Migrate from Prometheus pull to OTel push (для serverless/edge)**. OTel Collector agent mode (DaemonSet) accepts OTLP from apps, exports to Prometheus (remote write) + Tempo (traces) + Loki (logs). Apps use OTel SDK; smoother for ephemeral workloads (Lambda) where scrape не успевает. Cardinality control через views. Migration: side-by-side, validate metrics parity.

---

## 13. Observability Dashboards (Grafana datasources, panels, variables, dashboards as code, alerting)

### Теория

- **Grafana architecture**: frontend (React), backend (Go), datasources (Prometheus, Loki, Tempo, Elasticsearch, InfluxDB, PostgreSQL, CloudWatch, Azure Monitor, many more via plugins); SQL/no-SQL. Multi-tenancy в Grafana Enterprise/Cloud.
- **Datasources**: configured via UI или provisioning (`provisioning/datasources/`); `url`, `access: proxy` (server-side, recommended) vs `browser` (CORS); auth: basic, OAuth, AWS auth, mTLS; `jsonData` для datasource-specific (httpMethod, timeInterval для Prometheus).
- **Panels types**: Time series, Stat, Gauge, Bar gauge, Table, Heatmap, Histogram, Pie chart, Geomap, Node graph (topology), Logs, Traces, Flame graph, Canvas (custom UI), State timeline. Panel plugins: clock, worldmap, etc.
- **Variables (templating)**: `$service`, `$namespace`, `$datasource`; types: Query (от datasource), Custom (manual list), Text box, Interval, Data source, Constant, Ad hoc filter (Elasticsearch). Variable chaining (`$cluster` → `$namespace` → `$pod`). Cascade updates.
- **Dashboard JSON model**: `schemaVersion`, `panels` (with gridPos, targets, fieldConfig), `templating.list`, `time` (default range), `refresh`, `annotations`, `links`. Version in dashboard metadata.
- **Dashboards as code**:
  - **Provisioning**: `provisioning/dashboards/` YAML — directories scanned for JSON dashboards; auto-imported on startup.
  - **Grafonnet** (Jsonnet library): programmatically generate dashboards; reusable functions, less repetition.
  - **Terraform provider**: `grafana_dashboard` resource; dashboards in HCL/JSON; version-controlled.
  - **Grafana dashboard API**: REST API for CRUD; used by CI/CD.
  - **git-sync sidecar**: pull dashboards from Git repo periodically.
- **Alerting (Grafana Unified Alerting)**: `AlertRule` (PromQL/Loki/...), `Notification policy` (routing tree), `Contact point` (Slack/PagerDuty/Email/webhook), `Mute timing`, `Notification template` (Go templates). Multi-datasource alerting (в отличие от Prometheus-only). Alert states: Normal, Pending, Firing, NoData, Error.
- **Annotations**: events on timeline (deploys, incidents, alerts); from datasource (Grafana annotations stored in DB) или from queries (e.g., deploy events from Loki). Help correlate metric anomalies with events.
- **Public dashboards**: share read-only без auth (с auth proxy); useful for status pages, business metrics for stakeholders.
- **Reporting**: scheduled PDF reports via email (Enterprise); для business users.
- **Folder permissions**: dashboards organized in folders; RBAC per folder; teams. Enterprise: fine-grained roles.
- **Grafana plugins**: data sources (CloudWatch, etc.), panels (встроенные + community), apps (复合 dashboards). Signed plugins only by default.
- **Performance**: avoid high-cardinality queries; use `$__interval` and `$__rate_interval` for auto resolution; recording rules for expensive queries; max data points per panel.
- **Best practices**: one dashboard per concern (service overview, JVM, infra, SLO); golden signals (latency, traffic, errors, saturation); use consistent naming; documentation in dashboard description; variables для reuse; review stale dashboards (Grafana usage insights).
- **Grafana LGTM stack**: Loki (logs) + Grafana (viz) + Tempo (traces) + Mimir (metrics) — open source, integrated, OTel-friendly. Plugins for exemplars (metrics → traces navigation).

### Материалы

- **Дока**: Grafana — https://grafana.com/docs/grafana/latest/
- **Дока**: Dashboards — https://grafana.com/docs/grafana/latest/dashboards/
- **Дока**: Variables — https://grafana.com/docs/grafana/latest/dashboards/variables/
- **Дока**: Provisioning — https://grafana.com/docs/grafana/latest/administration/provisioning/
- **Дока**: Alerting — https://grafana.com/docs/grafana/latest/alerting/
- **Дока**: Grafonnet — https://grafana.github.io/grafonnet/
- **Дока**: Terraform provider — https://registry.terraform.io/providers/grafana/grafana/latest/docs
- **Книга**: "Grafana Starter Guide", Marcus Smedman (Packt, 2024)

### Практика / Кейсы

1. **Java/Spring Boot service overview dashboard с golden signals**. Panels: 1) Request rate (QPS) by status class, 2) p50/p95/p99 latency, 3) Error rate (5xx), 4) HikariCP pool utilization, 5) JVM heap used/committed/max, 6) GC pause time (G1), 7) CPU usage (process + container), 8) Pod count (HPA). Variables: `$datasource`, `$namespace`, `$pod` (cascading). Exemplars on latency panel — click to jump to Tempo trace. Annotation from Loki for deploy events.

2. **Dashboards as code через Grafonnet**. `main.jsonnet`:
   ```jsonnet
   local g = import 'grafonnet/grafana.libsonnet';
   g.dashboard.new('API Overview', refresh='30s')
     .addPanel(g.graphPanel.new('Request Rate'), gridPos={x:0,y:0})
     .addTarget(prometheusTarget('sum(rate(http_requests_total[5m])) by (service)'))
   ```
   `jsonnet main.jsonnet > dashboard.json`; commit в Git; CI: `grafana-cli` или Terraform apply. Reusable library functions for common panels.

3. **Terraform-managed Grafana**. `main.tf`:
   ```hcl
   resource "grafana_dashboard" "api" {
     folder       = grafana_folder.services.uid
     config_json  = file("dashboards/api.json")
     version      = 1
   }
   resource "grafana_data_source" "prometheus" {
     type = "prometheus"
     name = "prometheus"
     url  = "http://prometheus:9090"
   }
   ```
   Plan/apply в CI; dashboards versioned in Git; PR review before production changes. State в S3 + DynamoDB lock.

4. **Unified Alerting с multi-datasource + routing**. Alert rules: metric-based (Prometheus: error rate > 5%), log-based (Loki: error count > 100 in 5m), composite (metric anomaly + log pattern). Notification policies: route by label (team, severity), contact points (PagerDuty for critical, Slack for warning), mute timings (maintenance windows). Templates: Go templates for Slack message formatting (includes runbook URL, dashboard link, silences).

5. **Cross-linking metrics → logs → traces (exemplars)**. В Prometheus включить exemplar storage (`--enable-feature=exemplar-storage`); в Micrometer — add trace ID as exemplar label; в Grafana — trace data point shows exemplar, click → Tempo trace view; from trace → Loki logs (by trace ID); from log line → other logs in same service/time. End-to-end debugging.

6. **SLO dashboard с error budget**. Recording rules compute SLI (good events / total events); error budget = 1 - SLO target (e.g., 99.9% → 0.1% budget); burn rate over multiple windows (1h, 6h, 1d, 3d); dashboard: SLI value vs target, error budget remaining (time + requests), burn rate alerts, events (deploy, incident). From Google SRE workbook.

7. **Multi-cluster Grafana с federation**. Central Grafana + Thanos Query datasource (all clusters) + per-cluster Loki + Tempo. Variables: `$cluster` (Thanos labels), cascading to `$namespace`, `$pod`. Dashboard: cluster selector; same dashboard works across clusters. RBAC: per-team dashboards in folders; cluster-admin role sees all. Anonymized public dashboard for status page (read-only).

---

## 14. Observability Logs (ELK/EFK, Loki+Promtail, structured JSON logging, MDC, log levels)

### Теория

- **Logging best practices**: structured (JSON, не plain text); contextual (request ID, user ID); appropriate level (TRACE/DEBUG/INFO/WARN/ERROR); never log secrets (PII, passwords, tokens); single line per event (multi-line stack traces как one event); include timestamp, level, logger, thread, MDC context.
- **Structured logging в Java**: SLF4J 2.x + Logback/Log4j2; `logback-spring.xml` с `LogstashEncoder` (Logback) или `JsonLayout` (Log4j2). Fields: `@timestamp`, `level`, `logger_name`, `thread_name`, `message`, `stack_trace`, `MDC fields`. Для логов в stdout — Kubernetes собирает через Fluent Bit.
- **MDC (Mapped Diagnostic Context)**: thread-local map; `MDC.put("requestId", uuid)`; автоматическое добавление в log; cleanup в finally или `try-with-resources`. В Spring Boot — `HandlerInterceptor` extracts request ID from headers или генерирует. Для async — `MDC.getCopyOfContextMap()` + restore в worker thread; в Spring `@Async` — `MdcTaskDecorator`; Reactor — `Hooks.onEachOperator`.
- **Log levels**: ERROR (system in failed state, immediate action), WARN (potential issue, investigate), INFO (lifecycle events, business milestones), DEBUG (diagnostic, dev only), TRACE (verbose). Production: INFO + selective DEBUG for troubleshooting. Spring Boot: `logging.level.org.springframework.web=DEBUG` per package.
- **ELK/EFK stack**: Elasticsearch (storage/search), Logstash/Fluentd (processing), Kibana (UI). EFK = Fluent Bit/Fluentd instead of Logstash (lighter). Beats (Filebeat, Metricbeat, etc.) — lightweight shippers.
- **Loki** (Grafana): "Prometheus for logs"; indexes only metadata (labels), not content; cheaper than ES for similar volume; Promtail (or Fluent Bit, Vector) ships logs; LogQL query language (similar to PromQL). Best-effort full-text search. Good for Kubernetes cluster logs.
- **Fluent Bit** (CNCF, Fluentd successor): lightweight C-based; DaemonSet on each K8s node; tails container logs (`/var/log/containers/`), parses, enriches (K8s metadata via API server), ships to multiple destinations (Loki, ES, S3, Kafka). Filters: grep, modify, parser (regex/json). Buffering (filesystem) for resilience.
- **Log aggregation pipeline**: app stdout → container log file → Fluent Bit (parse, enrich) → buffer → shipper → storage → query UI. Each stage может add tags, drop noise, mask secrets.
- **Centralized log architecture considerations**: throughput (events/sec), retention (hot/warm/cold tiers), search latency, cost (ES storage expensive). Sampling для DEBUG/TRACE в prod. Hot tier (SSD, 7d), warm (HDD, 30d), cold (S3, 1y) через ILM (Index Lifecycle Management).
- **Log routing**: business logs (INFO+) → Loki/ES; access logs → S3/BigQuery для analytics; audit logs → immutable storage (compliance); security logs → SIEM (Splunk, Sentinel). Different pipelines per log type.
- **Trace ID correlation**: inject `traceId` в MDC; log ships to Loki с label `trace_id`; from Tempo/Jaeger → click "View logs" → query Loki by trace ID; from log line → "View trace" → Tempo. End-to-end observability.
- **PII masking**: в Fluent Bit `lua` filter (regex replace); в Java app — `MaskingPatternLayout` (custom converter); structured logging — mask sensitive fields before serialization (Jackson `@JsonSerialize` with custom serializer). Compliance: GDPR, HIPAA, PCI-DSS.
- **OpenTelemetry logs**: OTel SDK log bridge (SLF4J → OTel LogRecordEmitter); OTLP exporter; unified with metrics/traces (resource, instrumentation scope). Emerging standard; 2024-stable.

### Материалы

- **Дока**: Elastic Stack — https://www.elastic.co/guide/en/elasticsearch/reference/current/index.html
- **Дока**: Fluent Bit — https://docs.fluentbit.io/manual
- **Дока**: Loki — https://grafana.com/docs/loki/latest/
- **Дока**: Promtail — https://grafana.com/docs/loki/latest/send-data/promtail/
- **Дока**: Logback — https://logback.qos.ch/documentation.html
- **Дока**: SLF4J MDC — http://www.slf4j.org/manual.html#mdc
- **Дока**: logstash-logback-encoder — https://github.com/logfellow/logstash-logback-encoder
- **Дока**: OTel Java logs — https://opentelemetry.io/docs/languages/java/instrumentation/logs/
- **Книга**: "Logging in Action", Phil Wilkins (Manning, 2022)
- **Книга**: "Elasticsearch in Action, 2nd ed.", Doug Turnbull, Radu Gheorghe (Manning, 2024)

### Практика / Кейсы

1. **Structured JSON logging в Spring Boot с Logback + MDC**. `logback-spring.xml`:
   ```xml
   <configuration>
     <appender name="JSON" class="ch.qos.logback.core.ConsoleAppender">
       <encoder class="net.logstash.logback.encoder.LogstashEncoder">
         <customFields>{"app":"api","env":"${ENV}"}</customFields>
         <includeMdcKeyName>requestId</includeMdcKeyName>
         <includeMdcKeyName>userId</includeMdcKeyName>
         <includeMdcKeyName>traceId</includeMdcKeyName>
       </encoder>
     </appender>
     <root level="INFO"><appender-ref ref="JSON"/></root>
   </configuration>
   ```
   `HandlerInterceptor`:
   ```java
   String requestId = req.getHeader("X-Request-ID") != null ? req.getHeader("X-Request-ID") : UUID.randomUUID().toString();
   MDC.put("requestId", requestId);
   try { chain.doFilter(...); } finally { MDC.clear(); }
   ```
   Для async (Spring `@Async`) — `MdcTaskDecorator` copies MDC.

2. **Loki + Promtail + Grafana для K8s cluster**. Promtail DaemonSet tail `/var/log/containers/*.log`; `scrape_configs` с `pipeline_stages`: `json` (parse structured), `labels` (extract `app`, `namespace`, `level`), `match` (drop DEBUG для prod). Loki single binary (small) или distributed mode (microservices) для scale. Retention: 30d hot, S3 via Boltdb-Shipper для long-term. LogQL: `{namespace="prod",app="api"} |= "ERROR" | json | latency > 1000`.

3. **PII masking pipeline**. В Fluent Bit `filters`:
   ```yaml
   filters:
   - name: lua
     match: "*"
     script: mask.lua
     call: mask
   ```
   Lua regex-replace emails, credit cards, SSNs before ship. В Java — `MaskingPatternLayout` с patterns. Compliance audit: verify no PII в logs через sampling. Documentation: data flow diagram, PII inventory.

4. **Multi-tenant log routing в ELK**. Fluent Bit `outputs`: business logs → Loki (per-team index); access logs → S3 (long-term analytics); audit logs → Kafka → SIEM. Per-tenant isolation через index pattern + Kibana spaces + RBAC. Hot/warm/cold ILM: hot 7d (SSD), warm 30d (HDD), cold 1y (S3 searchable snapshot), delete after.

5. **Trace ID correlation across services**. Spring Cloud Sleuth (deprecated) → Micrometer Tracing (Boot 3.x) + OTel; inject `traceId`/`spanId` в MDC; log includes traceId; в Tempo UI — click "Logs" button → Loki query by `traceId="..."`. В Kubernetes — Fluent Bit enriches with pod labels, allows query `{namespace="prod",app="api"} | json | trace_id="abc123"`.

6. **Log level dynamic change без redeploy**. Spring Boot Actuator `/actuator/loggers` endpoint; POST `{"configuredLevel":"DEBUG"}` для конкретного logger; изменения apply мгновенно. В K8s — admin API exposed только internally. Для prod — pre-defined "troubleshooting profiles" (через ScriptMaster или custom endpoint) — enable DEBUG для всех request loggers на 10 min, auto-revert. Audit log всех изменений log level.

7. **Migrate from ELK to Loki (cost reduction)**. Симптом: ES cluster expensive ($50k+/month), search latency не critical. Шаги: deploy Loki параллельно; Fluent Bit dual-ship в ES + Loki 1 месяц; validate query parity; team migration по сервисам; decommission ES после успеха. Cost: Loki ~10x cheaper для same volume (less indexing). Trade-off: full-text search slower; complex queries limited.

---

## 15. Observability Traces (OpenTelemetry SDK, Java agent auto-instrumentation, Jaeger/Tempo, sampling, baggage, W3C trace context)

### Теория

- **Distributed tracing fundamentals**: trace = tree of spans; span = unit of work (start, end, attributes, events, status, links); parent-child relationship propagates context; propagation через W3C Trace Context (`traceparent`/`tracestate` headers) или B3 (legacy Zipkin).
- **W3C Trace Context**: `traceparent: 00-<trace-id 32 hex>-<span-id 16 hex>-<flags 2 hex>`; `tracestate: vendor-specific`. `flags` bit 0 = sampled. Standard adopted всеми major frameworks (OTel, Jaeger, Zipkin, AWS X-Ray, etc.).
- **OpenTelemetry (OTel)**: CNCF project merging OpenTracing + OpenCensus. Specification + API + SDK + Collectors. Three pillars: traces, metrics, logs. Semantic conventions (standard attribute names: `http.method`, `db.system`, `messaging.destination`). Instrumentation libraries для популярных frameworks (auto-instrumentation).
- **OTel Java**: 
  - **API** (`io.opentelemetry.api`): app code, no-op if no SDK.
  - **SDK** (`io.opentelemetry.sdk`): implementation — tracer provider, span processors, exporters, samplers.
  - **Auto-instrumentation agent** (`-javaagent:opentelemetry-javaagent.jar`): bytecode injection для Spring, JDBC, Kafka, gRPC, HTTP clients, etc.; no code changes for basic tracing.
  - **Manual instrumentation**: `Tracer tracer = GlobalOpenTelemetry.getTracer("my-app"); Span span = tracer.spanBuilder("operation").startSpan(); try (Scope s = span.makeCurrent()) { ... } finally { span.end(); }`.
- **OTel Collector**: vendor-neutral proxy; receivers (OTLP, Jaeger, Zipkin, Prometheus), processors (batch, attributes, tail sampling, filter), exporters (Jaeger, Tempo, Datadog, ES, S3). DaemonSet (agent) or gateway deployment. Offloads sampling, enrichment, retries от apps.
- **Sampling strategies**:
  - **Head-based**: decision at trace start (in root span); `ParentBased(TraceIdRatioBased(0.1))` — 10% traces; consistent across services; misses rare errors.
  - **Tail-based**: decision at trace end (in collector); can sample based on attributes (errors, slow traces); more expensive (collector must buffer); better for SLO/SLI.
  - **Probabilistic**: random by trace ID hash.
  - **Rate-limiting**: max N traces/sec.
- **Baggage**: key-value context propagated across services (через `baggage` HTTP header); use cases: A/B test variant, customer tier, feature flags; do NOT put large data (size limits). Access via `Baggage.current().getEntryValue("key")`.
- **Span attributes vs resources**: span attributes = per-operation (HTTP method, status code); resource attributes = per-process (service.name, host.name, K8s pod). Semantic conventions стандартизируют имена.
- **Span events**: timestamped log within span (e.g., "cache miss", exception with stack trace). Span links: связь между spans без parent-child (e.g., fan-in from queue).
- **Jaeger** (CNCF): UI-focused tracing; storage Cassandra/Elasticsearch/OTLP; features: service map, dependency graph, comparison. Native OTLP support в recent versions. UI от Temp'а заимствован.
- **Tempo** (Grafana): object storage (S3) backend; high scalability; no index on tags (only trace ID); TraceQL для query (similar to LogQL). Integration с Grafana, Loki, Prometheus (exemplars).
- **AWS X-Ray, Azure Monitor, GCP Cloud Trace**: cloud-native; vendor lock-in, но zero ops.
- **Trace-metrics correlation (exemplars)**: high-cardinality data point в Prometheus histogram bucket с trace ID; в Grafana click → Tempo trace. Bridge between metrics и traces.
- **Common instrumentation gaps**: async boundaries (CompletableFuture, Reactor, virtual threads — context propagation manual); message queues (Kafka headers — explicit inject/extract); database (JDBC auto, но multi-statement transactions требуют manual spans); external HTTP calls (OkHttp/Apache — auto, но custom headers могут не propagation).
- **Performance**: OTel agent overhead ~1-5% CPU, ~20-50MB memory; sampling critical для prod scale (100% sampling impossible at scale); batch exports (not synchronous); async span processors.

### Материалы

- **Дока**: OpenTelemetry — https://opentelemetry.io/docs/
- **Дока**: OTel Java — https://opentelemetry.io/docs/languages/java/
- **Дока**: OTel Java agent — https://github.com/open-telemetry/opentelemetry-java-instrumentation
- **Дока**: OTel Collector — https://opentelemetry.io/docs/collector/
- **Дока**: Semantic conventions — https://opentelemetry.io/docs/specs/semconv/
- **Дока**: W3C Trace Context — https://www.w3.org/TR/trace-context/
- **Дока**: Jaeger — https://www.jaegertracing.io/docs/
- **Дока**: Tempo — https://grafana.com/docs/tempo/latest/
- **Книга**: "Mastering Distributed Tracing", Yuri Shkuro (O'Reilly, 2019)
- **Книга**: "Distributed Tracing in Practice", Bhatti/Bird/Sigelman (O'Reilly, 2020)

### Практика / Кейсы

1. **Spring Boot + OTel Java agent + Tempo**. Шаги: dockerize app; в Dockerfile `COPY opentelemetry-javaagent.jar /app/`; `ENTRYPOINT ["java", "-javaagent:/app/opentelemetry-javaagent.jar", "-Dotel.service.name=api", "-Dotel.exporter.otlp.endpoint=http://otel-collector:4317", "-jar", "app.jar"]`. Auto-instruments Spring MVC, JDBC, HikariCP, Tomcat, Kafka, RestTemplate/WebClient. Collector → Tempo. Grafana datasource = Tempo. Spring Boot 3.x alternative: Micrometer Tracing + OTel bridge (no agent).

2. **Context propagation через Kafka**. Producer: OTel SDK inject context в Kafka headers:
   ```java
   producer.send(new ProducerRecord<>(topic, key, value), (metadata, e) -> {
     // auto-capture span, but headers inject manual
   });
   ```
   Consumer: extract context, start consumer span as child. OTel Java agent делает это автоматически для Kafka clients 3.x+. Manual alternative: `TextMapPropagator` + Kafka headers. Test: trace shows producer span → consumer span в tree view.

3. **Tail-based sampling в OTel Collector**. Collector processor:
   ```yaml
   processors:
     tail_sampling:
       decision_wait: 30s
       num_traces: 50000
       policies:
       - { name: errors, type: status_code, status_code: {status_codes: [ERROR]} }
       - { name: slow, type: latency, latency: {threshold_ms: 500} }
       - { name: random-10pct, type: probabilistic, probabilistic: {sampling_percentage: 10} }
   ```
   Все errors + slow traces + 10% rest. Buffer 30s для collect all spans of trace. Memory: ~50000 traces buffered. Trade-off: latency до export (30s), memory.

4. **Baggage для feature flag propagation**. In gateway: `Baggage.current().toBuilder().put("feature.new-checkout", "enabled").build().makeCurrent()`; propagates через `baggage` header to downstream services. Service checks: `if ("enabled".equals(Baggage.current().getEntryValue("feature.new-checkout"))) { ... }`. LaunchDarkly / OpenFeature SDK integration. Alternative:专门的 feature flag headers.

5. **Migrate from Spring Cloud Sleuth to Micrometer Tracing + OTel (Spring Boot 3.x)**. Шаги: remove `spring-cloud-starter-sleuth`; add `micrometer-tracing-bridge-otel`, `opentelemetry-exporter-otlp`; `management.tracing.sampling.probability: 1.0` (dev), `0.1` (prod); auto-configuration in Boot 3.2+. Spans в MDC автоматически (`traceId`/`spanId`). Validate trace propagation через tests (Testcontainers + Jaeger).

6. **Trace-metrics correlation для debugging latency**. Micrometer + OTel bridge — автоматически добавляет exemplar (trace ID) к Prometheus histogram. В Grafana dashboard: p99 latency panel; click на spike → "Show traces" → Tempo query filtered by service + time window → identify slow span (e.g., DB query 5s). Drill-down to logs (Loki) by trace ID. End-to-end root cause в minutes.

7. **Custom span для business logic**. Manual instrumentation:
   ```java
   @Autowired Tracer tracer;
   public Order createOrder(OrderRequest req) {
     Span span = tracer.spanBuilder("order.create").setAttribute("order.type", req.getType()).startSpan();
     try (Scope s = span.makeCurrent()) {
       // business logic
       span.addEvent("inventory.checked", Attributes.of(AttributeKey.longKey("items"), count));
       return order;
     } catch (Exception e) {
       span.recordException(e).setStatus(StatusCode.ERROR);
       throw e;
     } finally { span.end(); }
   }
   ```
   В Jaeger/Tempo — видны business events в span, attribute `order.type` для filtering. Не over-instrument (каждый method) — добавь spans для significant business operations.

---

## 16. Профилирование Java в production (JFR, async-profiler flame graphs, Arthas, JMC, heap dump MAT, GC logs, JMX)

### Теория

- **Java Flight Recorder (JFR)**: low-overhead profiling framework (built-in JDK 11+, open source); event-based (JVM events, GC, allocation, IO, method profiling); `jcmd <pid> JFR.start duration=60s filename=/tmp/rec.jfr`; overhead <1% (в typical), 2-5% (detailed profile). JFR streaming (Java 14+) — real-time consumption в app. Custom events (`jdk.jfr.Event` subclass).
- **JDK Mission Control (JMC)**: UI для анализа JFR recordings; tabs: Method Profiling, Memory, GC, IO, Locks, Exceptions. Standalone app (RCP). JMC 9+ supports JDK 21+. JFR events are stable across JDK versions (JDK 9+).
- **async-profiler** (github.com/async-profiler/async-profiler): sampling profiler, low overhead, native (no JVMTI overhead для CPU); supports CPU, allocation, lock, page faults, off-CPU; output: flame graph SVG, JFR (compatible with JMC), HTML. `./profiler.sh -d 60 -f cpu.html <pid>`. Wall-clock mode (`-e wall`) для latency issues. SymRusenkov's dtrace-based на macOS.
- **Flame graphs** (Brendan Gregg): visualization of profiled stacks; width = % time in stack; y-axis = call stack depth; "icicle" variant (inverted); identify hot paths; flat top — hot method. Tools: `flamegraph.pl` (Perl), `async-profiler` built-in, `jfr2flame`.
- **Arthas** (Alibaba, github.com/alibaba/arthas): interactive Java diagnostic tool; attach to running JVM (no restart); commands: `dashboard` (overview), `thread` (top hot threads), `trace` (method calls with timing), `watch` (method return values), `jad` (decompile class), `redefine` (hot-swap class!), `profiler` (async-profiler wrapper), `sc` (search class), `monitor` (method stats). Invaluable для prod troubleshooting.
- **Heap dump analysis**:
  - **jmap** (deprecated в JDK 18+): `jmap -dump:live,format=b,file=heap.hprof <pid>`.
  - **jcmd**: `jcmd <pid> GC.heap_dump /tmp/heap.hprof`.
  - **Trigger on OOM**: `-XX:+HeapDumpOnOutOfMemoryError -XX:HeapDumpPath=/data/heapdumps/` (с date/`%p` для PID).
  - **Eclipse MAT** (Memory Analyzer Tool): `Leak Suspects Report`, `Dominator Tree`, `Histogram` (by class), `Path to GC Roots`, OQL (Object Query Language). Identify dominators (huge retained memory).
  - **VisualVM**: simpler, real-time.
- **GC logs**: `-Xlog:gc*=info:file=/tmp/gc.log:time,uptime,level,tags:filecount=5,filesize=10M` (unified logging since JDK 9); analyze with GCViewer, gceasy.io, Pedro's GC Log Analyzer. Key metrics: pause time, throughput, frequency, allocation rate, promotion rate.
- **JMX (Java Management Extensions)**: MBeans (Managed Beans) expose metrics и operations; `jconsole`, `jmc`, `VisualVM-MBeans`; remote JMX через RMI (`-Dcom.sun.management.jmxremote.port=9010 -Dcom.sun.management.jmxremote.ssl=false -Dcom.sun.management.jmxremote.authenticate=false`) — security risk, лучше local-only или JMXMP. Micrometer exposition JMX metrics.
- **Thread dump**: `jstack <pid> > thread.txt` или `jcmd <pid> Thread.print`; analyze: blocked threads (waiting on monitors), deadlocks (`jstack -l`), CPU hot threads (`top -H` + `printf "%x\n" tid` + grep в thread dump). Tool: fastthread.io, jstack.review.
- **Native memory tracking (NMT)**: `-XX:NativeMemoryTracking=detail`, `jcmd <pid> VM.native_memory summary` — breakdown по areas (Java heap, class, thread, code, GC, internal). Helps find off-heap leaks (Netty direct buffers, JNI).
- **JIT compilation analysis**: `-XX:+PrintCompilation` (JDK 8) или `-Xlog:jit*=info`; JITWatch для visualisation; `-XX:+UnlockDiagnosticCompilationOptions -XX:+PrintInlining`. Identify methods not inlined (hot but small).
- **Production-safe profiling principles**: всегда sampling, не instrumentation; async-profiler и JFR safe для prod (low overhead); avoid `jmap` без `live` на больших heaps (long pause); heap dumps на OOM через separate path; rotate GC logs; enable JMX readonly.

### Материалы

- **Дока**: JFR — https://docs.oracle.com/en/java/javase/21/jfapi/overview.html
- **Дока**: JMC — https://www.oracle.com/java/technologies/jdk-mission-control.html
- **Tool**: async-profiler — https://github.com/async-profiler/async-profiler
- **Tool**: Arthas — https://arthas.aliyun.com/doc/
- **Tool**: Eclipse MAT — https://eclipse.dev/mat/
- **Дока**: Java unified logging — https://docs.oracle.com/en/java/javase/21/docs/api/java.logging/module-summary.html
- **Статья**: "Java Performance" by Brendan Gregg — http://www.brendangregg.com/FlameGraphs/javaflamegraphs.html
- **Книга**: "Java Performance", Scott Oaks (O'Reilly, 3rd ed., 2024)
- **Книга**: "Optimizing Java", Bradley, Cao, Newland, Evans (O'Reilly, 2018)
- **Книга**: "The Java Performance Companion", Kirk Pepperdine (Addison-Wesley, 2023)

### Практика / Кейсы

1. **High CPU в production через async-profiler**. Симптом: Pod использует 100% CPU, latency растёт. Шаги (без restart): `kubectl exec pod -- sh -c './profiler.sh -d 60 -f /tmp/cpu.html 1'` (1 = Java PID inside container); download `kubectl cp pod:/tmp/cpu.html ./cpu.html`; открыть SVG в browser; найти hot path — `HashMap.resize` занимает 60%; в коде `ConcurrentHashMap` инициализируется с размером по умолчанию (16) → resizing под нагрузкой; fix: `new ConcurrentHashMap<>(initialCapacity=expectedSize / 0.75 + 1)`. Validate: повторный profile показывает fix.

2. **Heap leak через MAT**. Симптом: OOMKilled каждые 6h. Шаги: `-XX:+HeapDumpOnOutOfMemoryError -XX:HeapDumpPath=/data/dumps/`; после OOM скачать `heap.hprof`; открыть в MAT → `Leak Suspects` → `Problem Suspect 1: ConcurrentHashMap$Node[] retains 800MB`; `Dominator Tree` → найти что holds it — `CacheManager.instance` (singleton); `Path to GC Roots` → cache не имеет eviction; fix: добавить Caffeine `maximumSize(10000).expireAfterWrite(10min)`. Validate: 24h soak test.

3. **GC pause spikes через JFR + JMC**. Симптом: p99 latency spikes каждые 30s. Шаги: `jcmd <pid> JFR.start duration=5m filename=gc.jfr settings=profile`; открыть в JMC → `GC` tab; видны 500ms pauses G1GC mixed GC; cause: humongous allocations (>50% region size) для batch processing; fix: tune G1 region size (`-XX:G1HeapRegionSize=32m`) или избежать large arrays; alternative — ZGC (sub-ms pauses, JDK 21 GA). Validate: JFR повторно.

4. **Arthas hot-fix без redeploy**. Симптом: production bug в method, нужен quick fix без deploy. `arthas-boot.jar` attach к JVM: `java -jar arthas-boot.jar <pid>`; `jad com.example.Service` (decompile); `watch com.example.Service.process returnObj` (monitor calls); `redefine Service.class` (загрузить patched .class). Внимание: временная мера, нужен proper hotfix через deploy. Документировать incident.

5. **Thread deadlock diagnosis**. Симптом: app hang, no errors. `jstack -l <pid>` → `Found Java-level deadlock`; видны 2 threads в `waiting to lock` с циклом. Fix: re-order locks (consistent ordering) или `tryLock(timeout)` instead of `synchronized`. Long-running: в K8s `kubectl exec pod -- jstack 1 > tdump.txt`. Tool: fastthread.io для analysis. Add monitoring: JMX `ThreadInfo` deadlock detection, alert на deadlocked threads count > 0.

6. **Off-heap leak через NMT**. Симптом: RSS растёт, heap stable. `-XX:NativeMemoryTracking=detail`; `jcmd <pid> VM.native_memory summary`; `Internal` section растёт; suspect: Netty direct buffers или JNI. `jcmd <pid> VM.native_memory baseline` → work some time → `jcmd <pid> VM.native_memory detail.diff` показывает delta. Fix: Netty `-Djdk.nio.maxCachedBufferSize=262144` или `MaxDirectMemorySize=512m` + detection via `PlatformDependent.usedDirectMemory()`. Validate: RSS stable.

7. **JFR streaming для continuous profiling**. В Java 14+:
   ```java
   try (var rs = new RecordingStream()) {
     rs.enable("jdk.GCPhasePause").withThreshold(Duration.ofMillis(100));
     rs.onEvent("jdk.GCPhasePause", e -> {
       if (e.getDuration("duration").toMillis() > 100) {
         log.warn("Long GC pause: {}ms", e.getDuration("duration").toMillis());
       }
     });
     rs.startAsync();
   }
   ```
   Continuous monitoring без sampling overhead. Export metrics в Micrometer. Alternative: Cryostat (JFR management for K8s) — collects JFRs from all JVMs centrally.

---

## 17. SRE практики (SLI/SLO/SLA, error budget, toil, blameless postmortem, on-call)

### Теория

- **SLI (Service Level Indicator)**: quantitative measure of service level; examples: request success rate (200/total), latency p99 < 500ms, availability (uptime/total). Should be measurable from user perspective.
- **SLO (Service Level Objective)**: target for SLI; e.g., "99.9% of requests succeed over 28 days". Should be ambitious but achievable. Below SLO → unhappy users; above → wasted resources (could relax, ship faster).
- **SLA (Service Level Agreement)**: contractual consequence of missing SLO (usually stricter than SLO — e.g., SLA 99.5%, SLO 99.9%); customer-facing; financial penalties.
- **Error budget**: 1 - SLO; for 99.9% → 0.1% budget per period (e.g., 28 days → 43.2 min downtime, или 0.1% failed requests). Spending budget: feature launches (some risk), experimental features, faster releases. Exceeding budget → freeze new features, focus on reliability.
- **SLI/SLO framework**:
  - SLI = good events / total events (or bad events / total events).
  - Time-based vs event-based: time-based (uptime per minute), event-based (per request). Event-based preferred (more granular, user-centric).
  - Multi-window multi-burn-rate alerts: alert if burn rate exceeds threshold over multiple windows (e.g., 14.4x over 1h → page, 6x over 6h → ticket). From Google SRE Workbook.
- **Toil**: operational work that is manual, repetitive, automatable, tactical, no enduring value, O(scale). SRE target: <50% time on toil. Reduce through automation, self-service, better tools.
- **Blameless postmortem**: after incident, write detailed timeline + root cause + action items; no blame on individuals — focus on systems and processes. Psychological safety critical. Action items tracked to completion. Publish postmortems internally (or publicly — e.g., Google Cloud postmortems).
- **Postmortem structure**: summary, impact (users, revenue, duration), timeline (UTC), root cause, contributing factors, what went well, what went wrong, where got lucky, action items (with owners + due dates), lessons learned. Severity levels (SEV1-SEV4) determine depth.
- **On-call**: rotation (primary + secondary), schedule (1 week typical, follow-the-sun для global teams), compensation (extra pay / time off). Pager fatigue — monitor; ensure <2 pages/shift, <1 page/night. Runbook for every alert. Acknowledge <5 min, resolve <1h typical.
- **Incident response**: SEV1 → incident commander (IC), comms lead, ops lead. War room (physical or Slack/Zoom). Status page updates (Atlassian Statuspage). Time-boxed decision making.
- **Capacity planning**: based on SLOs + growth forecasts; headroom factor (typically 1.5-2x); capacity reviews quarterly; load testing (Gatling, k6, Locust) для validate.
- **Change management**: canary deployments, feature flags, blast radius limitation, rollback in <5min. Change advisory board (CAB) для high-risk changes.
- **Reliability tiers**: tier 1 (critical, user-facing, 99.99% SLO), tier 2 (important, 99.9%), tier 3 (internal, 99%). Different on-call, postmortem depth, etc.
- **Pre-mortems**: before launch, imagine it failed, identify causes, mitigate proactively.
- **Toil reduction techniques**: automation (scripts, operators), self-service platforms (developer portal), error budget-aware feature freeze, alert tuning (remove noise), documentation.
- **SRE team organization**: embedded SREs (in product teams), central SRE (platform), hybrid. Service reliability ladder: Level 1 (manual ops), Level 2 (some automation), Level 3 (self-service platform), Level 4 (fully automated).
- **User journey-based SLOs**: end-to-end SLO covering critical user journey (e.g., "checkout" = browse + cart + payment + confirmation), not just individual service SLOs.

### Материалы

- **Книга**: "Site Reliability Engineering", Beyer, Jones, Petoff, Murphy (Google, 2016) — бесплатно: https://sre.google/sre-book/table-of-contents/
- **Книга**: "The Site Reliability Workbook", Beyer et al. (Google, 2018) — бесплатно: https://sre.google/workbook/table-of-contents/
- **Книга**: "Observability Engineering", Majors, Fong-Jones, Miranda (O'Reilly, 2022)
- **Дока**: Google SRE — https://sre.google/
- **Дока**: Google SRE workbook SLI/SLO — https://sre.google/workbook/implementing-slos/
- **Статья**: "Atlassian Incident Handbook" — https://www.atlassian.com/incident-management/handbook
- **Статья**: "How to Run a Postmortem" — https://sre.google/workbook/postmortem/
- **Дока**: SRE weekly — https://sreweekly.com/
- **Tool**: Sloth (SLO generator) — https://github.com/slok/sloth
- **Tool**: Pyrra (SLO backend) — https://pyrra.dev/

### Практика / Кейсы

1. **Define SLI/SLO для e-commerce checkout**. User journey: browse → add to cart → checkout → payment → confirmation. SLIs: 1) availability (200 response), 2) latency p99 < 800ms (frontend), < 200ms (backend API), 3) end-to-end checkout success rate > 99%. SLO: 99.9% availability + 99% latency per 28 days. Error budget: 0.1% × 28d = 43.2 min downtime или 0.1% failed requests. Implement: recording rules compute SLI; dashboard shows SLO + budget burn; alerts on burn rate.

2. **Multi-window multi-burn-rate alerts**. Sloth config:
   ```yaml
   slos:
   - name: api-availability
     objective: 99.9
     sli:
       events:
         error_query: sum(rate(http_requests_total{status=~"5.."}[{{.window}}]))
         total_query: sum(rate(http_requests_total[{{.window}}]))
     alerting:
       windows:
       - { severity: page, short: 5m, long: 1h, factor: 14.4 }
       - { severity: ticket, short: 30m, long: 6h, factor: 6 }
       - { severity: ticket, short: 2h, long: 1d, factor: 3 }
   ```
   `14.4x burn rate over 1h` → exhaust 30% budget in 1h → page. `6x over 6h` → exhaust 25% in 6h → ticket.

3. **Blameless postmortem template после major incident**. Template (Markdown):
   ```markdown
   # Postmortem: [Incident name] (SEV1, 2024-XX-XX)

   ## Summary
   [1-paragraph executive summary]

   ## Impact
   - Duration: 14:32–15:18 UTC (46 min)
   - Users affected: ~50k (20% of active)
   - Revenue impact: $X
   - Severity: SEV1

   ## Timeline (UTC)
   - 14:32 — Alert fired: HighErrorRate on api service
   - 14:35 — On-call acked, started investigation
   - 14:40 — Identified recent deploy (v1.4.2) as trigger
   - 14:55 — Rollback initiated
   - 15:05 — Rollback completed, error rate normalizing
   - 15:18 — All clear

   ## Root Cause
   [Technical root cause]

   ## Contributing Factors
   - [Factor 1]
   - [Factor 2]

   ## What Went Well
   - Alert fired promptly
   - Rollback completed in <10 min

   ## What Went Wrong
   - Deploy did not have canary stage
   - Test coverage missed this scenario

   ## Action Items
   - [ ] Add canary deployment (owner: Alice, due: 2024-XX-XX)
   - [ ] Add integration test for X (owner: Bob, due: 2024-XX-XX)
   - [ ] Update runbook (owner: Carol, due: 2024-XX-XX)

   ## Lessons Learned
   [Insights for future]
   ```
   Review meeting within 5 business days. Action items tracked in Jira.

4. **On-call setup: rotation, runbooks, escalation**. Primary + secondary on-call, 1-week shifts, ~6 engineers в rotation. PagerDuty schedule + escalation policy (3 levels: primary → secondary → manager). Runbooks per alert (linked в PagerDuty). On-call compensation: extra day off + on-call bonus. Quarterly on-call review: pages per shift, top noise alerts, time-to-resolve. SREs aim <2 pages/shift.

5. **Toil audit + reduction plan**. Quarterly: each SRE logs time spent on toil categories (manual deploys, troubleshooting, capacity requests, ad-hoc requests, repetitive debugging). Identify top 3 toil sources; prioritize automation; target 30% reduction quarter-over-quarter. Examples: 1) self-service deploy via ArgoCD (was manual kubectl), 2) Terraform modules для infrastructure (was ClickOps), 3) ChatOps для common ops (restart pod, view logs).

6. **Pre-mortem для major release**. Перед launch of new payment gateway: 1-hour session, imagine launch failed catastrophically; brainstorm causes (integration with bank fails under load, fraud detection false positives, no rollback path, etc.); assign mitigations (load test, canary, rollback drill); document in launch checklist. Compare with actual launch — feed lessons back into pre-mortem template.

7. **Capacity planning с load testing + SLO-based headroom**. Gatling/k6 load test: simulate 2x peak traffic (Black Friday); measure: latency p99 (target < SLO), error rate (target < 1%), resource utilization (CPU/mem < 80%). Capacity: provision for 2x current + 50% growth = 3x. Quarterly review with product team (forecast growth). Alert if utilization > 70% (need capacity), > 90% (emergency).

---

## 18. IaC (Terraform state/modules/providers/workspaces/Atlantis; Pulumi; Crossplane; Ansible)

### Теория

- **IaC principles**: declarative (describe desired state, tool reconciles); idempotent (apply multiple times = same result); versioned (Git); reviewed (PR); drift detection (compare live vs declared).
- **Terraform** (HashiCorp): HCL language; providers (AWS, GCP, Azure, Kubernetes, Vault, etc.); resources (`resource "aws_instance" "web" {...}`); data sources (read existing); modules (reusable); state (terraform.tfstate — JSON, records resource IDs and metadata). Workflows: `init` (download providers), `plan` (diff), `apply` (execute), `destroy`.
- **Terraform state**: critical — single source of truth for what Terraform manages; backend (`local`, `s3` + DynamoDB lock, `remote`/TFC, `gcs`, `azurerm`); state locking (prevent concurrent apply); state separation per environment (separate state files); sensitive data в state (passwords!) → restrict access, encryption.
- **Terraform modules**: reusable package of resources; `module "vpc" { source = "terraform-aws-modules/vpc/aws"; version = "5.0" }`; inputs (variables), outputs; `source` = local path, Git, Terraform Registry, S3. Versioning через Git tags or Registry versions. Module composition (modules calling modules).
- **Terraform providers**: plugins (RPC) implementing resource types for specific API (AWS, etc.); `provider "aws" { region = "eu-west-1" }; alias` для multi-region. Provider configuration, authentication (env vars, IAM roles, OIDC). Provider versioning via `required_providers`.
- **Terraform workspaces**: `terraform workspace select prod`; same config, different state files; useful for similar environments (dev/staging/prod with same resources). Limitation: not isolated (variables не per-workspace); often separate state files preferred over workspaces.
- **Terragrunt** (Gruntwork): wrapper for Terraform; DRY (keep config DRY), remote state management, parallel apply, dependencies. Useful для multi-environment + multi-account setups.
- **Atlantis** (Runatlantis): GitOps for Terraform; webhook receiver on PR; runs `terraform plan`, posts output as PR comment; on merge — `terraform apply`. Per-dir workflow (run plan per changed dir). Server-side (not developer local). RBAC via GitHub teams.
- **Pulumi** (Pulumi Corp): real programming languages (TypeScript, Python, Go, C#, Java) для IaC; natural loops, conditionals, functions; same resource model as Terraform (via providers); state в Pulumi Service, S3, local. Better for complex logic, testing, integration с app code.
- **Crossplane** (CNCF, Upbound): K8s-native IaC; providers as K8s controllers; resources as CRDs; `kubectl get rdsinstances.aws.crossplane.io`; GitOps via ArgoCD/Flux. Unified control plane for K8s + cloud. Composition (high-level abstractions like `XDatabase`).
- **Ansible** (Red Hat): agentless (SSH), procedural YAML playbooks; idempotent modules; inventory (INI/YAML, dynamic for cloud); roles (reusable); variables, templates (Jinja2). Use cases: configuration management (not provisioning), app deployment, orchestration. Alternative: Salt, Chef, Puppet.
- **IaC для K8s vs Helm/Kustomize**: Terraform для cloud resources (VPC, RDS, IAM), Helm/Kustomize для K8s resources; Crossplane bridges (cloud resources via K8s CRDs). Pulumi — both cloud + K8s.
- **Drift detection**: `terraform plan -detailed-exitcode` returns non-zero if drift; periodic check (cron); alert on drift. Tools: Terraform Cloud drift detection, driftctl, Firefly.
- **Secret management в IaC**: never inline secrets; use Vault provider, AWS SSM Parameter Store, External Secrets Operator; mark variables as `sensitive = true` (но state всё равно хранит). `terraform plan` mask sensitive outputs.
- **Terraform Cloud/Enterprise**: hosted Terraform with workspaces, RBAC, policy-as-code (Sentinel), cost estimation, VCS integration. Alternative: Spacelift, env0.
- **Policy as code**: Sentinel (Terraform Enterprise), OPA/Rego (open source), Kyverno (K8s); enforce tagging, encryption, regions, cost limits. CI-time and apply-time checks.
- **Cost estimation**: infracost (open source) — PR comment with monthly cost delta; Terraform Cloud cost estimation; CloudHealth, Spot.io.

### Материалы

- **Дока**: Terraform — https://developer.hashicorp.com/terraform/
- **Дока**: Terraform modules — https://developer.hashicorp.com/terraform/language/modules
- **Дока**: Terraform state — https://developer.hashicorp.com/terraform/language/state
- **Дока**: Terragrunt — https://terragrunt.gruntwork.io/
- **Дока**: Atlantis — https://www.runatlantis.io/
- **Дока**: Pulumi — https://www.pulumi.com/docs/
- **Дока**: Crossplane — https://docs.crossplane.io/
- **Дока**: Ansible — https://docs.ansible.com/ansible/latest/
- **Книга**: "Terraform: Up & Running, 3rd ed.", Yevgeniy Brikman (O'Reilly, 2022)
- **Книга**: "Infrastructure as Code", Kief Morris (O'Reilly, 2nd ed., 2020)

### Практика / Кейсы

1. **Terraform multi-environment с separate state + Atlantis**. Структура:
   ```
   infra/
     modules/
       vpc/
       eks/
       rds/
     environments/
       dev/
         main.tf (module "vpc" { source = "../../modules/vpc" })
         backend.tf (backend "s3" { bucket = "tf-state-dev" })
       staging/...
       prod/...
   ```
   Atlantis `atlantis.yaml`:
   ```yaml
   projects:
   - { dir: environments/dev, workspace: default, workflow: terragrunt }
   - { dir: environments/staging, workspace: default, workflow: terragrunt }
   ```
   On PR — `terraform plan` per changed dir, comment with plan; on merge — `terraform apply`. RBAC: only `terraform-approvers` team can approve apply.

2. **Crossplane: PostgreSQL RDS через K8s CRD + GitOps**. Crossplane AWS provider installed; `Composition` `XPostgreSQLInstance` (high-level abstraction) → `RDSInstance` (real AWS RDS); developer creates `XPostgreSQLInstance` via YAML; GitOps via ArgoCD. Connection info written to `Secret`. App consumes Secret. Benefits: developer self-service via K8s, no AWS console, GitOps audit trail.

3. **Ansible для конфигурации bastion host + java deployment on VMs**. `inventory.yml` (dynamic from AWS EC2); `playbook.yml`:
   ```yaml
   - hosts: app_servers
     become: true
     roles:
     - common    # users, packages, security hardening
     - java      # JDK install, JVM tuning
     - app       # deploy artifact, systemd service, logrotate
     vars: { jdk_version: "17", app_version: "1.4.2" }
   ```
   Roles: `roles/java/tasks/main.yml` installs JDK via apt, sets `JAVA_HOME`, configures cgroups. Idempotent (check if already installed). Run via `ansible-playbook -i inventory.yml playbook.yml`. Use for hybrid (VMs + K8s) environments.

4. **Pulumi для multi-cloud serverless (AWS Lambda + GCP Cloud Run)**. TypeScript:
   ```typescript
   import * as aws from "@pulumi/aws";
   import * as gcp from "@pulumi/gcp";
   const fn = new aws.lambda.Function("api", { runtime: "java21", code: ..., handler: "com.example.Handler::handle" });
   const svc = new gcp.cloudrun.Service("api", { template: { spec: { containers: [{ image: fnImage }] } } });
   export const awsUrl = fn.invokeArn;
   export const gcpUrl = svc.statuses[0].url;
   ```
   Real programming language — loops, conditionals, tests. CI: `pulumi preview` on PR, `pulumi up` on merge.

5. **Drift detection + alerting**. Cronjob (GitHub Actions scheduled): `terraform plan -detailed-exitcode`; exit code 2 = drift; alert to Slack. `driftctl` для unsanctioned resources (resources in cloud not in Terraform state). Quarterly drift review meeting. Remediation: either update Terraform (intended change) or destroy resource (unintended).

6. **Policy as code с OPA + Terraform**. `conftest test plan.json` — Rego policies:
   ```rego
   package main
   deny[msg] {
     resource := input.resource_changes[_]
     resource.type == "aws_s3_bucket"
     not resource.change.after.server_side_encryption_configuration
     msg := sprintf("S3 bucket %s must have encryption", [resource.address])
   }
   ```
   CI-time check on `terraform plan` output; block PR if violations. Policies: tagging required, encryption mandatory, no public S3, regions allowed. Terraform Cloud Sentinel for enterprise.

7. **Cost estimation в CI (infracost)**. GitHub Actions: `infracost diff --path plan.json --format github-comment`; PR comment shows monthly cost delta ("+$50/mo"); approver sees cost before merge. Monthly budget alerts via AWS Budgets / GCP budgets; auto-shutdown of dev environments outside business hours (savings 60%+).

---

## 19. CI/CD для Java на K8s (GitLab CI/Jenkins/GitHub Actions: build→test→SAST/SCA→image→scan→push→deploy→canary; Argo Rollouts)

### Теория

- **CI/CD principles**: build once, deploy many; immutable artifacts; deterministic builds; signed artifacts (provenance); fast feedback (test <10min for PRs); promote artifacts, not rebuild.
- **CI stages (typical Java pipeline)**:
  1. **Checkout** + cache (`~/.m2` or `~/.gradle`).
  2. **Compile** (`mvn compile` or `./gradlew classes`).
  3. **Unit tests** (`mvn test`) + coverage (JaCoCo).
  4. **Static analysis (SAST)**: SpotBugs, SonarQube, Error Prone, Codacy.
  5. **Dependency scan (SCA)**: OWASP Dependency-Check, Snyk, Trivy fs scan, Dependabot.
  6. **Integration tests** (Testcontainers, embedded Kafka) — slower, may be separate stage.
  7. **Package** (jar/war).
  8. **Build image** (Docker multi-stage, JIB, Buildpacks).
  9. **Image scan** (Trivy, Grype).
  10. **SBOM generation** (Syft, CycloneDX).
  11. **Sign image** (cosign keyless).
  12. **Push to registry**.
  13. **Deploy** (dev → staging → prod via promotion).
  14. **Smoke tests / canary analysis**.
  15. **Promote or rollback**.
- **Build tools comparison**: 
  - **Dockerfile**: most control, manual layering.
  - **JIB** (Google): Maven/Gradle plugin, builds image without Docker, layered for cache.
  - **Buildpacks** (CNCF, Pack): auto-detect framework, build optimized image, reproducible.
- **GitHub Actions**: YAML workflows; runners (hosted GitHub, self-hosted, ARM); matrix builds; reusable workflows; composite actions; OIDC to cloud (no long-lived secrets); cache via `actions/cache` or `cache` property on `setup-java`/`setup-maven`.
- **GitLab CI**: `.gitlab-ci.yml`; stages, jobs, runners (shared, specific); auto-devOps templates; dind (Docker-in-Docker) for image builds; parent-child pipelines; merge request pipelines.
- **Jenkins**: pipeline-as-code (`Jenkinsfile`, Groovy DSL); multibranch pipelines; shared libraries; agents (Kubernetes plugin for dynamic agents); blue ocean UI; high customizability, but more maintenance.
- **Argo Rollouts** (Argo project): progressive delivery controller; `Rollout` CR replaces `Deployment`; strategies: `canary` (weighted traffic), `blueGreen` (instant switch); analysis templates (Prometheus, Wavefront, Datadog, NewRelic); auto-promote/rollback.
- **Canary deployment**: route small % of traffic to new version; monitor error rate, latency, business metrics; auto-promote if healthy, rollback if not. Tools: Argo Rollouts + Istio/SM (weighted), Flagger (alternative).
- **Blue-green deployment**: two environments (blue = current, green = new); switch traffic; rollback = switch back; requires 2x capacity during switch.
- **Feature flags**: decouple deploy from release; LaunchDarkly, OpenFeature, ConfigCat, Flagsmith; trunk-based development; kill switches; A/B testing; dark launches.
- **Database migrations в CI/CD**: Flyway/Liquibase migrations as part of deploy; backward-compatible migrations (expand/contract pattern); never run destructive migration in same release as code that breaks old version; rollback migrations are dangerous (data loss).
- **Secret management в CI**: OIDC federation (short-lived tokens, no static secrets); Vault; cloud-native (AWS Secrets Manager, GCP Secret Manager); never commit secrets; mask in logs.
- **Pipeline observability**: metrics (build duration, failure rate, MTTR); DORA metrics (deployment frequency, lead time, change failure rate, MTTR); tools: DevLake, OpsLevel, Sleuth.
- **Trunk-based development vs GitFlow**: trunk-based (main branch, short-lived feature branches, frequent merge) better for CI/CD; GitFlow (release branches) better for versioned releases.
- **Quality gates**: test coverage threshold, sonar quality gate, no critical CVEs, no failing tests, code review approval. Enforce via branch protection.

### Материалы

- **Дока**: GitHub Actions — https://docs.github.com/en/actions
- **Дока**: GitLab CI/CD — https://docs.gitlab.com/ee/ci/
- **Дока**: Jenkins Pipeline — https://www.jenkins.io/doc/book/pipeline/
- **Дока**: Argo Rollouts — https://argo-rollouts.readthedocs.io/
- **Дока**: JIB — https://github.com/GoogleContainerTools/jib
- **Дока**: Buildpacks — https://buildpacks.io/docs/
- **Дока**: OpenFeature — https://docs.openfeature.dev/
- **Дока**: DORA metrics — https://cloud.google.com/blog/products/devops-sre/using-the-four-keys-to-measure-your-devops-performance
- **Книга**: "Continuous Delivery", Jez Humble, David Farley (Addison-Wesley, 2010)
- **Книга**: "Accelerate", Forsgren, Humble, Kim (IT Revolution, 2018)

### Практика / Кейсы

1. **GitHub Actions pipeline для Spring Boot**. `.github/workflows/build.yml`:
   ```yaml
   jobs:
     build:
       runs-on: ubuntu-latest
       permissions: { contents: read, packages: write, id-token: write }
       steps:
       - uses: actions/checkout@v4
       - uses: actions/setup-java@v4
         with: { distribution: temurin, java-version: '21', cache: maven }
       - run: mvn -B verify
       - uses: SonarSource/sonarqube-scan-action@v2
         env: { SONAR_TOKEN: ${{ secrets.SONAR_TOKEN }} }
       - run: mvn org.cyclonedx:cyclonedx-maven-plugin:makeBom
       - uses: docker/build-push-action@v5
         with: { push: true, tags: ghcr.io/${{ github.repository }}:${{ github.sha }}, cache-from: type=gha, cache-to: type=gha,mode=max }
       - run: trivy image --exit-code 1 --severity CRITICAL ghcr.io/${{ github.repository }}:${{ github.sha }}
       - uses: sigstore/cosign-installer@v3
       - run: cosign sign --yes ghcr.io/${{ github.repository }}@${{ steps.build.outputs.digest }}
     deploy-staging:
       needs: build
       if: github.ref == 'refs/heads/main'
       runs-on: ubuntu-latest
       steps:
       - run: |
           # Update Helm values.yaml with new image tag
           # Commit to git-infra repo
           # ArgoCD auto-syncs staging
   ```
   OIDC auth to AWS (no static AWS keys); reusable workflow для common steps.

2. **Argo Rollouts canary для Spring Boot**. `Rollout` CR:
   ```yaml
   strategy:
     canary:
       trafficRouting:
         istio: { virtualService: { name: api-vs, routes: [primary] } }
       steps:
       - setWeight: 5
       - pause: { duration: 5m }
       - analysis:
           templates: [{ templateName: success-rate, args: [{ name: service-name, value: api }] }]
       - setWeight: 25
       - pause: { duration: 10m }
       - setWeight: 50
       - pause: { duration: 10m }
       - setWeight: 100
   ```
   `AnalysisTemplate`:
   ```yaml
   spec:
     args: [{ name: service-name }]
     metrics:
     - name: success-rate
       interval: 1m
       successCondition: result[0] >= 0.95
       failureLimit: 3
       provider: { prometheus: { address: http://prometheus:9090, query: sum(rate(http_requests_total{service="{{args.service-name}}",status!~"5.."}[5m])) / sum(rate(http_requests_total{service="{{args.service-name}}"}[5m])) } }
   ```
   Auto-rollback если success rate < 95% for 3 intervals.

3. **Database migration strategy (expand/contract)**. Stage 1 (release N): add new nullable column (no app changes); Stage 2 (release N+1): dual-write to old + new column, read from new; Stage 3 (release N+2): read from new only; Stage 4 (release N+3): drop old column. Each release backward-compatible. Flyway migrations per stage. Tests cover both schemas during transition.

4. **Feature flag dark launch**. LaunchDarkly flag `new-payment-gateway`; deploy new code with flag OFF in prod; enable for internal users (1% traffic); monitor metrics; expand to 5% → 25% → 100% via flag; kill switch if issues. Code path: `if (flag.enabled(userId)) { newGateway.charge(); } else { oldGateway.charge(); }`. OpenFeature SDK for vendor-neutral.

5. **DORA metrics dashboard**. Pipelines emit events to DevLake or DataDog:
   - **Deployment frequency**: deploys per day/week.
   - **Lead time for changes**: commit → production time.
   - **Change failure rate**: % deploys causing incident.
   - **MTTR**: incident duration.
   - Elite: >1 deploy/day, <1h lead, <15% failure, <1h MTTR. Track monthly, share with engineering.

6. **Jenkins Kubernetes pipeline с dynamic agents**. `Jenkinsfile`:
   ```groovy
   pipeline {
     agent {
       kubernetes {
         yaml '''
           apiVersion: v1
           kind: Pod
           spec:
             containers:
             - { name: maven, image: maven:3.9-eclipse-temurin-21, command: ["cat"], ttyEnabled: true }
             - { name: docker, image: docker:24, command: ["cat"], ttyEnabled: true, securityContext: { privileged: true } }
         '''
       }
     }
     stages {
       stage('Build') { steps { container('maven') { sh 'mvn -B verify' } } }
       stage('Image') { steps { container('docker') { sh 'docker build -t registry/app:$BUILD_NUMBER .' } } }
     }
   }
   ```
   Dynamic pod per build, scales with workload; no idle agents; shared library for common steps.

7. **Pipeline security: supply chain hardening**. Steps: 1) Pin action versions to commit SHA (not tag), 2) `actions/dependency-review-action` on PR for vulnerable deps, 3) Signed commits (GPG/Sigstore gitsign), 4) Branch protection + required reviews + status checks, 5) SLSA provenance attestation via `slsa-github-generator`, 6) cosign image signing + Kyverno admission verification in cluster, 7) Read-only tokens default, escalate via OIDC just-in-time. Document supply chain threat model.

---

## 20. Chaos Engineering (Chaos Mesh, Litmus, Game days, steady state, blast radius)

### Теория

- **Chaos Engineering principles** (Principles of Chaos, principlesofchaos.org): 1) define steady state, 2) hypothesize it will continue, 3) introduce real-world events (servers crash, network partitions, latency), 4) validate hypothesis. Goal: build confidence in system resilience through experimentation.
- **Steady state hypothesis**: measurable indicators that system is healthy (e.g., error rate < 0.1%, p99 < 500ms, throughput > 1000 RPS). Before experiment — measure steady state; during — verify it holds; after — analyze. Without steady state, chaos is just breaking things.
- **Blast radius**: scope of impact of an experiment; start small (one Pod, one AZ); gradually expand (entire AZ, multiple services); use circuit breakers — abort experiment if steady state violated. Feature flags + canary environments to limit.
- **Chaos Mesh** (CNCF, MeshDynamics): K8s-native chaos platform; CRDs: `PodChaos` (kill/pause), `NetworkChaos` (delay, loss, duplicate, partition), `StressChaos` (CPU/memory), `IOChaos` (latency, errors), `TimeChaos` (skew clock), `DNSChaos`, `HTTPChaos` (mutate request/response), `JVMChaos` (Java-specific: GC, exception, latency). Workflow `Workflow` CR for multi-step experiments.
- **Litmus Chaos** (CNCF, Harness): alternative K8s chaos platform; ChaosHub (chaos experiment catalog); `ChaosEngine`, `ChaosResult`; GitOps-friendly.
- **Gremlin** (commercial): enterprise chaos platform; attacks: CPU, memory, disk, network, process, shutdown; scenarios + scheduling; safety controls.
- **AWS Fault Injection Service (FIS)**: managed chaos for AWS resources (EC2, RDS, ECS, EKS, network); integrates with CloudWatch alarms for auto-rollback.
- **Game days**: scheduled exercises; team simulates incidents; pre-defined scenarios or open-ended; learnings feed into runbooks, architecture, training. Format: 1) brief on system + scenario, 2) inject faults, 3) team responds as if real incident, 4) debrief (what worked, what didn't, action items).
- **Experiment design**:
  - **Hypothesis**: "If AZ-A fails, traffic will route to AZ-B/C within 30s, no user impact."
  - **Steady state metrics**: error rate, latency, throughput.
  - **Fault injection**: terminate EC2 instances in AZ-A.
  - **Blast radius**: one service in staging first.
  - **Duration**: 5 min fault, 10 min recovery, 5 min monitoring.
  - **Abort conditions**: error rate > 5%, latency p99 > 2s.
- **Java-specific chaos**: JVM GC pressure (jcmd GC.run), OOM simulation (allocate big arrays), thread leak, slow method injection (Arthas), classpath corruption. Chaos Mesh `JVMChaos` covers common scenarios.
- **Dependency failure simulation**: mock external API failures (Stripe, Twilio), simulate database failover, message queue partition. Tests retry logic, circuit breakers, fallback behavior.
- **Network chaos scenarios**: latency (50ms, 200ms, 1s), packet loss (1%, 5%, 10%), partition (split-brain), DNS failure, bandwidth limit. Reveals timeout configuration issues, retry storms, cascade failures.
- **Production chaos**: advanced practice — run experiments in prod (because staging doesn't catch prod-specific issues); start small, business hours, with monitoring; Netflix Chaos Monkey pioneered this.
- **Chaos in CI/CD**: continuous chaos — run small chaos experiments in pipeline (e.g., kill random Pod, verify HPA recovers); catches regressions in resilience.
- **Resilience patterns validated by chaos**: bulkhead (isolate failures), circuit breaker (stop calling failing service), retry with backoff, timeout, fallback (cache, default value), idempotency.
- **Post-experiment analysis**: did steady state hold? Did monitoring detect? Did alert fire? Did on-call respond correctly? Did recovery happen automatically or manual? Action items: fix monitoring gaps, tune timeouts, add fallbacks, update runbooks.

### Материалы

- **Сайт**: Principles of Chaos — https://principlesofchaos.org/
- **Дока**: Chaos Mesh — https://chaos-mesh.org/docs/
- **Дока**: Litmus Chaos — https://litmuschaos.github.io/litmus/
- **Дока**: AWS FIS — https://docs.aws.amazon.com/fis/latest/userguide/
- **Дока**: Gremlin — https://www.gremlin.com/docs/
- **Книга**: "Chaos Engineering", Casey Rosenthal, Nora Jones (O'Reilly, 2020)
- **Книга**: "Chaos Engineering: Crash Course", Mikaila Allen (Apress, 2023)
- **Статья**: Netflix Chaos Monkey — https://netflixtechblog.com/chaos-monkey-release-into-the-wild-352fe0b31e6e
- **Статья**: "Game Day Exercises" AWS — https://aws.amazon.com/builders-library/gameday/
- **Дока**: Chaos Mesh JVMChaos — https://chaos-mesh.org/docs/simulate-java-application-chaos/

### Практика / Кейсы

1. **Chaos Mesh setup + Pod kill experiment для Java service**. Install Chaos Mesh via Helm:
   ```bash
   helm install chaos-mesh chaos-mesh/chaos-mesh -n chaos-testing --create-namespace
   ```
   Experiment:
   ```yaml
   apiVersion: chaos-mesh.org/v1alpha1
   kind: PodChaos
   metadata: { name: api-pod-kill }
   spec:
     action: pod-kill
     mode: one
     selector: { labelSelectors: { app: api } }
     scheduler: { cron: "@every 10m" }
   ```
   Steady state: error rate < 0.1%, p99 < 500ms. Validate: HPA recovers replicas within 60s; Service routes around killed Pod; no user-visible errors. Run continuously as part of resilience validation.

2. **Network chaos: simulate latency to PostgreSQL**. `NetworkChaos`:
   ```yaml
   spec:
     action: delay
     mode: all
     selector: { labelSelectors: { app: api } }
     delay: { latency: "200ms", correlation: "100", jitter: "50ms" }
     duration: "5m"
   ```
   Hypothesis: connection pool timeout (HikariCP `connectionTimeout: 30s`) covers 200ms latency, no errors. Validate: error rate < 0.5% (some requests slow but succeed). If fails — tune HikariCP + retries + circuit breaker. Test application behavior under degraded DB.

3. **Game day: AZ failure для e-commerce**. Scenario: AWS AZ-A failure during peak hours. Steps: 1) mark all EC2 + RDS in AZ-A as failed (FIS or Chaos Mesh network partition), 2) team responds — verifies failover (RDS multi-AZ, ALB health checks remove AZ-A, autoscaler replaces in AZ-B/C), 3) monitor error rate + latency, 4) debrief — what worked (auto-failover), what didn't (some singletons on AZ-A took time to recover). Action items: identify AZ-singleton anti-patterns, add cross-AZ replication, update runbooks. Quarterly cadence.

4. **JVMChaos: GC pressure на Spring Boot**. `JVMChaos`:
   ```yaml
   spec:
     action: gc
     mode: all
     selector: { labelSelectors: { app: api } }
     duration: "2m"
   ```
   Forces GC pressure. Steady state: p99 < 500ms. Validate: GC pauses don't exceed 1s; GC logs show behavior; if fails — tune GC (`-XX:MaxGCPauseMillis`, switch to ZGC). Identify memory pressure issues.

5. **Dependency failure: Stripe API outage**. Simulate via `HTTPChaos` или external mock:
   ```yaml
   spec:
     action: abort
     mode: all
     selector: { labelSelectors: { app: api } }
     target: Request
     port: 443
     abort: { code: INTERNAL_HTTP_ERROR }
   ```
   Validate: payment fallback to secondary gateway (if implemented) or graceful degradation (return "try again later" instead of 500). Test Resilience4j circuit breaker opens after N failures. Recovery — verify circuit closes when Stripe back.

6. **Continuous chaos in CI/CD pipeline**. GitHub Actions job after staging deploy: run Chaos Mesh experiment (kill Pod), verify steady state via k6 load test (errors < 1%), then allow prod deploy. Catches regressions in HPA, probes, retry logic. Failure → block prod deploy, page SRE.

7. **Blast radius escalation strategy для prod chaos**. Phased rollout: 1) staging, full chaos suite, 2) prod, single Pod kill during business hours, monitor 1 week, 3) prod, AZ-level during low traffic, 4) prod, cross-region failover test (with backup). Each phase: well-defined abort conditions, rollback in <5min, post-experiment review. Communication: notify stakeholders before prod experiments, status page note during. Build organizational confidence gradually.

---

## Сводка раздела

- **Объём:** 20 подразделов, каждый с теорией (10-20 пунктов), материалами (4-10 реальных источников: книги, доки, статьи), практикой (4-7 трудных многоступенчатых кейсов).
- **Фокус:** Java-специфика в DevOps — JVM в контейнерах (`MaxRAMPercentage`, GC tuning, GC logs, JFR, async-profiler), Spring Boot Actuator/Micrometer, layered jars, OpenTelemetry Java agent, Arthas для hot-fixes, JVMChaos.
- **Связь с другими разделами:**
  - **01 (Java Core)**: Process API, JMX, JFR — основа для observability.
  - **02 (Java Advanced)**: JVM tuning в контейнерах (`-XX:MaxRAMPercentage`), GC logs, virtual threads + K8s CPU limits, heap dump analysis (MAT), JFR streaming.
  - **03 (Build/Test)**: Testcontainers, Maven/Gradle cache в multi-stage Docker builds, JIB/Buildpacks.
  - **04 (Spring)**: Actuator/Micrometer, Spring Boot layered jars, OTel Spring Boot starter, Spring Cloud Kubernetes, Micrometer Tracing.
  - **05 (Persistence)**: CloudNativePG, Strimzi (Kafka), Flyway migrations in K8s Job, DB backup strategies.
  - **06 (Distributed)**: Kafka on K8s (Strimzi), RabbitMQ Cluster Operator, trace propagation через очереди.
  - **08 (Arch/Leadership)**: SRE-культура, postmortem-процесс, on-call ротации, release management, capacity planning.

- **Топ-12 источников (канонические доки):**
  1. `https://kubernetes.io/docs/` — Kubernetes documentation (Pods, Deployments, Services, Probes, NetworkPolicy, Storage)
  2. `https://docs.docker.com/` — Docker documentation (Dockerfile, multi-stage, BuildKit, Compose)
  3. `https://prometheus.io/docs/` — Prometheus + PromQL + Alertmanager
  4. `https://grafana.com/docs/` — Grafana + Loki + Tempo + Mimir
  5. `https://opentelemetry.io/docs/` — OpenTelemetry (Java SDK, agent, Collector, semantic conventions)
  6. `https://argoproj.github.io/` — Argo CD, Argo Rollouts
  7. `https://argo-cd.readthedocs.io/` — Argo CD documentation
  8. `https://istio.io/latest/docs/` — Istio service mesh
  9. `https://helm.sh/docs/` — Helm charts and templates
  10. `https://developer.hashicorp.com/terraform/` — Terraform
  11. `https://sre.google/sre-book/table-of-contents/` — Google SRE Book (free)
  12. `https://chaos-mesh.org/docs/` — Chaos Mesh

- **Ключевые книги (O'Reilly / Manning / Apress / Addison-Wesley):**
  - "Kubernetes: Up and Running, 3rd ed." (Burns/Hightower/Luedde)
  - "Kubernetes in Action, 2nd ed." (Lukša)
  - "Production Kubernetes" (Rosso et al.)
  - "Docker Deep Dive" (Poulton)
  - "Helm in Action" (Litkas)
  - "Prometheus: Up & Running, 2nd ed." (Brazil)
  - "Observability Engineering" (Majors/Fong-Jones/Miranda)
  - "Mastering Distributed Tracing" (Shkuro)
  - "Java Performance, 3rd ed." (Oaks)
  - "Site Reliability Engineering" (Google, free online)
  - "The Site Reliability Workbook" (Google, free online)
  - "Terraform: Up & Running, 3rd ed." (Brikman)
  - "Continuous Delivery" (Humble/Farley)
  - "Accelerate" (Forsgren/Humble/Kim)
  - "Chaos Engineering" (Rosenthal/Jones)

- **Статус:** завершено. 20 подразделов с 3-уровневой структурой (Теория / Материалы / Практика), реальные ссылки на kubernetes.io / docs.docker.com / prometheus.io / grafana.com / opentelemetry.io / argoproj.github.io / istio.io / helm.sh / developer.hashicorp.com / sre.google / chaos-mesh.org.
