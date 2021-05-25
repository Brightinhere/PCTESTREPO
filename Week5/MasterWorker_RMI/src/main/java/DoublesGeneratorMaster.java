import common.MasterServiceInterface;
import common.Timer;

import java.io.Serializable;
import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import java.util.function.Supplier;

public class DoublesGeneratorMaster extends UnicastRemoteObject implements MasterServiceInterface<Double> {

    private long nNumbersToDo;
    private int nTasksToGive;
    private double taskSizeDifferentiator = 1.0;

    public DoublesGeneratorMaster(long nNumbers, int nTasks) throws RemoteException {
        super();
        this.nNumbersToDo = nNumbers;
        this.nTasksToGive = nTasks;
    }
    public DoublesGeneratorMaster(long nNumbers, int nTasks, double taskSizeDifferentiator) throws RemoteException {
        this(nNumbers, nTasks);
        this.taskSizeDifferentiator = Double.max(0, Double.min(3, taskSizeDifferentiator));
    }

    private double accumulatedResult = 0.0;
    private int numTasksGiven = 0;
    private int numTasksCompleted = 0;
    private long minTaskSize = Long.MAX_VALUE;
    private long maxTaskSize = 0;

    // getters for the aggregated results
    public Double getAccumulatedResult() {
        return this.accumulatedResult;
    }
    public int getNumTasksGiven() {
        return this.numTasksGiven;
    }
    public int getNumTasksCompleted() {
        return this.numTasksCompleted;
    }

    public long getMinTaskSize() { return minTaskSize; }
    public long getMaxTaskSize() { return maxTaskSize; }

    /**
     * remote interface method by which a worker asks the master service for a next task to be executed
     * its access is synchronised to prevent interference from multiple simultaneous client calls
     * @param workerId  the id of the worker, not used in this example
     * @return
     */
    @Override
    synchronized public Supplier<Double> getCalculationTask(int workerId)  {
        int remainingTasksToGive = this.nTasksToGive - this.numTasksGiven;
        if (remainingTasksToGive > 0) {
            final long nNumbers;
            if (remainingTasksToGive > 1) {
                // calculate a semi-random task size, depending on the differentiator parameter
                nNumbers = Math.round(Double.min(this.nNumbersToDo, Double.max(1,
                        (this.nNumbersToDo / remainingTasksToGive) *
                        (1 + this.taskSizeDifferentiator * 0.67 * (Math.random()-0.5))
                )));
            } else {
                nNumbers = this.nNumbersToDo;
            }

            // track minimum and maximum of task sizes
            this.minTaskSize = Long.min(this.minTaskSize, nNumbers);
            this.maxTaskSize = Long.max(this.maxTaskSize, nNumbers);

            Timer.echo(2, "Gave task-%d with %d numbers to worker-%d\n", this.nTasksToGive, nNumbers, workerId);
            this.nNumbersToDo -= nNumbers;
            this.numTasksGiven++;

            // deliver the task as a function of type Supplier<Double>
            return (Supplier<Double> & Serializable)
                    ()-> calculateSumOfRandomNumbers(nNumbers);
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
    synchronized public void processCalculationResult(int workerId, int taskNr, Double result) {
        Timer.echo(2, "Worker-%d has submitted result %.4f of task-%d\n", workerId, result, taskNr);
        this.accumulatedResult += result;
        this.numTasksCompleted++;
    }

    /**
     * Actual algorithm to be executed by the child worker.
     * this algorithm will be packaged as a Supplier<Double> and given to the child.
     * It will get its actual parameter from the context (closure) of the master service.
     * @param nNumbers  the number of random numbers that shall be generated
     * @return          the sum of all generated random numbers
     */
    private static Double calculateSumOfRandomNumbers(long nNumbers) {
        double sum = 0.0;
        for (long i = 0; i < nNumbers; i++) {
            sum += Math.random();
        }
        return sum;
    }
}
