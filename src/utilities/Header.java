package utilities;

public class Header {
    private static String messageType;
    private static String version;
    private static int replicationDeg;
    private static String file_id;
    private static String sender_id;
    private static int chunk_number;

    public Header (String messageType, String version, String sender_id, String file_id, int chunk_number, int replicationDeg){
        this.messageType = messageType;
        this.version = version;
        this.replicationDeg = replicationDeg;
        this.file_id = file_id;
        this.sender_id = sender_id;
        this.chunk_number = chunk_number;
        
    }

    //GETTERS AND SETTERS
    public static String getMessageType() {
        return messageType;
    }

    public static void setMessageType(String messageType) {
        Header.messageType = messageType;
    }

    public static String getVersion() {
        return version;
    }

    public static String getSenderId() {
        return sender_id;
    }

    public String get_file_id() {
        return file_id;
    }

    public int get_chunk_number() {
        return chunk_number;
    }

    public static void setChunkNo(int chunkNo) {
        Header.chunk_number = chunkNo;
    }

    public static int getReplicationDeg() {
        return replicationDeg;
    }
}
