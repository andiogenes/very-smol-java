class Main {
    int x = 0;

    int firstReturn() {
        return 1;
        return 2;
        return 3;
        return 4;
    }

    double switchReturn() {
        switch (x) {
            case 0:
                x = 1;
                return 12.1E0;
            case 1:
                x = 2;
                return 45.2E0;
        }
        return 1.5E0;
    }

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
        println(firstReturn());
        println();

        println(switchReturn());
        println(switchReturn());
        println(switchReturn());
        println();

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