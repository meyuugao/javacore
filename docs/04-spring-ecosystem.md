# Раздел 4. Spring Ecosystem

> Spring — де-факто стандарт Java backend-разработки. Senior/Staff-инженер обязан знать Spring не как набор аннотаций, а как **architectural platform**: IoC-контейнер, AOP-инфраструктура, конфигурация, авто-конфигурация Boot, реактивный стек, data-доступ, транзакции, безопасность, наблюдаемость, облачные паттерны и миграции. Этот раздел — самый объёмный в roadmap: 20 подразделов, ~130 теоретических тезисов в каждом, реальные ссылки на docs.spring.io/spring.io/baeldung.com/reflectoring.io/Manning/O'Reilly, и многоступенчатые кейсы уровня production.
>
> **Базовые версии для раздела:** Spring Framework **6.1.x / 6.2.x**, Spring Boot **3.2.x / 3.3.x / 3.4.x**, Java **17 LTS** (минимально), Java **21 LTS** (рекомендовано). Jakarta EE 9+ (`jakarta.*` вместо `javax.*`). Native image через GraalVM (Spring Boot 3 native buildpacks / Native Build Tools).

---

## Содержание раздела

1. Spring Core / IoC / DI
2. Spring AOP
3. Spring Configuration
4. Spring Boot
5. Spring Web MVC
6. Spring WebFlux (Reactive)
7. Spring Data JPA
8. Spring Data (другие хранилища)
9. Spring TX (Transactions)
10. Spring Security
11. Spring Boot Actuator & Micrometer
12. Spring Cloud
13. Spring Batch
14. Spring Cache
15. Spring Messaging (JMS/AMQP/Kafka)
16. Spring Integration & Apache Camel
17. Spring Boot Test
18. Spring Modulith
19. Spring REST Docs / OpenAPI
20. Spring версии и миграции

---

## 1. Spring Core / IoC / DI

### Теория

- **IoC (Inversion of Control)** — инверсия управления: фреймворк вызывает код пользователя, а не наоборот. В Spring это выражается через DI-контейнер, который управляет жизненным циклом bean-ов и их зависимостями.
- **DI (Dependency Injection)** — реализация IoC: зависимости передаются в объект извне (конструктором, сеттером, полем), а не создаются внутри через `new`.
- **Bean** — объект, инстанциированный, собранный и управляемый Spring IoC-контейнером. По умолчанию bean-ы — синглтоны в рамках одного `ApplicationContext`.
- **BeanDefinition** — метаданные о том, как создать bean: имя класса, scope, lifecycle-колбэки, зависимости (`constructor arguments`, `property values`), `lazy-init`, `autowire mode`, `primary`, `dependsOn`. Загружаются из XML / `@Configuration` / Groovy / аннотаций через `BeanDefinitionReader`.
- **BeanFactory** — корневой интерфейс IoC-контейнера (`getBean`, `containsBean`, `isSingleton`, `getType`, `getAliases`). Это **ленивая** реализация: bean-ы создаются по запросу. `DefaultListableBeanFactory` — основная реализация.
- **ApplicationContext** — extends `BeanFactory`, добавляет: eager instantiation синглтонов (по умолчанию), `MessageSource` (i18n), `ResourceLoader`, `ResourcePatternResolver`, event publishing через `ApplicationEventPublisher`, environment через `EnvironmentCapable`.
- **Иерархия контекстов**: parent-child `ApplicationContext`. Ребёнок видит bean-ы родителя, но не наоборот. Используется в Spring MVC: root context (services/repositories) → dispatcher servlet context (controllers).
- **Встроенные реализации**: `ClassPathXmlApplicationContext`, `FileSystemXmlApplicationContext`, `AnnotationConfigApplicationContext`, `GenericWebApplicationContext`, `AnnotationConfigServletWebServerApplicationContext` (Boot сервлет-стек), `AnnotationConfigReactiveWebServerApplicationContext` (Boot реактивный стек), `GenericApplicationContext` (основа для Boot).
- **Bean lifecycle** — последовательность:
  1. Instantiation (`BeanDefinition` → `createBeanInstance()` — конструктор или factory-method).
  2. Populate properties (`populateBean()` — `@Autowired`, `@Value`, сеттеры, `PropertyValues`).
  3. `BeanNameAware.setBeanName(String)` — bean получает своё имя.
  4. `BeanClassLoaderAware.setBeanClassLoader(ClassLoader)`.
  5. `BeanFactoryAware.setBeanFactory(BeanFactory)` (или `ApplicationContextAware.setApplicationContext` — через `ApplicationContextAwareProcessor`).
  6. Другие `Aware`-колбэки: `EnvironmentAware`, `ResourceLoaderAware`, `ApplicationEventPublisherAware`, `MessageSourceAware`, `ServletContextAware` (web), `EmbeddedValueResolverAware`.
  7. `BeanPostProcessor.postProcessBeforeInitialization(Object bean, String beanName)` — вызывается **до** init-методов. Здесь работает `@Autowired`/`@Value` (через `AutowiredAnnotationBeanPostProcessor`), `@PostConstruct` (через `CommonAnnotationBeanPostProcessor`), JSR-250.
  8. `InitializingBean.afterPropertiesSet()` — стандартный init-колбэк (Spring interface).
  9. `@PostConstruct`-метод (фактически вызывается на шаге 7 через `CommonAnnotationBeanPostProcessor`, но логически — часть инициализации).
  10. Custom init method (`@Bean(initMethod = "...")` или `<bean init-method="...">`).
  11. `BeanPostProcessor.postProcessAfterInitialization(Object bean, String beanName)` — здесь создаётся **AOP-прокси** (через `AbstractAutoProxyCreator` / `AnnotationAwareAspectJAutoProxyCreator`).
  12. Bean готов к использованию.
  13. На закрытии контекста: `DisposableBean.destroy()` → `@PreDestroy` → custom destroy method (`@Bean(destroyMethod = "...")`).
- **BeanPostProcessor (BPP)** — стратегия кастомизации всех bean-ов контейнера. Это мощный, но опасный механизм: BPP-ы работают на _каждом_ bean. Важные BPP: `AutowiredAnnotationBeanPostProcessor`, `CommonAnnotationBeanPostProcessor`, `ConfigurationClassPostProcessor` (обрабатывает `@Configuration`), `AsyncAnnotationBeanPostProcessor`, `ScheduledAnnotationBeanPostProcessor`, `MethodValidationPostProcessor`, `AbstractAutoProxyCreator`.
- **BeanFactoryPostProcessor (BFPP)** — модифицирует `BeanDefinition`-ы **до** инстанциации bean-ов. `PropertySourcesPlaceholderConfigurer` / `PropertyResourceConfigurer` / `ConfigurationClassPostProcessor` — примеры. Запускаются в `invokeBeanFactoryPostProcessors()`.
- **Scopes**: `singleton` (по умолчанию, один экземпляр на контейнер), `prototype` (новый экземпляр на каждый `getBean`), `request` (HTTP request), `session` (HTTP session), `application` (ServletContext), `websocket` (WebSocket session). Кастомные scopes через `CustomScopeConfigurer` и реализацию `Scope` (`registerScope`, `get`, `remove`, `registerDestructionCallback`).
- **Scoped proxies**: при инъекции request/session-scoped bean-а в singleton создаётся прокси (`ScopedProxyMode.INTERFACES` = JDK dynamic proxy, `ScopedProxyMode.TARGET_CLASS` = CGLIB, `ScopedProxyMode.NO` = исключение). На вызове метода прокси делегирует к актуальному bean-у из `RequestContext` / `Session`.
- **Стереотипные аннотации**: `@Component` (базовый), `@Service` (сервисный слой, семантика), `@Repository` (DAO + автоматический перевод исключений через `PersistenceExceptionTranslationPostProcessor` в `DataAccessException`), `@Controller`/`@RestController` (web), `@Configuration` (конфигурация). Все они помечены `@Component` мета-аннотацией → подхватываются `@ComponentScan`.
- **@ComponentScan**: сканирует classpath на наличие стереотипных аннотаций. Параметры: `basePackages`/`basePackageClasses`, `includeFilters`/`excludeFilters` (5 типов: `ANNOTATION`, `ASSIGNABLE_TYPE`, `ASPECTJ`, `REGEX`, `CUSTOM`), `nameGenerator`, `scopeResolver`, `scopedProxy`, `resourcePattern`, `useDefaultFilters`.
- **@Autowired**: по умолчанию **by type**, обязателен (`required=true`). Если несколько кандидатов — по имени поля/параметра. Не находит → `NoSuchBeanDefinitionException` (если `required=true`). На конструкторе: с Spring 4.3 если у bean-а **один** конструктор, `@Autowired` не нужен.
- **@Qualifier**: уточняет инъекцию по имени/qualifier. Можно повесить на сам bean-класс (`@Qualifier("primaryDataSource")`) или на точку инъекции.
- **@Primary**: помечает bean как приоритетный при множественных кандидатах. Работает на уровне `BeanDefinition.isPrimary()`.
- **@Resource (JSR-250)**: по умолчанию **by name** (`name`), если не найдено — by type. Не Spring-specific.
- **@Inject (JSR-330)**: эквивалент `@Autowired` (by type). Требует `jakarta.inject` зависимости.
- **Constructor injection vs Field injection vs Setter injection**: constructor — иммутабельность, easy testability, fail-fast (видны циклические зависимости на старте), рекомендован Spring team. Field — кратко, но скрывает зависимости и ломает testability. Setter — опциональные зависимости.
- **@Profile**: помечает bean-ы активные только при определённом профиле (`-Dspring.profiles.active=prod`). Реализовано через `ProfileCondition` → `@Conditional`.
- **@Conditional / @ConditionalOn***: условное создание bean-а на основе `Condition.matches(ConditionContext, AnnotatedTypeMetadata)`. Семейство Boot: `@ConditionalOnClass`, `@ConditionalOnMissingBean`, `@ConditionalOnProperty`, `@ConditionalOnWebApplication`, `@ConditionalOnExpression`, `@ConditionalOnBean`, `@ConditionalOnResource`, `@ConditionalOnJava`, `@ConditionalOnCloudPlatform`.
- **@Lazy**: откладывает создание bean-а до первого использования. На точке инъекции позволяет обойти циклические зависимости (вместо исключения Spring вставит прокси). На `@Configuration` — все `@Bean` методы внутри становятся lazy.
- **@DependsOn**: гарантирует порядок создания bean-ов, не связанных напрямую инъекцией. Полезно для singleton-ов с side-effects (event listener-ы, JMX).
- **@Bean**: объявляет bean-метод внутри `@Configuration` или `@Component`. Параметры: `name`/`value`, `autowire` (deprecated), `initMethod`, `destroyMethod` (по умолчанию `infer` — автодетект `close`/`shutdown` метода), `scope`, `primary`, `lazy`.
- **Циклические зависимости**: 
  - Constructor injection → `BeanCurrentlyInCreationException` (Spring не может разрешить).
  - Field/setter injection → Spring использует **early reference** (трёхшаговый кэш: `singletonObjects`, `earlySingletonObjects`, `singletonFactories`) — но это **устаревший** механизм и был **отключён** по умолчанию в Spring 6.1 (`spring.main.allow-circular-references=false`). Лучше всегда рефакторить цикл.
  - `@Lazy` на одном конце цикла — обходной путь, но симптом design smell.
- **Proxy modes**:
  - **JDK Dynamic Proxy** — по умолчанию, если класс реализует интерфейс. Прокси реализует те же интерфейсы, делегирует через `InvocationHandler`. Не работает с `final`-методами и при приведении к конкретному классу.
  - **CGLIB** — по умолчанию для `@Configuration` и для классов без интерфейсов (Spring Boot 2.x+ — `spring.aop.proxy-target-class=true` по умолчанию). Создаёт подкласс целевого класса через bytecode generation. Не работает с `final` классами/методами.
  - Переключение через `@EnableAspectJAutoProxy(proxyTargetClass = true)` или `spring.aop.proxy-target-class`.
- **`@Configuration(proxyBeanMethods = false)`** — "lite" режим: `@Bean`-методы не перехватываются прокси, вызовы между методами создают **новые** экземпляры. Экономит память, но ломает меж-bean зависимости. Рекомендован для конфигураций, где все bean-ы изолированы.
- **`@Configuration` full mode** — CGLIB-прокси перехватывает вызовы `@Bean`-методов и возвращает singleton из контейнера. Поэтому `beanA()` из `beanB()` вернёт тот же экземпляр.
- **FactoryBean** — bean, который сам является фабрикой других bean-ов. `getObject()` возвращает целевой bean, `getObjectType()`, `isSingleton()`. Примеры: `SqlSessionFactoryBean`, `LocalContainerEntityManagerFactoryBean`, `JedisConnectionFactory`. Инъекция `&myFactoryBean` даёт сам `FactoryBean`, `myFactoryBean` — результат `getObject()`.
- **`BeanDefinitionRegistry`** — регистр `BeanDefinition`-ов. `registerBeanDefinition`, `removeBeanDefinition`. `DefaultListableBeanFactory` реализует и `BeanFactory`, и `BeanDefinitionRegistry`.
- **`ImportBeanDefinitionRegistrar`** — программируемая регистрация bean-ов. Используется в `@EnableScheduling`, `@EnableAsync`, `@EnableTransactionManagement`, `@EnableAspectJAutoProxy`, `@MapperScan` (MyBatis), Feign clients.
- **Events**: `ApplicationEvent` + `ApplicationListener<E>`. С Spring 4.2 — любой POJO метод с `@EventListener`. Синхронно по умолчанию, `@Async` — асинхронно. `@TransactionalEventListener(phase = AFTER_COMMIT|BEFORE_COMMIT|AFTER_COMPLETION|AFTER_ROLLBACK)`. Spring 6 — `ApplicationEventPublisher.publishEvent(Object)` принимает любой объект, не обязательно `ApplicationEvent` подкласс.
- **`@Autowired` на `Map<String, T>`** — инъекция всех bean-ов типа `T` по имени. На `List<T>` — все в порядке (по `@Order`/`Ordered`). На `Optional<T>` — опциональная инъекция. На `ObjectProvider<T>` — ленивая инъекция с `.getIfAvailable()`, `.forEach()`, `.iterator()`.
- **`ApplicationContextInitializer`** — программирует `ConfigurableApplicationContext` перед `refresh()`. Используется в Boot для environment post-processing, profiles.
- **`SmartInitializingSingleton`** — колбэк `afterSingletonsInstantiated()` после создания всех синглтонов. Используется для post-initialization логики (как `@EventListener(ContextRefreshedEvent)`, но чище).
- **`Lifecycle` / `SmartLifecycle`** — `start()`, `stop()`, `isRunning()`, `getPhase()`. Управление компонентами с жизненным циклом (TCP серверы, schedulers). Boot стартует их после refresh.

### Материалы

- **docs.spring.io — Spring Framework Reference, Core → IoC container** — `https://docs.spring.io/spring-framework/reference/core/beans.html` (базовая теория BeanFactory/ApplicationContext)
- **docs.spring.io — Bean Overview, lifecycle, scopes** — `https://docs.spring.io/spring-framework/reference/core/beans/definition.html`
- **docs.spring.io — Dependencies and configuration in detail** — `https://docs.spring.io/spring-framework/reference/core/beans/dependencies/factory-collaborators.html`
- **docs.spring.io — Bean scopes (singleton/prototype/web)** — `https://docs.spring.io/spring-framework/reference/core/beans/factory-scopes.html`
- **docs.spring.io — Customizing the nature of a bean (Lifecycle, Aware, BeanPostProcessor)** — `https://docs.spring.io/spring-framework/reference/core/beans/factory-nature.html`
- **docs.spring.io — Annotation-based container configuration** — `https://docs.spring.io/spring-framework/reference/core/beans/annotation-config.html`
- **docs.spring.io — Classpath scanning and managed components** — `https://docs.spring.io/spring-framework/reference/core/beans/classpath-scanning.html`
- **docs.spring.io — JSR-330 standard annotations (@Inject/@Named/@Provider)** — `https://docs.spring.io/spring-framework/reference/core/beans/standard-annotations.html`
- **docs.spring.io — Environment abstraction, profiles, property sources** — `https://docs.spring.io/spring-framework/reference/core/beans/environment.html`
- **Baeldung — Spring Bean Lifecycle** — `https://www.baeldung.com/spring-bean-lifecycle`
- **Baeldung — Spring @Autowired Guide** — `https://www.baeldung.com/spring-autowire`
- **Baeldung — Spring @Configuration vs @Component** — `https://www.baeldung.com/spring-configuration-annotation`
- **Baeldung — Spring circular dependencies** — `https://www.baeldung.com/circular-dependencies-in-spring`
- **Spring blog — Why constructor injection? by Olivier Drotbohm** — `https://spring.io/blog/2007/07/11/setter-injection-versus-constructor-injection-the-needle-and-the-thread/` (и обновлённая серия от Phil Webb)
- **Reflectoring — Spring Bean Lifecycle** — `https://reflectoring.io/spring-bean-lifecycle/`
- **Книга — "Pro Spring 6" (Walls/Schaefer/Harrop)** — apress.com, ISBN 978-1-4842-8640-1 (глубокий разбор IoC, BeanPostProcessor, конфигурации)
- **Книга — "Spring in Action, Sixth Edition" (Craig Walls)** — Manning, ISBN 978-1617297944 (базовая теория IoC/DI)
- **Исходники Spring Framework на GitHub** — `https://github.com/spring-projects/spring-framework` (`AbstractAutowireCapableBeanFactory.createBean`, `doCreateBean`, `populateBean`, `initializeBean`)

### Практика / Кейсы

1. **Полный lifecycle trace**: создать bean, реализующий все `Aware`-интерфейсы, `InitializingBean`, `DisposableBean`, с `@PostConstruct`/`@PreDestroy`, custom init/destroy, и кастомный `BeanPostProcessor`, который логирует каждый шаг. Запустить, вывести в лог последовательность колбэков. Замерить какой шаг выполняется первым. **Цель:** убедиться в порядке: `BeanNameAware → BeanFactoryAware → ApplicationContextAware → BPP:before → @PostConstruct → afterPropertiesSet → custom init → BPP:after → ... → @PreDestroy → destroy → custom destroy`.
2. **Циклическая зависимость с разными стратегиями**: создать `A` ↔ `B` (поле), запустить на Spring 6.1 (по умолчанию `allow-circular-references=false`) — получить исключение. Включить `spring.main.allow-circular-references=true` — проверить, что заработало. Переписать на constructor injection — получить исключение. Применить `@Lazy` на одной стороне. **Цель:** понять все сценарии и почему constructor injection форсирует clean design.
3. **Кастомный scope**: реализовать `TenantScope` (один bean-экземпляр на tenant-id из `ThreadLocal`), зарегистрировать через `CustomScopeConfigurer`, создать bean с `@Scope("tenant")` и `ScopedProxyMode.TARGET_CLASS`. Проверить что разные tenants получают разные экземпляры. **Цель:** глубоко понять scoped proxies и `Scope` контракт.
4. **BeanFactoryPostProcessor для подмены `@Value`**: написать `BFPP`, который сканирует все `BeanDefinition` и подменяет `${...}` placeholders на значения из кастомного secret manager (Vault). Сравнить с `PropertySourcesPlaceholderConfigurer` и `EnvironmentPostProcessor` в Boot. **Цель:** освоить BFPP vs BPP vs `EnvironmentPostProcessor`.
5. **`@Configuration` full vs lite**: создать конфиг с двумя `@Bean`, где один ссылается на другой. С `proxyBeanMethods=true` — подтвердить, что возвращается singleton. С `proxyBeanMethods=false` — подтвердить, что создаётся новый экземпляр при каждом вызове. Измерить memory footprint и startup time через `ApplicationStartup`. **Цель:** понять CGLIB-прокси `@Configuration`.
6. **Множественные `ApplicationContext` с parent-child**: поднять root context (сервисы/репозитории) и dispatcher servlet context (controllers), где controllers видят services, но не наоборот. Реализовать через `AbstractAnnotationConfigDispatcherServletInitializer`. **Цель:** понять иерархию контекстов в classic Spring MVC.
7. **JDK dynamic proxy vs CGLIB**: создать интерфейс `Service` и реализацию `ServiceImpl` с `final` методом. Активировать AOP-прокси. С `proxyTargetClass=false` — получить JDK proxy, вызвать `final` метод — он работает (т.к. в интерфейсе). С `proxyTargetClass=true` — CGLIB proxy, `final` метод **не перехватывается**. **Цель:** понять ограничения каждого proxy mode.
8. **`@Conditional` кастомное условие**: реализовать `@ConditionalOnPropertyNotEmpty`, `@ConditionalOnFreeMemoryGreaterThan(MB)`, `@ConditionalOnKubernetes`. Написать unit-тест для каждого `Condition` через `ConditionContext` mock. **Цель:** научиться писать кастомные условия для авто-конфигурации.
9. **Event-driven architecture**: создать domain event `OrderCreatedEvent`, publisher в `OrderService`, `@EventListener` в `EmailService` и `@Async @EventListener` в `AnalyticsService`, `@TransactionalEventListener(AFTER_COMMIT)` в `InventoryService`. Поднять `ApplicationEventMulticaster` с кастомным `TaskExecutor`. **Цель:** освоить все типы listener-ов и когда какой применять.
10. **`ObjectProvider` для опциональных bean-ов**: в авто-конфигурации использовать `ObjectProvider<DataSource>` и `ObjectProvider<List<DataSourcePoolMetadataProvider>>` для опциональных зависимостей. Использовать `.getIfAvailable(Supplier)`, `.getIfUnique()`, `.forEach()`. **Цель:** понять идиомы авто-конфигурации Spring Boot.
11. **`SmartLifecycle` для управления TCP-сервером**: реализовать `TcpServerLifecycle extends SmartLifecycle`, который стартует Netty-сервер после контекста и останавливает на shutdown. Настроить `getPhase()` так, чтобы он стартовал после `WebServerStartStopLifecycle`, но останавливался раньше. **Цель:** понять lifecycle фаз и порядок shutdown.
12. **`ImportBeanDefinitionRegistrar` для автоматической регистрации клиентов**: реализовать `@EnableApiClient(basePackages = "...")`, который сканирует интерфейсы с `@ApiClient` и регистрирует для каждого `FactoryBean`-прокси (как Feign). **Цель:** понять, как Feign/MyBatis работают внутри.

---

## 2. Spring AOP

### Теория

- **AOP (Aspect-Oriented Programming)** — парадигма для разделения cross-cutting concerns (логирование, транзакции, безопасность, кэширование, метрики) от бизнес-логики. Spring AOP — реализация на runtime-прокси.
- **Aspect** — модуль, объединяющий pointcut + advice. В Spring реализуется классом с `@Aspect` (AspectJ-style).
- **Join Point** — точка выполнения программы. В Spring AOP join points **только на уровне методов** (execution). AspectJ поддерживает field/constructor/static initializer.
- **Pointcut** — предикат, какие join points перехватывать. Spring использует **AspectJ pointcut expression language**.
- **Pointcut designators** в Spring AOP:
  - `execution(modifiers-pattern? ret-type-pattern declaring-type-pattern?name-pattern(param-pattern) throws-pattern?)` — основной designator, например `execution(public * com.acme.service..*.*(..))`.
  - `within(com.acme.service..*)` — ограничение по типу (все методы внутри типов).
  - `this(com.acme.Foo)` — bean-прокси реализует интерфейс `Foo`.
  - `target(com.acme.Foo)` — целевой объект реализует `Foo`.
  - `args(java.io.Serializable, ..)` — аргументы метода соответствуют типам.
  - `@annotation(com.acme.Traceable)` — метод помечен аннотацией.
  - `@within(com.acme.Traceable)` — тип целевого объекта помечен аннотацией.
  - `@target(com.acme.Traceable)` — runtime class объекта помечен аннотацией.
  - `@args(com.acme.Traceable)` — тип runtime аргумента помечен аннотацией.
  - `bean(nameOrPattern)` — Spring-специфичный, по имени bean-а (например `bean(orderService)` или `bean(*Service)`).
- **Advice types**:
  - **`@Before`** — до выполнения метода. Не может изменить return value, но может выбросить исключение.
  - **`@After`** (finally) — выполняется всегда после метода (как `finally` блок), даже при исключении.
  - **`@AfterReturning`** — после нормального возврата. Может читать/модифицировать return value через `returning` атрибут. Не вызывается при исключении.
  - **`@AfterThrowing`** — после выброса исключения. Через `throwing` атрибут binds исключение. Не вызывает при нормальном возврате. Не может «проглотить» исключение (нельзя его отменить).
  - **`@Around`** — оборачивает метод. Контроль вызова через `ProceedingJoinPoint.proceed()`. Может менять аргументы, return value, перехватывать исключения. Самый мощный, но самый дорогой advice.
- **`JoinPoint` API** (доступно во всех advice, кроме `@Around` который использует `ProceedingJoinPoint`): `getSignature()` (имя метода, тип, declaring type), `getArgs()`, `getTarget()`, `getThis()` (прокси), `getStaticPart()`, `getSourceLocation()`.
- **`ProceedingJoinPoint`** extends `JoinPoint`: `proceed()`, `proceed(Object[] args)` — для подмены аргументов.
- **Pointcut composition**: `&&`, `||`, `!`. Можно переиспользовать через `@Pointcut`-методы: `@Pointcut("execution(public * *(..))") public void anyPublicOperation() {}` и далее `@Before("anyPublicOperation() && @annotation(trace)")`.
- **Weaving** — процесс внедрения aspect в целевой код:
  - **Runtime weaving (Spring AOP)** — через JDK dynamic proxy / CGLIB. Прокси создаётся при создании bean-а (`AbstractAutoProxyCreator.postProcessAfterInitialization`). Aspect — это Spring bean.
  - **Compile-time weaving (AspectJ CTW)** — AspectJ compiler (`ajc`) вплетает аспекты в `.class` файлы при компиляции. Работает с field/constructor join points.
  - **Load-time weaving (AspectJ LTW)** — weaving при загрузке классов через Java agent (`-javaagent:aspectjweaver.jar`). Spring поддерживает через `@EnableLoadTimeWeaving` и `META-INF/aop.xml`.
- **`@EnableAspectJAutoProxy`**: включает AspectJ-стиль аннотаций. Параметры: `proxyTargetClass` (true = CGLIB), `exposeProxy` (true = `AopContext.currentProxy()` доступен внутри метода для self-invocation через прокси).
- **Performance implications**: каждый advice добавляет накладные. `@Around` — самый тяжёлый (вызов `proceed` через reflection). На горячих путях могут быть микросекунды. AspectJ CTW/LTW — на порядок быстрее (нет прокси). Тестирование: JMH-бенчмарки для критичных участков.
- **`@DeclareParents`** (introduction) — добавляет интерфейс в существующий класс. Дает "mixin" возможность. `@DeclareParents(value="com.acme..*", defaultImpl=DefaultUsageTracked.class) UsageTracked mixin;`
- **`@Aspect`-бины**: должны быть в контейнере (`@Component` поверх `@Aspect`). Не являются обычными bean-ами для автопрокси (Spring исключает их из авто-прокси через `BeanFactoryAspectJAdvisorsFactory`).
- **Order advice** через `@Order` / `Ordered`: при нескольких аспектах на одном join point порядок: `@Around(before) → @Before → [target method] → @AfterReturning/@AfterThrowing → @After → @Around(after)`. Между аспектами — по `@Order` (меньше = раньше в stack, позже в unwind).
- **CGLIB limitations**: `final` классы не могут быть проксированы (исключение). `final`/`static`/`private` методы не перехватываются. Конструкторы CGLIB-прокси вызываются дважды (один раз — для самого прокси, не через Spring).
- **Self-invocation problem**: если метод `A` вызывает метод `B` того же bean-а напрямую (`this.b()`), то `B` выполняется на **target** объекте, **не через прокси** → аспект на `B` не сработает. Решения: инжектить self-proxy (`@Autowired private MyService self`), `AopContext.currentProxy()` (с `exposeProxy=true`), `@Resource` self-reference, вынести `B` в другой bean.
- **`IntroductionAdvisor` / `DelegatePerTargetObjectIntroductionInterceptor`** — реализация introduction через Spring API (альтернатива `@DeclareParents`).
- **`ProxyFactory`** — программный AOP: `ProxyFactory pf = new ProxyFactory(target); pf.addAdvice(new MethodInterceptor() {...}); MyService proxy = (MyService) pf.getProxy();`. Полезно для unit-тестов и библиотек.
- **`Advisor` = Pointcut + Advice**: `PointcutAdvisor` (`DefaultPointcutAdvisor`, `NameMatchMethodPointcutAdvisor`, `RegexpMethodPointcutAdvisor`), `IntroductionAdvisor`. Spring автоматически регистрирует `Advisor`-ы как bean-ы.
- **`@AspectJ` XML-стиль**: `<aop:config>`/`<aop:aspect>`/`<aop:pointcut>`/`<aop:before>` — legacy XML подход. В современных проектах не используется.
- **AOT compatibility** (Spring 6 / Boot 3 native): AOP-прокси генерируются в AOT-фазе (через `BeanFactoryInitializationAotContribution`), нет runtime reflection — критично для GraalVM.

### Материалы

- **docs.spring.io — Spring AOP Reference** — `https://docs.spring.io/spring-framework/reference/core/aop.html`
- **docs.spring.io — @AspectJ support (pointcut, advice)** — `https://docs.spring.io/spring-framework/reference/core/aop/ataspectj.html`
- **docs.spring.io — Schema-based AOP (XML)** — `https://docs.spring.io/spring-framework/reference/core/aop/schema.html`
- **docs.spring.io — API ProxyFactory, Advisor** — `https://docs.spring.io/spring-framework/reference/core/aop-api.html`
- **Baeldung — Introduction to Spring AOP** — `https://www.baeldung.com/spring-aop`
- **Baeldung — Spring AOP pointcut tutorial** — `https://www.baeldung.com/spring-aop-pointcut-tutorial`
- **Baeldung — Spring AOP advice types** — `https://www.baeldung.com/spring-aop-advice-tutorial`
- **Baeldung — Spring AOP vs AspectJ** — `https://www.baeldung.com/spring-aop-vs-aspectj`
- **Baeldung — AspectJ — advise methods** — `https://www.baeldung.com/aspectj-advise-methods`
- **Reflectoring — Spring AOP and the proxy pattern** — `https://reflectoring.io/spring-aop/`
- **Eclipse AspectJ documentation** — `https://www.eclipse.org/aspectj/doc/released/progguide/`
- **Spring blog — AOP at SpringOne 2012 (Adrian Colyer)** — `https://spring.io/blog/2012/01/06/springone-2-0-2012-aop/`
- **Книга — "Pro Spring 6", Chapter 6: Cross-cutting concerns with Spring AOP** — Apress, ISBN 978-1484286401
- **Книга — "AspectJ in Action, Second Edition" (Ramnivas Laddad)** — Manning (глубокий reference по AspectJ)

### Практика / Кейсы

1. **`@Around` для измерения времени + trace-id**: аспект, который оборачивает все публичные методы сервисов, пишет в MDC `traceId` (из Sleuth/Micrometer Tracing), логирует вход/выход с аргументами (через Jackson, без sensitive полей), замеряет длительность, публикует metric в Micrometer. Покрыть тестами через `ProxyFactory` и unit-тесты advice. **Цель:** освоить `@Around` + `ProceedingJoinPoint`.
2. **Кастомная аннотация `@Retryable` через AOP**: реализовать аспект `@Retry(maxAttempts = 3, backoff = @Backoff(delay = 100, multiplier = 2))` для методов, выбрасывающих указанные исключения. Сравнить с Spring Retry (`@Retryable` из spring-retry). **Цель:** глубоко понять pointcut на `@annotation` + `@Around`.
3. **Self-invocation demo**: класс с двумя публичными методами `A` и `B`, аспект на обоих. Из `A` вызвать `this.B()` — аспект не сработает. Решить тремя способами: self-proxy инъекция, `AopContext.currentProxy()`, вынос `B` в другой bean. Написать unit-тест, проверяющий срабатывание. **Цель:** глубоко понять self-invocation.
4. **`@DeclareParents` (introduction)**: добавить всем bean-ам с `@Service` интерфейс `Auditable` с дефолтной реализацией, который хранит last-modified timestamp. Проверить, что service можно кастовать к `Auditable`. **Цель:** освоить introduction.
5. **Load-time weaving с AspectJ**: для domain-классов JPA (`@Entity`) нельзя использовать Spring AOP (не Spring beans). Использовать LTW для трассировки setter-ов на entity. Настроить `@EnableLoadTimeWeaving`, `META-INF/aop.xml`, `-javaagent:aspectjweaver.jar`. **Цель:** понять, когда Spring AOP недостаточен и нужен AspectJ LTW.
6. **JMH benchmark @Around**: замерить накладные расходы совета `@Around` для метода, который ничего не делает (`noOp()`). Сравнить: без аспекта, с аспектом-пустышкой, с аспектом-с-логированием, с AspectJ CTW. Записать разницу в наносекундах на вызов. **Цель:** quantify AOP cost.
7. **`@Conditional` для feature flag через AOP**: аспект `@FeatureFlag("new-algo")`, который проверяет feature flag (через `FeatureManager` Togglz/OpenFeature) и либо вызывает target method, либо fallback. Покрыть unit-тестами обе ветки. **Цель:** AOP для product features.
8. **Multi-aspect ordering**: создать 3 аспекта (`@Order(1) SecurityAspect`, `@Order(2) TransactionAspect`, `@Order(3) LoggingAspect`) на одном методе. Протестировать порядок через логи. Объяснить почему `@Order` меньшее число — outer (вызывается раньше). **Цель:** понять advice-стек.
9. **`MethodInterceptor` через `ProxyFactory`**: написать библиотеку для кэширования без `@Cacheable` через программный AOP. Использовать `NameMatchMethodPointcutAdvisor`. **Цель:** низкоуровневый API.

---

## 3. Spring Configuration

### Теория

- **`@Configuration`** — класс-конфигурация, который содержит `@Bean`-методы. Spring создаёт CGLIB-подкласс для перехвата вызовов `@Bean`-методов (full mode), гарантируя singleton-семантику при меж-bean зависимостях.
- **Full vs Lite `@Configuration`**:
  - **Full** (`@Configuration` без параметров, по умолчанию `proxyBeanMethods=true`): CGLIB-прокси перехватывает вызовы `@Bean`-методов, возвращает singleton из контейнера.
  - **Lite** (`@Configuration(proxyBeanMethods=false)` или `@Component`/`@Controller`/`@Service` с `@Bean`-методами): нет прокси, вызов `beanA()` из `beanB()` создаёт **новый** экземпляр. Меньше memory, быстрее startup, несовместимо с меж-bean зависимостями через method calls (но работает через параметры метода).
  - Boot рекомендует `proxyBeanMethods=false` для авто-конфигураций (с Spring Boot 2.2+) — экономит ~10-15% startup time.
- **`@Import`**: импортирует другие `@Configuration`-классы (или `ImportSelector`/`ImportBeanDefinitionRegistrar` реализации). Аналог `<import/>` в XML. Позволяет строить модульные конфигурации.
- **`@ComponentScan`**: сканирует classpath на стереотипные аннотации. В Boot активируется `@SpringBootApplication` → `@ComponentScan` от package главного класса. Параметры: `basePackages`, `basePackageClasses`, `includeFilters`, `excludeFilters`, `nameGenerator`, `scopeResolver`, `scopedProxy`, `useDefaultFilters` (по умолчанию true — сканирует `@Component`, `@Repository`, `@Service`, `@Controller`, `@Configuration`, `@ControllerAdvice`).
- **`@PropertySource`**: добавляет `PropertySource` (файл `properties`/`XML`) в `Environment`. `@PropertySource("classpath:application.properties")`. С Spring 5.1 — опциональный `ignoreResourceNotFound`. **Не работает с YAML напрямую** — нужен `YamlPropertiesFactoryBean` через `@Bean`.
- **`@PropertySources`**: контейнер для нескольких `@PropertySource`.
- **`Environment`** abstraction:
  - `MutablePropertySources` — упорядоченный список `PropertySource`-ов.
  - PropertySource order (Boot): command-line args > `spring.application.json` > ServletConfig/ServletContext init params > JNDI > Java system properties > OS environment variables > `RandomValuePropertySource` > application-{profile}.yml/properties вне jar > application.yml/properties вне jar > application-{profile}.yml/properties в jar > application.yml/properties в jar > `@PropertySource` > default properties.
  - `Environment.getProperty(key, Class<T>, defaultValue)` — с конвертацией типа через `ConversionService`.
  - Active profiles: `spring.profiles.active`, default profiles: `spring.profiles.default` (по умолчанию "default").
  - Profile groups (Spring 5.1+): `spring.profiles.group.prod=proddb,prodmetrics`.
- **`@Value`**: инъекция отдельного свойства. `"${my.prop}"` — placeholder (resolver через `PropertySourcesPlaceholderConfigurer`); `"#{...}"` — SpEL expression. Можно дефолтное значение: `"${my.prop:default}"`. Поддерживает конвертацию типа. Работает через `AutowiredAnnotationBeanPostProcessor`.
- **SpEL (Spring Expression Language)**:
  - `#{systemProperties['user.region']}` — доступ к system properties.
  - `#{T(java.lang.Math).random() * 100}` — static method call.
  - `#{beanName.fieldName}` — доступ к полю bean-а.
  - `#{user.email matches '^[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$'}` — regex.
  - `#{systemProperties['user.region'] ?: 'US'}` — Elvis operator.
  - Безопасность: SpEL — это injection вектор, если exposes user input.
- **`@ConfigurationProperties`** — type-safe binding группы свойств к POJO. Префикс: `@ConfigurationProperties(prefix = "app.mail")`. Требует `@EnableConfigurationProperties(MailProps.class)` или `@ConfigurationPropertiesScan` (Spring Boot 2.2+). Properties:
  - Class должен быть `public` (или record в Boot 3 с constructor binding).
  - Getter/setter (JavaBean style) **или** constructor binding (Boot 2.2+, требует `@ConstructorBinding` до 3.0, после — авто-детект).
  - Поддерживает nested, `Map`, `List`, `Set`, `Duration`, `Period`, `DataSize`, `InetAddress`, enums, primitives.
  - Validation: `@Validated` на классе + Bean Validation constraints на полях.
  - `@ConfigurationPropertiesBinding` — кастомный `Converter<String, T>` для своих типов.
- **`@ConfigurationPropertiesBinding`** — помечает `Converter` для применения ко всем `@ConfigurationProperties` (а не только к одному). Должен быть зарегистрирован как bean.
- **`Binder` API** (programmatic): `Binder.get(environment).bind("app.mail", MailProps.class)` или `bind("app.port", Integer.class).orElse(8080)`. Полезно в `EnvironmentPostProcessor` и для условной логики.
- **`@EnableConfigurationProperties`**: регистрирует `@ConfigurationProperties`-классы как bean-ы. Список классов в `value`.
- **`@ConfigurationPropertiesScan`** (Boot 2.2+): автоматически находит `@ConfigurationProperties`-классы через classpath scan (как `@ComponentScan`, но для props).
- **Profiles**:
  - `@Profile("dev")` на bean-методе или `@Configuration`-классе — активен только при active profile `dev`.
  - `@Profile("!prod")` — негативный match.
  - Profile expression (Spring 5.1+): `@Profile("prod & west-eu")`, `@Profile("(dev | staging) & !local")`.
  - В YAML: `spring.config.activate.on-profile: prod` (Spring Boot 2.4+).
- **Externalized config в Boot**: файлы `application.{properties|yml}`, `application-{profile}.{properties|yml}`, расположенные в: `./config/`, `./`, classpath `/config/`, classpath `/`. Порядок (внешние перекрывают внутренние). Можно указать через `spring.config.location` (полная замена) или `spring.config.additional-location` (добавление).
- **Config import** (Spring Boot 2.4+): `spring.config.import=optional:file:./my-config.yaml,optional:configtree:/etc/config/`. Поддерживает `configtree:` (file-based config из Kubernetes ConfigMap), `optional:` (не fail если не существует).
- **Config data API** (Spring Boot 2.4+): `ConfigDataLocation`, `ConfigDataLoader`, `ConfigDataResource`. Заменяет старый `EnvironmentPostProcessor`-based подход для некоторых случаев. Позволяет сторонние форматы (consul, vault).
- **`@ConditionalOnProperty`**: создание bean-а на основе свойства. `prefix`, `name`, `havingValue`, `matchIfMissing`. Часто используется в авто-конфигурации Boot.
- **`PropertySource` ordering в Boot**: cmd line > `RandomValuePropertySource` > OS env > `application.yml` (вне jar) > `application.yml` (в jar). Это означает, что env vars переопределяют application.yml. **Важно для Docker/K8s** deployments.
- **`ConfigurableEnvironment`**: `MutablePropertySources getPropertySources()`, `addFirst()`, `addLast()`, `addBefore()`, `addAfter()` — управление порядком. `setActiveProfiles()`, `setDefaultProfiles()`, `addActiveProfile()`.
- **`EnvironmentPostProcessor`** — программирует environment **до** autoconfig. Примеры: `ConfigTreeLoader`, `RandomValuePropertySourceEnvironmentPostProcessor`. Кастомный: реализовать интерфейс, зарегистрировать в `META-INF/spring.factories` (`org.springframework.boot.env.EnvironmentPostProcessor=...`), в Boot 3+ — `META-INF/spring/org.springframework.boot.env.EnvironmentPostProcessor.imports`.
- **Spring Cloud Config** — внешний source. `spring.config.import=optional:configserver:http://localhost:8888`. Доступ к конфигам через Git/JDBC/Vault backend.

### Материалы

- **docs.spring.io — Spring Framework, Java-based container configuration** — `https://docs.spring.io/spring-framework/reference/core/beans/java.html`
- **docs.spring.io — `@Configuration` full vs lite** — `https://docs.spring.io/spring-framework/reference/core/beans/java/configuration-annotation.html`
- **docs.spring.io — Environment abstraction** — `https://docs.spring.io/spring-framework/reference/core/beans/environment.html`
- **docs.spring.io — BeanWrapper, type conversion** — `https://docs.spring.io/spring-framework/reference/core/validation/beans-beanconversion.html`
- **docs.spring.io — Spring Boot Externalized Configuration** — `https://docs.spring.io/spring-boot/reference/features/external-config.html`
- **docs.spring.io — Spring Boot features → Externalized config order** — `https://docs.spring.io/spring-boot/how-to/properties-and-configuration.html`
- **docs.spring.io — `@ConfigurationProperties` (Type-safe Configuration Properties)** — `https://docs.spring.io/spring-boot/reference/features/external-config.html#features.external-config.typesafe-configuration-properties`
- **docs.spring.io — Spring Boot application.properties/yml appendix** — `https://docs.spring.io/spring-boot/appendix/application-properties/index.html`
- **Baeldung — A Guide to @ConfigurationProperties in Spring Boot** — `https://www.baeldung.com/configuration-properties-in-spring-boot`
- **Baeldung — Properties with Spring** — `https://www.baeldung.com/properties-with-spring`
- **Baeldung — Spring @Value vs @ConfigurationProperties** — `https://www.baeldung.com/spring-value-vs-configurationproperties`
- **Baeldung — Spring Profiles** — `https://www.baeldung.com/spring-profiles`
- **Baeldung — Spring SpEL** — `https://www.baeldung.com/spring-expression-language`
- **Reflectoring — Spring Boot ConfigurationProperties** — `https://reflectoring.io/spring-boot-configuration-properties/`
- **Spring blog — Config data API in Spring Boot 2.4** — `https://spring.io/blog/2020/08/14/config-file-processing-in-spring-boot-2-4`
- **Книга — "Spring Boot in Action" (Craig Walls)** — Manning, ISBN 978-1617292545 (хоть и старая, основы external config хорошо изложены)
- **Книга — "Pro Spring 6", Chapter 4: IOC in Spring (Configuration)** — Apress

### Практика / Кейсы

1. **Type-safe config + validation**: `MailProps` с `host`, `port`, `username`, `password` (через `@ConfigurationProperties("app.mail")`), валидация `@HostValid`, `@Min(1) @Max(65535) port`, `@NotBlank` для кредов в `prod` профиле (через group). Покрыть тестом через `@ConfigurationPropertiesScan` + `@SpringBootTest`. **Цель:** освоить `@ConfigurationProperties` + Bean Validation.
2. **Custom `Converter` для `@ConfigurationPropertiesBinding`**: реализовать `String → Money` (BigDecimal + Currency), `String → Crn` (custom type). Зарегистрировать как `@ConfigurationPropertiesBinding`. Покрыть unit-тестами `Converter`. **Цель:** понять binding extensions.
3. **`EnvironmentPostProcessor` для Vault**: реализовать `EnvironmentPostProcessor`, который до autoconfig подгружает секреты из HashiCorp Vault и добавляет их как `PropertySource` в начало (перезаписывая application.yml). Зарегистрировать в `spring.factories`. **Цель:** понять external config customization.
4. **`configtree` для Kubernetes**: задеплоить app в K8s, где ConfigMap монтируется в `/etc/config/app/` как файлы (один файл на property). Использовать `spring.config.import=optional:configtree:/etc/config/`. Проверить что `app.database.url` подхватывается из `database/url` файла. **Цель:** понять configtree.
5. **SpEL в `@Value`**: `@Value("#{T(java.time.Instant).now().toEpochMilli()}")` — startup timestamp. `@Value("#{environment['APP_REGION'] ?: 'eu-west-1'}")`. `@Value("#{@cacheManager.getCache('users').size()}")` — сложный SpEL. **Цель:** освоить SpEL выражения в конфигурации.
6. **Multi-document YAML + profiles**: один `application.yml` с несколькими документами, каждый с `spring.config.activate.on-profile`. Протестировать, что активация разных профилей даёт разные биндинги. Использовать profile groups (`spring.profiles.group.prod=proddb,prodmetrics`). **Цель:** понять multi-document YAML.
7. **`Binder` API для условной логики**: в `@Configuration`-классе использовать `Binder.get(env).bind("app.features", Features.class).orElseGet(Features::default)` для условной регистрации bean-ов. **Цель:** programmatic binding.
8. **Custom `PropertySource` из `etcd`/Consul KV**: реализовать `PropertySource<?>`, который читает ключи из Consul KV, и `EnvironmentPostProcessor` для регистрации. Обработать refresh через `@Scheduled` + `ConfigurableEnvironment.getPropertySources().replace()`. **Цель:** глубоко понять property source ordering.

---

## 4. Spring Boot

### Теория

- **Spring Boot** — opinionated надстройка над Spring Framework: convention over configuration, авто-конфигурация, embedded servers, production-ready features (actuator), внешняя конфигурация, метрики.
- **`@SpringBootApplication`** = `@SpringBootConfiguration` + `@EnableAutoConfiguration` + `@ComponentScan` (на package главного класса). Эквивалентно `@Configuration` + `@EnableAutoConfiguration` + `@ComponentScan`.
- **Auto-configuration**:
  - Активируется через `@EnableAutoConfiguration` (входит в `@SpringBootApplication`).
  - Реализация: `AutoConfigurationImportSelector` читает `META-INF/spring/org.springframework.boot.autoconfigure.AutoConfiguration.imports` (Boot 2.7+, заменил `META-INF/spring.factories` для авто-конфигураций).
  - Каждая авто-конфигурация — `@Configuration`-класс с `@Conditional*`-аннотациями.
  - Disable: `@SpringBootApplication(exclude = DataSourceAutoConfiguration.class)` или `spring.autoconfigure.exclude` property.
  - Исключить по имени: `spring.autoconfigure.exclude[0]=org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration`.
- **`AutoConfiguration.imports`** (Boot 2.7+): одна строка — один FQN-класс авто-конфигурации. Замена `spring.factories` (который теперь используется только для `EnvironmentPostProcessor`, `ApplicationListener`, `ApplicationContextInitializer` и т.д.).
- **`spring.factories`** (legacy, до Boot 2.7): properties-файл с ключами-типами и значениями-классами. Для autoconfig теперь deprecated, но ещё используется для non-autoconfig целей.
- **`@AutoConfiguration`** (Boot 2.7+): специальная аннотация для авто-конфигураций (extends `@Configuration`). Параметры `before`, `beforeName`, `after`, `afterName` для управления порядком (через `AutoConfigureBefore`/`AutoConfigureAfter`/`AutoConfigureOrder`).
- **Starters**: набор dependency descriptors, который включает все нужные зависимости для функционала. `spring-boot-starter-web`, `spring-boot-starter-data-jpa`, `spring-boot-starter-actuator`, `spring-boot-starter-test`, `spring-boot-starter-security`, и т.д. Spring Cloud starters: `spring-cloud-starter-netflix-eureka-client`, и т.д.
- **Embedded servers**:
  - **Tomcat** (по умолчанию) — `spring-boot-starter-tomcat` (входит в `spring-boot-starter-web`).
  - **Jetty** — исключить Tomcat starter, добавить `spring-boot-starter-jetty`.
  - **Undertow** — исключить Tomcat, добавить `spring-boot-starter-undertow` (часто самый быстрый по throughput).
  - **Netty** (для реактивного стека) — в `spring-boot-starter-webflux`.
  - Конфигурация: `server.port`, `server.servlet.context-path` (servlet), `server.http2.enabled`, `server.compression.enabled`, `server.ssl.*`, `server.tomcat.*`, `server.jetty.*`, `server.undertow.*`.
- **Externalized configuration order** (от высшего приоритета к низшему):
  1. Devtools global settings (`$HOME/.config/spring-boot`).
  2. `@TestPropertySource` на тесте.
  3. Properties-атрибут `@SpringBootTest` и других test-аннотаций.
  4. Command-line args (`--app.name=...`).
  5. `SPRING_APPLICATION_JSON` (inline JSON в env var).
  6. `ServletConfig` init parameters.
  7. `ServletContext` init parameters.
  8. JNDI attributes (`java:comp/env`).
  9. Java System properties (`System.getProperty`).
  10. OS environment variables.
  11. `RandomValuePropertySource` (`random.*`).
  12. Profile-specific application properties **вне** packaged jar (`application-{profile}.yml`).
  13. Application properties **вне** packaged jar (`application.yml`).
  14. Profile-specific application properties **внутри** packaged jar.
  15. Application properties **внутри** packaged jar.
  16. `@PropertySource` на `@Configuration`-классах.
  17. Default properties (`SpringApplication.setDefaultProperties`).
- **Actuator** endpoints (Boot 3.x):
  - `health` — health check (liveness/readiness probes).
  - `info` — app info (build, git, custom).
  - `metrics` — Micrometer metrics с тегами.
  - `env` — environment properties (с маскировкой секретов).
  - `loggers` — runtime log level changes.
  - `beans` — все bean-ы контекста.
  - `configprops` — все `@ConfigurationProperties`.
  - `mappings` — все HTTP-маппинги (servlet + reactive).
  - `threaddump` — JVM thread dump.
  - `heapdump` — JVM heap dump (.hprof).
  - `caches` — управление кэшами.
  - `sessions` — Spring Session management.
  - `shutdown` (disabled по умолчанию) — graceful shutdown.
  - `prometheus` — Prometheus scrape endpoint (если micrometer-registry-prometheus на classpath).
  - `scheduledtasks` — `@Scheduled` задачи.
  - `startup` — startup steps (ApplicationStartup).
  - `quartz` — Quartz scheduler (если есть).
- **`@Endpoint`** — кастомный actuator-эндпоинт. `@ReadOperation`, `@WriteOperation`, `@DeleteOperation`. Технология-agnostic (работает и на servlet, и на reactive).
- **Devtools** — development experience: automatic restart на classpath change (через 2 classloader: base + restart), LiveReload, cache-disable для template engines. Не включается в production (по умолчанию disabled при fat jar run). `spring.devtools.restart.enabled`.
- **Banner**: `banner.txt` в classpath root. Переменные: `${spring-boot.version}`, `${application.title}`, `${application.version}`, `${AnsiColor.RED}`. Отключение: `spring.main.banner-mode=off`.
- **Spring Boot Maven plugin**:
  - `spring-boot:run` — запуск app.
  - `spring-boot:repackage` — создание fat jar / layered jar.
  - `spring-boot:build-image` — Cloud Native Buildpacks (Paketo) → Docker image.
  - `spring-boot:build-info` — генерация `META-INF/build-info.properties` для actuator `/info`.
- **Spring Boot Gradle plugin**: `bootRun`, `bootJar`, `bootBuildImage`, `bootBuildInfo`.
- **Layered jars** (Boot 2.3+): fat jar разбивается на слои — `dependencies`, `spring-boot-loader`, `snapshot-dependencies`, `application`. Каждый слой — отдельная директория в jar. Позволяет Docker-кэшировать слои с зависимостями отдельно от кода → быстрые rebuilds.
  - Извлечь слои: `java -Djarmode=layertools -jar app.jar extract`.
  - Конфигурация: `spring-boot.layered.enabled=true` (по умолчанию true в Boot 2.4+) + `layers.xml`.
- **GraalVM Native Image** (Boot 3+):
  - Spring AOT engine генерирует GraalVM metadata во время build.
  - Maven: `mvn -Pnative native:compile` (через `org.graalvm.buildtools:native-maven-plugin`).
  - Gradle: `./gradlew nativeCompile`.
  - Buildpacks: `spring-boot:build-image -Pnative` (Paketo Bellsoft Liberica).
  - Ограничения: reflection (частично через hints), dynamic proxies (только JDK), resources, classpath scanning. Нужно писать `RuntimeHints` через `RuntimeHintsRegistrar`.
  - Memory footprint: ~50-100MB вместо ~300-500MB JVM. Startup: ~30-100ms вместо ~1-3s.
- **`@SpringBootConfiguration`** — то же что `@Configuration`, но специфично для Boot (для тестов, чтобы найти главный конфиг).
- **SpringApplication run sequence**:
  1. Создание `SpringApplication` instance (через `SpringApplication.run` или builder).
  2. `SpringApplicationRunListeners.starting()` — `ApplicationStartingEvent`.
  3. Загрузка `Environment` (`ApplicationEnvironmentPreparedEvent`).
  4. Создание `ApplicationContext`.
  5. `prepareContext` (`ApplicationContextInitializedEvent`, `ApplicationPreparedEvent`).
  6. `refreshContext` (полный Spring refresh).
  7. `afterRefresh` — `CommandLineRunners` и `ApplicationRunner` (`ApplicationStartedEvent` перед ними, `ApplicationReadyEvent` после).
- **`ApplicationRunner` vs `CommandLineRunner`**: оба запускаются после старта контекста. `ApplicationRunner` принимает `ApplicationArguments` (parsed), `CommandLineRunner` — raw `String... args`. Order через `@Order`.
- **Conditional family for autoconfiguration**: `@ConditionalOnClass`, `@ConditionalOnMissingClass`, `@ConditionalOnBean`, `@ConditionalOnMissingBean`, `@ConditionalOnProperty`, `@ConditionalOnResource`, `@ConditionalOnWebApplication` (and `@ConditionalOnNotWebApplication`), `@ConditionalOnExpression`, `@ConditionalOnJava`, `@ConditionalOnCloudPlatform` (Cloud Foundry, Kubernetes, Heroku, etc.), `@ConditionalOnAvailableEndpoint`.
- **`@AutoConfigureOrder` / `@AutoConfigureBefore` / `@AutoConfigureAfter`** — управление порядком autoconfig (аннотации на самих авто-конфигурациях).
- **`SpringBootCondition`** — базовый класс для кастомных `Condition`-ов с logging-ом match/no-match (видно в `--debug` mode).
- **Logging**: по умолчанию Logback + Bridge-и для JUL, Log4j, Apache Commons Logging. Конфигурация `logback-spring.xml` (с `<springProfile>` support). Альтернативы через `spring-boot-starter-log4j2`.
- **Banner, hooks, exit codes**: `SpringApplicationBuilder` для fluent API. `ExitCodeGenerator` для кодов возврата. `SpringApplicationExit` в Boot 3.2+.

### Материалы

- **docs.spring.io — Spring Boot Reference Documentation** — `https://docs.spring.io/spring-boot/reference/`
- **docs.spring.io — Spring Boot Features → Auto-configuration** — `https://docs.spring.io/spring-boot/reference/using/auto-configuration.html`
- **docs.spring.io — Creating Your Own Auto-configuration** — `https://docs.spring.io/spring-boot/reference/features/developing-auto-configuration.html`
- **docs.spring.io — Externalized Configuration** — `https://docs.spring.io/spring-boot/reference/features/external-config.html`
- **docs.spring.io — Spring Boot Actuator** — `https://docs.spring.io/spring-boot/reference/actuator/index.html`
- **docs.spring.io — Web (Servlet) server configuration** — `https://docs.spring.io/spring-boot/reference/web/servlet.html`
- **docs.spring.io — Spring Boot Gradle Plugin** — `https://docs.spring.io/spring-boot/docs/current/gradle-plugin/reference/htmlsingle/`
- **docs.spring.io — Spring Boot Maven Plugin** — `https://docs.spring.io/spring-boot/docs/current/maven-plugin/reference/htmlsingle/`
- **docs.spring.io — GraalVM Native Image Support** — `https://docs.spring.io/spring-boot/reference/features/native-image.html`
- **docs.spring.io — Efficient Docker images (Layered jars)** — `https://docs.spring.io/spring-boot/how-to/packaging.html`
- **Baeldung — Spring Boot: Custom Auto-Configuration** — `https://www.baeldung.com/spring-boot-custom-auto-configuration`
- **Baeldung — Spring Boot Starters** — `https://www.baeldung.com/spring-boot-starters`
- **Baeldung — Spring Boot ApplicationRunner vs CommandLineRunner** — `https://www.baeldung.com/spring-boot-runlogic`
- **Reflectoring — Spring Boot startup events** — `https://reflectoring.io/spring-boot-application-events/`
- **Spring blog — Spring Boot 3.0 Release Notes** — `https://github.com/spring-projects/spring-boot/wiki/Spring-Boot-3.0-Release-Notes`
- **Spring blog — Spring Boot 3.2 Release Notes** — `https://github.com/spring-projects/spring-boot/wiki/Spring-Boot-3.2-Release-Notes`
- **Spring blog — Spring Boot 3.3 Release Notes** — `https://github.com/spring-projects/spring-boot/wiki/Spring-Boot-3.3-Release-Notes`
- **Spring blog — Spring Boot 3.4 Release Notes** — `https://github.com/spring-projects/spring-boot/wiki/Spring-Boot-3.4-Release-Notes`
- **Paketo Buildpacks documentation** — `https://paketo.io/docs/`
- **Книга — "Spring Boot in Action" (Craig Walls)** — Manning
- **Книга — "Cloud Native Spring in Action" (Cosmin Macrinici)** — Manning, ISBN 978-1633439429 (native image, buildpacks, K8s)
- **Книга — "Learning Spring Boot 3.0" (Greg L. Turnquist)** — Packt

### Практика / Кейсы

1. **Кастомный starter**: создать `myorg-spring-boot-starter-metrics` — starter, который авто-конфигурирует кастомный `MetricsCollector` bean с `@ConditionalOnClass(MeterRegistry.class)` и `@ConditionalOnMissingBean`. Написать `META-INF/spring/org.springframework.boot.autoconfigure.AutoConfiguration.imports`. В демо-проекте проверить что bean появляется. **Цель:** понять pipeline starter → autoconfig → consumer.
2. **Замена Tomcat на Undertow**: в проекте исключить `spring-boot-starter-tomcat`, добавить `spring-boot-starter-undertow`. Сравнить startup time, throughput (через wrk/k6) и memory footprint с Tomcat. **Цель:** уметь менять servlet container.
3. **Layered jar + Docker multi-stage**: использовать `java -Djarmode=layertools -jar app.jar extract` в Docker-билде, каждый слой скопировать отдельно. Зафиксировать ускорение rebuild (после изменения кода зависимости кэшируются). **Цель:** production-ready Docker image.
4. **GraalVM Native Image**: добавить `native` Maven profile, собрать `target/app-native`. Замерить startup time и memory. Добавить `RuntimeHintsRegistrar` для Jackson serialization DTO (требует reflection). Запустить в Docker (Distroless). **Цель:** понять native image pitfalls.
5. **Actuator endpoints + Prometheus + Grafana**: подключить `micrometer-registry-prometheus`, открыть `/actuator/prometheus`, настроить scrape в Prometheus. Дашборд в Grafana (JVM + HTTP requests). Добавить кастомный `@Endpoint` для runtime feature toggles. **Цель:** production observability.
6. **Externalized config chain**: env var `APP_DATASOURCE_URL` → command-line `--app.datasource.url=...` → `application.yml` (вне jar) → `application-prod.yml` (внутри jar). Протестировать порядок через `env` actuator. **Цель:** понять config precedence.
7. **Кастомный `SpringApplicationRunListener`**: реализовать listener, который логирует startup phase и измеряет время каждой фазы (через `ApplicationStartup` + `BufferingApplicationStartup`). Вывести top-10 медленных bean-ов. **Цель:** startup profiling.
8. **Buildpacks в CI**: настроить `mvn spring-boot:build-image` в GitHub Actions для сборки Docker image без Dockerfile. Использовать Paketo Bellsoft Liberica для native. Запушить в GHCR. **Цель:** cloud-native build pipeline.
9. **Graceful shutdown**: включить `server.shutdown=graceful` + `spring.lifecycle.timeout-per-shutdown-phase=30s`. В K8s настроить `preStop` hook + `terminationGracePeriodSeconds`. Проверить, что in-flight requests завершаются, новые отклоняются (503). **Цель:** zero-downtime deployments.
10. **`@Endpoint` web + JMX**: создать кастомный `FeatureFlagsEndpoint` с `@ReadOperation` (список флагов), `@WriteOperation` (toggle flag), `@DeleteOperation` (reset). Проверить что работает и через HTTP (`/actuator/featureflags`), и через JMX (через JConsole). **Цель:** понять endpoint abstraction.
11. **Auto-configuration ordering**: создать две кастомные autoconfig (`DataSourceAutoConfiguration` и `MigrationAutoConfiguration`), которые должны выполниться в определённом порядке. Использовать `@AutoConfigureAfter`. Включить `--debug` и проверить order в `ConditionEvaluationReport`. **Цель:** понять авто-config graph.
12. **`ApplicationStartup` + `BufferingApplicationStartup`**: настроить `BufferingApplicationStartup(1000)` на `SpringApplication`. После старта проанализировать через `/actuator/startup` — найти топ-N медленных bean-ов. **Цель:** startup optimization.

---

## 5. Spring Web MVC

### Теория

- **Spring Web MVC** — servlet-based web framework, реализация Spring-обёртки вокруг `DispatcherServlet`. Центральный фронт-контроллер паттерн (J2EE Front Controller).
- **`DispatcherServlet`** — единая точка входа всех HTTP-запросов. Делегирует работу специализированным компонентам: `HandlerMapping`, `HandlerAdapter`, `HandlerInterceptor`, `HandlerExceptionResolver`, `ViewResolver`, `LocaleResolver`, `ThemeResolver`, `MultipartResolver`.
- **Request processing pipeline** в `DispatcherServlet`:
  1. `WebApplicationContext` lookup (по `ServletContext` атрибуту).
  2. `LocaleResolver.resolveLocale` — определение локали.
  3. `ThemeResolver.resolveThemeName` — тема (legacy).
  4. `MultipartResolver.resolveMultipart` (если multipart) — парсинг `multipart/form-data`.
  5. `HandlerMapping.getHandler` — поиск `HandlerExecutionChain` (handler + interceptors).
  6. `HandlerInterceptor.preHandle` — для каждого interceptor в порядке. Если любой возвращает `false` — цепочка прерывается.
  7. `HandlerAdapter.handle` — вызов controller-метода.
  8. `HandlerInterceptor.postHandle` — для каждого interceptor в **обратном** порядке.
  9. Если исключение — `HandlerExceptionResolver.resolveException`.
  10. `DispatcherServlet.render` (если view) или запись response body (если `@ResponseBody`).
  11. `HandlerInterceptor.afterCompletion` — для каждого interceptor в обратном порядке (finally).
- **`HandlerMapping`** implementations: `RequestMappingHandlerMapping` (`@RequestMapping`), `BeanNameUrlHandlerMapping`, `RouterFunctionMapping` (functional endpoints), `SimpleUrlHandlerMapping` (static resources, redirects).
- **`HandlerAdapter`** implementations: `RequestMappingHandlerAdapter` (для `@RequestMapping` методов), `HttpRequestHandlerAdapter`, `SimpleControllerHandlerAdapter`, `HandlerFunctionAdapter` (functional).
- **`@Controller` vs `@RestController`**: `@RestController` = `@Controller` + `@ResponseBody` на каждом методе. Возвращает данные (JSON/XML), а не view names.
- **Mapping annotations**:
  - `@RequestMapping(method = GET, path = "/users/{id}")` — общая.
  - Короткие формы: `@GetMapping`, `@PostMapping`, `@PutMapping`, `@PatchMapping`, `@DeleteMapping`.
  - Параметры: `value`/`path`, `method`, `params`, `headers`, `consumes`, `produces`, `name`.
  - Path patterns: `/users/{id}` (path variable), `/users/**` (ant, deprecated в 6+ в пользу `PathPatternParser`), `/users/{id:[0-9]+}` (regex).
- **`PathPatternParser`** (Spring 6+): замена `AntPathMatcher`. Производительность ~6x лучше. Поддерживает `{var}`, `{var:regex}`, `/{*path}` (catch-all), `/` буквально. Используется по умолчанию в Boot 3 для MVC.
- **Method argument annotations**:
  - `@PathVariable` — извлечение из URL (`/users/{id}` → `@PathVariable Long id`). С поддержкой regex, optional (`required=false`), `Map<String, String>`.
  - `@RequestParam` — query/form params. `required` (по умолчанию true), `defaultValue`. Поддерживает `Map<String,String>`, `MultiValueMap`, `List<T>`.
  - `@RequestBody` — body deserialized через `HttpMessageConverter` (Jackson по умолчанию для JSON). Обязательно `@Valid`/`@Validated` для валидации.
  - `@RequestHeader` — HTTP headers. По имени или `Map`.
  - `@CookieValue` — cookies.
  - `@MatrixVariable` — matrix params (`/users;id=1;id=2`).
  - `@ModelAttribute` — bind form/query params к объекту + добавление в Model.
  - `@SessionAttribute` — attribute из HTTP session.
  - `@RequestPart` — часть multipart request (файл или JSON в multipart).
- **Other supported arguments**: `HttpServletRequest`, `HttpServletResponse`, `HttpSession`, `Principal`, `Locale`, `TimeZone`, `ZoneId`, `InputStream`, `Reader`, `OutputStream`, `Writer`, `PushBuilder` (HTTP/2), `MultipartFile`, `Part` (Servlet 3.0), `RedirectAttributes`, `Model`, `ModelMap`, `BindingResult`, `SessionStatus`, `UriComponentsBuilder`, `Errors`, `WebRequest`, `NativeWebRequest`.
- **Return types**: `String` (view name, `"redirect:..."`, `"forward:..."`), `View`, `ModelAndView`, `Model`, `Map`, `ResponseEntity<T>`, `ResponseEntity<Void>`, `HttpEntity<T>`, `HttpHeaders`, `Object` (с `@ResponseBody` → JSON), `void` (если response записан напрямую), `Callable<T>`, `DeferredResult<T>`, `ResponseBodyEmitter`, `SseEmitter`, `StreamingResponseBody`, `Flux`/`Mono` (reactive types в servlet MVC).
- **`@ResponseBody`** — указывает, что return value должно быть сериализовано в response body через `HttpMessageConverter`.
- **`HttpMessageConverter`** — стратегия сериализации/десериализации. Стандартные: `MappingJackson2HttpMessageConverter` (JSON), `MappingJackson2XmlHttpMessageConverter` (XML, через Jackson XML), `Jaxb2RootElementHttpMessageConverter` (XML, JAXB), `StringHttpMessageConverter`, `ByteArrayHttpMessageConverter`, `ResourceHttpMessageConverter`, `FormHttpMessageConverter`, `ProtobufHttpMessageConverter`. Кастомный: реализовать интерфейс, зарегистрировать через `WebMvcConfigurer.configureMessageConverters` или `extendMessageConverters`.
- **`@ExceptionHandler`** — метод в `@ControllerAdvice` или в самом controller для обработки исключений. Возвращает view, `ResponseEntity`, или `@ResponseBody` object.
- **`@ControllerAdvice`** — глобальный component для `@ExceptionHandler`, `@ModelAttribute`, `@InitBinder`, `@ResponseBodyAdvice`. Параметры: `basePackages`, `basePackageClasses`, `assignableTypes`, `annotations`. `@RestControllerAdvice` = `@ControllerAdvice` + `@ResponseBody`.
- **`@ResponseStatus`** — задаёт HTTP status. На методе controller, на `@ExceptionHandler`-методе, на exception-классе. `value`/`code`, `reason`.
- **`ResponseEntity<T>`** — обёртка над body + status + headers. Builder API: `ResponseEntity.ok().header(...).body(...)`, `ResponseEntity.status(201).body(...)`, `ResponseEntity.noContent().build()`.
- **`ProblemDetail` (RFC 7807, Spring 6+)** — стандартизированный формат ошибок. `ProblemDetail.forStatus(404).setTitle("Not Found").setDetail("User 123 not found").setProperty("userId", 123)`. Авто-генерация через `ProblemDetailExceptionHandler` (часть `ResponseEntityExceptionHandler`). ContentType: `application/problem+json`. С Boot 3 включен по умолчанию для стандартных exceptions (`NoSuchElementFoundException`, etc).
- **`HandlerInterceptor`** — `preHandle(request, response, handler)`, `postHandle`, `afterCompletion`. `preHandle` возвращает boolean. Регистрация через `WebMvcConfigurer.addInterceptors`. Подходит для: auth, logging, metrics, header injection. НЕ для безопасности — для этого `Spring Security` filters.
- **`AsyncHandlerInterceptor`** — добавляет `afterConcurrentHandlingStarted` (вызывается в другом потоке, когда async request стартовал).
- **`WebRequestInterceptor`** — упрощённый interceptor без доступа к `HttpServletResponse` (полезно для portlet/общего API).
- **`WebMvcConfigurer`** — Spring Boot-style customization. Методы: `addInterceptors`, `addArgumentResolvers`, `addReturnValueHandlers`, `configureMessageConverters`, `extendMessageConverters`, `addFormatters`, `configurePathMatch`, `configureContentNegotiation`, `addResourceHandlers`, `configureDefaultServletHandling`, `addViewControllers`, `configureViewResolvers`, `addCorsMappings`, `configureAsyncSupport`.
- **`MultipartFile`** — представление загруженного файла. `getOriginalFilename()`, `getContentType()`, `getBytes()`, `getInputStream()`, `transferTo(File)`. Кастомный resolver: `StandardServletMultipartResolver` (по умолчанию, Servlet 3.0 API) или `CommonsMultipartResolver` (legacy, Apache Commons FileUpload).
- **Validation**: `@Valid` (JSR-380) на `@RequestBody`/`@ModelAttribute`/`@RequestPart` → `MethodArgumentNotValidException` если есть violations (ловится `@ControllerAdvice`). `@Validated` — Spring-вариант с поддержкой validation groups. Bean Validation constraints: `@NotNull`, `@NotBlank`, `@NotEmpty`, `@Size`, `@Min`, `@Max`, `@Pattern`, `@Email`, `@Past`, `@Future`, `@DecimalMin`, `@Positive`, `@Digits`, кастомные `@Constraint(validatedBy = ...)`.
- **`@ControllerAdvice` для validation errors**: ловить `MethodArgumentNotValidException`, конвертировать в `ProblemDetail` или кастомный error response с полями violations.
- **`MethodArgumentNotValidException` vs `ConstraintViolationException`**: первое — для `@Valid` на `@RequestBody`/`@ModelAttribute`, второе — для `@Validated` на service-level методы (через `MethodValidationPostProcessor`).
- **CORS**: `@CrossOrigin` на controller-методе/классе, `WebMvcConfigurer.addCorsMappings` глобально. Spring Security имеет свой `CorsConfigurationSource` (он заменяет MVC CORS если security на classpath).
- **Async controllers**:
  - `DeferredResult<T>` — return из controller, потом в другом потоке вызвать `setResult(T)`. Полезно для long polling, webhook-style APIs.
  - `Callable<T>` — Spring выполняет в `AsyncTaskExecutor`, возвращает результат.
  - `ResponseBodyEmitter` — низкоуровневый, можно отправлять несколько объектов в одном response.
  - `SseEmitter` — Server-Sent Events, можно отправлять несколько events.
  - `StreamingResponseBody` — стриминг raw bytes (например, генерация PDF на лету).
  - Конфигурация: `spring.mvc.async.request-timeout`, `WebMvcConfigurer.configureAsyncSupport` (setDefaultTimeout, setTaskExecutor, registerCallableInterceptors, registerDeferredResultInterceptors).
- **Content negotiation**: через URL extension (`.json`, `.xml`, deprecated), `Accept` header, параметр `?format=json`. Настройка: `WebMvcConfigurer.configureContentNegotiation` (`favorParameter`, `parameterName`, `ignoreAcceptHeader`, `defaultContentType`, `mediaTypes`).
- **Static resources**: `spring.web.resources.static-locations=classpath:/static/,classpath:/public/`. ETag/cache control через `spring.web.resources.cache.cachecontrol.*`.
- **View resolution**: `ViewResolver` (`InternalResourceViewResolver` для JSP, `ThymeleafViewResolver`, `FreeMarkerViewResolver`). Boot авто-конфигурирует под classpath.
- **`RestControllerAdvice` + `ResponseBodyAdvice`**: `ResponseBodyAdvice` позволяет модифицировать body перед записью (например, обернуть в envelope, шифровать, удалить sensitive поля). `supports`, `beforeBodyWrite`.

### Материалы

- **docs.spring.io — Spring Web MVC Reference** — `https://docs.spring.io/spring-framework/reference/web/webmvc.html`
- **docs.spring.io — Spring Web MVC → DispatcherServlet** — `https://docs.spring.io/spring-framework/reference/web/webmvc/mvc-servlet.html`
- **docs.spring.io — Spring Web MVC → Handlers (annotations)** — `https://docs.spring.io/spring-framework/reference/web/webmvc/mvc-controller.html`
- **docs.spring.io — Spring Web MVC → MVC Exceptions** — `https://docs.spring.io/spring-framework/reference/web/webmvc/mvc-servlet/exceptionhandlers.html`
- **docs.spring.io — Spring Web MVC → REST Exceptions (ProblemDetail)** — `https://docs.spring.io/spring-framework/reference/web/webmvc/mvc-ann-rest-exceptions.html`
- **docs.spring.io — Spring Web MVC → CORS** — `https://docs.spring.io/spring-framework/reference/web/webmvc-cors.html`
- **docs.spring.io — Spring Web MVC → Async** — `https://docs.spring.io/spring-framework/reference/web/webmvc/mvc-ann-async.html`
- **docs.spring.io — Spring Web MVC → Multipart** — `https://docs.spring.io/spring-framework/reference/web/webmvc/mvc-multipart.html`
- **Spring blog — Exception Handling in Spring MVC** — `https://spring.io/blog/2013/11/01/exception-handling-in-spring-mvc`
- **Baeldung — Error Handling for REST with Spring** — `https://www.baeldung.com/exception-handling-for-rest-with-spring`
- **Baeldung — Spring @RequestBody and @ResponseBody** — `https://www.baeldung.com/spring-requestbody-responsebody`
- **Baeldung — A Guide to Spring MVC Handler Interceptors** — `https://www.baeldung.com/spring-mvc-handler-interceptor`
- **Baeldung — New in Spring 6: ProblemDetail** — `https://www.baeldung.com/spring-6-problemdetail`
- **Baeldung — Spring Async Controllers (Callable, DeferredResult)** — `https://www.baeldung.com/spring-mvc-async-deferredresult`
- **Baeldung — Spring MVC File Upload** — `https://www.baeldung.com/spring-file-upload`
- **Reflectoring — Spring MVC essentials** — `https://reflectoring.io/spring-mvc/`
- **RFC 7807 — Problem Details for HTTP APIs** — `https://datatracker.ietf.org/doc/html/rfc7807`
- **Книга — "Spring in Action, Sixth Edition" (Craig Walls)** — Manning, Chapter 5 (Spring MVC)
- **Книга — "Pro Spring 6", Chapter 17: Spring Web MVC** — Apress

### Практика / Кейсы

1. **REST API с CRUD + ProblemDetail**: создать `/api/users` с GET/POST/PUT/DELETE, `@Valid` на `UserDto`, `@RestControllerAdvice` с обработкой `MethodArgumentNotValidException`, `UserNotFoundException`, `DataIntegrityViolationException` → `ProblemDetail` (RFC 7807). Покрыть `@WebMvcTest` с MockMvc. **Цель:** production-grade error handling.
2. **Async long-polling через `DeferredResult`**: реализовать endpoint `/notifications/{userId}` который ждёт уведомления из in-memory queue, возвращает их через `DeferredResult` с timeout 30s. Настроить `AsyncTaskExecutor`. Сравнить с реактивным аналогом (WebFlux). **Цель:** понять async servlet.
3. **`HandlerInterceptor` для auth + audit**: interceptor проверяет JWT-токен из `Authorization` header, устанавливает `UserPrincipal` в request attributes, логирует audit event в Kafka. Настроить через `WebMvcConfigurer.addInterceptors`. Тестировать через MockMvc. **Цель:** понять interceptor vs Spring Security filter.
4. **`SseEmitter` для push уведомлений**: реализовать `/events/stream` который держит SSE-соединение и пушит события всем подписчикам (in-memory `CopyOnWriteArrayList<SseEmitter>`). Обработать `onCompletion`, `onTimeout`, `onError`. Тестировать через `WebClient` реактивный клиент. **Цель:** освоить SSE.
5. **Custom `HttpMessageConverter` для CSV**: реализовать `CsvHttpMessageConverter` на Apache Commons CSV, который сериализует `List<UserDto>` в CSV. Зарегистрировать через `extendMessageConverters`. Добавить content negotiation `?format=csv`. **Цель:** понять message converters.
6. **CORS multi-domain**: настроить CORS для разных origins в dev (`localhost:3000`, `localhost:8080`) и prod (`app.example.com`). Использовать `@CrossOrigin` на методах и глобальный `CorsConfigurationSource` в Spring Security. Тестировать preflight `OPTIONS`. **Цель:** secure CORS setup.
7. **File upload с S3**: `/files/upload` принимает `MultipartFile`, валидирует (`@NotNull`, `size < 10MB`, mime type whitelist), сохраняет в AWS S3 (через `software.amazon.awssdk:s3`), возвращает presigned URL. Покрыть testcontainers LocalStack S3. **Цель:** production upload flow.
8. **Custom `HandlerMethodArgumentResolver`**: реализовать resolver для `@CurrentUser UserPrincipal` который извлекает user из JWT-токена в `Authorization` header. Зарегистрировать через `WebMvcConfigurer.addArgumentResolvers`. **Цель:** понять argument resolution.
9. **`ResponseBodyAdvice` для envelope**: обернуть все ответы в `{ "data": ..., "meta": { "timestamp": ..., "requestId": ... } }`, кроме уже обёрнутых. Сравнить с явным подходом (DTO wrappers). **Цель:** understand response post-processing.

---

## 6. Spring WebFlux (Reactive)

### Теория

- **Spring WebFlux** — reactive-stack web framework, представлен в Spring 5.0 (2017). Альтернатива Spring Web MVC для non-blocking, event-driven приложений. Не заменяет MVC, а параллельная опция.
- **Reactive Streams** — спецификация (RS) для асинхронной обработки потоков с backpressure. 4 интерфейса: `Publisher<T>`, `Subscriber<T>`, `Subscription`, `Processor<T,R>`. Контракт: `request(n)` для demand-signaling, `cancel()` для отмены.
- **Reactor** — реализация Reactive Streams от Pivotal/VMware, базируется на WebFlux. Основные типы: `Mono<T>` (0..1 элементов), `Flux<T>` (0..N элементов).
- **`Mono`**: emits 0 or 1 item. Operators: `map`, `flatMap`, `filter`, `defaultIfEmpty`, `switchIfEmpty`, `zip`, `zipWith`, `merge`, `concat`, `delay`, `timeout`, `retry`, `retryWhen`, `onErrorResume`, `onErrorReturn`, `doOnError`, `subscribeOn`, `publishOn`, `fromCallable`, `fromSupplier`, `fromRunnable`, `just`, `empty`, `error`, `never`, `defer`, `cache`, `repeat`, `then`.
- **`Flux`**: emits 0..N items. Operators: `map`, `flatMap`, `concatMap`, `flatMapSequential`, `switchMap`, `scan`, `reduce`, `collectList`, `collectMap`, `buffer`, `window`, `groupBy`, `take`, `skip`, `takeUntil`, `skipUntil`, `merge`, `mergeSequential`, `zip`, `combineLatest`, `amb`, `retry`, `retryWhen`, `onErrorResume`, `publishOn`, `subscribeOn`, `parallel`, `sequential`, `limitRate`, `onBackpressureBuffer`, `onBackpressureDrop`, `onBackpressureLatest`, `elapsed`, `timestamp`, `cast`, `ofType`, `defaultIfEmpty`, `switchIfEmpty`, `distinct`, `distinctUntilChanged`, `sort`, `sample`, `throttleFirst`, `throttleLast`, `debounce`, `cache`, `replay`.
- **Hot vs Cold publishers**:
  - **Cold** — данные генерируются для каждого подписчика (HTTP-запрос, SQL-запрос). `Mono.fromCallable`, `Flux.fromIterable`. По умолчанию все операторы создают cold publisher.
  - **Hot** — данные генерируются независимо от подписчиков. `Sinks.many().multicast()`, `Sinks.many().replay()`, `EmitterProcessor` (deprecated), `ReplayProcessor` (deprecated). Подписчики получают только то, что пришло после подписки.
  - `share()` / `publish().refCount()` — превращает cold publisher в hot, когда есть хотя бы один подписчик.
- **Backpressure strategies**:
  - `request(n)` — subscriber запрашивает n элементов.
  - `onBackpressureBuffer(maxSize, BufferOverflowStrategy)` — буферизация. Стратегии: ERROR, DROP_OLDEST, DROP_LATEST.
  - `onBackpressureDrop()` — drop новые элементы если нет demand.
  - `onBackpressureLatest()` — оставлять только последний.
  - `limitRate(highTide, lowTide)` — регулирует request rate (по умолчанию `Long.MAX_VALUE`, что может привести к OOM).
- **Schedulers**:
  - `Schedulers.immediate()` — текущий поток.
  - `Schedulers.single()` — один reusable thread.
  - `Schedulers.boundedElastic()` — пул для blocking I/O (аналог cached thread pool, но bounded). Использовать только для legacy/blocking code.
  - `Schedulers.parallel()` — пул для CPU-bound (по умолчанию = CPU cores).
  - `Schedulers.fromExecutor(Executor)` — кастомный.
- **`subscribeOn` vs `publishOn`**:
  - `subscribeOn(scheduler)` — на каком scheduler выполняется подписка (источник данных). Должен быть до первого `publishOn`.
  - `publishOn(scheduler)` — на каком scheduler выполняются downstream operators после этой точки. Меняет thread context.
  - Только один `subscribeOn` имеет эффект (ближайший к источнику).
- **Operators chain** — `Mono.zip(a, b).flatMap(...)`. Important: **operators не выполняются** до `.subscribe()` (или до возврата из controller). В WebFlux контроллер возвращает `Mono`/`Flux` → фреймворк подписывается.
- **`Context`** (Reactor 3.3+): immutable key-value storage, передаётся **вверх** по цепочке (от downstream к upstream). Используется для trace-id, tenant-id, security context. `Mono.deferContextual(ctx -> ...)`. Аналог ThreadLocal для reactive.
- **`Hooks`** — глобальные обработчики: `onEachOperator`, `onLastOperator`, `onErrorDropped`, `onNextDropped`, `onOperatorError`. Полезны для интеграции (MDC, tracing).
- **Netty** — основной runtime для WebFlux. Асинхронный, event-loop based. `Reactor Netty` (`reactor-netty-http`) — обёртка над Netty для реактивного HTTP server/client. Альтернативы: Tomcat/Undertow/Jetty в реактивном режиме (если servlet 3.1+ async).
- **`@Controller` reactive** — те же аннотации что в MVC (`@GetMapping`, `@RequestBody`, etc). Контроллер возвращает `Mono<T>` или `Flux<T>` (или `Mono<ResponseEntity<T>>`).
- **`WebClient`** — non-blocking HTTP клиент, замена `RestTemplate`. Builder-based: `WebClient.builder().baseUrl(...).defaultHeader(...).build()`. Методы: `get()`, `post()`, `put()`, `delete()`. `.uri(...)`, `.retrieve()`, `.bodyToMono(Class)`, `.exchangeToMono()`. Поддерживает filters (`ExchangeFilterFunction`), retries, timeouts, OAuth2.
- **`ExchangeFilterFunction`** — interceptor для всех запросов/ответов WebClient. Примеры: добавление Authorization header, logging, retry-on-5xx.
- **Functional endpoints** (`RouterFunction` + `HandlerFunction`): альтернатива аннотациям, функциональный DSL.
  ```java
  RouterFunction<?> route = route()
      .GET("/users/{id}", accept(JSON), handler::getUser)
      .POST("/users", handler::createUser)
      .build();
  ```
  Преимущества: явная композиция, легче тестировать, лучшая AOT-совместимость.
- **Reactive `WebFilter`** — аналог servlet filter, но для reactive stack. `Mono<Void> filter(ServerWebExchange, WebFilterChain)`.
- **Reactive `@ExceptionHandler` / `WebExceptionHandler`**: аналог `@ControllerAdvice` для WebFlux. `ResponseEntityExceptionHandler` reactive variant.
- **`ServerWebExchange`** — reactive аналог `HttpServletRequest`/`HttpServletResponse`. Доступ к request/response, attributes, session (reactive), formData (Mono), multipartData (Mono).
- **Reactive Data Access**:
  - **R2DBC** (Reactive Relational Database Connectivity) — reactive спецификация JDBC. Драйверы для PostgreSQL, MySQL, MariaDB, MSSQL, H2, Oracle.
  - Spring Data R2DBC: `ReactiveCrudRepository`, `R2dbcEntityTemplate`, кастомные `DatabaseClient` queries.
  - Reactive MongoDB: `ReactiveMongoRepository`, `ReactiveMongoTemplate`.
  - Reactive Redis: `ReactiveRedisTemplate`, `ReactiveRedisConnectionFactory`.
  - Reactive Cassandra, Reactive Couchbase.
- **`R2dbcEntityTemplate` / `DatabaseClient`**: programmatic SQL builder. `databaseClient.sql("SELECT * FROM users WHERE id = :id").bind("id", 123).map(row -> ...).one()`.
- **Transactions в WebFlux**: `TransactionalOperator` (реактивный аналог `PlatformTransactionManager`). `mono.as(txOperator::transactional)`. Не блокирует thread.
- **MDC в реактивном коде**: классический MDC (ThreadLocal) не работает — нужно использовать Reactor `Context` и копировать в MDC только при логировании (через `Hooks.onEachOperator` или `Micrometer Tracing`).
- **Performance comparison**: WebFlux — выше throughput на I/O-bound нагрузках (1 connection = 1 event loop thread = thousands of concurrent requests). MVC — выше latency-стабильность для CPU-bound и проще для transactional/business logic. Сложность WebFlux — debugging, stack traces, тестирование.
- **Backpressure в WebClient**: при приёме больших ответов. `WebClient` использует Netty's `ByteBuf` allocation. Не загружать весь body в память: `.retrieve().bodyToFlux(String.class)` для стриминга.
- **`@Controller` reactive + SSE**: `Flux<ServerSentEvent>` — server-sent events, каждый элемент как event. `Flux.interval(Duration.ofSeconds(1)).map(i -> ServerSentEvent.builder(i).build())`.
- **WebSocket reactive**: `@MessageMapping` (для STOMP), или `WebSocketHandler` + `WebSocketHandlerRegistry`, или `Reactor Netty` raw WebSocket.

### Материалы

- **docs.spring.io — Spring WebFlux Reference** — `https://docs.spring.io/spring-framework/reference/web/webflux.html`
- **docs.spring.io — WebFlux → New Framework** — `https://docs.spring.io/spring-framework/reference/web/webflux/new-framework.html`
- **docs.spring.io — WebFlux → Functional Endpoints (RouterFunction)** — `https://docs.spring.io/spring-framework/reference/web/webflux-functional.html`
- **docs.spring.io — WebFlux → WebClient** — `https://docs.spring.io/spring-framework/reference/integration/rest-clients.html#webflux-client`
- **docs.spring.io — Spring Boot Reactive Web** — `https://docs.spring.io/spring-boot/reference/web/reactive.html`
- **docs.spring.io — R2DBC Reference** — `https://docs.spring.io/spring-data/relational/reference/r2dbc.html`
- **docs.spring.io — Spring Data R2DBC** — `https://spring.io/projects/spring-data-r2dbc`
- **Project Reactor Reference** — `https://projectreactor.io/docs/core/release/reference/`
- **Project Reactor JavaDoc** — `https://projectreactor.io/docs/core/release/api/`
- **Reactive Streams Specification** — `https://www.reactive-streams.org/`
- **Baeldung — Intro to Spring WebFlux** — `https://www.baeldung.com/spring-webflux`
- **Baeldung — Spring WebClient** — `https://www.baeldung.com/spring-5-webclient`
- **Baeldung — Reactor Java Guide** — `https://www.baeldung.com/java-reactor`
- **Baeldung — Backpressure in WebFlux** — `https://www.baeldung.com/spring-webflux-backpressure`
- **Reflectoring — A Practical Guide to Spring WebFlux** — `https://reflectoring.io/getting-started-with-spring-webflux/`
- **Spring blog — WebFlux parallel threads and scheduling** — `https://spring.io/blog/2019/07/30/i-have-been-wondering-how-reactive-programming-actually-works`
- **Книга — "Reactive Spring" (Greg L. Turnquist)** — Apress, ISBN 978-1484251939
- **Книга — "Hands-On Reactive Programming in Spring 5" (Oleh Dokuka, Ilya Lantsberg)** — Packt
- **Reactor BlockHound** — `https://github.com/reactor/BlockHound` (детекция blocking calls в reactive коде)

### Практика / Кейсы

1. **Reactive CRUD API + R2DBC**: реализовать `/api/users` с `ReactiveCrudRepository`, PostgreSQL через Testcontainers R2DBC, валидация через reactive Bean Validation, error handling через `WebExceptionHandler` + `ProblemDetail`. Покрыть `WebTestClient`. **Цель:** освоить полный reactive stack.
2. **WebClient с retry/circuit breaker**: реализовать upstream-calling service через `WebClient` с exponential backoff (`retryWhen(Retry.backoff(...))`), timeout (`responseTimeout`), circuit breaker (Resilience4j reactive). Сравнить с MVC `RestTemplate`. **Цель:** reactive resilience.
3. **SSE поток через `Flux`**: `/events/stream` который пушит события каждые 1s, с backpressure (`onBackpressureDrop` для slow clients), timeout 60s, cleanup на disconnect (`doOnCancel`). Тестировать через `WebClient` reactive. **Цель:** освоить SSE в WebFlux.
4. **Functional endpoints (RouterFunction)**: переписать REST API с аннотаций на `RouterFunction` + `HandlerFunction`. Сравнить читаемость, тестируемость (`WebTestClient.bindToRouterFunction`), AOT-совместимость. **Цель:** functional DSL.
5. **`Context` для trace propagation**: в reactive filter извлечь trace-id из header, положить в Reactor `Context`, в downstream сервисе прочитать через `Mono.deferContextual`. Интегрировать с Micrometer Tracing. **Цель:** MDC в реактивном коде.
6. **BlockHound**: подключить `BlockHound` в тестах, найти все blocking calls в reactive pipeline (`JDBC`, `Thread.sleep`, blocking I/O). Переписать на reactive или обернуть в `Schedulers.boundedElastic()`. **Цель:** detect blocking.
7. **`TransactionalOperator`**: реализовать reactive service с двумя R2DBC операциями в одной транзакции (`mono.flatMap(a -> update(a)).flatMap(b -> audit(b)).as(txOperator::transactional)`). Сравнить с `@Transactional` (не работает в reactive). **Цель:** reactive transactions.
8. **Backpressure тестирование**: producer `Flux.range(0, Integer.MAX_VALUE)`, consumer обрабатывает 1 элемент в 100ms. Демонстрировать OOM без backpressure, потом применить `onBackpressureBuffer(1000, DROP_OLDEST)`. Сравнить throughput. **Цель:** понять backpressure визуально.
9. **Reactive WebSocket**: реализовать `/ws/chat` через `WebSocketHandler`, broadcast сообщений всем подписчикам через `Sinks.many().multicast()`. Тестировать через `WebSocketClient`. **Цель:** reactive WebSocket.

---

## 7. Spring Data JPA

### Теория

- **Spring Data JPA** — слой абстракции над JPA (Hibernate, EclipseLink, etc). Уменьшает boilerplate DAO-кода через repository-интерфейсы.
- **Repository interfaces**:
  - `Repository<T, ID>` — маркерный, без методов.
  - `CrudRepository<T, ID>` — `save`, `findById`, `findAll`, `count`, `delete`, `existsById`.
  - `PagingAndSortingRepository<T, ID>` — extends `CrudRepository`, добавляет `findAll(Sort)`, `findAll(Pageable)`.
  - `JpaRepository<T, ID>` — extends `PagingAndSortingRepository` + `QueryByExampleExecutor`. JPA-specific: `findAll()`, `flush()`, `saveAndFlush()`, `deleteInBatch()`, `getOne(ID)` (lazy proxy), `getReferenceById(ID)` (то же, новое имя).
  - `QueryByExampleExecutor<T>` — `count(Example)`, `findAll(Example)`, `findOne(Example)`.
  - `QuerydslPredicateExecutor<T>` (если QueryDSL) — type-safe queries через predicates.
- **Query derivation**: Spring Data парсит имя метода и генерирует JPQL. `findByLastnameAndFirstnameOrderByFirstnameAsc(String lastname, String firstname)`. Keywords: `findBy`, `readBy`, `getBy`, `queryBy`, `countBy`, `existsBy`, `deleteBy`, `removeBy`. Subject keywords: `First`, `Top`, `Distinct`. Predicate keywords: `Is`, `Equals`, `Between`, `LessThan`, `LessThanEqual`, `GreaterThan`, `GreaterThanEqual`, `After`, `Before`, `IsNull`, `IsNotNull`, `NotNull`, `Like`, `NotLike`, `StartingWith`, `EndingWith`, `Containing`, `IgnoreCase`, `NotContaining`, `Regex`, `In`, `NotIn`, `True`, `False`, `Sort`, `OrderBy`. Limitations: длинные имена, сложные join-ы — лучше через `@Query`.
- **`@Query`** — ручной JPQL или native SQL. `@Query("SELECT u FROM User u WHERE u.status = :status")`. `nativeQuery = true` для native SQL. Поддержка SpEL: `#{#entityName}`, `:#{#dynamicParam}`. Модификации через `@Modifying`.
- **`@Modifying`** — для `UPDATE`/`DELETE` queries. Параметры: `flushAutomatically` (true = flush перед запросом), `clearAutomatically` (true = clear `EntityManager` после, чтобы избежать stale data). `@Modifying(clearAutomatically = true, flushAutomatically = true)`.
- **Sort и Pageable**: `findAll(Pageable)` — `PageRequest.of(page, size, Sort.by("lastname").ascending())`. Возвращает `Page<T>` (с total count) или `Slice<T>` (без total count, быстрее для infinite scroll). `OffsetBasedPageRequest` для offset-based pagination.
- **Projections**:
  - **Interface-based**: `interface UserProjection { String getFirstname(); String getLastname(); }`. `List<UserProjection> findBy();` — Spring Data создаёт proxy. Хорошо для скрытия полей.
  - **DTO-based (class)**: `record UserDto(String firstname, String lastname) {}`. Конструктор с правильными именами параметров. `@Query("SELECT new com.acme.UserDto(u.firstname, u.lastname) FROM User u")`. С Boot 3 / Spring Data JPA 3 — поддержка record-projections без явного `new` в JPQL.
  - **Dynamic projections**: `<T> List<T> findByLastname(String lastname, Class<T> type)`.
- **Specifications** — type-safe предикаты через JPA Criteria API. `Specification<T>` = `toPredicate(Root, CriteriaQuery, CriteriaBuilder)`. Репозиторий: `JpaSpecificationExecutor<T>`. `where(spec1.and(spec2).or(spec3))`. Composable: `Specification.where(hasStatus(status)).and(createdAtAfter(date))`.
- **QueryDSL** — альтернатива Criteria, генерирует Q-классы из entity-ов (через annotation processor `com.querydsl:querydsl-apt`). Type-safe, fluent API: `new JPAQueryFactory(em).selectFrom(user).where(user.status.eq(ACTIVE)).fetch()`. Преимущества над Specifications: чище, IDE auto-complete.
- **Auditing**:
  - `@CreatedDate` — дата создания, автоматически.
  - `@LastModifiedDate` — дата последнего обновления.
  - `@CreatedBy` / `@LastModifiedBy` — кто создал/обновил. Через `AuditorAware<T>`.
  - `@Version` — optimistic locking version.
  - Включается: `@EnableJpaAuditing(auditorAwareRef = "auditorAware")` на `@Configuration`.
  - Entity: `@EntityListeners(AuditingEntityListener.class)`.
- **`@EntityGraph`** — управляет fetch plan для query (аналог `fetch join`, но без JOIN — через entity graph API). `@EntityGraph(attributePaths = {"roles", "addresses"})`. `type = FETCH` (default, replace) или `LOAD` (добавить к default). Решает N+1.
- **N+1 problem**: `@OneToMany` lazy relation → при обращении к коллекции каждого из N родительских entity делается отдельный SQL. Решения: `@EntityGraph`, `fetch join` в `@Query` ("SELECT DISTINCT u FROM User u LEFT JOIN FETCH u.roles"), `@Fetch(FetchMode.SUBSELECT)` (Hibernate), batch fetching (`spring.jpa.properties.hibernate.default_batch_fetch_size=50`).
- **Custom repositories**:
  - **Fragment pattern** (Spring Data 2.x+): разделить интерфейс на куски. `UserRepository extends CrudRepository<User, Long>, UserRepositoryCustom`. `UserRepositoryCustom` — имплементация `UserRepositoryCustomImpl` (суффикс `Impl` конфигурируется). Spring Data комбинирует.
  - Кастомный суффикс: `@EnableJpaRepositories(repositoryImplementationPostfix = "CustomImpl")`.
- **`EntityManager` injection**: `@PersistenceContext private EntityManager em`. Использовать для Criteria API, native SQL с result mapping, bulk operations.
- **`@Transactional` на repository**: по умолчанию `CrudRepository` методы — `@Transactional` (read-only для find-ов). Override через `@Transactional` на методе своего интерфейса.
- **Query hints**: `@QueryHints({@QueryHint(name = "org.hibernate.fetchSize", value = "50"), @QueryHint(name = "org.hibernate.readOnly", value = "true")})`.
- **Locks**: `@Lock(LockModeType.PESSIMISTIC_WRITE)` на методе. `PESSIMISTIC_READ`, `PESSIMISTIC_WRITE`, `OPTIMISTIC`, `OPTIMISTIC_FORCE_INCREMENT`.
- **JPA Callbacks**: `@PrePersist`, `@PostPersist`, `@PreUpdate`, `@PostUpdate`, `@PreRemove`, `@PostRemove`, `@PostLoad`. На методах entity-класса или в отдельном `EntityListener`-классе (через `@EntityListeners(MyListener.class)`).
- **`JdbcTemplate` integration**: для batch operations, complex SQL, performance-critical queries. Spring Data JPA и `JdbcTemplate` могут сосуществовать (через `JdbcTemplate` bean или `JdbcOperations`).
- **`@NoRepositoryBean`** — помечает базовый интерфейс репозитория, чтобы Spring Data не пытался его инстанциировать.
- **`RepositoryFactoryBeanSupport` / `RepositoryFactorySupport`** — для создания своих Spring Data модулей (например, Spring Data JDBC построен на этой инфраструктуре).
- **Multiple `EntityManagerFactory`**: несколько БД. Каждая со своим `@EnableJpaRepositories(basePackages = "...", entityManagerFactoryRef = "...", transactionManagerRef = "...")`.
- **`@Transactional` propagation и self-invocation**: при вызове метода репозитория из другого метода того же сервиса, транзакция существует. Но если вызвать `@Transactional` метод сервиса из другого метода сервиса — транзакция создастся только если вызов через прокси (self-invocation проблема).
- **`OpenEntityManagerInViewFilter`**: держит `EntityManager` открытым на весь HTTP request, позволяя lazy-loading в controllers. Boot включает по умолчанию (`spring.jpa.open-in-view=true`). Anti-pattern: скрывает N+1, тратит connection на всю длительность request. **Рекомендуется отключать**.
- **Hibernate-specific properties**: `spring.jpa.properties.hibernate.format_sql=true`, `hibernate.jdbc.batch_size=50`, `hibernate.order_inserts=true`, `hibernate.order_updates=true`, `hibernate.jdbc.fetch_size=100`, `hibernate.jdbc.batch_versioned_data=true`, `hibernate.cache.use_second_level_cache=true` (с `JCacheRegionFactory`).

### Материалы

- **docs.spring.io — Spring Data JPA Reference** — `https://docs.spring.io/spring-data/jpa/reference/`
- **docs.spring.io — JPA Query Methods (query derivation)** — `https://docs.spring.io/spring-data/jpa/reference/jpa/query-methods.html`
- **docs.spring.io — JPA Repositories (Definitions, custom implementations)** — `https://docs.spring.io/spring-data/jpa/reference/repositories.html`
- **docs.spring.io — Projections** — `https://docs.spring.io/spring-data/jpa/reference/projections.html`
- **docs.spring.io — Specifications (Query by Example)** — `https://docs.spring.io/spring-data/jpa/reference/jpa/query-methods.html#jpa.query-methods.query-by-example`
- **docs.spring.io — JPA Entity Graphs** — `https://docs.spring.io/spring-data/jpa/reference/jpa/query-methods.html#jpa.entity-graph`
- **Spring blog — Advanced Spring Data JPA — Specifications and Querydsl** — `https://spring.io/blog/2011/04/26/advanced-spring-data-jpa-specifications-and-querydsl`
- **Baeldung — Spring Data JPA intro** — `https://www.baeldung.com/the-persistence-layer-with-spring-data-jpa`
- **Baeldung — Spring Data JPA @Query** — `https://www.baeldung.com/spring-data-jpa-query`
- **Baeldung — Spring Data JPA Projections** — `https://www.baeldung.com/spring-data-jpa-projections`
- **Baeldung — Spring Data JPA Custom Repository** — `https://www.baeldung.com/spring-data-jpa-method-in-all-repositories`
- **Baeldung — Avoiding N+1 in Spring Data JPA** — `https://www.baeldung.com/jpa-hibernate-fix-n-plus-1-problem`
- **Baeldung — Spring Data JPA Auditing** — `https://www.baeldung.com/database-auditing-jpa`
- **Baeldung — Hibernate Tips** — `https://www.baeldung.com/hibernate-4-spring`
- **Reflectoring — Spring Data JPA** — `https://reflectoring.io/spring-data-jpa/`
- **Thorben Janssen blog (JPA expert)** — `https://thorben-janssen.com/`
- **Книга — "Pro Spring 6", Chapter 9: Spring Data JPA** — Apress
- **Книга — "High-Performance Java Persistence" (Vlad Mihalcea)** — `https://vladmihalcea.com/books/high-performance-java-persistence/` (must-read для JPA performance)
- **Hibernate User Guide** — `https://docs.jboss.org/hibernate/orm/current/userguide/html_single/Hibernate_User_Guide.html`

### Практика / Кейсы

1. **Multi-tier repository с Specifications**: `UserRepository extends JpaRepository, JpaSpecificationExecutor`. Specifications: `hasRole`, `createdBetween`, `emailMatches`. Service composes через `Specification.where(spec1).and(spec2)`. Покрыть `@DataJpaTest` с Testcontainers. **Цель:** освоить composable specs.
2. **EntityGraph vs fetch join vs batch fetching**: тестировать N+1 сценарий с 3 подходами. Замерить SQL count через Hibernate statistics. Сравнить throughput на 1000 entity-ов. **Цель:** quantify N+1 fixes.
3. **QueryDSL integration**: подключить `querydsl-apt` для генерации Q-классов. Переписать Specifications на QueryDSL predicates. Сравнить читаемость и type-safety. **Цель:** type-safe queries.
4. **DTO projections с constructor expression**: `@Query("SELECT new com.acme.UserSummary(u.id, u.firstname, u.lastname, COUNT(o)) FROM User u LEFT JOIN u.orders o GROUP BY u")`. Сравнить с interface projection и record projection. **Цель:** выбрать правильный projection.
5. **Auditing + multi-tenancy**: `@CreatedBy`, `@LastModifiedBy` через `AuditorAware` (читает из SecurityContext). Tenant-id через `@Entity` listener + Hibernate filter (`@FilterDef`, `@Filter`). Покрыть тестом с двумя tenant-ами. **Цель:** understand auditing + filtering.
6. **Batch inserts/updates**: `hibernate.jdbc.batch_size=50` + `order_inserts=true` + `order_updates=true`. Замерить скорость bulk insert 10 000 entity-ов с/без батчинга. Сравнить с `JdbcTemplate.batchUpdate`. **Цель:** high-performance bulk operations.
7. **Pessimistic locking**: `@Lock(LockModeType.PESSIMISTIC_WRITE)` на `findByIdForUpdate`. Симулировать 2 concurrent transactions на одну запись — убедиться что вторая ждёт. Сравнить с optimistic locking через `@Version`. **Цель:** understand locking strategies.
8. **Custom fragment repository**: разделить `UserRepository extends JpaRepository, UserRepositoryCustom`. `UserRepositoryCustomImpl` с complex query через `EntityManager`. Зарегистрировать через дефолтный суффикс `Impl`. **Цель:** extend Spring Data.
9. **Two databases**: настроить 2 `EntityManagerFactory`, 2 `PlatformTransactionManager`, 2 `@EnableJpaRepositories` (с разными `basePackages`). Сервис работает с двумя БД в одной транзакции через `ChainedTransactionManager` (или JTA/Atomikos). **Цель:** multi-DB setup.
10. **Disable OpenEntityManagerInView**: `spring.jpa.open-in-view=false`. Выявить все LazyInitializationException, переписать на `@EntityGraph` или DTO projections. Замерить удержание connection в pool. **Цель:** fix OEIV anti-pattern.

---

## 8. Spring Data (другие хранилища)

### Теория

- **Spring Data project** — umbrella-проект с модулями для разных хранилищ: JDBC, JPA, MongoDB, Redis, Elasticsearch, Cassandra, Couchbase, Neo4j, GraphQL, REST, Envers, KeyValue, Map, Apache Geode, LDAP, для Apache Solr.
- **Common abstractions**:
  - `Repository<T, ID>` — базовый интерфейс.
  - `CrudRepository<T, ID>` — CRUD.
  - `PagingAndSortingRepository<T, ID>` — paging/sort.
  - Reactive variants: `ReactiveCrudRepository`, `ReactiveSortingRepository`.
  - `ReactiveQueryByExampleExecutor` для reactive.
- **Spring Data MongoDB**:
  - `MongoRepository<T, ID>` extends `PagingAndSortingRepository`, `QueryByExampleExecutor`.
  - `MongoTemplate` — низкоуровневый доступ. `find`, `findOne`, `insert`, `save`, `upsert`, `remove`, `aggregate`, `mapReduce`, `group`, `geoNear`.
  - Annotations: `@Document(collection = "users")`, `@Id`, `@Field("first_name")`, `@Indexed(unique=true)`, `@CompoundIndex`, `@DBRef`, `@TextIndexed`, `@GeoSpatialIndexed`, `@Version`.
  - Query methods: `findByFirstnameAndLastname`, `findByAddress_City` (nested).
  - `@Query` для JSON-queries: `@Query("{ 'status' : 'ACTIVE' }")`, с placeholder `?0`.
  - Full-text search через `TextQuery`.
  - Aggregation pipeline через `Aggregation` API.
  - Spring Data MongoDB Reactive: `ReactiveMongoRepository`, `ReactiveMongoTemplate`.
- **Spring Data Redis**:
  - `RedisTemplate<K, V>` — основной API. `opsForValue()`, `opsForList()`, `opsForSet()`, `opsForZSet()`, `opsForHash()`, `opsForStream()`, `opsForGeo()`, `opsForHyperLogLog()`.
  - `StringRedisTemplate` — удобная специализация для `<String, String>`.
  - `@RedisHash` + `RedisRepository<T, ID>` — абстракция поверх Redis как key-value store объектов.
  - Pub/Sub: `RedisMessageListenerContainer` + `MessageListener`.
  - Streams: `StreamMessage`, `XAdd`, `XRead`, consumer groups.
  - Connection factories: `JedisConnectionFactory` (Jedis), `LettuceConnectionFactory` (Lettuce, по умолчанию в Boot), Redis Sentinel, Redis Cluster.
  - Lettuce vs Jedis: Lettuce — non-blocking (Netty), thread-safe shared connection. Jedis — blocking, нужен pool.
- **Spring Data Elasticsearch**:
  - `ElasticsearchRepository<T, ID>` extends `CrudRepository`.
  - `ElasticsearchOperations` / `ElasticsearchTemplate` (deprecated) / `ReactiveElasticsearchOperations`.
  - Annotations: `@Document(indexName = "users")`, `@Id`, `@Field(type = FieldType.Keyword)`, `@Field(type = FieldType.Text, analyzer = "standard")`, `@MultiField(mainField = ..., otherFields = ...)`.
  - `NativeSearchQuery` / `CriteriaQuery` для сложных запросов.
  - Aggregations, suggesters, scripts.
  - Boot 3 — поддерживает Elasticsearch 8.x (new Java client `co.elastic.clients:elasticsearch-java`).
- **Spring Data JDBC** (не путать с JPA!):
  - Не ORM, а "lightweight object mapping" над JDBC.
  - `JdbcRepository<T, ID>` extends `CrudRepository`, `PagingAndSortingRepository`.
  - Без lazy loading, без N+1 (aggregate root loaded atomically), без dirty checking, без persistence context.
  - Aggregate root + value objects (DDD-style).
  - `@Table`, `@Column`, `@Id`, `@Version`, `@MappedCollection` (для коллекций в aggregate).
  - Конфигурация: `@EnableJdbcRepositories`.
  - Performance: проще и быстрее JPA для simple cases.
- **Spring Data JDBC + `JdbcTemplate`**:
  - `JdbcTemplate` — основной template для JDBC.
    - `query`, `queryForList`, `queryForObject`, `queryForMap`, `update`, `batchUpdate`, `execute`.
    - `RowMapper<T>` — маппинг ResultSet → объект.
    - `RowCallbackHandler` — обработка каждой строки без возврата.
    - `ResultSetExtractor<T>` — обработка всего ResultSet (для nested объектов).
    - `PreparedStatementSetter`, `BatchPreparedStatementSetter`.
    - Translation исключений через `SQLExceptionTranslator` → `DataAccessException` (через `SQLErrorCodeSQLExceptionTranslator`).
  - `NamedParameterJdbcTemplate` — обёртка с named params (`:name` вместо `?`). `MapSqlParameterSource`, `BeanPropertySqlParameterSource`.
  - `SimpleJdbcInsert`, `SimpleJdbcCall` — упрощения для INSERT и stored procedures.
- **Spring Data REST** — автоматически генерирует REST API из repository-ов (`@RepositoryRestResource`). `/users`, `/users/{id}`, search endpoints `/users/search/findByLastname?lastname=Smith`. HATEOAS. Часто не используется в production (нет контроля над API).
- **Spring Data Envers** — интеграция с Hibernate Envers для аудита (`@Audited`). Авто-генерация revision-таблиц.
- **Spring Data Cassandra**: `CassandraRepository<T, ID>`, `CassandraTemplate`. CQL-queries. Partition key, clustering key. `@Table`, `@PrimaryKey`, `@PrimaryKeyColumn`.
- **Spring Data Neo4j**: `Neo4jRepository`, `Neo4jTemplate`. Cypher queries. `@Node`, `@Relationship`.
- **Spring Data Couchbase**: `CouchbaseRepository`, `CouchbaseTemplate`. N1QL queries.
- **Spring Data KeyValue** — базовый модуль для key-value stores (in-memory `MapKeyValueAdapter`).
- **`@RepositoryRestController`** — кастомный controller для Spring Data REST endpoint-ов (вместо обычного `@RestController`).
- **Spring Data projections work for all modules**: interface-based, DTO, dynamic.
- **Auditing** для всех модулей: `@CreatedDate`, `@LastModifiedDate`, `@CreatedBy`, `@LastModifiedBy`. Включается через `@EnableJpaAuditing` (для JPA) или `@EnableMongoAuditing`, `@EnableRedisAuditing` и т.д.
- **Multi-store**: можно смешивать модули (JPA + MongoDB + Redis) в одном проекте. Каждый со своим `@Enable*Repositories`.

### Материалы

- **docs.spring.io — Spring Data MongoDB Reference** — `https://docs.spring.io/spring-data/mongodb/reference/`
- **docs.spring.io — Spring Data Redis Reference** — `https://docs.spring.io/spring-data/redis/reference/`
- **docs.spring.io — Spring Data Elasticsearch Reference** — `https://docs.spring.io/spring-data/elasticsearch/reference/`
- **docs.spring.io — Spring Data JDBC Reference** — `https://docs.spring.io/spring-data/relational/reference/jdbc.html`
- **docs.spring.io — Spring Data JDBC project page** — `https://spring.io/projects/spring-data-jdbc`
- **docs.spring.io — Spring Data Cassandra** — `https://docs.spring.io/spring-data/cassandra/reference/`
- **docs.spring.io — Spring Data Neo4j** — `https://docs.spring.io/spring-data/neo4j/reference/`
- **docs.spring.io — Spring Framework → Data Access → JDBC** — `https://docs.spring.io/spring-framework/reference/data-access/jdbc.html`
- **docs.spring.io — Spring Boot → Data → NoSQL** — `https://docs.spring.io/spring-boot/reference/data/nosql.html`
- **docs.spring.io — Spring Boot → Data → SQL** — `https://docs.spring.io/spring-boot/reference/data/sql.html`
- **Baeldung — Intro to Spring Data MongoDB** — `https://www.baeldung.com/spring-data-mongodb-tutorial`
- **Baeldung — Intro to Spring Data Redis** — `https://www.baeldung.com/spring-data-redis-tutorial`
- **Baeldung — Spring Data Elasticsearch** — `https://www.baeldung.com/spring-data-elasticsearch-tutorial`
- **Baeldung — Spring Data JDBC** — `https://www.baeldung.com/spring-data-jdbc`
- **Baeldung — A Guide to JdbcTemplate in Spring** — `https://www.baeldung.com/spring-jdbc-jdbctemplate`
- **Baeldung — Spring Data Redis Pub/Sub** — `https://www.baeldung.com/spring-data-redis-pub-sub`
- **Reflectoring — Spring Data JDBC** — `https://reflectoring.io/spring-data-jdbc/`
- **Redis Lettuce Wiki** — `https://github.com/lettuce-io/lettuce-core/wiki`
- **MongoDB Reference** — `https://www.mongodb.com/docs/manual/`
- **Книга — "Spring Data, Second Edition" (Mark Pollack et al.)** — O'Reilly (немного устарела по версиям, но концепции актуальны)
- **Книга — "Data with Spring, Second Edition" (Greg L. Turnquist)** — Apress

### Практика / Кейсы

1. **MongoDB aggregation pipeline**: реализовать отчёт "top-10 активных пользователей за последние 7 дней" через `Aggregation.match` → `group` → `sort` → `limit`. Покрыть Testcontainers MongoDB. **Цель:** освоить aggregations.
2. **Redis cache + pub/sub**: использовать `RedisTemplate` для кэша с TTL, pub/sub для инвалидации cache на других нодах. Сравнить с `@Cacheable`. **Цель:** понять pub/sub cache invalidation.
3. **Redis Streams**: consumer group для очереди задач, `XADD`, `XREADGROUP`, `XACK`. Сравнить с Kafka/RabbitMQ для лёгких use-cases. **Цель:** Redis как queue.
4. **Elasticsearch 8.x + Spring Data**: индексировать сущности из PostgreSQL в ES (CQRS-style), полнотекстовый поиск с `MultiMatch`, `fuzziness`, highlight. Synchronizer черезDebezium или outbox pattern. **Цель:** polyglot persistence.
5. **Spring Data JDBC vs JPA**: одну и ту же модель реализовать на JPA и Spring Data JDBC. Сравнить code size, performance (insert 10k records), N+1 риск. **Цель:** understand trade-offs.
6. **Multi-store app**: JPA (PostgreSQL) для транзакций, MongoDB для event log, Redis для cache. Транзакция — только в JPA. Настроить 3 `@Enable*Repositories` с разными `basePackages`. **Цель:** polyglot persistence.
7. **JdbcTemplate для bulk operations**: `batchUpdate` для вставки 100k строк. Сравнить с JPA `saveAll` + batch_size. Объяснить почему JdbcTemplate быстрее. **Цель:** performance critical path.
8. **Spring Data REST**: создать REST API из repository, отключить exposing определенных методов (`@RepositoryRestResource(exported = false)`). Сравнить с явным `@RestController`. **Цель:** rapid prototyping trade-off.

---

## 9. Spring TX (Transactions)

### Теория

- **Spring TX** — abstraction над transaction management (JDBC, JTA, Hibernate, JPA, R2DBC). Единый API для разных backends.
- **`PlatformTransactionManager`** — корневой интерфейс. `getTransaction(TransactionDefinition)`, `commit(TransactionStatus)`, `rollback(TransactionStatus)`.
- **Implementations**:
  - `DataSourceTransactionManager` — JDBC.
  - `JpaTransactionManager` — JPA (extends `DataSourceTransactionManager`).
  - `HibernateTransactionManager` — native Hibernate (legacy, JpaTransactionManager предпочтительнее).
  - `JtaTransactionManager` — JTA (Application Server, Atomikos, Bitronix).
  - `R2dbcTransactionManager` — reactive R2DBC.
  - `JmsTransactionManager` — JMS.
  - `ChainedTransactionManager` — несколько менеджеров в цепочке (deprecated в Spring 5.2+, рекомендуется XA/JTA для multi-DB consistency).
  - `DataSourceUtils.getConnection` — корректное получение connection в текущей транзакции.
- **`TransactionDefinition`**:
  - `getPropagationBehavior()` — REQUIRED, REQUIRES_NEW, NESTED, SUPPORTS, MANDATORY, NEVER, NOT_SUPPORTED.
  - `getIsolationLevel()` — DEFAULT, READ_UNCOMMITTED, READ_COMMITTED, REPEATABLE_READ, SERIALIZABLE.
  - `getTimeout()` — в секундах.
  - `isReadOnly()`.
  - `getName()`.
- **`@Transactional`** — declarative TX. Атрибуты: `value`/`transactionManager` (имя бина TM, для multi-DB), `propagation` (default REQUIRED), `isolation` (default DEFAULT), `timeout` (default -1 = use TM default), `readOnly` (default false), `rollbackFor` (default `RuntimeException` + `Error`), `rollbackForClassName`, `noRollbackFor`, `noRollbackForClassName`, `label` (Spring 5.3+, для фильтрации).
- **Propagation behaviors**:
  - **REQUIRED** (default) — если есть TX, использовать её; если нет — создать новую. Большинство методов.
  - **REQUIRES_NEW** — всегда создать новую TX, приостановить текущую (если есть). Использовать для audit logs (независимо от основной TX), но дорого (две DB connections).
  - **NESTED** — создать savepoint в текущей TX. Rollback только до savepoint. Не поддерживается всеми DB (PostgreSQL поддерживает через SAVEPOINT, MySQL да, Oracle да). Не работает с JTA.
  - **SUPPORTS** — если есть TX, использовать; если нет — выполнить без TX. Для read-методов.
  - **MANDATORY** — если есть TX, использовать; если нет — exception (`IllegalTransactionStateException`). Для принудительной TX-контекста.
  - **NEVER** — если есть TX, exception; если нет — выполнить без TX.
  - **NOT_SUPPORTED** — приостановить текущую TX (если есть), выполнить без TX. Для long-running операций вне TX.
- **Isolation levels**:
  - **DEFAULT** — уровень по умолчанию DB.
  - **READ_UNCOMMITTED** — грязное чтение (dirty read). Практически не используется.
  - **READ_COMMITTED** — no dirty reads, но non-repeatable reads и phantom reads возможны. Дефолт в PostgreSQL/Oracle/SQL Server.
  - **REPEATABLE_READ** — no dirty + no non-repeatable reads, но phantom reads возможны. Дефолт в MySQL InnoDB.
  - **SERIALIZABLE** — full isolation, no anomalies. Самый медленный.
  - Spring isolation level — это **hint**; реальный уровень зависит от DB и может быть выше (например, PostgreSQL не отличает REPEATABLE_READ от SERIALIZABLE в JDBC mapping).
- **Phenomena**:
  - Dirty read — TX1 читает uncommitted изменение TX2.
  - Non-repeatable read — TX1 читает row, TX2 modifies+commits, TX1 читает снова — другая data.
  - Phantom read — TX1 делает `SELECT WHERE age > 18`, TX2 inserts новый row matching, TX1 делает тот же SELECT — разные rows.
  - Lost update — TX1 и TX2 оба читают, оба modify, TX2 коммитит последним, потеряв изменения TX1.
- **Rollback rules**:
  - По умолчанию rollback на unchecked exceptions (`RuntimeException` и его подклассы) и `Error`.
  - **No rollback** на checked exceptions (`Exception` подклассы, не `RuntimeException`).
  - Это **непривычное** поведение для большинства Java-разработчиков.
  - Кастомизация: `@Transactional(rollbackFor = {SQLException.class, IOException.class})`, `@Transactional(noRollbackFor = IllegalArgumentException.class)`.
- **Self-invocation problem** — главная ловушка `@Transactional`:
  - `@Transactional` работает через **прокси** (AOP).
  - Если метод `A` в сервисе вызывает метод `B` того же сервиса через `this.b()`, вызов идёт **напрямую**, минуя прокси → `@Transactional` на `B` **не применяется**.
  - Решения: вынести `B` в другой bean, инжектить self-proxy (`@Autowired private MyService self; self.b()`), использовать `AopContext.currentProxy()` (с `@EnableAspectJAutoProxy(exposeProxy = true)`), использовать AspectJ CTW/LTW (но тогда вся `@Transactional` через aspect, не через прокси), программный TX через `TransactionTemplate`/`PlatformTransactionManager`.
- **JDBC vs JTA transactions**:
  - JDBC TX — локальная, одна DB, через `DataSourceTransactionManager`. Быстрая, простая.
  - JTA — распределённая, multi-resource (DB + JMS), через XA-протокол. Медленная, сложная, иногда "last resource commit optimization" для DB+JMS.
  - **Modern alternative**: outbox pattern для DB+JMS (события в DB-таблицу в той же TX, потом async polling to JMS).
- **`@EnableTransactionManagement`** — включает `@Transactional`. Атрибуты: `mode` (PROXY по умолчанию, ASPECTJ для compile-time weaving), `proxyTargetClass` (true = CGLIB), `order` (порядок TX-аспекта в chain).
- **`TransactionTemplate`** — программный TX. `template.execute(status -> { ... return result; })` или `template.executeWithoutResult(status -> { ... })`. Гибче, но менее декларативно.
- **`PlatformTransactionManager` напрямую**: `TransactionStatus tx = tm.getTransaction(definition); try { ... tm.commit(tx); } catch (Exception e) { tm.rollback(tx); }`.
- **`TransactionSynchronizationManager`** — связывает TX с потоком через `ThreadLocal`. `registerSynchronization(TransactionSynchronization)` — колбэки `beforeCommit`, `afterCommit`, `beforeCompletion`, `afterCompletion`, `suspend`, `resume`. Полезно для: отправки событий после коммита, очистки кэша, отправки JMS после DB commit.
- **`@TransactionalEventListener(phase = AFTER_COMMIT)`** — alternative way: событие публикуется внутри TX, listener срабатывает после коммита. Не сработает если rollback. Если listener бросает exception — **не** откатывает основную TX.
- **`ChainedTransactionManager`** (multi-DB):
  - `new ChainedTransactionManager(tm1, tm2)`.
  - В одной `@Transactional` можно работать с двумя DB.
  - **Важно**: это **not XA** — если commit на tm1 succeeds, но commit на tm2 fails, данные теряются (best-effort 1PC).
  - Deprecated в Spring 5.2+ именно из-за этого. Для real consistency — JTA (Atomikos/Bitronix) или outbox pattern.
- **`@Transactional` readOnly**:
  - Hint для persistence provider (Hibernate может skip dirty checking).
  - На уровне DB: `SET TRANSACTION READ ONLY`, может направлять на read-replica (если драйвер поддерживает).
  - В JPA: может включить `FlushMode.MANUAL`.
- **`@Transactional` timeout**:
  - В секундах. Применяется на уровне transaction manager (не statement timeout).
  - Для statement timeout: `@QueryHints(@QueryHint(name = "org.hibernate.timeout", value = "30"))` или JPA `javax.persistence.query.timeout`.
- **`TransactionRequiredException`** — если JPA operation (modify) выполняется вне TX.
- **Reactive transactions**: `TransactionalOperator` — reactive аналог. `mono.as(operator::transactional)`. `TransactionalOperator.create(tm)`. Поддерживает R2DBC, reactive MongoDB.
- **Best practices**:
  - `@Transactional` на **service** layer, не на repository (Spring Data JPA уже добавляет), не на controller.
  - На class-level + override на method-level для особых случаев.
  - Specific методам — specific TX (например, `@Transactional(readOnly = true)` для find-ов).
  - Избегать `REQUIRES_NEW` кроме случаев audit log.
  - Чёткие rollback rules.
  - Beware self-invocation.

### Материалы

- **docs.spring.io — Spring Framework → Data Access → Transaction Management** — `https://docs.spring.io/spring-framework/reference/data-access/transaction.html`
- **docs.spring.io — Declarative transaction management** — `https://docs.spring.io/spring-framework/reference/data-access/transaction/declarative.html`
- **docs.spring.io — @Transactional propagation** — `https://docs.spring.io/spring-framework/reference/data-access/transaction/declarative/tx-propagation.html`
- **docs.spring.io — Programmatic transaction management** — `https://docs.spring.io/spring-framework/reference/data-access/transaction/programmatic.html`
- **docs.spring.io — Spring Boot → Transaction Management** — `https://docs.spring.io/spring-boot/reference/data/sql.html#data.sql.jpa-and-spring-data.transactions`
- **Baeldung — Spring @Transactional propagation and isolation** — `https://www.baeldung.com/spring-transactional-propagation-isolation`
- **Baeldung — Transaction configuration with JPA and Spring** — `https://www.baeldung.com/transaction-configuration-with-jpa-and-spring`
- **Baeldung — Spring programmatic transaction management** — `https://www.baeldung.com/spring-programmatic-transaction-management`
- **Baeldung — Spring self-invocation with @Transactional** — `https://www.baeldung.com/spring-self-injection`
- **Spring blog — Transaction synchronization** — `https://spring.io/blog/2018/02/26/spring-tips-the-basics-of-spring-framework-transaction-management`
- **Vlad Mihalcea — A beginner's guide to transaction isolation levels** — `https://vladmihalcea.com/tutorial-jpa/`
- **Vlad Mihalcea — The best way to handle the N+1 problem** — `https://vladmihalcea.com/n-plus-1-query-problem/`
- **Vlad Mihalcea — Transactions** — `https://vladmihalcea.com/transactions/`
- **PostgreSQL transaction isolation docs** — `https://www.postgresql.org/docs/current/transaction-iso.html`
- **Reflectoring — Spring Transactions** — `https://reflectoring.io/spring-transactional/`
- **Martin Fowler — Outbox pattern** — `https://microservices.io/patterns/data/transactional-outbox.html`
- **Книга — "Pro Spring 6", Chapter 12: Transactions** — Apress
- **Книга — "Database Internals" (Alex Petrov)** — O'Reilly (по изоляции и consensus)

### Практика / Кейсы

1. **Self-invocation trap**: класс с `@Transactional public void saveUser()` который вызывает `private void audit()` (тоже `@Transactional(propagation = REQUIRES_NEW)`). Демонстрировать что новая TX **не создаётся**. Решить тремя способами. Покрыть тестом через `TransactionSynchronizationManager.isActualTransactionActive()`. **Цель:** глубоко понять.
2. **REQUIRES_NEW для audit log**: сохранить order в основной TX, audit log в отдельной TX (`REQUIRES_NEW`). Если основная rollback — audit уже сохранён. Если audit fail — основной rollback. Демонстрировать с двумя exceptions. **Цель:** propagation semantics.
3. **NESTED с savepoint**: PostgreSQL `SAVEPOINT` через `propagation = NESTED`. Внутри try-catch откатить до savepoint через `TransactionAspectSupport.currentTransactionStatus().setRollbackOnly()`... — объяснить почему это не работает, нужен `TransactionInterceptor` workaround. Использовать `TransactionTemplate` программно. **Цель:** deep dive NESTED.
4. **Multi-DB через outbox pattern**: основная TX пишет order в PostgreSQL + audit event в outbox-таблицу (та же TX). Async worker читает outbox и пушит в Kafka (at-least-once). Покрыть Testcontainers + EmbeddedKafka. **Цель:** reliable cross-system messaging.
5. **`TransactionSynchronization.afterCommit`**: отправлять welcome email после успешной регистрации (только если TX commits). Реализовать через `TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() { ... afterCommit() { ... } })`. Сравнить с `@TransactionalEventListener(AFTER_COMMIT)`. **Цель:** post-commit actions.
6. **Isolation level demo**: PostgreSQL с READ_COMMITTED vs REPEATABLE_READ. Симулировать non-repeatable read и phantom read через 2 concurrent transactions. Замерить производительность. **Цель:** feel isolation levels.
7. **Reactive transaction через `TransactionalOperator`**: R2DBC service с двумя операциями в одной TX. Симулировать exception → rollback обеих. Сравнить с `@Transactional` (не работает в reactive). **Цель:** reactive transactions.
8. **`@TransactionalEventListener` pipeline**: publish domain event в service (`applicationEventPublisher.publishEvent`), listener с фазами `BEFORE_COMMIT`, `AFTER_COMMIT`, `AFTER_ROLLBACK`, `AFTER_COMPLETION`. Демонстрировать, что `AFTER_ROLLBACK` не вызывается при коммите. **Цель:** understand event-TX interaction.
9. **Optimistic locking с `@Version`**: `User` с `@Version Long version`. 2 concurrent updates одного user — один `OptimisticLockException`. Handler: retry 3 раза с exponential backoff. **Цель:** optimistic locking strategy.

---

## 10. Spring Security

### Теория

- **Spring Security** — framework для authentication, authorization, protection against common attacks (CSRF, XSS, session fixation, clickjacking). De-facto стандарт для Java web security.
- **Authentication** — проверка who you are (credentials).
- **Authorization** — проверка what you can do (permissions/roles).
- **Core abstractions**:
  - `Authentication` — token, представляющий authenticated principal (после login) или unauthenticated credentials (до login). Содержит `Principal`, `Credentials`, `Authorities`, `isAuthenticated()`, `getDetails()`.
  - `SecurityContext` — содержит `Authentication`. `SecurityContextHolder.getContext()`. По умолчанию — `ThreadLocal` стратегия.
  - `SecurityContextHolder` — статический доступ к `SecurityContext`. Стратегии: `MODE_THREADLOCAL` (default), `MODE_INHERITABLETHREADLOCAL` (для child threads), `MODE_GLOBAL`.
  - `GrantedAuthority` — permission/role. `ROLE_ADMIN`, `user:read`. `SimpleGrantedAuthority`.
  - `UserDetails` — Spring's user representation. `getUsername`, `getPassword`, `getAuthorities`, `isAccountNonExpired`, `isAccountNonLocked`, `isCredentialsNonExpired`, `isEnabled`.
  - `UserDetailsService` — service to load user by username. `loadUserByUsername(String)`.
  - `AuthenticationManager` — entry point for authentication. `authenticate(Authentication)` → `Authentication` (populated) or throws `AuthenticationException`.
  - `ProviderManager` — основной implementation, делегирует к списку `AuthenticationProvider`-ов.
  - `AuthenticationProvider` — strategy для конкретного типа authentication (`DaoAuthenticationProvider` для username/password, `JwtAuthenticationProvider`, `OAuth2LoginAuthenticationProvider`).
  - `PasswordEncoder` — encode passwords. `BCryptPasswordEncoder` (по умолчанию), `Argon2PasswordEncoder` (рекомендуется для новых проектов, memory-hard), `Pbkdf2PasswordEncoder`, `SCryptPasswordEncoder`, `DelegatingPasswordEncoder` (multi-algorithm с prefix `"{bcrypt}..."`).
- **Servlet filter chain**:
  - Запрос проходит через `FilterChainProxy` (DelegatingFilterProxy в web.xml era) → `SecurityFilterChain`.
  - `SecurityFilterChain` — список `Filter`-ов. В Spring Security 6 — `SecurityFilterChain` bean(s), настраивается через `SecurityFilterChain` lambda (стало стандартом в Spring Security 6, заменило `WebSecurityConfigurerAdapter` deprecated с 5.7+).
  - Основные filters (в порядке): `DisableEncodeUrlFilter`, `ForceEagerSessionCreationFilter`, `ChannelProcessingFilter`, `WebAsyncManagerIntegrationFilter`, `SecurityContextHolderFilter`, `HeaderWriterFilter`, `CorsFilter`, `CsrfFilter`, `LogoutFilter`, `OAuth2AuthorizationRequestRedirectFilter`, `Saml2WebSsoAuthenticationFilter`, `UsernamePasswordAuthenticationFilter`, `DefaultLoginPageGeneratingFilter`, `DefaultLogoutPageGeneratingFilter`, `ConcurrentSessionFilter`, `DigestAuthenticationFilter`, `BearerTokenAuthenticationFilter` (для JWT/OAuth2 Resource Server), `BasicAuthenticationFilter`, `RequestCacheAwareFilter`, `SecurityContextHolderAwareRequestFilter`, `AnonymousAuthenticationFilter`, `SessionManagementFilter`, `ExceptionTranslationFilter`, `FilterSecurityInterceptor` (или `AuthorizationFilter` в Spring Security 6).
  - Каждый filter делает специфичную задачу: CSRF, headers, logout, login, session, auth translation, authz.
- **`SecurityFilterChain` конфигурация** (Spring Security 6):
  ```java
  @Bean
  SecurityFilterChain apiFilterChain(HttpSecurity http) throws Exception {
      return http
          .csrf(csrf -> csrf.disable())
          .authorizeHttpRequests(auth -> auth
              .requestMatchers("/api/public/**").permitAll()
              .requestMatchers("/api/admin/**").hasRole("ADMIN")
              .anyRequest().authenticated())
          .sessionManagement(s -> s.sessionCreationPolicy(STATELESS))
          .oauth2ResourceServer(o -> o.jwt(Customizer.withDefaults()))
          .build();
  }
  ```
- **`HttpSecurity` DSL** — fluent API. CSRF, CORS, headers, sessionManagement, authorizeHttpRequests, formLogin, httpBasic, oauth2Login, oauth2ResourceServer, saml2Login, rememberMe, logout, anonymous, exceptionHandling, addFilter, addFilterBefore, addFilterAfter, requestCache, portMapper, securityContext.
- **Method security**:
  - `@EnableMethodSecurity` (Spring Security 6, заменил `@EnableGlobalMethodSecurity`).
  - `@PreAuthorize("hasRole('ADMIN')")`, `@PreAuthorize("#username == authentication.principal.username")` (SpEL).
  - `@PostAuthorize("returnObject.owner == authentication.principal.username")`.
  - `@PostFilter("filterObject.owner == authentication.principal.username")` — фильтрация return collection.
  - `@PreFilter("filterObject.owner == authentication.principal.username")` — фильтрация input collection.
  - `@Secured("ROLE_ADMIN")` (legacy, не поддерживает SpEL).
  - `@RolesAllowed("ADMIN")` (JSR-250).
  - Работает через AOP (`MethodSecurityInterceptor` / `AuthorizationManagerBeforeMethodInterceptor`).
  - В Boot 3.2+ поддерживает создание новых `AuthorizationManager` через кастомные beans.
- **OAuth2 Resource Server (JWT)**:
  - `spring-boot-starter-oauth2-resource-server`.
  - `spring.security.oauth2.resourceserver.jwt.issuer-uri=https://accounts.google.com` или `jwk-set-uri`.
  - JWT validation: signature (через JWK), `iss`, `aud`, `exp`, `nbf`.
  - `BearerTokenAuthenticationFilter` извлекает `Authorization: Bearer ...` header.
  - Конвертация claims в `Authentication` через `JwtAuthenticationConverter` + `Converter<Jwt, AbstractAuthenticationToken>` кастомизация.
- **OAuth2 Login (Authorization Code Grant)**:
  - `spring-boot-starter-oauth2-client`.
  - `spring.security.oauth2.client.registration.google.client-id=...`, `client-secret=...`.
  - `http.oauth2Login()` в `SecurityFilterChain`.
  - `OAuth2AuthorizedClientService` для доступа к токенам.
  - PKCE для public clients (Spring Security 6+).
- **OAuth2 Authorization Server** (новый, Spring Authorization Server):
  - `spring-boot-starter-oauth2-authorization-server`.
  - Реализует Authorization Code, Client Credentials, Refresh Token, Device Code grants.
  - Заменил устаревший Spring Security OAuth (проект закрыт).
- **SAML 2.0**:
  - `spring-boot-starter-security` + `spring-security-saml2-service-provider`.
  - `saml2Login()` в `SecurityFilterChain`.
  - Service Provider metadata, Identity Provider (Okta, ADFS, Shibboleth).
  - Подписанные/зашифрованные assertions.
- **Password encoding**:
  - `BCryptPasswordEncoder(strength=10)` — адаптивный, замедляется.
  - `Argon2PasswordEncoder` (Argon2id) — winner of Password Hashing Competition 2015, memory-hard, recommended.
  - `Pbkdf2PasswordEncoder` — NIST-approved.
  - `SCryptPasswordEncoder` — memory-hard.
  - `DelegatingPasswordEncoder` — default в Boot 2+, поддерживает multiple algorithms через prefix `{bcrypt}$2a$10$...`, `{argon2}@argon2id$...`. Позволяет мигрировать пользователей на новый алгоритм лениво (при login).
  - **NoOpPasswordEncoder** — plain text, deprecated, использовать только в тестах.
- **CSRF protection**:
  - По умолчанию включен для state-changing HTTP methods (POST, PUT, DELETE, PATCH).
  - `CsrfToken` в session, exposed через `_csrf` parameter или `X-CSRF-TOKEN` header.
  - В SPA (React/Angular) — cookie-based: `CookieCsrfTokenRepository.withHttpOnlyFalse()`.
  - Для stateless JWT APIs — обычно отключают (`csrf.disable()`).
- **CORS**:
  - `CorsConfigurationSource` bean + `http.cors(c -> c.configurationSource(...))`.
  - Spring Security CORS имеет приоритет над другими filter chains (для preflight OPTIONS).
  - В Boot — `WebMvcConfigurer.addCorsMappings` может конфликтовать с Security CORS.
- **Session management**:
  - `SessionCreationPolicy`: ALWAYS, NEVER, IF_REQUIRED (default), STATELESS.
  - Для stateless JWT — `STATELESS`.
  - `sessionManagement.maximumSessions(1).maxSessionsPreventsLogin(true)` — concurrent session control.
  - `SessionManagementFilter` — fixed session ID on auth (session fixation protection).
- **Exception handling**:
  - `AuthenticationEntryPoint` — для unauthenticated requests (401). `Http403ForbiddenEntryPoint`, `BasicAuthenticationEntryPoint`, `BearerTokenAuthenticationEntryPoint`, кастомный.
  - `AccessDeniedHandler` — для authenticated-but-unauthorized (403). `AccessDeniedHandlerImpl` (default), кастомный.
  - `ExceptionTranslationFilter` — центральный filter, который ловит `AuthenticationException` и `AccessDeniedException`, делегирует к handlers.
- **Multi-tenancy**:
  - Per-tenant `SecurityFilterChain` (chain selection по path / header).
  - Tenant-aware `UserDetailsService`.
  - JWT с tenant claim.
- **`SecurityFilterChain` selection**: `RequestMatcher` определяет какую цепочку применить. Несколько `SecurityFilterChain` beans, отсортированы по `@Order`.
- **`RsaKeyPairGenerator` / JWK**: для local JWT signing, генерировать RSA ключи и использовать `NimbusJwtDecoder.withPublicKey(...)` / `NimbusJwtEncoder`.
- **Pre-authenticated scenarios**: `@EnableWebSecurity` + `PreAuthenticatedAuthenticationProvider` + `RequestHeaderAuthenticationFilter` для SSO fronted by API gateway.
- **Method security domain-specific**: `@AuthorizeRequests("...")
 @PreAuthorize("@orderService.canAccessOrder(#id, authentication)")` — вызов bean-метода в SpEL.
- **Reactive Spring Security** (`WebFlux`): `SecurityWebFilterChain` (аналогично, но reactive). `@EnableWebFluxSecurity`, `@EnableReactiveMethodSecurity`. `ReactiveAuthenticationManager`, `ServerSecurityContextRepository`.
- **`@RegisteredOAuth2AuthorizedClient`** — для инъекции OAuth2 authorized client в controller-метод (для outbound OAuth2 calls).

### Материалы

- **docs.spring.io — Spring Security Reference** — `https://docs.spring.io/spring-security/reference/`
- **docs.spring.io — Servlet applications architecture** — `https://docs.spring.io/spring-security/reference/servlet/architecture.html`
- **docs.spring.io — Authorization (HttpServletRequest)** — `https://docs.spring.io/spring-security/reference/servlet/authorization/authorize-http-requests.html`
- **docs.spring.io — Method Security** — `https://docs.spring.io/spring-security/reference/servlet/authorization/method-security.html`
- **docs.spring.io — OAuth2 Resource Server JWT** — `https://docs.spring.io/spring-security/reference/servlet/oauth2/resource-server/jwt.html`
- **docs.spring.io — OAuth2 Login** — `https://docs.spring.io/spring-security/reference/servlet/oauth2/login/index.html`
- **docs.spring.io — SAML 2.0 Login** — `https://docs.spring.io/spring-security/reference/servlet/saml2/index.html`
- **docs.spring.io — Password Storage** — `https://docs.spring.io/spring-security/reference/features/authentication/password-storage.html`
- **docs.spring.io — CSRF** — `https://docs.spring.io/spring-security/reference/servlet/exploits/csrf.html`
- **docs.spring.io — Session Management** — `https://docs.spring.io/spring-security/reference/servlet/authentication/session-management.html`
- **docs.spring.io — Spring Authorization Server** — `https://docs.spring.io/spring-authorization-server/docs/current/reference/`
- **Baeldung — Spring Security Form Login** — `https://www.baeldung.com/spring-security-login`
- **Baeldung — Spring Security JWT** — `https://www.baeldung.com/spring-security-oauth-jwt`
- **Baeldung — Spring Security OAuth2 Resource Server** — `https://www.baeldung.com/spring-security-oauth-resource-server`
- **Baeldung — Method Security with Spring Security** — `https://www.baeldung.com/spring-security-method-security`
- **Baeldung — Spring Security Password Encoding** — `https://www.baeldung.com/spring-security-password-encoding`
- **Spring Security blog — What's new in Spring Security 6** — `https://spring.io/blog/2022/10/31/spring-security-6-0`
- **Reflectoring — Spring Security essentials** — `https://reflectoring.io/spring-security/`
- **OWASP — Password Storage Cheat Sheet** — `https://cheatsheetseries.owasp.org/cheatsheets/Password_Storage_Cheat_Sheet.html`
- **RFC 7519 — JSON Web Token (JWT)** — `https://datatracker.ietf.org/doc/html/rfc7519`
- **RFC 6749 — OAuth 2.0** — `https://datatracker.ietf.org/doc/html/rfc6749`
- **Книга — "Pro Spring Security" (Carlo Scarioni)** — Apress
- **Книга — "Learning Spring Security" (Mert Caliskan, SnakeBytesBites)** — Packt

### Практика / Кейсы

1. **JWT-based REST API security**: настроить `oauth2ResourceServer().jwt()`. Реализовать `JwtAuthenticationConverter` который извлекает roles из `realm_access.roles` claim (Keycloak-style). Покрыть тестами через `SecurityMockMvcRequestPostProcessors.jwt()`. **Цель:** JWT authorization.
2. **OAuth2 Login с Google + own UserDetailsService**: после успешного OAuth2 login — синхронизировать user в local DB (`OAuth2UserService<OAuth2UserRequest, OAuth2User>`), merge с local data. Handle first-time login (registration flow). **Цель:** OAuth2 + local user merge.
3. **Method security с SpEL**: `@PreAuthorize("@orderService.isOwner(#orderId, authentication)")` — SpEL вызывает bean-метод. Покрыть тестом с mock user. Сравнить с programmatic check `if (!orderService.isOwner(...)) throw new AccessDeniedException()`. **Цель:** idiomatic method security.
4. **Stateless JWT + refresh token**: реализовать `/auth/login` → access token (15min) + refresh token (7d, в httpOnly cookie). `/auth/refresh` → new access token. Refresh tokens в Redis для revocation. Покрыть тестами. **Цель:** real-world JWT flow.
5. **Multi-tenancy через `SecurityFilterChain`**: 2 chains — `/api/tenant-a/**` с tenant-A UserDetailsService, `/api/tenant-b/**` с tenant-B. `@Order` для правильного match. JWT с `tenant_id` claim. **Цель:** multi-tenant security.
6. **CSRF в SPA (React)**: настроить `CookieCsrfTokenRepository.withHttpOnlyFalse()`, frontend читает `XSRF-TOKEN` cookie и шлёт `X-XSRF-TOKEN` header. Демонстрировать что POST без CSRF token отклоняется. **Цель:** CSRF for SPA.
7. **Password migration с `DelegatingPasswordEncoder`**: старые пароли в legacy format (SHA-256 + salt), новые в BCrypt. `DelegatingPasswordEncoder` с `{sha256}` и `{bcrypt}` prefixes. При login старого user — re-hash в BCrypt. Покрыть тестом миграцию. **Цель:** graceful password migration.
8. **Spring Authorization Server как own IdP**: поднять свой OAuth2 Authorization Server с Authorization Code + PKCE, client credentials grant. Использовать в resource-server-приложении. Демонстрировать end-to-end flow. **Цель:** become own IdP.
9. **Method security в reactive (WebFlux)**: `@EnableReactiveMethodSecurity` + `@PreAuthorize` на reactive service методах. Сравнить с servlet-версией. Демонстрировать что SpEL работает с `Mono` return types. **Цель:** reactive security.
10. **Audit log через Spring Security**: `AuthenticationSuccessEvent`, `AuthenticationFailureBadCredentialsEvent`, `AuthorizationDeniedEvent`. `@EventListener` → audit log в DB/ELK. Использовать `SpringSecurityAuditLogger`. **Цель:** security audit trail.

---

## 11. Spring Boot Actuator & Micrometer

### Теория

- **Spring Boot Actuator** — production-ready features: endpoints, metrics, health checks, loggers management, info, thread dumps, env. Входит в `spring-boot-starter-actuator`.
- **Endpoints** — HTTP/JMX-exposed operations. По умолчанию Boot 3: только `/health` exposed через HTTP. Остальные через JMX или нужно явно включить `management.endpoints.web.exposure.include=*` или список.
- **Endpoint configuration**:
  - `management.endpoints.enabled-by-default` — disable all.
  - `management.endpoint.<name>.enabled=true/false`.
  - `management.endpoints.web.exposure.include` / `exclude`.
  - `management.endpoints.web.base-path=/actuator` (default).
  - `management.endpoints.web.path-mapping.<name>=/custom-path`.
  - `management.endpoints.jmx.exposure.include`.
  - `management.server.port=9090` — отдельный management port (security isolation).
- **`/health` endpoint**:
  - Агрегация `HealthIndicator`-ов: `DB` (DataSource), `DiskSpace`, `Ping`, `Liveness` (Boot 2.3+), `Readiness` (Boot 2.3+), `Redis`, `Mongo`, `Rabbit`, `Elasticsearch`, `Cassandra`, etc.
  - `Health` object: `Status` (UP, DOWN, OUT_OF_SERVICE, UNKNOWN) + details.
  - `management.endpoint.health.show-details=always|never|when-authorized`.
  - `management.endpoint.health.show-components=...`.
  - Custom: `@Component class MyHealthIndicator implements HealthIndicator { Health health() {...} }`.
  - Группы (Boot 2.2+): `management.endpoint.health.group.liveness.include=livenessState,ping`, `management.endpoint.health.group.readiness.include=readinessState,db`.
- **Probes (liveness/readiness)** (Boot 2.3+):
  - `/health/liveness` — k8s liveness probe (живо ли приложение — restart если нет).
  - `/health/readiness` — k8s readiness probe (готово ли принимать трафик — добавить в load balancer).
  - `ApplicationAvailability` bean, `AvailabilityState` (`LivenessState` — CORRECT/BROKEN, `ReadinessState` — ACCEPTING_TRAFFIC/REFUSING_TRAFFIC).
  - Авто-обновления: `ApplicationStartedEvent` → `ReadinessState.ACCEPTING_TRAFFIC`, `ApplicationFailedEvent` → `LivenessState.BROKEN`, `ContextClosedEvent` → `ReadinessState.REFUSING_TRAFFIC`.
- **`/info` endpoint**:
  - Build info (`spring-boot-maven-plugin:build-info` → `META-INF/build-info.properties`): group, artifact, name, version, build time.
  - Git info (`git-commit-id-plugin` → `git.properties`).
  - Java runtime info.
  - Custom: `InfoContributor` bean.
- **`/metrics` endpoint**:
  - Список всех metrics: `GET /actuator/metrics`.
  - Specific metric с tags: `GET /actuator/metrics/http.server.requests?tag=status:200&tag=uri:/api/users`.
  - Источник: `MeterRegistry` из Micrometer.
- **Micrometer** — facade для metrics (как SLF4J для logging). Multiple registries: Prometheus, Datadog, New Relic, Dynatrace, CloudWatch, Stackdriver, Atlas, Graphite, Influx, JMX, Elastic, OTLP, SignalFx, StatsD, Wavefront.
  - `MeterRegistry` — корневой интерфейс.
  - `CompositeMeterRegistry` — агрегация нескольких registries.
- **Meter types**:
  - **Counter** — монотонно возрастающее число. `Counter.builder("http.requests").tag("status", "200").register(registry).increment()`. Использовать для count of events.
  - **Gauge** — мгновенное значение. `Gauge.builder("queue.size", queue::size).register(registry)`. Использовать для current state.
  - **Timer** — распределение длительностей. `Timer.Sample sample = Timer.start(); ... sample.stop(Timer.builder("http.requests").register(registry));`. Или `timer.record(() -> ...)`. Гистограмма percentiles.
  - **Distribution Summary** — распределение arbitrary значений (не только durations). `DistributionSummary.builder("order.amount").register(registry).record(amount)`.
  - **LongTaskTimer** — для активных long-running задач (сколько сейчас in-flight).
  - **FunctionCounter**, **FunctionTimer**, **TimeGauge** — производные от функций.
- **Built-in metrics** (Boot 3):
  - JVM: `jvm.memory.used`, `jvm.threads.live`, `jvm.gc.pause`, `jvm.classes.loaded`, `jvm.buffer.memory.used`.
  - System: `system.cpu.usage`, `process.cpu.usage`, `process.start.time`, `system.load.average.1m`.
  - HTTP: `http.server.requests`, `http.client.requests`.
  - Tomcat: `tomcat.sessions.active`, `tomcat.threads.busy`.
  - JDBC: `jdbc.connections.active`, `jdbc.connections.idle`.
  - HikariCP: `hikaricp.connections.active`, `hikaricp.connections.pending`.
  - Logback: `logback.events` (counter по уровням).
  - Uptime: `process.uptime`.
  - Boot startup: `application.started.time`.
- **Custom metrics**:
  - `@Timed` аннотация на controller-методе (`@Timed(value = "api.users.create", percentiles = {0.5, 0.95, 0.99})`).
  - `MeterRegistryCustomizer` bean — глобальная конфигурация (например, добавление common tags).
  - Прямой `MeterRegistry` injection в сервисы.
- **Common tags**: `management.metrics.tags.application=my-app`, `management.metrics.tags.region=eu-west-1` — добавляются ко всем metrics.
- **Prometheus integration**:
  - `micrometer-registry-prometheus`.
  - Endpoint `/actuator/prometheus` — Prometheus exposition format.
  - Scrape config в Prometheus: `job_name: 'spring-boot-apps'`, `metrics_path: '/actuator/prometheus'`, `static_configs` или service discovery.
  - **Histograms vs summaries**: `_bucket` для histogram quantile approximation (точнее для percentiles). `management.metrics.distribution.percentiles-histogram.http.server.requests=true` включает.
  - SLO (Service Level Objectives): `management.metrics.distribution.slo.http.server.requests=50ms,100ms,200ms,500ms`.
- **Tracing** (Micrometer Tracing, заменил Spring Cloud Sleuth):
  - `micrometer-tracing-bridge-brave` (для Zipkin через Brave).
  - `micrometer-tracing-bridge-otlp` (для OpenTelemetry).
  - `management.tracing.sampling.probability=1.0` (100% sampling; для prod — 0.1).
  - `management.tracing.enabled=true`.
  - `management.endpoints.web.exposure.include=prometheus,health,metrics,tracing`.
  - **Trace IDs**: автоматически инжектируются в MDC → логи содержат `traceId`, `spanId`.
  - Propagation: W3C Trace Context (по умолчанию), B3 (для legacy Zipkin).
- **OTLP exporter** (Spring Boot 3.2+):
  - `management.otlp.tracing.endpoint=http://otel-collector:4318/v1/traces`.
  - `management.tracing.export.otlp.enabled=true`.
  - Может слать в Tempo, Jaeger, Datadog, Honeycomb, etc.
- **Brave (Zipkin) exporter**:
  - `management.zipkin.tracing.endpoint=http://zipkin:9411/api/v2/spans`.
- **MDC (Mapped Diagnostic Context)** в Logback:
  - Tracing bridge автоматически кладёт `traceId`/`spanId` в MDC.
  - В `logback-spring.xml`: `%d{yyyy-MM-dd HH:mm:ss} [%thread] [%X{traceId},%X{spanId}] %-5level %logger - %msg%n`.
- **Custom `@Endpoint`**:
  - `@Endpoint(id = "featureflags")`, `@ReadOperation`, `@WriteOperation`, `@DeleteOperation`, `@Selector`.
  - `@WebEndpoint` — только HTTP.
  - `@JmxEndpoint` — только JMX.
- **`InfoContributor`** — добавить данные в `/info`. `Map<String, Object> contribute()`.
- **`HealthIndicator`** — добавить health check. `Health health()`.
- **`ReactiveHealthIndicator`** — для WebFlux, возвращает `Mono<Health>`.
- **Loggers management**: `GET /actuator/loggers` — список. `POST /actuator/loggers/com.acme.level` с body `{"configuredLevel": "DEBUG"}` — runtime change. Не требует restart.
- **`/env` endpoint**: показывает все `PropertySource`-ы, маскирует секреты (`management.endpoint.env.show-values=NEVER|ALWAYS|WHEN_AUTHORIZED`).
- **`/threaddump`**: JVM thread dump в text или JSON. `GET /actuator/threaddump`.
- **`/heapdump`**: скачивает HPROF. `GET /actuator/heapdump`.
- **`/configprops`**: все `@ConfigurationProperties` биндинги.
- **`/beans`**: все bean-ы, их зависимости, scope.
- **`/mappings`**: все HTTP endpoints (servlet + reactive).
- **`/startup`**: startup steps (через `BufferingApplicationStartup`).
- **`/scheduledtasks`**: все `@Scheduled` методы.
- **`/caches`**: список кэшей, можно очистить через `DELETE /actuator/caches/{name}`.

### Материалы

- **docs.spring.io — Spring Boot Actuator Reference** — `https://docs.spring.io/spring-boot/reference/actuator/index.html`
- **docs.spring.io — Endpoints** — `https://docs.spring.io/spring-boot/reference/actuator/endpoints.html`
- **docs.spring.io — Health information** — `https://docs.spring.io/spring-boot/reference/actuator/health.html`
- **docs.spring.io — Metrics (Micrometer)** — `https://docs.spring.io/spring-boot/reference/actuator/metrics.html`
- **docs.spring.io — Spring Boot → Observability** — `https://docs.spring.io/spring-boot/reference/actuator/observability.html`
- **Micrometer Reference** — `https://docs.micrometer.io/micrometer/reference/`
- **Micrometer JavaDoc** — `https://docs.micrometer.io/micrometer/reference/api.html`
- **docs.spring.io — Micrometer Tracing** — `https://docs.micrometer.io/micrometer/reference/implementations/bridges.html`
- **docs.spring.io — Prometheus registry** — `https://docs.micrometer.io/micrometer/reference/implementations/prometheus.html`
- **Baeldung — Spring Boot Actuator** — `https://www.baeldung.com/spring-boot-actuator`
- **Baeldung — Micrometer in Spring Boot** — `https://www.baeldung.com/micrometer-spring-boot`
- **Baeldung — Metrics with Spring Boot and Prometheus** — `https://www.baeldung.com/spring-boot-prometheus`
- **Baeldung — Spring Boot Liveness and Readiness Probes** — `https://www.baeldung.com/spring-liveness-readiness-probes`
- **Spring blog — Observability with Spring Boot 3** — `https://spring.io/blog/2022/10/12/observability-with-spring-boot-3`
- **OpenTelemetry documentation** — `https://opentelemetry.io/docs/`
- **Prometheus documentation** — `https://prometheus.io/docs/introduction/overview/`
- **Reflectoring — Spring Boot Actuator endpoints** — `https://reflectoring.io/spring-boot-actuator/`
- **Grafana Loki/Tempo for traces and logs** — `https://grafana.com/docs/tempo/latest/`
- **Книга — "Spring Boot: Up and Running" (Mark Heckler)** — O'Reilly (observability chapter)

### Практика / Кейсы

1. **Full observability stack (Prometheus + Grafana + Tempo + Loki)**: Spring Boot 3 app с `micrometer-registry-prometheus` + `micrometer-tracing-bridge-otlp` + OTLP exporter. Docker Compose с Prometheus, Grafana, Tempo, Loki. Custom dashboard с JVM, HTTP, business metrics. **Цель:** end-to-end observability.
2. **Custom `HealthIndicator` для external dependencies**: health check который проверяет доступность external API (с timeout 2s, не блокирующий startup). Group — `readiness` включает external dependency, `liveness` — нет. **Цель:** understand liveness vs readiness.
3. **Business metrics**: `Counter` "orders.created" с tags `product_category`, `region`. `Timer` "checkout.duration" с percentiles 50/95/99. `Gauge` "cart.size" — current average. Покрыть тестом через `SimpleMeterRegistry`. **Цель:** business metrics.
4. **Custom `@Endpoint`**: `FeatureFlagsEndpoint` с `@ReadOperation` (list), `@WriteOperation` (toggle), `@DeleteOperation` (reset), `@Selector` для конкретного flag. Тестировать через MockMvc. **Цель:** custom endpoint.
5. **Runtime log level change**: через `/actuator/loggers` менять уровень без restart. Настроить retention в Logback с SizeAndTimeBasedRollingPolicy. Симулировать prod issue — temporary DEBUG на проблемном package. **Цель:** runtime observability.
6. **Distributed tracing через multiple services**: 2 Spring Boot сервиса общаются через `WebClient`. Tracing bridge автоматически propagates `traceId` через `traceparent` header. Tempo показывает span tree. **Цель:** distributed tracing.
7. **Slow endpoint detection**: `@Timed` на controller-методе + histogram percentiles. Grafana alert если p99 > 500ms. **Цель:** SLO monitoring.
8. **`BufferingApplicationStartup` + startup analysis**: включить, после старта вытащить `/actuator/startup` → найти top-10 самых медленных bean-ов. Оптимизировать через `@Lazy`, `proxyBeanMethods=false`. **Цель:** startup performance.
9. **Management on separate port**: `management.server.port=9090`. Доступ к actuator только из internal network, не из public. Security: `SecurityFilterChain` для `/**:9090/actuator/**` — basic auth. **Цель:** security isolation.
10. **OTLP exporter to Tempo**: Spring Boot app → OTLP HTTP → Tempo. Custom span attributes (например, `user.id`). Tempo search по `user.id`. **Цель:** OTLP pipeline.

---

## 12. Spring Cloud

### Теория

- **Spring Cloud** — umbrella-проект для distributed system patterns (service discovery, config, circuit breaker, gateway, load balancer, messaging). Версии: Spring Cloud 2023.x (для Boot 3.2+), 2022.x (для Boot 3.0+), 2021.x (последняя для Boot 2.x).
- **Release Train naming** (Spring Cloud): Alphabetical — Angel, Brixton, Camden, Dalston, Edgware, Finchley, Greenwich, Hoxton, Ilford, 2020.0, 2021.0, 2022.0, 2023.0, 2024.0. С 2020 — calendar versioning.
- **Service Discovery**:
  - **Netflix Eureka** (Spring Cloud Netflix Eureka): `@EnableEurekaServer` — server. `@EnableEurekaClient` — клиент (deprecated, теперь просто зависимость). Eureka Server хранит registry, клиенты регистрируются и периодически (30s) шлют heartbeats. Eureka — AP (available + partition tolerant), eventual consistency.
  - **HashiCorp Consul** (Spring Cloud Consul): более general-purpose KV store, поддерживает health checks, service mesh.
  - **Apache Zookeeper** (Spring Cloud Zookeeper).
  - **Kubernetes Service discovery** (Spring Cloud Kubernetes): читает K8s API для services/endpoints. Не требует отдельного discovery server.
  - **Nacos** (Alibaba, Spring Cloud Alibaba).
- **Config Server/Client**:
  - **Spring Cloud Config Server**: `@EnableConfigServer`. Backend: Git (по умолчанию), File system, Vault, JDBC, AWS S3, Redis, CredHub.
  - URLs: `/{application}/{profile}`, `/{application}-{profile}.yml`, `/{label}/{application}-{profile}.yml`. Label = Git branch.
  - Encryption/decryption: symmetric/asyymmetric.
  - **Spring Cloud Config Client**: `spring.config.import=optional:configserver:http://localhost:8888`. Auto-reload через `@RefreshScope` + `/actuator/refresh` (или `/actuator/busrefresh` для всех clients через Spring Cloud Bus).
  - **Spring Cloud Bus** — broadcast config changes через message broker (Kafka/RabbitMQ). `/actuator/busrefresh` → all clients get refresh event.
- **Circuit Breaker**:
  - **Resilience4j** (рекомендуется) — `spring-cloud-starter-circuitbreaker-resilience4j`. Заменил Netflix Hystrix (deprecated, no longer in maintenance).
  - Modules: `CircuitBreaker`, `Bulkhead`, `RateLimiter`, `Retry`, `TimeLimiter`, `Cache`.
  - States: CLOSED (normal), OPEN (failures > threshold → reject requests), HALF_OPEN (let some through to test).
  - Configuration: `failureRateThreshold`, `slowCallRateThreshold`, `waitDurationInOpenState`, `permittedNumberOfCallsInHalfOpenState`, `slidingWindowSize`, `minimumNumberOfCalls`, `registerHealthIndicator`.
  - Annotations: `@CircuitBreaker(name = "myCircuit", fallbackMethod = "fallback")`.
  - Reactive: `ReactiveCircuitBreaker`, `ReactiveResilience4JCircuitBreakerFactory`.
- **API Gateway**:
  - **Spring Cloud Gateway** (Spring WebFlux-based, заменил Netflix Zuul): `spring-cloud-starter-gateway`. Reactive, Netty. Routes, predicates, filters.
  - Predicates: `After`, `Before`, `Between`, `Cookie`, `Header`, `Host`, `Method`, `Path`, `Query`, `RemoteAddr`, `Weight`, `XForwardedRemoteAddr`.
  - Filters: `AddRequestHeader`, `AddResponseHeader`, `RewritePath`, `StripPrefix`, `PrefixPath`, `SetStatus`, `Retry`, `RequestRateLimiter` (Redis-based), `CircuitBreaker`, `SaveSession`, `ModifyRequestBody`, `ModifyResponseBody`, `TokenRelay` (OAuth2 token forwarding), custom.
  - Route discovery: auto-routes к registered services (`lb://service-name`).
  - Rate limiting: `RequestRateLimiter` через Redis + Lua script.
- **Load Balancer**:
  - **Spring Cloud LoadBalancer** (заменил Netflix Ribbon, deprecated): `spring-cloud-starter-loadbalancer`. Поддерживает service discovery integration. Default — `RoundRobinLoadBalancer`. Кастомный: реализация `ReactorLoadBalancer` / `ReactorServiceInstanceLoadBalancer`.
  - Usage: `WebClient.builder().baseUrl("lb://user-service")` + `@LoadBalanced` annotation на `WebClient.Builder` bean.
- **OpenFeign** (Spring Cloud OpenFeign): declarative HTTP client. `@EnableFeignClients` на `@Configuration` или `@SpringBootApplication`. Interface с `@FeignClient(name = "user-service", url = "${user-service.url}")` + JAX-RS / Spring MVC-стиль аннотаций.
  - Features: contract (Spring MVC annotations), interceptors (`RequestInterceptor`), error decoder (`ErrorDecoder`), logging, retries (`Retryer`), Hystrix/Resilience4j integration (fallback).
  - `@FeignClient(name = "user-service", fallback = UserClientFallback.class)` — fallback при ошибке.
  - Spring Cloud 2022.0+ — support for reactive `@FeignClient` through ` ReactiveFeignClient` (limited).
- **Sleuth → Micrometer Tracing**:
  - Spring Cloud Sleuth — устарел с Spring Cloud 2022.0, заменён на `micrometer-tracing-bridge-brave`/`-otlp`.
  - Тот же API: `@NewSpan`, `@ContinueSpan`, `Tracer`.
- **Spring Cloud Stream** — message-driven microservices. Functional programming model (Spring Cloud Stream 3.x): `java.util.function.Function`, `Consumer`, `Supplier` beans. Binding to destinations (Kafka topics, RabbitMQ exchanges).
  - `spring.cloud.function.definition=processOrder;sendEmail`.
  - `spring.cloud.stream.bindings.processOrder-in-0.destination=orders`.
  - Binder abstraction: `kafka`, `rabbit`, `kinesis`, `pubsub`, etc.
- **Spring Cloud Bus** — broadcast state changes (config refresh, feature flags) через AMQP/Kafka. `/actuator/busrefresh`.
- **Spring Cloud Task** — short-lived tasks (batch jobs, scripts). Records task execution in DB.
- **Spring Cloud Open Service Broker** — implement Open Service Broker API (for selling services on Cloud Foundry/Kubernetes).
- **Spring Cloud Contract** — consumer-driven contracts for microservices. CDC testing.
- **Spring Cloud Kubernetes**:
  - Service discovery: reads `Endpoints`/`Service` from K8s API.
  - Config: reads `ConfigMap`/`Secret`.
  - Ribbon/LoadBalancer integration with K8s services.
- **Spring Cloud Function** — serverless abstractions: `Function<T,R>`, `Consumer<T>`, `Supplier<T>`. Can be deployed to AWS Lambda, Azure Functions, GCP Functions, Knative.
- **Spring Cloud Tencent** / **Spring Cloud Alibaba** — regional distributions.
- **Distributed transactions** — **НЕ** Spring Cloud. Используется Seata (Alibaba), Saga pattern (orchestration/choreography), outbox pattern.

### Материалы

- **docs.spring.io — Spring Cloud Reference** — `https://docs.spring.io/spring-cloud/reference/`
- **Spring Cloud project page** — `https://spring.io/projects/spring-cloud`
- **Spring Cloud Netflix Eureka Reference** — `https://docs.spring.io/spring-cloud-netflix/reference/`
- **Spring Cloud Config Reference** — `https://docs.spring.io/spring-cloud-config/reference/`
- **Spring Cloud Gateway Reference** — `https://docs.spring.io/spring-cloud-gateway/reference/`
- **Spring Cloud OpenFeign Reference** — `https://docs.spring.io/spring-cloud-openfeign/reference/`
- **Spring Cloud LoadBalancer Reference** — `https://docs.spring.io/spring-cloud-loadbalancer/reference/`
- **Spring Cloud Stream Reference** — `https://docs.spring.io/spring-cloud-stream/reference/`
- **Spring Cloud Bus Reference** — `https://docs.spring.io/spring-cloud-bus/reference/`
- **Spring Cloud Kubernetes Reference** — `https://docs.spring.io/spring-cloud-kubernetes/reference/`
- **Resilience4j documentation** — `https://resilience4j.readme.io/`
- **Baeldung — Spring Cloud Series** — `https://www.baeldung.com/spring-cloud-series`
- **Baeldung — Intro to Spring Cloud Gateway** — `https://www.baeldung.com/spring-cloud-gateway`
- **Baeldung — Spring Cloud OpenFeign** — `https://www.baeldung.com/spring-cloud-openfeign`
- **Baeldung — Spring Cloud Config** — `https://www.baeldung.com/spring-cloud-configuration`
- **Baeldung — Resilience4j Guide** — `https://www.baeldung.com/resilience4j`
- **Spring blog — Spring Cloud 2023.0 Release Notes** — `https://spring.io/blog/2023/09/21/spring-cloud-2023-0-RC1`
- **Reflectoring — Spring Cloud Gateway** — `https://reflectoring.io/spring-cloud-gateway/`
- **Книга — "Spring Microservices in Action, Second Edition" (John Carnell)** — Manning, ISBN 978-1617295636
- **Книга — "Cloud Native Spring in Action" (Cosmin Macrinici)** — Manning (Boot 3 + Spring Cloud 2022+)

### Практика / Кейсы

1. **Microservices topology**: 3 сервиса (user-service, order-service, payment-service) + Eureka server + Spring Cloud Config Server (Git backend) + API Gateway. Service-to-service через OpenFeign. Resilience4j circuit breaker на downstream calls. **Цель:** full Spring Cloud stack.
2. **API Gateway с custom filters**: filter для JWT validation + propagation, rate limiting (Redis), rewrite paths, add correlation id header. Routes: `/api/users/**` → user-service, `/api/orders/**` → order-service. **Цель:** production gateway.
3. **Distributed config refresh**: change config in Git, trigger `/actuator/busrefresh` через Spring Cloud Bus (Kafka). All services receive refresh event, `@RefreshScope` beans обновляются. **Цель:** hot config reload.
4. **Resilience4j full stack**: CircuitBreaker + Bulkhead + RateLimiter + Retry на downstream call. Tuning: failure rate 50%, wait 60s in OPEN, bulkhead 20 concurrent, rate limit 100 req/s, retry 3 attempts exponential. Покрыть тестом через `CircuitBreakerRegistry`. **Цель:** resilience patterns.
5. **Service discovery in Kubernetes**: Spring Cloud Kubernetes, deployment в K8s, service discovery через K8s API. Сравнить с Eureka (extra hop). In-cluster service account RBAC. **Цель:** K8s-native discovery.
6. **Spring Cloud Stream с Kafka**: `Function<Order, OrderEvent>` bean, binding to `orders` topic. `application.yml`: `spring.cloud.stream.bindings.function-in-0.destination=orders`, `function-out-0.destination=order-events`. Consumer groups, partitions, DLQ. **Цель:** event-driven microservices.
7. **OAuth2 token relay в Gateway**: gateway выполняет OAuth2 login, получает access token, relay-ит его downstream в `Authorization` header через `TokenRelay=` filter. Downstream сервисы — resource servers. **Цель:** SSO for microservices.
8. **OpenFeign с custom `ErrorDecoder`**: decoder который на 503 → бросает `RetryableException` (triggers retry), на 404 → fallback, на 400 → бросает `BadRequestException`. Logging через `Logger.Level.FULL`. **Цель:** robust HTTP client.
9. **Spring Cloud Contract CDC**: provider пишет contract в YAML/DSL, генерирует тесты. Consumer stubs из contract. Build pipeline: contract → publish to repo → consumer downloads stub. **Цель:** contract-first microservices.
10. **Gateway + Service mesh comparison**: реализовать routing, rate limiting, mTLS через Spring Cloud Gateway, потом через Istio service mesh. Сравнить complexity, features, performance. **Цель:** understand trade-offs.

---

## 13. Spring Batch

### Теория

- **Spring Batch** — framework для robust batch processing: ETL, financial reports, data migration, scheduled jobs. Production-grade features: restartability, retry, skip, chunking, partitioning, transactions, statistics.
- **Domain language**:
  - **Job** — sequence of Steps. Encapsulates entire batch process.
  - **Step** — independent, sequential phase of a Job. Either tasklet (single operation) or chunk-oriented.
  - **JobInstance** — logical job run (e.g., "Daily ETL job for 2024-01-15").
  - **JobExecution** — single attempt to run a JobInstance. May have multiple executions (restarts).
  - **StepExecution** — single attempt to run a Step.
  - **ExecutionContext** — collection of key-value pairs, persisted between executions (per StepExecution and JobExecution).
  - **JobParameters** — input to Job (date, file paths, etc.). Identifies JobInstance.
- **`JobRepository`** — persists Job/Step executions metadata in DB. Tables: `BATCH_JOB_INSTANCE`, `BATCH_JOB_EXECUTION`, `BATCH_JOB_EXECUTION_PARAMS`, `BATCH_STEP_EXECUTION`, `BATCH_STEP_EXECUTION_CONTEXT`, `BATCH_JOB_EXECUTION_CONTEXT`. Required for restartability.
- **`JobLauncher`** — entry point to run a Job. `JobLauncher.run(job, jobParameters)`. Sync (default) or async (`TaskExecutor`-based).
- **`JobExplorer`** — read-only access to JobRepository (query history).
- **`JobOperator`** — operations: start, restart, stop, abandon.
- **`JobRegistry`** — registry of Jobs by name (for runtime access).
- **Chunk-oriented processing**:
  - Reader → Processor → Writer, processing `chunk-size` items at a time.
  - Transaction wraps each chunk. If chunk fails — only that chunk rolls back.
  - Configuration: `step.chunk(100).reader(reader).processor(processor).writer(writer).build()`.
- **`ItemReader<T>`** — strategy for reading items one at a time. Returns `null` when exhausted.
  - Built-in: `JdbcCursorItemReader`, `JdbcPagingItemReader`, `JpaPagingItemReader`, `HibernatePagingItemReader`, `StaxEventItemReader` (XML), `JsonItemReader`, `FlatFileItemReader` (CSV/fixed-width), `MongoItemReader`, `Neo4jItemReader`, `RepositoryItemReader`, `LdapItemReader`, `KafkaItemReader`, `ListItemReader`, `JmsItemReader`, `AmqpItemReader`.
  - Custom: реализовать `read()`.
- **`ItemProcessor<I, O>`** — transform/filter items. `process(I item) -> O`. Return `null` — filter out item.
  - Built-in: `ValidatingItemProcessor` (Bean Validation), `CompositeItemProcessor` (chain).
- **`ItemWriter<T>`** — write chunk of items. `write(List<? extends T> items)`.
  - Built-in: `JdbcBatchItemWriter`, `JpaItemWriter`, `HibernateItemWriter`, `StaxEventItemWriter`, `JsonFileItemWriter`, `FlatFileItemWriter`, `MongoItemWriter`, `Neo4jItemWriter`, `RepositoryItemWriter`, `KafkaItemWriter`, `JmsItemWriter`, `AmqpItemWriter`, `CompositeItemWriter`, `ClassifierCompositeItemWriter` (route by classifier).
- **Tasklet** — single-step processing. `RepeatStatus execute(StepContribution, ChunkContext)`. `CONTINUABLE` or `FINISHED`. Use for setup/cleanup tasks, single SQL statements, system commands.
- **Listeners**:
  - `JobExecutionListener` — `beforeJob`, `afterJob`.
  - `StepExecutionListener` — `beforeStep`, `afterStep`.
  - `ChunkListener` — `beforeChunk`, `afterChunk`, `afterChunkError`.
  - `ItemReadListener` — `beforeRead`, `afterRead`, `onReadError`.
  - `ItemProcessListener` — `beforeProcess`, `afterProcess`, `onProcessError`.
  - `ItemWriteListener` — `beforeWrite`, `afterWrite`, `onWriteError`.
  - `SkipListener` — `onSkipInRead`, `onSkipInProcess`, `onSkipInWrite`.
  - Annotations: `@BeforeJob`, `@AfterJob`, `@BeforeStep`, `@AfterStep`, etc.
- **Restartability**:
  - JobInstance can be restarted if last execution failed.
  - `Job.restartable = true` (default).
  - ExecutionContext persists state (last processed item, etc.).
  - Reader must support restart: implement `ItemStreamReader` with `open(ExecutionContext)`, `update(ExecutionContext)`, `close()`.
- **Skip logic**:
  - `step.faultTolerant().skip(InvalidItemException.class).skipLimit(10)`.
  - Skipped items go to skip listener (logging, dead-letter).
  - Use for recoverable data errors (malformed records).
- **Retry logic**:
  - `step.faultTolerant().retry(TransientException.class).retryLimit(3)`.
  - Retry policy: `SimpleRetryPolicy`, `ExceptionClassifierRetryPolicy`, `TimeoutRetryPolicy`, `NeverRetryPolicy`.
  - Backoff: `BackoffPolicy` — `FixedBackoffPolicy`, `ExponentialBackoffPolicy`, `ExponentialRandomBackoffPolicy`, `NoBackoffPolicy`.
  - `RetryTemplate` — programmatic retry.
  - Spring Retry (`@Retryable`) — separate library, can be used outside Batch.
- **`noSkip` / `noRollback`**: `step.faultTolerant().noSkip(SQLException.class).noRollback(ValidationException.class)`.
- **Partitioning** — split a Step across multiple threads/processes.
  - `Partitioner` — creates `ExecutionContext` for each partition (e.g., by ID range).
  - `Step` is replicated for each partition, executed in parallel via `TaskExecutor`.
  - Master Step coordinates partitions, Slave Steps execute.
- **Multi-threaded Step** — `step.taskExecutor(taskExecutor).throttleLimit(4)`. Reader/writer must be thread-safe.
- **Parallel Steps** — `Job.flow(step1).split(taskExecutor).add(flow(step2), flow(step3)).end()`.
- **Remote Chunking** — distribute work across multiple JVMs via messaging (Kafka/Spring Integration). Master sends chunks, slaves process and reply.
- **Remote Partitioning** — distribute partitions across JVMs. Master sends partition requests, slaves execute Steps.
- **Job scheduling**:
  - Spring `@Scheduled` — simple.
  - Quartz Scheduler — Quartz integration with Spring Batch (`JobDetailFactoryBean`, `CronTriggerFactoryBean`).
  - Spring Cloud Data Flow — orchestration of batch and stream tasks.
  - Kubernetes CronJobs — native K8s scheduler.
  - Airflow / Prefect / Dagster — external orchestration.
- **`@JobScope` / `@StepScope`** — bean-scopes для late binding of JobParameters/StepExecutionContext. `@Value("#{jobParameters['input.file']}")` в reader bean.
- **Boot integration**: `spring-boot-starter-batch`. Auto-configuration: `JobRepository`, `JobLauncher`, `JobExplorer`, `JobOperator` beans. Default: in-memory `JobRepository` если нет DataSource; H2/real DB если есть.
  - `spring.batch.job.enabled=true` (default) — стартует все Jobs при запуске app.
  - `spring.batch.jdbc.initialize-schema=embedded|always|never`.
- **Metrics**: Spring Batch 4+ интегрирован с Micrometer. Built-in: `spring.batch.job`, `spring.batch.job.active`, `spring.batch.step`, `spring.batch.chunk`. Custom: `ItemReadListener`, `ItemProcessListener` counters.
- **JSR-352** — Java standard for batch processing. Spring Batch implements it. `JSLJobLoader`, `JobOperator` from `javax.batch.operations`.
- **Best practices**:
  - Chunk size: tune for DB performance (50-1000 typically).
  - Use `JdbcCursorItemReader` for streaming large resultsets (не загружает всё в память).
  - Use `JdbcBatchItemWriter` for bulk inserts.
  - Avoid `JpaRepository` in batch — slow per-row.
  - Use partitioning for parallelism on independent datasets.
  - Persist restart state for long-running jobs.

### Материалы

- **docs.spring.io — Spring Batch Reference** — `https://docs.spring.io/spring-batch/reference/`
- **docs.spring.io — Spring Batch Domain Language** — `https://docs.spring.io/spring-batch/reference/domain.html`
- **docs.spring.io — Configuring a Job** — `https://docs.spring.io/spring-batch/reference/job/configuring.html`
- **docs.spring.io — Configuring a Step** — `https://docs.spring.io/spring-batch/reference/step/configuring.html`
- **docs.spring.io — Chunk-oriented processing** — `https://docs.spring.io/spring-batch/reference/step/chunk-oriented-processing.html`
- **docs.spring.io — ItemReaders and ItemWriters** — `https://docs.spring.io/spring-batch/reference/reader-writer.html`
- **docs.spring.io — Scaling and Parallel Processing** — `https://docs.spring.io/spring-batch/reference/scaling.html`
- **docs.spring.io — Spring Boot → Batch** — `https://docs.spring.io/spring-boot/reference/howto/batch.html`
- **Baeldung — Intro to Spring Batch** — `https://www.baeldung.com/introduction-to-spring-batch`
- **Baeldung — Spring Batch: ItemReader, ItemProcessor, ItemWriter** — `https://www.baeldung.com/spring-batch-item-processor`
- **Baeldung — Spring Batch using Partitioning** — `https://www.baeldung.com/spring-batch-partitioning`
- **Baeldung — Spring Boot Batch** — `https://www.baeldung.com/spring-boot-spring-batch`
- **Spring blog — Spring Batch 5** — `https://spring.io/blog/2021/12/16/spring-batch-5-0-0-M1-released`
- **Reflectoring — Spring Batch essentials** — `https://reflectoring.io/spring-batch/`
- **Spring Cloud Data Flow Reference** — `https://dataflow.spring.io/docs/`
- **JSR-352 Specification** — `https://jcp.org/en/jsr/detail?id=352`
- **Книга — "Pro Spring Batch" (Michael T. Minella)** — Apress (немного устарела по версиям, но концепции глубокие)
- **Книга — "Spring Batch in Action" (Arnaud Cogoluegnes et al.)** — Manning

### Практика / Кейсы

1. **CSV → DB ETL job**: `FlatFileItemReader` с `DelimitedLineTokenizer`, `ItemProcessor` (validation, transformation), `JdbcBatchItemWriter`. Restartability: `ItemStreamReader` с `update(ExecutionContext)` — сохранять last processed line. Skip 10 malformed records. Покрыть `@SpringBatchTest`. **Цель:** classic ETL.
2. **Partitioning for parallel processing**: `Partitioner` разбивает диапазон IDs на 10 partitions (например, 0-1M, 1M-2M, ...). Каждый partition — отдельный `Step` в своём потоке. Замерить ускорение. **Цель:** scale-out.
3. **Multi-step Job with conditional flow**: `Job.start(step1).on("FAILED").to(failureStep).from(step1).on("*").to(step2).end()`. `step1` — extract, `step2` — transform, `step3` — load. Decision-based routing через `JobExecutionDecider`. **Цель:** complex job flows.
4. **Spring Cloud Data Flow orchestration**: зарегистрировать batch job как task в SCDF, запускать по расписанию (Cron), мониторить execution history, alerts on failure. **Цель:** managed batch platform.
5. **Quartz + Spring Batch**: Quartz scheduler запускает Job в указанное время. Сохранение Quartz tables + Spring Batch tables в одной DB. Recovery on app restart. **Цель:** robust scheduling.
6. **Remote chunking через Kafka**: master Step отправляет chunks в Kafka topic, slave процессы (отдельные JVMs) читают, обрабатывают, пишут результат. Reply через Kafka. **Цель:** distributed batch.
7. **Restartability after crash**: Job с 1M records, chunk size 100. На chunk 5000 — crash (kill process). Restart: Job продолжит с chunk 5000 (через ExecutionContext). Покрыть тестом симуляции crash. **Цель:** recoverable batch.
8. **Skip + dead-letter queue**: 1% records invalid. Skip limit 1000. `SkipListener` пишет skipped records в DLQ topic (Kafka). Alert если skip rate > 0.5%. **Цель:** production fault tolerance.
9. **Performance tuning**: chunk size 10 vs 100 vs 1000 vs 10000. Замерить throughput. Объяснить почему 1000 лучше (DB roundtrips, transaction overhead). Use `JdbcBatchItemWriter` с `assertUpdates=false`. **Цель:** batch performance.
10. **Audit & metrics через Micrometer**: custom `BatchListener` который пишет Micrometer metrics: `batch.job.duration`, `batch.items.processed`, `batch.errors.count`. Grafana dashboard для batch jobs. Alert если job execution > 1h. **Цель:** batch observability.

---

## 14. Spring Cache

### Теория

- **Spring Cache abstraction** — `@Cacheable`, `@CacheEvict`, `@CachePut`, `@Caching`, `@CacheConfig`. Работает поверх AOP, proxy-based. Не реализация, а абстракция.
- **`@Cacheable(value = "users", key = "#id")`** — перед выполнением метода проверить cache; если есть — вернуть из cache; если нет — выполнить метод, положить в cache.
- **`@CacheEvict(value = "users", key = "#id")`** — удалить запись из cache. `allEntries = true` — очистить весь cache.
- **`@CachePut(value = "users", key = "#user.id")`** — всегда выполнить метод, обновить cache с результатом.
- **`@Caching`** — composite of multiple cache operations.
- **`@CacheConfig`** — class-level, общие настройки для всех методов (`cacheNames`, `keyGenerator`, `cacheManager`, `cacheResolver`).
- **`CacheManager`** — central interface. `getCache(String name)`, `getCacheNames()`. Implementations:
  - `ConcurrentMapCacheManager` — in-memory, default if nothing else.
  - `CaffeineCacheManager` (Caffeine, recommended for in-memory) — features: size-based eviction, time-based eviction, statistics, async loading, near-async.
  - `RedisCacheManager` (Spring Data Redis) — distributed cache.
  - `HazelcastCacheManager` — distributed, IMDG.
  - `EhCacheCacheManager` (Ehcache 3 via JCache).
  - `JCacheCacheManager` (JSR-107 JCache).
  - `NoOpCacheManager` — disable caching.
  - `CompositeCacheManager` — multiple managers.
- **`Cache` interface** — `get(Object key)`, `get(key, Class<T>)`, `put(key, value)`, `putIfAbsent`, `evict(key)`, `clear()`. `ValueWrapper` — wraps null values (так как `null` означает "no entry").
- **`@EnableCaching`** — включает Spring Cache AOP (`CacheInterceptor`).
- **Key generation**:
  - Default: `SimpleKeyGenerator`. Ключ — массив всех параметров (или `SimpleKey.EMPTY` для без параметров, или сам параметр если один).
  - `KeyGenerator` interface — кастомный. `generate(Object target, Method method, Object... params)`.
  - SpEL в `key`: `#id`, `#user.id`, `#root.methodName`, `#root.targetClass`, `#root.args`, `#result` (in `@CachePut` / `@CacheEvict` after method).
- **Conditional caching**:
  - `condition` — SpEL, evaluated **before** method. If false — method executes normally, no cache interaction.
  - `unless` — SpEL, evaluated **after** method. If true — result is NOT put in cache. Useful for caching only non-null or positive results: `unless = "#result == null"`, `unless = "#result.size() == 0"`.
- **Multi-cache**: `@Cacheable({"users", "userSummaries"})` — write to multiple caches.
- **`CacheResolver`** — programmatic cache resolution. `resolve(CacheOperationInvocationContext)`. Более гибкий чем `cacheManager`.
- **Synchronization**:
  - По умолчанию, если несколько потоков запрашивают один и тот же ключ одновременно и ключа нет — все выполняют метод. Это "thundering herd".
  - `sync = true` на `@Cacheable` — синхронизация: только один поток выполняет метод, остальные ждут. Поддерживается не всеми CacheManagers (Caffeine, Redis — да).
- **TTL (time-to-live)**:
  - Зависит от CacheManager.
  - Caffeine: `.expireAfterWrite(10, MINUTES)`, `.expireAfterAccess(5, MINUTES)`, `.expireAfterVariadic(...)` (custom).
  - Redis: `spring.cache.redis.time-to-live=PT10M` (ISO-8601 duration), per-cache config через `RedisCacheConfiguration`.
  - Ehcache: XML config `<expiry>` element.
- **Caffeine features**:
  - Size-based eviction: `maximumSize(1000)`, `maximumWeight(...)` + `weigher(...)`.
  - Time-based: `expireAfterWrite`, `expireAfterAccess`, `expireAfter(Expiry)` (custom per-entry).
  - Reference-based: `weakKeys()`, `weakValues()`, `softValues()`.
  - Statistics: `recordStats()` — hit rate, eviction count, average load time.
  - Async loading: `AsyncCacheLoader`, `LoadingCache`.
  - Refresh: `refreshAfterWrite`.
- **Redis cache specifics**:
  - TTL на каждой записи (не глобально).
  - Cache-aside pattern: read-through from DB if not in cache.
  - Use `RedisCacheManager.builder(...).cacheDefaults(...).withInitialCacheConfigurations(...)`.
  - Сериализация: Jackson JSON (default), JDK serialization, custom `RedisSerializer`.
  - Pub/Sub для cache invalidation across nodes.
- **Spring Boot cache starter**: `spring-boot-starter-cache`. Auto-detect CacheManager из classpath:
  - Caffeine → `CaffeineCacheManager`.
  - Redis (Spring Data Redis) → `RedisCacheManager`.
  - Hazelcast → `HazelcastCacheManager`.
  - EhCache 3 (XML) → `JCacheCacheManager`.
  - Если ничего — `ConcurrentMapCacheManager`.
- **Multi-layered cache (L1 + L2)**:
  - L1 — local in-memory (Caffeine), short TTL (1 min).
  - L2 — distributed (Redis), longer TTL (1 hour).
  - Custom `CacheManager` который пробует L1, потом L2, потом source.
  - Invalidation: pub/sub на L2 update → evict L1 на всех нодах.
- **`@Cacheable` на reactive types**: Spring 5.3+ поддерживает `Mono`/`Flux` return types. Кэшируется reactive pipeline, не значение.
- **Caching pitfalls**:
  - Self-invocation (как `@Transactional`): прокси не сработает.
  - `null` values: `@Cacheable(unless = "#result == null")` — не кэшировать null (или использовать `allowNullValues = false` на CacheManager).
  - Mutable values в cache: если кто-то модифицирует cached value, состояние cache повреждается. Defensive copy на write.
  - Keys: hashable, immutable (или `equals`/`hashCode` правильно реализованы).
  - Large values в cache: memory pressure. Size-based eviction.
- **Cache stampede**: thundering herd при expire. Решения: `sync = true`, request coalescing, refresh-ahead (Caffeine `refreshAfterWrite`), probabilistic early expiration.
- **`@CacheEvict` timing**: `beforeInvocation = true` — evict **до** выполнения метода (если метод может бросить exception, всё равно evict). `beforeInvocation = false` (default) — после.
- **Cache abstraction vs direct API**: `@Cacheable` удобен, но opaque. Прямой `CacheManager.getCache("users").get(id)` — больше контроля, но менее декларативно.

### Материалы

- **docs.spring.io — Spring Framework → Cache Abstraction** — `https://docs.spring.io/spring-framework/reference/integration/cache.html`
- **docs.spring.io — Spring Boot → Caching** — `https://docs.spring.io/spring-boot/reference/io/caching.html`
- **docs.spring.io — Cache annotations** — `https://docs.spring.io/spring-framework/reference/integration/cache/annotations.html`
- **Caffeine Wiki** — `https://github.com/ben-manes/caffeine/wiki`
- **Spring Data Redis → Cache** — `https://docs.spring.io/spring-data/redis/reference/redis.html#redis:cache`
- **Baeldung — A Guide To Caching in Spring** — `https://www.baeldung.com/spring-cache-tutorial`
- **Baeldung — Spring Boot Cache Provider** — `https://www.baeldung.com/spring-boot-cache-providers`
- **Baeldung — Caffeine Cache** — `https://www.baeldung.com/java-caffeine-cache`
- **Baeldung — Spring Cache with Redis** — `https://www.baeldung.com/spring-data-redis-cache`
- **Baeldung — Spring Boot Cache Configuration** — `https://www.baeldung.com/spring-boot-cache-configuration`
- **Spring blog — Cache abstraction enhancements in Spring 4.3** — `https://spring.io/blog/2016/03/04/core-container-refinements-in-spring-framework-4-3`
- **Reflectoring — Spring Cache abstraction** — `https://reflectoring.io/spring-cache/`
- **JSR-107 (JCache) Specification** — `https://jcp.org/en/jsr/detail?id=107`
- **Caffeine design docs (W-TinyLFU)** — `https://github.com/ben-manes/caffeine/wiki/Efficiency`
- **Martin Fowler — Cache Patterns (read-through, write-through, etc.)** — `https://martinfowler.com/bliki/TwoHardThings.html`
- **Microsoft — Cache-aside pattern** — `https://learn.microsoft.com/en-us/azure/architecture/patterns/cache-aside`
- **Книга — "Pro Spring 6", Chapter 11: Caching** — Apress

### Практика / Кейсы

1. **Cache-aside с Redis**: `@Cacheable("users")` на `findById`. Redis — distributed cache. Disable cache: `@Cacheable(condition = "${cache.enabled}")`. Покрыть тестом с Embedded Redis. **Цель:** distributed cache.
2. **Multi-layered cache (Caffeine + Redis)**: кастомный `CacheManager` который сначала L1 (Caffeine, 1 min), потом L2 (Redis, 1 hour). Pub/Sub на Redis для L1 invalidation на всех нодах. Замерить latency улучшение. **Цель:** L1+L2 strategy.
3. **Cache stampede mitigation**: simulate 1000 concurrent requests на один hot key после expire. С `sync=false` — все выполняют метод (DB overload). С `sync=true` — один выполняет, остальные ждут. Замерить DB load. **Цель:** prevent thundering herd.
4. **Custom `KeyGenerator`**: composite key из `userId` + `tenantId` + `feature`. В SpEL это утомительно. Реализовать `KeyGenerator`. Покрыть unit-тестом. **Цель:** key generation patterns.
5. **Conditional caching**: `@Cacheable(unless = "#result == null")` — не кэшировать null. `@Cacheable(condition = "#id > 1000")` — кэшировать только "hot" users. `@Cacheable(unless = "#result.score < 50")` — кэшировать только significant scores. **Цель:** selective caching.
6. **`@CacheEvict` patterns**: `@CacheEvict(key = "#user.id")` после update. `@CacheEvict(allEntries = true)` после bulk operation. `@Caching(evict = {@CacheEvict(...), @CacheEvict(...)})` для multi-cache. **Цель:** cache invalidation discipline.
7. **Cache refresh ahead**: Caffeine `refreshAfterWrite` — обновлять записи в фоне до expire. `CacheLoader.reload` для async refresh. Сравнить с passive expire-then-load. **Цель:** proactive refresh.
8. **Cache metrics через Micrometer**: `CaffeineCacheManager` с `recordStats()`. Micrometer integration: `cache.gets`, `cache.puts`, `cache.evictions`, `cache.size`. Grafana dashboard с hit rate per cache. Alert если hit rate < 80%. **Цель:** cache observability.
9. **`@Cacheable` на reactive (Mono)**: `@Cacheable` на методе возвращающем `Mono<User>`. Проверить что кэшируется результат, не publisher. Сравнить с `Mono.cache()` operator. **Цель:** reactive caching.
10. **Spring Boot cache TTL customization**: per-cache TTL через `RedisCacheManager.builder().withInitialCacheConfigurations(Map.of("users", RedisCacheConfiguration.defaultCacheConfig().entryTtl(Duration.ofMinutes(5))))`. Покрыть тестом через `CacheManager.getCacheNames()`. **Цель:** fine-grained cache config.

---

## 15. Spring Messaging (JMS / AMQP / Kafka)

### Теория

- **Spring Messaging** — umbrella для messaging integrations: JMS, AMQP (RabbitMQ), Kafka, STOMP, WebSocket.
- **JMS (Java Message Service)** — Java standard for messaging. Two models: Queue (point-to-point), Topic (publish-subscribe).
  - Brokers: ActiveMQ Classic, ActiveMQ Artemis (next-gen, рекомендован), IBM MQ, Solace, Tibco EMS.
  - Spring JMS: `spring-boot-starter-activemq` (Classic) или `spring-boot-starter-artemis`.
  - `JmsTemplate` — main API. `send`, `convertAndSend`, `receive`, `receiveAndConvert`. Synchronous (blocking) по умолчанию.
  - `@JmsListener` — asynchronous message listener. `@JmsListener(destination = "orders.queue")`.
  - `DefaultJmsListenerContainerFactory` — configures listeners. Concurrent consumers, error handler, transaction manager.
  - `MessageConverter` — POJO ↔ JMS Message. `MappingJackson2MessageConverter`, `SimpleMessageConverter`.
  - Transactions: `JmsTransactionManager`, `@Transactional` works for JMS operations.
- **AMQP (Advanced Message Queuing Protocol)** — wire-level protocol. RabbitMQ — most popular implementation.
  - Concepts: Exchange (Direct, Topic, Fanout, Headers), Queue, Binding, Routing Key.
  - Spring AMQP: `spring-boot-starter-amqp`.
  - `RabbitTemplate` — main API. `convertAndSend`, `send`, `receive`, `receiveAndConvert`, `execute`.
  - `@RabbitListener` — async listener. `@QueueBinding`, `@RabbitHandler` (for typed handlers).
  - `SimpleRabbitListenerContainerFactory` — configures listeners. Concurrent consumers, prefetch, retry.
  - `MessageConverter`: `Jackson2JsonMessageConverter`, `SimpleMessageConverter`.
  - Publisher confirms: `RabbitTemplate.setConfirmCallback`, returns: `setReturnsCallback`.
  - DLX (Dead Letter Exchange): configure on queue, dead-lettered messages go to DLX.
  - Transactions: `RabbitTransactionManager`, less performant than publisher confirms.
  - Spring Cloud Stream RabbitMQ binder — higher-level abstraction.
- **Apache Kafka** — distributed streaming platform. Topics (partitioned), consumer groups, offsets, retention.
  - Spring Kafka: `spring-boot-starter-kafka`.
  - `KafkaTemplate<K, V>` — producer. `send`, `sendDefault`, `execute`.
  - `@KafkaListener` — consumer. `topics`, `groupId`, `containerFactory`, `id`, `autoStartup`, `properties`. `@Header(KafkaHeaders.RECEIVED_TOPIC)` for metadata.
  - `ConcurrentKafkaListenerContainerFactory` — configures listeners. Concurrent consumers (per partition), error handler, retry.
  - `ConsumerFactory` / `ProducerFactory` — configurations.
  - `KafkaTransactionManager` — transactional producer (exactly-once via Kafka transactions).
  - Serialization: `StringSerializer`, `IntegerSerializer`, `ByteArraySerializer`, custom `Serializer<T>`, Avro through Schema Registry, Protobuf, JSON.
  - Deserialization: dual — `Deserializer<T>`.
  - Error handling: `DefaultErrorHandler` (Spring Kafka 2.8+, заменил `SeekToCurrentErrorHandler`), `DeadLetterPublishingRecoverer` to DLQ topic.
  - `@RetryableTopic` (Spring Kafka 2.9+) — declarative retry + DLQ.
- **Kafka Streams** — stream processing library. State stores, KTable, KStream, GlobalKTable, aggregations, joins, windows.
  - Spring Kafka Streams support: `KafkaStreamsConfiguration`, `StreamsBuilderFactoryBean`.
  - `@EnableKafkaStreams`, `KStream<String, Order>` bean.
  - Serdes: `Serdes.String`, `Serdes.Long`, custom `JsonSerde<T>`, Avro serde.
  - Interactive queries: `ReadOnlyKeyValueStore`, queryable state stores.
- **Spring Cloud Stream** — higher-level abstraction, binder-based. Same code, different brokers.
  - Spring Cloud Stream 3.x — functional programming model. `Function`, `Consumer`, `Supplier` beans.
  - Binders: `spring-cloud-stream-binder-kafka`, `spring-cloud-stream-binder-rabbit`, Kinesis, Pub/Sub.
  - Configuration: `spring.cloud.stream.bindings.<functionName>-in-0.destination=topic`, `group=consumer-group`.
  - `spring.cloud.stream.kafka.binder` — Kafka-specific config.
- **`@StreamListener`** (legacy, Spring Cloud Stream 2.x) — annotation-based, replaced by functional model.
- **Spring Integration** — basis for Spring Cloud Stream. Channel adapters, message channels, transformers, routers.
- **Transactions in messaging**:
  - **JMS**: `JmsTransactionManager` — local JMS transactions. `ChainedTransactionManager` с DB (но deprecated, not XA).
  - **AMQP**: `RabbitTransactionManager`. Publisher confirms — better for reliability.
  - **Kafka**: producer transactions (`transactional.id`), EOS (exactly-once semantics) for stream processing.
  - **DB + message (transactional outbox)**: write message in same DB transaction, separate process polls and publishes. Reliable, but eventual consistency (delay in seconds).
- **`MessageListenerContainer`** abstraction — common across JMS/AMQP/Kafka.
  - Lifecycle: start, stop, pause, resume.
  - Concurrency: `setConcurrency(3-10)`.
  - Error handling: `ErrorHandler`, `RetryTemplate`.
- **`MessageConverter` / `SmartMessageConverter`** — serialize/deserialize payload.
- **Message headers**: `MessageHeaders`, immutable. Standard headers: `id`, `timestamp`. Custom via `MessageBuilder.withPayload(payload).setHeader("traceId", id).build()`.
- **`MessageChannel` / `MessageHandler`** (Spring Integration base) — for in-memory messaging within app.
- **Spring Messaging abstraction**: `Message<T>`, `MessageChannel`, `SubscribableChannel`, `PollableChannel`, `MessageHandler`.
- **Distributed tracing** — Spring Cloud Sleuth / Micrometer Tracing automatically propagates `traceId`/`spanId` via W3C trace context header (`traceparent`) for HTTP, B3 headers for messaging.
- **`@SendTo`** — publishes return value of listener method to specified destination.

### Материалы

- **docs.spring.io — Spring JMS Reference** — `https://docs.spring.io/spring-framework/reference/integration/jms.html`
- **docs.spring.io — Spring AMQP Reference** — `https://docs.spring.io/spring-amqp/reference/`
- **docs.spring.io — Spring Kafka Reference** — `https://docs.spring.io/spring-kafka/reference/`
- **docs.spring.io — Spring Boot → Messaging** — `https://docs.spring.io/spring-boot/reference/messaging.html`
- **docs.spring.io — Spring Cloud Stream Reference** — `https://docs.spring.io/spring-cloud-stream/reference/`
- **Apache Kafka Documentation** — `https://kafka.apache.org/documentation/`
- **Kafka Streams Documentation** — `https://kafka.apache.org/documentation/streams/`
- **RabbitMQ Documentation** — `https://www.rabbitmq.com/documentation.html`
- **ActiveMQ Artemis Documentation** — `https://activemq.apache.org/components/artemis/documentation/`
- **Baeldung — Spring JMS** — `https://www.baeldung.com/spring-jms`
- **Baeldung — Spring AMQP** — `https://www.baeldung.com/spring-amqp`
- **Baeldung — Spring Kafka** — `https://www.baeldung.com/spring-kafka`
- **Baeldung — Kafka Streams in Spring Boot** — `https://www.baeldung.com/spring-kafka-streams`
- **Baeldung — Spring Cloud Stream** — `https://www.baeldung.com/spring-cloud-stream`
- **Spring blog — Spring Kafka 3.0** — `https://spring.io/blog/2022/11/22/spring-for-apache-kafka-3-0-0`
- **Reflectoring — Spring Kafka** — `https://reflectoring.io/spring-kafka/`
- **Confluent Schema Registry** — `https://docs.confluent.io/platform/current/schema-registry/index.html`
- **Avro Specification** — `https://avro.apache.org/docs/current/spec.html`
- **Kafka Exactly-Once Semantics** — `https://www.confluent.io/blog/enabling-exactly-once-kafka-streams/`
- **Transactional Outbox pattern** — `https://microservices.io/patterns/data/transactional-outbox.html`
- **Книга — "Kafka: The Definitive Guide, 2nd Edition" (Narkhede, Shapira, Palino)** — O'Reilly
- **Книга — "Pro Spring Boot 2" (Greg L. Turnquist), Chapter on Messaging** — Apress

### Практика / Кейсы

1. **Kafka producer/consumer с DLQ + retry**: `@KafkaListener` + `DefaultErrorHandler` с `DeadLetterPublishingRecoverer` → DLQ topic. Retry policy: 3 attempts, exponential backoff. Cover EmbeddedKafka test. **Цель:** robust Kafka consumption.
2. **Kafka Streams aggregation**: KStream из `orders`, group by `customerId`, aggregate total in 5-min hopping window. Materialize to state store. Interactive query endpoint. **Цель:** stream processing.
3. **RabbitMQ with DLX pattern**: configure exchange + queue + DLX. `@RabbitListener` для main queue, dead-lettered messages go to DLX → DLQ. Consumer rejects malformed messages. Cover Testcontainers RabbitMQ. **Цель:** RabbitMQ error handling.
4. **Transactional outbox pattern**: write event to `outbox` table in same DB transaction as business operation. Async poller (Spring Batch or Spring Cloud Stream) reads outbox, publishes to Kafka, marks as published. Cover at-least-once semantics. **Цель:** reliable cross-system messaging.
5. **Spring Cloud Stream functional model**: `Function<OrderEvent, OrderProcessed>` bean. Bindings: `process-in-0.destination=orders`, `process-out-0.destination=processed-orders`. Switch binder from Kafka to RabbitMQ by changing dependency. Cover test. **Цель:** binder abstraction.
6. **Kafka exactly-once semantics**: producer with `transactional.id`, EOS config. Kafka Streams with read-process-write pattern. Verify idempotent consumer side. **Цель:** EOS in practice.
7. **Avro + Schema Registry**: define Avro schema for `OrderEvent`. Generate Java classes. Configure `KafkaAvroSerializer`/`KafkaAvroDeserializer` with Schema Registry. Schema evolution (backward compatibility). **Цель:** contract-driven messaging.
8. **JMS с ActiveMQ Artemis**: `spring-boot-starter-artemis`, `@JmsListener` для queue, `JmsTemplate.convertAndSend` для publish. Transactions через `JmsTransactionManager`. Cover embedded Artemis. **Цель:** JMS basics.
9. **Backpressure и consumer tuning**: `max.poll.records`, `fetch.max.bytes`, `max.partition.fetch.bytes` для slow consumer. `ConcurrentKafkaListenerContainerFactory.setConcurrency(3)`. Покрыть тестом с producer 1000 msg/s, consumer 200 msg/s — мониторить lag. **Цель:** Kafka performance tuning.
10. **Distributed tracing через Kafka**: Spring Boot app 1 → Kafka → app 2. Micrometer Tracing auto-propagates `traceId` via Kafka headers. Tempo shows end-to-end trace. **Цель:** cross-service tracing.

---

## 16. Spring Integration & Apache Camel

### Теория

- **Spring Integration** — implementation of Enterprise Integration Patterns (EIP) by Gregor Hohpe and Bobby Woolf. Provides messaging within Spring applications.
- **EIP patterns implemented**:
  - **Channel** — `MessageChannel`, `SubscribableChannel`, `PollableChannel`, `PublishSubscribeChannel`, `QueueChannel`, `PriorityChannel`, `RendezvousChannel`.
  - **Channel Adapter** — bridges external system to channel. `@InboundChannelAdapter`, `@ServiceActivator` for outbound.
  - **Transformer** — converts message payload. `@Transformer`.
  - **Filter** — drops messages based on condition. `@Filter`.
  - **Router** — routes messages to different channels. `@Router`, `HeaderValueRouter`, `PayloadTypeRouter`, `RecipientListRouter`.
  - **Splitter** — splits message into multiple. `@Splitter`.
  - **Aggregator** — combines multiple messages into one. `@Aggregator`. Related: **Resequencer** (reorders).
  - **Service Activator** — invokes service method on message. `@ServiceActivator`.
  - **Gateway** — proxy interface for messaging. `@MessagingGateway`.
  - **Bridge** — connects two channels.
  - **Scatter-Gather** — broadcast then aggregate.
  - **Wire Tap** — copy message to another channel (for inspection/audit).
  - **Claim Check** — replace payload with reference (e.g., store large payload in DB, pass claim).
  - **Content Enricher** — add data to payload.
  - **Dead Letter Channel** — for failed messages.
- **`Message<T>`** — payload + headers (`MessageHeaders`). Immutable.
- **`MessageChannel`**:
  - `SubscribableChannel` — `subscribe(MessageHandler)`, push-based.
  - `PollableChannel` — `receive()`, pull-based. `QueueChannel` (FIFO), `PriorityChannel` (comparator), `RendezvousChannel` (blocking).
  - `PublishSubscribeChannel` — broadcast to all subscribers.
  - `DirectChannel` — synchronous, single subscriber.
  - `ExecutorChannel` — async via TaskExecutor.
  - `FluxMessageChannel` (reactive) — for Spring Integration Reactive.
- **DSL (Java DSL)** — `IntegrationFlow` builder. `IntegrationFlows.from(...)`.transform(...).filter(...).route(...).handle(...).get()`.
- **`IntegrationFlow` bean**:
  ```java
  @Bean
  IntegrationFlow orderFlow() {
      return IntegrationFlows.from("orders.input")
          .filter(Order.class, o -> o.isValid())
          .transform(Order.class, this::enrich)
          .route(Order.class, o -> o.getType(), m -> m
              .subFlowMapping("STANDARD", sf -> sf.handle(standardHandler))
              .subFlowMapping("VIP", sf -> sf.handle(vipHandler)))
          .get();
  }
  ```
- **`@MessagingGateway`** — proxy interface, converts method call to message:
  ```java
  @MessagingGateway(defaultRequestChannel = "orders.input")
  interface OrderGateway {
      void placeOrder(Order order);
  }
  ```
- **Apache Camel** — alternative EIP implementation, language-agnostic (runs on JVM, also Kotlin/Quarkus). Different model — `RouteBuilder` DSL:
  ```java
  from("file:input?noop=true")
      .filter(body().contains("important"))
      .to("jms:queue:important.messages");
  ```
- **Camel components** — 300+ components: file, jms, kafka, http, rest, websocket, aws, azure, gcp, mongodb, redis, sql, elasticsearch, etc.
- **Spring Integration vs Apache Camel**:
  - Spring Integration — native Spring, integrates seamlessly with Spring beans, `@Component`-style. Less components (file, ftp, http, jdbc, jms, kafka, mail, mongodb, redis, sftp, syslog, websocket, xml, etc.). Better for Spring-only projects.
  - Apache Camel — language-agnostic, more components, more mature ecosystem. Better for complex integration projects. Has Spring integration.
- **`CamelContext`** — runtime container for routes. Lifecycle: start, stop.
- **`RouteBuilder`** — Java DSL for route definition. `from(uri).process(processor).to(uri)`.
- **Endpoint DSL** (Camel 3+) — type-safe endpoints: `from(file("input")).to(jms("queue:orders"))`.
- **EIP in Camel** — full EIP support: content-based router, message filter, splitter, aggregator, resequencer, multicast, recipient list, wire tap, claim check, throttler, delayer, load balancer, etc.
- **`Processor` / `Bean`** — processing logic. `Processor.process(Exchange)`.
- **`Exchange`** — message envelope: in-message, out-message, properties, exception.
- **Camel error handling**: `onException(Exception.class).handled(true).to("jms:queue:dlq")`. `errorHandler(deadLetterChannel("jms:queue:dlq").maximumRedeliveries(3))`.
- **`@EndpointInject` / `@Produce` / `@Consume`** — Spring integration annotations for Camel endpoints.
- **Enterprise Integration Patterns book** — Gregor Hohpe, Bobby Woolf (Addison-Wesley, 2003). The bible of integration patterns.
- **Spring Integration extensions**:
  - Spring Integration FTP/FTPS/SFTP.
  - Spring Integration HTTP/HTTPS.
  - Spring Integration JDBC.
  - Spring Integration JMS.
  - Spring Integration Kafka.
  - Spring Integration Mail.
  - Spring Integration MongoDB.
  - Spring Integration Redis.
  - Spring Integration AWS.
  - Spring Integration WebFlux (reactive).
  - Spring Integration WebSocket.
  - Spring Integration XML (XPath, XSLT).
  - Spring Integration Web Services (SOAP).
- **Spring Cloud Stream vs Spring Integration**:
  - Spring Cloud Stream — higher-level, binder-based, focus on event-driven microservices.
  - Spring Integration — lower-level, in-app integration patterns.
- **`IntegrationGraphEndpoint`** — actuator endpoint showing integration graph (`/actuator/integrationgraph`).
- **Micrometer integration** — Spring Integration автоматически exposes metrics per channel/handler.

### Материалы

- **docs.spring.io — Spring Integration Reference** — `https://docs.spring.io/spring-integration/reference/`
- **Spring Integration project page** — `https://spring.io/projects/spring-integration`
- **Apache Camel Documentation** — `https://camel.apache.org/manual/`
- **Apache Camel Components** — `https://camel.apache.org/components/`
- **Apache Camel EIP** — `https://camel.apache.org/components/4.18.x/eips/enterprise-integration-patterns.html`
- **Enterprise Integration Patterns website** — `https://www.enterpriseintegrationpatterns.com/`
- **Spring Integration + Apache Camel** — `https://docs.spring.io/spring-integration/reference/camel.html`
- **Baeldung — Spring Integration** — `https://www.baeldung.com/spring-integration`
- **Baeldung — Apache Camel with Spring Boot** — `https://www.baeldung.com/apache-camel-spring-boot`
- **Camel Spring Boot starter** — `https://camel.apache.org/camel-spring-boot/latest/spring-boot.html`
- **Spring blog — Spring Integration 6** — `https://spring.io/blog/2022/11/15/spring-integration-6-0-goes-rc1`
- **Reflectoring — Spring Integration** — `https://reflectoring.io/spring-integration/`
- **Camel in Action, Second Edition (Claus Ibsen, Jonathan Anstey)** — Manning, ISBN 978-1617292934
- **Enterprise Integration Patterns (Gregor Hohpe, Bobby Woolf)** — Addison-Wesley, ISBN 978-0321206836 (must-read for integration patterns)
- **Книга — "Pro Spring Integration" (Josh Long, Gary Mak)** — Apress

### Практика / Кейсы

1. **Spring Integration flow для file → JMS**: `IntegrationFlow` который мониторит директорию, парсит CSV файлы, сплитит в строки, фильтрует валидные, публикует в JMS queue. Cover с embedded ActiveMQ. **Цель:** classic EIP pipeline.
2. **Apache Camel route для REST → Kafka**: `from("rest:get:/users/{id}").to("kafka:users")`. Swagger doc через `restConfiguration().apiContextPath("/api-doc")`. Cover с Testcontainers Kafka. **Цель:** Camel REST integration.
3. **Content-based router**: route messages по типу (STANDARD → standard-channel, VIP → vip-channel, ERROR → error-channel). Демонстрировать `@Router` и DSL `.route(...)`. Cover unit test. **Цель:** routing patterns.
4. **Aggregator для batch**: split большой файл на 1000 records, каждый обрабатывается отдельно, aggregator ждёт 1000 завершённых, потом batch write в DB. Release strategy: `MessageCountReleaseStrategy(1000)`. **Цель:** aggregator pattern.
5. **Wire tap для audit**: основной flow копирует каждое сообщение в audit channel (non-blocking). Audit channel → Kafka topic для исторического анализа. Cover test. **Цель:** wire tap.
6. **Camel error handling с DLQ**: route `from("kafka:orders").process(...).to("jpa:Order")`. `onException(PersistenceException.class).maximumRedeliveries(3).useExponentialBackoff().to("kafka:orders.dlq")`. Cover test with poison message. **Цель:** Camel error handling.
7. **Spring Integration gateway для async API**: `@MessagingGateway` interface для order placement. Async response через `@Gateway(replyChannel = "...", requestChannel = "...")`. Cover с timeout. **Цель:** sync-to-async bridge.
8. **Camel + Spring Boot observability**: Micrometer metrics для Camel routes (`camel.routes`, `camel.exchanges`). Distributed tracing через OpenTelemetry (Camel 3+ tracing). Tempo/Grafana. **Цель:** Camel observability.
9. **Spring Integration vs Camel — same ETL**: реализовать один и тот же ETL (file → transform → DB) на Spring Integration и на Camel. Сравнить code size, readability, performance, components availability. **Цель:** informed choice.
10. **EIP claim check**: большой attachment сохраняется в S3, в message channel передаётся только claim (S3 URL). Получатель скачивает из S3 по claim. Cover с LocalStack S3. **Цель:** claim check pattern.

---
## 17. Spring Boot Test

### Теория

- **Spring Boot Test** — модуль `spring-boot-starter-test`, который включает: JUnit 5 (Jupiter), AssertJ, Hamcrest, Mockito, JSONassert, JsonPath, Spring Test, Spring Boot Test. С Boot 3.4 также добавлен `spring-boot-testcontainer-utility` и интеграция с JUnit 5 extensions.
- **TestContext Framework (TCF)** — ядро Spring Test, управляет `ApplicationContext` для тестов, кэширует его между тестами, отвечает за DI в тестах, transactional rollback, mock-инъекцию. Точка входа — `SpringExtension` (JUnit 5) или `SpringRunner` (JUnit 4).
- **Context cache** — TCF кэширует контексты по уникальному ключу (`MergedContextConfiguration`). Конфигурация контекста определяется: классами `@Configuration`, `@ComponentScan`, `@PropertySource`, profiles, `initializers`, `@WebAppConfiguration`, `resourceLocations`, `@DirtiesContext`. Одинаковый ключ — переиспользование (быстро). Разный ключ — новый контекст (медленно).
- **`@SpringBootTest`** — full integration test: поднимает полный `ApplicationContext`. Без аргументов ищет `@SpringBootConfiguration` (обычно main-класс). Параметры:
  - `classes` — какие конфиг-классы использовать.
  - `properties` — переопределить свойства (`@SpringBootTest(properties = "spring.jpa.hibernate.ddl-auto=create-drop")`).
  - `webEnvironment` — `MOCK` (по умолчанию, `MockServletContext` + `MockMvc`), `RANDOM_PORT` (стартует реальный embedded сервер на случайном порту), `DEFINED_PORT` (`server.port`), `NONE` (только контекст, без web).
  - `args` (Boot 3.x) — аргументы командной строки для `SpringApplication`.
- **`@SpringBootConfiguration`** — мета-аннотация, которую `@SpringBootApplication` наследует. Указывает, что класс — primary config. Если у теста нет `@SpringBootConfiguration` в package — надо указать `@SpringBootTest(classes = ...)`.
- **Test slices** — узкие тесты, поднимающие минимальный контекст (быстрее, изолированнее):
  - **`@WebMvcTest(MyController.class)`** — только web-слой: `DispatcherServlet`, `MockMvc`, `@Controller`/`@ControllerAdvice`/`HandlerMethodArgumentResolver`/`Filter`/`HandlerInterceptor`. Репозитории/сервисы НЕ поднимаются — их надо мокать (`@MockitoBean`).
  - **`@DataJpaTest`** — только JPA: `EntityManager`, `Repository`, `DataSource`. По умолчанию заменяет `DataSource` на embedded H2 (через `@AutoConfigureTestDatabase`), запускает каждый тест в транзакции с rollback. `@EntityScan` требуется, если entities вне скана.
  - **`@DataR2dbcTest`** — реактивный R2DBC slice.
  - **`@DataMongoTest` / `@DataRedisTest` / `@DataNeo4jTest` / `@DataCassandraTest` / `@DataLdapTest`** — аналоги для NoSQL.
  - **`@JdbcTest`** — `JdbcTemplate` + embedded H2, без JPA.
  - **`@MyBatisTest`** (через `mybatis-spring-boot-starter-test`).
  - **`@JsonTest`** — `JacksonTester`/`GsonTester`/`JsonbTester`, `ObjectMapper` для тестов JSON-сериализации.
  - **`@RestClientTest`** — `RestTemplateBuilder`/`WebClient.Builder` + `MockWebServiceServer`/`MockRestServiceServer`, тестирует REST-клиенты.
  - **`@WebServiceClientTest`** — SOAP-клиенты.
  - **`@HttpClientTest`** (Boot 3.4) — для `RestClient`/`RestTemplate`/`WebClient`/`HttpClient` в одном slice.
- **`@MockBean` / `@SpyBean`** (Spring Boot Test) — **DEPRECATED с Boot 3.4.0** (ноябрь 2024). Заменены на `@MockitoBean` / `@MockitoSpyBean` из `org.springframework.test.context.bean.override.mockito` (Spring Framework 6.2+). Причина: `@MockBean`/`@SpyBean` жёстко модифицировали `BeanDefinition` в context-cache ключе, что приводило к созданию нового контекста для каждого уникального набора моков. Новые `@MockitoBean`/`@MockitoSpyBean` используют `BeanOverride`-инфраструктуру, которая не меняет ключ контекста — кэш работает эффективнее. Старые аннотации работают до Boot 3.5/4.0, но миграция рекомендована.
- **`@MockitoBean`** — заменяет bean в контексте на Mockito-mock. Атрибуты: `value`/`name` (имя bean-а), `types` (тип, если разрешается неоднозначно), `extraInterfaces`, `answers`, `serializationMode`. По умолчанию mock reset'ится после каждого тест-метода (`MockReset.AFTER_EACH_TEST_METHOD`).
- **`@MockitoSpyBean`** — заменяет bean на Mockito-spy (partial mock, реальные методы вызываются, пока не override'нуты).
- **`@MockitoBean` vs `@Mock` (Mockito)**: `@Mock` создаёт новый mock в поле теста, но **не кладёт его в Spring-контекст**. `@MockitoBean` регистрирует mock в контексте, заменяя реальный bean.
- **`@TestConfiguration`** — конфигурация, которая НЕ подхватывается component scan, а подключается через `@Import(MyTestConfig.class)` или автоматически, если находится в том же package, что и тест, и имеет `static` модификатор. Используется для тестовых bean-ов.
- **`@DynamicPropertySource`** — программное добавление свойств в `Environment` во время теста (например, для Testcontainers: `@DynamicPropertySource static void props(DynamicPropertyRegistry r) { r.add("spring.datasource.url", postgres::getJdbcUrl); }`). Альтернатива `@TestPropertySource` для динамических значений.
- **Testcontainers integration** (Boot 3.1+): `@ServiceConnection` на поле-контейнере автоматически настраивает `DataSource`/`RedisConnectionFactory`/`MongoClient`/etc. без `@DynamicPropertySource`. Пример: `@Container @ServiceConnection static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:16");`. Требуется `spring-boot-testcontainers` зависимость.
- **`@Container` (JUnit 5)** + `@Testcontainers` — lifecycle контейнера: `static` поле — общий для всех тестов в классе (контейнер стартует once), instance-поле — новый контейнер на каждый тест (медленно). Использовать `static` для общего `PostgreSQLContainer`/`KafkaContainer`/`GenericContainer`.
- **`MockMvc`** — server-side Spring MVC testing без запуска HTTP-сервера. Через `DispatcherServlet` напрямую. Использует `MockHttpServletRequest`/`MockHttpServletResponse`. Авто-настраивается через `@AutoConfigureMockMvc`. Методы: `.perform(get("/api/...").param(...)).andExpect(status().isOk()).andExpect(jsonPath("$.id").value(1));`. Быстрее, чем `@SpringBootTest(RANDOM_PORT)` + `TestRestTemplate`.
- **`@AutoConfigureMockMvc`** — добавляет `MockMvc` в контекст (обычно комбинируется с `@WebMvcTest` или `@SpringBootTest`).
- **`WebTestClient`** — для WebFlux: можно использовать как для reactive endpoints (без сервера), так и для MVC (через embedded сервер). Подключается через `@AutoConfigureWebTestClient`.
- **`TestRestTemplate`** — `RestTemplate` для `@SpringBootTest(webEnvironment = RANDOM_PORT)`. Упрощённая работа с relative URLs (`@LocalServerPort`).
- **`@LocalServerPort`** — `@Value("${local.server.port}")`-альтернатива: инжектит порт запущенного embedded сервера.
- **`OutputCaptureExtension` / `@ExtendWith(OutputCaptureExtension.class)` + `CapturedOutput`** — JUnit 5 extension, который захватывает `System.out`/`System.err` для assert'ов на лог.
- **`@DirtiesContext`** — помечает контекст как «грязный» после теста/класса/метода → контекст удаляется из кэша и пересоздаётся для следующего теста. Использовать осторожно — убивает performance. Параметры: `@DirtiesContext(classMode = ClassMode.AFTER_EACH_TEST_METHOD)`, `@DirtiesContext(methodMode = MethodMode.AFTER_METHOD)`. Применять, когда тест необратимо меняет состояние контекста (singleton bean, cache eviction, schema changes).
- **`@Transactional` в тестах** — по умолчанию rollback после каждого теста. Если нужен commit: `@Rollback(false)` или `@Commit`. Доступен только если в контексте есть `PlatformTransactionManager`.
- **`@Sql`** — выполняет SQL-скрипты до/после тест-метода: `@Sql(scripts = "/test-data.sql", executionPhase = BEFORE_TEST_METHOD)`. `@SqlGroup` для нескольких скриптов.
- **`@TestPropertySource`** — программная установка свойств: `@TestPropertySource(properties = {"spring.datasource.url=jdbc:h2:mem:test", "feature.flag=true"})`. Альтернатива — `properties` на `@SpringBootTest`.
- **`ApplicationContextRunner`** (Spring Framework) — fluent API для тестирования авто-конфигурации: `new ApplicationContextRunner().withUserConfiguration(MyAutoConfig.class).withPropertyValues("my.prop=value").run(context -> assertThat(context).hasSingleBean(MyService.class));`. Используется для unit-тестов авто-конфигурации без поднятия Spring Boot.
- **`ApplicationContextRunner` variants**: `WebApplicationContextRunner`, `ReactiveWebApplicationContextRunner`. Используются для тестирования web-specific auto-config.
- **`@SpringJUnitConfig` / `@SpringJUnitWebConfig`** — упрощённые `@ExtendWith(SpringExtension.class) + @ContextConfiguration`, без Spring Boot. Для чистых Spring-тестов.
- **`@BootstrapWith`** — extension point для кастомизации TCF. Spring Boot использует `SpringBootTestContextBootstrapper` для full `@SpringBootTest`, `WebMvcTestContextBootstrapper` для `@WebMvcTest`, и т.д.
- **`MockServerWebExchange` / `EntityExchangeResult`** — для реактивных WebFlux endpoint-тестов через `WebTestClient`.
- **`@Nested` (JUnit 5) + Spring** — поддерживается Spring Test: nested тест-классы могут иметь свои `@MockitoBean`/`@DynamicPropertySource`. Полезно для группировки тестов одного feature.
- **`@ClassOrderer` / `@MethodOrderer` (JUnit 5)** — не Spring-специфика, но важно: по умолчанию JUnit 5 nondeterministic order. Для зависимых integration-тестов указать `@TestMethodOrder(MethodOrderer.OrderAnnotation.class)`.
- **`@Timeout` (JUnit 5)** — таймаут на тест-метод. Полезно для async/reactive-тестов.
- **Async tests**:
  - В Spring MVC: `MockMvc` + `asyncDispatch()` для `DeferredResult`/`Callable`/`ResponseBodyEmitter`.
  - В WebFlux: `StepVerifier` из Reactor — самый idiomatic способ тестировать `Mono`/`Flux`. `StepVerifier.create(flux).expectNext("a").expectNext("b").verifyComplete();`.
- **`@MetaAnnotation`-композиция** — можно создавать мета-аннотации типа `@IntegrationTest`, объединяющие `@SpringBootTest + @ActiveProfiles("it") + @TestPropertySource(...)`. Распространённый паттерн в enterprise-проектах.
- **Spring Boot Testcontainers «DevServices» / «Built-in Testcontainers»** (Boot 3.1+): для тестов можно использовать `@ServiceConnection` для PostgreSQL, MySQL, MariaDB, MSSQL, Oracle XE, MongoDB, Redis, Neo4j, Cassandra, ClickHouse, Pulsar, ActiveMQ, RabbitMQ, Kafka, Zipkin, Localstack, Elasticsearch, OpenSearch, Azurite, Selenium, Vault и т.д. Под капотом — `ContainerConnectionDetailsFactory`.
- **`@DataJpaTest` + `@AutoConfigureTestDatabase(replace = NONE)`** — для тестирования на реальной БД через Testcontainers, а не на embedded H2.
- **Slice test isolation**: каждый slice имеет свой `BootstrapWith`. Slice не поднимает полный `@SpringBootApplication`, только нужные компоненты. Если bean не поднимается в slice, но нужен — `@Import(MyComponent.class)` или `@TestConfiguration` с явным `@Bean`.
- **`@SpringBootTest` + `@AutoConfigureTestDatabase`** — в отличие от `@DataJpaTest`, не включает транзакционный rollback автоматически. Использовать `@Transactional` явно или `@Sql` для data setup.
- **Test execution time optimization**:
  - Минимизировать `@DirtiesContext` (главный виновник медленных тестов).
  - Использовать slices вместо `@SpringBootTest` где можно.
  - Переиспользовать Testcontainers через `static` поля + `@ServiceConnection`.
  - `@MockitoBean` вместо `@MockBean` (Boot 3.4+, лучше context cache hit).
  - Параллелизация через JUnit 5 `junit.jupiter.execution.parallel.enabled=true` (но осторожно с общими ресурсами).
  - Layered/test JAR slicing для ускорения startup.
- **Spring Boot 3.4 testing features**:
  - `@MockitoBean` / `@MockitoSpyBean` (новые).
  - Улучшенная поддержка `@Nested` тестов с `@DynamicPropertySource`.
  - `spring.threads.virtual.enabled=true` работает и для тестовых executor'ов.
  - Новые slice annotations: `@HttpClientTest`.
  - Встроенные Testcontainers «singleton containers» через `Container.DockerImageName`-parsing.
- **Contract testing**: Spring Cloud Contract (CDC) — для consumer-driven contracts. Producer генерирует тесты из контракта, consumer stubs через WireMock. Не путать с REST Docs (который генерирует документацию, не stubs).

### Материалы

- **docs.spring.io — Testing Spring Boot Applications** — `https://docs.spring.io/spring-boot/reference/testing/spring-boot-applications.html`
- **docs.spring.io — Test Scopes** (`@SpringBootTest`, slices) — `https://docs.spring.io/spring-boot/reference/testing/spring-boot-applications.html#spring-boot-applications.testing`
- **docs.spring.io — Auto-configured Spring MVC Tests** — `https://docs.spring.io/spring-boot/reference/testing/spring-boot-applications.html#spring-boot-applications.testing.spring-mvc`
- **docs.spring.io — Auto-configured Data JPA Tests** — `https://docs.spring.io/spring-boot/reference/testing/spring-boot-applications.html#spring-boot-applications.testing.spring-data-jpa`
- **docs.spring.io — Testcontainers support in Spring Boot** — `https://docs.spring.io/spring-boot/reference/testing/testcontainers.html`
- **spring.io/blog — Improved Testcontainers Support in Spring Boot 3.1** — `https://spring.io/blog/2023/06/23/improved-testcontainers-support-in-spring-boot-3-1`
- **spring.io/blog — Spring Boot 3.4 Release Notes (testing changes)** — `https://github.com/spring-projects/spring-boot/wiki/Spring-Boot-3.4-Release-Notes`
- **Spring Framework reference — Testing** — `https://docs.spring.io/spring-framework/reference/testing.html`
- **Spring Framework reference — MockMvc** — `https://docs.spring.io/spring-framework/reference/testing/web.html#spring-mvc-test-framework`
- **GitHub issue — `@MockitoBean` migration** — `https://github.com/spring-projects/spring-boot/issues/43348`
- **Baeldung — Testing in Spring Boot** — `https://www.baeldung.com/spring-boot-testing`
- **Baeldung — Spring Boot Slices Tests** — `https://www.baeldung.com/spring-boot-test-slices`
- **Baeldung — Guide to `@MockBean` and `@MockitoBean`** — `https://www.baeldung.com/java-spring-mockito-bean`
- **Baeldung — Testcontainers in Spring Boot** — `https://www.baeldung.com/spring-boot-built-in-testcontainers`
- **Reflectoring — Spring Boot Test slices** — `https://reflectoring.io/spring-boot-test/`
- **Spring blog — `@MockitoBean` announcement** — `https://spring.io/blog/2024/10/01/spring-framework-6-2-0-rc1-available-now` (Spring 6.2 release notes)
- **Docker blog — Building Spring Boot's ServiceConnection for Testcontainers** — `https://www.docker.com/blog/building-spring-boots-serviceconnection-for-testcontainers-wiremock`
- **Книга — "Spring Boot in Action" (Craig Walls), Глава 4: Testing** — Manning
- **Книга — "Pro Spring 6" (Walls/Schaefer/Harrop), Глава 18: Testing with Spring** — Apress, ISBN 978-1484286401
- **Книга — "Spring in Action, Sixth Edition" (Craig Walls), Глава 4: Testing Spring applications** — Manning, ISBN 978-1617297944

### Практика / Кейсы

1. **REST API: Boot 3 + Data JPA + Testcontainers + slice tests + Spring Security JWT + Resilience4j + dockerize + CI GitHub Actions**: полноценное приложение «Order Service»:
   - `OrderController` (Web MVC) + `OrderService` + `OrderRepository` (JPA) + `SecurityConfig` (JWT via OAuth2 Resource Server) + `PaymentClient` (Feign + Resilience4j circuit breaker).
   - **Unit-тесты** (no Spring): `OrderServiceTest` через Mockito `@ExtendWith(MockitoExtension.class)` + `@Mock` для `OrderRepository` и `PaymentClient`.
   - **Slice `@WebMvcTest(OrderController.class)`**: `MockMvc` + `@MockitoBean OrderService` + `@WithMockUser` (Spring Security test). Проверка JSON-structure через `jsonPath`, validation errors через `ProblemDetail` (RFC 7807).
   - **Slice `@DataJpaTest`**: с `@AutoConfigureTestDatabase(replace = NONE)` + `@ServiceConnection static PostgreSQLContainer<?>`. Тесты `findByCustomer` (custom query), `findAll(Pageable)`, optimistic locking (`@Version`).
   - **`@SpringBootTest(RANDOM_PORT)` integration**: с `@ServiceConnection` PostgreSQL + `WireMockContainer` для mock payment service + `TestRestTemplate` для end-to-end HTTP. Тест JWT-авторизации (token из Keycloak Testcontainer или self-signed via Nimbus).
   - **Resilience4j test**: circuit breaker открывается после 5 failures — симулировать через WireMock returning 500, проверить `CircuitBreaker` state через `CircuitBreakerRegistry`.
   - **Testcontainers singleton pattern**: `abstract class AbstractIntegrationTest` со `static` PostgreSQL/WireMock контейнерами, от которого наследуются все IT-тесты (1 контейнер на весь test-run).
   - **Dockerize**: multi-stage `Dockerfile` с layered JAR (`spring-boot-jarmode-layertools`), `docker-compose.yml` с PostgreSQL + app.
   - **CI GitHub Actions**: `mvn verify` + service-mapped PostgreSQL + upload Surefire-отчётов; `mvn test -Pnative` (опционально) на отдельной job с GraalVM setup-action.
   **Цель:** production-ready testing pyramid.

2. **`@MockitoBean` migration с `@MockBean`**: refactor тестов с Boot 3.3 на 3.4 — заменить все `@MockBean`/`@SpyBean` на `@MockitoBean`/`@MockitoSpyBean`. Сравнить количество созданных ApplicationContext (через `@SpringBootTest` debug logging: `logging.level.org.springframework.test.context.cache=DEBUG`). Доказать, что context cache hit-rate вырос. Использовать OpenRewrite recipe `org.openrewrite.java.spring.boot4.ReplaceMockBeanAndSpyBean` для автоматической миграции. **Цель:** понять новую `BeanOverride`-инфраструктуру.

3. **`@DataJpaTest` + `@ServiceConnection` Testcontainers PostgreSQL**: тесты для `OrderRepository`:
   - Schema management через Flyway (`flyway:migrate` автоматически при старте контейнера).
   - Тесты на `@EntityGraph` (запрет N+1) — включить Hibernate SQL logging, assert count of `SELECT` queries через `@PersistenceContext EntityManager` + custom `StatementInspector`.
   - Тесты на `@Version` (optimistic locking) — два потока пытаются обновить одну запись, второй получает `OptimisticLockException`.
   - Тесты на `Specification` (dynamic queries) — `where(hasCustomer(customerId).and(hasStatus(OrderStatus.SHIPPED))`).
   - Тесты на `Pageable` (offset/limit) — assert SQL генерирует `LIMIT ... OFFSET ...` (PostgreSQL dialect).
   - Тесты на `@Modifying` queries (bulk update) — `UPDATE Order o SET o.status = :status WHERE o.createdAt < :date` + `@Modifying(clearAutomatically = true, flushAutomatically = true)`.
   **Цель:** покрыть JPA edge cases на реальной БД.

4. **`@WebMvcTest` + `@ControllerAdvice` + validation**: тест контроллера с `@Valid @RequestBody OrderDto`:
   - Happy path: валидный DTO → 201 Created с правильным JSON.
   - Validation error: `@NotNull` violation → 400 Bad Request + `ProblemDetail` (RFC 7807) с field errors в `errors` array.
   - `@ExceptionHandler(OrderNotFoundException.class)` → 404 + `ProblemDetail`.
   - `@ExceptionHandler(MethodArgumentNotValidException.class)` → 400 + structured errors.
   - Content negotiation: `Accept: application/xml` → 406 Not Acceptable (если нет XML converter).
   - Async endpoint (`DeferredResult`): `MockMvc.perform(asyncDispatch(result))`.
   - `@WithMockUser(roles = "ADMIN")` для security checks.
   **Цель:** покрыть все error/response paths Web-слоя.

5. **`@SpringBootTest` + WireMock + Testcontainers Kafka end-to-end**: тест full event-driven flow:
   - HTTP-запрос на POST `/api/orders` → `OrderService` создаёт запись в PostgreSQL (Testcontainer) → публикует `OrderCreatedEvent` в Kafka (Testcontainer) → `@KafkaListener` в `InventoryService` обновляет inventory → отправляет подтверждение.
   - Использовать `Awaitility` для асинхронных проверок: `await().atMost(10, SECONDS).untilAsserted(() -> assertThat(inventoryRepository.findByProductId(...)).hasValue(99));`.
   - WireMock для mock внешнего payment service (`WireMockContainer`).
   - Проверить Distributed Tracing через `@MicrometerTest` + assert trace propagation (traceId passed через Kafka headers).
   **Цель:** integration test для event-driven архитектуры.

6. **`ApplicationContextRunner` для авто-конфигурации**: написать собственную `MyLibraryAutoConfiguration` с `@ConditionalOnProperty`, `@ConditionalOnClass`, `@ConditionalOnMissingBean`. Юнит-тесты через `ApplicationContextRunner`:
   - When `my.feature.enabled=true` and class on classpath → `MyService` bean exists.
   - When `my.feature.enabled=false` → no bean.
   - When user-defined `MyService` exists → auto-config backs off (`@ConditionalOnMissingBean`).
   - When `MyService` has `@ConfigurationProperties` — assert `@EnableConfigurationProperties` registers it.
   - Test `@AutoConfigureBefore` / `@AutoConfigureAfter` ordering.
   **Цель:** освоить testing авто-конфигурации — критично для library authors.

7. **`@DirtiesContext` performance measurement**: написать 100 integration-тестов. Сначала с `@DirtiesContext(classMode = AFTER_EACH_TEST_METHOD)` — измерить total time. Затем убрать — измерить снова. Затем разбить на 5 test-классов с общим base — измерить. Вывести формулу: «каждый `@DirtiesContext` × количество тестов × startup time ≈ total overhead». **Цель:** обосновать для команды отказ от `@DirtiesContext`.

8. **Reactive WebFlux slice tests**: `@WebFluxTest(OrderReactiveController.class)` + `WebTestClient`:
   - `webTestClient.get().uri("/orders/{id}", 1).exchange().expectStatus().isOk().expectBody().jsonPath("$.id").isEqualTo(1);`
   - Test backpressure: `Flux<Order>` с задержкой — assert subscriber получает элементы порциями.
   - Test SSE endpoint: `expectHeader().contentType(MediaType.TEXT_EVENT_STREAM)`, parse `data:` lines.
   - `@MockitoBean` для `OrderRepository` (Reactive CRUD).
   - `StepVerifier` для unit-test реактивных сервисов (без Spring).
   **Цель:** покрыть reactive stack.

9. **Testcontainers «single container per test run» pattern**: реализовать через JUnit 5 `@Testcontainers` + `static` поле + `@ServiceConnection`:
   ```java
   @SpringBootTest
   @Testcontainers
   abstract class BaseIT {
       @Container @ServiceConnection
       static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:16-alpine");
   }
   ```
   Подклассы наследуют общий контейнер. Контекст Spring переиспользуется между тестами (если другие свойства совпадают). Сравнить время выполнения 100 тестов с instance-vs-static контейнером. **Цель:** оптимизация integration-тестов.

10. **Contract testing with Spring Cloud Contract**: producer и consumer сервисы:
    - Producer: контракт YAML в `contracts/orders_should_return_200.yaml`, Spring Cloud Contract Verifier генерирует JUnit-тесты, которые проверяют producer.
    - Consumer: Spring Cloud Contract Stub Runner поднимает WireMock со стабами, сгенерированными из контрактов producer'а.
    - CI: producer публикует stubs в Artifactory/Nexus; consumer скачивает по GAV.
    - Тест consumer: `@AutoConfigureStubRunner(ids = "com.acme:order-service:+:stubs:8080")`.
    **Цель:** освоить CDC для безопасной эволюции микросервисных API.

11. **Open Rewrite для тест-миграций**: применить recipes:
    - `org.openrewrite.java.spring.boot4.ReplaceMockBeanAndSpyBean` — `@MockBean` → `@MockitoBean`.
    - `org.openrewrite.java.spring.boot3.MigrateJUnitSpringRunner` — `@RunWith(SpringRunner)` → `@ExtendWith(SpringExtension)`.
    - `org.openrewrite.java.testing.junit5.JUnit5BestPractices` — JUnit 4 → 5.
    - `org.openrewrite.java.spring.boot3.UpgradeSpringBoot_3_4` — общий апгрейд.
    Покрыть отчёт: «что изменилось», «сколько файлов», «что сломалось». **Цель:** автоматизация миграций.

12. **Performance baseline через JMH в тестах**: использовать JUnit Benchmark или JMH в integration-тестах для измерения startup time с/без native image, с/без AOT processing. Записать baseline в `baseline.json`, использовать в CI для regression detection (через `assertThat(startupTimeMs).isLessThan(baseline * 1.1)`). **Цель:** prevent performance regressions.

---


## 18. Spring Modulith

### Теория

- **Spring Modulith** (1.0 GA в ноябре 2023, подглавление Spring 6.x) — подход и библиотека для структурирования Spring Boot приложений как **modular monolith**. Цель: явные границы модулей внутри монолита, контролируемая коммуникация, поддерживаемость без микросервисов.
- **Мотивация**: микросервисы — это high operational cost (deploy, observe, debug distributed). Modular monolith — компромисс: единый deployment unit, но с явной архитектурной структурой. Spring Modulith даёт инструменты, чтобы enforcement границ был кодом, а не «convention over code review».
- **Module definition**: Spring Modulith рассматривает каждый package напрямую под root-пакетом приложения (например, `com.acme.orders`, `com.acme.inventory`) как модуль. Модуль = main package + subpackages.
- **Application module**: main package может содержать `package-info.java` с аннотацией `@org.springframework.modulith.ApplicationModule` для дополнительных настроек (allowed dependencies, named events, etc.).
- **Module by convention**: если `package-info.java` отсутствует, модуль всё равно определяется по package structure.
- **`ApplicationModules.of(MyApplication.class)`** — entry point API для inspecting модулей приложения. Возвращает `ApplicationModules` со всеми модулями, их зависимостями, внешними (public) типами, internal типами.
- **External vs internal types**: Spring Modulith считает types в **main package** (прямые children, не subpackages) «public»/external — их могут импортировать другие модули. Types в **subpackages** — internal, не должны использоваться вне модуля.
- **`NamedInterface`**: можно явно объявить named interface (Java package concept) через `package-info.java`:
  ```java
  @org.springframework.modulith.NamedInterface("order-api")
  package com.acme.orders;
  ```
  Тогда другие модули могут явно зависеть от `"order-api"`: `@ApplicationModule(allowedDependencies = "orders :: order-api")`.
- **`@ApplicationModule`** (на `package-info.java`):
  - `allowedDependencies` — список модулей (или named interfaces), от которых можно зависеть. По умолчанию модули могут зависеть от всех.
  - `displayName` — human-readable имя для documentation.
  - `open` (boolean) — если `true`, модуль не имеет ограничений на incoming dependencies (legacy module).
- **Module dependencies verification**: `ApplicationModulesVerifier` (JUnit 5 integration) — assert, что фактические зависимости модулей соответствуют заявленным. Тест:
  ```java
  @Test
  void verifiesModularStructure() {
      ApplicationModules.of(MyApplication.class).verify();
  }
  ```
  Тест падает, если модуль зависит от internal type другого модуля или от не-declared модуля.
- **`@ApplicationModuleTest`** (Spring Modulith Test) — extension `@SpringBootTest`, который автоматически создаёт `ApplicationModules` и применяет тестовый контекст только с bean-ами из тестируемого модуля + его dependencies. Slice test для модуля.
- **Module events**: Spring Modulith formalizes публикацию и потребление domain events между модулями. Событие, опубликованное в одном модуле, может быть обработано listener-ом в другом.
- **`ApplicationEventPublisher.publishEvent(...)`** — стандартный Spring API. Spring Modulith перехватывает publication и логирует/трекает.
- **`@ApplicationModuleListener`** (Spring Modulith) — `@TransactionalEventListener(AFTER_COMMIT)` + `@Async` по умолчанию. Listener в отдельной транзакции, не блокирует publisher.
- **Event externalization** (1.1+): автоматическая публикация domain events в Kafka/RabbitMQ/AMQP/SQS/SNS через `@Externalized` (Spring Modulith 1.3+):
  ```java
  @Externalized("orders.${target}.${event}") // routing key
  public record OrderCompletedEvent(UUID orderId) {}
  ```
  Под капотом — `EventExternalizationConfiguration` + `spring.modulith.events.externalization.enabled=true`.
- **Event Publication Registry**: Spring Modulith хранит опубликованные (но не подтверждённые listener-ом) события в БД (`EventPublicationRegistry`, JPA-реализация — `JpaEventPublicationRegistry`). При restart приложения listener-ы повторно получают неподтверждённые события (at-least-once delivery). Таблица `EVENT_PUBLICATION`.
- **`@TransactionalEventListener` vs `@ApplicationModuleListener`**: первый — синхронный (по умолчанию) или `@Async`, второй — всегда async + transactional + автоматически использует Event Publication Registry.
- **Completion of events**: после успешной обработки listener-ом события registry удаляет его из таблицы. При failure — сохраняется и ретраится при следующем старте.
- **Documentation generation**: Spring Modulith генерирует PlantUML-диаграммы модулей и их зависимостей:
  ```java
  new ApplicationModulesAssembler(ApplicationModules.of(MyApplication.class))
      .writeTo(new File("target/spring-modulith-docs"));
  ```
  Output: `modules.puml` (PlantUML), `module-<name>.puml`. Подключается через Asciidoctor для красивой HTML-документации.
- **PlantUML output** — visual representation модулей и их отношений. Полезно для архитектурных ADR.
- **`Documenter`** API — fluent API для генерации документации: `Documenter.of(ApplicationModules.of(MyApplication.class)).writeModulesAsPlantUml().writeIndividualModulesAsPlantUml().writeAggregatingDocument();`.
- **Asciidoctor integration**: Spring Modulith генерирует Asciidoctor-сниппеты, которые можно включать в основной `README.adoc` или архитектурную документацию. Требует `spring-modulith-docs` зависимости.
- **Spring Modulith + Spring Boot DevTools**: при изменении кода модуля dev-сервер рестартует контекст, Spring Modulith пере-вычисляет зависимости и показывает предупреждения в логах.
- **`@org.springframework.modulith.ApplicationModule` lifecycle**: модули могут быть «`open`» (no incoming restriction, для legacy), «`closed`» (по умолчанию, restricted).
- **Verifications**:
  - Cyclic dependency detection — если модуль A зависит от B, а B от A, тест `verify()` падает.
  - Cross-module internal access detection — если модуль A импортирует `com.acme.orders.internal.OrderInternal` (sub-package), тест падает.
  - Unused allowed dependencies detection — если `allowedDependencies = "inventory"` объявлен, но inventory не импортируется, тест может сообщить (warning level).
- **Modulith + DDD bounded contexts**: 1 модуль = 1 bounded context. Sub-modules для sub-domains. `@ApplicationModule` с nested packages.
- **Modulith + Hexagonal architecture**: модуль может быть структурирован как `orders/application`, `orders/domain`, `orders/infrastructure`. Spring Modulith позволяет это, но `verify()` нужно правильно настроить.
- **Migration path monolith → microservices**: Spring Modulith спроектирован так, что модули могут быть extracted в отдельные сервисы с минимальными изменениями. Domain events → asynchronous messaging. Internal API → external REST/Kafka. Цель: postpone decision о микросервисах до real need.
- **Spring Modulith vs Java Platform Module System (JPMS)**: JPMS — низкоуровневый Java module system (`module-info.java`, `requires`/`exports`). Spring Modulith — application-level, использует Spring container, гибче, не требует JPMS. Можно комбинировать (Modulith поверх JPMS), но обычно не делают.
- **Spring Modulith vs ArchUnit**: ArchUnit — generic library для архитектурных тестов (layered, package, class dependencies). Spring Modulith — специализирован для Spring, интегрирован с container, имеет event registry. Часто используются вместе: Modulith для модулей + events, ArchUnit для layer-rules (controller → service → repository).
- **Spring Modulith versions**: 1.0.x (Spring Boot 3.1), 1.1.x (Boot 3.2), 1.2.x (Boot 3.3), 1.3.x (Boot 3.4), 1.4.x (Boot 3.5). Aligned с Boot release trains.
- **`spring-modulith-events-*` modules**: `spring-modulith-events-api`, `spring-modulith-events-core`, `spring-modulith-events-jpa` (default), `spring-modulith-events-mongodb`, `spring-modulith-events-amqp` (externalization to RabbitMQ), `spring-modulith-events-kafka` (Kafka), `spring-modulith-events-jms` (JMS).
- **Event externalization configuration**: `application.yml`:
  ```yaml
  spring:
    modulith:
      events:
        externalization:
          enabled: true
          target: kafka:orders-events # generic
  ```
  Или программно через `EventExternalizationConfiguration`.
- **`@Externalized` routing**: SpEL-выражение для вычисления topic/routing key: `@Externalized("order-events.${event.type}")`. Поддерживает переменные `${event}`, `${target}`, `${module}`.
- **Modulith Actuator endpoint**: `/actuator/modulith` (Boot Actuator) — exposes module structure as JSON. Полезно для runtime inspection и observability.
- **Limitations Spring Modulith 1.x**:
  - Не запрещает cross-module beans на уровне runtime (только compile-time через verify()).
  - Не поддерживает реактивные события (только sync/async Spring events).
  - Event Publication Registry — single-database, не distributed.

### Материалы

- **spring.io — Spring Modulith project** — `https://spring.io/projects/spring-modulith`
- **docs.spring.io — Spring Modulith Reference** — `https://docs.spring.io/spring-modulith/reference/`
- **GitHub — spring-projects/spring-modulith** — `https://github.com/spring-projects/spring-modulith`
- **docs.spring.io — Spring Modulith Application Modules** — `https://docs.spring.io/spring-modulith/reference/fundamentals.html`
- **docs.spring.io — Verifying the Modular Structure** — `https://docs.spring.io/spring-modulith/reference/verification.html`
- **docs.spring.io — Working with Application Events** — `https://docs.spring.io/spring-modulith/reference/events.html`
- **docs.spring.io — Event Externalization** — `https://docs.spring.io/spring-modulith/reference/events.html#events.externalization`
- **docs.spring.io — Documenting the Architecture** — `https://docs.spring.io/spring-modulith/reference/documenting.html`
- **spring.io/blog — Simplified Event Externalization with Spring Modulith** — `https://spring.io/blog/2023/09/22/simplified-event-externalization-with-spring-modulith`
- **spring.io/blog — Spring Modulith 1.0 goes GA** — `https://spring.io/blog/2023/11/13/spring-modulith-1-0-goes-ga`
- **spring.io/blog — Spring Modulith 1.1** — `https://spring.io/blog/2023/11/21/spring-modulith-1-1-released`
- **Baeldung — Introduction to Spring Modulith** — `https://www.baeldung.com/spring-modulith`
- **Baeldung — Event Externalization with Spring Modulith** — `https://www.baeldung.com/spring-modulith-event-externalization`
- **Reflectoring — Spring Modulith** — `https://reflectoring.io/spring-modulith/`
- **Dan Vega — Spring Modulith Externalized Events: Publishing Events to Kafka** — `https://www.danvega.dev/blog/spring-modulith-externalized-events`
- **BellSoft blog — Build a modular monolith with Spring Modulith** — `https://bell-sw.com/blog/how-to-build-a-modular-application-with-spring-modulith`
- **Codecentric blog — Modularization the easy way: Spring Modulith with Kotlin and Hexagonal Architecture** — `https://www.codecentric.de/en/knowledge-hub/blog/modularization-the-easy-way-spring-modulith-with-kotlin-and-hexagonal-architecture`
- **Книга — "Modular Monoliths with Spring" (Oliver Drotbohm, в работе)** — следить за spring.io/blog
- **Spring blog — Spring Modulith 1.3 (Boot 3.4 aligned)** — `https://spring.io/blog/2024/11/21/spring-modulith-1-3-released`

### Практика / Кейсы

1. **Полный modular monolith e-commerce**: 4 модуля:
   - `orders` — Order entity, OrderService, OrderController. Public types: `Order`, `OrderId`, `OrderService`. Internal: `OrderEntity`, `OrderRepository` (JPA), `OrderServiceImpl`.
   - `inventory` — InventoryService, StockLevel. Subscribes to `OrderCompletedEvent` (decrement stock).
   - `shipping` — ShippingService. Subscribes to `OrderCompletedEvent` (queue shipment).
   - `customers` — CustomerService. `orders` depends on `customers`.
   - `package-info.java` в каждом модуле с `@ApplicationModule(allowedDependencies = {...})`.
   - `verify()` тест проходит. Добавить «плохую» зависимость (`orders.internal.OrderEntity` импортируется в `inventory`) → тест падает с понятным сообщением.
   **Цель:** освоить module structure.

2. **Event Publication Registry + recovery**: тест crash recovery:
   - OrderService публикует `OrderCompletedEvent` в транзакции (JPA).
   - InventoryService listener выбрасывает исключение → событие остаётся в `EVENT_PUBLICATION` таблице с `completion_date_time = NULL`.
   - Перезапустить приложение → Modulith находит незавершённое событие, re-invokes listener → если успешно, удаляет.
   - Cover integration test: restart context через `@DirtiesContext`, assert event re-processed.
   **Цель:** at-least-once delivery с persistence.

3. **Event externalization to Kafka**: доменное событие `OrderCompleted` публикуется автоматически в Kafka topic `orders-events`:
   - `@Externalized("orders-events")` на event record.
   - `spring.modulith.events.externalization.enabled=true` + `spring-modulith-events-kafka` dependency.
   - Конфигурация routing key через SpEL: `@Externalized("orders-${event.type}")`.
   - Integration test: Kafka Testcontainer, assert event arrived в topic.
   - Producer = Spring Modulith app, consumer = external service (можно тоже Spring Boot).
   **Цель:** unified API для internal и external events.

4. **Documentation generation**: подключить `spring-modulith-docs`, сгенерировать PlantUML-диаграммы:
   - `Documenter.of(...).writeModulesAsPlantUml().writeIndividualModulesAsPlantUml().writeAggregatingDocument();` в тесте.
   - Output: `target/spring-modulith-docs/modules.puml` + per-module.
   - Включить в Asciidoctor-generated `index.html` (`include::modules.puml[]`).
   - Подключить Asciidoctor Diagram extension (`@asciidoctor/plugin-asciidoctor-diagram`) для рендера PlantUML в PNG/SVG.
   - CI: build генерирует docs, publishes в GitHub Pages.
   **Цель:** living architectural documentation.

5. **Migration monolith → microservices**: спроектировать Spring Modulith monolith так, чтобы любой модуль можно было extract в микросервис за 1-2 sprint:
   - Module public API = REST endpoints (не in-process Java API).
   - Cross-module communication = events only (no direct method calls).
   - Database schema per module (через `@Table(schema = "orders")`) → легко split.
   - Тест: extract `inventory` модуль в отдельный Spring Boot приложение, publish `OrderCompletedEvent` в Kafka вместо in-process. `orders` модуль продолжает работать без изменений (потребляет тот же event type).
   **Цель:** design for extractability.

6. **`@ApplicationModuleTest` slice test**: тест одного модуля без поднятия всего приложения:
   - `@ApplicationModuleTest` на тест-классе для `orders` модуля.
   - В контексте: только bean-ы из `orders` + его зависимости (`customers`).
   - `inventory` и `shipping` модули не поднимаются → их `@ApplicationModuleListener`-ы не вызываются.
   - Мокать `OrderCompletedEvent` listener-ы если нужно проверить публикацию.
   - Сравнить startup time: `@ApplicationModuleTest` vs `@SpringBootTest`.
   **Цель:** module-level slice testing.

7. **Modulith + ArchUnit**: использовать оба:
   - Spring Modulith `verify()` — для module structure.
   - ArchUnit — для layered rules внутри модуля (`com.acme.orders.application` should not depend on `com.acme.orders.infrastructure`).
   - ArchUnit — для naming conventions (`*Controller`, `*Service`, `*Repository`).
   - ArchUnit — для prohibiting annotations (`@Field` не в `domain` package).
   **Цель:** combined architecture enforcement.

8. **Modulith observability**: включить `/actuator/modulith` endpoint, expose module dependencies. Настроить Micrometer-метрики для event publication latency (`spring.modulith.events.publication.duration`). Distributed tracing через Micrometer Tracing для cross-module event flows. Dashboard в Grafana с панелями «events per module», «events failed». **Цель:** observability для modular monolith.

9. **Modulith + Hexagonal architecture**: реализовать модуль `orders`:
   - `orders/application` — use cases (`PlaceOrderUseCase`, `CancelOrderUseCase`), ports (`OrderRepository`, `PaymentPort`).
   - `orders/domain` — `Order`, `OrderId`, domain events.
   - `orders/infrastructure` — JPA `OrderEntity`, `OrderRepositoryImpl`, `RestPaymentAdapter`.
   - `package-info.java` в `orders` — `@ApplicationModule(displayName = "Orders module")`.
   - Spring Modulith `verify()` проходит: только `application` package public, `domain` и `infrastructure` internal.
   - Тесты: unit tests для domain, `@ApplicationModuleTest` для use cases (с mocked ports).
   **Цель:** idiomatic Modulith + Hexagonal.

10. **Cyclic dependency detection**: создать ситуацию, где `orders` зависит от `inventory` (через `InventoryService`), а `inventory` от `orders` (через `OrderEvent`). Spring Modulith `verify()` падает. Решить через introduction `OrderEvent` в shared `events` module (или `orders` module) и `inventory` depends only on events, not on orders. **Цель:** understand и resolve cyclic dependencies.

---


## 19. Spring REST Docs / OpenAPI

### Теория

- **Документация REST API** в Spring-экосистеме имеет два главных подхода: **Spring REST Docs** (test-driven, генерация из тестов) и **springdoc-openapi** (annotation/ introspection-driven, OpenAPI 3 spec).
- **Spring REST Docs** (проект Spring, `spring-restdocs-mockmvc` / `spring-restdocs-webtestclient` / `spring-restdocs-restassured`) — генерирует документацию из **successful тестов**. Идея: тест проходит → документация точная. Тест падает → сборка падает → документация устаревшей быть не может.
- **REST Docs philosophy**: documentation by tests (always up-to-date), snippets (Asciidoctor/Markdown), integration with existing test framework.
- **Workflow Spring REST Docs**:
  1. Пишем `MockMvc` test.
  2. Оборачиваем `MockMvc` в `MockMvcRestDocumentation.documentationConfiguration(mockMvc)`.
  3. В тесте вызываем `.andDo(document("orders/get-by-id"))` — генерирует snippets в `target/generated-snippets/orders/get-by-id/`.
  4. В Asciidoctor-документе (`src/main/asciidoc/index.adoc`) включаем snippets: `include::{snippets}/orders/get-by-id/http-request.adoc[]`.
  5. `asciidoctor-maven-plugin` (или Gradle) собирает HTML/PDF.
- **Snippet types** (генерируются автоматически):
  - `http-request.adoc` — HTTP запрос (method, URI, headers, body).
  - `http-response.adoc` — HTTP ответ.
  - `curl-request.adoc` — эквивалентная `curl` команда.
  - `httpie-request.adoc` — эквивалентная HTTPie команда.
  - `request-body.adoc` — тело запроса (formatted JSON).
  - `response-body.adoc` — тело ответа.
  - `response-fields.adoc` — описание полей response (если указать `PayloadDocumentation.responseFields(...)`).
  - `request-fields.adoc` — описание полей request body.
  - `path-parameters.adoc` — path параметры.
  - `request-parameters.adoc` — query параметры.
  - `request-parts.adoc` — multipart parts.
  - `headers.adoc`, `response-headers.adoc` — headers.
  - `links.adoc` — HAL links.
- **`document(identifier, snippets...)`** — главный метод. Identifier — путь относительно `generated-snippets/`. Snippets — `PathParameters.parameter("id", "Order ID")`, `ResponseFields.field("id", "Order ID")`, etc.
- **`PayloadDocumentation.responseFields(FieldDescriptor...)`** — описывает поля response. `fieldWithPath("id").type(JsonFieldType.NUMBER).description("Order ID")`. Поддержка nested paths: `fieldWithPath("customer.id")`, arrays: `fieldWithPath("items[].id")`.
- **`Preprocessors`** — для форматирования HTTP request/response: `Preprocessors.preprocessRequest(prettyPrint())`, `Preprocessors.preprocessResponse(prettyPrint(), removeHeaders("X-Secret"))`. Кастомные preprocessors для masking sensitive data.
- **`Snippet` customization**: можно создать custom snippet через template (Asciidoctor `.snippet` template в `src/test/resources/org/springframework/restdocs/templates/asciidoctor/`). Например, custom snippet для бизнес-логики, deprecation notices.
- **REST Docs + WebFlux**: `WebTestClient` обернуть через `WebTestClientRestDocumentation.documentationConfiguration(webTestClient)`. Аналогично MockMvc.
- **REST Docs + REST Assured**: `RestAssured` (для e2e через HTTP) — `RestAssuredRestDocumentation` (через `RestDocumentationFilter`).
- **REST Docs + Spring Security**: тестируя secured endpoints, использовать `@WithMockUser` + MockMvc security setup. Токены в snippets можно маскировать через preprocessor.
- **Pros REST Docs**: always up-to-date (test-driven), accurate, fine-grained control, Asciidoctor output (HTML/PDF/EPUB).
- **Cons REST Docs**: требует писать тесты (more work), output is Asciidoctor (not OpenAPI spec), no Swagger UI out-of-the-box (можно сконвертировать через `restdocs-openapi` plugin).
- **OpenAPI 3** — индустриальный стандарт описания REST API (преемник Swagger 2 / OpenAPI 2). JSON/YAML спецификация: paths, components (schemas, security schemes, parameters), responses, examples.
- **springdoc-openapi** (`org.springdoc:springdoc-openapi-starter-webmvc-ui`) — наиболее популярная библиотека для Spring Boot 3. Автоматически генерирует OpenAPI 3 spec из:
  - `@RequestMapping`/`@GetMapping`/etc. на контроллерах.
  - `@RequestBody`/`@ResponseBody` DTO (через Jackson reflection).
  - `@Parameter`/`@Operation`/`@ApiResponse` аннотации (OpenAPI annotations, `io.swagger.v3.oas.annotations.*`).
  - `@Schema` на DTO классах и полях.
- **springdoc-openapi endpoints**:
  - `/v3/api-docs` — JSON OpenAPI spec.
  - `/v3/api-docs.yaml` — YAML.
  - `/swagger-ui.html` (или `/swagger-ui/index.html`) — Swagger UI web interface.
- **springdoc configuration** (`application.yml`):
  ```yaml
  springdoc:
    api-docs:
      path: /v3/api-docs
    swagger-ui:
      path: /swagger-ui.html
      operationsSorter: alpha
      tagsSorter: alpha
    packagesToScan: com.acme.api
    pathsToMatch: /api/**
  ```
- **`@OpenAPIDefinition`** (на main class) — top-level metadata: `info` (title, version, description), `servers`, `security`, `externalDocs`.
- **`@Tag`** (на controller) — group of endpoints (`@Tag(name = "Orders", description = "Order management API")`).
- **`@Operation`** (на методе) — описание endpoint: `summary`, `description`, `operationId`, `responses`, `parameters`, `requestBody`, `security`.
- **`@ApiResponse`** — описание HTTP response: `responseCode`, `description`, `content` (`@Content(schema = @Schema(implementation = OrderDto.class))`).
- **`@Parameter`** — описание parameter (path, query, header): `name`, `in`, `required`, `description`, `example`.
- **`@Schema`** (на DTO) — описание schema: `description`, `example`, `required`, `allowableValues`, `minimum`/`maximum`, `pattern`.
- **`@RequestBody` (OpenAPI)** — не путать с Spring's `@RequestBody`. OpenAPI-аннотация: `description`, `required`, `content`.
- **Security schemes** в OpenAPI: `@SecurityScheme` (на main class или `@SecurityScheme` в `OpenAPIService`):
  ```java
  @SecurityScheme(name = "bearerAuth", scheme = "bearer", type = SecuritySchemeType.HTTP, bearerFormat = "JWT")
  ```
  На endpoint: `@SecurityRequirement(name = "bearerAuth")`.
- **`OpenApiResource`** (внутренний bean) — генерирует spec on-demand по HTTP запросу. Можно кастомизировать через `OpenApiCustomizer` bean.
- **Spring Boot 3.x migration**: для Boot 3 использовать `springdoc-openapi-starter-webmvc-ui` (новый `springdoc-openapi-starter-*` naming для Boot 3, версия 2.x of springdoc). Старый `springdoc-openapi-ui` — только Boot 2.x.
- **Versions**:
  - springdoc-openapi 1.x — Spring Boot 2.x, swagger-annotations 1.x (Swagger 2).
  - springdoc-openapi 2.x — Spring Boot 3.x, swagger-annotations 2.x (OpenAPI 3). `springdoc-openapi-starter-webmvc-ui` / `springdoc-openapi-starter-webflux-ui`.
- **Contract-first approach**: OpenAPI spec пишется вручную (или в Stoplight/Swagger Editor), генерируется Java-код (interfaces + DTOs) через `openapi-generator-maven-plugin`. Spring `@RestController` implements generated interface. Преимущества: spec = single source of truth, language-agnostic, можно генерировать clients для других языков.
- **`openapi-generator-maven-plugin`** — генерирует server stubs (`spring`), clients (`java`, `resttemplate`, `webclient`, `jersey2`, etc.), documentation, tests. Config:
  ```xml
  <generatorName>spring</generatorName>
  <library>spring-boot</library>
  <configOptions>
      <interfaceOnly>true</interfaceOnly>
      <useSpringBoot3>true</useSpringBoot3>
      <useTags>true</useTags>
  </configOptions>
  ```
- **`@Generated` (javax.annotation.processing.Generated / jakarta.annotation.Generated)** — аннотация на сгенерированных классах, чтобы IDE/SonarQube игнорировали их в coverage checks.
- **REST Docs vs springdoc-openapi**:
  - REST Docs: test-driven, Asciidoctor output, no Swagger UI natively, perfect accuracy.
  - springdoc: annotation-driven, OpenAPI JSON, Swagger UI built-in, easier to start, less accurate (annotations can drift).
  - Hybrid: `restdocs-api-spec` (ePirat) — REST Docs + generates OpenAPI spec → both worlds. Less mature.
- **REST Docs с OpenAPI**: `restdocs-spec-maven-plugin` или `spring-restdocs-openapi` (community) — конвертация snippets в OpenAPI. Less mainstream.
- **Springfox** (deprecated) — старая библиотека для Swagger 2 в Spring. Не поддерживает Spring Boot 3, OpenAPI 3. **Не использовать**. Мигрировать на springdoc-openapi.
- **Swagger annotations 2.x (`io.swagger.v3.oas.annotations.*`)** — стандартные OpenAPI annotations, не Spring-специфичные. Используются в springdoc-openapi и Quarkus/Micronaut.
- **`@Hidden`** (OpenAPI) — скрыть endpoint/DTO из documentation.
- **`GroupedOpenApi`** (springdoc) — несколько групп endpoints с разными spec'ами: `GroupedOpenApi.builder().group("orders").pathsToMatch("/api/orders/**").build()`. Полезно для крупных API.
- **Spring REST Docs + ApiMetaData**: можно добавить metadata в generated Asciidoctor через `Snippets.apiDocumentation(...)`.
- **`MockMvcRestDocumentationConfigurer`** — конфигурирует MockMvc: `uris().withScheme("https").withHost("api.acme.com").withPort(443)`. Влияет на URLs в snippets.
- **`RestDocumentationContextProvider`** — управление контекстом документации (output directory, etc.). В тестах обычно инжектится автоматически через `@AutoConfigureRestDocs`.
- **`@AutoConfigureRestDocs`** (Spring Boot) — авто-конфигурация REST Docs для MockMvc/WebTestClient/REST Assured. Параметры: `outputDir`, `uriScheme`, `uriHost`, `uriPort`.
- **Coverage metric for REST Docs**: можно посчитать % endpoints, покрытых REST Docs tests. CI-fail если ниже порога (например, 100% для public API).
- **Spring REST Docs c Asciidoctor**: `asciidoctor-maven-plugin` собирает `index.adoc` → `index.html`. Asciidoctor supports `include`, `ifdef`, `ifeval`, custom attributes. Полезно для стилизации (theme, header/footer).
- **`spring-restdocs-asciidoctor`** — добавляет Asciidoctor-extensions для работы со snippets (auto-resolve `{snippets}` path).
- **OpenAPI + Spring Security**: указать security schemes в spec, чтобы Swagger UI показывал "Authorize" кнопку. Для OAuth2: `@SecurityScheme(type = SecuritySchemeType.OAUTH2, flows = @OAuthFlows(authorizationCode = @OAuthFlow(authorizationUrl = "...", tokenUrl = "...", scopes = {@OAuthScope(name = "read", description = "...")})))`.
- **OpenAPI + Spring Security JWT**: `@SecurityScheme(type = SecuritySchemeType.HTTP, scheme = "bearer", bearerFormat = "JWT")`.
- **`OpenAPIService` / `SpringDocConfig`** — programmatic customization. Можно `@Bean OpenApiCustomizer` для модификации spec перед отдачей (например, добавить global headers, schemas из external source).
- **OpenAPI validation**: `swagger-request-validator` (Atlassian) — валидирует входящие HTTP requests против OpenAPI spec. Можно использовать в tests или как servlet filter.
- **Mock server из OpenAPI**: `wiremock-swagger` / `prism` — генерирует mock-server из spec. Полезно для consumer-driven development.
- **OpenAPI + reactive (WebFlux)**: springdoc-openapi-starter-webflux-ui — same API, but uses WebFlux routes.

### Материалы

- **docs.spring.io — Spring REST Docs Reference** — `https://docs.spring.io/spring-restdocs/docs/current/reference/htmlsingle/`
- **spring.io — Spring REST Docs project** — `https://spring.io/projects/spring-restdocs`
- **GitHub — spring-projects/spring-restdocs** — `https://github.com/spring-projects/spring-restdocs`
- **springdoc.org — springdoc-openapi documentation** — `https://springdoc.org/`
- **GitHub — springdoc/springdoc-openapi** — `https://github.com/springdoc/springdoc-openapi`
- **Baeldung — Documenting Spring REST API with OpenAPI 3** — `https://www.baeldung.com/spring-rest-openapi-documentation`
- **Baeldung — Introduction to Spring REST Docs** — `https://www.baeldung.com/spring-rest-docs`
- **swagger.io — OpenAPI Specification** — `https://swagger.io/specification/`
- **OpenAPI Generator** — `https://openapi-generator.tech/`
- **OpenAPI Generator Maven plugin docs** — `https://openapi-generator.tech/docs/plugins/`
- **Spring blog — REST Docs, OpenAPI и restdocs-api-spec** — `https://spring.io/blog/2023/08/29/rest-docs-and-openapi-combined`
- **Atlassian swagger-request-validator** — `https://bitbucket.org/atlassian/swagger-request-validator/`
- **Reflectoring — Documenting Spring Boot APIs** — `https://reflectoring.io/spring-boot-openapi/`
- **Stoplight Studio** (OpenAPI editor) — `https://stoplight.io/studio`
- **Baeldung — Springfox to springdoc migration** — `https://www.baeldung.com/springfox-vs-springdoc`
- **Книга — "Spring REST Docs and Asciidoctor" (community tutorials)** — Baeldung и spring.io
- **Asciidoctor documentation** — `https://docs.asciidoctor.org/asciidoctor/latest/`
- **Swagger UI** — `https://swagger.io/tools/swagger-ui/`

### Практика / Кейсы

1. **REST Docs full setup**: 
   - `@WebMvcTest(OrderController.class)` + `@AutoConfigureRestDocs`.
   - Для каждого endpoint (`GET /orders/{id}`, `POST /orders`, `GET /orders`) — тест с `.andDo(document("orders/get-by-id", pathParameters(parameterWithName("id").description("Order ID")), responseFields(fieldWithPath("id").type(NUMBER).description("Order ID"), fieldWithPath("customer").type(OBJECT).description("Customer info"), fieldWithPath("items[]").type(ARRAY).description("Order items"), fieldWithPath("status").type(STRING).description("Order status"))))`.
   - В `src/main/asciidoc/api.adoc` включить snippets: `= Order API\n\n== Get Order by ID\n\ninclude::{snippets}/orders/get-by-id/http-request.adoc[]\ninclude::{snippets}/orders/get-by-id/http-response.adoc[]\n...`.
   - `asciidoctor-maven-plugin` в `pom.xml` → HTML output в `target/generated-docs/api.html`.
   - CI: publish HTML в GitHub Pages.
   **Цель:** живая documentation из тестов.

2. **springdoc-openapi full setup**:
   - `springdoc-openapi-starter-webmvc-ui:2.6.0`.
   - `@OpenAPIDefinition(info = @Info(title = "Order API", version = "v1", description = "..."))` на main class.
   - `@SecurityScheme(name = "bearerAuth", type = HTTP, scheme = "bearer", bearerFormat = "JWT")`.
   - На controller: `@Tag(name = "Orders", description = "...")`.
   - На endpoint: `@Operation(summary = "Get order by ID", responses = { @ApiResponse(responseCode = "200", description = "Order found", content = @Content(schema = @Schema(implementation = OrderDto.class))), @ApiResponse(responseCode = "404", description = "Not found", content = @Content(schema = @Schema(implementation = ProblemDetail.class))) })`, `@SecurityRequirement(name = "bearerAuth")`.
   - На DTO: `@Schema(description = "Order DTO", example = "{\"id\":1,\"status\":\"NEW\"}")`, на fields — `@Schema(description = "...", example = "...", required = true)`.
   - Запустить приложение, открыть `http://localhost:8080/swagger-ui.html` → интерактивная documentation.
   **Цель:** OpenAPI с Swagger UI.

3. **Contract-first development**: 
   - Написать `openapi.yaml` в `src/main/resources/api/orders.yaml` через Stoplight Studio.
   - `openapi-generator-maven-plugin` генерирует `OrdersApi` interface и `OrderDto`, `CustomerDto` в `target/generated-sources/openapi/src/main/java`.
   - `@RestController class OrdersController implements OrdersApi { ... }` — controller implements generated interface.
   - DTOs (`OrderDto`) — used in `@RestController` signature.
   - springdoc-openapi reading generated OpenAPI? No — server-first uses springdoc. Contract-first uses static `openapi.yaml` exposed at `/v3/api-docs` (или через `springdoc.swagger-ui.url` pointing to static file).
   - Pros: spec = single source of truth, clients generated for any language.
   **Цель:** spec-first.

4. **REST Docs + MockMvc для validation errors**:
   - Тест `POST /orders` с invalid body → 400 + `ProblemDetail` (RFC 7807).
   - snippet `response-fields` с описанием `errors[]` array.
   - preprocessResponse для masking `instance` field (test-specific).
   - Asciidoctor: includes snippet в секцию `Validation Errors`.
   **Цель:** document error responses.

5. **springdoc + OAuth2 security scheme**:
   - `@SecurityScheme(type = OAUTH2, flows = @OAuthFlows(authorizationCode = @OAuthFlow(authorizationUrl = "${oauth.auth-url}", tokenUrl = "${oauth.token-url}", scopes = {@OAuthScope(name = "orders:read", description = "Read orders"), @OAuthScope(name = "orders:write", description = "Write orders")})))`.
   - На endpoints: `@SecurityRequirement(name = "oauth2", scopes = {"orders:read"})`.
   - Swagger UI: «Authorize» button → OAuth2 flow → token stored → subsequent requests authorized.
   **Цель:** interactive OAuth2 in Swagger UI.

6. **`GroupedOpenApi` для large API**: 10+ resource types → разбить на группы:
   ```java
   @Bean GroupedOpenApi ordersApi() { return GroupedOpenApi.builder().group("orders").pathsToMatch("/api/orders/**").build(); }
   @Bean GroupedOpenApi customersApi() { return GroupedOpenApi.builder().group("customers").pathsToMatch("/api/customers/**").build(); }
   ```
   Swagger UI показывает dropdown с группами. `/v3/api-docs/orders` — отдельный spec. **Цель:** scalable documentation.

7. **REST Docs + restdocs-api-spec (hybrid)**:
   - `io.epirator:spring-restdocs-api-spec` — генерирует OpenAPI spec из REST Docs snippets.
   - Snippet'ы `document("orders/get-by-id", restDocRequestFields, restDocResponseFields, restDocPathParameters, OpenApiResourceDocumentation.resource(...))`.
   - Output: `target/generated-snippets/openapi.json` — OpenAPI 3 spec.
   - Swagger UI: настроить на чтение этого spec.
   - Pros: test-driven + Swagger UI.
   **Цель:** best of both worlds.

8. **OpenAPI validator (Atlassian) в tests**:
   - `swagger-request-validator-spring-webmvc` — валидирует запросы в MockMvc tests против OpenAPI spec.
   - `swagger-request-validator-spring-webclient` — для WebClient.
   - Тесты: assert, что запрос соответствует spec → fail если не соответствует.
   - Можно как servlet filter в production (отвергает malformed requests).
   **Цель:** runtime validation of API contract.

9. **API versioning + documentation**:
   - URL-versioning (`/api/v1/orders`, `/api/v2/orders`) → `GroupedOpenApi` для каждой версии.
   - Header-versioning (`Accept: application/vnd.acme.v1+json`) → custom `OpenApiCustomizer` для filtering.
   - Springdoc + versioning: `@Operation(deprecated = true)` для deprecated endpoints.
   **Цель:** document multiple API versions.

10. **Auto-generated clients from OpenAPI**:
    - Server publishes OpenAPI spec.
    - Client project: `openapi-generator-maven-plugin` generates `OrdersApi` client (`spring-boot` library или `resttemplate`/`webclient`).
    - CI: server publishes spec to Artifactory, client CI downloads and regenerates on spec change.
    - Breaking change detection through `oasdiff` (open-source) in CI.
    **Цель:** SDK generation for clients.

11. **API documentation metrics**: считать % endpoints покрытых REST Docs тестами. CI script:
    - Parse all `@RequestMapping`/`@GetMapping`/etc. в source code → expected endpoints.
    - Parse generated snippets directories → documented endpoints.
    - `assertThat(documentedEndpoints).isEqualTo(expectedEndpoints)`.
    - Fail build if coverage < 100%.
    **Цель:** enforce documentation discipline.

12. **Migrate Springfox → springdoc-openapi**: проект на Spring Boot 2 + Springfox 3.0.0 → миграция на Boot 3 + springdoc 2.x:
    - Убрать `io.springfox:springfox-boot-starter`.
    - Добавить `org.springdoc:springdoc-openapi-starter-webmvc-ui:2.6.0`.
    - `@Api` → `@Tag`, `@ApiOperation` → `@Operation`, `@ApiParam` → `@Parameter`, `@ApiModelProperty` → `@Schema`.
    - `@ApiModel` → `@Schema`. `@ApiResponses` → `@ApiResponse` (one per response).
    - `Docket` bean → `OpenAPI` bean.
    - OpenRewrite recipe: `org.openrewrite.java.spring.boot3.MigrateSpringfoxToSpringdoc`.
    **Цель:** modernize documentation.

---


## 20. Spring версии и миграции

### Теория

- **Spring Framework versions timeline**:
  - **Spring 1.0** (март 2003) — первый релиз, XML-конфигурация, AOP Alliance.
  - **Spring 2.0** (октябрь 2006) — namespaces в XML, Java 5 (generics), `@RequestMapping`, `@Transactional`, JPA.
  - **Spring 3.0** (декабрь 2009) — Java 5+, `@Configuration`, `@Component`, `@Autowired`, `@PathVariable`, REST support, SpEL.
  - **Spring 4.0** (декабрь 2013) — Java 8, WebSocket, `@RestController`, `@Conditional`.
  - **Spring 4.3** (июнь 2016) — `@GetMapping`/`@PostMapping`/etc., `@RequestScope`/`@SessionScope`/`@ApplicationScope` composed annotations.
  - **Spring 5.0** (сентябрь 2017) — Java 8+ baseline, reactive stack (WebFlux, Reactor), JUnit 5, Kotlin support.
  - **Spring 5.3** (октябрь 2020) — `@Configuration(proxyBeanMethods = false)` lite mode, `@ControllerAdvice` improvements.
  - **Spring 6.0** (ноябрь 2022) — **Java 17+ baseline**, **Jakarta EE 9+ (`jakarta.*`)**, AOT processing, native image, `HTTP Interface clients` (`@HttpExchange`), `ProblemDetail` (RFC 7807), Micrometer `@Observed`, removal of deprecated APIs.
  - **Spring 6.1** (ноябрь 2023) — `RestClient` (modern sync HTTP client), `HttpServiceProxyFactory` improvements, JCache 1.1, partial Bean metadata AOT, improved observation integration.
  - **Spring 6.2** (ноябрь 2024) — `BeanOverride` infrastructure (для `@MockitoBean`), improved AOT, virtual threads integration (better), `@MockitoBean`/`@MockitoSpyBean` first-class.
  - **Spring 7.0** (планируется ноябрь 2025) — Java 17+ (вероятно 21+ baseline), further native image, HTTP/3 client.
- **Spring Boot versions timeline**:
  - **Spring Boot 1.0** (апрель 2014) — auto-configuration, starters, embedded Tomcat, actuator. Game-changer.
  - **Spring Boot 1.5** (январь 2017) — логирование, Actuator endpoint'ы, `@ConfigurationProperties` improvements.
  - **Spring Boot 2.0** (март 2018) — Spring 5 baseline, reactive web, Java 8+, Micrometer (вместо dropwizard-metrics), `@SpringBootApplication` improvements.
  - **Spring Boot 2.1** (октябрь 2018) — Java 11, multi-`DataSource` auto-config, `spring-boot-jarmode-layertools`.
  - **Spring Boot 2.3** (май 2020) — Graceful shutdown, Docker layers, Liveness/Readiness probes (Kubernetes).
  - **Spring Boot 2.4** (октябрь 2020) — `ConfigData` API (новый `application.yml` loading), `spring.config.import`, JUnit 5 default.
  - **Spring Boot 2.5** (май 2021) — `DataInitializer` для SQL, improved Docker support.
  - **Spring Boot 2.6** (ноябрь 2021) — default `spring.main.allow-circular-references=false`, PathPatternParser default в MVC.
  - **Spring Boot 2.7** (май 2022) — `@AutoConfiguration`, deprecation of `WebSecurityConfigurerAdapter`, Spring 5.3, `@SpringBootApplication` improvements, OAuth2 client fixes.
  - **Spring Boot 3.0** (ноябрь 2022) — **Java 17+ baseline, Jakarta EE 9+ baseline (`jakarta.*`)**, Spring 6, native image (GraalVM) first-class, `@Observability`, `@SpringBootConfiguration` loader changes, removal of `WebSecurityConfigurerAdapter`, `@Enable*` deprecation в пользу auto-configuration.
  - **Spring Boot 3.1** (май 2023) — Testcontainers `@ServiceConnection`, Docker Compose support (`spring.docker.compose.enabled`), `ConnectionDetails` abstraction, configuration property binding improvements.
  - **Spring Boot 3.2** (ноябрь 2023) — Virtual threads (`spring.threads.virtual.enabled=true`), `RestClient`, JdbcClient, SSL bundle reloading, `@Fallback` for Feign, improved native image hints.
  - **Spring Boot 3.3** (май 2024) — `@Fallback`, improved Docker Compose, CDS (Class Data Sharing) for faster startup, multilingual GH actions, structured logging (JSON) via `structure-log` libraries, better PG/MySQL drivers.
  - **Spring Boot 3.4** (ноябрь 2024) — `@MockitoBean`/`@MockitoSpyBean` (deprecate `@MockBean`/`@SpyBean`), `@ConditionalOnThreading` (VIRTUAL/PLATFORM), `TaskExecutor` Virtual threads improvements, Apache HttpClient5 as default (`httpexchanges` → `httpexchanges-v5`), bean overriding registration via `BeanOverride`, HTTP Interface clients improvements, structured logging built-in (`<pattern>` for JSON), `@EnableWebSecurity` deprecation favors `SecurityFilterChain` only, more native image hints.
  - **Spring Boot 3.5** (май 2025) — улучшения HTTP Interface, structured logging stable, bean definition merging.
  - **Spring Boot 4.0** (планируется 2025/2026) — Java 17+ baseline (вероятно 21+), Spring 7, removal deprecated APIs, `@MockBean`/`@SpyBean` removed.
- **Spring 5 → 6 migration** (также Boot 2.x → 3.x):
  - **Java 17+**: минимальная версия. Java 21 LTS recommended.
  - **Jakarta EE 9+ migration**: `javax.*` → `jakarta.*`. Затронуты: `javax.servlet` → `jakarta.servlet`, `javax.persistence` → `jakarta.persistence`, `javax.validation` → `jakarta.validation`, `javax.annotation` → `jakarta.annotation`, `javax.transaction` → `jakarta.transaction`, `javax.mail` → `jakarta.mail`, `javax.ws.rs` → `jakarta.ws.rs`. Использовать OpenRewrite recipe `org.openrewrite.java.migrate.JavaxToJakarta`.
  - **Removed APIs**: 
    - `WebSecurityConfigurerAdapter` (Spring Security) — удалён в Security 6. Использовать `SecurityFilterChain` `@Bean`.
    - `@Enable*`-аннотации (часть) — deprecation, auto-configuration preferred.
    - `antMatchers`/`mvcMatchers` → `requestMatchers` в Security.
    - `org.springframework.boot.web.servlet.error.ErrorController`-based custom error handling changes.
    - `spring.factories` для auto-configuration → `META-INF/spring/org.springframework.boot.autoconfigure.AutoConfiguration.imports` (новый механизм).
  - **Third-party dependencies upgrade**:
    - Hibernate 5 → 6 (HQL changes, type system, `Type` → `@Convert`, generated SQL changes — проверить все queries).
    - Jackson 2.13 → 2.14+ (мелкие behavioral changes).
    - Micrometer 1.9 → 1.10+ (new metrics).
    - Tomcat 9 → 10 (jakarta.servlet).
    - Jetty 9 → 11.
    - Netty 4.1.x — compatible, но API updates.
  - **Native image support**: AOT processing (Spring 6) — `spring-boot-maven-plugin process-aot` goal. Native buildpacks (`pack build`), Native Build Tools (`native-maven-plugin`).
- **Native image migration challenges**:
  - Reflection: Hibernate/Jackson/Spring AOP используют reflection. Spring AOT hints покрывают 99%, но custom reflection нужно указывать через `RuntimeHints` (`@RegisterReflectionForBinding`, `@ImportRuntimeHints`).
  - Dynamic proxies: JDK proxies — AOT hints. CGLIB — AOT-friendly.
  - Resources: `src/main/resources/META-INF/resources/...` — `resource-config.json` через `native-maven-plugin`.
  - JNI, `Unsafe`, `sun.misc.*` — несовместимо с native image.
  - Build time: native image build длится 1-10 минут.
- **Boot 2.7 → 3.0 migration checklist**:
  1. Upgrade to Boot 2.7.latest (intermediate step).
  2. Run `spring-boot-properties-migrator` — auto-migrate renamed properties.
  3. Fix all deprecations в Boot 2.7 (visible в compiler warnings).
  4. Migrate `WebSecurityConfigurerAdapter` → `SecurityFilterChain` (use Spring Security 5.7+ features).
  5. Replace `antMatchers` → `requestMatchers` (Spring Security 5.8+).
  6. Migrate `spring.factories` auto-configuration → `AutoConfiguration.imports` (use `spring-boot-autoconfigure-processor`).
  7. Upgrade Java 11 → 17.
  8. Run `org.openrewrite.java.migrate.JavaxToJakarta` recipe.
  9. Upgrade third-party deps (Hibernate 6, etc.).
  10. Switch to Boot 3.0. Run all tests. Fix failures.
- **Spring Cloud versions** (aligned с Boot через release trains):
  - **2020.0.x** (Ilford) — Boot 2.4, 2.5, 2.6, 2.7. Spring 5.3.
  - **2021.0.x** (Jubilee) — Boot 2.6, 2.7. Spring 5.3.
  - **2022.0.x** (Kilburn) — Boot 3.0, 3.1. Spring 6.
  - **2023.0.x** (Leyton) — Boot 3.2, 3.3. Spring 6.1.
  - **2024.0.x** (Moorgate) — Boot 3.4, 3.5. Spring 6.2.
  - **2025.0.x** (Northfields) — Boot 3.5+ planned.
- **Naming convention release trains**: London Tube stations, alphabetical. Ilford → Jubilee → Kilburn → Leyton → Moorgate → Northfields.
- **Spring Cloud to Boot compatibility matrix** (важно!): каждая версия release train поддерживает конкретные Boot версии. Несовместимость → странные runtime errors. Всегда проверять `spring-cloud-dependencies` BOM версию.
- **`spring-boot-properties-migrator`** — dependency, которая логирует renamed/removed properties и (где возможно) auto-migrates. **Удалить после миграции** (влияет на startup time).
- **`spring-boot-configuration-processor`** — генерирует `additional-spring-configuration-metadata.json` из `@ConfigurationProperties` классов. IDE автокомплит для properties.
- **OpenRewrite recipes** для миграций:
  - `org.openrewrite.java.spring.boot2.UpgradeSpringBoot_2_7` — upgrade to Boot 2.7.
  - `org.openrewrite.java.spring.boot3.UpgradeSpringBoot_3_0` / `_3_1` / `_3_2` / `_3_3` / `_3_4` — Boot 3.x upgrades.
  - `org.openrewrite.java.spring.boot4.ReplaceMockBeanAndSpyBean` — `@MockBean` → `@MockitoBean`.
  - `org.openrewrite.java.migrate.JavaxToJakarta` — `javax.*` → `jakarta.*`.
  - `org.openrewrite.java.spring.framework.UpgradeSpringFramework_6` — Spring 6.
  - `org.openrewrite.java.spring.security5.UpgradeSpringSecurity_5_8` / `6_2` — Security upgrades.
  - `org.openrewrite.java.spring.data.UpgradeSpringData_3` — Data 3 (JPA, MongoDB, etc.).
- **Spring Security migration 5.x → 6.x**:
  - `WebSecurityConfigurerAdapter` removed → `SecurityFilterChain` `@Bean`.
  - `antMatchers`/`mvcMatchers` → `requestMatchers`.
  - `@EnableWebSecurity` deprecation в пользу auto-config (но всё ещё работает).
  - `@EnableGlobalMethodSecurity` → `@EnableMethodSecurity` (prePostEnabled=true по умолчанию).
  - `OAuth2LoginConfigurer` — minor API changes.
  - `ReactiveOAuth2UserService` — minor changes.
  - `SecurityContextHolder` strategy по умолчанию — `MODE_THREADLOCAL` (но для reactive — `MODE_INHERITABLETHREADLOCAL` recommended).
- **Spring Boot 3.x migration для Configuration**:
  - `spring.factories` (auto-config) → `META-INF/spring/org.springframework.boot.autoconfigure.AutoConfiguration.imports`.
  - `spring-configuration-metadata.json` для `@ConfigurationProperties` — generated automatically через `spring-boot-configuration-processor`.
- **Bean overriding disabled by default** (Boot 2.1+): `spring.main.allow-bean-definition-overriding=false`. Если есть переопределение — включить явно.
- **Circular references disabled by default** (Boot 2.6+): `spring.main.allow-circular-references=false`. Если есть циклические зависимости — refactor или включить.
- **PathPatternParser** (Boot 2.6+, default в 3.0): замена `AntPathMatcher` для MVC routing. Быстрее, но не поддерживает `?` в path (только `*` и `**`). Используется в WebFlux всегда, в MVC — default с 3.0.
- **Properties migration examples**:
  - `spring.jpa.generate-ddl` → `spring.jpa.hibernate.ddl-auto` (warn).
  - `server.servlet.context-path` → `server.servlet.context-path` (unchanged, but `server.context-path` removed).
  - `spring.datasource.driver-class-name` — auto-detected from URL (не нужно явно указывать для H2/Postgres/MySQL).
  - `management.endpoints.web.exposure.include` → unchanged.
- **Spring Boot 3.2 features**:
  - **Virtual threads**: `spring.threads.virtual.enabled=true` — Tomcat/Jetty/Undertow use virtual threads for request handling. Async executors, `@Async`, `TaskScheduler` — switch to virtual threads.
  - **`RestClient`**: fluent synchronous HTTP client (modern `RestTemplate`).
  - **`JdbcClient`**: fluent JDBC operations.
  - **SSL bundle**: configurable SSL via `spring.ssl.bundle.*`, supports reloading.
  - **HTTP Interface clients improvements**: `@HttpExchange`, `HttpServiceProxyFactory`.
- **Spring Boot 3.3 features**:
  - **CDS (Class Data Sharing)**: `spring-boot-maven-plugin:repackage -Dspring-boot.cds.enabled=true` — faster startup (5-20% improvement). CDS archive built during build, used at runtime.
  - **Structured logging**: `logging.structured.format.file=json` (Logback JSON encoder built-in).
  - **Improved Docker Compose**: lifecycle management, `spring.docker.compose.enabled=true`.
  - **Better native image hints**: more components support native out-of-the-box.
- **Spring Boot 3.4 features**:
  - **`@MockitoBean`/`@MockitoSpyBean`** (Boot 3.4 / Spring 6.2) — замена `@MockBean`/`@SpyBean` (deprecated).
  - **`@ConditionalOnThreading`** — conditional bean на основе `spring.threads.virtual.enabled`.
  - **Structured logging stable**: built-in JSON support for Logback, no extra dependencies.
  - **Apache HttpClient5** as default (`httpexchanges` → uses Apache HttpClient5).
  - **BeanOverride infrastructure**: extensible bean overriding mechanism (used by `@MockitoBean`).
  - **HTTP Interface clients**: improved proxy creation.
  - **More native image hints**: less manual `RuntimeHints` needed.
- **Spring Framework 6.2 features**:
  - **`BeanOverride`** infrastructure — extensible, used by `@MockitoBean`/`@MockitoSpyBean`.
  - **Improved AOT processing**: faster, less memory.
  - **Better virtual threads integration**: detection + auto-config.
  - **`@MockitoBean`/`@MockitoSpyBean`** first-class in Spring Framework Test.
  - **Improved `RestClient`**: new features.
  - **`HttpClient` improvements**.
- **Spring Boot 3.5 features** (expected):
  - Bean definition merging.
  - HTTP Interface clients improvements.
  - Structured logging enhancements.
  - More `@ConditionalOn*` annotations.
- **Spring Framework 7.0 / Boot 4.0 expectations**:
  - Java 21+ baseline (very likely).
  - Removal of deprecated APIs (e.g., `@MockBean`/`@SpyBean` finally removed).
  - HTTP/3 client support.
  - Improved native image: maybe ahead-of-time compilation becomes default.
  - More reactive improvements.
  - New major versions of Spring Data, Security, etc.
- **LTS strategy**:
  - Spring Framework: minor versions every ~6 months (May/November), LTS designation для selected.
  - Spring Boot: minor every ~6 months, matches Framework.
  - Open source support: latest minor + previous minor (free).
  - Commercial support (Broadcom/Tanzu): extended LTS for older versions (Spring Enterprise).
- **EOL versions**:
  - Spring Boot 2.7 OSS EOL: август 2023 (commercial до 2025+).
  - Spring Boot 3.0 OSS EOL: ноябрь 2023.
  - Spring Boot 3.1 OSS EOL: май 2024.
  - Spring Boot 3.2 OSS EOL: ноябрь 2024.
  - Текущие supported OSS: Boot 3.3.x (до ~май 2025), 3.4.x, 3.5.x (после release).
- **Migration tooling**:
  - **OpenRewrite** (moderne.io): автоматические recipes для всех Spring upgrades. Open-source, можно запускать локально или через SaaS.
  - **Spring Boot Migrator** (Spring project, deprecated в пользу OpenRewrite): GUI для миграций.
  - **`spring-boot-properties-migrator`**: properties migration.
  - **IDE inspections**: IntelliJ IDEA Spring Boot plugin detects deprecated properties/annotations.
- **Best practices для upgrade**:
  - Upgrade incrementally (2.7 → 3.0 → 3.1 → 3.2 → ...), не 2.7 → 3.4 в один шаг.
  - Каждый шаг — отдельный PR, full CI green.
  - Использовать OpenRewrite recipes для механических изменений.
  - Прогнать все тесты (unit + integration) после каждого шага.
  - Прогнать на staging перед production.
  - Иметь rollback plan (feature flags, blue-green deploy).
- **Spring Boot compatibility table (examples)**:
  | Boot version | Spring Framework | Java min | Jakarta EE |
  |---|---|---|---|
  | 2.7.x | 5.3.x | 8 (11 rec.) | javax.* |
  | 3.0.x | 6.0.x | 17 | jakarta.* |
  | 3.1.x | 6.0.x | 17 | jakarta.* |
  | 3.2.x | 6.1.x | 17 | jakarta.* |
  | 3.3.x | 6.1.x | 17 | jakarta.* |
  | 3.4.x | 6.2.x | 17 | jakarta.* |
  | 3.5.x | 6.2.x | 17 (21 rec.) | jakarta.* |

### Материалы

- **spring.io/blog — A Java 17 and Jakarta EE 9 baseline for Spring Framework 6** — `https://spring.io/blog/2021/09/02/a-java-17-and-jakarta-ee-9-baseline-for-spring-framework-6`
- **GitHub wiki — Upgrading to Spring Framework 6.x** — `https://github.com/spring-projects/spring-framework/wiki/Upgrading-to-Spring-Framework-6.x`
- **GitHub wiki — Spring Boot 3.0 Migration Guide** — `https://github.com/spring-projects/spring-boot/wiki/Spring-Boot-3.0-Migration-Guide`
- **docs.spring.io — Upgrading Spring Boot** — `https://docs.spring.io/spring-boot/upgrading.html`
- **spring.io/blog — Preparing for Spring Boot 3.0** — `https://spring.io/blog/2022/05/24/preparing-for-spring-boot-3-0`
- **spring.io/blog — Spring Boot 3.4.0 available now** — `https://spring.io/blog/2024/11/21/spring-boot-3-4-0-available-now`
- **GitHub wiki — Spring Boot 3.4 Release Notes** — `https://github.com/spring-projects/spring-boot/wiki/Spring-Boot-3.4-Release-Notes`
- **GitHub wiki — Spring Framework 6.2 Release Notes** — `https://github.com/spring-projects/spring-framework/wiki/Spring-Framework-6.2-Release-Notes`
- **spring.io/blog — Spring Framework 6.2.0 Available Now** — `https://spring.io/blog/2024/11/14/spring-framework-6-2-0-available-now`
- **spring.io/blog — Spring Cloud 2024.0.0 (aka Moorgate) Is Now Available** — `https://spring.io/blog/2024/12/03/spring-cloud-2024-0-0`
- **GitHub wiki — Spring Cloud Release Train Naming Convention** — `https://github.com/spring-cloud/spring-cloud-release/wiki/Release-Train-Naming-Convention`
- **GitHub wiki — Spring Cloud Supported Versions** — `https://github.com/spring-cloud/spring-cloud-release/wiki/Supported-Versions`
- **Baeldung — Spring Boot 3 and Spring Framework 6 What's New** — `https://www.baeldung.com/spring-boot-3-spring-6-new`
- **Baeldung — Migrate from Spring Boot 2 to Spring Boot 3** — `https://www.baeldung.com/spring-boot-3-migration`
- **OpenRewrite recipes — Spring** — `https://docs.openrewrite.org/recipes/java/spring/`
- **OpenRewrite — Migrate Spring Boot 3.x** — `https://docs.openrewrite.org/recipes/java/spring/boot3/`
- **BellSoft blog — How to migrate to Spring Boot 3** — `https://bell-sw.com/blog/migration-to-spring-boot-3`
- **Reflectoring — Spring 6 / Boot 3 migration guide** — `https://reflectoring.io/spring-boot-3-migration/`
- **Книга — "Spring Boot 3 Migration Guide" (community tutorials)** — Baeldung, spring.io/blog
- **Книга — "Cloud Native Spring in Action" (Thomas Vitale)** — Manning, ISBN 978-1633439432 (covers Boot 3, Spring 6, native image, Kubernetes)
- **Книга — "Spring Microservices in Action, Second Edition" (John Carnell, Illary Huaylupo Sánchez)** — Manning, ISBN 978-1617298224 (covers Spring Cloud, Boot 3)

### Практика / Кейсы

1. **Full migration Boot 2.7 → Boot 3.4 end-to-end**:
   - Стартовое приложение: Boot 2.7, Spring 5.3, Java 11, `javax.*`, Spring Security 5.7 with `WebSecurityConfigurerAdapter`, Springfox 3.0.0, Hibernate 5.
   - **Phase 1 (Boot 2.7.latest)**: apply OpenRewrite `UpgradeSpringBoot_2_7`. Fix all deprecation warnings. Use `WebSecurityConfigurerAdapter` → `SecurityFilterChain` (на Security 5.7+, ещё Boot 2.7).
   - **Phase 2 (Java 17)**: upgrade JDK, fix compiler errors (var, switch expressions, sealed classes — опционально).
   - **Phase 3 (`javax.*` → `jakarta.*`)**: apply OpenRewrite `JavaxToJakarta`. Upgrade servlet API, JPA, validation, etc.
   - **Phase 4 (Springfox → springdoc)**: apply `MigrateSpringfoxToSpringdoc`. Replace annotations.
   - **Phase 5 (Hibernate 5 → 6)**: fix HQL queries, type system changes, `@Type` → `@Convert`, custom types. Test all repositories.
   - **Phase 6 (Boot 3.0)**: upgrade. Run `spring-boot-properties-migrator`. Fix all issues. Run all tests.
   - **Phase 7 (Boot 3.1, 3.2, 3.3, 3.4)**: incrementally upgrade. At 3.2 enable virtual threads. At 3.4 replace `@MockBean` → `@MockitoBean` (OpenRewrite recipe).
   - **Phase 8 (Native image)**: AOT processing, build native image, test runtime, fix `RuntimeHints`.
   - At each phase: full CI green, PR merged.
   **Цель:** production-grade migration.

2. **Native image migration**: Boot 3.x app, build native image:
   - Add `spring-boot-starter` (already there).
   - Add `org.graalvm.buildtools:native-maven-plugin` (or Gradle equivalent).
   - `mvn -Pnative native:compile` — builds native executable in `target/`.
   - Test executable: `./target/myapp` — verify startup time (<100ms), memory (<100MB RSS).
   - Issues:
     - Custom Jackson serializers not detected → `@RegisterReflectionForBinding(MyDto.class)`.
     - Hibernate `@Convert` custom converters → `RuntimeHintsRegistrar`.
     - Resources (templates, static files) → `@ImportResource` or `META-INF/native-image/resource-config.json`.
     - Third-party library without native hints → `RuntimeHints` manual registration.
   - Compare startup time / memory / throughput vs JVM mode. Use JMH for throughput.
   **Цель:** understand GraalVM native image trade-offs.

3. **Properties migration analysis**: запустить `spring-boot-properties-migrator` на legacy Boot 2.5 app. Собрать логи: какие properties were renamed, removed, defaulted. Сгенерировать migration report для ops team. Apply OpenRewrite `UpgradeSpringBoot_3_4` recipe — verify properties migrated correctly. Remove `properties-migrator` after migration. **Цель:** systematic properties migration.

4. **Spring Cloud release train upgrade**: Boot 3.0 + Cloud 2022.0.x → Boot 3.4 + Cloud 2024.0.x:
   - Upgrade Boot incrementally (3.0 → 3.1 → 3.2 → 3.3 → 3.4).
   - Upgrade Cloud release train aligned: 2022.0.x → 2023.0.x → 2024.0.x.
   - Breaking changes: Spring Cloud Netflix Eureka client updates, Resilience4j 1.x → 2.x (Spring Cloud Circuit Breaker 3.0+), Spring Cloud Gateway changes (WebFlux-only since 4.0).
   - Test all microservices with integration tests.
   - Blue-green deployment to production.
   **Цель:** coordinated Spring Cloud upgrade.

5. **Virtual threads adoption**: Boot 3.2+ app:
   - Set `spring.threads.virtual.enabled=true`.
   - Profile existing `@Async` executors, `TaskScheduler`, Tomcat threads — verify all switched to virtual.
   - Run load tests (Gatling/JMeter): compare throughput, latency, memory with platform vs virtual threads.
   - Identify blocking calls that benefit most (DB queries, external HTTP calls).
   - Edge cases: `ThreadLocal` performance (virtual threads are cheap, but `ThreadLocal` is per-thread — memory overhead), `synchronized` blocks (pinning issues — switch to `ReentrantLock`).
   - Add JFR (Java Flight Recorder) monitoring for pinning events.
   **Цель:** production-ready virtual threads.

6. **OpenRewrite mass migration across microservices**:
   - 20+ Spring Boot microservices, various versions (2.5-3.2).
   - Set up OpenRewrite CLI with `rewrite.yml` containing all recipes.
   - Apply recipes across all repos via CI pipeline.
   - Each repo: branch created, recipes applied, PR opened, CI verified, PR merged.
   - Track progress in dashboard (e.g., Moderne SaaS or custom).
   - Identify recipes that need manual fixes (OpenRewrite не покрывает всё).
   - Documentation: «cookbook» of common manual fixes.
   **Цель:** large-scale automated migration.

7. **Spring Security 5 → 6 migration**:
   - Replace `WebSecurityConfigurerAdapter` (already deprecated in 5.7, removed in 6).
   - Replace `antMatchers`/`mvcMatchers` → `requestMatchers`.
   - Replace `@EnableGlobalMethodSecurity` → `@EnableMethodSecurity`.
   - Replace `User.withDefaultPasswordEncoder()` (deprecated, only for tests).
   - Review `SecurityContextHolder` strategy for reactive code.
   - Test all secured endpoints — verify behaviour unchanged.
   - Migrate custom `Filter` chains to `SecurityFilterChain` `@Bean`.
   **Цель:** Security 6 compatibility.

8. **Hibernate 5 → 6 migration**:
   - `@Type(type = "...")
` → `@Convert(converter = ...)`.
   - Custom `UserType` → `AttributeConverter`.
   - HQL changes: implicit joins, type inference stricter.
   - Generated SQL: verify (join syntax, parameter binding).
   - `Criteria API` changes (jakarta.persistence).
   - `@GeneratedValue(strategy = AUTO)` — semantics changed (SEQUENCE preferred).
   - Run all repository tests, inspect SQL logs.
   **Цель:** Hibernate 6 compatibility.

9. **`spring.factories` → `AutoConfiguration.imports` migration**:
   - Identify `META-INF/spring.factories` files with `EnableAutoConfiguration` keys.
   - Create `META-INF/spring/org.springframework.boot.autoconfigure.AutoConfiguration.imports` listing auto-configuration classes (one per line, FQN).
   - Remove `EnableAutoConfiguration` section from `spring.factories` (other sections like `EnvironmentPostProcessor` remain).
   - Use `@AutoConfiguration` annotation (Boot 2.7+) on auto-configuration classes instead of `@Configuration`.
   - `@AutoConfigureBefore`/`@AutoConfigureAfter`/`@AutoConfigureOrder` still work.
   - Test that auto-configuration still loads.
   **Цель:** modernize library auto-config.

10. **CDS (Class Data Sharing) startup optimization** (Boot 3.3+):
    - Enable CDS in build: `mvn package -Dspring-boot.cds.enabled=true`.
    - CDS archive created inside JAR.
    - Run app: `java -jar app.jar` — JVM uses CDS archive.
    - Measure startup time with and without CDS (5-20% improvement expected).
    - Compare with native image (faster startup, but longer build).
    - Use CDS for dev/test, native for production short-lived instances.
    **Цель:** startup time optimization.

11. **Structured logging migration** (Boot 3.4+):
    - `logging.structured.format.file=json` — built-in JSON encoder for Logback.
    - Replace custom JSON encoder (Logstash encoder) with built-in.
    - Add ECS (Elastic Common Schema) fields: `logging.structured.format.file=ecs`.
    - Configure log aggregation (Loki/ELK) to parse JSON.
    - Test MDC fields, trace IDs — verify propagated.
    **Цель:** structured logging without third-party deps.

12. **Spring Boot 4.0 preparation**:
    - Track Spring blog for 4.0 announcements.
    - Run deprecation scan (Boot 3.4 deprecated APIs) — these will be removed in 4.0.
    - Plan migration: `@MockBean`/`@SpyBean` → `@MockitoBean`/`@MockitoSpyBean` (already in 3.4 as preview).
    - Java 21 baseline — verify compatibility.
    - Set up CI job that runs against Spring Boot milestones/snapshots for early detection.
    **Цель:** proactive 4.0 readiness.

13. **Compatibility matrix audit for production**: enterprise приложение с большим stack:
    - Build matrix of: Boot version × Spring Framework version × Spring Cloud version × Spring Security version × Spring Data version × Java version × Hibernate version × Tomcat version.
    - Verify all combinations supported per official matrix.
    - Document for ops team.
    - Set up Renovate Bot / Dependabot with `groups` to upgrade compatible versions together.
    **Цель:** avoid runtime version conflicts.

14. **Rollback strategy for failed migration**:
    - Blue-green deploy: deploy new version to green, switch traffic gradually.
    - Database rollback: Flyway `undo` (commercial) or manual `V<n>__rollback.sql`.
    - Feature flags: `spring.feature.use-new-security=true` — toggle new SecurityFilterChain.
    - If migration failed at day 2: rollback to previous Boot version, retain database (compatible).
    - Test rollback in staging first.
    **Цель:** safe migration with rollback.

---

## Сводка раздела

**Spring Ecosystem** — крупнейший раздел roadmap (20 подразделов, ~3000 строк). Покрыты все ключевые темы уровня Senior/Staff Spring-инженера:

- **Core**: IoC/DI, AOP, Configuration (1-3)
- **Boot**: Auto-configuration, starters, embedded servers, native image, AOT, layered JARs, virtual threads, structured logging (4)
- **Web**: Spring MVC, ProblemDetail (RFC 7807), validation, async, SSE (5); WebFlux, Reactor, WebClient, R2DBC (6)
- **Data**: JPA + Specifications + QueryDSL + N+1 + EntityGraph (7); MongoDB/Redis/Elasticsearch/JdbcTemplate (8)
- **Cross-cutting**: Transactions (9), Security OAuth2/JWT (10), Actuator + Micrometer + OTel tracing (11)
- **Cloud**: Service discovery, Config, Circuit Breaker, Gateway, OpenFeign, Stream (12)
- **Specialized**: Batch (13), Cache (14), Messaging JMS/AMQP/Kafka (15), Integration & Camel (16)
- **Quality**: Testing slices + `@MockitoBean` (17), Modulith modular monolith (18), REST Docs + OpenAPI (19)
- **Evolution**: версии и миграции 5→6, 2.x→3.x, jakarta.* (20)

**Топ-источники (15)** для углублённого изучения:
1. `docs.spring.io/spring-framework/reference/` — Spring Framework reference
2. `docs.spring.io/spring-boot/reference/` — Spring Boot reference
3. `docs.spring.io/spring-security/reference/` — Spring Security reference
4. `docs.spring.io/spring-data/jpa/reference/` — Spring Data JPA reference
5. `docs.spring.io/spring-boot/reference/testing/` — Spring Boot Test reference
6. `docs.spring.io/spring-modulith/reference/` — Spring Modulith reference
7. `docs.spring.io/spring-restdocs/docs/current/reference/htmlsingle/` — Spring REST Docs
8. `spring.io/blog/` — Spring blog (release announcements, deep dives)
9. `github.com/spring-projects/` — Spring source code and wiki
10. `springdoc.org/` — springdoc-openapi documentation
11. `www.baeldung.com/spring-boot` — Baeldung Spring tutorials
12. `reflectoring.io/` — Reflectoring Spring deep-dives
13. `projectreactor.io/docs/core/release/reference/` — Project Reactor reference
14. `resilience4j.readme.io/` — Resilience4j docs
15. `docs.openrewrite.org/recipes/java/spring/` — OpenRewrite Spring migration recipes

**Книги**:
- "Spring in Action, Sixth Edition" (Craig Walls) — Manning, ISBN 978-1617297944
- "Pro Spring 6" (Walls, Schaefer, Harrop) — Apress, ISBN 978-1484286401
- "Cloud Native Spring in Action" (Thomas Vitale) — Manning, ISBN 978-1633439432
- "Spring Microservices in Action, Second Edition" (Carnell, Huaylupo Sánchez) — Manning, ISBN 978-1617298224
- "Spring Boot in Action" (Craig Walls) — Manning
- "Spring Security in Action, Second Edition" (Laurențiu Spilcă) — Manning
- "Learning Spring Boot 3.0" (Greg L. Turnquist) — Packt
- "AspectJ in Action, Second Edition" (Ramnivas Laddad) — Manning
- "Camel in Action, Second Edition" (Claus Ibsen, Jonathan Anstey) — Manning
- "Enterprise Integration Patterns" (Gregor Hohpe, Bobby Woolf) — Addison-Wesley

**Версионные рекомендации (на момент составления)**:
- Production-ready: **Spring Boot 3.4.x**, **Spring Framework 6.2.x**, **Java 21 LTS**, **Jakarta EE 10**.
- Adopting: **Spring Modulith 1.3.x**, **Spring Cloud 2024.0.x** (Moorgate).
- Migrating now: Boot 2.7.x → 3.x (jakarta.* + Java 17+).
- Preview/watching: Boot 4.0 / Spring 7 (ожидается в 2025/2026).

---

