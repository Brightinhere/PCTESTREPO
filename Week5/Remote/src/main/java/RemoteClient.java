import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
public class RemoteClient {

    private static final int PORT = 1199;

    public static void main(String[] args) throws RemoteException, NotBoundException {
//        System.setProperty("java.rmi.server.hostname","192");
        try{
            String host = "192.168.2.13";
            Registry registry = LocateRegistry.getRegistry(host, PORT);
            RemoteInterface service = (RemoteInterface) registry.lookup("//" + host + "/MyService");
            System.out.println(service.helloTo("world!"));
        } catch (Exception e) {
            System.out.println(e);
        }

    }

}
