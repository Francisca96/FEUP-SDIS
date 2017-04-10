package channels;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.InetAddress;

import peers.FileManage;
import peers.Peer;
import subprotocols.Restore;
import utilities.Header;

public class MdrChannel extends Channel{
	
	private boolean chunks_waiting = false;
	
    public MdrChannel(InetAddress addr, int port) throws IOException {
        super(addr, port);
        this.thread = new MdrThread();
    }
    
    public class MdrThread extends Thread {
		public void run() {
			System.out.println("Mdr channel");
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

                    //Separate Body
                    int offsetOfBody = dataArray[0].length() + 4;
                    byte[] bodyByteArray = getArrayFromOffset(packet.getData(), offsetOfBody, packet.getLength());
                    
                    //Use data
                    String message_type = header.getMessageType();
                    System.out.println(message_type);
					if(!Peer.getPeer_id().equals(header.getSenderId())) {
						switch (message_type) {
						case "CHUNK":
							if (chunks_waiting) {
								int chunkNum = header.get_chunk_number();
								
								//Handle
								int size = 64*1000;
								if (bodyByteArray == null || (bodyByteArray.length <= size && chunkNum == Restore.get_number_of_chunks())) {
									save_chunk_restore(Restore.getFile_name(), bodyByteArray);
								}
								else{
									System.out.println("Cant restore");
								}					
							}
							break;
						}
					}
					socket.leaveGroup(addr);
				} catch (IOException e) {
                    e.printStackTrace();
                }
			}
		}
    }
    
    public void save_chunk_restore(String file_name, byte[] body) throws IOException {
		FileManage file = Peer.getData().get_file_backup().get(file_name);
		Restore.getNew_output().write(body);
		int size = 64*1000;
		if (body.length < size) {
			Peer.getMdrChannel().setWaitingChunks(false);
			Restore.getNew_output().close();
			System.out.println("Restore finish!");
			if (Restore.get_number_of_chunks() != file.get_chunks_number()){
					System.out.println("The number of received chunks is different the number of chunks in this file");
				}
			Restore.init_restore();
		} else {
			Restore.inc_number_of_chunks();
			Restore.send_chunk();
		}
	}

	public void setWaitingChunks(boolean chunks_waiting) {
		this.chunks_waiting = chunks_waiting;
		
	}

}