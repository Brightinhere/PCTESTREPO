import java.util.concurrent.*;

import static java.lang.Thread.*;

/**
 * Voorbeeld programma waarmee Amdahl's law aanschouwelijk kan worden gemaakt
 * @author henk
 */
public class Amdahl {

    private static final double ALPHA = 0.8;
    private static final int CYCLE = 500;
    private static final int CORE = 2;
    private static final int SIZE = 30;

    private static void serial() {
        try {
            sleep(Math.round((1 - ALPHA) * CYCLE));
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    private static void concur() {
        try {
            sleep(Math.round(ALPHA * CYCLE / CORE));
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    static class Worker implements Runnable {

        private static int i = 0;
        private static CyclicBarrier barrier = new CyclicBarrier(CORE, () -> {
            serial();
            i++;
        });

        public void run() {
            while (i < SIZE) {
                concur();
                try {
                    barrier.await();
                } catch (InterruptedException | BrokenBarrierException ignored) {
                }
            }
        }
    }

    private static void concurrent() throws InterruptedException {
        Thread[] threads = new Thread[CORE];
        for (int i = 0; i < CORE; i++) {
            threads[i] = new Thread(new Worker());
            threads[i].start();
        }
        for (int i = 0; i < CORE; i++) threads[i].join();
    }

    public static void main(String[] args) throws InterruptedException {
        long start = System.nanoTime();
        concurrent();
        System.out.println(((System.nanoTime() - start) / 1E9) + " sec.");
    }

}
