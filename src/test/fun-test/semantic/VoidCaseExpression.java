class Main {
    void foo() {}

    void main() {
        switch (10) {
            case foo(): break;
        }
    }
}