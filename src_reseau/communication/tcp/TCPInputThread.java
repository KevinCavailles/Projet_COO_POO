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

	public TCPInputThread(ObjectInputStream input) {
		this.input = input;
		this.running = true;
	}

	@Override
	public void run() {

		while (this.running) {
			try {
				
//				System.out.println("dans read");
				Object o =  this.input.readObject();
				this.obsInput.update(this, o);

			} catch (IOException | ClassNotFoundException e) {
				this.interrupt();

			}

		}
	}

	@Override
	public void interrupt() {
		
		try {
			//Stop the thread
			this.running = false;
			//Close the stream and the socket
			this.input.close();
			
			if(this.obsState != null) {
				//Send an update to the controller
				this.obsState.updateSocketState(this, true);
			}
			
			
			//Set every attribute to null so they're collected by the GC
			this.obsInput = null;
			this.obsState = null;
			this.input = null;
			
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	protected void setObserverInputMessage(ObserverInputMessage o) {
		this.obsInput = o;
	}
	
	protected void setObserverSocketState(ObserverSocketState o) {
		this.obsState = o;
	}

}
