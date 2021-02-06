class Main {
    // Должен сработать первый return
    int foo() {
        return 1;
        return 2;
        return 3;
        return 4;
    }

    int x;

    // return в условных операторах
    double bar() {
        switch (x) {
            case 0:
                return 1.E0;
            default:
                return 2.E0;
        }
        return 0.E0;
    }

    // приведение типов в return
    double cast() { return 9; }

    void main() {
        int a = foo();

        x = 0;
        double b = bar();

        x = 1;
        double c = bar();

        double d = cast() / 2;
    }
}