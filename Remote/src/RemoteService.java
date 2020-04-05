import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;

public class RemoteService extends UnicastRemoteObject implements RemoteInterface {

    private static final int PORT = 1199;
    private static final long serialVersionUID = 1L;

    private RemoteService() throws RemoteException {
        super();
    }

    public String helloTo(String name) throws RemoteException {
        return "Hello, " + name;
    }

    public static void main(String[] args) throws RemoteException {
        Registry registry = LocateRegistry.createRegistry(PORT);
        registry.rebind("//localhost/MyService", new RemoteService());
    }

}
