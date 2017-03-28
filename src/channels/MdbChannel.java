package channels;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.InetAddress;
import java.net.MulticastSocket;
import java.util.Arrays;

import messages.Header;

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
                    DatagramPacket packet = new DatagramPacket(buf, buf.length);
                    socket.receive(packet);
                    String data = new String(packet.getData(), 0, packet.getLength());

                    //Separete Header
                    String[] dataArray = data.split("\\r\\n\\r\\n");
                    Header header = getHeader(dataArray);

                    //Separete Body
                    int offsetOfBody = dataArray[0].length() + 4;
                    byte[] bodyByteArray = getArrayFromOffset(packet.getData(), offsetOfBody, packet.getLength());

                    //Analisar a data e ver se ja tem guardada versao senao guardar e mandar pelo Mc que recebeu


                    socket.leaveGroup(addr);
                }
                /*
                 *
                 * tratar exceçoes no handlle
                 *
                 */

                catch (IOException  | InterruptedException e) {
                    e.printStackTrace();
                }
            }

        }
    }


    //Gets a byte[] from a determined index to the end - USE TO BODY
    private byte[] getArrayFromOffset(byte[] data, int offsetOfBody, int length) {
        int size = length - offsetOfBody;
        byte[] toRet = new byte[size];

        for(int i = offsetOfBody; i < length; i++) {
            toRet[i - offsetOfBody] = data[i];
        }

        return toRet;
    }

    //USE TO HEADER
    public Header getHeader(String[] dataArray){
        String headerMsg = dataArray[0];
        String[] headerTokens = headerMsg.split("\\s+");
        String messageType = headerTokens[0];
        String version = headerTokens[1];
        int senderId = Integer.parseInt(headerTokens[2]);
        String fileId = headerTokens[3];
        int ChunkNo = Integer.parseInt(headerTokens[4]);
        int replicationDeg;
        if(headerTokens.length >= 6)
            replicationDeg = Integer.parseInt(headerTokens[5]);
        else
            replicationDeg = Integer.parseInt("");

        return new Header(messageType,version,senderId,fileId,ChunkNo,replicationDeg);
    }

}