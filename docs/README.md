# 🚀 Java Senior Roadmap — Полное руководство по пути до Senior/Staff

> Подробное дерево изучения Java с теорией, реальными материалами (книги, статьи, документация) и трудными многоступенчатыми кейсами. Каждый раздел построен по схеме **Теория → Материалы → Практика**.

---

## 📊 Общая статистика

| Метрика | Значение |
|---|---|
| Разделов | **8** |
| Подразделов | **~174** |
| Суммарно строк | **~14 985** |
| Реальных ссылок | **~1 659** (книги, статьи, документация) |
| Трудных кейсов | **~1 000+** |
| Охват | Junior → Middle → Senior → Staff |
| Целевая точность | **90%+** |

---

## 🗺️ Карта разделов

### 📘 Раздел 1. [Java Core & Foundations](01-java-core.md)
**Фундамент Java, который Senior обязан знать досконально.**
Синтаксис · ООП (abstract/interface, records, sealed) · inner/лямбды/method refs · String/StringPool · исключения · Collections (HashMap internals, fail-fast) · Generics (PECS, type erasure) · Enum · I/O и NIO · java.time · equals/hashCode · Stream API · Optional · Reflection · Annotations · wrapper classes/autoboxing · Unicode/Charset.

*18 подразделов · 1500 строк · 206 URL · ~110 кейсов*

Ключевые книги: **Effective Java** (Bloch), Java Language Specification.

---

### 📗 Раздел 2. [Java Advanced — Concurrency, JVM, Memory](02-java-advanced.md)
**Глубокое понимание JVM, многопоточности, памяти.**
JVM architecture · Java Memory Model (happens-before, volatile) · Garbage Collection (G1/ZGC/Shenandoah, tuning) · Threads/virtual threads · synchronized & locks (ReentrantLock, StampedLock) · java.util.concurrent (ConcurrentHashMap) · Executors/ForkJoinPool/CompletableFuture · Atomic & CAS (ABA, VarHandle) · Synchronizers · Reactive Flow API · Reflection dynamic proxies · Method Handles/invokedynamic · Annotation processing · JNI/JNA/Panama FFM · JPMS modules · JCA/JCE/SSL/TLS · NIO.2 · Modern Java 9–21 · JVM диагностика (jstack/JFR/async-profiler/Arthas/MAT).

*19 подразделов · 1501 строка · 148 URL*

Ключевые книги: **Java Concurrency in Practice** (Goetz), **Java Performance** (Oaks), **Optimizing Java** (Clarke), JVM Specification.

---

### 📙 Раздел 3. [Build, Test & Engineering Practices](03-build-test-eng.md)
**Инженерные практики, без которых Senior не Senior.**
Maven (lifecycle/BOM/profiles/plugins) · Gradle (Kotlin DSL/version catalogs/configuration cache) · Ant + сравнение с Bazel · Git (branching strategies, rebase, bisect, hooks) · JUnit 5 · Mockito (mockStatic, mockConstruction) · AssertJ/Hamcrest · Testcontainers · Типы тестов (mutation PITest, property-based jqwik) · TDD/BDD (Cucumber) · Test doubles · JaCoCo · SonarQube/Checkstyle/PMD/SpotBugs/Error Prone · CI/CD (Jenkins/GitHub Actions/GitLab CI, blue-green/canary) · Nexus/Artifactory/Maven Central · Релиз-менеджмент (SemVer, JReleaser) · Code Review · ArchUnit/ADR/C4 · pre-commit hooks · security scanning (Snyk/Dependabot/Trivy/CodeQL).

*20 подразделов · 2567 строк · 208 URL · 140+ кейсов*

Ключевые книги: **JUnit in Action**, Building Modern Java Apps.

---

