package peers;

import channels.*;
import subprotocols.Backup;
import subprotocols.Delete;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.net.UnknownHostException;

import javax.xml.crypto.Data;

/**
 * Created by Francisca on 27/03/17.
 */
public class Peer {

    //Server Id
    private static String peer_id;
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


    public static void main(String[] args) throws IOException {
    	/* Needed for Mac OS X */
        System.setProperty("java.net.preferIPv4Stack", "true");
    	
    	if (!checkArguments(args)) {
            return;
        }

        mcChannel = new McChannel(mcAddr, mcPort);
        mdbChannel = new MdbChannel(mcAddr, mcPort);
        mdrChannel = new MdrChannel(mcAddr, mcPort);
        socket = new DatagramSocket();
        init_peer = new InitiatorPeer(peer_id);

        loadData();
        
        //Channel Listening
        mcChannel.thread.start();
        mdbChannel.thread.start();
        mdrChannel.thread.start();
        
        protocoles();

    }

    public static void saveData() {
        try {
            FileOutputStream fileOut =
                    new FileOutputStream("../data_" + Peer.getPeer_id() + "/data.ser");
            ObjectOutputStream output = new ObjectOutputStream(fileOut);
            output.writeObject(data);
            output.close();
            fileOut.close();
        } catch (IOException ex) {
            ex.printStackTrace();
        }
    }

    public static void loadData() {
        try {
            File dataBase = new File("../data_" + Peer.getPeer_id() + "/data.ser");
            if (!dataBase.exists()) {
                System.out.println("Creating new file.");
                File dir = new File("../data_" + Peer.getPeer_id());
                dir.mkdirs();
                dataBase.createNewFile();
                data = new DataBase();
                saveData();
                return;
            }
            FileInputStream fileIn = new FileInputStream("../data_" + Peer.getPeer_id() + "/data.ser");
            ObjectInputStream input = new ObjectInputStream(fileIn);

            data = (DataBase) input.readObject();
            input.close();
            fileIn.close();
            return;
        } catch (ClassNotFoundException ex) {
            System.out.println("Cannot perform input. Class not found.");
        } catch (IOException ex) {
            System.out.println("Could not load data, maybe the file does not exist.");
        }
        data = null;
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
                default:
                    break;
            }
        }

    }

    private static boolean checkArguments(String[] args) throws UnknownHostException {
        if (args.length != 4) {
            System.out.println("Invalid number of arguments! Usage: <peerId> <mcAddr>:<mcPort> <mdbAddr>:<mdbPort> <mdrAddr>:<mdrPort>");
            return false;
        }

        setPeer_id(args[0]);

        setMcAddr(InetAddress.getByName(args[1].split(":")[0]));
        setMdbAddr(InetAddress.getByName(args[2].split(":")[0]));
        setMdrAddr(InetAddress.getByName(args[3].split(":")[0]));

        setMcPort(Integer.parseInt(args[1].split(":")[1]));
        setMcPort(Integer.parseInt(args[2].split(":")[1]));
        setMcPort(Integer.parseInt(args[3].split(":")[1]));

        return true;
    }


    public static String getPeer_id() {
        return peer_id;
    }

    public static void setPeer_id(String peer_id) {
        Peer.peer_id = peer_id;
    }

    public static int getMcPort() {
        return mcPort;
    }

    public static void setMcPort(int mcPort) {
        Peer.mcPort = mcPort;
    }

    public static int getMdbPort() {
        return mdbPort;
    }

    public static void setMdbPort(int mdbPort) {
        Peer.mdbPort = mdbPort;
    }

    public static int getMdrPort() {
        return mdrPort;
    }

    public static void setMdrPort(int mdrPort) {
        Peer.mdrPort = mdrPort;
    }

    public static InetAddress getMcAddr() {
        return mcAddr;
    }

    public static void setMcAddr(InetAddress mcAddr) {
        Peer.mcAddr = mcAddr;
    }

    public static InetAddress getMdbAddr() {
        return mdbAddr;
    }

    public static void setMdbAddr(InetAddress mdbAddr) {
        Peer.mdbAddr = mdbAddr;
    }

    public static InetAddress getMdrAddr() {
        return mdrAddr;
    }

    public static void setMdrAddr(InetAddress mdrAddr) {
        Peer.mdrAddr = mdrAddr;
    }

    public static McChannel getMcChannel() {
        return mcChannel;
    }

    public static void setMcChannel(McChannel mcChannel) {
        Peer.mcChannel = mcChannel;
    }

    public static MdbChannel getMdbChannel() {
        return mdbChannel;
    }

    public static void setMdbChannel(MdbChannel mdbChannel) {
        Peer.mdbChannel = mdbChannel;
    }

    public static MdrChannel getMdrChannel() {
        return mdrChannel;
    }

    public static void setMdrChannel(MdrChannel mdrChannel) {
        Peer.mdrChannel = mdrChannel;
    }

    public static DataBase getData() {
        return data;
    }

    public static void setData(DataBase data) {
        Peer.data = data;
    }

    public static DatagramSocket getSocket() {
        return socket;
    }

    public static void setSocket(DatagramSocket socket) {
        Peer.socket = socket;
    }
}
