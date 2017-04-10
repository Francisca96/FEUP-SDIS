package channels;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.InetAddress;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.concurrent.ThreadLocalRandom;

import peers.Peer;
import subprotocols.Backup;
import utilities.Chunk;
import utilities.Header;
import utilities.Message;

public class McChannel extends Channel{

	private static boolean waitting_putchunk = false;
	ArrayList<Message> replies_stored;
	
	//Constructor
    public McChannel(InetAddress addr, int port) throws IOException {
        super(addr, port);
        this.replies_stored = new ArrayList<>();
        this.thread = new MulticastThread();
    }
   
    //Thread
    public class MulticastThread extends Thread {
		public void run() {
			System.out.println("Listening the MC channel...");
			while(true) {
				try {
					socket.joinGroup(addr);
					
					//Separate header and body from data
                    byte[] buf = new byte[64 * 1000];
                    DatagramPacket packet = new DatagramPacket(buf, buf.length);
                    socket.receive(packet);
                    String data = new String(packet.getData(), 0, packet.getLength());

                    //Separate Header
                    String[] dataArray = data.split("\\r\\n\\r\\n");
                    Header header = getHeader(dataArray);
                    String message_type = header.getMessageType();
                    String sender_id = header.getSenderId();

                    //Separate Body
                    int offsetOfBody = dataArray[0].length() + 4;
                    byte[] bodyByteArray = getArrayFromOffset(packet.getData(), offsetOfBody, packet.getLength());
					
                    Message message = new Message(header,bodyByteArray);
					
					if(Peer.getPeer_id() != sender_id) {
						switch (message_type) {
						case "GETCHUNK":
							System.out.println("GETCHUNK");
							if (!Peer.getData().check_stored(header.getFileId(), header.getChunkNo())) {
								System.out.println("Chunk is not stored");
								break;
							}
							
							//Handle
							String version = Peer.getProtocole_version();
							String peer_id = Peer.getPeer_id();
							String file_id = header.getFileId();
							int chunk_number = header.getChunkNo();
							Header reply_header = new Header("CHUNK", version, peer_id, file_id, chunk_number, 0);
							
							
							byte[] body = Peer.getData().get_chunk_body(file_id, chunk_number);		
							
							Message reply = new Message(Peer.getMdrChannel().getSocket(), Peer.getMdrChannel().getAddr(), reply_header, body);
							Thread.sleep(ThreadLocalRandom.current().nextInt(0, 400));
							new Thread(reply).start();
							
							break;
						case "STORED":
							System.out.println("STORED");
							replies_stored.add(message);
							Peer.getData().add_receive_message(header);
							break;
						case "DELETE":
							System.out.println("DELETE");
							Peer.getData().clear_store(header);
							break;
						}
					} 
					switch (message_type) {
					case "REMOVED":
						System.out.println("REMOVED");
						removed(header);
						break;
				}
					socket.leaveGroup(addr);
				} catch (IOException | InterruptedException e) {
					e.printStackTrace();
				}
			}
		}
    }

    //Handle Removed
	private void removed(Header header) throws InterruptedException, IOException {
		
		Chunk chunk = Peer.getData().delete_store_message(header);
		
		if (chunk != null) {
			waitting_putchunk = false;
			int timeout = ThreadLocalRandom.current().nextInt(0, 400);
			Thread.sleep(timeout);
			if (waitting_putchunk) {
				removed(header);
				return;
			}
			prepareChunk(chunk);
		}		
	}

	private void prepareChunk(Chunk chunk) throws IOException {
		String fileName = "chunks_" + Peer.getPeer_id() + "/" + chunk.getFileId() + "/" + chunk.getChunkNo() + ".data";
		String chunkPath = "../res/" + fileName;
		
		byte[] chunk_tmp = null;
	
		chunk_tmp = Files.readAllBytes(Paths.get(chunkPath));
		
		String version = Peer.getProtocole_version();
		String peer_id = Peer.getPeer_id();
		String file_id = chunk.getFileId();
		int chunk_number = chunk.getChunkNo();
		int rep_deg = chunk.getReplicationDeg();
		Header header = new Header("PUTCHUNK", version, peer_id, file_id, chunk_number, rep_deg);
		Backup.sendChunk(header, chunk_tmp);
	}

	public static void setReceivedPutchunk(boolean waitting_putchunk) {
		McChannel.waitting_putchunk = waitting_putchunk;
	}

	public ArrayList<Message> getReplies_stored() {
		return replies_stored;
	}

	public static void sendRemoved(Chunk chunk) {
		
		String version = Peer.getProtocole_version();
		String peer_id = Peer.getPeer_id();
		String file_id = chunk.getFileId();
		int chunk_number = chunk.getChunkNo();
		Header header = new Header("REMOVED", version, peer_id,file_id,chunk_number,0);
		
		Message message = new Message(Peer.getMcChannel().getSocket(), Peer.getMcChannel().getAddr(), header, null);
		new Thread(message).start();
		
	}
    

}