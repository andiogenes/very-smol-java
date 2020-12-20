class Main {
    void main() {
        short x;
        int y;
        // "расширяющее" преобразование, допустимо
        y = x;
        // "сужающее" преобразование, недопустимо
        long foo;
        int bar;
        bar = foo;
    }
}