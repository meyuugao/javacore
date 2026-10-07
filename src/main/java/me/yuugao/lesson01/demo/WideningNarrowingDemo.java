package me.yuugao.lesson01.demo;

/**
 * Класс {@code WideningNarrowingDemo} демонстрирует расширяющие (widening)
 * и сужающие (narrowing) преобразования примитивных типов в Java.
 * <p>
 * Особый фокус на:
 *     <ul>
 *         <li>Автоматическом расширении {@code byte}/{@code short}/{@code char} до {@code int} в арифметике (JLS §5.6).</li>
 *         <li>Ловушке {@code b = b + 1} для {@code byte} и неявном касте в составном присваивании {@code b += 1} (JLS §15.26.2).</li>
 *         <li>Сужающем касте с потерей: {@code (byte) 300 == 44}, {@code (int) 3.9 == 3}, насыщении {@code (int) 1e20}.</li>
 *         <li>Проверке {@code instanceof}, в том числе {@code null instanceof X == false}.</li>
 *     </ul>
 * </p>
 *
 * @author MeYuugao
 * @version 1.0
 * @since Java 21
 */
public class WideningNarrowingDemo {
    private final String wideningResult;
    private final String narrowingResult;
    private final String compoundResult;
    private final String instanceofResult;

    public WideningNarrowingDemo() {
        wideningResult = testWidening();
        narrowingResult = testNarrowing();
        compoundResult = testCompoundAssign();
        instanceofResult = testInstanceofNull();
    }

    /**
     * Демонстрирует расширяющие преобразования (widening primitive conversion, JLS §5.1.2).
     * <p>
     * Правила:
     *     <ol>
     *         <li>{@code byte}, {@code short}, {@code char} в любом арифметическом выражении
     *         сначала расширяются до {@code int} (unary/binary numeric promotion, JLS §5.6).</li>
     *         <li>Дальше побеждает более широкий тип: {@code int + long -> long},
     *         {@code int + double -> double}, {@code int + float -> float}.</li>
     *         <li>Поэтому {@code byte + byte} имеет тип {@code int}, что видно через
     *         {@code getClass()} упакованного результата.</li>
     *     </ol>
     * Причина на уровне JVM: арифметических байткодов для {@code byte}/{@code short}
     * не существует, есть только {@code iadd}/{@code isub} и т.д. для {@code int}.
     * </p>
     */
    private String testWidening() {
        StringBuilder sb = new StringBuilder();
        sb.append("--- Расширение (widening): byte/short/char -> int ---\n");

        byte b = 10;
        short s = 20;
        char c = 'A'; // 65

        Object sumByteShort = (b + s);
        sb.append("byte b=10 + short s=20 = %d (тип: %s)".formatted(b + s, sumByteShort.getClass().getSimpleName())).append("\n");

        Object sumCharInt = (c + 1);
        sb.append("char 'A'(65) + 1 = %d (тип: %s), (char)(c+1) = '%c'".formatted(c + 1, sumCharInt.getClass().getSimpleName(), (char) (c + 1))).append("\n");

        int i = 100;
        long l = 1_000_000_000_00L;
        Object intPlusLong = (i + l);
        sb.append("int + long -> %s: 100 + 100000000000 = %d".formatted(intPlusLong.getClass().getSimpleName(), i + l)).append("\n");

        Object intPlusDouble = (i + 0.5);
        sb.append("int + double -> %s: 100 + 0.5 = %s".formatted(intPlusDouble.getClass().getSimpleName(), i + 0.5)).append("\n");

        Object intPlusFloat = (i + 0.5F);
        sb.append("int + float -> %s: 100 + 0.5F = %s".formatted(intPlusFloat.getClass().getSimpleName(), i + 0.5F));

        return sb.toString();
    }

