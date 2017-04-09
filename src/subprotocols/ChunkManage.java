package subprotocols;

import java.util.ArrayList;

import utilities.Chunk;
import utilities.Header;
import utilities.Message;
import peers.ChunksList;
import peers.Peer;

public class ChunkManage {
	private Message message;
	private ArrayList<Header> validReplies;
	
	public ChunkManage(Header header, byte[] body) {
		this.message = new Message(Peer.getMdbChannel().getSocket(), Peer.getMdbChannel().getAddr(), header, body);
		this.validReplies = new ArrayList<>();
	}

	public void sendChunk() {
		new Thread(this.message).start();
	}

	public void checkReplies() {
		int replicationDeg = message.getHeader().getReplicationDeg();
		Message reply;
		ArrayList<Message> storedReplies = Peer.getMcChannel().getStoredReplies();
		int counter = 0;
		for (int i = 0; i < storedReplies.size(); i++) {
			reply = storedReplies.get(i);
			if (validReply(reply.getHeader())) {
				counter++;
			} 
			Peer.getMcChannel().getStoredReplies().remove(storedReplies.get(i));
		}
		//Checking if this peer has the chunk saved
		Chunk chunkInfo = new Chunk(message.getHeader(), message.getBody().length);
		if (Peer.getData().getChunksSaved().get(message.getHeader().getFileId()) != null) 
			if (Peer.getData().getChunksSaved().get(message.getHeader().getFileId()).contains(chunkInfo))
				counter++;
		
		if (counter >= replicationDeg) {
			System.out.println("RepDeg achieved! Telling storage");
			DataBase();
		}
		
	}
	
	private void DataBase() {
		
			Peer.getData();
			ChunksList chunksList = Peer.getData().getChunksBackedUp().get(message.getHeader().getFileId()) != null ? Peer.getData().getChunksBackedUp().get(message.getHeader().getFileId()) :
				new ChunksList();
			Chunk chunkInfo = new Chunk(message.getHeader(), message.getBody().length);
			for (Chunk savedChunkInfo : chunksList) {
				if (chunkInfo.equals(savedChunkInfo)) {
					chunkInfo = savedChunkInfo;
					break;
				}
			}
			chunkInfo.addToStoredHeaders(validReplies);
			if (!chunksList.contains(chunkInfo))
				chunksList.add(chunkInfo);
			
			Peer.getData().getChunksBackedUp().put(message.getHeader().getFileId(), chunksList);
		}
	

	private boolean validReply(Header replyHeader) {
		if (!replyHeader.getMessageType().equals("STORED"))
			return false;
		if (replyHeader.getSenderId() == Peer.getPeer_id())
			return false;
		if (!replyHeader.getFileId().equals(message.getHeader().getFileId()))
			return false;
		if (replyHeader.getChunkNo() != message.getHeader().getChunkNo())
			return false;
		validReplies.add(replyHeader);
		return true;
	}
}
