package communication.tcp;

import java.io.IOException;
import java.io.ObjectInputStream;
import observers.ObserverInputMessage;
import observers.ObserverSocketState;

public class TCPInputThread extends Thread {

	private ObjectInputStream input;
	private boolean running;
	private ObserverInputMessage obsInput;
	private ObserverSocketState obsState;

	/**
	 * Create the thread used to read the messages
	 * 
	 * @param input 	The ObjectInputStream to read data from
	 */
	protected TCPInputThread(ObjectInputStream input) {
		this.input = input;
		this.running = true;
	}

	@Override
	public void run() {

		while (this.running) {
			try {
				Object o = this.input.readObject();
				// Notify the observer a message was received
				this.obsInput.updateInput(this, o);

			} catch (IOException | ClassNotFoundException e) {
				this.interrupt();

			}

		}
	}

	@Override
	public void interrupt() {
		// Stop the thread
		this.running = false;
		// Close the stream and the socket

		if (this.obsState != null) {
			// Send an update to the controller
			this.obsState.updateSocketState(this, true);
		}

		// Set every attribute to null so they're collected by the GC
		this.obsInput = null;
		this.obsState = null;
		this.input = null;
	}

	/**
	 * Set the observer to notify when a message is received
	 * 
	 * @param o 	The observer
	 */
	protected void setObserverInputMessage(ObserverInputMessage o) {
		this.obsInput = o;
	}

	/**
	 * Set the observer to notify when the session is cut/closed.
	 * 
	 * @param o 	The observer
	 */
	protected void setObserverSocketState(ObserverSocketState o) {
		this.obsState = o;
	}

}
