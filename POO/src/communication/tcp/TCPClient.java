package communication.tcp;

import java.io.File;
import java.io.IOException;

import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

import java.net.InetAddress;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;

import observers.ObserverInputMessage;
import observers.ObserverSocketState;

import messages.MessageTexte;
import messages.MauvaisTypeMessageException;
import messages.Message;
import messages.Message.TypeMessage;

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
	
	
	public TCPClient(InetAddress addr, int port) throws IOException {
		this(new Socket(addr, port));

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
