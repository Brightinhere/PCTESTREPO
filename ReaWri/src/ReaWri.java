import java.util.Random;
import java.util.concurrent.locks.*;

import static java.lang.Thread.*;

/**
 * Voorbeeld programma van het gebruik van een reentrant read/write lock
 *
 * @author henk
 */
public class ReaWri {
    private static final int SEED = 0;
    private static Random random = new Random(SEED);
    private static final int READERS = 8, READ = 50, WAITREAD = 100;
    private static final int WRITERS = 3, WRITE = 50, WAITWRITE = 200;

    private static class ReadersWriters {

        private static volatile int numReaders = 0, numWriters = 0;
        private static final Object mutex = new Object();
        private static ReentrantReadWriteLock lock = new ReentrantReadWriteLock();

        private static void startRead() {
            lock.readLock().lock();
            synchronized (mutex) {
                numReaders++;
            }
        }

        private static void endRead() {
            synchronized (mutex) {
                numReaders--;
            }
            lock.readLock().unlock();
        }

        private static void startWrite() {
            lock.writeLock().lock();
            synchronized (mutex) {
                numWriters++;
            }
        }

        private static void endWrite() {
            synchronized (mutex) {
                numWriters--;
            }
            lock.writeLock().unlock();
        }

        private static void reader() {
            while (true) {
                try {
                    startRead();
                    assert numWriters == 0;
                    System.out.println("reading " + numReaders);
                    sleep(random.nextInt(READ));
                    endRead();
                    sleep(random.nextInt(WAITREAD));
                } catch (InterruptedException ignored) {
                }
            }
        }

        private static void writer() {
            while (true) {
                try {
                    startWrite();
                    assert numWriters == 1 && numReaders == 0;
                    System.out.println("writing " + numWriters);
                    sleep(random.nextInt(WRITE));
                    endWrite();
                    sleep(random.nextInt(WAITWRITE));
                } catch (InterruptedException ignored) {
                }
            }
        }
    }

    public static void main(String[] args) {
        for (int i = 0; i < READERS; i++) new Thread(ReadersWriters::reader).start();
        for (int i = 0; i < WRITERS; i++) new Thread(ReadersWriters::writer).start();
    }
}
