package communication.tcp;

import java.io.IOException;

import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

import java.net.Socket;

import observers.ObserverInputMessage;
import observers.ObserverSocketState;

import messages.Message;

public class TCPClient {

	private Socket sockTCP;
	private ObjectOutputStream output;
	private ObjectInputStream input;
	private TCPInputThread inputThread;

	/**
	 * Create a TCP client from an existing socket. It will ensure the transmission
	 * of messages during a session. Two ObjectStream are created in order to
	 * read/write messages. The ObjectInputStream is given to a thread to read
	 * continuously the incoming message and allowing the user to write a message
	 * anytime.
	 * 
	 * @param sockTCP
	 * @throws IOException
	 */
	public TCPClient(Socket sockTCP) throws IOException {
		this.sockTCP = sockTCP;

		this.output = new ObjectOutputStream(sockTCP.getOutputStream());
		this.input = new ObjectInputStream(sockTCP.getInputStream());
		this.inputThread = new TCPInputThread(this.input);
	}

	/**
	 * Start the thread that will continuously read from the socket.
	 */
	public void startInputThread() {
		this.inputThread.start();
	}

	/**
	 * Send a message by writing it in the ObjectOutputStream of the socket
	 * 
	 * @param message
	 * @throws IOException
	 */
	public void sendMessage(Message message) throws IOException {
		this.output.writeObject(message);
	}

	/**
	 * Set the observer to notify when a message is received
	 * 
	 * @param o 	The observer
	 */
	public void setObserverInputThread(ObserverInputMessage o) {
		this.inputThread.setObserverInputMessage(o);
	}

	/**
	 * Set the observer to notify when the session is closed/the communication is
	 * broken.
	 * 
	 * @param o 	The observer
	 */
	public void setObserverSocketState(ObserverSocketState o) {
		this.inputThread.setObserverSocketState(o);
	}

	/**
	 * Method used when the session is over. Set all attribute references to null,
	 * interrupt the inputThread and close the streams and the socket.
	 */
	public void destroyAll() {
		try {
			if (!this.sockTCP.isClosed()) {
				this.inputThread.setObserverSocketState(null);
				this.inputThread.interrupt();
				this.input.close();
				this.output.close();
				this.sockTCP.close();
			}
			this.inputThread = null;
			this.sockTCP = null;
			this.output = null;

		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}
