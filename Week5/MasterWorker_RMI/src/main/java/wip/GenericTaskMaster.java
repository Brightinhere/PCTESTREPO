package wip;

import common.MasterServiceInterface;
import common.Timer;

import java.io.File;
import java.io.IOException;
import java.io.Serializable;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;
import java.util.function.BinaryOperator;
import java.util.function.Function;
import java.util.function.Supplier;

public class GenericTaskMaster<R> extends UnicastRemoteObject implements MasterServiceInterface<R> {

    private int nTasksToGive;
    private Function<Integer,R> taskCode;
    private BinaryOperator<R> resultAccumulator;


    public GenericTaskMaster(int nTasks, Function<Integer,R> taskCode, BinaryOperator<R> resultAccumulator) throws RemoteException {
        super();
        this.nTasksToGive = nTasks;
        this.taskCode = taskCode;
        this.resultAccumulator = resultAccumulator;
    }
    private R defaultResult;

    public GenericTaskMaster(int nTasks, Function<Integer,R> taskCode, BinaryOperator<R> resultAccumulator, R defaultResult) throws RemoteException {
        this(nTasks, taskCode, resultAccumulator);
        this.defaultResult = defaultResult;
    }

    private R accumulatedResult = null;
    private int numTasksGiven = 0;
    private int numTasksCompleted = 0;

    // getters for the aggregated results
    public R getAccumulatedResult() {
        return this.accumulatedResult;
    }
    public int getNumTasksGiven() {
        return this.numTasksGiven;
    }
    public int getNumTasksCompleted() {
        return this.numTasksCompleted;
    }

    /**
     * remote interface method by which a worker asks the master service for a next task to be executed
     * its access is synchronised to prevent interference from multiple simultaneous client calls
     * @param workerId  the id of the worker, not used in this example
     * @return
     */
    @Override
    synchronized public Supplier<R> getCalculationTask(int workerId)  {
        if (this.numTasksGiven < this.nTasksToGive) {
            // consolidate the taskNr in the closure of the task code
            final int taskNr = this.numTasksGiven;
            final R result = this.defaultResult;
            final Function<Integer,R> task = this.taskCode;
            this.numTasksGiven++;
            // deliver the task as a function of type Supplier<Double>
            //return (Supplier<R> & Serializable)(() -> { return this.taskCode.apply(taskNr); });
            return (Supplier<R> & Serializable)(() -> { return task.apply(taskNr); });
        }
        return null;
    }

    /**
     * remote interface method by which a worker delivers an outcome of a task the master service
     * its access is synchronised to prevent interference from multiple simultaneous client calls
     * @param workerId  the id of the worker, not used in this example
     * @param taskNr    the sequence number of the task, not used in this example
     * @param result    the result of the task, as computed by the worker
     */
    @Override
    synchronized public void processCalculationResult(int workerId, int taskNr, R result) {
        Timer.echo(2, "Worker-%d has submitted result %s of task-%d\n", workerId, result.toString(), taskNr);
        if (this.accumulatedResult == null) {
            this.accumulatedResult = result;
        } else {
            this.accumulatedResult = this.resultAccumulator.apply(this.accumulatedResult, result);
        }
        this.numTasksCompleted++;
    }

    public void register(String serviceHost, String serviceName, int servicePort) throws RemoteException {
        Timer.start();
        // create the remote object register
        Registry registry = LocateRegistry.createRegistry(servicePort);
        // create a new master service and register it

        registry.rebind("//" + serviceHost + serviceName, this);
        long sampleTime = System.currentTimeMillis();
        Timer.measure(2, "Creation of registry has completed\n");
    }

    public Process[] launchWorkersAtLocalHost(int numWorkers, String serviceHost, Class mainClass) throws IOException {
        Timer.start();
        Process[] workers = new Process[numWorkers];
        String javaBin = System.getProperty("java.home") + File.separator + "bin" + File.separator + "java";
        String classPath = System.getProperty("java.class.path");
        // launch the worker processes
        for (int childId = 0; childId < numWorkers; childId++) {

            // restart the current main with child worker command line arguments
            ProcessBuilder child = new ProcessBuilder(
                    javaBin, "-classpath", classPath, mainClass.getCanonicalName(),
                    "--verbosityLevel", String.valueOf(Timer.verbosityLevel),
                    "--serviceHost", serviceHost,
                    "--workerId", String.valueOf(childId)
            );

            workers[childId] = child.inheritIO().start();
        }
        Timer.measure(1,"%d worker processes have been launched\n", numWorkers);
        return workers;
    }

    public void shutdownWorkers(Process[] workers) throws InterruptedException {
        Timer.echo(1, "Waiting for %d workers to complete\n", workers.length);
        for (int childId = 0; childId < workers.length; childId++) {
            workers[childId].waitFor();
        }
        Timer.measure(-1, "All worker processes have finished\n");
    }

    public static <RR> void workerMain(int workerId, String registerHost, String serviceName, int servicePort) throws RemoteException, NotBoundException {
        Timer.echo(2, "Worker-%d is up and running\n", workerId);
        Registry registry = LocateRegistry.getRegistry(registerHost, servicePort);
        MasterServiceInterface<RR> masterService = (MasterServiceInterface<RR>) registry.lookup("//" + registerHost + serviceName);

        int tasksCompleted = 0;

        // get tasks from the master and execute them
        Supplier<RR> calculationTask = masterService.getCalculationTask(workerId);
        while (calculationTask != null) {
            // evaluate the calculation task function and retrieve the result
            RR result = calculationTask.get();
            Timer.echo(2, "Worker-%d has completed task-%d with result %s\n", workerId, tasksCompleted, result.toString());
            masterService.processCalculationResult(workerId, tasksCompleted, result);
            tasksCompleted++;
            calculationTask = masterService.getCalculationTask(workerId);
        }

        Timer.echo(-1, "Worker-%d has completed %d tasks\n", workerId, tasksCompleted);
    }

    public static String getExternalIPAddress() throws UnknownHostException {
        String ipa = "localhost";
        ipa = InetAddress.getLocalHost().getHostAddress().toString();
        return ipa;
    }
}
