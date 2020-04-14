import java.util.*;
import java.util.concurrent.*;

/**
 * Voorbeeld programma waarom parallel programmeren op multicore processors voordelen biedt
 * @author henk
 */
public class Parallel {

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

    private static void produce() {
        try {
            process();
            inp.put(rand.nextInt(10));
        } catch (InterruptedException ignore) {
        }
    }

    private static void compute() {
        try {
            process();
            out.put(inp.take());
        } catch (InterruptedException ignore) {
        }
    }

    private static void consume() {
        try {
            process();
            System.out.println(out.take());
        } catch (InterruptedException ignore) {
        }
    }

    private static void sequential() {
        while (true) {
            produce();
            compute();
            consume();
        }
    }

    private static void concurrent() {
        new Thread(() -> {while (true) produce();}).start();
        new Thread(() -> {while (true) compute();}).start();
        new Thread(() -> {while (true) consume();}).start();
    }

    public static void main(String[] args) {
        sequential();
        //concurrent();
    }
}
