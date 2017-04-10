package subprotocols;

import java.io.File;
import java.security.MessageDigest;

import peers.Peer;
import utilities.Header;
import utilities.Message;

public class Delete extends Thread{
	String file_name;
	
	public Delete(String file_name) {
		this.file_name = file_name;
		
	}
	
	public void run() {
		File file = new File("../res/" + file_name);
		
		String version = "1.0";
		String peer_id = Peer.getPeer_id();
		String file_id = getFileId(file);
		
		Header header = new Header("DELETE", version, peer_id, file_id, 0, 0);
		Message delete_message = new Message(Peer.getMcChannel().getSocket(), Peer.getMcChannel().getAddr(), header, null);
		new Thread(delete_message).start();
		
		if(file.delete()){
			System.out.println("File deleted!");
		}else{
			System.out.println("Deleted Failed!");
		}
		Peer.getData().getBackedUpFiles().remove(file.getName());
	}
	
	//Get File Id
	public static String getFileId(File file) {
		return sha256(file.getName() + file.lastModified() + Peer.getPeer_id());
	}
	
	
	// From internet http://stackoverflow.com/questions/3103652/hash-string-via-sha-256-in-java
	public static String sha256(String base) {
		try{
			MessageDigest digest = MessageDigest.getInstance("SHA-256");
			byte[] hash = digest.digest(base.getBytes("UTF-8"));
			StringBuffer hexString = new StringBuffer();

			for (int i = 0; i < hash.length; i++) {
				String hex = Integer.toHexString(0xff & hash[i]);
				if(hex.length() == 1) hexString.append('0');
				hexString.append(hex);
			}

			return hexString.toString();
		} catch(Exception ex){
			throw new RuntimeException(ex);
		}
	}
}
