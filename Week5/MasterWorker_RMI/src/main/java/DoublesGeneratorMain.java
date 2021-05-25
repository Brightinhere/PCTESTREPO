import common.MasterServiceInterface;
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
import java.util.function.Supplier;

public class DoublesGeneratorMain {

    private static final int SERVICE_PORT = 49991;
    private static final String SERVICE_NAME = "/doubleTasksToDo";

    public static void main(String[] args) throws InterruptedException, IOException, NotBoundException {
        String serviceHost = getExternalIPAddress();

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
                int workerId = Integer.valueOf(args[i]);

                // launch the worker
                workerMain(workerId, serviceHost);
                return;
            }
        }

        // if no worker launched, continue as the master-service
        Scanner input = new Scanner(System.in);

        System.out.printf("Welcome Distributed Doubles Generator Master/Worker test on %s\n", serviceHost);
        System.out.printf("\nPlease provide your input and output verbosity level [-1,0,1,2]: ");
        Timer.verbosityLevel = input.nextInt();
        int numNumbersMillions = 250;
        int numWorkers = 2;
        int numTasks = 10;
        double taskSizeDifferentiator = 1.0;
        if (Timer.verbosityLevel >= 0) {
            System.out.print("Please provide the number of numbers to generate (in millions): ");
            numNumbersMillions = input.nextInt();
            System.out.print("Please provide the number of worker processes: ");
            numWorkers = input.nextInt();
            System.out.print("Please provide the total number of tasks: ");
            numTasks = input.nextInt();
            System.out.print("Please provide a differentiation factor for task sizes [0..3, 0=equal sizes]: ");
            taskSizeDifferentiator = input.nextDouble();
        }

        // create and register the master service object
        DoublesGeneratorMaster master = new DoublesGeneratorMaster(numNumbersMillions * 1000000L, numTasks, taskSizeDifferentiator);
        registerMaster(master, serviceHost);

        // launch separate worker processes
        Timer.echo(-1, "\nLaunching %d workers at %s for %d tasks on %d million random numbers\n",
                numWorkers, serviceHost, numTasks, numNumbersMillions);
        Process[] workers = launchWorkersAtLocalHost(numWorkers, serviceHost);

        // wait until all workers have finished
        shutdownWorkers(workers);

        // present the results as obtained by the master
        System.out.printf("%d workers have completed %d tasks of sizes %d-%d yielding a total sum of %.4fM\n",
                numWorkers,
                master.getNumTasksCompleted(),
                master.getMinTaskSize(), master.getMaxTaskSize(),
                master.getAccumulatedResult() / 1000000);

        UnicastRemoteObject.unexportObject(master, true);
        // registry.unbind("//" + serviceHost + SERVICE_NAME);
    }

    private static void workerMain(int workerId, String registerHost) throws RemoteException, NotBoundException {
        Timer.echo(2, "Worker-%d is up and running\n", workerId);
        Registry registry = LocateRegistry.getRegistry(registerHost, SERVICE_PORT);
        MasterServiceInterface<Double> masterService = (MasterServiceInterface)registry.lookup("//" + registerHost + SERVICE_NAME);

        int tasksCompleted = 0;

        // get tasks from the master and execute them
        Supplier<Double> calculationTask = masterService.getCalculationTask(workerId);
        while (calculationTask != null) {
            // evaluate the calculation task function and retrieve the result
            double result = calculationTask.get();
            Timer.echo(2, "Worker-%d has completed task-%d with result %.1fK\n", workerId, tasksCompleted, result / 1000);
            masterService.processCalculationResult(workerId, tasksCompleted, result);
            tasksCompleted++;
            calculationTask = masterService.getCalculationTask(workerId);
        }

        Timer.echo(-1, "Worker-%d has completed %d tasks\n", workerId, tasksCompleted);
    }

    private static String getExternalIPAddress() throws UnknownHostException {
        String ipa = "localhost";
        ipa = InetAddress.getLocalHost().getHostAddress().toString();
        return ipa;
    }

    private static void registerMaster(MasterServiceInterface master, String serviceHost) throws RemoteException {
        Timer.start();
        // create the remote object register
        Registry registry = LocateRegistry.createRegistry(SERVICE_PORT);
        // create a new master service and register it

        registry.rebind("//" + serviceHost + SERVICE_NAME, master);
        long sampleTime = System.currentTimeMillis();
        Timer.measure(2, "Creation of registry has completed\n");
    }

    private static Process[] launchWorkersAtLocalHost(int numWorkers, String serviceHost) throws IOException {
        Timer.start();
        Process[] workers = new Process[numWorkers];
        String javaBin = System.getProperty("java.home") + File.separator + "bin" + File.separator + "java";
        String classPath = System.getProperty("java.class.path");
        // launch the worker processes
        for (int childId = 0; childId < numWorkers; childId++) {

            // restart the current main with child worker command line arguments
            ProcessBuilder child = new ProcessBuilder(
                    javaBin, "-classpath", classPath, DoublesGeneratorMain.class.getSimpleName(),
                    "--verbosityLevel", String.valueOf(Timer.verbosityLevel),
                    "--serviceHost", serviceHost,
                    "--workerId", String.valueOf(childId)
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
