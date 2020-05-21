import java.util.Arrays;

public class ParSumMain {

    private static final int VECTOR_LENGTH = 50000000;
    //private static final int VECTOR_LENGTH = 250000000;

    private static final int MIN_CORES = 1;
    private static final int MAX_CORES = Runtime.getRuntime().availableProcessors();
    //private static final int MAX_CORES = 8;
    private static final int FJ_SPLITFACTOR = 3;
    //private static final int TASK_FACTOR = 5;
    private static final int TASK_FACTOR = 25;

    private static final int REPEAT = 3;

    public static void main(String[] args) {
        long[] timesFJ = new long[MAX_CORES+1];
        long[] timesFJ2 = new long[MAX_CORES+1];
        Arrays.fill(timesFJ, 0L);
        Arrays.fill(timesFJ2, 0L);
        long[] timesES = new long[MAX_CORES+1];
        long[] timesES2 = new long[MAX_CORES+1];
        Arrays.fill(timesES, 0L);
        Arrays.fill(timesES2, 0L);

        Double solution;
        Vector vector = new Vector(VECTOR_LENGTH);
        vector.randomize(19630427);

        for (int r = 0; r < REPEAT; r++) {

            System.gc();
            long startTime = System.currentTimeMillis();
            solution = vector.sequentialSum();
            long duration = (System.currentTimeMillis()-startTime);
            System.out.printf("Sequential solution=%f took %d msecs\n", solution, duration);
            timesFJ[0] += duration;
            timesFJ2[0] += duration * duration;
            timesES[0] += duration;
            timesES2[0] += duration * duration;

            for (int p = MIN_CORES; p <= MAX_CORES; p++) {

                System.gc();
                startTime = System.currentTimeMillis();
                solution = vector.forkJoinSum(p, TASK_FACTOR * p, FJ_SPLITFACTOR);
                duration = (System.currentTimeMillis()-startTime);
                timesFJ[p] += duration;
                timesFJ2[p] += duration * duration;
                System.out.printf("ForkJoin: %d-Thread %d-task splitfactor=%d solution=%f took %d msecs\n",
                        p, p* TASK_FACTOR, FJ_SPLITFACTOR, solution, duration);

                System.gc();
                startTime = System.currentTimeMillis();
                solution = vector.executorSum(p, TASK_FACTOR * p);
                duration = (System.currentTimeMillis()-startTime);
                timesES[p] += duration;
                timesES2[p] += duration * duration;
                System.out.printf("ExecutorService: %d-Thread %d-task solution=%f took %d msecs\n",
                        p, p* TASK_FACTOR, solution, duration);
            }
        }

        System.out.print("\nAverages:\n P:\tForkJoin:    \t\tExecutorService:");
        for (int p = 0; p <= MAX_CORES; p++) {
            double avgFJ = timesFJ[p] * 1.0 / REPEAT;
            double avgES = timesES[p] * 1.0 / REPEAT;
            System.out.printf("\n%2d:", p);
            if (avgFJ > 0.001) {
                System.out.printf("\t%.1f \t±%.2f    ",
                        avgFJ,
                        Math.sqrt((timesFJ2[p] - REPEAT * avgFJ * avgFJ) / REPEAT));
            }
            if (avgES > 0.001) {
                System.out.printf("\t%.1f \t±%.2f",
                        avgES,
                        Math.sqrt((timesES2[p] - REPEAT * avgES * avgES) / REPEAT));
            }
        }
    }
}
