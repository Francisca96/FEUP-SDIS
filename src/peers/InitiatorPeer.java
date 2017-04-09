package peers;

import java.net.UnknownHostException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;

import subprotocols.*;

public class InitiatorPeer implements Services {
    private static String remote_obj_name;

    public InitiatorPeer(String remote_obj_name){
        InitiatorPeer.remote_obj_name = remote_obj_name;
    }

    public static void main(String args[]) throws UnknownHostException, RemoteException {
        if (!checkArguments(args)) {
            return;
        }

        try {
            InitiatorPeer init_peer = new InitiatorPeer(remote_obj_name);

            Services service = (Services) UnicastRemoteObject.exportObject(init_peer, 1099);
            //LocateRegistry.createRegistry(1099);
            Registry registry = LocateRegistry.getRegistry();
            registry.rebind(remote_obj_name, service);

        } catch(Exception e) {
            e.printStackTrace();
            System.exit(-1);
        }
    }

    private static boolean checkArguments(String[] args) throws UnknownHostException {
        if (args.length != 1) {
            System.out.println("Invalid number of arguments! Usage: <remote_obj_name>");
            return false;
        }

        remote_obj_name = args[0];

        return true;

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
