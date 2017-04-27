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

	
	ArrayList<Message> replies_stored;
	private static boolean waitting_putchunk = false;
	
	//Constructor
    public McChannel(InetAddress addr, int port) throws IOException {
        super(addr, port);
        this.thread = new MulticastThread();
        this.replies_stored = new ArrayList<>();
    }
   
    //Thread
    public class MulticastThread extends Thread {
		public void run() {
			System.out.println("Mc channel listening...");
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

					if(!Peer.getPeer_id().equals(sender_id)) {
						System.out.println(message_type + "_"+sender_id);
						switch (message_type) {
						case "GETCHUNK":
							if (!Peer.getData().check_stored(header.get_file_id(), header.get_chunk_number())) {
								System.out.println("Chunk is not stored");
								break;
							}
							
							//Handle
							String version = Peer.getProtocole_version();
							String peer_id = Peer.getPeer_id();
							String file_id = header.get_file_id();
							int chunk_number = header.get_chunk_number();
							Header reply_header = new Header("CHUNK", version, peer_id, file_id, chunk_number, 0);
							
							
							byte[] body = Peer.getData().get_chunk_body(file_id, chunk_number);		
							
							Message reply = new Message(Peer.getMdrChannel().getSocket(), Peer.getMdrChannel().getAddr(), reply_header, body);
							Thread.sleep(ThreadLocalRandom.current().nextInt(0, 400));
							new Thread(reply).start();
							
							break;
						case "DELETE":
							Peer.getData().clear_store(header);
							break;
						case "STORED":
							Peer.getData().add_receive_message(header);
							break;
					}
					}
					switch (message_type) {
					case "REMOVED":
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
		String file_name = "chunks_" + Peer.getPeer_id() + "/" + chunk.get_file_id() + "/" + chunk.get_chunk_number() + ".data";
		String path = "../files/" + file_name;
		
		byte[] chunk_tmp = null;
	
		chunk_tmp = Files.readAllBytes(Paths.get(path));
		
		String version = Peer.getProtocole_version();
		String peer_id = Peer.getPeer_id();
		String file_id = chunk.get_file_id();
		int chunk_number = chunk.get_chunk_number();
		int rep_deg = chunk.get_replication_deg();
		Header header = new Header("PUTCHUNK", version, peer_id, file_id, chunk_number, rep_deg);
		Backup.send_chunk(header, chunk_tmp);
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
		String file_id = chunk.get_file_id();
		int chunk_number = chunk.get_chunk_number();
		Header header = new Header("REMOVED", version, peer_id,file_id,chunk_number,0);
		Message message = new Message(Peer.getMcChannel().getSocket(), Peer.getMcChannel().getAddr(), header, null);
		new Thread(message).start();
		
	}
    

}