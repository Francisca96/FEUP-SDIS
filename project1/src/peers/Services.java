package peers;

import java.io.FileNotFoundException;
import java.rmi.Remote;
import java.rmi.RemoteException;

public interface Services extends Remote {

    String backup(String file_name, int replication_deg) throws RemoteException;

    String restore(String file_name) throws RemoteException, FileNotFoundException;

    String delete(String file_name) throws RemoteException;

    String manage(String file_name) throws RemoteException;

    String state() throws RemoteException;
}
