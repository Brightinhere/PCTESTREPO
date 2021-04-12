import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.concurrent.atomic.AtomicInteger;

public class BigMath {
    private static final BigDecimal ZERO = new BigDecimal(0);
    private static final BigDecimal TWO = new BigDecimal(2);

    /**
     * calculates the third root of an input with a given decimal precision
     * @param input
     * @param precision
     * @return
     */
    public static BigDecimal thirdRoot(BigDecimal input, int precision) {
        BigDecimal lower = ZERO.setScale(precision, RoundingMode.HALF_UP);
        BigDecimal upper = input.setScale(precision, RoundingMode.HALF_UP);

        // apply a binary search between lower and upper to find the root
        while (!upper.equals(lower)) {
            // find the mid-point between the lower and upper bound of the root
            BigDecimal average = lower.add(upper)
                    .divide(TWO, precision, RoundingMode.HALF_UP);

            // calculate the third power of the mid-point
            BigDecimal thirdPower = average.multiply(average)
                    .multiply(average)
                    .setScale(precision, RoundingMode.HALF_UP);

            // tricky decision making to deal with the rounding impact
            int thirdComparison = thirdPower.compareTo(input);
            int averageComparison = 0;
            if (thirdComparison == 0) {
                // thirdPower hits the input exactly, so we are done
                lower = average;
                upper = average;
            } else if (thirdComparison < 0) {
                // thirdPower is below the input, so we seem to have found a better lower
                averageComparison = average.compareTo(lower);
                if (averageComparison > 0) {
                    lower = average;
                } else {
                    // but if the average does not exceed the latest lower, we are done
                    upper = average;
                }
            } else {
                // thirdPower is above the input, so we seem to have found a better upper
                averageComparison = average.compareTo(upper);
                if (averageComparison < 0) {
                    upper = average;
                } else {
                    // but if the average is not below the latest upper, we are done
                    lower = average;
                }
            }
        }
        return lower;
    }

    public static void calculateThirdRootsSequentially(int nInputs, int precision) {
        System.out.printf("Calculating %d third roots sequentially with %d decimals precision\n",
                                nInputs, precision);
        // force a java garbage collection, to clean up clutter memory usage
        System.gc();
        // start the timer
        long start = System.nanoTime();

        // perform all root calculations sequentially
        for (int i = 0; i < nInputs; i++) {
            final int input = 125+i;
            BigDecimal root = BigMath.thirdRoot(new BigDecimal(input), precision);
            System.out.printf("thirdRoot(%d) = %.50s...\n", input, root.toPlainString());
        }

        System.out.printf("Elapsed time = %.3f ms\n", ((System.nanoTime() - start) / 1E6));
    }

    public static void calculateThirdRootsConcurrently(int nInputs, int scale) throws InterruptedException {
        System.out.printf("Calculating %d third roots concurrently with %d decimals precision\n",
                                nInputs, scale);
        Thread[] threads = new Thread[nInputs];
        // force a java garbage collection, to clean up clutter memory usage
        System.gc();
        // start the timer
        long start = System.nanoTime();

        // create a separate thread for every root calculation
        for (int i = 0; i < nInputs; i++) {
            final int input = 125+i;
            threads[i] = new Thread(
                    () -> {
                        BigDecimal root = BigMath.thirdRoot(new BigDecimal(input), scale);
                        System.out.printf("thirdRoot(%d) = %.50s...\n", input, root.toPlainString());
                    }
            );

            // start the thread just created
            threads[i].start();
        }

        // synchronise completion of all calculations
        for (Thread t : threads) t.join();

        System.out.printf("Elapsed time = %.3f ms\n", ((System.nanoTime() - start) / 1E6));
    }

    public static void calculateThirdRootsConcurrentPool(AtomicInteger nextInput, int nWorkers, int precision)
                    throws InterruptedException {
        System.out.printf("Calculating %d third roots in %d concurrent workers with %d decimals precision\n",
                nextInput.get(), nWorkers, precision);
        Thread[] threads = new Thread[nWorkers];
        // force a java garbage collection, to clean up clutter memory usage
        System.gc();
        // start the timer
        long start = System.nanoTime();

        // Load balance all root calculations across the available workers
        for (int i = 0; i < nWorkers; i++) {
            // create one thread per worker
            threads[i] = new Thread(
                    () -> {
                        // claim the next available input to be processed
                        int inputIndex = nextInput.decrementAndGet();

                        // loop until no more work to do
                        while (inputIndex >= 0) {
                            int input = 125+inputIndex;
                            BigDecimal root = BigMath.thirdRoot(new BigDecimal(input), precision);
                            System.out.printf("thirdRoot(%d) = %.50s...\n", input, root.toPlainString());
                            // claim the next available input to be processed
                            inputIndex = nextInput.decrementAndGet();
                        }
                    }
            );
            // start the thread just created
            threads[i].start();
        }

        // synchronise completion of all calculations in all workers
        for (Thread t : threads) t.join();

        System.out.printf("Elapsed time = %.3f ms\n", ((System.nanoTime() - start) / 1E6));
    }
}

