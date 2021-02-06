class Main {
    int x = 10;

    // Без вызова код в методе не выполняется
    void foo() {
        x = 20;
        x++;
        x++;
        x--;
    }

    // Без вызова код в методе не выполняется
    void bar() {
        x = 30;
        x++;
        x++;
        x--;
    }

    void main() {
        println(x);
    }
}