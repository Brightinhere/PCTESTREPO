import java.util.*;
import java.util.concurrent.*;

import static java.lang.Thread.*;

/**
 * Voorbeeld programma waarom parallel programmeren zelfs op single core processors voordelen biedt
 * @author henk
 */
public class Spooler {

    private static Random rand = new Random(0);
    private static BlockingQueue<Integer> inp = new ArrayBlockingQueue<>(1);
    private static BlockingQueue<Integer> out = new ArrayBlockingQueue<>(1);

    /**
     * Methode die bij iedere aanroep ca. 1 sec. CPU power nodig heeft
     */
    private static void process() {
        for (int i = 0; i < Integer.MAX_VALUE / 30; i++) {
            double d = Math.acos(i);
        }
    }

    private static void reader() {
        try {
            sleep(1000); // 1 sec. input device wait
            inp.put(rand.nextInt(10));
        } catch (InterruptedException ignore) {
        }
    }

    private static void copier() {
        try {
            process();
            out.put(inp.take());
        } catch (InterruptedException ignore) {
        }
    }

    private static void writer() {
        try {
            sleep(1000); // 1 sec. output device wait
            System.out.println(out.take());
        } catch (InterruptedException ignore) {
        }
    }

    private static void sequential() {
        while (true) {
            reader();
            copier();
            writer();
        }
    }

    private static void concurrent() {
        new Thread(() -> {while (true) reader();}).start();
        new Thread(() -> {while (true) copier();}).start();
        new Thread(() -> {while (true) writer();}).start();
    }

    public static void main(String[] args) {
        sequential();
        //concurrent();
    }
}
