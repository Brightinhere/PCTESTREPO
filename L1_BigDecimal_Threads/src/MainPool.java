import java.util.concurrent.atomic.AtomicInteger;

public class MainPool {
    private static final int N_CPU = 16;
    private static final int N_ROOTS = 1000;

    public static void main(String[] args) throws InterruptedException {
        AtomicInteger nextId = new AtomicInteger();

        nextId.set(N_ROOTS * 10);
        BigMath.calculateThirdRootsConcurrentPool(nextId, N_CPU, 1000);
        //BigMath.calculateThirdRootsConcurrently(N_ROOTS * 10, 1000);

        nextId.set(N_ROOTS);
        BigMath.calculateThirdRootsConcurrentPool(nextId, N_CPU, 2500);
    }
}
