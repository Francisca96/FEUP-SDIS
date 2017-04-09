package channels;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.InetAddress;
import java.net.MulticastSocket;
import java.util.Arrays;
import java.util.concurrent.ThreadLocalRandom;

import peers.DataBase;
import peers.FileInfo;
import peers.Peer;
import utilities.Header;
import utilities.Message;

public class MdbChannel extends Channel{

    public MdbChannel(InetAddress addr, int port) throws IOException {
        super(addr, port);
        this.thread = new MdbThread();
    }
    
    
    private void handlePutChunk(Header header, byte[] bodyByteArray) throws InterruptedException {
		Peer.getData();
		// file was backed up by this peer
		for (FileInfo fileInfo : Peer.getData().getBackedUpFiles().values()) 
		    if (fileInfo.getFileId().equals(header.getFileId()))
		    	return;
		//save chunk
		try {
			Peer.getData().saveChunk(header, bodyByteArray);
		} catch (IOException e) {
			System.out.println("Could not save the chunk " + header.getChunkNo() + "from file " + header.getFileId());
			return;
		}
		//reply
		Header replyHeader = new Header("STORED", header.getVersion(),
				Peer.getPeerId(), header.getFileId(), header.getChunkNo(), 0);
		Message reply = new Message(Peer.getMcChannel().getSocket(), Peer.getMcChannel().getAddr(), replyHeader, null);
		int timeout = ThreadLocalRandom.current().nextInt(0, 400);
		Thread.sleep(timeout);
		new Thread(reply).start();
		System.out.println("Replying...");
		
	}

    

    public class MdbThread extends Thread {
        public void run() {
            System.out.println("MdbChannel listening...");
            while(true){
                try{
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

                    if(Peer.getPeerId() != header.getSenderId()) {
						switch (header.getMessageType()) {
						case "PUTCHUNK":
							System.out.println("Received PUTCHUNK");
							if (DataBase.repDegAchieved(header) && header.getVersion().equals("1.2")) {
								System.out.println("ReplicationDeg was already achieved! Ignoring chunk");
								break;
							}
							McChannel.setReceivedPutchunk(true);
							handlePutChunk(header, bodyByteArray);
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

