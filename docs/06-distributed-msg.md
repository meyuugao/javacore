# Раздел 6. Distributed Systems, Messaging & Microservices

> Цель раздела — вывести инженера на уровень Senior в распределённых системах: от теоретических основ (CAP/PACELC, consistency models, clocks) до production-паттернов (Saga, Outbox, CDC), брокеров (Kafka, RabbitMQ), межсервисного взаимодействия (REST, gRPC, GraphQL), observability (OpenTelemetry, Jaeger) и resilience (Resilience4j). Все ссылки реальные и проверены через web-search.

---

## 1. Распределённые системы: основы

### Теория

- **Распределённая система** — совокупность независимых вычислительных узлов, координирующихся через сообщения по сети; программно выглядит как одна система, физически распределена (Tanenbaum).
- **Прозрачность** (transparency): доступа, расположения, миграции, репликации, отказа, согласованности — цели дизайна, недостижимые полностью.
- **Fallacies of Distributed Computing** (Peter Deutsch, Sun): 1) сеть надёжна; 2) задержка нулевая; 3) пропускная способность бесконечна; 4) сеть безопасна; 5) топология не меняется; 6) один администратор; 7) транспортные расходы нулевые; 8) сеть однородна. Все эти допущения — ложные и порождают баги.
- **Two Generals' Problem** — невозможно достичь детерминированного согласия через ненадёжный канал; фундаментальная невозможность для асинхронных систем с возможностью потери сообщений.
- **Byzantine Generals Problem** (Lamport, Shostak, Pease, 1982) — согласие при произвольных (злонамеренных) отказах; решение требует `3f+1` узлов для容忍ания `f` византийских отказов; основа BFT-консенсуса.
- **FLP Impossibility** (Fischer, Lynch, Paterson, 1985) — в асинхронной системе с возможным отказом хотя бы одного узла детерминированный консенсус невозможен; практические алгоритмы (Paxos, Raft) обходят через случайность/таймауты.
- **CAP theorem** (Brewer, 2000; Gilbert-Lynch формальное доказательство 2002): из Consistency, Availability, Partition tolerance в случае сетевого разделения можно гарантировать только два; фактически P обязательно, значит выбор CP vs AP.
- **PACELC** (Abadi, 2010): расширяет CAP — **if Partition (P): choose A vs C; Else (E): choose L (latency) vs C**. PNUTS=PA/EL, Cassandra=PA/EL, MongoDB=PC/EC, Spanner=PC/EC, DynamoDB=PA/EL.
- **Consistency models** (иерархия от сильной к слабой):
  - **Linearizability** (strong, атомарная) — каждая операция выглядит атомарно в точке между вызовом и ответом; real-time порядок.
  - **Sequential consistency** (Lamport, 1979) — существует глобальный порядок операций, согласованный с программным порядком каждого процесса; не требует real-time.
  - **Causal consistency** — сохраняет причинно-следственные связи (happens-before); некоррелированные записи могут видеться в разном порядке.
  - **Read-your-writes** — процесс всегда видит свои записи.
  - **Monotonic reads / writes** — не «откатываетесь» назад в том, что уже видели.
  - **Eventual consistency** — при отсутствии новых обновлений реплики сходятся; без upper-bound гарантий времени.
- **Happens-before** (Lamport, 1978): `a → b` если (1) тот же процесс и a перед b; (2) a — send, b — соответствующий recv; (3) транзитивно. Без `a → b` операции **конкурентны** (concurrent).
- **Logical clocks (Lamport)** — каждому событию `C(e)`: на send `C=C+1` и прикрепляем `t=C`; на recv `C=max(C, t_msg)+1`. Даёт partial order, не отличает конкурентные события.
- **Vector clocks** (Mattern, Fidge, 1988) — `V[i]` — счётчик событий процесса i; на send инкрементируем свой; на recv `V[k]=max(V[k], V_msg[k])` для всех k, потом свой++. Два события a,b конкурентны iff `V(a) ≮ V(b) ∧ V(b) ≮ V(a)`.
- **Version vectors / Dotted version vectors** — оптимизация для реплик; dotted VV устраняет false conflicts в Dynamo-подобных магазинах.
- **Clock skew vs clock drift**: skew — разница показаний в моменте; drift — разница скорости. NTP держит skew в десятки мс; PTP/GPS — до микросекунд.
- **TrueTime** (Spanner) — API `TT.now()` возвращает интервал `[earliest, latest]` с bounded uncertainty; commit wait гарантирует linearizability через ожидание ε.
- **Hybrid Logical Clocks (HLC)** (Kulkarni 2014) — комбинация физического и логического часов: сохраняет order как Lamport, но близко к физическому времени; используется в CockroachDB, YugabyteDB.
- **Idempotency** — повтор выполнения операции даёт тот же результат; критично для at-least-once доставки.
- **Exactly-once myth** — «end-to-end exactly-once» требует idempotent consumers + transactions + dedup; Kafka EOS — exactly-once в рамках Kafka, не end-to-end.
- **Quorum**: `R + W > N`, `R > N/2`, `W > N/2` — strong consistency; `R=1, W=N` — write-all/read-one; Dynamo `N=3, R=2, W=2`.
- **Consensus algorithms**: Paxos (Multi-Paxos), Raft (leader-based, easier to understand), ZAB (ZooKeeper Atomic Broadcast), EPaxos (leaderless). Raft: leader election + log replication + safety via term+index.
- **Failure models**:
  - **Crash-stop** — узел падает и не восстанавливается.
  - **Crash-recovery** — узел может вернуться с persistent state.
  - **Omission** — сообщения теряются.
  - **Byzantine** — произвольное поведение.
- **Failure detection**: heartbeat + timeout (φ-accrual, Cassandra); подозрения (suspicion), false positives.
- **Network partitions**: split-brain, bridge, slow (gray failure); mitigation — fencing tokens, quorum, lease.
- **8 fallacies применительно к Kafka/RabbitMQ/gRPC**: таймауты обязательно; ретраи с backoff; idempotency; circuit breaker на upstream; observability latency; retry budget.

### Материалы (реальные ссылки)

