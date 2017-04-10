package peers;

import channels.*;
import subprotocols.Backup;
import subprotocols.Delete;
import subprotocols.Reclaim;
import subprotocols.Restore;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.UnknownHostException;

public class Peer {

    //Server Id
    private static String peer_id;
    private static int serviceAccessPoint;
    private static String protocole_version;
    //Ports
    private static int mcPort;
    private static int mdbPort;
    private static int mdrPort;
    //Addresses
    private static InetAddress mcAddr;
    private static InetAddress mdbAddr;
    private static InetAddress mdrAddr;
    //Channels
    private static McChannel mcChannel;
    private static MdbChannel mdbChannel;
    private static MdrChannel mdrChannel;

    private static DataBase data;
    private static DatagramSocket socket;

    private static InitiatorPeer init_peer;


    public static void main(String[] args) throws IOException, ClassNotFoundException {
    	/* Needed for Mac OS X */
        System.setProperty("java.net.preferIPv4Stack", "true");
    	
    	if (!checkArguments(args)) {
            return;
        }

        mcChannel = new McChannel(mcAddr, mcPort);
        mdbChannel = new MdbChannel(mdbAddr, mdbPort);
        mdrChannel = new MdrChannel(mdrAddr, mdrPort);
        socket = new DatagramSocket();
        init_peer = new InitiatorPeer(peer_id, serviceAccessPoint);

        loadData();
        
        //Channel Listening
        mcChannel.thread.start();
        mdbChannel.thread.start();
        mdrChannel.thread.start();
        
        protocoles();

    }

    public static void saveData() {
        try {
            FileOutputStream file =
            new FileOutputStream("../database_" + Peer.getPeer_id() + "/database.ser");
            ObjectOutputStream output = new ObjectOutputStream(file);
            output.writeObject(data);
            output.close();
            file.close();
        } catch (IOException ex) {
            ex.printStackTrace();
        }
    }

    public static void loadData() throws IOException, ClassNotFoundException {
            File dataBase = new File("../database_" + Peer.getPeer_id() + "/database.ser");
            if (!dataBase.exists()) {
                File dir = new File("../database_" + Peer.getPeer_id());
                dir.mkdirs();
                dataBase.createNewFile();
                data = new DataBase();
                saveData();
                return;
            }
            
            FileInputStream fileIn = new FileInputStream("../database_" + Peer.getPeer_id() + "/database.ser");
            ObjectInputStream input = new ObjectInputStream(fileIn);

            data = (DataBase) input.readObject();
            input.close();
            fileIn.close();
            return;
    }


    private static void protocoles() throws IOException {
        String protocole = "";
        while (true) {
            byte[] buf = new byte[256];
            DatagramPacket packet = new DatagramPacket(buf, buf.length);
            socket.receive(packet);
            protocole = new String(packet.getData(), 0, packet.getLength());
            String[] protocoleArray = protocole.split("\\s+");
            switch (protocoleArray[0]) {
                case "BACKUP":
                    Backup backup = new Backup(protocoleArray[1], Integer.parseInt(protocoleArray[2]));
                    backup.start();
                    break;
                case "DELETE":
                	Delete delete = new Delete(protocoleArray[1]);
                	delete.start();
                	break;
                case "RESTORE":
                	Restore restore = new Restore(protocoleArray[1]);
                	restore.start();
                case "RECLAIM":
                	Reclaim reclaim = new Reclaim(Integer.parseInt(protocoleArray[1]));
                	reclaim.start();
                default:
                    break;
            }
        }

    }

    private static boolean checkArguments(String[] args) throws UnknownHostException {
        if (args.length != 6) {
            System.out.println("Invalid number of arguments! Usage: <ProtocoleVersion> <peerId> <serviceAccessPoint> <mcAddr>:<mcPort> <mdbAddr>:<mdbPort> <mdrAddr>:<mdrPort>");
            return false;
        }
        
        setProtocoleVersion(args[0]);
        setPeer_id(args[1]);
        serviceAccessPoint = Integer.parseInt(args[2]);

        setMcAddr(InetAddress.getByName(args[3].split(":")[0]));
        setMdbAddr(InetAddress.getByName(args[4].split(":")[0]));
        setMdrAddr(InetAddress.getByName(args[5].split(":")[0]));

        setMcPort(Integer.parseInt(args[3].split(":")[1]));
        setMcPort(Integer.parseInt(args[4].split(":")[1]));
        setMcPort(Integer.parseInt(args[5].split(":")[1]));

        return true;
    }


    private static void setProtocoleVersion(String version) {
		Peer.protocole_version = version;
		
	}

	public static String getPeer_id() {
        return peer_id;
    }

    public static void setPeer_id(String peer_id) {
        Peer.peer_id = peer_id;
    }

    public static void setMcPort(int mcPort) {
        Peer.mcPort = mcPort;
    }

    public static void setMcAddr(InetAddress mcAddr) {
        Peer.mcAddr = mcAddr;
    }

    public static void setMdbAddr(InetAddress mdbAddr) {
        Peer.mdbAddr = mdbAddr;
    }

    public static void setMdrAddr(InetAddress mdrAddr) {
        Peer.mdrAddr = mdrAddr;
    }

    public static McChannel getMcChannel() {
        return mcChannel;
    }

    public static MdbChannel getMdbChannel() {
        return mdbChannel;
    }

    public static MdrChannel getMdrChannel() {
        return mdrChannel;
    }

    public static DataBase getData() {
        return data;
    }

	public static String getProtocole_version() {
		return protocole_version;
	}
    
    
    
}
