// Объявление класса
class Main {
    // Члены класса
    int foo;

    // Объявление класса
    class Nested {
        // Члены класса

        // Поле
        int foo;
        // Поле
        long bar;
        // Поле
        short baz;
        // Поле
        double foobar;

        // Метод
        void method1() {}

        // Метод
        int method2() {
            // Возврат из метода
            return 1;
        }
    }

    long bar;

    // Метод
    void bar() {}

    void main() {
        // Составной оператор
        {
            // Объявление переменной
            int foo;
            // Объявление переменной
            long bar;
            // Объявление переменной
            short baz;
            // Объявление переменной
            double foobar;
        }
        // Пустые операторы
        ;;;;;;;;;
        // Определение переменной
        int foo = 5;
        // Определение переменной
        long bar = 252525353213134;
        // Определение переменной
        short baz = 15;
        // Определение переменной
        double foobar = 0.1E1;

        // Оператор switch
        switch (foo) {
            // Метка ветви switch
            case 1:
                int inSwitch = 1;
                int inSwitch2 = 1;
                // Тело ветви switch
                // Метка ветви switch
            case 2:
                // Тело ветви switch
                // Присваивание
                int inSwitch = 1;
                int inSwitch2 = 1;
                foo = 5;
                // Метка ветви switch
            case 3:
                // Тело ветви switch
                // Присваивание
                foo = 10;
                // Метка ветви switch
            case 4:
                // Тело ветви switch
                // Оператор break
                break;
            // Метка ветви switch
            case 5: /*оператор break*/ break;
            default:
        }

        // Определение переменной
        int foobarbaz = /* унарный плюс */ +10;
        foo = foobarbaz = /* унарный минус */ -15;

        // Равенство
        short pseudoboolean1 = foo == foobarbaz;
        // Неравенство
        short pseudoboolean2 = foo != foobarbaz;
        // Логические И и ИЛИ
        short pseudoboolean3 = !(pseudoboolean1 || pseudoboolean2) || (pseudoboolean1 && !pseudoboolean2);

        // Операции сравнения
        short pb1 = 5 > 5;
        short pb2 = 7 < 8;
        short pb3 = 8 >= 6;
        short pb4 = 4 <= 4;

        // Арифметические операции (аддитивные и мультипликативные)
        foo = (5 + 5 - 9) * 45;
        // Инкремент (префиксный, постфиксный)
        foo++;
        ++foo;
        // Декремент (префиксный, постфиксный)
        --foo;
        foo--;
        // Деление
        foobar = foobar / 10;

        // Доступ по идентификатору (оператор-выражение);
        foobar;
        // Доступ к полю
        foo = Main.Nested.foo;
        // Вызов метода объекта
        Main.Nested.method1();
        // Вызов метода текущего объекта
        bar();
    }
}