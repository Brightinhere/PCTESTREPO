import java.io.Serializable;
import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import java.util.function.Supplier;

public class MasterService extends UnicastRemoteObject implements MasterServiceInterface {

    public MasterService(long nNumbers, int nTasks) throws RemoteException {
        super();
        this.nNumbersToDo = nNumbers;
        this.nTasksToGive = nTasks;
    }

    private long nNumbersToDo;
    private int nTasksToGive;
    synchronized public Supplier<Double> getCalculationTask(int workerId)  {

        if (this.nTasksToGive > 0) {
            final long nNumbers;
            if (this.nTasksToGive > 1) {
                nNumbers = Math.round((this.nNumbersToDo / this.nTasksToGive) * (2.5 + Math.random()) / 3);
            } else {
                nNumbers = this.nNumbersToDo;
            }
            System.out.printf("Gave task-%d with %d numbers to worker-%d\n", this.nTasksToGive, nNumbers, workerId);
            this.nNumbersToDo -= nNumbers;
            this.nTasksToGive--;
            return (Supplier<Double> & Serializable)
                    ()->{ return calculateSumOfRandomNumbers(nNumbers); };
        }
        return null;
    }

    public double getCalculatedSum() {
        return this.calculatedSum;
    }
    public int getTasksCompleted() {
        return this.tasksCompleted;
    }

    private double calculatedSum = 0.0;
    private int tasksCompleted = 0;
    synchronized public void processCalculationResult(int workerId, Double result) {
        System.out.printf("Worker-%d has submitted result %.4f\n", workerId, result);
        this.calculatedSum += result;
        this.tasksCompleted++;
    }

    private static Double calculateSumOfRandomNumbers(long nNumbers) {
        double sum = 0.0;
        for (long i = 0; i < nNumbers; i++) {
            sum += Math.random();
        }
        return sum;
    }
}