### 📕 Раздел 4. [Spring Ecosystem](04-spring-ecosystem.md)
**Spring — де-факто стандарт Java backend. Самый большой раздел.**
Spring Core/IoC/DI (bean lifecycle, circular deps, JDK proxy vs CGLIB) · Spring AOP · Configuration (@ConfigurationProperties) · Spring Boot (auto-config, actuator, GraalVM native) · Web MVC (ProblemDetail RFC 7807, SSE) · WebFlux (Reactor, WebClient) · Spring Data JPA (N+1, Specifications, QueryDSL) · Spring Data (Mongo/Redis/ES/JDBC) · Spring TX (propagation, self-invocation) · Spring Security (JWT, OAuth2, BCrypt/Argon2) · Actuator + Micrometer/OTel · Spring Cloud (Eureka, Resilience4j, Gateway, OpenFeign, Stream) · Spring Batch · Spring Cache · Spring Messaging (Kafka/Rabbit) · Spring Integration/Camel · Spring Boot Test (@MockitoBean, @ServiceConnection) · Spring Modulith · REST Docs/OpenAPI · Spring 5→6 / Boot 2.x→3.x migration (jakarta.*, OpenRewrite).

*20 подразделов · 2731 строка · 346 URL*

Ключевые книги: **Spring in Action** (Walls), **Pro Spring 6**, **Cloud Native Spring in Action**, **Spring Microservices in Action**.

---

### 📓 Раздел 5. [Persistence & Databases](05-persistence-db.md)
**Работа с данными: SQL, ORM, NoSQL, распределённые транзакции.**
JDBC · Connection pools (HikariCP) · Транзакции (ACID, isolation, MVCC) · SQL (JOIN, CTE, window functions, EXPLAIN) · PostgreSQL (индексы B-tree/GIN/GiST/BRIN, pg_stat_statements, partitioning) · MySQL (InnoDB, GTID) · Hibernate ORM (lifecycle, @Version, N+1, second-level cache) · JPA (Entity Graph, Criteria API) · Spring Data JPA · Flyway/Liquibase · MongoDB · Redis (Redlock, cache patterns, Lua) · Elasticsearch · Cassandra · ClickHouse · CAP/PACELC/BASE · Distributed transactions (2PC, Saga, Outbox, CDC Debezium) · CQRS/Event Sourcing · Моделирование данных (normal forms, SCD) · Performance tuning.

*20 подразделов · 1744 строки · 173 URL · 160 кейсов*

Ключевые книги: **Designing Data-Intensive Applications** (Kleppmann), **SQL Antipatterns** (Karwin), **High Performance MySQL**, **Hibernate in Action**.

---

### 📔 Раздел 6. [Distributed Systems, Messaging & Microservices](06-distributed-msg.md)
**Распределённые системы и асинхронный обмен сообщениями.**
Основы distributed systems (CAP/PACELC, consistency models) · REST API дизайн (HATEOAS, idempotency) · gRPC (Protocol Buffers, streaming) · GraphQL (DataLoader, federation) · Idempotency/exactly-once · **Apache Kafka** (partitions, ISR, exactly-once transactions, Kafka Streams, schema registry, KRaft) · RabbitMQ (exchanges, DLX, quorum queues) · Spring Kafka/AMQP · Микросервисные паттерны (Saga, API Gateway, BFF, Service Mesh, Strangler) · Resilience4j (Circuit Breaker, Bulkhead, Rate Limiter) · Service Discovery (Eureka/Consul) · API Gateway · Service Mesh (Istio/Linkerd) · Distributed Tracing (OpenTelemetry, Jaeger) · Distributed Locking · Caching distributed · Outbox/CDC (Debezium) · Event-driven architecture · WebSocket/SSE · Coordination (Zookeeper/etcd).

*20 подразделов · 1772 строки · 297 URL*

Ключевые книги: **Designing Data-Intensive Applications** (Kleppmann), **Microservices Patterns** (Richardson), **Kafka: The Definitive Guide**, **Building Microservices** (Newman), **Release It!** (Nygard), **Enterprise Integration Patterns** (Hohpe).

---

### 📒 Раздел 7. [DevOps, Containers & Observability](07-devops-observability.md)
**Эксплуатация Java-приложений: контейнеры, K8s, наблюдаемость.**
Linux/Shell · Docker (multi-stage, distroless, BuildKit) · Docker Compose · Registry (Harbor/ECR, Trivy, cosign, SBOM) · K8s основы (Pod/Deployment/Service/Probes/QoS) · K8s продолжение (StatefulSet/HPA/affinity/PDB) · K8s networking (Ingress, NetworkPolicy, CNI) · K8s storage (PV/PVC/CSI) · Helm/Kustomize · GitOps (ArgoCD/Flux) · Service Mesh (Istio/Linkerd) · Prometheus/PromQL/Micrometer · Grafana · ELK/Loki structured logging · OpenTelemetry traces (Jaeger/Tempo) · Профилирование в prod (JFR/async-profiler/Arthas/MAT) · SRE (SLI/SLO/error budget/postmortem) · IaC (Terraform/Pulumi/Ansible) · CI/CD на K8s (Argo Rollouts) · Chaos Engineering (Chaos Mesh).

