package communication.tcp;

import java.io.IOException;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.UnknownHostException;

import observers.ObserverInputMessage;

public class TCPServer extends Thread {

	private ServerSocket sockListenTCP;
	private ObserverInputMessage obsInput;

	/**
	 * Create a TCP Server on the specified port. It will listen continuously for
	 * connections in order to create new sessions between users.
	 * 
	 * @param port 		The port on which the server will listen
	 * @throws UnknownHostException
	 * @throws IOException
	 */
	public TCPServer(int port) throws UnknownHostException, IOException {
		this.sockListenTCP = new ServerSocket(port, 50, InetAddress.getLocalHost());
	}

	@Override
	public void run() {
		Socket sockAccept;
		while (true) {
			try {
				sockAccept = this.sockListenTCP.accept();

				// Notify the observer of the new connexion
				this.obsInput.updateInput(this, sockAccept);
			} catch (IOException e) {
				e.printStackTrace();
			}

		}
	}

	/**
	 * Set the observer to notify when a new connection is made.
	 * 
	 * @param o 	The observer
	 */
	public void addObserver(ObserverInputMessage o) {
		this.obsInput = o;
	}
}
