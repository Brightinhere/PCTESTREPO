import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.concurrent.atomic.AtomicInteger;

public class BigMath {
    private static final BigDecimal ZERO = new BigDecimal(0);
    private static final BigDecimal TWO = new BigDecimal(2);

    public static BigDecimal thirdRoot(BigDecimal x, int scale) {
        BigDecimal lower = ZERO.setScale(scale, RoundingMode.HALF_UP);
        BigDecimal upper = x.setScale(scale, RoundingMode.HALF_UP);
        while (!upper.equals(lower)) {
            BigDecimal average = lower.add(upper)
                    .divide(TWO, scale, RoundingMode.HALF_UP);
            BigDecimal third = average.multiply(average)
                    .multiply(average)
                    .setScale(scale, RoundingMode.HALF_UP);

            int thirdComparison = third.compareTo(x);
            int averageComparison = 0;
            if (thirdComparison == 0) {
                lower = average;
                upper = average;
            } else if (thirdComparison < 0) {
                averageComparison = average.compareTo(lower);
                if (averageComparison > 0) {
                    lower = average;
                } else {
                    upper = average;
                }
            } else {
                averageComparison = average.compareTo(upper);
                if (averageComparison < 0) {
                    upper = average;
                } else {
                    lower = average;
                }
            }
        }
        return lower;
    }

    public static void calculateThirdRootsSequentially(int nRoots, int scale) {
        System.out.printf("Calculating %d third roots sequentially with %d decimals accuracy\n",
                                nRoots, scale);
        System.gc();
        long start = System.nanoTime();

        for (int i = 0; i < nRoots; i++) {
            final int xx = 125+i;
            BigDecimal root = BigMath.thirdRoot(new BigDecimal(xx), scale);
            System.out.printf("thirdRoot(%d) = %.50s...\n", xx, root.toPlainString());
        }

        System.out.printf("Elapsed time = %.3f ms\n", ((System.nanoTime() - start) / 1E6));
    }

    public static void calculateThirdRootsConcurrently(int nRoots, int scale) throws InterruptedException {
        System.out.printf("Calculating %d third roots concurrently with %d decimals accuracy\n",
                                nRoots, scale);
        Thread[] threads = new Thread[nRoots];
        System.gc();
        long start = System.nanoTime();

        for (int i = 0; i < nRoots; i++) {
            final int xx = 125+i;
            threads[i] = new Thread(
                    () -> {
                        BigDecimal root = BigMath.thirdRoot(new BigDecimal(xx), scale);
                        System.out.printf("thirdRoot(%d) = %.50s...\n", xx, root.toPlainString());
                    }
            );
            threads[i].start();
        }
        for (Thread t : threads) t.join();

        System.out.printf("Elapsed time = %.3f ms\n", ((System.nanoTime() - start) / 1E6));
    }

    public static void calculateThirdRootsConcurrentPool(AtomicInteger id, int nWorkers, int scale)
                    throws InterruptedException {
        System.out.printf("Calculating %d third roots in %d concurrent workers with %d decimals accuracy\n",
                                id.get(), nWorkers, scale);
        Thread[] threads = new Thread[nWorkers];
        System.gc();
        long start = System.nanoTime();

        for (int i = 0; i < nWorkers; i++) {
            threads[i] = new Thread(
                    () -> {
                        int ii = id.decrementAndGet();
                        while (ii >= 0) {
                            int xx = 125+ii;
                            BigDecimal root = BigMath.thirdRoot(new BigDecimal(xx), scale);
                            System.out.printf("thirdRoot(%d) = %.50s...\n", xx, root.toPlainString());
                            ii = id.decrementAndGet();
                        }
                    }
            );
            threads[i].start();
        }
        for (Thread t : threads) t.join();

        System.out.printf("Elapsed time = %.3f ms\n", ((System.nanoTime() - start) / 1E6));
    }
}

