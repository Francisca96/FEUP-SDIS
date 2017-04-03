import java.rmi.Remote;
import java.rmi.RemoteException;

/**
 * Created by Francisca on 29/03/17.
 */
public interface Services extends Remote {

    public void backup() throws RemoteException;
    public void restore() throws RemoteException;
    public void delete() throws RemoteException;
    public void manage() throws RemoteException;
    public void state() throws RemoteException;
}
