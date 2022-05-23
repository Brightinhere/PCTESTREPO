import java.util.*;
import java.util.concurrent.*;

/**
 * Demo program showing parallelstream and concurrenthashmap
 *
 * @author henk
 */
public class StreamMain {

    private static final int SEED = 0;
    private static final int MIN = 2, MAX = 5;
    private static final int SIZE = 10000000;
    private static ArrayList<String> words = new ArrayList<>();
    private static Map<String, Integer> index;

    private static void initialize() {
        Random random = new Random(SEED);
        int length = MIN + random.nextInt(MAX - MIN + 1);
        for (int i = 0; i < SIZE; i++) {
            StringBuilder word = new StringBuilder();
            for (int j = 0; j < length; j++) {
                word.append((char) ('a' + random.nextInt(26)));
            }
            words.add(word.toString());
        }
    }

    public static void main(String[] args) {
        initialize();

        long start = System.nanoTime();
        index = new HashMap<>();
        words.forEach((word) -> index.merge(word, 1, Integer::sum));
        System.out.println(((System.nanoTime() - start) / 1E9) + " sec.");

        start = System.nanoTime();
        index = new ConcurrentHashMap<>();
        words.parallelStream().forEach((word) -> index.merge(word, 1, Integer::sum));
        System.out.println(((System.nanoTime() - start) / 1E9) + " sec.");
    }

}
