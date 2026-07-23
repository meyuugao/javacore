package me.yuugao.lesson01;

import me.yuugao.lesson01.demo.PassByValueDemo;
import me.yuugao.lesson01.demo.TypesDemo;

public class Lesson01 {
    public static void run() {
        TypesDemo typesDemo = new TypesDemo();
        System.out.println(typesDemo);

        PassByValueDemo passByValueDemo = new PassByValueDemo();
        System.out.println(passByValueDemo);
    }
}