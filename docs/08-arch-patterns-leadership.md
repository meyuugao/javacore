# Раздел 8. Architecture, Design Patterns, Algorithms & Senior Leadership

> Цель раздела — провести Java-инженера от уровня «умею применить паттерн из учебника» до уровня Senior/Staff-инженера: системно мыслить в архитектурах (Layered/Hexagonal/Clean/DDD), владеть паттернами GoF с примерами из JDK, проектировать масштабируемые системы (Twitter/URL-shortener/chat), уверенно решать LeetCode Hard (DP/graphs/monotonic stack), понимать performance engineering (Little's Law, Amdahl, queueing theory), руководить командой (code review, ADR, RFC, менторство), отвечать за безопасность (OWASP Top 10, STRIDE) и осознанно двигаться по карьерной лестнице Junior → Middle → Senior → Staff → Principal.
>
> Связь с другими разделами:
> - **01 (Java Core)**: reflection, sealed interfaces, records — основа для паттернов GoF.
> - **02 (Java Advanced)**: JMM, ConcurrentHashMap, lock-free — фундамент concurrent data structures.
> - **03 (Build/Test)**: JUnit 5, Testcontainers, mutation testing (PITest), code coverage.
> - **04 (Spring)**: IoC/DI в Spring, AOP-паттерны, @Transactional как Application Service.
> - **05 (Persistence)**: Repository pattern, Aggregate boundaries, sharding.
> - **06 (Distributed)**: CQRS, Event-Driven, saga, idempotency, eventually consistent.
> - **07 (DevOps)**: SLO-driven roadmap, observability-driven architecture, postmortem-культура.

## Содержание

1. SOLID принципы (SRP/OCP/LSP/ISP/DIP) + DRY/KISS/YAGNI/Law of Demeter
2. Паттерны GoF (Creational/Structural/Behavioral) — реализация на Java, примеры из JDK
3. Dependency Injection паттерны (constructor/setter/field, IoC containers, Service Locator)
4. Архитектурные паттерны (Layered, Hexagonal, Onion, Clean, CQRS, Event-Driven, Microservices, Monolith, Serverless)
5. Domain-Driven Design (Ubiquitous Language, Bounded Context, Aggregate, Repository, Domain Events)
6. System Design (capacity estimation, load balancing, caching, sharding, latency numbers, back-of-envelope)
7. System Design кейсы (Twitter, URL shortener, chat, news feed, notification system, rate limiter)
8. Алгоритмы и структуры данных (массивы, списки, стек, очередь, heap, hashmap, BST/AVL/Red-Black, Trie, графы, union-find)
9. Алгоритмы (sorting, searching, graphs, DP, backtracking, greedy, divide&conquer, sliding window, two pointers, monotonic stack)
10. Concurrent data structures (ConcurrentHashMap, SkipList, Michael-Scott queue, Treiber stack, Disruptor)
11. Clean Code & Refactoring (code smells, Fowler refactoring catalog, Boy Scout rule, technical debt quadrant)
12. Code Review (checklist, security/performance review, anti-patterns, constructive feedback, Gerrit/GitHub PRs)
13. Testing strategy (test pyramid, trophy, ICE score, mutation testing, microservices testing)
14. Документация и коммуникация (RFC, ADR, technical specs, C4, PlantUML/Mermaid, Sequence diagrams)
15. Senior soft skills (менторство, техническое лидерство, stakeholder management, переговоры, конфликты, delegation)
16. Agile/Lean/DevOps культуры (Scrum/Kanban/XP, estimation, retrospectives, WIP limits, lead/cycle time, flow efficiency)
17. Performance engineering (latency vs throughput, Amdahl/Gustafson, Little's Law, queueing theory M/M/1, profiler-driven optimization)
18. Security для разработчика (OWASP Top 10, secure coding, STRIDE/DREAD, secrets management, ASVS, SAST/DAST/SCA)
19. Cost optimization (cloud cost, FinOps, right-sizing, spot instances, reserved capacity, multi-tenancy, autoscaling)
20. Career path (Junior→Middle→Senior→Staff→Principal; staff engineer archetypes, influence without authority, tech radar, OSS, conferences, writing)

---

## 1. SOLID принципы (SRP/OCP/LSP/ISP/DIP) + DRY/KISS/YAGNI/Law of Demeter

### Теория

- **SRP (Single Responsibility Principle)** — класс имеет только одну причину для изменения (один actor/stakeholder). «Responsibility» здесь = «причина изменения», а не «делает одну вещь». Класс `Invoice` не должен одновременно считать налоги и сохранять в БД — у них разные stakeholders (FinOps vs DBA).
- **OCP (Open/Closed Principle)** — открыт для расширения, закрыт для модификации. Достигается через Strategy/Template Method/Plugin. В Java — `sealed interface` + `permits` + pattern matching `switch` (JEP 441), `ServiceLoader` для плагинов.
- **LSP (Liskov Substitution Principle)** — подтип `T` можно подставить везде, где ожидается `S`, без нарушения контракта. Контракт включает: предусловия не усиливаются, постусловия не ослабляются, инварианты сохраняются, history constraint (mutable через immutable). Классика: `Square extends Rectangle` нарушает LSP, т.к. меняет инвариант независимости сторон.
- **ISP (Interface Segregation Principle)** — клиенты не должны зависеть от методов, которые они не используют. `Fat interface` (`Worker` с `work()` и `eat()`) → разбить на `Workable` + `Eatable`. В Java — default methods (Java 8+) усугубляют риск fat-interface.
- **DIP (Dependency Inversion Principle)** — модули верхнего уровня не зависят от нижних; оба зависят от абстракций. Абстракции не зависят от деталей, детали зависят от абстракций. Реализуется через DI-контейнер (Spring, Guice) + интерфейсы в доменном слое, имплементации — во внешних слоях.
- **DRY (Don't Repeat Yourself)** — «Every piece of knowledge must have a single, unambiguous, authoritative representation within a system» (Hunt & Thomas, Pragmatic Programmer). Дублировать можно код (acceptable incidental duplication), но не знания (business rule duplication — зло). В Java — extracted helper, `@Configurable`, `@MappedSuperclass` JPA.
- **KISS (Keep It Simple, Stupid)** — simplicity — это фича. Избегать преждевременной абстракции, лишних слоёв, smart-кода. «Debugging is twice as hard as writing the code in the first place. Therefore, if you write the code as cleverly as possible, you are, by definition, not smart enough to debug it» (Kernighan).
- **YAGNI (You Aren't Gonna Need It)** — из XP (Beck). Не реализуй фичу, пока она реально нужна. «Always implement things when you actually need them, never when you just foresee that you need them» (Ron Jeffries). Связан с rule of three для абстракций.
- **Law of Demeter (Principle of Least Knowledge)** — объект `O` может вызывать методы только у: самого себя, своих полей, параметров метода, объектов созданных в методе, своих компонентов. Запрет `a.getB().getC().do()` — train wreck. Мотивация: снижение coupling. В DDD — это аргумент за Aggregate Root как единую точку доступа.
- **Connascence** (Page-Jones) — обобщение coupling: Connascence of Name (хорошо, refactor-safe), Connascence of Position (плохо, порядок аргументов), Connascence of Meaning (магические числа), Connascence of Execution (порядок вызовов), Connascence of Algorithm (две реализации одного алгоритма).
- **Composition over Inheritance** — наследование нарушает инкапсуляцию подкласса (вызовы переопределённых методов из конструктора суперкласса — bug из Effective Java Item 19). Composition + delegation + interface → гибче.
- **Hollywood Principle** — «Don't call us, we'll call you». Inversion of Control: фреймворк вызывает твой код (callback), а не наоборот. Основа Spring, JUnit, Servlet API.
- **Robustness Principle (Postel's Law)** — «Be conservative in what you send, be liberal in what you accept». Опасен при парсинге (приводит к parser differentials, security bugs); в современном API-дизайне лучше explicit schema (Protobuf/JSON Schema).
- **Fail Fast** — валидируй вход в конструкторе/фабрике, бросай `IllegalArgumentException`/`NullPointerException` (через `Objects.requireNonNull`) немедленно. Сокращает debugging. Связан с defensive programming — баланс с Postel.
- **Principle of Least Astonishment** — API ведёт себя так, как ожидает пользователь. `boolean delete()` не должен молча возвращать `false`, если файл не существует, а должен кидать.
- **Rule of Three** — третья дубликация — повод к рефакторингу (extract). Связан с YAGNI: первая — пиши, вторая — задумайся, третья — абстрагируй.
- **SOLID vs Agile** — SOLID не означает «больше слоёв». Плохо понятый SOLID ведёт к Ravioli Code (микро-классы) и Architecture Astronaut syndromes. Цель — направления зависимостей и изоляция изменений, а не количество интерфейсов.

### Материалы

- **Книга**: "Clean Architecture: A Craftsman's Guide to Software Structure and Design", Robert C. Martin (Pearson, 2017) — Part III и IV: SOLID + компоненты.
- **Книга**: "Agile Principles, Patterns, and Practices in C#", Robert C. Martin (Pearson, 2006) — оригинальное изложение SOLID с UML.
- **Книга**: "The Pragmatic Programmer: Your Journey to Mastery" 20th Anniversary Edition, Hunt & Thomas (Addison-Wesley, 2019) — DRY, Orthogonality, Tracer Bullet.
- **Статья**: Martin Fowler — https://martinfowler.com/bliki/TellDontAsk.html (Tell-Don't-Ask, альтернатива Demeter для DDD).
- **Статья**: "Software Design Principles Every Programmer Should Know" — https://medium.com/@peterlee2068/software-design-principles-every-programmer-should-know-c164a83c6f87
- **Статья**: "SOLID Dry Kiss Yagni" — https://www.mohitkhare.com/blog/solid-dry-kiss-yagni
- **Статья**: "What Is Software Design?" — https://bitloops.com/resources/software-design/what-is-software-design

### Практика / Кейсы

1. **Рефакторинг God Class `InvoiceService` (1200 строк, 17 зависимостей, 4 stakeholders)**. Шаги: построить feature map (какие методы для какого stakeholder) → выделить `TaxCalculator` (SRP, отвечает перед FinOps), `InvoiceRepository` (SRP, DBA), `InvoicePdfRenderer` (SRP, Customer Support) → ввести `Invoice` как Aggregate Root, `InvoiceService` остаётся Application Service → ввести интерфейсы `TaxCalculator`, `InvoiceRepository` (DIP) → инжектить через Spring `@Autowired` constructor → покрыть каждый новый класс unit-тестами → удалить мёртвый код. Контрольная проверка: изменения в налоговом коде не требуют правок в репозитории.

2. **LSP-нарушение в иерархии `Account` → `SavingsAccount`/`CheckingAccount`/`LockedAccount`**. Симптом: код вызывает `account.withdraw(amount)` и ловит `UnsupportedOperationException` для `LockedAccount` в runtime. Шаги: признать нарушение LSP → ввести sealed interface `Account` permits `Withdrawable`, `Lockable` (JEP 409) → pattern matching `switch (account) { case Withdrawable w -> w.withdraw(amount); ... }` → в тестах.contract tests: для каждого подтипа проверить все инварианты. Урок: лучше узкий интерфейс с `permits`, чем широкий абстрактный класс.

3. **Применить Law of Demeter к цепочке `order.getCustomer().getAddress().getCity()` в reporting-сервисе**. Шаги: ввести метод `order.getShippingCity()` на Aggregate Root (Tell-Don't-Ask) → для аналитики (где Demeter мешает) — ввести DTO `OrderProjection` через query-сторону (CQRS read-model) с flat structure → для маппинга использовать MapStruct (compile-time generated) → в тестах проверить, что `Order` не утекает customer internals. Trade-off: Demeter хорош для command-side (DDD), но вредит query-side (нужен DTO-проекция).

4. **YAGNI vs Architecture Flexibility для платежного шлюза (3 провайдера пока)**. Шаги: НЕ делать `PaymentProvider` interface + Strategy + plugin-loader сразу → написать `StripeService` конкретно (rule of one) → когда добавляется 2-й провайдер — extract `PaymentProvider` interface + два реализации + factory → когда 3-й — добавить plugin через `ServiceLoader` + ADR с фиксацией интерфейса. Урок: каждая абстракция должна оплатить свою стоимость реальным second use case.

5. **KISS валидация против «smart» generic-framework подхода**. Команда предлагает «универсальный rule engine» на Drools для 5 бизнес-правил скидок. Шаги: оценка TCO (learning curve, debugging, runtime perf) → counter-proposal: 5 явных стратегий `DiscountStrategy` с `if/else` в `DiscountService` → benchmark: 5 ns vs 200 µs (Drools RETE) → ADR фиксирующий выбор + критерий «переехать на Drools если > 50 правил или они редактируются бизнесом». Принцип: KISS — это не лень, а осознанное сокращение accidental complexity.

6. **DRY-анализ двух реализаций retry-логики в `RestClient` и `KafkaProducer`**. Шаги: обнаружить «knowledge duplication» (один и тот же exponential backoff + jitter, разные имена) → extract `RetryPolicy` value object (`maxAttempts`, `initialDelay`, `multiplier`, `jitter`) + `RetryExecutor` (Resilience4j или свой) → покрыть property-based тестами (jqwik) на инвариант «attempts ≤ maxAttempts» → ввести конфигурацию через Spring `@ConfigurationProperties`. Урок: дублировать можно boilerplate, нельзя — policy.

7. **DIP на уровне модулей Maven multi-module**. Структура: `domain` (pure Java, без Spring) ← `application` (use cases, Spring `@Service`) ← `infrastructure` (JPA, REST adapters) ← `web` (controllers). Шаги: в `domain/pom.xml` запретить spring-data-jpa (maven-enforcer-plugin `banned-dependencies`) → интерфейс `OrderRepository` живёт в `domain`, реализация `JpaOrderRepository` в `infrastructure` → `application` зависит только от `domain` → Spring `@ComponentScan` с фильтрами по слоям → архитектурный тест через ArchUnit: `classes().that().resideInAPackage("..domain..").should().onlyDependOnClassesThat().resideInAnyPackage("..domain..", "java..")`.

---

## 2. Паттерны GoF — реализация на Java, примеры из JDK

### Теория

- **Порождающие (Creational)** — изоляция создания объектов: Singleton, Factory Method, Abstract Factory, Builder, Prototype, Object Pool.
- **Singleton**: lazy vs eager vs holder idiom vs enum. В Java — `enum Singleton { INSTANCE; }` (Effective Java Item 3) — serialization-safe, reflection-safe (с Java 5+), thread-safe без `synchronized`. JDK: `Runtime.getRuntime()`, `System` (через static), `Desktop`. Антипаттерн: глобальное состояние, тестируемость, нарушение SRP.
- **Factory Method**: `Creator.product()` возвращает `Product`. JDK: `java.util.List.of()`, `java.net.URL.openConnection()` (возвращает `URLConnection`), `java.nio.charset.Charset.forName()`, `java.util.ResourceBundle.getBundle()`. В Java 21+ — record + static factory `of()`, alternative to constructor.
- **Abstract Factory**: семейство связанных объектов. JDK: `javax.xml.parsers.DocumentBuilderFactory` (создаёт `DocumentBuilder` → `Document`), `java.sql.Connection` (создаёт `Statement`, `PreparedStatement`, `CallableStatement`), `Toolkit.getDefaultToolkit()` (AWT peer). Spring `BeanFactory`/`ApplicationContext` — Abstract Factory для bean-ов.
- **Builder**: разделение конструирования сложного объекта от представления. Variant 1: GoF Builder (abstract director). Variant 2: Effective Java Item 2 Builder (fluent, immutable). JDK: `StringBuilder` (mutable), `Stream.Builder`, `Locale.Builder`, `HttpRequest.Builder`. Lombok `@Builder`. Immutable vs telescoping constructor (10+ параметров).
- **Prototype**: клонирование через `Cloneable` + `Object.clone()` (broken в Java — Effective Java Item 13) или copy-constructor / static factory `from(other)`. JDK: `ArrayList.clone()` (shallow), `HashMap.clone()`. Records + `with`-pattern (JDK proposal) — modern alternative.
- **Object Pool**: переиспользование дорогих объектов (DB connections, threads). JDK: `java.util.concurrent.ThreadPoolExecutor` (pool of workers), `javax.sql.DataSource` (HikariCP). commons-pool2 для generic. Опасность: leak, stale state, synchronization overhead. Дорога только если создание > 1 ms.
- **Структурные (Structural)** — композиция объектов: Adapter, Bridge, Composite, Decorator, Facade, Flyweight, Proxy.
- **Adapter**: конвертация интерфейса. JDK: `java.io.InputStreamReader(InputStream → Reader)`, `java.util.Arrays.asList(T[] → List<T>)` (fixed-size), `java.util.Collections.enumeration(Collection → Enumeration)`. Class-adapter (multiple inheritance, в Java недоступно) vs Object-adapter (composition, standard).
- **Bridge**: разделение abstraction и implementation, оба расширяются отдельно. JDK: `JDBC` (`Driver` bridge to vendor-specific), `java.util.logging` (`Handler` + `Formatter`), `AbstractButton` + `ButtonUI` в Swing. Позволяет менять UI-look independently от button logic.
- **Composite**: дерево из uniform interface. JDK: `java.awt.Container` (содержит `Component`, который может быть `Container`), `java.util.List<List<T>>`, DOM `Node`. Pattern: `interface Component { void operation(); } class Leaf implements Component {} class Composite implements Component { List<Component> children; }`.
- **Decorator**: wraps объект, добавляет поведение, тот же интерфейс. JDK: `java.io.InputStream` → `BufferedInputStream` → `DataInputStream`, `java.util.Collections.synchronizedList()`, `java.util.Collections.unmodifiableList()`. В Java 8+ — `Function<T,T>` decorators: `s -> log(s) -> validate(s) -> s`.
- **Facade**: упрощённый интерфейс к подсистеме. JDK: `java.util.Collections` (facade для Collection algorithms), `java.net.URL` (facade для socket + DNS + http), `Spring ApplicationContext` (facade для BeanFactory + ResourceLoader + EventPublisher). Не нарушает инкапсуляцию подсистем — просто агрегирует.
- **Flyweight**: разделение общего состояния (intrinsic) и контекстного (extrinsic). JDK: `Integer.valueOf(int)` cache `-128..127`, `String` intern pool (`String.intern()`), `Boolean.TRUE/FALSE`, `BigDecimal.ZERO/ONE/TEN`. В Java 19+ — `Matcher` из `Pattern` thread-local. Память-экономия для огромного количества мелких объектов.
- **Proxy**: placeholder, контролирующий доступ. JDK: `java.lang.reflect.Proxy` (dynamic), `java.rmi.*` stubs, Spring AOP proxies (JDK dynamic или CGLIB), Hibernate lazy-loading proxy, `java.nio.ByteBuffer` (direct vs heap). Types: virtual (lazy), remote (RMI), protection (security), smart (cache/log).
- **Поведенческие (Behavioral)** — распределение ответственности: Chain of Responsibility, Command, Interpreter, Iterator, Mediator, Memento, Observer, State, Strategy, Template Method, Visitor.
- **Chain of Responsibility**: запрос проходит по цепочке обработчиков. JDK: `java.util.logging.Logger` (propagate to parent handlers), servlet `FilterChain`, Spring `HandlerInterceptor` chain, `javax.servlet.Filter`. Reactive: `Mono<T>.transformDeferred()` operators chain.
- **Command**: инкапсуляция запроса в объект. JDK: `Runnable` (command без аргументов/return), `Callable<V>`, `java.awt.Action`, Spring `ApplicationEvent`. Позволяет undo/redo, queue, log. Macro = `Composite` of commands.
- **Interpreter**: AST + evaluate. JDK: `java.util.regex.Pattern` (regex engine), `java.text.Normalizer`, SpEL `SpelExpressionParser`. Patterns: visitor-based interpreter, recursive descent.
- **Iterator**: последовательный доступ к элементам без раскрытия структуры. JDK: `java.util.Iterator` (+ `ListIterator` bidirectional), `java.util.stream.Stream` (lazy iterator), `Spliterator` (parallel). Fail-fast (`ConcurrentModificationException`) vs fail-safe (`CopyOnWriteArrayList`).
- **Mediator**: централизует коммуникацию между коллегами. JDK: `java.util.Timer` (медиатор между TimerTask-ами), `java.util.concurrent.ExecutorService`, Spring `ApplicationContext` (mediator между beans), `java.util.Observable` (deprecated).
- **Memento**: снимок состояния для отката. JDK: `java.io.Serializable` (serialization as memento), `javax.swing.undo.UndoableEdit`, git stash. Pattern: `Originator.save()` → `Memento`, `Originator.restore(Memento)`.
- **Observer**: publisher-subscriber. JDK: `java.util.Observable`/`Observer` (deprecated с Java 9), `java.beans.PropertyChangeListener`, Spring `ApplicationEventPublisher` + `@EventListener`, `Flow.Subscriber`/`Flow.Subscription` (reactive streams, Java 9+). В Java 21+ — virtual threads + `BlockingQueue` проще, чем reactive.
- **State**: поведение зависит от состояния, состояния — объекты. JDK: `java.lang.Thread.State` (NEW/RUNNABLE/BLOCKED/WAITING/TIMED_WAITING/TERMINATED) — это enum state, но transitions external; `Matcher` state implicitly. Spring State Machine — library.
- **Strategy**: семейство алгоритмов, interchangeable. JDK: `java.util.Comparator` (strategy для сортировки), `java.util.function.Function`/`Predicate`/`Consumer`, `ThreadPoolExecutor.RejectedExecutionHandler` (AbortPolicy/CallerRunsPolicy/DiscardPolicy/DiscardOldestPolicy).
- **Template Method**: скелет алгоритма в суперклассе, шаги — в подклассах. JDK: `java.util.AbstractList` (`get(int)`, `size()` абстрактны, `indexOf` использует их), `java.io.InputStream` (`read(byte[],int,int)` реализован через `read()`), `HttpServlet.doGet/doPost` + `service()`. В Java 8+ — default methods дают альтернативу.
- **Visitor**: двойная диспетчеризация, добавление операций без изменения классов. JDK: `javax.lang.model.element.ElementVisitor` (annotation processing), `java.nio.file.FileVisitor` (`Files.walkFileTree`), ASM bytecode library. Опасность: нарушение LSP при добавлении новых ConcreteElement — нужно менять все Visitor.
- **Паттерны и Java-features**: sealed interfaces + pattern matching (JEP 441) делают Visitor менее нужным (можно `switch` по `permits`). Records упрощают Builder/Value Object. Virtual threads уменьшают потребность в Object Pool для threads. Lombok `@Builder`/`@Singleton` — boilerplate killer.

### Материалы

- **Книга**: "Design Patterns: Elements of Reusable Object-Oriented Software", Gamma/Helm/Johnson/Vlissides (Addison-Wesley, 1994) — канон GoF.
- **Книга**: "Head First Design Patterns" 2nd ed., Freeman & Robson (O'Reilly, 2021) — современное (Java 8+) изложение.
- **Книга**: "Effective Java" 3rd ed., Joshua Bloch (Addison-Wesley, 2018) — Items 2–5 (Builder, Singleton, Factory), Item 13 (Cloneable), Item 19 (inheritance vs composition).
- **Статья**: "Examples of GoF Design Patterns in Java's core libraries" — https://stackoverflow.com/questions/1673841/examples-of-gof-design-patterns-in-javas-core-libraries
- **Статья**: "Most Common Design Patterns in Java" — https://www.digitalocean.com/community/tutorials/java-design-patterns-example-tutorial
- **Статья**: "GoF Design Patterns in Java: The Complete Guide" — https://ankurm.com/gof-design-patterns-java
- **Сайт**: refactoring.guru — https://refactoring.guru/design-patterns (с UML и code на Java).

### Практика / Кейсы

1. **Реализовать thread-safe Singleton с lazy init, reflection-safe, serialization-safe**. Шаги: вариант 1 — `enum Singleton { INSTANCE; public void doWork(){} }` (рекомендация Bloch, но не поддерживает lazy runtime init) → вариант 2 — holder idiom:
   ```java
   public class Singleton {
     private Singleton() {}
     private static class Holder { static final Singleton INSTANCE = new Singleton(); }
     public static Singleton instance() { return Holder.INSTANCE; }
   }
   ```
   → вариант 3 (если нужны параметры) — `Double-Checked Locking` с `volatile` → покрыть тестом через `SerializationTest` (serialize/deserialize → same instance) → для reflection-защиты бросать исключение в приватном конструкторе если `instance != null`. Урок: в 95% случаев выбирай `enum`, в остальных — holder.

2. **Реализовать Composite + Visitor для AST арифметического выражения**. Шаги: `sealed interface Expr permits Num, Add, Mul {}` → `record Num(double v) implements Expr` → `record Add(Expr l, Expr r) implements Expr` → `record Mul(Expr l, Expr r) implements Expr` → `interface ExprVisitor<T> { T visit(Num n); T visit(Add a); T visit(Mul m); }` → `EvalVisitor implements ExprVisitor<Double>` → в `Expr` метод `default <T> T accept(ExprVisitor<T> v) { return switch(this) { case Num n -> v.visit(n); case Add a -> v.visit(a); case Mul m -> v.visit(m); }; }` → добавить `PrettyPrintVisitor` и `DerivativeVisitor` без изменения `Expr` иерархии. Урок: sealed + pattern matching = visitor без boilerplate.

3. **Применить Decorator для HTTP-клиента с retry/circuit-breaker/logging**. Шаги: `interface HttpClient { HttpResponse send(HttpRequest req) throws IOException; }` → `JavaHttpClient implements HttpClient` (basic) → `LoggingClient implements HttpClient { HttpClient delegate; ... }` → `RetryClient implements HttpClient { ... }` → `CircuitBreakerClient implements HttpClient { ... }` → собирать через builder: `client = new LoggingClient(new RetryClient(new CircuitBreakerClient(new JavaHttpClient(), cb), retry), log);` → сравнить с Resilience4j functional decorators: `Function<HttpRequest, HttpResponse> pipeline = Decorators.ofSupplier(() -> client.send(req)).withRetry(retry).withCircuitBreaker(cb).withFallback(...).decorate();`. Trade-off: классический Decorator — лучше типизация; functional — лучше composability.

4. **Adapter + Facade для интеграции легаси SOAP-сервиса в современный REST API**. Шаги: сгенерить JAXB/JAX-WS stubs из WSDL → написать `LegacyCustomerSoapClient` (low-level) → ввести доменный интерфейс `CustomerGateway { Customer findById(String id); }` → `LegacyCustomerSoapAdapter implements CustomerGateway` (маппинг SOAP `CustomerDto` → domain `Customer`) → `CustomerFacade` (spring `@Service`) агрегирует `CustomerGateway` + `OrderGateway` в единый use case `getCustomerProfile` → покрыть адаптер contract тестами (WireMock + реальный WSDL response) → мониторить: падение SOAP → circuit breaker в facade → fallback к cache.

5. **Strategy + Factory для tax calculation в multi-jurisdiction (US/DE/UK/JP)**. Шаги: `interface TaxCalculator { Money calculate(Order o); }` → `USTaxCalculator`, `DETaxCalculator` (MwSt 19%), `UKTaxCalculator` (VAT 20%), `JPTaxCalculator` (consumption tax 10%) → registry: `Map<Country, TaxCalculator>` (через Spring `@Autowired Map<String, TaxCalculator>` с `@Qualifier` = country code) → добавить новый country = новая реализация `@Component("CA")` без изменения существующих → тесты: для каждого calculator — table-driven test с boundary cases (нулевой tax, max rate, exempt categories) → property-based тест: для случайного order сумма tax = base × rate. Урок: OCP через Strategy + DI-registry.

6. **Flyweight для glyph rendering в текстовом редакторе (100k символов)**. Шаги: naive: `class Glyph { char c; Font font; int x, int y; }` — 100k × 32 bytes = 3.2 MB → Flyweight: `class Glyph { char c; Font font; }` (intrinsic, shared, 256 + 4 fonts = 1k объектов) + `class GlyphContext { int x, int y; }` (extrinsic, per-position) → `GlyphFactory.get('a', Font.ARIAL)` возвращает cached instance → memory 100k × 8 bytes (context) + 1k × 24 bytes (flyweights) = ~820 KB (4× reduction) → thread-safety: Glyph immutable, context per-thread. Урок: Flyweight оправдан только при измеренном memory pressure.

7. **Template Method vs Strategy для Order processing pipeline**. Шаги: Template: `abstract class OrderProcessor { final void process(Order o) { validate(o); applyDiscount(o); charge(o); notify(o); } protected abstract void applyDiscount(Order o); }` — `B2COrderProcessor`, `B2BOrderProcessor` (override applyDiscount). vs Strategy: `class OrderProcessor { DiscountStrategy discount; ... }` inject strategy. Выбор: Template Method — если шаги фиксированы (контракт с клиентами), strategy — если шаги конфигурируются runtime. Spring `JdbcTemplate` — Template Method, `Comparator` — Strategy.

---

## 3. Dependency Injection паттерны (constructor/setter/field, IoC containers, Service Locator)

### Теория

- **Inversion of Control (IoC)** — инверсия потока управления: фреймворк вызывает твой код (Hollywood Principle). DI — частный случай IoC для управления зависимостями. Container: создаёт объекты, связывает, управляет lifecycle.
- **DI vs Service Locator** — DI: объект получает зависимости извне (constructor/setter), не знает о контейнере. Service Locator: объект сам запрашивает зависимости у глобального реестра (`Services.get(Foo.class)`). DI = декларативность + testability, SL = антипаттерн (Fowler, 2004) — скрывает зависимости, нарушает SRP, усложняет testing.
- **Constructor Injection** — recommended (Fowler, Spring docs). Преимущества: immutability (`final` fields), fail-fast (нельзя создать объект без зависимостей), visible in signature (документация), testability (легко `new`). Недостаток: boilarplate для optional deps. В Spring 4.3+ — единственный `@Autowired` constructor не требует аннотации.
- **Setter Injection** — для optional dependencies или circular references (антипаттерн сам по себе). Spring `@Autowired` на setter. Недостатки: mutability, partial state, NPE если не вызван setter.
- **Field Injection** — `@Autowired private Foo foo;`. Антипаттерн (Spring IDE warning): нельзя `final`, нельзя создать без reflection (untestable without Spring), скрывает dependencies. Допустим только в tests (`@SpringBootTest` + `@MockBean`).
- **DI Container** — BeanFactory (lazy) vs ApplicationContext (eager, +event publishing +i18n +AOP). Bean lifecycle: instantiation → populate properties → BeanNameAware/BeanFactoryAware → BeanPostProcessor.before → @PostConstruct/InitializingBean.afterPropertiesSet → BeanPostProcessor.after → destroy (@PreDestroy/DisposableBean). В Spring Boot — AnnotationConfigServletWebServerApplicationContext.
- **Scopes** — singleton (default, one per container), prototype (new instance per injection), request (HTTP request), session (HTTP session), application (ServletContext). В Spring WebFlux — нет request scope, есть custom через `Context`.
- **Qualifiers** — `@Qualifier("primaryFoo")` для disambiguation нескольких bean-ов одного типа. `@Primary` помечает дефолтный. `@Named` (JSR-330). Custom qualifier annotations (`@CustomerDb`, `@OrderDb`) — clearer than string-based.
- **Profiles** — `@Profile("prod")`/`@Profile("dev")` для environment-specific beans. В Spring Boot — `spring.profiles.active`. В тестах — `@ActiveProfiles("test")`.
- **Conditional beans** — `@Conditional`, `@ConditionalOnClass`, `@ConditionalOnMissingBean`, `@ConditionalOnProperty` (Spring Boot autoconfig). Основа Spring Boot starter mechanism.
- **Component scan** — `@ComponentScan(basePackages=...)` рекурсивно находит `@Component`/`@Service`/`@Repository`/`@Controller`. В Spring Boot — `@SpringBootApplication` сканирует package main class. Best practice: main class в корневом package.
- **Lifecycle hooks** — `@PostConstruct` (JSR-250), `@PreDestroy`, `InitializingBean`, `DisposableBean`, `@Bean(initMethod=, destroyMethod=)`. Для programmatic — `@EventListener(ApplicationReadyEvent.class)`. В Spring Boot 3+ — `@PostConstruct` в `jakarta.*` (vs `javax.*`).
- **Factory bean** — `FactoryBean<T>` для сложной логики создания (например, `ProxyFactoryBean` для AOP, `LocalContainerEntityManagerFactoryBean` для JPA). `@Bean` methods — simpler alternative в `@Configuration`.
- **Circular dependencies** — `@Autowired` field injection их прощает (через 3-phase init), constructor injection — нет (Spring throws `BeanCurrentlyInCreationException` в 6.0+, в 5.3 deprecated spring.main.allow-circular-references). Решение: redesign (extract third class), `@Lazy`, или events.
- **JSR-330** — `@Inject` (constructor/setter/field), `@Named`, `@Qualifier`, `@Singleton`, `Provider<T>` (lazy lookup). Spring, Guice, HK2 (Jersey) поддерживают. Portable, но Spring annotation extensions богаче.
- **DI в других языках** — Rust (no DI container, traits + generics), Go (wire/dig code-gen), Scala (Macwire / ZIO env). Java-ecosystem уникален зрелостью Spring/Guice.
- **Micronaut/Quarkus DI** — compile-time DI через annotation processors (без reflection) → startup 10–100× быстрее Spring, меньше memory. `@Inject`, `@Singleton`, `@Context` (application scope). Критично для GraalVM native image.
- **Multi-module DI** — Spring `@Import(Config.class)`, Spring Boot `@EnableAutoConfiguration` + spring.factories / `AutoConfiguration.imports` (Spring Boot 3). Большие проекты: один root context + child contexts (Spring Boot multi-context via `SpringApplicationBuilder.parent().child()`).

### Материалы

- **Статья**: Martin Fowler — "Inversion of Control Containers and the Dependency Injection pattern" (2004) — https://martinfowler.com/articles/injection.html
- **Книга**: "Spring in Action" 6th ed., Craig Walls (Manning, 2022) — Chapter 2 (wiring beans).
- **Дока**: Spring Framework Reference — IoC Container — https://docs.spring.io/spring-framework/reference/core/beans.html
- **Дока**: Micronaut DI — https://docs.micronaut.io/latest/guide/#ioc
- **Статья**: "Why Field Injection Is Evil" — https://www.vojtechruzicka.com/field-dependency-injection-considered-harmful/
- **Книга**: "Dependency Injection Principles, Practices, and Patterns", Seemann & van Deursen (Manning, 2nd ed. 2019) — DI в .NET, но концепции переносимы.

### Практика / Кейсы

1. **Миграция legacy-кода с Service Locator на Constructor Injection**. Симптом: `Services.getService(CustomerRepo.class)` в 200+ местах. Шаги: ввести Spring `@SpringBootApplication` → для каждого класса — добавить `private final Foo foo;` + constructor → IDE рефакторинг "Add constructor parameter" → удалить вызовы `Services.get` → запустить Spring context test → постепенно удалить `Services` registry →ArchUnit правило: `classes().should().notDependOnClassesThat().resideInAPackage("..legacy.services..")`. Метрика: coverage перехода = 80% классов за 2 спринта.

2. **Разрешение circular dependency в `OrderService` ↔ `CustomerService`**. Шаги: диагност — `BeanCurrentlyInCreationException` → root cause: `OrderService.getCustomer()` и `CustomerService.getOrders()` ссылаются друг на друга → redesign: выделить `CustomerOrderQueryService` (читает оба, не циклит) → или `@Lazy` на одной стороне (workaround, не solution) → или event-driven: `OrderService` публикует `OrderCreatedEvent`, `CustomerService` слушает → долгосрочно — bounded context (DDD): `Order` и `Customer` в разных BC, общаются через API/Anticorruption Layer.

3. **Multi-tenant DI: разные реализации `TaxCalculator` per tenant**. Шаги: `@Component` с `@Scope(value="tenant", proxyMode=ScopedProxyMode.INTERFACES)` → custom `TenantScope` implements `Scope` (registry `ThreadLocal<String> tenantId` + `Map<String, Map<String, Object>>`) → register scope в `PostProcessor` → `TaxCalculator` switch по tenant в `@Configuration` `@Bean` с `@Profile`-like conditional → тесты: `TenantContext.set("ACME"); taxService.calculate();` → asserts ACME-specific implementation. Урок: custom scope мощный, но дорогой в поддержке — лучше strategy + tenant-aware factory.

4. **Конфигурация через `@ConfigurationProperties` + `@Bean`**. Шаги: `@ConfigurationProperties(prefix="app.cache") record CacheProps(Duration ttl, long maxSize, boolean enabled) {}` → `@EnableConfigurationProperties(CacheProps.class)` → в `@Configuration` классе:
   ```java
   @Bean
   @ConditionalOnProperty(prefix="app.cache", name="enabled", havingValue="true")
   public CacheManager cacheManager(CacheProps p) {
     return Caffeine.newBuilder().expireAfterWrite(p.ttl()).maximumSize(p.maxSize()).build();
   }
   ```
   → валидация `@Validated` + JSR-303 (`@Min`, `@DurationMin`) → тесты `@ConfigurationPropertiesTest` с `@SpringBootTest` + проверка биндинга → ADR: `app.cache.enabled=false` = no-op `NoOpCacheManager`. Урок: явная оп-out важнее неявной оп-in.

5. **DI-тестирование с `@MockBean` vs constructor + plain JUnit**. Шаги: вариант 1 (Spring): `@SpringBootTest class FooTest { @MockBean Bar bar; @Autowired Foo foo; }` — поднимает контекст, медленно (500ms+ per test class). → вариант 2 (plain JUnit 5): `class FooTest { @Test void test() { Bar bar = mock(Bar.class); Foo foo = new Foo(bar); } }` — мгновенно, но без Spring features. → правило: 80% unit-тестов без Spring, 20% integration — с `@SpringBootTest`. ArchUnit: `classes().that().areAnnotatedWith("@Test").should().notBeAnnotatedWith("@SpringBootTest")` — нарушение → ревью. Урок: constructor injection делает тестирование лёгким, field injection — нет.

6. **Micronaut vs Spring для serverless AWS Lambda**. Шаги: спайк — Spring Lambda cold start 4s (загрузка контекста), Micronaut 200ms (compile-time DI, AOT graalvm) → миграция: `@Autowired` → `@Inject`, `@Component` → `@Singleton`, `@Configuration` → `@Factory` + `@Bean` → `@Value` → `@Property(name=...)` → Spring `@EventListener` → Micronaut `@EventListener` (event bus) → native image: `native-image` + `reflect-config.json` → cold start 50ms. ADR: для Lambda/Lambda@Edge/Containers с auto-scaling = Micronaut; для heavy monolith с зрелым Spring-ecosystem = Spring Boot.

7. **DI в тестах: `@TestConfiguration`, `@Import`, profiles**. Шаги: `@TestConfiguration` с `@Bean TestClock fixedClock() { return Clock.fixed(Instant.parse("2024-01-01T00:00:00Z"), ZoneOffset.UTC); }` → в тесте `@SpringBootTest @Import(TestConfig.class) @ActiveProfiles("test")` → в `application-test.yml` — H2 + wiremock URLs → `@MockBean` для медленных интеграций (Kafka) → `@DynamicPropertySource` для Testcontainers: `static PostgreSQLContainer<?> pg = new PostgreSQLContainer<>("postgres:16"); @DynamicPropertySource static void props(DynamicPropertyRegistry r) { r.add("spring.datasource.url", pg::getJdbcUrl); }`. Урок: тестовый DI не должен дублировать production-config — только overrides.

---

## 4. Архитектурные паттерны (Layered, Hexagonal, Onion, Clean, CQRS, Event-Driven, Microservices, Monolith, Serverless)

### Теория

- **Layered (n-tier) Architecture** — Presentation → Business → Data → Database. Правило: зависимость только сверху вниз. Проблема: «smart UI, dumb database», домен размазан по слоям, schema leaks через layers. Дисциплина: DTO на каждом слое (mapping overhead). Хорош для CRUD, плох для complex domain.
- **Hexagonal Architecture (Ports and Adapters)** — Alistair Cockburn 2005. Ядро (domain + application) изолировано, общается с внешним миром через ports (interfaces). Driving adapters (REST controller, CLI) вызывают port; Driven adapters (DB, external API) реализуют port. Преимущество: domain free от frameworks; можно заменить REST на gRPC без правок domain. Связан с DDD.
- **Onion Architecture** — Jeffrey Palermo 2008. Концентрические слои: Domain Model → Domain Services → Application Services → Infrastructure. Зависимости направлены внутрь. Похоже на Hexagonal, но более явно про слои внутри.
- **Clean Architecture** — Robert C. Martin 2017. Концентрические круги: Entities → Use Cases → Interface Adapters → Frameworks & Drivers. The Dependency Rule: исходящий код зависит только от внутреннего круга. Круги = уровни абстракции. Сходство с Onion/Hexagonal — все три — это ports&adapters под разными углами.
- **CQRS (Command Query Responsibility Segregation)** — Greg Young. Разделение моделей на write (commands) и read (queries). Команды меняют state, queries возвращают данные. В простом виде — разные services. В сложном — разные data stores (write — normalized RDBMS, read — denormalized Elasticsearch/Cassandra). + Event Sourcing → store events as source of truth, projections — read models. Опасность: eventual consistency, complexity.
- **Event-Driven Architecture** — компоненты общаются через events (часто через broker — Kafka/RabbitMQ). Топологии: broker (pub-sub) vs bus (mediator). Преимущества: loose coupling, scalability, resilience. Недостатки: harder debugging, eventual consistency, event schema evolution, no transactional boundary (saga).
- **Microservices** — Sam Newman. Маленькие автономные сервисы, каждый со своей БД, общение по network (REST/gRPC/async). Преимущества: independent deployment, scaling per service, technology diversity. Недостатки: distributed system complexity, network failures, data consistency (saga), observability challenge, Conway's Law (org structure drives architecture).
- **Modular Monolith** — Simon Newman/recent revival. Один process/deployment, но строгие module boundaries (Java modules / Maven modules). Лучшее из двух миров: simplicity monolith + isolation microservices. Базовые принципы: shared-nothing modules, явно экспортируемый API, private schema, без shared DB tables.
- **Serverless / FaaS** — AWS Lambda, Azure Functions, Google Cloud Functions. Cold start проблема (JVM historically bad — 3-5s; GraalVM native image — 50ms; Spring Boot 3 AOT — 1s). SnapStart (AWS, Java 21+) — 200ms через restore из snapshot. Trade-offs: pay-per-use, no scaling management, vendor lock-in, hard local testing, 15min timeout, statelessness.
- **Service-Oriented Architecture (SOA)** — предшественник microservices, с enterprise service bus (ESB). SOAP/WSDL-centric. Отличие от microservices: ESB = smart middleware, dumb endpoints; microservices = dumb middleware (brokers), smart endpoints.
- **Space-Based Architecture** — для extreme scalability: in-memory data grid (Hazelcast, Coherence) + processing units без БД. Все данные в памяти, реплицированы. Подходит для trading, gaming.
- **Event Sourcing** — store events as source of truth, state derived by replay. Преимущества: audit log, time travel, undo. Недостатки: schema evolution (event upcasting), snapshotting, eventual consistency, complexity. Подходит для financial, inventory.
- **Sidecar pattern** — auxiliary process рядом с основным (Envoy proxy, istio sidecar). В K8s — sidecar container в same Pod. Spring Cloud Gateway/Service Mesh.
- **Backends for Frontends (BFF)** — Phil Calçado, SoundCloud. Один backend per frontend (mobile, web, partner API). Убирает over-fetching/under-fetching. Часто реализуется GraphQL или thin REST aggregator.
- **Strangler Fig** — Martin Fowler. Постепенная миграция legacy: новый функционал в новом сервисе, routing (facade) постепенно переключает трафик. Когда legacy становится пустым — удалить. Альтернатива big-bang rewrite (risky).
- **Saga pattern** — распределённая транзакция как последовательность локальных транзакций с compensating actions. Choreography (events) vs Orchestration (central coordinator). Связан с Event-Driven.
- **Outbox pattern** — атомарная запись в БД + event через transactional outbox table → separate publisher читает outbox и отправляет в Kafka. Решает dual-write problem (БД commit + Kafka send не atomic).
- **Database per service** — каждый микросервис владеет своей схемой/БД. Anti-pattern: shared database (привязывает сервисы, нарушает autonomy). Communication — только через service API.

### Материалы

- **Книга**: "Clean Architecture", Robert C. Martin (Pearson, 2017) — Chapter 22-34: архитектурные главы.
- **Статья**: Herberto Graça — "DDD, Hexagonal, Onion, Clean, CQRS... How I put it all together" — https://herbertograca.com/2017/11/16/explicit-architecture-01-ddd-hexagonal-onion-clean-cqrs-how-i-put-it-all-together/
- **Статья**: Martin Fowler — "StranglerFigApplication" — https://martinfowler.com/bliki/StranglerFigApplication.html
- **Статья**: "Hexagonal vs Clean vs Onion" — https://dev.to/dev_tips/hexagonal-vs-clean-vs-onion-which-one-actually-survives-your-app-in-2026-273f
- **Книга**: "Building Microservices" 2nd ed., Sam Newman (O'Reilly, 2021).
- **Книга**: "Software Architecture: The Hard Parts", Ford/Richards/Sadalage/Dehghani (O'Reilly, 2021) — distributed decision-making.
- **Видео**: Simon Brown — "The C4 model for visualising software architecture" — https://www.youtube.com/watch?v=KvoBrUd1-5E

### Практика / Кейсы

1. **Миграция Layered Monolith → Hexagonal + Modular Monolith**. Шаги: текущее состояние — `controller → service → repository` с schema leak (`OrderEntity` возвращается из REST) → ввести модули: `order`, `customer`, `payment`, `inventory` (Maven modules) → в каждом: `domain` (pure Java), `application` (use cases), `infrastructure` (JPA adapters), `web` (REST) → выделить ports: `OrderRepository` (in domain), `JpaOrderRepository implements OrderRepository` (in infrastructure) → ArchUnit: `no classes in domain should depend on javax.persistence` → удалить `OrderEntity` из API — DTO `OrderResponse` в web → миграция по модулю за раз (strangler). Сложность: 6-12 месяцев, senior+ участие.

2. **Внедрить CQRS для high-read inventory service (10k RPS read, 50 RPS write)**. Шаги: измерить bottleneck — SELECT на inventory_items с JOIN (200ms avg) → разделить: write side — `InventoryCommandHandler` (+Event Sourcing через outbox), read side — `InventoryQueryService` из denormalized проекции (Materialized View в PostgreSQL или Redis) → sync через Debezium (CDC) из PostgreSQL → Elasticsearch для поиска → eventual consistency ~100ms → API: `GET /inventory/{sku}` (read), `POST /inventory/{sku}/reserve` (command) → мониторить lag (Debezium metric) → SLO: 99% reads < 50ms, 99% writes < 200ms. Урок: CQRS не для всего — только когда read/write ratio > 10:1 и read сложный.

3. **Event-Driven миграция: синхронный REST → асинхронные события через Kafka**. Шаги: текущее: `OrderService.placeOrder()` → REST call `PaymentService.charge()` → `InventoryService.reserve()` (timeout cascades) → ввести outbox pattern: OrderService пишет `OrderPlacedEvent` в outbox table → Debezium публикует в Kafka → PaymentService подписан, обрабатывает, публикует `PaymentSucceeded`/`PaymentFailed` → OrderService listens, обновляет status → для синхронных UI: WebSocket/SSE уведомление клиенту, или front-end poll status API. Сложность: idempotency (payment double-charge), out-of-order events, retries. Урок: async сложнее, оправдан только когда sync блокирует.

4. **Serverless migration: Spring Boot REST API → AWS Lambda**. Шаги: спайк — cold start 5s (Spring Boot 2.7 на Java 11) → варианты: (a) SnapStart (Java 21) — 1s; (b) Spring Boot 3 AOT + GraalVM native image — 200ms; (c) Micronaut — 100ms native → выбираем (b) для ecosystem-compatibility → build: `native-image` через buildpacks или `native-maven-plugin` → deploy: Lambda container image (Docker), 10GB memory (native image compute-optimised) → API Gateway → Lambda → DynamoDB → cost: $0.0000002 per request vs EC2 always-on $50/mo, break-even at ~250M requests → ограничение: 15min timeout, 10GB memory, no background threads after response. ADR: для burst traffic и idle periods — Lambda; для steady load — EC2/ECS.

5. **Modular Monolith на Java Platform Module System (JPMS)**. Шаги: `module-info.java` в каждом модуле: `module com.shop.orders { exports com.shop.orders.api; requires com.shop.shared; }` → private packages `com.shop.orders.internal` не экспортируются → boundary enforcement на compile-time (vs ArchUnit runtime) → `OrderService` (api) — public, `OrderRepositoryImpl` (internal) — package-private → каждый модуль Maven multi-module с `module-info` → shared kernel `com.shop.shared` — value objects (Money, CustomerId) → migration: gradual per-module, начиная с нового функционала → тесты: `module` integration test через `ModuleLayer` API. Урок: JPMS жёстче Spring component-scan, но learning curve высокий.

6. **Выбор между microservices и modular monolith для стартапа (10 инженеров, MVP 3 месяца)**. Шаги: анализ — business domain unclear (нужен fast feedback), team small (microservices = ops overhead), budget constrained → решение: Modular Monolith (3-4 модуля: catalog, orders, billing, identity) с чёткими API boundaries (Spring `@ComponentScan` per-module + ArchUnit) → когда команда вырастет > 20 человек и модуль станет bottleneck → вынести в microservice (strangler pattern) → критерий: «выносим когда команда > 5 человек на модуль И deploys конфликтуют». ADR фиксирует + переоценка quarterly. Урок: «start monolith, extract microservices» — стандартный совет Newman/Fowler.

7. **Saga orchestration для order processing (OrderService → PaymentService → InventoryService → ShippingService)**. Шаги: выбрать orchestration (central `OrderSagaOrchestrator`) vs choreography (events only) → orchestration лучше для видимости и complex flows → реализация: Spring Statemachine или temporal.io (workflow engine) → steps: `CreateOrder` → `ChargePayment` (compensate: `RefundPayment`) → `ReserveInventory` (compensate: `ReleaseInventory`) → `ScheduleShipping` (compensate: `CancelShipping`) → state persisted в DB (saga state table) → idempotency через `saga_id` + `step_id` → failure: orchestrator выполняет compensations в reverse order → timeout: idempotent retry + poison queue. Урок: orchestration проще отлаживать, choreography — слабее coupled.

8. **Decompose monolith по данным (Strangler + Database per service)**. Шаги: (1) карта — диаграмма текущей схемы, найти bounded contexts (DDD event storming) → изобразить целевую архитектуру: 4 сервиса (orders, customers, payments, inventory) каждый со своей схемой → (2) первый кандидат: `inventory` — читается чаще пишется, слабо coupled с orders → (3) extract API: Spring `@RestController` в монолите, делегирует в будущий сервис → клиенты переходят на новый API (strangler) → (4) data extraction: создать schema `inventory` в той же Postgres (пока shared DB), перенести таблицы → (5) вынести в отдельный сервис: новый microservice `inventory-svc` подключается к новой БД, sync через Debezium CDC из старой → (6) dual-write через outbox: пишем в старую + новую одновременно → (7) cut-over: переводим read traffic на новый сервис → (8) удалить old schema. Таймлайн: 3-6 месяцев на сервис. Урок: extract data сначала, сервисы после.

---

## 5. Domain-Driven Design (Ubiquitous Language, Bounded Context, Context Map, Entity/Value Object/Aggregate/Aggregate Root, Repository, Domain Service, Application Service, Domain Events, Anti-Corruption Layer, Shared Kernel, Conformist)

### Теория

- **DDD Strategic vs Tactical** — Strategic (бизнес-моделирование): Ubiquitous Language, Bounded Context, Context Map. Tactical (реализация): Entity, Value Object, Aggregate, Repository, Domain Service, Domain Event. Опасность: команды используют tactical без strategic — получают «DDD-lite» без выгод.
- **Ubiquitous Language** — общий язык между разработчиками и domain experts. Один термин = одно значение во всех артефактах: речь, документация, код, тесты. Если в коде `Order` а в бизнесе `Trade request` — нарушение. Связан с Bounded Context: один термин может иметь разные значения в разных контекстах (`Product` в sales vs shipping).
- **Bounded Context** — граница, в которой Ubiquitous Language единообразен. Один BC = один subdomain (часто = один микросервис, но не всегда). Границы определяются linguistic boundaries: «Product» в Catalog BC vs Pricing BC vs Shipping BC — разные модели.
- **Context Map** — карта связей между Bounded Contexts. Patterns (Vaughn Vernon): Partnership, Shared Kernel, Customer-Supplier, Conformist, Anticorruption Layer (ACL), Open Host Service (OHS), Published Language, Separate Ways, Big Ball of Mud.
- **Subdomain types** — Core domain (heart of business, competitive advantage — invest), Supporting (necessary but not differentiator — buy/outsource), Generic (commodity — auth, billing — buy SaaS).
- **Entity** — объект с identity (`equals/hashCode` по ID), mutable, lifecycle. Пример: `Order` с `OrderId`. Идентичность = survives state changes and serialization.
- **Value Object** — immutable, идентичность по значению (`equals/hashCode` по всем полям), без lifecycle. Пример: `Money(amount, currency)`, `Address`. В Java — `record`. Должны быть self-validating (конструктор проверяет инварианты).
- **Aggregate** — консистентная группа Entities + Value Objects, рассматриваемая как единое целое. Имеет invariant (бизнес-правило), которое должно выполняться всегда. Граница транзакции = один Aggregate. Пример: `Order` (root) + `OrderLine[]` + `ShippingAddress`.
- **Aggregate Root** — единственная точка входа в Aggregate. Внешний код может держать ссылку только на root, не на внутренние entities. Root отвечает за enforcement invariants. Reference между Aggregates — по ID (не прямой ссылкой).
- **Aggregate design principles** (Vaughn Vernon) — design small aggregates; reference others by identity; use eventual consistency outside boundary; one transaction = one aggregate.
- **Repository** — абстракция над storage для Aggregate Root. `interface OrderRepository { OrderId nextId(); Optional<Order> findById(OrderId); void save(Order); }`. Реализация: JPA, MongoDB. Не DAO (DAO = table, Repository = aggregate). Не должен содержать business logic.
- **Domain Service** — stateless операция, не принадлежит одному Entity. Пример: `TransferService.transfer(Account from, Account to, Money amount)` — затрагивает 2 aggregates. Когда операция неестественна для Entity — Domain Service.
- **Application Service** — orchestrates use case: транзакция, security, mapping DTO↔domain, вызов Domain Services. Не содержит business logic. Spring `@Service` + `@Transactional`. Пример: `PlaceOrderUseCase`.
- **Domain Event** — факт, что что-то произошло в domain (`OrderPlaced`, `PaymentConfirmed`). Past tense. Immutable. Поддерживает eventual consistency между aggregates/BCs. В Spring — `ApplicationEventPublisher` + `@EventListener`/`@TransactionalEventListener`.
- **Factory** — инкапсуляция сложного создания Aggregate. Может быть: static method на root (`Order.create(...)`), separate Factory class, или builder. Восстанавливает invariant.
- **Anti-Corruption Layer (ACL)** — adapter между BC, переводит язык upstream в локальный. Защищает core от «коррупции» legacy/integration model. Реализуется как Adapter + Translator + Facade.
- **Shared Kernel** — часть модели, явно shared между BC. Редкий, дорогой (требует coordination). Пример: `Money` value object.
- **Conformist** — downstream BC принимает upstream model как есть (без ACL). Удобно для generic upstream (например,_country codes ISO).
- **Open Host Service + Published Language** — upstream предоставляет публичный API + schema (например, REST + OpenAPI). Альтернатива интеграции per-consumer.
- **Event Storming** — workshop для discovery domain events, команд, aggregates. Big picture → design level → walkthrough. Sticky notes на бесконечной стене. Originator — Alberto Brandolini.
- **CQRS в DDD** — read models (projections) как separate concern, часто в другом BC. Domain events — источник для projections.
- **Связь с Hexagonal** — domain (entities, value objects, domain services) — в ядре; repositories, events publishers — ports; JPA implementations, REST controllers — adapters.

### Материалы

- **Книга**: "Domain-Driven Design: Tackling Complexity in the Heart of Software", Eric Evans (Addison-Wesley, 2003) — канон (синяя книга).
- **Книга**: "Implementing Domain-Driven Design", Vaughn Vernon (Addison-Wesley, 2013) — практическое (красная книга).
- **Книга**: "Domain-Driven Design Distilled", Vaughn Vernon (Addison-Wesley, 2016) — короткий обзор.
- **Статья**: Martin Fowler — "Bliki: DomainDrivenDesign" — https://martinfowler.com/bliki/DomainDrivenDesign.html
- **Дока**: DDD Reference — https://www.domainlanguage.com/wp-content/uploads/2016/05/DDD_Reference_2015-03.pdf
- **Статья**: "A gentle introduction to Domain Driven Design" — https://medium.com/@thelonearchitect/a-gentle-introduction-to-domain-driven-design-dc7cc169b1d
- **Статья**: "Implementing Domain-Driven Design [Book]" — https://www.oreilly.com/library/view/implementing-domain-driven-design/9780133039900

### Практика / Кейсы

1. **Event Storming для e-commerce домена (sales, shipping, billing, inventory)**. Шаги: собрать 8 человек (devs + business + QA) на 2 дня → sticky notes: domain events (orange) — `OrderPlaced`, `PaymentCaptured`, `InventoryReserved`, `OrderShipped` → commands (blue) — `PlaceOrder`, `CapturePayment`, `ReserveInventory` → actors (yellow) → external systems (pink) — Stripe, FedEx → aggregates (large yellow) — `Order`, `Payment`, `InventoryItem`, `Shipment` → boundaries (dashed lines) — Sales BC, Inventory BC, Shipping BC, Billing BC → hotspots (red, с вопросом) — `OrderCancelled` event → compensation flow. Output: context map, list of aggregates per BC, ubiquitous language glossary.

2. **Aggregate design для Order (1 root + OrderLines + shipping)**. Шаги: рассмотреть варианты: (a) один большой Aggregate `Order` со всеми `OrderLine` (транзакция охватывает всё, просто); (b) `Order` + `OrderLine` как отдельные aggregates (id reference). Правило (Vernon): «design small aggregates» → выбираем (a) — OrderLine не имеет смысла без Order, invariant «total = sum(lines.price × qty)» принадлежит Order → reference на Customer by `CustomerId` (не прямой Customer) — другой aggregate → reference на Product by `ProductId` + snapshot price (чтобы изменение цены продукта не меняло исторический order). В одной транзакции — только `order.save()`, не `customer.save()`. Cross-aggregate consistency — через events.

3. **Anti-Corruption Layer для интеграции с legacy CRM (SOAP + weird schema)**. Шаги: legacy возвращает `CustomerDTO` с 50 полями (бизнес говорит на разных терминах) → ввести ACL: `LegacyCrmClient` (SOAP) → `CrmTranslator` (mapping `CustomerDTO` → `Customer` domain) → `CustomerGateway` interface (port) в domain → `LegacyCrmCustomerGateway implements CustomerGateway` (adapter) в infrastructure → domain код видит только `Customer`, не `CustomerDTO` → тесты: для каждого weird field scenario (null, unknown enum, malformed date) — translation rule documented → со временем migrate на REST API upstream, ACL остаётся — меняется только adapter impl. Урок: ACL = insulation от upstream chaos.

4. **Domain Events + eventual consistency для рассинхрона Order и Inventory**. Шаги: `OrderService.placeOrder()` → save Order → publish `OrderPlacedEvent` (через outbox + transactional) → `InventoryService` listens → tries `reserve(OrderLine items)` → success: `InventoryReserved` event; failure: `InventoryReservationFailed` → `OrderService` listens → mark Order as `REJECTED` или `BACKORDERED` → compensation: если платёж уже списан — `RefundRequested` event → PaymentService listens. Сложности: idempotency (event processed twice), out-of-order (events пришли в разном порядке), retries with exponential backoff. Договорённость: каждый event handler — idempotent (через `processed_events` table).

5. **Repository pattern + Spring Data JPA**. Шаги: domain layer: `public interface OrderRepository { OrderId nextId(); Optional<Order> findById(OrderId id); void save(Order order); }` (no Spring annotations) → infrastructure layer: `@Repository class JpaOrderRepository implements OrderRepository { private final OrderJpaRepository jpa; ... }` где `OrderJpaRepository extends JpaRepository<OrderEntity, Long>` → mapping `Order` (domain) ↔ `OrderEntity` (JPA) через MapStruct → `OrderEntity` НЕ имеет business methods, только state for persistence → `nextId()` — через DB sequence или UUID. Урок: JPA-сущность и domain Aggregate — часто разные классы (чтобы не смешивать persistence и domain concerns).

6. **Value Object `Money` с multi-currency support**. Шаги: `record Money(BigDecimal amount, Currency currency) { public Money { Objects.requireNonNull(amount); Objects.requireNonNull(currency); if (amount.scale() > currency.getDefaultFractionDigits()) throw new IllegalArgumentException(); } public Money plus(Money other) { requireSameCurrency(other); return new Money(amount.add(other.amount), currency); } public Money multiply(DecimalFactor f) { ... } }` → никогда не использовать `double` для денег → BigDecimal + roundingMode (`HALF_EVEN` банковский) → для high-perf — `long` cents + currency code → сериализация: JSON `{"amount":"10.50","currency":"USD"}` (string для amount — avoids float precision loss). Урок: VO = self-validating immutable — фундамент domain.

7. **Bounded Context mapping для retail platform (Sales, Inventory, Shipping, Billing)**. Шаги: Sales BC (core) — owns `Order`, `Customer`, `Product` (as in catalog reference) → Inventory BC (core) — owns `StockItem`, `Warehouse` → Shipping BC (supporting) — owns `Shipment`, `Carrier` → Billing BC (generic,可以考虑 Stripe) — owns `Invoice`, `Payment` → context map: Sales → Billing (Customer-Supplier, sales is upstream, billing conforms partially); Sales → Inventory (Partnership, both modify state); Sales → Shipping (Customer-Supplier); Billing ↔ Sales events (`OrderInvoiced`); ACL между Sales и legacy ERP. Каждый BC — отдельный микросервис или модуль. Trade-off: полный DDD дорогой — для CRUD-like subdomains можно simpler models.

8. **Domain event design — schema, versioning, ordering**. Шаги: event `OrderPlaced` (Avro): `{order_id: UUID, customer_id: UUID, items: [{sku, qty, price}], total: Money, placed_at: Timestamp, schema_version: 1}`. Schema evolution: adding optional field = backward compatible; removing field = forward compatible with default; renaming = breaking → publish new event type `OrderPlacedV2`, deprecate old. Versioning via Schema Registry — compatibility enforced on publish. Ordering: Kafka guarantees order per partition; partition by `order_id` for per-order ordering. Idempotency: each event has unique `event_id` (UUID); consumers track `processed_events` table; dedup on `event_id`. Outbox pattern: write event to `events_outbox` table in same DB transaction as state change; separate publisher reads outbox → publishes to Kafka → marks as published. Урок: events = contract, evolution + idempotency — first-class concerns.

---

## 6. System Design (capacity estimation, load balancing, caching strategies, CDN, database sharding, replication, queueing, back-of-envelope, latency numbers, read/write ratio, hot spots)

### Теория

- **Back-of-envelope estimation** — приблизительные расчёты для проверки feasibility. Правила: round numbers, powers of 2/10, консервативные предположения. Пример: 100M DAU × 10 actions/day × 100 bytes = 100 GB/day; × 365 = 36 TB/year. Цель — понять порядок величины, не точность.
- **Latency numbers every programmer should know** (Jeff Dean): L1 cache 0.5 ns; L2 7 ns; RAM 100 ns; SSD 100 µs; HDD 10 ms; LAN 0.5 ms; WAN 10 ms; internet 50-150 ms. Update 2020+: NVMe SSD 10-30 µs; 100Gbps network 1 µs RTT (kernel-bypass). Memory: keep in mind 1 GB ≈ 1B bytes ≈ 250M integers.
- **Throughput numbers**: HDD sequential 100 MB/s; SSD 500 MB/s - 7 GB/s (NVMe); 1 Gbps network 125 MB/s; 10 Gbps 1.25 GB/s. PCIe 4.0 x16 32 GB/s.
- **Capacity estimation steps** — (1) estimate users: DAU, MAU, peak QPS (обычно 3-10× avg); (2) per-user data: storage, bandwidth; (3) read/write ratio; (4) total storage + growth; (5) compute: CPU, memory; (6) network: bandwidth, connections; (7) redundancy + overhead.
- **Load balancing algorithms** — Round Robin (stateless, simple); Weighted Round Robin (per server capacity); Least Connections (dynamic); IP Hash (sticky); Consistent Hashing (для cache — минимизирует redistribution); Random; P2C (power of two choices). L4 (TCP, NLB) vs L7 (HTTP, ALB). Java: Ribbon (deprecated), Spring Cloud LoadBalancer.
- **Load balancing layers** — DNS LB (geo); Anycast (BGP); CDN edge; L4 LB (HAProxy, NLB); L7 LB (NGINX, ALB); service mesh sidecar (Envoy); application-level (ribbon); database (PgBouncer, ProxySQL).
- **Caching strategies** — Cache-aside (lazy, application manages); Read-through (synchronous); Write-through (write cache + db synchronously); Write-behind/write-back (async write to db, fast but risky); Refresh-ahead (preemptive). Eviction: LRU, LFU, FIFO, ARC, TinyLFU (Caffeine). TTL. Cache stampede prevention: probabilistic early expiration, lock per key.
- **Cache-aside pattern** — read: check cache; if miss, read DB, write cache, return. Pros: simple, cache only for hot data. Cons: stale data, write race. Spring Cache (`@Cacheable`).
- **CDN** — edge nodes cache static + dynamic (CDN dynamic acceleration). Cloudflare, Akamai, Fastly, CloudFront. Origin shield. Cache-Control: `public/private/no-cache/no-store/max-age/s-maxage`. Invalidation: purge API, versioned URLs (`/v123/asset.js`). Edge compute (Cloudflare Workers, Lambda@Edge) — serverless at edge.
- **Database sharding** — horizontal partitioning. Strategies: range-based (1M users per shard), hash-based (consistent hashing), directory (lookup service). Shard key choice критичен — uneven distribution = hot shards. Resharding: double-down (move half), consistent hashing (Cassandra), Vitess for MySQL, Citus for PostgreSQL.
- **Database replication** — single-leader (read replicas, async/sync); multi-leader (CockroachDB, Spanner, Galera); leaderless (Dynamo, Cassandra). RPO/RTO. Replication lag — read-after-write problem (read-your-writes consistency via sticky session, read-from-leader for own writes).
- **Read/write ratio** —大多数 web apps 100:1 to 1000:1 (read-heavy). Social network feed: 1000:1. E-commerce checkout: 1:1. Decisions: cache layer, read replicas, denormalization, CQRS.
- **Hot spots** — hot keys (celebrity tweet), hot shards, hot rows (stock ticker). Mitigation: replication (read replicas), client-side caching, hot-key replication (each key stored on N nodes), rate limiting, queue + batch writes.
- **Queueing theory basics** — Little's Law: `L = λW` (avg number in system = arrival rate × avg wait). M/M/1 queue: utilization `ρ = λ/μ`; avg wait `W = 1/(μ-λ)`; as ρ → 1, W → ∞. Practical: target ρ < 0.7 for low latency.
- **Connection pooling** — DB: HikariCP (max pool size = ((core_count × 2) + effective_spindle_count), но для SSD — 2× cores). HTTP: Apache HttpClient, OkHttp, Reactor Netty. Too many connections — DB overloaded; too few — clients wait. TCP keepalive.
- **Database indexes** — B-tree (PostgreSQL default), LSM-tree (Cassandra, RocksDB), Hash index, GiST, BRIN (PostgreSQL, time-series). Covering index (INCLUDE) avoids heap fetch. Composite index — column order matters (leftmost prefix). Partial index (WHERE). Over-indexing — write slowdown.
- **Backpressure** — consumer сигнализирует producer-у что не справляется. Reactive: `Flux.onBackpressureBuffer/Drop/Latest`. JVM: `BlockingQueue.put()` blocks. HTTP: 429 Too Many Requests. gRPC: flow control (HTTP/2 window).
- **CAP theorem** — Consistency, Availability, Partition tolerance. При partitioning (network failure) — выбирать C или A. PACELC: else (no partition) — Latency vs Consistency. CP systems (HBase, MongoDB strict), AP systems (Cassandra, DynamoDB), linearizable (Spanner).
- **Idempotency** — повторный запрос даёт тот же результат. HTTP methods: GET/PUT/DELETE — idempotent, POST — нет. Implementation: idempotency key (Stripe), deduplication table. Критично для retries/payments.
- **Rate limiting** — token bucket (Guava `RateLimiter`), leaky bucket, fixed window, sliding window log/counter. Distributed: Redis + Lua script, Envoy, API gateway. Response: 429 + `Retry-After` header. Per-user, per-IP, per-API.

### Материалы

- **Книга**: "Designing Data-Intensive Applications", Martin Kleppmann (O'Reilly, 2017) — главы 5-9: replication, partitioning, transactions.
- **Книга**: "System Design Interview – An Insider's Guide" Vol. 1 & 2, Alex Xu (ByteByteGo, 2020/2022).
- **Сайт**: ByteByteGo — https://bytebytego.com (courses, newsletter).
- **Статья**: "Latency Numbers Every Programmer Should Know" (Jeff Dean, обновлено) — https://gist.github.com/jboner/2841832
- **Статья**: "Design A Rate Limiter" — https://bytebytego.com/courses/system-design-interview/design-a-rate-limiter
- **Статья**: "Design A URL Shortener" — https://bytebytego.com/courses/system-design-interview/design-a-url-shortener
- **Сайт**: High Scalability — https://highscalability.com (case studies).

### Практика / Кейсы

1. **Capacity estimation для Twitter-like сервиса (200M DAU, 500M tweets/day)**. Шаги: writes 500M/86400s ≈ 5800 tweets/s avg, peak 3× = 17k/s. Reads: avg user views 200 tweets/day → 200M × 200 = 40B reads/day ≈ 460k/s avg, peak 1.4M/s. Storage: tweet avg 300 bytes (text+meta) → 500M × 300 = 150 GB/day, × 5 years = 270 TB (compressed). Media: 30% tweets with image avg 500KB → 150M × 500KB = 75 TB/day → S3. Bandwidth: 460k reads/s × 300 bytes = 138 MB/s read = 1.1 Gbps. Caching: top 20% tweets generate 80% reads → 100 GB Redis for hot tweets. Compute: 1.4M reads/s ÷ 5k reads/server = 280 read servers; 17k writes/s ÷ 1k writes/server = 17 write servers. + 2× redundancy = ~600 servers. Conclusion: feasible.

2. **Шардинг PostgreSQL для 10 TB user data**. Шаги: choose shard key — `user_id` (most queries per-user) → hash-based sharding via Citus или自家 consistent hashing → N=32 shards (powers of 2 for easy split) → per-shard = 320 GB (fits on 1 TB NVMe server with replication) → routing: `shard_num = hash(user_id) % 32` → cross-shard queries (admin, analytics) — separate analytics replica (Citus MX or OLAP ClickHouse) → resharding plan: при росте до 20 TB — split each shard into 2 → 64 shards, online via logical replication (pglogical). Hot spot: a single user with 1M followers → read replicas + cache. Урок: не шардируй преждевременно — start с read replicas, вертикальный scaling, потом sharding.

3. **Cache-aside + cache stampede prevention для hot product page**. Шаги: Spring Cache `@Cacheable("product")` + Caffeine (in-process) + Redis (distributed) — two-tier cache → для hot key (Black Friday sale item) — 100k RPS → cache miss cascade → solution: probabilistic early expiration (XFetch algorithm) — refresh cache in background at 90% TTL with probability increasing exponentially → если miss всё же произошёл — `Cache.get(key, k -> loadFromDb(k))` с single-flight lock (Caffeine `AsyncCache`) → 1 поток грузит, остальные ждут. Метрики: hit ratio > 99.5%, p99 latency < 5ms (cache), 50ms (db fallback). Урок: cache stampede убивает DB — всегда single-flight + early refresh.

4. **Read-after-write consistency для user profile update**. Шаги: user updates profile → write to DB leader → user immediately views profile → read from replica (lag 200ms) → sees old data → bug. Решения: (a) sticky session — user's reads go to leader for 30s after write; (b) read-your-writes через version token — write returns `version=N`, read waits until replica has `version >= N` (PostgreSQL `pg_logical_slot`); (c) write-through cache — write updates cache synchronously → reads hit cache. Выбор: (c) для user-specific data, (a) для generic. В микросервисах — sticky не работает (no shared session), нужно (c) или accept eventual consistency + UI hints.

5. **Rate limiter 100 req/s per user (Redis + Lua)**. Шаги: token bucket — `key = ratelimit:user:{userId}`, value = `{tokens, last_refill}` → Lua script (atomic):
   ```lua
   local key = KEYS[1]
   local rate = tonumber(ARGV[1])
   local capacity = tonumber(ARGV[2])
   local now = tonumber(ARGV[3])
   local bucket = redis.call('HMGET', key, 'tokens', 'ts')
   local tokens = tonumber(bucket[1]) or capacity
   local ts = tonumber(bucket[2]) or now
   tokens = math.min(capacity, tokens + (now - ts) * rate)
   if tokens < 1 then return 0 end
   tokens = tokens - 1
   redis.call('HMSET', key, 'tokens', tokens, 'ts', now)
   redis.call('EXPIRE', key, 60)
   return 1
   ```
   → call через `redis.eval(script, keys, args)` → при 429 — response header `Retry-After: 1` → distributed: Redis Cluster, key sharded by user_id → SLO: 99% rate-check < 1ms. Урок: Lua script = atomic + single round-trip.

6. **Hot row mitigation: stock ticker `AAPL` с 100k writes/s**. Шаги: single row contention → DB locks → throughput 1k writes/s (bottleneck). Варианты: (a) batch writes — collect in memory, flush every 100ms; (b) partitioned counter — N sub-rows, sum on read; (c) write-behind cache — Redis INCR + periodic flush to DB; (d) CRDT (Redis) или specialized (HyperLogLog для cardinality). Для stock: in-memory order book (Disruptor), DB = periodic snapshot + event log. Урок: hot row = fundamental DB limit, нужно in-memory layer.

7. **Multi-region active-active для global app**. Шаги: regions: US-East, EU-West, AP-Southeast → каждый регион имеет full stack (app + DB) → DB: CockroachDB / Spanner / PostgreSQL with bidirectional logical replication (Bucardo) → conflict resolution: last-write-wins (timestamp), CRDT (eventually consistent fields), or per-record ownership → routing: GeoDNS → nearest region → writes: route to owner region (may add latency), reads: local → consistency: tunable per query (strong for billing, eventual for social). Trade-off: complexity vs latency. Урок: multi-region active-active оправдан только когда > 30% users outside primary region И latency SLA < 100ms global.

8. **Database connection pool sizing под high load**. Шаги: HikariCP defaults suboptimal. Formula (HikariCP wiki): `pool_size = ((core_count × 2) + effective_spindle_count)`. For 8-core + SSD: `(8×2)+1 = 17`. But: PostgreSQL has `max_connections=100`, 10 app instances × 17 = 170 > 100 → use PgBouncer (transaction pooling, max 1000 client conns to 25 server conns). Empirical: too-large pool — DB spends time context-switching, throughput drops; too-small — app waits for connections. Benchmark: pool=10 → 5k RPS; pool=20 → 8k RPS; pool=50 → 7k RPS (DB saturated); pool=100 → 4k RPS (context switch overhead). Tune: start with formula, benchmark with production-like load, watch DB CPU + wait events. Connection leak detection: `leakDetectionThreshold=60s`. Урок: bigger pool ≠ better throughput.

---

## 7. System Design кейсы (design Twitter, URL shortener, chat, news feed, notification system, rate limiter, distributed cache, search autocomplete, payment system, ticket booking)

### Теория (подход)

- **Универсальный шаблон system design interview** (Alex Xu, 6 steps): (1) Understand requirements — clarify scope, functional + non-functional; (2) Back-of-envelope estimation; (3) High-level design (1 diagram); (4) Deep dive (component-by-component); (5) Scaling (sharding, replication, cache); (6) Wrap-up (bottlenecks, monitoring, SLO).
- **Functional requirements** — что система делает: «user can post tweet», «user can follow another user». Non-functional: scale (DAU, QPS), latency (p99 < 200ms), availability (99.9%), consistency (strong/eventual), cost.
- **API design** — REST vs gRPC vs GraphQL vs WebSocket. REST: simple, cacheable, ubiquitous. gRPC: typed, bidirectional streaming, low overhead. GraphQL: client-driven, solves over-fetching. WebSocket: bidirectional, low latency.
- **Storage schema choice** — RDBMS (transactional, structured), NoSQL document (flexible schema, horizontal scale), wide-column (Cassandra, time-series, huge scale), key-value (Redis, simple), graph (Neo4j, relationships), search (Elasticsearch, full-text). Polyglot persistence — каждое concern со своим store.
- **Fan-out on write vs fan-out on read** — write: при новом твите — write to all followers' timelines (pre-computed); read: при read timeline — fetch & merge from followers. Twitter originally write (celebrity problem) → hybrid (write for normal, read for celebrities).
- **Notification fanout** — email (SES, SendGrid), push (APNs, FCM), SMS (Twilio), in-app (WebSocket/SSE). Per-channel rate limiting, user preferences, opt-out, batching.
- **Search autocomplete** — prefix tree (Trie), Redis sorted set (ZADD/ZREVRANGEBYLEX), Elasticsearch completion suggester. Personalized: top-N per user, A/B for diversity. Cache hot prefixes in-process.
- **Payment system** — PCI-DSS compliance (don't store PAN, tokenize), idempotency keys (no double-charge), reconciliation (daily batch vs ledger), refunds, fraud detection, webhooks (async status updates).
- **Ticket booking** — seat locking (TTT - temporary ticket token, 5 min), optimistic locking (version), saga for distributed booking (flight + hotel + car), inventory model (allocated per show/time).

### Материалы

- **Книга**: "System Design Interview – An Insider's Guide" Vol. 1 & 2, Alex Xu — https://www.amazon.com/System-Design-Interview-insiders-Second/dp/B08CMF2CQF
- **Сайт**: ByteByteGo courses — https://bytebytego.com/courses/system-design-interview/design-a-rate-limiter
- **Статья**: "Design a News Feed System" — https://bytebytego.com/courses/system-design-interview/design-a-news-feed-system
- **Статья**: "Design A URL Shortener" — https://bytebytego.com/courses/system-design-interview/design-a-url-shortener
- **Статья**: "Design A Chat System" — https://bytebytego.com/courses/system-design-interview/design-a-chat-system
- **Статья**: "Rate Limiting Fundamentals" — https://blog.bytebytego.com/p/rate-limiting-fundamentals
- **Сайт**: Grokking the System Design Interview — https://www.educative.io/courses/grokking-the-system-design-interview

### Практика / Кейсы

1. **Design Twitter (capacity → API → schema → timeline fan-out → caching → sharding → replication → rate limiting → monitoring)**. Шаги: (a) Capacity: 200M DAU, 500M tweets/day, 460k reads/s, 17k writes/s peak (см. раздел 6). (b) API: `POST /tweets`, `GET /home_timeline`, `GET /user_timeline/{userId}`, `POST /follows`. (c) Schema: `tweets(tweet_id BIGINT PK, user_id, content, media_urls, created_at)`, `users(user_id PK, ...)`, `follows(follower_id, followee_id)`, `timeline(user_id, tweet_id, created_at)` (pre-computed). (d) Fan-out: hybrid — for users with < 100k followers, write to all followers' timeline (async via Kafka); for celebrities (>100k followers) — don't fan-out, fetch on read. (e) Caching: Redis for hot timelines (LRU 100 GB), CDN for media. (f) Sharding: tweets by `tweet_id` (Snowflake ID = timestamp + shard + seq); timelines by `user_id` (32 shards). (g) Replication: write to leader, read from 2 replicas (read-your-writes для own tweets). (h) Rate limiting: 100 tweets/hour/user, 1000 follows/day. (i) Monitoring: tweet rate, timeline fan-out lag, p99 read latency, error budget. SLO: 99.9% availability, p99 < 200ms read.

2. **Design URL shortener (100M URLs, 10k shortens/day, 100M redirects/day)**. Шаги: (a) Capacity: 10k writes/day = 0.1/s; 100M reads/day = 1150/s avg, 5k/s peak. Storage: 100M URLs × 500 bytes = 50 GB. (b) API: `POST /shorten {long_url}` → `{short_url}`; `GET /{short_code}` → 301 redirect (cacheable). (c) Short code generation: base62 (a-zA-Z0-9) of 7 chars = 62^7 = 3.5T combinations. Options: hash (MD5 first 7 chars — collision risk), counter + base62, Snowflake, pre-generated pool. Choose counter-based (deterministic, sortable). (d) Schema: `urls(short_code PK, long_url, user_id, created_at, expires_at)`. (e) Caching: Redis for hot URLs (LRU 10M = 10 GB), 95% hit rate. (f) Sharding: by short_code hash, 8 shards. (g) High availability: 301 (cacheable, reduces load) vs 302 (allows analytics). Choose 301 with custom analytics endpoint. (h) Custom aliases: reserved words, rate limit per user. (i) Analytics: click events to Kafka → ClickHouse for dashboards. (j) Anti-abuse: rate limit, blocklist for spam URLs. SLO: 99.99% availability (read-heavy), p99 redirect < 50ms.

3. **Design WhatsApp-like chat (1B users, 50B messages/day)**. Шаги: (a) Capacity: 50B/86400 = 580k msg/s avg, 2M/s peak. Storage: 50B × 200 bytes = 10 TB/day. (b) Connection model: WebSocket (bidirectional), 1M concurrent connections per server (Netty + epoll, 100KB per connection = 100 GB RAM). Need 1000 servers for 1B users. (c) Message flow: sender → edge WS server → message router (by recipient_id hash) → recipient's edge WS server → recipient. (d) Offline messages: store in DB, deliver on reconnect. (e) Schema: `messages(msg_id PK, from, to, content, ts, status)`, partitioned by `(from, to)` pair hash. (f) Group chat: 1 message → fan-out to N recipients; for large groups (100+), use topic-based pub-sub. (g) End-to-end encryption: Signal protocol, keys per device, server can't decrypt. (h) Delivery status: sent/delivered/read receipts as separate small messages. (i) Media: upload to S3 presigned URL, link in message. (j) Last-seen: gossip protocol, eventual consistency. (k) Push notifications: APNs/FCM for offline users. SLO: p99 delivery < 1s for online, < 30s for offline.

4. **Design news feed ranking (Facebook-like, 1B users, 1000 items per user)**. Шаги: (a) Sources: friends' posts, groups, pages, ads. (b) Fan-out: write-time (pre-compute per user) — too expensive for users with many friends; read-time (compute on demand) — too slow. Hybrid: write-time for majority, read-time for influencers. (c) Ranking: ML model (features: recency, affinity, weight of post type, interactions) → score per (user, item) → top-N. (d) Architecture: candidate generation (recent 10k items) → ranking model (deep learning) → filtering (already seen) → final 100. (e) Storage: event log (Kafka), feature store (Redis/Feature Store), model (TensorFlow). (f) Personalization: real-time features (last 10 interactions) + batch (long-term history). (g) Cold start: new users — show popular + diversity. (h) A/B testing: feature flag, 1% → 10% → 100%. (i) Caching: top-N cached 5 min, refresh on new post. (j) Ads: separate ranking, interleaved with organic. SLO: p99 feed generation < 500ms, freshness < 5 min.

5. **Design rate limiter (distributed, 10M RPS, 1000 services)**. Шаги: (a) Requirements: per-user, per-API, per-IP; multiple strategies (token bucket, fixed window). (b) Architecture: API Gateway (Envoy/Kong) → Redis Cluster (rate state) → Lua atomic scripts. (c) Token bucket Lua (см. раздел 6). (d) Redis Cluster: 16 shards, key by user_id → hash slot. (e) Local cache (in-process, Caffeine) для hot keys — saves Redis round-trip. (f) Fallback: if Redis down — fail-open (allow) for non-critical, fail-close for billing. (g) Configuration: per-service YAML, hot-reload via Spring Cloud Config. (h) Response: 429 + `Retry-After` + `X-RateLimit-Limit/Remaining/Reset`. (i) Monitoring: rate-limit hits per service, top throttled users. (j) Multi-region: per-region Redis + global sync for cross-region quotas. SLO: 99.99% availability, p99 check < 1ms.

6. **Design distributed cache (Redis-compatible, 100 GB, 1M RPS)**. Шаги: (a) Architecture: client → proxy (Twemproxy/Cerberus) → Redis Cluster. (b) Sharding: consistent hashing, 16384 slots, 32 nodes × 512 slots. (c) Replication: 1 leader + 2 replicas per shard, async replication (read-your-writes via `WAIT` command). (d) Failover: Redis Sentinel или Cluster gossip (Gossip protocol), 30s detect failure, promote replica. (e) Eviction: maxmemory-policy = allkeys-lru. (f) Persistence: AOF every second (RPO 1s) + RDB snapshot hourly. (g) Cross-region: async replication to DR region (1-2s lag). (h) Hot keys: detect via `redis-cli --hotkeys`, replicate to N nodes (read from random replica). (i) Client: Lettuce (async, reactive), connection pool per node. (j) Monitoring: memory usage, hit ratio, evictions, replication lag, slow log. SLO: 99.99% availability, p99 GET < 1ms, p99 SET < 2ms.

7. **Design payment system (Stripe-like, 1M transactions/day)**. Шаги: (a) Requirements: process payment, refund, webhook notifications, reconciliation. (b) Idempotency: client sends `Idempotency-Key`, server stores (key, response) for 24h. (c) PCI-DSS: never store PAN (card number), tokenize via payment processor (Stripe, Adyen). (d) Flow: charge → processor → success/fail → update order → webhook to merchant. (e) Storage: `payments(payment_id PK, order_id, amount, currency, processor, status, idempotency_key UNIQUE)`. (f) At-least-once delivery for webhooks: retry with exponential backoff (1m, 5m, 30m, 4h, 24h), merchant must be idempotent. (g) Reconciliation: daily batch from processor, compare with our ledger, flag mismatches. (h) Refunds: separate flow, original payment must be captured. (i) Fraud: ML model (features: amount, velocity, geo) → score → block/review/allow. (j) Multi-currency: store in minor units (cents), convert at payment time. SLO: 99.999% availability (payments critical), p99 charge < 2s, no double-charge ever.

8. **Design ticket booking (concert, 50k seats, 100k concurrent users onsale)**. Шаги: (a) Requirements: browse event, select seat, hold (5 min), pay, confirm. (b) Browse: CDN-cached event page, seat map (image, not live). (c) Seat selection: WebSocket for live seat status; held seats shown as yellow. (d) Hold: `POST /holds {seat_ids, user_id}` → server sets Redis key `hold:{seat_id}` = `{user_id, expires_at}` TTL 5min → atomic via Lua (check all seats free, then set). (e) Pay: standard payment flow, on success → `confirm_hold` → write to DB `bookings`. (f) Inventory model: `seats(seat_id, event_id, status: available/held/booked, held_by, held_until)`. (g) Scaling: 50k seats fit in Redis (memory 50MB), 100k concurrent = 20 servers (5000 conn each). (h) Hot event: queue users before onsale (Cloudflare Waiting Room), admit at capacity. (i) Anti-bot: rate limit per IP, captcha, device fingerprint. (j) Post-sale: ticket QR codes (signed JWT), validation at venue via scan. SLO: 99.99% availability during onsale, no double-booking ever.

8. **Design search autocomplete (Google-like, 1B queries/day)**. Шаги: (a) Capacity: 1B/86400 = 11.5k QPS avg, 50k peak. Per query: prefix + personalization + ranking. (b) Data structure: Trie per language, top-N completions per node (pre-computed daily from query logs). Redis sorted set per prefix (top 1000 by score). (c) Architecture: edge cache (CDN) for top 1M prefixes (95% hit rate); app-level LRU (Caffeine) for last 5min personalization; backend Trie service for tail. (d) Personalization: blend global top-N with user's recent queries (weighted by recency). (e) Ranking: score = frequency × recency × personalization × diversity. (f) Real-time: trending queries (last 5min) bump score. (g) Schema: `query_log(query, user_id, ts, location)` partitioned by day → Spark job nightly → top-N per prefix → Redis. (h) Cold start: new query → not in top-N → fall back to Trie substring match. (i) Anti-abuse: rate limit, filter adult/spam queries. (j) Multi-language: separate Trie per locale. SLO: p99 < 50ms, 95% cache hit.

---

## 8. Алгоритмы и структуры данных (массивы, списки, стек, очередь, heap, hashmap, дерево BST/AVL/Red-Black, Trie, графы, union-find) — Big-O, LeetCode hard уровень

### Теория

- **Массивы (arrays)** — contiguous memory, O(1) random access, O(n) insert/delete (in middle). Динамические (ArrayList) амортизированно O(1) append (geometric growth 1.5×). Cache-friendly (sequential access). В Java — `int[]`, `Integer[]` (boxed, indirection), `ArrayList<T>` (array of Object).
- **Связные списки** — singly/doubly. O(1) insert/delete (если есть pointer), O(n) search. Не cache-friendly. В Java — `LinkedList` (doubly). Применение: LRU cache (doubly linked + hashmap), queue, polynomial multiplication. В современных JVM почти всегда проигрывает ArrayList из-за cache locality.
- **Стек (stack)** — LIFO. Array-based или linked-list. O(1) push/pop/peek. Применение: expression evaluation, function call stack, DFS, monotonic stack (next greater element), valid parentheses. В Java — `ArrayDeque` (faster than `Stack`, which is synchronized).
- **Очередь (queue)** — FIFO. Array-based (circular buffer) или linked-list. O(1) enqueue/dequeue. Применение: BFS, producer-consumer, task scheduler. В Java — `ArrayDeque`, `LinkedList`, `PriorityQueue`, `Deque` interface. Lock-free: `ConcurrentLinkedQueue` (Michael-Scott).
- **Heap (priority queue)** — complete binary tree, parent ≤/≥ children. Array-based. O(log n) insert/extract-min, O(n) build-heap (Floyd's algorithm). Min-heap / max-heap. В Java — `PriorityQueue` (min-heap, использует Comparator для max). Применение: top-K, Dijkstra, scheduling, median of stream (two heaps). `kth largest` = `O(n log k)` с heap size k.
- **Hashmap** — array of buckets + hash function. Average O(1) get/put. Collisions: chaining (linked list / tree at >8 in Java 8+ → O(log n)) или open addressing (linear probing, Robin Hood). Load factor 0.75 в Java. Rehashing при превышении. Cryptographic hash vs non-cryptographic. Consistent hashing — для distributed cache.
- **BST (binary search tree)** — left < root < right. Average O(log n) operations, worst O(n) (unbalanced). In-order traversal = sorted order. Применение: ordered map (`TreeMap` в Java — Red-Black tree).
- **AVL tree** — strictly balanced BST (height difference ≤ 1). Rotations: LL, RR, LR, RL. O(log n) guaranteed. Tighter balance than RB → better lookup, worse insert/delete (more rotations).
- **Red-Black tree** — relaxed balance (root black, red nodes have black children, all paths same black-height). O(log n) guaranteed, fewer rotations than AVL. В Java — `TreeMap`, `HashMap` buckets > 8. Linux CFS scheduler.
- **B-tree / B+tree** — balanced multi-way tree. B+tree: data only in leaves, leaves linked. Используется в БД (PostgreSQL, MySQL InnoDB). Minimizes disk I/O (high fan-out). В Java — `TreeMap` (RB), нет built-in B-tree.
- **Trie (prefix tree)** — tree по символам строки. O(L) search/insert (L = длина строки). Применение: autocomplete, dictionary, IP routing (longest prefix match), spell checker. Memory-optimization: radix tree (Patricia), DAWG.
- **Графы** — V vertices, E edges. Representations: adjacency matrix (O(V²), fast lookup), adjacency list (O(V+E), standard), edge list (O(E) iteration). Types: directed/undirected, weighted, cyclic/acyclic, connected. В Java — `Map<Integer, List<Integer>>` или custom class.
- **BFS / DFS** — обход графа. BFS (queue) — shortest path in unweighted, level-order; O(V+E). DFS (stack/recursion) — topological sort, cycle detection, SCC (Tarjan/Kosaraju); O(V+E). Iterative DFS через Stack (avoid stack overflow).
- **Union-Find (Disjoint Set Union, DSU)** — соединение множеств, проверка connected. Path compression + union by rank → near O(α(n)) ≈ O(1) amortized. Применение: Kruskal MST, dynamic connectivity, accounts merge.
- **Segment tree / Fenwick tree (BIT)** — range queries (sum, min, max) с point updates. Segment tree O(log n) update/query, 2n memory. Fenwick tree — simpler, less memory, only for invertible operations (sum). Применение: range sum, RMQ, inversion count.
- **Sparse table** — static RMQ, O(n log n) build, O(1) query. Только для idempotent operations (min, max, gcd).
- **Skip list** — вероятностная альтернатива BST. O(log n) average. В Java — `ConcurrentSkipListMap` (concurrent sorted map).
- **Big-O** — asymptotic complexity. O(1) < O(log n) < O(n) < O(n log n) < O(n²) < O(2^n) < O(n!). Amortized analysis (dynamic array append). Best/worst/average/expected. Space complexity (auxiliary).
- **Stability of sort** — stable: merge sort, insertion sort, bubble sort. Unstable: quicksort, heapsort. Java `Arrays.sort` для objects = TimSort (stable, O(n log n)); для primitives = dual-pivot quicksort.
- **In-place vs out-of-place** — in-place uses O(1) extra memory. Quicksort in-place, merge sort out-of-place (O(n)). Heapsort in-place, unstable.

### Материалы

- **Книга**: "Introduction to Algorithms" (CLRS), 4th ed., Cormen/Leiserson/Rivest/Stein (MIT Press, 2022) — канон.
- **Книга**: "Algorithms" 4th ed., Sedgewick & Wayne (Addison-Wesley, 2011) — с Java-кодом.
- **Сайт**: LeetCode — https://leetcode.com (Hard problems).
- **Статья**: "Algorithms and Data Structures Cheatsheet" — https://leetcode.com/discuss/study-guide/5346973/algorithms-and-data-structures-cheatsheet-part-3
- **Сайт**: AlgoMonster — https://algo.monster (pattern-based learning).
- **Книга**: "Elements of Programming Interviews" (EPI), Aziz/Lee/Prakash — Java версия.

### Практика / Кейсы

1. **Median of two sorted arrays (LeetCode 4, hard) в O(log(min(m,n)))**. Шаги: brute — merge + find middle, O(m+n). Better — binary search on smaller array: partition A at i, B at j = (m+n+1)/2 - i → check `A[i-1] <= B[j]` and `B[j-1] <= A[i]` → if yes, median = max(A[i-1], B[j-1]) (odd) or (max(left) + min(right))/2 (even) → if `A[i-1] > B[j]` — move left (i decreases) → else move right. Edge cases: i=0 or i=m (use ±infinity). Code ~30 lines. Урок: binary search on partition, not value.

2. **LRU Cache (LeetCode 146, medium-hard) — O(1) get/put**. Шаги: combine HashMap + doubly linked list. HashMap: key → Node. List: most recently used at head, least at tail. `get(key)`: lookup map → if exists, move node to head → return value. `put(key, val)`: if exists — update, move to head. Else — create node at head, put in map; if size > capacity — remove tail node, remove from map. Use sentinel head/tail to avoid null checks. `LinkedHashMap` с `removeEldestEntry` = built-in LRU. Урок: combination of two data structures = pattern.

3. **Trapping Rain Water (LeetCode 42, hard)**. Шаги: brute — for each position, find max left and max right, water = min(maxL, maxR) - height[i]; O(n²). DP — precompute leftMax[], rightMax[] arrays; O(n) time, O(n) space. Two pointers — left=0, right=n-1, leftMax=0, rightMax=0; if height[left] < height[right] — water at left = leftMax - height[left] (if positive), left++; similar for right; O(n) time, O(1) space. Stack-based — monotonic decreasing stack of indices; on each pop, compute water between current and stack top. Урок: multiple optimal solutions — know all 4.

4. **Word Ladder II (LeetCode 126, hard) — все кратчайшие пути**. Шаги: BFS to find shortest length AND build predecessor graph. Then DFS from end to start via predecessors to reconstruct all paths. Optimizations: bidirectional BFS (from beginWord AND endWord) — meets in middle, reduces branching. Word generalization: `hit → h*t, *it, hi*` — map wildcard to words. Time O(L² × N), space O(L × N). Edge cases: no path → empty list, single path. Урок: BFS+DFS combination for all shortest paths.

5. **Sliding Window Maximum (LeetCode 239, hard) — O(n) monotonic deque**. Шаги: deque stores indices, monotonic decreasing (front = max). For each new element: remove from back while nums[back] ≤ nums[i] (useless), append i. Remove from front if front index ≤ i - k (outside window). Result = nums[deque.front] for each window. Time O(n) — each element added/removed once. Space O(k). Same pattern: "longest subarray with at most K distinct". Урок: monotonic deque = classic.

6. **Alien Dictionary (LeetCode 269, hard) — topological sort**. Шаги: build graph — for each pair of adjacent words, find first differing char → edge from char1 to char2. Detect invalid input (prefix issue: "abc", "ab" — invalid). Topological sort via Kahn's algorithm (BFS) — in-degree map, queue of 0-in-degree, process, decrease neighbors. Cycle detection — if not all chars processed, cycle exists → return "". Result = topological order. Edge cases: duplicate edges, single char, multiple valid orders. Урок: build graph carefully + topo sort + cycle check.

7. **Range Sum Query 2D Mutable (LeetCode 308, hard)**. Шаги: naive — O(mn) per query. 2D Fenwick tree — O(log m × log n) update/query. Construction O(mn log m log n). Each row has its own BIT, update = for each affected BIT row, column-wise update. Query = sum over rows × columns via inclusion-exclusion. Alternative: 2D segment tree (more memory). Trade-off: BIT simpler but limited to invertible ops (sum, not min). Урок: 2D BIT — мощная, но редкая структура.

8. **Algorithm choice for production: heap vs sorted list vs BST**. Шаги: задача — top-10 items by score, continuously updated, queried frequently. (a) sorted list — O(n) insert, O(1) top-10 — bad for n inserts O(n²). (b) min-heap of size 10 — O(log 10) per insert, O(10) for top — good for streaming. (c) full BST (`TreeMap`) — O(log n) insert + top-10 via reverse iteration O(10). (d) `PriorityQueue` (heap) — recommended Java approach. Production concerns: memory bound, persistence, distribution. For distributed top-K across N nodes: each node local top-K → merge at aggregator (k-way merge via heap). Урок: data structure choice depends on operation frequency + data size; measure.

---

## 9. Алгоритмы (sorting: quick/merge/heap/radix; searching: binary/BFS/DFS; graphs: Dijkstra/Bellman-Ford/A*/topological sort; DP; backtracking; greedy; divide&conquer; sliding window; two pointers; monotonic stack)

### Теория

- **Quicksort** — divide: partition around pivot (Lomuto/Hoare), conquer: recurse on parts. Avg O(n log n), worst O(n²) (sorted input + bad pivot). In-place, unstable. Randomized pivot avoids worst case. Dual-pivot (Java `Arrays.sort` primitives). Tail recursion elimination on larger partition → O(log n) stack.
- **Merge sort** — divide: split half, conquer: sort each, merge. O(n log n) guaranteed, stable, out-of-place (O(n) extra). Used in Java `Arrays.sort(Object[])` (TimSort variant — adaptive, finds runs). External sort (large files) — k-way merge.
- **Heapsort** — build max-heap (O(n)), extract max n times (each O(log n)). In-place, unstable, O(n log n) guaranteed. Slower than quicksort in practice (poor cache). Java `PriorityQueue`.
- **Radix sort** — non-comparison, LSD/MSD by digit. O(d × (n + k)) where d = digits, k = radix. Stable. Fast for fixed-width integers/strings. Used in LSD string sort.
- **Counting sort** — O(n + k) for k distinct values in range. Stable. Used as subroutine in radix.
- **Binary search** — sorted array, O(log n). Variants: lower_bound, upper_bound, find insert position. Поиск в rotated sorted array (LeetCode 33). Median of two sorted (LeetCode 4). In Java — `Arrays.binarySearch`, `Collections.binarySearch`.
- **BFS / DFS** — см. раздел 8. BFS для shortest path in unweighted, level-order. DFS для cycle detection, topological sort, SCC, connected components.
- **Dijkstra** — shortest path, non-negative weights. Greedy: priority queue of (dist, node), process min. O((V+E) log V) with binary heap, O(V²) with array (better for dense). LeetCode 743 (Network Delay Time).
- **Bellman-Ford** — handles negative weights, detects negative cycles. O(VE). Iterates V-1 times, each edge relaxed. LeetCode 787 (Cheapest Flights with K stops — variant).
- **Floyd-Warshall** — all-pairs shortest paths. O(V³). DP: `dist[i][j] = min(dist[i][j], dist[i][k] + dist[k][j])`. LeetCode 1334.
- **A\* search** — Dijkstra + heuristic (admissible, consistent). Best-first with `f = g + h`. Used in pathfinding (games, maps). Heuristic example: Manhattan distance for grid.
- **Topological sort** — DAG only. Kahn's (BFS, in-degree) or DFS-based (post-order reversed). Detects cycle (if not all sorted → cycle). LeetCode 207 (Course Schedule), 210, 269.
- **Kruskal / Prim MST** — Kruskal: sort edges, union-find, O(E log E). Prim: priority queue, O(E log V). LeetCode 1584 (Min Cost to Connect Points).
- **Dynamic Programming (DP)** — optimal substructure + overlapping subproblems. Top-down (memoization) vs bottom-up (tabulation). Patterns: 1D (Fibonacci, climbing stairs), 2D (knapsack, edit distance), interval (matrix chain, burst balloons), state-machine (stock trading), bitmask (TSP), tree DP. LeetCode 312 (Burst Balloons, hard).
- **Backtracking** — DFS with state + undo. Combinations, permutations, subsets, N-Queens, Sudoku solver, palindrome partitioning. Pruning — отсечение ранних веток. LeetCode 51 (N-Queens), 37 (Sudoku).
- **Greedy** — локальный optimal = global optimal. Не всегда работает — нужно доказать (exchange argument). Примеры: activity selection, Huffman codes, Dijkstra, Kruskal. LeetCode 55 (Jump Game), 134 (Gas Station).
- **Divide & Conquer** — split, solve, combine. Merge sort, quicksort, FFT, closest pair. Master theorem: `T(n) = aT(n/b) + f(n)` → cases.
- **Sliding window** — fixed-size or variable-size window over array. O(n). Patterns: max sum subarray size k, longest substring with at most K distinct, longest without repeating chars, min window substring. LeetCode 76, 239.
- **Two pointers** — opposite ends (palindrome, two-sum sorted), same direction (remove duplicates, slow/fast for cycle detection). LeetCode 11 (Container with Most Water), 15 (3Sum), 141 (Linked List Cycle).
- **Monotonic stack** — stack maintained monotonic (inc/dec). Patterns: next greater element, largest rectangle in histogram, trapping rain water, daily temperatures. LeetCode 84 (Largest Rectangle), 42, 496, 739.
- **Bit manipulation** — XOR (single number), AND/OR (subsets), bitmasks (DP TSP), Brian Kernighan's algorithm (count set bits). LeetCode 338 (Counting Bits).
- **Interval problems** — merge intervals, insert interval, meeting rooms (sort by start/end). Sweep line (skyline problem). LeetCode 56, 57, 218.

### Материалы

- **Книга**: "Introduction to Algorithms" (CLRS), 4th ed. — главы 4 (divide & conquer), 15 (DP), 22-24 (graphs), 6 (heapsort), 7 (quicksort).
- **Сайт**: LeetCode — https://leetcode.com/problemset/all/?difficulty=HARD
- **Статья**: "Monotonic Stack - Guide + List of Problems" — https://leetcode.com/discuss/study-guide/5148505/Monotonic-Stack-Guide-%2B-List-of-Problems
- **Статья**: "All graph algorithms with a template - Dijkstra | Bellman-Ford | Floyd-Warshall" — https://leetcode.com/discuss/interview-question/6153851/
- **Статья**: "A noob's guide to Dijkstra's Algorithm" — https://leetcode.com/discuss/general-discussion/1059477/A-noob's-guide-to-Djikstra's-Algorithm
- **Курс**: MIT 6.006 Introduction to Algorithms — https://ocw.mit.edu/courses/6-006-introduction-to-algorithms-spring-2020/

### Практика / Кейсы

1. **Burst Balloons (LeetCode 312, hard) — interval DP**. Шаги: brute force — try each balloon last, O(n!). DP: reverse thinking — consider balloon `i` as LAST burst in range [left, right]. Coins = `nums[left-1] * nums[i] * nums[right+1]` + dp[left][i-1] + dp[i+1][right]. State: `dp[left][right]` = max coins. Recurrence: iterate length 1..n, for each [l,r], try each k as last. Time O(n³), space O(n²). Урок: think backwards (last to first).

2. **Edit Distance (LeetCode 72, hard) — 2D DP**. Шаги: `dp[i][j]` = min edits to convert word1[0..i] to word2[0..j]. Recurrence: if chars equal → `dp[i-1][j-1]`; else → `1 + min(dp[i-1][j] (delete), dp[i][j-1] (insert), dp[i-1][j-1] (replace))`. Base: `dp[0][j] = j`, `dp[i][0] = i`. Time O(mn), space O(mn) or O(min(m,n)) with rolling array. Урок: identify subproblem — prefixes.

3. **N-Queens (LeetCode 51, hard) — backtracking**. Шаги: place queens row by row, columns/ diagonals as constraints. Use sets for cols, main diag (row-col), anti-diag (row+col). When row == n — collect solution. Pruning: if no valid column, backtrack. Time O(N!) (worst). Optimizations: bit manipulation (int as set of available columns), symmetry (only first half + mirror). Урок: backtracking template — choose, explore, unchoose.

4. **Word Search II (LeetCode 212, hard) — Trie + DFS backtracking**. Шаги: build Trie from words. For each cell, DFS in 4 directions, descend Trie node. If node.isWord — add to result, mark word found (avoid dup). Pruning: if Trie branch has no words left — remove. Time O(M × N × 4^L) worst, much better with Trie pruning. Урок: Trie + backtracking = pattern for "find words in matrix".

5. **Median of Two Sorted Arrays (LeetCode 4, hard)** — см. раздел 8 кейс 1.

6. **Alien Dictionary (LeetCode 269, hard) — topological sort** — см. раздел 8 кейс 6.

7. **Longest Increasing Path in Matrix (LeetCode 329, hard) — DFS + memoization**. Шаги: for each cell, DFS in 4 directions where neighbor > current. Memoize `dp[i][j]` = longest path starting here. Result = max. Time O(MN), space O(MN). Урок: DFS + memo = DP on DAG (matrix DAG with edges to larger neighbors).

8. **Find Median from Data Stream (LeetCode 295, hard) — two heaps**. Шаги: max-heap for lower half, min-heap for upper half. Balance: size difference ≤ 1. Add: if ≤ max-heap top — push max-heap; else min-heap. Rebalance. Median: if equal size — avg of tops; else top of larger. Time O(log n) add, O(1) median. Урок: two heaps pattern — running median, sliding median.

9. **Regular Expression Matching (LeetCode 10, hard) — DP**. Шаги: `dp[i][j]` = does s[0..i] match pattern p[0..j]. If `p[j] == '*'`: match zero (`dp[i][j-2]`) or one+ (`dp[i-1][j]` if char matches `p[j-1]`). Else char match: `dp[i-1][j-1]`. Time O(mn). Урок: DP on strings — state = (i, j) indices, transitions = char/`*` rules.

---

## 10. Concurrent data structures (ConcurrentHashMap, SkipList, lock-free queue Michael-Scott, Treiber stack, Disruptor pattern)

### Теория

- **ConcurrentHashMap (Java)** — segment-locking (Java 7: 16 segments, each a HashMap with own lock) → CAS + synchronized on bin head (Java 8+). Read: volatile read, no lock. Write: CAS on bin head, fallback to synchronized. `size()` — optimistic (CounterCell stripes). Treeify bins > 8 (TreeBin — RB tree). `computeIfAbsent` — atomic per key (long lock hold risk).
- **ConcurrentHashMap internal mechanics** — `tab` is `volatile Node[]`. `casTabAt(tab, i, null, new Node)` — atomic insertion. `synchronized (f)` where `f = tabAt(tab, i)` — bin-level lock for chain/tree ops. `transfer()` — parallel resizing, each thread takes `stride` bins. `ForwardingNode` — marker for migrated bins, redirects reads to new table.
- **ConcurrentSkipListMap** — skip list (William Pugh 1990). Multiple linked lists at different levels, probabilistic height (coin flip). Search: start at top, go right while larger, drop down. O(log n) avg. Concurrent: CAS on nodes, lock-free reads, optimistic writes. Sorted iteration. Better than ConcurrentRBTree (no rebalancing).
- **ConcurrentLinkedQueue** — Michael-Scott lock-free queue (1996). FIFO. Head and tail pointers, CAS-based enqueue/dequeue. ABA problem avoided via `AtomicStampedReference` or implicit (Java GC prevents reuse). Linearizable.
- **Michael-Scott queue algorithm** — enqueue: create node, CAS tail.next from null to node, then CAS tail to node (may lag, other enqueuers help). Dequeue: read head, head.next; CAS head to next. Handle empty + consistency. Lazy tail advance.
- **Treiber stack** — lock-free LIFO. `push`: create node with next = top; CAS top from old to node; retry on failure. `pop`: read top; if null, return; CAS top from old to top.next. ABA risk — solved with versioned references (AtomicStampedReference) or elimination backoff (for low contention).
- **Disruptor pattern** — LMAX. Ring buffer (power-of-2 size), sequence numbers (LongArray, cache-line padded). Producers claim sequence via CAS, write to slot, publish. Consumers track sequence, batch-process. No locks, no allocation in steady state. Single-writer principle (per consumer). 10M+ msgs/sec on single thread. Used in Log4j2 AsyncLogger, Storm.
- **Disruptor internals** — `RingBuffer` (slots), `Sequence` (per consumer position, padded to avoid false sharing), `WaitStrategy` (Blocking, Sleeping, Yielding, BusySpin, LiteBlocking). `BatchEventProcessor` for consumers. Gating sequences — slowest consumer blocks producers. Memory barriers via `volatile` and `Unsafe.putOrderedObject` (lazySet).
- **Cache-line padding** — false sharing: multiple threads writing to fields in same 64-byte cache line → invalidation traffic. Fix: pad with `@Contended` (Java 8+, `-XX:-RestrictContended`) or manual `long p1,p2,...,p15` fields. Disruptor `Sequence` uses 15 longs (15×8 = 120 bytes padding).
- **CAS (Compare-And-Swap)** — atomic `if (mem == expected) mem = new`. `Unsafe.compareAndSwap*` (Java 8-) or `VarHandle.compareAndSet` (Java 9+). LL/SC on ARM. Strong vs weak CAS (weak may spuriously fail). Higher-level: `AtomicInteger`, `AtomicReference`, `LongAdder` (striped counters, faster under contention than AtomicLong).
- **LongAdder / Striped64** — `base` + `Cell[]` (hashed by thread). `sum()` = base + Σ cells. Contention spreads across cells. Better than AtomicLong under high write contention. `add()` — try CAS on cell, else create/expand. Use for counters (`hits`, `invocations`).
- **StampedLock** — optimistic read with stamp (long). `tryOptimisticRead()` returns stamp; `validate(stamp)` checks if still valid (no write occurred). If invalid — upgrade to read lock or retry. Better throughput than ReadWriteLock for read-mostly. Reentrant? No. Need careful usage.
- **CopyOnWriteArrayList** — writes copy entire array (O(n)), reads lock-free (volatile array). Best for read-heavy (99%), small lists (listeners, configs). Iterator doesn't reflect subsequent writes (snapshot).
- **Non-blocking algorithms progression** — mutex (blocking) → read-write lock (concurrent reads) → lock-free (CAS, system-wide progress) → wait-free (per-thread progress, harder). Lock-free harder to design + verify.
- **Memory model relevance** — `volatile` = visibility (no reordering with normal access) + atomicity for long/double. `synchronized` = mutual exclusion + memory barrier. `AtomicReference.set/get` = volatile semantics. Final fields = safe publication (immutable objects).
- **Memory visibility in lock-free** — publishing object: `field = new Object()` — without volatile, another thread may see partially constructed (final fields + freeze help). Safe publication: volatile, AtomicReference, Concurrent collections, synchronized blocks.
- **Amdahl's law for concurrent data structures** — speedup limited by serial fraction (CAS contention). To scale: striping (segments), sharding (per-key locks), elimination (Treiber stack — pops meet pushes and exchange).
- **Java Memory Layout** — object header (12 bytes mark word + 4 bytes klass pointer on 64-bit compressed), alignment to 8 bytes. `jol` (Java Object Layout) library for inspection. Compact strings (Java 9+) — byte[] instead of char[]. Relevant for memory-constrained lock-free.

### Материалы

- **Книга**: "Java Concurrency in Practice", Goetz/Peierls/Bloch/Bowbeer/Holmes/Lea (Addison-Wesley, 2006) — главы 5, 13.
- **Книга**: "The Art of Multiprocessor Programming" 2nd ed., Herlihy & Shavit (MIT Press, 2020).
- **Статья**: "Low Latency Java with the Disruptor" — https://blog.scottlogic.com/2021/12/01/disruptor.html
- **Статья**: "java.util.concurrent Package Tour" — https://algomaster.io/learn/concurrency-interview/java-util-concurrent-package-tour
- **Дока**: LMAX Disruptor — https://lmax-exchange.github.io/disruptor/
- **Статья**: "Java Concurrency: Day 11: Lock Free Ring Buffer" — https://peerlist.io/scroll/post/ACTHP69DN7BNJLJMAHK8MK6NOLD9JB
- **Исходники**: OpenJDK ConcurrentHashMap — https://github.com/openjdk/jdk/blob/master/src/java.base/share/classes/java/util/concurrent/ConcurrentHashMap.java

### Практика / Кейсы

1. **Реализовать Michael-Scott lock-free queue**. Шаги: `class MSQueue<T> { AtomicReference<Node<T>> head, tail; static class Node<T> { T value; AtomicReference<Node<T>> next; } }` → constructor: dummy node, head=tail=dummy. `enqueue(T v)`: `Node<T> n = new Node<>(v); while (true) { Node<T> t = tail.get(); Node<T> next = t.next.get(); if (t == tail.get()) { if (next == null) { if (t.next.compareAndSet(null, n)) { tail.compareAndSet(t, n); return; } } else { tail.compareAndSet(t, next); } } }` → `dequeue()`: similar loop, advance head, return head.next.value. Test: 8 threads × 1M ops — no lost/duplicate items. Verify with `jcstress` for memory model. Урок: lock-free = subtle, JCStress is mandatory.

2. **ConcurrentHashMap tuning для high-contention cache**. Шаги: profile — `ConcurrentHashMap.computeIfAbsent` holds lock per bin, slow lambda blocks others. Solutions: (a) pre-compute value outside map, `putIfAbsent` (avoid long lambda); (b) `Cache.get(key, k -> loader.load(k))` with Caffeine (uses ConcurrentMap + async). (c) stripe locks — `Stripe<L>` with N locks, hash key to lock. (d) For 10M entries — `initialCapacity` (avoid resizes), `concurrencyLevel` (deprecated in Java 8+, but still affects internal sizing). Benchmark: JMH with `@Threads(8) @BenchmarkMode(Throughput)`. Урок: computeIfAbsent is footgun — long lambdas block bins.

3. **Disruptor для event sourcing pipeline (10M events/sec)**. Шаги: define `Event` class (long sequence, payload). `RingBuffer<Event> rb = RingBuffer.createSingleProducer(Event::new, 1<<20 /* 1M slots */, new BlockingWaitStrategy());` → consumers: `BatchEventProcessor<Event> p1 = new BatchEventProcessor<>(rb, rb.newBarrier(), (event, sequence, endOfBatch) -> process(event));` → dependency: `EventHandler h2 = ...; rb.addGatingSequences(p2.getSequence()); rb.addGatingSequences(p1.getSequence());` (p2 after p1) → producer: `long seq = rb.next(); rb.get(seq).set(...); rb.publish(seq);` → benchmark: single-producer single-consumer = 10M/sec; multi-consumer diamond (p1 → [p2,p3] → p4) = 1-3M/sec. Compare to BlockingQueue: 100K/sec. Урок: Disruptor massive perf win for high-throughput pipelines.

4. **Treiber stack с elimination backoff**. Шаги: basic Treiber: `push` CAS top, `pop` CAS top.next. Contention hotspot. Elimination: array of slots (size = num threads × 2). `push`: pick random slot, CAS null → own request; wait; if popped by another thread — done; else timeout → fall back to main stack. `pop`: pick random slot, if slot has push request — CAS slot to null, return pushed value (eliminated). On contention — pushes/pops eliminate in pairs. Implementation: `AtomicReferenceArray<Exchange>` for slots. Benchmark: 8 threads, 80% push, 20% pop — elimination doubles throughput at high contention. Урок: elimination = backoff + helping.

5. **StampedLock для read-heavy cache**. Шаги: `StampedLock sl = new StampedLock(); Map<K,V> map = new HashMap<>();` → `get(K k)`: `long stamp = sl.tryOptimisticRead(); V v = map.get(k); if (!sl.validate(stamp)) { stamp = sl.readLock(); try { v = map.get(k); } finally { sl.unlockRead(stamp); } } return v;` → `put(K k, V v)`: `long stamp = sl.writeLock(); try { map.put(k, v); } finally { sl.unlockWrite(stamp); }` → benchmark vs `synchronized` (RW lock): optimistic read 3-10× faster in 99% read case; write contention — degrades like RW lock. Trade-off: not reentrant, not thread-safe with exceptions (must unlock in finally). Урок: StampedLock for read-heavy + careful code; for general use — ConcurrentHashMap.

6. **LongAdder vs AtomicLong benchmarking**. Шаги: JMH `@Benchmark @Threads(8) public void inc() { counter.increment(); }` → AtomicLong: 50M ops/sec → LongAdder: 500M ops/sec (10×) under contention → explain: AtomicLong CAS contention serializes threads; LongAdder stripes across cells → sum() is approximate but cheap. Use cases: AtomicLong — for low-contention, exact value needed (e.g., sequence generator); LongAdder — high-contention counters (hits, requests). Caveat: `sum()` is not atomic snapshot (writes during sum). Урок: choose LongAdder for counters, AtomicLong for sequence/id.

7. **ConcurrentLinkedQueue vs ArrayBlockingQueue vs Disruptor — выбор**. Шаги: requirements matrix: throughput, latency, backpressure needed, blocking OK? → ArrayBlockingQueue (lock-based, blocking, backpressure) — for producer-consumer with bounded queue, e.g. log appender. → ConcurrentLinkedQueue (lock-free, non-blocking, unbounded) — for unbounded work queue, e.g. thread pool work queue (but unbounded = OOM risk). → Disruptor (ring buffer, batch, no allocation) — for ultra-low-latency pipelines, e.g. trading. JMH benchmarks: 1P-1C 1M msgs — ArrayBlockingQueue 100K/sec, CLQ 500K/sec, Disruptor 10M/sec. Trade-offs: complexity, error handling, batching semantics. Урок: pick simplest that meets SLO — Disruptor только если latency critical.

---

## 11. Clean Code & Refactoring (code smells, refactoring techniques Fowler, Boy Scout rule, technical debt quadrant)

### Теория

- **Clean Code principles** (Robert C. Martin): meaningful names (intention-revealing), small functions (do one thing, one level of abstraction), minimal arguments (≤3 ideal), no side effects, command-query separation, error handling without exceptions (return Result), no dead code, DRY, comments only for "why" not "what".
- **Code smells (Fowler)** — symptoms of deeper problems. Categories: Bloaters (Long Method, Large Class, Primitive Obsession, Long Parameter List, Data Clumps), Object-Orientation Abusers (Switch Statements, Temporary Field, Refused Bequest, Alternative Classes with Different Interfaces), Change Preventers (Divergent Change, Shotgun Surgery, Parallel Inheritance Hierarchies), Dispensables (Comments, Duplicate Code, Lazy Class, Data Class, Dead Code, Speculative Generality), Couplers (Feature Envy, Inappropriate Intimacy, Message Chains, Middle Man).
- **Long Method smell** — extract method, replace temp with query, decompose conditional. Threshold: > 20-30 lines (loose), but more important = levels of abstraction mixed.
- **Large Class / God Object** — extract class, extract subclass, extract interface, replace data with object. Measure: cyclomatic complexity, number of dependencies.
- **Primitive Obsession** — replace primitive with object (`String` userId → `UserId` value object). Replace type code with class/State/Strategy. Replace array with object.
- **Long Parameter List** — introduce parameter object, preserve whole object, replace parameter with method call.
- **Refactoring techniques (Fowler catalog)** — Extract Method, Inline Method, Extract Class, Move Method, Rename, Replace Conditional with Polymorphism, Replace Inheritance with Delegation, Replace Magic Number with Symbolic Constant, Replace Exception with Test, Introduce Parameter Object, Replace Method with Method Object.
- **Refactoring workflow** — (1) ensure tests cover the code (refactor only with safety net); (2) identify smell; (3) pick refactoring; (4) apply mechanically (small steps, IDE tools); (5) run tests after each step; (6) commit frequently.
- **Boy Scout Rule** — «Always leave the code behind in a better state than you found it» (Robert C. Martin, adapted from Boy Scouts). Small refactors during feature work — rename variable, extract method, fix typo. Accumulates over time.
- **Opportunistic refactoring** (Fowler) — refactor while doing feature work or bug fix in same area. Not separate sprint, not big rewrite. Combined with Boy Scout Rule.
- **Preparatory refactoring** (Fowler) — refactor code to make upcoming change easier (e.g., extract method before adding new branch). «Make the change easy, then make the easy change» (Kent Beck).
- **Technical Debt Quadrant** (Martin Fowler): (1) Reckless & Deliberate — «we don't have time for design»; (2) Reckless & Inadvertent — «what is layered architecture?»; (3) Prudent & Deliberate — «we must ship now and deal with consequences»; (4) Prudent & Inadvertent — «now we know how we should have done it». Only (1) is unacceptable; (4) is unavoidable learning.
- **Technical debt types** — code debt (bad code), design debt (architecture issues), testing debt (low coverage), documentation debt, dependency debt (outdated libs), infrastructure debt. Track in backlog, allocate 20-30% capacity.
- **Code metrics** — cyclomatic complexity (CC, ≥10 = smell), cognitive complexity (SonarQube), NPath, fan-in/fan-out, afferent/efferent coupling, LCOM4 (cohesion), duplication (CPD). Tools: SonarQube, Code Climate, IntelliJ inspections.
- **Self-documenting code** — prefer clear names over comments; comments for "why" (business rule, workaround, external constraint); avoid redundant comments (`i++; // increment i`); Javadoc only for public API.
- **Naming conventions** — classes = nouns (NounPhrase), methods = verbs (verbPhrase), boolean = is/has/can/should. Avoid: `data`, `info`, `manager`, `helper`, `util`, `temp`, `foo`. Domain language (DDD ubiquitous language).
- **Function composition over deep nesting** — guard clauses (early return), avoid arrow code (`if { if { if }}`). Stream API in Java reduces nesting.
- **Refactoring tools** — IntelliJ IDEA (extract, inline, rename, change signature, move, pull up/push down), Eclipse, VSCode Java extension. Safe rename/move needs tooling (manual = risk).
- **Code review vs refactoring** — review identifies smells, refactoring fixes them. Both are ongoing practices, not phases.

### Материалы

- **Книга**: "Clean Code: A Handbook of Agile Software Craftsmanship", Robert C. Martin (Pearson, 2008).
- **Книга**: "Refactoring: Improving the Design of Existing Code" 2nd ed., Martin Fowler (Addison-Wesley, 2018) — с JavaScript примерами, но концепции universal.
- **Книга**: "Clean Code" 2nd ed. — в работе, следите за выходом.
- **Сайт**: Refactoring.Guru — https://refactoring.guru/refactoring/smells (catalog smells + techniques).
- **Статья**: Martin Fowler — "Opportunistic Refactoring" — https://martinfowler.com/bliki/OpportunisticRefactoring.html
- **Статья**: Martin Fowler — "Technical Debt Quadrant" — https://martinfowler.com/bliki/TechnicalDebtQuadrant.html
- **Статья**: "The Boy Scout Rule" — https://matiasfrndz.ch/2015/04/The-Boy-Scout-Rule

### Практика / Кейсы

1. **Refactor God Class `OrderProcessor` (1500 строк, CC avg 15, 20 dependencies)**. Шаги: (a) ensure tests — coverage 60% → add characterization tests for happy path + edge cases; (b) extract responsibilities: `OrderValidator`, `OrderPricer`, `OrderInventoryChecker`, `OrderPaymentProcessor`, `OrderNotificationSender` — each a separate class with single responsibility; (c) introduce `Order` Aggregate Root that orchestrates via methods; (d) `OrderProcessor` becomes thin Application Service delegating to components; (e) replace switch on `OrderType` with polymorphism (Strategy pattern); (f) extract magic numbers (`MAX_ITEMS_PER_ORDER = 100`) to constants; (g) replace primitive `String customerId` with `CustomerId` value object. After: avg CC 5, max class size 200 lines, test coverage 85%.

2. **Long Method refactoring: `calculateInvoice()` (180 lines, nested 6 levels)**. Шаги: identify levels of abstraction mixed (validation, calculation, formatting, persistence) → extract `validate()`, `calculateSubtotal()`, `calculateTax()`, `calculateTotal()`, `formatInvoice()`, `saveInvoice()` → each ~20 lines, single responsibility → replace nested if/else with guard clauses (early returns) → replace temp variables with queries (`double tax = ...` → `calculateTax()` method) → introduce `Invoice` value object to bundle subtotal/tax/total → final method: `validate(); Invoice inv = calculate(); format(inv); save(inv);` — 5 lines, top-level orchestration. Coverage 100%.

3. **Primitive Obsession: 20 `String` user IDs → `UserId` value object**. Шаги: define `record UserId(String value) { public UserId { Objects.requireNonNull(value); if (!value.matches("usr_[a-zA-Z0-9]+")) throw new IllegalArgumentException(); } }` → migrate code: IDE "Replace type" wrapper (or manual), one method at a time → tests: each conversion = small commit, run tests → old API: keep `String` overload for backward compat, mark `@Deprecated` → after all callers migrated — remove overload → benefits: type safety (can't pass `OrderId` as `UserId`), validation at construction, ubiquitous language. Catch: serialization (Jackson needs `@JsonValue` on `value()`).

4. **Replace conditional with polymorphism: `switch (order.type)` in 5 places**. Шаги: identify types — `STANDARD`, `EXPRESS`, `BULK` → introduce sealed interface `interface OrderType permits Standard, Express, Bulk {}` → each `case` becomes method in concrete class: `record Standard() implements OrderType { public Money shippingCost() { ... } public Duration deliveryTime() { ... } }` → replace `switch` with `order.type.shippingCost()` (dynamic dispatch) → tests: contract test for `OrderType` interface — each subtype passes → add new type = new record, no changes to existing (OCP). After: 5 switches eliminated, adding new type is trivial.

5. **Shotgun Surgery: changing discount logic requires edits in 8 files**. Шаги: identify all places where discount calculated → consolidate into `DiscountPolicy` class (or Spring `@Component`) → all callers depend on `DiscountPolicy` interface → change one place, affects all → handle edge cases: per-customer overrides, time-based discounts → tests: single test class for `DiscountPolicy`. Smell source: business rule scattered due to incremental patches over time. Preventive: feature ownership — one team owns discount logic.

6. **Feature Envy: `Invoice` method uses 80% `Customer` data**. Шаги: identify — `Invoice.getCustomerDisplay()` accesses `customer.getName()`, `customer.getEmail()`, `customer.getAddress().format()`, `customer.getDiscountTier()` → move method to `Customer`: `Customer.getDisplayInfo()` → or extract `CustomerDisplayFormatter` if formatting logic complex → if `Customer` is in different bounded context (DDD) — use DTO projection (read model) instead of moving → test both classes. Урок: Feature Envy = misplaced responsibility.

7. **Preparatory refactoring: before adding new payment method**. Шаги: task — add PayPal support. Current code: `processPayment(CreditCard card)` → preparatory: extract `PaymentProvider` interface, refactor existing code to use it (`CreditCardProcessor implements PaymentProvider`) → now adding `PayPalProcessor implements PaymentProvider` is one new class, no changes to existing → Kent Beck: «Make the change easy, then make the easy change». Time investment: 2h prep refactoring + 1h feature vs 4h feature without prep = win. Урок: prep refactoring pays off when change is non-trivial.

---

## 12. Code Review (checklist, security review, performance review, anti-patterns, constructive feedback, review tools Gerrit/GitHub PRs)

### Теория

- **Code review goals** — catch bugs early, share knowledge, enforce standards, mentor juniors, improve maintainability, security, performance. NOT: gatekeeping, ego-stroking, style nitpicking (automate).
- **Review checklist (functionality)** — does code do what it claims? edge cases? error handling? tests cover happy path + sad path + boundary? Idempotency where needed? Transaction boundaries correct? Backward compatibility?
- **Review checklist (design)** — SOLID adherence? appropriate patterns? right level of abstraction? separation of concerns? DDD boundaries respected? API design (will it be easy to use correctly, hard to misuse — Joshi)? naming clear? error messages helpful?
- **Review checklist (security)** — input validation? SQL injection (parameterized queries)? XSS (escaping)? CSRF tokens? authentication/authorization checks? secrets in code/logs? OWASP Top 10? dependency vulnerabilities (SCA)? deserialization of untrusted data? insecure crypto?
- **Review checklist (performance)** — N+1 queries? unnecessary object creation in hot loop? proper collection size? algorithmic complexity? blocking I/O in async path? caching opportunities? memory leaks (static collections, listeners)? thread safety? lock contention?
- **Review checklist (concurrency)** — visibility (volatile/Atomic)? mutual exclusion correct? deadlock risk (lock ordering)? happens-before established? race conditions? immutable safe publication? `synchronized` on `this` (leak)? `ConcurrentHashMap.computeIfAbsent` with long lambda? `Object.wait()` in loop?
- **Review checklist (tests)** — coverage meaningful (not 100% line)? tests fast (< 1s)? test names describe behavior? one assert per test (or cohesive group)? test isolation (no order dependency)? mocks minimal? integration tests for boundaries? mutation testing?
- **Anti-patterns in code review** — LGTM (lazy review); bike-shedding (focus on trivial); ad hominem; "in my day..."; driving personal style; reviewing what you don't understand; nitpicking formatting (automate!); blocking for personal preference; too many reviewers (no one responsible).
- **Constructive feedback** — be specific (line, issue, fix); explain why (not just what); offer alternatives; acknowledge good code; ask questions («have you considered...?»); separate "must fix" from "suggestion" from "nit"; assume good intent; respect author's time.
- **Feedback framing** — observation → impact → request. Example: «This method is 200 lines (observation), which makes it hard to test and modify (impact), could we extract the validation logic? (request)». Avoid: «This is awful», «Why did you do X?».
- **PR size matters** — small PRs (50-200 LOC) reviewed better than large (1000+ LOC). Studies: defect density increases with PR size, review quality drops. Break large changes into series of small PRs. Stack PRs (Graphite, Stacked Diffs).
- **Review tools** — GitHub Pull Requests, GitLab Merge Requests, Gerrit (per-line comments, change numbers), Bitbucket. Conftools: Reviewable.io. Automations: SonarQube, CodeClimate, Snyk, Dependabot, Renovate.
- **GitHub PR features** — reviews (approve/request changes/comment), threads, suggestions (commit directly), draft PRs, branch protection, CODEOWNERS, status checks, merge strategies (squash/rebase/merge commit), auto-merge.
- **Gerrit** — change-based (not branch-based), each change = one commit, amend workflow, per-line inline comments, review labels (Code-Review+2, Verified+1), used by Android, Chromium, GerritForge.
- **CODEOWNERS** — file-pattern → team/user. Auto-request review. Per-directory ownership. Enforce via branch protection (required review from owner).
- **Approval policies** — one approver for trivial, two for major, security review for auth/payment, architecture review for cross-cutting. Risk-based, not one-size-fits-all.
- **Review SLA** — first response within 4h (business hours), full review within 1 business day. Blockers escalate. Don't let PRs sit — context switching cost.
- **Pair programming as review** — real-time review, knowledge transfer. Particularly good for: new hires, complex code, security-critical, refactoring. Tradeoff: time investment (2× person-hours).
- **Review metrics** — review depth (comments per PR), review time, rework cycles, defect escape rate (bugs found post-merge). Track trends, not individuals.
- **Async vs sync review** — async for routine, sync (call, pair) for complex or disagreement. Don't have endless comment threads — switch to chat/voice after 2-3 rounds.

### Материалы

- **Книга**: "Best Kept Secrets of Peer Code Review", Jason Cohen (Smartbear, 2006) — исследование code review practices.
- **Книга**: "Handbook of Code Review", Trisha Gee (в работе).
- **Статья**: Google Engineering Practices — Code Review — https://google.github.io/eng-practices/review/
- **Статья**: "Code Review Guidelines for Humans" — https://phauer.com/2018/code-review-guidelines/
- **Дока**: GitHub Pull Requests — https://docs.github.com/en/pull-requests
- **Дока**: Gerrit Code Review — https://www.gerritcodereview.com/
- **Статья**: "How to Make Your Code Reviews More Empathetic" — https://medium.com/@copyconstruct/empathy-in-code-reviews

### Практика / Кейсы

1. **Checklist-driven review для security-sensitive payment PR**. Шаги: автоматический — SAST (SonarQube), SCA (Snyk), secret scan (gitleaks). Manual: OWASP-focused — input validation on `amount` (>0, <MAX_AMOUNT, integer cents not float double), idempotency key check (no double-charge), SQL queries parameterized, no `Log.info(creditCardNumber)`, PCI scope (no PAN storage), crypto (use `javax.crypto` with AES-GCM, not DES/MD5), secure random (`SecureRandom`, not `Random`), exception messages don't leak internal info, audit log for every state transition. Required: 2 approvers (1 from security team). Documentation: threat model updated if surface changed.

2. **Performance review для hot path (10k RPS endpoint)**. Шаги: profile — JMH benchmark added? GC pressure — allocation per request? (`-XX:+PrintStringDeduplicationStatistics`)? N+1 queries? — `@EntityGraph` or JOIN FETCH? Cache TTL appropriate? Connection pool sizing? — HikariCP `maximumPoolSize` не < (cores × 2) + spindles. Locks? — `synchronized(this)` on hot path = contention, use `ConcurrentHashMap`/`LongAdder`. Backpressure? — bounded queue, fail-fast on overflow. Network: batching? compression? HTTP keep-alive? Reviewer asks: «What's p99 latency? Did you load-test?». Required: JMH benchmarks in PR if perf-sensitive.

3. **Resolve disagreement on design pattern choice**. Сценарий: author used Singleton, reviewer suggests DI. Шаги: switch to chat/voice after 3 comment rounds. Each side explains: author — «need single instance, no DI container in this lib»; reviewer — «hard to test, hidden dependency». Seek third opinion (tech lead) if needed. Compromise: `Holder` idiom (testable via reflection) or refactor to allow DI with default for non-DI users. Document decision in ADR. Lesson: opinions are fine, but seek principled decision, not "because I prefer".

4. **Large PR breakdown (5000 LOC → 8 small PRs)**. Шаги: identify logical chunks (feature 1, feature 2, refactor, tests, docs) → create base branch → stack PRs: PR1 (refactor, no behavior change) → PR2 (foundation: new interfaces) → PR3 (impl A) → PR4 (impl B) → ... → PR8 (wire up + tests). Each PR: < 500 LOC, < 1 day review. Use stacked-diffs tool (Graphite) if many. CI runs on each PR. Final merge: PR1 first, then sequentially. Author benefit: faster review, easier rollback. Reviewer benefit: manageable context.

5. **Review a PR from junior dev — coaching without discouraging**. Шаги: start with positive: «Nice use of sealed interface here». Use questions: «What happens if `userId` is null here?». Suggest with rationale: «Consider extracting `validateAmount` — easier to test and reuse». Share knowledge: «We had a bug similar to this last year, here's the postmortem». Set expectations clearly: must-fix = «This will NPE under load», suggestion = «Would be cleaner if...». Avoid: «This is wrong», «Why didn't you...». End with: «Looking forward to the next iteration».

6. **Implement CODEOWNERS for monorepo (10 teams, 500k LOC)**. Шаги: `.github/CODEOWNERS` with patterns: `src/payments/    @payments-team` / `src/auth/        @security-team @auth-team` / `*.tf              @devops-team` / `pom.xml          @arch-team` → branch protection: require review from CODEOWNER → auto-request via GitHub → for cross-cutting changes (e.g., new shared util) — explicit `@arch-team` review → for ambiguous ownership — define in `OWNERS.yaml` (more flexible) → quarterly review: who's actually reviewing? redistribute if bottleneck.

7. **PR review metrics dashboard**. Шаги: collect — PR size (LOC), time-to-first-review, time-to-merge, comments per PR, rework cycles (iterations), defect rate post-merge (from Jira/Linear). Visualize in Grafana. Identify outliers: PRs > 1000 LOC = coach author; PRs open > 5 days = unblock; reviewers with <5% comment rate = coach for depth. Don't: rank individuals, use as punitive. Do: identify systemic issues (team A has slow reviews — investigate).

---

## 13. Testing strategy (test pyramid, trophy, ICE score, mutation testing, testing strategies для microservices)

### Теория

- **Test Pyramid (Mike Cohn)** — много unit-тестов (base), меньше integration (middle), мало end-to-end (top). Принцип: fast/cheap at bottom, slow/expensive at top. Anti-pyramid: ice-cream cone (lots of E2E, few unit) — slow, flaky, hard to debug.
- **Test Trophy (Kent C. Dodds)** — для frontend/JS: много integration (base), unit (middle), some E2E (top), static types (small cap). Integration tests give most confidence per effort in UI-heavy apps.
- **Testing shapes (Fowler 2021)** — «On the Diverse And Fantastical Shapes of Testing» — criticizes rigid pyramid; suggests thinking about: business-facing vs technology-facing, support programming vs critique product. Multiple valid shapes.
- **Unit test** — изолирует SUT (System Under Test), все collaborators — test doubles (mocks/stubs). Fast (<10ms), deterministic, one behavior per test. JDK: JUnit 5, TestNG. Mocks: Mockito, EasyMock.
- **Integration test** — реальная БД (Testcontainers), реальный HTTP server (WireMock для external), in-memory или containerized. Spring: `@SpringBootTest`, `@DataJpaTest`, `@WebMvcTest`. Slow (100ms-10s), but catch wiring bugs.
- **End-to-end (E2E)** — full stack, real browser (Selenium, Playwright, Cypress). Slow (10s-1min), flaky, catch user-facing bugs. Limit to critical paths.
- **Contract testing** — consumer-driven contracts (Pact). Consumer writes «I expect this response», provider verifies. CI: each provider + consumer pipeline runs pact verification. Solves microservices integration testing without full E2E.
- **Component test** — тестирует один микросервис с внешними зависимостями mocked/stubbed. Mid-level between integration and E2E.
- **Smoke test** — после деплоя: критические пути работают (`/health` returns 200, can login, can place order). Fast, catches deployment issues.
- **Mutation testing** — внедряет баги в код (изменяет `+` на `-`, removes null check), проверяет: какие тесты падают? Если тесты не падают — они слабые. Tools: PITest (Java), Stryker (JS). Mutation score = % mutants killed.
- **Test doubles (Meszaros)** — dummy (передаётся, не используется), stub (возвращает предопределённые ответы), spy (записывает вызовы), mock (ожидает specific calls), fake (реальная, но упрощённая реализация — in-memory DB). Mockito covers all.
- **AAA pattern** — Arrange (setup), Act (call), Assert (verify). Given-When-Then (BDD). One assert per test (or cohesive group).
- **Test naming** — `should_<expected>_when_<condition>` (e.g., `should_throw_when_amount_is_negative`). Or BDD: `when_X_then_Y`. Describes behavior, not implementation.
- **FIRST principles** — Fast, Independent, Repeatable, Self-validating, Timely. Tests should be: fast (seconds, not minutes), independent (no order), repeatable (any env, any time), self-validating (pass/fail, no manual check), timely (written with/before code).
- **TDD (Test-Driven Development)** — Red-Green-Refactor. Write failing test → make it pass (minimal) → refactor. Tests drive design. Outside-in (London school, mockist) vs Inside-out (Chicago school, classicist).
- **Property-based testing** — генерирует случайные входы, проверяет инварианты (property). `jqwik` для Java. Example: for any list, `reverse(reverse(l)) == l`. Better coverage than example-based for edge cases.
- **Testing microservices strategies** — unit (per service), integration (per service + DB), contract (Pact between services), component (single service + mocks), E2E (full system, rare). Outbox pattern test: event actually published? Saga test: compensations work? Idempotency test: replay doesn't double-apply.
- **ICE score** — Impact, Confidence, Ease. Для prioritization (A/B tests, features, refactor candidates). Each 1-10. Score = I × C × E. Useful for backlog grooming.
- **Test coverage** — line, branch, method. 100% line coverage ≠ bug-free. Mutation score better metric. Don't mandate % — set floor (e.g., 80% line) + critical paths 100%.
- **Flaky tests** — fail randomly without code change. Causes: test order dependency, shared state, time-dependent (`LocalDate.now()`), external service, race conditions. Zero tolerance: quarantine + fix within 24h. Track flakiness rate.
- **Snapshot testing** — для serializable output (JSON, HTML). Snapshot stored, next run compared. Tools: Jest snapshot, ApprovalTests.Java. Careful: easy to update snapshot without thinking.
- **Performance testing** — load (expected), stress (beyond), soak (long duration), spike (sudden bursts), breakpoint (find limit). Tools: JMeter, Gatling, k6, wrk2. Continuous: benchmarks in CI (JMH).

### Материалы

- **Книга**: "xUnit Test Patterns: Refactoring Test Code", Gerard Meszaros (Addison-Wesley, 2007).
- **Книга**: "Growing Object-Oriented Software, Guided by Tests", Freeman & Pryce (Addison-Wesley, 2009) — London school TDD.
- **Статья**: Martin Fowler — "The Practical Test Pyramid" — https://martinfowler.com/articles/practical-test-pyramid.html
- **Статья**: Martin Fowler — "On the Diverse And Fantastical Shapes of Testing" — https://martinfowler.com/articles/2021-test-shapes.html
- **Дока**: JUnit 5 User Guide — https://junit.org/junit5/docs/current/user-guide/
- **Дока**: PITest mutation testing — https://pitest.org/
- **Дока**: Pact contract testing — https://pact.io/

### Практика / Кейсы

1. **Test pyramid audit для Spring Boot микросервиса**. Шаги: collect — 5 unit tests, 80 `@SpringBootTest`, 3 Selenium E2E. Build time 25 min, flakiness 15%. Diagnosis: ice-cream cone. Plan: (1) extract unit tests for domain logic — pure Java, no Spring; (2) replace broad `@SpringBootTest` with `@WebMvcTest` (controller layer, mocks), `@DataJpaTest` (DB layer with Testcontainers); (3) integration tests only for cross-layer flows; (4) replace Selenium with Playwright for 1 critical path; (5) add Contract tests (Pact) for downstream services. After 3 months: 200 unit, 50 integration, 5 E2E, build 8 min, flakiness 2%.

2. **Mutation testing с PITest для critical `Money` class**. Шаги: add Maven plugin `pitest-maven` с `targetClasses=...Money*`. Run: 50 mutants generated (change `+` to `-`, remove null checks, change `>` to `>=`). 35 killed (70% score). Investigate survivors: `multiply` — mutation: change scale check `>` to `>=`. Test missed this edge case. Add test: `money.multiply(0).scale() == original.scale()`. Re-run: 95% score. Conclusion: line coverage 100% but mutation 70% initial — tests weren't catching real bugs. Add to CI: nightly mutation run on core domain.

3. **Contract test для OrderService ↔ InventoryService**. Шаги: consumer (OrderService) defines Pact: `given inventory exists for SKU=X, expect response {available: 5, price: 10.00}`. Pact file published to Pact Broker. Provider (InventoryService) CI: `@PactTest` verifies — given X, response matches. Breaking change in InventoryService API (e.g., remove `price` field) — fails provider CI before deploy. Pact Broker: can-i-deploy check — `pact-broker can-i-deploy --pacticipant OrderService --version <git-sha>`. Result: cross-service changes caught in CI, no 3am production breakage.

4. **Property-based test для `reverse` function**. Шаги: jqwik:
   ```java
   @Property
   void reverseReverseIsOriginal(@ForAll List<Integer> list) {
     assertEquals(list, reverse(reverse(list)));
   }
   @Property
   void reversePreservesSize(@ForAll List<Integer> list) {
     assertEquals(list.size(), reverse(list).size());
   }
   @Property
   void reverseOfEmptyIsEmpty(@ForAll List<Integer> list) {
     if (list.isEmpty()) assertTrue(reverse(list).isEmpty());
   }
   ```
   jqwik generates 1000 random lists, including edge cases (nulls, duplicates, large). Found bug: original `reverse` had `IndexOutOfBoundsException` for size 1 list. Fixed. Урок: property-based catches edge cases missed by examples.

5. **Test strategy для saga orchestration (OrderService)**. Шаги: (1) Unit tests — `OrderSaga` state transitions, pure logic; (2) Integration — saga state persisted, recovery on restart (Temporal.io handles); (3) Compensation tests — manually trigger compensation, verify all steps run in reverse; (4) Idempotency tests — replay event N times, state unchanged after first; (5) Timeout tests — slow downstream, verify saga retries then times out; (6) Concurrent saga tests — multiple sagas in parallel, no interference; (7) E2E — full flow through real Kafka + DB. Mutation: inject failures, verify saga recovers.

6. **Test flakiness — fix `LocalDate.now()` test**. Шаги: test fails randomly — `assertThat(calculateExpiry()).isEqualTo(LocalDate.now().plusDays(30))`. Race: test runs near midnight, `now()` called twice (test + prod code), different dates. Fix: inject `Clock` bean — `Clock clock; calculateExpiry() { return LocalDate.now(clock).plusDays(30); }` → test: `Clock fixed = Clock.fixed(Instant.parse("2024-06-01T10:00:00Z"), ZoneOffset.UTC); service.setClock(fixed);` → assertion deterministic. General principle: external dependencies (time, random, network) — injectable.

7. **Performance regression test с JMH в CI**. Шаги: benchmark module with `@Benchmark` methods for hot paths (parse, serialize, sort). CI: nightly run on dedicated runner, compare to baseline (last green build). Threshold: > 10% regression fails build. Allocations benchmark: `-prof gc` for allocation rate. False positives: noisy environment → use `<forks>3</forks>` and `<warmups>5</warmups>`, geometric mean of last 3 runs. Trend dashboard: Grafana shows perf over time. Catch: a "harmless" PR adding `synchronized` to hot path regressed throughput 30% — caught by JMH before deploy.

---

## 14. Документация и коммуникация (RFC, ADR, technical specs, architecture diagrams C4/PlantUML/Mermaid, Sequence diagrams, decision records)

### Теория

- **RFC (Request for Comments)** — design document proposing change. Origin: IETF RFCs. Modern: Rust RFCs, React RFCs, Amazon's "narrative". Structure: motivation, goals/non-goals, detailed design, alternatives considered, rollout plan, open questions. Reviewed by wider team before implementation.
- **ADR (Architecture Decision Record)** — short doc capturing one architectural decision. Michael Nygard template: Title, Status (proposed/accepted/deprecated/superseded), Context, Decision, Consequences. Lives in repo (`docs/adr/`). Lightweight, append-only (new ADR supersedes old). Tooling: adr-tools, log4brains.
- **Technical spec** — detailed plan for feature: problem statement, goals, design (API, data model, algorithms), test plan, rollout, observability, security, cost. Length: 5-50 pages depending on scope. Reviewed before implementation.
- **Narrative (Amazon)** — 6-page memo read in silence at start of meeting. Forces clear thinking, full sentences, no bullet point shorthand. Replaces PowerPoint.
- **C4 model (Simon Brown)** — Context, Container, Component, Code. Zoom levels: Context (system + external actors), Container (deployable units: API, DB, SPA), Component (classes/modules in container), Code (optional, rarely used). Notation: simple boxes, no UML ceremony.
- **PlantUML** — text-to-diagram (DSL). Generates UML (sequence, class, component, deployment), non-UML (mindmap, Gantt). Integration with IDE (IntelliJ plugin), AsciiDoc, Markdown. Source-controlled (text). Disadvantage: server-side rendering.
- **Mermaid** — similar to PlantUML, JS-based, renders in GitHub, GitLab, Notion natively. Less expressive than PlantUML, but better DX for quick diagrams. Supports flowchart, sequence, class, state, ER, Gantt.
- **UML diagrams** — class, sequence, state, activity, component, deployment, use case, object. Modern usage: simplified (no exhaustive UML). Most useful: sequence, class (simplified), state, deployment.
- **Sequence diagrams** — show interaction between objects over time. Lifelines, messages (sync/async), activations, alt/opt/loop fragments. Tools: PlantUML, Mermaid, WebSequenceDiagrams. Use for: API flows, protocol, debugging.
- **Decision log vs ADR** — ADR is structured (one decision per file). Decision log is chronological (running list). ADRs scale better (each decision searchable, has stable ID).
- **Documentation types** (Diátaxis framework): tutorials (learning), how-to guides (problem-solving), reference (info), explanation (understanding). Quadrant: tutorial (novice, learning) ↔ how-to (novice, doing) ↔ reference (expert, doing) ↔ explanation (expert, learning).
- **README.md anatomy** — title, one-paragraph what/why, install, quickstart, usage examples, configuration, development (build/test), contribution guide, license, badges. Keep short — link to docs/ for detail.
- **API documentation** — OpenAPI (Swagger) for REST; gRPC proto + comments → Buf; GraphQL schema + descriptions. Auto-generated clients. SDK docs: Javadoc (Java), godoc (Go). Try-it-out: Swagger UI, Postman.
- **Code comments** — Javadoc for public API; inline for "why" (algorithm choice, workaround, business rule); avoid "what" (code already says). `TODO: name YYYY-MM-DD` for tech debt. `FIXME` for known bugs.
- **Runbook / playbook** — operational doc for on-call: what to monitor, what alerts mean, how to mitigate common incidents, escalation contacts. Lives in wiki (Confluence) + PagerDuty integration. Updated after each incident.
- **Architecture diagrams principles** — show structure (boxes + lines), label arrows with protocol/data, group by boundary (DDD BC), color = layer/type (consistent legend). Avoid: "spaghetti" (too many lines), "eye candy" (3D, gradients), outdated (diagrams age — keep in source control, generate from code where possible).
- **Living documentation** — diagrams generated from code: Structurizr (DSL → C4), PlantUML from Spring beans, schema diagrams from JPA entities. Keeps docs in sync. Tradeoff: limited detail vs accuracy.
- **Communication patterns** — async (RFC, PR comments, Slack) for deep thought, broad audience; sync (meetings, calls) for high-bandwidth, quick decisions. Default to async, escalate to sync only when needed.
- **Stakeholder communication** — engineers: technical RFC; PM: 1-page summary + impact; leadership: 3-slide (problem, options, recommendation); customer: release notes. Same decision, different framing.

### Материалы

- **Книга**: "Docs for Developers", Bhatti/Cox/Lambert/Wright/Narwojski (O'Reilly, 2022).
- **Дока**: C4 model — https://c4model.com
- **Дока**: PlantUML — https://plantuml.com
- **Дока**: Mermaid — https://mermaid.js.org
- **Статья**: Michael Nygard — "Documenting Architecture Decisions" — https://cognitect.com/blog/2011/11/15/documenting-architecture-decisions
- **Статья**: "C4 Model Architecture and ADR Integration" — https://visual-c4.com/blog/c4-model-architecture-adr-integration
- **Дока**: Diátaxis framework — https://diataxis.fr

### Практика / Кейсы

1. **Write an RFC for migrating from RabbitMQ to Kafka**. Шаги: (1) Motivation: throughput limits, replayability, schema evolution; (2) Goals: 10× throughput, exactly-once for payments; non-goals: replace REST; (3) Detailed design: topic structure, partitioning by `aggregate_id`, schema registry (Avro), consumer group design, outbox pattern for producers; (4) Alternatives: stay with RabbitMQ + sharding, switch to Pulsar; (5) Migration: strangler — new events to Kafka, mirror old; (6) Rollout: team-by-team migration, 6 months; (7) Risks: ops complexity, learning curve; (8) Open questions: schema evolution policy. Review with arch + SRE. Decision: 1-week comment period, then ADR.

2. **ADR for choosing PostgreSQL over MongoDB for new service**. Шаги: file `docs/adr/0012-postgres-for-order-service.md`:
   ```
   # ADR-0012: Use PostgreSQL for Order Service
   ## Status
   Accepted (2024-06-15)
   ## Context
   Order service needs: ACID transactions (payment + inventory), relational integrity, JSON for flexible metadata.
   Considered: PostgreSQL, MongoDB, DynamoDB.
   ## Decision
   PostgreSQL. Rationale: ACID + relational integrity critical for orders; team expertise; ops has Postgres runbooks.
   ## Consequences
   + Transactions, foreign keys, mature ecosystem.
   - Schema migrations required (Flyway); horizontal sharding harder than MongoDB.
   Need: PgBouncer for connection pooling, Citus if sharding needed later.
   ```
   Committed to repo, linked from PRs touching the decision.

3. **C4 model diagram for payment service**. Шаги: Level 1 Context — PaymentService box, arrows from Merchant (external), to Stripe/Adyen (external), to Bank. Level 2 Container — boxes: PaymentAPI (Spring Boot), PaymentWorker (Async), PostgresDB, Kafka, Redis. Arrows: HTTPS, JDBC, pub-sub. Level 3 Component (for PaymentAPI) — Controllers, Services, Repositories, with dependencies. Generate via Structurizr DSL:
   ```
   workspace {
     model {
       user = person "Merchant"
       ps = softwareSystem "Payment Service" {
         api = container "Payment API" "Spring Boot"
         db = container "PostgreSQL" "Database"
       }
       stripe = softwareSystem "Stripe"
       user -> api "Submits payment"
       api -> db "JDBC"
       api -> stripe "HTTPS"
     }
     views { systemContext ps "Context" include * }
   }
   ```
   Render PNG, commit to repo, link from README.

4. **Sequence diagram for order checkout flow**. Шаги: PlantUML:
   ```
   @startuml
   actor Customer
   participant "Web" as W
   participant "OrderService" as OS
   participant "PaymentService" as PS
   participant "Kafka" as K
   database "Postgres" as DB
   Customer -> W: Click "Checkout"
   W -> OS: POST /orders
   OS -> DB: INSERT order (PENDING)
   OS -> PS: POST /payments (async via Kafka)
   OS -> W: 202 Accepted (order_id)
   ... payment processed ...
   PS -> K: PaymentSucceeded
   OS -> K: consume PaymentSucceeded
   OS -> DB: UPDATE order SET status=CONFIRMED
   OS -> W: WebSocket notify
   W -> Customer: "Order confirmed!"
   @enduml
   ```
   Use for: onboarding new devs, debugging (annotate with timing), API doc.

5. **README.md для open-source library**. Шаги: title + tagline («Idempotency for Spring Boot»), badges (Maven Central, CI, coverage, license), 30-second quickstart (3-line code example), install (Maven/Gradle snippets), features (bullets with mini examples), configuration table, advanced usage (links to docs), migration guide, contributing (link to CONTRIBUTING.md), code of conduct, license. Total: 200 lines max. Link to `docs/` for deep dive. Test the README: ask 3 new users to follow quickstart, iterate.

6. **Runbook for on-call: Kafka consumer lag alert**. Шаги: alert name + severity; what it means (consumer can't keep up); impact (events delayed, business consequences); diagnosis steps: Grafana — which consumer? which topic? lag trend (rising/sustained/spike); mitigation: scale consumer group (kubectl scale), check for poison message (DLQ), restart stuck consumer; escalation: contact Kafka on-call (PagerDuty schedule); post-incident: update runbook with new findings, create Jira for root cause. Format: structured, scannable, action-oriented. Test: new on-call engineer follows runbook during drill.

7. **Diátaxis restructure of API docs site**. Шаги: audit current docs — 100 pages mixed. Reorganize: Tutorials (getting started in 5 min, first API call), How-to (authenticate, paginate, retry, webhooks), Reference (full endpoint list, error codes, types), Explanation (rate limiting philosophy, idempotency model). Each page clearly tagged. Navigation reflects structure. Result: new users find tutorial; experienced devs find reference; PM finds how-to. Reduces support tickets by 30%.

---

## 15. Senior soft skills (менторство, техническое лидерство, stakeholder management, переговоры, конфликты, delegation, growing team)

### Теория

- **Senior vs Junior difference** — Junior: solves given problem; Middle: defines problem; Senior: chooses which problems to solve; Staff: aligns team on problem set; Principal: shapes what org works on. Increasing scope of impact.
- **Mentorship** — long-term (months-years), career-focused. Mentee drives agenda, mentor asks questions, shares experience, opens doors. Not: do work for mentee, dictate path. Goal: build mentee's capacity, not dependency.
- **Sponsorship vs mentorship** — mentor gives advice; sponsor advocates for you in rooms you're not in. Senior+ should sponsor high-potential juniors: nominate for high-visibility projects, recommend for promotion, introduce to networks.
- **Technical leadership** — set direction (vision, strategy), raise bar (code review, standards), enable others (tools, docs, removal of blockers), communicate (upward to leadership, downward to team, outward to other teams). Not: boss people around, do all the work, take credit.
- **Influence without authority** — Staff+ engineers don't have direct reports but need to influence. Tools: data (benchmarks, metrics), expertise (deep knowledge), relationships (trust built over time), persuasion (clear writing, speaking), alliances (coalitions with other seniors). Book: "Influence Without Authority", Cohen & Bradford.
- **Stakeholder management** — identify stakeholders (who cares? who has power?), map their interests/concerns, communicate appropriately (depth, frequency, format), involve early (avoid surprises), manage expectations (realistic commitments). Power-interest grid (Mendelow).
- **Negotiation** — principled (Fisher & Ury "Getting to Yes"): separate people from problem, focus on interests not positions, invent options for mutual gain, use objective criteria. BATNA (Best Alternative To Negotiated Agreement). Avoid: hard/soft bargaining, anchoring on first number.
- **Conflict resolution** — Thomas-Kilmann styles: Compete (assertive, uncooperative), Collaborate (assertive, cooperative), Compromise (middle), Avoid (unassertive, uncooperative), Accommodate (unassertive, cooperative). Default for senior: Collaborate; use Compromise when time-pressed; Avoid rarely (only for trivial).
- **Conflict de-escalation** — listen first (don't interrupt), acknowledge emotion («I see this is frustrating»), focus on issue not person, seek common ground, take break if heated, escalate to manager if pattern. Non-Violent Communication (Rosenberg): Observe → Feel → Need → Request.
- **Delegation** — hand off task + authority + responsibility. Steps: define outcome (not method), match to person's level, give context (why it matters), agree on milestones, check-in (not micromanage), review work, give feedback. Mistakes: dump without context, micromanage, take back when stumble.
- **Delegation levels (Meyer)** — TELL (I decide, you do) → SELL (I decide, explain why) → CONSULT (I decide after your input) → AGREE (we decide together) → ADVISE (you decide, I offer input) → INQUIRE (you decide, I ask questions) → DELEGATE (you decide, I trust). Match to person + task maturity.
- **Situational Leadership (Hersey-Blanchard)** — match style to follower's readiness: D1 (enthusiastic beginner) → Directing; D2 (disillusioned learner) → Coaching; D3 (capable but cautious) → Supporting; D4 (self-reliant achiever) → Delegating. Wrong style → frustration or stagnation.
- **Growing team** — hire well (raise bar with each hire), onboard systematically (30-60-90 plan), mentor and sponsor, give stretch projects, celebrate wins, address underperformance quickly (don't let it fester). Team's success > individual brilliance.
- **Difficult conversations** — prepare (write key points), open with shared purpose, describe behavior (not personality), share impact, ask for their perspective, agree on next steps. Book: "Difficult Conversations", Stone/Patton/Heen.
- **Feedback giving** — SBI (Situation-Behavior-Impact): «In yesterday's standup (S), you interrupted Sarah twice (B), which made it hard for her to share her status (I)». Specific, observable, recent. Not: «You're rude».
- **Feedback receiving** — assume good intent, listen without defending, ask for specific examples, say thank you (not agreement), decide what to act on, follow up. Hard but essential skill.
- **1:1 meetings** — recurring (weekly), 30 min, mentee-driven agenda (not status update), discuss growth/blockers/career, take notes (private), follow up on action items. Skip-level 1:1s quarterly.
- **Tech lead role** — half IC (own work), half lead (review, design, unblock). 30% lead → 70% lead as team grows. Tech lead != manager (no HR responsibilities, but people-aware). Book: "The Manager's Path", Camille Fournier.
- **Manager vs IC track** — both valid, equal prestige. Switching possible (often IC→Mgr is easier than Mgr→IC). Companies should support both ladders to senior levels (Staff, Principal, Distinguished).
- **Burnout prevention** — workload management (say no), recovery (vacation, weekends), meaning (connect to impact), control (autonomy), support (talk to manager/peers). Senior+ set example (don't heroics, don't 80h weeks normalized).

### Материалы

- **Книга**: "The Manager's Path: A Guide for Tech Leaders Navigating Growth and Change", Camille Fournier (O'Reilly, 2017).
- **Книга**: "Staff Engineer: Leadership beyond the management track", Will Larson (leo.com, 2021) — бесплатный онлайн: https://staffeng.com/book
- **Книга**: "The Staff Engineer's Path", Tanya Reilly (O'Reilly, 2022).
- **Книга**: "Influence Without Authority", Allan Cohen & David Bradford (Wiley, 2nd ed. 2005).
- **Книга**: "Difficult Conversations: How to Discuss What Matters Most", Stone/Patton/Heen (Penguin, 2010).
- **Статья**: "The Staff Engineer's Path [Book]" — https://www.oreilly.com/library/view/the-staff-engineers/9781098118723
- **Статья**: Pragmatic Engineer — "The Staff Engineer's Path: You're a Role Model Now" — https://newsletter.pragmaticengineer.com/p/the-staff-engineers-path

### Практика / Кейсы

1. **Mentorship program для junior dev (6-month plan)**. Шаги: month 1 — establish rapport, understand mentee's goals (tech stack, soft skills, career level), set 6-month SMART goals. Month 2-3 — pair on real task 1h/week, review PRs together, recommend resources. Month 4 — give stretch task (lead small feature), review approach, give feedback. Month 5 — discuss progress, identify gaps, plan next stretch. Month 6 — review against goals, plan next 6 months or hand off to another mentor for different perspective. Track in shared doc. Mistakes to avoid: doing work for mentee, judging, missing 1:1s.

2. **Resolve conflict between two seniors on architecture (microservices vs modular monolith)**. Шаги: 1:1 with each — understand positions and underlying interests (A: «microservices scale, polyglot»; B: «monolith simpler, ops cost»). Joint meeting: restate shared goal (deliver feature X reliably under budget). Brainstorm options: (a) microservices, (b) monolith, (c) modular monolith now, extract microservice when needed. Evaluate each against criteria: cost, complexity, scalability, team size. Decision: (c) — written as ADR. Both commit to decision (disagree and commit). Lesson: focus on interests not positions, use data not opinions.

3. **Influence another team to adopt your library**. Шаги: identify their pain points (read their postmortems, talk to their tech lead). Show how library solves their problem — concrete demo on their codebase. Address concerns: support, migration cost, learning curve. Offer: pair on migration, write integration docs, provide Slack support. Pilot with one team first, gather metrics (before/after). Present results at engineering all-hands. Don't: mandate, shame, or be pushy. Principle: «people support what they help create».

4. **Delegation: hand off critical payment service ownership**. Шаги: identify successor (mid-level, ambitious). Level 1 (TELL): walk through architecture, runbook, on-call expectations. Level 2 (SELL): explain why decisions made, history. Level 3 (CONSULT): for new features, get their input, decide together. Level 4 (AGREE): for operational issues, they propose, you align. Level 5 (ADVISE): for routine work, they decide, you answer questions. Level 7 (DELEGATE): they own it, you trust. Timeline: 3 months to Level 5, 6 months to full ownership. Your time freed for new initiative. Don't: take back when they stumble (coach instead).

5. **Difficult conversation: underperforming team member**. Шаги: prepare — gather specific examples (missed deadlines, bugs), check with peers. Meeting: open with shared purpose («we want you to succeed»). Describe behavior: «In last 3 sprints, 4 of 7 stories missed estimates by >50%». Share impact: «caused release delay, team had to pick up slack». Ask for their perspective: «what's getting in the way?». Listen — maybe personal issue, skill gap, unclear expectations. Agree on plan: training, pair programming, clearer estimates, weekly check-in. Document. Follow up in 2 weeks. If no improvement in 30-60 days — escalate to manager with PIP.

6. **Stakeholder management for cross-cutting refactor (touches 5 teams)**. Шаги: identify stakeholders — team leads (interest: roadmap impact), PMs (interest: feature delivery), platform team (interest: shared infra), security (interest: vulnerabilities fixed), leadership (interest: ROI). Map: power-interest grid. Communicate: weekly written update (5 bullet points), bi-weekly sync with team leads, monthly demo to leadership. Address concerns early — adapt plan based on feedback. Track commitments in shared doc. Success metric: 0 incidents, 20% perf improvement, all teams onboarded.

7. **Sponsorship: advocate for junior in promotion committee**. Шаги: identify high-potential — exceeded expectations, has growth trajectory, level-appropriate impact. Document their work: specific projects, impact metrics, peer feedback. In promotion committee: present evidence, answer questions, push back if needed. After: tell them what was discussed, what to work on next. Continue sponsoring — introduce to other seniors, nominate for high-visibility projects, write recommendation on LinkedIn. Track: how many people you've sponsored and their outcomes.

---

## 16. Agile/Lean/DevOps культуры (Scrum/Kanban/XP, estimation, retrospectives, WIP limits, lead time, cycle time, flow efficiency)

### Теория

- **Agile Manifesto (2001)** — individuals and interactions over processes/tools; working software over comprehensive documentation; customer collaboration over contract negotiation; responding to change over following a plan. Values, not prescription.
- **Scrum** — framework: roles (Product Owner, Scrum Master, Developers), events (Sprint, Sprint Planning, Daily Scrum, Sprint Review, Sprint Retrospective), artifacts (Product Backlog, Sprint Backlog, Increment). Sprints 2-4 weeks. Sprint goal drives focus.
- **Kanban** — visualize workflow (board), limit WIP (work in progress), manage flow, make policies explicit, implement feedback loops, improve collaboratively. Continuous flow, no iterations. Pull system (vs Scrum push).
- **Extreme Programming (XP)** — engineering practices: TDD, pair programming, continuous integration, refactoring, simple design, collective code ownership, small releases, sustainable pace, customer on-site, planning game. Kent Beck, 1999.
- **Lean Software Development** — Poppendieck: eliminate waste, amplify learning, decide as late as possible, deliver as fast as possible, empower the team, build integrity in, see the whole. Origin: Toyota Production System.
- **DevOps culture** — break dev/ops silo. CALMS: Culture, Automation, Lean, Measurement, Sharing. SRE (Site Reliability Engineering) — Google's approach: engineers run what they build. Blameless postmortems.
- **Estimation** — Story Points (relative, Fibonacci: 1,2,3,5,8,13), Ideal Days, T-shirt sizes. Planning Poker (consensus). Velocity = points completed per sprint (rolling avg). Anti-pattern: estimate in hours, equate points to hours, gamify velocity.
- **#NoEstimates movement** — decompose to small similarly-sized stories, count throughput, predict by Monte Carlo simulation. Forecasts based on history, not estimates.
- **Lead time vs Cycle time** (Lean): Lead time = customer request → delivery (full wait + work). Cycle time = work start → work done. Lead ≥ Cycle. Reducing lead = reduce wait queue.
- **Flow efficiency** = work time / lead time. Typical 5-15% (most time is waiting). Improving = reduce handoffs, batch size, queues.
- **WIP (Work In Progress) limits** — Kanban: max items per column. Limits context switching, exposes bottlenecks. Less WIP → faster flow (Little's Law).
- **Little's Law** — `L = λ × W` (avg items in system = arrival rate × avg lead time). Implication: reduce WIP to reduce lead time. Foundation of queueing theory.
- **Retrospective** — Scrum event: what went well, what didn't, what to improve. Formats: Start/Stop/Continue, 4Ls (Liked/Learned/Lacked/Longed for), Sailboat, Mad/Sad/Glad. Action items with owners, follow up next retro.
- **Sprint Planning** — PO presents backlog, team forecasts capacity, picks stories, breaks into tasks. Sprint goal articulated. 2-week sprint: 4h max.
- **Daily Scrum** — 15 min, developers sync: yesterday, today, blockers. Not status report to SM. Optional: walk the board (right-to-left).
- **Backlog refinement (grooming)** — ongoing: PO adds detail, estimates, priority to backlog items. 5-10% of sprint capacity.
- **Definition of Done (DoD)** — checklist: code reviewed, tests pass, deployed to staging, docs updated, no critical bugs. Team-owned. Without DoD, "done" is subjective.
- **Definition of Ready (DoR)** — criteria for a story to be picked: clear acceptance criteria, estimated, dependencies resolved.
- **Burn-down / burn-up charts** — burn-down: remaining work over time. Burn-up: completed work + total scope (shows scope changes). Better: cumulative flow diagram (CFD) for Kanban.
- **Cumulative Flow Diagram (CFD)** — stacked area chart of items in each state over time. Bands width = WIP per state. Parallel bands = stable flow. Widening = bottleneck.
- **Cadence** — Scrum: fixed sprint cadence. Kanban: continuous flow with regular reviews/demos. Mix: «Scrumban» — Scrum ceremonies + Kanban flow.
- **Spikes** — timeboxed research/exploration. Outcome: knowledge, not production code. Used before estimating complex stories.
- **Continuous improvement (Kaizen)** — small, incremental changes from front-line workers. Vs Kaikaku (radical reform). Retro action items = Kaizen.
- **Theory of Constraints (Goldratt)** — every system has one bottleneck; improving non-bottleneck is illusion. Identify → exploit → subordinate → elevate → repeat.

### Материалы

- **Книга**: "Scrum: The Art of Doing Twice the Work in Half the Time", Jeff Sutherland (Crown, 2014).
- **Книга**: "Kanban: Successful Evolutionary Change for Your Technology Business", David J. Anderson (Blue Hole Press, 2010).
- **Книга**: "Extreme Programming Explained" 2nd ed., Kent Beck (Addison-Wesley, 2004).
- **Книга**: "The DevOps Handbook" 2nd ed., Kim/Humble/Debois/Rockwood/Willis (IT Revolution, 2021).
- **Книга**: "Accelerate", Forsgren/Humble/Kim (IT Revolution, 2018) — DORA metrics.
- **Дока**: Scrum Guide — https://scrumguides.org/scrum-guide.html
- **Дока**: Kanban Guides — https://www.kanban.guide

### Практика / Кейсы

1. **Measure DORA metrics for team**. Шаги: collect — Deployment Frequency (deploys per day/week from CI/CD), Lead Time for Changes (commit → deploy, from git history), Change Failure Rate (failed deploys / total, from incidents), Time to Restore (incident detected → resolved, from PagerDuty). Elite: deploy multiple/day, lead < 1h, change failure < 15%, restore < 1h. Track monthly. Identify bottleneck: lead time high → PR review slow. Action: pair review, smaller PRs. Track over 6 months → trend. Beware: gaming metrics (deploys of trivial changes) — pair with quality metrics.

2. **Kanban implementation for ops-heavy team**. Шаги: visualize workflow — columns: Incoming, Triage, In Progress, Blocked, Review, Done. Set WIP limits: 3 per engineer in Progress, 5 in Review. Daily 10-min standup: walk board right-to-left, focus on items near Done + blockers. Bi-weekly replenishment: pull from intake based on capacity. CFD: track weekly, watch for widening bands. Action: when In Progress grows — swarm to finish, don't start new. Result: cycle time from 14 days → 5 days in 3 months.

3. **Retrospective with "5 Whys" for incident**. Шаги: incident — prod DB down 30 min. Fact-gathering (no blame). 5 Whys: (1) Why did DB go down? Connection pool exhausted. (2) Why? New endpoint had N+1 query, opened 100 connections per request. (3) Why? Code review missed it. (4) Why? Reviewer was new, no checklist for query patterns. (5) Why? Onboarding didn't include perf review. Root cause: onboarding gap. Actions: (1) add perf checklist to PR template, (2) update onboarding, (3) add JMeter test for new endpoint, (4) connection pool alert at 80%. Owners + due dates. Follow up next retro.

4. **Estimate complex feature (4-month scope) without upfront estimation**. Шаги: break feature into small stories (1-3 days each). Result: 60 stories. Run 1 sprint (2 weeks) — completed 8 stories. Velocity = 8 stories/sprint. Forecast: 60/8 = 7.5 sprints = 15 weeks. Use Monte Carlo (1000 simulations) for probabilistic forecast: 50% chance by week 14, 85% by week 17, 95% by week 20. Present to PM as ranges, not dates. Re-forecast each sprint with new data. Advantage: more honest than single-point estimate.

5. **WIP limit conflict: team over-commits**. Симптом: 15 stories in progress, 8 engineers. Standup: status updates, no flow. Action: reduce WIP to 8 (1 per engineer). Initial pushback: «we're more productive parallel». Reality: nothing finishes, context switching. First 2 weeks: throughput drops (existing items blocking new). Week 3: items start finishing. Week 4: cycle time drops 50%. Lesson: WIP limits feel slower but finish faster.

6. **Hybrid Scrumban for product team with ops support**. Шаги: keep Scrum ceremonies (planning, review, retro) for product focus. Use Kanban board with WIP limits within sprint. Sprint goal: «ship feature X, support ops < 20% capacity». Ops interrupts go to «Intake» column with capacity allocation (e.g., 30% of team). If ops exceeds capacity — renegotiate sprint goal. Demos: ship increment end of sprint, but stories can flow continuously. Result: balance between product focus and ops reality.

7. **Scale agile across 8 teams (LeSS vs SAFe)**. Шаги: LeSS (Large-Scale Scrum) — minimal framework, 1 PO for all teams, shared backlog, sprint sync. SAFe — heavier, Agile Release Trains, Program Increment planning every 8-12 weeks, hierarchy. Choose: small org, alignment-focused → LeSS; large enterprise, regulatory → SAFe. Implementation: start with 2-3 teams pilot, learn, expand. Critical: shared definition of done, integration every sprint, communities of practice for cross-team learning. Failure mode: process without principles (ceremonies as theater).

---

## 17. Performance engineering (latency vs throughput, Amdahl/Gustafson, Little's Law, queueing theory M/M/1, profiler-driven optimization, premature optimization)

### Теория

- **Latency vs Throughput** — Latency: time per request (ms, µs). Throughput: requests per second (RPS). Often trade-off: batching increases throughput but worsens per-item latency. Conversation: «this service has 10ms p99 latency at 1000 RPS» — both metrics needed.
- **Latency percentiles** — p50 (median), p90, p99, p99.9. Average misleading (long tail). SLO: «99% of requests < 100ms». Tail latency (p99.9) dominated by GC, lock contention, slow downstream. Coordinated omission — measurement bias from slow requests delaying others.
- **Throughput saturation curve** — as load increases, throughput rises linearly, then plateaus (CPU/network/DB limit), then degrades (queueing). Optimal operating point = 70-80% of saturation (headroom for spikes).
- **Amdahl's Law** — speedup from parallelization limited by serial fraction: `S = 1 / ((1-p) + p/N)` where p = parallel fraction, N = processors. If 10% serial, max speedup = 10× even with infinite cores. Implication: find and reduce serial bottlenecks.
- **Gustafson's Law** — counter to Amdahl: with more cores, problem size grows (scaled speedup). `S = N - α(N-1)` where α = serial fraction. Implication: for problems where you can do more with more cores (3D rendering, simulation), parallelism pays off.
- **Amdahl's Law for Tail Latency** (CACM 2018) — if 1% of requests are slow (e.g., 1s) and rest are fast (10ms), p99 = 1s. To reduce p99, must reduce slowest 1%. Adding resources to fast requests doesn't help.
- **Little's Law** — `L = λ × W` (L = avg items in system, λ = arrival rate, W = avg wait). Universally true for stable systems. For M/M/1 queue: `W = 1 / (μ - λ)` where μ = service rate. As λ → μ, W → ∞.
- **M/M/1 queue** — Markovian arrivals, Markovian service, 1 server. Utilization `ρ = λ/μ`. Avg wait in queue `Wq = ρ / (μ(1-ρ))`. At ρ=0.5: Wq = 1/μ (one service time). At ρ=0.9: Wq = 9/μ (9× service time). At ρ=0.99: 99×. Implication: stay below 70% utilization for low latency.
- **Universal Scalability Law (USL, Gunther)** — `N(x) = λN / (1 + α(N-1) + βN(N-1))` where α = contention, β = coherency. Models both contention (linear) and coherency (quadratic) costs. Used to predict scalability from measurements.
- **Premature optimization (Knuth)** — «Premature optimization is the root of all evil». Don't optimize without measurement. But: don't write pessimization either (O(n²) when O(n) obvious). Optimize hot paths first (profile-driven).
- **Profiler-driven optimization** — measure (profile), identify hot spot, optimize, measure again. Tools: JFR (Java Flight Recorder), async-profiler (flame graphs), JMH (microbenchmarks). Never guess: profile shows real bottleneck.
- **Java-specific perf** — JIT (C1/C2 compilation), GC pauses (G1/ZGC), allocation rate (TLAB), false sharing, branch prediction, cache locality. Heap dump analysis (MAT). `jcmd` for runtime inspection.
- **GC tuning** — choice: G1 (default Java 9+, balanced), ZGC (low pause <10ms, Java 15+), Shenandoah (low pause). GC pause = stop-the-world or concurrent. Tune: heap size (`-Xmx`, `MaxRAMPercentage`), generation sizes, pause target (`MaxGCPauseMillis`).
- **Allocation rate** — measure with JFR: alloc rate per thread, hot allocation sites. Reduce: object pooling (controversial), primitive collections (Eclipse Collections),`-XX:+UseStringDeduplication` (G1), avoid autoboxing (`Integer` vs `int`), immutable copies.
- **Cache locality** — sequential access (arrays) is 10-100× faster than random (linked list, HashMap with collisions). CPU cache: L1 1ns, L2 4ns, L3 12ns, RAM 100ns. Pre-fetcher exploits spatial locality.
- **Branch prediction** — predictable branches (e.g., `if (x > 0)` for mostly positive x) cheap, unpredictable branches cost ~20 cycles. `@BranchProbability` JMH hint. Sorting data improves branch prediction (but adds sort cost).
- **False sharing** — multiple threads writing fields in same cache line (64 bytes) → cache invalidation traffic. Mitigation: `@Contended` (Java 8+), padding fields. Relevant for high-frequency counters.
- **Network perf** — TCP handshake (3-way), TLS handshake (extra 1 RTT, TLS 1.3 0-RTT), HTTP/2 multiplexing (one connection), HTTP/3 (QUIC, UDP-based, no head-of-line blocking). gRPC over HTTP/2: lower overhead than REST/JSON.
- **Database perf** — query plan (EXPLAIN ANALYZE), index usage, N+1 problem, connection pool sizing (HikariCP formula), batch inserts, prepared statements. Slow query log. pg_stat_statements.
- **Benchmarking methodology** — JMH: warmup (5 iterations), measurement (5-20), forks (3+ for isolation), `@State` for shared state. Avoid: dead code elimination (use Blackhole), constant folding. Measure: throughput (ops/sec), average time, sampling (for percentiles).
- **Coordinated omission** — benchmark tool sends request when previous completes; if one slow, all subsequent appear fast (skipped). Fix: send at fixed rate (not dependent on response), measure all (including queued). Tools: wrk2, vegeta. JMH: `-bm sample` with `Timeout`.

### Материалы

- **Книга**: "Java Performance" 2nd ed., Scott Oaks (O'Reilly, 2020).
- **Книга**: "Systems Performance" 2nd ed., Brendan Gregg (Addison-Wesley, 2020).
- **Статья**: "Latency Numbers Every Programmer Should Know" — https://gist.github.com/jboner/2841832
- **Статья**: "Amdahl's Law for Tail Latency" (CACM) — https://cacm.acm.org/research/amdahls-law-for-tail-latency
- **PDF**: "Amdahl's Law for Tail Latency" MIT — https://people.csail.mit.edu/delimitrou/papers/2018.cacm.amdahlsTail.pdf
- **PDF**: "Queueing Theory I" — https://www.eng.ucy.ac.cy/christos/Courses/ECE466/Lectures/QueueingTheory%20I.pdf
- **Сайт**: Brendan Gregg — Linux Performance — http://www.brendangregg.com/linuxperf.html

### Практика / Кейсы

1. **Diagnose p99 latency spike (50ms → 500ms)**. Шаги: (1) SLO dashboard shows p99 spike 14:00; (2) check correlations: GC log — Full GC 13:59-14:00 (200ms pause); (3) why Full GC? Heap dump — old gen filled with byte[] from large report generation; (4) root cause: report generator loads entire dataset to memory, byte[] promoted to old gen, never GC'd (held by static cache); (5) fix: stream processing instead of full load, weak references for cache, `SoftReference` for cache eviction under memory pressure; (6) verify: GC log normal, p99 back to 50ms. Lesson: latency often correlates with GC, profile GC first.

2. **Optimize hot path from 1000 RPS to 5000 RPS**. Шаги: (1) baseline: async-profiler flame graph — 40% in JSON serialization (Jackson), 30% in DB query, 20% in business logic, 10% other; (2) serialization — switch to Jackson `Afterburner`/`Blackbird` module (3× faster), or Smile binary format, or precompute serialized form for hot objects; (3) DB query — EXPLAIN ANALYZE shows seq scan; add covering index, fetch reduced from 100ms to 5ms; (4) business logic — `HashMap` lookups on hot path, replace with primitive array indexed by ID; (5) re-profile: 70% in serialization (now faster), 15% DB, 10% logic; (6) iterate: switch to Protobuf for inter-service (2× faster); (7) verify with wrk2: 5000 RPS sustained at p99 < 100ms.

3. **Apply Amdahl's Law to migration from sync to async**. Шаги: serial fraction of request = DB query (50ms) + serialization (10ms). Parallelizable: 3 independent API calls (each 100ms). Current: 100×3 + 50 + 10 = 360ms. With 3-way parallel: 100 (max of parallel) + 60 = 160ms. Speedup = 2.25×. If we had 100 cores, can't go below 160ms (serial fraction = 60ms). Conclusion: optimize serial fraction (DB index, faster codec) for further gains.

4. **M/M/1 sizing for queue depth vs latency trade-off**. Шаги: service μ = 100 req/s (10ms per request). At λ=50 (ρ=0.5): Wq = 0.5/(100×0.5) = 10ms wait; total latency 20ms. At λ=80 (ρ=0.8): Wq = 0.8/(100×0.2) = 40ms wait; total 50ms. At λ=90 (ρ=0.9): Wq = 90ms; total 100ms. At λ=99 (ρ=0.99): Wq = 990ms; total 1s. Decision: target ρ=0.7 for p99 < 50ms. Provision: max λ = 70 req/s per server. For 1000 RPS target: 1000/70 = 15 servers (with headroom).

5. **GC tuning for low-latency trading service (p99 < 5ms)**. Шаги: (1) baseline: G1, p99 = 50ms (G1 pause 200ms every 30s); (2) try: `-XX:+UseZGC -XX:+ZGenerational` (Java 21) — pause < 1ms; throughput slightly lower (concurrent GC overhead); (3) verify: p99 = 3ms sustained; (4) tune: `-XX:SoftMaxHeapSize=70%` (aggressive GC), `-XX:ZUncommitDelay=300` (return memory); (5) allocation rate still high — refactor: object pooling for hot path, escape analysis, scalar replacement via C2; (6) benchmark with JMH `@BenchmarkMode(Mode.SampleTime)` — confirm p99.99 = 5ms.

6. **Coordinated omission in load test**. Шаги: tool sends next request when previous completes; load test shows 1ms latency at 10k RPS. Reality: when server slows (1s), tool waits, sends fewer requests. True latency = much higher. Fix: use wrk2 with fixed rate (constant throughput), measures all (including queued). Re-run: p99 = 500ms (not 1ms). Fix server: thread pool sizing, queue bounded. Lesson: tool choice critical — wrk2, vegeta, TRL not ab/JMeter default.

7. **Premature optimization avoidance: measure first**. Сценарий: dev wants to replace `ArrayList` with `int[]` for "performance". Шаги: profile — list access is 0.1% of CPU. Optimization would save 0.1% — invisible in production. Real bottleneck: JDBC fetch. Optimize that (10× gain). Lesson: profile-driven, not assumption-driven. Document: «considered array optimization, measured impact <0.1%, deprioritized».

---

## 18. Security для разработчика (OWASP Top 10, secure coding, threat modeling STRIDE/DREAD, secrets management, OWASP ASVS, supply chain security, SAST/DAST/SCA)

### Теория

- **OWASP Top 10 (2021)** — A01 Broken Access Control, A02 Cryptographic Failures, A03 Injection, A04 Insecure Design, A05 Security Misconfiguration, A06 Vulnerable & Outdated Components, A07 Identification & Authentication Failures, A08 Software & Data Integrity Failures, A09 Security Logging & Monitoring Failures, A10 Server-Side Request Forgery (SSRF). Periodic update.
- **OWASP ASVS (Application Security Verification Standard)** — 3 levels: L1 (low assurance), L2 (standard), L3 (high — financial, critical). 286 requirements across 14 chapters. Use as checklist for secure design.
- **STRIDE threat model (Microsoft)** — Spoofing (identity), Tampering (data integrity), Repudiation (deny action), Information Disclosure (leak), Denial of Service (availability), Elevation of Privilege. Per-component analysis.
- **DREAD risk assessment** — Damage (potential), Reproducibility, Exploitability, Affected users, Discoverability. Each 1-10, avg = risk score. Criticized: subjective, but useful for prioritization.
- **PASTA (Process for Attack Simulation and Threat Analysis)** — 7-step risk-centric methodology: define objectives, define scope, application decomposition, threat analysis, vulnerability analysis, attack modeling, risk analysis. More rigorous than STRIDE.
- **Secure coding principles** — validate input (allowlist not blocklist), encode output (context-aware: HTML, JS, URL, CSS), enforce authz (deny by default), fail secure (default deny), least privilege, defense in depth, separate privileges, security through obscurity ≠ security.
- **Injection prevention** — parameterized queries (PreparedStatement in Java, never string concat), ORMs (Hibernate HQL/JPQL with parameters), stored procedures (still parameterize), input validation (type, length, format, range). SQL injection example: `' OR '1'='1` → if string concat, returns all rows.
- **XSS prevention** — context-aware encoding: HTML body (`&lt;`), attribute (`"` → `&quot;`), JavaScript (`\x23`), URL (`%23`). CSP (Content-Security-Policy) header — restrict scripts to whitelist. `HttpOnly` cookies (no JS access). Frameworks: Spring `HtmlUtils.htmlEscape()`, OWASP Java Encoder.
- **CSRF prevention** — anti-CSRF token (synchronizer pattern), SameSite cookie attribute (Strict/Lax), check `Origin`/`Referer` header, double-submit cookie. Spring Security CSRF filter enabled by default.
- **SSRF prevention** — allowlist of allowed hosts, deny private IP ranges (RFC 1918), validate redirect chains, no raw user input in URL, network segmentation (server can't reach internal).
- **Authentication** — bcrypt/scrypt/Argon2 for passwords (slow, memory-hard), PBKDF2 acceptable. Never MD5/SHA1/SHA256 alone. MFA — TOTP (RFC 6238), WebAuthn (FIDO2). Session: long random token (128+ bits), secure cookie, rotate on login, invalidate on logout.
- **Authorization** — RBAC (Role-Based Access Control), ABAC (Attribute-Based, fine-grained), ReBAC (Relationship-Based, Google Zanzibar). Enforce on every request, not just UI. Default deny. Idempotent checks.
- **Cryptographic failures (A02)** — TLS 1.2+ (1.3 preferred), strong ciphers (AES-GCM, ChaCha20-Poly1305), no RC4/DES/3DES. Certificate validation (don't disable!). Hashing: SHA-256+ for integrity, bcrypt/Argon2 for passwords. Random: `SecureRandom`, not `Random`. Keys: rotation, KMS, never hardcode.
- **Secrets management** — never in code/repo (git history is forever), env vars (limited — leak via /proc, ps), secrets manager (HashiCorp Vault, AWS Secrets Manager, GCP Secret Manager). Spring Cloud Vault. Rotation: automated. Audit log of access.
- **Supply chain security** — dependencies (CVE scan: Snyk, Dependabot, Trivy), SBOM (Software Bill of Materials, CycloneDX, SPDX), reproducible builds, signed artifacts (Sigstore, cosign), pinned versions (not `latest`), private registry (Nexus, Harbor) with proxy + scan.
- **SAST (Static Application Security Testing)** — analyze source code, no execution. Tools: SonarQube, Snyk Code, Semgrep, Fortify, Checkmarx. Find: injection, hardcoded secrets, weak crypto. False positives common — tune.
- **DAST (Dynamic Application Security Testing)** — test running app, black-box. Tools: OWASP ZAP, Burp Suite, Nikto. Find: runtime vulns, misconfigurations. Slower, but catches what SAST misses.
- **SCA (Software Composition Analysis)** — scan dependencies for known CVEs. Tools: Snyk, Dependabot, Trivy, OWASP Dependency-Check. Critical: zero-day in dependency (Log4Shell).
- **IAST/RAST** — Interactive (instrumented) — runs in test with sensors. RAST (Runtime) — in production. More accurate than SAST/DAST, but less common.
- **Logging & monitoring (A09)** — log security events (auth, authz, input validation failures, exceptions), structured (JSON), centralized (SIEM: Splunk, ELK, Datadog), alerting on anomalies. Don't log secrets (PII, tokens, passwords).
- **Security headers** — `Content-Security-Policy`, `Strict-Transport-Security` (HSTS), `X-Frame-Options` (clickjacking), `X-Content-Type-Options: nosniff`, `Referrer-Policy`, `Permissions-Policy`. Spring Security `HeaderWriter`.
- **PCI-DSS** — for handling payment cards. Tokenization (don't store PAN), segregation, audit trails. Use payment processor (Stripe, Adyen) to reduce scope.
- **GDPR / data protection** — minimize PII collected, encrypt at rest, right to deletion, data residency. Audit log of access to PII.

### Материалы

- **Книга**: "Web Security for Developers", Malcolm McDonald (No Starch Press, 2020).
- **Книга**: "Threat Modeling: Designing for Security", Adam Shostack (Wiley, 2014).
- **Дока**: OWASP Top 10 — https://owasp.org/www-project-top-ten/
- **Дока**: OWASP ASVS — https://owasp.org/www-project-application-security-verification-standard/
- **Дока**: OWASP Cheat Sheet Series — https://cheatsheetseries.owasp.org/
- **Статья**: "Threat Modeling - OWASP Cheat Sheet" — https://cheatsheetseries.owasp.org/cheatsheets/Threat_Modeling_Cheat_Sheet.html
- **Статья**: "STRIDE vs DREAD vs PASTA: Threat Modeling Framework Comparison" — https://www.softwaresecured.com/post/comparison-of-stride-dread-pasta

### Практика / Кейсы

1. **Threat model for new payment API with STRIDE**. Шаги: per component: (a) Auth Service — Spoofing: stolen credentials (mitigation: MFA, rate limit); Repudiation: deny transaction (mitigation: signed audit log); (b) Payment API — Tampering: modify amount in transit (mitigation: TLS, signature); Information Disclosure: leak PAN (mitigation: tokenize, no logging); (c) Webhook Receiver — Spoofing: fake webhook (mitigation: HMAC signature verify); DoS: flood webhooks (mitigation: rate limit, idempotency); (d) Database — Elevation of Privilege: SQL injection (mitigation: parameterized queries, least privilege DB user). Document in ADR. Review quarterly.

2. **Secure coding: prevent SQL injection in Spring Data JPA**. Шаги: BAD — `@Query("SELECT u FROM User u WHERE u.name = '" + name + "'")` (string concat). GOOD — `@Query("SELECT u FROM User u WHERE u.name = :name") List<User> findByName(@Param("name") String name);`. For dynamic: `CriteriaBuilder` or `QueryDSL` — type-safe, parameterized. For native: `EntityManager.createNativeQuery("... WHERE name = ?", User.class).setParameter(1, name)`. SAST catches string concat. Test: try `name = "admin' OR '1'='1"` — should return 0 rows, not all.

3. **Secrets management migration: hardcoded → Vault**. Шаги: audit — `git log -p | grep -E "(password|api_key|secret)"` — found 50 hardcoded secrets across services. Rotate all (assume compromised). Setup Vault: KV secrets engine, policies per service. Spring Cloud Vault: `spring.cloud.vault.uri`, `spring.cloud.vault.token` (from env, not in repo). Refactor: `@ConfigurationProperties` reads from Vault. CI: Vault token via JWT auth (short-lived). Audit: Vault audit log to SIEM. Local dev: `.env` files (gitignored), Vault dev mode. Result: 0 secrets in git.

4. **Supply chain: Log4Shell response + prevention**. Шаги: incident (Dec 2021) — Log4j 2.14 CVE-2021-44228 RCE. Response: (1) inventory — `mvn dependency:tree | grep log4j` — find all uses; (2) upgrade to 2.17.1 (patched); (3) for unpatchable — set `log4j2.formatMsgNoLookups=true` (mitigation); (4) verify — canary exploit attempt, observe no callback; (5) rotate secrets (assume compromise); (6) IDS for exploit attempts. Prevention going forward: (1) SCA in CI (Snyk blocks on critical CVE); (2) SBOM generation per release; (3) automated dependency PRs (Dependabot); (4) private registry proxy (Nexus) with scan; (5) pin versions, no `latest`.

5. **Implement CSP to prevent XSS**. Шаги: audit current scripts — inline scripts, eval, external CDNs. Strict CSP: `Content-Security-Policy: default-src 'self'; script-src 'self' 'nonce-{random}'; style-src 'self' 'unsafe-inline'; img-src 'self' data: https:; connect-src 'self' https://api.example.com; frame-ancestors 'none'; base-uri 'self'; form-action 'self'`. Spring Security: `.headers().contentSecurityPolicy("default-src 'self'...")`. Replace inline scripts with nonce-tagged. Report-only mode first (no breakage), monitor violations, then enforce.

6. **SAST + DAST pipeline для Java service**. Шаги: PR pipeline: (1) SAST — SonarQube scan, fail on Critical; (2) SCA — Snyk scan, fail on Critical with no fix path; (3) secret scan — gitleaks, fail on any; (4) license check — OWASP Dependency-Check. Merge pipeline: (5) DAST — OWASP ZAP scan on staging, fail on High+; (6) container scan — Trivy, fail on Critical; (7) IAST — in integration tests, instrumented. Production: (8) RASP (optional) — runtime protection. Weekly: (9) external pentest. Track: time-to-fix by severity. SLA: Critical 24h, High 7d, Medium 30d, Low best-effort.

7. **OWASP ASVS L2 audit for fintech app**. Шаги: gather team (security + dev). Go through 286 ASVS L2 requirements. Status: pass/fail/NA per control. Findings: (1) V2 (auth) — password policy missing rotation for admins; (2) V4 (access control) — no rate limit on password reset; (3) V7 (logging) — auth events not logged centrally; (4) V9 (communications) — TLS 1.0 still enabled. Remediation plan: each finding → Jira ticket, owner, due date. Track in dashboard. Re-audit quarterly. Goal: L2 certified within 6 months.

---

## 19. Cost optimization (cloud cost, FinOps, right-sizing, spot instances, reserved capacity, multi-tenancy, autoscaling)

### Теория

- **FinOps** — financial operations: cultural practice bringing financial accountability to cloud. Phases: Inform (visibility), Optimize (reduce), Operate (continuous). FinOps Foundation: practitioner framework. Goal: cost-aware engineering decisions.
- **Cloud cost dimensions** — compute (EC2, EKS, Lambda), storage (S3, EBS, RDS), network (egress, NAT, VPN), managed services (RDS, MSK, OpenSearch), licenses (RHEL, Windows). Tagging strategy: team, project, environment, cost center.
- **Right-sizing** — match instance type/size to actual usage. Over-provisioned = waste. Process: monitor (CPU, memory, network), identify underused (CPU < 30% for 2 weeks), downgrade or terminate. AWS Compute Optimizer, Kubecost for K8s.
- **Reserved capacity** — Reserved Instances (AWS, 1-3 yr), Committed Use Discounts (GCP), Reserved VM Instances (Azure). Up to 72% off on-demand. Trade-off: commitment vs flexibility. Convertible RIs allow instance family changes.
- **Savings Plans** — AWS: commit to $/hour spend for 1-3 yr, applies to EC2/Fargate/Lambda. More flexible than RIs. Compute Savings Plans (broader) vs EC2 Instance Savings Plans (specific family).
- **Spot instances** — spare cloud capacity, up to 90% off. Can be terminated with 2-min warning. Use for: batch jobs, CI workers, stateless web (with checkpointing), data processing. NOT for: long-running singletons, databases, stateful services.
- **Autoscaling** — scale out/in based on metrics (CPU, RPS, queue depth). HPA (K8s Horizontal Pod Autoscaler), Cluster Autoscaler, Karpenter. Scale to zero (serverless, Fargate). Trade-off: cold start vs cost. Predictive autoscaling — ML-based forecasting.
- **Multi-tenancy** — share infrastructure across tenants. Models: (a) silo — per-tenant resources (most isolated, most expensive); (b) pool — shared resources (cheapest, less isolated); (c) bridge — hybrid. Cost vs isolation trade-off. SaaS services typically pool with logical separation.
- **Storage tiering** — S3 Standard, Infrequent Access (IA), Glacier (archive). Lifecycle policies: move to IA after 30 days, Glacier after 90. EBS: gp3 (cheaper than gp2), through-put optimized for logs. RDS: storage autoscaling.
- **Network cost** — egress (data leaving cloud) is expensive ($0.09/GB AWS to internet). Reduce: CDN (cached at edge), CloudFront Function Compute (vs origin), VPC endpoints (avoid NAT), same-region traffic (free in AWS within AZ, $0.01/GB cross-AZ). Multi-region: minimize cross-region replication.
- **Database cost** — RDS: instance type + storage + IOPS. Aurora Serverless (scale-to-zero, pay-per-use). Read replicas — cheaper than primary if read-heavy. DynamoDB: provisioned (cheaper if predictable) vs on-demand (cheaper if bursty). Multi-region: high cost, only if justified.
- **Caching cost optimization** — Redis/ElastiCache: smallest instance for workload, read replicas for read-heavy, cluster mode for sharding. Caffeine (in-process) — free, but per-instance. Two-tier: Caffeine (L1) + Redis (L2) — reduces Redis load.
- **Lambda cost model** — pay per invocation + GB-seconds. $0.0000002 per request + $0.0000166667 per GB-second. Break-even vs EC2: ~250M req/month for small workloads. Provisioned concurrency — for low latency, extra cost. SnapStart — reduces cold start, free.
- **Container cost** — ECS/EKS: EC2 (pay for VM) vs Fargate (pay per pod, premium). Fargate break-even: ~10 pods per EC2. Karpenter — better bin-packing than Cluster Autoscaler (15-30% savings).
- **License cost** — RHEL, Windows Server, Oracle DB, MS SQL. Bring-your-own-license (BYOL) vs cloud-included. Open-source alternatives: PostgreSQL for Oracle, Linux for Windows. Audit annually.
- **Cost visibility** — AWS Cost Explorer, GCP Billing, Azure Cost Management. Tag-based allocation. Per-team chargeback/showback. Anomaly detection (budget alerts). Unit economics: cost per request, cost per user.
- **Cost of dev/test environments** — auto-stop non-prod at night/weekend (60-70% savings). Ephemeral envs per PR (Terraform + auto-destroy). Shared staging (vs per-team) — tradeoff with isolation.
- **Architectural cost levers** — (1) serverless (pay-per-use, no idle); (2) microservices with shared infra (DB pooling); (3) event-driven (idle services don't run); (4) batch (vs real-time when latency OK); (5) CDN for static (vs origin); (6) edge compute for low-latency regions.
- **Cost of observability** — metrics/logs/traces storage is expensive. Strategies: sampling (traces 1-10%), retention tiers (hot 7d, cold 90d), pre-aggregation (recording rules in Prometheus), drop high-cardinality labels. Datadog/NewRelic host-based pricing — count hosts carefully.
- **Technical decisions cost-aware** — choice of database (PostgreSQL vs DynamoDB), queue (SQS vs Kafka self-hosted), framework (Spring Boot vs Quarkus native for serverless). Each has TCO implication. Document in ADR.

### Материалы

- **Книга**: "Cloud FinOps", J.R. Storment & Mike Fuller (O'Reilly, 2019).
- **Дока**: FinOps Foundation — https://www.finops.org
- **Дока**: AWS Cost Optimization — https://aws.amazon.com/aws-cost-optimization/
- **Дока**: AWS Well-Architected — Cost Optimization Pillar — https://docs.aws.amazon.com/wellarchitected/latest/cost-optimization-pillar/welcome.html
- **Статья**: "Cost Optimization for AWS EC2 Autoscaling" — https://www.finops.org/wg/cost-optimization-for-aws-ec2-autoscaling
- **Статья**: "Cloud Cost Optimization Strategies: The Complete Guide" — https://spot.rackspace.com/blog/cloud-cost-optimization
- **Дока**: Karpenter (K8s autoscaling) — https://karpenter.sh

### Практика / Кейсы

1. **Right-sizing Kubernetes cluster (50 nodes, $50k/month)**. Шаги: (1) install Kubecost orkubectl-cost; (2) identify low-utilization pods (CPU < 100m, memory < 256Mi requested but actually using 50Mi) → reduce requests; (3) over-provisioned nodes — replace m5.2xlarge (8 vCPU, 32GB) with m5.large (2 vCPU, 8GB) if node avg < 30% CPU; (4) switch from Cluster Autoscaler to Karpenter — better bin-packing (15% savings); (5) Spot for stateless workloads (CI, batch) — 70% savings on those nodes; (6) Consolidate namespaces per team — fewer but larger namespaces; (7) Right-size Java heap (`-XX:MaxRAMPercentage=75`, not `-Xmx4G` everywhere). Result: 35 nodes, $32k/month (36% savings).

2. **Reserved Instances + Savings Plans mix**. Шаги: analyze past 6 months usage. Steady-state (production EC2): 60% of spend → Reserved Instances (3yr, all upfront, 60% off). Predictable but variable: 25% → Compute Savings Plans (1yr, no upfront, 50% off). Unpredictable/bursty: 15% → On-Demand + Spot. Mix: 60% RI + 25% SP + 15% OD/Spot. Total: 55% off blended rate. Re-evaluate quarterly as workload changes. Tools: AWS Cost Explorer RI recommendations.

3. **Spot instances for CI/CD fleet**. Шаги: identify stateless workloads — CI runners (GitHub Actions self-hosted, Jenkins agents). Replace on-demand EC2 with Spot. Config: Spot Fleet with multiple instance types (m5.large, m5a.large, c5.large), 2-min termination handler (drain runner, finish current job). Result: 80% cost reduction on CI ($20k → $4k/month). Tradeoff: occasional 2-min job interruption (acceptable, retry). For critical pipelines: 1 on-demand + N spot, fall back on-demand if Spot unavailable.

4. **Multi-tenancy architecture for SaaS**. Шаги: 100 tenants, small/medium. Options: (a) silo (per-tenant RDS) — $500/tenant/month × 100 = $50k/month; (b) bridge (shared app, per-tenant schema in one RDS) — $5k/month + per-tenant overhead; (c) pool (shared app, shared schema, tenant_id column) — $5k/month total. Choose (b) for moderate isolation + reasonable cost. Implementation: RDS Proxy for connection pooling, Flyway per-schema migrations, per-tenant rate limit. Pool isolation: Row-Level Security (PostgreSQL RLS) for tenant_id. Audit: per-tenant query log. Cost: $8k/month for 100 tenants ($80/tenant).

5. **Storage tiering for S3 logs (50 TB)**. Шаги: current — all S3 Standard ($0.023/GB = $1150/month). Lifecycle policy: (1) Standard for 30 days (hot, debugging); (2) S3 Standard-IA for 30-90 days ($0.0125/GB — rare access); (3) S3 Glacier Instant Retrieval for 90-365 days ($0.004/GB); (4) S3 Glacier Deep Archive for 1+ year ($0.00099/GB). Result: 30 days × 50TB × $0.023 + 60d × $0.0125 + 275d × $0.004 + 365d+ × $0.00099 → ~$300/month (75% savings). Add: S3 Intelligent-Tiering for unpredictable access patterns (auto-moves based on access).

6. **Lambda vs Fargate vs EC2 cost analysis**. Шаги: workload — REST API, 100 RPS avg, 1000 RPS peak, 50ms processing. (a) Lambda: 100 req/s × 86400s × 30d = 259M req/month × $0.0000002 + 259M × 0.05s × 512MB/1024 × $0.0000166667 = $52 + $108 = $160/month. (b) Fargate: 1 task (0.5 vCPU, 1GB) for avg, autoscale to 10 for peak. 30 days × 24h × $0.04048/vCPU × 0.5 + $0.004445/GB × 1 = ~$50/month avg + spike. Total ~$70/month. (c) EC2: 1 t3.medium (2 vCPU, 4GB) always-on = $25/month, handles 100 RPS comfortably, autoscale to 4 for peak. Total ~$50/month. Choose EC2 for steady load; Lambda for spiky; Fargate for moderate. Conclusion: EC2 cheapest for this workload.

7. **Cost of observability — reduce Datadog bill ($30k/month)**. Шаги: (1) analyze — host-based pricing, 500 hosts × $30/host = $15k; logs $10k (5TB ingest); APM $5k (1M traces); (2) hosts — 100 dev/test hosts: install only metrics (no logs/APM) — save $3k; (3) logs — drop DEBUG/INFO in prod, only WARN+; reduce retention 30d → 14d; sample 50% — save $5k; (4) APM — instrument only critical services (payment, auth), sample 5% — save $3k; (5) alternative — self-host Grafana/Loki/Tempo/Prometheus for non-critical, keep Datadog for production critical — save $7k. Total: $30k → $12k/month (60% savings).

---

## 20. Career path (Junior→Middle→Senior→Staff→Principal; staff engineer archetypes, influence without authority, tech radar, community, open source contribution, conferences, writing)

### Теория

- **Career ladder (IC track)** — Junior (L3): learns, executes well-defined tasks; Middle (L4): independent on standard problems, learns breadth; Senior (L5): leads features, mentors juniors, makes technical decisions for team; Staff (L6): cross-team impact, sets technical direction; Principal (L7): org-wide impact, multi-year vision; Distinguished (L8): industry-recognized expertise; Fellow (L9+): rare, world-class.
- **Manager track parallel** — Senior Manager (L6), Director (L7), Sr Director (L8), VP (L9), etc. IC and Manager tracks should be equivalent in compensation and prestige.
- **Time at level** — Junior→Middle: 1-2 years; Middle→Senior: 2-4 years; Senior→Staff: 3-6 years (often slower, fewer Staff slots); Staff→Principal: 4-8 years. Don't rush — depth matters.
- **Staff Engineer archetypes (Will Larson)** — (1) The Architect (single system, multi-team); (2) The Problem Solver (jumps into crises, unblocks); (3) The Right Hand (partners with manager/Director); (4) The Partner (works alongside EMs of multiple teams). Most people lean to one; can shift over time.
- **Influence without authority** — Staff+ doesn't have direct reports. Tools: trust (built over time), expertise (deep knowledge), data (benchmarks, metrics), persuasion (writing, speaking), alliances (other seniors, EMs), sponsorship (advocated by leadership). Book: "Influence Without Authority" Cohen & Bradford.
- **Tech radar** — ThoughtWorks-style: Adopt (use), Trial (evaluate), Assess (explore), Hold (don't use). Per-category (languages, frameworks, tools, platforms). Updates quarterly. Aligns org on tech direction without mandating.
- **Open source contribution** — levels: (1) bug reports (low effort); (2) doc improvements; (3) small bug fixes; (4) features; (5) maintainer. Benefits: visibility, network, learning, career signal. Etiquette: read CONTRIBUTING.md, small PRs, be patient, don't demand.
- **Conference speaking** — local meetups (entry), regional confs (specialty), international (QCon, Devoxx, KubeCon, DevOps Days). CFP (Call for Proposals) process. Benefits: visibility, network, deepen knowledge (teaching = learning), career signal. Start with lightning talks.
- **Technical writing** — blog posts (own site or Medium), articles (InfoQ, DZone, company blog), books (Manning, O'Reilly). Writing clarifies thinking. SEO compounds over time. Guest posts reach broader audience.
- **Community building** — meetups, Discord/Slack, mentoring programs (Google Summer of Code, Outreachy), conference organizing. Builds network, leadership, visibility. Time investment, but compounds.
- **Promotion process** — at most companies: (1) self-nominate or manager nominates; (2) write promo packet (projects, impact, scope, leadership); (3) peer feedback collected; (4) calibration committee reviews; (5) decision. Process takes 1-3 months. Staff+ often requires evidence of cross-team/org impact.
- **Promo packet writing** — STAR: Situation, Task, Action, Result. Quantify impact (X% improvement, $Y saved, N users affected). Show scope (team, multi-team, org). Show leadership (mentored N, drove initiative, influenced direction). Skip +1 skip-level reviews. Multiple cycles may be needed.
- **Networking** — internal: cross-team collaboration, tech talks, ERGs; external: conferences, OSS, Twitter/LinkedIn, alumni. Network = opportunities (jobs, info, mentors). Build before you need it.
- **Mentor and sponsor** — mentor: gives advice. Sponsor: advocates in rooms you're not in. Both needed for advancement. Senior+ should sponsor juniors.
- **Performance review** — self-assessment, peer feedback, manager assessment, calibration. Cycle: 6-12 months. Document achievements continuously (brag document). Discuss career goals regularly with manager.
- **Continuous learning** — books (1-2 per quarter), online courses (Coursera, Udemy, Frontend Masters), conference talks (YouTube), technical blogs (subscribed RSS), podcasts. Apply learning (side projects, OSS). Track in personal TIL (Today I Learned) doc.
- **Specialist vs generalist** — specialist: deep in one area (e.g., JVM perf, distributed systems); generalist: broad across stack. T-shaped: depth in one, breadth across. Senior+ often T-shaped. Career trajectory: generalist early, specialize later (or remain generalist at Staff+).
- **Job changes** — average tenure 2-4 years. Reasons to leave: comp, growth, toxic culture, dead-end. Reasons to stay: growth, comp, autonomy, mission, team. Don't leave for <20% comp bump without other reasons. Staff+ often achieved faster via job changes (fewer Staff slots internally).
- **Burning out** — signs: cynicism, exhaustion, reduced efficacy. Causes: workload, lack of control, lack of recognition, poor relationships, unfairness, values mismatch. Recovery: time off, set boundaries, therapy, change role/team/job. Senior+ must model healthy behavior.
- **Imposter syndrome** — feeling of being a fraud despite evidence of competence. Common in tech, especially among underrepresented groups. Mitigation: track achievements, talk to peers (universal feeling), seek mentorship, accept growth mindset (not knowing is OK).
- **Personal brand** — what you're known for. Build deliberately: tech focus (e.g., "Java performance guy"), content (blog, talks, OSS), visibility (social media, conferences). Brand = opportunities.

### Материалы

- **Книга**: "The Staff Engineer's Path", Tanya Reilly (O'Reilly, 2022) — https://www.oreilly.com/library/view/the-staff-engineers/9781098118723
- **Книга**: "Staff Engineer: Leadership beyond the management track", Will Larson — https://staffeng.com/book (free online).
- **Книга**: "The Manager's Path", Camille Fournier (O'Reilly, 2017).
- **Книга**: "Growing as a Developer", Pat Kua — https://blog.patkua.com/category/growing-as-a-developer/
- **Сайт**: StaffEng — интервью со Staff+ engineers — https://staffeng.com
- **Статья**: Pragmatic Engineer — "The Staff Engineer's Path: You're a Role Model Now (Sorry)" — https://newsletter.pragmaticengineer.com/p/the-staff-engineers-path
- **Статья**: "The Staff+ Canon: Tools for Leading Without Authority" — https://laconicwit.com/the-staff-canon-tools-for-leading-without-authority

### Практика / Кейсы

1. **Promo packet for Senior → Staff (cross-team impact)**. Шаги: identify Staff-level work in past year: (1) led migration from RabbitMQ to Kafka across 5 teams — saved $200k/year, enabled 10× throughput; (2) architected multi-region failover (RTO 5min) — prevented 2 incidents from becoming outages; (3) mentored 3 mid-levels, 1 promoted to Senior; (4) drove org-wide ADR on event schema versioning. For each: STAR — Situation (teams using ad-hoc queueing), Task (standardize, scale), Action (designed, presented RFC, partnered with each team), Result (5 teams migrated, $200k savings, 10× throughput). Peer feedback from 4 EMs + 2 PMs. Skip-level endorsement. Submit 6 months ahead of cycle — start gathering evidence early.

2. **Tech radar launch for engineering org (200 engineers)**. Шаги: form committee — 1 Principal + 2 Staff + 1 EM + rotate 2 engineers. Quarterly process: (1) collect proposals (any engineer can submit via form); (2) committee reviews, classifies Adopt/Trial/Assess/Hold; (3) publish to internal wiki + discuss at eng all-hands; (4) follow up next quarter (move Trial→Adopt or Trial→Hold based on experience). Initial radar: Adopt — Java 21, Spring Boot 3, Kafka, PostgreSQL, K8s. Trial — GraalVM native, Micronaut, Temporal. Assess — Rust for services, WebAssembly. Hold — new frameworks without adoption. Result: aligns org, reduces tech fragmentation.

3. **First open source contribution: Spring Framework**. Шаги: (1) identify issue — Spring GitHub "good first issue" label; (2) read CONTRIBUTING.md — sign CLA, code style; (3) fork, branch `fix-issue-12345`; (4) write test reproducing bug; (5) write fix, run tests; (6) commit with conventional message `fix: handle null in BeanWrapper`; (7) push, open PR with description linking issue; (8) respond to reviewer feedback (may take weeks — be patient); (9) PR merged — celebrate. Benefits: Spring team knows your name, learning contribution workflow, public artifact on GitHub profile. Next: tackle bigger issue, or contribute to docs.

4. **Conference talk proposal (QCon)**. Шаги: pick topic — «Java 21 virtual threads in production: lessons». Outline: (1) why we migrated; (2) approach (gradual, strangler); (3) pitfalls (synchronized pinning, ThreadLocal leaks); (4) results (RPS 2×, memory 50% lower); (5) recommendations. CFP submission: title, abstract (200 words), learning outcomes (3 bullets), speaker bio. Submit 6-9 months before conference. If accepted: prepare slides (30 slides for 40-min talk), rehearse 5+ times, get feedback, deliver at local meetup first. If rejected: submit to 3-4 other conferences (acceptance rate 10-30%). Talk becomes evergreen content on YouTube.

5. **Personal brand building over 12 months**. Шаги: month 1-3: identify niche — Java performance & observability. Start blog, write 1 post/week (deep dives on JMH, JFR, async-profiler). Month 4-6: tweet daily insights (use Twitter/X), engage with Java community, share blog posts. Month 7-9: submit talks to local JUG meetups. Month 10-12: aim for regional conference (Jfall, Devoxx Belgium). Result: 1000 blog subscribers, 5000 Twitter followers, known in Java perf community. Career impact: recruiter outreach 3×, consulting opportunities, book deal offered. Sustain: schedule content time (4h/week), batch writing, don't burn out.

6. **Career transition: Senior at startup → Staff at big tech**. Шаги: (1) assess — Senior at startup = wears many hats, broad; Staff at big tech = cross-team depth in one area. Gap: formal promo process, written communication, influencing without authority at scale. (2) prepare — write 2-3 RFCs on past work (practice written design), read company's Staff promo criteria, network with current Staff engineers. (3) interview — Staff interviews often include system design (multi-component), behavioral (cross-team leadership), coding (hard). Practice: 50+ system design mock interviews. (4) on-the-job — first 6 months: take on Staff-scope project (multi-team), build relationships, deliver visible impact. (5) promo cycle — 1-2 years typical. Don't expect immediate Staff; may need to demonstrate at new company first.

7. **Burnout recovery and prevention plan**. Шаги: recognition — cynical about work, exhausted, declining performance. Immediate: take 2-week vacation (no email, no Slack), talk to manager about workload. Short-term: reduce hours (40 max), drop non-essential commitments, therapy. Long-term: set boundaries (no work evenings/weekends), negotiate realistic commitments with PM, rotate on-call (not primary for 6 months), find meaning in work (connect to impact), build support network (peers, mentor). Company-level: model healthy behavior as Staff+ (don't send midnight emails, take visible vacations). Track: weekly self-check (energy, cynicism, efficacy). Burnout is organizational, not individual — but recovery starts with individual boundaries.

---

## Сводка раздела

**Раздел 8** покрывает переход от «Senior-разработчика» к «Staff/Principal-инженеру»: от умения применять паттерны GoF и SOLID — к проектированию систем (DDD, Clean Architecture, CQRS), от понимания алгоритмов LeetCode Hard — к performance engineering (Little's Law, Amdahl, queueing theory), от code review — к stakeholder management и техническое лидерство.

**Ключевые темы:**
- **Patterns**: SOLID + GoF (23 паттерна) + DI — фундамент.
- **Architecture**: Hexagonal/Clean/Onion + DDD + CQRS/Event-Driven + Microservices/Modular Monolith — выбор под проблему.
- **System Design**: capacity estimation, sharding, replication, caching, hot spots — масштабирование.
- **Algorithms & Data Structures**: LeetCode Hard уровень — Foundation для system design и code review.
- **Concurrent data structures**: ConcurrentHashMap, Disruptor, Michael-Scott queue — низкоуровневая конкуренция.
- **Engineering practices**: refactoring, code review, testing pyramid, documentation (RFC/ADR/C4).
- **Leadership**: mentoring, influence without authority, stakeholder management, conflict resolution.
- **Operating**: Agile/Lean/DevOps culture, performance engineering, security (OWASP/STRIDE), FinOps cost optimization.
- **Career**: Junior→Staff→Principal ladder, staff engineer archetypes, personal brand, OSS, conferences, writing.

**Top источников (12):**
1. "Design Patterns" GoF (Gamma et al.) — https://en.wikipedia.org/wiki/Design_Patterns
2. "Clean Architecture" Robert C. Martin — Pearson, 2017.
3. "Domain-Driven Design" Eric Evans — Addison-Wesley, 2003.
4. "Implementing DDD" Vaughn Vernon — Addison-Wesley, 2013.
5. "Designing Data-Intensive Applications" Martin Kleppmann — O'Reilly, 2017.
6. "System Design Interview" Alex Xu — ByteByteGo, 2020 (https://bytebytego.com).
7. "Clean Code" Robert C. Martin — Pearson, 2008.
8. "Refactoring" 2nd ed. Martin Fowler — Addison-Wesley, 2018 (https://refactoring.guru).
9. "The Staff Engineer's Path" Tanya Reilly — O'Reilly, 2022 (https://www.oreilly.com/library/view/the-staff-engineers/9781098118723).
10. "Staff Engineer" Will Larson — https://staffeng.com/book (free).
11. Martin Fowler bliki — https://martinfowler.com/bliki/ (DDD, TellDontAsk, OpportunisticRefactoring, TechnicalDebtQuadrant, StranglerFigApplication).
12. OWASP Top 10 + ASVS — https://owasp.org/www-project-top-ten/ + https://owasp.org/www-project-application-security-verification-standard/.

**Связь с другими разделами:**
- **01 (Java Core)**: records, sealed interfaces, pattern matching — основа для современных паттернов.
- **02 (Java Advanced)**: JMM, ConcurrentHashMap, virtual threads — фундамент concurrent data structures.
- **03 (Build/Test)**: JUnit 5, Testcontainers, PITest mutation testing — основа testing strategy.
- **04 (Spring)**: IoC/DI, AOP, @Transactional — реализация архитектурных паттернов.
- **05 (Persistence)**: Repository pattern, Aggregate boundaries, sharding — DDD tactical.
- **06 (Distributed)**: CQRS, Event Sourcing, saga, idempotency — Event-Driven architecture.
- **07 (DevOps)**: SLO-driven architecture, observability, postmortem culture — operating practices.

**Следующие шаги для изучения:**
1. Прочитать минимум 3 книги из списка: Clean Architecture, DDIA, Staff Engineer's Path.
2. Решить 50 LeetCode Hard задач (DP, graphs, monotonic stack).
3. Провести Event Storming для текущего проекта.
4. Записать ADR для последнего важного технического решения.
5. Сделать первый OSS contribution.
6. Подать CFP на конференцию.
7. Начать менторить junior разработчика.
