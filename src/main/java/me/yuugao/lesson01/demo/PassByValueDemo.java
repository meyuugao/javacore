package me.yuugao.lesson01.demo;

/**
 * Класс {@code me.yuugao.lesson01.demo.PassByValueDemo} наглядно демонстрирует главное правило java:
 * передача параметров всегда происходит <b>по назначению (pass-by-value)</b>.
 * <p>
 * Для примитивов копируется само значение, а для объектов - копия ссылки на объект в куче (heap).
 * Внутри класса проведены 3 эксперимента:
 *     <ol>
 *         <li>Попытка изменить примитив (ожидаемо провалится).</li>
 *         <li>Мутация (изменение состояние) объекта по переданной ссылке (ожидаемо сработает).</li>
 *         <li>Попытка перенаправить ссылку на новый объект (ожидаемо провалится).</li>
 *     </ol>
 * </p>
 *
 * @author MeYuugao
 * @version 1.0
 * @since Java 21
 */
public class PassByValueDemo {
    private final String firstResult;
    private final String secondResult;
    private final String thirdResult;

    public PassByValueDemo() {
        firstResult = first();
        secondResult = second();
        thirdResult = third();
    }

    private String first() {
        int i = 0;
        StringBuilder sb = new StringBuilder();
        sb.append("До метода: %d".formatted(i)).append("\n");

        modifyPrimitive(i);

        sb.append("После метода: %d".formatted(i));
        return sb.toString();
    }

    private void modifyPrimitive(int i) {
        i = 1;
    }

    private String second() {
        Wallet wallet = new Wallet(0);
        StringBuilder sb = new StringBuilder();
        sb.append("До метода: %s".formatted(wallet)).append("\n");

        mutateObject(wallet);

        sb.append("После метода: %s".formatted(wallet));
        return sb.toString();
    }

    private void mutateObject(Wallet wallet) {
        wallet.addMoney(100);
    }

    private String third() {
        Wallet wallet = new Wallet(0);
        StringBuilder sb = new StringBuilder();
        sb.append("До метода: %s".formatted(wallet)).append("\n");

        reassignObject(wallet);

        sb.append("После метода: %s".formatted(wallet));
        return sb.toString();
    }

    private void reassignObject(Wallet wallet) {
        wallet = new Wallet(100);
    }

    /**
     * Возвращает красиво отформатированный отчёт о всех проведённых тестах.
     * Переопределение метода {@link Object#toString()} позволяет нам просто вызвать {@link System#out#println()} для красивого вывода.
     *
     * @return форматированную строку с демонстрацией Pass-By-Value.
     */
    @Override
    public String toString() {
        return """
                 --- Демонстрация Pass-By-Value в Java ---
                \s
                 [Эксперимент 1: Передача примитива (int)]
                   %s
                   Вывод: Примитив не изменился. В метод передалась копия значения.
                  \s
                 [Эксперимент 2: Мутация объекта (Wallet)]
                   %s
                   Вывод: Объект изменился! В метод передалась копия ссылки на тот же объект.
                  \s
                 [Эксперимент 3: Переприсвоение ссылки (Wallet)]
                   %s
                   Вывод: Оригинал не изменился. Мы поменяли только локальную копию ссылки.
                \s
                 ---------------------------------------------------
                \s""".formatted(firstResult, secondResult, thirdResult);
    }

    static class Wallet {
        private int money;

        public Wallet(int money) {
            this.money = money;
        }

        public void addMoney(int amount) {
            money += amount;
        }

        @Override
        public String toString() {
            return "Wallet {money=%s}".formatted(money);
        }
    }
}