import java.util.concurrent.Semaphore;

public class Semaphores {

    private static final long MAX_ITEMS = 100000L;

    public static void main(String[] args) throws InterruptedException {
        Thread producer = new Thread(() -> {
            System.out.printf("producing items worth=%d\n",
                    MAX_ITEMS * (MAX_ITEMS-1) / 2);
            for (int i = 0; i < MAX_ITEMS; i++) {
                produce(i);
            }
        });
        Thread consumer = new Thread(() -> {
            long sum = 0L;
            for (int i = 0; i < MAX_ITEMS; i++) {
                sum += consume();
            }
            System.out.printf("consumes items worth=%d\n", sum );
        });

        // force a java garbage collection, to clean up clutter memory usage
        System.gc();
        // start the timer
        long start = System.nanoTime();
        producer.start();
        consumer.start();
        producer.join();
        consumer.join();
        System.out.printf("Elapsed time = %.3f ms\n", ((System.nanoTime() - start) / 1E6));
    }

    // play with the buffersize and investigate performance impact
    private static final int BUFFER_SIZE = 10;
    private static int buffer[] = new int[BUFFER_SIZE];
    private static int head = 0, tail = 0;
    private static Semaphore freeSpace = new Semaphore(BUFFER_SIZE);
    private static Semaphore usedSpace = new Semaphore(0);

    private static void produce(int item) {
        freeSpace.acquireUninterruptibly();
        buffer[head] = item;
        head = (head+1) % BUFFER_SIZE;
        usedSpace.release();
    }

    private static int consume() {
        usedSpace.acquireUninterruptibly();
        int item = buffer[tail];
        tail = (tail+1) % BUFFER_SIZE;
        freeSpace.release();
        return item;
    }

    private static int count = 0;

    private static
    Semaphore mutex = new Semaphore(1);

    private void increment()
            throws InterruptedException {
        // wait for exclusive access
        mutex.acquire();
        count++;
        // release exclusive access
        mutex.release();
    }

    private static
    Semaphore mutex1 = new Semaphore(1),
              mutex2 = new Semaphore(1);

    private void increment2()
            throws InterruptedException {
        // wait for exclusive access
        mutex1.acquire(); mutex2.acquire();
        count++;
        // release exclusive access
        mutex2.release(); mutex1.release();
    }

    private void decrement2()
            throws InterruptedException {
        // wait for exclusive access
        mutex2.acquire(); mutex1.acquire();
        count++;
        // release exclusive access
        mutex1.release(); mutex2.release();
    }

    private static boolean want1, want2;
    private static int turn;

    private static void acquire1() {
        want1 = true;
        turn = 2;
        while (want2 && turn == 2) {}
    }
    private static void release1() {
        want1 = false;
    }

    private static void acquire2() {
        want2 = true;
        turn = 1;
        while (want1 && turn == 1) {}
    }
    private static void release2() {
        want2 = false;
    }
}
