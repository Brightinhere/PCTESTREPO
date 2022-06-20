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
        String host = "192.168.2.13";
        System.out.println("We up and running fool");
        Registry registry = LocateRegistry.createRegistry(PORT);
        registry.rebind("//" + host + "/MyService", new RemoteService());
    }

}
