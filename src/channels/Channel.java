package channels;

import java.io.IOException;
import java.net.InetAddress;
import java.net.MulticastSocket;
import java.util.Arrays;

import utilities.Header;

public class Channel{

    public Thread thread;
    MulticastSocket socket;
    InetAddress addr;
    private int port;

    public Channel(InetAddress addr, int port) throws IOException {
        this.addr = addr;
        this.port = port;
        this.socket = new MulticastSocket(this.port);

        //The initiator-peer collects the confirmation messages during a time interval of 1 second.
        this.socket.setTimeToLive(1);
    }

	public Thread getThread() {
		return thread;
	}

	public void setThread(Thread thread) {
		this.thread = thread;
	}

	public MulticastSocket getSocket() {
		return socket;
	}

	public void setSocket(MulticastSocket socket) {
		this.socket = socket;
	}

	public InetAddress getAddr() {
		return addr;
	}

	public void setAddr(InetAddress addr) {
		this.addr = addr;
	}

	public int getPort() {
		return port;
	}

	public void setPort(int port) {
		this.port = port;
	}
	
    //Gets a byte[] from a determined index to the end - USE TO BODY
    public byte[] getArrayFromOffset(byte[] data, int offsetOfBody, int length) {
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