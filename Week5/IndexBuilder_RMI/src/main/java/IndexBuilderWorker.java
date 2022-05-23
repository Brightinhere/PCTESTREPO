import common.Timer;

import java.io.IOException;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Supplier;

public class IndexBuilderWorker {

    private static final int MIN = 2, MAX = 5;
    private static ArrayList<String> words = new ArrayList<>();
    private static Map<Character, Integer> index;


    public static void main(String[] args) throws InterruptedException, IOException, NotBoundException {
        String serviceHost = "localhost";
        int workerId = 0;
        int numWords = 0;
        long randomSeed = 0;

        // process command line arguments, decide to assume master role or worker role
        for (int i = 0; i < args.length; i++) {
            if (args[i].equals("--serviceHost")) {
                i++;
                serviceHost = args[i];
            } else if (args[i].equals("--verbosityLevel")) {
                i++;
                Timer.verbosityLevel = Integer.valueOf(args[i]);
            }
            else if (args[i].equals("--workerId")) {
                i++;
                workerId = Integer.valueOf(args[i]);
            } else if (args[i].equals("--numWords")) {
                i++;
                numWords = Integer.valueOf(args[i]);
            } else if (args[i].equals("--seed")) {
                i++;
                randomSeed = Long.valueOf(args[i]);
            }
        }

        Registry registry = LocateRegistry.getRegistry(serviceHost, IndexBuilderMain.SERVICE_PORT);
        IndexBuilderMaster indexBuilderMaster =
                (IndexBuilderMaster)registry.lookup("//" + serviceHost + IndexBuilderMain.SERVICE_NAME);

        // run the worker task and communicate the result with the master
        workerMain(workerId, numWords, randomSeed, indexBuilderMaster);

    }

    private static void workerMain(int workerId, int numWords, long randomSeed, IndexBuilderMaster master) throws RemoteException, NotBoundException {
        Timer.start(2, "Worker-%d is up and running\n", workerId);

        Random random = new Random(randomSeed);
        long start = System.nanoTime();
        int length = MIN + random.nextInt(MAX - MIN + 1);
        for (int i = 0; i < numWords; i++) {
            StringBuilder word = new StringBuilder();
            for (int j = 0; j < length; j++) {
                word.append((char) ('a' + random.nextInt(26)));
            }
            words.add(word.toString());
        }
        Timer.measure(2, "Worker-%d has completed initialisation\n", workerId);

        index = new HashMap<>();
        words.forEach((word) -> index.merge(word.charAt(0), 1, Integer::sum));
        Timer.measure(2, "Worker-%d has completed the first-character count\n", workerId);

        master.processIndex(index);
        Timer.measure(2, "Worker-%d has completed the merge at master\n", workerId);

    }
}
