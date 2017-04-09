package channels;

import java.io.File;
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.InetAddress;
import java.net.MulticastSocket;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.concurrent.ThreadLocalRandom;

import peers.Peer;
import subprotocols.Backup;
import utilities.Chunk;
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
		File file =  new File("../res/" + "chunks_" + Peer.getPeer_id() + "/" + header.getFileId() + "/");
	}

	private void getChunk(Header header) throws InterruptedException, IOException {
		byte[] body = Peer.getData().getChunkBody(header.getFileId(), header.getChunkNo());		
		Header replyHeader = new Header("CHUNK", "1.0", Peer.getPeer_id(), header.getFileId(), header.getChunkNo(), 0);
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
					
					if(Peer.getPeer_id() != (header.getSenderId())) {
						switch (header.getMessageType()) {
						case "GETCHUNK":
							System.out.println("GETCHUNK");
							if (!Peer.getData().chunkIsStored(header.getFileId(), header.getChunkNo())) {
								System.out.println("Chunk is not stored");
								break;
							}
							getChunk(header);
							break;
						case "STORED":
							System.out.println("STORED");
							storedReplies.add(message);
							Peer.getData().addToReceivedStoreMessages(header);
							break;
						case "DELETE":
							System.out.println("DELETE");
							delete(message);
							break;
						}
					} 
					switch (header.getMessageType()) {
					case "REMOVED":
						System.out.println("REMOVED");
						handleRemoved(header);
						break;
				}
					socket.leaveGroup(addr);
				} catch (IOException | InterruptedException e) {
					e.printStackTrace();
				}
			}
		}

		private void handleRemoved(Header header) throws InterruptedException {
			Chunk chunkInfo = Peer.getData().removeFromReceivedStoreMessages(header);
			if (chunkInfo != null) {
				putchunkWhileWaiting = false;
				int timeout = ThreadLocalRandom.current().nextInt(0, 400);
				Thread.sleep(timeout);
				if (putchunkWhileWaiting) {
					handleRemoved(header);
					return;
				}
				prepareChunk(chunkInfo);
			} else {
				System.out.println("Chunk info is null.");
			}
			
		}

		private void prepareChunk(Chunk chunkInfo) {
			String fileName = "chunks_" + Peer.getPeer_id() + "/" + chunkInfo.getFileId() + "/" + chunkInfo.getChunkNo() + ".data";
			String chunkPath = "../res/" + fileName;
			
			byte[] chunk = new byte[0];
			try {
				chunk = Files.readAllBytes(Paths.get(chunkPath));
			} catch (IOException e) {
				System.out.println("Could not read bytes from " + chunkPath);
			}
			String version = "1.0";
			Header header = new Header("PUTCHUNK", version, Peer.getPeer_id(), chunkInfo.getFileId(), chunkInfo.getChunkNo(), chunkInfo.getReplicationDeg());
			Backup.sendChunk(header, chunk);
			
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

	public static void sendRemoved(Chunk chunk) {
		Header header = new Header("REMOVED", "1.0",
				Peer.getPeer_id(), chunk.getFileId(), chunk.getChunkNo(), 0);
		Message message = new Message(Peer.getMcChannel().getSocket(), Peer.getMcChannel().getAddr(), header, null);
		new Thread(message).start();
		
	}
    

}