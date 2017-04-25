package peers;

import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;

import subprotocols.*;

public class InitiatorPeer implements Services {
    private String remote_obj_name;
    private int serviceAccessPoint;

    public InitiatorPeer(String remote_obj_name, int serviceAccessPoint) {
        this.remote_obj_name = remote_obj_name;
        this.serviceAccessPoint = serviceAccessPoint;
        initRMI();
    }

    private void initRMI() {

        try {
            System.setProperty("java.rmi.server.hostname", "192.168.1.5");

            Services service = (Services) UnicastRemoteObject.exportObject(this, serviceAccessPoint);
            LocateRegistry.createRegistry(serviceAccessPoint);
            Registry registry = LocateRegistry.getRegistry(serviceAccessPoint);
            System.out.println("bind: "+remote_obj_name);
            registry.bind(remote_obj_name, service);

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
