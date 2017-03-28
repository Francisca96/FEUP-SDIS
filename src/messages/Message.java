package messages;

/**
 * Created by Francisca on 28/03/17.
 */
public class Message {
    private byte[] body;
    private Header header;

    public Message(Header header, byte[] body){
        this.header = header;
        this.body = body;
    }
}
