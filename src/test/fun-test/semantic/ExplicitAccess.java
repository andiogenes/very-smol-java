class Main {
    class A {
        class B {
            class C {
                void foo() {}
            }
        }
    }

    void main() {
        // Полная цепочка доступа (явная) - допустима
        Main.A.B.C.foo();
        // Неполная цепочка доступа (неявная) - недопустима
        A.B.C.foo();
    }
}