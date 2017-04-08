package channels;

import java.io.IOException;
import java.net.InetAddress;
import java.net.MulticastSocket;
import java.util.Arrays;

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
    
}