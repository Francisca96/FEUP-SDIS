package peers;

import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;

import subprotocols.*;

public class InitiatorPeer implements Services {
    private int remote_obj_name;

    public InitiatorPeer(int remote_obj_name) {
        this.remote_obj_name = remote_obj_name;
        initRMI();
    }

    private void initRMI() {

        try {
            //System.setProperty("java.rmi.server.hostname", "172.30.10.228");

            Services service = (Services) UnicastRemoteObject.exportObject(this, 0);
            //LocateRegistry.createRegistry(serviceAccessPoint);
            Registry registry = LocateRegistry.getRegistry();
            System.out.println("bind: "+remote_obj_name);
            registry.rebind(""+remote_obj_name, service);

        } catch (Exception e) {
            e.printStackTrace();
            System.exit(-1);
        }
    }

    @Override
    public String backup(String file_name, int replication_deg) throws RemoteException {
        Backup backup = new Backup(file_name, replication_deg);
        backup.run();
        return null;
    }

    @Override
    public String restore(String file_name) throws RemoteException {
        return null;
    }

    @Override
    public String delete(String file_name) throws RemoteException {
        return null;
    }

    @Override
    public String manage(String file_name) throws RemoteException {
        return null;
    }

    @Override
    public String state() throws RemoteException {
        return null;
    }
}
