public class StartAndJoin {

    private static final int MAX_COUNT = 1000000;
    private static int count = 0;

    public static void main(String[] args) throws InterruptedException {

        Thread t = new Thread(StartAndJoin::counter);

        System.out.println(count);
            // start the child thread
        t.start();
            // main thread continues the work in parallel with child thread
        System.out.println(t.getState() + ", count=" + count);
            // synchronise child and main thread
        t.join();
        System.out.println(t.getState() + ", count=" + count);
    }

    private static void counter() {
        for (int i = 0; i < MAX_COUNT; i++) {
            count++;
        }
    }

    private static void timerExample1() {

        // start the nanosecond timer
        long startTime = System.nanoTime();

        // TODO execute worker code

        // measure elapsed time in milliseconds
        double elapsedTime = (System.nanoTime() - startTime) / 1E6;

    }

    private static void timerExample2() {

        // start the millisecond timer
        long startTime = System.currentTimeMillis();

        // TODO execute worker code

        // measure elapsed time in milliseconds
        long elapsedTime = System.currentTimeMillis() - startTime;

    }
}
