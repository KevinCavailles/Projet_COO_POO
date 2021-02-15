package communication.udp;

import java.io.IOException;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.ArrayList;

import main.Utilisateur;

import messages.*;
import observers.ObserverUserList;

public class CommunicationUDP extends Thread {

	private UDPClient client;
	private UDPServer server;
	private int portServer;
	private ArrayList<Integer> portOthers;
	private ArrayList<Utilisateur> users = new ArrayList<Utilisateur>();
	private ObserverUserList obsList;

	/**
	 * Create the object that will manage the userlist and contain a UDPClient and a
	 * UDPServer. Since the applications will run on localhost, it needs to know
	 * every UDPServer ports used in order to replicate a broadcast behaviour.
	 * 
	 * @param portClient 	The port number for the UDPClient
	 * @param portServer 	The port number for the UDPServer
	 * @param portsOther 	The port numbers for every other application's UDPServer
	 * @throws IOException
	 */
	public CommunicationUDP(int portClient, int portServer, int[] portsOther) throws IOException {
		this.portServer = portServer;
		this.portOthers = this.getArrayListFromArray(portsOther);
		this.server = new UDPServer(portServer, this);
		this.server.start();
		this.client = new UDPClient(portClient);
	}

	/**
	 * Create an ArrayList<Integer> from the int[] list of every servers' ports and
	 * remove the port of this application UDPServer.
	 * 
	 * @param ports 	The UDPServer port numbers.
	 * @return An ArrayList<Integer> without the port of this UDPServer.
	 */
	private ArrayList<Integer> getArrayListFromArray(int ports[]) {
		ArrayList<Integer> tmp = new ArrayList<Integer>();
		for (int port : ports) {
			tmp.add(port);
		}
		tmp.remove(Integer.valueOf(portServer));

		return tmp;
	}

	/**
	 * Set the observer to notify when the userList is updated.
	 * 
	 * @param obs 	The observer
	 */
	public void setObserver(ObserverUserList o) {
		this.obsList = o;
	}

	// -------------- USER LIST UPDATE METHODS -------------- //

	/**
	 * Add a new user to the userlist and notify the observer.
	 * 
	 * @param idClient
	 * @param pseudoClient
	 * @param ipClient
	 * @param port
	 * @throws UnknownHostException
	 */
	protected synchronized void addUser(String idUser, String pseudoUser, InetAddress ipUser, int portTCPServer)
			throws UnknownHostException {
		users.add(new Utilisateur(idUser, pseudoUser, ipUser, portTCPServer));
		this.sendUpdate();

	}

	/**
	 * Change the pseudo of an user and notify the observer if it exists in the
	 * userlist. Do nothing otherwise.
	 * 
	 * @param idClient
	 * @param pseudoClient
	 * @param ipClient
	 * @param port
	 */
	protected synchronized void changePseudoUser(String idUser, String pseudoUser, InetAddress ipUser,
			int portTCPServer) {
		int index = getIndexFromID(idUser);
		if (index != -1) {
			users.get(index).setPseudo(pseudoUser);
			this.sendUpdate();
		}

	}

	/**
	 * Remove an user from the userlist and notify the observer if it exists in the
	 * userlist. Do nothing otherwise.
	 * 
	 * @param idUser
	 * @param pseudoUser
	 * @param ipUser
	 * @param portTCPServer
	 */
	protected synchronized void removeUser(String idUser, String pseudoUser, InetAddress ipUser, int portTCPServer) {
		int index = getIndexFromID(idUser);
		if (index != -1) {
			users.remove(index);
			this.sendUpdate();
		}

	}

	public void removeAllUsers() {
		this.users.clear();
	}

	// -------------- CHECKERS -------------- //

	/**
	 * Check if there is an user in the list that has the given id.
	 * 
	 * @param id 	The user's id.
	 * @return True if the user is in the list 
	 * false otherwise.
	 */
	protected boolean containsUserFromID(String id) {
		for (Utilisateur u : users) {
			if (u.getId().equals(id)) {
				return true;
			}
		}
		return false;
	}

