class Main {
    // Вложенный класс
    class Nested {
        // Поля
        int foo;
        long bar;
        short baz;
        double foobar;

        // Методы
        void method1() {
            int foo = 1;
            return;
        }

        int method2() {
            // Возврат из метода
            return 1;
        }
    }

    // Метод без явного return
    void bar() {}

    // Неявное приведение литеральных значений в return
    short baz() { return 1; }

    // Поля
    int x;
    int foo;
    long bar;
    short baz;
    double foobar;

    // Главный метод
    void main() {
        // x в новой области видимости
        int x;
        // Составной оператор
        {
            // x в новой области видимости
            int x;
            double foo;
            short bar;
            long baz;
            int foobar;
        }

        int foo = 5;
        long bar = 252525353213;
        short baz = 15;
        double foobar = 0.1E1;

        // Оператор switch
        switch (foo) {
            case 1:
                int x = 0;
            case 2:
                int x;
                foo = 5;
            case 3:
                foo = 10;
            case 4:
                break;
            case 5: break;
            default:
        }

        switch (Main.Nested.method2()) {
            case 0:
            default:
        }

        // Унарные операции
        int foobarbaz = +10;
        foo = foobarbaz = -15;

        // Логические операции
        short pseudoboolean1 = foo == foobarbaz;
        short pseudoboolean2 = foo != foobarbaz;
        short pseudoboolean3 = !(pseudoboolean1 || pseudoboolean2) || (pseudoboolean1 && !pseudoboolean2);

        // Операции сравнения
        short pb1 = 5 > 5;
        short pb2 = 7 < 8;
        short pb3 = 8 >= 6;
        short pb4 = 4 <= 4;

        // Арифметические операции (аддитивные и мультипликативные)
        foo = (5 + 5 - 9) * 45;
        // Инкремент (префиксный, постфиксный)
        int a = foo++;
        int b = ++foo;
        // Декремент (префиксный, постфиксный)
        int c = --foo;
        int d = foo--;
        // Деление
        foobar = foobar / 10;

        // Доступ по идентификатору (оператор-выражение);
        foobar;
        // Доступ к полю
        foo = Main.Nested.foo;
        bar = Main.Nested.bar;
        baz = Main.Nested.baz;
        foobar = Main.Nested.foobar;

        // Вызов метода объекта
        Main.Nested.method1();
        foo = Main.Nested.method2();

        // Вызов метода текущего объекта
        bar();
        baz = baz();
    }
}