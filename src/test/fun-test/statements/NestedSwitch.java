class Main {
    int x = 0;

    void nestedSwitch() {
        switch (1) {
            case 1:
                switch (x) {
                    default:
                        return;
                }
                x = 100;
        }
        x = 10;
    }

    void main() {
        nestedSwitch();
    }
}