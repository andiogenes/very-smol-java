class Main {
    int foo = 10;
    int foo2 = 10;
    double boo = 10.E01;

    int bar() {
        int foo = 1;
        println(foo);
        return foo;
    }

    double baz() { return 0.E0; }

    void lolbutz() {
        foo = foo2 = 100 / 2 + 5 * 10 - 8;
    }

    void foobar() {
        println(foo = foo || 0);
    }

    void main() {
        switch (2) {
            case 1:
                println(1);
            case 2:
                println(2);
            case 3:
                println(3);
            default:
                println(4);
        }

        return;
        println(boo++);
        println(boo);
        println(boo--);
        println(boo);
        println();

        println(foo);
        println(foo++);
        println(foo);
        println();
        println(foo2);
        println(++foo2);
        println(foo2);
        println();
        lolbutz();
        println(foo);
        println(foo2);
        bar();
        int foo = 10;
        {
            int foo = 20;
            println(foo);
        }
        println(foo);
        println(42.1E0);
        foobar();
    }
}