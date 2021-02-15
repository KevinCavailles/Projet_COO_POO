package main;
import java.io.Serializable;
import java.net.*;

public class Utilisateur implements Serializable{


	private static final long serialVersionUID = 1L;
	
	private String id;
	private String pseudo;
	private InetAddress ip;
	private int port;
	
	//Represents the user that is currently using the application
	private static Utilisateur self;
	
	
	/**
	 * Create and initialize an object representing an user
	 * 
	 * @param id : user id as String
	 * @param pseudo : name under which other users can see this user as String
	 * @param ip : ip of the device this user is currently using as InetAddress
	 * @param port : on local mode, port used for the TCP listen socket as int
	 * 
	 */
	public Utilisateur(String id, String pseudo, InetAddress ip, int port) throws UnknownHostException {
		this.id = id;
		this.pseudo = pseudo;
		this.ip = ip;
		this.port = port;

	}
	
	// ----- GETTERS ----- //
	
	/**
	 * Returns user id as String
	 * 
	 * @return user id as String
	 */
	public String getId() {
		return id;
	}

	/**
	 * Returns user pseudo as String
	 * 
	 * @return user pseudo as String
	 */
	public String getPseudo() {
		return pseudo;
	}
	
	/**
	 * Returns user device's ip as String
	 * 
	 * @return user device's ip as String
	 */
	public InetAddress getIp() {
		return ip;
	}
	
	/**
	 * Returns the port the user uses for their TCP listen socket as int
	 * 
	 * @return TCP listen socket port as int
	 */
	public int getPort() {
		return port;
	}
	
	/**
	 * Returns the user currently using this instance of the application as Utilisateur
	 * 
	 * @return current user as Utilisateur
	 */	
	public static Utilisateur getSelf() {
		return Utilisateur.self;
	}
	
	
	// ----- SETTERS ----- //

	/**
	 * Change the pseudo used by an user
	 * 
	 * @param pseudo : new pseudo as String
	 */	
	public void setPseudo(String pseudo) {
		this.pseudo = pseudo;
	}
	
	/**
	 * Sets the self static attribute with a new Utilisateur
	 * 
	 * @param id : user id as String
	 * @param pseudo : name under which other users can see this user as String
	 * @param ip : ip of the device this user is currently using as InetAddress
	 * @param port : on local mode, port used for the TCP listen socket as int
	 */	
	public static void setSelf(String id, String pseudo, String host, int port) throws UnknownHostException {
		if(Utilisateur.self == null) {
			Utilisateur.self = new Utilisateur(id, pseudo, InetAddress.getByName(host), port);
		}
	}
	
	/**
	 * Sets the self static attribute with null
	 */
	public static void resetSelf() {
		Utilisateur.self = null;
	}
}
