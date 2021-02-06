class Main {
    int bar() {
        return 9;
    }

    double foo() {
        return bar();
    }

    void main() {
        double a = foo() / 2;

        double b = 5;
        b = b / 2;

        // Вернет 2.0, т.к. 5/2 - целочисленное деление
        double c = 5 / 2;

        // Вернет 2.5, т.к. один из операндов - число с плавающей точкой.
        double d = 5.E0 / 2;

        // Вернет 2.5 по той же причине
        d = 5 / 2.E0;
    }
}