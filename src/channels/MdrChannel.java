package channels;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.InetAddress;
import java.net.MulticastSocket;
import java.util.Arrays;

import channels.MdbChannel.MdbThread;
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
			System.out.println("Listening the MDR channel...");
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
					if(!Peer.getPeer_id().equals(header.getSenderId())) {
						switch (header.getMessageType()) {
						case "CHUNK":
							if (chunks_waiting) {
								int chunkNum = header.getChunkNo();
								
								//Handle
								int size = 64*1000;
								if (bodyByteArray == null || (bodyByteArray.length <= size && chunkNum == Restore.getNumOfChunks())) {
									Peer.getData().saveRestoredChunk(Restore.getFile_name(), bodyByteArray);
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
    
    
	private void handleChunk(int chunkNum, byte[] bodyByteArray) throws IOException {
		int size = 64*1000;
		if (bodyByteArray == null || (bodyByteArray.length <= size && chunkNum == Restore.getNumOfChunks())) {
			Peer.getData().saveRestoredChunk(Restore.getFile_name(), bodyByteArray);
		}
		else{
			System.out.println("Chunk Num = " + chunkNum + " vs Stored chunk Num = " + Restore.getNumOfChunks());
			System.out.println("The received chunk has" + bodyByteArray.length + " bytes.");
		}
	}
	

	public void setWaitingChunks(boolean chunks_waiting) {
		this.chunks_waiting = chunks_waiting;
		
	}

}