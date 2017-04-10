package channels;

import java.io.IOException;
import java.net.InetAddress;
import java.net.MulticastSocket;

import utilities.Header;

public class Channel{

    public Thread thread;
    MulticastSocket socket;
    InetAddress addr;
    private int port_number;

    public Channel(InetAddress addr, int port_number) throws IOException {
        this.addr = addr;
        this.port_number = port_number;
        this.socket = new MulticastSocket(this.port_number);

        //Confirmation message in 1 second
        this.socket.setTimeToLive(1);
    }

	public MulticastSocket getSocket() {
		return socket;
	}

	public InetAddress getAddr() {
		return addr;
	}
	

    public byte[] getArrayFromOffset(byte[] data, int body_length, int length) {
        int size = length - body_length;
        
        byte[] tmp = new byte[size];

        for(int i = body_length; i < length; i++) {
        	tmp[i - body_length] = data[i];
        }

        return tmp;
    }

    //USE TO HEADER
    public Header getHeader(String[] data){
        String headerMsg = data[0];
        String[] headerTokens = headerMsg.split("\\s+");
        String messageType = headerTokens[0];
        String version = headerTokens[1];
        String senderId = headerTokens[2];
        String fileId = headerTokens[3];
        int ChunkNo = Integer.parseInt(headerTokens[4]);
        int replicationDeg;
        if(headerTokens.length >= 6)
            replicationDeg = Integer.parseInt(headerTokens[5]);
        else
            replicationDeg = Integer.parseInt("");

        return new Header(messageType, version, senderId, fileId, ChunkNo, replicationDeg);
    }
    
}