package communication;

import java.io.IOException;
import java.net.InetAddress;
import java.net.Socket;

public class TCPHandlerConnection extends Thread {
	
	private Socket sockAccept;
	
	public TCPHandlerConnection(Socket sockAccept) {
		this.sockAccept = sockAccept;
	}
	
	public TCPHandlerConnection(InetAddress addr, int port) throws IOException {
		this(new Socket(addr, port));
	}
	
	@Override
	public void run() {
		TCPClient tcpC;
		try {
			tcpC = new TCPClient(sockAccept);
			tcpC.connexionAccepted();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
	}
	
	
	
}
