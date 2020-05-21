import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.concurrent.*;
import java.util.function.UnaryOperator;

public class Vector {
    private static final int MAX_SIZE = 100;
    private static final int SPLIT_FACTOR = 3;

    private double[] data;
    private ForkJoinPool forkJoinPool;


    public Vector(int size) {
        this.data = new double[size];
    }

    public void randomize(long seed) {
        Random randomizer = new Random();
        randomizer.setSeed(seed);
        for (int i = 0; i < data.length; i++) {
            this.data[i] = randomizer.nextDouble();
        }
    }

    double mapper(double x) {
        return Math.sqrt(x + Math.sqrt(x + Math.sqrt(x)));
    }

    public double sequentialSum() {
        double sum = 0.0;
        for (int i = 0; i < data.length; i++) {
            sum += this.mapper(this.data[i]);
        }
        return sum;
    }

    public double forkJoinSum(int nThreads, int maxTasks, int splitFactor) {
        return this.forkJoinSum(nThreads, maxTasks, splitFactor, this::mapper);
    }

    public double forkJoinSum(int nThreads, int maxTasks, int splitFactor,
                              UnaryOperator<Double> mapper) {
        this.forkJoinPool = new ForkJoinPool(nThreads);
        double sum = forkJoinSumTask(0, this.data.length, this.data.length / maxTasks,
                splitFactor, mapper);
        this.forkJoinPool.shutdown();
        return sum;
    }

    private double forkJoinSumTask(int from, int to, int maxSize, int splitFactor,
                                   UnaryOperator<Double> mapper) {
        double sum = 0.0;
        int size = to - from;
        if (size <= maxSize) {
            for (int i = from; i < to; i++) {
                sum += mapper.apply(this.data[i]);
            }
        } else {
            final int splitSize = (size > splitFactor * maxSize) ?
                    (size - maxSize) / (splitFactor-1) :
                    size / splitFactor;
            ForkJoinTask<Double>[] subSums = new ForkJoinTask[splitFactor-1];
            for (int t = 0; t < splitFactor-1; t++) {
                final int finalT = t;
                subSums[t] = this.forkJoinPool.submit(
                        () -> forkJoinSumTask(from + finalT * splitSize,
                                from + (finalT+1) * splitSize, maxSize, splitFactor, mapper)
                );
            }

            for (int i = from + (splitFactor-1)*splitSize; i < to; i++) {
                sum += mapper.apply(this.data[i]);
            }

            for (int t = 0; t < splitFactor-1; t++) {
                sum += subSums[t].join();
            }
        }
        return sum;
    }

    private double forkJoinSumTask(int from, int to) {
        double sum = 0.0;
        int size = to - from;

        if (size <= MAX_SIZE) {
            // just do a small size job
            for (int i = from; i < to; i++) {
                sum += this.data[i];
            }
        } else {
            // split a big size job
            final int splitSize = size / SPLIT_FACTOR;

            // fork the partial sums
            ForkJoinTask<Double>[] subSums = new ForkJoinTask[SPLIT_FACTOR];
            for (int t = 0; t < SPLIT_FACTOR; t++) {
                final int finalT = t;
                subSums[t] = this.forkJoinPool.submit(
                        () -> forkJoinSumTask(from + finalT * splitSize,
                                from + (finalT+1) * splitSize)
                );
            }
            // join the partial sums
            for (int t = 0; t < SPLIT_FACTOR; t++) {
                sum += subSums[t].join();
            }
        }
        return sum;
    }


    public double executorSum(int nThreads, int maxTasks) {
        return this.executorSum(nThreads, maxTasks, this::mapper);
    }

    public double executorSum(int nThreads, int maxTasks, UnaryOperator<Double> mapper) {
        // create a threadpool, and the service to gather the result from each task
        ExecutorService executor = Executors.newFixedThreadPool(nThreads);
        CompletionService<Double> executorTaskQueue = new ExecutorCompletionService<>(executor);

        final int splitSize = this.data.length / maxTasks;
        // spawn the concurrent tasks, one for each segment in the array
        for (int t = 0; t < maxTasks; t++) {
            final int finalT = t;
            // java lambda expressions only accept finals in their closure...
            executorTaskQueue.submit(
                    () -> executorSumTask(finalT*splitSize, (finalT+1)*splitSize, mapper)
            );
        }

        double sum = 0.0;
        try {
            // gather the results and accumulate them
            for (int t = 0; t < maxTasks; t++) {
                sum += executorTaskQueue.take().get();
            }
        } catch (Exception ignored) {}

        executor.shutdown();
        return sum;
    }

    private double executorSumTask(int from, int to,
                                   UnaryOperator<Double> mapper) {
        double sum = 0.0;
        for (int i = from; i < to; i++) {
            sum += mapper.apply(this.data[i]);
        }
        return sum;
    }

    public double executorSum1(int nThreads, int maxTasks) {
        // create a thread pool and the service to gather the result from each task
        ExecutorService executor = Executors.newFixedThreadPool(nThreads);
        CompletionService<Double> executorTaskQueue = new ExecutorCompletionService<>(executor);

        final int splitSize = this.data.length / maxTasks;
        // spawn the concurrent tasks, one for each segment in the array
        for (int t = 0; t < maxTasks; t++) {
            final int finalT = t;
            // java lambda expressions only accept finals in their closure...
            executorTaskQueue.submit(
                    () -> executorSumTask(finalT*splitSize, (finalT+1)*splitSize)
            );
        }

        double sum = 0.0;
        try {
            // gather the results and accumulate them
            for (int t = 0; t < maxTasks; t++) {
                sum += executorTaskQueue.take().get();
            }
        } catch (Exception ignored) {}

        executor.shutdown();
        return sum;
    }

    private double executorSumTask(int from, int to) {
        double sum = 0.0;
        for (int i = from; i < to; i++) {
            sum += this.data[i];
        }
        return sum;
    }
}
