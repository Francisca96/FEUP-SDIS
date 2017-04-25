package channels;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.InetAddress;
import java.util.concurrent.ThreadLocalRandom;

import peers.DataBase;
import peers.FileManage;
import peers.Peer;
import utilities.Header;
import utilities.Message;

public class MdbChannel extends Channel{

    public MdbChannel(InetAddress addr, int port) throws IOException {
        super(addr, port);
        this.thread = new MdbThread();
    }
    
    public class MdbThread extends Thread {
        public void run() {
        	System.out.println("Mdb channel listening...");
            while(true){
                try{
                    socket.joinGroup(addr);
                    
                    //separate header and body from data
                    byte[] buf = new byte[64 * 1000];
                    DatagramPacket packet = new DatagramPacket(buf, buf.length);
                    socket.receive(packet);
                    String data = new String(packet.getData(), 0, packet.getLength());

                    //separate Header
                    String[] dataArray = data.split("\\r\\n\\r\\n");
                    Header header = getHeader(dataArray);
                    String message_type = header.getMessageType();
                    String sender_id = header.getSenderId();

                    //separate Body
                    int offsetOfBody = dataArray[0].length() + 4;
                    byte[] bodyByteArray = getArrayFromOffset(packet.getData(), offsetOfBody, packet.getLength());

                    System.out.println(message_type);
                    if(Peer.getPeer_id() != sender_id) {
						switch (message_type) {
						case "PUTCHUNK":
							if (DataBase.replication_complete(header)) {
								System.out.println("ReplicationDeg achived");
								break;
							}
							McChannel.setReceivedPutchunk(true);
						
							//Handle
					    	
							// Check if the file was not backed up by this peer
							for (FileManage fileInfo : Peer.getData().get_file_backup().values()) 
							    if (fileInfo.get_file_id().equals(header.get_file_id()))
							    	return;
							
							//save chunk
							Peer.getData().save_chunk(header, bodyByteArray);
							
							//reply
							String version = header.getVersion();
							String peer_id = Peer.getPeer_id();
							String file_id = header.get_file_id();
							int chunk_number = header.get_chunk_number();
							
							Header reply_header = new Header("STORED", version, peer_id, file_id, chunk_number, 0);
							Message reply = new Message(Peer.getMcChannel().getSocket(), Peer.getMcChannel().getAddr(), reply_header, null);
							
							int timeout = ThreadLocalRandom.current().nextInt(0, 400);
							Thread.sleep(timeout);
							new Thread(reply).start();
							
							System.out.println("Replying...");
							
							break;
						}
					}
                    socket.leaveGroup(addr);
                }
                catch (IOException  | InterruptedException e) {
                    e.printStackTrace();
                }
            }

        }
    }
}

