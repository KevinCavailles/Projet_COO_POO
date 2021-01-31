package communication.tcp;

import java.io.IOException;

import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

import java.net.InetAddress;
import java.net.Socket;

import observers.ObserverInputMessage;
import observers.ObserverSocketState;

import messages.MauvaisTypeMessageException;
import messages.Message;

public class TCPClient {

	private Socket sockTCP;
	private ObjectOutputStream output;
	private TCPInputThread inputThread;
	

	public TCPClient(Socket sockTCP) throws IOException {
		this.sockTCP = sockTCP;
		
		this.output = new ObjectOutputStream(sockTCP.getOutputStream());
		ObjectInputStream input = new ObjectInputStream(sockTCP.getInputStream());
		this.inputThread = new TCPInputThread(input);
	}
	
	public TCPClient(InetAddress addr) throws IOException {
		this(new Socket(addr, TCPServer.PORT_SERVER));

	}

	public void startInputThread() {
		this.inputThread.start();
	}

	
	public void sendMessage(Message message) throws IOException, MauvaisTypeMessageException {
		System.out.println("dans write");
		this.output.writeObject(message);
	}

	
	public void setObserverInputThread(ObserverInputMessage o) {
		this.inputThread.setObserverInputMessage(o);
	}

	
	public void setObserverSocketState(ObserverSocketState o) {
		this.inputThread.setObserverSocketState(o);
	}
	
	
	public void destroyAll() {
		try {
			if (!this.sockTCP.isClosed()) {
				this.output.close();
				this.sockTCP.close();
				this.inputThread.setObserverSocketState(null);
			}
			this.inputThread = null;
			this.sockTCP = null;
			this.output = null;

		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}
