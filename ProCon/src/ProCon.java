import java.util.*;
import java.util.concurrent.*;

public class ProCon {

    private static final int CORE = 4;
    private static final int SIZE = 100;

    private static BlockingQueue<Integer> queue = new ArrayBlockingQueue<>(5);

    private static class Producer implements Runnable {

        public void run() {
            for (int i = 0; i < SIZE; i++)
                try {
                    queue.put(i);
                } catch (InterruptedException ignored) {
                }
            queue.add(-1);
        }
    }

    private static class Consumer implements Callable<Integer> {

        int count = 0;

        public Integer call() throws Exception {
            int value;
            do {
                value = queue.take();
                if (value != -1) count += value;
                else queue.put(value);
            } while (value != -1);
            return count;
        }
    }

    public static void main(String[] args) throws ExecutionException, InterruptedException {
        Thread producer = new Thread(new Producer());
        ExecutorService executor = Executors.newFixedThreadPool(CORE);
        List<Future<Integer>> consumers = new ArrayList<>(0);
        producer.start();
        for (int i = 0; i < CORE; i++) {
            consumers.add(executor.submit(new Consumer()));
        }
        executor.shutdown();
        int count = 0;
        for (int i = 0; i < CORE; i++) count += consumers.get(i).get();
        producer.join();
        queue.take();
        System.out.println(count);
    }

}
