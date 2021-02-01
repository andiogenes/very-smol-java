class Main {
    void main() {
        // Инкремент-декремент
        {
            int foo = 0;
            println(foo++); // 0
            println(foo);   // 1
            println();

            println(foo--); // 1
            println(foo);   // 0
            println();

            foo = 0;
            println(++foo); // 1
            println(foo);   // 1
            println();

            println(--foo); // 0
            println(foo);   // 0
            println();
        }

        // Унарный плюс-минус
        println(+10);       // 10
        println(+0.1E1);    // 1.0
        println();

        println(-10);       // -10
        println(-0.1E1);    // -1.0
        println();

        // Отрицание
        println(!10);       // 0
        println(!20);       // 0
        println(!(-5));     // 0
        println(!0);        // 1
        println();

        // Умножение
        println(3 * -5);            // -15
        println(3 * 0);             // 0
        println(1 * 2 * 3 * 4);     // 24
        println(10 * 0.5E0);        // 5.0
        println(0.5E0 * 10);        // 5.0
        println();

        // Деление
        println(5 / 2);         // 2
        println(5.E0 / 2);      // 2.5
        println(0 / 5.E0);      // 0.0
        println(5 / -2);        // -2
        println(-5 / 2);        // -2
        println(-5 / -2);       // 2
        println();

        // Сложение
        println(1 + 2);         // 3
        println(1 + -2);        // -1
        println(1 + 1.E0);      // 2.0
        println();

        // Вычитание
        println(1 - 2);         // -1
        println(-1 - -2);       // 1
        println(1 - 0.E0);      // 1.0
        println(1.E0 - 0);      // 1.0
        println();

        // Сравнение
        println(1.E0 > 0);  // 1
        println(1 > 0.E0);  // 1
        println(1 > 0);     // 1
        println(1 > 1);     // 0
        println(1 >= 1);    // 1
        println(1 >= 2);    // 0
        println(1 > 2);     // 0
        println();

        println(1.E0 < 0);  // 0
        println(1 < 0.E0);  // 0
        println(1 < 0);     // 0
        println(1 < 1);     // 0
        println(1 <= 1);    // 1
        println(1 <= 2);    // 1
        println(1 < 2);     // 1
        println();

        println(0 == 1);    // 0
        println(0 != 1);    // 1
        println(0 == 0);    // 1
        println(0 == 0.E0); // 1
        println();

        // Логическое И
        println(1 && 0);    // 0
        println(0 && 1);    // 0
        println(1 && 1);    // 1
        println(0 && 0);    // 0
        println(10 && 1);   // 1
        println();

        // Логическое ИЛИ
        println(1 || 0);    // 1
        println(0 || 1);    // 1
        println(1 || 1);    // 1
        println(0 || 0);    // 0
        println(10 || 1);   // 1
        println();

        // Сложные выражения
        println((1 || 0) && (0 || 1) || 0); // 1
        println((3 > 2) && (3 <= 3) == 1);  // 1
        println(-(5 * 8 + 32 / 2));         // -56
        println(5 * 8 + 32 / 2 + 1 / 3.E0); // 56.333...
        println();
    }
}