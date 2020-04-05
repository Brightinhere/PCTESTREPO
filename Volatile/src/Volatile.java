/**
 * Voorbeeld programma waarom volatile nodig is bij concurrent read door andere thread
 * @author henk
 */
public class Volatile {

    private static /* volatile */ boolean stop = false;

    private static class Runner implements Runnable {

        @Override
        public void run() {
            while (!stop) {}
            System.out.println("Stopped");
        }
    }

    public static void main(String[] args) throws InterruptedException {
        new Thread(new Runner()).start();
        Thread.sleep(2000);
        System.out.println("Signals");
        stop = true;
    }
}
