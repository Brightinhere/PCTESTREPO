import java.util.concurrent.atomic.AtomicInteger;

/**
 * Voorbeeld programma waarom synchronized nodig is bij concurrent write door andere thread
 * Oplossing 1 : gebruik een volatile variabele en busy wait (leuke oefening voor in de klas)
 * Oplossing 2 : gebruik synchronized keyword in declaratie van increment methode
 *
 * @author henk
 */
public class Atomic_atomic {

    private static AtomicInteger count = new AtomicInteger(0);

    private static void increment(int id) {
        count.incrementAndGet();
    }

    static class Counter extends Thread {
        private int id;

        public Counter(int id) {
            this.id = id;
        }

        public void run() {
            for (int i = 0; i < 100000; i++) increment(id);
        }
    }

    public static void main(String[] args) throws InterruptedException {
        Counter t1 = new Counter(1);
        Counter t2 = new Counter(2);
        t1.start();
        t2.start();
        t1.join();
        t2.join();
        System.out.println(count);
    }
}
