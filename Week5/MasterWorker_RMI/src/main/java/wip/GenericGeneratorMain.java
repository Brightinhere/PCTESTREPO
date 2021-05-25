package wip;

import common.Timer;

import java.io.IOException;
import java.rmi.NotBoundException;
import java.rmi.server.UnicastRemoteObject;
import java.util.Scanner;

public class GenericGeneratorMain {

    private static final int SERVICE_PORT = 49992;
    private static final String SERVICE_NAME = "/genericTasksToDo";

    private static int averageTaskSize = 10;
    private static double taskSizeDifferentiator = 1.0;

    public static void main(String[] args) throws InterruptedException, IOException, NotBoundException {
        String serviceHost = GenericTaskMaster.getExternalIPAddress();

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
                GenericTaskMaster.<Double>workerMain(workerId, serviceHost, SERVICE_NAME, SERVICE_PORT);
                return;
            }
        }

        // if no worker launched, continue as the master-service
        Scanner input = new Scanner(System.in);

        System.out.printf("Welcome Distributed Generic Task Master/Worker test on %s\n", serviceHost);
        System.out.printf("\nPlease provide your input and output verbosity level [-1,0,1,2]: ");
        Timer.verbosityLevel = input.nextInt();
        int numWorkers = 2;
        int numTasks = 10;
        if (Timer.verbosityLevel >= 0) {
            System.out.print("Please provide the number of worker processes: ");
            numWorkers = input.nextInt();
            System.out.print("Please provide the total number of tasks: ");
            numTasks = input.nextInt();
            System.out.print("Please provide the average task size (in millions): ");
            averageTaskSize = input.nextInt();
            System.out.print("Please provide a differentiation factor for task sizes [0..3, 0=equal sizes]: ");
            taskSizeDifferentiator = input.nextDouble();
        }

        // create and register the master service object
        // needs to have code for the tasks to be distributed and a method to aggregate the results.
        //wip.GenericTaskMaster<Double> master = new wip.GenericTaskMaster<>(numTasks, wip.GenericGeneratorMain::workerTask, Double::sum);
        GenericTaskMaster<Double> master =
                new GenericTaskMaster<>(numTasks, GenericGeneratorMain::workerTask0, Double::sum, 1.0);
        master.register(serviceHost, SERVICE_NAME, SERVICE_PORT);

        // launch separate worker processes
        Timer.echo(-1, "\nLaunching %d workers at %s for %d tasks each on %d million numbers\n",
                numWorkers, serviceHost, numTasks, averageTaskSize);
        Process[] workers = master.launchWorkersAtLocalHost(numWorkers, serviceHost, GenericGeneratorMain.class);

        // wait until all workers have finished
        master.shutdownWorkers(workers);

        // present the results as obtained by the master
        System.out.printf("%d workers have completed %d tasks yielding a total sum of %.4fM\n",
                numWorkers,
                master.getNumTasksCompleted(),
                master.getAccumulatedResult() / 1000000);

        UnicastRemoteObject.unexportObject(master, true);
        // registry.unbind("//" + serviceHost + SERVICE_NAME);
    }

    /**
     * Worker task code, which will know what to do exclusively on the basis of a task number
     * @param taskNr    helps defining the task (contributes to randomization of the task size)
     * @return          the calculated result
     */
    private static Double workerTask(Integer taskNr) {
        long averageNumbers = averageTaskSize * 1000000L;
        long nNumbers = Math.round(Double.min(10*averageNumbers, Double.max(1,
                10*averageNumbers * (1.0 + taskSizeDifferentiator * (0.6 + 0.067 * taskNr % 3) * (Math.random()-0.5))
        )));
        double sum = 0.0;
        for (long i = 0; i < nNumbers; i++) {
            sum += Math.random();
        }
        return sum;
    }

    public static Double workerTask0(Integer taskNr) {
        return 1.0;
    }
}
