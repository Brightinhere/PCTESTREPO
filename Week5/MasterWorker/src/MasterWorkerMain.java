import java.io.File;
import java.io.IOException;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;
import java.util.Scanner;
import java.util.function.Supplier;

public class MasterWorkerMain  {

    private static final int SERVICE_PORT = 49991;
    private static final String SERVICE_NAME = "/tasksToDo";

    public static void main(String[] args) throws InterruptedException, IOException, NotBoundException {
        String serviceHost = "localhost";
        int workerId = -1;

        // process command line arguments
        for (int i = 0; i < args.length; i++) {
            if (args[i].equals("--workerId")) {
                i++;
                workerId = Integer.valueOf(args[i]);
            } else if (args[i].equals("--serviceHost")) {
                i++;
                serviceHost = args[i];
            }
        }

        // if launching a worker, invoke the worker code
        if (workerId >= 0) {
            workerMain(workerId, serviceHost);
            return;
        }

        Scanner input = new Scanner(System.in);

        System.out.println("Welcome Distributed Master/Worker test");
        System.out.print("Please provide the number of numbers (in millions): ");
        int numNumbersMilions = input.nextInt();
        System.out.print("Please provide the number of worker processes: ");
        int numWorkers = input.nextInt();
        System.out.print("Please provide the total number of tasks: ");
        int numTasks = input.nextInt();

        long startTime = System.currentTimeMillis();
        // create the remote object register
        Registry registry = LocateRegistry.createRegistry(SERVICE_PORT);
        // create a new master service and register it
        MasterService masterService = new MasterService(numNumbersMilions*1000000L, numTasks);
        registry.rebind("//" + serviceHost + SERVICE_NAME, masterService);
        long sampleTime = System.currentTimeMillis();
        System.out.printf("Creation of registry took %d msecs\n", sampleTime - startTime);
        startTime = sampleTime;

        // launch the worker processes
        Process[] workers = new Process[numWorkers];
        String javaBin = System.getProperty("java.home") + File.separator + "bin" + File.separator + "java";
        String classPath = System.getProperty("java.class.path");
        for (int childId = 0; childId < numWorkers; childId++) {
            ProcessBuilder child = new ProcessBuilder(
                    javaBin, "-classpath", classPath, MasterWorkerMain.class.getSimpleName(),
                    "--serviceHost", serviceHost,
                    "--workerId", String.valueOf(childId)
            );
            workers[childId] = child.inheritIO().start();
        }
        sampleTime = System.currentTimeMillis();
        System.out.printf("Launching worker processes took %d msecs\n", sampleTime - startTime);
        startTime = sampleTime;

        // wait until all workers have finished
        System.out.printf("Waiting for %d workers to complete %d tasks\n", numWorkers, numTasks);
        for (int childId = 0; childId < numWorkers; childId++) {
            workers[childId].waitFor();
        }
        sampleTime = System.currentTimeMillis();
        System.out.printf("Completing all worker processes took %d msecs\n", sampleTime - startTime);
        startTime = sampleTime;

        // present the result
        System.out.printf("%d workers have completed %d tasks yielding a total sum of %.4fM\n",
                numWorkers,
                masterService.getTasksCompleted(),
                masterService.getCalculatedSum()/1000000);

        UnicastRemoteObject.unexportObject(masterService, true);
    }

    private static void workerMain(int workerId, String registerHost) throws RemoteException, NotBoundException {
        System.out.printf("Worker-%d is up and running\n", workerId);
        Registry registry = LocateRegistry.getRegistry(registerHost, SERVICE_PORT);
        MasterServiceInterface service = (MasterServiceInterface) registry.lookup("//" + registerHost + SERVICE_NAME);


        Supplier<Double> calculationTask = service.getCalculationTask(workerId);
        while (calculationTask != null) {
            double result = calculationTask.get();
            //System.out.printf("Worker-%d has completed a task with result %.1fK\n", workerId, result / 1000);
            service.processCalculationResult(workerId, result);
            calculationTask = service.getCalculationTask(workerId);
        }
    }
}
