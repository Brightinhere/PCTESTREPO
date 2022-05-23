import java.rmi.Remote;
import java.rmi.RemoteException;
import java.util.Map;

public interface IndexBuilderInterface extends Remote {

    // child worker calls this method to deliver a local index to the master service
    void processIndex(Map<Character,Integer> index) throws RemoteException;

    // child worker calls this method to deliver a local index entry to the master service
    void processIndexEntry(char firstCharacter, int numWords) throws RemoteException;
}
