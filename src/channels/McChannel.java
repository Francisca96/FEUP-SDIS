package channels;

import java.io.IOException;
import java.net.InetAddress;
import java.net.MulticastSocket;
import java.util.ArrayList;
import java.util.Arrays;

import utilities.Message;

public class McChannel extends Channel{

	private static boolean putchunkWhileWaiting = false;
	ArrayList<Message> storedReplies;
	
    public McChannel(InetAddress addr, int port) throws IOException {
        super(addr, port);
        this.storedReplies = new ArrayList<>();
    }

	public ArrayList<Message> getStoredReplies() {
		return storedReplies;
	}

	public void setStoredReplies(ArrayList<Message> storedReplies) {
		this.storedReplies = storedReplies;
	}

	public static void setReceivedPutchunk(boolean putchunkWhileWaiting) {
		McChannel.putchunkWhileWaiting = putchunkWhileWaiting;
	}
    

}