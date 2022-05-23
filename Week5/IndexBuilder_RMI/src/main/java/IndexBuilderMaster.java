import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

public class IndexBuilderMaster extends UnicastRemoteObject implements IndexBuilderInterface {

    private Map<Character,Integer> firstCharacterCounts;
    private AtomicInteger minSubIndexSize = new AtomicInteger(Integer.MAX_VALUE);
    private AtomicInteger maxSubIndexSize = new AtomicInteger(Integer.MIN_VALUE);

    public IndexBuilderMaster() throws RemoteException {
        super();
        this.firstCharacterCounts = new ConcurrentHashMap<>();
    }

    // thread-save processing of sub-index
    @Override
    public void processIndex(Map<Character,Integer> index) {
        int numWords = 0;
        for (Map.Entry<Character,Integer> e : index.entrySet()) {
            this.processIndexEntry(e.getKey(),e.getValue());
            numWords += e.getValue();
        }
        minSubIndexSize.accumulateAndGet(numWords, Integer::min);
        maxSubIndexSize.accumulateAndGet(numWords, Integer::max);
    }


    // thread-save processing of sub-index entry into concurrent hashmap
    @Override
    public void processIndexEntry(char firstCharacter, int numWords) {
        this.firstCharacterCounts.merge(firstCharacter, numWords, Integer::sum);
    }

    public Map<Character,Integer> getFirstCharacterCounts() {
        return firstCharacterCounts;
    }

    public int getMinSubIndexSize() {
        return minSubIndexSize.get();
    }

    public int getMaxSubIndexSize() {
        return maxSubIndexSize.get();
    }

    public int getTotalWordCount() {
        return firstCharacterCounts.values().stream().reduce(0,Integer::sum);
    }
}
