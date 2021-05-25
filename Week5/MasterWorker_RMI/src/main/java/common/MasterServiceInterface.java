package common;

import java.rmi.Remote;
import java.rmi.RemoteException;
import java.util.function.Supplier;

public interface MasterServiceInterface<R> extends Remote {

    // child worker calls this method to obtain a next task from the master service
    Supplier<R> getCalculationTask(int workerId) throws RemoteException;

    // child worker calls this method to deliver a task result to the master service
    void processCalculationResult(int workerId, int taskNr, R result) throws RemoteException;
}
