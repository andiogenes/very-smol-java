class Main {
    class A {
        int foo() { return 0; }
        class B {
            int foo() { return 1; }
            class C {}
        }
    }

    void main() {
        int foo = Main.A.foo(); // found
        int foo2 = Main.A.B.foo(); // found
        int foo3 = Main.A.B.C.foo(); // not found
    }
}