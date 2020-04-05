import java.util.*;
import java.util.concurrent.*;

public class Recmin {

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
        if (minimum < Recmin.minimum) {
            Recmin.minimum = minimum;
        }
    }

    private static class Worker extends RecursiveAction {

        private int lo, hi;
        private int[] array;

        private Worker(int[] array, int lo, int hi) {
            this.lo = lo;
            this.hi = hi;
            this.array = array;
        }

        protected void compute() {
            if (hi - lo > 2) {
                int mi = lo + (hi - lo) / 2;
                RecursiveAction los = new Worker(array, lo, mi);
                RecursiveAction his = new Worker(array, mi, hi);
                invokeAll(los, his);
            } else {
                update(Math.min(array[lo], array[hi - 1]));
            }
        }
    }

    private static void recursiveMinimum(int[] array) {
        ForkJoinPool pool = new ForkJoinPool(CORE);
        Worker root = new Worker(array, 0, array.length - 1);
        pool.invoke(root);}

    public static void main(String[] args) throws InterruptedException {
        fillArray(array);
        long start = System.nanoTime();
        recursiveMinimum(array);
        System.out.println(((System.nanoTime() - start) / 1E9) + " sec. ");
        assert minimum == -1;
    }

}
