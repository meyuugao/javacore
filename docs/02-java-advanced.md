# Раздел 2. Java Advanced

> **Цель раздела:** вывести разработчика с уровня Core на уровень Senior/Staff Java Engineer. Покрываются внутренности JVM, Java Memory Model, Garbage Collection, расширенная конкурентность (java.util.concurrent, virtual threads, locks, atomics, synchronizers, reactive Flow), низкоуровневые механизмы (method handles, invokedynamic, annotation processing, JNI/Panama FFM), модульность (JPMS), криптография (JCA/JCE/JSSE), NIO.2, современные языковые фичи Java 9–21 и инструменты диагностики (jcmd, JFR, async-profiler, MAT, Arthas).
>
> **Связь с другими разделами:** фундамент для [03 Build/Test/Eng](./03-build-test-eng.md) (JIT-тестирование, JMH), [04 Spring](./04-spring-ecosystem.md) (AOP через proxy/ByteBuddy, transaction propagation через ThreadLocal/ScopedValue), [06 Distributed](./06-distributed-msg.md) (backpressure, Flow API), [07 DevOps](./07-devops-observability.md) (JFR → OpenTelemetry, GC tuning).
>
> **Базовые книги раздела:**
> - Brian Goetz и др. — **"Java Concurrency in Practice"** (2006, всё ещё валиден для JMM, locks, executors; не покрывает virtual threads)
> - Scott Oaks — **"Java Performance"** (O'Reilly, 2-е издание 2020, JDK 11)
> - Clarke, Martirosyan, Izzo — **"Optimizing Java"** (O'Reilly, 2018, JVM tuning)
> - **"The Java Virtual Machine Specification, Java SE 21 Edition"** — Tim Lindholm, Frank Yellin et al.
> - Ron Pressler — статьи про virtual threads на openjdk.org
> - Aleksey Shipilëv — shipilev.net (JMM, GC, JMH)

> **Рекомендуемый порядок изучения (зависимости):**
> 1. **JVM Architecture (§1)** + **JMM (§2)** — фундамент, без которого остальное не работает.
> 2. **Threads & lifecycle (§4)** + **synchronized & locks (§5)** — базовая конкурентность.
> 3. **Atomic & CAS (§8)** + **java.util.concurrent (§6)** — структуры данных и low-level primitives.
> 4. **Executors & Thread Pools (§7)** + **Synchronizers (§9)** — high-level координация.
> 5. **Garbage Collection (§3)** — после понимания memory model; обязателен перед tuning.
> 6. **Virtual threads (часть §4)** + **Reactive Flow API (§10)** — современная конкурентность.
> 7. **Reflection (§11)** + **Method Handles (§12)** + **Annotation Processing (§13)** — метапрограммирование.
> 8. **JPMS (§15)** + **Modern Java 9–21 (§18)** — языковые и платформенные новшества.
> 9. **JNI / JNA / Panama FFM (§14)** + **NIO.2 (§17)** + **Security (§16)** — прикладные advanced темы.
> 10. **JVM диагностика (§19)** — на любой стадии, по мере практики; возвращает к предыдущим темам.

---

## 1. JVM Architecture

### Теория

- **JVM** — это спецификация (JSR 924), описывающая абстрактную вычислительную машину; HotSpot — эталонная реализация OpenJDK. Альтернативы: Eclipse OpenJ9 (IBM), GraalVM (Oracle/Graal), Azul Zing/Prime, Android Runtime (ART).
- **Жизненный цикл класса:** loading → linking (verification + preparation + resolution) → initialization → use → unload. Loading — чтение байткода из classloader; verification — проверка корректности (4 прохода); preparation — default значения static; resolution — ленивый resolve constant pool; initialization — `<clinit>`.
- **Runtime Data Areas (спецификация):**
  - **pc register** — счётчик байткода текущего метода (undefined для native);
  - **JVM Stack** — стек фреймов (per-thread); бросает `StackOverflowError`, `OutOfMemoryError`;
  - **Heap** — общая память для всех объектов и массивов; GC-управляемая;
  - **Method Area** — логически часть heap, но реализация HotSpot с Java 8 — **Metaspace** в native memory (раньше PermGen);
  - **Run-Time Constant Pool** — per-class, часть Method Area;
  - **Native Method Stack** — для native методов (JNI).
- **Frame (стек-фрейм):** local variables array (long/double занимают 2 слота), operand stack, dynamic linking (reference to runtime constant pool), return value, exception table reference.
- **Heap layout HotSpot (до G1):** young gen = Eden + 2 Survivor (S0, S1); old gen; метапространство рядом.
- **Object header (HotSpot):** Mark Word (64 bit) + Klass Pointer (32 bit с compressed oops) + поля + padding до 8-байтовой границы. Mark word хранит hash/age/lock bits/monitor pointer.
- **Compressed oops** (`-XX:+UseCompressedOops`): указатели сжимаются до 32 бит, доступно до ~32 GB heap; свыше 32 GB автоматически отключается, что ведёт к росту header.
- **Classloader hierarchy:** bootstrap (нативный, загружает `java.base`) → platform (бывший extension, до Java 8 `ext`) → application (classpath). Параллельная загрузка с Java 7 (JDK-7189531).
- **Parent delegation:** `ClassLoader.loadClass` сначала делегирует родителю, потом ищет сам. Нарушают: Tomcat (WebappClassLoader), OSGi (граф classloaders), JEE серверы.
- **Bytecode:** 200+ однобайтовых опкодов, stack-based; формат `.class` файла — magic `0xCAFEBABE`, version, constant_pool, access_flags, this/super, interfaces, fields, methods, attributes.
- **Constant pool:** индексированный пул литералов — `CONSTANT_Class`, `Methodref`, `Fieldref`, `String`, `Integer`/`Float`/`Long`/`Double`, `NameAndType`, `Utf8`, `MethodHandle`, `MethodType`, `InvokeDynamic`, `Dynamic` (Java 11+, JEP 309).
- **Verification:** Pass 1 — формат class file; Pass 2 — при загрузке (semantic check); Pass 3 — при линковке (bytecode verifier, dataflow); Pass 4 — runtime checks (array bounds, null, cast).
- **Интерпретатор → JIT:** bytecode интерпретируется, но горячие методы компилируются JIT-компилятором.
- **Tiered compilation** (`-XX:+TieredCompilation` по умолчанию с Java 8): уровни 0 (interpreter) → 1 (C1 no profiling) → 2 (C1 light profiling) → 3 (C1 full profiling) → 4 (C2).
- **C1 (client compiler):** быстрый, простые оптимизации (inlining, null-check elimination, classic optimizations).
- **C2 (server compiler):** агрессивные оптимизации — escape analysis (скалярная замена, lock elision), loop unrolling, vectorization (auto-vectorization через SuperWord), graph-coloring register allocation.
- **OSR (On-Stack Replacement):** компиляция метода посередине выполнения (например, long-running loop) и замена frame в горячем цикле.
- **Deoptimization:** если спекуляции C2 не подтвердились (изменилась иерархия классов, profile pollution, unloaded class) — deoptimизация на interpreting, может быть "non-uncommon trap".
- **Safepoints:** моменты, когда JVM может остановить все потоки для GC, deopt, biased lock revocation. В counted loops safepoint добавляется через `-XX:+UseCountedLoopSafepoints` (JDK 10+, по умолчанию с JDK 12 при `+TieredCompilation`).
- **GraalVM:** Graal — JIT-компилятор на Java (замена C2), включается через `-XX:+UseGraalJIT` (GraalVM JDK). **Native Image** — AOT компиляция, closed-world assumption, мгновенный старт, меньше footprint; tradeoff — нет динамической загрузки классов, ограниченный reflection (через config).
- **Truffle** — фреймворк для написания интерпретаторов языков на AST (JavaScript, Ruby, Python, R),Graal компилирует в нативный код.
- **Полезные флаги диагностики:** `-XX:+PrintCompilation`, `-XX:+UnlockDiagnosticVMOptions -XX:+PrintInlining`, `-XX:+PrintFlagsFinal`, `-XX:+TraceClassLoading`, `-XX:+TraceClassUnloading`.
- **JITWatch** — инструмент для визуализации логов компиляции и анализа инлайнинга.

### Материалы

- Книга: **"Java Performance"** Scott Oaks (O'Reilly, 2020) — главы 1–4 про JVM internals и JIT
- Книга: **"Optimizing Java"** Clarke, Martirosyan, Izzo (O'Reilly, 2018) — глава 2 "JVM Architecture"
- Книга: **"The Java Virtual Machine Specification, Java SE 21 Edition"** — https://docs.oracle.com/javase/specs/jvms/se21/html/
- OpenJDK Wiki: HotSpot — https://wiki.openjdk.org/display/HotSpot/Main
- Inside.java — JVM Architecture / JIT articles — https://inside.java/jvm/
- GraalVM Documentation — https://www.graalvm.org/22.2/docs/introduction/
- "Understanding How Graal Works" — Chris Seaton, JokerConf 2017 — https://chrisseaton.com/truffleruby/jokerconf17/
- JITWatch — https://github.com/AdoptOpenJDK/jitwatch

### Практика / Кейсы

1. **«Метод не инлайнится» — JIT diagnostics.** Включить `-XX:+UnlockDiagnosticVMOptions -XX:+PrintCompilation -XX:+PrintInlining`. Видно: `hot method too big`. Решение: разбить метод, поднять `-XX:MaxInlineSize` / `-XX:FreqInlineSize`, аннотировать `@DontInline` (через compiler hints). Измерить до/после через JMH.
2. **Metaspace leak через classloader.** Webapp перезагружается в Tomcat, через 50 redeploys — `OutOfMemoryError: Metaspace`. `jcmd <pid> VM.classloader_stats` — найти hung classloader; heap dump → MAT → коротчайший путь до GC root, где живёт `WebappClassLoader` через `ThreadLocal` или static field. Исправить: cleanup в `ServletContextListener.contextDestroyed`.
3. **Compressed oops boundary на 32 GB heap.** Продакшен с 36 GB heap — throughput ниже, чем с 31 GB. Причина: свыше 32 GB отключается compressed oops, header растёт на 4 байта на объект. Замер через JMH, откатить heap до 31 GB или включить `-XX:+UseCompressedClassPointers` с пределом Metaspace.
4. **C1 vs C2 benchmark.** Сравнить `-XX:TieredStopAtLevel=1` (только C1) vs default. JMH тест на tight loop, escape analysis, lock elision — видна разница 2–10x.
5. **OSR bottleneck.** Long-running loop в одном методе компилируется в suboptimal код (нет escape analysis). Вынести цикл в отдельный метод, проверить через PrintCompilation, что появляется нормальная C2 компиляция, замерить прирост.
6. **Spring Boot AOT через GraalVM.** Native Image: `native-image -H:ConfigurationFileDirectories=...`; замерить startup time (50 ms vs 2 sec) и RSS (60 MB vs 400 MB). Проблема: reflection в Jackson — добавить `reflect-config.json`. Tradeoff vs JIT для long-running.
7. **Classloader leak в OSGi.** Bundle reload → ClassLoader остаётся. `jcmd VM.classloader_stats`, MAT → Dominator Tree. Причина: `ThreadLocal` в bundle持有的 service. Fix: register cleanup hook.
8. **Verification impact.** Отключить verification `-Xverify:none` (deprecated с JDK 13, удалён в JDK 13+). Альтернатива — `-XX:+TieredStopAtLevel=1` для dev. Замерить startup delta.

---

## 2. Java Memory Model

### Теория

- **JMM** определена в **JSR 133** (вошла в Java 5, 2004), глава **17 JLS**. До JSR 133 volatile был слабо определён, final fields не были безопасны для safe publication.
- Три аспекта JMM: **visibility** (видимость записей между потоками), **ordering** (порядок инструкций), **atomicity** (для long/double без volatile возможен torn write).
- **Happens-before (HB)** — формальное отношение между двумя действиями A и B. Если A hb B, то эффекты A видны потоку B (memory effects переданы).
- **HB правила:**
  1. Program order (в одном потоке без reordering, нарушающего semантику);
  2. Monitor unlock (поток A) hb lock на том же мониторе (поток B);
  3. Запись в volatile поле hb последующего чтения того же volatile;
  4. `Thread.start` hb первое действие в стартовавшем потоке;
  5. Код в потоке hb `Thread.join` возвращается (т.е. эффекты завершённого потока видны joiner);
  6. `Thread.interrupt` hb обнаружение interrupt;
  7. Завершение конструктора hb `finalize`;
  8. Запись в `final` поле hb [freeze action] publish reference через data race.
- **volatile:** чтение/запись имеют load-acquire/store-release семантику (в JMM: StoreStore+StoreLoad на запись, LoadLoad+LoadStore на чтение); запрещает reordering с обычными полями; запись всегда видна; делает long/double write атомарным.
- **final field semantics:** safe publication — если поле `final` инициализировано в конструкторе и конструктор завершился без утечки `this`, любой поток, получивший reference, видит корректное значение. Без final виден 0/null по умолчанию.
- **Data race:** два conflicting accesses (минимум один write) без HB между ними. Правильно синхронизированная программа не имеет data races (DRF — Data Race Freedom).
- **DRF guarantee:** для программ без data races JMM эквивалентна sequential consistency (SC). Для программ с data races — формальная модель (causality model) допускает многие порядки.
- **Memory barriers (hardware):** LoadLoad, LoadStore, StoreStore, StoreLoad. StoreLoad — самый дорогой (на x86 — `mfence`/`lock` prefix). JMM реализуется через расстановку этих барьеров JIT-компилятором.
- **Out-of-thin-air:** JMM формально не полностью запрещает такие значения (известная слабость спецификации), но на практике JIT не генерирует таких результатов.
- **Causality model:** формальное определение через causality-when ordered execution graph.
- **volatile `++` не атомарно:** чтение-инкремент-запись — три операции, нужен `AtomicInteger` или `synchronized`.
- **Double-checked locking** — классический баг Java. Без `volatile` на field можно увидеть partially-constructed object. Корректная версия требует volatile; альтернатива — holder idiom / enum singleton.
- **VarHandle (Java 9+, JEP 193):** явное управление memory order — `PLAIN`, `OPAQUE`, `RELEASE`/`ACQUIRE`, `VOLATILE`. Замена `sun.misc.Unsafe` для low-level memory access.
- **ACC_VOLATILE flag** в bytecode: `javap -v` показывает `volatile` flag на field access_flags.
- **Causality tests** через **JCStress** — инструмент от OpenJDK для проверки JMM-сценариев.
- **Object.wait/notify** также устанавливают HB (через monitor).
- **Thread.start наследует visible writes** родителя → можно безопасно передавать данные через обычные поля, если они записаны до `start()`.

### Материалы

- Книга: **"Java Concurrency in Practice"** Goetz, Peierls, Bloch, Bowbeer, Holmes, Lea (Addison-Wesley, 2006) — глава 16 "The Java Memory Model"
- Aleksey Shipilëv — **"Close Encounters of The Java Memory Model Kind"** — https://shipilev.net/blog/2016/close-encounters-of-jmm-kind/
- Aleksey Shipilëv — **"Java Memory Model Pragmatics"** (slides) — https://shipilev.net/talks/narnia-2555-jmm-pragmatics-en.pdf
- JLS Chapter 17 — "Threads and Locks" — https://docs.oracle.com/javase/specs/jls/se21/html/jls-17.html
- **JSR 133 FAQ** — https://www.cs.umd.edu/~pugh/java/memoryModel/jsr-133-faq.html
- Jeremy Manson — блог "Java Memory Model" — https://jeremymanson.blogspot.com/
- OpenJDK **JCStress** — https://github.com/openjdk/jcstress
- "Happens-before Rules specified in Java Memory Model" — https://medium.com/@gathilaharism/happens-before-rules-specified-in-java-memory-model-734ab400170f

### Практика / Кейсы

1. **JCStress тест для visibility без volatile.** Класс `Holder { int data; boolean ready; }`, поток A записывает `data=42; ready=true;`, поток B крутит `while (!ready); return data;`. Без `volatile ready` видны результаты, где `data == 0`. Пометить `@JCStressTest`, `@Outcome(id = "0", expect = ACCEPTABLE_INTERESTING)`, прогнать, исправить — добавить `volatile`, прогнать, убедиться `0` исчезает.
2. **Safe publication через final.** Без final publish через `publisher.field = new Event(42)` можно увидеть `Event` с `id == 0`. Сделать `id` final → safe. JCStress подтверждает.
3. **Double-checked locking bug.** Классический singleton без volatile: создать через JCStress тест, ловить partially-init. Исправить: `volatile` + локальная переменная (`Holder local = instance; if (local == null) ...`). Альтернатива — holder idiom (lazy init через nested class), enum singleton.
4. **`++volatile` race.** `volatile int counter`, 10 потоков по 100K инкрементов → финальное значение < 1M. Заменить на `AtomicInteger.getAndIncrement` или `LongAdder`.
5. **LongAdder с @Contended против false sharing.** Поле `Cell.value` в `@Contended` (требует `-XX:-RestrictContended`). JMH throughput в high contention: LongAdder ~10x быстрее AtomicLong.
6. **VarHandle acquire/release vs volatile.** Заменить `volatile` на `VarHandle.setRelease/getAcquire` — убрать StoreLoad barrier, JCStress подтверждает корректность, JMH показывает прирост на ARM (на x86 разница невелика).
7. **HB через Thread.start.** Поток A записывает в обычные (не volatile) поля, затем `Thread t = new Thread(() -> read(fields)); t.start();`. В потоке `t` видны все записи — HB start. JCStress.
8. **Long torn write без volatile (теоретически).** На 32-битных JVM (редкость сейчас) запись `long` без volatile может быть torn. Тест JCStress с `@Outcome(id = "0", ...)`. На современных 64-битных JVM практически невоспроизводимо, но спецификация допускает.

---

## 3. Garbage Collection

### Теория

- **GC roots** (точки, из которых начинается mark):
  - локальные переменные в активных фреймах стеков всех Java-потоков;
  - активные Java-потоки;
  - static поля классов;
  - JNI global references (`NewGlobalRef`);
  - синхронизированные мониторы (объекты, на которых держим lock);
  - системные classloaders.
- **Mark-Sweep:** mark reachable → sweep unreachable. Минус — фрагментация.
- **Mark-Compact:** mark + compact (сдвиг). Нет фрагментации, но pause дольше.
- **Copying (semi-space):** from-space → to-space, выжившие копируются. Черезput хорош для young gen, но теряется половина памяти.
- **Generational hypothesis:** большинство объектов умирают молодыми → young/old gen. Young — copying; old — mark-compact/mark-sweep.
- **Young gen:** Eden + Survivor S0 + S1 (по умолчанию 8:1:1, через `-XX:SurvivorRatio=8`). Minor GC → copying live из Eden+S0 в S1, потом swap.
- **Promotion:** объект survived `MaxTenuringThreshold` (по умолчанию 15, dynamic с `-XX:+UseAdaptiveSizePolicy`). Большие объекты (>= `PretenureSizeThreshold`) сразу в old.
- **TLABs (Thread-Local Allocation Buffers):** каждый поток имеет свой регион Eden для lock-free allocation. `-XX:+UseTLAB` (по умолчанию).
- **Card table:** write barrier отмечает dirty card (512 байт) при cross-gen ссылке, чтобы GC проверил только dirty cards.
- **Remembered Set (RSet)** в G1: какие другие regions ссылаются на данную — позволяет собирать region независимо (паузы пропорциональны RSet, не heap).
- **Concurrent Marking phases (G1):** initial mark (STW, piggyback на young GC) → root region scan (concurrent, scan survivors) → concurrent mark → remark (STW, finalize) → cleanup (STW partial).
- **Serial GC** (`-XX:+UseSerialGC`): один поток, для маленьких heap (<100 MB), встраиваемые системы.
- **Parallel GC** (`-XX:+UseParallelGC`, throughput collector): multiple GC threads, STW. Default в Java 8. Минус — pause растёт с heap.
- **G1** (`-XX:+UseG1GC`): default с Java 9. Heap разбит на regions (1–32 MB, степень 2). Pause предсказуема: `-XX:MaxGCPauseMillis=200` (goal, не hard). Mixed GC (young + часть old).
- **ZGC** (`-XX:+UseZGC`, JEP 377 prod Java 15, JEP 439 generational Java 21): colored pointers + load barriers. Паузы < 1 ms на heap до 16 TB. Concurrent everything.
- **Shenandoah** (`-XX:+UseShenandoahGC`, Red Hat, prod Java 15): Brooks pointer / load barrier. Concurrent evacuate. Generational (JEP 404 experimental).
- **Epsilon** (`-XX:+UseEpsilonGC`, JEP 318 Java 11): no-op GC, для performance testing.
- **Tuning flags:**
  - `-Xmx`, `-Xms` — max/min heap (рекомендуется равными для production);
  - `-XX:NewRatio=2` — old:young = 2:1;
  - `-XX:SurvivorRatio=8` — Eden:S = 8:1;
  - `-XX:MaxGCPauseMillis=200` — goal для G1;
  - `-XX:GCTimeRatio=99` — цель: GC ≤ 1% времени;
  - `-XX:ParallelGCThreads=` — STW threads;
  - `-XX:ConcGCThreads=` — concurrent threads (обычно ParallelGCThreads / 4);
  - `-XX:InitiatingHeapOccupancyPercent=` (IHOP) — порог для запуска concurrent cycle (G1);
  - `-XX:G1HeapRegionSize=` — размер region G1.
- **Heap dump:**
  - `jcmd <pid> GC.heap_dump /tmp/dump.hprof`;
  - `jmap -dump:format=b,file=dump.hprof <pid>` (deprecated);
  - `-XX:+HeapDumpOnOutOfMemoryError -XX:HeapDumpPath=/var/dumps/`.
- **MAT (Eclipse Memory Analyzer Tool):**
  - **Shallow size** — размер самого объекта;
  - **Retained size** — сколько освободится при GC этого объекта (включая dominated tree);
  - **Dominator Tree** — иерархия по retained size;
  - **Histogram** — по классам;
  - **GC Roots** — пути к root;
  - **Leak Suspects Report** — автоматический отчёт.
- **GC logging (Java 9+ unified logging):** `-Xlog:gc*:file=gc.log:time,uptime,level,tags:filecount=10,filesize=100M`.
- **gceasy.io** — анализ GC логов, p99 pause, throughput.
- **JFR (Java Flight Recorder)** — low-overhead profiling (<1%), события GC встроены.

### Материалы

- Книга: **"Java Performance"** Scott Oaks (O'Reilly, 2020) — главы 5–8 "Understanding Memory", "Heap", "Garbage Collection"
- Книга: **"Optimizing Java"** Clarke et al. — главы 6–8 GC
- OpenJDK Wiki: **Shenandoah** — https://wiki.openjdk.org/display/shenandoah/Main
- JEP 439: **Generational ZGC** — https://openjdk.org/jeps/439
- JEP 404: **Generational Shenandoah (Experimental)** — https://openjdk.org/jeps/404
- Oracle Docs — **G1 GC Tuning** — https://docs.oracle.com/en/java/javase/21/gctuning/
- **GCeasy** — https://blog.gceasy.io/garbage-collection-tuning-guide/
- "G1 Garbage Collector Performance Tuning" — https://blog.heaphero.io/g1-garbage-collector-performance-tuning
- IBM Community — "G1, ZGC, and Shenandoah: OpenJDK's Garbage Collectors" — https://community.ibm.com/community/user/blogs/theo-ezell/2025/09/03/g1-shenandoah-and-zgc-garbage-collectors

### Практика / Кейсы

1. **OutOfMemoryError: Java heap space.** Включить `-XX:+HeapDumpOnOutOfMemoryError`. Воспроизвести (static `ConcurrentHashMap` с некэшируемыми ключами). MAT → Leak Suspects → найти static map → Dominator Tree → подтвердить retained size 80% heap. Исправить: bounded cache (Caffeine `maximumSize`), eviction policy.
2. **GC pause spike на проде.** Собрать unified GC log: `jcmd <pid> GC.log_filename gc.log` или смотреть существующий. Загрузить в gceasy.io — p99 pause 4 сек на Full GC (Parallel). Решение: migrate на G1 (`-XX:+UseG1GC -XX:MaxGCPauseMillis=200`), перенастройка IHOP. Замерить p99 до/после.
3. **Migration G1 → ZGC для low-latency.** Сервис с p99 = 50 ms из-за GC pauses на 32 GB heap. `java -XX:+UseZGC -XX:+ZGenerational`. Замерить wrk + JMH: pause < 1 ms, throughput -5–10%. Tradeoff: чуть выше CPU overhead.
4. **Metaspace OOM через CGLIB.** Приложение создаёт dynamic proxies per request — `OutOfMemoryError: Metaspace`. Решение: кэшировать proxy factories, ограничить `-XX:MaxMetaspaceSize=512m`, переписать на compile-time codegen (ByteBuddy).
5. **Allocation rate profiling.** Сервис создаёт 5 GB/sec garbage → minor GC каждую секунду. `async-profiler -d 60 -e alloc -f alloc.html <pid>` → flame graph; найти hot allocation в tight loop (например, boxing `Integer`). Заменить на `int[]` / `VarHandle`/primitive specialization.
6. **False sharing через @Contended.** Два `AtomicLong` counter в одном объекте лежат в одной cache line → cache invalidation между cores. JMH benchmark. Аннотировать `@jdk.internal.vm.annotation.Contended` + `-XX:-RestrictContended`. Throughput 3x.
7. **Large heap G1 tuning (64 GB).** Default region size 32 MB — слишком крупно для mixed GC. `-XX:G1HeapRegionSize=8m`. IHOP 45% по умолчанию → слишком рано → frequent concurrent cycles → поднять IHOP до 55%, disable adaptive (`-XX:-G1UseAdaptiveIHOP`). Замерить mixed GC frequency.
8. **Retained size analysis в MAT.** Найти объект, удерживающий 4 GB (Map.Entry → Map → static field). `Merge Shortest Paths to GC Roots → exclude weak/soft references`. Исправить через `WeakReference` или `WeakHashMap` для cache.

---

## 4. Threads, lifecycle, interrupts, daemon, ThreadLocal, virtual threads

### Теория

- **Thread states** (`Thread.State` enum): `NEW` (создан, не запущен), `RUNNABLE` (готов или выполняется в OS), `BLOCKED` (ждёт monitor lock), `WAITING` (wait/join/LockSupport.park без timeout), `TIMED_WAITING` (sleep/wait/join/parkNanos), `TERMINATED`.
- **Thread.start vs run:** `start` создаёт native поток (через `pthread_create` на Linux) и вызывает `run`; прямой вызов `run` — обычный sync-вызов в текущем потоке.
- **HotSpot thread model — 1:1** (один Java-поток = один native OS thread). Изменение в Java 21 — virtual threads (M:N).
- **Priority** 1 (MIN) – 5 (NORM) – 10 (MAX). На HotSpot мапится на OS nice level, но семантика слабая, использовать не рекомендуется.
- **Daemon threads:** `setDaemon(true)` до `start()`. JVM не ждёт daemon при выходе. daemon не должен держать I/O ресурсы (могут быть оборваны mid-write).
- **Interrupts:**
  - Каждый поток имеет interrupt flag.
  - `Thread.interrupt()` — устанавливает flag.
  - Блокирующие методы (`sleep`, `wait`, `join`, blocking I/O через `InterruptibleChannel`) проверяют flag и кидают `InterruptedException`, **сбрасывая** flag.
  - `Thread.interrupted()` — статический, возвращает flag и сбрасывает его (использовать в current thread).
  - `Thread.isInterrupted()` — instance, не сбрасывает.
- **Правильная обработка `InterruptedException`:**
  - **Не проглатывать** `catch (InterruptedException) { }` — теряется сигнал о shutdown.
  - Восстановить flag: `Thread.currentThread().interrupt();` или прокинуть исключение выше.
- **Deprecated методы** (`stop`, `suspend`, `resume`, `countStackFrames`, `destroy`) — устарели с JDK 1.2. `stop` кидает `ThreadDeath` в произвольной точке → inconsistent state. Замена — cooperative shutdown через interrupt + volatile flag.
- **ThreadLocal:** per-thread хранилище. Реализация — `Thread.threadLocals` (ThreadLocalMap). Key — `WeakReference<ThreadLocal>`, value — strong reference.
- **Memory leak через ThreadLocal в пуле:** пул живёт долго, потоки переиспользуются, value в ThreadLocalMap не очищается без явного `remove()`. Классический пример — Tomcat WebappClassLoader leak.
- **`InheritableThreadLocal`:** при `Thread.start()` копируется из parent в child через `Thread.parent.inheritableThreadLocals`. Не работает для пулов (получают existing thread, а не новый).
- **`ThreadLocal.withInitial(Supplier)`** — ленивая инициализация.
- **`remove()` в finally** — обязательно.
- **Virtual Threads (JEP 444, Java 21, final):** lightweight user-mode threads. Планируются на carrier threads (по умолчанию `ForkJoinPool` с `availableProcessors` carriers). Continuation park/unpark — stack сохраняется в heap.
- **Создание:** `Thread.ofVirtual().start(runnable)` или `Thread.startVirtualThread(runnable)` или `Executors.newVirtualThreadPerTaskExecutor()`.
- **Pinning (JDK 21):** virtual thread "приколот" к carrier, когда:
  - внутри `synchronized` блока/метода (монитор)
  - вызов `Object.wait()` (native wait)
  - JNI native frames
  Pinning блокирует carrier на всё время pinning. Исправлено в **JEP 491** (Java 24, синхронизация без pinning).
- **Диагностика pinning:** `jcmd <pid> Thread.dump_to_file -format=json`, искать `"pinned": true`. Или `-Djdk.tracePinnedThreads=full/short`.
- **Carrier pool starvation:** если virtual threads делают CPU-bound работу или blocking без yield → carriers заняты, virtual threads не прогрессируют.
- **Structured Concurrency (JEP 453/462/480/505, preview в Java 21–25):**
  - `StructuredTaskScope` — родитель ждёт дочерние tasks;
  - `ShutdownOnFailure` — при первой ошибке отменяет остальные;
  - `ShutdownOnSuccess` — при первом успехе отменяет остальные.
- **Scoped Values (JEP 446/462/480/506, preview в Java 21–25):** immutable, bounded lifetime альтернатива ThreadLocal. Не наследуется бездумно — передаётся явно через `ScopedValue.where(V, value).run(() -> ...)`. Эффективнее памяти для миллионов virtual threads.
- **`Thread.Builder` (Java 19+):** `Thread.ofPlatform().name("worker-%d", 0).daemon(true).start(r)` — fluent API.
- **`ThreadMXBean`** — JMX бин для диагностики: `findDeadlockedThreads()`, `getThreadInfo()`, `getThreadCpuTime()`.
- **Stack size:** `-Xss512k` (по умолчанию 1 MB на Linux x64). Глубокая рекурсия → StackOverflowError.
- **Thread.toString** содержит name, priority, thread group.

### Материалы

- Книга: **"Java Concurrency in Practice"** — главы 5 "Building Blocks", 7 "Cancellation and Shutdown"
- JEP 444: **Virtual Threads** — https://openjdk.org/jeps/444
- JEP 453 (preview 21): **Structured Concurrency** — https://openjdk.org/jeps/453
- JEP 446 (preview 21): **Scoped Values** — https://openjdk.org/jeps/446
- JEP 491 (Java 24): **Synchronize Virtual Threads without Pinning** — https://openjdk.org/jeps/491
- Oracle Docs — **Structured Concurrency (Java 21)** — https://docs.oracle.com/en/java/javase/21/core/structured-concurrency.html
- InfoQ — "Enhancing Java Concurrency with Scoped Values" — https://www.infoq.com/news/2023/06/scoped-values-java-21/
- DZone — "Memory Leak Due to Uncleared ThreadLocal Variables" — https://dzone.com/articles/memory-leak-threadlocal-variables
- Inside.java — Ron Pressler's virtual threads articles — https://inside.java/2024/04/25/virtualthreads/

### Практика / Кейсы

1. **Virtual thread pinning в REST endpoint.** Endpoint `@Transactional` использует `synchronized` в Hibernate session. Под нагрузкой carrier pool (4 потока) забит → throughput проседает. `jcmd <pid> Thread.dump_to_file -format=json -format=json`, видны `"pinned": true` на DB вызовах. Решение: заменить `synchronized` на `ReentrantLock`, либо обновиться до JDK 24+ (JEP 491).
2. **ThreadLocal leak в Tomcat после redeploy.** WebappClassLoader не GC'd, MAT показывает retained через ThreadLocal entries в пуле Tomcat threads. Fix: в `ServletRequestListener.requestDestroyed` или в `finally` — `tl.remove()`. Для пулов — `afterExecute` hook.
3. **Проглатывание InterruptedException → deadlock при shutdown.** Код `catch (InterruptedException ignored) {}`. При `shutdownNow()` executor пул не завершается. Решение: restore flag `Thread.currentThread().interrupt()` + propagate.
4. **Deadlock через lock ordering.** Два потока: `transfer(A, B)` и `transfer(B, A)`. `jstack -l <pid>` — "Found 1 deadlock". Решение: consistent ordering (по `System.identityHashCode`), либо `tryLock` обоих с timeout + retry, либо统一的 lock через `ReentrantLock.tryLock()`.
5. **Migrate ExecutorService → virtual threads.** Thread pool с 200 платформ-потоками для HTTP-запросов (I/O-bound) — bottleneck на 200 concurrent. Заменить на `Executors.newVirtualThreadPerTaskExecutor()`, поднять concurrency до 10K. Throughput ×5, latency стабилизирована.
6. **InheritableThreadLocal memory blowup с virtual threads.** 1M virtual threads, каждый наследует 1 KB ThreadLocal map → 1 GB. Заменить на `ScopedValue` — bounded, не наследуется вглубь, занимает память только в области.
7. **Graceful shutdown пула.** `shutdown()` (no new tasks) + `awaitTermination(30, SECONDS)` + если не завершился — `shutdownNow()` (interrupt). Логировать отменённые задачи (`getReturnedTasks()`).
8. **Carrier pool starvation через CPU-bound virtual thread.** Запуск 1000 virtual threads с compute-bound loop → carriers заняты, I/O-bound virtual threads голодают. Решение: CPU-bound работы — platform threads через `Thread.ofPlatform()` или `ForkJoinPool` для compute.

---

## 5. synchronized & locks

### Теория

- **Intrinsic lock (monitor):** каждый `Object` имеет monitor (неявно в header Mark Word). `synchronized` methods и blocks используют его.
- **Bytecode:** `monitorenter` / `monitorexit` (для блоков; synchronized method имеет флаг `ACC_SYNCHRONIZED`, JIT вставляет enter/exit неявно). `monitorexit` вызывается дважды для блока — normal exit + exception exit.
- **Monitor inflation (HotSpot):** biased (removed) → thin (CAS в header) → fat (`ObjectMonitor` с wait set, entry list).
- **Biased locking** устарел и удалён в JDK 15 (JEP 374) — на современных x86 с CAS стоимостью ~5 ns уже не выгодно.
- **ReentrantLock** (`java.util.concurrent.locks`): `lock()`, `unlock()` (обязательно в `finally`), `lockInterruptibly()`, `tryLock()`, `tryLock(time, unit)`, `newCondition()`. Реентерабельный.
- **Fairness:** `new ReentrantLock(true)` — FIFO через AQS queue. Non-fair (default) — может barge (войти без очереди), throughput выше, но возможна starvation.
- **ReentrantReadWriteLock:** read lock shared, write lock exclusive. Read lock можно держать нескольким потокам, write — одному. Read → write upgrade **запрещён** (deadlock).
- **StampedLock (Java 8):**
  - `writeLock()` — exclusive, возвращает stamp;
  - `readLock()` — pessimistic shared;
  - `tryOptimisticRead()` — zero-overhead read, возвращает stamp;
  - `validate(stamp)` — проверка, не было ли write с момента stamp;
  - если `validate` false → fallback на `readLock`.
  - **Не реентерабелен!** Если один и тот же поток вызовет readLock дважды — deadlock.
  - Не поддерживает `Condition`.
- **Condition:** `lock.newCondition()`, `await()` / `signal()` / `signalAll()`. Несколько condition queues per lock (в отличие от единственного wait set монитора). Позволяет будить конкретную категорию waiters.
- **AQS (AbstractQueuedSynchronizer):** основа для ReentrantLock, Semaphore, CountDownLatch, ReentrantReadWriteLock. Семантика — `volatile int state` + FIFO queue (CLH-variant). Эксклюзивный и shared режимы.
- **LockSupport.park/unpark:** низкоуровневая блокировка. `unpark` можно вызвать до `park` — permit запасается, следующий `park` вернётся немедленно. В отличие от `Object.notify`, не теряется.
- **tryLock без timeout — non-blocking:** возвращает сразу, не блокирует. Используется для deadlock avoidance через lock ordering.
- **Reader/writer starvation:** non-fair RW lock может голодить writers (постоянный поток readers не даёт write). Fair — голодит readers (writer ждёт). `StampedLock` с optimistic read — выход.
- **Deadlock conditions (Coffman):** mutual exclusion, hold-and-wait, no preemption, circular wait. Для борьбы: нарушить hold-and-wait (взять все locks сразу через tryLock) или circular wait (consistent ordering).
- **Livelock через tryLock retry loop:** два потока берут locks по одному, оба fail, оба backoff. Решение: randomized backoff.
- **synchronized vs ReentrantLock performance:** после JDK 6 фактически сравнялись (biased lock removal ухудшил synchronized, escape analysis + elision помогает в отсутствии contention). ReentrantLock выбран для: interruptible, timeout, multiple conditions, fairness, virtual threads (нет pinning).
- **Virtual threads:** `synchronized` пинит carrier в JDK 21. `ReentrantLock` — нет. Spring/Hibernate мигрируют на ReentrantLock.

### Материалы

- Книга: **"Java Concurrency in Practice"** — глава 13 "Explicit Locks"
- Baeldung — **"Guide to java.util.concurrent.Locks"** — https://www.baeldung.com/java-concurrent-locks
- Baeldung — **"Binary Semaphore vs Reentrant Lock"** — https://www.baeldung.com/java-binary-semaphore-vs-reentrant-lock
- Baeldung — **"Synchronize Virtual Thread Without Pinning"** — https://www.baeldung.com/java-synchronize-virtual-thread-no-pinning
- Winterbe — **"Java 8 Concurrency Tutorial: Synchronization and Locks"** — https://winterbe.com/posts/2015/04/30/java8-concurrency-tutorial-synchronized-locks-examples/
- Oracle Docs — `java.util.concurrent.locks` package summary — https://docs.oracle.com/en/java/javase/21/docs/api/java.base/java/util/concurrent/locks/package-summary.html
- JEP 374: **Disable and Deprecate Biased Locking** — https://openjdk.org/jeps/374
- DZone — "Java Dynamic Proxies" (связано с AOP + locks) — https://dzone.com/articles/java-dynamic-proxies

### Практика / Кейсы

1. **Deadlock через lock ordering → jstack → fix.** Два потока `transfer(A, B)` и `transfer(B, A)` через `synchronized` на обоих аккаунтах. `jstack -l <pid>` — секция "Found 1 deadlock". Решение: сравнить `System.identityHashCode(a)` vs `(b)`, всегда брать сначала меньший; либо `tryLock` обоих с timeout и retry.
2. **Writer starvation в ReentrantReadWriteLock.** Non-fair RW lock, read-heavy workload → writer голодает, latency write операций растёт. Переключить на `fair=true`, замерить throughput/latency. Или перейти на `StampedLock` с optimistic reads.
3. **StampedLock optimistic read для read-heavy cache.** `long stamp = sl.tryOptimisticRead(); long v = value; if (!sl.validate(stamp)) { stamp = sl.readLock(); try { v = value; } finally { sl.unlockRead(stamp); } }`. JMH: throughput ×2–3 vs ReentrantReadWriteLock на read-heavy, нет overhead на read path.
4. **Producer-consumer с 2 Conditions.** Bounded buffer, `notFull` и `notEmpty` conditions на одном `ReentrantLock`. `put`: lock + `while (full) notFull.await(); add; notEmpty.signal();` `take` — симметрично. Не разбудить не ту категорию.
5. **Interruptible lock для cancellation.** Long-running task в `ExecutorService`, `shutdownNow()`. Задача использует `lock.lockInterruptibly()` — корректно отвечает на interrupt, освобождает lock. С `synchronized` interrupt игнорируется.
6. **tryLock для avoiding deadlock.** Transfer money: `if (from.lock.tryLock()) { try { if (to.lock.tryLock()) { try { ... } finally { to.lock.unlock(); } } } finally { from.lock.unlock(); } }` + retry с random backoff если второй не получился.
7. **StampedLock deadlock через reentrancy.** Метод A берёт `readLock`, вызывает метод B, который тоже `readLock` того же StampedLock — deadlock. Решение: не вкладывать, либо передать stamp.
8. **Migration synchronized → ReentrantLock для virtual threads.** Hibernate `SessionImpl` использует synchronized. На JDK 21 carrier pool голодает. Патч — заменить на ReentrantLock, измерить throughput.

---

## 6. java.util.concurrent

### Теория

- **ConcurrentHashMap (Java 7):** массив `Segment` (по умолчанию 16) — каждый `Segment` extends `ReentrantLock`. Concurrency level = число segments.
- **ConcurrentHashMap (Java 8+):** Segments убраны (остались только для backward compat). Базовая структура — `Node[]` table. Запись в bin:
  - пустой bin — CAS в `Node`;
  - непустой — `synchronized` на head Node bin;
  - bin >= `TREEIFY_THRESHOLD` (8) и capacity >= `MIN_TREEIFY_CAPACITY` (64) → `TreeBin` (красно-чёрное дерево, O(log n)).
- **`size()`:** `CounterCell[]` (аналог LongAdder) + `baseCount`. `sumCount` — sum с iteration. `size()` возвращает `int` (может переполниться, deprecated usage) — лучше `mappingCount()` (returns `long`).
- **`computeIfAbsent(K, Function)`:** атомарно; если mapping отсутствует, вызывает function. В Java 8 рекурсивный вызов `computeIfAbsent` того же CHM того же key → deadlock (зафиксировано JDK-8062841, исправлено в Java 9).
- **`compute`, `merge`, `putIfAbsent`, `replace`, `remove(k, v)`** — атомарные.
- **`forEach(parallelismThreshold, action)`** — параллельная итерация, если размер > threshold.
- **Iterator weak consistency:** не бросает `ConcurrentModificationException`, но не гарантирует отражение изменений после создания итератора.
- **Happens-before:** операции CHM создают HB (как synchronization actions). Запись в CHM hb последующее чтение.
- **ConcurrentSkipListMap / Set:** skip list, sorted, O(log n), lock-free reads, CAS writes. Альтернатива TreeMap в concurrent context.
- **CopyOnWriteArrayList:** при каждой write (`add`, `set`, `remove`) создаётся новая копия array. Reads — lock-free через volatile `array` reference. Write O(n). Идеально для listener registries, configurations (read-heavy, write-rare).
- **CopyOnWriteArraySet:** обёртка над COW ArrayList.
- **BlockingQueue implementations:**
  - **ArrayBlockingQueue:** bounded, array, single ReentrantLock (read+write на одном lock);
  - **LinkedBlockingQueue:** optionally bounded, linked nodes, 2 locks (put/take separate) → выше throughput на contention;
  - **SynchronousQueue:** zero capacity, hand-off (producer ждёт consumer);
  - **PriorityBlockingQueue:** unbounded, heap, priority comparator;
  - **DelayQueue:** элементы доступны после delay;
  - **LinkedTransferQueue:** lock-free, optimized для transfer pattern;
  - **ArrayDeque / ConcurrentLinkedQueue / ConcurrentLinkedDeque** — non-blocking, unbounded.
- **put vs offer vs take vs poll:** `put` блокирует при full; `offer` — non-blocking (или с timeout); `take` блокирует при empty; `poll` — non-blocking (или с timeout).
- **HashMap в многопотке:** **НЕ thread-safe**. В Java 7 resize мог построить циклический linked list → `get` зацикливался на 100% CPU. В Java 8 цикл исправлен, но потеря данных остаётся. Замена — `ConcurrentHashMap`.
- **`ConcurrentHashMap.newKeySet()`** — concurrent set view backed by CHM.

### Материалы

- Книга: **"Java Concurrency in Practice"** — глава 5 "Building Blocks"
- Oracle Docs — `java.util.concurrent` package — https://docs.oracle.com/en/java/javase/21/docs/api/java.base/java/util/concurrent/package-summary.html
- Oracle Docs — `ConcurrentHashMap` — https://docs.oracle.com/en/java/javase/21/docs/api/java.base/java/util/concurrent/ConcurrentHashMap.html
- Inside.java — "ConcurrentHashMap: The Internals" — https://inside.java/2021/09/28/concurrencypill-concurrenthashmap/
- Jenkov — "ConcurrentHashMap" — http://tutorials.jenkov.com/java-util-concurrent/concurrentmap.html
- AlgoMaster.io — "java.util.concurrent Package Tour" — https://algomaster.io/learn/concurrency-interview/java-util-concurrent-package-tour
- GeeksforGeeks — "Need for Concurrent Collections" — https://www.geeksforgeeks.org/java/need-concurrent-collections-java/
- DZone — "Java Concurrent Collections" — https://dzone.com/articles/java-concurrent-collections

### Практика / Кейсы

1. **CHM `computeIfAbsent` recursion deadlock (Java 8).** `map.computeIfAbsent(key, k -> map.computeIfAbsent(k + "'s child", ...))` → deadlock в Java 8. Воспроизвести на JDK 8, протестировать на JDK 17+, сравнить. Workaround: split into `if (!map.containsKey(k)) map.put(k, ...)`.
2. **HashMap infinite loop (Java 7) / data loss (Java 8).** Воспроизвести стресс-тестом: 10 потоков по 100K put. На JDK 7 — `get` зацикливается на 100% CPU. На JDK 8+ — `size()` меньше ожидаемого. Заменить на `ConcurrentHashMap`.
3. **CopyOnWriteArrayList для event listeners.** `fireEvent()` итерирует по snapshot — нет `ConcurrentModificationException`. Но `addListener` на каждый event — O(n) copy → memory churn. Решение: копировать только при actual change, отдавать immutable snapshot через `AtomicReference<List<Listener>>`.
4. **ArrayBlockingQueue vs LinkedBlockingQueue throughput.** JMH: produce-consume, 4 producer + 4 consumer, capacity 1000. Linked — обычно быстрее (два lock). Array — ниже latency (нет node allocation). Choose по требованиям.
5. **`ConcurrentHashMap` memory pressure на 10M entries.** `Node` header (12 bytes) + hash (4) + key/value ref (4+4) + padding = 24 bytes на entry + table. Если memory критична — `Trove`/`fastutil` primitive maps или `HashMap`+external sync через striped locks.
6. **PriorityBlockingQueue starvation.** High-priority tasks всегда впереди → low-priority голодает. Решение: комбинировать с fairness по времени (custom comparator с timestamp), либо `ForkJoinPool` с work-stealing.
7. **`size()` overflow vs `mappingCount()`.** CHM с 3G entries → `size()` returns negative (int overflow). Использовать `mappingCount()` (long). Замерить через `ConcurrentHashMap<Integer,Integer>` с 2^31 entries (потребует heap > 100 GB).
8. **ConcurrentLinkedQueue weak iterator.** Создать итератор, добавить 1000 элементов, итерировать — итератор видит только subset. Семантика weak consistency. Для snapshot — скопировать в ArrayList, потом итерировать.

---

## 7. Executors & Thread Pools

### Теория

- **`Executor`** interface: `void execute(Runnable)`. Минимальная абстракция.
- **`ExecutorService`** расширяет: `shutdown()`, `shutdownNow()`, `awaitTermination(...)`, `submit(Callable/Runnable)` → `Future<T>`, `invokeAll(Collection<Callable>)`, `invokeAny(...)`.
- **`ThreadPoolExecutor` параметры конструктора:**
  - `corePoolSize` — минимальное кол-во потоков (не вытесняются по timeout если не `allowCoreThreadTimeOut(true)`);
  - `maximumPoolSize` — максимум;
  - `keepAliveTime` + `TimeUnit` — сколько idle thread живёт сверх core;
  - `BlockingQueue<Runnable>` — workQueue;
  - `ThreadFactory` — создаёт потоки (name, daemon, uncaught exception handler);
  - `RejectedExecutionHandler` — что делать при overflow.
- **Pool growth semantics (важно!):** при `execute(r)`:
  1. Если потоков < core → создать новый (даже если idle потоки есть);
  2. Иначе положить в queue;
  3. Если queue full → создать поток до maximumPoolSize;
  4. Если и max достигнут → reject.
  То есть **bounded queue блокирует рост до max**. Это контринтуитивно для многих.
- **Queues и пулы `Executors` factory:**
  - `newFixedThreadPool(n)` → core=max=n, queue = `LinkedBlockingQueue` (unbounded) → **OOM risk**;
  - `newCachedThreadPool()` → core=0, max=Integer.MAX_VALUE, queue = `SynchronousQueue` → **thread explosion risk**;
  - `newSingleThreadExecutor()` → core=max=1, unbounded queue;
  - `newScheduledThreadPool(n)` → `ScheduledThreadPoolExecutor`, delayed + periodic (`scheduleAtFixedRate`, `scheduleWithFixedDelay`).
- **Rejection policies:**
  - `AbortPolicy` (default) — кидает `RejectedExecutionException`;
  - `CallerRunsPolicy` — выполняет задачу в потоке вызывающего (обратное давление);
  - `DiscardPolicy` — молча выбрасывает;
  - `DiscardOldestPolicy` — выкидывает head очереди, потом `execute` снова.
  - Кастомный `RejectedExecutionHandler` — логировать, persist в DB, etc.
- **ForkJoinPool (Java 7+):**
  - Work-stealing: idle thread крадёт task из хвоста чужого deque.
  - `ForkJoinTask` — `RecursiveAction` (no return), `RecursiveTask<V>`, `CountedCompleter<V>`.
  - `commonPool()` — статический, parallelism = `availableProcessors - 1` (настраивается `-Djava.util.concurrent.ForkJoinPool.common.parallelism`).
  - Используется в `parallelStream()` и `CompletableFuture` (если не указан executor).
- **CompletableFuture (Java 8+):**
  - `runAsync` / `supplyAsync(Supplier)` — async, default = `commonPool` (BAD для blocking I/O);
  - `thenApply(Function)` — sync в текущем потоке (или completed thread);
  - `thenApplyAsync(fn, executor)` — explicit executor;
  - `thenCompose(Function<...CompletableFuture>)` — flatten (flatMap);
  - `thenCombine(other, BiFunction)` — комбинировать два future;
  - `allOf(...)` / `anyOf(...)` — ждать все / первый;
  - `exceptionally(fn)` — recover;
  - `handle((value, ex) -> ...)` — bi-handler;
  - `whenComplete((value, ex) -> ...)` — side-effect, не влияет на результат.
- **CF pitfalls:**
  - Blocking `get()` на common pool → starvation;
  - `thenApply` с blocking I/O → выполняется в common pool, блокирует carrier;
  - Exception в `thenApply` не виден до `get()` без `exceptionally`.
- **`newVirtualThreadPerTaskExecutor()` (Java 21):** создаёт executor, который стартует virtual thread на каждую `execute`. Подходит для I/O-bound fan-out.
- **Shutdown semantics:**
  - `shutdown()` — refuse new, complete queued + in-flight;
  - `shutdownNow()` — interrupt in-flight, return queued (`List<Runnable>`);
  - `awaitTermination(timeout)` — блокирующее ожидание.
- **Принцип:** никогда не использовать `Executors.newFixed/Cached` в production. Всегда `new ThreadPoolExecutor(...)` с явными параметрами и named ThreadFactory.

### Материалы

- Книга: **"Java Concurrency in Practice"** — глава 8 "Thread Pools"
- Oracle Docs — `ThreadPoolExecutor` — https://docs.oracle.com/en/java/javase/21/docs/api/java.base/java/util/concurrent/ThreadPoolExecutor.html
- Oracle Docs — `ForkJoinPool` — https://docs.oracle.com/en/java/javase/21/docs/api/java.base/java/util/concurrent/ForkJoinPool.html
- Baeldung — "CompletableFuture and ThreadPool" — https://www.baeldung.com/java-completablefuture-threadpool
- Baeldung — "ExecutorService vs ForkJoinPool" — https://www.baeldung.com/java-executor-service-vs-forkjoinpool
- Java Performance — "Be wary of the common ForkJoinPool" — https://dzone.com/articles/beware-the-common-forkjoinpool
- JEP 444 — Virtual Threads — секция "New API: `Executors.newVirtualThreadPerTaskExecutor`" — https://openjdk.org/jeps/444
- Softwaremill — "Threads, ThreadPools and Executors" — https://softwaremill.com/threadpools-executors-and-java

### Практика / Кейсы

1. **Bounded pool с backpressure.** Конфиг: core=10, max=50, queue = `ArrayBlockingQueue(100)`, `CallerRunsPolicy`. Под нагрузкой 1000 req/s — после queue full, задачи выполняются в caller threads → backpressure на HTTP server, который снижает accept rate. Измерить p99 latency, throughput.
2. **OOM через `newFixedThreadPool` с unbounded queue.** Сервис принимает 10K req/s, каждый кладёт task на 1 sec, pool = 10 threads. Queue растёт → 1M pending → OOM. Заменить на `ArrayBlockingQueue(500)` + `CallerRunsPolicy`.
3. **CompletableFuture на common pool с blocking I/O.** `CompletableFuture.supplyAsync(() -> httpClient.get(url))` — blocking в commonPool (parallelism = n-1). Под нагрузкой все parallelism заняты → parallelStream голодает. Решение: явный `Executor` для I/O (например, virtual threads в Java 21).
4. **ParallelStream с blocking = common pool starvation.** `list.parallelStream().map(this::callExternalApi).collect(...)` — все 7 (8-1) parallelism заняты blocking I/O. Заменить на `Stream` + `map` на virtual threads через `StreamSupport`/`Semaphore`.
5. **Custom rejection handler.** Persist отвергнутой задачи в DB (`pending_tasks`), отдельный scheduler retries. Логировать с MDC context (tracing).
6. **RecursiveTask Fibonacci через ForkJoinPool vs sequential.** Benchmark: для N=40 — sequential быстрее из-за overhead fork/join. Для CPU-bound задач FJP не выгоден, только для делимых задач (merge sort, деревообработка).
7. **Shutdown race.** Только `shutdownNow()` без `awaitTermination` → теряются in-flight tasks. Корректный паттерн:
   ```java
   pool.shutdown();
   if (!pool.awaitTermination(30, SECONDS)) {
       List<Runnable> dropped = pool.shutdownNow();
       log.warn("Dropped {} tasks", dropped.size());
       if (!pool.awaitTermination(10, SECONDS)) err();
   }
   ```
8. **Deadlock в bounded pool.** Task A ждёт result task B, но B в очереди (max достигнут) → deadlock. Решение: pool size >= max parallelism chain, либо unbounded, либо `CallerRunsPolicy`, либо `ForkJoinPool` для nested tasks.

---

## 8. Atomic & CAS

### Теория

- **Atomic classes** в `java.util.concurrent.atomic`:
  - `AtomicInteger`, `AtomicLong`, `AtomicBoolean`, `AtomicReference<V>`;
  - `AtomicIntegerArray`, `AtomicLongArray`, `AtomicReferenceArray<E>`;
  - `AtomicIntegerFieldUpdater<T>`, `AtomicLongFieldUpdater<T>`, `AtomicReferenceFieldUpdater<T,V>` — reflection-based CAS для volatile полей;
  - `AtomicStampedReference<V>` — с версией (для ABA);
  - `AtomicMarkableReference<V>` — с boolean mark;
  - `DoubleAdder`, `LongAdder`, `LongAccumulator`, `DoubleAccumulator` — для high-contention counters.
- **CAS (Compare-And-Swap):** `compareAndSet(expected, new) → boolean`. Атомарная операция CPU (`cmpxchg` на x86, `ldrex/strex` pair на ARM, `cas` на RISC-V). Если текущее значение == expected, записать new, вернуть true; иначе false.
- **`getAndIncrement`, `incrementAndGet`, `getAndAdd`, `addAndGet`** — реализованы через CAS loop (spin until success).
- **FAA (Fetch-And-Add):** на x86 инструкция `lock xadd`. JDK использует через VarHandle `getAndAdd` → JIT компилирует в `xadd` (не CAS loop) — значительно быстрее.
- **`weakCompareAndSet`:** может ложно возвращать false (spurious failure), не имеет volatile memory effects (на некоторых платформах). На современных JVM совпадает с `compareAndSet`. Использовать для retry-loop без memory ordering требований.
- **ABA problem:** значение меняется A → B → A. CAS думает, что не менялось, и проходит. Пример: lock-free stack pop — между peek A и CAS, другой поток дважды pop/push того же A, но стек другой.
  - Решение: `AtomicStampedReference` (версия инкрементируется на каждое CAS) или hazard pointers / epoch-based reclamation.
- **LongAdder:** `Cell[]` массив (по умолчанию до 4 × CPU). Каждый поток пишет в свою Cell (thread-id hash) → contention dispersion. `sum()` — пробегает по Cell'ам + base. Не точный в моменте (lost updates в flight). Для counters, не для семафоров.
- **LongAccumulator:** arbitrary function `long applyAsLong(long current, long x)`. Например, max.
- **`@Contended`** (`-XX:-RestrictContended` для application code): padding в 128 байт вокруг поля, чтобы избежать false sharing.
- **VarHandle (Java 9+, JEP 193):**
  - Замена `sun.misc.Unsafe` для typed memory access.
  - Получение: `MethodHandles.privateLookupIn(MyClass.class, MethodHandles.lookup()).findVarHandle(MyClass.class, "field", int.class)`.
  - Memory order modes: `PLAIN` (no ordering), `OPAQUE` (visible but no ordering), `RELEASE`/`ACQUIRE` (release on write, acquire on read), `VOLATILE` (total ordering).
  - `get`/`set`, `getVolatile`/`setVolatile`, `getOpaque`/`setOpaque`, `getAcquire`/`setRelease`, `compareAndSet`, `weakCompareAndSet`, `getAndSet`, `getAndAdd`, `getAndBitwiseOr/And/Xor`.
- **`sun.misc.Unsafe`:** deprecated (JDK 23+, JEP 471). Замена — VarHandle / `MemorySegment` (Panama).
- **`lazySet`** (`AtomicInteger.lazySet`): release order без StoreLoad barrier — запись может задержаться, видна позже, но reorder не пересекает. Используется для non-blocking cleanup flags.
- **AtomicReferenceFieldUpdater:** например, `AtomicReferenceFieldUpdater.newUpdater(Node.class, Node.class, "next")`. Не требует создания AtomicReference per field — экономия памяти. Поле должно быть `volatile`.
- **CAS limitation:** только single variable. Multi-variable atomicity → `synchronized` / `ReentrantLock`, либо обернуть несколько значений в один объект и CAS на reference (`AtomicReference<State>`).

### Материалы

- Книга: **"Java Concurrency in Practice"** — глава 15 "Atomic Variables and Nonblocking Synchronization"
- Baeldung (Computer Science) — **"The ABA Problem"** — https://www.baeldung.com/cs/aba-concurrency
- Inside.java / Oracle Java Magazine — **"Method handles: A better way to do Java reflection"** — https://blogs.oracle.com/javamagazine/java-reflection-method-handles
- Nimit Sharma — "Exploring VarHandles: Java's Modern Alternative to Unsafe" — https://nimitsharma.wordpress.com/2025/06/01/exploring-varhandles-javas-modern-alternative-to-unsafe-and-atomic-classes/
- JEP 193: **Variable Handles** — https://openjdk.org/jeps/193
- JEP 471: **Deprecate the Memory-Access Methods in sun.misc.Unsafe for Removal** — https://openjdk.org/jeps/471
- Cracking Walnuts — "Java Atomics: AtomicInteger, AtomicReference, LongAdder" — https://crackingwalnuts.com/concurrency/java-atomics
- Medium — "Techniques for Handling ABA Problems in CAS with Java" — https://medium.com/tuanhdotnet/techniques-for-handling-aba-problems-in-cas-with-java-648fb79f1eb3

### Практика / Кейсы

1. **ABA в lock-free stack.** Treiber stack: `pop` читает head `A`, читает next `B`, CAS(head, A, B). Между чтениями другой поток pop A, pop B, push A (с другим next). CAS проходит, но `next` теперь невалиден → leak, use-after-free. Воспроизвести JCStress, исправить через `AtomicStampedReference<Node>` (version on each CAS).
2. **LongAdder vs AtomicLong benchmark.** 8 потоков, 100M increments each. JMH throughput: AtomicLong ~30 Mops/s, LongAdder ~300 Mops/s на 8 cores. LongAdder — для counters; AtomicLong — для семафоров/CAS state.
3. **Migration Unsafe → VarHandle.** Легаси код `unsafe.compareAndSwapObject(this, offset, exp, upd)`. Переписать: `private static final VarHandle VALUE = ...findVarHandle(...); VALUE.compareAndSet(this, exp, upd)`. Проверить эквивалентность JCStress.
4. **CAS retry storm.** 100 потоков инкрементят AtomicInteger — почти все CAS fail, CPU burn. JCStress показывает ~10% success rate. Решение: `LongAdder` или backoff (`Thread.onSpinWait()` на x86 → `pause`, экономит power + меньше cache contention).
5. **AtomicReferenceFieldUpdater для memory efficiency.** Node в linked list с CAS на `next` поле. Создать `AtomicReferenceFieldUpdater<Node, Node>` (статик, переиспользуется) вместо `AtomicReference<Node>` per node — экономия 16+ байт на node.
6. **`@Contended` против false sharing.** Два `AtomicLong` в одном cache line → каждое CAS инвалидирует cache на другом core. JMH throughput с `@Contended` — 3x.
7. **Lock-free Treiber stack через AtomicReference.** `push(Node n)`: `Node h; do { h = head.get(); n.next = h; } while (!head.compareAndSet(h, n));`. Тест на JMH, сравнить с synchronized stack.
8. **VarHandle acquire/release vs volatile.** Заменить `volatile int state` на `VarHandle.setRelease/getAcquire` — убрать StoreLoad barrier. JCStress подтверждает корректность в producer-consumer, JMH показывает прирост на ARM (на x86 — почти идентично, т.к. volatile уже дешёвый).

---

## 9. Synchronizers

### Теория

- **CountDownLatch:** одноразовый. Конструктор `new CountDownLatch(int count)`. `countDown()` декрементирует, `await()` блокирует до 0, `await(timeout)`. Не reset. Используется для "wait N threads ready" или "wait N services respond".
- **CyclicBarrier:** reset после достижения. `new CyclicBarrier(int parties)` или `new CyclicBarrier(parties, Runnable barrierAction)` — action выполняется потоком, который достиг барьера последним. `await()` блокирует до N потоков, потом все освобождаются, latch сбрасывается. Если любой поток бросает exception или interrupt → barrier broken, все `await` кидают `BrokenBarrierException`.
- **Семантика reset:** CyclicBarrier можно переиспользовать. CountDownLatch — нет.
- **Семантика "один-ко-многим" vs "все-ко-всем":** CountDownLatch — один (или несколько) wait, многие countdown. CyclicBarrier — все wait друг друга.
- **Semaphore:** permits, `acquire()` decrements, `release()` increments. `new Semaphore(n)` non-fair (default), `new Semaphore(n, true)` fair (FIFO). Permit не привязан к потоку (release может сделать другой поток).
- **Semaphore как lock:** `new Semaphore(1)` — аналог mutex, но **не реентерабельный** (повторный acquire тем же потоком → deadlock).
- **Exchanger<V>:** два потока меняются значениями через `exchange(V)`. Когда оба вызвали `exchange`, оба получают значение другого. Используется для producer/consumer buffer swap.
- **Phaser (Java 7):**
  - Гибкий, динамическая регистрация: `register()`, `bulkRegister(n)`, `arriveAndDeregister()`.
  - Фазы: `arrive()`, `arriveAndAwaitAdvance()`, `awaitAdvance(phase)`.
  - Tiering: `new Phaser(parent)` — родитель ждёт детей.
  - Можно переопределить `onAdvance(phase, parties)` для side-effects.
  - Используется для multi-phase simulations, fork-join с динамическим числом parties.
- **AQS shared mode:** Semaphore, CountDownLatch, CyclicBarrier (через ReentrantLock + Condition, не AQS напрямую). `tryAcquireShared`/`tryReleaseShared`. State интерпретируется по-разному.
- **CountDownLatch = AQS shared, state = count.** `tryAcquireShared` возвращает 1 если state == 0.
- **CyclicBarrier Generation:** при broken создаётся new Generation, ждущие освобождаются с `BrokenBarrierException`.
- **Timeout:** `await(timeout)` → `TimeoutException` (CDL), `TimeoutException`/`BrokenBarrierException` (CB).
- **Use cases:**
  - CountDownLatch: инициализация при старте приложения (wait DB + cache + config);
  - CyclicBarrier: batch processing, parallel sorting phases;
  - Semaphore: rate limit, connection pool, login throttle;
  - Exchanger: producer-consumer с pre-allocated buffers;
  - Phaser: fork-join с динамическим числом задач, genetic algorithm generations.

### Материалы

- Книга: **"Java Concurrency in Practice"** — глава 5.5.3 "Barriers", глава 12 "Testing Concurrent Programs" (latch usage)
- Oracle Docs — `java.util.concurrent` Synchronizers — https://docs.oracle.com/en/java/javase/21/docs/api/java.base/java/util/concurrent/package-summary.html
- Baeldung — **"CyclicBarrier in Java"** — https://www.baeldung.com/java-cyclic-barrier
- Baeldung — **"CountDownLatch vs Semaphore"** — https://www.baeldung.com/java-countdown-latch-vs-semaphore
- Baeldung — **"Guide to Phaser"** — https://www.baeldung.com/java-phaser
- Medium — "Choosing Between CountDownLatch, CyclicBarrier, Phaser and Semaphore" — https://medium.com/@japleen.kaur11/java-concurrency-choosing-between-countdownlatch-cyclicbarrier-phaser-and-semaphore-8ab855d570f6
- Jenkov — "Exchanger" — http://tutorials.jenkov.com/java-util-concurrent/exchanger.html

### Практика / Кейсы

1. **Multi-service parallel init.** При старте приложения: parallel load config, warmup cache, DB migration. `CountDownLatch(3)`, каждый worker `latch.countDown()` в finally, main `latch.await(30, SECONDS)`. Если timeout — log warning и продолжить.
2. **CyclicBarrier для batch processing.** Обработка потока по 100 элементов: 4 worker потока берут по 25, после завершения — `barrier.await()`, потом flush buffer. Reusable — следующий batch на тех же threads. Если один worker упал → BrokenBarrierException, все видят, restart.
3. **Semaphore для DB connection pool.** `Semaphore(10)` permits. `acquire()` before checkout, `release()` after close. Fair — для устранения starvation. Throttle down при overload.
4. **Exchanger для buffer swap.** Producer заполняет `Buffer A`, consumer обрабатывает `Buffer B`. Когда producer полон — `Buffer full = exchanger.exchange(A)` (получает B). Symmetric на consumer. Нет allocation per element.
5. **Phaser с динамическим числом workers.** Recursive tree traversal: каждый worker регистрирует детей через `phaser.register()`, потом `phaser.arriveAndAwaitAdvance()`. Удобно, когда число подзадач неизвестно заранее.
6. **Broken barrier recovery.** Один поток в `CyclicBarrier.await` бросает exception → все остальные получают `BrokenBarrierException`. Решение: try-catch + reset barrier, либо restart batch с новыми workers, либо использовать `Phaser` (более устойчив).
7. **Semaphore для throttling HTTP outbound.** Внешний API limit = 50 concurrent. `Semaphore(50)` перед каждым HTTP вызовом. Под нагрузкой 1000 req/s — 950 ждут permits. Время latency растёт → backpressure на upstream.
8. **Migration CountDownLatch → Phaser.** Если нужно reset (multi-round init) — Phaser поддерживает фазы. `CountDownLatch` нельзя переиспользовать.

---

## 10. Reactive Flow API (Publisher/Subscriber/Subscription, backpressure)

### Теория

- **`java.util.concurrent.Flow`** (Java 9+, JEP 266) — встроенная в JDK адаптация Reactive Streams spec (тот же API, что `org.reactivestreams.*`, но в `java.util.concurrent`).
- **4 интерфейса:**
  - `Publisher<T>` — `void subscribe(Subscriber<? super T> subscriber)`;
  - `Subscriber<T>` — `onSubscribe(Subscription)`, `onNext(T)`, `onError(Throwable)`, `onComplete()`;
  - `Subscription` — `request(long n)`, `cancel()`;
  - `Processor<T, R>` extends `Publisher<R>, Subscriber<T>` — intermediate stage.
- **Reactive Streams спецификация** (https://www.reactive-streams.org/) — 38 правил (rules). Java `Flow` полностью совместим по семантике.
- **Backpressure:** subscriber управляет demand через `subscription.request(n)`. Publisher не должен слать больше, чем cumulative request. `request(Long.MAX_VALUE)` — unbounded (эквивалентно no backpressure).
- **Lifecycle:**
  1. `Publisher.subscribe(s)` вызывается;
  2. Publisher вызывает `s.onSubscribe(sub)` — ровно один раз;
  3. Subscriber вызывает `sub.request(n)`;
  4. Publisher шлёт ≤ n элементов через `s.onNext(t)`;
  5. Завершение через `s.onComplete()` или `s.onError(throwable)`.
- **Правило 3:** `request(n)` должно быть > 0 (иначе `IllegalArgumentException` через `onError`).
- **Правило 7:** после `onError`/`onComplete` — никаких больше `onNext`.
- **Hot vs Cold publishers:**
  - Cold — создаёт данные per subscriber (например, HTTP request). Subscribers independent.
  - Hot — shared source (например, sensor stream, market data). Подписчики, пришедшие позже, пропускают предыдущие элементы.
- **`SubmissionPublisher<T>`** — встроенная реализация в JDK. Один publisher → много subscribers. Использует `Executor` для async delivery, `maxBufferCapacity` per subscriber. При переполнении — `onError` с `RejectedExecutionException`-like.
- **Demand overflow:** если subscriber не успевает и buffer переполняется → publisher решает: блокировать, дропать, кидать error.
- **Cancellation:** `Subscription.cancel()` — publisher должен перестать слать. Resources cleanup.
- **Нет операторов в JDK** — `Flow` только спецификация. Для операторов (`map`, `filter`, `merge`, `flatMap`) — используйте **Project Reactor** (`Flux`/`Mono`), **RxJava 3** (`Flowable`/`Observable`), **Mutiny** (`Multi`/`Uni`), **Akka Streams**.
- **Reactive Streams TCK** — test compatibility kit: https://github.com/reactive-streams/reactive-streams-jvm/tree/main/tck
- **Spring WebFlux** — построен на Reactor + Servlet 3.1 async I/O + Netty.
- **Backpressure в HTTP:** нет нативной backpressure в HTTP/1.1 — обычно через bounded queue + 503/429. В gRPC — flow control через HTTP/2 window.
- **NullPointerException контракта:** `subscribe(null)`, `onNext(null)` — `NullPointerException`.
- **Async-non-blocking:** не блокируйте `onNext`. Если нужно blocking I/O — offload в отдельный executor.

### Материалы

- Reactive Streams Spec — https://github.com/reactive-streams/reactive-streams-jvm
- Oracle Docs — `java.util.concurrent.Flow` — https://docs.oracle.com/en/java/javase/21/docs/api/java.base/java/util/concurrent/Flow.html
- JEP 266: **More Concurrency Updates** (reactive flows in JDK) — https://openjdk.org/jeps/266
- Baeldung — **"Java Reactive Streams"** (Flow API overview) — https://www.baeldung.com/java-9-reactive-streams
- InfoQ — "Backpressure in Reactive Systems" — https://blog.frankel.ch/backpressure-reactive-systems/
- Zone84 — "Backpressure in reactive streams: a closer look" — https://zone84.tech/programming/backpressure-in-reactive-streams-a-closer-look/
- Project Reactor docs — https://projectreactor.io/docs/core/release/reference/
- DEV — "Reactive Streams and the Flow API in Java" — https://dev.to/adaumircosta/reactive-streams-and-the-flow-api-in-java-3eg6

### Практика / Кейсы

1. **Custom Publisher с TCK compliance.** Реализовать `Publisher<Integer>`, эмитящий 1..N. Подписка сохраняется в `AtomicReference<Subscription>`. Прогнать reactive-streams-tck `SubscriberWhiteboxVerification` + `PublisherVerification`. Частые ошибки: `onSubscribe` дважды, `onNext` после `onComplete`, `request(0)` без `onError`.
2. **SubmissionPublisher для event bus.** `new SubmissionPublisher<>(executor, 16)`. Подписать 3 subscribers с разной скоростью обработки. После 16 элементов в самом медленном → `onError(RejectedExecutionException)` (default handler). Решение: `bufferOverflowStrategy` (через кастомную реализацию) или drop.
3. **File processing с backpressure.** Publisher читает 1M lines из файла. Subscriber обрабатывает по 100, потом `request(100)` ещё. Publisher должен паузить чтение, когда нет demand. Сравнить с unbounded (`request(Long.MAX_VALUE)`) — последнее вызывает OOM.
4. **Bounded queue subscriber.** Wrap blocking subscriber: очередь `ArrayBlockingQueue(100)`, в `onNext` → `queue.put` (блокирует если full, но `onNext` нельзя блокировать!). Решение: `queue.offer` → если false, не вызывать `request` ещё. Или offload в executor.
5. **Wrap legacy listener API в Publisher.** `interface Listener { void onEvent(E e); }`. Создать publisher, который при subscribe регистрирует listener, передаёт в `onNext`. На `cancel` — deregister. Critical: синхронизация между subscribe и cancel.
6. **Processor chain.** `Processor<Integer, String> p = ...` — map int to string. `Processor` должен правильно проксировать `request` upstream и `cancel` downstream. Сравнить с Reactor `.map(...)`.
7. **Hot publisher multicast.** Publisher, эмитящий независимо от subscribers. Subscribers, пришедшие позже, пропускают старые элементы. Реализация через `SubmissionPublisher` с `bufferOverflowStrategy=DROP`. Сравнить с Reactor `Sinks.multicast()`.
8. **Error handling contract.** `onError` — terminal. Cleanup resources (close file). Не выбрасывать из `onNext`. Catch + relay to `onError` если нужно.

---

## 11. Reflection (продвинуто): dynamic proxies, InvocationHandler

### Теория

- **Core API:** `Class<?>`, `Field`, `Method`, `Constructor`, `Modifier`, `Array`.
- **Получение Class:** `Class.forName(name)` (load + init), `MyClass.class` (no init), `obj.getClass()`.
- **Methods:** `getDeclaredMethod(name, types...)` (any declared in this class, including private), `getMethod` (public, including inherited).
- **`setAccessible(true)`:** bypass access checks (private, package-private). На JDK 9+ в module system — `InaccessibleObjectException` если модуль не opens.
- **`Constructor.newInstance(args...)` vs `Class.newInstance()`** (deprecated с Java 9): новый — корректно обрабатывает checked exceptions, поддерживает generics.
- **`Method.invoke(obj, args...)`** — varargs; бросает `InvocationTargetException` (wrap target exception). Раскрыть через `getCause()`.
- **Reflective cost:** first call — поиск, security checks, JIT медленно оптимизирует. После warmup — JIT инлайнит, но всё равно ~5x slower прямого вызова.
- **`Array.newInstance(componentType, length)`, `Array.get/set`** для reflection над arrays.
- **Dynamic proxy:** `Proxy.newProxyInstance(ClassLoader, Class<?>[] interfaces, InvocationHandler)`. Создаёт класс в runtime, реализующий все интерфейсы.
- **`InvocationHandler.invoke(proxy, method, args)`:** каждый вызов метода прокси проходит через `invoke`. Можно логировать, кэшировать, делегировать.
- **Restrictions:**
  - Только interfaces (можно несколько). Не class (для subclass proxy — CGLIB/ByteBuddy).
  - Default методы — нужно явно вызывать `Method.invoke` на прокси или через `InvocationHandler.invokeDefault` (Java 16+).
- **MethodHandle (Java 7+):** быстрее reflection после JIT, typed. `MethodHandles.lookup().findVirtual(Class, name, MethodType)`.
- **`invokeExact` vs `invoke`:** `invokeExact` требует точных типов ((signature-polymorphic), иначе `WrongMethodTypeException`. `invoke` добавляет auto-cast — медленнее.
- **`LambdaMetafactory`** (Java 8+): стандартный путь компиляции лямбд — `invokedynamic` + `LambdaMetafactory.metafactory`. JIT-компилятор создаёт класс в runtime.
- **Bytecode libs:**
  - **CGLIB** — устаревший, не поддерживает Java 16+ (issuer: `sun.misc.Unsafe::defineClass` удалён).
  - **ByteBuddy** — modern, активная поддержка, поддерживает Java 21+, используется в Mockito, Spring AOT, Hibernate.
  - **Javassist** — runtime bytecode manipulation.
- **Hidden classes (Java 15, JEP 371):** классы, не видные через reflection (`Class.forName`), не загружаются через classloader. Используются JIT для lambdas, dynamic codegen. Memory-efficient — JLT может выгружать.
- **Records:** `Class.getRecordComponents()` — массив `RecordComponent`. Canonical constructor + accessor methods.
- **Sealed classes:** `Class.getPermittedSubclasses()`.
- **Annotation processing** — см. отдельный раздел (13). Annotation processor работает compile-time, не runtime reflection.
- **Reflection + modules:**
  - `--add-opens java.base/java.util=ALL-UNNAMED` — открыть пакет для reflective access.
  - `--add-exports java.base/sun.nio.ch=ALL-UNNAMED` — compile-time + runtime.
  - Без флагов на JDK 17+ — `IllegalAccessError` / `InaccessibleObjectException`.

### Материалы

- Oracle Tutorial — **"Reflection"** — https://docs.oracle.com/javase/tutorial/reflect/
- Oracle Docs — **"Dynamic Proxy Classes"** — https://docs.oracle.com/en/java/javase/21/docs/api/java.base/java/lang/reflect/Proxy.html
- Baeldung — **"Dynamic Proxies in Java"** — https://www.baeldung.com/java-dynamic-proxies
- DZone — **"Java Dynamic Proxies"** — https://dzone.com/articles/java-dynamic-proxies
- Baeldung — **"ByteBuddy Tutorial"** — https://www.baeldung.com/bytebuddy
- Oracle Java Magazine — **"Method handles: A better way to do Java reflection"** — https://blogs.oracle.com/javamagazine/java-reflection-method-handles
- JEP 371: **Hidden Classes** — https://openjdk.org/jeps/371
- VUB lecture — "Metaprogramming & Reflection in Java" — http://soft.vub.ac.be/~pcostanz/documents/csse10/04-meta-java.pdf

### Практика / Кейсы

1. **Dynamic proxy для logging.** `Service s = (Service) Proxy.newProxyInstance(loader, new Class[]{Service.class}, (p, m, a) -> { long t = nanoTime(); Object r = m.invoke(target, a); log.info("{} took {} ns", m.getName(), nanoTime() - t); return r; });`. Сравнить overhead с прямым вызовом — JMH.
2. **Reflection в Spring `@Autowired` field injection.** Spring использует reflection для private field. Migrate to constructor injection — reflection не нужен, быстрее start. Замерить startup time до/после.
3. **Annotation processor для `@Builder` codegen.** Compile-time генерация Builder класса. processor сканирует `@Builder` на классе, генерирует `XBuilder` с fluent API. Тест через `compile-testing`. Сравнить с Lombok @Builder (runtime vs compile-time).
4. **ByteBuddy для AOP.** Subclass proxy с `MethodDelegation.to(interceptor)`. Interceptor — `@RuntimeType public Object intercept(@Origin Method m, @SuperCall Callable<?> z) throws Exception { ... }`. Замерить overhead vs прямого вызова.
5. **LambdaMetafactory hand-craft.** Сгенерировать `Function<Integer, Integer>` через `LambdaMetafactory.metafactory` без lambda syntax. Сравнить JMH с reflection `Method.invoke` — порядок быстрее.
6. **Module reflective access без `--add-opens`.** На JDK 17+ `setAccessible(true)` на private поле `java.util.ArrayList` → `InaccessibleObjectException`. Добавить `--add-opens=java.base/java.util=ALL-UNNAMED`. Логировать warning при reflection на internal API.
7. **Hidden class для hot path.** Динамически сгенерировать класс через `Lookup.defineHiddenClass` (ByteBuddy) — JIT-компиляция, не виден в heap dump class list. Используется lambdas.
8. **CGLIB vs ByteBuddy на Java 21.** CGLIB падает — `UnsupportedOperationException` от `Unsafe::defineClass`. Перевести на ByteBuddy `subclassStrategy`, сохранить API.

---

## 12. Method Handles & invokedynamic

### Теория

- **MethodHandle** (`java.lang.invoke.MethodHandle`) — typed direct pointer to method/field. Не проходит security checks каждый вызов (как reflection), проверка типов один раз при создании.
- **`MethodHandles.lookup()`** — `Lookup` object с правами вызывающего контекста. Только методы, доступные caller, можно искать.
- **Lookup methods:**
  - `findVirtual(Class, name, MethodType)` — instance method (non-private, non-static);
  - `findStatic(Class, name, MethodType)`;
  - `findSpecial(Class, name, MethodType, specialCaller)` — для `invokespecial` semantics (super-calls, private);
  - `findConstructor(Class, MethodType)`;
  - `findGetter/findSetter(Class, name, Class<?>)` — field accessors;
  - `findVirtual` для interface — default methods.
- **`MethodType`** — return type + parameter types (например `MethodType.methodType(String.class, int.class, int.class)`). Immutable, interned.
- **`invokeExact(args...)`:** signature-polymorphic; типы аргументов должны точно совпадать с `MethodType`, иначе `WrongMethodTypeException`. Самый быстрый.
- **`invoke(args...)`:** auto-cast через `asType`, медленнее.
- **`asType(MethodType)`:** адаптация типов (boxing, widening, casting).
- **`bindTo(value)`:** partial application — фиксирует первый аргумент (this для virtual).
- **Combinators:**
  - `filterArguments(target, pos, filter...)` — фильтрует аргументы перед target;
  - `filterReturnValue(target, filter)` — фильтрует результат;
  - `foldArguments(target, folder)` — применяет folder к аргументам, добавляет результат как первый аргумент target;
  - `guardWithTest(test, target, fallback)` — branching (if test true → target else fallback);
  - `catchException(target, exType, handler)` — try-catch;
  - `permuteArguments(target, newType, reorder...)` — переставить аргументы;
  - `collectArguments` / `spreadArguments` — varargs;
  - `dropArguments` — игнорировать аргументы;
  - `constant(type, value)` / `identity(type)` — elementary.
- **`invokedynamic` bytecode** (Java 7, JSR 292):
  - Стек: не ссылается на конкретный метод, а на **bootstrap method (BSM)** в constant pool `CONSTANT_InvokeDynamic`.
  - При первом выполнении JVM вызывает BSM: `bsm(lookup, name, type, ...optionalArgs) → CallSite`.
  - `CallSite` хранит `MethodHandle` — target. Дальнейшие вызовы идут напрямую через target.
- **Типы CallSite:**
  - `ConstantCallSite` — fixed after first link, never changes;
  - `MutableCallSite` — `setTarget` можно менять (sync via `syncAll`);
  - `VolatileCallSite` — volatile target.
- **`LambdaMetafactory.metafactory`** — BSM для lambda expressions и method references. Лямбда `() -> 42` компилируется в `invokedynamic` `LambdaMetafactory.metafactory`, BSM создаёт класс (через hidden class) реализующий функциональный интерфейс.
- **String concatenation (Java 9+, JEP 280):** `+` для строк компилируется в `invokedynamic` `makeConcatWithConstants` — BSM создаёт optimized builder. Заменяет `StringBuilder.append` chain.
- **Pattern matching for switch (Java 21, JEP 441):** `switch` с типами компилируется в `invokedynamic` `typeSwitch` — BSM создаёт dispatch table.
- **Records (Java 16):** accessor methods — обычные, но canonical constructor может использовать invokedynamic для validation.
- **Comparison reflection vs MethodHandle:**
  - Reflection: проверка доступа каждый вызов, slower, не JIT-friendly.
  - MethodHandle: проверка один раз в lookup, JIT inline, после warmup ~ native call.
- **`privateLookupIn(Class, lookup)`** (Java 9): получить lookup с правами другого класса (для frameworks). Защищено module access checks.

### Материалы

- Книга: **"Java Performance"** Scott Oaks — глава про invokedynamic
- OpenJDK Wiki — **"Method handles and invokedynamic"** — https://wiki.openjdk.org/display/HotSpot/Method+handles+and+invokedynamic
- Baeldung — **"An Introduction to Invoke Dynamic in the JVM"** — https://www.baeldung.com/java-invoke-dynamic
- InfoQ — **"Invokedynamic - Java's Secret Weapon"** — https://www.infoq.com/articles/Invokedynamic-Javas-secret-weapon/
- Oracle Java Magazine — **"Method handles: A better way to do Java reflection"** — https://blogs.oracle.com/javamagazine/java-reflection-method-handles
- JEP 160: **Lambda-Form Representation for Method Handles** — https://openjdk.org/jeps/160
- JEP 276: **Dynamic Linking of Language-Defined Call Sites** — https://openjdk.org/jeps/276
- Aleksey Shipilëv — "Lambda Anatomy" — https://shipilev.net/jvm/anatomy-quark/14-constant-entropy/

### Практика / Кейсы

1. **Лямбда через invokedynamic.** Написать `Runnable r = () -> System.out.println("hi");`. `javap -v -p` — видеть `invokedynamic` с BSM `LambdaMetafactory.metafactory`. Написать эквивалент вручную через `LambdaMetafactory.metafactory(lookup, "run", MethodType.methodType(Runnable.class), MethodType.methodType(void.class), mh, MethodType.methodType(void.class))`. Сравнить байткод.
2. **MH vs reflection benchmark.** `Method.invoke` 100M вызовов vs `MethodHandle.invokeExact`. JMH: MH быстрее в 5–10 раз после warmup (JIT inline), reflection страдает от security checks.
3. **Custom invokedynamic.** Написать BSM через `CallSite` и подключить к своему классу с помощью ASM/ByteBuddy. Простой logger: первый вызов — создавать target, потом никогда не меняться (ConstantCallSite).
4. **String concat benchmark.** Сравнить `String.format`, `+`, `StringBuilder`, `makeConcatWithConstants`. JMH: `+` на Java 9+ сравним с StringBuilder (invokedynamic), `String.format` в 5x медленнее (regex parsing format).
5. **Pattern matching switch bytecode.** На Java 21 `switch(o) { case Integer i -> ...; case String s -> ...; }`. `javap -v` — видеть `invokedynamic` `typeSwitch`. Понять BSM, который создаёт dispatch.
6. **Guard with test для dispatch.** Реализовать polymorphic call через `guardWithTest(test, target, fallback)`. `test` проверяет тип, `target` для конкретного типа, `fallback` — generic. Сравнить с `if instanceof` chain.
7. **`privateLookupIn` для framework.** Library хочет deep reflection в user classes. На JDK 17+ обычный `setAccessible` падает. Использовать `MethodHandles.privateLookupIn(targetClass, lookup)` — работает если target class открывает пакет (через `--add-opens` или `opens` в module-info).
8. **MutableCallSite для hot-swap.** Logger с динамическим уровнем: `MutableCallSite` хранит target — либо `log`, либо `noop`. При смене уровня — `setTarget` + `syncAll` для visibility. Эффективнее чем `if (level >= threshold)` на каждый вызов.

---

## 13. Annotations processing (annotation processor, генерация кода)

### Теория

- **javax.annotation.processing** (JSR 269, Java 6+). Annotation processor работает **compile-time**, не требует runtime reflection.
- **`Processor` interface** — основной; `AbstractProcessor` — базовый класс.
- **Аннотации на процессоре:**
  - `@SupportedAnnotationTypes({"com.example.MyAnnotation"})` — какие аннотации обрабатывать;
  - `@SupportedSourceVersion(SourceVersion.RELEASE_21)` — версия;
  - `@AutoService` (Google) — генерирует `META-INF/services/javax.annotation.processing.Processor`.
- **Жизненный цикл:**
  - `init(ProcessingEnvironment env)` — один раз; получить `Filer`, `Messager`, `Elements`, `Types`;
  - `process(Set<? extends TypeElement> annotations, RoundEnvironment roundEnv)` — вызывается для каждого раунда;
  - `roundEnv.processingOver()` — true в последнем раунде.
- **Rounds:** компилятор запускает процессоры несколько раз. Сначала исходные файлы, потом сгенерированные (из предыдущего раунда), и так пока не будет новых. Последний раунд — `processingOver=true`.
- **Element hierarchy** (compile-time model):
  - `PackageElement`, `TypeElement` (class/interface), `ExecutableElement` (method/constructor), `VariableElement` (field/param/local), `TypeParameterElement`;
  - `Element.getEnclosingElement()`, `getEnclosedElements()`.
- **TypeMirror** — тип: `DeclaredType` (class), `ArrayType`, `PrimitiveType`, `TypeVariable`, `WildcardType`, `NoType`, `ExecutableType`.
- **`Types`** utility: `isSameType`, `isAssignable`, `erasure`, `boxedClass`.
- **`Elements`** utility: `getTypeElement(name)`, `getAllMembers(type)`, `getDocComment(element)`.
- **`Filer`** — write source/class/resource files:
  - `createSourceFile(name, originatingElements)` — генерирует `.java` файл;
  - `createClassFile(name, originatingElements)` — генерирует `.class` (через bytecode lib вроде ByteBuddy/ASM);
  - `createResource(...)`.
- **`Messager`** — diagnostics: `printMessage(Kind.ERROR, msg, element)` — ошибка компиляции; `Kind.WARNING`, `Kind.NOTE`.
- **Service registration:** `META-INF/services/javax.annotation.processing.Processor` — текстовый файл с FQN классов. Или `@AutoService(Processor.class)` (Google Auto).
- **Build integration:**
  - Maven: `maven-compiler-plugin` с `<annotationProcessors>` или auto-discovery через classpath;
  - Gradle: `annotationProcessor "..."` для compile-only deps;
  - Gradle: `kapt` для Kotlin, `ksp` для Kotlin Symbol Processing.
- **Annotation processors в ecosystem:**
  - **Lombok** — НЕ стандартный AP; hack через internal javac API (`com.sun.tools.javac.tree.JCTree`) для AST modification. Ломается на новых Java (workarounds через `--add-opens`).
  - **MapStruct** — генерирует mapper реализации (BeanUtils.clone экономит reflection в runtime).
  - **Dagger 2** — DI кодогенерация, без reflection в runtime (vs Spring/Guice).
  - **AutoValue** (Google) — immutable value classes.
  - **Immutables** — annotation-driven codegen.
  - **Hibernate JPAModelGen** — type-safe metamodel для Criteria API.
  - **Google AutoService** — генерация `META-INF/services`.
- **Records / sealed:** с Java 16+ можно генерировать код, использующий records как DTO. Sealed classes — exhaustive switch в generated code.
- **Debugging processor:** `-J-Xdebug -J-Xrunjdwp:transport=dt_socket,server=y,address=5005` + IDE attach.
- **compile-testing** (Google) — библиотека для тестирования processors: `assert_().about(javaSource()).that(source).processedWith(processor).compilesWithoutError().and().generatesSources(expected)`.
- **Performance:** processors добавляют к compile time. На больших кодбазах — секунды. Parallel compilation mitigates.

### Материалы

- Oracle Tutorial — **"Annotation Processing"** — https://docs.oracle.com/en/java/javase/21/docs/api/java.compiler/javax/annotation/processing/package-summary.html
- Hannes Dorfmann — **"Write an annotation processor"** — https://hannesdorfmann.com/annotation-processing/annotationprocessing101
- Baeldung — **"Java Annotation Processing"** — https://www.baeldung.com/java-annotation-processing
- Google Compile Testing — https://github.com/google/compile-testing
- Google AutoService — https://github.com/google/auto/tree/main/service
- MapStruct docs — https://mapstruct.org/documentation/reference-guide/
- Immutables — https://immutables.github.io/
- Lombok internals — https://projectlombok.org/contributing/

### Практика / Кейсы

1. **`@Builder` processor.** Аннотация `@GenerateBuilder` на классе. Processor сканирует поля, генерирует `XBuilder` с fluent setters + `build()`. Тест через `compile-testing`: компилирует класс с аннотацией, проверяет что сгенерирован Builder, что `build()` создаёт объект с правильными полями.
2. **MapStruct migration.** Заменить ручные mappers (Entity → DTO) на MapStruct. Сравнить производительность: MapStruct ~0 reflection, эквивалент ручному. Type-safe: при изменении поля — compile-time error.
3. **Lombok @Data — AST hack.** Понять что Lombok нестандартный: модифицирует AST javac. Почему ломается на новых Java: `--add-opens jdk.compiler/com.sun.tools.javac.tree=ALL-UNNAMED`. Альтернативы: Records (Java 16+) для immutable DTO + IDE templates для boilerplate.
4. **AutoValue vs Records.** Records (Java 16+) покрывают 80% случаев AutoValue. AutoValue добавляет: `Builder`, `withX` (with pattern), custom `toString`. Для backwards compat / extension — AutoValue, для новых — Records.
5. **Compile-time validation.** `@NonEmpty` на String field — processor в `process()` проверяет что в коде все literal-инициализации непусты, иначе `printMessage(ERROR, "...", element)`. Compile-fail если нарушено.
6. **Auto-generate `META-INF/services`.** `@AutoService(MyService.class)` на классе — processor генерирует `META-INF/services/com.example.MyService` с FQN класса. Без ручного редактирования.
7. **Round env infinite loop.** Processor генерирует класс с той же аннотацией → следующий round триггерит генерацию ещё одного класса → ... → компилятор limit. Решение: в `process()` проверять `roundEnv.getElementsAnnotatedWith(...)` только для исходных (не сгенерированных) — например, по package name prefix.
8. **Debug processor.** `mvnDebug` + IDE remote debug на порт 5005. Breakpoint в `process()`, inspect `RoundEnvironment`, `Element` tree.

---

## 14. JNI, JNA, Panama FFM (Java 22)

### Теория

- **JNI (Java Native Interface):** стандарт с Java 1.1. Позволяет вызывать C/C++ код из Java.
  - `javac -h` (или `javah` до Java 10) генерирует C header с сигнатурами;
  - C код `#include <jni.h>`, реализует функции;
  - Компиляция в `.so` (Linux) / `.dll` (Windows) / `.dylib` (macOS);
  - `System.loadLibrary("mylib")` загружает;
  - `native` методы в Java.
- **JNIEnv pointer** — per-thread context. `(*env)->FindClass(env, "java/lang/String")`, `(*env)->NewObjectArray(env, len, cls, init)`, etc.
- **Local vs Global refs:** `NewLocalRef` (auto-freed at return), `NewGlobalRef` (must `DeleteGlobalRef`). GC не двигает нативные refs.
- **Critical regions:** `GetPrimitiveArrayCritical(env, arr, &isCopy)` — возвращает direct pointer, **нельзя блокировать** (GC паузится в GC mode `GC locker`). Block I/O → deadlock GC.
- **JNA (Java Native Access):** динамический binding через `libffi`. Без написания C кода. Декларируешь `interface MyLib extends Library { int printf(String fmt, ...); }`, `Native.load("c", MyLib.class)`.
  - Pros: быстро разрабатывать, без C компиляции;
  - Cons: overhead per call (reflection + libffi indirection), в 5–10x медленнее JNI на small calls.
- **JNA structures:** Java класс extends `Structure`, поля с правильным order matching C struct. `Structure.getByValue` / `Structure.ByReference`.
- **JNA callbacks:** `Callback` interface, `Native.getCallbackOptions`.
- **Panama Project (Foreign Function & Memory API, FFM):**
  - JEP 419 (Java 17 preview) → JEP 424 (Java 18 preview) → JEP 442 (Java 19 preview) → JEP 454 (Java 22 final).
  - `java.lang.foreign` package.
  - Замена JNI: проще, безопаснее, быстрее.
- **Linker:** `Linker.nativeLinker()` — default для текущей платформы.
- **SymbolLookup:**
  - `Linker.defaultLookup()` — стандартные symbols (libc);
  - `SymbolLookup.libraryLookup("c", Arena)` — explicit library;
  - `SymbolLookup.loaderLookup()` — из текущего classloader.
- **Function descriptor:** `FunctionDescriptor.of(return layout, arg layouts...)`. Layouts — `ValueLayout.JAVA_INT`, `JAVA_LONG`, `JAVA_DOUBLE`, `ADDRESS`, etc.
- **MemorySegment:** bounded off-heap memory. Created by `Arena.allocate(size)` or `MemorySegment.ofArray(byte[])` (on-heap view).
- **Arena:**
  - `Arena.ofConfined()` — single thread (fastest, no sync);
  - `Arena.ofShared()` — multi-thread, sync overhead;
  - `Arena.ofAuto()` — auto-freed when GC sees no references (Cleaner);
  - `Arena.global()` — process lifetime.
- **Memory layout:**
  - `ValueLayout` — primitives (JAVA_INT, JAVA_LONG, JAVA_DOUBLE, ADDRESS);
  - `SequenceLayout` — array;
  - `StructLayout` — C struct, `MemoryLayout.structLayout(...)`;
  - `UnionLayout` — C union.
- **VarHandle from layout:** `layout.varHandle()` — typed access. `seg.set(handle, 0L, 42); int v = seg.get(handle, 0L);`.
- **Downcall:** `Linker.downcallHandle(address, functionDesc)` → `MethodHandle`, потом `invoke` / `invokeExact`.
- **Upcall:** `Linker.upcallStub(methodHandle, functionDesc, arena)` → `MemorySegment` (function pointer), передаётся в C как callback.
- **Critical linker:** `Linker.Option.critical(true)` — fast path без safepoint, но функция должна быть short и non-blocking.
- **Performance:** FFM ~1.5x медленнее direct C call, на порядок быстрее JNI на small calls (no JNIEnv overhead). Near-direct.
- **Native memory safety:** MemorySegment проверяет bounds → `IndexOutOfBoundsException` вместо segfault. Lifetime проверяется → `IllegalStateException` при доступе к freed segment.
- **JNI deprecation (JEP 472):** future restriction на JNI + Unsafe memory methods, FFM — recommended replacement.

### Материалы

- Oracle Tutorial — **JNI** — https://docs.oracle.com/en/java/javase/21/docs/specs/jni/index.html
- JNA on GitHub — https://github.com/java-native-access/jna
- JEP 454: **Foreign Function & Memory API** (final Java 22) — https://openjdk.org/jeps/454
- JEP 472: **Prepare for Restricting JNI** — https://openjdk.org/jeps/472
- Oracle Docs — **Foreign Function and Memory API** — https://docs.oracle.com/en/java/javase/22/core/foreign-function-and-memory-api.html
- Inside.java — **"Foreign Function & Memory API - A peek under the hood"** — https://inside.java/2024/02/13/fosdem2024-ffm-api/
- IBM Developer — **"From JNI to FFM"** — https://developer.ibm.com/articles/j-ffm/
- JNA tutorial — https://www.java-success.com/jna-tutorial/

### Практика / Кейсы

1. **JNI tutorial: вызвать `printf`.** Java класс с `private native void sayHello();`. `javac -h .` генерирует `.h`. C файл реализует `JNIEXPORT void JNICALL Java_Hello_sayHello(JNIEnv *env, jobject obj) { printf("Hello from C!\n"); }`. Скомпилировать `gcc -shared -fPIC -I$JAVA_HOME/include -I$JAVA_HOME/include/linux hello.c -o libhello.so`. `System.loadLibrary("hello")` + `-Djava.library.path=.`
2. **Critical region deadlock.** `GetPrimitiveArrayCritical` + blocking I/O (read from socket) внутри → GC locker ждёт release, другие потоки не могут alloc. Воспроизвести, диагностировать через `jcmd Thread.print`, исправить — освободить critical region до I/O.
3. **JNA vs JNI benchmark.** Вызвать `strlen` 10M раз. JNA — ~50 ns/call (reflection + libffi), JNI — ~5 ns/call (direct), FFM — ~3 ns/call. JMH тест.
4. **Panama FFM вызов `printf`.** `Linker linker = Linker.nativeLinker(); SymbolLookup c = linker.defaultLookup(); MemorySegment addr = c.find("printf").orElseThrow(); FunctionDescriptor desc = FunctionDescriptor.of(ValueLayout.JAVA_INT, ValueLayout.ADDRESS); MethodHandle printf = linker.downcallHandle(addr, desc); try (Arena arena = Arena.ofConfined()) { MemorySegment fmt = arena.allocateUtf8String("Hello %s!\n"); MemorySegment arg = arena.allocateUtf8String("Panama"); printf.invoke(fmt, arg); }`. Сравнить с JNI по сложности кода.
5. **Off-heap buffer через MemorySegment.** `Arena shared = Arena.ofShared(); MemorySegment buf = shared.allocate(1024);` → передаётся между потоками, bounded (проверка bounds), freed when `shared.close()`. Заменяет `ByteBuffer.allocateDirect` + Cleaner.
6. **JNI global ref leak.** `NewGlobalRef` без `DeleteGlobalRef` в callback → leak. `jcmd GC.class_histogram` не показывает native memory, но NMT (`-XX:NativeMemoryTracking=summary`) показывает рост "Internal" section. MAT не помогает — native. Профиль через Valgrind на C side.
7. **FFM struct access.** C struct `struct Point { int x; int y; }`. `StructLayout pointLayout = MemoryLayout.structLayout(ValueLayout.JAVA_INT.withName("x"), ValueLayout.JAVA_INT.withName("y")); VarHandle x = pointLayout.varHandle(MemoryLayout.PathElement.groupElement("x")); MemorySegment seg = arena.allocate(pointLayout); seg.set(x, 0L, 42);` Сравнить throughput с JNI GetIntField.
8. **Migration JNI → FFM в библиотеке.** Сохранить Java API совместимым, заменить native impl на FFM. Тесты на совместимость. Убрать `System.loadLibrary`, добавить automatic module descriptor.

---

## 15. JPMS modules (module-info, exports/requires/opens, jlink)

### Теория

- **Java Platform Module System (JPMS, JSR 376, Java 9, JEP 261):** формальная модульность для платформы и приложений.
- **`module-info.java`:**
  ```
  module com.example.app {
      requires com.example.lib;
      requires transitive com.example.api;
      requires static lombok;
      exports com.example.app;
      exports com.example.app.internal to com.example.test;
      opens com.example.app.entity to org.hibernate.orm.core;
      uses com.example.app.Service;
      provides com.example.app.Service with com.example.app.DefaultService;
  }
  ```
- **Directives:**
  - `requires M` — compile + runtime dependency;
  - `requires transitive M` — публичный API зависит от M, все dependants тоже implicitly require M;
  - `requires static M` — compile-only (optional at runtime);
  - `exports P` — пакет P публично доступен;
  - `exports P to M1, M2` — qualified export, только этим модулям;
  - `opens P` — package open для reflection (frameworks: Spring, Hibernate);
  - `opens P to M` — qualified open;
  - `uses S` — ServiceLoader consumer;
  - `provides S with Impl` — ServiceLoader provider;
- **Module path vs classpath:**
  - На module path — JAR с `module-info.class` → named module; без — automatic module (имя из `Automatic-Module-Name` в MANIFEST или filename).
  - На classpath — unnamed module (видит все, к нему можно обратиться через reflection).
- **Split package:** один package в нескольких модулях запрещён — compile/runtime error.
- **Cyclic dependency запрещена:** A requires B, B requires A — error.
- **Reflective access:** на JDK 9–16 — `--illegal-access=permit` (default 9), `warn` (default 16), `deny` (default 17+, removed flag).
  - `--add-opens M/P=ALL-UNNAMED` — открыть package P модуля M для unnamed (classpath);
  - `--add-exports M/P=ALL-UNNAMED`;
  - `--add-reads M1=M2`.
- **`jdeps`:**
  - `jdeps -s myapp.jar` — summary зависимостей;
  - `jdeps --generate-module-info ./dir myapp.jar` — создать черновик `module-info.java`;
  - `jdeps --list-deps`, `--list-reduced-deps` — какие JDK модули нужны;
  - `jdeps --multi-release base` — для multi-release JAR.
- **`jlink`:** создание кастомного runtime image:
  ```
  jlink --module-path $JAVA_HOME/jmods:./mods \
        --add-modules com.example.app \
        --launcher app=com.example.app/com.example.app.Main \
        --strip-debug --compress=zip-6 \
        --output ./myapp-runtime
  ```
  - Уменьшает размер: только нужные модули + native libs;
  - `--strip-debug` — убрать debug info;
  - `--compress=zip-6` (или 0–2 в новых версиях) — компрессия;
  - `--include-locales en,ru` — только нужные локали;
  - `--no-header-files`, `--no-man-pages`.
- **`jmod` format:** `jmods/` в JDK — module binaries (нельзя положить на classpath, только module path). Аналог JAR для модулей + native libs + config files.
- **`jpackage` (Java 16+, JEP 392):** создать platform-specific installer (`.dmg`, `.deb`, `.rpm`, `.msi`, `.exe`).
- **ModuleLayer:** runtime graph модулей. Можно создавать child layers с дополнительными модулями (dynamic load).
- **ServiceLoader + modules:** `uses`/`provides` декларации, service lookup работает через module layer.
- **Migration strategies:**
  - Big-bang migration редко работает;
  - gradual: сначала unnamed (classpath), потом automatic modules, потом named;
  - libraries: добавить `Automatic-Module-Name` в MANIFEST для стабильного имени модуля (без full module-info).
- **Encapsulation benefits:** strong encapsulation — internal packages не видны извне без `exports`. Безопасность + clear API surface.
- **Drawbacks:** complexity для libraries, reflection breaks (Spring/Hibernate/Lombok need `--add-opens`), многие libraries не имеют `module-info`.

### Материалы

- Oracle Tutorial — **"Modules"** — https://docs.oracle.com/javase/tutorial/java/package/index.html (Module section)
- JEP 261: **Module System** — https://openjdk.org/jeps/261
- JEP 392: **Packaging Tool (jpackage)** — https://openjdk.org/jeps/392
- Baeldung — **"Guide to the Java Module System"** — https://www.baeldung.com/java-9-modularity
- Inside.java — **"Module System"** — https://inside.java/jvm/modules/
- AlgoMaster.io — `module-info.java` — https://algomaster.io/learn/java/module-info
- Onyxwizard — "Java Modules: From Zero to Mastery" — https://dev.to/onyxwizard/java-modules-from-zero-to-mastery-59h6
- OpenJDK Project Jigsaw — https://openjdk.org/projects/jigsaw/

### Практика / Кейсы

1. **Spring Boot app в modules.** `jdeps` показывает зависимости. Spring требует `opens` для reflection на entity classes. `--add-opens java.base/java.lang=ALL-UNNAMED` для Lombok. Начать с automatic modules для всех libs, мигрировать постепенно.
2. **`jlink` custom runtime.** CLI приложение: `jlink --module-path $JAVA_HOME/jmods:./build/mods --add-modules com.example.app --launcher app=com.example.app/com.example.app.Main --strip-debug --compress=2 --output ./dist`. Размер: 200 MB JRE → 45 MB custom. Startup time уменьшается.
3. **Split package bug.** Два JAR содержат `com.example.util`. На classpath — работало (последний выигрывает). На module path — error "Module reads package X from both module A and module B". Решение: переименовать один package, или объединить JAR.
4. **Service provider module.** Module `api` декларирует `uses Service`; module `impl` декларирует `provides Service with Impl`. `ServiceLoader.load(Service.class)` находит. Тест через multi-module проект.
5. **Reflective access в JDK 17.** Hibernate хочет private field в `java.util.LinkedHashMap`. Без `--add-opens=java.base/java.util=ALL-UNNAMED` → `InaccessibleObjectException`. Spring Boot добавляет `JAVA_TOOL_OPTIONS` автоматически, но в production — прописать в JVM args.
6. **`jdeps --generate-module-info` для legacy JAR.** Сгенерировать черновик, допилить руками — `requires transitive` для public API dependencies, `requires static` для optional. Тестировать.
7. **Cyclic dependency fix.** Module A requires B, B requires A → error. Рефакторинг через interface segregation: выделить интерфейс в module C, оба A и B require C.
8. **`jpackage` для desktop app.** `jpackage --type dmg --name MyApp --module-path ./build/mods --module com.example.app/com.example.app.Main --runtime-image ./dist`. Создаёт `.dmg` installer для macOS, включает JRE.

---

## 16. Security: JCA/JCE, MessageDigest, Cipher, KeyStore, SSL/TLS

### Теория

- **JCA (Java Cryptography Architecture)** — `java.security` package: `Provider`, `MessageDigest`, `Signature`, `KeyStore`, `SecureRandom`, `AlgorithmParameters`, `KeyFactory`, `CertificateFactory`.
- **JCE (Java Cryptography Extension)** — `javax.crypto` package: `Cipher`, `Mac`, `KeyGenerator`, `SecretKeyFactory`, `KeyAgreement`, `CipherInputStream/OutputStream`. Исторически отдельный download из-за US export controls, с Java 9 встроено.
- **Provider architecture:** плагин-провайдеры. Default в JDK: `SUN` (digests, signatures), `SunRsaSign`, `SunJCE` (cipher, mac), `SunEC` (EC), `SunPKCS11` (HSM/PKCS#11). Внешние: **BouncyCastle** (FIPS), **Conscrypt** (Google, OpenSSL-backed).
  - `Security.getProviders()` — список;
  - `Security.addProvider(new BouncyCastleProvider())` или через `java.security` config.
- **`MessageDigest`:**
  - `MessageDigest.getInstance("SHA-256")` (или `SHA-1`, `SHA-512`, `SHA3-256`, `MD5` — не использовать для security);
  - `digest.update(byte[])` несколько раз, потом `digest()` → 32 байта для SHA-256;
  - thread-safety: НЕ thread-safe, синхронизировать или new instance per thread.
- **`SecureRandom`:** криптостойкий RNG. Реализации:
  - `NativePRNG` (default Linux, читает `/dev/urandom`);
  - `SHA1PRNG`;
  - `DRBG` (Java 9+, NIST SP 800-90A) — `SecureRandom.getInstance("DRBG")`;
  - `/dev/random` может блокировать на entropy lack — `securerandom.source=file:/dev/urandom` в `java.security`.
- **`Cipher`:**
  - `Cipher.getInstance("AES/GCM/NoPadding")` — algorithm/mode/padding;
  - `init(ENCRYPT_MODE, key, ivSpec)`;
  - `doFinal(plaintext)` → ciphertext.
  - Modes:
    - `ECB` — НЕ использовать, leaks pattern;
    - `CBC` — vulnerable to padding oracle (unless MAC'd);
    - `GCM` — authenticated encryption, recommended;
    - `CTR` — для streaming;
  - Padding: `PKCS5Padding` (CBC), `NoPadding` (GCM).
- **IV (Initialization Vector):** для GCM — 12 bytes, random, prepend to ciphertext. Для CBC — 16 bytes.
- **GCM tag:** 128 bit по умолчанию, можно указать 96/104/112/120/128.
- **`KeyStore`:**
  - Types: `JKS` (legacy, deprecated), `PKCS12` (default с Java 9), `Windows-MY` (Windows cert store), `PKCS11` (HSM).
  - `KeyStore.getInstance("PKCS12")`, `load(InputStream, char[] password)`.
  - `keytool` CLI: `keytool -genkeypair -alias myalias -keyalg RSA -keysize 2048 -validity 365 -keystore my.p12`.
- **Keys:**
  - `SecretKey` — symmetric (AES, HmacSHA256); `KeyGenerator.getInstance("AES").generateKey()`;
  - `PublicKey` / `PrivateKey` — asymmetric; `KeyPairGenerator.getInstance("RSA")`, `.generateKeyPair()`.
  - Algorithms: `RSA` (2048+ bits), `EC` (ECC, 256+ bits), `Ed25519` / `Ed448` (Java 15+, JEP 339, modern), `X25519` / `X448` (key agreement).
- **`Signature`:** sign/verify. `Signature.getInstance("SHA256withRSA")`, `Ed25519`, `RSASSA-PSS`.
- **JSSE (Java Secure Socket Extension)** — `javax.net.ssl`:
  - `SSLSocket`, `SSLServerSocket`;
  - `SSLEngine` — non-blocking TLS (for Netty, NIO);
  - `SSLContext.getInstance("TLS")` → `init(KeyManager[], TrustManager[], SecureRandom)`;
  - `TLS 1.3` (Java 11+, JEP 332) — faster handshake, removed legacy crypto;
  - `SSLParameters` — set protocols, cipher suites.
- **trustStore vs keyStore:**
  - keyStore — your private key + cert (for server presenting identity);
  - trustStore — CA certs you trust (for verifying peer).
  - System properties: `-Djavax.net.ssl.keyStore=...`, `-Djavax.net.ssl.trustStore=...`.
- **mTLS (mutual TLS):** server также проверяет клиентский cert. `SSLContext` с `KeyManager` (client cert) + `TrustManager` (verify server + server verifies client).
- **Certificate path validation:** `CertPathValidator`, `PKIXParameters` — chain to trusted root, OCSP / CRL checks.
- **`SecurityManager`** deprecated в JDK 17 (JEP 411), removal planned. Использовать OS-level security (containers, OS users).
- **Permissions** (`java.security.Permission`, `AllPermission`, `FilePermission`, `SocketPermission`) — связаны с SecurityManager, deprecated вместе.
- **OWASP crypto guidelines:** use GCM/ChaCha20-Poly1305, never ECB, random IV per encryption, constant-time comparisons (`MessageDigest.isEqual`).
- **`CipherInputStream` / `CipherOutputStream`:** stream encryption.

### Материалы

- Oracle Docs — **JCA Reference Guide** — https://docs.oracle.com/en/java/javase/21/security/java-cryptography-architecture-jca-reference-guide.html
- Oracle Docs — **Java Secure Socket Extension (JSSE) Reference Guide** — https://docs.oracle.com/en/java/javase/21/security/java-secure-socket-extension-jsse-reference-guide.html
- Baeldung — **"Java KeyStore"** — https://www.baeldung.com/java-keystore
- Baeldung — **"AES Encryption and Decryption"** — https://www.baeldung.com/java-aes-encryption-decryption
- Baeldung — **"Introduction to SSL in Java"** — https://www.baeldung.com/java-ssl
- JEP 339: **EdDSA (Ed25519, Ed448)** — https://openjdk.org/jeps/339
- JEP 332: **TLS 1.3** — https://openjdk.org/jeps/332
- OWASP — **"Cryptographic Storage Cheat Sheet"** — https://cheatsheetseries.owasp.org/cheatsheets/Cryptographic_Storage_Cheat_Sheet.html

### Практика / Кейсы

1. **AES-GCM encrypt/decrypt.** `KeyGenerator.getInstance("AES").init(256).generateKey()` → SecretKey. `Cipher.getInstance("AES/GCM/NoPadding")`. IV = `SecureRandom` 12 bytes, GCMParameterSpec(128, iv). Encrypt → prepend IV. Decrypt → split IV + ciphertext. Test round-trip + tampering detection (GCM tag fails).
2. **TLS 1.3 server с mutual TLS.** `SSLContext.getInstance("TLS")`. KeyManager from server keystore (server cert), TrustManager from truststore (client CA). `SSLParameters.setNeedClientAuth(true)`. Test with `openssl s_client -connect host:port -key client.pem`.
3. **Password hashing.** PBKDF2: `SecretKeyFactory.getInstance("PBKDF2WithHmacSHA256")`. `PBEKeySpec(password, salt, 600000, 256)`. Salt = 16 bytes random. Store salt + hash. Verify: re-hash with same salt, `MessageDigest.isEqual` (constant-time). Alternatives: Argon2 через BouncyCastle, bcrypt через Spring Security.
4. **JWT sign/verify.** RS256: `Signature.getInstance("SHA256withRSA")`. EdDSA (modern): `Signature.getInstance("Ed25519")` + `KeyPairGenerator.getInstance("Ed25519")`. Compare performance RS256 vs Ed25519 — Ed25519 быстрее sign и verify, короче signature.
5. **JKS → PKCS12 migration.** `keytool -importkeystore -srckeystore old.jks -srcstoretype JKS -destkeystore new.p12 -deststoretype PKCS12`. Test with `-list` на обоих. Update app config to use PKCS12.
6. **BouncyCastle provider.** `Security.addProvider(new BouncyCastleProvider())`. `MessageDigest.getInstance("SHA3-256", "BC")`. SHA-3 support до Java 17 был через BC; с Java 17 — встроено в SunEC.
7. **Self-signed cert для dev.** `keytool -genkeypair -keyalg EC -keysize 256 -sigalg SHA256withECDSA -validity 365 -alias dev -keystore dev.p12 -storetype PKCS12 -ext SAN=dns:localhost`. Import в truststore client. Configure Spring Boot `server.ssl.*`.
8. **HSM via PKCS#11.** Configure `SunPKCS11` provider via `pkcs11.cfg` (`name = SoftHSM\nlibrary = /usr/lib/softhsm/libsofthsm2.so`). `KeyStore.getInstance("PKCS11")`. Generate keys inside HSM — private key never leaves. Test sign/verify.

---

## 17. NIO.2 (Path/Files/FileSystem, AsynchronousFileChannel, Selector non-blocking IO)

### Теория

- **`java.nio`** (Java 4) — buffers + channels; **NIO.2** (Java 7, JEP 51) — `Path`, `Files`, `FileSystem`, `AsynchronousFileChannel`, `WatchService`, directory traversal.
- **`Path`:** заменяет `File`. Immutable.
  - `Path.of("a", "b", "c.txt")` или `Paths.get("a/b/c.txt")` (старый);
  - `resolve(other)` — конкатенация;
  - `relativize(other)` — относительный путь;
  - `normalize()` — убрать `.` и `..`;
  - `toAbsolutePath()`, `toRealPath()`;
  - `getParent()`, `getFileName()`, `getRoot()`, `subpath()`.
- **`Files`:**
  - `readAllBytes(path)` — весь файл в `byte[]`;
  - `readString(path, charset)` (Java 11+) — строка;
  - `write(path, bytes, options...)`;
  - `lines(path, charset)` → `Stream<String>` (lazy, через `BufferedReader`);
  - `copy(src, dst, StandardCopyOption.REPLACE_EXISTING, COPY_ATTRIBUTES)`;
  - `move(src, dst, ATOMIC_MOVE)`;
  - `delete(path)`, `deleteIfExists`;
  - `exists`, `isDirectory`, `isRegularFile`, `size`, `getLastModifiedTime`;
  - `walk(start, maxDepth, options...)` → `Stream<Path>` lazy;
  - `find(start, maxDepth, BiPredicate, options...)` — с фильтром;
  - `newBufferedReader`, `newBufferedWriter`, `newInputStream`, `newOutputStream`;
  - `readAttributes(path, BasicFileAttributes.class)`, `PosixFileAttributes`, `DosFileAttributes`;
  - `setAttribute`, `getOwner`, `setPosixFilePermissions`.
- **`FileSystem`** — абстракция над FS. Default = OS. Zip/Jar — через `FileSystems.newFileSystem(zipPath, (ClassLoader) null)` (Java 7+, в Java 9+ упрощён).
- **`FileChannel`:**
  - позиционный `read(buf, position)`, `write(buf, position)`;
  - `transferTo(position, count, target)` — zero-copy `sendfile` на Linux;
  - `map(MapMode.READ_WRITE, position, size)` → `MappedByteBuffer` (memory-mapped file).
- **`MappedByteBuffer`:**
  - Файл маппится в адресное пространство процесса;
  - Read/write через memory — OS paging управляет I/O;
  - Для больших файлов — эффективно;
  - `force()` flush dirty pages;
  - Caveat: `unmap()` нет public API — ждать GC + Cleaner (проблема для очень больших maps).
- **`AsynchronousFileChannel`** (Java 7):
  - `open(path, options, executor, attrs)`;
  - `read(buf, position, attachment, CompletionHandler)` — callback;
  - `read(buf, position)` → `Future<Integer>`;
  - Альтернатива callback hell — обернуть в `CompletableFuture`.
- **`Selector`** — non-blocking I/O для network sockets (TCP/UDP), **не для файлов** (file I/O всегда блокирует в Java).
  - `Selector.open()`;
  - `SocketChannel.configureBlocking(false)`;
  - `channel.register(selector, SelectionKey.OP_READ | SelectionKey.OP_WRITE)`;
  - `selector.select(timeout)` — блокирует до ready key;
  - `selector.selectedKeys()` — set, итерировать, **удалить из set** после обработки;
  - `SelectionKey.interestOps()` — обновить интерес;
  - `OP_ACCEPT`, `OP_CONNECT`, `OP_READ`, `OP_WRITE`.
- **Selector loop pattern:**
  ```
  while (true) {
      selector.select(1000);
      Iterator<SelectionKey> it = selector.selectedKeys().iterator();
      while (it.hasNext()) {
          SelectionKey k = it.next();
          it.remove();
          if (k.isAcceptable()) accept(k);
          if (k.isReadable()) read(k);
          if (k.isWritable()) write(k);
      }
  }
  ```
- **`ByteBuffer`:** `allocate(capacity)` (heap), `allocateDirect(capacity)` (off-heap, быстрее I/O). `flip()` — switch from write to read mode (limit=pos, pos=0). `compact()` — shift unread bytes to start. `clear()` — pos=0, limit=capacity. `rewind()` — pos=0, limit unchanged.
- **`Charset`:** `StandardCharsets.UTF_8` — default с Java 18 (`-Dfile.encoding=UTF-8` always UTF-8 on Linux/macOS, on Windows still depends).
- **`PathMatcher`:** glob syntax — `**` для recursive, `*` для single-level, `{a,b}`, `[abc]`.
- **`WatchService`:** мониторинг изменений в директории. `WatchKey key = dir.register(service, ENTRY_CREATE, ENTRY_MODIFY, ENTRY_DELETE)`. `service.take()` блокирует. Caveat: events могут дублироваться, не на всех FS работает (macOS broken historically).
- **`FileVisitor<T>`:** `walkFileTree(start, visitor)` — preVisitDirectory, visitFile, visitFileFailed, postVisitDirectory.
- **`SeekableByteChannel`:** random access через `Files.newByteChannel(path, READ, WRITE)`.

### Материалы

- Oracle Tutorial — **"NIO.2"** — https://docs.oracle.com/javase/tutorial/essential/io/fileio.html
- Jenkov — **"Java NIO Tutorial"** — http://tutorials.jenkov.com/java-nio/index.html
- Baeldung — **"Java NIO2 Path API"** — https://www.baeldung.com/java-nio-2-path-api
- Baeldung — **"Guide to ByteBuffer"** — https://www.baeldung.com/java-bytebuffer
- Oracle Docs — `java.nio.channels` — https://docs.oracle.com/en/java/javase/21/docs/api/java.base/java/nio/channels/package-summary.html
- Martin Thompson — **"Mechanical Sympathy"** blog — https://mechanical-sympathy.blogspot.com/
- Aleksey Shipilëv — "NIO.2 performance" — https://shipilev.net/
- DZone — "Java NIO.2 Asynchronous Channels" — https://dzone.com/articles/java-nio2-asynchronous-channels

### Практика / Кейсы

1. **Non-blocking echo server через Selector.** `ServerSocketChannel.open()` + `Selector`. Accept → register OP_READ. Read → register OP_WRITE (если buf full). Write → switch back to OP_READ. Benchmark concurrent connections 10K — single thread, low CPU.
2. **Async log writer через `AsynchronousFileChannel` + `CompletableFuture`.** Wrap `read(buf, pos)` future → thenApply parse → thenAccept process. Avoid callback hell. Test throughput: 1M lines written in parallel.
3. **`MappedByteBuffer` для 10 GB файла.** Map entire file (или chunks по 1 GB для очень больших). Sequential read/write — OS paging быстрее `RandomAccessFile`. Caveat: close без unmap — memory занята до GC. Решение: chunks + try-with-resources + `Cleaner`.
4. **`walkFileTree` для backup.** `FileVisitor` копирует файлы с сохранением attributes (`COPY_ATTRIBUTES`), пропускает `.git`, обрабатывает errors в `visitFileFailed`. Sym links — `FileVisitOption.FOLLOW_LINKS` (осторожно с циклами).
5. **ZipFileSystem.** `FileSystems.newFileSystem(Path.of("lib.jar"), (ClassLoader) null)`. Read entries через `Files.walk`. Modify (добавить файл) — `Files.copy` в zip path. Test round-trip.
6. **`WatchService` для hot reload.** Config file change → reload. `service.take()` → process events → debounce (events дублируются, особенно на Windows). 100 ms debounce timer. Restart watcher если key reset false.
7. **`transferTo` zero-copy для HTTP static files.** `FileChannel.transferTo(0, size, socketChannel)` — на Linux использует `sendfile(2)`, без copy через user space. Benchmark vs `read`+`write` loop — ~30% быстрее, меньше CPU.
8. **`PathMatcher` glob.** `FileSystems.getDefault().getPathMatcher("glob:**/*.java")`. Recursive поиск по проекту. Сравнить с `Files.walk().filter(p -> p.toString().endsWith(".java"))` — `PathMatcher` корректнее на разных OS (разделители).

---

## 18. Modern Java 9–21

### Теория

- **Java 9 (2017, JEP 261):**
  - JPMS modules;
  - JShell (REPL);
  - Private interface methods;
  - `Collection.of(...)`, `List.of`, `Map.of` — immutable factories;
  - `Stream.takeWhile`, `dropWhile`, `ofNullable`;
  - `Stream.iterate(seed, hasNext, next)`;
  - `CompletableFuture.orTimeout`, `completeOnTimeout`;
  - `ProcessHandle`, `Process.pid()`;
  - `StackWalker` (lazy stack trace);
  - Reactive Streams (`Flow` API).
- **Java 10 (2018, JEP 286):**
  - `var` — local type inference (только local variables, не fields/params/return type);
  - `List.copyOf`, `Set.copyOf`, `Map.copyOf` — immutable copies;
  - `Optional.orElseThrow()` (без аргумента) — `NoSuchElementException`;
  - `Collectors.toUnmodifiableList/Set/Map`;
  - Immutable snapshot factories in collections;
  - Heap allocation on alternative memory devices (NVDIMM);
  - JIT compiler hints G1 (default GC).
- **Java 11 LTS (2018, JEP 323):**
  - `var` in lambda parameters (`(var x, var y) -> ...`);
  - `HttpClient` (standard, JEP 321) — HTTP/2 + WebSocket;
  - `String.lines()` → `Stream<String>`, `strip()`, `stripLeading/Trailing()`, `repeat(n)`, `isBlank()`;
  - `Files.readString`, `writeString`;
  - `Collection.toArray(IntFunction)` — typed array;
  - `Pattern.asMatchPredicate` (Java 11);
  - `Optional.isEmpty`;
  - Nest-based access control (JEP 181);
  - TLS 1.3 (JEP 332);
  - ZGC experimental (JEP 333);
  - Epsilon GC (JEP 318).
- **Java 12 (2019):**
  - Switch expressions (preview, JEP 325);
  - `String.transform(Function)`, `indent(int)`;
  - `Collectors.teeing(downstream1, downstream2, merger)`;
  - `CompactNumberFormat` (`NumberFormat.getCompactNumberInstance`);
  - G1 garbage collector enhancements (JEP 344 — abortable mixed collections, JEP 346 — return unused memory).
- **Java 13 (2019):**
  - Text blocks preview (JEP 355);
  - `String.stripIndent()`, `formatted(Object...)`;
  - Reimplement legacy Socket API (NioSocketImpl).
- **Java 14 (2020):**
  - Switch expressions final (JEP 361);
  - Pattern matching for `instanceof` preview (JEP 305);
  - Records preview (JEP 359);
  - Helpful NullPointerException (JEP 358, `-XX:+ShowCodeDetailsInExceptionMessages`);
  - Packaging tool `jpackage` (incubating);
  - ZGC on macOS, Windows.
- **Java 15 (2020):**
  - Text blocks final (JEP 378);
  - Sealed classes preview (JEP 360);
  - Hidden classes (JEP 371);
  - ZGC + Shenandoah production (JEP 377, JEP 379);
  - Biased locking disabled (JEP 374);
  - Edwards-CurveDSA (EdDSA, JEP 339).
- **Java 16 (2021):**
  - Records final (JEP 395);
  - Pattern matching for `instanceof` final (JEP 394);
  - Sealed classes second preview (JEP 397);
  - `jpackage` final (JEP 392);
  - Vector API incubator (JEP 338);
  - Unix-domain sockets (`SocketChannel`, `ServerSocketChannel`).
- **Java 17 LTS (2021):**
  - Sealed classes final (JEP 409);
  - Pattern matching for `instanceof` final (already in 16);
  - Enhanced pseudo-random (`RandomGenerator`, `RandomGeneratorFactory` — L128X1024Mix, Xoshiro256++);
  - macOS aarch64 (Apple Silicon) port (JEP 391);
  - Removal of SecurityManager (JEP 411 — deprecation);
  - Strong encapsulation by default (JEP 396, illegal-access denied).
- **Java 18 (2022):**
  - Simple Web Server (`com.sun.net.httpserver`, JEP 408);
  - UTF-8 default charset (JEP 400);
  - Code snippets in Javadoc (`@snippet`, JEP 413);
  - Pattern matching for switch second preview (JEP 420);
  - Vector API third incubator.
- **Java 19 (2022):**
  - Virtual threads preview (JEP 425);
  - Structured concurrency incubator (JEP 428);
  - Pattern matching for switch preview (JEP 427);
  - Record patterns preview (JEP 405);
  - Foreign Function & Memory API preview (JEP 424);
  - Vector API fifth incubator.
- **Java 20 (2023):**
  - Virtual threads second preview (JEP 436);
  - Scoped values preview (JEP 429);
  - Structured concurrency preview (JEP 437);
  - Record patterns second preview (JEP 432);
  - Pattern matching for switch fourth preview (JEP 433);
  - FFM API preview (JEP 442).
- **Java 21 LTS (2023):**
  - **Virtual threads final (JEP 444)** — без `Thread.toVirtual` (отказались), без `StructuredTaskScope` (ушёл в preview);
  - **Sequenced Collections (JEP 431)** — `SequencedCollection`, `SequencedSet`, `SequencedMap`; `addFirst`, `addLast`, `getFirst`, `getLast`, `removeFirst`, `removeLast`, `reversed()`;
  - **Pattern matching for switch final (JEP 441)** — `case Integer i when i > 0 -> ...; case null -> ...; case default -> ...;` exhaustiveness with sealed;
  - **Record patterns final (JEP 440)** — `case Point(int x, int y) -> ...`;
  - **String Templates preview (JEP 430)** — `STR."Hello \{name}!"` (отозвано в Java 23 для redesign);
  - **Unnamed patterns and variables preview (JEP 443)** — `_` для игнорируемых;
  - **Unnamed classes and instance main methods preview (JEP 445)** — упрощённый entry point для обучения;
  - **Generational ZGC (JEP 439)** — major;
  - **Foreign Function & Memory API third preview (JEP 442)**.
- **Java 22 (2024):**
  - **Foreign Function & Memory API final (JEP 454)** — без preview;
  - **Unnamed variables & patterns final (JEP 456)**;
  - **Statements before `super()` (JEP 447)** preview;
  - **Stream Gatherers (JEP 461)** preview;
  - **String Templates removed** (JEP 465, redesign).
- **`var`** — local only, не fields/params/return type, не null без инициализатора, не массив с `[]` в левой части.
- **Text blocks:**
  ```
  String json = """
          {"name": "%s", "age": %d}
          """.formatted(name, age);
  ```
  Incidental whitespace stripping; `\` escapes newline (line continuation); `\s` trailing space.
- **Switch expression + yield + exhaustiveness:**
  ```
  int len = switch (s) {
      case null -> -1;
      case String x when x.isEmpty() -> 0;
      case String x -> x.length();
  };
  ```
  Exhaustiveness обязательна для switch expression (или `default`, или sealed с покрытием всех permitted).
- **Pattern matching `instanceof`:** `if (obj instanceof String s && s.length() > 5) { ... use s ... }`. Scope — extends to right of `&&`, into then-block.
- **Records:**
  ```
  record Point(int x, int y) {}
  ```
  Implicit: final class, private final fields, canonical constructor, accessors `x()`, `y()`, `equals`/`hashCode`/`toString`. Compact constructor для validation: `record Range(int lo, int hi) { public Range { if (lo > hi) throw ...; } }`. Не может extends, может implements.
- **Sealed classes:**
  ```
  sealed interface Shape permits Circle, Square, Triangle {}
  record Circle(double r) implements Shape {}
  record Square(double s) implements Shape {}
  non-sealed class Triangle implements Shape {}  // open
  ```
  Exhaustive switch без `default`.
- **Sequenced Collections:** `LinkedHashMap` теперь `SequencedMap` с `reversed()` → reversed view, `putFirst`/`putLast`. `LinkedHashSet` — `SequencedSet`. `List` — `SequencedCollection`.
- **Structured Concurrency (preview в 21, 25 final):**
  ```
  try (var scope = new StructuredTaskScope.ShutdownOnFailure()) {
      Future<User> userF = scope.fork(() -> findUser());
      Future<Order> orderF = scope.fork(() -> findOrder());
      scope.join();          // wait all
      scope.throwIfFailed(); // propagate first exception
      return new Response(userF.resultNow(), orderF.resultNow());
  }
  ```
- **Scoped Values (preview):**
  ```
  static final ScopedValue<User> CURRENT_USER = ScopedValue.newInstance();
  ScopedValue.where(CURRENT_USER, user).run(() -> service.handle());
  ```
  Immutable, bounded to the run() scope. Better than ThreadLocal для virtual threads.

### Материалы

- Oracle Docs — **"Java Language Changes"** (21) — https://docs.oracle.com/en/java/javase/21/language/java-language-changes.html
- JEP 444: **Virtual Threads** — https://openjdk.org/jeps/444
- JEP 441: **Pattern Matching for Switch** — https://openjdk.org/jeps/441
- JEP 440: **Record Patterns** — https://openjdk.org/jeps/440
- JEP 431: **Sequenced Collections** — https://openjdk.org/jeps/431
- JEP 395: **Records** — https://openjdk.org/jeps/395
- JEP 409: **Sealed Classes** — https://openjdk.org/jeps/409
- OpenJDK — все JEPs — https://openjdk.org/jeps/0
- Inside.java — "Java 21 launch" articles — https://inside.java/2023/09/25/java21/

### Практика / Кейсы

1. **Migration Java 8 → 21.** Шаги: проверить compatibility (jdeps --multi-release), обновить dependencies, добавить `--add-opens` для reflection (Spring/Lombok), переключить GC на G1/ZGC, протестировать на staging. Проблемы: удалённые API (`sun.misc.Unsafe`), JPMS strict encapsulation.
2. **Records vs Lombok @Value.** Заменить DTO классы на records. Преимущества: меньше boilerplate, native serialization. Ограничения: immutable only, no inheritance. Для mutable DTO — оставить классы.
3. **Pattern matching switch + sealed для state machine.**
   ```
   sealed interface State permits Idle, Running, Error {}
   record Idle() implements State {}
   record Running(long startedAt) implements State {}
   record Error(String msg) implements State {}
   
   String describe(State s) {
       return switch (s) {
           case Idle i -> "idle";
           case Running r -> "running since " + r.startedAt();
           case Error e -> "error: " + e.msg();
       };
  // no default — exhaustive
   }
   ```
   При добавлении нового `permits` — compile-time ошибка в switch.
4. **Sequenced Collections refactoring.** Заменить `Collections.reverse(list); list.get(0);` на `list.getLast()`. `LinkedHashMap` — `sequencedKeySet().reversed()` для обратной итерации.
5. **Text blocks для SQL/JSON.** Заменить `"SELECT * FROM users\nWHERE id = ?"` на `"""\n    SELECT * FROM users\n    WHERE id = ?\n    """`. Interpolation: `STR."Hello \{name}, you are \{age}!"` (preview, Java 21; removed в 23 — ждать redesign). Без templates — `block.formatted(name, age)`.
6. **StructuredTaskScope для parallel API calls.** 3 microservice calls параллельно; если любой fail — отменить остальные. `ShutdownOnFailure` — exactly эта семантика. До Java 21 — `CompletableFuture.allOf` + manual cancel.
7. **ScopedValue для request context.** Replace `ThreadLocal<User>`. `ScopedValue.where(CURRENT_USER, user).run(() -> controller.handle(request))`. Inside nested virtual threads — value inherited, but bounded to `run()`. Memory: не сохраняется в ThreadLocalMap.
8. **Helpful NPE.** Включить `-XX:+ShowCodeDetailsInExceptionMessages` (default on JDK 15+). NPE теперь показывает: "Cannot invoke 'String.length()' because 'name' is null". Overhead <1%.

---

## 19. JVM диагностика (jstack/jstat/jmap/jcmd, VisualVM, JFR/JMC, async-profiler, Arthas, MAT)

### Теория

- **`jps`** — список Java процессов. `jps -lvm` — с full package name и args.
- **`jstack`** — thread dump. `jstack -l <pid>` — с ownable synchronizers и deadlocks. `jstack -F <pid>` — force (если процесс не отвечает). Альтернатива: `jcmd <pid> Thread.print`.
- **`jstat`** — GC stats. `jstat -gcutil <pid> 1000` — каждые 1 sec, показать utilization по generations. `jstat -gccause` — причина последнего GC. Deprecated в пользу `jcmd GC.class_histogram`.
- **`jmap`** — heap histogram, dump. `jmap -histo:live <pid>` — гистограмма с live objects (triggers GC). `jmap -dump:format=b,file=dump.hprof <pid>` — heap dump. Deprecated, использовать `jcmd`.
- **`jcmd`** — унифицированная команда, заменяет jstack/jstat/jmap/jinfo. `jcmd <pid> help` — список команд. Примеры:
  - `jcmd <pid> VM.version`
  - `jcmd <pid> VM.system_properties`
  - `jcmd <pid> VM.flags`
  - `jcmd <pid> GC.heap_info`
  - `jcmd <pid> GC.class_histogram`
  - `jcmd <pid> Thread.print`
  - `jcmd <pid> GC.heap_dump /tmp/dump.hprof`
  - `jcmd <pid> JFR.start duration=60s filename=/tmp/rec.jfr`
  - `jcmd <pid> Compiler.queue`
  - `jcmd <pid> VM.classloader_stats`
  - `jcmd <pid> VM.native_memory summary`
  - `jcmd <pid> Thread.dump_to_file -format=json /tmp/threads.json`
  - `jcmd <pid> ManagementAgent.start jmx.port=7091`
- **JFR (Java Flight Recorder)** — event-based profiling, low overhead (<1%):
  - Built-in events: GC, allocation, monitor contention, file I/O, socket I/O, class loading, thread dump;
  - `jcmd <pid> JFR.start duration=300s filename=/tmp/rec.jfr settings=profile`;
  - Custom events через `jdk.jfr.Event` + `@Name`, `@Category`, `@Threshold`;
  - Settings: `default` (low overhead), `profile` (more events).
- **JMC (JDK Mission Control)** — GUI для анализа JFR:
  - Flame graph;
  - Allocation pressure;
  - Lock contention;
  - GC paз;
  - Hot methods;
  - I/O;
  - Download: https://www.oracle.com/java/technologies/jdk-mission-control.html (или Adoptium JDK Mission Control).
- **VisualVM** — UI: heap dump, thread dump, CPU profiler, MBeans. Бесплатно. Устарел по сравнению с JFR + async-profiler. Download: https://visualvm.github.io/.
- **async-profiler** — sampling CPU/heap profiler без safepoint bias:
  - GitHub: https://github.com/async-profiler/async-profiler;
  - На Linux: `perf_events` или Asynchronous Get Call Trace (AGCT);
  - На macOS: mach `backtrace`;
  - `./asprof -d 60 -f flame.html <pid>` — 60 sec CPU profile, flame graph SVG;
  - `-e alloc` — allocation profile;
  - `-e lock` — lock contention;
  - `-e cache-misses` — hardware counter;
  - `-e itimer` — fallback если perf_events недоступен;
  - Heap profiling: `--live` для live objects only.
- **MAT (Eclipse Memory Analyzer Tool)** — анализ heap dumps (`.hprof`):
  - **Histogram** — по классам, shallow/retained size;
  - **Dominator Tree** — иерархия dominance;
  - **Leak Suspects Report** — автоматический анализ утечек;
  - **Path to GC Roots** — путь до root;
  - **OQL (Object Query Language)** — SQL-like для объектов;
  - **Thread Overview** — stacks в момент dump;
  - **Custom Queries** — extension points.
- **Arthas** (Alibaba) — interactive диагностика running JVM без restart:
  - `dashboard` — overview;
  - `thread` — thread dump, `thread <id>` — детали;
  - `trace ClassName method` — дерево вызовов с временем;
  - `watch ClassName method params+return` — наблюдение в реальном времени;
  - `jad ClassName` — декомпиляция loaded class;
  - `mc` — Memory Compiler, `redefine` — hot-swap class;
  - `profiler start/stop` — async-profiler интегрирован;
  - GitHub: https://github.com/alibaba/arthas.
- **GC logging (Java 9+):** `-Xlog:gc*:file=/var/log/app/gc.log:time,uptime,level,tags:filecount=10,filesize=100M`. Tags: `gc`, `gc+heap`, `gc+task`, `safepoint`. `-Xlog:help` — список.
- **gceasy.io** — онлайн-анализатор GC логов. p99 pause, throughput, alloc rate, recommendations.
- **Heap dump on OOM:** `-XX:+HeapDumpOnOutOfMemoryError -XX:HeapDumpPath=/var/dumps/`. С auto-rotate через filename pattern.
- **Native Memory Tracking (NMT):** `-XX:NativeMemoryTracking=summary` (или `detail`). `jcmd <pid> VM.native_memory summary`. Показывает: Java heap, class (Metaspace), thread, internal, GC, compiler, symbol, native memory. Baseline: `jcmd <pid> VM.native_memory baseline`, diff: `diff`.
- **JMX (Java Management Extensions):**
  - `MBeanServer`, MBeans;
  - Platform MBeans: `MemoryMXBean`, `ThreadMXBean`, `GarbageCollectorMXBean`, `RuntimeMXBean`, `OperatingSystemMXBean`;
  - Доступ: `ManagementFactory.getPlatformMBeanServer()`;
  - Remote: `-Dcom.sun.management.jmxremote.port=7091 -Dcom.sun.management.jmxremote.authenticate=false -Dcom.sun.management.jmxremote.ssl=false`.
- **`SIGQUIT` (kill -3)** — печатает thread dump в stdout JVM (HotSpot).
- **`-XX:+UnlockDiagnosticVMOptions -XX:+PrintFlagsFinal`** — все JVM flags с текущими значениями.
- **`arthas boot`** — `java -jar arthas-boot.jar`, выбирает PID.

### Материалы

- Oracle Docs — **"JDK Diagnostic Tools"** — https://docs.oracle.com/en/java/javase/21/troubleshoot/diagnostic-tools.html
- OpenJDK — **JFR / JMC documentation** — https://docs.oracle.com/en/java/javase/21/jdk-flight-recorder/
- async-profiler GitHub — https://github.com/async-profiler/async-profiler
- Baeldung — **"A Guide to async-profiler"** — https://www.baeldung.com/java-async-profiler
- Baeldung — **"A Guide to Java Profilers"** — https://www.baeldung.com/java-profilers
- Arthas GitHub + docs — https://github.com/alibaba/arthas
- Eclipse MAT — https://eclipse.dev/mat/
- Inside.java — "JFR articles" — https://inside.java/jmc/

### Практика / Кейсы

1. **CPU 100% на проде.** `top -Hp <pid>` — найти hotspot thread (TID в hex). `jstack <pid> | grep -A 30 <hex_tid>` — что делает поток. Если thread в user code — `async-profiler -d 30 -f cpu.html <pid>` → flame graph → hot method. Профиль показать какая строчка. Fix: оптимизация алгоритма, кэш.
2. **OutOfMemoryError: Java heap space.** Heap dump через `-XX:+HeapDumpOnOutOfMemoryError`. MAT → Leak Suspects → "Problem Suspect 1: `java.util.HashMap` retained 4 GB". Dominator Tree → static field cache. Fix: bounded cache (Caffeine `maximumSize(10000)`), eviction.
3. **GC pause spike на проде.** GC log (`-Xlog:gc*`), загрузить в gceasy.io → p99 = 3.5 sec на Full GC Parallel. Решение: migration на G1 (`-XX:+UseG1GC`), tuning `MaxGCPauseMillis`, IHOP. Замерить после — p99 < 200 ms.
4. **Lock contention.** JFR 60 sec: `jcmd <pid> JFR.start duration=60s filename=rec.jfr settings=profile`. Открыть в JMC → "Lock Instances" — топ contended locks. Найти monitor с high contention. Refactor: striped locks (`Striped<Lock>`), `LongAdder`, lock-free data structures.
5. **Allocation rate profiling.** `async-profiler -d 60 -e alloc -f alloc.html <pid>` → flame graph по allocation. Найти hot allocation (например, `StringBuilder` в tight loop). Fix: `StringBuilder` reuse, primitive specialization, `VarHandle`.
6. **Native memory leak.** RSS растёт, heap стабилен. `-XX:NativeMemoryTracking=summary`, baseline: `jcmd <pid> VM.native_memory baseline`. Через час: `jcmd <pid> VM.native_memory summary.diff`. Видно рост "Internal" или "Thread" section. Direct buffers? Thread leak? `jcmd <pid> Thread.print` — посмотреть число потоков. Direct buffer: `ManagementFactory.getPlatformMBeanServer().getAttribute(BufferPoolMXBean, "MemoryUsed")`.
7. **Classloader leak после Tomcat redeploy.** `jcmd <pid> VM.classloader_stats` — найти hung WebappClassLoader. Heap dump → MAT → "Path to GC Roots → exclude weak/soft references" — найти static ref или ThreadLocal. Fix: cleanup в `contextDestroyed`.
8. **Slow startup analysis.** JFR startup profile: `jcmd <pid> JFR.start duration=120s filename=startup.jfr settings=profile`. Открыть в JMC → "Events" → найти blocking I/O в init (например, DNS lookup, JDBC connect). Использовать Class Data Sharing (CDS): `-Xshare:dump` → `-Xshare:on`. AppCDS для application classes. Spring AOT для нативной инициализации. Startup 30s → 5s.

---

## Сводка

**19 подразделов** полностью покрывают Java Advanced: от внутренностей JVM и Java Memory Model до modern Java 21 (virtual threads, pattern matching, records, sealed, scoped values), concurrency primitives (locks, atomics, synchronizers, reactive Flow), низкоуровневых механизмов (method handles, invokedynamic, JNI/Panama FFM), модульности (JPMS), криптографии (JCA/JCE/JSSE), NIO.2 и инструментов диагностики.

**Ключевые книги:**
1. Brian Goetz et al. — "Java Concurrency in Practice"
2. Scott Oaks — "Java Performance" (2nd ed, 2020)
3. Clarke, Martirosyan, Izzo — "Optimizing Java"
4. "The Java Virtual Machine Specification, Java SE 21 Edition"

**Ключевые источники online:**
- shipilev.net — JMM, GC, JMH anatomy
- openjdk.org/jeps — все JEPs (444 virtual threads, 454 FFM, 439 Gen ZGC, 431 Sequenced Collections)
- docs.oracle.com/javase/21 — JCA, JSSE, NIO.2, JFR reference
- inside.java — Ron Pressler (virtual threads), Claes Redestad
- baeldung.com — практические туториалы
- jenkov.com — NIO, concurrency
- mechanical-sympathy.blogspot.com — Martin Thompson на low-latency
- infoq.com — engineering articles
- dzone.com — практические статьи
- habr.com — русскоязычные разборы

**Связь с другими разделами roadmap:**
- После Core (01) — этот раздел даёт глубокое понимание JVM и concurrency;
- Готовит к [03 Build/Test/Eng](./03-build-test-eng.md): JMH benchmarking основан на понимании JIT, GC, JMM;
- Готовит к [04 Spring](./04-spring-ecosystem.md): AOP через CGLIB/ByteBuddy (reflection, ByteBuddy), transaction propagation через ThreadLocal/ScopedValue;
- Готовит к [06 Distributed](./06-distributed-msg.md): backpressure (Flow API), shared nothing concurrency через virtual threads;
- Готовит к [07 DevOps](./07-devops-observability.md): JFR → OpenTelemetry, GC tuning на production, profiling;
- Готовит к [08 Architecture](./08-arch-patterns-leadership.md): trade-offs между throughput / latency / consistency — фундамент через JMM, GC, locks.
