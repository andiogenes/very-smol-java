class Main {
    int field = (10 + 5 * 4 - 2) / 6;   // 4

    int method() {
        return 1;
    }

    void counting() {
        // Инкремент-декремент
        int foo = 0;
        int post_incr = foo++;   // 0
        post_incr = foo;         // 1

        int post_decr = foo--; // 1
        post_decr = foo;       // 0

        foo = 0;
        int pre_incr = ++foo; // 1
        pre_incr = foo;       // 1

        int pre_decr = --foo; // 0
        pre_decr = foo;       // 0
    }

    void unaryPlus() {
        // Унарный плюс
        int plus1 = +10;          // 10
        double plus2 = +0.1E1;    // 1.0
    }

    void unaryMinus() {
        // Унарный минус
        int minus1 = -10;          // -10
        double minus2 = -0.1E1;    // -1.0
    }

    void neg() {
        // Отрицание
        short neg1 = !10;       // 0
        short neg2 = !20;       // 0
        short neg3 = !(-5);     // 0
        short neg4 = !0;        // 1
    }

    void mul() {
        // Умножение
        int mul1 = 3 * -5;            // -15
        int mul2 = 3 * 0;             // 0
        int mul3 = 1 * 2 * 3 * 4;     // 24
        double mul4 = 10 * 0.5E0;     // 5.0
        double mul5 = 0.5E0 * 10;     // 5.0
    }

    void div() {
        // Деление
        int div1 = 5 / 2;         // 2
        double div2 = 5.E0 / 2;   // 2.5
        double div3 = 0 / 5.E0;   // 0.0
        int div4 = 5 / -2;        // -2
        int div5 = -5 / 2;        // -2
        int div6 = -5 / -2;       // 2
    }

    void add() {
        // Сложение
        int add1 = 1 + 2;            // 3
        int add2 = 1 + -2;           // -1
        double add3 = 1 + 1.E0;      // 2.0
    }

    void sub() {
        // Вычитание
        int sub1 = 1 - 2;           // -1
        int sub2 = -1 - -2;         // 1
        double sub3 = 1 - 0.E0;     // 1.0
        double sub4 = 1.E0 - 0;     // 1.0
    }

    void comparison() {
        // Сравнение
        short comparison1 = 1.E0 > 0;  // 1
        short comparison2 = 1 > 0.E0;  // 1
        short comparison3 = 1 > 0;     // 1
        short comparison4 = 1 > 1;     // 0
        short comparison5 = 1 >= 1;    // 1
        short comparison6 = 1 >= 2;    // 0
        short comparison7 = 1 > 2;     // 0

        short comparison8 = 1.E0 < 0;   // 0
        short comparison9 = 1 < 0.E0;   // 0
        short comparison10 = 1 < 0;     // 0
        short comparison11 = 1 < 1;     // 0
        short comparison12 = 1 <= 1;    // 1
        short comparison13 = 1 <= 2;    // 1
        short comparison14 = 1 < 2;     // 1

        short comparison15 = 0 == 1;    // 0
        short comparison16 = 0 != 1;    // 1
        short comparison17 = 0 == 0;    // 1
        short comparison18 = 0 == 0.E0; // 1
    }

    void and() {
        // Логическое И
        short and1 = 1 && 0;    // 0
        short and2 = 0 && 1;    // 0
        short and3 = 1 && 1;    // 1
        short and4 = 0 && 0;    // 0
        short and5 = 10 && 1;   // 1
    }

    void or() {
        // Логическое ИЛИ
        short or1 = 1 || 0;    // 1
        short or2 = 0 || 1;    // 1
        short or3 = 1 || 1;    // 1
        short or4 = 0 || 0;    // 0
        short or5 = 10 || 1;   // 1
    }

    void complex() {
        // Сложные выражения
        short complex1 = (1 || 0) && (0 || 1) || 0;                 // 1
        short complex2 = (3 > 2) && (3 <= 3) == 1;                  // 1
        int complex3 = -(5 * 8 + 32 / 2);                           // -56
        double complex4 = 5 * 8 + 32 / 2 + 1 / 3.E0;                // 56.333...
        double complex5 = (complex3 + complex4) * complex2 + field; // 4.333...
        complex5 = complex5 * method();                             // 4.333...
    }

    void main() {
        counting();
        unaryPlus();
        unaryMinus();
        neg();
        mul();
        div();
        add();
        sub();
        comparison();
        and();
        or();
        complex();
    }
}