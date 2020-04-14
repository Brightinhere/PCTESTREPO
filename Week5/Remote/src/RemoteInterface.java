import java.rmi.Remote;
import java.rmi.RemoteException;

public interface RemoteInterface extends Remote {

    String helloTo(String name) throws RemoteException;

}
