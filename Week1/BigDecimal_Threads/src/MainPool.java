import java.util.concurrent.atomic.AtomicInteger;

public class MainPool {
    private static final int N_THREADS = 16;
    private static final int N_ROOTS = 1000;

    public static void main(String[] args) throws InterruptedException {
        AtomicInteger nextInput = new AtomicInteger();

        nextInput.set(N_ROOTS * 10);
        // calculate nextInput roots in N_TREADS worker threads with precision s
        BigMath.calculateThirdRootsConcurrentPool(nextInput, N_THREADS, 1000);
        //BigMath.calculateThirdRootsConcurrently(N_ROOTS * 10, 1000);

        nextInput.set(N_ROOTS);
        BigMath.calculateThirdRootsConcurrentPool(nextInput, N_THREADS, 2500);
    }
}
