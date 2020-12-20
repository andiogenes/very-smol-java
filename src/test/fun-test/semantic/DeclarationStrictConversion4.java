class Main {
    void main() {
        short x = 0;
        // "расширяющее" преобразование, допустимо
        int y = x;
        // "сужающее" преобразование, недопустимо
        long foo = 0;
        int bar = foo;
    }
}