package utilities;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.InetAddress;
import java.net.MulticastSocket;

public class Message implements Runnable{
    private byte[] body;
    private Header header;
    private InetAddress addr;
    private MulticastSocket socket;

    public Message(Header header, byte[] body){
        this.header = header;
        this.body = body;
        this.socket = null;
        this.addr = null;
    }

    //To know the socket and adress of a channel
    public Message(MulticastSocket socket, InetAddress addr, Header header, byte[] body){
        this.header = header;
        this.body = body;
        this.socket = socket;
        this.addr = addr;
    }
    
    public void run() {
		System.out.println("Sending " + header.getMessageType());
		byte[] headerBytes = header.toString().getBytes();
		byte[] message = {};
		
		if (body != null) {
			message = concatenateBytes(headerBytes, body);
		} else {
			message = headerBytes;
		}
		
		DatagramPacket packet = new DatagramPacket(message,
				message.length, addr,
				socket.getLocalPort());
		try {
			socket.send(packet);
		} catch (IOException e) {
			System.out.println("Could not send chunk");
		}
	}
    
   // From internet: http://stackoverflow.com/questions/5513152/easy-way-to-concatenate-two-byte-arrays
 	public static byte[] concatenateBytes(byte[]a, byte[] b) {
 		byte[] c = new byte[a.length + b.length];
 		System.arraycopy(a, 0, c, 0, a.length);
 		System.arraycopy(b, 0, c, a.length, b.length);
 		return c;
 	}

	public byte[] getBody() {
		return body;
	}

	public void setBody(byte[] body) {
		this.body = body;
	}

	public Header getHeader() {
		return header;
	}

	public void setHeader(Header header) {
		this.header = header;
	}

	public InetAddress getAddr() {
		return addr;
	}

	public void setAddr(InetAddress addr) {
		this.addr = addr;
	}

	public MulticastSocket getSocket() {
		return socket;
	}

	public void setSocket(MulticastSocket socket) {
		this.socket = socket;
	}
 	
 	



}
