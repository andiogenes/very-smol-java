class Main {
    class Nested {
        int bar;
        int baz;
    }

    int bar() {
        return 10;
    }

    int x = bar();

    int foo() {
        return bar() * bar();
    }

    int y = foo() * bar();

    void main() {
        int z = x = y = 45;

        println(); println();

        Main.Nested.bar = Main.Nested.baz = x = y = z = foo() * bar();

        {
            int y = x = z = 45;
        }
        x = x;
        y = y;
        z = z;
    }
}