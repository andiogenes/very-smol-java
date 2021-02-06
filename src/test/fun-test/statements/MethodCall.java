class Main {
    int x = 0;

    void foo() {
        println(3);
        x = x + 10;
    }

    void bar() {
        println(2);
        foo();
        x = x + 10;
    }

    void baz() {
        println(1);
        bar();
        x = x + 10;
    }

    void main() {
        baz();
        println(x);
    }
}