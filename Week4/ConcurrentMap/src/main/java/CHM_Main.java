import java.util.*;
import java.util.concurrent.*;
import java.util.stream.Collectors;

/**
 * https://docs.oracle.com/javase/tutorial/collections/streams/parallelism.html
 *
 * demonstrates use of concurrentMap and parallelStream
 *
 * @author j.a.somers@hva.nl
 *
 */
public class CHM_Main {

    // dummy synchronised list, used to drive the parallelStream for setup
    private static final int NTASKS = 40;
    private static List<Integer> dummyList =
            Collections.synchronizedList(new ArrayList<>());

    private static final int NWORDS = 10000000;
    // word dictionary counts words
    private static ConcurrentHashMap<String,Integer> wordMap =
            new ConcurrentHashMap<String,Integer>(64, (float)0.9, NTASKS);
    // dictionary index counts total number of words by first character
    private static ConcurrentHashMap<Character,Integer> index =
            new ConcurrentHashMap<Character,Integer>(64, (float)0.9, NTASKS);

    private static void setupWordMap(Map<String,Integer> map, int nWords) {
        Random random = new Random();

        for (int i=0; i<nWords; i++) {
            // create a random word with 2-5 characters
            int l = 2 + random.nextInt(4);
            String w = "";
            for (int j=0; j<l; j++) {
                w += (char)('A' + random.nextInt(26));
            }

            // enter a new word or add 1 to its current count
            map.merge(w, 1, Integer::sum);
        }
    }

    private static void printMap(Map map) {
        map.forEach(
                (k,v) -> {
                    System.out.println(k + ": " + v);
                }
        );
    }

    public static void main(String[] args) {

        // create the dummy list for arbitrary parallelStream
        for (int i=0; i<NTASKS; i++) {
            dummyList.add(i);
        }

        System.out.println("Streaming order:");
        dummyList.stream().forEach((i)->{
            System.out.print(" "+i);
        });
        System.out.println();
        dummyList.parallelStream().forEach((i)->{
            System.out.print(" "+i);
        });
        System.out.println();

        long start;
        System.out.println("NWORDS = " + NWORDS);
        wordMap.clear();
        start = System.nanoTime();
        dummyList.stream().forEach((i)->{
            setupWordMap(wordMap, NWORDS/NTASKS);
        });
        System.out.println("Serial setup: "+((System.nanoTime() - start) / 1E9) + " sec.");

        wordMap.clear();
        start = System.nanoTime();
        dummyList.parallelStream().forEach((i)->{
            setupWordMap(wordMap, NWORDS/NTASKS);
        });
        System.out.println("Parallel setup: "+((System.nanoTime() - start) / 1E9) + " sec.");

        index.clear();
        start = System.nanoTime();
        wordMap
            .entrySet() // unfortunately maps cannot be streamed yet?
            .stream()
            .forEach(
                (Map.Entry e) -> {
                    // add the word-count to the character index
                    index.merge(
                        ((String)e.getKey()).charAt(0),
                        (Integer)e.getValue(),
                        Integer::sum);
                    }
                );
        System.out.println("Serial index: "+((System.nanoTime() - start) / 1E9) + " sec.");

        System.out.println("NumWords in serial index = " +
             index.reduceValues(1, Integer::sum));

        index.clear();
        start = System.nanoTime();
        wordMap
                .entrySet() // unfortunately maps cannot be streamed yet?
                .parallelStream()
                .forEach(
                        (Map.Entry e) -> {
                            // add the word-count to the character index
                            index.merge(
                                    ((String)e.getKey()).charAt(0),
                                    (Integer)e.getValue(),
                                    Integer::sum);
                        }
                );
        System.out.println("Parallel index: "+((System.nanoTime() - start) / 1E9) + " sec.");

        System.out.println("NumWords in parallel index = " +
                index.reduceValues(NTASKS, Integer::sum));

        start = System.nanoTime();
        ConcurrentMap<Character,Integer> index2 =
                wordMap
                .entrySet()
                .parallelStream()
                .collect(Collectors.groupingByConcurrent(e->e.getKey().charAt(0),
                        Collectors.summingInt(e->e.getValue()))
                    );
        System.out.println("Parallel collected index: "+((System.nanoTime() - start) / 1E9) + " sec.");


        System.out.println("NumWords in parallel collected index = " +
                index2.entrySet()
                        .stream()
                        .mapToLong(e->e.getValue())
                        .sum()
                        );
    }
}
