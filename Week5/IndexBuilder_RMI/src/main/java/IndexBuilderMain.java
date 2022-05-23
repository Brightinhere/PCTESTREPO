import common.Timer;

import java.io.File;
import java.io.IOException;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;
import java.util.Scanner;

public class IndexBuilderMain {

    public static final int SERVICE_PORT = 49991;    // + new Random().nextInt(100)
    public static final String SERVICE_NAME = "/IndexBuilder";

    public static void main(String[] args) throws InterruptedException, IOException, NotBoundException {
        String serviceHost = getExternalIPAddress();

        // if no worker launched, continue as the master-service
        Scanner input = new Scanner(System.in);

        System.out.printf("Welcome Distributed Index Builder Master/Worker test on %s\n", serviceHost);
        System.out.printf("\nPlease provide your input and output verbosity level [-1,0,1,2]: ");
        Timer.verbosityLevel = input.nextInt();
        int numWordsMillions = 250;
        int numWorkers = 2;
        int numTasks = 10;
        double taskSizeDifferentiator = 1.0;
        if (Timer.verbosityLevel >= 0) {
            System.out.print("Please provide the number of words to generate (in millions): ");
            numWordsMillions = input.nextInt();
            System.out.print("Please provide the number of worker processes: ");
            numWorkers = input.nextInt();
            System.out.print("Please provide a differentiation factor for task sizes [0..3, 0=equal sizes]: ");
            taskSizeDifferentiator = input.nextDouble();
        }

        // create and register the master service object
        IndexBuilderMaster master = new IndexBuilderMaster();
        registerMaster(master, serviceHost);

        // launch separate worker processes
        Timer.echo(-1, "\nLaunching %d workers at %s on %d million random numbers\n",
                numWorkers, serviceHost, numWordsMillions);
        Process[] workers = launchWorkersAtLocalHost(numWorkers, serviceHost, numWordsMillions, taskSizeDifferentiator);

        // wait until all workers have finished
        shutdownWorkers(workers);

        // present the results as obtained by the master
        System.out.printf("%d workers have completed merging of wordindexes of sizes %d-%d processing %d words in total\n",
                numWorkers,
                master.getMinSubIndexSize(), master.getMaxSubIndexSize(),
                master.getTotalWordCount());

        UnicastRemoteObject.unexportObject(master, true);
        // registry.unbind("//" + serviceHost + SERVICE_NAME);
    }

    private static String getExternalIPAddress() throws UnknownHostException {
        String ipa = "localhost";
        ipa = InetAddress.getLocalHost().getHostAddress().toString();
        return ipa;
    }

    private static void registerMaster(IndexBuilderInterface master, String serviceHost) throws RemoteException {
        Timer.start();
        // create the remote object register
        Registry registry = LocateRegistry.createRegistry(SERVICE_PORT);
        // create a new master service and register it

        registry.rebind("//" + serviceHost + SERVICE_NAME, master);
        long sampleTime = System.currentTimeMillis();
        Timer.measure(2, "Creation of registry has completed\n");
    }

    private static Process[] launchWorkersAtLocalHost(int numWorkers, String serviceHost, int numWordsMillions, double taskSizeDifferentiator) throws IOException {
        Timer.start();
        Process[] workers = new Process[numWorkers];
        String javaBin = System.getProperty("java.home") + File.separator + "bin" + File.separator + "java";
        String classPath = System.getProperty("java.class.path");
        // launch the worker processes
        for (int childId = 0; childId < numWorkers; childId++) {

            // restart the current main with child worker command line arguments
            ProcessBuilder child = new ProcessBuilder(
                    javaBin, "-classpath", classPath, IndexBuilderWorker.class.getCanonicalName(),
                    "--verbosityLevel", String.valueOf(Timer.verbosityLevel),
                    "--serviceHost", serviceHost,
                    "--workerId", String.valueOf(childId),
                    "--numWords", String.valueOf(Math.round(numWordsMillions * 1000000 *
                                                            (1 + 0.66*taskSizeDifferentiator*(Math.random()-0.5))))
            );

            workers[childId] = child.inheritIO().start();
        }
        Timer.measure(1,"%d worker processes have been launched\n", numWorkers);
        return workers;
    }

    private static void shutdownWorkers(Process[] workers) throws InterruptedException {
        Timer.echo(1, "Waiting for %d workers to complete\n", workers.length);
        for (int childId = 0; childId < workers.length; childId++) {
            workers[childId].waitFor();
        }
        Timer.measure(-1, "All worker processes have finished\n");
    }

}
