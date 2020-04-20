import java.util.Random;

public class BoundedBufferMain {
    static final int MAX_ITEMS = 100000;
    static final int BUFFER_SIZE = 10000;
    static BoundedBuffer<Double> buffer = new BoundedBuffer(BUFFER_SIZE);

    static Random randomizer = new Random();

    private static void producer(int nItems) {
        double sum = 0;
        for (int i = 0; i < nItems; i++) {
            double item = randomizer.nextDouble();
            sum += item;
            buffer.put(item);
        }
        System.out.printf("Produced %d items with total value %f\n",
                nItems, sum);
    }

    private static void consumer(int nItems) {
        double sum = 0;
        for (int i = 0; i < nItems; i++) {
            sum += buffer.take();
        }
        System.out.printf("Consumed %d items with total value %f\n",
                nItems, sum);
    }

    public static void main(String[] args) throws InterruptedException {
        Thread producer1 = new Thread(() -> producer(MAX_ITEMS));
        Thread producer2 = new Thread(() -> producer(2*MAX_ITEMS));
        Thread producer3 = new Thread(() -> producer(3*MAX_ITEMS));
        Thread consumer1 = new Thread(() -> consumer(3*MAX_ITEMS));
        Thread consumer2 = new Thread(() -> consumer(3*MAX_ITEMS));

        // force a java garbage collection, to clean up clutter memory usage
        System.gc();
        // start the timer
        long start = System.nanoTime();
        producer1.start(); producer2.start(); producer3.start();
        consumer1.start(); consumer2.start();

        producer1.join(); producer2.join(); producer3.join();
        consumer1.join(); consumer2.join();
        System.out.printf("Elapsed time = %.3f ms\n", ((System.nanoTime() - start) / 1E6));
    }

}