    /**
     * Демонстрирует сужающие преобразования (narrowing primitive conversion, JLS §5.1.3).
     * <p>
     * Правила:
     *     <ol>
     *         <li>Целое к целому: отбрасываются старшие биты (остаток по модулю 2^N).
     *         {@code (byte) 300 == 44}, так как {@code 300 - 256 = 44};
     *         {@code (byte) 128 == -128} (переход через границу знакового диапазона).</li>
     *         <li>Дробное к целому: отбрасывается дробная часть, округление к нулю
     *         (не вниз): {@code (int) 3.9 == 3}, {@code (int) -3.9 == -3}.</li>
     *         <li>Особый случай: {@code double}/{@code float} в {@code int} при переполнении
     *         не заворачивается, а насыщается: {@code (int) 1e20 == Integer.MAX_VALUE}.</li>
     *     </ol>
     * Сужающий каст всегда пишется явно и всегда что-то теряет — либо биты, либо дробь, либо диапазон.
     * </p>
     */
    private String testNarrowing() {
        StringBuilder sb = new StringBuilder();
        sb.append("--- Сужение (narrowing): явный каст с потерей ---\n");

        int v300 = 300;
        sb.append("(byte) 300 = %d   (300 - 256 = 44, младший байт)".formatted((byte) v300)).append("\n");

        sb.append("(byte) 128 = %d   (выход за 127, переход в -128)".formatted((byte) 128)).append("\n");
        sb.append("(byte) 130 = %d   (130 - 256 = -126)".formatted((byte) 130)).append("\n");

        sb.append("(int) 3.9 = %d;  (int) -3.9 = %d   (отброс дроби, к нулю)".formatted((int) 3.9, (int) -3.9)).append("\n");

        sb.append("(int) 1e20 = %d   (насыщение, а не обёртка: MAX_VALUE)".formatted((int) 1e20)).append("\n");

        long huge = 3_000_000_000L;
        sb.append("(int) 3000000000L = %d   (отброшены старшие 32 бита)".formatted((int) huge));

        return sb.toString();
    }

    /**
     * Демонстрирует разницу между простым и составным присваиванием для узких типов.
     * <p>
     * Строка {@code b = b + 1} для {@code byte b} не компилируется: справа тип
     * {@code int} (см. {@link #testWidening()}), а неявного сужения Java не делает.
     * Нужен явный каст: {@code b = (byte) (b + 1)}.
     * </p>
     * <p>
     * Составное присваивание {@code b += 1} компилируется, потому что по JLS §15.26.2
     * оно раскрывается как {@code b = (byte) (b + 1)} — каст вставлен неявно.
     * То же верно для {@code *=}, {@code <<=} и остальных {@code op=}.
     * </p>
     */
    private String testCompoundAssign() {
        StringBuilder sb = new StringBuilder();
        sb.append("--- Составное присваивание: скрытый каст ---\n");
        sb.append("byte b = 1; b = b + 1;       // НЕ компилируется: справа int, нужен (byte)").append("\n");

        byte b = 1;
        b += 1;
        sb.append("byte b = 1; b += 1;  -> b = %d   (раскрывается как b = (byte)(b + 1))".formatted(b)).append("\n");

        b *= 10;
        sb.append("b *= 10;             -> b = %d".formatted(b)).append("\n");

        b <<= 1;
        sb.append("b <<= 1;             -> b = %d   (сдвиг + неявный каст)".formatted(b));

        return sb.toString();
    }

    /**
     * Демонстрирует оператор {@code instanceof}, включая проверку на {@code null}.
     * <p>
     * Правила:
     *     <ol>
     *         <li>{@code null instanceof X} всегда {@code false} для любого типа {@code X} — без исключения.</li>
     *         <li>Проверяется отношение is-a, включая интерфейсы:
     *         {@code ArrayList instanceof List} — {@code true}.</li>
     *         <li>С Java 16 доступен pattern binding: {@code if (o instanceof String str)},
     *         переменная {@code str} видна только там, где проверка прошла.</li>
     *     </ol>
     * </p>
     */
    private String testInstanceofNull() {
        StringBuilder sb = new StringBuilder();
        sb.append("--- instanceof и null ---\n");

        Object nothing = null;
        sb.append("null instanceof String = %b;  null instanceof Object = %b".formatted(nothing instanceof String, nothing instanceof Object)).append("\n");

        Object text = "hi";
        sb.append("\"hi\" instanceof String = %b;  \"hi\" instanceof Integer = %b".formatted(text instanceof String, text instanceof Integer)).append("\n");

        java.util.ArrayList<String> list = new java.util.ArrayList<>();
        sb.append("ArrayList instanceof List = %b   (проверка по интерфейсу)".formatted(list instanceof java.util.List)).append("\n");

        String bound;
        if (text instanceof String str) {
            bound = "pattern binding сработал, длина = %d".formatted(str.length());
        } else {
            bound = "pattern binding не сработал";
        }
        sb.append("if (text instanceof String str) -> %s".formatted(bound));

        return sb.toString();
    }

    /**
     * Возвращает красиво отформатированный отчёт о всех проведённых тестах.
     * Переопределение метода {@link Object#toString()} позволяет нам просто вызвать {@link System#out#println()} для красивого вывода.
     *
     * @return форматированную строку с демонстрацией widening/narrowing преобразований.
     */
    @Override
    public String toString() {
        return """
                --- Демонстрация widening/narrowing в Java ---

                %s
                %s
                %s
                %s
                ---------------------------------------------------
                """.formatted(wideningResult, narrowingResult, compoundResult, instanceofResult);
    }
}
