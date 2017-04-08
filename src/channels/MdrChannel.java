package channels;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.InetAddress;
import java.net.MulticastSocket;
import java.util.Arrays;

import channels.MdbChannel.MdbThread;
import utilities.Header;

public class MdrChannel extends Channel{

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

                    //Separete Body
                    int offsetOfBody = dataArray[0].length() + 4;
                    byte[] bodyByteArray = getArrayFromOffset(packet.getData(), offsetOfBody, packet.getLength());
                    
					socket.leaveGroup(addr);
				} catch (IOException e) {
                    e.printStackTrace();
                }
			}
		}
	}

}