class Main {
    int x = 0;

    void fallthrough() {
        switch (x) {
            case 0:
                println(10);
            case 1:
                println(20);
            case 2:
                println(30);
            default:
                println(40);
        }
        println();
    }

    void main() {
        x = 0;
        fallthrough();

        x = 1;
        fallthrough();

        x = 2;
        fallthrough();

        x = 3;
        fallthrough();
    }
}