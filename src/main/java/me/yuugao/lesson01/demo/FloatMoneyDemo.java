package me.yuugao.lesson01.demo;

import java.math.BigDecimal;
import java.math.MathContext;
import java.math.RoundingMode;

/**
 * Класс {@code FloatMoneyDemo} демонстрирует неточность двоичного
 * floating-point и правильный учёт денег через {@code BigDecimal}.
 * <p>
 * Особый фокус на:
 *     <ul>
 *         <li>Причины {@code 0.1 + 0.2 != 0.3} ({@code 0.30000000000000004}): 0.1 не представим в двоичной дроби.</li>
 *         <li>Трёх способах создать {@code BigDecimal}: {@code new BigDecimal(0.1)} vs {@code valueOf(0.1)} vs {@code new BigDecimal("0.1")}.</li>
 *         <li>Округлениях {@code HALF_UP} vs {@code HALF_EVEN} (банковское) на границе {@code 0.005}.</li>
 *         <li>Правиле: деньги — только {@code BigDecimal(String)} + явный {@code RoundingMode}, сравнение через {@code compareTo}.</li>
 *     </ul>
 * </p>
 *
 * @author MeYuugao
 * @version 1.0
 * @since Java 21
 */
public class FloatMoneyDemo {
    private final String inaccuracyResult;
    private final String bigDecimalWaysResult;
    private final String roundingResult;
    private final String comparisonResult;

    public FloatMoneyDemo() {
        inaccuracyResult = testDoubleInaccuracy();
        bigDecimalWaysResult = testBigDecimalWays();
        roundingResult = testRounding();
        comparisonResult = testComparison();
    }

    /**
     * Демонстрирует двоичную неточность {@code double}.
     * <p>
     * Десятичное {@code 0.1} в двоичной системе — бесконечная периодическая дробь
     * (как {@code 1/3} в десятичной), поэтому в 64 битах IEEE 754 хранится ближайшее
     * приближение, чуть больше {@code 0.1}. Отсюда {@code 0.1 + 0.2 == 0.30000000000000004},
     * а не {@code 0.3}. Вывод с 17 знаками показывает ошибку явно.
     * </p>
     */
    private String testDoubleInaccuracy() {
        StringBuilder sb = new StringBuilder();
        sb.append("--- double: 0.1 + 0.2 != 0.3 ---\n");

        double sum = 0.1 + 0.2;
        sb.append("0.1 + 0.2 = %.17f".formatted(sum)).append("\n");
        sb.append("0.3       = %.17f".formatted(0.3)).append("\n");
        sb.append("0.1 + 0.2 == 0.3  -> %b".formatted(sum == 0.3)).append("\n");
        sb.append("Вывод: сравнивать double через == нельзя, только с эпсилон");

        return sb.toString();
    }

    /**
     * Демонстрирует три способа создать {@code BigDecimal} из {@code 0.1}.
     * <p>
     * Правила:
     *     <ol>
     *         <li>{@code new BigDecimal(0.1)} — копирует двоичную ошибку как есть:
     *         получается {@code 0.1000000000000000055511151231257827...}.</li>
     *         <li>{@code BigDecimal.valueOf(0.1)} — внутри вызывает
     *         {@code Double.toString(0.1)} (даёт {@code "0.1"}) и парсит уже строку,
     *         поэтому даёт чистые {@code 0.1}.</li>
     *         <li>{@code new BigDecimal("0.1")} — эталон: парсится десятичная строка,
     *         никакой двоичной ошибки нет изначально.</li>
     *     </ol>
     * Для денег годится только третий вариант (строка), второй — терпим,
     * первый — запрещён.
     * </p>
     */
    private String testBigDecimalWays() {
        StringBuilder sb = new StringBuilder();
        sb.append("--- BigDecimal тремя способами ---\n");

        BigDecimal fromDouble = new BigDecimal(0.1);
        BigDecimal viaValueOf = BigDecimal.valueOf(0.1);
        BigDecimal fromString = new BigDecimal("0.1");

        sb.append("new BigDecimal(0.1)   = %s".formatted(fromDouble)).append("\n");
        sb.append("BigDecimal.valueOf    = %s".formatted(viaValueOf)).append("\n");
        sb.append("new BigDecimal(\"0.1\") = %s".formatted(fromString)).append("\n");

        BigDecimal sumString = new BigDecimal("0.1").add(new BigDecimal("0.2"));
        sb.append("\"0.1\" + \"0.2\" (BigDecimal) = %s; == \"0.3\" -> %b".formatted(sumString, sumString.compareTo(new BigDecimal("0.3")) == 0)).append("\n");
        sb.append("Тот же счёт в MathContext.DECIMAL64: %s".formatted(new BigDecimal("0.1").add(new BigDecimal("0.2"), MathContext.DECIMAL64)));

        return sb.toString();
    }

