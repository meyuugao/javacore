package me.yuugao.lesson01.demo;

/**
 * Класс {@code me.yuugao.lesson01.demo.TypesDemo} демонстрирует размер и диапазоны примитивных типов данных в Java.
 * <p>
 * В Java существуют 8 примитивных типов.
 * Они не являются объектами и хранят в памяти напрямую (а если это локальные переменные - в стеке).
 * Этот класс хранит константы, отражающие минимальные и максимальные значения каждого типа для наглядного отражения их границ.
 * </p>
 *
 * <ul>
 *     <li><b>byte</b> - 1 байт (8 бит)</li>
 *     <li><b>short</b> - 2 байта (16 бит)</li>
 *     <li><b>int</b> - 4 байта (32 бита)</li>
 *     <li><b>long</b> - 8 байт (64 бита)</li>
 *     <li><b>float</b> - 4 байта (32 бита, IEEE 754)</li>
 * </ul>
 *
 * @author MeYuugao
 * @version 1.0
 * @since Java 21
 */
public class TypesDemo {
    private final byte minByte = -128;
    private final byte maxByte = 127;

    private final short minShort = -32768;
    private final short maxShort = 32767;

    private final int minInt = -2147483648;
    private final int maxInt = 2147483647;

    private final long minLong = -9_223_372_036_854_775_808L;
    private final long maxLong = 9_223_372_036_854_775_807L;

    private final float minFloat = 1.4E-45F;
    private final float maxFloat = 3.4028235E38F;

    /**
     * Возвращает красиво отформатированный отчёт о всех проведённых тестах.
     * Переопределение метода {@link Object#toString()} позволяет нам просто вызвать System.out.println(types) для красивого вывода.
     *
     * @return форматированную строку с диапазонами типов данных.
     */
    @Override
    public String toString() {
        return """
                 --- Размеры и диапазоны примитивных типов Java ---
                \s
                 [byte]  (1 байт | 8 бит)
                   Min: %d
                   Max: %d
                  \s
                 [short] (2 байта | 16 бит)
                   Min: %d
                   Max: %d
                  \s
                 [int]   (4 байта | 32 бита)
                   Min: %,d
                   Max: %,d
                  \s
                 [long]  (8 байт | 64 бита)
                   Min: %,d
                   Max: %,d
                  \s
                 [float] (4 байта | 32 бита, IEEE 754)
                   Min: %f
                   Max: %f
                  \s
                 -----------------------------------------------
                \s""".formatted(
                minByte, maxByte,
                minShort, maxShort,
                minInt, maxInt,
                minLong, maxLong,
                minFloat, maxFloat
        );
    }
}