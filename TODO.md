# TODO — План уроков Java Senior Roadmap (docs → code)

> Сгенерировано из `docs/` (8 разделов, 157 подразделов, README + 01–08).
> Принцип: **1 подраздел docs = 1 урок (lesson) = 1 пакет `me.yuugao.lessonNN` + раннер `LessonNN.run()` + 3–5 demo-классов в `demo/`**.
> Сами классы пока НЕ пишем — только план. Реализация — в будущих итерациях.
> Стек: Java 21, Gradle Kotlin DSL, JUnit 5. Стиль Javadoc — как в `lesson01/demo/OperatorsDemo.java`.

## Конвенции

- Пакет: `me.yuugao.lessonNN` (NN = 01…157, сквозная нумерация).
- Раннер: `me.yuugao.lessonNN.LessonNN` с `public static void run()`; подключается в `Main.java`.
- Demo-классы: `me.yuugao.lessonNN.demo.*Demo.java`, каждый с классовым Javadoc:
  `/** Класс {@code XxxDemo} … <p>…</p> @author MeYuugao @version 1.0 @since Java 21 */`
  + методовые Javadoc на каждый разбор.
- Каждый demo: `toString()`-отчёт для консольного вывода (паттерн lesson01), без внешних зависимостей,
  пока не дойдём до уроков Spring/DB/Kafka/Testcontainers (там — отдельные модули/профили).
- Источник истины по теории/кейсам: указан `docs/0X-*.md §Y`.
- Статус: `[ ]` todo, `[x]` done. Сейчас НЕТ завершённых уроков: `lesson01` в работе (~30%, см. аудит в Lesson 01).

## Фазы (из docs/README.md)

- [ ] Фаза 0 — каркас: `Main`, `lesson01/Lesson01` (в работе, НЕ done — см. Lesson 01 аудит)
- [ ] Фаза 1 — Фундамент (уроки 01–37): Core + Advanced (JMM, GC, concurrency, virtual threads)
- [ ] Фаза 2 — Инженерия (уроки 38–57): Maven/Gradle/Git/JUnit/Mockito/Testcontainers/CI
- [ ] Фаза 3 — Enterprise (уроки 58–97): Spring + Persistence/DB
- [ ] Фаза 4 — Распределённые системы (уроки 98–137): Kafka/Rabbit/microservices + DevOps/K8s/observability
- [ ] Фаза 5 — Архитектура и лидерство (уроки 138–157): SOLID/GoF/DDD/System Design/algorithms/security

## Прогресс по разделам

- [ ] Раздел 1 Java Core (01–18) — `docs/01-java-core.md`
- [ ] Раздел 2 Java Advanced (19–37) — `docs/02-java-advanced.md`
- [ ] Раздел 3 Build/Test/Eng (38–57) — `docs/03-build-test-eng.md`
- [ ] Раздел 4 Spring (58–77) — `docs/04-spring-ecosystem.md`
- [ ] Раздел 5 Persistence/DB (78–97) — `docs/05-persistence-db.md`
- [ ] Раздел 6 Distributed/Msg (98–117) — `docs/06-distributed-msg.md`
- [ ] Раздел 7 DevOps/Observability (118–137) — `docs/07-devops-observability.md`
- [ ] Раздел 8 Arch/Patterns/Leadership (138–157) — `docs/08-arch-patterns-leadership.md`

---

# Раздел 1. Java Core & Foundations (`docs/01-java-core.md`, 18 уроков)

## Lesson 01 — Синтаксис, типы, операторы, управляющие конструкции (01 §1) — `[ ] В РАБОТЕ, требует дополнения`
- Пакет `me.yuugao.lesson01`, раннер `Lesson01.run()`. Источник: `docs/01-java-core.md §1` (24 пункта теории + 6 кейсов).
- Аудит текущего состояния (что есть):
  - `demo/TypesDemo.java` — ЧАСТИЧНО. Есть только min/max для `byte/short/int/long/float`. НЕТ: `double/char/boolean`, литералов (`0b/0x/052/_/L/F/D`), `var` (JEP 323), widening/narrowing, массивов, scope, `String ==` vs `equals`, JLS-ссылок. Требует расширения или разбиения (см. ниже).
  - `demo/PassByValueDemo.java` — ЧАСТИЧНО. Есть 3 базовых эксперимента (примитив/мутация/переприсвоение). НЕТ кейса 1.6 полностью: `swap(Integer)` no-op, reflection-взлом `Integer.value`, ломка `IntegerCache`, `Holder<T>`-фикс, сравнение с C++ `int&`. Плюс опечатка в Javadoc: «по назначению» → должно быть «по значению».
  - `demo/OperatorsDemo.java` — ХОРОШО, почти done. Есть приоритеты JLS §15 (14 уровней), short-circuit `&` vs `&&`, `>>` vs `>>>`, `pad32`. НЕТ: привязки к кейсам 1.1 (`HashMap.hash` + гистограмма 1M строк + `h & (n-1)` для n=16/1024), 1.2 (`Integer ==`), 1.5 (init order `static/instance/ctor` + `A/B extends` + `++i + i++` слева-направо + `&` vs `&&` с null).
  - ИТОГ: урок НЕ завершён. Покрыто ~30% теории §1 и ~1.5 из 6 кейсов.
