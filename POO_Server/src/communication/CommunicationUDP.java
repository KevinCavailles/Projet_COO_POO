package communication;

import java.io.IOException;
import java.io.PrintWriter;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.ArrayList;

import main.Observer;
import main.Utilisateur;

import messages.*;


public class CommunicationUDP extends Thread {

	private UDPClient client;
	private int portServer;
	private ArrayList<Integer> portOthers;
	private ArrayList<Utilisateur> users = new ArrayList<Utilisateur>();

	private Observer observer;

	public CommunicationUDP(int portClient, int portServer, int[] portsOther) throws IOException {
		this.portServer = portServer;
		this.portOthers = this.getArrayListFromArray(portsOther);
		new UDPServer(portServer, this).start();
		this.client = new UDPClient(portClient);
	}
	

	private ArrayList<Integer> getArrayListFromArray(int ports[]) {
		ArrayList<Integer> tmp = new ArrayList<Integer>();
		for (int port : ports) {
			tmp.add(port);
		}
		tmp.remove(Integer.valueOf(portServer));

		return tmp;
	}
	
	
	// ----- CHECKERS ----- //
	
	protected boolean containsUserFromID(String id) {
		for(Utilisateur u : users) {
			if(u.getId().equals(id) ) {
				return true;
			}
		}
		return false;
	}
	
	
	public boolean containsUserFromPseudo(String pseudo) {
		for(Utilisateur u : users) {
			if(u.getPseudo().equals(pseudo) ) {
				return true;
			}
		}
		
		return false;
	}
	
	
	// ----- GETTERS ----- //
	
	public int getPortFromPseudo(String pseudo) {
		for(int i=0; i < users.size() ; i++) {
			
			if(users.get(i).getPseudo().equals(pseudo) ) {
				return users.get(i).getPort();
			}
		}
		return -1;
	}
	
	private int getIndexFromID(String id) {
		for(int i=0; i < users.size() ; i++) {
			if(users.get(i).getId().equals(id) ) {
				return i;
			}
		}
		return -1;
	}
	
	public Utilisateur getUserFromID(String id) {
		for(Utilisateur u : users) {
			if(u.getId().equals(id) ) {
				return u;
			}
		}
		return null;
	}
	
	public Observer getObserver () {
		return this.observer;
	}
	
	
	// ----- SETTERS ----- //
	
	public void setObserver (Observer obs) {
		this.observer=obs;
	}
	
	
	
	
	// ----- USER LIST MANAGEMENT ----- //
	
	//Prints a html table containing the pseudo of all active local users
	 public void printActiveUsersUDP(PrintWriter out) {
		    for (Utilisateur uIn : users) {
		    	out.println("<TH> " + uIn.getPseudo() + ",</TH>");
		    }
	    }
	
	//Add an user to the list of active local users
	protected synchronized void addUser(String idClient, String pseudoClient, InetAddress ipClient, int port) throws IOException {
		users.add(new Utilisateur(idClient, pseudoClient, ipClient, port));
		try {
			Message message = new MessageSysteme(Message.TypeMessage.INFO_PSEUDO, idClient, pseudoClient, port);
			observer.update(this, message);
		} catch (MauvaisTypeMessageException e) {
		}		
	}
	
	//Change the pseudo of an user already in the active local users list
	protected synchronized void changePseudoUser(String idClient, String pseudoClient, InetAddress ipClient, int port) {
		int index = getIndexFromID(idClient);
		users.get(index).setPseudo(pseudoClient);
		try {
			Message message = new MessageSysteme(Message.TypeMessage.INFO_PSEUDO, idClient, pseudoClient, port);
			observer.update(this, message);
		} catch (MauvaisTypeMessageException e) {
		}
	}

	//Remove an user from the active local users list
	protected synchronized void removeUser(String idClient, String pseudoClient,InetAddress ipClient, int port) {
		int index = getIndexFromID(idClient);
		if( index != -1) {
			users.remove(index);
		}
		try {
			Message message = new MessageSysteme(Message.TypeMessage.JE_SUIS_DECONNECTE, pseudoClient,  idClient, port);
			observer.update(this, message);
		} catch (MauvaisTypeMessageException e) {
		}
	}
	
	//Remove all users from the active local users list
	public void removeAll(){
		int oSize = users.size();
		for(int i=0; i<oSize;i++) {
			users.remove(0);
		}
	}
	
	
	// ----- SENDING MESSAGES ----- //
	
	
	// Send the message "add,id,pseudo" to localhost on all the ports in
	// "portOthers"
	// This allows the receivers' agent (portOthers) to create or modify an entry with the
	// data of this agent
	//Typically used to notify of a name change
	public void sendMessageInfoPseudo() throws UnknownHostException, IOException {

		Utilisateur self = Utilisateur.getSelf();
		
		String pseudoSelf =self.getPseudo();
		String idSelf = self.getId();
		int portSelf = self.getPort();
		
		Message msout = null;
		try {
			msout = new MessageSysteme(Message.TypeMessage.INFO_PSEUDO, pseudoSelf, idSelf, portSelf);
			for(int port : this.portOthers) {
				this.client.sendMessageUDP_local(msout, port, InetAddress.getLocalHost());
			}
		} catch (Exception e) {
		}
	}
	
	//Same, but on only one port
	//Typically used to give your current name and id to a newly arrived host
	public void sendMessageInfoPseudo(int portOther) throws UnknownHostException, IOException {
	
		Utilisateur self = Utilisateur.getSelf();
		try {
			Message msout = new MessageSysteme(Message.TypeMessage.INFO_PSEUDO, self.getPseudo(), self.getId(), self.getPort());
			this.client.sendMessageUDP_local(msout, portOther, InetAddress.getLocalHost());
		} catch (MauvaisTypeMessageException e) {e.printStackTrace();}
	}

	
	//Broadcast a given message on the local network (here modelized by ports)
	public void sendMessage(Message m) {
		try {
			for(int port : this.portOthers) {
				this.client.sendMessageUDP_local(m, port, InetAddress.getLocalHost());
			}
		} catch (IOException e) {
		}
	}
	
	//Send a given message to a specific user (here, by port)
	public void sendMessage(Message m, int port) {
		try {
			this.client.sendMessageUDP_local(m, port, InetAddress.getLocalHost());
		} catch (IOException e) {
		}
	}

}
