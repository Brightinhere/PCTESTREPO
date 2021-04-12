import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * TODO Excercise:
 *  This program populates a large list with randomly generated words
 *      and then applies a linear search algorithm to find one of its words.
 *      complete the parallelSearchAny method that tries to apply multi-threading to find the word quicker
 *  Follow below steps:
 *   1. run the program to test the sequential solution with 0 parallel tasks
 *      choose number of words > 1,000,000 and such that sequential solution searches more than 100 msecs
 *      choose wordLength > 5 to start with in order to avoid chance to hit a duplicate
 *      choose seed>0 for the randomizer in order to reproduce specific initialisation
 *   2. implement and test the parallel search e.g. up to 64 tasks
 *   3. check/refine your parallel algorithm until it beats the sequential version in elapsed time
 *   4. test your algorithm with wordLength=3 and verify that different task numbers find different solutions
 *   5. implement and test a parallel initialization
 *   6. improve the parallel initialization until it beats the sequential initialization
 *      where does your overhead come from?
 */

public class TextSearch {

    // TODO handle the exception
    private static int parallelSearchAny(String target, List<String> words, int nTasks) throws InterruptedException {
        Thread[] tasks = new Thread[nTasks];
        // TODO think of a proper data type to capture any found index of the target word
        //int foundIndex = -1;
        AtomicInteger foundIndex = new AtomicInteger(-1);

        // Create and start all threads searching part of the list
        final int nTasks2 = nTasks * nTasks;
        for (int t = 0; t < nTasks; t++) {
            final long taskNrBase = t * nTasks;
            tasks[t] = new Thread( () -> {
                // TODO search part of the list for the target string
                int latestFoundIndex = foundIndex.get();
                for (int stNr = 0; stNr < nTasks && latestFoundIndex < 0; stNr++) {
                    int stFoundIndex = sequentialSearchAny(target, words,
                            (int)((taskNrBase+stNr) * words.size() / nTasks2),
                            (int)((taskNrBase+stNr+1) * words.size() / nTasks2)
                    );
                    // TODO remember the index, if found
                    latestFoundIndex = foundIndex.accumulateAndGet(stFoundIndex, Integer::max);
                }
            });

            // TODO start the task
            tasks[t].start();
        }

        // TODO wait until all tasks have finished
        for (int t = 0; t < nTasks; t++) {
            // wait until tasks[t] has finished
            tasks[t].join();
        }

        // return the index found
        //return foundIndex;
        return foundIndex.get();
    }

    private static Randomizer randomizer = new Randomizer();

    // TODO handle the exception
    public static void main(String[] args) throws InterruptedException {
        long startTime, elapsedTime;

        System.out.println("Welcome to the parallel text searcher\n");
        Scanner input = new Scanner(System.in);
        System.out.print("How many words do you want to search from ?");
        int numWords = input.nextInt();
        numWords = Integer.max(1000, numWords);
        System.out.print("What is the length of each word ?");
        int wordLength = input.nextInt();
        wordLength = Integer.max(1, wordLength);
        System.out.print("How many parallel tasks do you want to involve max ?");
        int maxNumTasks = input.nextInt();
        System.out.print("Any specific seed for the randomizer ?");
        long randomSeed = input.nextLong();
        if (randomSeed > 0) randomizer.setSeed(randomSeed);

        // generate the list of words
        startTime = System.currentTimeMillis();
        List<String> words = new ArrayList<>(numWords);
        for (int i = 0; i < numWords; i++) words.add(null);

        // choose your initialization algorithm
        //sequentialInitialize(maxWordLength, words, 0, numWords);
        parallelInitialize(wordLength, words, Integer.max(1,maxNumTasks));

        elapsedTime = System.currentTimeMillis() - startTime;
        System.out.printf("Initialization took %d msecs\n", elapsedTime);

        // pick at random a word to be searched
        String wordToBeSearched = words.get(randomizer.nextInt(numWords));
        System.out.printf("Sequential search for '%s'\n", wordToBeSearched);

        startTime = System.currentTimeMillis();
        int seqIdxFound = sequentialSearchAny(wordToBeSearched, words, 0, numWords);
        elapsedTime = System.currentTimeMillis() - startTime;
        System.out.printf("Found '%s' at position %d\n", words.get(seqIdxFound), seqIdxFound);
        System.out.printf("   first sequential search took %d msecs\n", elapsedTime);

        startTime = System.currentTimeMillis();
        int seqIdxFound2 = sequentialSearchAny(wordToBeSearched, words, 0, numWords);
        elapsedTime = System.currentTimeMillis() - startTime;
        System.out.printf("Found '%s' at position %d\n", words.get(seqIdxFound), seqIdxFound2);
        System.out.printf("   second sequential search took %d msecs\n", elapsedTime);

        // parallel searches
        for (int nTasks = 1; nTasks <= maxNumTasks; nTasks *= 2) {
            System.out.printf("Parallel search for '%s' by %d tasks\n", wordToBeSearched, nTasks);

            startTime = System.currentTimeMillis();
            int parFoundAny = parallelSearchAny(wordToBeSearched, words, nTasks);
            elapsedTime = System.currentTimeMillis() - startTime;
            System.out.printf("Found '%s' at position %d\n", words.get(parFoundAny), parFoundAny);
            System.out.printf("   parallelSearchAny(%d) took %d msecs\n", nTasks, elapsedTime);
        }
    }

    /**
     * Sequential search of the target in the sub-list words[from..to]
     * @param target
     * @param words
     * @param from
     * @param to
     * @return  the highest index: from <= index < to that matches the target: words[index] == target
     */
    private static int sequentialSearchAny(String target, List<String> words, int from, int to) {
        for (int i = from; i < to; i++) {
            if (words.get(i).equals(target)) {
                return i;
            }
        }
        return -1;
    }

    private static void sequentialInitialize(int wordLength, List<String> words, int from, int to) {
        // reduce synchronization by global randomizer
        Randomizer localRandomizer = new Randomizer(randomizer.nextLong());
        for (int i = from; i < to; i++) {
            words.set(i, localRandomizer.nextWord(wordLength));
        }
    }

    // TODO handle the exception
    private static void parallelInitialize(int wordLength, List<String> words, int nTasks) throws InterruptedException {
        System.out.printf("Running parallel initialization-%d with %d tasks\n", wordLength, nTasks);
        Thread[] tasks = new Thread[nTasks];

        // Create and start all threads searching part of the list
        for (int t = 0; t < nTasks; t++) {
            final long taskNr = t;
            tasks[t] = new Thread( () -> {
                // TODO initialize part of the list for the target string
                sequentialInitialize(wordLength, words,
                        (int)(taskNr * words.size() / nTasks), (int)((taskNr+1) * words.size() / nTasks)
                );
            });

            // TODO start the task
            tasks[t].start();
        }

        // TODO wait until all tasks have finished
        for (int t = 0; t < nTasks; t++) {
            // wait until tasks[t] has finished
            tasks[t].join();
        }

        // finish
    }
}
