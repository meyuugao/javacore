package me.yuugao.lesson01.demo;

/**
 * Класс {@code OperatorsDemo} демонстрирует работу логических,
 * побитовых операторов и операторов сдвига в Java
 * <p>
 * Особый фокус на:
 *     <ul>
 *         <li>Порядок логических операторов</li>
 *         <li>Коротком замыкании (short-circuiting) логических операторов {@code &&} и {@code ||}.</li>
 *         <li>Разнице между {@code >>} (сдвиг с сохранением знака) и {@code >>>} (беззнаковый сдвиг).</li>
 *     </ul>
 * </p>
 *
 * @author MeYuugao
 * @version 1.0
 * @since Java 21
 */
public class OperatorsDemo {
    private final String precedenceResult;
    private final String shortCircuitResult;
    private final String bitwiseShiftResult;
    private boolean wasCalled;

    public OperatorsDemo() {
        precedenceResult = testPrecedence();
        shortCircuitResult = testShortCircuit(3); // Передаём такое i, чтобы первое условие не сработало
        bitwiseShiftResult = testBitwiseShift(-8);
    }

    /**
     * Демонстрирует всю цепочку приоритетов операторов Java -
     * от высшего (постфиксные) к низшему (присваивание).
     * <p>
     * Полная цепочка по JLS §15:
     *     <ol>
     *         <li>Постфиксные: {@code i++}, {@code arr[i]}, {@code obj.method()}</li>
     *         <li>Унарные: {@code ++i}, {@code -i}, {@code !b}, {@code ~x}</li>
     *         <li>Мультипликативные: {@code * / %}</li>
     *         <li>Аддитивные: {@code + -}</li>
     *         <li>Сдвиги: {@code << >> >>>}</li>
     *         <li>Реляционные: {@code < > <= >= instanceof}</li>
     *         <li>Равенство: {@code == !=}</li>
     *         <li>Побитовое И: {@code &}</li>
     *         <li>Побитовое XOR: {@code ^}</li>
     *         <li>Побитовое ИЛИ: {@code |}</li>
     *         <li>Логическое И: {@code &&}</li>
     *         <li>Логическое ИЛИ: {@code ||}</li>
     *         <li>Тернарный: {@code ? :}</li>
     *         <li>Присваивание: {@code = += -= ...}</li>
     *     </ol>
     * </p>
     * <p>
     *     Правило: операторы ВЫШЕ по списку выполняются ПЕРВЫМИ.
     *     Одинаковый приоритет - слева направо (кроме унарных и присваивания - справа налево).
     * </p>
     */
    private String testPrecedence() {
        StringBuilder sb = new StringBuilder();
        sb.append("--- Полная цепочка приоритетов (высший → низший) ---\n");

        int i = 5;
        int postfixVsUnary = -i++;
        sb.append("[1] Постфикс > Унарный: -i++  (i=5)  = %d   (после: i=%d)".formatted(postfixVsUnary, i)).append("\n");

        int x = 10;
        int unaryVsMul = ++x * 2;
        sb.append("[2] Унарный > *: ++x * 2  (x=10) = %d   (после: x=%d)".formatted(unaryVsMul, x)).append("\n");

        int mulVsAdd = 2 + 3 * 4;
        sb.append("[3] * > +: 2 + 3 * 4 = %d".formatted(mulVsAdd)).append("\n");

        int addVsShift = 1 + 2 << 1;
        sb.append("[4] + > <<: 1 + 2 << 1 = %d   ((1+2)<<1, не 1+(2<<1))".formatted(addVsShift)).append("\n");

        boolean shiftVsRel = 1 << 2 < 5;
        sb.append("[5] << > <: 1 << 2 < 5  = %b   ((1<<2)=4 < 5)".formatted(shiftVsRel)).append("\n");

        boolean relVsEq = 1 < 2 == true;
        sb.append("[6] < > ==:  1 < 2 == true = %b".formatted(relVsEq)).append("\n");

        boolean eqVsBitAnd = false & 2 == 0;
        sb.append("[7] == > & : false & 2 == 0  = %b   (читается как false & (2==0))".formatted(eqVsBitAnd)).append("\n");

        int bitAndVsXor = 5 & 3 ^ 1;
        sb.append("[8] & > ^: 5 & 3 ^ 1   = %d   ((5&3)=1, 1^1=0)".formatted(bitAndVsXor)).append("\n");

        int xorVsBitOr = 5 ^ 3 | 2;
        sb.append("[9] ^ > |: 5 ^ 3 | 2   = %d   ((5^3)=6, 6|2=6)".formatted(xorVsBitOr)).append("\n");

        boolean bitOrVsLogicAnd = false | true && false;
        sb.append("[10] | > &&: false | true && false = %b   (true&&false=false, потом |)".formatted(bitOrVsLogicAnd)).append("\n");

        boolean andVsOr = true || false && false;
        sb.append("[11] && > ||:  true || false && false = %b".formatted(andVsOr)).append("\n");

        String orVsTernary = true ? "A" : "B";
        sb.append("[12] || > ?: false || true ? \"A\" : \"B\" = \"%s\"".formatted(orVsTernary)).append("\n");

        int cond = 5;
        int ternaryVsAssign = cond > 3 ? 1 : 0;
        sb.append("[13] ?: > =: r = cond>3 ? 1 : 0  → r = %d".formatted(ternaryVsAssign)).append("\n");

        int a, b, c;
        a = b = c = 5;
        sb.append("[14] = справа налево:  a = b = c = 5  → a=%d, b=%d, c=%d".formatted(a, b, c)).append("\n");

        int j = 0;
        int r = ++j + j++ + j;
        sb.append("\n[Бонус] i=0; ++i + i++ + i = %d   (после: i=%d) — порядок вычисления операндов, не приоритет".formatted(r, j));

        return sb.toString();
    }

