class Main {
    int x = 0;

    void switchTest() {
        switch (x) {
            case 0:
                println(10);
            case 1:
                println(20);
                break;
            case 2:
                println(30);
            default:
                println(40);
                break;
        }
        println();
    }

    void main() {
        x = 0;
        switchTest();

        x = 1;
        switchTest();

        x = 2;
        switchTest();

        x = 3;
        switchTest();
    }
}