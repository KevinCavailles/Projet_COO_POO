package communication;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.util.Arrays;

import main.Observer;
import messages.Message;

public class TCPInputThread extends Thread {

	private ObjectInputStream input;
	private boolean running;
	private char[] buffer;
	private Observer obs;

	public TCPInputThread(ObjectInputStream input) {
		this.input = input;
		this.running = true;
		this.buffer = new char[200];
	}

	@Override
	public void run() {

		while (this.running) {
			try {
				
				
				System.out.println("dans read");
				Object o =  this.input.readObject();
				this.obs.update(this, o);
				
		
			} catch (IOException | ClassNotFoundException e) {
				this.interrupt();
				//e.printStackTrace();
			}

		}
	}

	@Override
	public void interrupt() {
		
		try {
			this.running = false;
			this.input.close();
			//this.obs.update(this, this.input);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	private void flushBuffer() {
		Arrays.fill(this.buffer, '\u0000');
	}

	protected void setObserver(Observer o) {
		this.obs = o;
	}

}
