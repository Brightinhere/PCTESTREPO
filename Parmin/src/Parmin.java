import java.util.*;

public class Parmin {

    private static final int SEED = 0;
    private static final int CORE = 4;
    private static final int SIZE = 200000000;

    private static int[] array = new int[SIZE];
    private static int minimum = Integer.MAX_VALUE;

    private static void fillArray(int[] array) {
        Random rand = new Random(SEED);
        for (int i = 0; i < SIZE; i++) array[i] = rand.nextInt(SIZE);
        array[rand.nextInt(SIZE)] = -1;
    }

    private static synchronized void update(int minimum) {
        if (minimum < Parmin.minimum) {
            Parmin.minimum = minimum;
        }
    }

    private static void sequentialMinimum(int[] array) {
        int minimum = Integer.MAX_VALUE;
        for (int i = 0; i < SIZE; i++) {
            if (array[i] < minimum) {
                minimum = array[i];
            }
        }
        update(minimum);
    }

    static class Worker implements Runnable {

        private int id;
        private int[] array;

        private Worker(int[] array, int id) {
            this.id = id;
            this.array = array;
        }

        public void run() {
            int minimum = Integer.MAX_VALUE;
            for (int j = id; j < SIZE; j += CORE) {
                if (array[j] < minimum) {
                    minimum = array[j];
                }
            }
            update(minimum);
        }
    }

    private static void concurrentMinimum(int[] array) throws InterruptedException {
        List<Thread> threads = new ArrayList<>(CORE);
        for (int i = 0; i < CORE; i++) {
            Thread thread = new Thread(new Worker(array, i));
            threads.add(thread);
            thread.start();
        }
        for (int i = 0; i < CORE; i++) threads.get(i).join();
    }

    public static void main(String[] args) throws InterruptedException {
        fillArray(array);
        int [] copy = Arrays.copyOf(array, SIZE);
        long start = System.nanoTime();
        sequentialMinimum(copy);
        System.out.println(((System.nanoTime() - start) / 1E9) + " sec.");
        copy = Arrays.copyOf(array, SIZE);
        start = System.nanoTime();
        concurrentMinimum(copy);
        System.out.println(((System.nanoTime() - start) / 1E9) + " sec.");
        assert(minimum == -1);
    }
}
