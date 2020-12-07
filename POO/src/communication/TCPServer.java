package communication;

import java.io.IOException;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.UnknownHostException;


public class TCPServer extends Thread {

	private ServerSocket sockListenTCP;
	
	public TCPServer(int port) throws UnknownHostException, IOException {
		this.sockListenTCP = new ServerSocket(port, 5, InetAddress.getLocalHost());
	}
	
	@Override
	public void run() {
		Socket sockAccept;
		while(true) {
			try {
				sockAccept = this.sockListenTCP.accept();
				new TCPHandlerConnection(sockAccept).run();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			
		}
	}
}
