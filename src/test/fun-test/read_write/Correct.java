class Main {
    class Foo {
        // Поле вложенного класа
        long bar;
    }

    // Поля класса
    long ba = 1000;
    long boo = ba;

    void main() {
        println(boo);
        println();

        long foo;
        int bar;
        short baz;

        // Составной оператор
        {
            long foo;
            int bar;
            short baz;
            foo = bar = baz = 25;
            println(foo);
            println(bar);
            println(baz);
            println();
        }

        // Вне составного оператора
        println(foo);
        println(bar);
        println(baz);
        println();

        // Цепочка присваиваний
        boo = Main.ba = foo = bar = baz = 10;
        println(foo);
        println(bar);
        println(baz);
        println(boo);
        println(ba);
        println();

        // Присваивание по отдельности
        foo = 10;
        bar = 20;
        baz = 30;
        boo = 40;
        Main.ba = 50;

        println(foo);
        println(bar);
        println(baz);
        println(boo);
        println(ba);
        println();

        // Присваивание полю вложенного класса
        Main.Foo.bar = foo;
        println(Main.Foo.bar);
    }
}