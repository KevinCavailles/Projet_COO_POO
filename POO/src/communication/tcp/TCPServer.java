package communication.tcp;

import java.io.IOException;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.UnknownHostException;

import observers.ObserverInputMessage;


public class TCPServer extends Thread {

	private ServerSocket sockListenTCP;
	private ObserverInputMessage obs;
	
	public TCPServer(int port) throws UnknownHostException, IOException {
		this.sockListenTCP = new ServerSocket(port, 5, InetAddress.getLocalHost());
	}
	
	@Override
	public void run() {
		System.out.println("TCP running");
		Socket sockAccept;
		while(true) {
			try {
				sockAccept = this.sockListenTCP.accept();
				this.obs.update(this, sockAccept);
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			
		}
	}
	
	public void addObserver(ObserverInputMessage obs) {
		this.obs = obs;
	}
}
