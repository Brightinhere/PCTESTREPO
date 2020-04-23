import java.util.Random;

import static java.lang.Thread.*;

/**
 * Demo program showing the use of wait and notify to create bounded buffer
 *
 * @author henk
 *
 */
public class Buffer {

    private static class Warehouse {

        private final int SEED = 0;
        private final int SIZE = 100;
        private volatile int buffer = 0;
        private final Random RANDOM = new Random(SEED);

        private void produce() {
            try {
                sleep(500);
                synchronized (this) {
                    int amount = 1 + RANDOM.nextInt(SIZE / 2);
                    while (buffer + amount > SIZE) {
                        wait();
                    }
                    buffer += amount;
                    System.out.println("Produced : " + amount + " buffer: " + buffer);
                    notify();
                }
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }

        private void consume() {
            try {
                sleep(500);
                synchronized (this) {
                    int amount = 1 + RANDOM.nextInt(SIZE / 2);
                    while (buffer - amount < 0) {
                        wait();
                    }
                    buffer -= amount;
                    System.out.println("Consumed : " + amount + " buffer: " + buffer);
                    notify();
                }
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    public static void main(String[] args) {
        Warehouse warehouse = new Warehouse();
        new Thread(() -> {while (true) warehouse.produce();}).start();
        new Thread(() -> {while (true) warehouse.consume();}).start();
    }

}
