# Раздел 1. Java Core & Foundations

> Базовый раздел дерева изучения Java до уровня Senior. Охватывает фундамент языка, который Senior обязан знать досконально: от синтаксиса и ООП до Stream API, Generics, NIO, java.time, Reflection и аннотаций. Версии Java: 8 (LTS), 11 (LTS), 17 (LTS), 21 (LTS) — явно отмечены там, где это критично. Каждая тема содержит теорию, реальные материалы (книги, статьи, документация Oracle) и трудные многоступенчатые инженерные кейсы.

## Оглавление подразделов

1. [Синтаксис, типы, операторы, управляющие конструкции](#1-синтаксис-типы-операторы-управляющие-конструкции)
2. [ООП: классы, наследование, полиморфизм, инкапсуляция; abstract vs interface; default/static/private методы интерфейсов; records; sealed](#2-ооп)
3. [Inner/nested/anonymous/local классы, лямбды, functional interfaces, method references](#3-inner-nested-anonymous-local-лямбды-functional-interfaces-method-references)
4. [String/StringBuilder/StringPool, иммутабельность, новые методы Java 11+](#4-string-stringbuilder-stringpool-иммутабельность-новые-методы-java-11)
5. [Исключения (checked/unchecked, try-with-resources, custom, chaining, suppressed)](#5-исключения)
6. [Collections (List/Set/Map/Queue, внутреннее устройство HashMap, fail-fast vs fail-safe)](#6-collections)
7. [Generics (PECS, type erasure, bridge methods)](#7-generics)
8. [Enum (EnumSet/EnumMap, singleton)](#8-enum)
9. [I/O и NIO (InputStream/Reader, java.nio.file Path/Files, Channels/ByteBuffer, сериализация)](#9-io-и-nio)
10. [java.time API (LocalDate/Time, ZonedDateTime, Instant, Duration/Period, DateTimeFormatter)](#10-javatime-api)
11. [equals/hashCode контракт, Comparable/Comparator](#11-equalshashcode-контракт-comparablecomparator)
12. [Stream API (intermediate/terminal, parallel, collectors, reduce, flatMap, groupingBy)](#12-stream-api)
13. [Optional (правильное использование, anti-patterns)](#13-optional)
14. [Reflection API (база: Class/Method/Field/Constructor)](#14-reflection-api)
15. [Annotations (встроенные, мета-аннотации, создание)](#15-annotations)
16. [Varargs, static initializer, init order, final/finally/finalize](#16-varargs-static-initializer-init-order-finallyfinalize)
17. [Wrapper classes, autoboxing, Integer cache](#17-wrapper-classes-autoboxing-integer-cache)
18. [Unicode/Charset/кодировки](#18-unicodecharsetкодировки)

---

## 1. Синтаксис, типы, операторы, управляющие конструкции

### Теория

- **Java — статически типизированный, строго типизированный объектно-ориентированный язык** с C-подобным синтаксисом. Код компилируется в байткод JVM (`.class`), который исполняется виртуальной машиной. Это даёт "write once, run anywhere", но требует установленной JRE.
- **Программа на Java** состоит из одного или нескольких классов в файлах `.java`. Один `public` класс на файл, имя файла должно совпадать с именем public-класса. Метод `public static void main(String[] args)` — точка входа.
- **Примитивные типы (8 шт.)**: `byte` (8 bit, −128..127), `short` (16 bit), `int` (32 bit, по умолчанию для целочисленных литералов), `long` (64 bit, литерал `L`), `float` (32 bit, литерал `F`), `double` (64 bit, по умолчанию для дробных литералов), `char` (16 bit безнаковый, хранит UTF-16 code unit), `boolean` (JVM не фиксирует размер, обычно 1 байт в массивах, 4 в стеке).
- **Ссылочные типы**: классы, интерфейсы, массивы, перечисления (`enum`), аннотации. Переменная ссылочного типа хранит ссылку на объект в куче (heap). `null` — допустимое значение по умолчанию для всех ссылочных типов.
- **Литералы**: десятичные (`42`), восьмеричные (`052`), шестнадцатеричные (`0x2A`), двоичные с Java 7 (`0b101010`), с подчёркиваниями для читаемости (`1_000_000`). Суффиксы `L` (long), `F` (float), `D` (double). Строковые литералы в `"`, символьные в `'`. Литерал `null` — специальный.
- **Unicode в исходниках**: с Java 5 поддерживаются Unicode-идентификаторы (`int π = 3;`). Сам файл может быть в любой кодировке, но `javac` требует указать её через `-encoding`. Java Text Blocks (JEP 378, Java 15) — многострочные строки `"""..."""` с управляемыми отступами.
- **Операторы**: арифметические (`+ - * / %`), инкремент/декремент (`++ --` prefix/postfix), отношения (`== != < > <= >=`), логические (`&& || !` с коротким замыканием; `& | ^` без него, также побитовые), побитовые (`~ << >> >>>`), `instanceof`, условный `? :`, присваивание с операцией (`+= -= *= /= %= &= |= ^= <<= >>= >>>=`).
- **Приоритет операторов** определяет порядок вычисления без скобок: постфиксные → унарные → `* / %` → `+ -` → сдвиги → отношения → `== !=` → побитовое `&` → `^` → `|` → `&&` → `||` → тернарный → присваивание. Senior должен помнить, что `&&` и `||` — короткозамкнутые: `a && b` не вычисляет `b`, если `a` ложно.
- **Битовое беззнаковое смещение `>>>`** заполняет нулями слева, в отличие от `>>`, который сохраняет знак. Используется для хеширования (`h ^ (h >>> 16)` в `HashMap.hash`), работы с цветами (ARGB), расчётов в беззнаковой арифметике.
- **Управляющие конструкции**: `if/else if/else`, `switch` (classic с `byte/short/int/char/enum/String`, Java 14+ switch expressions `yield`), `for` (classic и for-each по `Iterable`), `while`, `do-while`, `break` (с меткой для выхода из вложенных циклов), `continue` (с меткой), `return`. Switch expression JEP 361 (Java 14 стандарт, Java 12-13 preview): `int r = switch(day) { case MONDAY -> 1; default -> 0; };` с `yield` для многострочных блоков.
- **Pattern matching для `instanceof`** (JEP 394, Java 16): `if (obj instanceof String s) { ... use s ... }` — переменная `s` объявляется и приводится, область видимости корректно определяется flow analysis.
- **Switch по pattern** (JEP 441, Java 21): `case Integer i when i > 0 -> ...`, `case null -> ...`, `case Point(int x, int y) -> ...` (record deconstruction). Важно: case `null` без явной обработки вызывал NPE в классическом switch.
- **Массивы**: объекты, длина фиксирована, доступ по индексу O(1), проверка границ с `ArrayIndexOutOfBoundsException`. Длина — `arr.length` (поле, не метод). Многомерные массивы — массивы массивов (jagged possible). `int[] a = {1,2,3};` — literal-init.
- **`var` (local variable type inference, JEP 323, Java 11)** — компилятор выводит тип из инициализатора: `var list = new ArrayList<String>();` эквивалентно `ArrayList<String> list = ...`. Применимо только к локальным переменным с инициализатором, нельзя для полей, параметров, return-типов. `var` НЕ ключевое слово, а reserved type name — можно использовать как идентификатор в старом коде.
- **Передача параметров**: Java строго pass-by-value. Для примитивов передаётся копия значения, для ссылочных типов — копия ссылки. Метод не может изменить примитив-аргумент вызывающего, но может мутировать объект по ссылке. String-аргумент нельзя изменить у вызывающего (иммутабелен).
- **Область видимости**: блок `{ }` определяет scope. Переменные уровня блока, метода, класса (static/instance). Переменная в `for(int i...)` видна только в цикле. Перекрытие имён запрещено: локальная переменная не может совпадать с параметром.
- **Числовое расширение (widening primitive conversion)**: `byte/short/char` автоматически расширяются до `int` в арифметических выражениях. Поэтому `byte b = 1; b = b + 1;` не компилируется (результат `int`), нужно `b = (byte)(b + 1);` или `b += 1;` (составное присваивание неявно кастует).
- **Сужение (narrowing)** требует явного cast: `int i = 300; byte b = (byte) i;` даст `44` (300 mod 256). Float → int теряет дробную часть: `(int) 3.9 == 3`.
- **Оператор `instanceof`** проверяет тип объекта во время выполнения. С Java 16 поддерживает pattern binding (см. выше). `null instanceof X` всегда `false`. Проверяет отношение is-a (включая интерфейсы).
- **`String` equals vs `==`**: `==` сравнивает ссылки, `.equals()` — содержимое. String literals и `String.intern()` — в пуле, поэтому `"a" == "a"` true, но `new String("a") == "a"` false. Один из самых частых источников багов.
- **Float/double неточность**: `0.1 + 0.2 != 0.3` (результат `0.30000000000000004`). Для денежных расчётов — `BigDecimal` с конструктором от `String` (`new BigDecimal("0.1")`), не от `double`.
- **JLS и JVMS**: точные спецификации языка — Java Language Specification (JLS) и JVM Specification. Senior должен уметь читать JLS для тонких вопросов (например, JLS §15.26.1 про простое присваивание, JLS §16 про definite assignment).
- **Text Blocks** (Java 15): тройные кавычки, поддержка `\` для line continuation, `\s` для сохранения trailing whitespace. Управление отступом через выравнивание по закрывающей `"""`. Экранирование не нужно для `"`, но `"""` внутри требует `\`. Полезно для SQL, JSON, HTML в коде.
- **`record` patterns и `sealed`** здесь упоминаются, но подробно раскрываются в разделе ООП.

### Материалы

- Книга: "Java: The Complete Reference" Herbert Schildt (11th ed., McGraw-Hill, 2021) — Chapter 2: Overview of Java, Chapter 3: Data Types, Variables, and Arrays, Chapter 4: Operators
- Книга: "Core Java Volume I—Fundamentals" Cay S. Horstmann (12th ed., Prentice Hall, 2021) — Chapter 3: Fundamental Programming Structures in Java
- Книга: "Effective Java" Joshua Bloch (3rd ed., Addison-Wesley, 2018) — Item 9 (prefer `try-with-resources`), Item 10 (override `equals`), Item 11 (override `hashCode`)
- Документация Oracle (Java Tutorials — Language Basics): https://docs.oracle.com/javase/tutorial/java/nutsandbolts/index.html
- Документация Oracle (Variables): https://docs.oracle.com/javase/tutorial/java/nutsandbolts/variables.html
- Документация Oracle (Operators): https://docs.oracle.com/javase/tutorial/java/nutsandbolts/opsummary.html
- Документация Oracle (Control Flow): https://docs.oracle.com/javase/tutorial/java/nutsandbolts/flow.html
- JEP 378: Text Blocks (Java 15): https://openjdk.org/jeps/378
- JEP 394: Pattern Matching for instanceof (Java 16): https://openjdk.org/jeps/394
- JEP 441: Pattern Matching for switch (Java 21): https://openjdk.org/jeps/441
- Java Language Specification (JLS): https://docs.oracle.com/javase/specs/jls/se21/html/index.html
- Baeldung — Java Switch Expression: https://www.baeldung.com/java-switch
- Baeldung — `var` keyword in Java: https://www.baeldung.com/java-var

### Практика / Кейсы

**Кейс 1.1. Побитовая арифметика и хеш-функции.** Цель: реализовать хеш-функцию, эквивалентную `HashMap.hash`, и измерить качество распределения.
- Шаги: (1) Реализовать `static int hash(Object key) { int h; return (key == null) ? 0 : (h = key.hashCode()) ^ (h >>> 16); }` (2) Сгенерировать 1 000 000 случайных строк длиной 5–15, посчитать их хеши. (3) Гистограмма по bucket-ам `h & (n-1)` для `n = 16` (4) Сравнить коллизии с «наивным» `key.hashCode()`. (5) Объяснить, зачем `>>> 16` (распространение старших бит в младшие, важно для маленьких `n`).
- Что проверить: число коллизий, uniformity (хи-квадрат), есть ли разница для `n=16` vs `n=1024`. Сделать выводы про power-of-two sizing.

**Кейс 1.2. `==` для примитивов и boxed-типов.**
- Постановка: объяснить, почему `Integer a = 127, b = 127; a == b` → true, а `Integer a = 128, b = 128; a == b` → false (без явного приведения).
- Шаги: (1) Воспроизвести в коде. (2) Дизассемблировать `javap -c` и увидеть `Integer.valueOf(int)`. (3) Прочитать JLS §5.1.7 (boxing conversion) и исходник `Integer.IntegerCache` (диапазон −128..127). (4) Запустить JVM с `-XX:AutoBoxCacheMax=1000` и проверить, что 1000 теперь тоже `==`. (5) Объяснить, почему этого флага нет для `Long/Short/Byte` (они кэшируют только −128..127 без возможности расширения).
- Что проверить: корректность объяснения, умение читать байткод, понимание кэша.

**Кейс 1.3. Switch expression + sealed + record pattern.**
- Постановка: реализовать AST-калькулятор для выражений `Num`, `Add`, `Mul` через sealed-интерфейс и switch-expression с record-pattern (Java 21).
- Шаги: (1) Объявить `sealed interface Expr permits Num, Add, Mul {}` и record-реализации. (2) Метод `int eval(Expr e)` через `switch (e) { case Num(int v) -> v; case Add(Expr a, Expr b) -> eval(a) + eval(b); ... }`. (3) Доказать exhaustiveness (без default компилируется, так как sealed). (4) Добавить `Sub` — проверить, что компилятор требует обновить switch (compile error). (5) Бенчмарк `eval` против visitor-паттерна через JMH.
- Что проверить: exhaustiveness, extensibility trade-off sealed vs visitor, производительность (pattern matching обычно не хуже visitor).

**Кейс 1.4. Float-неточность в финансовых расчётах.**
- Постановка: реализовать вычисление процентов `0.1 + 0.2` тремя способами (`double`, `BigDecimal.valueOf`, `new BigDecimal(String)`) и показать разницу.
- Шаги: (1) Вывести результат каждого способа с `MathContext.DECIMAL64`. (2) Сравнить с эталоном. (3) Объяснить, почему `BigDecimal.valueOf(0.1)` эквивалентен `new BigDecimal(Double.toString(0.1))` и не равен `new BigDecimal(0.1)`. (4) Реализовать `Money` value-object с explicit rounding (`RoundingMode.HALF_EVEN`). (5) Покрыть тестами граничные случаи: `0.005` round → `0.00` (HALF_EVEN) vs `0.01` (HALF_UP).
- Что проверить: понимание binary floating-point, RoundingMode, правила banker's rounding.

**Кейс 1.5. Init order и порядок вычисления операторов.**
- Постановка: объяснить и пронумеровать вывод `System.out.println` в коде с комбинацией `static {}`, instance `{}`, конструкторов, наследования и короткозамкнутого `&&`.
- Шаги: (1) Написать классы `A` с `static { print("A.static"); }` и `B extends A` со статическим и instance-блоками. (2) Использовать `int x = computeX();` где `computeX` печатает шаг. (3) Создать `new B()` два раза и проследить, что static-блок выполнится один раз. (4) Включить `i = 0; print(++i + " " + i++ + " " + i);` — убедиться, что результат `1 1 3` (вычисление слева направо, операторы с побочным эффектом). (5) Использовать short-circuit `if (a != null && a.size() > 0)` — почему безопасно, что будет с `&`.
- Что проверить: точное знание JLS §12.4 (initialization), §15.7 (evaluation order), §12.5 (creation of new class instances).

**Кейс 1.6. Pass-by-value на ссылочных типах.**
- Постановка: объяснить, почему метод `swap(Integer a, Integer b)` не меняет значения у вызывающего, даже если мутировать через reflection.
- Шаги: (1) Реализовать наивный `void swap(Integer a, Integer b) { Integer t = a; a = b; b = t; }` и доказать no-op. (2) Через reflection (`field.setAccessible(true)` на `value` у `Integer`) — попытка мутировать. (3) Объяснить, почему это ломает инварианты `IntegerCache` и кэш `==`. (4) Реализовать обёртку `Holder<T>` с публичным полем — теперь swap работает. (5) Сравнить с C++ `swap(int& a, int& b)` — что принципиально отличается в Java.
- Что проверить: понимание ссылок как значений, рефлексия как «эскапизм», design trade-off инкапсуляции.

---

## 2. ООП

### Теория

- **Классы и объекты**. Класс — шаблон, описывающий состояние (поля) и поведение (методы). Объект — экземпляр класса в куче. Конструкторы (`ClassName(params) {...}`) инициализируют объект; если не объявлен — компилятор добавляет default no-arg конструктор, который вызывает `super()`. Конструктор не наследуется.
- **Модификаторы доступа (4 уровня)**: `public` — всем; `protected` — пакет + наследники; package-private (по умолчанию, без модификатора) — только в своём пакете; `private` — только в своём классе. Senior должен помнить, что `protected` шире package-private, и что `protected`-метод доступен из другого пакета только через наследование (не через ссылку на суперкласс).
- **Инкапсуляция** — скрытие внутреннего состояния за приватными полями и публичными accessor-методами (`getX`/`setX`). Позволяет менять реализацию, не ломая клиентов, и поддерживать инварианты. Канонический пример: `java.util.ArrayList` скрывает массив и управляет размером.
- **Наследование** в Java — single inheritance для классов (один суперкласс). Класс неявно наследует `java.lang.Object`, если не указан `extends`. `final`-класс нельзя наследовать (`String`, `Integer`, `LocalDate`). Множественное наследование типа — через интерфейсы.
- **Полиморфизм** — способность метода работать с разными типами. Статический (compile-time) — overloading (перегрузка по сигнатуре). Динамический (runtime) — overriding (переопределение виртуального метода); JVM использует vtable для диспетчеризации.
- **Переопределение (`@Override`)**: сигнатура должна совпадать; ковариантный return-тип разрешён с Java 5 (можно вернуть подтип); доступ не может быть более узким; checked exceptions — только те, что объявлены в суперклассе или их подтипы. Static-методы не переопределяются, а скрываются (hiding) — вызов определяется типом ссылки, не объекта.
- **Перегрузка (overloading)**: методы с одинаковым именем, но разной сигнатурой (число/тип параметров). Различие только в return-типе не допускается. Разрешение перегрузки — compile-time, по «most specific method» (JLS §15.12.2). Частая ловушка: `null`-аргумент выбирает наиболее специфичный не-примитивный тип.
- **`abstract` класс**: не может быть инстанциирован, может содержать `abstract`-методы (без тела) и конкретные методы/поля. Используется для общей реализации с шаблонными методами (Template Method pattern). Может иметь конструкторы — вызываются из дочерних через `super(...)`.
- **`interface`**: контракт, все методы по умолчанию `public abstract` (до Java 7). С Java 8 — `default` и `static` методы. С Java 9 — `private` методы (для устранения дублирования между default-методами). Поля — `public static final` неявно. Класс может реализовывать много интерфейсов.
- **`default`-метод интерфейсов (Java 8, JEP 126)**: позволяет добавлять методы в существующие интерфейсы без breaking changes. Пример: `Iterable.forEach(Consumer)`, `Collection.stream()`. Если класс реализует два интерфейса с одинаковым default-методом — компилятор требует явного переопределения (`Interface.super.method()`).
- **`static`-методы в интерфейсах (Java 8)**: утилитарные методы, не наследуются реализациями. `Stream.of(...)`, `Predicate.isEqual(...)`. Нельзя переопределить статические методы интерфейса в реализующем классе (hiding).
- **`private`-методы в интерфейсах (Java 9, JEP 213)**: вспомогательные методы для default-методов. Не наследуются, не доступны реализациям. `private static` — общие утилиты, `private` (instance) — для default-методов.
- **`abstract class` vs `interface`**: класс — для "is-a" с общим состоянием и поведением; интерфейс — для "can-do" контракта. Senior правило: предпочитать интерфейсы и композицию наследованию (Effective Java Item 18). Если нужно общее состояние или конструкторы с инвариантами — абстрактный класс.
- **`record` (Java 14 preview, Java 16 final, JEP 395)**: компактный носитель данных. `record Point(int x, int y) {}` неявно генерирует: `private final int x, y;`, canonical constructor, `x()`, `y()` accessor-ы, `equals`, `hashCode`, `toString`. Неявно `final`, не может наследовать другие классы, но может реализовывать интерфейсы. Может иметь compact constructor для валидации: `record Range(int lo, int hi) { public Range { if (lo > hi) throw ...; } }`.
- **`sealed` (Java 15 preview, Java 17 final, JEP 409)**: ограничивает иерархию наследования. `sealed interface Shape permits Circle, Square, Triangle {}`. Все permitted-классы должны быть `final`, `sealed` или `non-sealed`. Позволяет компилятору проверять exhaustiveness в switch. Используется для algebraic data types.
- **`non-sealed`** — открывает иерархию обратно для произвольного наследования (отменяет sealed-ограничение для этого подкласса).
- **Композиция vs наследование**: наследование нарушает инкапсуляцию (подкласс зависит от реализации суперкласса — Effective Java Item 18). Композиция + делегирование (Forwarding) — безопаснее. `HashSet` можно обернуть в `ForwardingSet` и добавлять instrumentation.
- **Ключевое слово `final`**: класс (нельзя наследовать), метод (нельзя переопределять), поле (нельзя переприсвоить; для объекта — ссылка неизменна, но сам объект мутабелен). `final` на параметрах и локальных переменных — для lambda-capture и явной документации намерения.
- **`this` и `super`**: `this` — ссылка на текущий объект; `super` — ссылка на часть объекта, соответствующую суперклассу (для вызова переопределённых методов и конструктора). `super()` должен быть первой строкой конструктора, если есть. `this(...)` вызывает другой конструктор того же класса, тоже первым.
- **Static-члены** принадлежат классу, а не объекту. Доступ через `ClassName.member` (через ссылку — bad practice, warning). Static-методы не имеют `this`, не могут напрямую обращаться к instance-членам. Static-блоки инициализируют класс (один раз, при первой загрузке).
- **`instanceof` и pattern matching**: `if (obj instanceof String s)` — Java 16+, переменная `s` в scope только когда проверка прошла (flow-sensitive). Сужает и кастует одновременно.
- **Полиморфизм и виртуальный вызов**: `Animal a = new Dog(); a.sound();` вызовет `Dog.sound()` (динамическая диспетчеризация). Static-методы и `private`/`final`-методы вызываются статически. JIT может девиртуализировать финальные методы в inline.
- **Object-методы**: `equals`, `hashCode`, `toString`, `getClass`, `clone` (`protected`, не рекомендуется без `Cloneable`), `finalize` (deprecated с Java 9, removed в Java 18 как `finalizable`), `wait/notify/notifyAll` (для монитора).
- **Value semantics для record**: `equals` сравнивает по компонентам, не по ссылке. `Point(1,2).equals(new Point(1,2))` → true. Важно для использования в `Map`/`Set`.
- **Record vs Lombok @Data**: record — нативный, но нельзя наследовать, нельзя мутировать (по умолчанию); Lombok более гибкий, но требует аннотаций и генерации кода. В новых проектах предпочтительнее record + builder pattern, если нужен immutability + гибкая сборка.

### Материалы

- Книга: "Effective Java" Joshua Bloch (3rd ed., Addison-Wesley, 2018) — Item 10 (equals), Item 11 (hashCode), Item 12 (toString), Item 15 (minimize accessibility), Item 16 (use accessor methods not public fields), Item 18 (composition over inheritance), Item 19 (design for inheritance or prohibit it), Item 22 (interfaces only for defining types), Item 23 (class hierarchy over tagged classes)
- Книга: "Core Java Volume I—Fundamentals" Cay S. Horstmann (12th ed., 2021) — Chapter 5: Inheritance, Chapter 6: Interfaces, Lambda Expressions, and Inner Classes
- Книга: "Modern Java in Action" Urma, Fusco, Mycroft (Manning, 2018) — Chapter 8 (Collection factory), Chapter 13 (default methods)
- Книга: "Java Generics and Collections" Maurice Naftalin & Philip Wadler (O'Reilly, 2006) — Chapter 1–2 (fundamentals of generics and subtyping)
- Документация Oracle (Classes and Objects): https://docs.oracle.com/javase/tutorial/java/javaOO/index.html
- Документация Oracle (Inheritance): https://docs.oracle.com/javase/tutorial/java/IandI/index.html
- Документация Oracle (Interfaces and Inheritance): https://docs.oracle.com/javase/tutorial/java/IandI/createinterface.html
- Документация Oracle (Records, Java 17): https://docs.oracle.com/en/java/javase/17/language/records.html
- Документация Oracle (Sealed Classes, Java 17): https://docs.oracle.com/en/java/javase/17/language/sealed-classes-and-interfaces.html
- Документация Oracle (Java Language Changes Summary): https://docs.oracle.com/en/java/javase/21/language/java-language-changes-summary.html
- JEP 395: Records (final in Java 16): https://openjdk.org/jeps/395
- JEP 409: Sealed Classes (final in Java 17): https://openjdk.org/jeps/409
- JEP 213: Milling Project Coin (private interface methods, Java 9): https://openjdk.org/jeps/213
- Baeldung — Java default methods: https://www.baeldung.com/java-default-methods
- Baeldung — Guide to Java Records: https://www.baeldung.com/java-record-keyword
- Baeldung — Sealed Classes in Java 17: https://www.baeldung.com/java-sealed-classes-interfaces

### Практика / Кейсы

**Кейс 2.1. Diamond problem с default-методами.**
- Постановка: класс реализует два интерфейса с одинаковым default-методом. Объяснить поведение, реализовать корректное разрешение.
- Шаги: (1) Создать `interface A { default String name() { return "A"; } }` и `interface B { default String name() { return "B"; } }`. (2) Класс `C implements A, B` — без явного override не компилируется (compile error: class C inherits unrelated defaults for name() from types A and B). (3) Реализовать `String name() { return A.super.name(); }` — выбрать A. (4) Добавить интерфейс `D extends A` с переопределением `name()` — теперь правило "most specific" выбирает D. (5) Сделать иерархию из 4 интерфейсов и вручную проследить разрешение по JLS §9.4.1.
- Что проверить: точное понимание правил, умение читать compile-ошибки, понимание "most specific" для interface inheritance.

**Кейс 2.2. Record с валидацией и "wither" (immutability + copy).**
- Постановка: реализовать immutable `Money` record с валидацией (amount ≥ 0, currency ≠ null) и методами `withAmount`, `withCurrency`, возвращающими новый record.
- Шаги: (1) `record Money(BigDecimal amount, Currency currency)` с compact constructor `public Money { Objects.requireNonNull(currency); if (amount.signum() < 0) throw new IllegalArgumentException(); }`. (2) Реализовать `withAmount(BigDecimal a)` → `new Money(a, currency)`. (3) Покрыть тестами: null currency → NPE, отрицательный amount → IAE. (4) Сериализовать через Jackson — проверить, что canonical constructor вызывается при десериализации (важно для сохранения инвариантов). (5) Сравнить производительность с mutable классом через JMH (record обычно не медленнее).
- Что проверить: immutability, validation, integration с фреймворками, безопасность десериализации.

**Кейс 2.3. Sealed AST + switch-exhaustiveness.**
- Постановка: реализовать domain model для выражений и заставить компилятор требовать обновление switch при добавлении нового типа.
- Шаги: (1) `sealed interface Expr permits Num, Var, Add, Mul, Neg {}` с record-реализациями. (2) `String pretty(Expr e)` через switch expression без default — компилируется. (3) Добавить `record Pow(Expr base, int exp) implements Expr` — теперь `pretty` выдаёт compile error. (4) Реализовать visitor параллельно, сравнить с pattern matching (visitor более многословен, но не требует sealed). (5) Бенчмарк visitor vs switch — обычно switch быстрее из-за `tableswitch`/`lookupswitch`.
- Что проверить: работу sealed + switch, способность рассуждать об открытости/закрытости иерархий (expression problem).

**Кейс 2.4. Template Method vs Strategy (через interface).**
- Постановка: реализовать алгоритм расчёта цены заказа двумя способами: abstract class (Template Method) и интерфейс + композиция (Strategy). Сравнить testability и flexibility.
- Шаги: (1) `abstract class PriceCalculator { final double calc(Order o) { return base(o) * discount(o) * tax(o); } abstract double discount(Order o); ... }` (2) Версия со Strategy: `PriceCalculator` принимает `DiscountStrategy`, `TaxStrategy` интерфейсы. (3) Написать юнит-тесты — Strategy-версия легко мокается, Template Method требует наследования иерархии. (4) Добавить новый тип скидки — в Strategy это новый класс, в Template Method — новый подкласс. (5) Объяснить, почему Effective Java Item 18 предпочитает композицию.
- Что проверить: понимание design trade-offs, тестопригодность, dependency injection.

**Кейс 2.5. Инициализация и порядок конструкторов с inheritance.**
- Постановка: предсказать вывод программы с многоуровневой иерархией и static/instance-init-блоками.
- Шаги: (1) Класс `A` с `static { print("A.s"); }`, `{ print("A.i"); }`, `A() { print("A()"); }`. Аналогично `B extends A`, `C extends B`. (2) Запустить `new C()` — порядок: `A.s, B.s, C.s, A.i, A(), B.i, B(), C.i, C()`. (3) Объяснить по JLS §12.4 (класс инициализируется лениво при первом активном использовании) и §12.5 (при создании объекта: сначала суперкласс, потом instance-init, потом конструктор). (4) Добавить обращение к `C.class` без создания — static-блок выполнится, instance — нет. (5) Доказать, что `Class.forName("C")` тоже триггерит инициализацию.
- Что проверить: точное знание порядка инициализации, умение читать JLS.

**Кейс 2.6. `clone()` и почему его избегать.**
- Постановка: реализовать глубокое клонирование объекта с вложенными mutable-коллекциями через `Cloneable` и через copy-constructor. Сравнить.
- Шаги: (1) Класс `Order implements Cloneable` с `List<Item> items` (mutable). (2) Наивный `clone()` — shallow copy, `cloned.items == original.items` → мутируя cloned, ломаем original. (3) Глубокий clone с ручным копированием `items`. (4) Copy-constructor `Order(Order other)` — explicit, без рефлексии, типобезопасен. (5) Объяснить, почему Effective Java Item 13 говорит "Provide a method for copying instead of `clone`" — `clone` нарушает инварианты final-полей, не работает без `Cloneable` (маркерный интерфейс без методов), легко выстрелить в ногу.
- Что проверить: глубокое понимание shallow vs deep copy, design выбора copy-метода.

---

## 3. Inner/Nested/Anonymous/Local классы, лямбды, functional interfaces, method references

### Теория

- **Nested-классы в Java делятся на 4 категории** (JLS §8.5): static nested (объявлен как `static` внутри другого класса), inner (non-static nested, имеет неявную ссылку на enclosing instance), local (объявлен внутри метода) и anonymous (local без имени). С Java 16 — `static`-члены разрешены и в inner-классах (JEP 395 prerequisite).
- **Static nested class**: `Outer.StaticNested`. Не имеет доступа к instance-членам Outer (только к static). Не требует экземпляра Outer для создания: `new Outer.StaticNested()`. Пример: `Map.Entry` — static nested interface в `Map`. Идиоматично для вспомогательных классов, тесно связанных с outer.
- **Inner class** (`Outer.Inner`): имеет неявную ссылку на enclosing instance (`Outer.this`). Создаётся только через экземпляр: `outer.new Inner()`. Может обращаться к private-полям Outer. Компилятор генерирует синтетическое поле `this$0`. Используется для итераторов, listener-ов, builder-ов.
- **Anonymous class**: выражение `new Type(args) { ... }` создаёт безымянный класс, реализующий/расширяющий `Type`. Используется для одноразовых реализаций (Comparator, Runnable). Имеет ссылку на enclosing instance, если создаётся в instance-контексте. С Java 8 заменяется лямбдами, но всё ещё нужен для классов с несколькими методами или state.
- **Local class**: объявляется внутри метода, имеет доступ к local-переменным метода (только `final` или effectively final). Имя видно только в этом методе. Используется редко, но полезно для специфичных helper-классов.
- **`final` / effectively final**: локальная переменная, используемая в local/anonymous class или lambda, должна быть `final` или effectively final (не переприсваивается после инициализации). Причина: instance local-класса переживает stack-frame метода (если сохранён в поле), и копия переменной «замораживается». Иначе семантика была бы неоднозначной.
- **Lambda (Java 8, JEP 126)**: анонимная функция `(params) -> body`. Тип — функциональный интерфейс (SAM — single abstract method). Байткод: компилятор генерирует `invokedynamic` с bootstrap-методом `LambdaMetafactory.metafactory`, который на первом вызове создаёт класс-реализацию через `MethodHandle` (часто через hidden classes с Java 15). В отличие от анонимного класса, не создаёт отдельный `.class` файл заранее.
- **Lambda vs anonymous class**: лямбда не вводит новую область видимости (`this` относится к enclosing class); anonymous — имеет свой `this`. Лямбда с состоянием (mutable) — не получится напрямую, только через capture mutable-holder. Лямбда работает только с функциональными интерфейсами.
- **Functional interface (SAM)**: интерфейс ровно с одним абстрактным методом (default/static/private — не в счёт). `@FunctionalInterface` — опциональная аннотация для проверки компилятором. Примеры из JDK: `Function<T,R>`, `Predicate<T>`, `Consumer<T>`, `Supplier<T>`, `BiFunction<T,U,R>`, `UnaryOperator<T>`, `BinaryOperator<T>`, `Runnable`, `Callable<V>`, `Comparator<T>`.
- **Method references (Java 8)**: компактная запись лямбды, когда тело — просто вызов существующего метода. 4 вида: (1) static: `Math::sqrt` (2) instance метод конкретного объекта: `System.out::println` (3) instance метод произвольного объекта: `String::length` → `(String s) -> s.length()` (4) constructor: `ArrayList::new`.
- **Capture в лямбдах**: лямбда захватывает значения (effective final) переменных и `this` (если обращается к instance-членам). Не захватывает состояние в момент создания; изменение enclosing-поля видно. Для thread-safety важно не мутировать shared state из lambda.
- **`var` в лямбда-параметрах (Java 11, JEP 323)**: `(var x, var y) -> x + y`. Полезно для аннотаций: `(@NonNull var x, @Nullable var y) -> ...`. Type inference работает.
- **Closures и escape analysis**: если лямбда не захватывает состояние (stateless) — JVM кэширует единственный экземпляр (через `LambdaMetafactory`). Если захватывает — каждый раз новый объект, но escape analysis может его стэк-аллоцировать. Senior должен понимать, что `() -> doSomething()` не аллоцирует в hot-path.
- **`this` в лямбде**: refers to enclosing `this` (lexical scope). В anonymous class — `this` это сам анонимный объект. Это ключевое отличие. Чтобы получить ссылку на саму лямбду — нельзя напрямую (её даже не существует как named type).
- **Exception в лямбдах**: functional interface-ы из `java.util.function` не объявляют checked exceptions. Если нужно бросить checked — либо оборачивать в `RuntimeException`, либо писать свой SAM (`ThrowingFunction<T,R,E extends Exception>`), либо использовать Either-монаду. Senior должен уметь работать с обоими подходами.
- **`Function.identity()`** vs `x -> x` — семантически эквивалентно, но `Function.identity()` возвращает константную инстанцию, а `x -> x` создаёт новую каждый раз (до JIT-оптимизации). Микрооптимизация, но в hot loops заметна.
- **Специализированные функциональные интерфейсы**: `IntFunction`, `IntPredicate`, `ToIntFunction` и т.д. — для примитивов, чтобы избежать boxing. В Stream API: `IntStream`, `LongStream`, `DoubleStream`. Senior должен знать, что `Stream<Integer>` почти всегда хуже `IntStream`.
- **Composition**: `andThen`, `compose` на `Function`; `and`, `or`, `negate` на `Predicate`; `andThen` на `Consumer`. Позволяет строить pipeline декларативно.
- **Currying в Java**: `Function<X, Function<Y, Z>>` — вручную, т.к. нативной поддержки нет. Полезно для partial application: `multiply = x -> y -> x * y; times2 = multiply.apply(2);`.
- **Сериализация лямбд**: если target-type implements `Serializable`, лямбда может быть сериализована (но не рекомендуется — implementation-defined). Лучше — отдельный класс.
- **Inner class и memory leaks**: anonymous inner class с implicit reference на outer часто является причиной утечек (например, `Handler` в Android, держащий Activity). Senior должен узнавать паттерн и применять `static nested` + WeakReference там, где нужно.

### Материалы

- Книга: "Effective Java" Joshua Bloch (3rd ed., Addison-Wesley, 2018) — Item 24 (favor static member classes over non-static), Item 42 (lambda vs anonymous class), Item 43 (method references), Item 44 (favor standard functional interfaces), Item 45 (use streams judiciously)
- Книга: "Modern Java in Action" Urma, Fusco, Mycroft (Manning, 2018) — Chapter 3 (Lambda expressions), Chapter 4 (Working with Streams)
- Книга: "Core Java Volume I—Fundamentals" Cay S. Horstmann (12th ed., 2021) — Chapter 6: Interfaces, Lambda Expressions, and Inner Classes (раздел 6.3 Inner Classes, 6.4 Lambda Expressions)
- Документация Oracle (Nested Classes): https://docs.oracle.com/javase/tutorial/java/javaOO/nested.html
- Документация Oracle (Lambda Expressions): https://docs.oracle.com/javase/tutorial/java/javaOO/lambdaexpressions.html
- Документация Oracle (Method References): https://docs.oracle.com/javase/tutorial/java/javaOO/methodreferences.html
- Документация Oracle (Functional Interfaces in `java.util.function` Javadoc): https://docs.oracle.com/en/java/javase/21/docs/api/java.base/java/util/function/package-summary.html
- JEP 126: Lambda Expressions and Virtual Extension Methods (Java 8): https://openjdk.org/jeps/126
- Baeldung — Nested Classes in Java: https://www.baeldung.com/java-nested-classes
- Baeldung — Anonymous Classes in Java: https://www.baeldung.com/java-anonymous-classes
- Baeldung — Lambda vs Anonymous Class: https://www.baeldung.com/java-lambdas-vs-anonymous-class
- Baeldung — Java 8 Functional Interfaces: https://www.baeldung.com/java-8-functional-interfaces
- Baeldung — Method References in Java: https://www.baeldung.com/java-method-references
- Oracle Technical Article — Making the Most of Java's Metadata (Custom Annotations): https://www.oracle.com/technical-resources/articles/hunter-meta1.html
- Jenkov — Java Reflection Tutorial ( lambdas and invokedynamic discussed): https://jenkov.com/tutorials/java-reflection/index.html

### Практика / Кейсы

**Кейс 3.1. Leak через inner class — диагностика через heap dump.**
- Постановка: в серверном коде `Cache` имеет внутренний `Listener`, который держит ссылку на большое `Payload`. Обнаружить и устранить утечку.
- Шаги: (1) Написать `class Cache { class Listener { void onEvent(Payload p) { ... } } }` — `Listener` неявно держит `Cache.this`. (2) Нагрузить приложение, сделать heap dump через `jmap -dump:live,format=b,file=h.hprof <pid>`. (3) Открыть в MAT (Eclipse Memory Analyzer), найти путь GC root → Listener → Cache → Payload. (4) Рефакторинг: `static class Listener` с явной `WeakReference<Payload>`. (5) Повторить dump, убедиться, что Payload теперь GC-eligible.
- Что проверить: способность работать с heap-дампами, понимание synthetic-ссылок (`this$0`).

**Кейс 3.2. ThrowingFunction — обёртка для checked exceptions в Stream.**
- Постановка: использовать `Stream.map` с методом, бросающим `IOException`, без try/catch в каждой лямбде.
- Шаги: (1) Объявить `@FunctionalInterface interface ThrowingFunction<T,R,E extends Exception> { R apply(T t) throws E; }`. (2) Статический метод-обёртка `static <T,R> Function<T,R> unchecked(ThrowingFunction<T,R,?> f) { return t -> { try { return f.apply(t); } catch (Exception e) { throw new RuntimeException(e); } }; }`. (3) Использовать `stream.map(unchecked(Files::readString))`. (4) Альтернатива через Either<L,R>: `stream.map(Either.lift(Files::readString))`. (5) Сравнить с `try/catch` в лямбде — readability и overhead.
- Что проверить: понимание SAM-расширений, design choice между RuntimeException-wrapping и Either.

**Кейс 3.3. Производительность lambda vs anonymous class.**
- Постановка: сравнить throughput трёх реализаций `Comparator<Integer>` через JMH.
- Шаги: (1) Анонимный класс. (2) Lambda `(a,b) -> Integer.compare(a,b)`. (3) Method reference `Integer::compare`. (4) JMH `@Benchmark` с `Mode.AverageTime`, 5 warmup iterations, 5 forks. (5) Проверить, что stateless lambda использует `LambdaMetafactory`-кэш (через `-XX:+PrintFlagsFinal` и профиль async-profiler). (6) Доказать, что `Function.identity()` не аллоцирует в отличие от `x -> x` (по профилю аллокаций).
- Что проверить: способность профилировать, понимание LambdaMetafactory.

**Кейс 3.4. Currying и partial application.**
- Постановка: построить конфигурируемый `TaxCalculator` через currying, использовать как `Function<Money, Money>`.
- Шаги: (1) `BiFunction<BigDecimal, BigDecimal, BigDecimal> tax = (rate, amount) -> amount.multiply(rate);`. (2) Каррировать: `Function<BigDecimal, Function<BigDecimal, BigDecimal>> curried = rate -> amount -> amount.multiply(rate);`. (3) Создать `Function<BigDecimal, BigDecimal> vat20 = curried.apply(new BigDecimal("0.20"));`. (4) Применить к потоку `List<Money>` через `stream.map(vat20)`. (5) Объяснить, почему каррирование в Java многословнее, чем в Haskell/Scala, но паттерн полезен для конфигурации.
- Что проверить: понимание partial application, design trade-offs.

**Кейс 3.5. Method reference ambiguities.**
- Постановка: разобрать compile-ошибки и предупреждения для перегруженных методов при использовании method reference.
- Шаги: (1) Иметь класс с двумя перегрузками `void log(String)` и `void log(Object)`. (2) Использовать `stream.forEach(System.out::println)` — работает. (3) Использовать свой `stream.forEach(this::log)` — для `Stream<String>` выберется `log(String)` (most specific). (4) Добавить `log(Integer)` — теперь для `Stream<Integer>` выбирается `log(Integer)`, но если `Stream<Object>` — неоднозначность. (5) Объяснить по JLS §15.13.1 (compile-time method lookup).
- Что проверить: понимание overload resolution для method references.

**Кейс 3.6. Effectively final и capture.**
- Постановка: объяснить, почему лямбда не может использовать не-final переменную и как обойти через `AtomicReference`.
- Шаги: (1) Код `int counter = 0; stream.forEach(x -> counter++);` — compile error: Variable used in lambda expression should be final or effectively final. (2) Объяснить, что лямбда захватывает значение, а не ссылку на переменную стека. (3) Обойти через `AtomicInteger counter = new AtomicInteger(); stream.forEach(x -> counter.incrementAndGet());` — работает, т.к. `counter` (ссылка) effectively final. (4) Альтернатива — collect через `Collectors.counting()`. (5) Объяснить, почему mutation shared state в параллельном stream-е — bad practice, лучше reduction.
- Что проверить: понимание capture semantics, side-effect-free streams.

---

## 4. String/StringBuilder/StringPool, иммутабельность, новые методы Java 11+

### Теория

- **`String` — immutable класс** (final, внутренний `byte[]` значение с Java 9 Compact Strings JEP 254). Все методы, "изменяющие" строку (`substring`, `concat`, `replace`), возвращают новый String. Это даёт thread-safety без синхронизации, безопасность как ключа в `HashMap`, кэширование хеша (`hashCode` lazy-init и кэшируется в поле).
- **Compact Strings (Java 9, JEP 254)**: внутреннее представление `byte[]` + флаг `coder` (LATIN1 или UTF16). Старая Java всегда использовала `char[]` (UTF-16, 2 байта/char). Для ASCII-строк экономия памяти ~50%. Свойство проверяется через `String.coder()` (package-private). Senior должен помнить, что `String.length()` теперь возвращает число code units (как прежде), но не всегда char-ов.
- **String Pool** — специальная область heap (раньше PermGen до Java 7, сейчас обычный heap). Содержит уникальные строковые литералы и интернированные строки. Литералы в коде автоматически помещаются в пул при загрузке класса.
- **Интернирование**: `String.intern()` возвращает каноническое представление строки из пула. `"a" == "a".intern()` → true. С Java 7 пул в heap, можно настроить размер через `-XX:StringTableSize=N` (по умолчанию зависит от heap). Злоупотребление intern-ом может вызвать OutOfMemory.
- **`new String("literal")`** — всегда создаёт новый объект в heap, даже если литерал уже в пуле. Использовать `new String(...)` почти всегда bad practice, кроме случая, когда нужно "обрезать" большой substring (до Java 7u6 substring делил underlying char[] с оригиналом — это была утечка памяти).
- **`String.substring`**: с Java 7u6 создаёт новый массив (копия), не деля с оригиналом. Это исправило известную утечку, когда маленький substring держал большой char[].
- **`StringBuilder`** — mutable, не thread-safe. Идиоматичен для конкатенации в одном потоке. Методы `append`, `insert`, `delete`, `reverse`. Начальная capacity 16, расширяется `int newCapacity = (oldCapacity << 1) + 2`. Лучше задавать expected capacity в конструкторе для производительности.
- **`StringBuffer`** — mutable, thread-safe (`synchronized` методы). С Java 5 конкатенация `a + b` компилируется в `StringBuilder` (если не в loop). В современном коде почти не используется, т.к. синхронизация избыточна в большинстве случаев.
- **`+` operator**: компилятор превращает `a + b + c` в `new StringBuilder().append(a).append(b).append(c).toString()` (один StringBuilder на выражение). В цикле `for` — на каждой итерации создаётся новый StringBuilder, что плохо. Явный StringBuilder в loop — best practice.
- **`String.join(CharSequence delimiter, CharSequence... elements)`** (Java 8) и `StringJoiner` (Java 8) — для удобной сборки строк с разделителем. `Collectors.joining(", ", "[", "]")` — для stream.
- **`String.repeat(int)`** (Java 11, JEP 323) — `"ab".repeat(3) → "ababab"`. Внутри использует `Arrays.fill` и System.arraycopy, быстрее чем цикл + StringBuilder.
- **`String.isBlank()`** (Java 11) — true если строка пустая или содержит только whitespace (по `Character.isWhitespace`). Отличается от `isEmpty()` (только нулевая длина). Unicode-aware: `\u2003` (em space) считается blank.
- **`String.strip()`, `stripLeading()`, `stripTrailing()`** (Java 11) — Unicode-aware замена `trim()`. `trim()` удаляет только ASCII-control chars (≤ U+0020), `strip()` — все whitespace по Unicode.
- **`String.lines()`** (Java 11) — `Stream<String>` по строкам, разделённым `\n`, `\r\n`, `\r`. Линии без терминатора. Ленивый, не загружает всю строку в память при больших входных данных (через `Stream`).
- **`String.indent(int n)`** (Java 12) — добавляет/удаляет отступы (n пробелов). Отрицательный n — удаляет до `Math.abs(n)` пробелов с каждой строки.
- **`String.transform(Function<String,R> f)`** (Java 12) — применяет функцию к строке: `"42".transform(Integer::parseInt)`. Цепочечный API.
- **`String.formatted(Object... args)`** (Java 15) — эквивалент `String.format(this, args)`. Удобно: `"Hello, %s".formatted(name)`.
- **Text Blocks (Java 15, JEP 378)**: `"""multi-line"""`. Поддержка `\` (line continuation) и `\s` (preserve trailing whitespace). Отступ определяется по закрывающей `"""`.
- **`indexOf`, `lastIndexOf`, `contains`, `startsWith`, `endsWith`** — основные методы поиска. `matches(String regex)` — full-match по regex (компилирует каждый раз; для повторного использования — `Pattern.compile`).
- **`String.split(String regex, int limit)`** — разбиение по regex. `limit < 0` — все совпадения (включая trailing пустые), `limit == 0` (по умолчанию) — обрезает trailing пустые, `limit > 0` — не более `limit` частей. Senior должен помнить, что `split` компилирует regex каждый раз — для hot path лучше `Pattern.split`.
- **`String.toCharArray()`** — копия внутреннего массива (не view). `getBytes()` — использует default charset (с Java 18 это UTF-8 по умолчанию, JEP 400). `getBytes(Charset)` или `getBytes("UTF-8")` — явно.
- **`char` vs `Character` vs codepoint**: `String.charAt(i)` возвращает char (UTF-16 code unit). Символы вне BMP (эмодзи, редкие иероглифы) — surrogate pairs (2 char). Для итерации по codepoint-ам — `String.codePoints().forEach(...)`. Senior должен помнить: `"\uD83D\uDE00".length() == 2`, но это один символ.
- **`String.equals` vs `equalsIgnoreCase`** — для case-insensitive. Unicode-aware: `Character.toLowerCase` использует UnicodeCase таблицы, не просто ASCII.
- **`String.replace(CharSequence, CharSequence)`** — literal replacement, без regex (в отличие от `replaceAll`). `replaceFirst(String regex, String replacement)` — regex, только первое совпадение.
- **`String.hashCode()`**: `h = 31 * h + char` для каждого char (или byte). Кэшируется в поле `hash` (по умолчанию 0 — sentinel "не вычислен"). Стандартный алгоритм, известный во многих языках.

### Материалы

- Книга: "Effective Java" Joshua Bloch (3rd ed., Addison-Wesley, 2018) — Item 6 (avoid creating unnecessary objects, в т.ч. `String` и autoboxing), Item 79 (synchronization, в т.ч. почему `StringBuffer` медленнее)
- Книга: "Java Performance" Scott Oaks (2nd ed., O'Reilly, 2020) — Chapter 5: Understanding Strings (Compact Strings, intern, String deduplication)
- Книга: "Core Java Volume I—Fundamentals" Cay S. Horstmann (12th ed., 2021) — Chapter 3.7: Strings (раздел про StringBuilder, String pool)
- Документация Oracle (Strings): https://docs.oracle.com/javase/tutorial/java/data/strings.html
- Документация Oracle (Numbers and Strings): https://docs.oracle.com/javase/tutorial/java/data/index.html
- Документация Oracle (String class Javadoc Java 21): https://docs.oracle.com/en/java/javase/21/docs/api/java.base/java/lang/String.html
- Документация Oracle (StringBuilder Javadoc): https://docs.oracle.com/en/java/javase/21/docs/api/java.base/java/lang/StringBuilder.html
- JEP 254: Compact Strings (Java 9): https://openjdk.org/jeps/254
- JEP 378: Text Blocks (Java 15): https://openjdk.org/jeps/378
- JEP 400: UTF-8 by Default (Java 18): https://openjdk.org/jeps/400
- Baeldung — Guide to Java String Pool: https://www.baeldung.com/java-string-pool
- Baeldung — Why String Is Immutable in Java: https://www.baeldung.com/java-string-immutable
- Baeldung — StringBuilder and StringBuffer in Java: https://www.baeldung.com/java-string-builder-string-buffer
- Baeldung — Java String Interview Questions: https://www.baeldung.com/java-string-interview-questions
- Baeldung — Java 11 String API Additions: https://www.baeldung.com/java-11-string-api

### Практика / Кейсы

**Кейс 4.1. Compact Strings — измерение экономии памяти.**
- Постановка: на одном и том же наборе из 1M ASCII-строк измерить heap usage в Java 8 (`char[]`) vs Java 9+ (`byte[]`).
- Шаги: (1) Сгенерировать 1M случайных строк длиной 20 (ASCII only). (2) Запустить на Java 8: `jcmd <pid> GC.heap_info` — измерить used. (3) Запустить на Java 21 — измерить. (4) Использовать JOL (Java Object Layout) для одного String: `String s = "abcdef..."; GraphLayout.parseInstance(s).toPrintable()`. (5) Оценить экономию ~50%. (6) Смешать UTF-16 строки (русский/китайский) — экономия меньше, т.к. coder=UTF16.
- Что проверить: понимание Compact Strings, умение измерять object size.

**Кейс 4.2. String pool tuning под нагрузкой.**
- Постановка: high-load парсер логов дедуплицирует строки через `intern()`, но throughput деградирует. Найти и устранить.
- Шаги: (1) Парсер делает `host = host.intern()` для каждого log entry. (2) Проверить `-XX:StringTableSize` (по умолчанию в Java 21: 65536 buckets для маленьких heap, больше для больших). (3) Если chain length большая — lookup O(n) на chain, замедляет. (4) Увеличить до `-XX:StringTableSize=1000003` (prime). (5) Альтернатива: собственный `ConcurrentHashMap<String, WeakReference<String>>` deduplicator с eviction. (6) Сравнить throughput через JMH.
- Что проверить: понимание internal String pool, умение выбрать стратегию дедупликации.

**Кейс 4.3. StringBuilder capacity и рост массива.**
- Постановка: построить JSON-сериализатор для 100K элементов, минимизировать аллокации.
- Шаги: (1) Наивный `StringBuilder sb = new StringBuilder();` для 100K элементов → capacity расширяется многократно, каждая экспансия = копирование массива. (2) Предсказать размер: `sb = new StringBuilder(100_000 * 8)`. (3) Измерить аллокации через `-XX:+PrintGCApplicationStoppedTime` или JFR. (4) Сравнить с `CharBuffer` и `Writer`. (5) Реализовать `JsonWriter` с pooling StringBuilder-ов (ThreadLocal) — аккуратно, возможен leak.
- Что проверить: capacity planning, понимание exponential growth.

**Кейс 4.4. Unicode и codepoints.**
- Постановка: реализовать `reverse(String)` корректно для эмодзи и surrogate pairs. Наивный `new StringBuilder(s).reverse().toString()` ломает surrogate.
- Шаги: (1) На `String s = "Hello \uD83D\uDE00 world!"` наивный reverse даёт `"!dlrow ... olleH"` с перевёрнутой surrogate (невалидный UTF-16). (2) Использовать `s.codePoints().toArray()` → reverse → `new String(int[], 0, len)`. (3) Дополнительно: учитывать combining marks (например, é = e + U+0301) — нужно нормализовать через `Normalizer.normalize(s, Form.NFC)`. (4) Grapheme clusters (флаги, ZWJ-эмодзи) — полностью корректно через `BreakIterator.getCharacterInstance()`. (5) Покрыть тестами кейсы с флагами, skin-tone эмодзи.
- Что проверить: глубокое понимание Unicode, способность рассуждать о codepoint vs code unit vs grapheme.

**Кейс 4.5. Тext Block для SQL/JSON — миграция.**
- Постановка: мигрировать legacy-код с конкатенацией SQL-запросов на text blocks, не сломав тесты.
- Шаги: (1) Старый: `String sql = "SELECT * FROM users WHERE id = " + id + " AND name = '" + name + "'";` (SQL injection!). (2) Новый: `String sql = """ SELECT * FROM users WHERE id = %d AND name = '%s' """.formatted(id, escape(name));` (3) Лучше — `PreparedStatement` с `?`-плейсхолдерами. (4) Тесты: assertEquals на нормализованных строках. (5) Объяснить, почему text blocks не защищают от injection, но улучшают читаемость многострочного SQL/JSON.
- Что проверить: безопасность (injection), readability.

**Кейс 4.6. `String.lines()` для потоковой обработки большого файла.**
- Постановка: обработать файл 5GB построчно, не загружая в память.
- Шаги: (1) Наивный `Files.readString(Path).lines()` — OOM. (2) `Files.lines(Path)` (NIO) — stream, ленивый, закрывается через try-with-resources. (3) `String` для одной строки — использовать `lines()` для разбиения многострочной конфигурации. (4) Сравнить throughput `Files.lines` vs `BufferedReader.lines` vs `Scanner` через JMH. (5) Для parallel обработки — `Files.lines(path).parallel()` работает, но thread-pool общий — лучше явный.
- Что проверить: ленивые stream-и, resource management.

---

## 5. Исключения

### Теория

- **Иерархия исключений**: `Throwable` → `Error` (серьёзные системные сбои JVM, не ловим — `OutOfMemoryError`, `StackOverflowError`) и `Exception` → `RuntimeException` (unchecked, `NullPointerException`, `ClassCastException`, `ArrayIndexOutOfBoundsException`, `ArithmeticException`) и остальные `Exception` (checked, должны быть объявлены или пойманы).
- **Checked vs unchecked**: checked — компилятор требует обработки (try/catch или throws). Идея — программист не пропустит recoverable-ошибку. Критика: verbose, утечка абстракций. Современный тренд (Spring, Hibernate) — обёртка checked в unchecked (`DataAccessException`). Effective Java Item 71: avoid checked exceptions unless recovery realistic.
- **`RuntimeException` и его подклассы** — unchecked. `NullPointerException`, `IllegalArgumentException`, `IllegalStateException`, `ClassCastException`, `IndexOutOfBoundsException`, `ConcurrentModificationException`, `UnsupportedOperationException`.
- **`Error`** — не должен ловиться в application-коде (кроме узких случаев вроде framework startup). `OutOfMemoryError`, `StackOverflowError`, `NoClassDefFoundError`, `AssertionError`.
- **`try-catch-finally`**: catch может быть несколько (по типам, от наиболее специфичного к наименее), finally — выполняется всегда (даже при return из try). В Java 7+ multi-catch: `catch (IOException | SQLException e)`.
- **try-with-resources (Java 7, JEP 213 — improvements)**: любой `AutoCloseable` ресурс закрывается автоматически. Несколько ресурсов: `try (FileInputStream fis = ...; BufferedReader br = ...) { ... }`. Закрытие происходит в reverse-порядке объявления. Требует интерфейс `AutoCloseable` (или `Closeable` из java.io, который extends AutoCloseable).
- **Suppressed exceptions**: если в try выброшено исключение, а в close() — тоже, второе добавляется к первому через `Throwable.addSuppressed`. Доступно через `Throwable.getSuppressed()`. Важно для диагностики: не теряется ни одно.
- **Exception chaining (wrapping)**: `new RuntimeException("context", cause)` — `cause` доступен через `getCause()`. Используется для абстрагирования слоёв (DAO throws SQLException → Service throws BusinessException с cause=SQLException).
- **Custom exceptions**: наследоваться от `RuntimeException` (если recoverable-by-design) или `Exception` (если forced-handle). Всегда определять конструкторы `(String message)`, `(String message, Throwable cause)`, `(Throwable cause)` — для совместимости с фреймворками. Опционально — код ошибки enum.
- **`throw` и `throws`**: `throw new X()` — выбросить; `throws X, Y` — объявить. В переопределённом методе нельзя бросать более широкие checked exceptions, чем в суперклассе. Можно не бросать вообще или бросать подтипы.
- **`finally` и `return`**: return в finally «перебивает» return из try. `finally` выполняется до return из try. Если finally выбрасывает — оригинальное исключение теряется (это плохо, избегать throw в finally). С try-with-resources этой проблемы нет.
- **`Throwable.fillInStackTrace()`** — дорогая операция (walk по stack). Если нужно много бросать одно исключение (например, для flow control — bad practice, но бывает) — можно переопределить `fillInStackTrace` на no-op. Или использовать pre-allocated exception для hot path.
- **Исключения и производительность**: создание исключения со stack trace — O(stack depth). В hot path — не использовать для flow control. Альтернативы: Result/Either-монады, возвращение null/Optional, boolean flags.
- **`Exception` сериализуемый** (`implements Serializable`), что позволяет передавать между JVM (RMI, JMS). Для этого custom exception должен быть Serializable и поля — Serializable.
- **`Throwable.getStackTrace()`** возвращает `StackTraceElement[]` — массив, который JVM заполняет. Можно подменить через `setStackTrace(StackTraceElement[])` — используется в mocking-фреймворках.
- **`Thread.setDefaultUncaughtExceptionHandler`** — для непойманных исключений в потоках. По умолчанию печатает в `System.err`. В server-приложениях — обязательно переопределять для логирования. `Thread.setUncaughtExceptionHandler` — для конкретного потока.
- **`ThreadGroup.uncaughtException`** — устаревший механизм, deprecated подход. Использовать `UncaughtExceptionHandler`.
- **`InterruptedException`** — checked, бросается когда другой поток прервал текущий через `Thread.interrupt()`. Корректная обработка: либо восстановить флаг `Thread.currentThread().interrupt()` и выйти, либо пробросить. Не "глотать". Effective Java Concurrency: always restore interrupt status.
- **`StackOverflowError`** — unbounded recursion. JVM ограничена `-Xss` (stack size per thread). Не ловить (исключение — recursive descent parser с recovery).
- **`NumberFormatException` extends IllegalArgumentException`** (unchecked) — бросается `Integer.parseInt` и др. при malformed input. Часто recoverable — обернуть в `Optional<Integer> parse(String)` через try-catch.
- **`AssertionError`** — бросается `assert` (если включён `-ea`). Не для production валидации, только для invariant-проверок в dev/test.

### Материалы

- Книга: "Effective Java" Joshua Bloch (3rd ed., Addison-Wesley, 2018) — Item 69 (exceptions for exceptional conditions only), Item 70 (checked exceptions for recoverable conditions, runtime for programming errors), Item 71 (avoid unnecessary checked exceptions), Item 72 (favor standard exceptions), Item 73 (throw exceptions appropriate to abstraction), Item 74 (document all thrown exceptions), Item 75 (include failure-capture information), Item 76 (strive for failure atomicity), Item 77 (don't ignore exceptions)
- Книга: "Java Concurrency in Practice" Brian Goetz et al. (Addison-Wesley, 2006) — Chapter 7: Cancellation and Shutdown (раздел `InterruptedException`)
- Книга: "Core Java Volume I—Fundamentals" Cay S. Horstmann (12th ed., 2021) — Chapter 7.3: Catching Exceptions, 7.4: Using Assertions, 7.7: Tips for Exception Handling
- Документация Oracle (Exceptions): https://docs.oracle.com/javase/tutorial/essential/exceptions/index.html
- Документация Oracle (try-with-resources): https://docs.oracle.com/javase/tutorial/essential/exceptions/tryResourceClose.html
- Документация Oracle (Uncaught Exception Handler): https://docs.oracle.com/javase/tutorial/essential/exceptions/handling.html
- Документация Oracle (Throwable class Javadoc): https://docs.oracle.com/en/java/javase/21/docs/api/java.base/java/lang/Throwable.html
- Документация Oracle (AutoCloseable Javadoc): https://docs.oracle.com/en/java/javase/21/docs/api/java.base/java/lang/AutoCloseable.html
- Baeldung — Exception Handling in Java: https://www.baeldung.com/java-exceptions
- Baeldung — Try With Resources: https://www.baeldung.com/java-try-with-resources
- Baeldung — Java Suppressed Exceptions: https://www.baeldung.com/java-suppressed-exceptions
- DZone — Exception Handling With Try-With-Resource Suppressed: https://dzone.com/articles/exception-handling-with-try-with-resource-suppress

### Практика / Кейсы

**Кейс 5.1. Try-with-resources и suppressed ordering.**
- Постановка: реализовать ресурс `Socket` и `BufferedReader`, оба бросают в `close()` — проверить порядок suppressed.
- Шаги: (1) `try (Socket s = open(); BufferedReader br = wrap(s)) { throw new IOException("body"); }` (2) В `close()` обоих бросить `IOException("close-br")` и `IOException("close-socket")`. (3) Пойманное исключение — `"body"`, suppressed — `[close-br, close-socket]` (reverse declaration order). (4) Доказать через `Throwable.getSuppressed()`. (5) Объяснить по спецификации `try-with-resources` JLS §14.20.3.
- Что проверить: точное знание ordering, умение читать suppressed в логах.

**Кейс 5.2. InterruptedException правильно.**
- Постановка: поток крутит `while(running) { Thread.sleep(100); }`. При `interrupt()` обработать корректно.
- Шаги: (1) Наивный `try { ... } catch (InterruptedException e) { /* swallow */ }` — bad, флаг сброшен, running не проверяется. (2) Корректно: `catch (InterruptedException e) { running = false; Thread.currentThread().interrupt(); }` — восстановить флаг для вышестоящего кода. (3) Альтернатива: пробросить `throw new RuntimeException(e)`. (4) Тест: запустить поток, interrupt, убедиться что он завершился за <100ms. (5) Объяснить, почему `Thread.interrupted()` (static, сбрасывает флаг) vs `Thread.currentThread().isInterrupted()` (не сбрасывает).
- Что проверить: понимание cooperative cancellation, interrupt protocol.

**Кейс 5.3. Custom exception hierarchy для layered app.**
- Постановка: спроектировать иерархию исключений для web-app: Controller → Service → Repository.
- Шаги: (1) Базовый `AppException extends RuntimeException` с полем `ErrorCode`. (2) `NotFoundException`, `ValidationException`, `ConflictException` — extends AppException. (3) Repository бросает `DataAccessException` (Spring) → Service ловит и оборачивает в `NotFoundException` (cause). (4) ControllerAdvice (Spring `@RestControllerAdvice`) маппит на HTTP status. (5) Тесты: каждой ошибки → корректный HTTP code. (6) Документация: все возможные ошибки в OpenAPI.
- Что проверить: layered design, error code strategy, observability (stack trace не теряется через cause).

**Кейс 5.4. Pre-allocated exception для hot path.**
- Постановка: парсер логов в hot path бросает `ParseException` на 0.01% записей, но stack trace замедляет. Оптимизировать.
- Шаги: (1) Создать `class FastParseException extends RuntimeException { @Override public synchronized Throwable fillInStackTrace() { return this; } }`. (2) Pre-allocate single instance. (3) В hot path: `if (parseFailed) throw FAST_PARSE_EXCEPTION;` (4) Измерить throughput через JMH — обычно 5–10x быстрее. (5) В логике上层 — при поимке проверить, что это singleton, и log message context, а не stack trace. (6) Объяснить trade-off: теряется stack trace, что ухудшает диагностику. Использовать только когда точно знаешь контекст.
- Что проверить: understanding of `fillInStackTrace` cost, design trade-offs.

**Кейс 5.5. Failure atomicity.**
- Постановка: метод `transfer(Account from, Account to, BigDecimal amount)` должен оставлять счета в согласованном состоянии, даже если выбрасывает.
- Шаги: (1) Наивный: списать с `from`, пополнить `to`. Если между ними exception — `from` уже списан, `to` не пополнен → inconsistent. (2) Проверить invariants (amount > 0, balance sufficient) до mutation. (3) Сначала пополнить `to` (если упадёт — `from` не тронут), потом списать `from` (если упадёт после пополнения `to` — дубликат, но detect-able через idempotency key). (4) Альтернатива: сделать operation atomic через 2PC/JTA (тяжело). (5) Тесты: simulated exceptions на каждом шаге, проверка state.
- Что проверить: failure atomicity principle (Effective Java Item 76), transactional thinking.

**Кейс 5.6. UncaughtExceptionHandler для потоков.**
- Постановка: веб-сервер с пулом потоков — любое непойманное исключение должно логироваться в Sentry, а не теряться.
- Шаги: (1) `Thread.setDefaultUncaughtExceptionHandler((t, e) -> sentry.capture(e));`. (2) Для `ExecutorService` — обернуть через `Executors.newFixedThreadPool(n, new ThreadFactory() { ... wrap Runnable with try-catch ... })` или `CompletableFuture.exceptionally`. (3) Доказать, что `Future.get()` выбрасывает `ExecutionException` с cause — handler не вызывается, т.к. exception сохранён в Future. (4) Для `CompletableFuture` — `exceptionally` или `whenComplete`. (5) Тест: бросить RuntimeException в задаче, проверить, что handler сработал и Future сообщила об ошибке.
- Что проверить: понимание потоков, executor-ов, async error handling.

---

## 6. Collections

### Теория

- **Collections Framework** (с Java 1.2, JEP 269 в Java 9 — convenience factories): `Collection<E>` (корневой интерфейс), `List`, `Set`, `Queue`, `Map` (отдельная иерархия, не extends Collection). Вспомогательные: `Iterator`, `Iterable`, `Comparator`, `Comparable`.
- **List**: упорядоченная коллекция с дубликатами, доступ по индексу. Реализации: `ArrayList` (массив, O(1) get/put, O(n) insert/remove в середине), `LinkedList` (двусвязный список, O(n) get, O(1) insert/remove если есть итератор), `Vector` (устарел, synchronized), `CopyOnWriteArrayList` (thread-safe, копия при записи).
- **Set**: без дубликатов. `HashSet` (HashMap-backed, O(1) в среднем), `LinkedHashSet` (сохраняет порядок вставки), `TreeSet` (NavigableSet на TreeMap, O(log n)), `EnumSet` (bit vector, очень эффективен для enum).
- **Map**: ключ-значение. `HashMap` (O(1) в среднем, O(log n) в worst case с Java 8 при коллизиях, превращающихся в деревья), `LinkedHashMap` (доступ к order), `TreeMap` (sorted), `Hashtable` (устарел, synchronized), `EnumMap` (array-backed для enum-ключей), `WeakHashMap` (ключи WeakReference), `IdentityHashMap` (== instead of equals), `ConcurrentHashMap` (thread-safe, lock striping с Java 8 — CAS + synchronized bin).
- **Queue/Deque**: `ArrayDeque`, `LinkedList`, `PriorityQueue` (heap), `PriorityBlockingQueue`, `ArrayBlockingQueue`, `LinkedBlockingQueue`, `ConcurrentLinkedQueue` (lock-free), `DelayQueue`, `SynchronousQueue`.
- **`HashMap` internals**: массив `Node<K,V>[] table` (power of 2). `hash(key)` = `(h = key.hashCode()) ^ (h >>> 16)` — распространение старших бит в младшие, чтобы при маленькой маске `(n-1)` использовались все биты хеша. Bucket = `table[(n-1) & hash]`. Bucket — связный список `Node` (initially), превращается в `TreeNode` если длина ≥ 8 (TREEIFY_THRESHOLD) и `table.length >= MIN_TREEIFY_CAPACITY=64`. Untreeify при shrink < 6.
- **`HashMap` resize**: при `size > capacity * loadFactor` (default 0.75) массив расширяется вдвое. Каждый элемент перераспределяется по новому индексу: либо старый, либо `oldIndex + oldCapacity` (по high bit). Дорогая операция O(n).
- **`HashMap` и `null` keys/values**: разрешены (один null key, hash=0). `Hashtable` и `ConcurrentHashMap` запрещают null.
- **`HashMap` not thread-safe**: структурные модификации в multi-threaded → race, infinite loop (до Java 8 — переlinked list в cycle), потеря данных. Использовать `ConcurrentHashMap` или `Collections.synchronizedMap`.
- **fail-fast итераторы**: `ArrayList`, `HashMap` — бросают `ConcurrentModificationException` если коллекция структурно изменена во время итерации. Реализовано через `modCount` поле: проверка в `next()`. Не гарантировано в multi-threaded (best-effort).
- **fail-safe итераторы**: `CopyOnWriteArrayList`, `ConcurrentHashMap` — работают с snapshot или слабой консистентностью. Не бросают CME, но могут не видеть свежие изменения. Trade-off: memory overhead (COW копирует на запись) или слабая консистентность.
- **`ConcurrentHashMap` internals (Java 8+)**: массив `Node[]`, каждый bucket на `put` синхронизируется (`synchronized` на first node) при пустом — CAS. `size` через `CounterCell[]` (striped counters, как `LongAdder`). Read без блокировок (volatile fields). Resize — concurrent (multiple threads help).
- **`LinkedHashMap`**: extends HashMap, дополнительно двусвязный список всех entries (before/after). Сохраняет порядок вставки (default) или access-order (`new LinkedHashMap<>(16, 0.75f, true)`). Override `removeEldestEntry(Map.Entry)` → LRU cache в 5 строк.
- **`TreeMap`**: red-black tree, O(log n) для всех операций. Гарантированный порядок (по `Comparable` или `Comparator`). `firstKey`, `lastKey`, `subMap`, `headMap`, `tailMap`, `floorKey`, `ceilingKey`, `lowerKey`, `higherKey`.
- **`EnumSet` / `EnumMap`**: array/bit-vector-backed, O(1) операции. Должны создаваться через factory: `EnumSet.of(...)`, `new EnumMap<>(MyEnum.class)`. Идиоматичны для enum-коллекций.
- **`Collections.unmodifiable*`**: wrapper, бросающий `UnsupportedOperationException` на mutation. Не deep-immutable — внутренняя коллекция всё ещё мутабельна. Для deep immutability — копия.
- **Immutable collections Java 9+ (JEP 269)**: `List.of(...)`, `Set.of(...)`, `Map.of(...)`, `Map.entry(k,v)`, `Map.ofEntries(...)`. Возвращают компактные immutable реализации (для 0–10 элементов — специализированные классы; для больших — `MapN` с хешированием). Бросают NPE на null elements/keys/values (в отличие от `Collections.unmodifiable*`). Не позволяют структурные модификации.
- **`Collections.synchronized*`**: wrapper, синхронизирующий все методы на mutex. Итерация — требует явной `synchronized(coll) { ... }`. Performance: хуже чем `ConcurrentHashMap` для high contention.
- **`Arrays.asList`**: fixed-size view над массивом (длина не меняется, но элементы мутабельны). `List.of` — immutable. `new ArrayList<>(Arrays.asList(...))` — полноценный mutable.
- **`ArrayList.subList`**: view над подсегментом. Изменения видны в оригинале. Если оригинал структурно модифицирован — subList бросает `ConcurrentModificationException`.
- **`Comparator` chain**: `Comparator.comparing(Person::getName).thenComparingInt(Person::getAge)`. `Comparator.nullsFirst`, `nullsLast`, `reverse`, `reversed`.
- **`Collections.sort` vs `List.sort`**: `List.sort(comparator)` (Java 8+) делегирует в `List`-реализацию (часто через внутренний массив). `Collections.sort` вызывает `list.sort(null)` (natural ordering). `Arrays.sort` для массивов — dual-pivot quicksort для примитивов, TimSort для объектов (stable).
- **`hashCode`/`equals` contract для Map/Set**: если equals для двух объектов true, hashCode должен быть равен. Иначе объект "потеряется" в HashMap. Mutable ключи опасны: изменение поля, влияющего на hashCode, после put → ключ не найти. Best practice: immutable ключи.

### Материалы

- Книга: "Effective Java" Joshua Bloch (3rd ed., Addison-Wesley, 2018) — Item 26 (don't use raw types), Item 27 (eliminate unchecked warnings), Item 28 (lists over arrays), Item 29 (favor generic types), Item 30 (favor generic methods), Item 31 (use bounded wildcards for flexibility), Item 32 (combine generics and varargs judiciously), Item 55 (return optionals), Item 57 (minimize scope of local variables), Item 61 (prefer primitive types to boxed primitives), Item 84 (don't use `Thread.interrupted` carelessly)
- Книга: "Java Generics and Collections" Maurice Naftalin & Philip Wadler (O'Reilly, 2006) — Part II: Collections (главы 12–19 про каждую коллекцию, internal details)
- Книга: "Java Concurrency in Practice" Brian Goetz et al. (Addison-Wesley, 2006) — Chapter 5: Building Blocks (`ConcurrentHashMap`, `CopyOnWriteArrayList`, blocking queues)
- Книга: "Core Java Volume I—Fundamentals" Cay S. Horstmann (12th ed., 2021) — Chapter 9: Collections
- Документация Oracle (Collections): https://docs.oracle.com/javase/tutorial/collections/index.html
- Документация Oracle (The Map Interface): https://docs.oracle.com/javase/tutorial/collections/interfaces/map.html
- Документация Oracle (HashMap Javadoc Java 21): https://docs.oracle.com/en/java/javase/21/docs/api/java.base/java/util/HashMap.html
- Документация Oracle (ConcurrentHashMap Javadoc): https://docs.oracle.com/en/java/javase/21/docs/api/java.base/java/util/concurrent/ConcurrentHashMap.html
- JEP 269: Convenience Factory Methods for Collections (Java 9): https://openjdk.org/jeps/269
- Baeldung — A Guide to Java HashMap: https://www.baeldung.com/java-hashmap
- Baeldung — The Java HashMap Under the Hood (Advanced): https://www.baeldung.com/java-hashmap-advanced
- Baeldung — A Guide to LinkedHashMap: https://www.baeldung.com/java-linked-hashmap
- Baeldung — Differences Between HashMap and Hashtable: https://www.baeldung.com/hashmap-hashtable-differences
- Baeldung — Java Maps Series: https://www.baeldung.com/java-maps-series
- Baeldung — Fail-Safe vs Fail-Fast Iterator: https://www.baeldung.com/java-fail-safe-vs-fail-fast-iterator
- GeeksforGeeks — Fail Fast and Fail Safe Iterators: https://www.geeksforgeeks.org/java/fail-fast-fail-safe-iterators-java
- GeeksforGeeks — Internal Working of HashMap in Java: https://www.geeksforgeeks.org/java/internal-working-of-hashmap-java

### Практика / Кейсы

**Кейс 6.1. LRU-кэш на LinkedHashMap + сравнение с ConcurrentHashMap+ReadWriteLock (через JMH).**
- Постановка: реализовать LRU-кэш с capacity, сравнить throughput двух реализаций.
- Шаги: (1) `class LRUCache<K,V> extends LinkedHashMap<K,V> { private final int cap; public LRUCache(int cap) { super(cap, 0.75f, true); this.cap = cap; } @Override protected boolean removeEldestEntry(Map.Entry<K,V> e) { return size() > cap; } }`. (2) Обернуть в `synchronized` (LinkedHashMap не thread-safe). (3) Альтернатива: `ConcurrentHashMap` + `ReadWriteLock` + собственный doubly-linked-list для LRU tracking. (4) JMH `@Benchmark` get/put ratio 80/20, capacity 1000, 4 threads. (5) Анализ: LinkedHashMap — simple, but global lock bottleneck. CHM+RWLock — лучше под read-heavy. Для write-heavy — segmented. (6) Сравнить с `Caffeine` (production-grade).
- Что проверить: глубокое понимание LinkedHashMap access-order, навыки JMH.

**Кейс 6.2. Treeify в HashMap — когда и почему.**
- Постановка: показать, как плохой hashCode приводит к treeify и как это спасает от O(n) worst case.
- Шаги: (1) Создать класс `BadKey` с hashCode = constant (все ключи в один bucket). (2) Заполнить HashMap 1000 элементов — без treeify O(n) get. (3) С Java 8 — после 8 элементов в bucket превращается в TreeNode, O(log n). (4) Измерить throughput через JMH. (5) Включить `-XX:+PrintFlagsFinal | grep TREEIFY` — проверить thresholds. (6) Объяснить, почему treeify только если `table.length >= 64` (иначе resize вместо treeify).
- Что проверить: понимание `TREEIFY_THRESHOLD`, `MIN_TREEIFY_CAPACITY`, мотивация JEP-оптимизации.

**Кейс 6.3. fail-fast vs fail-safe в ConcurrentModificationException.**
- Постановка: показать CME на ArrayList, объяснить почему CopyOnWriteArrayList и ConcurrentHashMap его не бросают.
- Шаги: (1) `List<Integer> list = new ArrayList<>(List.of(1,2,3)); for (int x : list) { if (x == 2) list.remove(Integer.valueOf(2)); }` — CME. (2) Использовать `Iterator.remove()` — работает. (3) На `CopyOnWriteArrayList` — итератор видит snapshot, remove через итератор бросает UnsupportedOperationException. (4) На `ConcurrentHashMap` — weakly consistent iterator, не бросает, может не увидеть изменений. (5) Объяснить, почему `modCount` проверяется в `next()`, не в `hasNext()`.
- Что проверить: точное понимание semantics fail-fast (best-effort) vs fail-safe.

**Кейс 6.4. ConcurrentHashMap size через striped counters.**
- Постановка: объяснить, почему `ConcurrentHashMap.size()` не использует один counter.
- Шаги: (1) В multi-threaded `put` contention на одном `volatile` counter → bottleneck. (2) Реализация: `volatile long baseCount` + `volatile CounterCell[] counterCells` (each cell — padded для false sharing avoidance через `@Contended`). (3) `size()` = `baseCount + sum(counterCells)`. (4) Аналогично `LongAdder` (public API). (5) Измерить throughput `AtomicLong.incrementAndGet()` vs `LongAdder.increment()` через JMH на 8 потоках — обычно 3–5x лучше.
- Что проверить: понимание striped counters, false sharing, ability to design counters.

**Кейс 6.5. Immutable collections Java 9+ — null handling.**
- Постановка: показать, что `List.of(1, null, 2)` бросает NPE, в отличие от `Arrays.asList`.
- Шаги: (1) `List.of(1, null, 2)` → NPE. (2) `Arrays.asList(1, null, 2)` — работает. (3) `new ArrayList<>(Arrays.asList(...))` — работает. (4) Объяснить: immutable collections Java 9 explicitly reject null для предсказуемости и compact storage. (5) Migration: при переходе с Arrays.asList на List.of — проверить на null-элементы. (6) `Map.of(k, null)` тоже NPE — nullable values не поддерживаются, нужен `HashMap`.
- Что проверить: understanding null semantics, ability to migrate safely.

**Кейс 6.6. Mutable ключи в HashMap — катастрофа.**
- Постановка: показать, что изменение поля ключа после put делает его "невидимым" в map.
- Шаги: (1) `class Key { int i; Key(int i) { this.i = i; } public int hashCode() { return i; } public boolean equals(Object o) { return o instanceof Key && ((Key)o).i == i; } }`. (2) `Map<Key, String> m = new HashMap<>(); Key k = new Key(1); m.put(k, "a"); k.i = 2; m.get(k)` → null (хотя тот же объект). (3) `m.get(new Key(1))` → null (тоже не найден). (4) Iterator по entries найдёт запись, но она в "неправильном" bucket. (5) Вывод: immutable ключи — must. `record`-классы — идеально. (6) Если нужен mutable ключ — использовать `IdentityHashMap` (по ссылке).
- Что проверить: глубокое понимание hashCode/equals контракта в действии.

**Кейс 6.7. PriorityQueue как scheduler.**
- Постановка: реализовать in-memory task scheduler с приоритетами и поддержкой delayed execution.
- Шаги: (1) `class Task implements Comparable<Task> { Instant when; int priority; ... }` (2) `PriorityBlockingQueue<Task>` — blocking для consumer-thread. (3) Для delayed execution — `DelayQueue<Task>` где `Task implements Delayed`. (4) Producer threads `put(task)`, consumer `take()` — блокируется до наступления времени. (5) Сравнить с `ScheduledExecutorService` — что готовое, что своё.
- Что проверить: понимание heap-based structures, ability to design scheduler.

---

## 7. Generics

### Теория

- **Generics (Java 5, JEP 8063180)**: параметризация типов. `List<String>` — типобезопасный список, проверка на compile-time, без cast. До Java 5 — raw types, везде `Object` и явные касты.
- **Type parameters**: `<T>` — generic type; `<T extends Bound>` — bounded type parameter (может быть class или interface, `extends` всегда). Множественные bounds: `<T extends A & B & C>` — первый должен быть class (или empty), остальные interface-ы.
- **Generic class**: `class Box<T> { T value; void set(T v) { value = v; } T get() { return value; } }`. Generic interface, generic method: `static <T> List<T> of(T... items) { ... }`.
- **Wildcards**: `<?>` — unknown (любой тип); `<? extends T>` — upper bound (T или подтип); `<? super T>` — lower bound (T или супертип). Используются в параметрах методов для гибкости.
- **PECS (Producer Extends, Consumer Super)** — правило из Effective Java Item 31. Если коллекция поставляет элементы (читается) — `<? extends T>`. Если принимает (записывается) — `<? super T>`. Если и читает и пишет — `<?>` или конкретный тип.
- **Type erasure**: generic-тип стирается в байткоде. `List<String>` и `List<Integer>` в runtime — один класс `List`. Generic-параметры заменяются на левую границу (`Object` если без bound, или bound). Поэтому нельзя `new T()`, `new T[]`, `instanceof List<String>` (только `instanceof List`), `T.class`.
- **Bridge methods**: компилятор синтетически добавляет методы для сохранения polymorphism при erasure. Например, `class StringList extends List<String> { void set(int i, String s) { ... } }` — компилятор добавляет `void set(int i, Object s) { set(i, (String) s); }` (bridge), чтобы вызов через raw `List`-ссылку работал корректно.
- **Raw types**: `List` без параметра. Допускается для backwards compat, но компилируется с warnings. В новом коде — не использовать. `List<Object>` — не raw, параметризован, но `List<String>` не является подтипом `List<Object>` (invariance).
- **Invariance generics**: `List<String>` не подтип `List<Object>` (в отличие от массивов, где `String[]` подтип `Object[]` — covariant, и это source of `ArrayStoreException`). Invariance — safe, но негибко. Поэтому wildcards — для variance.
- **Covariance через `<? extends T>`**: можно читать T, но нельзя писать (кроме `null`) — компилятор не знает точный тип.
- **Contravariance через `<? super T>`**: можно писать T (и его подтипы), но читать только как `Object` (точный тип неизвестен).
- **`<T extends Comparable<? super T>>`** — рекурсивный bound, используется в `Collections.sort`. Позволяет сортировать `List<Man>` где `Man extends Person` и `Person implements Comparable<Person>`.
- **Generic methods**: `<T> T identity(T x) { return x; }`. Может быть в generic- и non-generic-классе. Type inference обычно работает, иногда нужно явно `<String>identity("a")`.
- **Type inference (Java 8 diamond, Java 10 `var`)**: `List<String> list = new ArrayList<>();` — компилятор выводит `String`. С Java 10 — `var list = new ArrayList<String>();`. Для generic-методов — target-type inference.
- **`@SuppressWarnings("unchecked")`** — для подавления warnings в legacy-коде. Использовать с осторожностью, с комментарием почему safe.
- **Heap pollution**: `List<String> l = new ArrayList<>(); List raw = l; raw.add(1); String s = l.get(0);` — ClassCastException в runtime. Происходит при смешивании raw и generic. Effective Java Item 32 — `combine generics and varargs judiciously` (varargs создаёт `Object[]`, тип-unsafe).
- **`@SafeVarargs`** — аннотация для static/final/private generic-методов с varargs, подавляет unchecked warning. Документирует, что метод не делает heap pollution. Например, `Collections.asList(T... a)`.
- **Type token**: `Class<T>` — для передачи типа в runtime. `static <T> T parse(String s, Class<T> clazz)` — внутри `clazz.cast(obj)`. Используется в Jackson, Hibernate.
- **Reified generics** — НЕ в Java (в отличие от C#, Kotlin, Scala). Запрос на reified generics долгое время откладывается из-за backwards compat. Workaround — type tokens и `TypeReference` (Jackson).
- **Generic-exceptions**: нельзя `class MyException<T> extends Exception`. Generic-класс не может extend Throwable. Можно `class MyException<T> extends RuntimeException` только если не parameterized в throws — но обычно generic не нужен для exception.
- **Само-ограниченные типы (self-bounded)**: `class Enum<E extends Enum<E>>` — рекурсивный bound, обеспечивает, что `Enum`-наследник параметризован сам собой. Позволяет `compareTo(E o)` с правильным типом. Используется в `Enum<E extends Enum<E>>`, в Builder-паттерне.
- **Wildcard capture**: компилятор иногда может "захватить" тип wildcard-а в свежий type variable. `void foo(List<?> l) { rev(l); } void <T> rev(List<T> l) { ... }` — captures `?` как `T`.

### Материалы

- Книга: "Effective Java" Joshua Bloch (3rd ed., Addison-Wesley, 2018) — Item 26–33 (Generics полностью)
- Книга: "Java Generics and Collections" Maurice Naftalin & Philip Wadler (O'Reilly, 2006) — Chapter 1 (Fundamentals), Chapter 2 (Subtyping and Wildcards), Chapter 5 (Generics in the Java Library), Chapter 7 (Reflection)
- Книга: "Core Java Volume I—Fundamentals" Cay S. Horstmann (12th ed., 2021) — Chapter 8.8: Generic Code and the Virtual Machine (Type Erasure)
- Документация Oracle (Generics tutorial): https://docs.oracle.com/javase/tutorial/extra/generics/index.html
- Документация Oracle (Generics — Inheritance): https://docs.oracle.com/javase/tutorial/java/generics/inheritance.html
- Документация Oracle (Bridge Methods, Type Erasure effects): https://docs.oracle.com/javase/tutorial/java/generics/bridgeMethods.html
- Документация Oracle (Type Erasure): https://docs.oracle.com/javase/tutorial/java/generics/erasure.html
- Dev.java — Type Erasure: https://dev.java/learn/generics/type-erasure
- Baeldung — Type Erasure in Java Explained: https://www.baeldung.com/java-type-erasure
- Stackify — Generics and Overcoming Type Erasure on the JVM: https://stackify.com/jvm-generics-type-erasure
- JEP 8063180 (Historical JEP about generics): https://openjdk.org/jeps/0 (JLS §4 Type, Values, and Variables; §5.1.10 Capture Conversion)

### Практика / Кейсы

**Кейс 7.1. PECS в API — `Collections.copy`.**
- Постановка: объяснить сигнатуру `static <T> void copy(List<? super T> dest, List<? extends T> src)` — почему PECS.
- Шаги: (1) `src` поставляет элементы → `extends T`. (2) `dest` принимает → `super T`. (3) Демонстрация: `copy(new ArrayList<Number>(), List.of(1, 2, 3))` — T=Number. `dest` может быть `List<Object>` (super of Integer). (4) Без wildcards: `copy(List<T>, List<T>)` — потребует точного совпадения. (5) Реализовать свой `static <T> List<T> filter(List<? extends T> list, Predicate<? super T> pred)` — продемонстрировать PECS в Composition. (6) Написать тесты с разными combo.
- Что проверить: способность применять PECS в API design.

**Кейс 7.2. Bridge methods и override.**
- Постановка: показать bridge-методы через `javap -v` и объяснить их необходимость.
- Шаги: (1) `class Node<T> { void set(T v) { ... } } class StringNode extends Node<String> { @Override void set(String v) { ... } }`. (2) `javap -v StringNode` — увидеть `void set(java.lang.String)` и синтетический `void set(java.lang.Object)` (bridge, ACC_BRIDGE | ACC_SYNTHETIC). (3) Bridge-метод: `set(Object s) { set((String) s); }` — вызывает ваш `set(String)`. (4) Без bridge — вызов `Node.set(Object)` через `Node`-ссылку не нашёл бы переопределение. (5) Демонстрация ClassCastException: если передать неправильный тип через raw — упадёт в bridge. (6) Сравнить с C# reified generics — там bridge не нужны.
- Что проверить: способность читать байткод, понимание erasure+polymorphism.

**Кейс 7.3. Generic singleton pattern.**
- Постановка: реализовать type-safe generic singleton `Comparator` для natural ordering.
- Шаги: (1) Наивный `static <T> Comparator<T> naturalOrder() { return (a,b) -> ((Comparable<T>)a).compareTo(b); }` — unchecked cast. (2) Type-safe: `class NaturalComparator<T extends Comparable<? super T>> implements Comparator<T> { public int compare(T a, T b) { return a.compareTo(b); } }` — checked. (3) Singleton: `static Comparator<?> INSTANCE = new NaturalComparator<>();` и `static <T extends Comparable<? super T>> Comparator<T> naturalOrder() { return (Comparator<T>) INSTANCE; }` — unchecked, но safe (stateless). (4) Сравнить с `Comparator.naturalOrder()` из JDK (именно так и реализовано). (5) Объяснить, почему stateless generic-singleton safe despite unchecked.
- Что проверить: эрудиция по `Comparator.naturalOrder`, ability to design type-safe singletons.

**Кейс 7.4. Type token и супер-type-token (TypeReference).**
- Постановка: реализовать `parse(String json, Class<T> clazz)` и `parseList(String json, TypeReference<List<Foo>> typeRef)` — второе нужно из-за erasure.
- Шаги: (1) `static <T> T parse(String json, Class<T> clazz)` — нормально для `Foo`. (2) Для `List<Foo>` — `Class<List>` теряет `Foo`. (3) `abstract class TypeReference<T> { private final Type type; protected TypeReference() { this.type = ((ParameterizedType) getClass().getGenericSuperclass()).getActualTypeArguments()[0]; } public Type getType() { return type; } }`. (4) Вызов: `parseList(json, new TypeReference<List<Foo>>(){})` — anonymous class фиксирует type. (5) Использовать внутри Jackson `ObjectMapper`/`Gson`. (6) Объяснить, что это workaround отсутствия reified generics.
- Что проверить: глубокое понимание reflection generics, ability to read ParameterizedType.

**Кейс 7.5. Heap pollution и @SafeVarargs.**
- Постановка: показать heap pollution и правильно использовать `@SafeVarargs`.
- Шаги: (1) `static <T> List<T> of(T... items) { return new ArrayList<>(Arrays.asList(items)); }` — warning: Possible heap pollution from parameterized vararg type T. (2) Demonstrate pollution: `Object[] arr = items; arr[0] = "string";` — ClassCastException при чтении как T. (3) Метод safe, если не пишет в varargs-массив — добавить `@SafeVarargs` (только static/final/private с Java 9+, до этого только static). (4) Несейф метод: `static <T> T[] toArray(Collection<T> c) { return c.toArray(new T[0]); }` — нельзя `new T[]`, нужен `Class<T>` или `Object[]` cast. (5) Правильно: `c.toArray((T[]) Array.newInstance(clazz, c.size()))`.
- Что проверить: способность рассуждать о heap pollution, `@SafeVarargs` semantics.

**Кейс 7.6. Self-bounded type в Builder.**
- Постановка: реализовать иерархию Builder-ов с self-bounded типом, чтобы `withX()` возвращал правильный подтип.
- Шаги: (1) `abstract class Builder<B extends Builder<B>> { abstract B self(); public B withName(String n) { ...; return self(); } }`. (2) `class PersonBuilder extends Builder<PersonBuilder> { PersonBuilder self() { return this; } }`. (3) `class EmployeeBuilder extends PersonBuilder { ... }` — `withName` возвращает `PersonBuilder`, не `EmployeeBuilder` — проблема. (4) Решение: `class EmployeeBuilder extends Builder<EmployeeBuilder>`. (5) Демонстрация fluent API: `new EmployeeBuilder().withName("X").withSalary(100).build()`. (6) Сравнить с Lombok `@Builder` — как он решает.
- Что проверить: понимание self-bounded generics (recursive bound), ability to design fluent APIs.

---

## 8. Enum

### Теория

- **`enum` в Java** — полноценный класс, наследник `java.lang.Enum` (неявно final, не может extends другой класс, но может implements интерфейсы). Все enum-константы — статические поля своего типа. С Java 5.
- **Объявление**: `enum Color { RED, GREEN, BLUE }` — компилятор генерирует класс `Color extends Enum<Color>` с приватными конструктором, статическими полями `RED`, `GREEN`, `BLUE` типа `Color`, методами `values()`, `valueOf(String)`, `ordinal()`, `name()`, `compareTo` (final).
- **Конструкторы**: только `private` (или package-private). Вызываются при загрузке enum-класса, для каждой константы. Не могут вызываться извне.
- **Поля и методы**: enum может иметь instance-поля, методы. Каждая константа может иметь собственное тело (anonymous subclass): `enum Op { PLUS { int apply(int a, int b) { return a + b; } }, MINUS { int apply(int a, int b) { return a - b; } }; abstract int apply(int a, int b); }`.
- **`values()`** возвращает копию массива (не view) — каждый вызов аллоцирует. Для часто используемого — кэшировать: `private static final Op[] VALUES = values();`.
- **`valueOf(String)`** — lookup по имени, бросает `IllegalArgumentException` если не найдено. Use case — парсинг конфигурации.
- **`ordinal()`** — позиция в объявлении (0-based). Не использовать для бизнес-логики (Effective Java Item 35: use instance fields instead of ordinals), т.к. переупорядочивание ломает код.
- **`EnumSet<E>`** — Set, реализованный как bit vector (или `RegularEnumSet` для ≤64 констант, `JumboEnumSet` для больших). O(1) add/remove/contains, очень compact. Идеален для флагов. `EnumSet.of(RED, GREEN)`, `EnumSet.allOf(Color.class)`, `EnumSet.noneOf(Color.class)`, `EnumSet.range(from, to)`, `EnumSet.complementOf(set)`.
- **`EnumMap<K extends Enum<K>, V>`** — Map, реализованный как массив с индексом `ordinal()`. O(1) операции. Идеален для enum-ключей. Создавать через `new EnumMap<>(Color.class)`.
- **Enum singleton**: `enum Singleton { INSTANCE; public void doSomething() { ... } }` — recommended способ (Effective Java Item 3). Гарантия одного экземпляра (даже против reflection-атаки, serialization-safe). Использование: `Singleton.INSTANCE.doSomething()`.
- **Enum и serialization**: enum-константы сериализуются по имени (`name()`), при десериализации — `Enum.valueOf(type, name)`. Никаких кастомных `readObject`/`writeObject` — JVM гарантирует identity. Даже если сериализовать через `ObjectOutputStream`, получите тот же singleton.
- **Enum с состоянием** — usually bad idea: enum-константы по природе singleton-ы, mutable state приводит к утечкам между "инстансами". Если нужно state — держать в `ThreadLocal` или внешнем map.
- **Enum реализует интерфейс**: `enum Color implements Paint { RED { public void paint() { ... } }, GREEN { public void paint() { ... } }; }` — полиморфное поведение. Альтернатива abstract method на enum-уровне.
- **Strategy pattern через enum**: `enum ShippingStrategy { STANDARD(t -> baseRate(t)), EXPRESS(t -> baseRate(t) * 2); private final Function<Double, Double> calc; ShippingStrategy(Function<Double, Double> c) { this.calc = c; } public double cost(double weight) { return calc.apply(weight); } }`.
- **Enum и switch**: `switch` по enum — compile-time exhaustive check (если нет default и значение не из enum — compile error для `Color c = ...; switch(c) { case RED: ... }`). На самом деле в байткоде всегда есть default → бросает исключение, но компилятор проверяет.
- **`Enum.ordinal` и enum-implemented patterns**: Для value-object-функциональности лучше использовать explicit field: `enum HttpVerb { GET("GET"), POST("POST"); private final String code; ... }`.
- **Enum valueOf из string с default**: `static <E extends Enum<E>> E fromString(Class<E> type, String s, E def) { try { return Enum.valueOf(type, s); } catch (IllegalArgumentException e) { return def; } }`.
- **EnumSet vs bitwise flags**: enum-флаги в `int` (как `java.lang.reflect.Modifier`) — compact, но type-unsafe. `EnumSet` — type-safe, чуть медленнее (boxing в int), но почти всегда предпочтительнее.

### Материалы

- Книга: "Effective Java" Joshua Bloch (3rd ed., Addison-Wesley, 2018) — Item 34 (use enums instead of int constants), Item 35 (use instance fields instead of ordinals), Item 36 (use EnumSet instead of bit fields), Item 37 (use EnumMap instead of ordinal indexing)
- Книга: "Java Generics and Collections" Maurice Naftalin & Philip Wadler (O'Reilly, 2006) — Chapter 12: Enum Types (раздел про EnumSet/EnumMap)
- Книга: "Core Java Volume I—Fundamentals" Cay S. Horstmann (12th ed., 2021) — Chapter 5.7: Enumerated Types
- Документация Oracle (Enum Types): https://docs.oracle.com/javase/tutorial/java/javaOO/enum.html
- Документация Oracle (EnumSet Javadoc): https://docs.oracle.com/en/java/javase/21/docs/api/java.base/java/util/EnumSet.html
- Документация Oracle (EnumMap Javadoc): https://docs.oracle.com/en/java/javase/21/docs/api/java.base/java/util/EnumMap.html
- Документация Oracle (`Enum` class Javadoc): https://docs.oracle.com/en/java/javase/21/docs/api/java.base/java/lang/Enum.html
- Stack Overflow — How to initialize Java enum based Singleton: https://stackoverflow.com/questions/12697726/how-to-initialize-java-enum-based-singleton
- DZone — Java Singletons Using Enum: https://dzone.com/articles/java-singletons-using-enum
- GeeksforGeeks — Advantages and Disadvantages of using Enum as Singleton: https://www.geeksforgeeks.org/java/advantages-and-disadvantages-of-using-enum-as-singleton-in-java

### Практика / Кейсы

**Кейс 8.1. EnumSet как bitmask для permissions.**
- Постановка: реализовать систему прав доступа через `EnumSet<Permission>` и протестировать throughput vs `int`-bitmask.
- Шаги: (1) `enum Permission { READ, WRITE, EXECUTE, DELETE }`. (2) `EnumSet<Permission> perms = EnumSet.of(READ, EXECUTE)`. (3) Проверка: `perms.contains(WRITE)` → false. (4) Intersection: `EnumSet.copyOf(perms); perms.retainAll(other)`. (5) JMH: сравнить с `int perms = READ_MASK | EXECUTE_MASK` и `(perms & WRITE_MASK) != 0` — обычно int быстрее в 1.5–2 раза, но EnumSet type-safe. (6) Сравнить memory: EnumSet ~ объект + long, int — 4 байта. Для высоконагруженных систем — int, для типичных enterprise — EnumSet.
- Что проверить: понимание trade-off type-safety vs raw performance.

**Кейс 8.2. Enum singleton vs classic singleton — reflection attack.**
- Постановка: показать, что reflection может создать второй экземпляр классического singleton, но не enum-singleton.
- Шаги: (1) `class ClassicSingleton { private static ClassicSingleton instance; private ClassicSingleton() {} public static ClassicSingleton getInstance() { ... } }`. (2) Через reflection: `Constructor c = ClassicSingleton.class.getDeclaredConstructor(); c.setAccessible(true); ClassicSingleton s2 = c.newInstance();` — создан второй. (3) Для enum: `Constructor<Singleton> c = Singleton.class.getDeclaredConstructor(String.class, int.class); c.setAccessible(true); c.newInstance("FAKE", 0);` → `IllegalArgumentException: Cannot reflectively create enum objects`. (4) Serialization: classic — readResolve необходим, enum — handled automatically. (5) Вывод: enum-singleton — recommended.
- Что проверить: понимание singleton pitfalls, security benefits enum.

**Кейс 8.3. EnumMap для dispatch-таблицы.**
- Постановка: заменить switch по enum на EnumMap<Enum, Handler> для расширяемости.
- Шаги: (1) `enum EventType { LOGIN, LOGOUT, PURCHASE }`. (2) Switch: `switch(event) { case LOGIN: ...; case LOGOUT: ...; ... }` — каждый раз при добавлении нового event нужно менять switch. (3) EnumMap: `EnumMap<EventType, Handler> handlers = new EnumMap<>(EventType.class); handlers.put(LOGIN, new LoginHandler());` — расширяемо. (4) `handlers.getOrDefault(event, DEFAULT).handle()`. (5) Performance: EnumMap O(1), switch — tableswitch O(1), почти одинаково. (6) Plug-in архитектура — handlers можно регистрировать динамически.
- Что проверить: design patterns, open-closed principle.

**Кейс 8.4. Enum + abstract method = strategy.**
- Постановка: реализовать state machine через enum с abstract methods.
- Шаги: (1) `enum OrderState { NEW { OrderState next() { return PAID; } boolean canCancel() { return true; } }, PAID { OrderState next() { return SHIPPED; } boolean canCancel() { return false; } }, SHIPPED { OrderState next() { return DELIVERED; } boolean canCancel() { return false; } }, DELIVERED { OrderState next() { return this; } boolean canCancel() { return false; } }; abstract OrderState next(); abstract boolean canCancel(); }`. (2) Тесты: transitions, canCancel на каждом state. (3) Динамически менять state — переход через `state.next()`. (4) Сравнить с class-hierarchy state pattern — enum более компактный. (5) Минус: при добавлении нового state — нужно дописать все abstract methods (компилятор заметит, хорошо).
- Что проверить: polymorphism в enum, state machine design.

**Кейс 8.5. Enum и обратная совместимость — добавление новой константы.**
- Постановка: при добавлении `SUPER_ADMIN` в `enum Role` найти все места, которые нужно обновить.
- Шаги: (1) В switch без default — compile error (если используется enum). (2) В `valueOf` — runtime `IllegalArgumentException` при чтении старых данных. (3) В serialization — старые инстансы с SUPER_ADMIN не десериализуются в старом коде. (4) Использовать `EnumSet.allOf(Role.class)` для проверки coverage. (5) Тесты: для каждой константы проверить поведение во всех бизнес-функциях. (6) Document: enum-эволюция должна быть backward-compatible (не удалять, только добавлять).
- Что проверить: эволюция API, backward compatibility.

**Кейс 8.6. Enum с params и validation.**
- Постановка: enum `HttpStatusCode` с кодом и описанием, метод fromCode(int).
- Шаги: (1) `enum HttpStatusCode { OK(200, "OK"), NOT_FOUND(404, "Not Found"), INTERNAL_ERROR(500, "Internal Server Error"); private final int code; private final String reason; HttpStatusCode(int code, String reason) { this.code = code; this.reason = reason; } public int code() { return code; } static HttpStatusCode fromCode(int c) { for (var v : values()) if (v.code == c) return v; throw new IllegalArgumentException(); } }`. (2) Оптимизировать `fromCode` через статический `Map<Integer, HttpStatusCode>` инициализируемый в static-блоке. (3) Покрыть тестами: все коды, unknown code. (4) Расширить до категории: `1xx Informational`, `2xx Success` через группировку. (5) Сериализовать по `code` (Jackson `@JsonValue`).
- Что проверить: enum design patterns, performance optimisation.

---

## 9. I/O и NIO

### Теория

- **`java.io` (классический I/O)** — потоковый (stream-based), блокирующий. Классы: `InputStream`, `OutputStream`, `Reader`, `Writer`, `File`, `RandomAccessFile`, `BufferedInputStream`, `BufferedReader`, `PrintWriter`. Иерархия с decorator-паттерном (например, `BufferedReader` оборачивает `Reader`).
- **`InputStream` / `OutputStream`** — byte-oriented (8 bit). `read()` возвращает `int` (0–255) или -1 (EOF). `read(byte[])`, `read(byte[], int, int)`. `write(int b)`, `write(byte[])`. `flush()`, `close()`. Все реализуют `Closeable` extends `AutoCloseable`.
- **`Reader` / `Writer`** — character-oriented (16 bit UTF-16), работают с `char`. Декодируют bytes → chars через `Charset`. `InputStreamReader` bridges InputStream → Reader с указанием charset. `FileReader` до Java 11 использовал default charset — плохая практика. С Java 11 есть конструктор `FileReader(File, Charset)`.
- **Buffering**: `BufferedInputStream`, `BufferedReader` — внутренний buffer (default 8KB), уменьшают syscalls. Без buffering каждый `read()` — syscall, медленно. `BufferedReader.readLine()` — удобно для текста.
- **`PrintStream` / `PrintWriter`** — не бросают `IOException` ( swallowing, можно `checkError()`). `System.out` — `PrintStream`. `printf(String format, Object... args)` — форматирование. Auto-flush.
- **`File` (java.io)** — представляет файл или директорию. `exists()`, `isFile()`, `isDirectory()`, `length()`, `delete()`, `mkdirs()`, `listFiles()`, `renameTo()`. Манипуляции с файловой системой (не с содержимым). Ограниченный metadata API.
- **`java.nio.file` (Java 7, NIO.2)** — современная замена `File`. `Path` — immutable, представляет путь (не обязательно существующий). `Paths.get("a/b")` (устаревший factory), `Path.of("a/b")` (Java 11+). `Files` — утилитный класс с методами: `readAllBytes`, `readString`, `write`, `lines`, `exists`, `createDirectory`, `createDirectories`, `copy`, `move`, `delete`, `size`, `getLastModifiedTime`, `walk`, `find`, `probeContentType`.
- **`Path`** — immutable. `resolve(other)` — конкатенация. `relativize(other)` — относительный путь. `normalize()` — удалить `.`/`..`. `toAbsolutePath()`. `getParent()`, `getFileName()`, `getRoot()`. `toUri()`.
- **`Files.lines(Path)`** — возвращает `Stream<String>` (ленивый), закрывает файл при `close()` (нужно try-with-resources). В отличие от `Files.readAllLines` (загружает весь файл в `List<String>` — OOM для больших файлов).
- **`Files.readString(Path)` / `Files.readString(Path, Charset)`** (Java 11) — весь файл в String. Удобно для конфигов. `Files.writeString(Path, CharSequence)` — запись.
- **File walking**: `Files.walk(Path)` — обход дерева каталогов (depth-first), возвращает `Stream<Path>`. `Files.walk(Path, maxDepth)`. `Files.find(Path, maxDepth, BiPredicate)` — с фильтром. Ленивый, закрывается через try-with-resources.
- **`WatchService`** — наблюдение за изменениями в директории (создание, модификация, удаление). `WatchKey` poll. Полезно для hot-reload, но имеет платформо-зависимую точность.
- **`java.nio.channels`** — каналы для NIO. `FileChannel` — для файлов, поддерживает `position`, `transferTo`, `transferFrom` (zero-copy на некоторых платформах), `map(MapMode, position, size)` — memory-mapped I/O. `DatagramChannel`, `SocketChannel`, `ServerSocketChannel` — для сети.
- **`ByteBuffer`** — буфер для NIO. Свойства: `capacity` (вместимость), `position` (текущая позиция), `limit` (граница до которой можно читать/писать). `flip()` — переключение из write-mode в read-mode (limit=position, position=0). `clear()` — reset для записи. `rewind()` — position=0 (без изменения limit). `compact()` — копирует unread в начало, готов к записи.
- **Direct vs heap ByteBuffers**: `ByteBuffer.allocate(n)` — heap (в куче, GC-managed), `ByteBuffer.allocateDirect(n)` — off-heap (вне кучи, I/O через DMA, без копирования в kernel). Direct быстрее для больших I/O, но аллокация/деаллокация дороже. Пулировать через `BufferPoolMXBean` или фреймворки (Netty `ByteBufAllocator`).
- **Selector** (NIO) — multiplexor для неблокирующих каналов. Один поток обрабатывает много `SocketChannel`. Основа netty, node.js. Реже в application code, но Senior должен понимать.
- **Сериализация**: `Serializable` (marker interface) — `ObjectOutputStream.writeObject(obj)`, `ObjectInputStream.readObject()`. Не рекомендуется в новом коде: insecure (deserialization vulnerabilities — CVE-2015-4852 и др.), tied to internal state, versioning painful. Альтернативы — JSON (Jackson, Gson), protobuf, Avro.
- **`Externalizable`** — programmer-controlled serialization. `writeExternal(ObjectOutput)`, `readExternal(ObjectInput)`. Быстрее, но требует ручной реализации.
- **`serialVersionUID`** — версия класса для совместимости. Если не объявлен — компилятор вычисляет (зависит от структуры класса, хрупко). Лучше явно: `private static final long serialVersionUID = 1L;`. Несовпадение → `InvalidClassException`.
- **`transient`** — поле не сериализуется. Для кэшей, derived fields, sensitive data.
- **`readResolve` / `writeReplace`** — hook для замены объекта при (де)сериализации. Используется для singleton-инварианта (enum — автоматически).
- **`try-with-resources`** для I/O — must-have. Все `AutoCloseable` закрываются гарантированно, даже при exception. Без него — resource leak.
- **`Scanner`** — простое чтение с parsing (nextLine, nextInt, nextDouble). Медленнее `BufferedReader` + ручной parsing, но удобен. `Scanner(File)` использует default charset (до Java 10), с Java 10 — `Scanner(File, Charset)`.
- **`Console`** (`System.console()`) — для CLI-приложений, `readPassword` (без echo). `null` в IDE (только в реальной консоли).

### Материалы

- Книга: "Effective Java" Joshua Bloch (3rd ed., Addison-Wesley, 2018) — Item 9 (prefer `try-with-resources`), Item 75 (failure-capture in exceptions, including I/O), Item 85–86 (alternatives to Java serialization — security), Item 87 (singleton with `readResolve`)
- Книга: "Core Java Volume I—Fundamentals" Cay S. Horstmann (12th ed., 2021) — Chapter 1.7 (Command-line I/O), Chapter 9.4 (Working with Files, NIO.2)
- Книга: "Core Java Volume II—Advanced Features" Cay S. Horstmann (12th ed., 2021) — Chapter 1: Streams and Files (раздел NIO, channels, ByteBuffer)
- Книга: "Java NIO" Ron Hitchens (O'Reilly, 2002) — классика по NIO (устаревшая по API, но концепции актуальны)
- Документация Oracle (Basic I/O): https://docs.oracle.com/javase/tutorial/essential/io/index.html
- Документация Oracle (File I/O): https://docs.oracle.com/javase/tutorial/essential/io/fileio.html
- Документация Oracle (NIO.2 — File I/O Features): https://docs.oracle.com/javase/tutorial/essential/io/fileio.html
- Документация Oracle (Path Javadoc): https://docs.oracle.com/en/java/javase/21/docs/api/java.base/java/nio/file/Path.html
- Документация Oracle (Files Javadoc): https://docs.oracle.com/en/java/javase/21/docs/api/java.base/java/nio/file/Files.html
- Документация Oracle (ByteBuffer Javadoc): https://docs.oracle.com/en/java/javase/21/docs/api/java.base/java/nio/ByteBuffer.html
- Документация Oracle (Serializable Javadoc): https://docs.oracle.com/en/java/javase/21/docs/api/java.base/java/io/Serializable.html
- Baeldung — Guide to Java FileChannel: https://www.baeldung.com/java-filechannel
- Jenkov — Java NIO FileChannel: https://jenkov.com/tutorials/java-nio/file-channel.html
- Jenkov — Java NIO Tutorial: https://jenkov.com/tutorials/java-nio/index.html
- Javarevisited — Reading/Writing Files using FileChannel and ByteBuffer: https://javarevisited.blogspot.com/2016/01/reading-writing-files-using-filechannel-bytebuffer-example.html
- Brown CS — Getting started with NIO (PDF): https://cs.brown.edu/courses/cs161/papers/j-nio-ltr.pdf

### Практика / Кейсы

**Кейс 9.1. Memory-mapped file для больших данных.**
- Постановка: обработать файл 50GB (больше RAM), не загружая в память.
- Шаги: (1) `try (FileChannel ch = FileChannel.open(path)) { MappedByteBuffer mbb = ch.map(MapMode.READ_ONLY, 0, ch.size()); ... }`. (2) OS сама page-in нужные страницы. (3) Читать как `mbb.getInt()`, `mbb.getLong()`. (4) Limit: max map size ~2GB на 32-bit, на 64-bit — практически без ограничений, но нужно `chunk`-ать. (5) `force()` для flush в файл. (6) Сравнить throughput с `BufferedInputStream` через JMH — mmap обычно быстрее в 3–10x на random access.
- Что проверить: понимание mmap, OS page cache, chunking strategy.

**Кейс 9.2. Zero-copy transfer через `transferTo`.**
- Постановка: копировать файл 5GB с минимальным CPU и без промежуточных буферов в user-space.
- Шаги: (1) `try (FileChannel src = open(srcPath, READ); FileChannel dst = open(dstPath, WRITE)) { src.transferTo(0, src.size(), dst); }`. (2) На Linux — `sendfile` syscall, копирование внутри kernel, без перехода в user-space. (3) Сравнить с ручным `ByteBuffer.allocate(8192)` loop — throughput через JMH. (4) Для network I/O — `transferTo` также использует zero-copy (sendfile). (5) Ограничения: не работает для S3-объектов (нужна stream-API), только local files / sockets.
- Что проверить: понимание zero-copy, syscall optimization.

**Кейс 9.3. Tail-f для лог-файла (как `tail -f`).**
- Постановка: реализовать программу, читающую прирастающий лог-файл в реальном времени.
- Шаги: (1) Открыть `RandomAccessFile` или `FileChannel`. (2) `channel.position(channel.size())` — стать в конец. (3) Loop: read available bytes, sleep 100ms если ничего. (4) На новой строке — обработать. (5) Обработка rotation (log4j создает новый файл): `WatchService` на директорию, при `ENTRY_MODIFY`/`ENTRY_CREATE` проверять inode. (6) Покрыть edge cases: пустой файл в начале, truncation, медленный writer.
- Что проверить: способность проектировать real-time pipelines.

**Кейс 9.4. Custom Externalizable vs Serializable — производительность.**
- Постановка: сравнить throughput и размер сериализации для DTO с 10 полями.
- Шаги: (1) `class Dto implements Serializable` с `serialVersionUID`. (2) То же через `Externalizable` с ручной записью каждого поля (`out.writeLong(id); out.writeUTF(name);`). (3) То же через Jackson JSON. (4) То же через Protobuf. (5) JMH: размер bytes и время (de)serialize. (6) Обычно: Protobuf < Externalizable < JSON << Serializable (по размеру), производительность — Protobuf ≈ Externalizable < JSON << Serializable.
- Что проверить: понимание serialization trade-offs, ability to choose.

**Кейс 9.5. Deserialization vulnerability.**
- Постановка: продемонстрировать уязвимость десериализации и mitigation.
- Шаги: (1) Создать `class Evil implements Serializable { private void readObject(ObjectInputStream in) throws IOException, ClassNotFoundException { in.defaultReadObject(); Runtime.getRuntime().exec("calc"); } }`. (2) Сериализовать, десериализовать — `readObject` выполнит код. (3) Mitigation: `ObjectInputFilter` (Java 9, JEP 290): `ObjectInputStream ois = ...; ois.setObjectInputFilter(info -> info.serialClass() == Dto.class ? ObjectInputFilter.Status.ALLOWED : Status.REJECTED);`. (4) Best practice: не использовать Java serialization для untrusted input. JSON / protobuf с явной схемой. (5) Ссылка на CVE-2015-4852 (Apache Commons Collections).
- Что проверить: security awareness, defense in depth.

**Кейс 9.6. Walk + parallel для обработки дерева файлов.**
- Постановка: посчитать суммарный размер всех `.log` файлов в `/var/log` рекурсивно, используя parallel stream.
- Шаги: (1) `try (Stream<Path> s = Files.walk(Paths.get("/var/log"))) { long total = s.filter(p -> p.toString().endsWith(".log")).filter(Files::isRegularFile).mapToLong(p -> { try { return Files.size(p); } catch (IOException e) { return 0; } }).sum(); }`. (2) Parallel: `.parallel()` — но общий ForkJoinPool, может блокировать. (3) Для больших деревьев — лучше явный `ExecutorService`. (4) Edge cases: символические ссылки (`Files.walk` по умолчанию не следует), нет прав (AccessDeniedException — нужно catch). (5) Сравнить throughput с single-thread + NIO.2.
- Что проверить: ability to design parallel I/O, handle errors.

---

## 10. java.time API

### Теория

- **java.time (Java 8, JSR 310, JEP 150)** — современная замена `java.util.Date`/`Calendar`. Полностью immutable, thread-safe, хорошо спроектированный API. Базируется на стандарте ISO-8601.
- **Основные классы**:
  - `LocalDate` — дата без времени и таймзоны (`2024-01-15`).
  - `LocalTime` — время без даты и таймзоны (`10:30:00`).
  - `LocalDateTime` — дата+время без таймзоны (`2024-01-15T10:30:00`).
  - `ZonedDateTime` — дата+время с таймзоной (`2024-01-15T10:30:00+03:00[Europe/Moscow]`).
  - `OffsetDateTime` — дата+время с offset (без zone rules) (`2024-01-15T10:30:00+03:00`).
  - `Instant` — мгновение на timeline (UTC), наносекундная точность.
  - `Duration` — время в секундах и наносекундах (machine-scale).
  - `Period` — дата-период (годы, месяцы, дни — human-scale).
  - `Year`, `YearMonth`, `MonthDay` — частичные представления.
- **`Instant`** — точка на timeline, UTC. `Instant.now()` — текущее. `Instant.ofEpochSecond(seconds)`, `ofEpochMilli(ms)`. Наносекундная точность (хотя точность системных часов зависит от OS). Гарантированно monotone (через `Clock.systemUTC()`).
- **`LocalDate`**: `LocalDate.of(2024, 1, 15)`, `LocalDate.parse("2024-01-15")`, `LocalDate.now()`, `now(ZoneId)`. Методы: `plusDays/Weeks/Months/Years`, `minus...`, `withYear/Month/DayOfMonth`, `with(TemporalAdjuster)` (например, `lastDayOfMonth()`), `getDayOfWeek` (DayOfWeek enum), `getDayOfYear`, `isLeapYear()`, `lengthOfMonth()`, `lengthOfYear()`.
- **`LocalTime`**: `LocalTime.of(10, 30, 0)`, `parse("10:30:00")`. `plusHours/Minutes/Seconds/Nanos`, `minus...`, `withHour(...)`.
- **`LocalDateTime`**: комбинация. Не хранит таймзону — ambiguous (один и тот же `LocalDateTime` в разных зонах — разные моменты). Нельзя конвертировать в `Instant` без зоны.
- **`ZonedDateTime`**: `ZonedDateTime.of(LocalDateTime, ZoneId)`, `ZonedDateTime.now(ZoneId.of("Europe/Moscow"))`. Хранит zone rules — корректно работает с DST. `toInstant()` — конвертация в UTC.
- **`OffsetDateTime`**: упрощённая версия — только offset (например, `+03:00`), без zone rules. Не понимает DST. Используется в протоколах (например, HTTP, XML).
- **`ZoneId`**: идентификатор таймзоны (`"Europe/Moscow"`, `"America/New_York"`, `"UTC"`). `ZoneId.of(...)`, `ZoneId.systemDefault()`, `ZoneId.getAvailableZoneIds()`. Правила загружаются из TZ database (обновляется).
- **`Duration`**: time-based (часы, минуты, секунды, наносекунды). `Duration.between(start, end)`, `Duration.ofMinutes(30)`, `ofHours(2)`, `plus(...)`, `toMillis()`, `toMinutes()`, `toSeconds()`. Immutable. Не теряет точность при конвертации.
- **`Period`**: date-based (годы, месяцы, дни). `Period.between(date1, date2)`, `Period.ofYears(1)`, `ofMonths(3)`, `ofDays(7)`, `plus(Period)`, `minus(Period)`. `getYears()`, `getMonths()`, `getDays()`. Нормализуется: `Period.of(1, 13, 0)` = `P2Y1M` (год и месяц).
- **`Duration` vs `Period`**: `Duration` — для точного времени (machine), `Period` — для человеческих концепций (1 месяц ≠ 30 дней точно). `LocalDate.now().plus(Period.ofMonths(1))` — добавит 1 календарный месяц. `LocalDate.now().plus(Duration.ofDays(30))` — добавит 30 дней (может отличаться от 1 месяца).
- **`TemporalAdjuster`**: корректировщики дат. `TemporalAdjusters.lastDayOfMonth()`, `firstDayOfYear()`, `firstInMonth(DayOfWeek)`, `next(DayOfWeek)`, `previous(DayOfWeek)`. Свой: `temporal -> temporal.with(DAY_OF_MONTH, 1)`.
- **`DateTimeFormatter`**: форматирование и парсинг. `DateTimeFormatter.ISO_LOCAL_DATE` (готовый), `ofPattern("yyyy-MM-dd HH:mm:ss")`, `ofPattern("d MMM uuuu", Locale.FRENCH)`. Thread-safe (в отличие от старого `SimpleDateFormat`). `LocalDate.parse(s, formatter)`, `date.format(formatter)`.
- **`DateTimeFormatter` patterns**: `yyyy` (год), `MM` (месяц 2 цифры), `dd` (день), `HH` (час 24), `mm` (минута), `ss` (секунда), `SSS` (миллисекунды), `Z` (зона offset), `V VV` (zone id), `E` (день недели), `MMM` (месяц буквенный). `uuuu` вместо `yyyy` рекомендуется (proleptic gregorian, поддержка BC).
- **`Clock`** — абстракция для времени, для testability. `Clock.systemUTC()`, `Clock.system(zone)`, `Clock.fixed(Instant, ZoneId)` (для тестов), `Clock.offset(clock, Duration)`. `LocalDateTime.now(clock)` — инъекция зависимости от времени.
- **Конвертация с legacy**: `Date.from(Instant)`, `Date.toInstant()`, `Calendar.toInstant()`, `GregorianCalendar.toZonedDateTime()`, `ZonedDateTime.toGregorianCalendar()`. `java.sql.Timestamp.from(Instant)`, `toInstant()`.
- **`Temporal` interface**: `Temporal` (для типов, к которым можно прибавить `TemporalAmount`), `TemporalAmount` (реализуют `Duration` и `Period`), `TemporalAdjuster` (функция корректировки), `TemporalQuery<R>` (запрос свойства, например `temporal.query(TemporalQueries.precision())`).
- **`ChronoUnit`**: enum единиц измерения (NANOS, MICROS, MILLIS, SECONDS, MINUTES, HOURS, HALF_DAYS, DAYS, WEEKS, MONTHS, YEARS, DECADES, CENTURIES, MILLENNIA, ERAS, FOREVER). `ChronoUnit.DAYS.between(date1, date2)`.
- **`Instant` vs `LocalDateTime`**: Instant — UTC момент (однозначный). LocalDateTime — wall-clock time без зоны (ambiguous). Для хранения в БД timestamp — лучше `Instant` или `OffsetDateTime`. Для расписаний (встреча в 10:00 в Москве) — `ZonedDateTime` или `LocalDateTime` + отдельная zone.
- **DST handling**: `ZonedDateTime` корректно обрабатывает transition. Например, при переходе на летнее время — 2 часа становится 3 часа, gap. ZonedDateTime берёт offset по правилам зоны. `ZonedDateTime.of(LocalDateTime.of(2024, 3, 31, 2, 30), ZoneId.of("Europe/Moscow"))` — ambiguous (если переход).
- **`Instant` precision**: наносекунды в API, но реальная точность часов OS-dependent (Linux — обычно наносекунды через `clock_gettime(CLOCK_REALTIME)`). Для measuring duration — `System.nanoTime()` (monotone, наносекундная).

### Материалы

- Книга: "Effective Java" Joshua Bloch (3rd ed., Addison-Wesley, 2018) — Item 9 (`try-with-resources` — для `Clock`, formatter), Item 81 (prefer concurrency utilities to `wait`/`notify` — `Clock` вместо `System.currentTimeMillis`)
- Книга: "Modern Java in Action" Urma, Fusco, Mycroft (Manning, 2018) — Chapter 12: New Date and Time API
- Книга: "Core Java Volume I—Fundamentals" Cay S. Horstmann (12th ed., 2021) — Chapter 6.4: Working with Dates and Times (jsr-310)
- Документация Oracle (Date Time): https://docs.oracle.com/javase/tutorial/datetime/index.html
- Документация Oracle (Period and Duration): https://docs.oracle.com/javase/tutorial/datetime/iso/period.html
- Документация Oracle (Instant Javadoc): https://docs.oracle.com/en/java/javase/21/docs/api/java.base/java/time/Instant.html
- Документация Oracle (ZonedDateTime Javadoc): https://docs.oracle.com/en/java/javase/21/docs/api/java.base/java/time/ZonedDateTime.html
- Документация Oracle (DateTimeFormatter Javadoc): https://docs.oracle.com/en/java/javase/21/docs/api/java.base/java/time/format/DateTimeFormatter.html
- Документация Oracle (Clock Javadoc): https://docs.oracle.com/en/java/javase/21/docs/api/java.base/java/time/Clock.html
- JEP 150: Date & Time API (Java 8, JSR 310): https://openjdk.org/jeps/150
- JEP 313: Remove the Native2ASCII Tool (relevant to unicode properties): https://openjdk.org/jeps/313
- Baeldung — Period and Duration in Java: https://www.baeldung.com/java-period-duration
- DigitalOcean — Java 8 Date LocalDate LocalDateTime Instant Guide: https://www.digitalocean.com/community/tutorials/java-8-date-localdate-localdatetime-instant
- Baeldung — Migrating to java.time: https://www.baeldung.com/migrating-to-java-time

### Практика / Кейсы

**Кейс 10.1. Schedule без багов с DST.**
- Постановка: cron-задача должна срабатывать в 09:00 по Москве каждый день, корректно в дни перехода на летнее/зимнее время.
- Шаги: (1) `LocalDateTime` + `ZoneId.of("Europe/Moscow")` → `ZonedDateTime`. (2) Каждый день: `ZonedDateTime next = ZonedDateTime.of(LocalDate.now().plusDays(1).atTime(9, 0), ZoneId.of("Europe/Moscow"));` (3) В день перехода на летнее время 09:00 может не существовать (gap) или существовать дважды (overlap). (4) `ZonedDateTime` берёт valid offset. (5) `Duration.between(now, next)` для подсчёта sleep. (6) Сравнить с хранением в UTC + offset — DST ломается. (7) Протестировать с `Clock.fixed` для симуляции перехода.
- Что проверить: глубокое понимание DST, ability to schedule correctly.

**Кейс 10.2. Clock injection для testability.**
- Постановка: метод с зависимостью от текущего времени должен быть unit-testable.
- Шаги: (1) Bad: `LocalDate.now()` в коде — непроверяемо. (2) Хорошо: `void process(Clock clock) { LocalDate today = LocalDate.now(clock); ... }`. (3) Тест: `Clock fixed = Clock.fixed(Instant.parse("2024-01-15T10:00:00Z"), ZoneOffset.UTC); process(fixed);` (4) Тест завтрашнего дня: `Clock tomorrow = Clock.offset(fixed, Duration.ofDays(1));` (5) Spring: `@Bean Clock clock() { return Clock.systemDefaultZone(); }` и `@Autowired Clock clock;` в сервисах. (6) Alternatives: `TimeProvider` interface + fake.
- Что проверить: dependency injection, ability to design testable code.

**Кейс 10.3. Парсинг дат из разных источников — unified parser.**
- Постановка: парсить даты из логов в трёх разных форматах (`2024-01-15T10:30:00Z`, `15/01/2024 10:30`, `Jan 15, 2024 10:30:00 AM UTC`).
- Шаги: (1) Три `DateTimeFormatter`: ISO_INSTANT, ofPattern("dd/MM/yyyy HH:mm"), ofPattern("MMM d, yyyy h:mm:ss a z", Locale.ENGLISH). (2) Метод `Instant parseAny(String s)`: try каждый formatter в порядке вероятности. (3) Возврат `Instant` для унификации. (4) Для `Jan 15...` — `Locale.ENGLISH` обязателен (иначе в русской локали не парсится). (5) Покрыть тестами все форматы + edge cases (timezone, 24h vs 12h). (6) Performance: pre-compiled `DateTimeFormatter` (instance), не `ofPattern` в каждом вызове.
- Что проверить: понимание formatter patterns, locales, performance.

**Кейс 10.4. Миграция с `Date`/`Calendar` на `java.time`.**
- Постановка: legacy-код использует `Date`, `Calendar`, `SimpleDateFormat`. Мигрировать без breaking API.
- Шаги: (1) Карта миграции: `Date` → `Instant` (через `toInstant()`), `Calendar` → `ZonedDateTime` (`GregorianCalendar.toZonedDateTime()`), `SimpleDateFormat` → `DateTimeFormatter`. (2) На уровне persistence — Hibernate 5+ поддерживает `java.time` нативно. (3) На уровне API (REST) — Jackson `jackson-datatype-jsr310` модуль, `@JsonFormat`. (4) Incremental: новые методы с `java.time`, старые deprecated, internally convert. (5) Удалить deprecated через release. (6) Тесты: back-and-forth conversion, проверка равенства момента.
- Что проверить: legacy migration strategy, backward compat.

**Кейс 10.5. High-precision timing через `Instant` vs `System.nanoTime`.**
- Постановка: измерить длительность операции с максимально возможной точностью.
- Шаги: (1) `long start = System.nanoTime(); op(); long elapsed = System.nanoTime() - start;` — monotone, наносекундная точность. (2) `Instant start = Instant.now(); ... Instant.now().minusNanos(start.toEpochMilli() * 1_000_000L)` — миллисекундная точность в `toEpochMilli`, наносекундная через `getNano()`. (3) Для бенчмарков — JMH. (4) Для логирования — `System.currentTimeMillis()` достаточно. (5) `Instant` можно сохранять в лог, сериализовать. `System.nanoTime()` — нельзя (epoch не определён, только differences). (6) Объяснить разницу между wall-clock и monotonic time.
- Что проверить: понимание clock types, ability to choose correct API.

**Кейс 10.6. Period vs Duration в бизнес-логике.**
- Постановка: подписка на 1 месяц — посчитать дату окончания, продления.
- Шаги: (1) Bad: `today.plusDays(30)` — некорректно (февраль → 28 дней, январь → 31). (2) Хорошо: `today.plus(Period.ofMonths(1))` — добавит 1 календарный месяц. (3) Edge case: 31 января + 1 месяц = 28/29 февраля (метод `plus` корректно обрабатывает, не существует 31 февраля). (4) Для автоматического продления: `subscriptionEnd.plus(Period.ofMonths(1))` — каждый раз. (5) Хранить в БД: `DATE` поле + период (не хранить `subscriptionEnd` как long epoch). (6) Тесты: всё дни месяцев, високосные годы.
- Что проверить: понимание calendar vs duration arithmetic.

---

## 11. equals/hashCode контракт, Comparable/Comparator

### Теория

- **Контракт `equals`** (Object Javadoc, Effective Java Item 10): reflexive (`x.equals(x)` true), symmetric (`x.equals(y)` ⇔ `y.equals(x)`), transitive (`x.equals(y) && y.equals(z)` → `x.equals(z)`), consistent (многократный вызов без изменений → тот же результат), `x.equals(null)` false.
- **Контракт `hashCode`** (Effective Java Item 11): если `x.equals(y)` → `hashCode(x) == hashCode(y)`. Обратное не обязательно (different objects могут иметь одинаковый hashCode — collision). Важно: при переопределении `equals` обязательно переопределять `hashCode`, иначе объект ломается в `HashMap`/`HashSet`.
- **Рецепт `equals`** (Effective Java Item 10): (1) использовать `==` для проверки того же объекта. (2) `instanceof` для типа. (3) cast. (4) Проверить значимые поля. Для `float`/`double` использовать `Float.compare`/`Double.compare` (или `Float.isNaN` для NaN). Для массивов — `Arrays.equals`.
- **Рецепт `hashCode`** (Effective Java Item 11, classic formula): `int result = 17; result = 31 * result + (field == null ? 0 : field.hashCode());` — для каждого значимого поля. Для примитивов — `Integer.hashCode(int)` и т.п. Для массивов — `Arrays.hashCode`. Или использовать `Objects.hash(Object...)` (внимание: varargs autoboxing).
- **Почему 31?** — нечётное простое, `31 * i == (i << 5) - i` — оптимизация JIT. Исторический выбор, не «магия». Можно использовать любое нечётное простое.
- **Lombok `@EqualsAndHashCode`** — генерирует автоматически. Опасность с наследованием: нужно `callSuper = true`, иначе теряются поля суперкласса.
- **`Objects.equals(a, b)`** — null-safe: `if (a == b || (a != null && a.equals(b)))`. Использовать для проверки полей-объектов.
- **Каноническая реализация через `record`**: автоматически генерируется `equals` по всем компонентам и `hashCode`. Нет необходимости в Lombok.
- **`Comparable<T>`** — интерфейс естественного упорядочения. `int compareTo(T other)` — отрицательное, 0, положительное. Контракт: antisymmetric (`sgn(a.compareTo(b)) == -sgn(b.compareTo(a))`), transitive (`a > b && b > c → a > c`), `a.compareTo(b) == 0` ⇔ `a.equals(b)` (strongly recommended, иначе sorted collections ведут себя странно).
- **`Comparator<T>`** — внешняя стратегия сравнения. `int compare(T a, T b)`. Используется в `Collections.sort(list, comparator)`, `TreeSet(comparator)`, `Stream.sorted(comparator)`.
- **`Comparator` default-методы (Java 8)**: `comparing(keyExtractor)`, `comparingInt`, `comparingLong`, `comparingDouble` (для примитивов без boxing), `thenComparing(other)` (chain), `reversed()`, `nullsFirst(cmp)`, `nullsLast(cmp)`, `naturalOrder()`, `reverseOrder()`.
- **`Comparator.comparing(Person::getName).thenComparingInt(Person::getAge)`** — chain для многоуровневой сортировки. Сначала по имени, при равенстве — по возрасту.
- **`compareTo` для примитивов**: `Integer.compare(a, b)`, `Long.compare`, `Double.compare` (NaN handling) — вместо `a - b`, который overflow на крайних значениях и не работает для NaN.
- **NaN handling**: `Double.NaN != Double.NaN` (в `equals`), но `Double.compare(NaN, x)` возвращает положительное (NaN считается больше всех). Это inconsistency между `equals` и `compareTo` — известная особенность.
- **`BigDecimal.equals` vs `compareTo`**: `new BigDecimal("1.0").equals(new BigDecimal("1.00"))` → false (different scale), но `compareTo` → 0. В `TreeSet<BigDecimal>` с natural ordering — `1.0` и `1.00` считаются одинаковыми (только один останется). В `HashSet` — оба останутся. Effective Java Item 14: consistency with equals.
- **`equals` и наследование** — главная проблема. Если добавить поле в подкласс, нельзя сохранить симметричность: `Point.equals ColoredPoint` может быть true, `ColoredPoint.equals Point` — false. Решения: (1) композиция вместо наследования (Effective Java Item 18), (2) `getClass()` вместо `instanceof` (но ломает LSP — подклассы не равны базовому), (3) abstract base class (не инстанциируется, не требует equals между уровнями).
- **`instanceof` vs `getClass` в equals**: `instanceof` сохраняет LSP, но ломает симметрию при добавлении полей в подклассе. `getClass()` строгий, но ломает LSP (подклассы не равны базовому). Effective Java рекомендует `getClass()` для final-классов или sealed-иерархий.
- **`equals` для `record`**: сравнивает по компонентам, корректно для record-иерархий (record не может наследовать, только реализовывать интерфейсы). Хорошо работает с sealed.
- **Persistent hash code**: `hashCode` должен вычисляться deterministically (не зависеть от mutable state, иначе объект в HashMap потеряется). Идеально — immutable fields или final вычисление в конструкторе.
- **`Objects.hash` caveat**: varargs autoboxing. `Objects.hash(1, 2, 3)` аллоцирует `Integer[]` и боксит. В hot path — медленно. Лучше вручную: `int h = 1; h = 31 * h + Integer.hashCode(a); h = 31 * h + Integer.hashCode(b); return h;`.
- **Cache `hashCode`**: для immutable объектов — вычислить один раз и закэшировать. `String` так и делает. Для `record` — компилятор генерирует cached version? Нет, на самом деле каждый раз перевычисляет (если только не переопределить).
- **`compareTo` для null**: `Comparator.nullsFirst(Comparator.naturalOrder())` — null-ы идут первыми. Без этого `a.compareTo(null)` бросает NPE (для `String.compareTo`).

### Материалы

- Книга: "Effective Java" Joshua Bloch (3rd ed., Addison-Wesley, 2018) — Item 10 (Obey the general contract when overriding equals), Item 11 (Always override hashCode when you override equals), Item 12 (Always override toString), Item 13 (Override clone judiciously), Item 14 (Consider implementing Comparable)
- Книга: "Java Generics and Collections" Maurice Naftalin & Philip Wadler (O'Reilly, 2006) — Chapter 3 (Comparison), Chapter 6 (The Comparable Interface)
- Книга: "Core Java Volume I—Fundamentals" Cay S. Horstmann (12th ed., 2021) — Chapter 5.2.3: Object: The Cosmic Superclass (equals, hashCode), Chapter 6.1.4: Comparators
- Документация Oracle (Object class): https://docs.oracle.com/en/java/javase/21/docs/api/java.base/java/lang/Object.html
- Документация Oracle (Comparable Javadoc): https://docs.oracle.com/en/java/javase/21/docs/api/java.base/java/lang/Comparable.html
- Документация Oracle (Comparator Javadoc): https://docs.oracle.com/en/java/javase/21/docs/api/java.base/java/util/Comparator.html
- Документация Oracle (Objects class): https://docs.oracle.com/en/java/javase/21/docs/api/java.base/java/util/Objects.html
- Baeldung — Java equals() and hashCode() Contracts: https://www.baeldung.com/java-equals-hashcode-contracts
- Baeldung — Comparator and Comparable in Java: https://www.baeldung.com/java-comparator-comparable
- Baeldung — Guide to Java Comparator.comparing(): https://www.baeldung.com/java-8-comparator-comparing
- Baeldung — Sorting in Java: https://www.baeldung.com/java-sorting
- Baeldung — Sort Collection by Multiple Fields: https://www.baeldung.com/java-sort-collection-multiple-fields
- DZone — Java Hashcode and Equals Deep Dive: https://dzone.com/articles/java-hashcode-and-equals-deep-dive
- Stack Overflow — Hashcode and equals methods contract: https://stackoverflow.com/questions/29159613/hashcode-and-equals-methods-contract

### Практика / Кейсы

**Кейс 11.1. Симметрия equals при наследовании — ломается.**
- Постановка: продемонстрировать нарушение симметрии при добавлении поля в подклассе и исправить через композицию.
- Шаги: (1) `class Point { int x, y; boolean equals(Object o) { ... по x, y ... } }`. (2) `class ColoredPoint extends Point { Color c; boolean equals(Object o) { ... по x, y, c ... } }`. (3) `Point p = new Point(1, 2); ColoredPoint cp = new ColoredPoint(1, 2, RED);` (4) `p.equals(cp)` — true (использует Point.equals). `cp.equals(p)` — false (использует ColoredPoint.equals, p не ColoredPoint). Симметрия нарушена. (5) Fix: композиция. `class ColoredPoint { Point point; Color color; ... }`. (6) Альтернатива: `getClass()` в equals — но тогда ColoredPoint не равен Point с теми же x, y, что может ломать LSP.
- Что проверить: понимание equals contract pitfalls, design trade-offs.

**Кейс 11.2. Хеш-функция и распределение.**
- Постановка: для 1M объектов проверить качество hashCode через χ²-test.
- Шаги: (1) Реализовать `Money` с `currency` и `amount`. (2) Naive hashCode: `currency.hashCode()`. (3) Good: `31 * currency.hashCode() + Long.hashCode(amountCents)`. (4) Сгенерировать 1M объектов, посчитать `hashCode` mod 1024. (5) Построить гистограмму, посчитать χ². (6) Naive — bad distribution (только от currency, ~3 значения). Good — равномерное. (7) Использовать `HashMap` с capacity 1024, измерить коллизии.
- Что проверить: понимание хеш-функций, ability to measure quality.

**Кейс 11.3. Comparator chain для многоуровневой сортировки.**
- Постановка: отсортировать сотрудников по департаменту (asc), затем по зарплате (desc), затем по имени (asc).
- Шаги: (1) `Comparator<Employee> byDept = Comparator.comparing(Employee::getDepartment);`. (2) `Comparator<Employee> bySalaryDesc = Comparator.comparing(Employee::getSalary).reversed();`. (3) `Comparator<Employee> byName = Comparator.comparing(Employee::getName);`. (4) Chain: `byDept.thenComparing(bySalaryDesc).thenComparing(byName)`. (5) Использовать `Comparator.nullsFirst` для nullable полей. (6) Тесты: проверить порядок для разных комбинаций. (7) Performance: один проход, O(n log n).
- Что проверить: способность строить сложные comparator-ы.

**Кейс 11.4. BigDecimal в HashMap vs TreeSet.**
- Постановка: продемонстрировать, что `BigDecimal` 1.0 и 1.00 — равны в TreeSet, но не в HashSet.
- Шаги: (1) `Set<BigDecimal> hashSet = new HashSet<>(); hashSet.add(new BigDecimal("1.0")); hashSet.add(new BigDecimal("1.00"));` → размер 2 (equals различает scale). (2) `Set<BigDecimal> treeSet = new TreeSet<>(); treeSet.add(new BigDecimal("1.0")); treeSet.add(new BigDecimal("1.00"));` → размер 1 (compareTo → 0). (3) Объяснить: TreeSet использует compareTo, HashSet — equals+hashCode. (4) Эффективное правило: использовать `BigDecimal.stripTrailingZeros()` перед сравнением/insert. (5) Для денежных сумм — лучше `long cents` или `Money` value object.
- Что проверить: глубокое понимание equals vs compareTo contract.

**Кейс 11.5. Кэширование hashCode для immutable.**
- Постановка: для immutable `Money` class кэшировать `hashCode` при первом вычислении.
- Шаги: (1) `private int hash;` (default 0 — sentinel). (2) `@Override public int hashCode() { int h = hash; if (h == 0) { h = 31 * currency.hashCode() + Long.hashCode(amountCents); hash = h; } return h; }`. (3) Доказать, что в `HashMap`-lookup ускоряется: hashCode не перевычисляется. (4) Caveat: если hashCode случайно 0 — будет перевычисляться каждый раз. Решение: sentinel через `boolean hashIsZero`. (5) `String` в JDK так и кэширует. (6) JMH измерить выигрыш.
- Что проверить: micro-optimization, awareness of sentinel issues.

**Кейс 11.6. Comparable с чужого класса — внешний Comparator.**
- Постановка: класс `Money` не реализует `Comparable` (или реализует не так), нужно отсортировать по amount desc.
- Шаги: (1) `Comparator<Money> byAmountDesc = Comparator.comparingLong(Money::getCents).reversed();`. (2) `Collections.sort(list, byAmountDesc)` или `list.sort(byAmountDesc)`. (3) Для `Stream<Money>` — `stream.sorted(byAmountDesc)`. (4) Альтернатива: обернуть в `MoneyWithNaturalOrder implements Comparable`. (5) Тесты на edge cases: null money, negative amounts. (6) Использовать `Comparator.nullsFirst(byAmountDesc)` для nullable.
- Что проверить: способность работать с legacy API.

---

## 12. Stream API

### Теория

- **Stream API (Java 8, JEP 107)** — декларативная обработка коллекций. Не структура данных, а pipeline операций. Источник (source) → промежуточные (intermediate, lazy) → терминальная (terminal, eager) операция.
- **Stream characteristics**: не хранит данные (проходной), functional (не мутирует source), lazy (промежуточные выполняются только при терминальной), возможно parallel (`parallelStream` или `stream().parallel()`).
- **Source**: `Collection.stream()`, `Stream.of(T...)`, `Stream.generate(Supplier)`, `Stream.iterate(seed, next)`, `Stream.iterate(seed, predicate, next)` (Java 9), `IntStream.range`, `IntStream.rangeClosed`, `BufferedReader.lines()`, `Files.lines(Path)`, `Stream.concat`, `IntStream.iterate`.
- **Intermediate operations (lazy)**: `filter(Predicate)`, `map(Function)`, `mapToInt/Long/Double`, `flatMap(Function<T, Stream<R>>)`, `distinct()`, `sorted()`, `sorted(Comparator)`, `peek(Consumer)`, `limit(n)`, `skip(n)`, `takeWhile(Predicate)` (Java 9), `dropWhile(Predicate)` (Java 9).
- **Terminal operations**: `forEach(Consumer)`, `forEachOrdered`, `toArray()`, `reduce(BinaryOperator)`, `reduce(identity, accumulator)`, `reduce(identity, accumulator, combiner)`, `collect(Collector)`, `min(Comparator)`, `max(Comparator)`, `count()`, `anyMatch`, `allMatch`, `noneMatch`, `findFirst()`, `findAny()`, `iterator()`.
- **Lazy execution**: промежуточные операции не выполняются до терминальной. `Stream<Integer> s = list.stream().filter(x -> x > 0).map(x -> x * 2);` — ничего не вычислено. `s.count()` запускает pipeline. Это позволяет short-circuit (`findFirst`, `limit`, `anyMatch`).
- **Short-circuit**: `findFirst` останавливает pipeline после первого найденного элемента. `limit(n)` берёт только n. `anyMatch` останавливается при первом true. Без lazy — не работало бы.
- **`map` vs `flatMap`**: `map` — 1 к 1 (каждый элемент → один результат). `flatMap` — 1 к N (каждый элемент → Stream, который "разворачивается" в один общий). Пример: `Stream<List<Integer>>` → `flatMap(List::stream)` → `Stream<Integer>`.
- **`collect`**: терминальная операция, аккумулирует элементы в коллекцию или другую структуру. `Collectors.toList()`, `toSet()`, `toMap(keyMapper, valueMapper)`, `toMap(key, value, mergeFunction)`, `toMap(key, value, merge, mapSupplier)`, `toUnmodifiableList()` (Java 10), `toUnmodifiableMap()`, `joining()`, `joining(delimiter, prefix, suffix)`, `counting()`, `summingInt/Long/Double`, `averagingInt/Long/Double`, `groupingBy(classifier)`, `groupingBy(classifier, downstream)`, `groupingBy(classifier, mapFactory, downstream)`, `partitioningBy(predicate)`, `partitioningBy(predicate, downstream)`, `mapping(mapper, downstream)`, `flatMapping(mapper, downstream)` (Java 9), `filtering(filter, downstream)` (Java 9), `reducing(BinaryOperator)`, `collectingAndThen(downstream, finisher)`, `teeing(c1, c2, merger)` (Java 12).
- **`groupingBy`**: `Map<K, List<T>> groupingBy(Function<T, K> classifier)`. Multilevel: `groupingBy(byDept, groupingBy(byGrade))` → `Map<Dept, Map<Grade, List<Emp>>>`. С downstream: `groupingBy(byDept, counting())` → `Map<Dept, Long>` (count per dept). С downstream: `groupingBy(byDept, mapping(Emp::getName, toList()))` → `Map<Dept, List<String>>`.
- **`partitioningBy`**: `Map<Boolean, List<T>>` по predicate. Эффективнее, чем groupingBy с boolean-classifier (две ячейки).
- **`toMap` merge function**: при дублирующемся ключе — `toMap` без merge function бросает `IllegalStateException`. `toMap(key, value, (a, b) -> a)` — оставляет первый, `(a, b) -> b)` — последний. `(a, b) -> a + b` — sum.
- **`reduce`**: `T reduce(T identity, BinaryOperator<T> accumulator)` — identity это нейтральный элемент (0 для сложения). `Optional<T> reduce(BinaryOperator<T>)` — без identity, возвращает Optional (пустой stream → empty). `<U> U reduce(U identity, BiFunction<U, ? super T, U> accumulator, BinaryOperator<U> combiner)` — для параллельных стримов, combiner объединяет результаты из разных потоков.
- **`parallelStream`**: делит источник на чанки, обрабатывает через общий `ForkJoinPool.commonPool()`. Для CPU-bound операций на больших данных — ускорение. Для маленьких или I/O-bound — overhead. Не использовать для потоков с side-effects. Внимание: order может не сохраняться (`forEach` vs `forEachOrdered`).
- **`Collector` characteristics**: `CONCURRENT` (можно использовать в parallel без синхронизации accumulator), `UNORDERED` (результат не зависит от порядка), `IDENTITY_FINISH` (finisher — identity).
- **`Collectors.toUnmodifiableList/Set/Map`** (Java 10): возвращают immutable коллекции, не разрешают null.
- **`Stream.toList()`** (Java 16, JEP 41156): unmodifiable `List<T>` без явного `collect`. `stream.toList()` короче и быстрее, чем `collect(Collectors.toList())` (последний возвращает mutable ArrayList).
- **`Collectors.teeing`** (Java 12): запуск двух коллекторов на одном stream-е, merge результатов. `stream.collect(teeing(minBy(cmp), maxBy(cmp), (min, max) -> new Range(min, max)))`.
- **Numeric streams**: `IntStream`, `LongStream`, `DoubleStream`. `mapToInt(ToIntFunction)`, `IntStream.range`, `rangeClosed`, `sum()`, `average()`, `max()`, `min()`, `summaryStatistics()` (в одном проходе). Без boxing для примитивов.
- **`IntStream.range(0, n).parallel()` — parallel iteration. ** Caveat: `IntStream.range(0, n).forEach(i -> work(i))` — порядок не гарантирован. Для ordered — `forEachOrdered`.
- **Side-effects и stateful lambdas**: lambdas в stream-операциях должны быть stateless и side-effect-free. Мутирование shared state (например, `forEach(synchronizedList::add)`) — non-thread-safe в parallel, медленно в sequential. Использовать `collect`.
- **`peek`** — intermediate, для отладки. Не использовать для мутации (side-effect). Bad practice: `stream.peek(x -> list.add(x)).count()`.
- **`Stream.iterate` Java 9**: `Stream.iterate(seed, hasNext, next)` — генерация с условием выхода. `Stream.iterate(1, i -> i <= 100, i -> i + 1)`.
- **`takeWhile`/`dropWhile`** (Java 9): `takeWhile` берёт элементы, пока predicate true, останавливается на первом false. `dropWhile` — наоборот, пропускает пока true, потом берёт всё остальное. На unordered stream-е — берёт подмножество, не обязательно начало.
- **`Collectors.flatMapping`/`filtering`** (Java 9): для multilevel collectors. `groupingBy(byDept, flatMapping(e -> e.getSkills().stream(), toSet()))` — собрать все уникальные навыки по департаменту.
- **`Collectors.toMap` performance**: для immutable map — `Collectors.toUnmodifiableMap`. Для `Map.of`-style — `Stream.collect` затем обернуть. Для mutable — `toMap` с `HashMap::new` supplier.
- **`IntStream.summaryStatistics()`**: `IntSummaryStatistics` в одном проходе даёт count, sum, min, max, average. Удобно и быстро.

### Материалы

- Книга: "Effective Java" Joshua Bloch (3rd ed., Addison-Wesley, 2018) — Item 45 (use streams judiciously — не всегда), Item 46 (prefer side-effect-free functions in streams), Item 47 (prefer Collection over Stream as return type), Item 48 (use parallel streams cautiously), Item 49 (check parameters validity)
- Книга: "Modern Java in Action" Urma, Fusco, Mycroft (Manning, 2018) — Chapter 4 (Working with Streams), Chapter 5 (Putting Streams into Practice), Chapter 6 (Collecting Data with Streams), Chapter 7 (Parallel Data Processing)
- Книга: "Core Java Volume II—Advanced Features" Cay S. Horstmann (12th ed., 2021) — Chapter 1.2: Working with Streams
- Документация Oracle (Streams tutorial): https://docs.oracle.com/javase/tutorial/collections/streams/index.html
- Документация Oracle (Aggregate Operations): https://docs.oracle.com/javase/tutorial/collections/streams/
- Документация Oracle (Stream Javadoc): https://docs.oracle.com/en/java/javase/21/docs/api/java.base/java/util/stream/Stream.html
- Документация Oracle (Collectors Javadoc): https://docs.oracle.com/en/java/javase/21/docs/api/java.base/java/util/stream/Collectors.html
- JEP 107: Bulk Data Operations for Collections (Java 8): https://openjdk.org/jeps/107
- Baeldung — Guide to Java Collectors: https://www.baeldung.com/java-collectors
- Baeldung — Collecting into Map using Collectors.toMap() vs groupingBy: https://www.baeldung.com/java-map-collectors-tomap-vs-groupingby
- Baeldung — Count Occurrences Using Java groupingBy: https://www.baeldung.com/java-groupingby-count
- Baeldung — Java 8 Stream API: https://www.baeldung.com/java-8-streams
- Baeldung — Java Stream reduce: https://www.baeldung.com/java-stream-reduce

### Практика / Кейсы

**Кейс 12.1. Multilevel groupingBy + downstream collectors.**
- Постановка: для списка сотрудников получить `Map<Department, Map<Grade, List<String>>>` (имена по департаменту и грейду), плюс среднюю зарплату по департаменту.
- Шаги: (1) `Map<Dept, Map<Grade, List<String>>>` через `stream.collect(groupingBy(Emp::getDept, groupingBy(Emp::getGrade, mapping(Emp::getName, toList()))))`. (2) `Map<Dept, Double>` средняя зарплата через `groupingBy(Emp::getDept, averagingDouble(Emp::getSalary))`. (3) В одном проходе: `teeing(groupingBy(...), averagingDouble(...), Pair::new)`. (4) Для `Map<Dept, Map<Grade, Double>>` средняя — triple nesting через `groupingBy(byDept, groupingBy(byGrade, averagingDouble(Emp::getSalary)))`. (5) Тесты на пустых и edge cases. (6) Performance: один проход, O(n).
- Что проверить: владение collectors composition.

**Кейс 12.2. Параллельный stream — когда помогает и когда вредит.**
- Постановка: сравнить throughput sequential vs parallel для разных задач.
- Шаги: (1) `IntStream.range(0, 10_000_000).sum()` — parallel быстрее (CPU-bound, big data). (2) `Stream.of(1,2,3).map(x -> heavy(x)).collect(...)` — parallel может быть быстрее или медленнее в зависимости от overhead. (3) `stream.iterate(0, i -> i + 1).limit(1000).parallel()` — iterate плохо параллелится (chain dependency). (4) `Files.lines` с `parallel()` — I/O bound, parallel может ухудшить (файловый I/O bottleneck). (5) `forEach(synchronizedList::add)` — race condition, использовать `collect(toList())`. (6) JMH измерить каждый кейс.
- Что проверить: понимание parallel stream pitfalls, ability to benchmark.

**Кейс 12.3. `reduce` vs `collect` для concat.**
- Постановка: построить string из списка через три способа, сравнить производительность.
- Шаги: (1) `stream.reduce("", (a, b) -> a + b)` — O(n²) (string concat копирует каждый раз). (2) `stream.reduce(String::concat)` — тоже O(n²). (3) `stream.collect(StringBuilder::new, StringBuilder::append, StringBuilder::append).toString()` — O(n). (4) `stream.collect(Collectors.joining())` — внутренне StringBuilder, O(n). (5) JMH измерить: разница в 100–1000x для больших списков. (6) Объяснить, почему reduce не подходит для mutable accumulation (нельзя использовать один StringBuilder).
- Что проверить: понимание reduce semantics (immutable accumulator) vs collect (mutable).

**Кейс 12.4. flatMap для трансформации иерархии.**
- Постановка: для `Map<Department, List<Employee>>` получить уникальный список всех навыков всех сотрудников.
- Шаги: (1) `deptMap.values().stream()` — `Stream<List<Employee>>`. (2) `flatMap(List::stream)` — `Stream<Employee>`. (3) `flatMap(e -> e.getSkills().stream())` — `Stream<String>`. (4) `distinct()` — уникальные. (5) `collect(toList())`. (6) Альтернатива: `flatMap` + `Collectors.toSet()`. (7) Для multilevel grouping: `groupingBy(Emp::getDept, flatMapping(e -> e.getSkills().stream(), toSet()))` — Java 9 flatMapping collector.
- Что проверить: понимание flatMap.

**Кейс 12.5. Custom Collector дляmoving average.**
- Постановка: написать custom `Collector` для скользящего среднего с окном N.
- Шаги: (1) `class MovingAverageCollector implements Collector<Double, MovingAverage, Double>`. (2) `Supplier<MovingAverage>` — `MovingAverage::new`. (3) `BiConsumer<MovingAverage, Double>` — `ma.add(value)`. (4) `BinaryOperator<MovingAverage>` — `ma1.combine(ma2)`. (5) `Function<MovingAverage, Double>` — `MovingAverage::getAverage`. (6) `Characteristics` — обычно `IDENTITY_FINISH`. (7) Применить к Stream: `stream.collect(new MovingAverageCollector(5))`. (8) Для parallel — combiner должен корректно работать с partial windows.
- Что проверить: владение `Collector` API.

**Кейс 12.6. `Stream.toList()` vs `collect(toList())` — memory и immutability.**
- Постановка: мигрировать код с `collect(toList())` на `toList()`, не сломав клиентов, которые мутируют результат.
- Шаги: (1) `collect(toList())` возвращает `ArrayList` (mutable). (2) `toList()` (Java 16) возвращает immutable `List`. (3) Если клиент делает `result.add(...)` — `UnsupportedOperationException`. (4) Использовать `collect(toCollection(ArrayList::new))` для явного mutable. (5) Для immutable — `toList()` компактнее и быстрее. (6) Тесты: проверить все места, где результат мутируется. (7) JMH: `toList()` обычно на 5–15% быстрее.
- Что проверить: понимание immutability semantics, migration.

**Кейс 12.7. Short-circuit и lazy в pipeline.**
- Постановка: доказать, что `findFirst` после `filter` не вычисляет весь stream.
- Шаги: (1) `Stream<Integer> s = IntStream.range(0, 1_000_000).boxed().peek(i -> System.out.println("peek " + i)).filter(i -> i > 100).findFirst();` (2) Вывод: peek напечатает 0, 1, ..., 101 (до первого найденного). (3) Без `peek` — silent. (4) В parallel stream — `findFirst` обрабатывает несколько первых чанков, берёт первый из меньшего индекса. (5) `findAny` — может взять любой, быстрее в parallel. (6) Объяснить, почему `peek` не должен иметь side-effects (only for debugging).
- Что проверить: глубокое понимание lazy evaluation.

---

## 13. Optional

### Теория

- **`Optional<T>` (Java 8, JEP 110)** — wrapper, который либо содержит значение T, либо пуст. Заменяет `null` для возвращаемых значений, явно сигнализируя об отсутствии. НЕ предназначен для полей, параметров, коллекций.
- **Создание**: `Optional.of(T)` — non-null (NPE если null), `Optional.ofNullable(T)` — null-safe (возвращает empty если null), `Optional.empty()` — явный empty.
- **Методы**: `isPresent()`, `isEmpty()` (Java 11), `get()` (бросает `NoSuchElementException` если empty — не использовать без проверки), `orElse(T)` (default значение), `orElseGet(Supplier<T>)` (lazy default), `orElseThrow()`, `orElseThrow(Supplier<X>)`, `ifPresent(Consumer<T>)`, `ifPresentOrElse(Consumer<T>, Runnable)` (Java 9), `map(Function<T, R>)`, `flatMap(Function<T, Optional<R>>)` (для вложенных Optional), `filter(Predicate<T>)`, `stream()` (Java 9 — для flatMap-а из stream-а Optional-ов).
- **`map` vs `flatMap`**: `map` применяет функцию, результат оборачивается в Optional. `flatMap` — функция возвращает `Optional<R>` напрямую (без двойной вложенности). Аналогично Stream API.
- **`orElse(T)` vs `orElseGet(Supplier<T>)`**: `orElse(default)` — вычисляет default всегда, даже если Optional non-empty. `orElseGet(() -> computeDefault())` — лениво. Для тяжёлых default — `orElseGet`.
- **Правильное использование**: return-тип метода, который может вернуть "ничего". Заменяет `return null;` в API. Effective Java Item 55.
- **Anti-patterns**:
  - **Поля класса Optional** — Optional не `Serializable`, расходует лишний объект. Для поля — просто `null`.
  - **Параметры методов Optional** — раздувает API, никто не мешает передать `null` ссылку типа `Optional`. Лучше overloading: `doIt()` и `doIt(value)`.
  - **`Optional<List<T>>`** — вместо пустого Optional возвращать `List.of()` (пустой список).
  - **`Optional.get()` без проверки** — бросает `NoSuchElementException`. Использовать `orElse`/`orElseThrow`.
  - **`optional.isPresent() ? optional.get() : default`** — лучше `optional.orElse(default)`.
  - **`if (optional.isPresent()) { ... } else { ... }`** — лучше `optional.ifPresentOrElse(a -> ..., () -> ...)`.
  - **Сериализация Optional** — не implements Serializable (до Java 11 с caveats). Не хранить в полях.
- **`OptionalInt`, `OptionalLong`, `OptionalDouble`** — для примитивов (аналог Optional<Integer>, но без boxing).
- **`Optional.or(Supplier<Optional<T>>)` (Java 9)** — chaining Optionals. `first.or(() -> second).orElse(default)`.
- **`Optional.stream()` (Java 9)** — `Stream<T>` с 0 или 1 элементом. Полезно в `flatMap`: `stream.flatMap(x -> findOptional(x).stream())`.
- **`Optional.ifPresentOrElse` (Java 9)** — explicit handling для обоих случаев.
- **В Stream API**: `Optional<T> findFirst()`, `findAny()`, `min(cmp)`, `max(cmp)`, `reduce(BinaryOperator)`. Возвращают Optional для пустого stream-а.
- **Performance**: Optional — объект (или два, если boxed), доступ через методы. В hot path — overhead. Не использовать в примитивных коллекциях или высоконагруженных вычислениях. Для API — ok.
- **Nullability annotations vs Optional**: `@Nullable`/`@NonNull` (JSR-305, JetBrains, Checker Framework) — annotations, проверяются статически. Optional — runtime representation. Часто комбинируют: Optional для return, @Nullable для параметров/полей.
- **`Optional` в REST-контроллерах**: возвращать `Optional<T>` из controller-метода — Jackson модуль `jackson-datatype-jdk8` сериализует в null для empty. Spring `ResponseEntity<T>` — лучше для явных 404.
- **`Optional` и exceptions**: `orElseThrow` — throws если empty. `Optional.ofNullable(value).orElseThrow(() -> new BusinessException("not found"))` — идиоматично.
- **Chain of Optionals** — `first.flatMap(a -> second(a).flatMap(b -> third(b)))` — монадический flow. В Java многословнее, чем в Scala/Haskell, но рабочий.
- **Don't use Optional for side-effects**: `optional.ifPresent(x -> sideEffect(x))` — ok для logging, но для business-логики лучше явный `if (optional.isPresent())` или `optional.orElseThrow()`.

### Материалы

- Книга: "Effective Java" Joshua Bloch (3rd ed., Addison-Wesley, 2018) — Item 55 (Return optionals), Item 78 (synchronize access to shared mutable data — Optional immutable)
- Книга: "Modern Java in Action" Urma, Fusco, Mycroft (Manning, 2018) — Chapter 11: Optional (раздел "Optional in depth")
- Книга: "Core Java Volume I—Fundamentals" Cay S. Horstmann (12th ed., 2021) — Chapter 5.5: Working with Optional
- Документация Oracle (Optional class Javadoc): https://docs.oracle.com/en/java/javase/21/docs/api/java.base/java/util/Optional.html
- Документация Oracle (OptionalInt Javadoc): https://docs.oracle.com/en/java/javase/21/docs/api/java.base/java/util/OptionalInt.html
- Документация Oracle (Stream.reduce Javadoc — Optional возвращается): https://docs.oracle.com/en/java/javase/21/docs/api/java.base/java/util/stream/Stream.html
- JEP 110: Lambda Expressions (relates to Optional introduction): https://openjdk.org/jeps/126
- Baeldung — Guide To Java Optional: https://www.baeldung.com/java-optional
- Baeldung — Uses for Optional in Java: https://www.baeldung.com/java-optional-uses
- DZone — Optional Anti-Patterns: https://dzone.com/articles/optional-anti-patterns
- Pasdam Blog — Java Optional anti-patterns: https://pasdam.github.io/blog/posts/java_optional_antipattern
- Oracle Technical Resources — Making the Most of Java's Metadata: https://www.oracle.com/technical-resources/articles/hunter-meta1.html

### Практика / Кейсы

**Кейс 13.1. Рефакторинг `return null` → Optional.**
- Постановка: legacy-метод `User findById(long id)` возвращает null. Мигрировать на `Optional<User>`.
- Шаги: (1) Старый: `User findById(long id) { User u = ...; return u; }` (может быть null). (2) Новый: `Optional<User> findById(long id) { return Optional.ofNullable(db.find(id)); }`. (3) Клиенты: `User u = findById(1); if (u != null) { ... }` → `findById(1).ifPresent(u -> ...)` или `findById(1).map(User::getName).orElse("unknown")`. (4) Backward compat: deprecated wrapper `User findByIdOrNull(long id) { return findById(id).orElse(null); }`. (5) Тесты: существующие продолжают работать через wrapper. (6) Spring Data JPA: `Optional<T> findById(ID)` — уже возвращает Optional.
- Что проверить: migration strategy, backward compat.

**Кейс 13.2. Chain of Optionals — монадический flow.**
- Постановка: получить город пользователя по цепочке `User → Address → City → Name`.
- Шаги: (1) Bad: `if (user != null && user.getAddress() != null && user.getAddress().getCity() != null) { String city = user.getAddress().getCity().getName(); }`. (2) Хорошо: `Optional.ofNullable(user).flatMap(User::getAddress).flatMap(Address::getCity).map(City::getName).orElse("unknown");` — при условии, что `getAddress`/`getCity` возвращают Optional. (3) Если методы возвращают plain null — `Optional.ofNullable(user).map(User::getAddress).map(Address::getCity).map(City::getName)` — `map` null-safe (оборачивает). (4) Performance: vs явных null-чеков — overhead небольшой. (5) Readability значительно лучше.
- Что проверить: понимание flatMap vs map, monadic flow.

**Кейс 13.3. `orElse` vs `orElseGet` — производительность.**
- Постановка: доказать, что `orElse(computeHeavy())` вызывает `computeHeavy` всегда, `orElseGet(() -> computeHeavy())` — только если empty.
- Шаги: (1) `Optional<String> opt = Optional.of("present"); String s = opt.orElse(computeHeavy());` — `computeHeavy()` вызывается, результат отбрасывается. (2) `opt.orElseGet(() -> computeHeavy())` — не вызывается. (3) JMH измерить разницу для heavy computation (DB query, network). (4) Для лёгкого default (constant, cached) — `orElse` достаточно. (5) Для heavy — всегда `orElseGet`. (6) Доказать через логирование вызовов.
- Что проверить: глубокое понимание eager vs lazy.

**Кейс 13.4. Anti-pattern: Optional в поле и сериализация.**
- Постановка: показать, что поле Optional ломает сериализацию и recommendation.
- Шаги: (1) `class User implements Serializable { private Optional<String> name; }` — `Optional` не implements Serializable (по крайней мере, не гарантировано). (2) Попытка сериализовать — может упасть. (3) Jackson модуль `jackson-datatype-jdk8` — работает, но это фреймворк-specific. (4) Recommendation Effective Java Item 55: "Optional is intended solely as a return type". (5) Refactor: поле `private String name;` (может быть null), getter возвращает `Optional.ofNullable(name)`. (6) Для коллекций — возвращать пустую коллекцию вместо `Optional<Collection>`.
- Что проверить: понимание design intent Optional.

**Кейс 13.5. `Optional.stream()` и flatMap.**
- Постановка: из `Stream<User>` получить `Stream<String>` имён, опуская пользователей без имени.
- Шаги: (1) `Stream<Optional<String>>` если `User::getNameOptional` возвращает Optional. (2) `stream.flatMap(u -> u.getNameOptional().stream())` — `Optional.stream()` даёт `Stream<String>` с 0 или 1 элементом, flatMap разворачивает. (3) Эквивалент: `stream.map(User::getNameOptional).filter(Optional::isPresent).map(Optional::get)` — многословнее. (4) Performance: `flatMap` обычно быстрее. (5) Для примитивов — `OptionalInt.stream()`.
- Что проверить: владение Optional + Stream integration.

**Кейс 13.6. Optional в REST API.**
- Постановка: REST-контроллер должен возвращать 404 если ресурс не найден.
- Шаги: (1) `@GetMapping("/users/{id}") public ResponseEntity<User> get(@PathVariable long id) { Optional<User> u = service.findById(id); return u.map(ResponseEntity::ok).orElseGet(() -> ResponseEntity.notFound().build()); }`. (2) Альтернатива: `u.orElseThrow(() -> new ResponseStatusException(NOT_FOUND))`. (3) Jackson serializes empty Optional → null (with `jackson-datatype-jdk8`). (4) Spring `ResponseEntity<User>` явно документирует 200/404 в OpenAPI. (5) Не возвращать `Optional` напрямую — `ResponseEntity` даёт контроль над HTTP status.
- Что проверить: integration Spring + Optional, REST design.

---

## 14. Reflection API

### Теория

- **Reflection** — механизм Java для интроспекции и модификации структуры классов, методов, полей, конструкторов во время выполнения. Классы в `java.lang.reflect`: `Class`, `Method`, `Field`, `Constructor`, `Modifier`, `Array`, `Proxy`, `Parameter`. Запрещён/ограничен в module-path без `--add-opens`.
- **`Class<T>`** — метаданные о классе. Получение: `object.getClass()`, `String.class`, `Class.forName("java.lang.String")`. Методы: `getName()`, `getSimpleName()`, `getModifiers()`, `getSuperclass()`, `getInterfaces()`, `getFields()`, `getMethods()`, `getConstructors()`, `getDeclaredFields()` (включая private), `getDeclaredMethods()`, `getField(name)`, `getMethod(name, Class...)`, `getDeclaredConstructor(Class...)`, `newInstance()` (deprecated с Java 9 — использовать `getDeclaredConstructor().newInstance()`).
- **`Method`** — метод класса. `invoke(Object target, Object... args)` — вызов. `getName()`, `getReturnType()`, `getParameterTypes()`, `getModifiers()`, `getAnnotation(Class)`, `setAccessible(boolean)` (для private методов), `getDefaultValue()` (для аннотаций).
- **`Field`** — поле класса. `get(Object target)`, `set(Object target, Object value)`, `getInt`, `setInt`, etc. (для примитивов). `getName()`, `getType()`, `getModifiers()`, `setAccessible(boolean)`. Для static-полей — `get(null)`, `set(null, value)`.
- **`Constructor<T>`** — конструктор. `newInstance(Object... args)` — создание объекта. `getParameterTypes()`, `getModifiers()`, `setAccessible(boolean)`.
- **`Modifier`** — утилитный класс для декодирования модификаторов: `Modifier.isPublic(mod)`, `isStatic`, `isFinal`, `isPrivate`, `isProtected`, `isSynchronized`, `isVolatile`, `isTransient`, `isNative`, `isAbstract`, `isInterface`, `isStrict`. `mod` — int из `getModifiers()`.
- **`getMethods()` vs `getDeclaredMethods()`**: `getMethods` возвращает все public методы (включая наследованные), `getDeclaredMethods` — все методы, объявленные в этом классе (включая private, но без наследованных).
- **`setAccessible(true)`** — снимает check access, позволяет работать с private. До Java 8 — без ограничений. С Java 9 (modules) — нужно `--add-opens` для открытия пакета. В Java 16+ strict по умолчанию, особенно для JDK internal (`--add-opens java.base/java.util=ALL-UNNAMED`).
- **`Array`** — для рефлексивной работы с массивами. `Array.newInstance(Class, int)`, `Array.get(array, i)`, `Array.set(array, i, value)`, `Array.getLength(array)`.
- **`Proxy`** — JDK dynamic proxy. `Proxy.newProxyInstance(classLoader, Class[] interfaces, InvocationHandler)`. Создаёт динамический прокси, реализующий указанные интерфейсы, все вызовы делегируются в `InvocationHandler.invoke(proxy, method, args)`. Основа Spring AOP (вместе с CGLIB), Hibernate lazy loading, mocking-фреймворков.
- **`InvocationHandler`** — функциональный интерфейс: `Object invoke(Object proxy, Method method, Object[] args) throws Throwable`. Любая логика — логирование, кэширование, security.
- **Performance**: reflection в 10–100x медленнее прямого вызова (без JIT). JIT может инлайнить через `MethodHandle` (Java 7+) — `MethodHandles.lookup()`, `MethodHandle.invokeExact` — почти как прямой вызов. Для hot path — `MethodHandle` или кодогенерация.
- **`MethodHandle` (Java 7, JEP 292)** — низкоуровневая альтернатива `Method`. `MethodHandles.Lookup l = MethodHandles.lookup(); MethodHandle mh = l.findVirtual(String.class, "length", MethodType.methodType(int.class)); int len = (int) mh.invoke("abc");`. `invokeExact` — строго типизированный, быстрее. `invoke` — с автоматическими casts.
- **`VarHandle` (Java 9)** — для атомарного доступа к полям, альтернатива `sun.misc.Unsafe`. `MemoryOrdering` modes (PLAIN, OPAQUE, RELEASE, ACQUIRE, VOLATILE). Основа `java.util.concurrent`.
- **Annotation processing через reflection**: `method.getAnnotation(MyAnno.class)`, `getAnnotationsByType(MyAnno.class)` (для `@Repeatable`), `getDeclaredAnnotations()`. Аннотация должна быть `@Retention(RUNTIME)`.
- **`Parameter` (Java 8)** — информация о параметрах метода. `getParameters()`. Имена параметров доступны только если скомпилировано с `-parameters` (по умолчанию off). Без — `arg0`, `arg1`, ...
- **`Type` иерархия**: `Type` (interface) → `Class`, `ParameterizedType` (`List<String>` — type args доступны), `GenericArrayType`, `TypeVariable<D>`, `WildcardType`. `field.getGenericType()` возвращает `Type` (включая generics info), `field.getType()` — `Class` (raw, erasure).
- **`TypeReference` workaround**: anonymous class `new TypeReference<List<Foo>>(){}` фиксирует generic-тип, доступен через `getGenericSuperclass()`. Используется в Jackson.
- **Records и reflection**: record components доступны через `Class.getRecordComponents()`. Возвращает `RecordComponent[]` с методом `getAccessor()`.
- **Reflection и security**: dangerous — может обходить private, final, security manager (deprecated с Java 17, removed in 24). Запуск с `-Djava.security.manager=disallow` (Java 18+) для production.
- **`Unsafe` (`sun.misc.Unsafe`)** — internal API, deprecated для removal. Alternatives: `VarHandle`, `MemorySegment` (Project Panama, JEP 454 в Java 22), `java.lang.foreign`. Senior должен знать, но не использовать в новом коде.

### Материалы

- Книга: "Effective Java" Joshua Bloch (3rd ed., Addison-Wesley, 2018) — Item 65 (prefer interfaces to reflection), Item 53 (refactor, don't reflect for fun)
- Книга: "Core Java Volume II—Advanced Features" Cay S. Horstmann (12th ed., 2021) — Chapter 5: Reflection (включая Proxy, MethodHandle)
- Книга: "Java Reflection in Action" Ira R. Forman & Nate Forman (Manning, 2004) — классика по теме
- Документация Oracle (Reflection API tutorial): https://docs.oracle.com/javase/tutorial/reflect/index.html
- Документация Oracle (Class Javadoc): https://docs.oracle.com/en/java/javase/21/docs/api/java.base/java/lang/Class.html
- Документация Oracle (Method Javadoc): https://docs.oracle.com/en/java/javase/21/docs/api/java.base/java/lang/reflect/Method.html
- Документация Oracle (Field Javadoc): https://docs.oracle.com/en/java/javase/21/docs/api/java.base/java/lang/reflect/Field.html
- Документация Oracle (Proxy Javadoc): https://docs.oracle.com/en/java/javase/21/docs/api/java.base/java/lang/reflect/Proxy.html
- Документация Oracle (MethodHandle Javadoc): https://docs.oracle.com/en/java/javase/21/docs/api/java.base/java/lang/invoke/MethodHandle.html
- Jenkov — Java Reflection Tutorial (overview): https://jenkov.com/tutorials/java-reflection/index.html
- Jenkov — Java Reflection Classes: https://jenkov.com/tutorials/java-reflection/classes.html
- Jenkov — Java Reflection Fields: https://jenkov.com/tutorials/java-reflection/fields.html
- Jenkov — Java Reflection Methods: https://jenkov.com/tutorials/java-reflection/methods.html
- Jenkov — Java Reflection Private Fields and Methods: https://jenkov.com/tutorials/java-reflection/private-fields-and-methods.html
- JEP 261: Module System (relates to `--add-opens` and reflection restrictions): https://openjdk.org/jeps/261

### Практика / Кейсы

**Кейс 14.1. Простой ORM через reflection.**
- Постановка: реализовать маппер `ResultSet → Object` через reflection: читать `@Column("name")` аннотации на полях, заполнять.
- Шаги: (1) `@Retention(RUNTIME) @Target(FIELD) @interface Column { String value(); }`. (2) `class User { @Column("id") long id; @Column("username") String name; }`. (3) `static <T> T map(ResultSet rs, Class<T> type) { T obj = type.getDeclaredConstructor().newInstance(); for (Field f : type.getDeclaredFields()) { Column c = f.getAnnotation(Column.class); if (c != null) { f.setAccessible(true); f.set(obj, rs.getObject(c.value())); } } return obj; }`. (4) Cache `Field[]` и annotation lookups (reflection медленный). (5) Для примитивов — `getInt`, `getLong` без autoboxing. (6) Сравнить с генерацией кода через annotation processor (MyBatis, MapStruct) — производительность в 5–10x лучше.
- Что проверить: владение reflection + annotation, awareness performance pitfalls.

**Кейс 14.2. JDK dynamic proxy — logging layer.**
- Постановка: обернуть service-интерфейс в логирующий прокси без изменения реализации.
- Шаги: (1) `interface UserService { User findById(long id); }`. (2) Реализация `UserServiceImpl implements UserService`. (3) `InvocationHandler logger = (proxy, method, args) -> { log.info("calling " + method.getName() + " with " + Arrays.toString(args)); Object result = method.invoke(target, args); log.info("returned " + result); return result; };`. (4) `UserService proxy = (UserService) Proxy.newProxyInstance(loader, new Class[]{UserService.class}, logger);`. (5) Все вызовы через proxy логируются. (6) Spring AOP использует этот механизм + CGLIB для классов без интерфейсов. (7) Limitation: только интерфейсы, нельзя проксировать final-классы.
- Что проверить: понимание AOP internals, ability to implement cross-cutting concerns.

**Кейс 14.3. `setAccessible` и module system.**
- Постановка: продемонстрировать `InaccessibleObjectException` в Java 16+ и обойти через `--add-opens`.
- Шаги: (1) Попытка `String.class.getDeclaredField("value").setAccessible(true)` в Java 16+ без флагов → `InaccessibleObjectException`. (2) Запуск с `--add-opens java.base/java.lang=ALL-UNNAMED` — работает. (3) Для library: declare `Add-Opens: java.base/java.lang` в MANIFEST.MF. (4) Для JDK internals: нужно `--add-opens java.base/java.lang=ALL-UNNAMED`. (5) Начиная с Java 16 — strict по умолчанию (`--illegal-access=deny`). (6) В Java 17+ `--illegal-access` удалён, всегда deny.
- Что проверить: понимание module system, ability to migrate legacy code.

**Кейс 14.4. MethodHandle vs reflection — performance.**
- Постановка: сравнить throughput прямого вызова, Method, MethodHandle, MethodHandle.invokeExact.
- Шаги: (1) Прямой `obj.method()` — baseline. (2) `Method m = ...; m.invoke(obj)` — reflection. (3) `MethodHandle mh = ...; mh.invoke(obj)` — 1.5–3x быстрее Method. (4) `mh.invokeExact(obj)` с точным типом — почти как прямой вызов. (5) JMH измерить — обычно прямой > invokeExact > invoke > Method.invoke. (6) Для hot path — `MethodHandle` + caching. (7) Для runtime-generated code — bytecode generation (ASM, ByteBuddy).
- Что проверить: понимание performance, ability to choose.

**Кейс 14.5. Super type tokens через reflection.**
- Постановка: реализовать `TypeReference<T>` для сохранения generic-информации.
- Шаги: (1) `abstract class TypeReference<T> { private final Type type; protected TypeReference() { Type superClass = getClass().getGenericSuperclass(); if (!(superClass instanceof ParameterizedType)) throw new IllegalArgumentException(); this.type = ((ParameterizedType) superClass).getActualTypeArguments()[0]; } public Type getType() { return type; } }`. (2) Использование: `TypeReference<List<String>> tr = new TypeReference<>() {};` — анонимный класс фиксирует `List<String>`. (3) `tr.getType()` возвращает `ParameterizedType` с `List<String>`. (4) Использовать для JSON-десериализации (Jackson `TypeReference`). (5) Limitation: только конкретные generic-типы, нельзя `T` (стирается). (6) Сравнить с C# reified generics — там workaround не нужен.
- Что проверить: глубокое понимание generic erasure, reflection generics.

**Кейс 14.6. Record components и reflection.**
- Постановка: обойти record-компоненты через reflection и проверить контракт canonical constructor.
- Шаги: (1) `record Point(int x, int y) {}`. (2) `RecordComponent[] comps = Point.class.getRecordComponents();` — `[x, y]`. (3) `comps[0].getName()` → "x", `comps[0].getType()` → `int.class`, `comps[0].getAccessor()` → method "x". (4) Создать через canonical: `Point.class.getCanonicalConstructor(int.class, int.class).newInstance(1, 2)`. (5) Проверить инварианты через compact constructor — если бросает, instance не создан. (6) Records не позволяют `setAccessible(true)` на final-полях (они всегда final).
- Что проверить: understanding record reflection, modern API.

---

## 15. Annotations

### Теория

- **Annotations (Java 5, JEP 117 historically, JSR 175)** — метаданные, привязанные к элементам кода (класс, метод, поле, параметр, пакет, type-parameter, type-use). Не влияют на исполнение напрямую, но обрабатываются компилятором, tools, runtime.
- **Синтаксис**: `@AnnotationName` или `@AnnotationName(key=value, ...)`. Типы параметров: примитивы, String, Class, enum, annotation, массивы вышеуказанных. Default значения через `default` в объявлении.
- **Объявление annotation type**: `@interface MyAnno { String value() default ""; int count() default 0; Class<?> type() default Void.class; }`. Неявно extends `java.lang.annotation.Annotation`.
- **Мета-аннотации**:
  - `@Retention(RetentionPolicy)` — как долго хранится: `SOURCE` (только в исходниках, например `@Override`), `CLASS` (в .class, но не в runtime, по умолчанию), `RUNTIME` (доступна через reflection).
  - `@Target(ElementType[])` — где можно применить: `TYPE`, `FIELD`, `METHOD`, `PARAMETER`, `CONSTRUCTOR`, `LOCAL_VARIABLE`, `ANNOTATION_TYPE`, `PACKAGE`, `TYPE_PARAMETER` (Java 8), `TYPE_USE` (Java 8), `MODULE` (Java 9), `RECORD_COMPONENT` (Java 16).
  - `@Documented` — включается в Javadoc.
  - `@Inherited` — наследуется подклассами (только для class-level annotations, не работает на интерфейсах).
  - `@Repeatable(Class<? extends Annotation>)` (Java 8) — может применяться несколько раз к одному элементу.
- **Встроенные annotations**:
  - `@Override` (SOURCE) — метод переопределяет суперкласс. Compile-time проверка.
  - `@Deprecated` (RUNTIME) — помечен как deprecated. `@Deprecated(since="9", forRemoval=true)` (Java 9+).
  - `@SuppressWarnings` (SOURCE) — подавить warnings. Аргументы: `"unchecked"`, `"rawtypes"`, `"unused"`, `"deprecation"`, `"varargs"`, и т.д.
  - `@SafeVarargs` (RUNTIME, Java 7) — подавить unchecked warning для generic varargs. Только static/final/private (с Java 9).
  - `@FunctionalInterface` (RUNTIME, Java 8) — функциональный интерфейс, компилятор проверяет SAM.
  - `@Native` (RUNTIME, Java 8) — константа загружается из native code.
  - `@PostConstruct`/`@PreDestroy` (раньше `javax.annotation`, с Java 11 — модуль `jakarta.annotation` или `javax.annotation.processing`).
- **Meta-annotations в библиотеках**: `@Component`, `@Service`, `@Repository` (Spring); `@Entity`, `@Table`, `@Column` (JPA); `@Inject` (JSR-330); `@Produces`/`@Consumes` (JAX-RS); `@Test`, `@BeforeEach`, `@ParameterizedTest` (JUnit 5).
- **Repeatable annotations (Java 8, JEP 120)**: `@Repeatable(Schedules.class)` на `@Schedule`. `@interface Schedules { Schedule[] value(); }`. На элементе: `@Schedule(hour=10) @Schedule(hour=14)`. Через reflection: `getAnnotation(Schedule.class)` → первый, `getAnnotationsByType(Schedule.class)` → массив всех.
- **Type annotations (Java 8, JEP 104)**: `@Target(TYPE_USE)`. Применяется к типам: `List<@NonNull String>`, `@NonNull String[]`, `String @NonNull []`. Полезны для static analysis (Checker Framework).
- **Type parameter annotations (Java 8)**: `<@NonNull T>` на type parameter. Используется в generic-классах.
- **Annotation processing** — `javax.annotation.processing.Processor` (compile-time).Processor обрабатывает annotations в исходниках, может генерировать новый код. Используется в Lombok, MapStruct, Dagger, Hibernate Metamodel. Запускается через `javac -processor MyProcessor`.
- **Runtime annotation processing через reflection**: `method.getAnnotation(MyAnno.class)`, `clazz.isAnnotationPresent(MyAnno.class)`, `field.getAnnotations()`. Только `@Retention(RUNTIME)`.
- **Annotations и inheritance**: `@Inherited` работает только для class-level annotations на superclass. Интерфейс-аннотации НЕ наследуются реализующими классами (часто source of confusion).
- **Default values и null**: нельзя использовать `null` как default. Дефолт должен быть конкретного типа (можно `Void.class` для Class<?>). При чтении через reflection default возвращается, если не задан (но это сложно определить — `AnnotationType.getDefaultValue(key)`).
- **Annotation и generics**: нельзя `@Anno List<T> method()` напрямую (только type-use annotation). Annotation-параметры — fixed types.
- **`@Target({})`** — annotation без target, чисто marker. Редко используется.
- **Combining annotations через meta-annotations**: Spring `@RestController` = `@Controller + @ResponseBody` (через meta-annotations на самом `@RestController`). Spring `@TransactionalService` — composed annotation. Способ композиции — через `@AliasFor` (Spring-specific).
- **Custom annotation — checklist**: (1) Определить retention (SOURCE для compile-time checks, RUNTIME для reflection-based processing). (2) Target — где применима. (3) Default values для параметров. (4) Document behavior. (5) Validate inputs (если annotation processor). (6) Не злоупотреблять — annotation как конфигурация, не как код.

### Материалы

- Книга: "Effective Java" Joshua Bloch (3rd ed., Addison-Wesley, 2018) — Item 39 (prefer annotations to naming patterns), Item 40 (consistently use `@Override`), Item 41 (use marker interfaces to define types)
- Книга: "Core Java Volume II—Advanced Features" Cay S. Horstmann (12th ed., 2021) — Chapter 8.3: Annotations (раздел объявлений и processing)
- Книга: "Core Java Volume I—Fundamentals" Cay S. Horstmann (12th ed., 2021) — Chapter 5.9: Annotations (basic usage)
- Документация Oracle (Annotations tutorial): https://docs.oracle.com/javase/tutorial/java/annotations/index.html
- Документация Oracle (Declaring Annotation Types): https://docs.oracle.com/javase/tutorial/java/annotations/declaring.html
- Документация Oracle (Predefined Annotation Types): https://docs.oracle.com/javase/tutorial/java/annotations/predefined.html
- Документация Oracle (Type Annotations and Pluggable Type Systems): https://docs.oracle.com/javase/tutorial/java/annotations/type_annotations.html
- Документация Oracle (Repeating Annotations): https://docs.oracle.com/javase/tutorial/java/annotations/repeating.html
- Документация Oracle (Annotation Interface Javadoc): https://docs.oracle.com/en/java/javase/21/docs/api/java.base/java/lang/annotation/Annotation.html
- Документация Oracle (Retention Javadoc): https://docs.oracle.com/en/java/javase/21/docs/api/java.base/java/lang/annotation/Retention.html
- Oracle Technical Resources — Making the Most of Java's Metadata (Part 2: Custom Annotations): https://www.oracle.com/technical-resources/articles/hunter-meta1.html
- Baeldung — Overview of Java Built-in Annotations: https://www.baeldung.com/java-default-annotations
- GeeksforGeeks — Java @Retention Annotations: https://www.geeksforgeeks.org/java/java-retention-annotations
- JEP 104: Annotation Types (Java 5): https://openjdk.org/jeps/104
- JEP 117: Pluggable Annotations (Java 5): https://openjdk.org/jeps/117
- JEP 120: Repeating Annotations (Java 8): https://openjdk.org/jeps/120

### Практика / Кейсы

**Кейс 15.1. Custom `@Retry` annotation + dynamic proxy.**
- Постановка: реализовать retry-on-failure через custom annotation и dynamic proxy.
- Шаги: (1) `@Retention(RUNTIME) @Target(METHOD) @interface Retry { int attempts() default 3; long delayMs() default 100; Class<? extends Throwable>[] retryOn() default {RuntimeException.class}; }`. (2) На методе сервиса: `@Retry(attempts=5, retryOn={IOException.class, TimeoutException.class}) public String call() { ... }`. (3) Dynamic proxy: в `invoke` проверить `method.getAnnotation(Retry.class)`, при exception проверять `retryOn`, retry с delay. (4) Без proxy — annotation processor, генерирующий wrapper-код (как Spring AOP). (5) Тесты: имитировать failures, проверить retry count и delay. (6) Spring `@Retryable` — production-grade аналог (Spring Retry).
- Что проверить: ability to design custom annotations, integration с reflection.

**Кейс 15.2. Repeatable annotation — `@Schedule`.**
- Постановка: метод должен запускаться по нескольким cron-выражениям.
- Шаги: (1) `@Repeatable(Schedules.class) @Retention(RUNTIME) @Target(METHOD) @interface Schedule { String cron(); }`. (2) `@interface Schedules { Schedule[] value(); }`. (3) На методе: `@Schedule(cron="0 0 * * *") @Schedule(cron="0 30 * * *") public void run() { ... }`. (4) Чтение: `method.getAnnotationsByType(Schedule.class)` — массив из двух. (5) Запуск через ScheduledExecutorService по каждому cron. (6) Сравнить с одним `@Schedule(cron={"0 0 * * *", "0 30 * * *"})` — массив строк менее гибкий (нет других параметров per cron).
- Что проверить: владение repeatable annotations.

**Кейс 15.3. Annotation processor для генерации builder-а.**
- Постановка: написать annotation processor, генерирующий Builder для классов с `@AutoBuilder`.
- Шаги: (1) `@Retention(SOURCE) @Target(TYPE) @interface AutoBuilder {}`. (2) `class MyProcessor extends AbstractProcessor { @Override public boolean process(Set<? extends TypeElement> anns, RoundEnvironment env) { for (Element e : env.getElementsAnnotatedWith(AutoBuilder.class)) { generateBuilder((TypeElement) e); } return true; } }`. (3) `generateBuilder` — через `Filer` создать `.java` файл с builder-классом. (4) Регистрация через `META-INF/services/javax.annotation.processing.Processor` или `@AutoService(Processor.class)` (Google AutoService). (5) Запуск через `javac -processor MyProcessor ...` или автоматически через Maven/Gradle dependency. (6) Сравнить с Lombok `@Builder` (использует internal javac API, более мощно но хакерски).
- Что проверить: understanding APT, ability to generate code.

**Кейс 15.4. Type-use annotations — `@NonNull`.**
- Постановка: использовать Checker Framework для проверки nullability через type-use annotations.
- Шаги: (1) `@Target(TYPE_USE) @Retention(CLASS) @interface NonNull {}` (или готовый из Checker Framework). (2) `String process(@NonNull String input) { ... return @NonNull String.format(...); }`. (3) `List<@NonNull String> list;` — элементы не null. (4) Запуск через `javac -processor org.checkerframework.checker.nullness.NullnessChecker ...`. (5) Compile-time warnings для возможных NPE. (6) Сравнить с `Optional<String>` — annotations не имеют runtime cost, но не enforced runtime.
- Что проверить: понимание type-use annotations, modern nullability strategies.

**Кейс 15.5. `@Inherited` — особенности работы с интерфейсами.**
- Постановка: показать, что `@Inherited` не работает для interface-annotations.
- Шаги: (1) `@Inherited @Retention(RUNTIME) @Target(TYPE) @interface MyAnno {}`. (2) `@MyAnno class Parent {}` (3) `class Child extends Parent {}` — `Child.class.isAnnotationPresent(MyAnno.class)` → true (inherited). (4) `@MyAnno interface Iface {}` (5) `class Impl implements Iface {}` — `Impl.class.isAnnotationPresent(MyAnno.class)` → false (interfaces НЕ наследуются). (6) Объяснить по JLS §9.6.4.1: `@Inherited` applies only to class hierarchy. (7) Workaround для interface-annotations — вручную проверять `getInterfaces()` рекурсивно (как делает Spring `AnnotationUtils.findAnnotation`).
- Что проверить: точное понимание inheritance semantics.

**Кейс 15.6. Annotation на generic type parameter.**
- Постановка: параметризованный тип с annotations на type parameter.
- Шаги: (1) `class Box<@NonNull T> { ... }` — type parameter annotated. (2) `class Validator<@Untainted T extends @NonNull Object> { ... }`. (3) Annotation type: `@Target(TYPE_PARAMETER)` для объявления type parameter, `@Target(TYPE_USE)` для использования типа. (4) Чтение через reflection: `Class.getTypeParameters()[0].getAnnotations()`. (5) Compile-time processing — Checker Framework. (6) Use cases: nullability, immutability, taintedness.
- Что проверить: понимание type annotations.

---

## 16. Varargs, static initializer, init order, final/finally/finalize

### Теория

- **Varargs (Java 5)** — переменное число аргументов. Объявление: `void method(String... args)`. Внутри метода `args` — `String[]`. Компилятор разворачивает `method("a", "b", "c")` в `method(new String[]{"a","b","c"})`. Вызов с массивом — `method(myArray)` работает напрямую.
- **Varargs и generics**: heap pollution risk. `static <T> List<T> of(T... items)` — компилятор создаёт `T[]`, но стёртый до `Object[]`. Запись в массив извне (через cast) может привести к ClassCastException при чтении. `@SafeVarargs` (Java 7) подавляет warning, но только static/final/private (с Java 9 — также private на instance methods).
- **Varargs performance**: каждый вызов создаёт массив (аллокация). Для hot path с 0–2 аргументами — overloading `of()`, `of(x)`, `of(x, y)`, `of(x, y, more...)` — оптимизация в `EnumSet.of`, `List.of`. Для большого числа вызовов — profile через JMH.
- **`static` initializer block**: `static { ... }` в классе. Выполняется один раз, при загрузке класса (class initialization). Порядок — сверху вниз по объявлению. Инициализирует static-поля, сложные вычисления (например, загрузка native lib, регистрация в registry).
- **Instance initializer block**: `{ ... }` без `static`. Выполняется при создании каждого объекта, перед конструктором, после `super()`. Редко используется, можно для anonymous class (где нельзя объявить конструктор).
- **Init order (JLS §12.4, §12.5)**:
  - Class init (lazy, on first active use): (1) суперклассы инициализируются сначала. (2) static-поля в порядке объявления. (3) static-блоки в порядке объявления.
  - Instance init (на `new`): (1) `super()` вызывается первым. (2) instance-поля в порядке объявления. (3) instance-блоки в порядке объявления. (4) конструктор.
- **Exception в static initializer** → `ExceptionInInitializerError` (unchecked, wraps checked). Класс переходит в состояние "erroneous initialization", последующие попытки использования → `NoClassDefFoundError`.
- **Lazy class initialization**: класс инициализируется при (1) создании экземпляра (`new`), (2) вызове static-метода, (3) обращении к static-полю (не final-константе), (4)反射е (например, `Class.forName` с `initialize=true` по умолчанию), (5) использовании вложенного класса. Не инициализируется: чтение `static final` compile-time constant (`String s = "abc"`), использование `Class.forName(name, false, loader)`.
- **`final` keyword**:
  - **`final` class**: нельзя наследовать (`String`, `Integer`, `LocalDate`). Дизайн-решение: immutable + security.
  - **`final` method**: нельзя переопределить. static-методы нельзя `final` (но можно hiding). private-методы неявно final.
  - **`final` field**: нельзя переприсвоить после инициализации (в конструкторе или initializer). Для объекта — ссылка final, но сам объект мутабелен (`final List<String> list = new ArrayList<>();` — `list` не переприсвоить, но `list.add(...)` можно).
  - **`final` local variable**: нельзя переприсвоить. Обязательна для lambda-capture и anonymous/local class use.
  - **`final` parameter**: нельзя переприсвоить в методе. Влияет только на переменную параметра, не на объект.
  - **Effectively final**: локальная переменная, которая не переприсваивается, эквивалентна `final` для lambda/inner-class capture.
- **`finally`** — блок, выполняющийся после `try`/`catch` независимо от того, было исключение. Используется для cleanup. С try-with-resources (Java 7+) — менее нужен. Не выполнять `return` или `throw` в finally (перебивает оригинальное исключение).
- **`finalize()` (`Object`)** — метод, вызываемый GC перед освобождением объекта. Deprecated с Java 9 (JEP 283), for removal. Reasons: nondeterministic timing, может не вызваться, slow (Stop-The-World), security risks. Replacement: `AutoCloseable` + try-with-resources, `Cleaner` API (Java 9+, `java.lang.ref.Cleaner`), `PhantomReference`.
- **`Cleaner` (Java 9, JEP 283)** — замена `finalize`. Регистрирует `Runnable` для выполнения, когда объект becomes phantom-reachable. Не determinate timing, но безопаснее (выполняется в отдельном потоке, не нужно `synchronize` на `this`). Использовать для native resource cleanup (file handles, native memory).
- **`final` в immutability**: для immutable класса — все поля `final` + класс `final` (или `sealed`) + defensive copies в конструкторе и accessor-ах. `record` автоматически final + final-поля.
- **`final` и JIT**: final-методы и final-классы позволяют JIT девиртуализировать и инлайнить. После `-XX:+CompileThreshold` hotspot-методы инлайнятся. Non-final методы могут быть инлайнены speculatively (с deopt при загрузке подкласса).
- **`final` static field initialization**: либо inline (`static final int X = 5;`), либо в static-блоке (`static final int X; static { X = compute(); }`). После инициализации — не переприсвоить.
- **Blank final** — `final` поле без инициализатора, должно быть присвоено в конструкторе (один раз). Используется для вычисления в конструкторе.
- **`finalize` chaining**: если переопределён, обязательно вызывать `super.finalize()` в finally — иначе ломается иерархия (хотя сам `finalize` deprecated, в legacy-коде встречается).
- **Static initializer и thread safety**: class initialization в JVM — synchronized (на Class object). Если static-блок создаёт поток, который обращается к классу — deadlock (init не завершён, поток ждёт).

### Материалы

- Книга: "Effective Java" Joshua Bloch (3rd ed., Addison-Wesley, 2018) — Item 8 (avoid finalizers and cleaners — prefer `try-with-resources`), Item 19 (design and document for inheritance or else prohibit it — `final`), Item 32 (combine generics and varargs judiciously — `@SafeVarargs`), Item 81 (prefer concurrency utilities to `wait`/`notify`)
- Книга: "Core Java Volume I—Fundamentals" Cay S. Horstmann (12th ed., 2021) — Chapter 4.6: Initialization (init blocks, order), Chapter 5.6: Object Construction (final, init order)
- Книга: "Java Concurrency in Practice" Brian Goetz et al. (Addison-Wesley, 2006) — Chapter 3.2 (safe publication — `final` fields and visibility), Chapter 7.3 (shutdown — `finalizer` alternatives)
- Документация Oracle (Initialization): https://docs.oracle.com/javase/tutorial/java/javaOO/initial.html
- Документация Oracle (Variable Arguments): https://docs.oracle.com/javase/tutorial/java/javaOO/arguments.html#varargs
- Документация Oracle (Final class/method): https://docs.oracle.com/javase/tutorial/java/IandI/final.html
- Документация Oracle (Object finalize Javadoc — deprecated): https://docs.oracle.com/en/java/javase/21/docs/api/java.base/java/lang/Object.html#finalize()
- Документация Oracle (Cleaner Javadoc Java 9+): https://docs.oracle.com/en/java/javase/21/docs/api/java.base/java/lang/ref/Cleaner.html
- Документация Oracle (Passing Information to a Method or a Constructor — varargs): https://docs.oracle.com/javase/tutorial/java/javaOO/arguments.html
- JEP 283: Disable Finalizers / Cleaner (relates): https://openjdk.org/jeps/283 (Cleaner API introduced Java 9)
- Stack Overflow — Why use a enum to create the singleton pattern (relates to finalize avoidance): https://softwareengineering.stackexchange.com/questions/266677/why-use-a-enum-to-create-the-singleton-pattern
- Baeldung — Java `finally` keyword: https://www.baeldung.com/java-try-finally
- Baeldung — Java Cleaners Introduction: https://www.baeldung.com/java-cleaner

### Практика / Кейсы

**Кейс 16.1. Порядок инициализации с inheritance.**
- Постановка: предсказать вывод программы с многоуровневой иерархией, static и instance блоками.
- Шаги: (1) `class A { static { print("A.s"); } { print("A.i"); } A() { print("A()"); } }` (2) Аналогично `B extends A`, `C extends B`. (3) `new C()` — вывод: `A.s, B.s, C.s, A.i, A(), B.i, B(), C.i, C()`. (4) `new C()` ещё раз — только `A.i, A(), B.i, B(), C.i, C()` (static уже выполнены). (5) Обращение к `C.class` без `new` — только static-блоки: `A.s, B.s, C.s`. (6) Compile-time constant `static final int X = 5;` — не триггерит init. `static final Integer X = 5;` (autoboxed, не constant) — триггерит.
- Что проверить: точное знание JLS §12.4 и §12.5.

**Кейс 16.2. `@SafeVarargs` — heap pollution.**
- Постановка: показать heap pollution и правильно применить `@SafeVarargs`.
- Шаги: (1) `static <T> List<T> of(T... items) { return new ArrayList<>(Arrays.asList(items)); }` — warning. (2) Demonstrate pollution: `Object[] arr = items; arr[0] = "string";` — ClassCastException при чтении как T. (3) Метод safe, если не пишет в varargs-массив — добавить `@SafeVarargs`. (4) На instance method (non-final) — нельзя (Java 8), только с Java 9 для private. (5) JDK примеры: `List.of(T...)`, `Arrays.asList(T...)`. (6) Сравнить с generic-array creation (`T[] arr = (T[]) new Object[10];`) — unchecked, без `@SafeVarargs`.
- Что проверить: способность рассуждать о heap pollution.

**Кейс 16.3. Deadlock в static initializer.**
- Постановка: показать deadlock, когда static-блок создаёт поток, обращающийся к классу.
- Шаги: (1) `class Deadlock { static { Thread t = new Thread(() -> { System.out.println(Deadlock.VALUE); }); t.start(); try { t.join(); } catch (InterruptedException e) {} } static final String VALUE = "hello"; }`. (2) Главный поток держит lock на `Deadlock.class` init. Дочерний поток пытается прочитать `VALUE` — требует init, ждёт lock. `t.join()` ждёт дочерний. Deadlock. (3) Fix: вынести инициализацию в отдельный метод, вызываемый после init. (4) jstack показывает: главный — `WAITING (on monitor)`, дочерний — `BLOCKED`. (5) Best practice: статические блоки должны быть быстрыми и без обращений к собственному классу из других потоков.
- Что проверить: понимание class initialization locking.

**Кейс 16.4. `finalize` deadlock / resurrection.**
- Постановка: продемонстрировать, почему `finalize` опасен (через resurrection и задержку GC).
- Шаги: (1) `class Leaky { static List<Leaky> resurrected = new ArrayList<>(); @Override protected void finalize() { resurrected.add(this); } }` — `finalize` resurrects object (ссылка сохранена). (2) GC собрал, вызвал `finalize`, объект снова reachable. (3) Второй раз `finalize` не вызывается (JLS гарантирует один вызов). (4) Memory leak. (5) Альтернатива — `Cleaner`: `cleaner.register(obj, () -> cleanup())` — `cleanup` выполняется в отдельном потоке Cleaner-а, не resurrects (PhantomReference). (6) Тесты: показать, что с Cleaner нет resurrection.
- Что проверить: понимание finalize pitfalls, modern alternatives.

**Кейс 16.5. `final` для safe publication.**
- Постановка: показать, что `final`-поля видимы другим потокам без synchronization.
- Шаги: (1) `class Config { final int port; final String host; Config(int p, String h) { this.port = p; this.host = h; } }` (2) Один поток создаёт `Config`, передаёт через `volatile`/`AtomicReference` другому. (3) Читающий поток видит правильно сконструированный объект (все `final`-поля видимы). (4) Без `final` — мог бы увидеть partially-constructed (например, `port = 0` ещё не записан). (5) Гарантия — JMM (Java Memory Model, JSR-133). (6) Для non-final-полей — `synchronized`, `volatile`, `AtomicReference` для safe publication.
- Что проверить: understanding JMM, safe publication.

**Кейс 16.6. Overloading `of()` для 0, 1, 2, varargs — оптимизация.**
- Постановка: реализовать `Set.of()`-style API с перегрузками для популярных количеств аргументов.
- Шаги: (1) `static <T> Set<T> of() { return ImmutableSet.empty(); }` (2) `static <T> Set<T> of(T e1) { return new SingletonSet<>(e1); }` (3) `static <T> Set<T> of(T e1, T e2) { ... }` (4) ... до 10 элементов (как в JDK `Set.of`). (5) `@SafeVarargs static <T> Set<T> of(T... items) { return new ImmutableSet<>(items); }` — для >10. (6) Избегает varargs-аллокации в common case. (7) JMH: `Set.of(1, 2, 3)` без varargs быстрее, чем `Set.of(new Integer[]{1,2,3})`.
- Что проверить: владение API design + performance optimization.

---

## 17. Wrapper classes, autoboxing, Integer cache

### Теория

- **Wrapper classes** — `Boolean`, `Byte`, `Short`, `Character`, `Integer`, `Long`, `Float`, `Double` (в `java.lang`). Все extend `Number` (кроме `Boolean` и `Character`). Immutable, `final`. Нужны для: коллекций (`List<Integer>`), reflection (`Method.invoke` принимает `Object[]`), дженериков, null-representation для примитивов (БД `null` в `Integer`-поле).
- **Автоматическое boxing/unboxing (Java 5)**: компилятор конвертирует `int → Integer` через `Integer.valueOf(int)` и `Integer → int` через `Integer.intValue()`. Например, `List<Integer> list = new ArrayList<>(); list.add(1);` — компилятор вставляет `list.add(Integer.valueOf(1))`.
- **`valueOf` и cache**: `Integer.valueOf(int)` для −128..127 возвращает закэшированный экземпляр (`Integer.IntegerCache`). Вне диапазона — новый объект. Это для memory efficiency (JLS §5.1.7).
- **`Integer.IntegerCache`**: static-класс, инициализируется при первой загрузке `Integer`. По умолчанию кэширует −128..127. Можно расширить верхнюю границу через `-XX:AutoBoxCacheMax=<N>` (system property `java.lang.Integer.IntegerCache.high`). Нижняя граница фиксирована −128.
- **Кэши в других wrappers**: `Byte`, `Short`, `Long` кэшируют −128..127 (без возможности расширения). `Character` кэширует 0..127. `Boolean` — два синглтона `TRUE` и `FALSE`. `Float` и `Double` — без кэша (слишком много значений).
- **`==` для wrappers**: `Integer a = 127, b = 127; a == b` → true (cached). `Integer a = 128, b = 128; a == b` → false (different objects). Часто source of bugs. Always use `.equals()` для wrappers.
- **`new Integer(1)`** — всегда создаёт новый объект, не использует cache. Deprecated с Java 9, for removal. Использовать `Integer.valueOf(1)`.
- **Autoboxing в conditional**: `Integer count = ...; if (count == 0) { ... }` — компилятор делает unboxing. Если `count == null` — NPE. Always check null first.
- **Autoboxing в loop**: `Long sum = 0L; for (int i = 0; i < Integer.MAX_VALUE; i++) sum += i;` — каждый `+=` создаёт новый `Long` (immutable), O(n) аллокаций, очень медленно. Effective Java Item 61: prefer primitives to boxed primitives.
- **`Integer.parseInt(String)`** — static, возвращает `int` (без boxing). Бросает `NumberFormatException` (unchecked). Аналогично `Long.parseLong`, `Double.parseDouble`.
- **`Integer.valueOf(String)`** — возвращает `Integer` (с cache для −128..127). Эквивалент `Integer.valueOf(Integer.parseInt(s))`.
- **`Integer.toString(int)`** — static, возвращает `String`. `Integer.toHexString`, `toOctalString`, `toBinaryString`. `String.format("%d", i)` — альтернатива.
- **`Number.intValue()`, `longValue()`, `floatValue()`, `doubleValue()`** — narrowing conversions, могут терять точность. `Double.MAX_VALUE` cast to `int` → `Integer.MAX_VALUE` (saturation, не overflow).
- **`Comparable<Number>` нет** — Number не implements Comparable. Только конкретные wrappers. `Integer implements Comparable<Integer>`. Сравнивать `Integer` и `Long` — через `intValue()` или `Long.compare`.
- **`compare(x, y)` static methods** (Java 7+): `Integer.compare(int, int)`, `Long.compare`, `Double.compare` — возвращают `int` (-1, 0, 1). Без overflow, NaN-aware. Лучше, чем `x - y`.
- **`Number.signum` (Math.signum)** — возвращает sign: -1, 0, 1.
- **`Integer.MIN_VALUE` = -2147483648, `Integer.MAX_VALUE` = 2147483647**. `Math.abs(Integer.MIN_VALUE)` = `Integer.MIN_VALUE` (overflow, т.к. positive analog не помещается). Same for Long.
- **`Integer.hashCode(int)`** — static, возвращает само значение (для −128..127 совпадает с value). `Long.hashCode(long)` — `(int)(value ^ (value >>> 32))`.
- **`Integer.bitCount(int)`** — число установленных бит (popcount). `Integer.highestOneBit`, `lowestOneBit`, `numberOfLeadingZeros`, `numberOfTrailingZeros`, `reverse`, `rotateLeft`, `rotateRight` — битовые утилиты.
- **`Integer.divideUnsigned`, `remainderUnsigned`, `parseUnsignedInt`, `toUnsignedLong` (Java 8)** — беззнаковая арифметика через signed-типы. Аналогично в `Long`.
- **`Integer.sum`, `max`, `min` (Java 8)** — static, для использования как `BinaryOperator`/`IntBinaryOperator` в stream reduce.
- **`Integer.compareUnsigned` (Java 8)** — беззнаковое сравнение.
- **`OptionalInt`, `OptionalLong`, `OptionalDouble`** — для примитивов (без boxing Optional<Integer>).

### Материалы

- Книга: "Effective Java" Joshua Bloch (3rd ed., Addison-Wesley, 2018) — Item 61 (prefer primitive types to boxed primitives), Item 9 (try-with-resources), Item 11 (hashCode), Item 14 (Comparable)
- Книга: "Core Java Volume I—Fundamentals" Cay S. Horstmann (12th ed., 2021) — Chapter 3.5: Wrapper Classes (раздел про autoboxing, Integer cache)
- Книга: "Java Performance" Scott Oaks (2nd ed., O'Reilly, 2020) — Chapter 5: Understanding Strings and Wrapper Classes (memory footprint)
- Документация Oracle (Numbers and Strings — Wrapper Classes): https://docs.oracle.com/javase/tutorial/java/data/numberclasses.html
- Документация Oracle (Autoboxing and Unboxing): https://docs.oracle.com/javase/tutorial/java/data/autoboxing.html
- Документация Oracle (Integer class Javadoc Java 21): https://docs.oracle.com/en/java/javase/21/docs/api/java.base/java/lang/Integer.html
- Документация Oracle (Number class Javadoc): https://docs.oracle.com/en/java/javase/21/docs/api/java.base/java/lang/Number.html
- Документация Oracle (JLS §5.1.7 Boxing Conversion): https://docs.oracle.com/javase/specs/jls/se21/html/jls-5.html#jls-5.1.7
- Документация Oracle (JLS §5.1.8 Unboxing Conversion): https://docs.oracle.com/javase/specs/jls/se21/html/jls-5.html#jls-5.1.8
- GitHub Gist — Java Integer Caching and Autoboxing: https://gist.github.com/ersiner/6100545
- Tavianator — Java autoboxing performance: https://tavianator.com/2015/autoboxing.html
- Blog Vanilla Java — Incorrect Core Java Interview Answers (autoboxing pitfalls): http://blog.vanillajava.blog/2011/07/incorrect-core-java-interview-answers.html
- Baeldung — Primitives vs Objects in Java: https://www.baeldung.com/java-primitives-vs-objects
- Stack Overflow — Number wrapper classes in Java: https://stackoverflow.com/questions/2031768/number-wrapper-classes-in-java

### Практика / Кейсы

**Кейс 17.1. Загадка `Integer ==`.**
- Постановка: объяснить результат `Integer a = 127, b = 127; System.out.println(a == b); Integer c = 128, d = 128; System.out.println(c == d);`.
- Шаги: (1) Запустить — `true, false`. (2) `javap -c` — увидеть `Integer.valueOf(int)` в обоих случаях. (3) Прочитать `Integer.IntegerCache` исходник (или JLS §5.1.7) — диапазон −128..127. (4) Запустить с `-XX:AutoBoxCacheMax=1000` — теперь `c == d` для 128 тоже true. (5) Доказать, что `new Integer(127) == new Integer(127)` — всегда false (deprecation не мешает разобраться). (6) Вывод: всегда `.equals()` для wrappers.
- Что проверить: глубокое понимание Integer cache, ability to read bytecode.

**Кейс 17.2. Autoboxing в loop — OOM/performance.**
- Постановка: показать замедление из-за autoboxing в цикле.
- Шаги: (1) `Long sum = 0L; for (int i = 0; i < 10_000_000; i++) sum += i;` — каждое `+=` создаёт новый Long (immutable), ~10M аллокаций. (2) Fix: `long sum = 0L; ...` — примитив. (3) JMH измерить — примитив в 10–100x быстрее, меньше GC pressure. (4) С `Integer sum = 0; for (int i = 0; i < 1000; i++) sum += i;` — 1000 аллокаций, но вне cache-range (после 127) каждый раз новый. (5) Внутри cache-range — медленнее из-за метод-вызова, но не аллокаций. (6) GC logs: показать частые minor GC с boxed-версией.
- Что проверить: понимание autoboxing performance impact.

**Кейс 17.3. `Math.abs(Integer.MIN_VALUE)` — quirk.**
- Постановка: объяснить, почему `Math.abs(Integer.MIN_VALUE) == Integer.MIN_VALUE` (отрицательное).
- Шаги: (1) `Math.abs(Integer.MIN_VALUE)` возвращает `Integer.MIN_VALUE` (-2147483648). (2) Причина: positive analog (2147483648) не помещается в `int` (max = 2147483647). Битовая операция `-x = ~x + 1` для `MIN_VALUE` даёт сам `MIN_VALUE` (overflow). (3) Аналогично для `Long.MIN_VALUE`. (4) Use case: hash function `Math.abs(key.hashCode()) % n` — может вернуть отрицательный! Fix: `(key.hashCode() & 0x7FFFFFFF) % n` или `Math.floorMod(hash, n)`. (5) `Math.floorMod` (Java 8) — корректный non-negative modulo.
- Что проверить: понимание integer overflow, ability to handle edge cases.

**Кейс 17.4. `Integer.parseInt` vs `Integer.valueOf` vs `new Integer(String)`.**
- Постановка: сравнить три способа конвертации String → Integer.
- Шаги: (1) `Integer.parseInt(s)` — возвращает `int`, без cache, fastest. (2) `Integer.valueOf(s)` — `Integer`, с cache для −128..127. Эквивалент `Integer.valueOf(Integer.parseInt(s))`. (3) `new Integer(s)` (deprecated) — новый объект, без cache, не recommended. (4) JMH: parseInt fastest, valueOf чуть медленнее (cache lookup для маленьких чисел), new Integer — аллокация каждый раз. (5) Для `Optional<Integer>` — `Optional.ofNullable(s).map(Integer::parseInt)`. (6) NumberFormatException — catch explicitly, не оборачивать в generic.
- Что проверить: способность выбирать правильный API.

**Кейс 17.5. Unsigned arithmetic через Integer.**
- Постановка: реализовать беззнаковое сравнение и деление для int.
- Шаги: (1) `Integer.compareUnsigned(int x, int y)` (Java 8) — интерпретирует биты как unsigned. (2) `Integer.divideUnsigned(int dividend, int divisor)` (Java 8) — unsigned division. (3) `Integer.parseUnsignedInt("4294967295")` — max unsigned int (2^32 − 1). (4) `Integer.toUnsignedLong(int)` — конвертирует в long без sign extension. (5) Use case: IP-адреса (IPv4 = unsigned int), networking. (6) Сравнить с использованием `long` для всех unsigned operations — медленнее, но проще.
- Что проверить: владение unsigned API.

**Кейс 17.6. Custom `Integer` cache extension.**
- Постановка: расширить `Integer.IntegerCache` до −128..1000 и измерить memory overhead.
- Шаги: (1) Запуск JVM с `-XX:AutoBoxCacheMax=1000`. (2) `Integer a = 1000, b = 1000; a == b` → true. (3) `Integer a = 1001, b = 1001; a == b` → false. (4) Memory: каждый cached Integer ≈ 16 bytes (object header + int). 1000 cached = 16 KB. Незначительно. (5) Запуск с `-Djava.lang.Integer.IntegerCache.high=10000` — альтернативный способ (system property, читается в static-блоке). (6) Сравнить с `Byte` cache (только −128..127, без расширения) — меньше гибкости, но и не нужно (диапазон маленький).
- Что проверить: понимание cache internals, ability to tune JVM.

---

## 18. Unicode/Charset/кодировки

### Теория

- **Unicode** — стандарт кодирования символов. Каждому символу присваивается codepoint (число 0–0x10FFFF, ~1.1M возможных, ~150K assigned). UTF-8, UTF-16, UTF-32 — кодировки (представление codepoint-ов в байтах).
- **UTF-8** — variable-length (1–4 байта). ASCII (0–127) — 1 байт, латиница — 2, китайский — 3, эмодзи — 4. Совместим с ASCII, compact для латиницы, dominates web (~98%). Default в Java с Java 18 (JEP 400).
- **UTF-16** — variable-length (2 или 4 байта). BMP (0–0xFFFF) — 2 байта (1 `char`). Supplementary planes (эмодзи, редкие иероглифы) — 4 байта (2 `char`, surrogate pair). Используется в Java `String` (с Java 9 — Compact Strings: LATIN1 для ASCII, UTF-16 для остальных).
- **UTF-32** — fixed 4 байта/codepoint. Редко используется (memory overhead), но прост. Python 3 internally uses something similar for codepoint access.
- **Surrogate pairs**: codepoints 0xD800–0xDFFF зарезервированы для surrogate pairs в UTF-16. High surrogate (0xD800–0xDBFF) + low surrogate (0xDC00–0xDFFF) кодируют codepoint > 0xFFFF. `String.length()` возвращает число `char`-ов (code units), не codepoints. `"\uD83D\uDE00".length() == 2`, но это один символ (😀).
- **`Charset`** (`java.nio.charset.Charset`) — представление кодировки. `StandardCharsets.UTF_8` (constant), `Charset.forName("UTF-8")` (lookup). `Charset.defaultCharset()` — default для JVM (зависела от locale до Java 18, теперь UTF-8 по JEP 400).
- **`String.getBytes(Charset)`** — кодирование в байты. `new String(byte[], Charset)` — декодирование. С Java 18 default charset = UTF-8 (раньше — `file.encoding`, OS-dependent, source of bugs).
- **`InputStreamReader` / `OutputStreamWriter`** — bridge между byte и char streams с указанным Charset. `new InputStreamReader(in, StandardCharsets.UTF_8)`. Без указания charset — используется default (UTF-8 с Java 18).
- **`FileReader`** (Java 11+) — конструктор с Charset: `new FileReader(file, StandardCharsets.UTF_8)`. До Java 11 — использовал default, что было source of bugs.
- **`Files.newBufferedReader(Path, Charset)`**, `Files.readString(Path, Charset)` — современные API с явным charset.
- **`CharsetEncoder` / `CharsetDecoder`** — low-level API для streaming encode/decode. Полезно для больших данных, error handling (replace, ignore, report).
- **Coding error actions**: `CodingErrorAction.REPORT` (бросает `CharacterCodingException`), `REPLACE` (заменяет на `?` или `\uFFFD`), `IGNORE` (пропускает). Default для `CharsetDecoder` — REPORT, для InputStreamReader — REPLACE.
- **BOM (Byte Order Mark)**: `U+FEFF` в начале файла указывает кодировку и endianness. UTF-8 BOM — `EF BB BF`. Java `String`/`InputStreamReader` не обрабатывает BOM автоматически — нужно вручную через `BOMInputStream` (Apache Commons) или skip первых 3 байт. UTF-16 BOM — `FE FF` (big-endian) или `FF FE` (little-endian).
- **`char` vs `Character` vs `int` codepoint**: `char` — 16 bit UTF-16 code unit (0–0xFFFF). `Character` — wrapper. Codepoint — `int` (0–0x10FFFF). `Character.toCodePoint(highSurrogate, lowSurrogate)` — из пары char в codepoint. `Character.toChars(codepoint)` — в `char[]` (1 или 2). `Character.isSupplementaryCodePoint(cp)`. `String.codePointAt(i)`.
- **`String.codePoints()`** (Java 8) — `IntStream` codepoints. `String.chars()` — `IntStream` char (UTF-16 code units). `s.codePoints().count()` — число codepoints (отличается от `s.length()` для supplementary).
- **Normalization**: один символ может иметь несколько Unicode-представлений. `é` = `U+00E9` (precomposed) или `e + U+0301` (decomposed). `Normalizer.normalize(s, Form.NFC)` — composed, `NFD` — decomposed, `NFKC`, `NFKD` — compatibility. Важно для сравнения строк, поиска.
- **Collator** (`java.text.Collator`) — locale-aware сравнение строк. Учитывает правила языка (в немецком `ä` между `a` и `b`, в шведском после `z`). `Collator.getInstance(Locale.GERMAN).compare("ä", "z")`. Полезно для сортировки в UI.
- **`BreakIterator`** — для разбиения на слова/строки/символы с учётом locale. `BreakIterator.getCharacterInstance()` — для grapheme clusters (визуальных символов, которые могут состоять из нескольких codepoints, например флаг `🇷🇺` = 2 regional indicator symbols).
- **`java.text.NumberFormat`**, `DateFormat` — locale-aware formatting. Не использовать `String.format` с locale-dependent паттернами без указания Locale.
- **JEP 400: UTF-8 by Default (Java 18)**: `Charset.defaultCharset()` возвращает UTF-8 независимо от OS. `file.encoding` теперь не влияет на default (только на `System.out`/`System.err` в некоторых средах). Для совместимости со старым поведением — `-Dfile.encoding=UTF-8` или `System.setOut(new PrintStream(System.out, true, "Cp1251"))`.
- **JEP 418: Internet-Address Resolution SPI (relates to i18n)**: not directly charset, but locale/region.
- **Locale**: `Locale.US`, `Locale.GERMANY`, `new Locale("ru", "RU")`. `Locale.forLanguageTag("ru-RU")` (BCP 47). `Locale.getDefault()`, `setDefault(Locale)`. Влияет на NumberFormat, DateFormat, Collator.
- **Resource bundles**: `ResourceBundle.getBundle("messages", locale)` — i18n-сообщения. `.properties` файлы — ISO-8859-1 по умолчанию (исторически), UTF-8 с Java 9 (через `PropertyResourceBundle` с UTF-8 или `UTF-8` encoded `.properties`).
- **`Files.readAllLines`** — использует UTF-8 по умолчанию (с Java 18), можно указать явно.
- **Security implications**: неправильная обработка кодировок → mojibake (кракозябры), security bypass (Unicode normalization — different representations of "/", "../"), Path Traversal. `URI` и `URL` — encoding/decoding `%XX` sequences.

### Материалы

- Книга: "Effective Java" Joshua Bloch (3rd ed., Addison-Wesley, 2018) — Item 79 (synchronization, relates to shared mutable data, Charset immutable), Item 85 (alternatives to serialization — note charset in serialized forms)
- Книга: "Core Java Volume II—Advanced Features" Cay S. Horstmann (12th ed., 2021) — Chapter 1.6: Character Encodings (Charset, CharBuffer)
- Книга: "Java Internationalization" Andrew Deitsch & David Czarnecki (O'Reilly, 2001) — классика, основы i18n
- Документация Oracle (Internationalization tutorial): https://docs.oracle.com/javase/tutorial/i18n/index.html
- Документация Oracle (Unicode): https://docs.oracle.com/en/java/javase/21/docs/api/java.base/java/lang/Character.html#unicode
- Документация Oracle (Charset Javadoc): https://docs.oracle.com/en/java/javase/21/docs/api/java.base/java/nio/charset/Charset.html
- Документация Oracle (StandardCharsets Javadoc): https://docs.oracle.com/en/java/javase/21/docs/api/java.base/java/nio/charset/StandardCharsets.html
- Документация Oracle (Normalizer Javadoc): https://docs.oracle.com/en/java/javase/21/docs/api/java.base/java/text/Normalizer.html
- Документация Oracle (Collator Javadoc): https://docs.oracle.com/en/java/javase/21/docs/api/java.base/java/text/Collator.html
- Документация Oracle (BreakIterator Javadoc): https://docs.oracle.com/en/java/javase/21/docs/api/java.base/java/text/BreakIterator.html
- Документация Oracle (Character Sets and Encodings — Java EE Tutorial): https://docs.oracle.com/javaee/5/tutorial/doc/bnayb.html
- JEP 400: UTF-8 by Default (Java 18): https://openjdk.org/jeps/400
- JEP 418: Internet-Address Resolution SPI (Java 18, relates): https://openjdk.org/jeps/418
- Baeldung — Guide to Character Encoding: https://www.baeldung.com/java-char-encoding
- GeeksforGeeks — How to Get and Set Default Character Encoding or Charset in Java: https://www.geeksforgeeks.org/java/how-to-get-and-set-default-character-encoding-or-charset-in-java
- StickyMinds — New Java 18 Feature Default Charset UTF-8: https://www.stickyminds.com/article/new-java-18-feature-default-charset-utf-8
- Stack Overflow — Setting the default Java character encoding: https://stackoverflow.com/questions/361975/setting-the-default-java-character-encoding

### Практика / Кейсы

**Кейс 18.1. Mojibake — диагностика и исправление.**
- Постановка: файл в UTF-8 прочитан как Cp1251, строки кривые. Восстановить оригинал.
- Шаги: (1) Создать UTF-8 файл с русским текстом. (2) Прочитать как Cp1251: `new String(Files.readAllBytes(path), "Cp1251")` — кракозябры. (3) Восстановление: `new String(garbled.getBytes("Cp1251"), "UTF-8")` — обратная конвертация через bytes. (4) Работает, если оба charset-а bijective (нет потерь). (5) Если были невалидные bytes (Cp1251 не может представить) — данные потеряны, восстановление невозможно. (6) Best practice: всегда указывать charset явно, использовать UTF-8 везде.
- Что проверить: способность рассуждать о кодировках, ability to recover mojibake.

**Кейс 18.2. Длина строки с эмодзи.**
- Постановка: объяснить, почему `"Hello 😀 world!".length() != 14` (графических символов).
- Шаги: (1) `String s = "Hello 😀 world!";` — 14 видимых символов, но `s.length() == 15` (эмодзи = 2 char). (2) `s.codePoints().count()` — 14 (по codepoints). (3) `BreakIterator.getCharacterInstance().characterIterator()` — grapheme clusters, тоже 14 (в этом случае). (4) Для флага `🇷🇺` = 2 regional indicators = 4 char = 1 grapheme cluster. (5) `BreakIterator` даёт правильное число grapheme. (6) Use case: ограничение длины в UI (twitter-like) — нужно по grapheme, не char.
- Что проверить: понимание codepoint vs char vs grapheme.

**Кейс 18.3. Unicode normalization для поиска.**
- Постановка: реализовать поиск, который находит "café" даже если в базе "café" (разные представления).
- Шаги: (1) База хранит `café` как `e + U+0301` (NFD). Запрос `café` как `U+00E9` (NFC). (2) Plain `equals` → false, `equals(NFD)` → true. (3) Solution: нормализ обе строки в одну форму (NFC или NFKC для совместимости): `Normalizer.normalize(s, Form.NFC)`. (4) Для case-insensitive: `s.toLowerCase(locale)` тоже не достаточно для немецкого ß → ss. Использовать `Collator` с `PRIMARY` strength. (5) Для поиска — построить normalized lowercase ключ заранее (store as separate field). (6) Performance: нормализация на каждый lookup — медленно, лучше pre-compute.
- Что проверить: понимание normalization, ability to design search.

**Кейс 18.4. Locale-aware сортировка.**
- Постановка: отсортировать список немецких имен с учётом правил немецкого (ä между a и b).
- Шаги: (1) `Collator collator = Collator.getInstance(Locale.GERMANY); collator.setStrength(Collator.PRIMARY);` — игнорировать accents и case. (2) `list.sort(collator::compare)` или `Collections.sort(list, collator)`. (3) Без Collator — `Collections.sort(list)` использует String.compareTo, который сравнивает по UTF-16 code units (ä после z). (4) Для secondary strength — учитывать accents. (5) Тесты: сравнить с ожидаемым порядком. (6) Performance: Collator медленнее String.compareTo — кэшировать CollationKey для повторной сортировки: `CollationKey key = collator.getCollationKey(s); list.sort(Comparator.comparing(o -> o.key))`.
- Что проверить: владение Collator, performance awareness.

**Кейс 18.5. Migration на UTF-8 default (Java 18).**
- Постановка: приложение, запускаемое на Java 11 с Cp1251 на Windows, мигрируется на Java 21. Найти и устранить regressions.
- Шаги: (1) На Java 11 Windows: `Charset.defaultCharset()` = Cp1251 (locale-dependent). На Java 21 — UTF-8. (2) Чтение файлов без указания charset: `Files.readAllBytes` + `new String(bytes)` — раньше Cp1251, теперь UTF-8. Для файлов в Cp1251 — mojibake. (3) Запуск с `-Dfile.encoding=Cp1251` для совместимости (но deprecated, will break in future). (4) Правильный фикс: конвертировать все файлы в UTF-8, явно указывать charset в коде. (5) Логи в `System.out` на Windows — могут быть проблемы с консолью (Cp1251). `System.setOut(new PrintStream(System.out, true, "Cp1251"))` или использовать logback с правильным charset. (6) Тесты: проверить чтение/запись всех конфигов, логов, взаимодействия с legacy-системами.
- Что проверить: понимание JEP 400, ability to migrate safely.

**Кейс 18.6. Безопасность — Path Traversal через Unicode normalization.**
- Постановка: злоумышленник передаёт путь с `%c0%af` (overlong UTF-8 encoding of `/`), обходя validation. Защититься.
- Шаги: (1) Validation проверяет `path.contains("/")` → false для `%c0%af`. (2) При декодировании в `String` — overlong UTF-8 может стать `/` (если decoder lenient). (3) Java `StandardCharsets.UTF_8` — strict, отвергает overlong (бросает `CharacterCodingException`). Но legacy-decoders могут быть lenient. (4) Лучший fix: использовать `Path.normalize()` и проверить, что результат внутри expected root: `if (!path.toAbsolutePath().startsWith(root.toAbsolutePath())) throw new SecurityException();`. (5) Также `..` в пути — `Path.of("../secret").normalize()` = `secret` (relative). (6) Использовать `Path.of(base).resolve(userInput).normalize()` и проверять startsWith. (7) Тесты: попытки обхода через `..`, overlong UTF-8, null bytes, URL encoding.
- Что проверить: понимание security implications Unicode, defensive coding.

---

## Сводка источников (топ-15)

Ниже — consolidated список ключевых ресурсов по всему разделу Java Core. Полные ссылки — в каждом подразделе.

1. **Effective Java, Joshua Bloch (3rd ed., Addison-Wesley, 2018)** — главный reference по best practices. Item 8–14 (object methods), 18–20 (inheritance), 26–33 (generics), 34–37 (enums), 55 (Optional), 65 (reflection), 69–77 (exceptions), 45–48 (streams).
2. **Java Concurrency in Practice, Brian Goetz et al. (Addison-Wesley, 2006)** — обязательный для Collections concurrent (ConcurrentHashMap, blocking queues, JMM, final fields).
3. **Java Generics and Collections, Maurice Naftalin & Philip Wadler (O'Reilly, 2006)** — глубокое погружение в Generics (type erasure, bridge methods) и все Collections.
4. **Core Java Volume I—Fundamentals, Cay S. Horstmann (12th ed., Prentice Hall, 2021)** — фундаментальный tutorial-справочник по всем темам раздела.
5. **Modern Java in Action, Urma/Fusco/Mycroft (Manning, 2018)** — современный взгляд на streams, Optional, java.time, default methods.
6. **Java Performance, Scott Oaks (2nd ed., O'Reilly, 2020)** — Compact Strings, autoboxing overhead, JVM tuning.
7. **Oracle Java Tutorials — Learning the Java Language**: https://docs.oracle.com/javase/tutorial/java/index.html
8. **Oracle Java Tutorials — Essential Classes (Exceptions, Concurrency, I/O)**: https://docs.oracle.com/javase/tutorial/essential/index.html
9. **Oracle Java Tutorials — Collections**: https://docs.oracle.com/javase/tutorial/collections/index.html
10. **Oracle Java Tutorials — Internationalization**: https://docs.oracle.com/javase/tutorial/i18n/index.html
11. **Oracle JDK 21 API Documentation**: https://docs.oracle.com/en/java/javase/21/docs/api/index.html
12. **OpenJDK JEPs (JDK Enhancement Proposals)**: https://openjdk.org/jeps/0 — ключевые: 126 (lambda), 107 (streams), 110 (Optional), 150 (java.time), 254 (Compact Strings), 269 (collection factories), 378 (text blocks), 395 (records), 409 (sealed), 400 (UTF-8 by default).
13. **Baeldung Java Tutorials**: https://www.baeldung.com/java — сотни статей по всем темам (HashMap, streams, Optional, exceptions, NIO, java.time).
14. **Jenkov Tutorials (Reflection, NIO)**: https://jenkov.com/tutorials/java-reflection/index.html, https://jenkov.com/tutorials/java-nio/index.html
15. **Java Language Specification (JLS) SE 21**: https://docs.oracle.com/javase/specs/jls/se21/html/index.html — авторитетный источник для тонких вопросов (init order, conversion rules, generics erasure, boxing conversion).

---

*Раздел 1 «Java Core & Foundations» завершён. Следующие разделы дерева: 02-java-advanced (JVM internals, concurrency, memory model, GC, JIT), 03-build-test-eng (Maven/Gradle, JUnit, Mockito, JMH), 04-spring-ecosystem, 05-persistence-db, 06-distributed-msg, 07-devops-observability, 08-arch-patterns-leadership.*
