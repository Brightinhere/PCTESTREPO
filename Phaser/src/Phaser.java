import java.util.concurrent.*;

public class Phaser {

    private static final int CORE = 4;
    private static final int SIZE = 100000;

    static class Worker implements Runnable {

        private static java.util.concurrent.Phaser phaser = new java.util.concurrent.Phaser(CORE);

        public void run() {
            while (phaser.getPhase() < SIZE) {
                // work
                phaser.arriveAndAwaitAdvance();
            }
        }
    }

    private static void concurrentWork() throws InterruptedException {
        Thread[] threads = new Thread[CORE];
        for (int i = 0; i < CORE; i++) {
            threads[i] = new Thread(new Worker());
            threads[i].start();
        }
        for (int i = 0; i < CORE; i++) threads[i].join();
    }

    public static void main(String[] args) throws InterruptedException {
        long start = System.nanoTime();
        concurrentWork();
        System.out.println(((System.nanoTime() - start) / 1E9) + " sec.");
    }
}
