import java.util.concurrent.*;

public class Barrier {

    private static final int CORE = 4;
    private static final int SIZE = 100000;

    static class Worker implements Runnable {

        private static int i = 0;
        private static CyclicBarrier barrier = new CyclicBarrier(CORE, () -> i++);

        public void run() {
            while (i < SIZE) {
                // work
                try {
                    barrier.await();
                } catch (InterruptedException | BrokenBarrierException ex) {
                    return;
                }
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
