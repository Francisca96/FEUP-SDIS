package channels;

import java.io.File;
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.InetAddress;
import java.net.MulticastSocket;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.concurrent.ThreadLocalRandom;

import peers.Peer;
import utilities.Header;
import utilities.Message;

public class McChannel extends Channel{

	private static boolean putchunkWhileWaiting = false;
	ArrayList<Message> storedReplies;
	
    public McChannel(InetAddress addr, int port) throws IOException {
        super(addr, port);
        this.storedReplies = new ArrayList<>();
        this.thread = new MulticastThread();
    }
    
	private void delete(Message message) {
		Header header = message.getHeader();
		Peer.getData().clearStoredChunks(header);
		File file =  new File("../res/" + "chunks_" + Peer.getPeerId() + "/" + header.getFileId() + "/");
	}

	private void getChunk(Header header) throws InterruptedException, IOException {
		byte[] body = Peer.getData().getChunkBody(header.getFileId(), header.getChunkNo());		
		Header replyHeader = new Header("CHUNK", "1.0", Peer.getPeerId(), header.getFileId(), header.getChunkNo(), 0);
		Message reply = new Message(Peer.getMdrChannel().getSocket(), Peer.getMdrChannel().getAddr(), replyHeader, body);
		int timeout = ThreadLocalRandom.current().nextInt(0, 400);
		Thread.sleep(timeout);
		new Thread(reply).start();
	}
    
    
    public class MulticastThread extends Thread {
		public void run() {
			System.out.println("Listening the MC channel...");
			while(true) {
				try {
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
					
                    Message message = new Message(header,bodyByteArray);
					
					System.out.println("Received " + header.getMessageType());
					
					if(Peer.getPeerId() != (header.getSenderId())) {
						switch (header.getMessageType()) {
						case "GETCHUNK":
							if (!Peer.getData().chunkIsStored(header.getFileId(), header.getChunkNo())) {
								System.out.println("Chunk is not stored");
								break;
							}
							getChunk(header);
							break;
						case "STORED":
							storedReplies.add(message);
							Peer.getData().addToReceivedStoreMessages(header);
							break;
						case "DELETE":
							delete(message);
							break;
							
						}
					} 
					socket.leaveGroup(addr);
				} catch (IOException | InterruptedException e) {
					e.printStackTrace();
				}
			}
		}
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