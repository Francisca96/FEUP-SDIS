import peers.Services;

import java.io.IOException;
import java.net.UnknownHostException;
import java.rmi.NotBoundException;
import java.rmi.registry.Registry;
import java.rmi.registry.LocateRegistry;

/**
 * Created by Francisca on 29/03/17.
 */
public class TestApp {
    private static String remote_obj_name;
    private static String operation;
    private static String file_name;
    private static int replication_deg;

    public static void main(String[] args) throws IOException {
        if(!checkArguments(args)){
            return;
        }

        Services service;
        Registry registry = LocateRegistry.getRegistry("localhost");

        try {
            service = (Services) registry.lookup(remote_obj_name);
            switch(operation) {
                case "BACKUP":
                    service.backup(file_name, replication_deg);
                    break;
                case "RESTORE":
                    service.restore(file_name);
                    break;
                case "DELETE":
                    service.delete(file_name);
                    break;
                case "MANAGE":
                    service.manage(file_name);
                    break;
                case "STATE":
                    service.state();
                    break;
                default:
                    System.out.println("Invalid Request");
                    System.exit(-1);
            }
        } catch (NotBoundException e) {
            e.printStackTrace();
        }
    }

    private static boolean checkArguments(String[] args) throws UnknownHostException {
        if(args.length < 2 || args.length > 4){
            System.out.println("Invalid number of arguments! Usage: java TestApp <peer_ap> <operation> <opnd_1> <opnd_2>");
            return false;
        }

        setRemote_obj_name(args[0]);
        setOperation(args[1]);

        switch(operation) {
            case "BACKUP":
                file_name = args[2];
                replication_deg = Integer.parseInt(args[3]);
                break;
            case "RESTORE":
                file_name = args[2];
                break;
            case "DELETE":
                file_name = args[2];
                break;
            case "MANAGE":
                file_name = args[2];
                break;
            case "STATE":
                break;
            default:
                System.out.println("Invalid Subprotocol");
                System.exit(-1);
        }

        return true;
    }


    public static String getRemote_obj_name() {
        return remote_obj_name;
    }

    public static void setRemote_obj_name(String remote_obj_name) {
        TestApp.remote_obj_name = remote_obj_name;
    }

    public static String getOperation() {
        return operation;
    }

    public static void setOperation(String operation) {
        TestApp.operation = operation;
    }

    public static String getFile_name() {
        return file_name;
    }

    public static void setFile_name(String file_name) {
        TestApp.file_name = file_name;
    }

    public static int getReplication_deg() {
        return replication_deg;
    }

    public static void setReplication_deg(int replication_deg) {
        TestApp.replication_deg = replication_deg;
    }
}
