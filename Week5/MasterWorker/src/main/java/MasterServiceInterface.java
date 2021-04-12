import java.rmi.Remote;
import java.rmi.RemoteException;
import java.util.function.Supplier;

public interface MasterServiceInterface extends Remote {

    Supplier<Double> getCalculationTask(int workerId) throws RemoteException;
    void processCalculationResult(int workerId, Double result) throws RemoteException;;
}