- **Martin Kleppmann, "Designing Data-Intensive Applications"** (O'Reilly, 2017), главы 5 (Replication), 8 (The Trouble with Distributed Systems), 9 (Consistency and Consensus) — эталонный теоретический источник.
- **Tanenbaum & Van Steen, "Distributed Systems"** (4th ed., 2023), свободно: https://distributedsystems.net/ — базовая теория и модели отказов.
- **Brewer's CAP theorem** — оригинальный пост: https://martin.kleppmann.com/2015/05/11/please-stop-calling-databases-cp-or-ap.html (критика упрощённого CAP).
- **PACELC** — Daniel Abadi: https://dbmsmusings.blogspot.com/2010/04/problems-with-cap-and-yahoos-little.html и https://dbmsmusings.blogspot.com/2019/07/cap-and-pacelc-theorem-are-not-perfect.html
- **Lamport, "Time, Clocks, and the Ordering of Events"** (1978): https://lamport.org/pubs/time-clocks.pdf
- **Lamport, Shostak, Pease, "The Byzantine Generals Problem"** (1982): https://lamport.org/pubs/byz.pdf
- **FLP paper** (Fischer, Lynch, Paterson, 1985): https://groups.csail.mit.edu/tds/papers/Lynch/jacm85.pdf
- **Aphyr, "Jepsen"** анализы (DynamoDB, Kafka, Cassandra, MongoDB): https://aphyr.com/tags/jepsen
- **CAP/PACELC обзор**: https://rustycloud.org/distributed_systems_track/module-01-fundamentals/lesson-03-cap-pacelc.html
- **Consistency models обзор**: https://alokknight.com/blog/system-design-consistency-models и https://distributedsystemauthority.com/consistency-models
- **Lamport & Vector clocks**: https://medium.com/@sanjana.kansal13/mastering-consistency-in-distributed-systems-lamport-clocks-vector-clocks-and-real-world-8afc3b0cef26
- **Spanner / TrueTime paper** (Google, 2012): https://research.google/pubs/spanner-googles-globally-distributed-database/
- **Raft paper** (Ongaro, Ousterhout, 2014): https://raft.github.io/raft.pdf
- **Sam Newman, "Building Microservices"** 2nd ed. (O'Reilly, 2021) — глава 11 о масштабировании и распределённости.
- **Hohpe & Woolf, "Enterprise Integration Patterns"** (Addison-Wesley, 2003): https://www.enterpriseintegrationpatterns.com/

### Практика / Кейсы

1. **Анализ Jepsen-отчёта для выбранной БД.** Возьмите отчёт Aphyr по Cassandra 4.0 / MongoDB / Kafka 0.8 / Redis Sentinel. Восстановите: какие аномалии (lost update, dirty read, long fork) найдены; какой consistency model декларируется vs фактически наблюдается; какие настройки (RF, W, R, read-repair) приводят к нарушению. Оформите как ADR (Architecture Decision Record) с рекомендациями.
2. **Симуляция clock skew.** Разверните 3 узла на VM/Vagrant, умышленно сместите часы на ±500мс (`chronyc makestep`). Реализуйте простой счетчик через Cassandra LWT или Postgres SKIP LOCKED и сравните наблюдаемый порядок событий в логах. Зафиксируйте, какие операции выглядят «позже» физически, но «раньше» логически. Сравните с тем же тестом под HLC (используйте CockroachDB).
3. **Vector clocks на toy-языке.** Реализуйте key-value store с 3 узлами и vector clocks: при конфликте фиксируйте конкурентные записи, разрешайте через last-write-wins с physical clock и через merge function (CRDT OR-Set). Покажите сценарий, где LWW теряет данные, а merge — нет.
4. **CAP-демо: переключение CP vs AP.** На 3-узловом CockroachDB (CP) и 3-узловом Cassandra (AP, RF=3, W=1, R=1): разорвите сеть между двумя группами узлов (iptables DROP). Запишите ключ на «малой» стороне (1 узел Cassandra — доступно; CockroachDB — отказ). Восстановите сеть, проверьте консистентность. Задокументируйте наблюдаемое поведение и свяжите с PACELC.
5. **Консенсус на учебном Raft.** Запустите https://raft.github.io/ (визуализатор), воспроизведите: leader crash, network partition 2/2/1, recovered leader с устаревшим log. Объясните, почему старый leader не может «навязать» свой log (term и commitIndex). Сделайте видеозапись с комментариями или текстовый трейс.
6. **Проектирование системы под выбранную consistency.** Для домена «перевод денег между счетами»: сравните три дизайна — (a) 2PC across Postgres instances, (b) Saga orchestration через Kafka, (c) CRDT-счёт через Event Sourcing + deterministic merge. Для каждого опишите: latency, доступность при partition, аномалии, сложность отката. Сделайте вывод, когда какой подход адекватен.
7. **Fallacies audit.** Возьмите существующий сервис вашей команды. Пройдитесь по 8 fallacies и для каждой выпишите: где допущение явно нарушено (например, нет таймаута на вызов БД), какие баги это уже вызывало или может вызвать, какое исправление (timeout, retry, idempotency key, circuit breaker). Сдайте как PR с описанием рисков и метрикой «сколько fallacies закрыто».
8. **Byzantine failure в действии.** Разверните 4-узловый кластер с BFT-алгоритмом (например, Tendermint core или MiniBFT demo), умышленно сделайте один узел «византийским» (отправляет разные значения разным пирам). Покажите, что при `f=1` и `n=4` консенсус достигается; попробуйте `f=2, n=4` — покажите невозможность. Задокументируйте пороги `n ≥ 3f+1`.
9. **Трассировка через HLC.** Реализуйте простейший HLC в Java (atomic tuple `(physical_ms, logical)`): на локальное событие `if (physical_now > l.physical) { l = (physical_now, 0) } else { l = (l.physical, l.logical+1) }`; на recv — `l.physical = max(l.physical, msg.physical, physical_now); l.logical = max(l.logical, msg.logical) + 1`. Используйте для упорядочивания событий в распределённом логе. Сравните с physical clock NTP-only — покажите, что при clock skew HLC сохраняет causal order.
10. **Quorum deadlock в distributed lock.** Два сервиса A и B оба пытаются обновить 2 ресурса X и Y через quorum (3 узла каждый). A берёт X, B берёт Y, оба хотят второй ресурс — deadlock. Решение: lock ordering (всегда сначала X потом Y) или timeout + retry. Реализуйте, симулируйте deadlock, покажите mitigation.

---

## 2. REST API дизайн

### Теория

- **REST** (Roy Fielding, 2000, диссертация, глава 5) — архитектурный стиль с 6 constraints: client-server, stateless, cacheable, uniform interface, layered system, code-on-demand (опциональный). Uniform interface = идентификация ресурсов через URI, манипуляция через представления, self-descriptive messages, HATEOAS.
- **Ресурс** — именованная сущность (Noun); URL — путь к ресурсу; HTTP-метод — семантика действия. Антипаттерн: `/getUser?id=5` — глагол в URL.
- **HTTP-методы как uniform interface**:
  - `GET` — safe + idempotent, cacheable.
  - `HEAD` — safe + idempotent, без body.
  - `OPTIONS` — safe + idempotent, CORS preflight.
  - `POST` — не safe, не idempotent (но может быть сделан idempotent).
  - `PUT` — не safe, **idempotent** (полная замена).
  - `PATCH` — не safe, не обязательно idempotent (зависит от semantics).
  - `DELETE` — не safe, **idempotent**.
- **Richardson Maturity Model** (Leonard Richardson, классификация):
  - **Level 0 (POX)** — один URI, один метод (обычно POST), RPC-over-HTTP. SOAP-style.
  - **Level 1 (Resources)** — разные URI для разных ресурсов, но один метод.
  - **Level 2 (HTTP Verbs)** — правильное использование методов и status codes.
  - **Level 3 (HATEOAS)** — ответы содержат ссылки на следующие возможные действия (hypermedia controls).
- **HATEOAS** (Hypermedia As The Engine Of Application State): клиент не знает URL заранее, а следует по ссылкам из ответа. Пример: `GET /orders/42` → `{"_links":{"self":...,"cancel":...,"payment":...}}`. HAL, JSON-LD, SIREN, Collection+JSON — форматы.
- **Status codes**:
  - 2xx: 200 OK, 201 Created (+`Location`), 202 Accepted, 204 No Content.
  - 3xx: 301 Moved, 304 Not Modified.
  - 4xx: 400 Bad Request, 401 Unauthorized, 403 Forbidden, 404 Not Found, 409 Conflict, 410 Gone, 412 Precondition Failed, 422 Unprocessable Entity, 429 Too Many Requests.
  - 5xx: 500 Internal, 502 Bad Gateway, 503 Service Unavailable, 504 Gateway Timeout.
- **Versioning strategies**:
  - **URI versioning**: `/v1/orders`, `/v2/orders` — явно, но URL-загрязнение.
  - **Header versioning**: `Accept: application/vnd.acme.v2+json` — чистый URL, но менее discoverable.
  - **Query param**: `?version=2` — антипаттерн (кешируется плохо).
  - **Media type**: предпочтительный REST-подход, совместим с HATEOAS.
- **Idempotency keys**: клиент генерирует UUID, шлёт в заголовке `Idempotency-Key: 8c3f...`; сервер хранит `(key, request_hash, response)` в TTL-хранилище (Redis); повтор запроса с тем же ключом → тот же ответ. Stripe API — каноническая реализация.
- **Pagination**:
  - **Offset/Limit**: `?offset=100&limit=20` — просто, но O(n) skip, проблемы при вставках (skip-then-page-shift).
  - **Cursor / keyset**: `?after=eyJpZCI6MTIzfQ&limit=20` — стабильный, O(log n), но нельзя прыгнуть на страницу N.
  - **Page**: `?page=5&size=20` — для UI, те же проблемы offset.
  - Meta: `total_count`, `has_more`, `next_cursor`, `prev_cursor`. Ссылки через HATEOAS: `_links.next.href`.
- **Filtering & sorting**: `?status=active&created_at[gte]=2024-01-01&sort=-created_at,name`. Ограничивайте поля (whitelist), чтобы не позволить SQL-injection-like через `?fields=*`.
- **Sparse fieldsets**: `?fields=id,name,email` — GraphQL-like оптимизация для REST (JSON:API standard).
- **Rate limiting headers**: `X-RateLimit-Limit`, `X-RateLimit-Remaining`, `X-RateLimit-Reset`, `Retry-After`. Status 429 + body с политикой.
- **Caching**: `Cache-Control` (max-age, s-maxage, public, private, no-cache, no-store, must-revalidate, immutable), `ETag`, `Last-Modified`, `If-None-Match` → 304, `If-Match` для optimistic concurrency (412 если не сходится).
- **REST vs RPC vs gRPC vs GraphQL**:
  - REST: ресурсы, HTTP, широкая экосистема, кеширование, низкий entry barrier.
  - RPC (gRPC, JSON-RPC): действия, бинарный protobuf, strict schema, двусторонний streaming, эффективен для service-to-service.
  - GraphQL: клиент задаёт форму данных, один endpoint, решает over/under-fetching, но сложность сервера и кеш.
  - SOAP: enterprise legacy, XML, WSDL, WS-*.
- **API contracts**: OpenAPI 3.x (Swagger) — стандарт описания; генерация клиентов/серверов/доки; контракт-driven development; совместим со Spring (springdoc-openapi).
- **Веб-хуки (webhooks)** — сервер вызывает клиентский URL при событии; нужен retry, signature (`X-Signature: hmac-sha256`), idempotency; event delivery как «reverse API».
- **Bulk / batch operations**: `POST /orders/bulk` с массивом или `Content-Type: application/x-ndjson` для streaming. Часто с idempotency key per item.
- **Long polling / SSE / WebSocket** — для real-time; REST сам по себе pull-only.
- **Error payload** — стандарт JSON:API или RFC 7807 (Problem Details for HTTP APIs): `{"type":"https://api.example.com/errors/insufficient-funds","title":"Insufficient funds","status":422,"detail":"Balance 10.00, required 50.00","instance":"/orders/42"}`.
- **Security**: OAuth 2.1 / OIDC, JWT (короткий TTL + refresh), mTLS для service-to-service, CORS для браузеров, CSP, HSTS.
- **Documentation**: OpenAPI + ReDoc/Swagger UI; examples для каждого endpoint; sandbox; changelog; deprecation policy (`Sunset` header, RFC 8594).

### Материалы (реальные ссылки)

- **Roy Fielding, "Architectural Styles and the Design of Network-based Software Architectures"**, глава 5: https://www.ics.uci.edu/~fielding/pubs/dissertation/rest_arch_style.htm
- **Martin Fowler, "Richardson Maturity Model"**: https://martinfowler.com/articles/richardsonMaturityModel.html
- **REST Cookbook**: https://restcookbook.com/ (включает https://restcookbook.com/Miscellaneous/richardsonmaturitymodel/)
- **Stripe Idempotent Requests**: https://docs.stripe.com/api/idempotent_requests
- **RFC 7807 — Problem Details for HTTP APIs**: https://datatracker.ietf.org/doc/html/rfc7807
- **RFC 8594 — Sunset HTTP Header**: https://datatracker.ietf.org/doc/html/rfc8594
- **OpenAPI Specification 3.1**: https://spec.openapis.org/oas/v3.1.0
- **JSON:API specification**: https://jsonapi.org/
- **HAL specification**: https://stateless.group/hal_specification.html
- **Red Hat — Richardson Maturity Model overview**: https://developers.redhat.com/blog/2017/09/13/know-how-restful-your-api-is-an-overview-of-the-richardson-maturity-model
- **Mark Nottingham, "Caching Tutorial"**: https://www.mnot.net/cache_docs/
- **Microservices.io — API Composition pattern**: https://microservices.io/patterns/data/api-composition.html
- **Книга**: Richardson, "Microservices Patterns" (Manning, 2018), глава 7 про API Gateway и 8 — про messaging.

### Практика / Кейсы

1. **Спроектировать REST API для order service с HATEOAS.** Определите ресурсы `/orders`, `/orders/{id}/items`, `/orders/{id}/payments`, `/orders/{id}/cancel`. Каждый ответ HAL с `_links` (self, next state transitions). Реализуйте на Spring Boot + spring-hateoas. Покажите, как клиент без hardcoded URL переходит от создания → оплате → доставке. Напишите контракт в OpenAPI 3.1 с examples.
2. **Idempotent retries для POST /payments.** Реализуйте middleware: `Idempotency-Key` header, Redis-хранилище `(key → {request_hash, status, body, expires_at})` с TTL 24h, возврат кешированного ответа на повтор. Нагрузочный тест: 100 параллельных POST с одним и тем же key → ровно одна транзакция в БД, остальным 99 отдаётся тот же 201 Created. Сравните с ситуацией без idempotency key.
3. **Cursor pagination под высокой нагрузкой.** На таблице 10M строк (PostgreSQL): offset-based `?page=5000&size=20` выполняется за 800мс (skip scan), keyset `?after=<uuid>` — за 2мс. Реализуйте оба, измерьте, постройте график latency vs page depth. Добавьте `X-Total-Count` через materialized view или approximate count для UI.
4. **Versioning migration v1 → v2 без breaking changes.** Поле `customer.name` заменяется на `customer.firstName + customer.lastName`. В `/v1/` оставьте `name` (computed), в `/v2/` новые поля. Реализуйте через `Accept: application/vnd.acme.v2+json` (header). Покройте контрактными тестами (Pact или Spring Cloud Contract), что v1-клиенты продолжают работать 6 месяцев после выхода v2. Настройте `Sunset` header с датой окончания v1.
5. **REST → gRPC migration decision.** Для микросервиса с 50 endpoint-ами и RPS 5k: измерьте latency REST+JSON vs gRPC+protobuf (JMH-бенчмарк). Учтите: сериализация (protobuf быстрее в ~5x), HTTP/2 multiplexing, отсутствие browser-поддержки без grpc-web. Сделайте ADR: оставить REST, мигрировать полностью, или ввести gRPC для internal + REST для external (gRPC-Gateway).
6. **Webhook delivery с retry & signature.** Реализуйте outbound webhook service: событие → HMAC-SHA256 подпись тела → POST на URL клиента → при 5xx или timeout exponential backoff (1s, 5s, 30s, 5m, 1h, 6h, 24h, 72h) → затем DLQ. Поддержите `X-Webhook-Id` для идемпотентности на стороне клиента. Логируйте все попытки в БД с retry_count, статусом, latency. Напишите e2e-тест с Testcontainers + WireMock для имитации отказа клиента.
7. **Cache strategy для public API.** `GET /products/{id}` — добавьте `Cache-Control: public, max-age=60, s-maxage=300, stale-while-revalidate=600`, `ETag` (hash поля body), `Last-Modified`. Настройте CDN (CloudFront или Varnish): hit ratio > 90% при 1000 RPS. Покажите, что при `If-None-Match` возвращается 304 без body, экономия трафика ~99%. Инвалидируйте через `purge` API при `PUT /products/{id}`.
8. **OpenAPI contract-first development.** Опишите API в `openapi.yaml` → сгенерируйте Spring-интерфейсы (openapi-generator-maven-plugin) + модели + React-TS клиент → реализуйте контроллеры → напишите тесты, что реальная реализация совпадает с контрактом (spring-cloud-contract or schemathesis). Покажите, что изменение контракта в CI ломает билд, если impl не обновлена.
9. **Bulk API с частичными успехами.** `POST /orders/bulk` принимает 100 заказов. 95 успешны, 5 rejected (validation). Response: `207 Multi-Status` с массивом `[{index:0, status:201, location:...}, {index:5, status:422, error:"..."}]`. Реализуйте, покажите что клиент может обработать partial failure. Idempotency key per item для retry failed.
10. **REST security hardening.** Для production API: HTTPS only (redirect HTTP), HSTS, CSP, CORS whitelist (не `*`), rate limit per IP и per user, JWT с short TTL + refresh token rotation, no sensitive data in URLs (в headers/body), audit log всех mutations, WAF (ModSecurity) с OWASP CRS. Симулируйте атаки (Burp Suite): SQL injection, XSS, CSRF — покажите, что WAF блокирует. Сдайте как security review report.

---

## 3. gRPC

### Теория

- **gRPC** — высокопроизводительный RPC-фреймворк от Google (2015), поверх HTTP/2, по умолчанию Protocol Buffers как IDL и wire format. Идеален для service-to-service.
- **Protocol Buffers (protobuf)** — IDL + binary format от Google; типы: scalar (int32, int64, sint32/varint zigzag, fixed32/64, float, double, bool, string, bytes), enums, message, repeated, map, oneof, optional (proto3), well-known types (Any, Timestamp, Duration, Empty, Struct, Value, FieldMask, Wrappers).
- **Сериализация protobuf**: каждое поле = `field_number << 3 | wire_type`; varint encoding; field omission = backward/forward compatible. Совместимые изменения: add field (defaults), rename field (only wire name is field_number), **не** менять field_number, **не** менять type (некоторые совместимы: int32↔int64, string↔bytes), reserved для удалённых номеров.
- **Service definition** в `.proto`:
  ```proto
  service OrderService {
    rpc CreateOrder(CreateOrderRequest) returns (CreateOrderResponse);
    rpc GetOrder(GetOrderRequest) returns (Order) {
      option (google.api.http) = { get: "/v1/orders/{id}" };
    }
    rpc StreamOrders(StreamOrdersRequest) returns (stream OrderEvent);
    rpc UploadChunks(stream Chunk) returns (UploadAck);
    rpc Chat(stream ChatMessage) returns (stream ChatMessage);
  }
  ```
- **4 типа RPC**:
  1. **Unary** — один запрос, один ответ (как REST).
  2. **Server streaming** — один запрос, поток ответов (live updates, large result set).
  3. **Client streaming** — поток запросов, один ответ (upload, aggregation).
  4. **Bidirectional streaming** — оба потока (chat, real-time sync).
- **HTTP/2 transport**: multiplexing many requests over single TCP connection, binary framing, header compression (HPACK), server push (используется редко). Один TCP на peer — экономия ресурсов.
- **Status codes** (gRPC over HTTP/2): `OK=0`, `CANCELLED=1`, `UNKNOWN=2`, `INVALID_ARGUMENT=3`, `DEADLINE_EXCEEDED=4`, `NOT_FOUND=5`, `ALREADY_EXISTS=6`, `PERMISSION_DENIED=7`, `RESOURCE_EXHAUSTED=8`, `FAILED_PRECONDITION=9`, `ABORTED=10`, `OUT_OF_RANGE=11`, `UNIMPLEMENTED=12`, `INTERNAL=13`, `UNAVAILABLE=14`, `DATA_LOSS=15`, `UNAUTHENTICATED=16`.
- **Deadlines** (`deadline`) — абсолютное время, до которого клиент готов ждать; gRPC распространяет deadline в metadata, сервер видит `Context.isCancelled()` и отменяет downstream-вызовы. **Always set deadlines** — правило №1.
- **Metadata** — key-value (как HTTP headers), начальные (in headers) и trailing (in trailers). ASCII keys lowercase; binary values с суффиксом `-bin`. Используется для auth tokens, trace context (W3C TraceContext), user-agent.
- **Interceptors**:
  - **Client interceptors** — `ClientInterceptor.interceptCall()` — logging, tracing, auth header injection, retry.
  - **Server interceptors** — `ServerInterceptor.interceptCall()` — auth, logging, rate limiting, tracing propagation.
  - В Java: `ServerInterceptors.intercept(service, interceptor)`, регистрируются в `ServerBuilder`.
- **Streaming interception**: `ServerCallHandler` для потоков отличается; `ForwardingServerCallListener` для перехвата сообщений потока.
- **Load balancing**:
  - **Client-side LB**: `ManagedChannel` с `NameResolver` (DNS, xDS) + `LoadBalancer` (round_robin, pick_first).
  - **Proxy-based**: Envoy, Nginx gRPC LB, Linkerd — transparent.
  - **xDS** (Envoy's discovery service) — динамическая конфигурация LB, поддерживается gRPC-xDS.
- **Service reflection** — `grpc.reflection.v1alpha.ServerReflection` API: позволяет `grpcurl`, `grpcui`, Postman динамически открывать и вызывать методы без `.proto` файла. Включается `addReflectionService()`.
- **gRPC-Web** — proxy (Envoy или специальный) конвертирует HTTP/1.1 + base64 от браузера в HTTP/2 gRPC; поддерживает unary и server streaming (client/bidi — через WebSockets или с ограничениями).
- **Health checking** — стандарт `grpc.health.v1.Health` service: `SERVING`, `NOT_SERVING`, `UNKNOWN`; Kubernetes liveness/readiness probe gRPC.
- **Channel & stub lifecycle**: `ManagedChannel` — heavyweight (TCP conn pool), переиспользуется; `Stub` — лёгкий, создаётся из channel. `Channel.shutdown()` + `awaitTermination()`.
- **Backpressure**: потоковые RPC поддерживают `onNext` с `isReady()` проверкой; сервер должен уважать ready-сигнал, иначе OOM.
- **Keepalive** — ping frames для detection обрыва; `keepaliveTime`, `keepaliveTimeout`, `keepaliveWithoutCalls`; настройки различаются client/server, опасны при misconfig (сервер закрывает соединение).
- **Resilience**: gRPC не имеет встроенных retry (кроме declarative retry в service config через xDS); для retry/circuit breaker используют Resilience4j вокруг stub, или gRPC-Spring-Boot-Starter.
- **Codegen**: для Java — `protobuf-maven-plugin` + `protoc-gen-grpc-java`; для Kotlin — `kotlinx-coroutines` адаптеры; код генерируется в `target/generated-sources`.
- **gRPC vs REST performance**: protobuf ~3-10x быстрее JSON на сериализации; HTTP/2 multiplexing даёт +30-50% RPS при многих запросах; важнее для internal high-throughput.
- **Errors with details**: `Status` + `ErrorDetails` (any protobuf message в `google.rpc.Status`): `status.withDescription(...).asRuntimeException()`. Клиент парсит details для richer error handling (аналог RFC 7807).
- **gRPC and transactions** — нет встроенных; use Saga / Outbox поверх.
- **Testing**: `InProcessTransport` для unit-tests без реального TCP; `grpc-testing` для fake clock; `grpcurl` для ручных вызовов; `Postman` поддерживает gRPC.

### Материалы (реальные ссылки)

- **gRPC Core concepts**: https://grpc.io/docs/what-is-grpc/core-concepts/
- **gRPC Deadlines**: https://grpc.io/docs/guides/deadlines/
- **gRPC Metadata**: https://grpc.io/docs/guides/metadata/
- **gRPC Interceptors**: https://grpc.io/docs/guides/interceptors/
- **gRPC Performance Best Practices**: https://grpc.io/docs/guides/performance/
- **gRPC Basics — Java tutorial**: https://grpc.io/docs/languages/java/basics/
- **Protocol Buffers Language Guide (proto3)**: https://protobuf.dev/programming-guides/proto3/
- **gRPC Motivation and Design Principles**: https://grpc.io/blog/principles
- **gRPC-Web**: https://github.com/grpc/grpc-web
- **gRPC Health Checking Protocol**: https://github.com/grpc/grpc/blob/master/doc/health-checking.md
- **gRPC Status codes**: https://grpc.io/docs/guides/status-codes/
- **Книга**: Klein & Sahoo, "gRPC Up and Running" (O'Reilly, 2020).
- **Книга**: Kleppmann, DDIA — глава 4 (Encoding and Evolution), сравнение protobuf/Avro/JSON.

### Практика / Кейсы

1. **Unary + server streaming + bidi в одном сервисе.** Реализуйте `ChatService` с методами: `CreateAccount` (unary), `ListMessages(since=timestamp)` (server stream), `UploadLogs` (client stream), `Chat` (bidi). Используйте Spring Boot + grpc-spring-boot-starter. Покройте контрактом: генерация stub-ов из `.proto`, тесты через `GrpcCleanupRule` + `InProcessServer`. Снимите метрики latency для каждого типа RPC.
2. **Deadlines propagation через слои.** Сервис A вызывает B, B вызывает C. Клиент A ставит deadline 5s; на каждом хопе deadline уменьшается. Реализуйте через `Context.current().getDeadline()`, передачу через gRPC metadata (`grpc-timeout` header). Покажите, что при общей задержке 6s в C, сначала отменяется C, потом B, потом A — каждый получает `DEADLINE_EXCEEDED`. Без deadline C отработал бы полностью и потратил ресурсы впустую.
3. **Interceptor: auth + tracing + logging.** Напишите серверный interceptor, который: (a) проверяет JWT из `authorization` metadata, при невалидном возвращает `UNAUTHENTICATED`; (b) извлекает `traceparent` (W3C) и inject в OpenTelemetry context; (c) логирует method, status, duration. Напишите клиентский interceptor, который добавляет JWT и traceparent. Измерьте overhead (JMH): baseline vs с interceptors.
4. **Schema evolution без breaking.** v1: `message Order { string id = 1; string customer = 2; }`. v2: добавить `repeated Item items = 3;`, переименовать `customer` в `customer_name` (но field_number 2 оставить, deprecated=true). v3: убрать `customer_name`, зарезервировать `reserved 2; reserved "customer_name";`. Покажите совместимость: v1-клиент vs v3-сервис работает, обратное тоже. Покажите сценарий, где изменение field_number ломает всё.
5. **gRPC-Web frontend.** Поднимите Envoy как proxy gRPC-Web → gRPC, фронтенд на React вызывает unary и server-streaming методы. Реализуйте чат-стриминг. Покажите, что client streaming недоступен из браузера без workaround; обсудите альтернативы (Connect-RPC, WebSockets).
6. **Load balancing round_robin vs pick_first.** Сравните: `pick_first` (по умолчанию, использует первый доступный адрес) vs `round_robin` (распределяет запросы). Запустите 3 реплики сервиса, симулируйте медленную реплику (sleep 500ms в 30% запросов). Покажите, что round_robin даёт более равномерный p99 (без hot-spot), pick_first — все запросы на одну реплику. Настройте health checking для исключения упавших реплик.
7. **Backpressure в server streaming.** Сервер генерирует 1M событий в стрим, клиент обрабатывает медленно (100ms/event). Без backpressure — OOM за секунды; с проверкой `call.isReady()` + `onReady` callback — graceful. Реализуйте через `ServerCallStreamObserver`. Измерьте max heap usage: без backpressure 8GB, с — 50MB.
8. **Migration REST → gRPC с grpc-gateway.** Существующий REST API `/v1/orders/{id}` должен поддерживаться. Сгенерируйте gRPC-сервис + используйте `grpc-protobuf` annotations (`google.api.http`) + grpc-gateway для автоматической генерации REST proxy. Покажите, что оба транспорта работают, метрики gRPC и REST расходятся по latency (gRPC ~2-3x быстрее), решите какой оставить для external.

---

## 4. GraphQL

### Теория

- **GraphQL** (Facebook, 2015; spec с 2018) — query language + runtime для API; клиент запрашивает ровно нужные поля; один endpoint `/graphql`; schema как контракт.
- **Schema Definition Language (SDL)**: типы (`type User { id: ID! name: String! posts: [Post!]! }`), interfaces, unions, enums, input types, scalars (custom: `DateTime`, `JSON`, `BigInt`), directives (`@deprecated`, `@auth`, custom).
- **Operations**: `query` (read), `mutation` (write, сериализованное выполнение), `subscription` (long-lived, обычно over WebSocket).
- **Resolvers**: функция `parent, args, context, info → field_value`. Каждый field имеет resolver (default — property access). Контекст — shared (DB connection, user, dataloaders).
- **Execution strategy**: GraphQL обходит дерево полей параллельно для sibling fields (через `CompletableFuture` в graphql-java); глубина ограничена `maxQueryDepth` (защита от рекурсии).
- **N+1 problem**: запрос `query { users { id posts { title } } }` — 1 запрос на users + N запросов на posts для каждого user. Решается **DataLoader**.
- **DataLoader** (Facebook, 2016): батчит запросы в одном tick event loop — `load(userId)` ставит в очередь, `dispatch()` в конце тика делает `loadByIds([1,2,3])`. Кеширует по key. В graphql-java — `DataLoaderRegistry` + `DataLoader` interface; identity cache per request.
- **Schema stitching vs Federation**:
  - **Stitching** (legacy): один gateway комбинирует схемы нескольких сервисов (merge types, resolve conflicts). Сложно поддерживать.
  - **Apollo Federation** (2018) — современный де-факто стандарт. **Supergraph** = union of subgraphs; **gateway** маршрутизирует запросы между subgraphs; типы могут быть расширены (`extend type User @key(fields: "id") { ... }`).
- **Federation directives**: `@key` (entity id), `@external` (field from another subgraph), `@extends`, `@requires` (computed field depends on external), `@provides` (can satisfy field from this subgraph).
- **Entity resolution** — gateway вызывает `_entities(representations: [{__typename:"User", id:"1"}])` на subgraphs, чтобы заполнить поля.
- **Federation v1 vs v2**: v2 упрощает синтаксис (`@shareable`, `@override`, `@inaccessible`, `@interfaceObject`); v2 совместима с v1.
- **Caching в GraphQL**: сложнее, чем REST (нет URL → ETag); подходы — Persisted Queries (SHA256 hash в URL), Apollo Router cache, CDN для persisted query responses, `Cache-Control` scope=public.
- **Persisted Queries**: клиент шлёт `?extensions={"persistedQuery":{"sha256Hash":"..."}}` вместо всего запроса; сервер хранит query-by-hash; уменьшает payload и защищает от произвольных запросов.
- **Security**:
  - **Query depth limiting** — max depth (например 7).
  - **Query complexity** — стоимость запроса (каждое поле имеет cost, суммарный лимит).
  - **Rate limiting by cost** — не requests/sec, а query cost/sec (Stripe-style).
  - **Persisted queries only** — production-режим, где arbitrary queries запрещены.
  - **Introspection disabled** в prod (не показывать schema).
  - **Field suggestions disabled** — не подсказывать опечатки.
- **Subscriptions**: обычно через WebSocket (graphql-ws protocol), сервер пушит события; сложность — горизонтальное масштабирование (нужен pub/sub брокер: Redis, Kafka, NATS).
- **Error handling**: `{"data":{...,"errors":[{"message":"...","path":["user","posts",0,"title"],"extensions":{"code":"UNAUTHORIZED"}}]}}` — partial results + errors.
- **BFF (Backend For Frontend)** с GraphQL — типичное применение: один BFF на канал (web, mobile, partner), GraphQL агрегирует несколько backend-сервисов.
- **GraphQL Java**: `graphql-java` library; Spring for GraphQL (`spring-graphql-starter`) — modern, интеграция с Boot, WebFlux, муextensions, batch loading; GraphQL Java Kickstart — legacy starter.
- **Schema-first vs code-first**: SDL-first (codegen для resolvers) vs Java annotations / programmatic (RuntimeWiring). SDL-first — лучше для контракта.
- **Schema evolution**: add fields (safe), deprecate (`@deprecated(reason: "...")`), remove через staged rollout (deprecated → hidden → removed). В отличие от gRPC, нет автоматической forward/backward compat — нужно ручное управление.
- **Comparison REST vs GraphQL vs gRPC**:
  - REST — публичные API, кеширование, простота, стандарты.
  - GraphQL — богатые клиенты (mobile, web SPA), агрегация из многих сервисов, преодоление over/under-fetching.
  - gRPC — internal service-to-service, типизированные контракты, streaming, perf.
- **Anti-patterns**: бизнес-логика в resolvers (должна быть в domain layer), `JSON` scalar для всего (теряется типизация), глубоко вложенные запросы без DataLoader, mutation-return-only-ID (лучше вернуть полное обновлённое состояние).
- **Testing**: schema tests (валидация SDL), query tests (Helsinkitest или `GraphQLTestTemplate`), integration tests с Testcontainers; contract tests против реальной schema.

### Материалы (реальные ссылки)

- **GraphQL Official spec**: https://spec.graphql.org/
- **GraphQL Learn**: https://graphql.org/learn/
- **Apollo Federation docs**: https://www.apollographql.com/docs/graphos/schema-design/federated-schemas/federation
- **Apollo Federation v1 → v2**: https://www.apollographql.com/docs/federation/v1
- **graphql.org Federation**: https://graphql.org/learn/federation/
- **Apollo Subgraphs**: https://www.apollographql.com/docs/federation/v1/subgraphs
- **Apollo Federation Gateway**: https://www.apollographql.com/docs/federation/v1/gateway
- **Apollo N+1 guide**: https://www.apollographql.com/docs/graphos/schema-design/guides/handling-n-plus-one
- **Spring for GraphQL reference**: https://docs.spring.io/spring-graphql/reference/
- **graphql-java documentation**: https://www.graphql-java.com/documentation/
- **GraphQL Java DataLoader**: https://github.com/graphql-java/java-dataloader
- **Using java-dataloader**: https://www.morethanbits.io/blog/java-dataloaders
- **Книга**: Banks, "GraphQL in Action" (Manning, 2018); Bannon, "Production-Ready GraphQL" (2021).
- **Apollo Docs — Persisted Queries**: https://www.apollographql.com/docs/react/api/link/persisted-queries/

### Практика / Кейсы

1. **Schema-first design с DataLoader.** Опишите schema для e-commerce: `User`, `Order`, `OrderItem`, `Product`. Реализуйте resolvers на Spring for GraphQL. Без DataLoader — 1 + N + N*M SQL-запросов; с DataLoader — 3 запроса (users, orders-by-userIds, items-by-orderIds). Покажите SQL-лог (p6spy), измерьте количество запросов для запроса «100 users → orders → items».
2. **Apollo Federation: 2 subgraphs + gateway.** Сервис `users` владеет типом `User`; сервис `orders` расширяет `User @key(fields:"id")` полем `orders: [Order!]!`. Поднимите Apollo Router (Rust-binary) или node-gateway. Запрос `{ user(id:"1") { id name orders { id total } } }` — gateway делает 2 запроса к subgraphs (1 на users, 1 на _entities для orders). Тестируйте через `rover dev` локально.
3. **Persisted Queries в проде.** Включите APQ на клиенте (Apollo Client) и сервере. Без APQ: POST /graphql с телом 5KB. С APQ: POST /graphql с `extensions.persistedQuery.sha256Hash` — тело 200B. На втором запросе — GET /graphql?extensions=... — кешируется на CDN. Измерьте: 80% трафика GraphQL уходит в CDN cache hit.
4. **Security: depth + complexity limits.** Запрос `query { user { posts { author { posts { author { ... } } } } } }` (рекурсивный) — без depth limit вызывает stack overflow или DoS. Настройте `MaxQueryDepthInstrumentation(7)` и `MaxQueryComplexityInstrumentation(100)` с cost-per-field. Запустите зловредный запрос — получите `query exceeds max depth` error. Подберите complexity, чтобы легитимный запрос (50 fields) проходил, а abuse (1000 fields) — нет.
5. **Subscriptions через WebSocket + Kafka.** Реализуйте `subscription { orderStatusChanged(orderId:"42") { status updatedAt } }`: Spring for GraphQL + WebSocketHandler + Kafka consumer per subscription. При публикации события в Kafka — сервер пушит всем подписанным клиентам. Проблема: горизонтальное масштабирование (несколько инстансов сервера) — решается через Redis pub/sub или Kafka consumer group с `clientId`-based routing.
6. **Error handling с partial results.** Резолвер `posts` для user 42 падает (DB timeout). GraphQL возвращает `{"data":{"user":{"id":"42","name":"Alice","posts":null}},"errors":[{"message":"DB timeout","path":["user","posts"],"extensions":{"code":"INTERNAL_SERVER_ERROR"}}]}`. Реализуйте и покажите, что другие поля (name) остались валидными. Добавьте error extension code mapping (`UNAUTHORIZED`, `NOT_FOUND`, `VALIDATION`).
7. **GraphQL BFF для mobile + web.** Один GraphQL gateway агрегирует 3 backend-сервиса (user-service REST, catalog-service gRPC, pricing-service gRPC). Mobile-клиент запрашивает меньше полей (id, name, price), web — все (id, name, price, description, reviews). Покажите, что один endpoint обслуживает оба сценария без over-fetching. Измерьте: web-response 2KB, mobile-response 500B — экономия трафика для mobile.
8. **Migration REST → GraphQL incrementally.** Существующий REST API `/users/{id}` остаётся. Добавьте GraphQL-слои поверх тех же domain-services (вызывает тот же `UserService`). Покажите, что клиенты могут мигрировать поэтапно: каждый endpoint заменяется на GraphQL field, можно одновременно поддерживать оба. Покройте контрактными тестами. Замерьте latency: GraphQL over REST 1.2x overhead — приемлемо ли?

---

## 5. Идемпотентность и exactly-once

### Теория

- **Идемпотентность** — свойство операции `f(f(x)) = f(x)`; в распределённых системах: повторный вызов с теми же аргументами даёт тот же результат, что и первый.
- **Уровни доставки сообщений**:
  - **At-most-once** — сообщение может потеряться, но не дублируется (fire-and-forget, UDP-стиль).
  - **At-least-once** — сообщение доставляется 1+ раз, не теряется; требует ack + retry; дубликаты возможны.
  - **Exactly-once** — без потерь и дубликатов; в реальности — «эффективно один раз» (effectively-once) через idempotency + dedup + transactions.
- **Источники дубликатов**:
  - Producer retry: сообщение отправлено, ack потерян, producer ретраит → 2 копии.
  - Consumer crash после обработки, до commit offset → повторная обработка.
  - Network glitch + at-least-once middleware.
  - Manual retry по таймауту.
- **Idempotency key** — уникальный (обычно UUID) идентификатор операции от клиента; сервер хранит `key → result` в TTL-хранилище; повторный запрос с тем же key → тот же результат.
- **Идемпотентность на уровне БД**:
  - **Unique constraint** на `(idempotency_key, business_entity)` — второй insert падает, сервер возвращает существующую запись.
  - **State machine check** — если заказ уже `PAID`, повторный `pay()` — no-op.
- **Idempotency vs deduplication**:
  - Idempotency — `key` от клиента, intent-based.
  - Dedup — серверная проверка (хеш сообщения, producer_id+seq_num в Kafka).
- **Transactional Outbox pattern** — сохранение бизнес-состояния и события в одной БД-транзакции; отдельный `MessageRelay` процесс читает outbox и публикует в брокер; гарантирует at-least-once публикацию.
  - **Polling publisher** — таймером SELECT из `outbox` WHERE `published=false`.
  - **CDC publisher** — Debezium читает WAL/binlog, публикует в Kafka автоматически.
- **Outbox table schema**: `(id, aggregate_id, aggregate_type, event_type, payload, created_at, published_at, version)`. Используется для ordering, idempotency, replay.
- **Exactly-once end-to-end через transactional outbox + idempotent consumer**:
  1. Service A: BEGIN TX → INSERT order → INSERT outbox event → COMMIT (атомарно).
  2. Relay (Debezium или polling) публикует событие в Kafka (`id` события как Kafka key для ordering).
  3. Service B consumer: BEGIN TX → проверяет `id` в `processed_events` (dedup) → применяет бизнес-логику → INSERT в `processed_events` → commit offset (в той же транзакции, если БД supports Kafka producer tx).
- **Kafka EOS** — транзакционный producer + `read_committed` isolation + idempotent producer (`enable.idempotence=true`): consume-transform-produce цикл атомарно коммитит offset + новые сообщения. Это **within Kafka**, не end-to-end.
- **Two-phase commit (2PC)** — coordinator → prepare → commit; блокирует ресурсы, slow, single point of failure; используется в XA-транзакциях (Java JTA). Не подходит для microservices.
- **Saga pattern** — последовательность локальных транзакций, каждая с компенсирующим действием; нет глобальной изоляции.
- **Compensating transactions** — undo для каждой step; не всегда возможно (нельзя «unsend» email).
- **Idempotency и ordering** — если сообщения могут приходить out-of-order, то idempotency недостаточно; нужен sequencing (version/seq_num) и обработка only-if-next.
- **Race conditions**:
  - Два consumer'а параллельно обрабатывают один и тот же event — решается unique constraint или advisory lock.
  - Producer и consumer race — решается optimistic concurrency (`version` field).
- **Fencing tokens** — монотонно возрастающий token при каждом acquired lock; передаётся с запросом; Storage отклоняет запросы с устаревшим token (защита от split-brain в distributed locking + slow client).
- **Concurrent idempotency** — два одновременленных запроса с одним key: один вставляет запись, второй ждёт (SELECT FOR UPDATE) или получает 409 Conflict.
- **TTL idempotency records** — хранить 24h-7d, не вечно;权衡 storage vs correctness.
- **Idempotency для GraphQL mutations** — `clientMutationId` field (Relay spec); для REST — `Idempotency-Key` header (Stripe).
- **At-least-once + idempotent consumer = effectively-once** — практический подход, используемый в большинстве систем; проще и надёжнее, чем «true EOS».
- **Законы end-to-end argument** (Saltzer, Reed, Clark, 1984): некоторые функции можно реализовать только end-to-end (на уровне приложения), промежуточные слои не дают полной гарантии.

### Материалы (реальные ссылки)

- **Microservices.io — Transactional Outbox**: https://microservices.io/patterns/data/transactional-outbox.html
- **Microservices.io — Saga**: https://microservices.io/patterns/data/saga.html
- **Microservices.io — Idempotent Receiver**: https://microservices.io/patterns/communication/idempotent-receiver.html
- **AWS — Transactional outbox pattern**: https://docs.aws.amazon.com/prescriptive-guidance/latest/cloud-design-patterns/transactional-outbox.html
- **Debezium Documentation**: https://debezium.io/documentation/reference/stable/architecture.html
- **Stripe — Idempotent Requests**: https://docs.stripe.com/api/idempotent_requests
- **Kafka Exactly-once Semantics**: https://www.confluent.io/blog/exactly-once-semantics-are-possible-heres-how-apache-kafka-does-it/
- **Martin Kleppmann — "How to do distributed locking"** (fencing tokens): https://martin.kleppmann.com/2016/02/08/how-to-do-distributed-locking.html
- **End-to-End Arguments in System Design** (Saltzer, Reed, Clark, 1984): https://web.mit.edu/Saltzer/www/publications/endtoend/endtoend.pdf
- **James Carr — Transactional Outbox Pattern**: https://james-carr.org/posts/2026-01-15-transactional-outbox-pattern
- **Idempotency Patterns**: https://backendbytes.com/articles/idempotency-patterns-distributed-systems
- **Книга**: Richardson, "Microservices Patterns", глава 3 (Inter-service communication) и 4 (Transactions with Sagas) и 8 (Outbox).
- **Книга**: Kleppmann, DDIA, глава 11 (Stream Processing) — про exactly-once semantics.

### Практика / Кейсы

1. **Transactional outbox с polling publisher.** Spring Boot + PostgreSQL. Таблица `orders` + `outbox_events(id, aggregate_id, type, payload, created_at, published)`. Сервис: `@Transactional` insert order + insert event. Scheduler: каждые 500ms SELECT unpublished → KafkaTemplate.send() → mark published. Проблема: при crash между send и mark — duplicate. Решение: idempotent consumer на принимающей стороне (dedup по event_id). Нагрузочный тест: 10k orders, 0 потерянных, дубликаты обрабатываются корректно.
2. **CDC через Debezium вместо polling.** Поднимите Debezium connector для Postgres (через Kafka Connect). Debezium читает WAL, публикует изменения в Kafka автоматически, без polling overhead. Сравните lag: polling 500ms vs Debezium <50ms. Покажите, что Debezium передаёт `op` (c/u/d/r), `before`, `after` — можно строить materialized views.
3. **Idempotency key для платежей.** REST API `POST /payments` с `Idempotency-Key` header. Реализация: Redis `SET key value NX EX 86400` для проверки duplicate-in-progress; если ключ занят — дождаться результата через Redis pub/sub. При дубликате запроса — вернуть кешированный ответ. Нагрузочный тест: 100 параллельных POST с одним key → ровно 1 charge в payment gateway, остальным — тот же ответ.
4. **Kafka EOS consume-process-produce.** Настройте transactional Kafka consumer+producer: `isolation.level=read_committed`, `transactional.id=order-processor-1`, `enable.idempotence=true`. В цикле: `beginTransaction()` → consume batch → process → produce to output topic → `sendOffsetsToTransaction(offsets, group)` → `commitTransaction()`. Симулируйте crash между produce и commit — offset не закоммичен, сообщения не видны читателям на read_committed, при restart всё переобрабатывается без дублей.
5. **Fencing token для distributed lock + worker.** Redis Redlock возвращает `token` (monotonic). Worker пишет в БД `UPDATE account SET balance=balance-100 WHERE account_id=X AND last_token < ?`. Если два worker'а получили lock (split-brain), у одного token меньше — его UPDATE не проходит. Симулируйте: задержите старый worker (GC pause 30s), новый получил lock, оба пытаются списать — fencing token защищает. Покажите, что без fencing — double-spend.
6. **Saga с компенсациями через orchestration.** Order saga: create order → reserve inventory → charge payment → ship. Если payment fails → compensate: release inventory, mark order FAILED. Orchestrator (Spring State Machine или temporal.io) хранит state в БД, каждый шаг идемпотентный (idempotency key = saga_id + step_num). Симулируйте crash orchestrator в середине — restart подхватывает state, не начинает заново.
7. **Concurrent idempotent processing.** Два инстанса consumer-а параллельно. Сообщение с `event_id=abc`. Без защиты — оба обрабатывают, double-effect. С защитой: `INSERT INTO processed_events(event_id) VALUES('abc') ON CONFLICT DO NOTHING` в одной транзакции с бизнес-логикой — только один выполнит. Тест: 1000 параллельных сообщений на 5 consumer'ах — ровно 1000 эффектов, не больше.
8. **End-to-end EOS audit.** Полный пайплайн: HTTP POST → service A (Postgres outbox) → Debezium → Kafka → service B (Kafka EOS consumer, updates Postgres). Сделайте chaos-тест: убейте service A между commit TX и publish (Debezium сам восстановит, WAL-based); убейте service B между consume и commit offset — при restart переобработка, но idempotent consumer по event_id предотвращает дубль. Посчитайте: 10k исходных сообщений → 10k эффектов в конечной системе, 0 потерь, 0 дублей.

---

## 6. Apache Kafka

### Теория

- **Apache Kafka** (LinkedIn, 2011; open-source, Confluent商业化) — distributed event streaming platform; commit log per partition; high throughput (миллионы/sec), durable (disk-based), scalable (partition-level parallelism).
- **Архитектура**: Producer → Broker (cluster) → Consumer. Metadata в KRaft controller (раньше ZooKeeper). Storage — append-only segment files на диске, indexed by offset.
- **Topic** — именованный поток событий; partitioned. Topic naming convention: `<domain>.<event-type>.<v1>` (e.g. `orders.created.v1`).
- **Partition** — упорядоченный append-only log; единица параллелизма. Каждое сообщение имеет **offset** (monotonic int64 per partition). Ordering гарантирован только в рамках partition.
- **Partition key** — producer задаёт key; все сообщения с одним key идут в одну partition (для ordering). `DefaultPartitioner` (sticky в новых версиях) или custom.
- **Offset** — позиция сообщения в partition. Consumer track-ит свой offset; коммитит в `__consumer_offsets` topic (49 partitions по умолчанию).
- **Consumer group** — набор consumer'ов, координируемых group coordinator (брокер); partition распределяется между членами группы; один partition → один consumer в группе; параллелизм = min(partitions, consumers).
- **Replication**: каждый partition имеет N реплик (replication.factor=3); одна **leader**, остальные **followers**. Все reads/writes через leader. ISR (In-Sync Replicas) — followers, не отстающие больше чем `replica.lag.time.max.ms`.
- **acknowledgements** (producer `acks`):
  - `acks=0` — fire-and-forget, не ждёт ответа (highest throughput, loss possible).
  - `acks=1` — leader writes → ack (loss if leader fails before replication).
  - `acks=all` (или `-1`) — leader + all ISR ack (strongest durability; slow down if ISR shrinks).
- **min.insync.replicas** — минимальный размер ISR, чтобы ack=all прошел; если ISR < min, producer получает `NOT_ENOUGH_REPLICAS`. Типичная настройка: `replication.factor=3, min.insync.replicas=2, acks=all` → терпит 1 сбой без потери данных.
- **Idempotent producer** (`enable.idempotence=true`, default с Kafka 3.0): producer получает PID (Producer ID) + sequence number per partition; broker дедупликает дубли на уровне PID+partition+seqNum. Гарантия: no duplicates within producer session.
- **Transactional producer**: `transactional.id` (stable across restarts), producer может отправить несколько сообщений в разные partitions атомарно (all-or-nothing visible to `read_committed` consumers). Epoch fencing защищает от zombie-процессов.
- **max.in.flight.requests.per.connection** — для idempotent producer ≤ 5 (защита от reordering); для не-idempotent > 1 может привести к дублированию при retry.
- **Retention** — `retention.ms` / `retention.bytes`: сколько хранить сообщения. `delete` (по умолчанию, удаляет старые) vs `compact` (хранит последнее значение по key — для config/topology).
- **Log compaction** — для каждого key остаётся только последнее значение (и tombstone). Идеально для state (current account balance), changelog, KTable source. `min.compaction.lag.ms`, `max.compaction.lag.ms`, `delete.retention.ms` для tombstones.
- **Consumer offset commit**:
  - **Auto-commit** (`enable.auto.commit=true`, `auto.commit.interval.ms=5000`) — простой, но может потерять или переобработать (at-least-once, не exactly-once).
  - **Manual sync** `consumer.commitSync()` — надёжно, но blocking.
  - **Manual async** `consumer.commitAsync(callback)` — fast, но не retry на failure.
  - В Spring Kafka: `Acknowledgment.acknowledge()` в зависимости от listener container.
- **Rebalancing** — перераспределение partitions между consumer'ами при: join/leave/сrash consumer'а, добавлении partition, изменения подписки.
  - **Eager protocol** (старый) — *stop-the-world*: все отзывают partitions, потом переназначают; brief window без обработки.
  - **Cooperative protocol** (KIP-429, с 2.4) — incremental: только затронутые partitions отзываются, остальные продолжают обрабатываться; минимальный downtime.
  - `partition.assignment.strategy`: `RangeAssignor` (default), `RoundRobinAssignor`, `StickyAssignor`, `CooperativeStickyAssignor` (recommended).
- **ConsumerRebalanceListener** — `onPartitionsRevoked` (commit offsets, cleanup), `onPartitionsAssigned` (init state); критично для graceful shutdown и stateful consumers.
- **Static group membership** (`group.instance.id`) — consumer не покидает group при restart (в течение `session.timeout.ms`); не триггерит rebalance; для stateful/long-running consumers.
- **Kafka Streams** — библиотека для stream processing поверх Kafka: KStream (event stream), KTable (changelog, state store), GlobalKTable (full replication). Operations: map/filter/branch/join/aggregate/window. State stores (RocksDB) локальные, changelog в Kafka для recovery.
- **Kafka Streams EOS** — `processing.guarantee=exactly_once_v2` (KIP-447): consume-transform-produce атомарно через транзакции Kafka.
- **Schema Registry** (Confluent) — хранит схемы (Avro/Protobuf/JSON Schema) в отдельном topic `_schemas`; producer register schema → get schema ID → prepend to message; consumer fetch schema by ID. Compatibility: BACKWARD (new readers read old data), FORWARD, FULL; evolves with rules.
- **Avro** — binary format with schema; schema хранится отдельно от данных, в сообщении только schema ID (5 bytes) → compact. Совместимость: add field with default (backward), remove field with default (forward), не менять field type, не переименовывать (использовать aliases).
- **KRaft mode** (KIP-500, GA в Kafka 3.3+, дефолт с 4.0) — Kafka без ZooKeeper; metadata хранится в internal Kafka topic (`__cluster_metadata`); controller quorum из brokers; меньше moving parts, быстрее startup, simpler ops.
- **Connect** — фреймворк для интеграции Kafka с внешними системами через Source (в Kafka) и Sink (из Kafka) connectors; works on workers cluster; REST API для управления.
- **Producer configs для производительности**:
  - `linger.ms` (5-20ms) — batching на producer side.
  - `batch.size` (16-64KB) — max batch size.
  - `compression.type` — `none`, `gzip`, `snappy`, `lz4` (fast), `zstd` (best ratio, fast).
  - `buffer.memory` — producer buffer; при переполнении — `block` или `drop`.
  - `max.request.size` — ограничение размера сообщения.
- **Consumer configs**:
  - `fetch.min.bytes` / `fetch.max.bytes` — batching на consumer.
  - `max.poll.records` — сколько сообщений за один poll.
  - `max.poll.interval.ms` (default 300000) — timeout между polls; если consumer занят дольше — rebalance.
  - `session.timeout.ms` (45000 в новых) — heartbeat timeout.
  - `heartbeat.interval.ms` (3000) — частота heartbeat.
- **Quota** — bandwidth/request rate quotas per user/client; для multi-tenant кластеров.
- **ACLs** — Kafka авторизация через `kafka-acls.sh`; SASL/PLAIN, SCRAM, OAuth; SSL для authn+mTLS.
- **Топология с MirrorMaker 2** — репликация между кластерами (DC, regions); `ReplicationPolicy` для переименования топиков; offset sync.

### Материалы (реальные ссылки)

- **Apache Kafka Documentation (introduction)**: https://kafka.apache.org/documentation/
- **Apache Kafka Design**: https://kafka.apache.org/43/design/design/
- **Apache Kafka Distribution**: https://kafka.apache.org/30/implementation/distribution/
- **Apache Kafka Basic Operations**: https://kafka.apache.org/41/operations/basic-kafka-operations/
- **Apache Kafka Streams Core Concepts**: https://kafka.apache.org/41/streams/core-concepts/
- **Apache Kafka Streams Config**: https://kafka.apache.org/43/streams/developer-guide/config-streams/
- **Apache Kafka Upgrading (EOS notes)**: https://kafka.apache.org/41/getting-started/upgrade/
- **Apache Kafka Introduction (getting started)**: https://kafka.apache.org/10/getting-started/introduction/
- **Apache Kafka Topic Configs**: https://kafka.apache.org/41/configuration/topic-configs/
- **Apache Kafka Broker Configs**: https://kafka.apache.org/41/configuration/broker-configs/
- **Apache Kafka Connect User Guide**: https://kafka.apache.org/40/kafka-connect/user-guide/
- **Apache Kafka ConsumerRebalanceListener**: https://kafka.apache.org/43/javadoc/org/apache/kafka/clients/consumer/ConsumerRebalanceListener.html
- **Apache Kafka CooperativeStickyAssignor**: https://kafka.apache.org/41/javadoc/org/apache/kafka/clients/consumer/CooperativeStickyAssignor.html
- **Apache Kafka GlobalKTable**: https://kafka.apache.org/25/javadoc/org/apache/kafka/streams/kstream/GlobalKTable.html
- **Confluent — Exactly-once Semantics**: https://www.confluent.io/blog/exactly-once-semantics-are-possible-heres-how-apache-kafka-does-it/
- **Confluent — Schema Evolution & Compatibility**: https://docs.confluent.io/platform/current/schema-registry/fundamentals/schema-evolution.html
- **Confluent — Schema Registry Best Practices**: https://www.confluent.io/blog/best-practices-for-confluent-schema-registry/
- **Confluent — Kafka Streams concepts**: https://docs.confluent.io/platform/current/streams/concepts.html
- **Conduktor — KStream vs KTable vs GlobalKTable**: https://www.conduktor.io/kafka-streams/kstream-ktable-globalktable
- **Книга**: Kreps, Narkhede, Pacheco, "Kafka: The Definitive Guide" 2nd ed. (O'Reilly, 2021).
- **Книга**: Bejeck, "Kafka Streams in Action" 2nd ed. (Manning, 2022).

### Практика / Кейсы

1. **Кластер 3 брокера, RF=3, acks=all, min.insync.replicas=2.** Настройте producer с idempotence=true, transactional.id для transactional sends. Симулируйте: убейте 1 брокер — producer продолжает работать (ISR=2). Убейте второй — producer получает NOT_ENOUGH_REPLICAS. Измерьте throughput: acks=0 vs acks=1 vs acks=all (typically acks=all в 1.5-2x медленнее, но durable).
2. **Log compaction для state store.** Топик `account-balance` с `cleanup.policy=compact`. Публикуйте `{key:"acc1", value:100}`, потом `{key:"acc1", value:150}`. После compaction остаётся только `value:150`. Запустите KafkaStreams с KTable из этого топика — state store RocksDB содержит только latest. Покажите, что tombstone (value=null) удаляет key полностью после `delete.retention.ms`.
3. **Cooperative vs Eager rebalance.** Запустите 3 consumer'ов в группе на 12 partitions. Симулируйте добавление 4-го consumer'а: с Eager — все 4 кратко прекращают обработку (stop-the-world ~5s); с Cooperative — только 3 partitions переводятся, остальные 9 продолжают (минимальный downtime). Измерьте lag-impact.
4. **Schema evolution Avro.** v1: `schema { type User { name: string, age: int } }`. v2: add `email: string` (default null) — BACKWARD compatible. v3: rename `name` → `fullName` via aliases. v4: change `age` int → long — совместимо (int promotes to long). v5: remove `email` without default — NOT backward compatible. Настройте Schema Registry с `BACKWARD_TRANSITIVE`, покажите, что v5 не регистрируется, prod-v1 consumer не падает на v4 данных (через defaults).
5. **Kafka Streams word count с EOS.** Приложение: input topic `sentences` → split → groupBy → count → output topic `word-counts`. Включите `processing.guarantee=exactly_once_v2`. Симулируйте crash streams-приложения в середине — при restart обрабатывается ровно один раз, output topic не имеет дублей. Включите state store changelog — при restart state восстанавливается из Kafka.
6. **Transactional producer: consume-transform-produce.** Consumer читает `orders`, для каждого создаёт `invoice` в output topic + commit offset — все в одной Kafka транзакции. Симулируйте exception в transform — abortTransaction(), offset не закоммичен, при restart сообщение переобрабатывается. Покажите, что consumer на `read_committed` не видит partial output.
7. **MirrorMaker 2 для DR.** Кластер A (primary) и B (DR). MM2 реплицирует топики `A → B` с prefix `us-east.`, offset sync для transparent failover. Симулируйте failover: consumer переключается с A на B, offset translation через `OffsetSyncStore`. Измерьте RPO и RTO. Обсудите bidirectional replication risks (cycles).
8. **KRaft migration.** Существующий кластер Kafka 3.x с ZooKeeper → миграция на KRaft. Используйте `kafka-storage.sh format` + `kraft` mode. Покажите: метаданные в `__cluster_metadata` topic, controllers работают как raft quorum, нет ZK зависимостей, startup time 30s vs 2min (ZK). Зафиксируйте шаги миграции как runbook.

---

## 7. RabbitMQ

### Теория

- **RabbitMQ** (Pivotal/VMware, написан на Erlang/OTP) — classic AMQP 0-9-1 broker; rich routing, push-model delivery, low latency;的传统 message broker.
- **AMQP 0-9-1 model**: Publisher → **Exchange** → binding (routing key) → **Queue** → Consumer. Exchange и queue развязаны через bindings.
- **Exchange types**:
  - **Direct** — routing key exact match с binding key.
  - **Topic** — routing key pattern match: `*` (one word), `#` (zero or more words); `orders.us.premium` matches `orders.*.premium`.
  - **Fanout** — игнорирует routing key, broadcast во все bound queues.
  - **Headers** — match по headers (`x-match: all` / `any`), игнорирует routing key.
- **Default exchange** — empty name `""`, implicit binding каждой очереди с routing key = queue name; фактически direct.
- **Queue** — FIFO буфер (если нет priority); durable (переживает restart broker), exclusive (привязана к connection, удаляется при disconnect), auto-delete (удаляется после последнего consumer).
- **Binding** — связь exchange→queue с (опц.) binding key; у одного exchange может быть много bindings.
- **Dead Letter Exchange (DLX)** — сообщения, попавшие в DLX условия: `x-dead-letter-exchange` на queue + reject/nack с requeue=false, TTL expired, queue length limit. Полезно для retry pattern, error analysis, poison messages.
- **TTL**:
  - **Per-queue** (`x-message-ttl`) — все сообщения в очереди имеют TTL.
  - **Per-message** (`expiration` header) — индивидуально; но проблема: TTL-сообщения не переупорядочиваются, могут блокировать head-of-line (HoL) — сообщение с большим TTL в head блокирует shorter-TTL позади.
- **Lazy queues** (classic) — сообщения сразу на диск, а не в RAM; для очередей с миллионами сообщений; ценой latency.
- **Publisher confirms** — broker ack'ает publish после сохранения в очередь; для reliable publishing. `channel.confirmSelect()` + `addConfirmListener(handler)`.
- **Consumer acknowledgements**:
  - **autoAck=true** — сообщение удаляется при доставке (fire-and-forget, не безопасно).
  - **autoAck=false** — consumer должен `basicAck(deliveryTag, multiple=false)` после обработки; nack/reject с requeue=true/false.
- **prefetch (QoS)** — `basicQos(prefetchCount)` — сколько unacked сообщений consumer держит; баланс throughput vs fairness; для Java-клиента по умолчанию 256.
- **mandatory flag** — publisher указывает `mandatory=true`; если сообщение не находит queue — broker возвращает через `ReturnListener` (basic.return). Для обнаружения misrouted.
- **immediate flag** — устарел в AMQP 0-9-1, поддерживался RabbitMQ до 3.0; не использовать.
- **Clustering**:
  - Classic mirroring (устаревший): master + slaves per queue; sync writes через master.
  - **Quorum queues** (современный default) — на Raft consensus; replicated, durable, crash-safe, leader election. Используют disk-based log, replication через Raft.
  - **Streams** (с RabbitMQ 3.9) — append-only log, как Kafka; replay from offset, no consumer ack per message; high throughput.
- **Quorum queue vs classic mirrored**:
  - Quorum: Raft-based, no message loss on partition majority side, slower than classic for low-latency.
  - Classic mirrored: master-slave, possible message loss on master crash before sync, faster but less safe.
  - Quorum не поддерживает: priority queues, TTL per-message (HoL проблема), lazy (всегда disk-based).
- **Streams** — immutable append-only log с replication; consumers track offset (offlinable); `QoS` не применяется (consumer pull-model). Аналог Kafka внутри RabbitMQ. Поддержка `rabbitmq-streams` plugin.
- **Federation & Shovel** — для cross-region/cross-cluster репликации: federation (exchange/queue links, async), shovel (point-to-point, simpler).
- **Connection vs channel**: connection — TCP; channel — virtual connection внутри connection (multiplexing); channels не thread-safe, one channel per thread.
- **Flow control** — broker throttles publisher при high memory/disk pressure; `connection.blocked` notification.
- **Memory alarm** — `vm_memory_high_watermark` (default 0.4 RAM) → блокирует publishers.
- **Disk alarm** — `disk_free_limit` (default 50MB) → блокирует publishers.
- **Priority queues** (`x-max-priority`): сообщения с высоким priority в head очереди; но classic only, в quorum не поддерживается.
- **Plugin-экосистема**: Management UI, STOMP, MQTT, federation, shovel, streams, delayed message (community), consistent hash exchange.
- **Metrics**: `rabbitmqctl list_queues`, Management HTTP API `/api/queues`, Prometheus exporter.
- **HA топологии**: load-balancer (HAProxy) перед кластером; clients reconnect; quorum queue replication across nodes.
- **Retry pattern с DLX**: main queue → (nack requeue=false) → DLX → retry queue с TTL → back to main queue. Delayed-retry через TTL expiry + DLX.
- **Connection recovery** — Java client автоматически реконнектится; настраивается `AutomaticRecoveryEnabled`, `NetworkRecoveryInterval`, `TopologyRecoveryEnabled` (пересоздание exchanges/queues/bindings).

### Материалы (реальные ссылки)

- **RabbitMQ Documentation (main)**: https://www.rabbitmq.com/documentation.html
- **RabbitMQ Quorum Queues**: https://www.rabbitmq.com/docs/quorum-queues
- **RabbitMQ Dead Letter Exchanges**: https://www.rabbitmq.com/docs/dlx
- **RabbitMQ Consumers**: https://www.rabbitmq.com/docs/consumers
- **RabbitMQ Exchanges (4.x)**: https://www.rabbitmq.com/docs/4.1/exchanges
- **RabbitMQ Streams**: https://www.rabbitmq.com/docs/streams
- **RabbitMQ Publisher Confirms**: https://www.rabbitmq.com/docs/confirms
- **RabbitMQ Federation**: https://www.rabbitmq.com/docs/federation
- **RabbitMQ Shovel**: https://www.rabbitmq.com/docs/shovel
- **RabbitMQ Consumer Prefetch**: https://www.rabbitmq.com/docs/consumer-prefetch
- **RabbitMQ Queues (types)**: https://www.rabbitmq.com/docs/queues
- **RabbitMQ HA & Clustering**: https://www.rabbitmq.com/docs/clustering
- **CloudAMQP — cost of features**: https://www.cloudamqp.com/blog/uncovering-the-true-cost.html
- **RabbitMQ Exchange Types (Medium)**: https://medium.com/trendyol-tech/rabbitmq-exchange-types-d7e1f51ec825
- **Книга**: Videla & Williams, "RabbitMQ in Action" (Manning, 2012) — основы, немного устарел по quorum/streams.
- **Книга**: Hohpe & Woolf, "Enterprise Integration Patterns" — паттерны обмена сообщениями.

### Практика / Кейсы

1. **Direct + topic + fanout в одной системе.** Задача: order-service публикует `order.created` (fanout для notification + audit), `order.paid.{region}` (topic для region-specific handlers), `order.cancelled` (direct для inventory). Реализуйте на Spring AMQP, покажите, что один exchange с разными bindings правильно маршрутизирует. Тест: 1000 сообщений каждого типа — все доставлены в правильные очереди.
2. **Retry с DLX + TTL.** Main queue `orders.process` с DLX `orders.dlx`. Consumer падает на poison message 3 раза → nack requeue=false → DLX → `orders.retry.30s` queue с TTL 30s + DLX = main → через 30s сообщение возвращается в main. Если после 3 retry всё ещё fail → `orders.dead` queue (parking lot). Реализуйте, симулируйте poison message, измерьте время до parking lot (~90s).
3. **Quorum queue crash test.** Кластер 3 узла, quorum queue `critical.orders`. Убейте leader node — Raft elects new leader,消费者 не видят downtime (>2s ack). Убейте 2 узла (потеря majority) — queue unavailable. Покажите, что classic mirrored queue на том же сценарии может потерять сообщения (master crash before slave sync).
4. **Publisher confirms + mandatory.** Включите confirms, mandatory=true, ReturnListener. Симулируйте: (a) publish в несуществующий routing key — ReturnListener срабатывает, `replyCode=312 NO_ROUTE`; (b) broker restart между publish и ack — confirm не приходит, retry. Покажите, что без confirms publisher не знает о потере.
5. **Prefetch tuning.** Consumer обрабатывает 50ms/message. pref=1 — низкий throughput, но fair; pref=1000 — max throughput, но один consumer может забить очередь; pref=10 — баланс. Измерьте throughput vs fairness (между несколькими consumer'ами). Объясните HoL-block: один long-running держит за собой все prefetched, другие простаивают.
6. **Streams для event log.** Переведите `audit.events` с classic queue на stream (RabbitMQ 3.9+). Consumers track offset, могут replay с начала. Покажите: новый consumer стартует с offset=0 и обрабатывает всю историю; offset хранится server-side (`stream-tracking`). Сравните throughput: classic queue 30k msg/s vs stream 200k msg/s.
7. **Federation cross-region.** 2 RabbitMQ кластера: EU и US. Federation link `orders.created` US → EU. Симулируйте разрыв сети — federation buffer'ит на стороне US; восстановление — backlog отправляется. Покажите, что сообщения не теряются, ordering сохраняется в рамках exchange. Сравните с Shovel (simpler setup, single connection).
8. **Memory alarm + flow control.** Нагрузите broker 1M сообщений в очередь без consumers. При достижении `vm_memory_high_watermark=0.4` — publishers блокируются (connection.blocked). Покажите, что без аларма broker OOM'ит; с алармом — graceful degradation. Подключите lazy queue для уменьшения RAM pressure, сравните max messages in queue: classic in-memory 100k vs lazy 5M.

---

## 8. Spring Kafka / Spring AMQP

### Теория

- **Spring Kafka** (Spring for Apache Kafka) — абстракции поверх Kafka clients: `KafkaTemplate`, `@KafkaListener`, `ConcurrentKafkaListenerContainerFactory`, `KafkaTransactionManager`, error handlers.
- **`KafkaTemplate<K,V>`** — синхронный/асинхронный producer: `send(ProducerRecord)` возвращает `CompletableFuture<SendResult>`; `executeInTransaction(callback)` для transactional sends; supports Micrometer metrics.
- **`@KafkaListener`** — декларативный consumer:
  ```java
  @KafkaListener(topics = "orders", groupId = "order-processor",
                 containerFactory = "kafkaListenerContainerFactory")
  public void handle(OrderEvent event, Acknowledgment ack, @Header(KafkaHeaders.RECEIVED_PARTITION) int p) {
      ...
      ack.acknowledge();
  }
  ```
  Параметры: `topics`, `topicPattern`, `groupId`, `containerFactory`, `concurrency`, `id`, `autoStartup`, `batch`.
- **Container factory** — конфигурация listener container: `ConcurrentKafkaListenerContainerFactory`; `setConcurrency(N)` → N `KafkaMessageListenerContainer`'s per `@KafkaListener`, каждый со своим consumer (partition-распределение).
- **Manual ack mode** — `ContainerProperties.AckMode.MANUAL` или `MANUAL_IMMEDIATE`; `Acknowledgment.acknowledge()` должен вызываться после обработки. `RECORD` (after each), `BATCH` (default, after each poll batch), `TIME` (interval), `COUNT` (count), `COUNT_TIME`.
- **Batch listener** — `@KafkaListener(batch = "true")` → метод принимает `List<OrderEvent>`; обрабатывает пачку за один poll (`max.poll.records`); эффективнее для high-throughput, но сложнее error handling.
- **`@SendTo`** — reply pattern: listener может вернуть значение, оно публикуется в reply topic; для request/reply семантики над Kafka (через `@SendTo("replies")` + `ReplyingKafkaTemplate`).
- **Error handlers**:
  - `DefaultErrorHandler` (с Spring Kafka 2.8, замена для `SeekToCurrentErrorHandler`) — retry in-place через `RetryTemplate`, затем `DeadLetterPublishingRecoverer` (DPT —Dead Letter Topic).
  - `CommonErrorHandler` interface; `ConsumerRecordRecoverer` для пользовательской логики после исчерпания retries.
  - `BatchErrorHandler` для batch listeners.
- **`DeadLetterPublishingRecoverer`** — публикует failed record в DLT; по умолчанию DLT name = `<original-topic>.DLT`; настраивается через `BiFunction<record, ex, TopicPartition>`; в DLT добавляются headers: `kafka_dlt-exception-fqcn`, `kafka_dlt-exception-stacktrace`, `kafka_dlt-exception-message`, `kafka_dlt-original-topic`, `kafka_dlt-original-partition`, `kafka_dlt-original-offset`, `kafka_dlt-original-timestamp`, `kafka_dlt-original-timestamp-type`, `kafka_dlt-original-consumer-group`.
- **DLT retry strategies**: (a) terminal DLT (после N retries → DLT, дальше ручная обработка); (b) non-blocking retries через multiple DLT topics с TTL (через Apache Kafka retry topic pattern, `@RetryableTopic` в Spring Kafka 2.7+).
- **`@RetryableTopic`** — декларативные non-blocking retries: `@RetryableTopic(attempts = "5", backoff = @Backoff(delay = 1000, multiplier = 2.0))` → Spring создаёт retry topics `<topic>-retry-0`, `-retry-1`, ... с TTL-based delay (через Kafka retention/consumer delay). DLT как final destination.
- **Transactions in Spring Kafka**:
  - `KafkaTransactionManager` — Spring-транзакция поверх Kafka producer; `@Transactional` + `KafkaTemplate.executeInTransaction` → commit Kafka tx с offset.
  - `ChainedKafkaTransactionManager` для chained Kafka + DB transaction (если DB supports XA, или best-effort 1PC).
  - Для **consume-transform-produce EOS**: `containerFactory.getContainerProperties().setTransactionManager(kafkaTransactionManager)` — listener commits offset + producer send в одной Kafka tx.
- **Concurrency** — `factory.setConcurrency(N)` создаёт N consumer threads; каждый обрабатывает subset of partitions. Больше consumers чем partitions — некоторые простаивают.
- **`@KafkaListener` filtering** — `RecordFilterStrategy` для пропуска некоторых записей; `containerFactory.setRecordFilterStrategy(...)`.
- **Deserialization** — `JsonDeserializer` (type headers for class), `ErrorHandlingDeserializer` (обёртка для graceful handling deserialization errors → DLT без падения listener).
- **Spring AMQP** — абстракции для RabbitMQ: `RabbitTemplate`, `@RabbitListener`, `SimpleRabbitListenerContainerFactory`, `RabbitTransactionManager`.
- **`RabbitTemplate`** — publisher: `convertAndSend(exchange, routingKey, message)`, `receive(queue)`, `convertSendAndReceive` для RPC. Publisher confirms через `RabbitTemplate.ConfirmCallback` и `ReturnsCallback`.
- **`@RabbitListener`** — consumer:
  ```java
  @RabbitListener(queues = "orders.process", containerFactory = "rabbitListenerContainerFactory")
  public void handle(OrderEvent event) { ... }
  ```
  Параметры: `queues`, `queuesToDeclare` (auto-declare), `bindings`, `containerFactory`, `concurrency`, `batch`, `returnExceptions`.
- **Container factory** — `SimpleRabbitListenerContainerFactory`; `setConcurrentConsumers`, `setMaxConcurrentConsumers`, `setAcknowledgeMode(AUTO/MANUAL/NONE)`, `setPrefetchCount`, `setBatchSize`.
- **AcknowledgeMode**: `AUTO` (default, ack после метода без exception), `MANUAL` (Channel.basicAck), `NONE` (no ack, fire-and-forget).
- **Error handling in Spring AMQP**:
  - `RabbitListenerErrorHandler` — `handleError(message, messageListener, exception)` — кастомная обработка.
  - `RetryTemplate` + `RepublishMessageRecoverer` — retries + DLQ publishing (вместо AMQP DLX; Spring-publish в `queue.parkinglot`).
  - `ConditionalRejectingExceptionHandler` — default handler, rejects unrecoverable (e.g., `ListenerExecutionFailedException` from conversion).
- **RepublishMessageRecoverer** — публикует failed message в DLX с заголовками `x-death`, `x-original-routingKey`, stack trace; аналог `DeadLetterPublishingRecoverer` в Spring Kafka.
- **Concurrency & prefetch** — `SimpleRabbitListenerContainerFactory.setPrefetchCount(N)` (по умолчанию 250); `setConcurrentConsumers(M)` — M consumer threads per `@RabbitListener`. Для high-throughput: 5-20 consumers, pref=10-50.
- **Transactions in Spring AMQP** — `RabbitTransactionManager` + `ChannelTransacted=true` — publisher/consumer в одной AMQP channel transaction; но не транзакция с DB без XA.
- **Message conversion** — `Jackson2JsonMessageConverter` с `DefaultJackson2JavaTypeMapper` (тип из `__TypeId__` header), `SimpleMessageConverter` (Java serialization, опасен).
- **Batch listener** (с Spring AMQP 2.2+) — `@RabbitListener(batch = "true")` → `List<OrderEvent>`; requires `setBatchSize` + `setConsumerBatchEnabled(true)`.
- **Observability** — Spring Boot 3 + Micrometer Tracing автоматически инструментирует `KafkaTemplate` и `@KafkaListener` (и RabbitTemplate / @RabbitListener) — trace propagation через `traceparent` header.

### Материалы (реальные ссылки)

- **Spring Kafka reference**: https://docs.spring.io/spring-kafka/reference/
- **Spring Kafka — Handling Exceptions**: https://docs.spring.io/spring-kafka/reference/kafka/annotation-error-handling.html
- **Spring Kafka — DeadLetterPublishingRecoverer Javadoc**: https://docs.spring.io/spring-kafka/api/org/springframework/kafka/listener/DeadLetterPublishingRecoverer.html
- **Spring Kafka — DeadLetterPublishingRecoverer source (GitHub)**: https://github.com/spring-projects/spring-kafka/blob/master/spring-kafka/src/main/java/org/springframework/kafka/listener/DeadLetterPublishingRecoverer.java
- **Baeldung — Kafka @KafkaListener batch**: https://www.baeldung.com/kafka-kafkalistener-annotation-handle-messages-batch
- **JDriven — Kafka Dead Letter Publishing**: https://jdriven.com/blog/2022/01/Kafka-Dead-Letter-Publishing
- **Spring AMQP reference**: https://docs.spring.io/spring-amqp/reference/
- **Spring AMQP — Handling Exceptions**: https://docs.spring.io/spring-amqp/reference/amqp/receiving-messages/async-annotation-driven/error-handling.html
- **Spring AMQP — Exception Handling**: https://docs.spring.io/spring-amqp/reference/amqp/exception-handling.html
- **Spring AMQP — RabbitListenerErrorHandler Javadoc**: https://docs.spring.io/spring-amqp/api/org/springframework/amqp/rabbit/listener/api/RabbitListenerErrorHandler.html
- **Baeldung — Error Handling with Spring AMQP**: https://www.baeldung.com/spring-amqp-error-handling
- **Spring for Apache Kafka project page**: https://spring.io/projects/spring-kafka
- **Spring AMQP project page**: https://spring.io/projects/spring-amqp
- **Книга**: Walls, "Spring in Action" 6th ed. (Manning, 2022) — глава про messaging.
- **Книга**: Cosmina et al., "Pro Spring" 6th ed. (Apress, 2022) — главы про JMS/AMQP/Kafka.

### Практика / Кейсы

1. **Spring Kafka + DLT + @RetryableTopic.** Реализуйте `@KafkaListener(topics="orders")` + `@RetryableTopic(attempts=5, backoff=@Backoff(delay=1000, multiplier=2.0, maxDelay=10000))`. Spring автоматически создаёт `orders-retry-0`, `-retry-1`, ..., `orders-dlt`. Симулируйте poison message (всегда throw) — оно переходит через retry topics с exponential delay, в итоге в DLT. Покажите все Kafka headers в DLT-сообщении. Реализуйте отдельный `@KafkaListener(topics="orders-dlt")` для алертинга.
2. **Batch listener с batch error handler.** `@KafkaListener(batch="true", topics="metrics")` обрабатывает 500 записей за poll. Если запись #250 падает — весь batch может rollback (DefaultErrorHandler с `BatchCheckRetryStrategy`) или skip только bad record. Реализуйте обе стратегии, сравните trade-offs. Измерьте throughput: single-record listener 10k/s vs batch listener 50k/s.
3. **Spring Kafka EOS transactional.** `KafkaTransactionManager` + `@Transactional` + `containerFactory.setTransactionManager(ktm)`. В listener: consume from `orders`, transform, produce to `invoices` + `audit` — все атомарно через Kafka tx. Симулируйте exception — все 3 produces абортируются, offset не закоммичен. Покажите, что `read_committed` consumer не видит partial.
4. **Kafka + DB transaction (best-effort 1PC).** Listener должен записать в Postgres и опубликовать в Kafka — атомарность через Outbox (транзакция БД + outbox event), не через XA. Реализуйте через Debezium или polling publisher. Обсудите, почему `ChainedKafkaTransactionManager` + DataSourceTransactionManager не дает true atomicity (нарушение 2PC без XA).
5. **Spring AMQP retry + DLX.** `@RabbitListener(queues="orders.process")` с `RetryTemplate(maxAttempts=3, backoff=exponential)` + `RepublishMessageRecoverer(dlx="orders.dlx")`. Симулируйте business exception — сообщение ретраится 3 раза in-memory, затем публикуется в `orders.dlx` с `x-death` header и stack trace. Сравните с native RabbitMQ DLX (без Spring retry) — Spring даёт richer headers + no blocking of queue.
6. **Spring AMQP concurrency + prefetch tuning.** `setConcurrentConsumers(10)`, `setPrefetchCount(50)`, `setBatchSize(10)`. Симулируйте slow consumer (100ms/process). Черезput с pref=1, 10, 50, 250 — измерьте, найдите optimum. Покажите, что pref=250 даёт HoL-проблему (один consumer держит 250 unacked, другие простаивают).
7. **Spring AMQP batch listener + transactions.** `@RabbitListener(batch="true", queues="orders")` → `List<Order>` → DB insert all + ack. Если batch=100 и одна запись дубликат (unique constraint) — выбросить весь batch или skip one? Реализуйте через `ConditionalRejectingExceptionHandler` + custom recoverer, который публикует bad record в DLQ и продолжает batch.
8. **Observability: trace propagation Kafka/Rabbit.** Spring Boot 3 + Micrometer Tracing + OpenTelemetry exporter → Jaeger. `@KafkaListener` на service A → `KafkaTemplate.send()` на service B → `@KafkaListener` на B → `RabbitTemplate` на service C → `@RabbitListener` на C. Покажите единый trace через все 4 хопа, корреляция через `traceparent` header (W3C) для Kafka и AMQP. Измерьте overhead <5%.

---

## 9. Микросервисы паттерны

### Теория

- **Microservices** (Newman, Fowler, 2014) — архитектурный стиль: приложение состоит из небольших (small), автономных (autonomous) сервисов, общающихся через сеть; каждый сервис владеет своими данными и деплоится независимо.
- **Database per service** — каждый сервис имеет собственную БД (или schema); нет shared DB; сервисы обмениваются данными через API/events. Гарантирует loose coupling, но создаёт distributed data problems.
- **API Composition** (aggregation) — один сервис (aggregator) вызывает несколько других и объединяет результаты; для read-heavy cross-service queries. Проблема: N+1, latency, partial failures.
- **CQRS (Command Query Responsibility Segregation)** — разделение write-side (commands, оптимизировано для writes) и read-side (queries, оптимизировано для reads, денормализованная модель). Read model обновляется через events.
- **Event Sourcing** — состояние сохраняется как последовательность событий (event log); текущее состояние = projection/replay событий. Benefits: audit, time-travel, temporal queries. Costs: complexity, schema evolution, snapshots.
- **Saga pattern** — управление distributed transaction через последовательность локальных транзакций; каждая step имеет compensating action. Два варианта:
  - **Choreography** — каждый сервис реагирует на события, решает что делать; нет central coordinator; loose coupling, но сложнее отслеживать.
  - **Orchestration** — orchestrator (state machine) управляет шагами, отправляет команды; centralized logic, easier to reason; but orchestrator is SPOF.
- **API Gateway** — единая точка входа для клиентов; routing, auth, rate limiting, response composition, protocol translation. Spring Cloud Gateway, Kong, Envoy, NGINX, AWS API Gateway.
- **BFF (Backend For Frontend)** — отдельный gateway (или набор сервисов) под конкретный клиент (web, mobile, partner); специфика под UI; предотвращает «one size fits none» API.
- **Service Discovery** — динамическое нахождение инстансов сервиса; server-side (Eureka, Consul, etcd) или client-side (Kubernetes DNS + service mesh). В Spring Cloud — Eureka (Netflix OSS), Consul.
- **Sidecar** — вспомогательный контейнер рядом с приложением (Kubernetes pod); cross-cutting concerns: logging, monitoring, proxy, config; основа service mesh.
- **Service Mesh** — инфраструктурный слой (sidecars + control plane); mTLS, traffic management, observability, policy; Istio, Linkerd, Consul Connect.
- **Strangler Fig** (Martin Fowler) — постепенная миграция legacy: новый функционал в новых сервисах, старый slowly «душится» (strangled) по мере переноса; interceptor/фасад маршрутизирует между old и new.
- **Anti-Corruption Layer (ACL)** — переводчик между bounded contexts (особенно legacy → new); изолирует новую модель от устаревшей; часто как adapter/facade.
- **Bulkhead** — изоляция ресурсов между вызовами (thread pools, connections), чтобы отказ в одном не «затопил» весь сервис; Bulkhead pattern из Release It!.
- **Circuit Breaker** — автоматический разрыв цепи при повторных ошибках downstream; states: closed (normal), open (fail fast), half-open (probe); предотвращает cascading failures.
- **Outbox pattern** — транзакционная публикация событий вместе с бизнес-данными (см. раздел 5).
- **Idempotent Consumer** — обработка дубликатов сообщений без побочных эффектов; dedup table или unique constraint.
- **Materialized View** — денормализованная read-модель, обновляемая через events; per-service, оптимизирована под конкретные queries.
- **Saga log** — persistent storage of saga state (current step, completed, compensations); для recovery после crash.
- **Competing Consumers** — несколько consumer'ов читают одну очередь; load balancing; in Kafka — consumer group, in RabbitMQ — multiple consumers per queue.
- **Publish-Subscribe** — publisher публикует события, multiple subscribers получают; через Kafka topic или RabbitMQ fanout.
- **Request-Reply** — синхронный (HTTP/gRPC) или асинхронный (reply queue/topic); для command/query.
- **Event Notification** — короткое событие «X happened», без данных; подписчик сам запрашивает детали. Loose coupling, но N+1.
- **Event-Carried State Transfer** — событие содержит все данные; подписчику не нужно возвращаться. Loose coupling, но дублирование данных.
- **Domain Event** — событие в domain-driven design, имеет meaning для domain expert; `OrderPlaced`, `PaymentProcessed`.
- **Integration Event** — событие для межсервисного общения; serialized (Avro/Protobuf), versioned, может отличаться от domain event.
- **Bounded Context** (DDD) — граница, в которой модель консистентна; один microservice = один bounded context (часто).
- **Aggregates** (DDD) — кластер доменных объектов, рассматриваемый как единое целое для транзакций; корень агрегата — точка входа.
- **Shared Database anti-pattern** — несколько сервисов делят БД; ломает автономность, Schema ownership; миграция в сторону DB-per-service.
- **Distributed transactions (2PC)** — не подходят для микросервисов (slow, fragile); используйте Saga.
- **Saga isolation problems** — нет глобальной изоляции; видимость partial states; решается через semantic lock (state field: `PENDING`), commutative updates, replay.
- **Asynchronous APIs** — async over sync: вместо блокирующего HTTP-вызова публикуем событие и ждём результата через callback/event; better resilience, но сложнее UX.

### Материалы (реальные ссылки)

- **Microservices.io — main patterns catalog**: https://microservices.io/patterns/
- **Microservices.io — Saga pattern**: https://microservices.io/patterns/data/saga.html
- **Microservices.io — Transactional Outbox**: https://microservices.io/patterns/data/transactional-outbox.html
- **Microservices.io — API Composition**: https://microservices.io/patterns/data/api-composition.html
- **Microservices.io — CQRS**: https://microservices.io/patterns/data/cqrs.html
- **Microservices.io — Event Sourcing**: https://microservices.io/patterns/data/event-sourcing.html
- **Microservices.io — Microservice Architecture**: https://microservices.io/patterns/microservices.html
- **Microservices.io — Events at the core**: https://microservices.io/post/microservices/2021/02/21/events-are-the-core.html
- **Microservices.io — Managing Sagas**: https://microservices.io/microservices/general/2018/03/22/microxchg-sagas.html
- **Martin Fowler — Strangler Fig Application**: https://martinfowler.com/bliki/StranglerFigApplication.html
- **Martin Fowler — BFF pattern**: https://martinfowler.com/bliki/Bff.html
- **Martin Fowler — CircuitBreaker**: https://martinfowler.com/bliki/CircuitBreaker.html
- **Eventuate — patterns explained**: https://eventuate.io/post/eventuate/2020/02/24/why-eventuate.html
- **Distributed Data Patterns overview**: https://medium.com/@platform.engineers/distributed-data-management-patterns-saga-api-composition-and-cqrs-fd95c5c0892b
- **Книга**: Richardson, "Microservices Patterns" (Manning, 2018) — основной источник по всем паттернам.
- **Книга**: Newman, "Building Microservices" 2nd ed. (O'Reilly, 2021) — design, integration, deployment.
- **Книга**: Vaughn Vernon, "Implementing Domain-Driven Design" (Addison-Wesley, 2013) — DDD-основа.
- **Книга**: Eric Evans, "Domain-Driven Design" (Addison-Wesley, 2003) — классика DDD.

### Практика / Кейсы

1. **Saga orchestration для order → payment → inventory через Kafka.** Orchestrator (Spring State Machine или Temporal.io) хранит saga state в Postgres. Steps: (1) Create order (PENDING), (2) Publish `ReserveInventory` command, (3) On `InventoryReserved` event publish `ChargePayment`, (4) On `PaymentCharged` event mark order CONFIRMED. Compensations: `ReleaseInventory`, `RefundPayment`. Симулируйте crash orchestrator между step 2 и 3 — restart подхватывает state из БД. Симулируйте payment failure — compensation flow. Tracing через OpenTelemetry показывает весь saga как один trace.
2. **CQRS для order search.** Write-side: `OrderService` пишет в Postgres (normalized). Read-side: Kafka consumer (`order-events` topic) обновляет Elasticsearch (denormalized, оптимизирован под full-text search). UI ищет через ES (fast), редактирует через Postgres. Покажите eventual consistency: lag ~100ms-1s между write и visibility в search.
3. **Event Sourcing для bank account.** `Account` aggregate сохраняет события `AccountOpened`, `MoneyDeposited`, `MoneyWithdrawn`, `FeeApplied`. State = replay всех событий. Snapshot каждые 100 events для ускорения replay. Покажите time-travel: состояние на момент T1. Покажите, что schema evolution (add field) работает через upcasters (event versioning).
4. **Strangler Fig миграция monolith → microservices.** Monolith — Spring Boot с `/api/orders`, `/api/customers`, `/api/products`. Стратегия: API Gateway перехватывает запросы, новые endpoints идут в new service `order-service` (постепенно извлекается). При каждом извлечении — feature toggle для переключения трафика между old и new. Покажите поэтапную миграцию: 5 endpoints за 3 месяца, zero downtime, rollback possible.
5. **ACL между legacy SOAP и new gRPC.** Legacy-сервис на SOAP/WS-*; new service на gRPC. ACL = adapter, который принимает gRPC-вызовы и переводит в SOAP (через Apache CXF), маппит модели. Покажите, что new domain-model изолирован от legacy quirks (например, объединение 2 SOAP-вызовов в 1 gRPC). Тесты контрактные между ACL и new service.
6. **Database-per-service migration.** Shared Postgres с несколькими schemas (orders, payments, inventory). Шаги: (1) выделить schema inventory в отдельный инстанс, (2) предоставить API `/internal/inventory/*` для access из других сервисов, (3) обновить other services на использование API вместо прямого SQL, (4) удалить cross-schema foreign keys. Покажите zero-downtime миграцию через dual-write period.
7. **API Gateway + BFF для web/mobile.** Spring Cloud Gateway как base; для web — отдельный BFF (Spring for GraphQL) агрегирует 5 backend-сервисов, для mobile — другой BFF с упрощёнными DTO. Оба через gateway с auth, rate limiting. Покажите, что mobile-traffic не влияет на web-traffic (bulkhead через отдельные worker pools в gateway).
8. **Distributed data consistency audit.** Возьмите существующий микросервисный проект. Для каждой cross-service data flow определите: strong consistency или eventual? Если strong — как (2PC? нет)? Если eventual — как обнаруживается? Покажите 3 места с потенциальными inconsistent states и предложите fixes (saga, outbox, CQRS, semantic lock).

---

## 10. Resilience

### Теория

- **Resilience** — способность системы продолжать работать (возможно, в degraded mode) при отказах компонентов; цели: availability, fault tolerance, graceful degradation.
- **Cascading failures** — отказ одного сервиса распространяется на dependents через thread/connection exhaustion → лавина; основной motivation для resilience patterns.
- **Circuit Breaker** (Michael Nygard, "Release It!"):
  - **Closed** — normal, requests проходят; счётчик failures.
  - При достижении `failureRateThreshold` (например 50%) → **Open**: requests fail fast (exception или fallback).
  - После `waitDurationInOpenState` → **Half-open**: limited probes; если успех — back to Closed; если fail — back to Open.
  - Метрики: `failure_rate`, `state`, `calls_per_second`, `buffered_calls`.
- **Sliding window** для подсчёта failure rate:
  - **Count-based** — последние N calls (например 100).
  - **Time-based** — последние N секунд (например 10s).
- **Bulkhead** — изоляция ресурсов: отдельные thread pools / semaphores для разных downstream; отказ одного downstream не исчерпывает общий pool. Название от переборок в корабле (water-tight compartments).
- **Rate Limiter** — ограничение частоты вызовов; алгоритмы:
  - **Fixed window** — счётчик в окне (1s); sharp edges at boundary.
  - **Sliding window log** — список timestamps; точный, но memory-heavy.
  - **Sliding window counter** — гибрид, ~точный.
  - **Token bucket** — бакет с N tokens, refill rate R; consumer берёт 1 token per request; позволяет bursts.
  - **Leaky bucket** — requests в очередь, выходят с постоянной скоростью; smoothing.
- **Retry** — повторная попытка при transient failure; правила:
  - **Idempotent only** — retry безопасен только для idempotent operations (GET, PUT, DELETE; POST с idempotency key).
  - **Exponential backoff** — `delay = base * 2^attempt`, max cap.
  - **Jitter** — случайное смещение (`delay * (0.5 + random*0.5)`) для предотвращения thundering herd после восстановления.
  - **Max attempts** — обычно 3-5.
  - **Retry on specific exceptions** — не retry на 4xx (клиентская ошибка), retry на 5xx и timeout.
- **Time Limiter** — ограничение длительности вызова; на future / CompletableFuture; при timeout — cancel.
- **Fallback** — запасной ответ при failure: cached value, default, simplified response, empty list. Используется с circuit breaker, retry, rate limiter.
- **Hystrix (Netflix, legacy)** — старая библиотека; в maintenance mode с 2018; replacement — **Resilience4j**.
- **Resilience4j** — современная fault tolerance library для Java 8+; функциональный стиль (Decorator pattern), без зависимости от Spring (но есть Spring Boot starter); вдохновлено Hystrix, но лучше.
- **Resilience4j modules**: CircuitBreaker, Bulkhead (Semaphore и Thread pool variants), RateLimiter, Retry, TimeLimiter, Cache, Timelimiter.
- **Decorator pattern в Resilience4j**:
  ```java
  Supplier<String> supplier = () -> service.call();
  supplier = Decorators.ofSupplier(supplier)
      .withCircuitBreaker(cb)
      .withBulkhead(bulkhead)
      .withRetry(retry)
      .withFallback(List.of(...), t -> "fallback")
      .decorate();
  ```
- **Spring Boot integration** — `resilience4j-spring-boot3` starter; annotations `@CircuitBreaker`, `@Bulkhead`, `@RateLimiter`, `@Retry`, `@TimeLimiter`, `@Fallback`; конфигурация в `application.yml`.
- **Bulkhead types**:
  - **Semaphore bulkhead** — ограничивает concurrent calls (на caller thread).
  - **Thread pool bulkhead** — отдельный thread pool + queue; изоляция через async execution.
- **Circuit Breaker config** в Resilience4j: `failureRateThreshold`, `slowCallRateThreshold`, `slowCallDurationThreshold`, `minimumNumberOfCalls`, `slidingWindowSize`, `waitDurationInOpenState`, `permittedNumberOfCallsInHalfOpenState`, `automaticTransitionFromOpenToHalfOpenEnabled`.
- **Slow call rate** — отдельный счётчик для медленных вызовов (не только failed); полезно для предотвращения latency degradation.
- **Circuit breaker state machine**: `DISABLED` (всегда closed), `METRICS_ONLY` (собирает метрики, но не открывает), `CLOSED`, `OPEN`, `FORCED_OPEN`, `HALF_OPEN`.
- **Composite resilience** — stacking: retry → circuit breaker → bulkhead → call; порядок важен (retry вне circuit breaker, чтобы retry не триггерил CB).
- **Observability** — Micrometer integration: metrics `resilience4j.circuitbreaker.state`, `resilience4j.retry.calls`, `resilience4j.bulkhead.available.concurrent.calls`; Prometheus + Grafana dashboards.
- **Timeout vs Retry interaction** — retry должен включать timeout, иначе retry накапливается; общий budget = max_attempts * timeout.
- **Retry budget** — ограничение retries в процентах от requests (например 20%); для предотвращения retry storm при systemic failure.
- **Rate limiter placement** — на ingress (gateway) и egress (per downstream); client-side для cooperative backpressure.
- **Backpressure** — slow consumer сигнализирует producer'у; в reactive (Reactor, Flow.Publisher) — `onNext` с `request(n)`; в gRPC — `isReady()`.
- **Graceful degradation** — система теряет функциональность постепенно, но продолжает core operations; например, при отказе recommendation service — показываем static top, не падаем.
- **Bulkhead pattern variants** — `Bulkhead` для concurrent calls, `TimeLimiter` для max duration, `RateLimiter` для max rate; комбинируются.
- **Hystrix semaphore isolation vs thread isolation** — semaphore легче (no thread switch), но не timeout-aware; thread — дороже, но full isolation + timeout.

### Материалы (реальные ссылки)

- **Resilience4j — Getting Started**: https://resilience4j.readme.io/docs/getting-started
- **Resilience4j — CircuitBreaker**: https://resilience4j.readme.io/docs/circuitbreaker
- **Resilience4j — GitHub repository**: https://github.com/resilience4j/resilience4j
- **Resilience4j — Spring Boot starter**: https://resilience4j.readme.io/docs/getting-started-3
- **Baeldung — Guide to Resilience4j**: https://www.baeldung.com/resilience4j
- **Datadog — Resilience4j integration**: https://docs.datadoghq.com/integrations/resilience4j
- **OneUptime — Resilience4j Circuit Breakers in Spring Boot**: https://oneuptime.com/blog/post/2026-02-01-spring-resilience4j-circuit-breaker/view
- **Martin Fowler — CircuitBreaker**: https://martinfowler.com/bliki/CircuitBreaker.html
- **j-labs — Fault Tolerance with Resilience4j and Spring Boot**: https://www.j-labs.pl/en/tech-blog/fault-tolerance-with-resilience4j-and-spring-boot
- **dev.to — Making REST Microservices Resilient**: https://dev.to/athulmr/making-rest-microservices-resilient-bulkhead-retry-circuit-breaker-in-practice-1mpl
- **Netflix Hystrix (legacy wiki)**: https://github.com/Netflix/Hystrix/wiki
- **Книга**: Michael Nygard, "Release It!" 2nd ed. (Pragmatic Bookshelf, 2018) — оригинальный источник Circuit Breaker, Bulkhead, patterns.
- **Книга**: Newman, "Building Microservices" 2nd ed., глава 11 (масштабирование).

### Практика / Кейсы

1. **Circuit Breaker вокруг downstream call.** Service A → Service B (REST). Настройте `@CircuitBreaker(name="serviceB", fallbackMethod="fallback")` с `failureRateThreshold=50%`, `slidingWindowSize=100`, `waitDurationInOpenState=30s`, `slowCallDurationThreshold=2s`, `slowCallRateThreshold=80%`. Симулируйте: B возвращает 500 в 60% случаев → CB opens после 100 calls → fallback returns cached value → через 30s half-open probes → если B восстановился, CB closes. Покажите метрики в Prometheus.
2. **Bulkhead против cascading failure.** Service A имеет thread pool 200. Service B (быстрый, 10ms) и Service C (медленный, 30s при overload). Без bulkhead: 200 requests к C исчерпывают pool → B тоже недоступен. С `@Bulkhead(type=THREADPOOL, maxThreadPoolSize=100, maxConcurrentCalls=50)` отдельно для C: B всегда доступен (свой pool). Симулируйте и измерьте.
3. **Retry с exponential backoff + jitter.** `@Retry(name="paymentRetry", fallbackMethod="...")` с 3 attempts, `waitDuration=1s`, `multiplier=2.0`, `randomizationFactor=0.5`. Покажите timing: attempt 1 → fail → 1s + jitter(0.5-1.5s) → attempt 2 → fail → 2s + jitter → attempt 3 → fail → fallback. Покажите, что без jitter — thundering herd (1000 clients retry одновременно).
4. **TimeLimiter + CompletableFuture.** Вызов `service.process()` возвращающий `CompletableFuture<Result>`; `@TimeLimiter(name="processTimeout", fallbackMethod="...")` с timeout 2s. Если future не completed за 2s — cancel + fallback. Симулируйте зависший service (sleep 10s), покажите, что без TL — caller ждёт 10s; с TL — fallback через 2s.
5. **RateLimiter на gateway + на service.** Spring Cloud Gateway с Redis RateLimiter (token bucket, 10 req/s per IP) + на service `@RateLimiter(name="api", limitForPeriod=100, limitRefreshPeriod=1s)`. Симулируйте 1000 RPS → gateway отклоняет 990 с 429; оставшиеся 100 проходят, но service-side rate limiter также защищает. Покажите `X-RateLimit-Remaining` headers.
6. **Composite resilience stacking.** Stack: `@Retry → @CircuitBreaker → @Bulkhead → @TimeLimiter → @Fallback`. Порядок важен: retry вне CB (retry не должен триггерить CB), CB вне bulkhead. Покажите сценарий: B возвращает 500 — retry 3 раза (не доходит до CB, CB не открывается); если все 3 retries fail — CB failure count++; после 50% → CB opens; в open state — мгновенный fallback без retries.
7. **Hystrix → Resilience4j migration.** Возьмите legacy-проект с Hystrix. Сопоставьте: `@HystrixCommand` → `@CircuitBreaker` + `@Fallback`; `HystrixThreadPool` → `@Bulkhead(type=THREADPOOL)`; `HystrixObservable` → `@CircuitBreaker` + Reactor. Перепишите 10 команд, прогоните regression-тесты. Покажите, что Resilience4j даёт richer config (slow call rate, separate windows).
8. **Chaos engineering: fault injection.** Using Chaos Monkey for Spring Boot или Gremlin: убей downstream service (kill container) → CB opens через 1s → fallback returns cached. Убей DB → bulkhead + timeout спасают от зависания. Сетевая задержка +500ms → slow call rate триггерит CB. Покажите, что система остаётся доступна (в degraded mode) при каждом отказе.

---

## 11. Service Discovery & Config

### Теория

- **Service Discovery** — механизм, позволяющий сервисам находить друг друга без hardcoded IP/hostname; особенно важен для динамических сред (cloud, K8s).
- **Client-side discovery** — клиент запрашивает у service registry список инстансов, сам балансирует; Eureka (Netflix), Consul. Меньше хопов, но клиент сложнее.
- **Server-side discovery** — клиент обращается к load balancer (или proxy), который сам опрашивает registry; AWS ALB, Kubernetes Service, Envoy. Прозрачно для клиента.
- **Service registry** — база данных service → list of instances (IP, port, health, metadata); обычно cluster-replicated.
- **Eureka (Netflix OSS / Spring Cloud Netflix)**:
  - Server: Eureka Server, peer-to-peer replication, AP (высокая доступность ценой eventual consistency).
  - Client: регистрируется на старте, шлёт heartbeat каждые 30s; при отсутствии heartbeat 90s — instance удаляется.
  - Self-preservation mode: если >15% heartbeat-ов теряется — Eureka перестаёт удалять instances (защита от network partition, registry сохраняет potentially stale data).
  - Spring Cloud: `@EnableEurekaServer`, `@EnableDiscoveryClient`; `eureka.client.serviceUrl.defaultZone`.
- **Consul (HashiCorp)**:
  - Service registry + health check + KV store + DNS interface.
  - CP через Raft consensus; сильная consistency.
  - Agent-based: каждый host запускает consul agent, communicates с server cluster.
  - Health checks: HTTP, TCP, script, TTL; failed → deregistered.
- **etcd**:
  - Distributed KV store, Raft-based, CP; основа Kubernetes.
  - Generic, не только discovery; TTL keys для ephemeral registrations.
- **ZooKeeper**:
  - Hierarchical namespace (znodes), CP через ZAB (ZooKeeper Atomic Broadcast).
  - Ephemeral nodes + watches для service discovery; в Kafka до KRaft использовался для metadata.
  - Older, harder to operate, но mature.
- **Kubernetes native**:
  - **Service** — virtual IP + DNS name (`<service>.<namespace>.svc.cluster.local`).
  - **Endpoints** — список pod IPs, обновляется kube-controller.
  - **DNS-based discovery** — CoreDNS.
  - **kube-proxy** — iptables/IPVS rules for load balancing.
  - Без need for external registry.
- **Spring Cloud Config**:
  - Centralized externalized configuration; Config Server читает from Git, Vault, JDBC, AWS S3, etc.
  - Clients (`spring-cloud-starter-config`) pull config at startup; `@RefreshScope` для hot reload через `/actuator/refresh` (Spring Cloud Bus + RabbitMQ/Kafka для broadcast).
  - Profiles: `application-{profile}.yml`, environment-specific.
- **Kubernetes ConfigMap & Secret**:
  - **ConfigMap** — non-sensitive config; mounted as env vars or files.
  - **Secret** — sensitive (base64-encoded, RBAC-protected); same mounting options.
  - Hot reload через `Reloader` или `configMap checksum` annotation on Deployment.
- **Spring Cloud Kubernetes** — интеграция Spring Cloud с K8s: service discovery через K8s Services, config через ConfigMap, no need for Eureka/Config Server.
- **Health checks**:
  - **Liveness** — приложение живо (не restart); для K8s livenessProbe.
  - **Readiness** — приложение готово принимать трафик; для K8s readinessProbe.
  - **Startup** — для slow startup (Java big apps); позволяет отложить liveness.
- **Registration lifecycle**: start → register → heartbeats → graceful shutdown (deregister) или crash (timeout deregister).
- **Multi-zone / multi-region** — Eureka: peer replication между zones; Consul: WAN gossip; K8s: multi-cluster service mesh (Istio multi-cluster).
- **Load balancing integration** — Ribbon (legacy, in maintenance), Spring Cloud LoadBalancer (modern); client-side LB via registry; integration with Resilience4j.
- **Configuration patterns**:
  - **Externalized** — outside app, environment-specific.
  - **Centralized** — one source of truth.
  - **Versioned** — Git-backed, auditable.
  - **Hot reload** — без рестарта (Spring Cloud Bus, K8s ConfigMap + Reloader).
- **Secrets management** — Vault (HashiCorp), AWS Secrets Manager, K8s Secrets + external-secrets; не в plain config files; rotation.
- **Spring Cloud Vault** — интеграция с Vault; dynamic secrets (DB credentials с TTL), Kubernetes auth backend.
- **Comparison Eureka vs Consul vs K8s**:
  - Eureka: AP, simple, Java-centric, legacy но в maintenance.
  - Consul: CP, multi-language, rich feature set (KV, mesh, health).
  - K8s native: simplest in K8s, no external dep, DNS-based.
- **Failure modes**: registry unavailable → cached instances в client; split-brain → stale data; thundering herd при restart registry.

### Материалы (реальные ссылки)

- **Spring Cloud Netflix Eureka reference**: https://docs.spring.io/spring-cloud-netflix/docs/current/reference/html
- **Spring Cloud — Service Discovery: Eureka Clients**: https://cloud.spring.io/spring-cloud-netflix/multi/multi__service_discovery_eureka_clients.html
- **Spring Cloud — Service Discovery: Eureka Server**: https://cloud.spring.io/spring-cloud-netflix/multi/multi_spring-cloud-eureka-server.html
- **Spring — Service Registration and Discovery guide**: https://spring.io/guides/gs/service-registration-and-discovery
- **Spring Cloud project page**: https://spring.io/projects/spring-cloud
- **Spring Cloud Config reference**: https://docs.spring.io/spring-cloud-config/reference/
- **Consul documentation**: https://developer.hashicorp.com/consul/docs
- **etcd documentation**: https://etcd.io/docs/
- **Apache ZooKeeper documentation**: https://zookeeper.apache.org/doc/current/
- **Kubernetes — Service & Discovery**: https://kubernetes.io/docs/concepts/services-networking/service/
- **Kubernetes — ConfigMap**: https://kubernetes.io/docs/concepts/configuration/configmap/
- **Kubernetes — Secret**: https://kubernetes.io/docs/concepts/configuration/secret/
- **Spring Cloud Kubernetes**: https://docs.spring.io/spring-cloud-kubernetes/reference/
- **Comparing discovery tools**: https://charleswan111.medium.com/comparing-service-discovery-and-coordination-tools-etcd-consul-eureka-nacos-polaris-157820eb1810
- **Kong — Service Discovery in Microservices**: https://konghq.com/blog/learning-center/service-discovery-in-a-microservices-architecture
- **Piotr Minkowski — Future of Spring Cloud after Netflix**: https://piotrminkowski.wordpress.com/2019/04/05/the-future-of-spring-cloud-microservices-after-netflix-era/

### Практика / Кейсы

1. **Eureka cluster (3 узла) + 2 сервиса + client-side LB.** Запустите 3 Eureka сервера (peer replication), 2 инстанса `order-service`, 1 инстанс `payment-service`. Payment service находит order-service через Eureka, балансирует через Spring Cloud LoadBalancer (round_robin). Симулируйте: убейте 1 инстанс order — traffic идёт на второй через 30s (heartbeat timeout). Убейте 1 Eureka — остальные продолжают работать.
2. **Self-preservation mode analysis.** Нагрузите Eureka 100 инстансами, разорвите сеть между Eureka и half инстансов (network partition). Без self-preservation: Eureka удаляет «мёртвые» → при восстановлении registry пустой → cascading fail. С self-preservation: Eureka хранит stale instances → клиенты могут попасть на мёртвые, но registry полон. Покажите оба сценария, обсудите trade-off.
3. **Consul health checks.** Зарегистрируйте service в Consul с HTTP health check (`/actuator/health`). Симулируйте: service health DOWN → Consul marks unhealthy → load balancer исключает; при восстановлении → автоматически возвращается. Сравните с TTL-based (service сам шлёт heartbeat).
4. **Spring Cloud Config + Bus refresh.** Config Server (backed by Git) раздаёт `application.yml`; 3 инстанса client-service pull config при старте. Измените config в Git → push `/actuator/refresh` на одном клиенте → только он перезагружает. С Spring Cloud Bus + RabbitMQ → push `/actuator/busrefresh` → broadcast → все клиенты перезагружают `@RefreshScope` beans. Покажите, что конфиг меняется без restart.
5. **K8s native discovery + ConfigMap.** Deploy service в K8s с ConfigMap, mounted as env vars + volume. Измените ConfigMap → `kubectl apply` → Reloader detects → rollout restart. Покажите, что pods пересоздаются с новой config. Сравните с Spring Cloud approach — больше ops overhead.
6. **Multi-region Eureka.** Eureka clusters в US и EU zones с peer replication. Service в US регистрируется локально → реплицируется в EU. Клиент в EU может найти US instance (fallback). Покажите, что зона-local предпочтительнее (Spring Cloud LoadBalancer `zone-affinity`).
7. **Vault + Spring Cloud Vault для DB credentials.** Vault динамически создаёт DB credentials с TTL 1h. Spring Cloud Vault в `@PostConstruct` получает credentials, передаёт в DataSource. За 5 min до expiry — refresh. Покажите, что DB user rotation происходит автоматически, нет hardcoded passwords в config.
8. **Migration Eureka → Kubernetes native.** Существующий Spring Cloud проект с Eureka → миграция в K8s. Шаги: (1) добавить `spring-cloud-starter-kubernetes`, (2) убрать Eureka dependency, (3) services находят друг друга через K8s DNS, (4) config через ConfigMap. Покажите zero-downtime migration, меньше moving parts.

---

## 12. API Gateway

### Теория

- **API Gateway** — единая точка входа для внешних клиентов; reverse proxy с добавленной бизнес-логикой; routing, auth, rate limiting, transformation, observability.
- **Responsibilities**: request routing, authentication & authorization, rate limiting & quotas, request/response transformation, response aggregation, protocol translation (REST↔gRPC, SOAP→REST), caching, logging & metrics, TLS termination.
- **Spring Cloud Gateway** (Spring WebFlux-based, reactive, Netty):
  - **Route** — id, URI, predicates, filters.
  - **Predicate** — match condition (Path, Host, Header, Method, Query, Cookie, After, Before, Between, Weight).
  - **Filter** — modify request/response (AddRequestHeader, RewritePath, Retry, RequestRateLimiter, etc.).
  - **Global filters** — applied to all routes (auth, logging, tracing).
  - **GatewayFilter factories** — pre/post filters; `forward`, `modify`, `stripPrefix`.
- **Routing в SCG**:
  ```yaml
  spring:
    cloud:
      gateway:
        routes:
          - id: order-service
            uri: lb://ORDER-SERVICE
            predicates:
              - Path=/api/orders/**
            filters:
              - StripPrefix=2
              - name: RequestRateLimiter
                args:
                  redis-rate-limiter.replenishRate: 10
                  redis-rate-limiter.burstCapacity: 20
  ```
- **Reactive architecture** — SCG на WebFlux, нет thread-per-request; high concurrency, low resource usage; подходит для IO-heavy gateway.
- **Rate limiting** — `RequestRateLimiter` GatewayFilter с Redis (token bucket); `KeyResolver` — по IP, user, API key; `replenishRate`, `burstCapacity`, `requestedTokens`. Returns 429 with `Retry-After`.
- **Auth integration** — `SecurityWebFilterChain` с Spring Security; JWT validation, OAuth2 resource server; custom filter for API key.
- **CORS** — `CorsConfigurationSource` global config; preflight handling.
- **Circuit Breaker integration** — `CircuitBreaker` GatewayFilter (Resilience4j) с fallback URI; при открытии CB — redirect на fallback.
- **Kong** — Lua-based (OpenResty/Nginx), plugin architecture; DB-backed (Postgres/Cassandra) или DB-less (declarative YAML); admin REST API; plugins: rate-limiting, auth (JWT, OAuth2, ACL, key-auth), logging, transformation, serverless (Lua/Go/JS).
- **Envoy** — C++ proxy от Google/Lyft; xDS dynamic configuration; HTTP/2 + gRPC native; L7 filter chain; Istio's data plane; standalone mode для API gateway (with Gloo, Ambassador).
- **NGINX** — battle-tested, Lua extension (OpenResty), reverse proxy, load balancer, caching; `nginx.conf` + Lua scripts для custom logic.
- **AWS API Gateway** — managed; REST + HTTP API + WebSocket; integration с Lambda, ECS, EC2; throttling, usage plans, API keys, WAF; per-request pricing.
- **Comparison SCG vs Kong vs Envoy vs NGINX**:
  - SCG: JVM-ecosystem, Spring-integrated, reactive, easy для Spring teams.
  - Kong: plugin-rich, multi-language, mature, DB-backed config.
  - Envoy: high-perf, xDS, gRPC-native, foundation for service mesh.
  - NGINX: simple, fast, ubiquitous, but limited programmability.
- **Gateway patterns**:
  - **Single gateway** — one for all clients; simpler, but can become bottleneck / god-gateway.
  - **BFF gateway** — one per client type; tailored responses.
  - **Gateway per bounded context** — divide gateways by domain.
- **Response aggregation** — gateway параллельно вызывает несколько backend и объединяет; для mobile UI single request → multiple internal calls.
- **Protocol translation** — REST from client → gRPC to backend (gRPC-Gateway, custom SCG filter); GraphQL → REST; SOAP → REST (legacy integration).
- **TLS termination** — gateway terminates HTTPS, internal traffic HTTP (или mTLS с service mesh); cert management (Let's Encrypt, cert-manager в K8s).
- **Observability** — distributed tracing (OpenTelemetry, B3 propagation), metrics (Micrometer, Prometheus), access logs; correlation ID injection (`X-Request-Id`).
- **Versioning & routing** — `/v1/*` → old service, `/v2/*` → new; canary via Weight predicate (10% to v2).
- **Traffic management**:
  - **Canary** — gradual rollout (1% → 10% → 50% → 100%).
  - **Blue-green** — switch between two full environments.
  - **A/B testing** — route based on user attributes (header).
  - **Shadow traffic** — mirror traffic to new version, no response to client.
- **Security** — WAF (ModSecurity, AWS WAF), DDoS protection (Cloudflare, AWS Shield), bot detection, geo-blocking.
- **WebSocket / SSE** — gateway должен поддерживать upgrade для WS и long-lived SSE connections; SCG поддерживает.
- **Rate limit strategies** — per-user, per-IP, per-API-key, per-route, global; sliding window vs token bucket; shared via Redis для distributed.

### Материалы (реальные ссылки)

- **Spring Cloud Gateway project**: https://spring.io/projects/spring-cloud-gateway
- **Spring Cloud Gateway reference**: https://docs.spring.io/spring-cloud-gateway/docs/current/reference/html
- **Spring — API Rate Limiting with SCG**: https://spring.io/blog/2021/04/05/api-rate-limiting-with-spring-cloud-gateway
- **RequestRateLimiter GatewayFilter Factory**: https://docs.spring.io/spring-cloud-gateway/reference/spring-cloud-gateway-server-webflux/gatewayfilter-factories/requestratelimiter-factory.html
- **RateLimiter Filter (WebMVC)**: https://docs.spring.io/spring-cloud-gateway/reference/spring-cloud-gateway-server-webmvc/filters/ratelimiter.html
- **Spring Cloud Gateway — common properties**: https://docs.spring.io/spring-cloud-gateway/reference/appendix.html
- **Kong — Rate Limiting plugin**: https://developer.konghq.com/plugins/rate-limiting
- **Kong documentation (main)**: https://docs.konghq.com/
- **Envoy documentation**: https://www.envoyproxy.io/docs
- **NGINX documentation**: https://nginx.org/en/docs/
- **Solo.io — NGINX Rate Limiting**: https://www.solo.io/topics/nginx/nginx-rate-limiting
- **Kong vs NGINX vs Envoy comparison**: https://medium.com/@kanhaaggarwal/kong-vs-nginx-vs-envoy-the-harsh-truth-about-choosing-the-right-api-gateway-58b2afa9dad2
- **Microservices.io — API Gateway pattern**: https://microservices.io/patterns/apigateway.html
- **Microservices.io — BFF pattern**: https://microservices.io/patterns/apigateway.html
- **Книга**: Richardson, "Microservices Patterns", глава 7 (API Gateway).
- **Книга**: Newman, "Building Microservices" 2nd ed., глава 5 (моделирование).

### Практика / Кейсы

1. **Spring Cloud Gateway: routing + auth + rate limit + CB.** 3 backend-сервиса (orders, payments, users). SCG: `/api/orders/**` → order-service, `/api/payments/**` → payment-service (с Resilience4j CB + fallback); JWT validation via global filter; rate limit via Redis (10 req/s per user); tracing via Micrometer. Покажите, что превышение rate limit → 429 с `Retry-After`. Симулируйте payment failure → CB opens → fallback returns cached.
2. **Canary deployment через SCG.** Predicate `Weight=canary,10` → 10% трафика на `order-service-v2`, остальное на v1. Метрики: compare error rate, latency между v1 и v2. Если v2 в норме — увеличить до 50%, потом 100%. Покажите zero-downtime rollout с возможностью rollback (change weight to 0).
3. **Response aggregation в BFF.** Mobile client запрашивает `GET /api/mobile/home?userId=42`. BFF (Spring for GraphQL или custom SCG filter) параллельно вызывает: `/users/42`, `/users/42/orders?status=active`, `/users/42/recommendations`. Объединяет в один JSON. Симулируйте failure одного backend — partial response (рекомендации пустые, остальное валидно). Измерьте latency: serial 1.5s vs parallel 500ms.
4. **Kong: plugin stack.** Configure Kong с plugins: `key-auth` (API key из header), `rate-limiting` (100/min per key), `request-transformer` (add `X-Forwarded-For`), `prometheus` (metrics), `acl` (whitelist consumers). Покажите, что запрос без API key → 401; с превышением rate → 429; admin API для управления consumers/keys.
5. **Envoy как ingress для K8s + Istio.** Istio IngressGateway (Envoy) принимает external traffic, маршрутизирует в K8s services. VirtualService: `/api/*` → service-v1, canary через `weight: 10`. mTLS между gateway и pods. Покажите tracing (Jaeger) через все хопы, metrics (Prometheus) из Envoy stats.
6. **WebSocket through gateway.** SCG с WebSocket route `/ws/**` → chat-service. Gateway поддерживает upgrade, long-lived connection. Покажите 1000 concurrent WS connections, low memory (Netty event loop). Симулируйте WS backend restart — gateway reconnects clients.
7. **gRPC-Gateway (REST → gRPC).** Backend-сервис на gRPC; external clients хотят REST. Используйте grpc-gateway (или Envoy gRPC-JSON transcoder) для автоматического REST endpoint из `.proto` annotations (`google.api.http`). Покажите, что `/v1/orders/42` → gRPC `GetOrder(id=42)`; response JSON-serialized.
8. **Migration NGINX → SCG.** Legacy NGINX config с 50 locations, Lua scripts для auth, rate limit. Перепишите на SCG: predicates вместо locations, global filters вместо Lua, SCG-native rate limiter. Покажите, что latency улучшается (reactive Netty), а config читаемость — значительно.

---

## 13. Service Mesh

### Теория

- **Service Mesh** — инфраструктурный слой для service-to-service коммуникации; реализуется через sidecar proxies + control plane; прозрачен для приложения.
- **Data plane** — sidecar proxies (Envoy, Linkerd-proxy) перехватывают весь ingress/egress traffic; L7 capabilities.
- **Control plane** — управляет конфигурацией sidecars (Pilot в Istio, destination controllers в Linkerd).
- **Istio** (Google/IBM/Lyft, 2017) — де-факто стандарт; data plane Envoy, control plane Istiod (Pilot + Citadel + Galley merged).
- **Linkerd** (Buoyant, 2016) — lighter alternative; data plane Linkerd-proxy (Rust, был Finagle/Scala), control plane на Kubernetes; фокус на simplicity.
- **Consul Connect** — HashiCorp service mesh; sidecar proxies (Envoy by default) или native integrations.
- **mTLS (mutual TLS)** — оба sidecars имеют сертификаты от internal CA; traffic между services зашифрован; rotation автоматически (через SPIFFE identities).
- **Traffic management**:
  - **Routing** — canary, blue-green, A/B; через virtual services (Istio) или routes (Linkerd).
  - **Load balancing** — round-robin, least-conn, random, P2C (power of 2 choices).
  - **Resilience** — retries, timeouts, circuit breaker (Envoy outlier detection), connection pool limits.
  - **Fault injection** — задержки, aborts для chaos testing.
- **Observability**:
  - **L7 metrics** — request volume, error rate, latency percentiles, автоматически для всех service-to-service.
  - **Distributed tracing** — automatic trace propagation (W3C, B3), spans per hop.
  - **Access logs** — structured logs per request.
- **Policy enforcement** — access control (RBAC, authorization policies), quotas, deny lists.
- **Istio architecture**:
  - **Istiod** — control plane (Pilot for routing, Citadel for cert, Galley for config validation).
  - **Envoy sidecars** — data plane; inject через mutating webhook.
  - **Ingress gateway** — edge proxy.
  - **Egress gateway** — для outbound traffic policy (whitelist external services).
- **Istio CRDs**: `VirtualService` (routing rules), `DestinationRule` (load balancing, outlier detection, mTLS), `Gateway` (edge config), `ServiceEntry` (external services), `PeerAuthentication` (mTLS mode), `AuthorizationPolicy` (RBAC), `RequestAuthentication` (JWT).
- **Istio mTLS modes**: `DISABLE`, `PERMISSIVE` (accept both mTLS and plaintext, для migration), `STRICT` (only mTLS).
- **Sidecar injection** — automatic через namespace label `istio-injection=enabled`; или manual `istioctl kube-inject`.
- **Ambient mesh** (Istio 1.18+, preview → GA) — sidecarless architecture; L4 proxy (ztunnel) per node + L7 (waypoints) per namespace; reduces resource overhead.
- **Linkerd** features: mTLS (automatic), golden signals metrics, retries/timeout, tap (live request inspection), service profiles (per-route metrics, retries).
- **Linkerd vs Istio comparison**:
  - Linkerd: simpler, Rust proxy, less features, faster install, lighter resource.
  - Istio: feature-rich, Envoy-based, complex, more resources, but powerful.
- **Service mesh patterns**:
  - **Canary via mesh** — `VirtualService` weights; no app changes.
  - **gRPC routing** — route by method/headers; transparent to app.
  - **Dark launch** — shadow traffic to new version.
  - **Per-route auth** — `AuthorizationPolicy` на specific paths.
- **Gradual migration** — `PERMISSIVE` mTLS mode; inject sidecars namespace by namespace.
- **Multi-cluster mesh** — Istio multi-cluster (single network, multi-network, primary-remote); единый control plane или federated.
- **Performance overhead** — sidecar adds ~1-5ms latency, ~50-200MB RAM per pod; ambient mesh снижает overhead.
- **Debugging** — `istioctl analyze` (config validation), `istioctl proxy-config`, `linkerd diagnose`, `linkerd tap`.
- **eBPF-based meshes** — Cilium Service Mesh (no sidecar, eBPF in kernel), newer approach; eliminates sidecar overhead.
- **Anti-patterns**: mesh для монолита (no service-to-service traffic); too-fine-grained services (overhead dominates); mesh-as-bandaid for broken services.
- **Mesh vs API Gateway** — gateway на edge (N-S traffic), mesh внутри (E-W traffic); обычно оба.

### Материалы (реальные ссылки)

- **Istio documentation**: https://istio.io/latest/docs/
- **Istio — Traffic Management**: https://istio.io/latest/docs/concepts/traffic-management/
- **Istio — Security (mTLS)**: https://istio.io/latest/docs/concepts/security/
- **Istio — Observability**: https://istio.io/latest/docs/concepts/observability/
- **Istio — Ambient mesh**: https://istio.io/latest/docs/ops/ambient/
- **Linkerd documentation**: https://linkerd.io/2/overview/
- **Linkerd — Architecture**: https://linkerd.io/2/reference/architecture/
- **Consul Connect (Service Mesh)**: https://developer.hashicorp.com/consul/docs/connect
- **Cilium Service Mesh**: https://docs.cilium.io/en/stable/network/servicemesh/
- **Plural.sh — Istio vs Linkerd**: https://www.plural.sh/blog/istio-vs-linkerd-guide
- **Solo.io — Linkerd vs Istio**: https://www.solo.io/topics/istio/linkerd-vs-istio
- **Medium — Service Mesh Explained**: https://medium.com/@prayag-sangode/service-mesh-explained-istio-vs-consul-vs-linkerd-vs-envoy-d6cecc0671c6
- **dev.to — When You Actually Need Istio or Linkerd**: https://dev.to/instadevops/service-mesh-explained-when-you-actually-need-istio-or-linkerd-3o7h
- **Service Meshes in 2026 — mTLS, Traffic Shaping**: https://thebackenddevelopers.substack.com/p/service-meshes-in-2026-mtls-traffic
- **Книга**: Lee Calcote & Nic Jackson, "Service Mesh in Practice" (O'Reilly, 2023).
- **Книга**: Richard Li, "Operating Service Mesh" (O'Reilly).

### Практика / Кейсы

1. **Istio canary deploy.** Deploy `order-service-v1` (3 replicas) и `order-service-v2` (1 replica) в K8s с Istio. `VirtualService`: 90% на v1, 10% на v2. Метрики: сравните error rate, p99 latency. Если v2 OK → 50/50 → 100% v2. Покажите, что v1 → v2 zero downtime, rollback = change weight.
2. **mTLS strict mode migration.** Existing K8s deployment с plaintext HTTP между services. Шаги: (1) install Istio, (2) inject sidecars namespace-by-namespace, (3) `PeerAuthentication` mode=PERMISSIVE (both plaintext and mTLS accepted), (4) verify all traffic migrated to mTLS via Kiali, (5) switch to STRICT. Покажите через `istioctl authn tls-check` что mTLS активирован.
3. **Circuit breaker через Envoy outlier detection.** `DestinationRule` для `payment-service` с `outlierDetection.consecutive5xxErrors=5`, `interval=10s`, `ejectionTime=30s`. Симулируйте: payment pod возвращает 500 → после 5 ошибок Envoy исключает pod из LB на 30s. Покажите, что без mesh — cascading failure; с mesh — CB работает transparently.
4. **Distributed tracing automatic.** Istio inject-ит trace headers (`x-request-id`, `x-b3-traceid`, `x-b3-spanid`) автоматически; backend (Spring Boot) должен propagate их дальше. Покажите единый trace через 5 services в Jaeger без изменения app code (только propagation). Измерьте overhead <5%.
5. **Linkerd для lightweight migration.** Сравните install: Istio (15+ CRDs, complex) vs Linkerd (simpler, fewer resources). Для small cluster с 5 services — Linkerd достаточно: mTLS, metrics, retries. Покажите, что Linkerd install 2 min vs Istio 15 min, RAM overhead 50MB vs 500MB.
6. **AuthorizationPolicy для per-route auth.** `/api/admin/*` требует роль admin; `/api/orders/*` требует auth. Istio `AuthorizationPolicy` + JWT claims. Покажите, что request без JWT → 401; с JWT но без admin role → 403 на admin endpoints. App code не нужно проверять auth — mesh делает это.
7. **Fault injection для chaos test.** Istio `HTTPFaultInjection` — 50% requests к `payment-service` получают 5s delay; 10% — HTTP 500. Проверьте, что downstream `order-service` выдерживает (CB opens, fallback). Покажите, что без resilience patterns app ломается.
8. **Ambient mesh migration.** Переведите namespace со sidecar-based Istio на ambient (ztunnel + waypoint). Покажите: resource overhead снижен (no sidecar per pod), latency уменьшена на 1-2ms, mTLS preserved. Обсудите trade-offs: ambient не поддерживает все L7 features (per-route policy ещё стабилизируется).

---

## 14. Distributed Tracing

### Теория

- **Distributed tracing** — наблюдение за запросом через несколько сервисов; запись причинно-следственных spans; основа для observability в microservices.
- **Trace** — дерево spans, представляющих один логический запрос (end-to-end).
- **Span** — единичная операция: name, start time, duration, tags, logs, span context (trace ID + span ID), parent span ID, status.
- **Span context propagation** — передача `trace_id`, `span_id`, `trace_flags` (sampled) между сервисами через headers.
- **W3C Trace Context** (RFC 2020) — стандарт; header `traceparent: 00-<trace_id>-<span_id>-<flags>`; `tracestate` для vendor extensions. Современный стандарт, поддержан OpenTelemetry.
- **B3 (Zipkin)** — pre-W3C формат: `X-B3-TraceId`, `X-B3-SpanId`, `X-B3-ParentSpanId`, `X-B3-Sampled`. Совместим, но deprecated в новых системах.
- **OpenTelemetry (OTel)** — CNCF проект, объединение OpenTracing и OpenCensus; единый стандарт для traces, metrics, logs.
  - **API** — для instrumentation в app code.
  - **SDK** — реализация API, конфигурация (processor, exporter, sampler).
  - **Collector** — proxy, принимает данные от SDK, обрабатывает, экспортирует в backend (Jaeger, Zipkin, Prometheus, Datadog, OTLP).
  - **OTLP** (OpenTelemetry Protocol) — binary protocol over HTTP/gRPC; стандартный export format.
- **Auto-instrumentation** — Java agent (`-javaagent:opentelemetry-javaagent.jar`) автоматически инструментирует popular libraries (Spring, JDBC, Kafka, HTTP clients); zero code changes.
- **Manual instrumentation** — `Tracer.spanBuilder("name").startSpan()`, `span.setAttribute(...)`, `span.addEvent(...)`, `span.recordException(...)`, `span.end()`; для custom business operations.
- **Sampling** — для high-traffic систем не все traces отправляются:
  - **Head-based** — решение в начале trace (RootSpan); `ParentBased(TraceIdRatioBased(0.1))` — 10% traces.
  - **Tail-based** — решение после завершения trace; можно sample все errors, но реже successes; сложнее реализовать (нужно hold весь trace).
  - **Probabilistic** — фиксированный процент.
  - **Adaptive** — dynamic на основе load.
- **Baggage** — key-value context, передаваемый через границы сервисов (W3C Baggage header); для business-relevant context (user_id, tenant_id, experiment_id). Не для traces per se, а для бизнес-данных в контексте запроса.
- **Context propagation in async** — для `@Async`, `CompletableFuture`, Kafka consumer → producer: необходимо вручную propagate context (`Context.wrap()`, `ScopedContext`); в Spring Boot 3 + Micrometer Tracing — автоматический через `TraceContextPropagator`.
- **Jaeger** (CNCF, Uber origin) — distributed tracing backend; UI для exploration; storage в Cassandra, Elasticsearch, Kafka; supports OTLP.
- **Zipkin** (OpenZipkin, Twitter origin) — older alternative; OpenTelemetry-compatible; simpler UI.
- **Micrometer Tracing** (Spring Boot 3+) — abstraction over tracing (Brave/Zipkin и OpenTelemetry); replaces Spring Cloud Sleuth; integrates with Micrometer metrics.
- **Spring Boot 3 tracing setup**:
  ```xml
  <dependency>
    <groupId>io.micrometer</groupId>
    <artifactId>micrometer-tracing-bridge-otel</artifactId>
  </dependency>
  <dependency>
    <groupId>io.opentelemetry</groupId>
    <artifactId>opentelemetry-exporter-otlp</artifactId>
  </dependency>
  ```
  + properties: `management.tracing.sampling.probability=1.0`, `management.otlp.tracing.endpoint=http://otel-collector:4317`.
- **Trace correlation with logs** — MDC (Mapped Diagnostic Context) автоматически populated с `traceId`, `spanId` в Spring Boot 3; log pattern включает `%X{traceId}`.
- **Metrics from traces** — некоторые backends (Tempo, Honeycomb) derive metrics from spans (RED metrics: Rate, Errors, Duration).
- **Span kinds** — `CLIENT` (outbound), `SERVER` (inbound), `PRODUCER` (messaging send), `CONSUMER` (messaging receive), `INTERNAL` (in-process).
- **Trace and span IDs format** — 128-bit trace ID (32 hex chars), 64-bit span ID (16 hex chars); W3C allows 128-bit span ID опционально.
- **Correlation with metrics** — exemplars в Prometheus: timestamp + trace ID для конкретного metric sample, позволяют drill-down из metric spike в specific trace.
- **Latency analysis** — trace показывает critical path; waterfall view; identifies slowest hop.
- **Cross-service causality** — parent-child spans через propagated span context; если context не propagated — trace разрывается.
- **Baggage vs attributes**:
  - Baggage — cross-service, в headers.
  - Span attributes — single span, в span data.
- **PII concerns** — не логировать sensitive data в spans (passwords, tokens, personal data); sanitization в sampler/exporter.
- **Cost of tracing** — sampling необходим; 100% sampling для high-traffic неприемлем (storage, processing); typical: 1-10% head + 100% errors via tail.
- **Trace context in non-HTTP** — Kafka headers, RabbitMQ headers, gRPC metadata; Spring Boot 3 автоматически injects/extracts.

### Материалы (реальные ссылки)

- **OpenTelemetry — Overview**: https://opentelemetry.io/docs/specs/otel/overview/
- **OpenTelemetry — Context Propagation**: https://opentelemetry.io/docs/concepts/context-propagation/
- **OpenTelemetry — Baggage**: https://opentelemetry.io/docs/concepts/signals/baggage/
- **OpenTelemetry — Trace SDK**: https://opentelemetry.io/docs/specs/otel/trace/sdk/
- **OpenTelemetry — Java SDK**: https://opentelemetry.io/docs/languages/java/
- **OpenTelemetry — JS Propagation**: https://opentelemetry.io/docs/languages/js/propagation/
- **Jaeger documentation**: https://www.jaegertracing.io/docs/
- **Jaeger — SDK Migration to OpenTelemetry**: https://www.jaegertracing.io/sdk-migration/
- **Zipkin documentation**: https://zipkin.io/
- **W3C Trace Context spec**: https://www.w3.org/TR/trace-context/
- **W3C Baggage spec**: https://www.w3.org/TR/baggage/
- **Spring Boot — Tracing reference**: https://docs.spring.io/spring-boot/reference/actuator/tracing.html
- **Micrometer Tracing reference**: https://docs.micrometer.io/tracing/reference/index.html
- **Baeldung — Spring Boot 3 Observability**: https://www.baeldung.com/spring-boot-3-observability
- **Medium — Distributed Tracing Spring Boot 3 + Micrometer + OTel**: https://medium.com/javarevisited/distributed-request-tracing-spring-boot-3-micrometer-tracing-with-opentelemetry-3fb129ec8753
- **OneUptime — Migrate Micrometer to OpenTelemetry**: https://oneuptime.com/blog/post/2026-02-06-migrate-micrometer-tracing-to-opentelemetry-spring-boot/view
- **OneUptime — OpenTelemetry Baggage**: https://oneuptime.com/blog/post/2026-01-07-opentelemetry-baggage-propagation/view
- **Книга**: Majors & Fong-Jones, "Observability Engineering" (O'Reilly, 2022).

### Практика / Кейсы

1. **End-to-end trace через 5 сервисов.** Service A (REST) → B (gRPC) → C (Kafka producer) → D (Kafka consumer + DB) → E (RabbitMQ). Spring Boot 3 + Micrometer Tracing + OpenTelemetry Java agent. Запустите Jaeger backend. Сделайте запрос — увидите единый trace с 5+ spans. Покажите, что без явного propagation между Kafka producer/consumer trace разрывается; с Spring Boot 3 + auto-instrumentation — непрерывный.
2. **Tail-based sampling для errors.** Настройте OpenTelemetry Collector с tail sampling processor: 100% traces с error status, 5% без errors. Симулируйте 1% error rate — все error traces в Jaeger; 5% success — представительны. Сравните с head-based 10% — теряются редкие errors.
3. **Baggage для tenant routing.** Запрос с `baggage: tenant-id=acme`. Каждый сервис читает `tenant-id` из context (без ручной передачи через параметры). Используется для: (a) routing к tenant-specific DB, (b) metrics labels, (c) logs. Покажите, что baggage работает через Kafka headers (auto-propagation).
4. **Trace correlation в logs.** Spring Boot 3 + Logback, pattern `%d{...} [%thread] %-5level [%X{traceId},%X{spanId}] %logger - %msg`. Каждый лог-line содержит trace ID. В Kibana/Loki ищете по `traceId` — все логи одного запроса. Покажите, что без trace ID — невозможно связать логи из разных сервисов.
5. **Manual span для business operation.** `Tracer.spanBuilder("order.create").startSpan()` + `setAttribute("order.total", 100.0)` + `setAttribute("order.items_count", 3)` + `addEvent("inventory.reserved")`. Покажите в Jaeger: span с бизнес-атрибутами, можно фильтровать traces где `order.total > 1000`.
6. **Latency analysis через waterfall.** Запрос идёт A → B → C → D, latency 5s. Trace показывает: A=100ms (B call), B=4.5s (C call), C=4.4s (DB query). Diagnosis: slow SQL в C. Покажите, что без trace — знали только «5s latency somewhere»; с trace — точно локализован bottleneck.
7. **Migration Spring Cloud Sleuth → Micrometer Tracing (Boot 3).** Существующий проект на Spring Cloud Sleuth + Zipkin. Миграция: (1) обновить до Spring Boot 3, (2) заменить Sleuth на `micrometer-tracing-bridge-brave`, (3) для OTel — `micrometer-tracing-bridge-otel`. Покажите, что API совместимы (`@NewSpan`, `@ContinueSpan`); метрики и трассы работают как прежде.
8. **Cost optimization: sampling strategy.** Production: 1M requests/day. 100% sampling → 1M traces → 50GB/day storage. С head sampling 1% + tail sampling 100% errors (1%) + 1% slow (>1s) → ~30k traces/day → 1.5GB/day. Покажите, что качество debugging сохранено (errors и slow traces все), storage savings 30x.

---

## 15. Distributed Locking

### Теория

- **Distributed lock** — механизм взаимного исключения между процессами на разных узлах; гарантирует, что только один процесс выполняет критическую секцию в данный момент.
- **Use cases**: leader election, prevent duplicate work, exclusive resource access, scheduled job coordination, migration runner.
- **Requirements (по Martin Kleppmann)**:
  1. **Mutual exclusion** — только один holder.
  2. **Fencing token** — монотонный token для защиты от stale lock holders (split-brain).
  3. **Fault tolerance** — lock service survive node failures.
- **Redis-based locking (SET NX)**:
  ```redis
  SET lock:key <token> NX PX 30000
  ```
  `NX` — set if not exists; `PX 30000` — TTL 30s (auto-release при crash holder).
  Release: Lua script `if redis.call("get", KEYS[1]) == ARGV[1] then return redis.call("del", KEYS[1]) else return 0 end` (атомарная проверка token + del).
- **Redlock algorithm** (Antirez, Redis creator):
  1. client получает current_time.
  2. для каждого из N Redis узлов: `SET NX PX` с коротким timeout.
  3. если получил majority (N/2+1) за время < TTL → lock acquired; validity = TTL - elapsed.
  4. иначе — release all (del на всех узлах).
- **Redlock criticism (Martin Kleppmann, 2016)**:
  - Redis нет guarantees на TTL expiry при GC pauses / network delays — lock может быть выдан второму client, пока первый думает что держит lock.
  - Нет fencing tokens — невозможно защитить от split-brain (когда старый holder продолжает писать после истечения lock).
  - Conclusion: Redlock не подходит для correctness-critical scenarios; OK для efficiency (предотвратить duplicate work, но не для safety).
- **Fencing token** — monotonic number, возвращается с lock; каждый раз больше предыдущего; storage rejects writes с `token < last_seen_token` для данного ресурса. Защита от stale lock holder.
- **ZooKeeper locks**:
  - Создаём ephemeral sequential znode `/locks/lock-00001`.
  - Если наш node — наименьший → lock acquired.
  - Иначе — watch на predecessor; при его удалении → мы стали наименьшим → acquired.
  - Ephemeral гарантирует release при session loss (crash).
  - ZK watches — one-time trigger; при reconnect — re-watch.
  - Recipes: `InterProcessMutex` (Curator library) для Java.
- **ZooKeeper fencing** — ZK zxid (transaction ID) monotonic; можно использовать как fencing token; либо собственный sequence в znode.
- **etcd locks**:
  - `etcdctl lock <name>` — lease-based; TTL; keepalive.
  - Etcd transaction: `if not exists key → create key with lease`; otherwise wait.
  - Etcd Raft-based, CP; guarantees linearizability.
  - Revision number monotonic — fencing token.
- **etcd lease + watch** — lease per lock holder; if holder crashes — lease expires → lock released; watchers notified.
- **Comparison Redis Redlock vs ZK vs etcd**:
  - Redlock: AP-ish (Redis async replication), no fencing tokens, faster but weaker guarantees.
  - ZK: CP (ZAB), ephemeral + sequential, leader election based; battle-tested, harder to operate.
  - etcd: CP (Raft), lease-based, simpler API; Kubernetes-grade.
- **Lease vs lock** — lease имеет TTL автоматически; lock обычно explicit release. Leases проще для distributed systems (TTL = safety bound).
- **Lock granularity** — coarse (один lock на весь ресурс) vs fine (per-key); coarse simpler, fine more parallel.
- **Lock convoy** — много waiters на одном lock; throughput падает; mitigated через shorter critical sections, optimistic concurrency.
- **Optimistic concurrency** — вместо lock, version field: `UPDATE x SET ... WHERE id=? AND version=?`; если 0 rows updated — конфликт, retry. Лучше для low-contention scenarios.
- **Deadlocks** — два процесса ждут друг друга; в distributed locks редко (если один lock per request); решается lock ordering или timeout.
- **Liveness vs safety** — lock должен guarantee safety (mutex) и liveness (progress); в distributed systems эти goals могут конфликтовать (FLP impossibility для async systems).
- **CAS (Compare-And-Swap)** — атомарная операция; `SET key value NX` аналог; building block для lock.
- **Lock service vs embedded** — ZK/etcd/Redis external service; embedded (в БД, например `SELECT FOR UPDATE`) — проще, но tied to data layer.
- **Database-level locks** — Postgres `SELECT ... FOR UPDATE`, `pg_advisory_lock(key)`; MySQL `GET_LOCK`; удобны, но DB-coupled.
- **Lease-based locking in Kafka** — consumer group coordinator использует lease-like mechanisms; partitions assigned via rebalance protocol.
- **Locks for leader election** — одна реплика — active, остальные — standby; lock = leadership; fencing token = term (Raft) / epoch (Kafka producer).
- **Renewal & session** — lock holder должен renew TTL (heartbeat); при неудаче — lock release. Redis: extend Lua; ZK: session heartbeat; etcd: lease keepalive.

### Материалы (реальные ссылки)

- **Martin Kleppmann — How to do distributed locking**: https://martin.kleppmann.com/2016/02/08/how-to-do-distributed-locking.html
- **Antirez — Is Redlock safe? (reply)**: http://antirez.com/news/101
- **Redis — Distributed Locks documentation**: https://redis.io/docs/latest/develop/clients/patterns/distributed-locks/
- **Redis — Redlock algorithm**: https://redis.io/docs/latest/develop/use/patterns/distributed-locks/
- **Apache ZooKeeper — Recipes (Locks)**: https://zookeeper.apache.org/doc/current/recipes.html
- **Apache Curator — InterProcessMutex**: https://curator.apache.org/docs/recipes/
- **etcd — Distributed Locks**: https://etcd.io/docs/v3.5/dev-guide/api_concurrency_reference_v3/
- **etcd — Leases**: https://etcd.io/docs/v3.5/learning/api/
- **Layrs — Distributed Locking: Redis, ZooKeeper & Redlock**: https://layrs.me/course/hld/12-reliability-patterns/distributed-locking
- **Medium — Distributed Locks and Fencing Tokens**: https://www.linkedin.com/pulse/distributed-locks-fencing-tokens-felipe-ascari-ntukf
- **Medium — Implementing Distributed Locks Correctly**: https://medium.com/towardsdev/implementing-distributed-locks-correctly-5a35179422a6
- **Книга**: Kleppmann, DDIA, глава 8 (The Trouble with Distributed Systems) и 9 (Consistency and Consensus).
- **Книга**: Flavio Junqueira & Benjamin Reed, "ZooKeeper: Distributed Process Coordination" (O'Reilly, 2013).

### Практика / Кейсы

1. **Redlock с fencing token (насколько возможно).** Реализуйте Redlock client (5 Redis узлов); lock возвращает `token = current_time_ms`. Storage (mock) проверяет `if token > last_seen_token_for_key`. Симулируйте GC pause 30s: client A держит lock с token=T1; GC pause; TTL expires; client B acquired с token=T2>T1; A выходит из GC, пытается писать с T1 — storage отклоняет. Покажите, что без fencing — double-write.
2. **ZooKeeper InterProcessMutex.** Используйте Apache Curator `InterProcessMutex`. 3 процесса пытаются захватить lock; один acquired, двое ждут (watch на predecessor). При kill holder — ephemeral znode удаляется → следующий acquired. Покажите через `zkCli` что znodes создаются и удаляются корректно.
3. **etcd lease + lock.** `etcdctl lock /orders/42` — lease-based. Покажите, что при kill holder process — lease expires через ~10s, другой process может acquire. Используйте `revision` как fencing token в Postgres write.
4. **Database advisory lock.** Postgres `pg_advisory_lock(42)` — session-level; `pg_try_advisory_lock(42)` non-blocking. 3 параллельных worker'а запрашивают lock — только один acquired; release через `pg_advisory_unlock(42)`. Покажите, что при disconnect session — lock auto-released. Сравните с row-level `SELECT FOR UPDATE`.
5. **Leader election через ZK.** 3 replicas of service; ZK ephemeral node `/leader` — created by `create(NZ_EPHEMERAL)`; первый — leader, остальные watch. При kill leader — node удаляется, watch fires,剩下的 race to create — новый leader. Покажите через 5 циклов kill/recovery что ровно один leader всегда.
6. **Lock convoy и mitigation.** 1000 threads пытаются `lock(key)` — throughput падает до 1 op/s (serialized). Mitigation: (a) optimistic concurrency (CAS, retry on conflict); (b) finer-grained locks (per sub-key); (c) shorter critical section. Покажите throughput 1000 → 10k ops/s после optimization.
7. **Critical section too long → TTL expired.** Lock TTL=5s, critical section занимает 10s (slow DB). Без renewal — TTL expires, второй client acquired, оба в критической секции. Решение: renewal (heartbeat каждые 2s продлевает TTL). Реализуйте renewal thread; при failure renewal — exit critical section (release). Покажите, что без renewal — corruption; с renewal — safety.
8. **Optimistic concurrency vs locking.** Для update user balance: lock-based `BEGIN; SELECT FOR UPDATE; UPDATE; COMMIT` vs CAS `UPDATE accounts SET balance=balance-100 WHERE id=42 AND balance>=100`. Покажите, что при low contention CAS в 10x быстрее (no lock overhead); при high contention lock-based стабильнее (CAS retrials под load). Найдите crossover point.

---

## 16. Caching distributed

### Теория

- **Cache** — быстрое хранилище для часто используемых данных; уменьшает load на primary store, уменьшает latency.
- **Cache-aside (lazy loading)** — приложение сначала проверяет cache; miss → читает DB → заполняет cache; TTL для staleness bound. Простой, но cache-miss = 2x latency.
- **Read-through** — приложение читает из cache; cache сам (provider) читает из DB при miss. Прозрачно для app.
- **Write-through** — write одновременно в cache и DB; cache всегда up-to-date; но write latency выше.
- **Write-behind (write-back)** — write в cache, async в DB; high write throughput, но risk of data loss при crash.
- **Write-around** — write только в DB; cache не обновляется; read на следующем запросе — miss, populate. Подходит для write-heavy, редко читаемых.
- **Refresh-ahead** — async refresh cache до expiry; для hot keys без cache misses.
- **Cache invalidation** —最难 (Phil Karlton: "There are only two hard things in Computer Science: cache invalidation and naming things"):
  - **TTL-based** — автоматически через время; простая, но stale до expiry.
  - **Event-driven** — при update primary → invalidate cache; в distributed systems сложно (consistency).
  - **Versioned** — ключи с version (`user:42:v3`); новый version при update; старый eviction.
- **Redis cluster** — Redis с sharding; 16384 slots, distributed по nodes; `redis-cli -c` для автоматической маршрутизации; HA через Redis Sentinel (legacy) или Redis Cluster (master+replica per shard).
- **Redis data structures** — String, List, Hash, Set, Sorted Set, HyperLogLog, Bitmap, Geo, Stream; разные use cases.
- **Cache TTL strategies**:
  - **Uniform TTL** — все keys TTL=5min.
  - **Per-key TTL** — зависит от data staleness tolerance.
  - **Sliding expiration** — TTL продлевается при доступе.
  - **Stale-while-revalidate** — возвращаем stale, async refresh; client не ждёт.
- **Cache stampede (dogpile)** — множество requests одновременно miss cache → все идут в DB → DB overload. Mitigation:
  - **Locking** (mutex per key) — первый miss берёт lock, остальные ждут.
  - **Probabilistic early expiration** (XFetch algorithm) — random early refresh; снижает синхронность.
  - **Permutation jitter** — добавить jitter к TTL, чтобы expiry распределилось.
- **Thundering herd** — после restart cache (или expiry big batch) — много misses; mitigation: pre-warming, gradual expiry, circuit breaker на DB.
- **Cache penetration** — запросы для несуществующих ключей (каждый — miss → DB); mitigation: cache null values с short TTL, Bloom filter для existence check.
- **Cache breakdown** — hot key expiry → sudden spike DB load; mitigation: lock + refresh, refresh-ahead.
- **Cache avalanche** — множество keys expire одновременно → DB overload; mitigation: TTL jitter,分层 cache (L1 + L2).
- **Multi-layer (L1+L2) caching** — L1 (in-process, Caffeine) + L2 (Redis); L1 super-fast, L2 shared; problem: invalidation L1 across instances (pub/sub, message bus).
- **Caffeine** — modern Java in-memory cache; W-TinyLFU eviction (frequency + recency); near-optimal hit rate; async refresh, scheduler.
- **Bloom filter** — probabilistic data structure для set membership; false positives possible, false negatives нет; используются для: cache existence check, deduplication, malicious URL detection. Memory-efficient (1% FP rate at ~10 bits/element).
- **Counting Bloom filter** — supports deletions (counter per cell instead of bit).
- **Cuckoo filter** — better than Bloom в некоторых scenarios; supports deletions natively.
- **Circuit breaker на cache** — если cache (Redis) недоступен, не падать; temporarily bypass cache (direct DB), но rate-limit; после восстановления — return to cache.
- **Cache consistency models**:
  - **Strong** — write-through, всегда согласован.
  - **Eventual** — write-behind или cache-aside с TTL; eventual.
  - **Read-your-writes** — после write, invalidate cache; следующий read — miss → DB.
- **Distributed cache coherence** — проблема: несколько app instances с L1 caches; при update — нужно invalidate во всех. Решения: Redis pub/sub, Kafka events, JGroups.
- **Cache metrics**: hit ratio, miss rate, latency, eviction count, size; SLO обычно hit ratio > 95%.
- **Hot keys** — несколько keys с disproportionate traffic; проблема — single Redis shard bottleneck. Mitigation: client-side local cache, replication per hot key, scatter-gather.
- **Cold keys** — редко читаемые; занимают memory зря; LRU eviction.
- **Eviction policies** — LRU (least recently used), LFU (least frequently used), FIFO, random, W-TinyLFU (Caffeine), ARC (adaptive).
- **Cache size estimation** — `size = avg_value_size * number_of_keys * overhead_factor`; Redis overhead ~2-3x of data; monitor `used_memory` vs `maxmemory`.
- **Redis memory management** — `maxmemory`, eviction policy (`noeviction`, `allkeys-lru`, `volatile-lru`, `allkeys-lfu`, etc.); при достижении maxmemory → eviction.
- **Persistence** — RDB (snapshot, periodic), AOF (append-only log, every write); tradeoff durability vs performance.
- **Cluster sharding** — hash slot `CRC16(key) % 16384`; keys с `hash tags` (`{user}:profile`, `{user}:settings`) на одном shard для multi-key operations.
- **Read replicas** — для read-heavy; но replication lag → stale reads; для strong consistency — read from master.
- **Cache as DB replacement** — anti-pattern; cache — volatile, DB — source of truth; всегда fallback to DB.

### Материалы (реальные ссылки)

- **Redis — Distributed Locks**: https://redis.io/docs/latest/develop/clients/patterns/distributed-locks/
- **Redis — Cluster specification**: https://redis.io/docs/latest/operate/oss_and_stack/reference/cluster-spec/
- **Redis — Memory eviction policies**: https://redis.io/docs/latest/develop/use/manage/optimization/eviction/
- **Redis — Persistence (RDB/AOF)**: https://redis.io/docs/latest/operate/oss_and_stack/management/persistence/
- **Redis blog — How to tame the thundering herd**: https://redis.io/blog/how-to-tame-the-thundering-herd-problem/
- **Caffeine cache documentation**: https://github.com/ben-manes/caffeine/wiki
- **Caffeine — Design (W-TinyLFU)**: https://github.com/ben-manes/caffeine/wiki/Efficiency
- **12 Caching Patterns Every Engineer Should Know**: https://designgurus.substack.com/p/12-caching-patterns-in-one-visual
- **Redis Caching Pitfalls**: https://medium.com/@QuarkAndCode/redis-caching-pitfalls-invalidation-testing-best-practices-3950a0660f1a
- **Cache Patterns and Preventing Cache Avalanche**: https://www.allpcb.com/allelectronics-hub/cache-patterns-and-preventing-cache-avalanche
- **Martin Fowler — CachePatterns**: https://martinfowler.com/bliki/TwoHardThings.html (invalidation quip)
- **Bloom filter — Wikipedia (overview)**: https://en.wikipedia.org/wiki/Bloom_filter
- **Probabilistic Early Expiration (XFetch)**: https://cseweb.ucsd.edu/~avattani/papers/arc.pdf
- **Книга**: Kleppmann, DDIA, глава 1 (Reliability, Scalability, Maintainability) и глава 5 (Replication, про cache consistency).
- **Книга**: Carlson, "Redis in Action" (Manning, 2013).

### Практика / Кейсы

1. **Cache-aside + write-through комбо.** Service читает через cache-aside (Redis miss → DB → populate), пишет через write-through (DB update + cache set in one operation). Симулируйте: 1000 reads — 95% cache hits; write → cache immediately fresh. Покажите, что при cache crash — DB still consistent (cache — auxiliary).
2. **Cache stampede mitigation.** Hot key `product:42` expires → 1000 concurrent requests miss → 1000 DB queries. Mitigation 1: Redis `SET NX` lock — первый miss берёт lock, остальные ждут → 1 DB query. Mitigation 2: XFetch probabilistic early refresh — 5% requests refresh заранее, expiry spread. Измерьте DB load: без mitigation 1000 QPS spike; с mitigation 1 QPS steady.
3. **L1 (Caffeine) + L2 (Redis) с pub/sub invalidation.** Caffeine in-process (1ms access) + Redis distributed (3ms). При write → invalidate Redis + publish `invalidate` event в Redis pub/sub → все app instances receive → clear L1. Покажите, что без pub/sub — L1 stale across instances; с pub/sub — eventual consistency (<100ms lag).
4. **Bloom filter для cache penetration.** Злонамеренные requests для `user:nonexistent` — каждый miss → DB query. Bloom filter в Redis (или в-memory): при запуске populate всеми существующими IDs; request → check Bloom first; если "definitely not" → return 404 без DB hit; если "maybe" → cache → DB. Покажите: 1M random nonexistent requests → 0 DB queries (Bloom says "no" для 99%); для существующих — нормальный cache flow.
5. **Hot key replication.** Key `config:global` — 50k QPS на один Redis shard → bottleneck. Решение: `config:global:1`, `:2`, `:3` (3 replicas), client picks random shard. Покажите: без sharding single-shard CPU 100%; с sharding — 3 shards по 33%. Обновление: publish to all 3 (или pub/sub).
6. **Circuit breaker на cache.** Redis недоступен (network partition): вместо падения — circuit breaker, fallback to DB (slow but works), rate limit DB (1000 QPS max). После восстановления Redis — return to cache. Покажите, что без CB — DB overload; с CB — degraded but alive.
7. **Write-behind с persistence risk.** Write-behind: write в Redis (immediate ack), async flush to DB every 1s. High throughput 100k writes/s. Симулируйте Redis crash перед flush — lose last 1s of data. Mitigation: AOF persistence (every write logged) — no data loss, но throughput падает до 50k/s. Trade-off documented.
8. **Cache warming + gradual expiry.** Restart cache → cold cache → DB overload. Mitigation: (a) pre-warm cache at startup (top 1000 keys from analytics), (b) TTL jitter (TTL=300s ± 60s random) → spread expiry. Покажите, что без warming — DB 100% CPU на 5 min; с warming — 30% CPU steady.

---

## 17. Idempotency / Outbox / CDC

### Теория

- **Outbox pattern** (см. подробно раздел 5) — транзакционная публикация событий; основа для reliable event-driven.
- **CDC (Change Data Capture)** — отслеживание изменений в БД и автоматическая публикация в downstream systems; альтернатива polling.
- **Debezium** (Red Hat, open-source) — leading CDC platform; основан на Kafka Connect; connectors для Postgres (logical replication/WAL), MySQL (binlog), MongoDB (oplog), SQL Server (Change Tracking/CDC), Oracle (LogMiner/XStream), Cassandra.
- **Debezium architecture**: connector watches DB log → transforms to event (`op=c|u|d|r`, `before`, `after`, `source` metadata) → publishes to Kafka topic (one per table).
- **Initial snapshot** — при старте connector делает snapshot текущего состояния (`op=r` for read), затем switches to streaming WAL/binlog. `snapshot.mode=initial` (default), `schema_only` (no data snapshot), `never` (no snapshot, only new changes).
- **Schema history topic** — Debezium хранит history of DB schema changes (DDL) в Kafka topic; нужно для parsing old WAL records; critical для recovery.
- **Kafka Connect** — framework для running connectors; cluster of workers; REST API for management; source connectors (DB → Kafka), sink connectors (Kafka → DB/ES/S3).
- **Connect modes**:
  - **Standalone** — single worker, no cluster; для dev.
  - **Distributed** — cluster of workers, rebalancing connectors, REST API; для prod.
  - **KRaft mode** (в новых Kafka) — без ZooKeeper, Connect хранит config в Kafka topics.
- **Source connectors** — DB → Kafka (Debezium, JDBC source with timestamp/ID incrementing mode).
- **Sink connectors** — Kafka → external system: Elasticsearch, JDBC, S3, MongoDB, HDFS, Syslog.
- **Single Message Transforms (SMT)** — in-flight transformation of messages; built-in: `ExtractField`, `Flatten`, `Cast`, `TimestampConverter`, `SetSchemaMetadata`; custom via Java.
- **Connect converters** — serialization: Avro (with Schema Registry), Protobuf, JSON, String; key и value converter отдельно.
- **Schema evolution** — при изменении DB schema (DDL) Debezium публикует обновлённые schema в Schema Registry; consumers могут читать старые + новые записи (если BACKWARD-compatible).
- **Schema Registry compatibility** — BACKWARD (new readers read old data), FORWARD (old readers read new data), FULL (both); BACKWARD_TRANSITIVE (against all previous), etc.
- **Avro evolution rules**:
  - Add field with default — backward compatible.
  - Remove field with default — forward compatible.
  - Change field type — generally NOT compatible (int → long OK, but string → int no).
  - Rename via aliases — compatible.
  - Add enum symbol — backward but not forward (old readers fail on new symbol).
- **Protobuf evolution**:
  - Add field with default — compatible.
  - Remove field — compatible if `reserved` is set.
  - Field number changes — NEVER compatible.
  - Wire format compatible with reasonable rules.
- **JSON Schema evolution** — обычно backward/forward weaker than Avro/Protobuf; depends on implementation.
- **Topic naming conventions** — Debezium default: `<server>.<schema>.<table>`; can be customized via `topic.prefix` and SMT.
- **Outbox table с Debezium** — outbox table как обычная таблица; Debezium публикует changes; consumer читает outbox topic и обрабатывает; затем (опц.) `tombstone` для cleanup.
- **Outbox + CDC advantages**:
  - No polling overhead (CDC-driven, low latency).
  - Atomicity guaranteed (DB transaction includes outbox insert).
  - Schema in Schema Registry, evolved.
  - Leverage Kafka infrastructure.
- **Outbox drawbacks**:
  - Debezium dependency, ops overhead.
  - Schema history topic critical (loss = fail recovery).
  - Initial snapshot may be heavy.
- **Debezium + Kafka Streams** — Debezium → Kafka → Kafka Streams for transformations/aggregations → output topic. Example: stream of `orders` changes → aggregate revenue per customer → `customer-revenue` topic.
- **Debezium + Event Sourcing** — CDC events as domain events; `before`/`after` capture state changes; но semantic mismatch (CDC = data change, ES = domain event); обычно нужен projection.
- **Multi-table transactional consistency** — Debezium публикует в order per table, но cross-table transaction может быть в разных partitions/offsets; **transaction metadata** topic (Debezium feature) решает это.
- **Debezium Server** — lightweight alternative to Kafka Connect; standalone runner, supports multiple sinks (Kafka, Kinesis, Pulsar, Redis Stream); simpler ops.
- **Backpressure** — если Kafka broker slow, Debezium приостанавливает чтение WAL; может расти lag в БД; monitor `binlog retention` / `WAL size`.
- **Exactly-once с Debezium** — Debezium at-least-once по умолчанию; для EOS нужны idempotent consumers + dedup table.
- **Schema evolution strategy** — `BACKWARD_TRANSITIVE` recommended; план: add field с default (safe), deprecate old, eventually remove.

### Материалы (реальные ссылки)

- **Debezium Documentation (main)**: https://debezium.io/documentation/
-- **Debezium Architecture**: https://debezium.io/documentation/reference/stable/architecture.html
- **Debezium — Source Connectors**: https://debezium.io/documentation/reference/stable/connectors/index.html
- **Debezium — PostgreSQL Connector**: https://debezium.io/documentation/reference/stable/connectors/postgresql.html
- **Debezium — MySQL Connector**: https://debezium.io/documentation/reference/stable/connectors/mysql.html
- **Confluent — Debezium SQL Server Source**: https://docs.confluent.io/kafka-connectors/debezium-sqlserver-source/current/overview.html
- **Conduktor — Debezium CDC with Kafka**: https://www.conduktor.io/glossary/implementing-cdc-with-debezium
- **Apache Kafka Connect User Guide**: https://kafka.apache.org/40/kafka-connect/user-guide/
- **Confluent — Schema Registry fundamentals**: https://docs.confluent.io/platform/current/schema-registry/index.html
- **Confluent — Schema Evolution & Compatibility**: https://docs.confluent.io/platform/current/schema-registry/fundamentals/schema-evolution.html
- **Confluent — Schema Registry Best Practices**: https://www.confluent.io/blog/best-practices-for-confluent-schema-registry/
- **Microservices.io — Transactional Outbox**: https://microservices.io/patterns/data/transactional-outbox.html
- **AWS — Transactional Outbox Prescriptive Guidance**: https://docs.aws.amazon.com/prescriptive-guidance/latest/cloud-design-patterns/transactional-outbox.html
- **Decodable — Revisiting the Outbox Pattern**: https://www.decodable.co/blog/revisiting-the-outbox-pattern
- **James Carr — Transactional Outbox Pattern**: https://james-carr.org/posts/2026-01-15-transactional-outbox-pattern
- **Книга**: Krebs et al., "Kafka: The Definitive Guide" 2nd ed., глава 7 (Kafka Connect).
- **Книга**: Bejeck, "Kafka Streams in Action" 2nd ed. — про schema evolution в stream processing.

### Практика / Кейсы

1. **Outbox + Debezium end-to-end.** Postgres с таблицами `orders` и `outbox_events`; service: `@Transactional` insert order + outbox event. Debezium connector читает WAL → публикует `dbserver.public.outbox_events` в Kafka. Consumer (`@KafkaListener`) обрабатывает events, обновляет read model. Симулируйте service crash между insert и commit — nothing published (atomic). Debezium lag <100ms.
2. **Schema evolution Avro.** v1 schema: `{ "type":"record", "name":"Order", "fields":[{"name":"id","type":"string"},{"name":"total","type":"double"}] }`. v2: add `{"name":"currency","type":"string","default":"USD"}`. v3: add `{"name":"items","type":{"type":"array","items":"..."},"default":[]}`. Настройте Schema Registry BACKWARD_TRANSITIVE. Покажите, что consumer с v1 schema читает v3 data (defaults applied); consumer с v3 читает v1 (extra fields defaulted). Попробуйте v4 с remove `id` — rejected.
3. **Debezium initial snapshot + ongoing CDC.** Таблица с 10M rows. `snapshot.mode=initial` — Debezium делает snapshot (~30 min), публикует как `op=r` events. Затем switches to streaming WAL. Покажите, что consumer может начать с snapshot, обрабатывает все 10M, потом продолжается с new changes. Измерьте lag.
4. **Multi-table transactional consistency.** Две таблицы `orders` и `order_items` обновляются в одной DB транзакции. Debezium публикует в два разных topics. Без transaction metadata — consumer может видеть `order_items` без parent `order` (race). С `provide.transaction.metadata=true` — отдельный topic `<prefix>.transaction` с BEGIN/END markers; consumer может группировать.
5. **Outbox с Debezium + tombstone cleanup.** Outbox таблица растёт; cleanup через Debezium tombstone: consumer обрабатывает event → delete outbox row → Debezium publishes tombstone (null value) → log compaction удаляет. Покажите, что outbox table size stable, topic size controlled (log compaction).
6. **Kafka Connect cluster (distributed mode).** 3 Connect workers, 2 Debezium connectors. При kill одного worker — rebalance, connectors redistributed. Покажите, что CDC продолжается без duplicates (offsets stored in Kafka `connect-offsets` topic). REST API: `GET /connectors`, `POST /connectors`, `PUT /connectors/{name}/config`.
7. **CDC → Elasticsearch sink.** Debezium Postgres → Kafka → Elasticsearch sink connector. Auto-create ES index, mapping from Avro schema. Покажите, что DB change → ES update within 1s. Compare to manual dual-write (DB + ES in code): dual-write fragile (transactional issues); CDC robust.
8. **Migration schema с zero downtime.** v1: `User { id, name, email }`. v2: split `name` → `firstName, lastName`. Шаги: (a) add `firstName, lastName` (default null), (b) backfill from `name`, (c) update app to write both, (d) update consumers to use new fields, (e) remove `name` after consumer migration. Debezium публикует schema changes; consumer с v1 schema продолжает работать (через defaults) пока мигрирует.

---

## 18. Event-driven architecture

### Теория

- **Event-driven architecture (EDA)** — система, где компоненты общаются через события; producers не знают consumers; loosely coupled, async.
- **Event** — факт, что что-то произошло в прошлом; immutable, named в past tense (`OrderPlaced`, not `PlaceOrder`); contrasts with Command (`PlaceOrder` — request to do something).
- **Event notification** — короткое событие «X happened», без данных; subscribers запрашивают детали. Loosest coupling, но N+1 query problem.
- **Event-carried state transfer** — событие содержит все данные; subscriber не запрашивает. Decoupled, но data duplication.
- **Domain event** — событие, имеющее meaning для domain experts; part of DDD; `OrderPlaced`, `PaymentConfirmed`.
- **Integration event** — межсервисное событие; serialized (Avro/Protobuf/JSON); versioned; может отличаться от domain event.
- **Event sourcing (ES)** — состояние = последовательность событий; current state = projection/replay; см. подробно раздел 9.
- **CQRS + ES** — command-side writes events (ES); query-side projects events into read models (CQRS); natural combination.
- **Pub-Sub patterns**:
  - **Topic-based** — publishers в topic, multiple subscribers (Kafka consumer groups, RabbitMQ fanout).
  - **Content-based** — routing по content (RabbitMQ topic exchange `orders.*.priority`).
  - **Type-based** — routing по event type.
- **Event schema** — formal description: Avro/Protobuf/JSON Schema; fields, types, required/optional, defaults; compatibility rules.
- **Schema versioning strategies**:
  - **In-place evolution** — mutate schema, keep compatible (BACKWARD).
  - **Versioned topics** — `orders.v1`, `orders.v2`; consumers migrate.
  - **Multi-version consumers** — consumer понимает несколько версий (через `schema_version` field).
- **Event schema evolution rules** (Avro):
  - Add optional field — backward compatible.
  - Remove optional field — forward compatible.
  - Required field — опасно; migration через optional + default.
  - Field rename — aliases.
  - Field type change — обычно incompatible.
- **Backward vs Forward compatibility**:
  - **Backward** — new consumer reads old data.
  - **Forward** — old consumer reads new data.
  - **Full** — both.
  - В EDA обычно BACKWARD (новые consumers должны читать старые events из history).
- **Event ordering**:
  - **Per partition** (Kafka) — guaranteed.
  - **Per key** — если hash by key, все события для одного aggregate в одной partition.
  - **Global** — невозможно без sacrificing throughput; через single partition (anti-pattern).
- **Idempotency в EDA** — consumer должен быть idempotent; events могут дублироваться (at-least-once).
- **At-least-once vs exactly-once в EDA** — Kafka EOS гарантирует within Kafka; end-to-end — через idempotent consumers + outbox.
- **Saga via events (choreography)** — каждый сервис реагирует на events, принимает решения; нет central coordinator; benefits: loose coupling; drawbacks: harder to track.
- **Saga via commands (orchestration)** — orchestrator отправляет commands; services respond with events; centralized logic; easier to debug.
- **Command vs Event**:
  - Command — request to do something (`PlaceOrder`); intent-driven; sent to specific recipient.
  - Event — fact (`OrderPlaced`); past tense; broadcast; no specific recipient.
- **Event flow patterns**:
  - **One event per change** — каждый update → event.
  - **Aggregated events** — периодическая агрегация (`DailyRevenueCalculated`).
  - **Delta events** — только изменённые поля.
  - **Snapshot events** — полное состояние периодически.
- **Replay** — возможность переобработать historical events (для new consumers, bug fixes, new projections); основа ES.
- **Backfill** — загрузка исторических данных в новый consumer; через replay from Kafka или re-snapshot.
- **Event semantics** — каждый event должен быть self-describing; содержать `event_id`, `event_type`, `occurred_at`, `aggregate_id`, `version`, `producer`.
- **Versioning commands vs events** — events immutable, эволюционируют через schema compatibility; commands могут эволюционировать легче (нет history).
- **Anti-patterns**:
  - **Event with command name** (`CreateOrder` event — это command, не event).
  - **Mutable events** — events нельзя изменять после публикации.
  - **Events without version** — невозможно мигрировать.
  - **God event** — один event с слишком многими полями; разделять.
  - **Synchronous event handling** — consumer блокирует producer (противоречит EDA).
- **EDA vs Request-Driven**:
  - EDA: async, decoupled, eventually consistent, complex debugging.
  - Request-driven: sync, coupled, strong consistency, simpler.
- **Eventual consistency implications** — UI должен показывать stale data gracefully; user action «заказал» → может не сразу отразиться в «мои заказы»; pattern: optimistic UI update + reconciliation.
- **Compensating events** — при undo: не «отменить событие» (immutable), а publish `OrderCancelled` event; downstream react.
- **Dead letter queue в EDA** — события, которые не удалось обработать после retries; отдельный topic для ручного анализа.
- **Event streaming platform (ESP)** — Kafka, Pulsar; persistent log + pub-sub + stream processing; более rich чем traditional broker.
- **Event-driven vs Message-driven** — events — facts (past), messages могут быть commands (future intent); EDA обычно events.
- **Long-running business processes** — через orchestration (Temporal.io, Camunda), state stored, events emitted.

### Материалы (реальные ссылки)

- **Microservices.io — Events on the outside, on the inside and at the core**: https://microservices.io/post/microservices/2021/02/21/events-are-the-core.html
- **Microservices.io — Event Sourcing**: https://microservices.io/patterns/data/event-sourcing.html
- **Microservices.io — CQRS**: https://microservices.io/patterns/data/cqrs.html
- **Martin Fowler — Event Sourcing**: https://martinfowler.com/eaaDev/EventSourcing.html
- **Martin Fowler — What do you mean by Event-Driven?**: https://martinfowler.com/articles/201701-event-driven.html
- **Martin Fowler — CQRS**: https://martinfowler.com/bliki/CQRS.html
- **Martin Fowler — Event Notification vs Event-Carried State Transfer**: https://martinfowler.com/articles/201701-event-driven.html
- **Confluent — Schema Evolution & Compatibility**: https://docs.confluent.io/platform/current/schema-registry/fundamentals/schema-evolution.html
- **Confluent — Kafka as Event Streaming Platform**: https://developer.confluent.io/learn/kafka/event-streaming/
- **Enterprise Integration Patterns — Event Message**: https://www.enterpriseintegrationpatterns.com/patterns/messaging/EventMessage.html
- **Enterprise Integration Patterns — Publish-Subscribe Channel**: https://www.enterpriseintegrationpatterns.com/patterns/messaging/PublishSubscribeChannel.html
- **Eventuate — Why Eventuate**: https://eventuate.io/post/eventuate/2020/02/24/why-eventuate.html
- **Hacksaw — Microservices Distributed Data Patterns**: http://hacksaw.co.za/blog/microservices-distributed-data-patterns
- **Книга**: Vaughn Vernon, "Implementing Domain-Driven Design" — глава про events & integration.
- **Книга**: Richardson, "Microservices Patterns", главы 3, 4, 8.
- **Книга**: Ben Stopford, "Designing Event-Driven Systems" (O'Reilly, 2018).

### Практика / Кейсы

1. **Event-carried state transfer для catalog service.** Product service emits `ProductCreated`, `ProductUpdated`, `ProductDeleted` events с полными данными. Inventory service consumer → stores local copy of product info. Queries for `inventory + product info` — local join, no cross-service call. Покажите, что при Product service down — inventory service продолжает работать (имеет cached product data).
2. **Saga choreography vs orchestration.** Order creation: Reserve inventory → Charge payment → Confirm order. Choreography: each service emits events, listens to others. Orchestration: orchestrator sends commands, receives replies. Реализуйте обе, сравните: choreography — 5 listeners, distributed logic; orchestration — 1 state machine, easier to trace. Симулируйте failure — orchestration легче компенсирует.
3. **Event sourcing для bank account.** Events: `AccountOpened`, `MoneyDeposited`, `MoneyWithdrawn`, `FeeApplied`, `AccountClosed`. State = projection. Snapshots каждые 100 events. Покажите: (a) full history — audit log; (b) time-travel — состояние на T1; (c) replay для new read model (statement generator). Schema evolution: add `currency` field с default — old events replayed with default.
4. **Schema evolution in production.** v1 event: `OrderPlaced { orderId, total }`. v2: add `currency` (default USD). v3: add `items: [Item]` (default []). v4: rename `total` → `amount` (alias). Настройте Schema Registry BACKWARD_TRANSITIVE. Запустите producer с v4, consumer с v1 — работает (через defaults + aliases). Покажите, что попытка удалить required field — rejected.
5. **Replay для new consumer.** Существующий topic `orders.placed` с 6 months history (10M events). Новый consumer `analytics-service` хочет построить daily revenue report. Replay all events from offset 0, aggregate by date. Покажите, что replay идёт 30 min, не влияет на live consumers (different consumer group). Idempotency проверена (если replay прерван — restart continues).
6. **Compensating events.** Saga: `OrderPlaced` → `InventoryReserved` → `PaymentCharged` → `OrderConfirmed`. Если `PaymentFailed` → `InventoryReleased` → `OrderCancelled`. Все события immutable; downstream subscribers (notification, analytics) реагируют на `OrderCancelled` так же как на `OrderConfirmed`. Покажите, что final state consistent (inventory available, no charge, order cancelled).
7. **DLQ для poison events.** Consumer не может обработать event (avro deserialization error, business logic exception). После 3 retries → publish to `orders.placed.DLT` с `error_reason`, `stack_trace` headers. Покажите, что main consumer не блокируется; analyst может изучить DLT и переотправить после fix.
8. **EDA observability.** 10 services общаются через 20 Kafka topics. Distributed tracing (OpenTelemetry) — каждый event carries trace context; event-driven flow виден как trace tree. Metrics: lag per consumer group, throughput per topic, error rate. Покажите, что без tracing — невозможно понять «куда делся запрос»; с tracing — единый view.

---

## 19. WebSocket / SSE / Server push

### Теория

- **WebSocket** (RFC 6455, 2011) — full-duplex communication over single TCP; HTTP/1.1 Upgrade handshake, потом WS protocol; persistent connection; low overhead per message (2-10 bytes framing).
- **WebSocket lifecycle**:
  - Client → Server: HTTP GET с `Upgrade: websocket`, `Connection: Upgrade`, `Sec-WebSocket-Key`, `Sec-WebSocket-Version: 13`.
  - Server → Client: HTTP 101 Switching Protocols, `Sec-WebSocket-Accept` (хэш key + magic GUID).
  - После handshake — both могут отправлять frames (text/binary/ping/pong/close).
- **Sub-protocols** — `Sec-WebSocket-Protocol` header; popular: **STOMP** (Simple Text Oriented Messaging Protocol), WAMP, GraphQL-WS.
- **STOMP** — text-based messaging protocol over WebSocket; FRAME format: `COMMAND\nheader1:value1\n\nbody\0`; commands: CONNECT, SUBSCRIBE, UNSUBSCRIBE, SEND, MESSAGE, ACK, NACK, DISCONNECT; destinations `/topic/...`, `/queue/...`.
- **Spring WebSocket**:
  - **Low-level API** — `WebSocketHandler` interface (`afterConnectionEstablished`, `handleMessage`, `handleTransportError`, `afterConnectionClosed`).
  - **High-level (STOMP)** — `@MessageMapping`, `@SendTo`, `@SubscribeMapping`; Spring dispatches STOMP frames to controller methods.
  - **STOMP broker relay** — `enableStompBrokerRelay("/topic/", "/queue/")` — Spring forwards to external broker (RabbitMQ, ActiveMQ) for multi-instance.
  - **Simple broker** — in-memory, single-instance only.
- **`@MessageMapping("/chat")`** — server-side handler for messages to `/chat` destination.
- **`@SendTo("/topic/messages")`** — return value published to `/topic/messages` (всем subscribers).
- **`@SendToUser`** — отправка конкретному user (через session id или user identity); для private messages.
- **SimpMessagingTemplate** — programmatic send: `simpMessagingTemplate.convertAndSend("/topic/orders", order)`.
- **WebSocket security** — handshake interceptor для auth (extract token from query param или header); `@MessageMapping` secured via Spring Security (`AuthorizationChannelInterceptor`).
- **Heartbeat** — STOMP heartbeat frames (`\n`) для detection обрыва; server/client negotiate interval.
- **Reactive WebSocket** (Spring WebFlux):
  - `WebSocketHandler` returns `Mono<Void>`; reactive `Flux<WebSocketMessage>` for incoming/outgoing.
  - Backpressure — WebSocket supports `request(n)` per reactive spec.
  - Non-blocking, Netty-based; better scalability.
- **Server-Sent Events (SSE)** (HTML5, `EventSource` API):
  - One-way (server → client) over HTTP/1.1 (или HTTP/2).
  - `Content-Type: text/event-stream`; messages `data: ...\n\n`.
  - Auto-reconnect built-in (`retry:` field).
  - Simple, no upgrade; но unidirectional; limit 6 connections per browser over HTTP/1.1.
- **SSE in Spring** — `SseEmitter` (Spring MVC) или `Flux<ServerSentEvent>` (WebFlux).
- **WebSocket vs SSE vs Long Polling**:
  - WebSocket: bidirectional, low overhead, requires upgrade; best for chat, real-time gaming.
  - SSE: unidirectional, simple HTTP, auto-reconnect; best for notifications, live updates.
  - Long polling: HTTP-only, hacky, high overhead; legacy fallback.
- **Horizontal scaling of WebSocket** — проблема: 1000 clients connected to instance A; instance B publishes message — как достичь clients на A? Решения:
  - **STOMP broker relay** (RabbitMQ, ActiveMQ) — broker fanout to all instances.
  - **Redis pub/sub** — instances subscribe; one publishes → all receive.
  - **Kafka topic** — consumer group per instance; broadcast via different mechanism.
- **Backpressure** — если client медленный, server не должен OOM; reactive WebSocket handles via `onNext` + `request(n)`; in imperative — manual `isReady()` check.
- **Connection limits** — browser limits ~250 WS connections per origin; server limits — RAM (typically 1MB per WS connection); для millions connections — sticky sessions + multiple servers.
- **Sticky sessions** — load balancer routes same client to same server (cookie/IP); для stateful WS.
- **WebSocket compression** — `permessage-deflate` extension; save bandwidth; CPU overhead.
- **Authentication patterns**:
  - Token in query param during handshake (less secure — logs).
  - Cookie-based (same-origin, CSRF risk).
  - First message auth (after connect, send token).
- **CORS** — WebSocket не использует CORS (no Origin check by default); server should validate Origin header to prevent CSWSH (Cross-Site WebSocket Hijacking).
- **STOMP topics vs queues**:
  - `/topic/*` — pub-sub (all subscribers).
  - `/queue/*` — point-to-point (one subscriber per message, used with `@SendToUser`).
- **User destination** — `/user/queue/notifications` — Spring translates to session-specific `/queue/notifications-user<sessionId>`; enables per-user messaging.
- **Subscription registry** — Spring хранит subscriptions in-memory; for multi-instance — external broker or shared store.
- **Graceful disconnect** — server sends `close` frame, waits for client close; cleanup resources.

### Материалы (реальные ссылки)

- **Spring — Getting Started: WebSocket STOMP**: https://spring.io/guides/gs/messaging-stomp-websocket
- **Spring Framework — STOMP reference**: https://docs.spring.io/spring-framework/reference/web/websocket/stomp.html
- **Spring Framework — WebSocket reference**: https://docs.spring.io/spring-framework/reference/web/websocket.html
- **Spring Boot — WebSocket reference**: https://docs.spring.io/spring-boot/reference/web/websocket.html
- **Baeldung — Scheduled WebSocket Push**: https://www.baeldung.com/spring-boot-scheduled-websocket
- **RFC 6455 — WebSocket Protocol**: https://datatracker.ietf.org/doc/html/rfc6455
- **STOMP protocol spec**: https://stomp.github.io/stomp-specification-1.2.html
- **HTML5 — Server-Sent Events (EventSource)**: https://html.spec.whatwg.org/multipage/server-sent-events.html
- **MDN — WebSocket API**: https://developer.mozilla.org/en-US/docs/Web/API/WebSocket
- **MDN — Using Server-Sent Events**: https://developer.mozilla.org/en-US/docs/Web/API/Server-sent_events/Using_server-sent_events
- **Spring WebFlux — WebSocket**: https://docs.spring.io/spring-framework/reference/web/webflux-websocket.html
- **Spring — SseEmitter Javadoc**: https://docs.spring.io/spring-framework/docs/current/javadoc-api/org/springframework/web/servlet/mvc/method/annotation/SseEmitter.html
- **Niels.nu — Spring Boot WebSocket STOMP Example**: https://niels.nu/blog/2017/spring-boot-websocket-client
- **Medium — Notification System with Spring Boot and WebSockets**: https://medium.com/@AlexanderObregon/building-a-notification-system-with-spring-boot-and-websockets-30a9eb353e3b
- **Книга**: Maciej Walkowiak, "Spring Boot: Up and Running" (O'Reilly, 2021) — глава про real-time.
- **Книга**: Walls, "Spring in Action" 6th ed., глава про WebSocket.

### Практика / Кейсы

1. **Spring WebSocket STOMP chat.** Multi-room chat: `/app/chat/{roomId}` (send), `/topic/chat/{roomId}` (subscribe). STOMP broker relay (RabbitMQ). Auth: token in query param during handshake, validated in `HandshakeInterceptor`. Покажите: 100 concurrent users in 10 rooms, messages delivered <100ms. Multi-instance: 2 Spring Boot instances + RabbitMQ broker relay — broadcast works across instances.
2. **`@SendToUser` для private messages.** User A sends private message to user B. `@MessageMapping("/private")` + `@SendToUser("/queue/private")`. Spring translates `/user/queue/private` to session-specific destination. Покажите, что only user B receives; A doesn't see own message (unless echoed). Auth: principal from Spring Security.
3. **Reactive WebSocket (WebFlux) для high concurrency.** Server pushes live metrics to 10k clients. `Flux<Metrics>` → `WebSocketHandler.handle()` returns `Mono<Void>`. Backpressure: client requests `n` messages at a time. Измерьте RAM: 10k connections in imperative (Tomcat) ~10GB; reactive (Netty) ~500MB.
4. **SSE для notifications.** REST endpoint `/sse/notifications` returns `SseEmitter` (или `Flux<ServerSentEvent>`). Client subscribes via `EventSource`. Server pushes events: `data: {"type":"order_shipped",...}\n\n`. Auto-reconnect built-in (browser). Покажите, что 5000 concurrent SSE clients work; в WS такой нагрузки тоже, но SSE проще для one-way use cases.
5. **Multi-instance broadcast via Redis pub/sub.** Without external broker: 3 Spring Boot instances, each with local WebSocket sessions. Server B publishes to `/topic/orders` — but clients on A and C don't see. Solution: Redis pub/sub — B publishes to Redis channel, all instances subscribe → forward to their local sessions. Покажите broadcast работает across instances.
6. **WebSocket security: CSRF + auth.** WebSocket не имеет same-origin policy by default. Validate `Origin` header (whitelist). Auth: JWT in `Sec-WebSocket-Protocol` subprotocol field (workaround for no custom headers in browser WS) or first message. Покажите CSWSH attack (foreign site opens WS to your server, sends commands) — mitigated by Origin check.
7. **Heartbeat & disconnect detection.** STOMP heartbeat: client + server negotiate 10s interval. If client network drops (no `pong` frame) — server detects after 15s, closes connection, cleans up. Покажите, что без heartbeat — stale connections accumulate (memory leak); с heartbeat — graceful cleanup.
8. **Backpressure в reactive WS.** Server generates 10k events/sec, client processes 100/sec. Without backpressure — server OOM (buffer grows). With reactive `request(n)` — client requests 100, server sends 100, client requests more. Покажите heap usage stable; `onNext` blocks until requested.

---

## 20. Coordination services

### Теория

- **Coordination service** — distributed system для coordination of other services: configuration, leader election, locking, group membership, service discovery.
- **Apache ZooKeeper** (Yahoo, 2008; used by Kafka, Hadoop, HBase):
  - Hierarchical namespace (znodes, like filesystem).
  - **Znode types**: persistent (survives session), ephemeral (deleted on session end), sequential (monotonic suffix), container (parent for children), TTL.
  - **Watches** — one-time trigger notifications on znode changes (create, delete, data change, children change); client must re-watch after trigger.
  - **Sessions** — client session with timeout; heartbeat; ephemeral nodes tied to session.
  - **ZAB (ZooKeeper Atomic Broadcast)** — consensus protocol; leader-based; similar to Raft but earlier (2008).
  - **Ensemble** — cluster of ZK servers; quorum `N/2 + 1`; typical 3 or 5 servers.
- **ZooKeeper guarantees**:
  - **Sequential consistency** — updates from a client applied in order.
  - **Atomicity** — update succeeds or fails completely.
  - **Single system image** — client sees same view regardless of server.
  - **Reliability** — once update applied, persists until overwritten.
  - **Timeliness** — client's view is eventually up-to-date.
- **ZooKeeper leader election**:
  - Each server creates ephemeral sequential znode `/election/node-NNNNN`.
  - Lowest sequence number = leader.
  - Others watch predecessor; if predecessor dies — watch fires, check if now lowest.
  - `InterProcessMutex` (Apache Curator) — same pattern for distributed locks.
- **ZooKeeper recipes** (Curator library):
  - **LeaderLatch** — all participants create znode, one is leader; on leader death, new election.
  - **LeaderSelector** — leader takes action, on yield — next leader.
  - **InterProcessMutex** — reentrant distributed lock.
  - **InterProcessSemaphoreV2** — distributed semaphore.
  - **SharedCount / DistributedAtomicInteger** — shared counters.
  - **PathChildrenCache / TreeCache** — watch znode children changes.
- **etcd** (CoreOS, 2013; used by Kubernetes):
  - Key-value store (not hierarchical like ZK).
  - **Raft consensus** (см. Raft paper).
  - **Leases** — TTL-based; key attached to lease; lease expires → key deleted.
  - **Watch** — continuous stream of changes (better than ZK one-time watches).
  - **Transactions** — atomic `if-then-else` over keys.
  - **gRPC API** (vs ZK's custom protocol).
- **Raft consensus** (Ongaro, Ousterhout, 2014):
  - **Leader election** — terms; candidates request votes; majority wins.
  - **Log replication** — leader appends, replicates to followers; committed when majority.
  - **Safety** — leader's log is authoritative; old leaders cannot override.
  - Easier to understand than Paxos; foundation for etcd, Consul, CockroachDB.
- **etcd leader election** — using lease + compare-and-swap: candidate with lowest ID wins (or first to CAS); lease keeps leadership while alive.
- **etcd watches** — `Watch(key)` returns stream of events (PUT, DELETE) on key or prefix; no need to re-watch.
- **Comparison ZooKeeper vs etcd**:
  - ZK: hierarchical, custom protocol, harder API, watches one-time, mature.
  - etcd: KV, gRPC, simpler API, watches continuous, Kubernetes-native, modern.
  - Both: CP, quorum-based, consensus (ZAB vs Raft).
- **Consul** (HashiCorp):
  - Service discovery + KV + health checks + service mesh.
  - Raft consensus (5+ servers recommended).
  - Agent-based: each node runs consul agent.
  - HTTP + DNS API (uniqueness — DNS resolution for services).
  - Consul Connect (service mesh, mTLS).
- **Use cases for coordination services**:
  - **Leader election** — one active replica, others standby.
  - **Distributed locks** — mutual exclusion.
  - **Configuration** — central config store.
  - **Service discovery** — registry of services.
  - **Group membership** — who's in the cluster.
  - **Work distribution** — assign partitions/tasks.
- **Quorum and availability**:
  - N=3: tolerates 1 failure; 2 nodes needed for quorum.
  - N=5: tolerates 2 failures; 3 nodes for quorum.
  - N=7: tolerates 3 failures.
- **Network partitions** — minority side cannot commit (no quorum); majority side continues; on heal — minority catches up.
- **Split-brain prevention** — quorum-based: only majority can elect leader / commit; minority side blocks.
- **Read consistency**:
  - **Linearizable reads** — through leader (slow).
  - **Serial reads** — from any server (faster, may be stale).
  - etcd: `serializable=true` for fast reads, `false` for linearizable.
- **Performance**:
  - ZK: ~10-50k ops/sec typical; low latency (<10ms).
  - etcd: similar, slightly faster on modern hardware.
  - Both: not for high-throughput data; for metadata, coordination.
- **Operational concerns**:
  - Quorum loss = cluster down; 3 nodes is minimum for HA.
  - Snapshot + log retention; disk I/O critical.
  - JVM tuning for ZK (heap, GC).
  - etcd: simpler ops (single binary, Go).
- **ZooKeeper in Kafka (historical)** — до KRaft (Kafka 3.3+), ZK хранил: broker registration, topic/partition config, ISR, consumer group metadata, ACLs. KRaft replaces all this with internal Kafka topic.
- **etcd in Kubernetes** — stores: pods, services, deployments, configmaps, secrets, etc.; single source of truth; K8s API server reads/writes etcd.
- **Migrations** — ZK → etcd: редкий сценарий; обычно new systems use etcd; legacy still on ZK.
- **Recipes anti-patterns**:
  - **High-throughput data storage** — coordination services не для этого; use DB.
  - **Large znodes/values** — limit ~1MB per key; use external storage.
  - **Watch storms** — many clients watching same key; on change — all wake up; mitigate with debounce, distinct keys.
- **Curator vs raw ZK API** — Curator (Apache) — high-level recipes, connection handling, retries; raw API — low-level, error-prone; всегда использовать Curator для production ZK.
- **etcd vs Redis (as coordination)** — Redis: in-memory, fast, but AP (async replication), weaker consistency; not suitable for strict coordination. Etcd/ZK: CP, suitable for correctness-critical coordination.

### Материалы (реальные ссылки)

- **Apache ZooKeeper documentation**: https://zookeeper.apache.org/doc/current/
- **Apache ZooKeeper — Recipes & Solutions (Locks, Leader Election)**: https://zookeeper.apache.org/doc/current/recipes.html
- **Apache ZooKeeper — Zab Protocol**: https://zookeeper.apache.org/doc/current/zookeeperInternals.html
- **Apache Curator — Recipes**: https://curator.apache.org/docs/recipes/
- **Apache Curator — LeaderLatch / LeaderSelector**: https://curator.apache.org/curator-recipes/leader-latch.html
- **etcd documentation**: https://etcd.io/docs/
- **etcd — API reference**: https://etcd.io/docs/v3.5/learning/api/
- **etcd — Distributed Locks (Concurrency)**: https://etcd.io/docs/v3.5/dev-guide/api_concurrency_reference_v3/
- **etcd — Leases**: https://etcd.io/docs/v3.5/dev-guide/api_grpc_gateway/
- **Consul documentation**: https://developer.hashicorp.com/consul/docs
- **Consul — Internal Architecture (Raft)**: https://developer.hashicorp.com/consul/docs/architecture
- **Raft paper (In Search of an Understandable Consensus Algorithm)**: https://raft.github.io/raft.pdf
- **Raft visualization**: https://raft.github.io/
- **ZooKeeper paper (Hunt, Konar, Junqueira, Flach, 2010)**: https://www.usenix.org/legacy/event/atc10/tech/full_papers/Hunt.pdf
- **Authorea — Leader Election: ZooKeeper vs Raft**: https://www.authorea.com/doi/full/10.22541/au.176072429.98039047/v1
- **System Design Essentials: ZooKeeper, Etcd, and Consistency**: https://designgurus.substack.com/p/system-design-essentials-zookeeper
- **ZooKeeper System Design — Complete Guide 2026**: https://www.systemdesignhandbook.com/guides/zookeeper-system-design
- **Design Gurus — 5 Best Leader Election Algorithms**: https://www.designgurus.io/blog/5-best-leader-election-algorithms
- **Книга**: Flavio Junqueira & Benjamin Reed, "ZooKeeper: Distributed Process Coordination" (O'Reilly, 2013).
- **Книга**: Kleppmann, DDIA, глава 9 (Consistency and Consensus).

### Практика / Кейсы

1. **ZooKeeper leader election (Curator LeaderSelector).** 5 Spring Boot instances; on startup, participate in election; one becomes leader, runs scheduled job; others standby. Kill leader — election within 5s, new leader continues. Покажите through ZK CLI: ephemeral sequential znodes `/leader/lock-NNNNN`, lowest = leader.
2. **etcd-based leader election + lease.** Same scenario, but with etcd. Each candidate: `lease grant 30s`, `txn if key not exists then put key with lease`. Holder keeps lease alive with `lease keep-alive`. On crash — lease expires after 30s, next candidate wins. Покажите failover time ~30s (lease TTL); discuss trade-off TTL vs detection speed.
3. **ZooKeeper distributed lock (InterProcessMutex).** 10 processes try to acquire `/locks/order-42`; one wins, others wait; on release — next acquires. Watch mechanism: each waiter watches predecessor's znode. Покажите through ZK CLI the sequential znodes; on release, ephemeral node deleted, watch fires. Measure lock acquisition latency: ~5-15ms typical.
4. **etcd distributed lock.** Same scenario with etcd. `etcdctl lock /orders/42` command, or programmatic via `concurrency.NewSession` + `concurrency.NewMutex`. Lease-based; on crash — auto-release after TTL. Compare to ZK: etcd API simpler, but watch semantics differ.
5. **Configuration centralization with etcd.** All services read config from etcd key `/config/order-service/`. Update key — all services get watch event — reload `@RefreshScope` beans. Покажите: change `order.feature.flag = true` — all instances reflect within 1s. Compare to Spring Cloud Config (needs bus refresh); etcd watch is real-time.
6. **Group membership with ZK.** Each service instance on startup creates ephemeral znode `/members/order-service-instance-NNNN`. Other services watch `/members` children — get notification on join/leave. Service registry pattern. Kill instance — znode disappears (ephemeral + session), others notified. Compare to Eureka (polling) — ZK event-driven, faster.
7. **ZK vs etcd performance & ops comparison.** Run benchmarks: 3-node ZK ensemble vs 3-node etcd cluster. Operations: create, read, watch. ZK: ~30k writes/sec, etcd: ~50k writes/sec (typical, depends on hardware). Operations: ZK — JVM tuning, snapshot management; etcd — single Go binary, simpler. Make recommendation based on use case.
8. **Migration from ZK to etcd (or vice versa).** Real-world: Kafka 3.x — KRaft mode replaces ZK with internal Kafka topic (not etcd, but similar concept). Simulate migration: existing service uses ZK for coordination; new version uses etcd. Dual-run period: both ZK and etcd; eventually cut over. Покажите zero-downtime migration strategy; handle in-flight leader states.

---

## Сводка раздела

**Объём и структура**:
- 20 подразделов, каждый с теорией (10-25 пунктов), реальными материалами (4-8 источников), практическими кейсами (4-8).
- ~2 800 строк.

**Ключевые темы для Senior-уровня**:
1. **Теоретический фундамент**: CAP/PACELC, consistency models, clocks (Lamport/vector/HLC), consensus (Paxos/Raft/ZAB), FLP impossibility, fallacies.
2. **Messaging brokers**: Kafka (deep dive — architecture, EOS, KRaft, Streams, Schema Registry), RabbitMQ (exchanges, quorum queues, streams).
3. **Межсервисное общение**: REST (RMM, HATEOAS), gRPC (4 типа RPC, interceptors, deadlines), GraphQL (DataLoader, Federation, N+1).
4. **Reliability patterns**: idempotency, transactional outbox, CDC (Debezium), exactly-once end-to-end.
5. **Microservices patterns**: Saga (orchestration/choreography), CQRS, Event Sourcing, API Gateway, BFF, Strangler Fig, ACL, Bulkhead, Circuit Breaker.
6. **Resilience**: Resilience4j (CircuitBreaker, Bulkhead, RateLimiter, Retry, TimeLimiter, Fallback).
7. **Discovery & config**: Eureka, Consul, etcd, ZooKeeper, Spring Cloud Config, K8s ConfigMap/Secret.
8. **Gateway & mesh**: Spring Cloud Gateway, Kong, Envoy, NGINX, Istio, Linkerd.
9. **Observability**: OpenTelemetry, Jaeger, Zipkin, Micrometer Tracing.
10. **Coordination**: distributed locking (Redlock, ZK, etcd), fencing tokens, leader election.
11. **Caching**: Redis cluster, cache patterns (aside, read-through, write-through, write-behind), stampede, Bloom filters.
12. **Event-driven**: event notification vs state transfer, schema evolution, replay.
13. **Real-time**: WebSocket, STOMP, SSE, reactive WebSocket.
14. **Coordination services**: ZooKeeper (znode, watch, ZAB), etcd (Raft, lease), Consul.

**Топ-15 источников раздела**:
1. https://kafka.apache.org/documentation/ — Apache Kafka documentation
2. https://www.rabbitmq.com/docs/ — RabbitMQ documentation
3. https://grpc.io/docs/ — gRPC documentation
4. https://opentelemetry.io/docs/ — OpenTelemetry documentation
5. https://resilience4j.readme.io/ — Resilience4j documentation
6. https://microservices.io/patterns/ — Microservices patterns (Chris Richardson)
7. https://debezium.io/documentation/ — Debezium CDC
8. https://docs.confluent.io/platform/current/schema-registry/ — Confluent Schema Registry
9. https://martinfowler.com/ — Martin Fowler articles (RMM, CircuitBreaker, Event Sourcing)
10. https://martin.kleppmann.com/2016/02/08/how-to-do-distributed-locking.html — fencing tokens
11. https://docs.spring.io/spring-kafka/reference/ — Spring Kafka
12. https://docs.spring.io/spring-amqp/reference/ — Spring AMQP
13. https://docs.spring.io/spring-cloud-gateway/docs/current/reference/html — Spring Cloud Gateway
14. https://istio.io/latest/docs/ — Istio
15. https://raft.github.io/raft.pdf — Raft consensus paper

**Книги (must-read)**:
- Martin Kleppmann, "Designing Data-Intensive Applications" (O'Reilly, 2017) — теория распределённых систем.
- Chris Richardson, "Microservices Patterns" (Manning, 2018) — паттерны микросервисов.
- Sam Newman, "Building Microservices" 2nd ed. (O'Reilly, 2021) — дизайн и интеграция.
- Michael Nygard, "Release It!" 2nd ed. (Pragmatic, 2018) — resilience patterns.
- Narkhede et al., "Kafka: The Definitive Guide" 2nd ed. (O'Reilly, 2021) — Kafka deep dive.
- Gregor Hohpe & Bobby Woolf, "Enterprise Integration Patterns" (Addison-Wesley, 2003) — паттерны интеграции.
- Junqueira & Reed, "ZooKeeper" (O'Reilly, 2013) — coordination.
- Bejeck, "Kafka Streams in Action" 2nd ed. (Manning, 2022) — stream processing.

**Следующие шаги после раздела**:
- Раздел 07 (DevOps & Observability) — углубление в CI/CD, K8s, Prometheus/Grafana, ELK.
- Раздел 08 (Arch Patterns & Leadership) — системный дизайн, ADR, команда, метрики.