- Что должно быть (финальный набор классов урока, 8 demo):
  - `demo/TypesLiteralsDemo.java` (НОВЫЙ, или расширить `TypesDemo`) — Javadoc: «Класс {@code TypesLiteralsDemo} демонстрирует 8 примитивов, ссылочные типы, литералы и `var`. <p>Разбирает: размеры/диапазоны всех 8 (добавить `double/char/boolean`), литералы `42/052/0x2A/0b101010/1_000_000/L/F/D`, `char` как UTF-16 code unit, `boolean` размер в стеке/массиве, ссылочные типы + `null`, Unicode-идентификаторы `int π`, `javac -encoding`.</p>» Покрывает теорию п.3–6.
  - `demo/VarScopeArraysDemo.java` (НОВЫЙ) — Javadoc: «Класс {@code VarScopeArraysDemo} демонстрирует `var` (JEP 323), области видимости и массивы. <p>Разбирает: `var list = new ArrayList<String>()`, ограничения (только локальные с инициализатором, не поля/параметры/return), `var` как reserved type name, scope блока/метода/класса, `for(int i)` видимость, запрет перекрытия с параметром, массивы `arr.length`, jagged, `ArrayIndexOutOfBoundsException`.</p>» Покрывает теорию п.13–14 + п.7 (Unicode) частично.
  - `demo/WideningNarrowingDemo.java` (НОВЫЙ) — Javadoc: «Класс {@code WideningNarrowingDemo} демонстрирует widening/narrowing преобразования. <p>Разбирает: `byte/short/char → int` в арифметике (`b = b + 1` не компилируется, `b += 1` кастует неявно), `(byte)300 → 44`, `(int)3.9 == 3`, `instanceof` + `null instanceof X == false`.</p>» Покрывает теорию п.17–19.
  - `demo/OperatorsDemo.java` (ЕСТЬ, дополнить) — Javadoc оставить, добавить методы: `hashSpreading()` (кейс 1.1: `hash(Object key)` + 1M строк + хи-квадрат + `n=16 vs 1024` + power-of-two), `boxedEquality()` (кейс 1.2: `127 ==` true / `128 ==` false + `javap -c Integer.valueOf` + `IntegerCache` + `-XX:AutoBoxCacheMax=1000`). Покрывает теорию п.8–10 + кейсы 1.1/1.2.
  - `demo/SwitchPatternDemo.java` (НОВЫЙ) — Javadoc: «Класс {@code SwitchPatternDemo} демонстрирует switch expressions (JEP 361), `instanceof`-pattern (JEP 394), switch-pattern (JEP 441) и Text Blocks (JEP 378). <p>Разбирает: classic switch vs `->` + `yield`, `if (o instanceof String s)`, `case Integer i when i > 0`, `case null` (NPE в classic), record deconstruction `case Point(int x, int y)`, `sealed Expr permits Num,Add,Mul` + exhaustiveness без `default`, Text Blocks `"""` + `\s` + `\` continuation для SQL/JSON.</p>» Покрывает теорию п.11–13,23 + кейс 1.3.
  - `demo/FloatMoneyDemo.java` (НОВЫЙ) — Javadoc: «Класс {@code FloatMoneyDemo} демонстрирует неточность binary floating-point и `BigDecimal`. <p>Разбирает: `0.1+0.2 == 0.30000000000000004`, `BigDecimal.valueOf(0.1)` vs `new BigDecimal(0.1)` vs `new BigDecimal("0.1")`, `Money` VO с `RoundingMode.HALF_EVEN` vs `HALF_UP`, `0.005` граничный тест.</p>» Покрывает теорию п.21 + кейс 1.4.
  - `demo/InitOrderEvalDemo.java` (НОВЫЙ) — Javadoc: «Класс {@code InitOrderEvalDemo} демонстрирует порядок инициализации и вычисления. <p>Разбирает: `static{}` один раз + `instance{}` + ctor + наследование `A→B→C` (JLS §12.4/§12.5), `Class.forName` триггер, `i=0; ++i + i++ + i == 1 1 3` (JLS §15.7 слева-направо), short-circuit `a != null && a.size()>0` vs `&`.</p>» Покрывает кейс 1.5.
  - `demo/PassByValueDemo.java` (ЕСТЬ, дополнить) — исправить Javadoc («по значению»), добавить методы: `swapFail()` (`swap(Integer a,Integer b)` no-op), `reflectionHack()` (`field value` + ломка `IntegerCache ==`), `holderFix()` (`Holder<T>` работает), `stringImmutability()` (`String` нельзя поменять у вызывающего). Покрывает теорию п.15 + кейс 1.6 + задел к `String ==` (п.20) и Reflection (L14).
- Критерий готовности Lesson 01:
  - [ ] `TypesDemo` дополнен до 8 типов или разбит на `TypesLiteralsDemo` + `VarScopeArraysDemo` + `WideningNarrowingDemo`
  - [ ] `OperatorsDemo` дополнен кейсами 1.1 + 1.2
  - [ ] Созданы `SwitchPatternDemo`, `FloatMoneyDemo`, `InitOrderEvalDemo`
  - [ ] `PassByValueDemo` дополнен кейсом 1.6 + фикс опечатки
  - [ ] `Lesson01.run()` вызывает все 8 demo, `Main` выводит без ошибок, `gradlew build` зелёный

## Lesson 02 — ООП: классы, наследование, abstract/interface, records, sealed (01 §2) — `[ ]`
- Пакет `me.yuugao.lesson02`
- Классы:
  - `demo/InheritancePolyDemo.java` — Javadoc: «Наследование single, overriding vs overloading, covariant return, static hiding, vtable». Разобрать: `@Override` правила, most-specific для overload, `super()/this()`.
  - `demo/InterfaceDefaultDemo.java` — Javadoc: «default/static/private в интерфейсах, diamond problem, most-specific (JLS §9.4.1)». Кейс 2.1: `A/B → C implements A,B`, `Interface.super.method()`.
  - `demo/RecordsDemo.java` — Javadoc: «record как носитель данных: canonical/compact ctor, equals/hashCode/toString, validation, wither». Кейс 2.2: `Money(BigDecimal,Currency)`.
  - `demo/SealedHierarchyDemo.java` — Javadoc: «sealed/non-sealed, permits, exhaustiveness в switch, expression problem». Кейсы 1.3/2.3: `Expr permits Num,Add,Mul`.
  - `demo/CompositionVsInheritanceDemo.java` — Javadoc: «Композиция vs наследование (EJ Item 18), Template Method vs Strategy, clone/copy-constructor». Кейсы 2.4/2.6.

## Lesson 03 — Inner/nested/lambda/method refs (01 §3) — `[ ]`
- Пакет `me.yuugao.lesson03`
- Классы:
  - `demo/NestedClassesDemo.java` — Javadoc: «static nested vs inner vs local vs anonymous, synthetic `this$0`, effectively final». Разобрать: `outer.new Inner()`, `Map.Entry`, leak через inner.
  - `demo/LambdaInternalsDemo.java` — Javadoc: «Лямбды: SAM, invokedynamic+LambdaMetafactory, capture, stateless-кэш, `this` lexical». Разобрать: lambda vs anonymous, escape analysis.
  - `demo/FunctionalInterfacesDemo.java` — Javadoc: «Стандартные FI: Function/Predicate/Consumer/Supplier/BiFunction, примитивные специализации, композиция andThen/compose». Разобрать: `IntStream vs Stream<Integer>`, `Function.identity()`, currying.
  - `demo/MethodRefsDemo.java` — Javadoc: «4 вида method refs, `var` в лямбдах, checked-исключения (ThrowingFunction/Either)». Кейс: serializable lambda — почему не надо.

## Lesson 04 — String/StringBuilder/StringPool (01 §4) — `[ ]`
- Пакет `me.yuugao.lesson04`
- Классы:
  - `demo/StringPoolDemo.java` — Javadoc: «StringPool, intern, `== vs equals`, литералы». Разобрать: `"a"== "a"`, `new String("a")`, `intern()` цена.
  - `demo/StringBuilderVsBufferDemo.java` — Javadoc: «StringBuilder vs StringBuffer vs `+` (invokedynamic StringConcatFactory), capacity». Разобрать: конкат в цикле, JMH-наблюдение.
  - `demo/StringNewMethodsDemo.java` — Javadoc: «Новые методы Java 11+: isBlank/lines/strip/repeat/indent/transform, Text Blocks». Разобрать: `\s`, выравнивание `"""`.

## Lesson 05 — Исключения (01 §5) — `[ ]`
- Пакет `me.yuugao.lesson05`
- Классы:
  - `demo/ExceptionHierarchyDemo.java` — Javadoc: «checked vs unchecked vs Error, catch/multi-catch, chaining, suppressed». Разобрать: когда свой checked, `throws` в сигнатуре.
  - `demo/TryWithResourcesDemo.java` — Javadoc: «try-with-resources (AutoCloseable), suppressed vs lost, порядок close». Разобрать: `PrintWriter` + `FileInputStream` двойное закрытие.
  - `demo/CustomExceptionsDemo.java` — Javadoc: «Свои иерархии, exception translation, Either/Result как альтернатива». Кейс: domain `OrderNotFoundException` + маппинг в ProblemDetail (задел к Spring).

## Lesson 06 — Collections (01 §6) — `[ ]`
- Пакет `me.yuugao.lesson06`
- Классы:
  - `demo/ListSetQueueDemo.java` — Javadoc: «List/Set/Queue: ArrayList vs LinkedList, HashSet/LinkedHashSet/TreeSet, ArrayDeque». Разобрать: сложность операций, когда что.
  - `demo/HashMapInternalsDemo.java` — Javadoc: «Устройство HashMap: hash spreading, power-of-two, treeify(8)/resize, load factor». Кейс 1.1: гистограмма бакетов.
  - `demo/FailFastVsFailSafeDemo.java` — Javadoc: «fail-fast (modCount) vs fail-safe/weakly-consistent (CHM/CopyOnWrite), CME». Разобрать: итератор + `remove()` во время обхода.
  - `demo/CollectionsUtilsDemo.java` — Javadoc: «Collections factory (List.of/Set.of/Map.of), unmodifiable vs immutable, sorting». Разобрать: `UnsupportedOperationException` ловушка.

## Lesson 07 — Generics: PECS, erasure, bridge (01 §7) — `[ ]`
- Пакет `me.yuugao.lesson07`
- Классы:
  - `demo/GenericsBasicsDemo.java` — Javadoc: «Инвариантность, diamond, bounded `<T extends Number>`, wildcards». Разобрать: `List<Integer>` не `List<Number>`.
  - `demo/PecsDemo.java` — Javadoc: «PECS: producer extends / consumer super, copy/max примеры (EJ Item 31)». Разобрать: `copy(List<? super T>, List<? extends T>)`.
  - `demo/ErasureBridgeDemo.java` — Javadoc: «Erasure, bridge methods, heap pollution, `@SafeVarargs`, ClassCast ловушки». Разобрать: `javap -c` bridge, `Generic[]` нельзя.

## Lesson 08 — Enum (01 §8) — `[ ]`
- Пакет `me.yuugao.lesson08`
- Классы:
  - `demo/EnumBasicsDemo.java` — Javadoc: «Enum как класс: ctor/fields/methods, `valueOf/values`, switch по enum». Разобрать: `enum` vs `static final int` константы.
  - `demo/EnumSetMapDemo.java` — Javadoc: «EnumSet/EnumMap как битовые/массивные оптимизации». Разобрать: перф vs HashSet/HashMap.
  - `demo/EnumSingletonStrategyDemo.java` — Javadoc: «Enum singleton (EJ Item 3), Strategy через enum, abstract method per constant». Разобрать: `Operation.PLUS.apply()`.

## Lesson 09 — I/O и NIO (01 §9) — `[ ]`
- Пакет `me.yuugao.lesson09`
- Классы:
  - `demo/IoStreamsDemo.java` — Javadoc: «InputStream/Reader, decorator (Buffered), кодировки, try-with-resources». Разобрать: bytes vs chars, `InputStreamReader`.
  - `demo/NioFilesDemo.java` — Javadoc: «Path/Files, walk/copy/move, attributes, WatchService (задел к NIO.2)». Разобрать: `Files.readString/writeString` (Java 11).
  - `demo/ChannelsBuffersDemo.java` — Javadoc: «Channels/ByteBuffer: position/limit/capacity/flip/clear, direct vs heap». Разобрать: ручной цикл чтения чанками.
  - `demo/SerializationDemo.java` — Javadoc: «Serializable, serialVersionUID, transient, record-сериализация, Jackson-задел». Разобрать: почему `clone`/`finalize` плохо (мостик к L02).

## Lesson 10 — java.time (01 §10) — `[ ]`
- Пакет `me.yuugao.lesson10`
- Классы:
  - `demo/LocalDateTimeDemo.java` — Javadoc: «LocalDate/Time/LocalDateTime: immutability, plus/minus, until». Разобрать: `Date`/`Calendar` legacy-проблемы.
  - `demo/ZonedInstantDemo.java` — Javadoc: «ZonedDateTime/OffsetDateTime/Instant, DST-ловушки, UTC-хранение». Разобрать: `ZoneId.systemDefault()` опасность.
  - `demo/DurationPeriodFormatterDemo.java` — Javadoc: «Duration vs Period, DateTimeFormatter thread-safety, parsing». Разобрать: `SimpleDateFormat` не потокобезопасен → `DateTimeFormatter`.

## Lesson 11 — equals/hashCode, Comparable/Comparator (01 §11) — `[ ]`
- Пакет `me.yuugao.lesson11`
- Классы:
  - `demo/EqualsHashCodeDemo.java` — Javadoc: «Контракт equals/hashCode (EJ 10/11): reflex/symm/trans/consist, HashMap-поломка». Разобрать: `Objects.equals/hash`, record-генерация.
  - `demo/ComparableComparatorDemo.java` — Javadoc: «Comparable vs Comparator, compareTo-контракт, chaining thenComparing». Разобрать: `compare(a,b)` переполнение `a-b` — использовать `Integer.compare`.

## Lesson 12 — Stream API (01 §12) — `[ ]`
- Пакет `me.yuugao.lesson12`
- Классы:
  - `demo/StreamBasicsDemo.java` — Javadoc: «intermediate (lazy) vs terminal (eager), pipeline, single-use». Разобрать: `filter/map/flatMap/distinct/sorted/limit/skip`.
  - `demo/CollectorsReduceDemo.java` — Javadoc: «collect/groupingBy/partitioning/joining, reduce vs collect, downstream». Разобрать: `groupingBy + counting/mapping`.
  - `demo/ParallelStreamsDemo.java` — Javadoc: «parallel: ForkJoinPool.common, когда вредно (boxing, stateful, IO), unordered». Разобрать: `parallel().reduce` ассоциативность, замер JMH.

## Lesson 13 — Optional (01 §13) — `[ ]`
- Пакет `me.yuugao.lesson13`
- Классы:
  - `demo/OptionalProperUseDemo.java` — Javadoc: «Правильное использование: map/flatMap/filter/orElseGet/orThrow, как return-type». Разобрать: `orElse vs orElseGet` (eager).
  - `demo/OptionalAntiPatternsDemo.java` — Javadoc: «Антипаттерны: `isPresent+get`, поле/параметр Optional, `of(null)`, сериализация». Разобрать: `ifPresent` vs `map`.

## Lesson 14 — Reflection API база (01 §14) — `[ ]`
- Пакет `me.yuugao.lesson14`
- Классы:
  - `demo/ReflectionBasicsDemo.java` — Javadoc: «Class/Method/Field/Constructor, setAccessible, newInstance». Разобрать: `getDeclared* vs get*`, цена reflection.
  - `demo/ReflectionBreaksEncapDemo.java` — Javadoc: «Как рефлексия ломает private/final/IntegerCache (мостик к 1.2/2.x), модульные запреты JPMS». Кейс 1.6.

## Lesson 15 — Annotations (01 §15) — `[ ]`
- Пакет `me.yuugao.lesson15`
- Классы:
  - `demo/BuiltInAnnotationsDemo.java` — Javadoc: «Встроенные: @Override/Deprecated/SuppressWarnings/SafeVarargs/FunctionalInterface». Разобрать: `@Deprecated(forRemoval,since)`.
  - `demo/MetaAnnotationsDemo.java` — Javadoc: «Мета-аннотации: @Retention/@Target/@Repeatable/@Inherited/@Documented». Разобрать: SOURCE/CLASS/RUNTIME.
  - `demo/CustomAnnotationDemo.java` — Javadoc: «Своя аннотация + чтение через reflection (задел к APT L31)». Разобрать: `@interface`, default, чтение в runtime.

## Lesson 16 — Varargs, init order, final/finally/finalize (01 §16) — `[ ]`
- Пакет `me.yuugao.lesson16`
- Классы:
  - `demo/VarargsOverloadDemo.java` — Javadoc: «varargs vs overload, heap pollution, `@SafeVarargs`». Разобрать: `foo(Object...)` vs `foo(String)` most-specific.
  - `demo/InitOrderDemo.java` — Javadoc: «Порядок инициализации: static/instance/ctor, наследование (JLS §12.4/12.5)». Кейсы 1.5/2.5.
  - `demo/FinalFinallyDemo.java` — Javadoc: «final (класс/метод/поле/переменная), finally vs try-with-resources, finalize deprecated». Разобрать: effectively final для лямбд.

## Lesson 17 — Wrapper, autoboxing, Integer cache (01 §17) — `[ ]`
- Пакет `me.yuugao.lesson17`
- Классы:
  - `demo/AutoboxingPitfallsDemo.java` — Javadoc: «Autoboxing/unboxing, NPE при `null`, `==` ловушка». Кейс 1.2.
  - `demo/IntegerCacheDemo.java` — Javadoc: «IntegerCache -128..127, `-XX:AutoBoxCacheMax`, valueOf vs new». Разобрать: `javap -c`, `Long/Short` без расширения.
  - `demo/PrimitiveSpecializationDemo.java` — Javadoc: «Специализации: IntFunction/ToIntFunction, IntStream, цена boxing в hot loop». Мостик к Stream/Flow.

## Lesson 18 — Unicode/Charset (01 §18) — `[ ]`
- Пакет `me.yuugao.lesson18`
- Классы:
  - `demo/UnicodeBasicsDemo.java` — Javadoc: «char UTF-16, code point vs code unit, surrogate pairs, `String.length vs codePointCount`». Разобрать: эмодзи `𝕩` длина 2.
  - `demo/CharsetEncodingDemo.java` — Javadoc: «Charset: UTF-8/Windows-1251, `javac -encoding`, `Files.readString(charset)`, mojibake». Разобрать: `StandardCharsets.UTF_8` всегда явно.

---

# Раздел 2. Java Advanced (`docs/02-java-advanced.md`, 19 уроков)

## Lesson 19 — JVM Architecture (02 §1) — `[ ]`
- Пакет `me.yuugao.lesson19`
- Классы:
  - `demo/ClassLoadingDemo.java` — Javadoc: «ЖЦ класса: loading/linking/init, bootstrap/platform/app loaders, parent delegation, `0xCAFEBABE`». Разобрать: `ClassLoader.loadClass`, Tomcat/OSGi-нарушения.
  - `demo/HeapMetaspaceDemo.java` — Javadoc: «Heap (Eden/Survivor/Old), Metaspace vs PermGen, object header/MarkWord, compressed oops 32GB-граница». Кейсы: Metaspace leak, 36GB vs 31GB.
  - `demo/JitTieredDemo.java` — Javadoc: «Interpreter→C1→C2, tiered, OSR, deopt, safepoints, `-XX:+PrintCompilation/PrintInlining`». Кейсы: too big/not inline, C1 vs C2 JMH, Graal/Native задел.

## Lesson 20 — Java Memory Model (02 §2) — `[ ]`
- Пакет `me.yuugao.lesson20`
- Классы:
  - `demo/HappensBeforeDemo.java` — Javadoc: «HB-правила JSR-133/JLS §17: program order, monitor, volatile, start/join, final-freeze». Разобрать: visibility/ordering/atomicity.
  - `demo/VolatileRaceDemo.java` — Javadoc: «volatile: acquire/release, `++volatile` не атомарно, DCL-баг». Кейсы: `Holder{data,ready}`, `volatile++` 10×100K.
  - `demo/SafePublicationDemo.java` — Javadoc: «Safe publication: final, holder idiom/enum singleton, VarHandle P/opaque/acquire-release (задел)». Кейс: JCStress-идея без JCStress — показать на обычном прогоне + объяснить JCStress.

## Lesson 21 — Garbage Collection (02 §3) — `[ ]`
- Пакет `me.yuugao.lesson21`
- Классы:
  - `demo/GcRootsAlgoDemo.java` — Javadoc: «GC roots, mark-sweep/compact/copying, generational hypothesis, TLAB/card table/RSet». Разобрать: young (Eden/S0/S1) + promotion.
  - `demo/CollectorsTuningDemo.java` — Javadoc: «Serial/Parallel/G1/ZGC/Shenandoah/Epsilon, флаги Xmx/NewRatio/MaxGCPauseMillis/IHOP, unified logging». Кейсы: Parallel→G1 p99, G1→ZGC.
  - `demo/HeapDumpMatDemo.java` — Javadoc: «Heap dump (jcmd/jmap/HeapDumpOnOOM), MAT: shallow/retained/dominator/leak suspects». Кейсы: static map leak, Caffeine-фикс, `WeakHashMap`.

## Lesson 22 — Threads, lifecycle, ThreadLocal, virtual threads (02 §4) — `[ ]`
- Пакет `me.yuugao.lesson22`
- Классы:
  - `demo/ThreadLifecycleDemo.java` — Javadoc: «NEW/RUNNABLE/BLOCKED/WAITING/TIMED_WAITING/TERMINATED, start vs run, daemon, priority». Разобрать: `ThreadMXBean`.
  - `demo/InterruptShutdownDemo.java` — Javadoc: «interrupt flag, `interrupted vs isInterrupted`, InterruptedException не глотать, graceful shutdown». Кейсы: `shutdown+awaitTermination+shutdownNow`.
  - `demo/ThreadLocalLeakDemo.java` — Javadoc: «ThreadLocal/InheritableThreadLocal, WeakKey+strong value, leak в пуле, `remove() in finally`». Кейс Tomcat redeploy.
  - `demo/VirtualThreadsDemo.java` — Javadoc: «Virtual threads (JEP 444): carrier pool, pinning (synchronized), `newVirtualThreadPerTaskExecutor`, StructuredTaskScope/ScopedValue задел». Кейс: pinning dump `"pinned":true`.

## Lesson 23 — synchronized & locks (02 §5) — `[ ]`
- Пакет `me.yuugao.lesson23`
- Классы:
  - `demo/SynchronizedInternalsDemo.java` — Javadoc: «monitorenter/exit, ACC_SYNCHRONIZED, inflation thin→fat, biased removed (JEP 374)». Разобрать: `synchronized` vs ReentrantLock для virtual threads.
  - `demo/ReentrantLockDemo.java` — Javadoc: «ReentrantLock: tryLock/timeout/interruptibly, fairness, Condition (notFull/notEmpty)». Кейсы: producer-consumer 2 conditions, `transfer(A,B)` deadlock fix.
  - `demo/StampedLockDemo.java` — Javadoc: «StampedLock: write/read/optimistic+validate, не реентерабелен, read→write нельзя». Кейс read-heavy cache ×2–3.

## Lesson 24 — java.util.concurrent (02 §6) — `[ ]`
- Пакет `me.yuugao.lesson24`
- Классы:
  - `demo/ConcurrentHashMapDemo.java` — Javadoc: «CHM 7 Segments vs 8+ CAS+synchronized bin+TreeBin, CounterCells, computeIfAbsent-recursion (JDK-8062841)». Кейсы: HashMap потеря/цикл → CHM.
  - `demo/BlockingQueuesDemo.java` — Javadoc: «Array/Linked/Synchronous/Priority/Delay/Transfer, put/offer/take/poll, 1-lock vs 2-locks». Разобрать: hand-off, weak iterator.
  - `demo/CopyOnWriteDemo.java` — Javadoc: «CopyOnWriteArrayList/Set, skip-list map, `newKeySet()`, size vs mappingCount». Разобрать: listener registry read-heavy.

## Lesson 25 — Executors & Thread Pools (02 §7) — `[ ]`
- Пакет `me.yuugao.lesson25`
- Классы:
  - `demo/ExecutorsTypesDemo.java` — Javadoc: «Fixed/Cached/Single/Scheduled/ForkJoinPool, очередь+rejection (Abort/CallerRuns/Discard), prestart». Разобрать: `newCached` unbounded-опасность.
  - `demo/ForkJoinPoolDemo.java` — Javadoc: «ForkJoin: work-stealing, RecursiveTask, commonPool, CompletableFuture задел». Разобрать: threshold tuning.
  - `demo/ScheduledGracefulDemo.java` — Javadoc: «scheduleAtFixedRate vs WithFixedDelay, graceful shutdown, `afterExecute` hook для ThreadLocal-cleanup». Мостик к L22.

## Lesson 26 — Atomic & CAS (02 §8) — `[ ]`
- Пакет `me.yuugao.lesson26`
- Классы:
  - `demo/AtomicCasDemo.java` — Javadoc: «AtomicInteger/Long/Reference, CAS loop, ABA, AtomicStampedRef». Разобрать: `getAndIncrement` vs `volatile++`.
  - `demo/LongAdderContendedDemo.java` — Javadoc: «LongAdder/Striped64/Cells, `@Contended`+`-XX:-RestrictContended`, false sharing». Кейс ×10 vs AtomicLong.
  - `demo/VarHandleDemo.java` — Javadoc: «VarHandle (JEP 193): PLAIN/OPAQUE/ACQUIRE/RELEASE/VOLATILE, замена Unsafe». Кейс acquire/release vs volatile на ARM.

## Lesson 27 — Synchronizers (02 §9) — `[ ]`
- Пакет `me.yuugao.lesson27`
- Классы:
  - `demo/LatchBarrierDemo.java` — Javadoc: «CountDownLatch (одноразовый старт-гейт) vs CyclicBarrier (многоразовый, barrier action)». Разобрать: тест-гейт для JCStress-идеи.
  - `demo/SemaphorePhaserDemo.java` — Javadoc: «Semaphore (permits, fair), Phaser (динамические фазы, arriveAndAwait)». Разобрать: rate-limit задел к Resilience4j.
  - `demo/ExchangerLockSupportDemo.java` — Javadoc: «Exchanger (hand-off), LockSupport park/unpark permit (vs wait/notify не теряется), AQS state+FIFO». Разобрать: основа ReentrantLock.

## Lesson 28 — Reactive Flow API (02 §10) — `[ ]`
- Пакет `me.yuugao.lesson28`
- Классы:
  - `demo/FlowApiDemo.java` — Javadoc: «Flow: Publisher/Subscriber/Subscription/Processor, request(n) backpressure». Разобрать: `SubmissionPublisher` демо.
  - `demo/BackpressureDemo.java` — Javadoc: «Backpressure стратегии: buffer/drop/latest, bounded queue + `offer vs put`». Мостик к WebFlux/Kafka (L63/L103).

## Lesson 29 — Reflection advanced: dynamic proxies (02 §11) — `[ ]`
- Пакет `me.yuugao.lesson29`
- Классы:
  - `demo/DynamicProxyDemo.java` — Javadoc: «JDK proxy: InvocationHandler, только интерфейсы, `Proxy.newProxyInstance`». Разобрать: AOP-задел (L59).
  - `demo/CglibByteBuddyDemo.java` — Javadoc: «CGLIB/ByteBuddy subclass-proxy для классов, final-ограничения, proxy-cache leak (Metaspace)». Кейс L21.

## Lesson 30 — Method Handles & invokedynamic (02 §12) — `[ ]`
- Пакет `me.yuugao.lesson30`
- Классы:
  - `demo/MethodHandlesDemo.java` — Javadoc: «MethodHandle/MethodType, findVirtual/invokeExact vs reflection, LambdaMetafactory-связь». Разобрать: перф vs reflection.
  - `demo/InvokeDynamicDemo.java` — Javadoc: «invokedynamic bootstrap, hidden classes (Java 15), string concat + lambda байткод». Разобрать: `javap -c` лямбды.

## Lesson 31 — Annotation processing (02 §13) — `[ ]`
- Пакет `me.yuugao.lesson31`
- Классы:
  - `demo/AnnotationProcessorDemo.java` — Javadoc: «APT: AbstractProcessor, RoundEnvironment, `META-INF/services`, Lombok/MapStruct-идея». Разобрать: `annotationProcessorPaths` (мостик к Maven L38).
  - `demo/CodegenVsReflectionDemo.java` — Javadoc: «Codegen (compile-time) vs reflection (runtime): type-safety, Graal-задел». Мостик к Spring AOT (L61).

## Lesson 32 — JNI/JNA/Panama FFM (02 §14) — `[ ]`
- Пакет `me.yuugao.lesson32`
- Классы:
  - `demo/JniJnaBasicsDemo.java` — Javadoc: «JNI (javah/JNINative, `native`, JNI global ref как GC root) vs JNA (без C-кода, медленнее)». Разобрать: зачем не использовать без нужды.
  - `demo/PanamaFfmDemo.java` — Javadoc: «Panama FFM (Java 22): Arena/MemorySegment/Linker, замена JNI, pinning-задел». Разобрать: off-heap идея.

## Lesson 33 — JPMS modules (02 §15) — `[ ]`
- Пакет `me.yuugao.lesson33` (особый: нужен `module-info.java` пример — в плане зафиксировать второй source-set)
- Классы:
  - `demo/JpmsBasicsDemo.java` — Javadoc: «module-info: requires/exports/opens, unnamed/automatic модули, `--add-opens`». Разобрать: почему reflection ломается в JPMS.
  - `demo/JlinkDemo.java` — Javadoc: «jlink custom runtime, closed-world задел к Graal Native». Разобрать: `jdeps` анализ.

## Lesson 34 — Security: JCA/JCE/SSL (02 §16) — `[ ]`
- Пакет `me.yuugao.lesson34`
- Классы:
  - `demo/MessageDigestCipherDemo.java` — Javadoc: «JCA/JCE: MessageDigest (SHA-256), Cipher (AES-GCM vs ECB-ловушка), KeyStore». Разобрать: IV/nonce нельзя reuse.
  - `demo/PasswordTlsDemo.java` — Javadoc: «Пароли: BCrypt/Argon2 (задел к Spring Security L67), SSL/TLS handshake, HttpsURLConnection». Разобрать: `SecureRandom` vs `Random`.

## Lesson 35 — NIO.2 (02 §17) — `[ ]`
- Пакет `me.yuugao.lesson35`
- Классы:
  - `demo/AsyncFileChannelDemo.java` — Javadoc: «AsynchronousFileChannel + CompletionHandler/Future, FileSystem/WatchService». Разобрать: не блокировать carrier (мостик к virtual).
  - `demo/SelectorNonBlockingDemo.java` — Javadoc: «Selector non-blocking IO: open/register/select/keys, задел к Netty/WebFlux». Разобрать: single-thread reactor идея.

## Lesson 36 — Modern Java 9–21 (02 §18) — `[ ]`
- Пакет `me.yuugao.lesson36`
- Классы:
  - `demo/CollectionsFactoryDemo.java` — Javadoc: «Фабрики List/Set/Map.of, SequencedCollections (Java 21), private interface methods». Разобрать: immutable ловушки.
  - `demo/RecordsSealedPatternDemo.java` — Javadoc: «Сводно: records+sealed+pattern matching (instanceof/switch/record patterns), `var`, Text Blocks». Мостик-повтор L01–L02 на новом уровне.
  - `demo/VirtualScopedStructuredDemo.java` — Javadoc: «Сводно: virtual threads + ScopedValue + StructuredTaskScope (preview 21–25)». Задел к L22/L28.

## Lesson 37 — JVM диагностика (02 §19) — `[ ]`
- Пакет `me.yuugao.lesson37`
- Классы:
  - `demo/JcmdToolsDemo.java` — Javadoc: «jstack/jstat/jmap/jcmd, `Thread.dump_to_file`, `GC.heap_dump`, `VM.classloader_stats`». Разобрать: deadlock-секция jstack.
  - `demo/JfrProfilerDemo.java` — Javadoc: «JFR/JMC low-overhead (<1%), async-profiler flame (cpu/alloc), Arthas, MAT». Кейс: alloc 5GB/sec → boxing find.

---

# Раздел 3. Build, Test & Engineering (`docs/03-build-test-eng.md`, 20 уроков)

> Особенность: уроки 38–57 частично «не-кодовые» (Maven/Gradle/Git/CI) — для них demo = генераторы `pom.xml`/`build.gradle.kts`/скриптов + парсеры/чекеры + тест-кейсы JUnit. Фиксировать в Javadoc: «генерирует/проверяет/воспроизводит».

## Lesson 38 — Maven (03 §1) — `[ ]`
- Пакет `me.yuugao.lesson38`
- Классы:
  - `demo/MavenLifecycleDemo.java` — Javadoc: «Lifecycle clean/default/site, phase vs goal(mojo), `install` цепочка, reactor `-pl -am`, effective-pom». Генерирует минимальный multi-module `pom.xml`.
  - `demo/DependencyScopeBomDemo.java` — Javadoc: «Scopes compile/provided/runtime/test/system/import, nearest-wins, BOM import, dependencyManagement vs dependencies». Кейс commons-logging vs jcl-over-slf4j.
  - `demo/ProfilesPluginsDemo.java` — Javadoc: «Profiles (os/jdk/property/file), settings.xml servers/mirrors, surefire vs failsafe (`*Test` vs `*IT`), shade/enforcer». Кейсы: dev/prod profiles, shade relocations, reproducible outputTimestamp.

## Lesson 39 — Gradle (03 §2) — `[ ]`
- Пакет `me.yuugao.lesson39`
- Классы:
  - `demo/GradleDslTasksDemo.java` — Javadoc: «Groovy vs Kotlin DSL, init/config/exec фазы, tasks inputs/outputs, `api vs implementation`». Разбирает текущий `build.gradle.kts` проекта.
  - `demo/VersionCatalogCacheDemo.java` — Javadoc: «Version catalogs `libs.versions.toml`, build-cache, configuration-cache, daemon/toolchains/foojay». Кейс: `Project`-поле ломает CC.
  - `demo/MultiProjectPublishDemo.java` — Javadoc: «Multi-project, composite `--included-build`, maven-publish, content routing». Кейс: convention plugin `acme.java-conventions`.

## Lesson 40 — Ant + Bazel сравнение (03 §3) — `[ ]`
- Пакет `me.yuugao.lesson40`
- Классы:
  - `demo/AntIvyDemo.java` — Javadoc: «Ant targets/build.xml, properties immutable, Ivy retrieve, antrun-гибрид». Кейс legacy Ant+Ivy без миграции.
  - `demo/BuildToolBenchmarkDemo.java` — Javadoc: «Сравнение Maven/Gradle/Bazel: clean vs incremental vs remote-cache, hermetic/reproducible». Кейс-бенч 30s/18s/25s.

## Lesson 41 — Git (03 §4) — `[ ]`
- Пакет `me.yuugao.lesson41`
- Классы:
  - `demo/GitModelBranchingDemo.java` — Javadoc: «Commits как snapshots (tree/blob), branch=указатель, GitFlow/TrunkBased, rebase vs merge». Разобрать: `reflog/bisect/cherry-pick`.
  - `demo/GitHooksLfsDemo.java` — Javadoc: «Hooks (pre-commit/commit-msg), submodules vs subtree, LFS, worktrees». Кейс: запрет паролей grep-хуком (связь L38 settings.xml).

## Lesson 42 — JUnit 5 (03 §5) — `[ ]`
- Пакет `me.yuugao.lesson42` (+ `src/test` — первый урок с реальными тестами)
- Классы:
  - `demo/JunitLifecycleDemo.java` — Javadoc: «Jupiter: @Test/@ParameterizedTest/@Nested/@ExtendWith, lifecycle, assertions». Покрыть тестами сам demo.
  - `demo/Junit4MigrationDemo.java` — Javadoc: «Миграция JUnit4→5: vintage engine, `@Rule`→extension, `assertThat`». Кейс legacy-модуль.

## Lesson 43 — Mockito (03 §6) — `[ ]`
- Пакет `me.yuugao.lesson43`
- Классы:
  - `demo/MockitoBasicsDemo.java` — Javadoc: «@Mock/@Spy/@InjectMocks, when/verify, matchers, BDD given/willReturn». Разобрать: stubbing order, unnecessary stubbing strictness.
  - `demo/StaticConstructionMockDemo.java` — Javadoc: «mockStatic/mockConstruction, inline mockmaker, `try(mockedStatic)` scope». Разобрать: почему static — design smell.

## Lesson 44 — AssertJ/Hamcrest (03 §7) — `[ ]`
- Пакет `me.yuugao.lesson44`
- Классы:
  - `demo/AssertJFluentDemo.java` — Javadoc: «AssertJ fluent: `assertThat(list).containsExactly`, exceptions `assertThatThrownBy`, recursive comparison». Разобрать: `usingRecursiveComparison`.
  - `demo/HamcrestMatchersDemo.java` — Javadoc: «Hamcrest: `assertThat(x, is/both/anyOf)`, custom matcher». Разобрать: когда Hamcrest в legacy.

## Lesson 45 — Testcontainers (03 §8) — `[ ]`
- Пакет `me.yuugao.lesson45`
- Классы:
  - `demo/GenericContainerDemo.java` — Javadoc: «GenericContainer, PostgreSQL/Kafka/Redis модули, @Testcontainers, singleton/reuse». Разобрать: `testcontainers.reuse.enable=true`.
  - `demo/SpringBootTcDemo.java` — Javadoc: «@ServiceConnection (Boot 3.1+), dynamic properties, reuse в CI». Задел к L74/L82/L103 (пометить `@Disabled` без Docker).

## Lesson 46 — Типы тестов (03 §9) — `[ ]`
- Пакет `me.yuugao.lesson46`
- Классы:
  - `demo/TestTypesPyramidDemo.java` — Javadoc: «unit/integration/contract/E2E, JMH, Gatling, PIT mutation, jqwik property-based». Разобрать: что где ловит.
  - `demo/MutationPropertyDemo.java` — Javadoc: «PIT surviving mutants, jqwik `@Property(tries=1000)` shrinking». Кейс: слабый тест `a+b` ловится мутацией.

## Lesson 47 — TDD/BDD (03 §10) — `[ ]`
- Пакет `me.yuugao.lesson47`
- Классы:
  - `demo/TddRedGreenDemo.java` — Javadoc: «TDD red-green-refactor на `Money`/`RateLimiter` (задел к L144)». Показать 3 коммита-итерации.
  - `demo/CucumberGherkinDemo.java` — Javadoc: «BDD: Gherkin Feature/Scenario/Stepdefs (Cucumber/JBehave), living docs». Разобрать: glue code.

## Lesson 48 — Test doubles (03 §11) — `[ ]`
- Пакет `me.yuugao.lesson48`
- Классы:
  - `demo/DoublesTaxonomyDemo.java` — Javadoc: «dummy/stub/spy/mock/fake, London (mockist) vs Chicago (classic)». Разобрать: `FakeRepository extends HashMap` vs mock.
  - `demo/FakeVsMockDemo.java` — Javadoc: «Когда fake (in-memory DB) лучше mock: скорость + рефакторинг-устойчивость». Кейс: `OrderService` оба варианта.

## Lesson 49 — JaCoCo покрытие (03 §12) — `[ ]`
- Пакет `me.yuugao.lesson49`
- Классы:
  - `demo/JacocoGoalsDemo.java` — Javadoc: «JaCoCo: prepare-agent/report/check, line/branch/missed, `report-aggregate`». Разобрать: thresholds 80/70 из L38.
  - `demo/CoverageAntiPatternsDemo.java` — Javadoc: «Антипаттерны: 100%-гонка, исключение Lombok/record, `getters` накрутка». Разобрать: mutation > coverage.

## Lesson 50 — Code Quality (03 §13) — `[ ]`
- Пакет `me.yuugao.lesson50`
- Классы:
  - `demo/StaticAnalysisDemo.java` — Javadoc: «SonarQube/Checkstyle/PMD/SpotBugs/ErrorProne/NullAway/Spotless: что ловит каждый». Разобрать: `NullAway` vs `Optional`.
  - `demo/QualityGatesDemo.java` — Javadoc: «Quality gates в CI: fail на new issues, baseline legacy». Задел к L51.

## Lesson 51 — CI/CD (03 §14) — `[ ]`
- Пакет `me.yuugao.lesson51`
- Классы:
  - `demo/PipelineAsCodeDemo.java` — Javadoc: «Jenkins/GitHub Actions/GitLab CI: артефакты/кэш/матрицы (17/21), `setup-java`+settings.xml». Генерирует `/.github/workflows/ci.yml` пример.
  - `demo/DeployStrategiesDemo.java` — Javadoc: «rolling/blue-green/canary (задел к Argo Rollouts L136), health/readiness гейт». Разобрать: DB-миграции без downtime (задел к L87).

## Lesson 52 — Artifact repositories (03 §15) — `[ ]`
- Пакет `me.yuugao.lesson52`
- Классы:
  - `demo/NexusArtifactoryDemo.java` — Javadoc: «Nexus/Artifactory proxy/hosted, distributionManagement, snapshots vs releases». Разобрать: `maven-metadata.xml`.
  - `demo/CentralPublishDemo.java` — Javadoc: «Maven Central: OSSRH→Central Portal, GPG sign, `central-publishing-maven-plugin`, POM metadata». Чеклист публикации.

## Lesson 53 — Релиз-менеджмент (03 §16) — `[ ]`
- Пакет `me.yuugao.lesson53`
- Классы:
  - `demo/SemVerChangelogDemo.java` — Javadoc: «SemVer MAJOR.MINOR.PATCH, conventional commits→changelog, Maven Release Plugin». Разобрать: `1.0-SNAPSHOT` flow.
  - `demo/JReleaserDemo.java` — Javadoc: «JReleaser: GitHub release + Homebrew/Scoop/Docker, announce». Задел к native (L61).

## Lesson 54 — Code Review (03 §17) — `[ ]`
- Пакет `me.yuugao.lesson54`
- Классы:
  - `demo/ReviewChecklistDemo.java` — Javadoc: «Checklist: correctness/security/perf/tests/naming, конструктивный фидбек, парное программирование». Разобрать: `Danger`/`Reviewdog` автоматизация (задел к L149).

## Lesson 55 — Документирование (03 §18) — `[ ]`
- Пакет `me.yuugao.lesson55`
- Классы:
  - `demo/JavadocArchUnitDemo.java` — Javadoc: «Javadoc (этот проект), ArchUnit: `noClasses().should().dependOn`». Разобрать: запрет `lesson*.demo` зависимостей назад.
  - `demo/AdrC4Demo.java` — Javadoc: «ADR (MADR template), C4 (PlantUML/Mermaid/Structurizr), decision log». Генерирует `docs/adr/0001-...md` пример.

## Lesson 56 — Pre-commit hooks (03 §19) — `[ ]`
- Пакет `me.yuugao.lesson56`
- Классы:
  - `demo/PreCommitDemo.java` — Javadoc: «husky/lint-staged/commitlint, conventional commits, spotless+checkstyle до push». Генерирует `.pre-commit-config.yaml` пример.

## Lesson 57 — Security scanning (03 §20) — `[ ]`
- Пакет `me.yuugao.lesson57`
- Классы:
  - `demo/DependencyScanDemo.java` — Javadoc: «Snyk/Dependabot/OWASP Dependency-Check/Trivy/CodeQL, SBOM (CycloneDX)». Кейс: уязвимый `log4shell` диапазон (связь L38 ranges).

---

# Раздел 4. Spring Ecosystem (`docs/04-spring-ecosystem.md`, 20 уроков)

> С L58 — нужен Spring Boot стартер-модуль (отдельный Gradle source-set или `spring/` пример). В Javadoc каждого demo фиксировать версии: Framework 6.1/6.2, Boot 3.2–3.4, Java 17/21, `jakarta.*`.

## Lesson 58 — Spring Core / IoC / DI (04 §1) — `[ ]`
- Пакет `me.yuugao.lesson58`
- Классы:
  - `demo/BeanLifecycleDemo.java` — Javadoc: «Lifecycle: BeanDefinition→populate→Aware→BPP:before→@PostConstruct→afterPropertiesSet→init→BPP:after (AOP proxy)→destroy». Кейс lifecycle-trace.
  - `demo/InjectionCircularDemo.java` — Javadoc: «Constructor vs field/setter, @Qualifier/@Primary, cycles: early-ref (устар.) + `allow-circular=false` (6.1), @Lazy-фикс». Кейс A↔B.
  - `demo/ScopesConditionalDemo.java` — Javadoc: «Scopes singleton/prototype/request/session + scoped-proxy, @Profile/@Conditional/@Lazy/@DependsOn, full vs lite `@Configuration`». Кейсы TenantScope + proxyBeanMethods.

## Lesson 59 — Spring AOP (04 §2) — `[ ]`
- Пакет `me.yuugao.lesson59`
- Классы:
  - `demo/PointcutAdviceDemo.java` — Javadoc: «Aspect/JoinPoint/Pointcut (`execution/within/@annotation/bean`), advice Before/After/AfterReturning/AfterThrowing/Around». Кейс `@Around` timing+MDC traceId.
  - `demo/SelfInvocationProxyDemo.java` — Javadoc: «JDK vs CGLIB, self-invocation (`this.b()` мимо прокси), exposeProxy/AopContext/self-inject, final-ограничения». Кейс A→`this.B()` 3 фикса.
  - `demo/WeavingOrderDemo.java` — Javadoc: «Weaving runtime vs CTW/LTW, @Order стек, JMH cost noOp». Кейсы ordering + LTW для `@Entity`.

## Lesson 60 — Spring Configuration (04 §3) — `[ ]`
- Пакет `me.yuugao.lesson60`
- Классы:
  - `demo/ValueSpelDemo.java` — Javadoc: «@Value (`${}`+`#{SpEL}`), SpEL injection-риск, Environment order (cmd>env>yml)». Разобрать: Docker/K8s override.
  - `demo/ConfigPropsBindingDemo.java` — Javadoc: «@ConfigurationProperties (record+constructor binding, Duration/DataSize), @Validated, custom Converter, Binder API». Разобрать: `@Value` vs `@ConfigurationProperties`.
  - `demo/ProfilesImportDemo.java` — Javadoc: «@Profile expressions, YAML `on-profile`, `spring.config.import/configtree`, EnvironmentPostProcessor». Кейс Vault BFPP vs EnvironmentPostProcessor.

## Lesson 61 — Spring Boot (04 §4) — `[ ]`
- Пакет `me.yuugao.lesson61`
- Классы:
  - `demo/AutoConfigStarterDemo.java` — Javadoc: «Auto-config: `@ConditionalOn*`, `spring.factories`→`AutoConfiguration.imports`, starter-структура, `ApplicationStartup`». Кейс свой starter.
  - `demo/ActuatorNativeDemo.java` — Javadoc: «Actuator endpoints, AOT/Native (GraalVM, reflect-config для Jackson), startup/RSS замер». Мостик к L68/L133.

## Lesson 62 — Spring Web MVC (04 §5) — `[ ]`
- Пакет `me.yuugao.lesson62`
- Классы:
  - `demo/ControllerAdviceDemo.java` — Javadoc: «@RestController, ProblemDetail RFC7807, validation, content negotiation, SSE». Кейс global `@ControllerAdvice` + `MethodArgumentNotValid`.
  - `demo/ParentChildContextDemo.java` — Javadoc: «Root vs servlet context иерархия (задел из L58), DispatcherServlet, interceptors». Разобрать: кто видит кого.

## Lesson 63 — Spring WebFlux (04 §6) — `[ ]`
- Пакет `me.yuugao.lesson63`
- Классы:
  - `demo/MonoFluxBackpressureDemo.java` — Javadoc: «Mono/Flux, Schedulers (boundedElastic vs parallel), backpressure (задел L28), WebClient». Разобрать: block() в event-loop — нельзя.
  - `demo/MvcVsWebfluxDemo.java` — Javadoc: «MVC (virtual threads) vs WebFlux: когда что, R2DBC-задел, `StepVerifier`». Кейс IO-bound выбор.

## Lesson 64 — Spring Data JPA (04 §7) — `[ ]`
- Пакет `me.yuugao.lesson64`
- Классы:
  - `demo/NPlusOneEntityGraphDemo.java` — Javadoc: «N+1, fetch join/EntityGraph, Specifications/QueryDSL, projections». Кейс `findAll` → 1+N → fix.
  - `demo/RepositoryQueryDemo.java` — Javadoc: «Derived queries, @Query, Criteria API,Auditing, pagination». Мостик к L81/L86.

## Lesson 65 — Spring Data прочие (04 §8) — `[ ]`
- Пакет `me.yuugao.lesson65`
- Классы:
  - `demo/MongoRedisEsDemo.java` — Javadoc: «Spring Data Mongo/Redis/ES/JDBC: template vs repository, Redis serializers». Задел к L88–L90.
  - `demo/JdbcClientDemo.java` — Javadoc: «JdbcClient/JdbcTemplate, RowMapper, batch (задел L78/L81)». Разобрать: когда JPA избыточен.

## Lesson 66 — Spring TX (04 §9) — `[ ]`
- Пакет `me.yuugao.lesson66`
- Классы:
  - `demo/PropagationSelfInvokeDemo.java` — Javadoc: «Propagation (REQUIRED/REQUIRES_NEW/NESTED), isolation, self-invocation мимо TX-proxy (связь L59), readOnly». Кейс `@Transactional` на private — не работает.
  - `demo/TransactionalEventsDemo.java` — Javadoc: «@TransactionalEventListener (AFTER_COMMIT/BEFORE_COMMIT), ProgrammaticTransaction, ChainedTransaction». Кейс OrderCreated→Inventory после commit.

## Lesson 67 — Spring Security (04 §10) — `[ ]`
- Пакет `me.yuugao.lesson67`
- Классы:
  - `demo/FilterChainJwtDemo.java` — Javadoc: «Filter chain, `SecurityFilterChain`, JWT (access/refresh), BCrypt/Argon2 (связь L34), method security». Кейс stateless REST + `Bearer`.
  - `demo/OAuth2ResourceDemo.java` — Javadoc: «OAuth2 login/resource server, OIDC, opaque vs JWT, CSRF/CORS для SPA». Разобрать: `authorization_code` flow.

## Lesson 68 — Actuator + Micrometer (04 §11) — `[ ]`
- Пакет `me.yuugao.lesson68`
- Классы:
  - `demo/ActuatorEndpointsDemo.java` — Javadoc: «Actuator: health/info/metrics/prometheus, `@Endpoint`, exposure». Разобрать: `show-details` безопасность.
  - `demo/MicrometerOtelDemo.java` — Javadoc: «Micrometer: Counter/Timer/DistributionSummary, tags/cardinality (задел L129), OTel bridge». Кейс timing из L59 → registry.

## Lesson 69 — Spring Cloud (04 §12) — `[ ]`
- Пакет `me.yuugao.lesson69`
- Классы:
  - `demo/DiscoveryGatewayFeignDemo.java` — Javadoc: «Eureka/Consul discovery, Gateway routes/filters, OpenFeign clients (ImportBeanDefinitionRegistrar-задел L58)». Разобрать: `lb://service`.
  - `demo/ResilienceStreamDemo.java` — Javadoc: «Resilience4j (CB/bulkhead/limiter/retry) + Spring Cloud Stream (Kafka/Rabbit binder)». Мостик к L103–L107.

## Lesson 70 — Spring Batch (04 §13) — `[ ]`
- Пакет `me.yuugao.lesson70`
- Классы:
  - `demo/JobStepChunkDemo.java` — Javadoc: «Job/Step/Chunk (reader-processor-writer), restartability (JobRepository), skip/retry». Кейс CSV→DB 1M строк.
  - `demo/PartitioningDemo.java` — Javadoc: «Partitioning/scaling, `TaskExecutor`, remote chunking задел». Разобрать: `JpaPagingItemReader` vs `JdbcCursor`.

## Lesson 71 — Spring Cache (04 §14) — `[ ]`
- Пакет `me.yuugao.lesson71`
- Классы:
  - `demo/CacheableEvictDemo.java` — Javadoc: «@Cacheable/@CacheEvict/@CachePut, key/condition/unless (SpEL), Caffeine vs Redis». Кейс stampede + `sync=true`.
  - `demo/ProxySelfCacheDemo.java` — Javadoc: «Cache через proxy: self-invocation не кэширует (связь L59/L66), `CacheManager` tuning». Разобрать: `unless=#result==null`.

## Lesson 72 — Spring Messaging (04 §15) — `[ ]`
- Пакет `me.yuugao.lesson72`
- Классы:
  - `demo/JmsAmqpKafkaTemplateDemo.java` — Javadoc: «JmsTemplate/RabbitTemplate/KafkaTemplate, `@JmsListener/@RabbitListener/@KafkaListener`, DLQ». Задел к L103–L105.
  - `demo/ExactlyOnceOutboxDemo.java` — Javadoc: «Idempotency key, outbox table + relay, transactional listener». Мостик к L102/L114.

## Lesson 73 — Integration/Camel (04 §16) — `[ ]`
- Пакет `me.yuugao.lesson73`
- Классы:
  - `demo/IntegrationFlowDemo.java` — Javadoc: «Spring Integration: Channel/Endpoint (Transformer/Filter/Router/Aggregator), DSL flow». Разобрать: `DirectChannel` vs `QueueChannel`.
  - `demo/CamelRouteDemo.java` — Javadoc: «Camel Route (`from().choice().to()`), EIP (задел L115), error handler DLQ». Кейс file→transform→kafka.

## Lesson 74 — Spring Boot Test (04 §17) — `[ ]`
- Пакет `me.yuugao.lesson74`
- Классы:
  - `demo/SliceTestsDemo.java` — Javadoc: «@SpringBootTest vs slices (@WebMvcTest/@DataJpaTest), @MockitoBean, @ServiceConnection (связь L45)». Разобрать: context caching.
  - `demo/RestDocsOpenApiDemo.java` — Javadoc: «Test-driven docs: REST Docs snippets vs OpenAPI/Springdoc, contract задел (L46)». Мостик к L76.

## Lesson 75 — Spring Modulith (04 §18) — `[ ]`
- Пакет `me.yuugao.lesson75`
- Классы:
  - `demo/ModulithStructureDemo.java` — Javadoc: «Модули (package by feature), `ApplicationModule`, events (`@ApplicationModuleListener`), ArchUnit-проверка». Кейс монолит→модули без microservices.

## Lesson 76 — REST Docs/OpenAPI (04 §19) — `[ ]`
- Пакет `me.yuugao.lesson76`
- Классы:
  - `demo/OpenApiDocsDemo.java` — Javadoc: «Springdoc OpenAPI: `@Operation/@Schema`, grouping, ProblemDetail schema (связь L62)». Разобрать: `springdoc-openapi` vs REST Docs точность.

## Lesson 77 — Миграции Spring/Boot (04 §20) — `[ ]`
- Пакет `me.yuugao.lesson77`
- Классы:
  - `demo/Boot2To3MigrationDemo.java` — Javadoc: «Boot 2→3: `javax.*`→`jakarta.*`, AOT, `@MockBean`→`@MockitoBean`, OpenRewrite recipes». Чеклист миграции.
  - `demo/Spring5To6Demo.java` — Javadoc: «Framework 5→6: Java 17 baseline, observability (Micrometer), Native hints». Разобрать: `spring-core` removals.

---

# Раздел 5. Persistence & Databases (`docs/05-persistence-db.md`, 20 уроков)

## Lesson 78 — JDBC (05 §1) — `[ ]`
- Пакет `me.yuugao.lesson78`
- Классы:
  - `demo/DriverDataSourceDemo.java` — Javadoc: «DriverManager SPI vs DataSource, URL, `Connection` lifecycle». Разобрать: `META-INF/services/java.sql.Driver`.
  - `demo/StatementsBatchDemo.java` — Javadoc: «Statement vs PreparedStatement (SQL-injection) vs CallableStatement, `addBatch/executeBatch`, fetchSize». Кейс `rewriteBatchedStatements`.
  - `demo/TransactionManualDemo.java` — Javadoc: «autoCommit=false→commit/rollback, savepoint, isolation set». Мостик к L80/L66.

## Lesson 79 — Connection pools (05 §2) — `[ ]`
- Пакет `me.yuugao.lesson79`
- Классы:
  - `demo/HikariTuningDemo.java` — Javadoc: «HikariCP: `ConcurrentBag`, maxPool `CPU*2`, `maxLifetime/idleTimeout/connectionTimeout`, `isValid` vs `SELECT 1`». Кейс sizing.
  - `demo/PoolMetricsLeakDemo.java` — Javadoc: «MXBean (active/idle/awaiting), leakDetectionThreshold, housekeeper, `evict`». Кейс connection leak find.

## Lesson 80 — Транзакции БД (05 §3) — `[ ]`
- Пакет `me.yuugao.lesson80`
- Классы:
  - `demo/AcidIsolationDemo.java` — Javadoc: «ACID, уровни READ_COMMITTED/REPEATABLE_READ/SERIALIZABLE, dirty/non-repeatable/phantom». Разобрать: PostgreSQL vs MySQL дефолты.
  - `demo/MvccLockingDemo.java` — Javadoc: «MVCC (snapshot), pessimistic (`FOR UPDATE`) vs optimistic (`@Version`), deadlocks». Кейс lost update → `@Version`.

## Lesson 81 — SQL (05 §4) — `[ ]`
- Пакет `me.yuugao.lesson81`
- Классы:
  - `demo/JoinsCteWindowDemo.java` — Javadoc: «JOIN (inner/left/full/cross/self), CTE (recursive), window (`ROW_NUMBER/RANK/LEAD/LAG`)». Кейс top-N per group.
  - `demo/ExplainDdlTclDemo.java` — Javadoc: «DDL/DML/DCL/TCL, EXPLAIN (seq scan vs index), `ANALYZE`». Разобрать: `EXPLAIN (ANALYZE, BUFFERS)`.

## Lesson 82 — PostgreSQL (05 §5) — `[ ]`
- Пакет `me.yuugao.lesson82`
- Классы:
  - `demo/PgIndexesDemo.java` — Javadoc: «B-tree/GIN/GiST/BRIN, `pg_stat_statements`, partial/expression index». Кейс `LIKE '%x'` → trigram GIN.
  - `demo/PgPartitioningDemo.java` — Javadoc: «Partitioning (range/list), `VACUUM/ANALYZE`, `FOR UPDATE SKIP LOCKED` queue». Кейс outbox relay poll.

## Lesson 83 — MySQL (05 §6) — `[ ]`
- Пакет `me.yuugao.lesson83`
- Классы:
  - `demo/InnoDbGtidDemo.java` — Javadoc: «InnoDB (clustered PK, gap locks), GTID replication, `SELECT ... FOR UPDATE` отличия от PG». Разобрать: `AUTO_INCREMENT` vs `SEQUENCE`.

## Lesson 84 — Hibernate ORM (05 §7) — `[ ]`
- Пакет `me.yuugao.lesson84`
- Классы:
  - `demo/LifecycleStatesDemo.java` — Javadoc: «Transient/persistent/detached/removed, flush/dirty checking, `@Version`». Разобрать: `persist vs merge`.
  - `demo/NPlusOneCacheDemo.java` — Javadoc: «N+1 (связь L64), 1st/2nd level cache, Query cache, `@BatchSize`». Кейс `FetchType.LAZY` + `LazyInitializationException`.

## Lesson 85 — JPA (05 §8) — `[ ]`
- Пакет `me.yuugao.lesson85`
- Классы:
  - `demo/EntityGraphCriteriaDemo.java` — Javadoc: «EntityGraph, Criteria API, `@Embeddable`, converters». Разобрать: type-safe query vs String.
  - `demo/MappingInheritanceDemo.java` — Javadoc: «Inheritance (SINGLE_TABLE/JOINED/TABLE_PER_CLASS), `@MappedSuperclass`, Envers-задел». Разобрать: discriminator.

## Lesson 86 — Spring Data JPA (05 §9, дубль 04 §7 — сделать акцент на Boot) — `[ ]`
- Пакет `me.yuugao.lesson86`
- Классы:
  - `demo/QueryMethodsPageDemo.java` — Javadoc: «Derived/Auditing/pagination (`Pageable`), `JpaSpecificationExecutor`, QueryDSL». Кейс `Page<N>` + `countQuery`.
  - `demo/LockingHintsDemo.java` — Javadoc: «`@Lock(PESSIMISTIC_WRITE)`, `@QueryHints`, `@Modifying(clearAutomatically)`». Кейс concurrent decrement.

## Lesson 87 — Flyway/Liquibase (05 §10) — `[ ]`
- Пакет `me.yuugao.lesson87`
- Классы:
  - `demo/MigrationVersioningDemo.java` — Javadoc: «Versioned (`V1__`) vs repeatable, checksum, `validate/migrate/repair`, rollback (Liquibase)». Кейс zero-downtime (expand/contract).
  - `demo/BranchingCiDemo.java` — Javadoc: «Ветки+миграции conflicts, CI `flyway validate`, Testcontainers-проверка (связь L45)». Разобрать: `baseline`.

## Lesson 88 — MongoDB (05 §11) — `[ ]`
- Пакет `me.yuugao.lesson88`
- Классы:
  - `demo/DocumentModelingDemo.java` — Javadoc: «Embed vs reference, `_id`, indexes (compound/TTL/text), aggregation pipeline». Кейс order+items embed.
  - `demo/TransactionsChangeDemo.java` — Javadoc: «Multi-doc TX (replica set), Change Streams (CDC задел L94), Spring Data Mongo». Разобрать: когда не Mongo.

## Lesson 89 — Redis (05 §12) — `[ ]`
- Пакет `me.yuugao.lesson89`
- Классы:
  - `demo/DataStructuresLuaDemo.java` — Javadoc: «Strings/Hashes/Sets/SortedSets/Streams, Lua atomicity, `SET NX EX`». Кейс rate limiter Lua.
  - `demo/CachePatternsRedlockDemo.java` — Javadoc: «Cache-aside/write-through, stampede (probabilistic early), Redlock споры, client-side caching». Мостик к L113/L112.

## Lesson 90 — Elasticsearch (05 §13) — `[ ]`
- Пакет `me.yuugao.lesson90`
- Классы:
  - `demo/MappingAnalysisDemo.java` — Javadoc: «Mapping/text vs keyword, analyzer, inverted index». Кейс `match vs term`.
  - `demo/SearchAggregationsDemo.java` — Javadoc: «Bool/must/filter, aggregations, pagination (`search_after` vs from/size)». Разобрать: `track_total_hits`.

## Lesson 91 — Cassandra (05 §14) — `[ ]`
- Пакет `me.yuugao.lesson91`
- Классы:
  - `demo/PartitionClusteringDemo.java` — Javadoc: «Partition key vs clustering, denormalization (query-first), tombstones». Кейс time-series bucket.
  - `demo/ConsistencyTuningDemo.java` — Javadoc: «ONE/QUORUM/ALL, `IF NOT EXISTS` LWT, repair». Мостик к CAP L93.

## Lesson 92 — ClickHouse (05 §15) — `[ ]`
- Пакет `me.yuugao.lesson92`
- Классы:
  - `demo/MergeTreeDemo.java` — Javadoc: «MergeTree (partition/order by), columnar, `Replacing/Aggregating`, batch insert». Кейс analytics vs OLTP.

## Lesson 93 — CAP/PACELC/BASE (05 §16) — `[ ]`
- Пакет `me.yuugao.lesson93`
- Классы:
  - `demo/CapPacelcDemo.java` — Javadoc: «CAP (P-выбор C/A), PACELC (E: L vs C), BASE/eventual, CRDT-идея». Разобрать: `Mongo PC/EC vs Cassandra PA/EL` (связь L06 docs).
  - `demo/ConsistencyModelsDemo.java` — Javadoc: «Strong/eventual/causal/read-your-writes, quorum `R+W>N`». Кейс `W=QUORUM,R=QUORUM`.

## Lesson 94 — Distributed TX (05 §17) — `[ ]`
- Пакет `me.yuugao.lesson94`
- Классы:
  - `demo/TwoPcSagaDemo.java` — Javadoc: «2PC (blocking) vs Saga (choreo/orch, compensations), TCC задел». Кейс booking saga.
  - `demo/OutboxCdcDemo.java` — Javadoc: «Outbox table + relay (SKIP LOCKED, связь L82) + Debezium CDC, inbox». Мостик к L114/L102.

## Lesson 95 — CQRS/Event Sourcing (05 §18) — `[ ]`
- Пакет `me.yuugao.lesson95`
- Классы:
  - `demo/CqrsSplitDemo.java` — Javadoc: «Command vs Query models, read-model projector, eventual consistency UX». Кейс `OrderWrite`→`OrderView`.
  - `demo/EventSourcingDemo.java` — Javadoc: «Event store (append-only), aggregate rehydrate, snapshots, versioning/upcasting». Разобрать: GDPR-стирание проблема.

## Lesson 96 — Моделирование данных (05 §19) — `[ ]`
- Пакет `me.yuugao.lesson96`
- Классы:
  - `demo/NormalizationScdDemo.java` — Javadoc: «1NF–BCNF/denorm, OLTP vs OLAP (star/snowflake), SCD1/2/3». Кейс SCD2 `valid_from/to`.
  - `demo/DomainMappingDemo.java` — Javadoc: «OO→relational impedance, `@Embedded/ElementCollection`, value objects». Мостик к DDD L142.

## Lesson 97 — Performance DB (05 §20) — `[ ]`
- Пакет `me.yuugao.lesson97`
- Классы:
  - `demo/SlowQueryExplainDemo.java` — Javadoc: «slow log, EXPLAIN (BUFFERS), missing index, `N+1` DB-сторона». Кейс sequential scan fix.
  - `demo/BatchingPartitionDemo.java` — Javadoc: «Batch (`rewriteBatched`, `@BatchSize`), partitioning (связь L82), connection sizing (связь L79)». Кейс 100K insert.

---

# Раздел 6. Distributed, Messaging & Microservices (`docs/06-distributed-msg.md`, 20 уроков)

## Lesson 98 — Основы распределённых систем (06 §1) — `[ ]`
- Пакет `me.yuugao.lesson98`
- Классы:
  - `demo/FallaciesConsensusDemo.java` — Javadoc: «Fallacies (8), Two Generals/Byzantine/FLP невозможности, transparency». Разобрать: зачем timeouts везде.
  - `demo/CapPacelcRecapDemo.java` — Javadoc: «CAP/PACELC повтор на языке messaging (мостик L93), consistency spectrum». Задел к Kafka/Rabbit выбору.

## Lesson 99 — REST дизайн (06 §2) — `[ ]`
- Пакет `me.yuugao.lesson99`
- Классы:
  - `demo/ResourceModelingDemo.java` — Javadoc: «Ресурсы (nouns), Richardson 0–3, HATEOAS, versioning (`/v1` vs header)». Антипаттерн `/getUser`.
  - `demo/StatusErrorIdempotencyDemo.java` — Javadoc: «Status codes, ProblemDetail (связь L62), idempotency-key (задел L102), pagination (`cursor` vs `offset`)». Кейс `POST` retry safe.

## Lesson 100 — gRPC (06 §3) — `[ ]`
- Пакет `me.yuugao.lesson100`
- Классы:
  - `demo/ProtobufContractDemo.java` — Javadoc: «protobuf: varint/field_number, backward/forward compat (never reuse number), `.proto` service». Разобрать: `optional` vs default.
  - `demo/StreamingDeadlineDemo.java` — Javadoc: «4 RPC (unary/server/client/bidi), HTTP/2 multiplex/HPACK, deadlines/cancellation, status codes». Кейс deadline propagation.

## Lesson 101 — GraphQL (06 §4) — `[ ]`
- Пакет `me.yuugao.lesson101`
- Классы:
  - `demo/SchemaNPlusOneDemo.java` — Javadoc: «Schema (type/query/mutation), N+1, DataLoader batching, depth/cost limits». Кейс nested `author.books`.
  - `demo/FederationSubscriptionsDemo.java` — Javadoc: «Federation (subgraphs), subscriptions (WS/SSE задел L116), persisted queries». Разобрать: когда REST лучше.

## Lesson 102 — Idempotency / exactly-once (06 §5) — `[ ]`
- Пакет `me.yuugao.lesson102`
- Классы:
  - `demo/IdempotencyKeyDemo.java` — Javadoc: «Idempotency-Key (`SET NX`+TTL, связь L89), deduplication table, retry-safe `POST`». Кейс payment double-charge.
  - `demo/ExactlyOnceLimitsDemo.java` — Javadoc: «at-most/at-least/exactly-once, Two Generals-предел, Kafka TX (задел L103)». Разобрать: end-to-end только с idempotent consumer.

## Lesson 103 — Apache Kafka (06 §6) — `[ ]`
- Пакет `me.yuugao.lesson103`
- Классы:
  - `demo/PartitionsIsrDemo.java` — Javadoc: «Topic/partition/offset, ISR, replication, `acks/min.insync`, KRaft (без ZK)». Разобрать: key→partition ordering.
  - `demo/TransactionsStreamsDemo.java` — Javadoc: «TX (idempotent producer+`transactional.id`), Streams (KTable/KStream), Schema Registry, consumer groups/rebalance». Кейс `read_committed`.

## Lesson 104 — RabbitMQ (06 §7) — `[ ]`
- Пакет `me.yuugao.lesson104`
- Классы:
  - `demo/ExchangesRoutingDemo.java` — Javadoc: «Exchanges (direct/topic/fanout/headers), bindings, quorum vs classic, DLX». Кейс `orders.*` topic.
  - `demo/AckPrefetchDemo.java` — Javadoc: «ack/nack/reject, prefetch QoS, publisher confirms, TTL/max-length». Кейс poison message → DLX.

## Lesson 105 — Spring Kafka/AMQP (06 §8) — `[ ]`
- Пакет `me.yuugao.lesson105`
- Классы:
  - `demo/ListenerContainerDemo.java` — Javadoc: «`@KafkaListener` (ackMode, concurrency, errorHandler), `RabbitListener`, `KafkaTemplate` TX». Разобрать: `SeekToCurrentErrorHandler`+DLT.
  - `demo/RetryDltDemo.java` — Javadoc: «Retry topic (non-blocking), DLT, `DeadLetterPublishingRecoverer`». Кейс exponential backoff без block.

## Lesson 106 — Микросервисные паттерны (06 §9) — `[ ]`
- Пакет `me.yuugao.lesson106`
- Классы:
  - `demo/SagaGatewayBffDemo.java` — Javadoc: «Saga (choreo vs orch, связь L94), API Gateway vs BFF, Strangler, Service Mesh задел». Кейс strangler legacy.
  - `demo/DecompositionDemo.java` — Javadoc: «Decomposition (by subdomain/DDD, связь L142), DB per service, shared kernel антипаттерн». Разобрать: distributed monolith ловушка.

## Lesson 107 — Resilience (06 §10) — `[ ]`
- Пакет `me.yuugao.lesson107`
- Классы:
  - `demo/CircuitBreakerBulkheadDemo.java` — Javadoc: «Resilience4j: CB (sliding window), bulkhead, rate limiter, retry+hedge, timeouts». Кейс cascading failure stop.
  - `demo/FallbackChaosDemo.java` — Javadoc: «Fallbacks, bulkhead isolation (thread vs semaphore), chaos задел L137». Разобрать: `TimeLimiter` + virtual threads.

## Lesson 108 — Service Discovery & Config (06 §11) — `[ ]`
- Пакет `me.yuugao.lesson108`
- Классы:
  - `demo/EurekaConsulDemo.java` — Javadoc: «Eureka/Consul discovery, health checks, client-side LB, `spring.config.import=configserver` (связь L60)». Разобрать: CAP у discovery.
  - `demo/ExternalConfigRefreshDemo.java` — Javadoc: «Config Server (Git/JDBC/Vault), `@RefreshScope`/Bus, versioned config». Кейс secret rotation.

## Lesson 109 — API Gateway (06 §12) — `[ ]`
- Пакет `me.yuugao.lesson109`
- Классы:
  - `demo/GatewayFiltersDemo.java` — Javadoc: «Gateway (Spring Cloud Gateway): routes/predicates/filters, rate limiting, JWT relay». Разобрать: `RewritePath` + `CircuitBreaker` filter.
  - `demo/BffAggregationDemo.java` — Javadoc: «BFF aggregation (fan-in, `Mono.zip` связь L63), versioning, `ProblemDetail` passthrough». Кейс mobile vs web BFF.

## Lesson 110 — Service Mesh (06 §13) — `[ ]`
- Пакет `me.yuugao.lesson110`
- Классы:
  - `demo/SidecarMtlsDemo.java` — Javadoc: «Sidecar (Envoy), mTLS, traffic split/canary, retries outside code». Мостик к Istio L128 — здесь без K8s, концепт + Java-настройки.

## Lesson 111 — Distributed Tracing (06 §14) — `[ ]`
- Пакет `me.yuugao.lesson111`
- Классы:
  - `demo/TraceContextDemo.java` — Javadoc: «Trace/span, W3C `traceparent`, baggage, sampling (head/tail)». Разобрать: `traceId` в MDC (связь L59).
  - `demo/OtelJaegerDemo.java` — Javadoc: «OpenTelemetry SDK + Java agent auto-instrumentation, Jaeger/Tempo exporters». Задел к L132.

## Lesson 112 — Distributed Locking (06 §15) — `[ ]`
- Пакет `me.yuugao.lesson112`
- Классы:
  - `demo/DbZkLockDemo.java` — Javadoc: «DB (`FOR UPDATE SKIP LOCKED`), ZK/etcd ephemeral+session (задел L117), fencing token». Разобрать: split-brain.
  - `demo/RedisLockDemo.java` — Javadoc: «Redis `SET NX PX`+Lua unlock, Redlock споры (связь L89), ShedLock для jobs». Кейс scheduler single-run.

## Lesson 113 — Caching distributed (06 §16) — `[ ]`
- Пакет `me.yuugao.lesson113`
- Классы:
  - `demo/TopologiesInvalidationDemo.java` — Javadoc: «Local vs distributed (Redis/Hazelcast), invalidation (pub/sub, CDC), thundering herd». Кейс `cache stampede` + `singleflight`.
  - `demo/ConsistentHashingDemo.java` — Javadoc: «Consistent hashing, rendezvous, hot keys, `HashTag` (Redis Cluster)». Разобрать: reshard без downtime.

## Lesson 114 — Idempotency/Outbox/CDC (06 §17, углубление L94) — `[ ]`
- Пакет `me.yuugao.lesson114`
- Классы:
  - `demo/OutboxRelayDebeziumDemo.java` — Javadoc: «Outbox relay poll vs log-based CDC (Debezium/Kafka Connect), inbox, ordering per aggregate». Кейс `OrderOutbox→Kafka`.
  - `demo/DedupInboxDemo.java` — Javadoc: «Consumer deduplication (`processed_events`), transactional consume+project». Разобрать: `consumer group` + idempotent handler = effectively-once.

## Lesson 115 — Event-driven architecture (06 §18) — `[ ]`
- Пакет `me.yuugao.lesson115`
- Классы:
  - `demo/EventsCommandsDemo.java` — Javadoc: «Events vs commands, EDA topologies (broker vs mediator), EIP (связь L73), versioning (`v1/v2` upcasting)». Разобрать: `event-carried state transfer`.
  - `demo/ChoreoOrchDemo.java` — Javadoc: «Choreography vs orchestration (Temporal/Camunda задел), saga visualizer». Кейс выбор для L94/L106.

## Lesson 116 — WebSocket/SSE (06 §19) — `[ ]`
- Пакет `me.yuugao.lesson116`
- Классы:
  - `demo/WebSocketStompDemo.java` — Javadoc: «WS handshake, STOMP over WS, heartbeats, horizontal scale (pub/sub broker)». Кейс chat presence.
  - `demo/SsePushDemo.java` — Javadoc: «SSE (`text/event-stream`, `SseEmitter`), retry/id, backpressure (связь L28/L63)». Кейс notifications feed.

## Lesson 117 — Coordination (06 §20) — `[ ]`
- Пакет `me.yuugao.lesson117`
- Классы:
  - `demo/ZkEtcdConsensusDemo.java` — Javadoc: «ZK (znode/ephemeral/sequential) vs etcd (Raft, lease), leader election recipe». Кейс `LeaderLatch`.
  - `demo/ConfigMembershipDemo.java` — Javadoc: «Membership (gossip/SWIM), config distribution, distributed barrier (связь L27)». Задел к K8s leases (L122).

---

# Раздел 7. DevOps, Containers & Observability (`docs/07-devops-observability.md`, 20 уроков)

> Уроки 118–137 — «не-Java» по коду: demo = доки-генераторы (Dockerfile/compose/K8s manifests), shell-чекеры, Java-агенты (Micrometer/OTel/Log). В Javadoc фиксировать: «генерирует манифест/проверяет/объясняет».

## Lesson 118 — Linux/Shell (07 §1) — `[ ]`
- Пакет `me.yuugao.lesson118`
- Классы:
  - `demo/SafeBashDemo.java` — Javadoc: «`set -euo pipefail`, trap, pipes/redirects, `ss/top/journalctl/ulimit` шпаргалка». Генерирует `scripts/check-java.sh` пример.
  - `demo/SshSystemdDemo.java` — Javadoc: «SSH (KEX/auth/config), systemd units, journald persistent vs volatile». Разобрать: `LimitNOFILE` для Java.

## Lesson 119 — Docker (07 §2) — `[ ]`
- Пакет `me.yuugao.lesson119`
- Классы:
  - `demo/DockerfileBestDemo.java` — Javadoc: «FROM/RUN/COPY, shell vs exec, multi-stage, layer cache, .dockerignore, BuildKit `--mount=cache/secret`». Генерирует `Dockerfile` для Boot JAR.
  - `demo/DistrolessNonRootDemo.java` — Javadoc: «distroless/slim, `USER non-root`, `dive` анализ, `JAVA_OPTS` (heap/entropy)». Кейс 400MB→60MB.

## Lesson 120 — Docker Compose (07 §3) — `[ ]`
- Пакет `me.yuugao.lesson120`
- Классы:
  - `demo/ComposeStackDemo.java` — Javadoc: «services/volumes/networks, healthcheck (`pg_isready`), override/profiles, `depends_on: condition`». Генерирует `compose.yaml` (pg+redis+kafka+app).

## Lesson 121 — Registry (07 §4) — `[ ]`
- Пакет `me.yuugao.lesson121`
- Классы:
  - `demo/RegistryScanSignDemo.java` — Javadoc: «Hub/Harbor/ECR, Trivy scan, cosign sign, SBOM (CycloneDX, связь L57)». Чеклист `push→scan→sign→deploy`.

## Lesson 122 — K8s основы (07 §5) — `[ ]`
- Пакет `me.yuugao.lesson122`
- Классы:
  - `demo/PodDeployServiceDemo.java` — Javadoc: «Pod/Deployment/ReplicaSet/Service/ConfigMap/Secret, labels/selectors». Генерирует `k8s/base/*.yaml`.
  - `demo/ProbesResourcesDemo.java` — Javadoc: «Probes (liveness/readiness/startup для медленного Spring), requests/limits/QoS (Guaranteed/Burstable)». Кейс OOMKilled Java без limits.

## Lesson 123 — K8s продолжение (07 §6) — `[ ]`
- Пакет `me.yuugao.lesson123`
- Классы:
  - `demo/StatefulHpaDemo.java` — Javadoc: «StatefulSet (pg/kafka), DaemonSet, Job/CronJob, HPA/VPA, init/sidecar». Разобрать: `volumeClaimTemplates`.
  - `demo/AffinityPdbDemo.java` — Javadoc: «affinity/taints/tolerations, PDB (minAvailable), graceful termination (`preStop`+`terminationGracePeriod`)». Кейс rolling без 503.

## Lesson 124 — K8s networking (07 §7) — `[ ]`
- Пакет `me.yuugao.lesson124`
- Классы:
  - `demo/ServiceIngressPolicyDemo.java` — Javadoc: «Service (ClusterIP/NodePort/LB), Ingress (+controllers), NetworkPolicy (default-deny), DNS, CNI Calico/Cilium». Генерирует `NetworkPolicy` deny-all + allow.

## Lesson 125 — K8s storage (07 §8) — `[ ]`
- Пакет `me.yuugao.lesson125`
- Классы:
  - `demo/PvcCsiDemo.java` — Javadoc: «PV/PVC/StorageClass/CSI, `volumeClaimTemplates` (связь L123), snapshots». Разобрать: `ReadWriteOnce` vs `ReadWriteMany` для Java-логов.

## Lesson 126 — Helm/Kustomize (07 §9) — `[ ]`
- Пакет `me.yuugao.lesson126`
- Классы:
  - `demo/HelmChartDemo.java` — Javadoc: «Chart (templates/values/hooks/subcharts), `helm template --debug`,SemVer chart». Генерирует `helm/app/*` скелет.
  - `demo/KustomizeOverlayDemo.java` — Javadoc: «Kustomize overlays (base/dev/prod), patches, Helm vs Kustomize выбор». Кейс `dev/prod` values.

## Lesson 127 — GitOps (07 §10) — `[ ]`
- Пакет `me.yuugao.lesson127`
- Классы:
  - `demo/ArgoFluxDemo.java` — Javadoc: «ArgoCD/Flux: desired vs live, sync waves, drift/rollback, `Application` CR». Разобрать: `auto-sync` опасность для DB.

## Lesson 128 — Service Mesh на K8s (07 §11) — `[ ]`
- Пакет `me.yuugao.lesson128`
- Классы:
  - `demo/IstioCanaryDemo.java` — Javadoc: «Istio/Linkerd: sidecar inject, mTLS (STRICT), canary (`VirtualService` weight), retries». Мостик к L110 — здесь YAML.

## Lesson 129 — Metrics (07 §12) — `[ ]`
- Пакет `me.yuugao.lesson129`
- Классы:
  - `demo/PromqlMicrometerDemo.java` — Javadoc: «Prometheus (pull), PromQL (rate/histogram_quantile), histograms vs summaries, Alertmanager, Micrometer (связь L68)». Кейс `http_server_requests_seconds` p99.
  - `demo/CardinalityRulesDemo.java` — Javadoc: «Cardinality explosion (userId в tags — нельзя), recording rules, `rate vs irate`». Разобрать: OOM Prometheus.

## Lesson 130 — Dashboards Grafana (07 §13) — `[ ]`
- Пакет `me.yuugao.lesson130`
- Классы:
  - `demo/DashboardAsCodeDemo.java` — Javadoc: «Datasources/panels/variables, dashboards as code (grafonnet), alerting (unified)». Генерирует `grafana/dashboard.json` скелет (JVM+http).

## Lesson 131 — Logs (07 §14) — `[ ]`
- Пакет `me.yuugao.lesson131`
- Классы:
  - `demo/StructuredLoggingDemo.java` — Javadoc: «ELK/EFK vs Loki+Promtail, JSON logs (logstash encoder), MDC traceId (связь L59/L111), levels». Кейс `trace_id` end-to-end.
  - `demo/LokiQueryDemo.java` — Javadoc: «LogQL (`{app="api"} |= "ERROR"`), retention, cardinality labels». Разобрать: high-cardinality в Loki.

## Lesson 132 — Traces (07 §15) — `[ ]`
- Пакет `me.yuugao.lesson132`
- Классы:
  - `demo/OtelJavaAgentDemo.java` — Javadoc: «OTel SDK vs Java agent auto-instrumentation, Jaeger/Tempo, sampling (parentbased_always_on), baggage, W3C». Кейс `-javaagent:opentelemetry-javaagent.jar`.
  - `demo/SpanCustomizationDemo.java` — Javadoc: «Ручные spans (`@WithSpan`, `Tracer.spanBuilder`), attributes/events, context propagation в Kafka (headers)». Мостик L103/L111.

## Lesson 133 — Профилирование Java в prod (07 §16) — `[ ]`
- Пакет `me.yuugao.lesson133`
- Классы:
  - `demo/JfrAsyncDemo.java` — Javadoc: «JFR (`jcmd JFR.start`), async-profiler flame (cpu/alloc/lock), JMC, `GC.log` (связь L21)». Кейс 5GB/sec alloc (связь L37).
  - `demo/ArthasMatProdDemo.java` — Javadoc: «Arthas (`trace/watch/tt`), heap dump MAT в prod (связь L21), JMX safe expose». Разобрать: `tt` re-execute риск.

## Lesson 134 — SRE (07 §17) — `[ ]`
- Пакет `me.yuugao.lesson134`
- Классы:
  - `demo/SliSloBudgetDemo.java` — Javadoc: «SLI/SLO/SLA, error budget (burn rate), toil budget, blameless postmortem, on-call». Кейс `99.9% → 43m/month`, alert на burn.
  - `demo/PostmortemTemplateDemo.java` — Javadoc: «Postmortem template (timeline/5 whys/action items), error budget policy (freeze deploys)». Генерирует `docs/postmortem/TEMPLATE.md`.

## Lesson 135 — IaC (07 §18) — `[ ]`
- Пакет `me.yuugao.lesson135`
- Классы:
  - `demo/TerraformStateDemo.java` — Javadoc: «Terraform: state (remote backend/lock), modules/providers/workspaces, Atlantis plan/apply». Разобрать: `taint` vs `replace`.
  - `demo/PulumiAnsibleDemo.java` — Javadoc: «Pulumi (Java SDK — связь с этим проектом), Crossplane, Ansible (agentless, idempotence)». Кейс JVM-ам в Pulumi Java.

## Lesson 136 — CI/CD на K8s (07 §19) — `[ ]`
- Пакет `me.yuugao.lesson136`
- Классы:
  - `demo/BuildTestScanPushDemo.java` — Javadoc: «Pipeline: build→test (L42–L45)→SAST/SCA (L57)→image (L119)→scan (L121)→push→deploy→canary (Argo Rollouts)». Генерирует `/.github/workflows/k8s.yml`.
  - `demo/RolloutsCanaryDemo.java` — Javadoc: «Argo Rollouts (canary/blueGreen, analysisTemplate с Prometheus), auto-rollback». Кейс `successRate>99%`.

## Lesson 137 — Chaos Engineering (07 §20) — `[ ]`
- Пакет `me.yuugao.lesson137`
- Классы:
  - `demo/ChaosMeshDemo.java` — Javadoc: «Chaos Mesh/Litmus: pod-kill/network-delay/partition, steady-state hypothesis, blast radius». Кейс `kill kafka-leader` + budget check.
  - `demo/GameDayTemplateDemo.java` — Javadoc: «Game days checklist, abort conditions, observability (связь L129–L132)». Генерирует `docs/chaos/gameday-TEMPLATE.md`.

---

# Раздел 8. Architecture, Patterns & Leadership (`docs/08-arch-patterns-leadership.md`, 20 уроков)

## Lesson 138 — SOLID + DRY/KISS/YAGNI (08 §1) — `[ ]`
- Пакет `me.yuugao.lesson138`
- Классы:
  - `demo/SolidViolationsDemo.java` — Javadoc: «SRP/OCP/LSP/ISP/DIP: по 1 нарушению + фикс, LSP-контракт (пред/постусловия)». Кейс `Square extends Rectangle` LSP-break.
  - `demo/DryKissYagniDemo.java` — Javadoc: «DRY (single source), KISS (no clever), YAGNI (no speculative), Law of Demeter (`a.getB().getC()` chain)». Кейс premature abstraction.

## Lesson 139 — GoF паттерны (08 §2) — `[ ]`
- Пакет `me.yuugao.lesson139`
- Классы:
  - `demo/CreationalDemo.java` — Javadoc: «Singleton (enum, EJ3) / Factory Method / Abstract Factory / Builder (EJ2) / Prototype (copy-ctor, не Cloneable) / Object Pool». JDK-примеры: `List.of`, `DocumentBuilderFactory`.
  - `demo/StructuralDemo.java` — Javadoc: «Adapter/Bridge/Composite/Decorator/Facade/Flyweight/Proxy (JDK: `InputStream` decorator, `Collections.unmodifiable`)». Кейс `ForwardingSet` (связь L02).
  - `demo/BehavioralDemo.java` — Javadoc: «Strategy/Observer/Command/State/Template/Visitor/Chain/Mediator/Iterator (JDK: `Comparator`, `Spliterator`)». Кейс visitor vs switch (связь L02).

## Lesson 140 — DI паттерны (08 §3) — `[ ]`
- Пакет `me.yuugao.lesson140`
- Классы:
  - `demo/DiManualVsContainerDemo.java` — Javadoc: «Constructor/setter/field (ручной), pure DI без Spring, Service Locator антипаттерн». Мостик к Spring L58: почему container.
  - `demo/CompositionRootDemo.java` — Javadoc: «Composition root, object graph, lifetime (singleton/transient), circular detection вручную». Кейс `new` в конструкторе — DI-нарушение.

## Lesson 141 — Архитектурные паттерны (08 §4) — `[ ]`
- Пакет `me.yuugao.lesson141`
- Классы:
  - `demo/LayeredHexagonalDemo.java` — Javadoc: «Layered vs Hexagonal (ports/adapters) vs Onion vs Clean: dependency rule (наружу — наружу нельзя)». Кейс `domain` без `spring/*` imports (ArchUnit-проверка L55).
  - `demo/CqrsEventDrivenMonoDemo.java` — Javadoc: «CQRS lite, Event-Driven, Microservices vs Modular Monolith (связь L75/L106), Serverless задел». Кейс выбор для стартапа.

## Lesson 142 — DDD (08 §5) — `[ ]`
- Пакет `me.yuugao.lesson142`
- Классы:
  - `demo/TacticalDddDemo.java` — Javadoc: «Entity/VO/Aggregate/Root/Repository/Domain Service/Domain Events, invariants в Root». Кейс `Order.addItem` проверяет инвариант.
  - `demo/StrategicDddDemo.java` — Javadoc: «Ubiquitous Language, Bounded Context/Context Map, ACL/Shared Kernel/Conformist». Кейс `BillingContext` ACL над `Legacy`.

## Lesson 143 — System Design база (08 §6) — `[ ]`
- Пакет `me.yuugao.lesson143`
- Классы:
  - `demo/CapacityEstimationDemo.java` — Javadoc: «Back-of-envelope: QPS×size×retention, latency numbers (L1/SSD/network), read/write ratio, hot spots». Кейс `10K RPS × 1KB × 30d`.
  - `demo/BuildingBlocksDemo.java` — Javadoc: «LB (L4/L7), caching (CDN, связь L113), sharding (`hash mod` vs consistent), replication (sync/async), queueing». Разобрать: `CDN` vs `cache-aside`.

## Lesson 144 — System Design кейсы (08 §7) — `[ ]`
- Пакет `me.yuugao.lesson144`
- Классы:
  - `demo/UrlShortenerTwitterDemo.java` — Javadoc: «Shortener (base62, KGS) + Twitter (fan-out on write vs read, timeline)». Кейсы capacity + API (связь L99/L143).
  - `demo/ChatRateLimiterDemo.java` — Javadoc: «Chat (WS, связь L116, presence), Rate Limiter (token/leaky/sliding, Redis Lua связь L89)». Кейс TDD-объект (связь L47).

## Lesson 145 — Структуры данных (08 §8) — `[ ]`
- Пакет `me.yuugao.lesson145`
- Классы:
  - `demo/LinearStructuresDemo.java` — Javadoc: «Массивы/списки/стек/очередь/heap/hashmap: Big-O, `ArrayList` рост ×1.5». LeetCode easy/medium задел.
  - `demo/TreesGraphsDemo.java` — Javadoc: «BST/AVL/Red-Black, Trie, графы, union-find». Кейс `TreeMap` Red-Black vs `HashMap` TreeBin (связь L06/L24).

## Lesson 146 — Алгоритмы (08 §9) — `[ ]`
- Пакет `me.yuugao.lesson146`
- Классы:
  - `demo/SortingSearchingDemo.java` — Javadoc: «quick/merge/heap/radix, binary/BFS/DFS, `Arrays.sort` dual-pivot». Разобрать: stability.
  - `demo/GraphsDpDemo.java` — Javadoc: «Dijkstra/Bellman-Ford/A*/toposort, DP/backtracking/greedy/divide, sliding window/two pointers/monotonic stack». LeetCode hard задел.

## Lesson 147 — Concurrent structures (08 §10) — `[ ]`
- Пакет `me.yuugao.lesson147`
- Классы:
  - `demo/LockFreeStructuresDemo.java` — Javadoc: «CHM/SkipList (связь L24), Michael-Scott queue, Treiber stack (CAS loop, связь L26)». Разобрать: ABA fix.
  - `demo/DisruptorDemo.java` — Javadoc: «Disruptor: ring buffer, single-writer, cache-line padding (`@Contended`, связь L26), batching». Кейс vs `ArrayBlockingQueue`.

## Lesson 148 — Clean Code & Refactoring (08 §11) — `[ ]`
- Пакет `me.yuugao.lesson148`
- Классы:
  - `demo/CodeSmellsDemo.java` — Javadoc: «Smells (long method/large class/primitive obsession/data clump), Boy Scout, tech debt quadrant». Кейс `OrderService` 500 строк → split.
  - `demo/RefactoringStepsDemo.java` — Javadoc: «Fowler techniques: extract (method/class/parameter object), replace conditional with polymorphism». Показать до/после.

## Lesson 149 — Code Review (08 §12, углубление L54) — `[ ]`
- Пакет `me.yuugao.lesson149`
- Классы:
  - `demo/SecurityPerfReviewDemo.java` — Javadoc: «Security review (OWASP задел L155: injection/secrets), perf review (alloc/N+1, связь L64/L97)». Чеклист approve/request-changes.
  - `demo/FeedbackToolsDemo.java` — Javadoc: «Конструктивный фидбек (SBI), Gerrit/GitHub PRs, `CODEOWNERS`, auto-assign». Кейс «как сказать нет».

## Lesson 150 — Testing strategy (08 §13) — `[ ]`
- Пакет `me.yuugao.lesson150`
- Классы:
  - `demo/PyramidTrophyDemo.java` — Javadoc: «Pyramid vs trophy, ICE score, contract (Pact, связь L46/L74), microservices testing (testcontainers, связь L45)». Кейс `ice: speed×confidence/cost`.
  - `demo/FlakyStrategyDemo.java` — Javadoc: «Flaky (quarantine, retry analyzer — когда зло), parallelization, `TestExecutionListener`». Разобрать: `Thread.sleep` в тестах — ban.

## Lesson 151 — Документация/RFC/ADR/C4 (08 §14) — `[ ]`
- Пакет `me.yuugao.lesson151`
- Классы:
  - `demo/RfcAdrDemo.java` — Javadoc: «RFC (problem/alternatives), ADR (MADR: context/decision/consequences, связь L55), tech specs». Генерирует `docs/adr/0002-*.md`.
  - `demo/C4DiagramsDemo.java` — Javadoc: «C4 (context/container/component/code), PlantUML/Mermaid/Structurizr, sequence». Генерирует `docs/c4/*.puml`.

## Lesson 152 — Senior soft skills (08 §15) — `[ ]`
- Пакет `me.yuugao.lesson152`
- Классы:
  - `demo/MentorshipFeedbackDemo.java` — Javadoc: «Менторство (ladder задач, code review как обучение), 1:1, delegation». Кейс план роста junior→middle (связь L157).
  - `demo/StakeholderConflictDemo.java` — Javadoc: «Stakeholder management, переговоры (trade-offs latency vs cost), конфликты (non-violent), influence without authority». Сценарии.

## Lesson 153 — Agile/Lean/DevOps культуры (08 §16) — `[ ]`
- Пакет `me.yuugao.lesson153`
- Классы:
  - `demo/ScrumKanbanFlowDemo.java` — Javadoc: «Scrum/Kanban/XP, estimation (story points vs t-shirt, no-estimates), retro format». Кейс WIP limits.
  - `demo/MetricsLeadTimeDemo.java` — Javadoc: «Lead/cycle time, flow efficiency, DORA (deploy freq/lead time/MTTR/change fail)». Мостик к SRE L134 + CI L51.

## Lesson 154 — Performance engineering (08 §17) — `[ ]`
- Пакет `me.yuugao.lesson154`
- Классы:
  - `demo/LawsQueueingDemo.java` — Javadoc: «Latency vs throughput, Amdahl/Gustafson, Little `L=λW`, M/M/1 queueing». Кейс `threads = latency×QPS`.
  - `demo/ProfilerDrivenDemo.java` — Javadoc: «Profiler-driven (JFR/async, связь L133), premature optimization — зло, JMH (связь L46)». Кейс «сначала flame, потом код».

## Lesson 155 — Security для разработчика (08 §18) — `[ ]`
- Пакет `me.yuugao.lesson155`
- Классы:
  - `demo/OwaspTopDemo.java` — Javadoc: «OWASP Top 10 (injection/broken access/crypto failures), secure coding (allowlist, parameterized), secrets (Vault, связь L60/L108)». Кейс `PreparedStatement` (связь L78).
  - `demo/ThreatSupplyDemo.java` — Javadoc: «STRIDE/DREAD, ASVS checklist, SAST/DAST/SCA (связь L57), SBOM sign (связь L121)». Кейс `npm audit`→Java аналог.

## Lesson 156 — Cost optimization (08 §19) — `[ ]`
- Пакет `me.yuugao.lesson156`
- Классы:
  - `demo/FinOpsRightsizingDemo.java` — Javadoc: «FinOps: tagging/showback, right-sizing (requests/limits связь L122), spot/reserved, autoscaling (HPA/VPA связь L123)». Кейс ` facture -30%`.
  - `demo/MultiTenancyDemo.java` — Javadoc: «Multi-tenancy (silo/pool/bridge, TenantScope связь L58), noisy neighbor, cost per tenant». Кейс `tenant_id` sharding.

## Lesson 157 — Career path (08 §20) — `[ ]`
- Пакет `me.yuugao.lesson157`
- Классы:
  - `demo/LevelsExpectationsDemo.java` — Javadoc: «Junior→Middle→Senior→Staff→Principal: scope/influence/ambiguity, staff archetypes (tech lead/architect/solver/right hand)». Чеклист готовности Senior (из README: HB/HashMap/G1/lock-free).
  - `demo/VisibilityGrowthDemo.java` — Javadoc: «Tech radar, open source, conferences/writing, mentoring (связь L152), promotion packet». План на 6 месяцев.

---

## Правила приёмки урока (Definition of Done)

- [ ] Пакет `me.yuugao.lessonNN` создан, `LessonNN.run()` вызывается из `Main` (или из агрегирующего раннера по разделам).
- [ ] 3–5 `demo/*Demo.java` с полным классовым + методом Javadoc (русский, как в lesson01), `toString()`-отчёт.
- [ ] Покрыты все `### Теория` буллеты подраздела + минимум 1 `### Практика/Кейсы` воспроизведён кодом.
- [ ] Для уроков 42+ — JUnit-тесты в `src/test/java/me/yuugao/lessonNN/`.
- [ ] `.\gradlew build` зелёный, кодировка UTF-8, Java 21.
- [ ] Ссылка на `docs/0X-*.md §Y` в классовом Javadoc (`@see`).

## Порядок работы (предлагаемый)

0. L01-дополнение (сначала): закрыть гэпы `lesson01` — `TypesLiterals/VarScope/Widening/SwitchPattern/FloatMoney/InitOrder/PassByValue-фикс` (см. аудит Lesson 01). Без этого фундамент дырявый.
1. L02–L18 (Core остаток) — закрыть фундамент, отточить Javadoc-стиль.
2. L19–L27 (JVM/JMM/GC/threads/locks/juc) — самый важный для Senior; делать с JMH-замерами.
3. L28–L37 (Flow/proxies/JPMS/NIO/modern/diag) — метапрограммирование + диагностика.
4. L38–L57 (Build/Test/Eng) — параллельно перевести проект на version catalogs + CI + Testcontainers.
5. L58–L77 (Spring) — выделить `spring/` source-set, иначе Boot-зависимости засорят Core.
6. L78–L97 (Persistence) — Testcontainers PG/Redis/Kafka обязательны.
7. L98–L117 (Distributed) — сначала без брокеров (in-memory fake), потом с Testcontainers.
8. L118–L137 (DevOps) — Dockerfile/compose/K8s YAML как артефакты уроков, не только Java.
9. L138–L157 (Arch/Leadership) — паттерны + System Design + soft skills, финальный `ARCHITECTURE.md`.

## Следующий шаг

- [ ] Согласовать этот TODO (границы уроков, имена demo-классов).
- [ ] Дополнить `lesson01` до 8 demo-классов по аудиту выше (НЕ начинать lesson02 пока lesson01 не done).
- [ ] После каждого урока отмечать `[x]` здесь и обновлять `Main.java`.

## Проверка полноты покрытия (аудит 2026-10-06)

- Сверены заголовки `##` всех 8 файлов `docs/`: 01→18, 02→19, 03→20, 04→20, 05→20, 06→20, 07→20, 08→20 = 157 подразделов = 157 уроков (L01–L157). Пропусков нет.
- Каждый урок покрывает все `### Теория` буллеты + все `### Практика/Кейсы` своего подраздела (см. Definition of Done: минимум 1 кейс кодом, для Core/Advanced — все кейсы).
- Исключение L01: из-за объёма §1 (24 теории + 6 кейсов) норма 3–5 demo расширена до 8 demo — зафиксировано выше.
- Дубль `Spring Data JPA` (04 §7 и 05 §9) разведен: L64 — web/репозитории со стороны Spring, L86 — Boot-акцент (paging/locking/hints), чтобы не дублировать.
