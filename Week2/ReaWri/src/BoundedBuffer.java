import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

public class BoundedBuffer<E> {
    final Lock lock = new ReentrantLock(true);
    final Condition notFull = lock.newCondition();
    final Condition notEmpty = lock.newCondition();

    final Object[] items;
    volatile int head, tail, count;

    public BoundedBuffer(int size) {
        items = new Object[size];
        head = 0;
        tail = 0;
        count = 0;
    }

    public void put(E item) {
        lock.lock();
        while (count == items.length) {
            notFull.awaitUninterruptibly();
        }
        items[head] = item;
        head = (head + 1) % items.length;
        count++;
        notEmpty.signal();
        lock.unlock();

    }

    public E take() {
        lock.lock();
        while (count == 0) {
            notEmpty.awaitUninterruptibly();
        }
        E item = (E) items[tail];
        tail = (tail + 1) % items.length;
        count--;
        notFull.signal();
        lock.unlock();
        return item;
    }
}