    /**
     * Демонстрирует разницу между && (и) и & (побитовое И для boolean).
     */
    private String testShortCircuit(int i) {
        wasCalled = false;
        boolean r1 = i == 2 & checkAndLog(i);
        boolean called1 = wasCalled;

        wasCalled = false;
        boolean r2 = i == 2 && checkAndLog(i);
        boolean called2 = wasCalled;

        return "--- Короткое замыкание ---\n" +
                "i = %d; условие: i == 2 &/&& checkAndLog(i)\n".formatted(i) +
                "Без короткого замыкания (&):  result = %b; checkAndLog вызван = %b\n".formatted(r1, called1) +
                "С коротким замыканием (&&):   result = %b; checkAndLog вызван = %b".formatted(r2, called2);
    }

    private boolean checkAndLog(int i) {
        wasCalled = true;
        return i % 2 == 0;
    }

    /**
     * Демонстрирует разницу между >> и >>>.
     */
    private String testBitwiseShift(int i) {
        String bin = Integer.toBinaryString(i);
        String shifted = Integer.toBinaryString(i >> 2);
        String unsigned = Integer.toBinaryString(i >>> 2);
        return "--- Побитовый сдвиг ---\n" +
                "i = %d; операции: i >> 2; i >>> 2.\n".formatted(i) +
                "i       : %s\n".formatted(pad32(bin)) +
                "i >> 2  : %s  (заполняет левыми битами — знак сохраняется)\n".formatted(pad32(shifted)) +
                "i >>> 2 : %s  (заполняет нулями — знак теряется)".formatted(pad32(unsigned));
    }

    private static String pad32(String s) {
        return String.format("%32s", s).replace(' ', '0');
    }

    /**
     * Возвращает красиво отформатированный отчёт о всех проведённых тестах.
     * Переопределение метода {@link Object#toString()} позволяет нам просто вызвать {@link System#out#println()} для красивого вывода.
     *
     * @return форматированную строку с демонстрацией работы операторов в Java.
     */
    @Override
    public String toString() {
        return """
                --- Демонстрация операторов Java ---
                
                %s
                %s
                %s
                ---------------------------------------------------
                """.formatted(precedenceResult, shortCircuitResult, bitwiseShiftResult);
    }
}