*20 подразделов · 1911 строк · 180 URL*

Ключевые книги: **Site Reliability Engineering** (Google), **The DevOps Handbook**, **Kubernetes Up & Running**, **Observability Engineering** (Majors), **Terraform Up & Running** (Brikman).

---

### 📃 Раздел 8. [Architecture, Design Patterns, Algorithms & Senior Leadership](08-arch-patterns-leadership.md)
**Архитектура, паттерны, системный дизайн и soft skills сеньора.**
SOLID + DRY/KISS/YAGNI · **Паттерны GoF** (все 23 с реализацией на Java) · DI паттерны · Архитектурные паттерны (Layered/Hexagonal/Onion/Clean Architecture/CQRS) · **Domain-Driven Design** (Bounded Context, Aggregate, Repository) · System Design (capacity, sharding, CDN) · System Design кейсы (Twitter, URL shortener, chat, rate limiter) · Алгоритмы и структуры (LeetCode hard) · Concurrent data structures (Disruptor) · Clean Code & Refactoring (Fowler) · Code Review · Testing strategy (pyramid/trophy) · Документация (RFC, ADR, C4) · Senior soft skills (менторство, stakeholder management) · Agile/Lean/DevOps культуры · Performance engineering (Little's Law, queueing theory) · Security (OWASP Top 10, STRIDE, SAST/DAST) · Cost optimization/FinOps · Career path Junior→Staff (staff engineer archetypes).

*20 подразделов · 1259 строк · 101 URL*

Ключевые книги: **Design Patterns** (GoF), **Clean Architecture** (Martin), **Domain-Driven Design** (Evans), **Implementing DDD** (Vernon), **System Design Interview** (Xu), **Clean Code** (Martin), **Refactoring** (Fowler), **The Staff Engineer's Path** (Tornhill).

---

## 🎯 Рекомендуемый путь изучения

### Фаза 1 — Фундамент (3–6 месяцев)
1. **Раздел 1** (Java Core) — полностью, без пропусков. Цель: ~90% пунктов теории + выполнение 50% кейсов.
2. **Раздел 2** (Java Advanced) — фокус на Concurrency и JVM Memory. GC tuning обязательно.

**Маркер готовности:** можешь объяснить happens-before, рассказать устройство HashMap, настроить G1 GC, реализовать lock-free очередь.

---

### Фаза 2 — Инженерия (2–4 месяца)
3. **Раздел 3** (Build/Test/Eng) — Maven/Gradle, JUnit 5, Mockito, Testcontainers, CI/CD.

**Маркер готовности:** настроил multi-module проект с Testcontainers и JaCoCo, настроил CI/CD пайплайн.

---

### Фаза 3 — Enterprise (4–6 месяцев)
4. **Раздел 4** (Spring) — самый объёмный. Spring Core → Boot → Data JPA → Security → TX → Cloud.
5. **Раздел 5** (Persistence/DB) — параллельно с Spring. SQL, Hibernate, PostgreSQL.

**Маркер готовности:** спроектировал и реализовал production-grade REST API с JWT, тестами, миграциями, Docker.

---

### Фаза 4 — Распределённые системы (3–5 месяцев)
6. **Раздел 6** (Distributed/Messaging) — Kafka, микросервисы, Resilience4j, tracing.
7. **Раздел 7** (DevOps/Observability) — Docker, K8s, Prometheus/Grafana, OTel.

**Маркер готовность:** развернул микросервисы в K8s с full observability, реализовал Saga через Kafka.

---

### Фаза 5 — Архитектура и лидерство (постоянно)
8. **Раздел 8** (Arch/Patterns/Leadership) — системный дизайн, DDD, soft skills.

**Маркер готовности Senior:** ведёшь архитектурные обсуждения, проводишь code review, менторишь junior/middle, принимаешь решения об архитектуре.

---

## 📚 Топ-20 книг (must-read для Senior)

1. **Effective Java** — Joshua Bloch (3rd ed., 2018)
2. **Java Concurrency in Practice** — Brian Goetz
3. **Designing Data-Intensive Applications** — Martin Kleppmann
4. **Clean Code** — Robert Martin
5. **Clean Architecture** — Robert Martin
6. **Refactoring** — Martin Fowler (2nd ed.)
7. **Domain-Driven Design** — Eric Evans
8. **Implementing Domain-Driven Design** — Vaughn Vernon
9. **Spring in Action** — Craig Walls (6th ed., Spring 6)
10. **Microservices Patterns** — Chris Richardson
11. **Building Microservices** — Sam Newman (2nd ed.)
12. **Release It!** — Michael Nygard (2nd ed.)
13. **Kafka: The Definitive Guide** — Narkhede/Shapira/Palino (2nd ed.)
14. **Java Performance** — Scott Oaks
15. **Optimizing Java** — Clarke/Martinez/Browning
16. **Design Patterns** — Gang of Four
17. **System Design Interview** — Alex Xu (vol. 1 & 2)
18. **Site Reliability Engineering** — Google
19. **The DevOps Handbook** — Kim/Humble/Debois/Willis
20. **The Staff Engineer's Path** — Tanya Reilly

---

## 🛠️ Топ-15 ресурсов (онлайн)

1. **Baeldung** — https://www.baeldung.com (короткие практические статьи по Java/Spring)
2. **docs.spring.io** — официальная документация Spring
3. **docs.oracle.com/javase/tutorial** — Oracle Java Tutorials
4. **openjdk.org/jeps** — JEP index (все новые фичи Java)
5. **spring.io/guides** — пошаговые гайды Spring
6. **martinfowler.com** — статьи по архитектуре и рефакторингу
7. **infoq.com** — архитектурные интервью и презентации
8. **dzone.com** — статьи и рефкарты
9. **shipilev.net** — глубокие статьи Aleksey Shipilev по JVM/JMM
10. **jenkov.com** — подробные туториалы по Java NIO/Concurrency/IO
11. **habr.com/hub/java** — русскоязычные статьи
12. **kubernetes.io/docs** — документация Kubernetes
13. **prometheus.io/docs** — документация Prometheus
14. **opentelemetry.io/docs** — Observability с OTel
15. **leetcode.com** — алгоритмическая практика

---

## 💡 Как работать с этим руководством

1. **Не пытайся пройти всё сразу.** Это многолетний путь. Бери 1 раздел за фазу.
2. **Теория → Материалы → Практика.** Сначала прочитай теорию раздела, затем изучи 2–3 материала по теме, затем выполни хотя бы 30–50% кейсов.
3. **Кейсы — главное.** Теория без практики не работает. Каждый кейс — трудная инженерная задача (не «напиши Hello World»).
4. **Веди заметки.** После каждого кейса записывай что узнал, какие грабли собрал.
5. **Read code.** Читай исходники Spring, Hibernate, Kafka — это лучший способ понять как устроены production-системы.
6. **Contribute в open source.** Senior должен уметь читать и писать код в больших кодовых базах.

---

## 📁 Структура архива

```
java-senior-roadmap/
├── README.md                          ← этот файл (навигация)
├── 01-java-core.md                    ← Раздел 1
├── 02-java-advanced.md                ← Раздел 2
├── 03-build-test-eng.md               ← Раздел 3
├── 04-spring-ecosystem.md             ← Раздел 4
├── 05-persistence-db.md               ← Раздел 5
├── 06-distributed-msg.md              ← Раздел 6
├── 07-devops-observability.md         ← Раздел 7
└── 08-arch-patterns-leadership.md     ← Раздел 8
```

---

## ⚠️ Замечания о точности

- Все ссылки проверены через веб-поиск во время составления (июль 2025).
- Версии библиотек/фреймворков актуальны на момент составления (Spring Boot 3.4.x, Spring Framework 6.2.x, Java 21 LTS, Gradle 8.x, JUnit 5.x).
- Некоторые ссылки на конкретные статьи могут со временем меняться — если ссылка 404, ищи по названию статьи.
- Технические факты (поведение API, контракты JMM, устройство GC) сверены с официальной документацией Oracle/OpenJDK.

**Удачи на пути к Senior! 🎯**
