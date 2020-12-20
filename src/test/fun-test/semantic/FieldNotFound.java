class Main {
    class A {
        int foo;
        class B {
            int foo;
            class C {}
        }
    }

    void main() {
        int foo = Main.A.foo; // found
        int foo2 = Main.A.B.foo; // found
        int foo3 = Main.A.B.C.foo; // not found
    }
}