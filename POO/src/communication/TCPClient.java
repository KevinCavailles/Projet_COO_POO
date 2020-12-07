package communication;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.net.InetAddress;
import java.net.Socket;

import main.Observer;
import main.VueSession;
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
		
		this.output = new ObjectOutputStream(this.sockTCP.getOutputStream()) ;
		ObjectInputStream input = new ObjectInputStream(this.sockTCP.getInputStream());
		this.inputThread = new TCPInputThread(input);
	}
	
	public TCPClient(InetAddress addr, int port) throws IOException {
		this(new Socket(addr, port)) ;
		
	}
	
	public void connexionAccepted() throws IOException {
		
		System.out.println("avant vue");
		
		new VueSession("Application", this);
			
	}
	
	public void startInputThread() {
		this.inputThread.start();
	}
	
	public void sendMessage(String contenu) throws IOException, MauvaisTypeMessageException {
		System.out.println("dans write");
		MessageTexte message = new MessageTexte(TypeMessage.TEXTE, contenu);
		this.output.writeObject(message);
	}
	
	public void setObserverInputThread(Observer o) {
		this.inputThread.setObserver(o);
	}
}
