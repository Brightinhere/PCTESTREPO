import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
public class RemoteClient {

    private static final int PORT = 1199;

    public static void main(String[] args) throws RemoteException, NotBoundException {
        System.setProperty("java.rmi.server.hostname","192.168.2.13");
        Registry registry = LocateRegistry.getRegistry("localhost", PORT);
        RemoteInterface service = (RemoteInterface) registry.lookup("//localhost/MyService");
        System.out.println(service.helloTo("world!"));
    }

}
