class Main {
    int foo() { return 0; }
    void bar() {}

    void main() {
        switch (foo()) {
            case 0: break;
        }
        switch (bar()) {
            default: break;
        }
    }
}