    /**
     * Демонстрирует округления {@code HALF_UP} и {@code HALF_EVEN}.
     * <p>
     * {@code HALF_UP} — школьное: {@code 2.5 → 3}, {@code 0.005 → 0.01} при двух знаках.
     * {@code HALF_EVEN} (банковское, по умолчанию в IEEE 754 и в {@code BigDecimal.divide}):
     * граница тянется к чётной цифре: {@code 2.5 → 2}, {@code 3.5 → 4},
     * {@code 0.005 → 0.00}, а {@code 0.015 → 0.02}. При тысячах операций ошибка
     * не копится в одну сторону, как у {@code HALF_UP}.
     * </p>
     */
    private String testRounding() {
        StringBuilder sb = new StringBuilder();
        sb.append("--- Округление: HALF_UP vs HALF_EVEN ---\n");

        BigDecimal boundary = new BigDecimal("0.005");
        sb.append("0.005, scale=2 HALF_UP   = %s".formatted(boundary.setScale(2, RoundingMode.HALF_UP))).append("\n");
        sb.append("0.005, scale=2 HALF_EVEN = %s   (к чётной: 0.00)".formatted(boundary.setScale(2, RoundingMode.HALF_EVEN))).append("\n");

        sb.append("2.5, scale=0 HALF_UP   = %s;  HALF_EVEN = %s".formatted(
                new BigDecimal("2.5").setScale(0, RoundingMode.HALF_UP),
                new BigDecimal("2.5").setScale(0, RoundingMode.HALF_EVEN))).append("\n");
        sb.append("3.5, scale=0 HALF_UP   = %s;  HALF_EVEN = %s".formatted(
                new BigDecimal("3.5").setScale(0, RoundingMode.HALF_UP),
                new BigDecimal("3.5").setScale(0, RoundingMode.HALF_EVEN))).append("\n");

        Money price = new Money(new BigDecimal("100.005"));
        sb.append("Money(100.005) банковским -> %s".formatted(price.rounded(RoundingMode.HALF_EVEN)));

        return sb.toString();
    }

    /**
     * Демонстрирует правильное сравнение денежных сумм.
     * <p>
     * Правила:
     *     <ol>
     *         <li>{@code double} через {@code ==} не сравнивают — только
     *         {@code Math.abs(a - b) < 1e-9}.</li>
     *         <li>{@code BigDecimal.equals} учитывает масштаб: {@code 1.0} и
     *         {@code 1.00} через {@code equals} не равны, а через
     *         {@code compareTo == 0} равны. Для денег нужен {@code compareTo}.</li>
     *     </ol>
     * </p>
     */
    private String testComparison() {
        StringBuilder sb = new StringBuilder();
        sb.append("--- Сравнение: == vs epsilon vs compareTo ---\n");

        double a = 0.1 + 0.2;
        sb.append("double: (a == 0.3) = %b;  |a - 0.3| < 1e-9 = %b".formatted(a == 0.3, Math.abs(a - 0.3) < 1e-9)).append("\n");

        BigDecimal one = new BigDecimal("1.0");
        BigDecimal oneZero = new BigDecimal("1.00");
        sb.append("BigDecimal(\"1.0\").equals(\"1.00\") = %b".formatted(one.equals(oneZero))).append("\n");
        sb.append("BigDecimal(\"1.0\").compareTo(\"1.00\") == 0 = %b   (правильно для денег)".formatted(one.compareTo(oneZero) == 0));

        return sb.toString();
    }

    /**
     * Минимальный носитель денежной суммы: хранит точный {@code BigDecimal}
     * и округляет только явным {@code RoundingMode}.
     *
     * @param amount точная сумма, создаётся из {@code String}, не из {@code double}
     */
    record Money(BigDecimal amount) {
        /**
         * Возвращает сумму, округлённую до копеек указанным режимом.
         *
         * @param mode режим округления, для денег обычно {@code HALF_EVEN}
         * @return округлённая сумма с масштабом 2
         */
        BigDecimal rounded(RoundingMode mode) {
            return amount.setScale(2, mode);
        }
    }

    /**
     * Возвращает красиво отформатированный отчёт о всех проведённых тестах.
     * Переопределение метода {@link Object#toString()} позволяет нам просто вызвать {@link System#out#println()} для красивого вывода.
     *
     * @return форматированную строку с демонстрацией float-неточности и BigDecimal.
     */
    @Override
    public String toString() {
        return """
                --- Демонстрация float-неточности и денег ---

                %s
                %s
                %s
                %s
                ---------------------------------------------------
                """.formatted(inaccuracyResult, bigDecimalWaysResult, roundingResult, comparisonResult);
    }
}
