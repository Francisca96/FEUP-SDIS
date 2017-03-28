package channels;

import java.io.IOException;
import java.net.InetAddress;
import java.net.MulticastSocket;
import java.util.Arrays;

public class MdbChannel extends Channel{

    public MdbChannel(InetAddress addr, int port) throws IOException {
        super(addr, port);
        this.thread = new MdbThread();
    }

    public class MdbThread extends Thread {
        public void run() {
            System.out.println("MdbChannel listening...");
            while(true){
                try{
                    socket.joinGroup(addr);
                    //separate header and body from data
                    byte[] buf = new byte[64 * 1000];
                    DatagramPacket packet = new DatagramPacket(rbuf, rbuf.length);
                    socket.receive(packet);
                    String data = new String(paclet.getData(), 0, packet.getLength());

                    //Separete Header
                    String[] dataArray = received.split("\\r\\n\\r\\n");
                    Header header = getHeader(dataArray);

                    //Separete Body
                    int offsetOfBody = dataArray[0].length() + 4;
                    byte[] bodyByteArray = getArrayFromOffset(packet.getData(), offsetOfBody, packet.getLength());

                    //Analisar a data e ver se ja tem guardada versao senao guardar e mandar pelo Mc que recebeu


                    socket.leaveGroup(addr);
                }
                catch (IOException | InterruptedException e) {
                    e.printStackTrace();
                }
            }

        }
    }

    public Header getHeader(String[] dataArray){
        String headerMsg = dataArray[0];
        String[] headerTokens = headerMsg.split("\\s+");
        String messageType = headerTokens[0];
        String version = headerTokens[1];
        int senderId = headerTokens[2];
        String fileId = headerTokens[3];
        int ChunkNo = headerTokens[4];
        int replicationDeg;
        if(headerTokens.length >= 6)
            replicationDeg = headerTokens[5];
        else
            replicationDeg = "";

        return new Header(messageType,version,senderId,fileId,ChunkNo,replicationDeg);
    }

}