class Main {
    int param;

    int factorial() {
        switch (param <= 1) {
            case 1: return 1;
        }
        int term = param;
        param = param - 1;
        return term * factorial();
    }

    void main() {
        param = 7;
        int result = factorial(); // 7! = 5040
        println(result);
    }
}