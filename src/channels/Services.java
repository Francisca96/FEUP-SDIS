import java.rmi.Remote;
import java.rmi.RemoteException;

/**
 * Created by Francisca on 29/03/17.
 */
public interface Services extends Remote {

    String backup(String file_name, int replication_deg) throws RemoteException;

    String restore(String file_name) throws RemoteException;

    String delete(String file_name) throws RemoteException;

    String manage(String file_name) throws RemoteException;

    String state() throws RemoteException;
}
