import java.util.concurrent.*;

import static java.lang.Thread.*;

public class Stencil {

    private static final int SIZE = 12;
    private static final int CORE = 3;

    private static void work(int i, int j) throws InterruptedException {
        sleep(1000);
        System.out.println("" + i + " " + j);
    }

    private static void sequential() throws InterruptedException {
        for (int i = 0; i < SIZE; i++) {
            for (int j = 0; j < SIZE; j++) work(i, j);
        }
    }

    private static class Worker implements Runnable {
        private int id;
        private Phaser phaser;

        public Worker(int id, Phaser phaser) {
            this.id = id;
            this.phaser = phaser;
            phaser.register();
        }

        @Override
        public void run() {
            for (int i = 0; i < SIZE; i++) {
                for (int j = id * SIZE / CORE; j < (id + 1) * SIZE / CORE; j++) {
                    try {
                        work(i, j);
                    } catch (InterruptedException ignore) {
                    }
                }
                if (i < SIZE - 1) phaser.arriveAndAwaitAdvance(); else phaser.arriveAndDeregister();
                if (i == 0 && id < CORE - 1) new Thread(new Worker(id + 1, phaser)).start();
            }
        }
    }

    private static void concurrent() throws InterruptedException {
        Phaser phaser = new Phaser();
        new Thread(new Worker(0, phaser)).start();
        while (phaser.getPhase() >= 0) phaser.awaitAdvance(0);
    }

    public static void main(String[] args) throws InterruptedException {
        //sequential();
        concurrent();
    }
}
