import java.util.Random;
import java.util.concurrent.*;

import static java.lang.Thread.*;

/**
 * Voorbeeld programma om deadlock te demonstreren in de dining philosophers setting
 * Oplossing 1: neem de vorken in dezelfde ordening op
 * Oplossing 2: beperk het aantal philosophers aan tafel
 *
 * @author henk
 */
public class Dining {

    private static final int THINK = 1;
    private static final int EAT = 5;
    private static final int SEED = 0;
    private static Random random = new Random(SEED);
    // private static Semaphore table = new Semaphore(4);

    private static Semaphore[] fork = {new Semaphore(1), new Semaphore(1), new Semaphore(1), new Semaphore(1), new Semaphore(1)};

    private static void philosopher(int id) {
        while (true) {
            try {
                sleep(random.nextInt(THINK));
                // table.acquire();
                System.out.println(id + " picks up");
//                if (id != 4) {
                fork[id].acquire();
                fork[(id + 1) % 5].acquire();
//                } else {
//                    fork[0].acquire();
//                    fork[4].acquire();
//                }
                sleep(random.nextInt(EAT));
                System.out.println(id + " put down");
                fork[id].release();
                fork[(id + 1) % 5].release();
                // table.release();
            } catch (InterruptedException ignored) {
            }
        }
    }

    public static void main(String[] args) {
        new Thread(() -> {philosopher(0);}).start();
        new Thread(() -> {philosopher(1);}).start();
        new Thread(() -> {philosopher(2);}).start();
        new Thread(() -> {philosopher(3);}).start();
        new Thread(() -> {philosopher(4);}).start();
    }

}