	/**
	 * Check if there is an user in the list that has the given pseudo.
	 * 
	 * @param pseudo 	The user's pseudo.
	 * @return True if the user is in the list 
	 * false otherwise.
	 */
	public boolean containsUserFromPseudo(String pseudo) {
		for (Utilisateur u : users) {
			if (u.getPseudo().toLowerCase().equals(pseudo.toLowerCase())) {
				return true;
			}
		}

		return false;
	}

	// -------------- GETTERS -------------- //

	/**
	 * Return the user with the given pseudo if it exists in the list.
	 * 
	 * @param pseudo 	The user's pseudo.
	 * @return The user if it exists in the list. 
	 * null otherwise.
	 */
	public Utilisateur getUserFromPseudo(String pseudo) {
		for (int i = 0; i < users.size(); i++) {
			if (users.get(i).getPseudo().equals(pseudo)) {
				return users.get(i);
			}
		}
		return null;
	}

	/**
	 * Return the index of the user with the given id if it exists in the list.
	 * 
	 * @param id 	The user's id.
	 * @return The index if the user exists in the list. 
	 * null otherwise
	 */
	private int getIndexFromID(String id) {
		for (int i = 0; i < users.size(); i++) {
			if (users.get(i).getId().equals(id)) {
				return i;
			}
		}
		return -1;
	}

	// -------------- SEND MESSAGES METHODS -------------- //

	/**
	 * Send a message indicating this application's user is connected to every
	 * UDPServer.
	 * 
	 * @throws UnknownHostException
	 * @throws IOException
	 */
	public void sendMessageConnecte() throws UnknownHostException, IOException {

		try {
			Message msgOut = new MessageSysteme(Message.TypeMessage.JE_SUIS_CONNECTE);
			for (int port : this.portOthers) {

				this.client.sendMessageUDP_local(msgOut, port);
			}
		} catch (MauvaisTypeMessageException e) {
			e.printStackTrace();
		}
	}

	/**
	 * Send a message containing this application's user's data to every UDPServer.
	 * This method is used to first add this user in the userlist or update this
	 * user's pseudo.
	 * 
	 * @throws UnknownHostException
	 * @throws IOException
	 */
	public void sendMessageInfoPseudo() throws UnknownHostException, IOException {

		Utilisateur self = Utilisateur.getSelf();

		try {
			Message msgOut = new MessageSysteme(Message.TypeMessage.INFO_PSEUDO, self.getPseudo(), self.getId(),
					self.getPort());
			for (int port : this.portOthers) {
				this.client.sendMessageUDP_local(msgOut, port);
			}
		} catch (MauvaisTypeMessageException e) {
			e.printStackTrace();
		}

	}

	/**
	 * Send a message containing this application's user's data to one user. This
	 * method is used to answer back when receiving a message with the type
	 * "JE_SUIS_CONNECTE"
	 * 
	 * @param portOther 	The port on which the other user's UDPServer is listening
	 * @throws UnknownHostException
	 * @throws IOException
	 */
	public void sendMessageInfoPseudo(int portOther) throws UnknownHostException, IOException {

		Utilisateur self = Utilisateur.getSelf();
		try {
			Message msgOut = new MessageSysteme(Message.TypeMessage.INFO_PSEUDO, self.getPseudo(), self.getId(),
					self.getPort());
			this.client.sendMessageUDP_local(msgOut, portOther);
		} catch (MauvaisTypeMessageException e) {
			e.printStackTrace();
		}
	}

	/**
	 * Send a message indicating this application's user is disconnected to every
	 * UDPServer.
	 * 
	 * @throws UnknownHostException
	 * @throws IOException
	 */
	public void sendMessageDelete() throws UnknownHostException, IOException {
		Utilisateur self = Utilisateur.getSelf();
		try {

			Message msgOut = new MessageSysteme(Message.TypeMessage.JE_SUIS_DECONNECTE, self.getPseudo(), self.getId(),
					self.getPort());
			for (int port : this.portOthers) {
				this.client.sendMessageUDP_local(msgOut, port);
			}

		} catch (MauvaisTypeMessageException e) {
			e.printStackTrace();
		}
	}
	
	
	// -------------- OTHERS -------------- //

	/**
	 * Notify the observer with the updated list
	 */
	private void sendUpdate() {
		if (this.obsList != null) {
			this.obsList.updateList(this, users);
		}
	}

}
