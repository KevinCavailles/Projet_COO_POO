package main;
import java.io.Serializable;
import java.net.*;

public class Utilisateur implements Serializable{

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	
	private String id;
	private String pseudo;
	private InetAddress ip;
	private int port;
	
	private static Utilisateur self;
	
	public Utilisateur(String id, String pseudo, InetAddress ip, int port) throws UnknownHostException {
		this.id = id;
		this.pseudo = pseudo;
		this.ip = ip;
		this.port = port;
		System.out.println(InetAddress.getLocalHost());
	}

	
	public String getId() {
		return id;
	}

	public String getPseudo() {
		return pseudo;
	}

	public void setPseudo(String pseudo) {
		this.pseudo = pseudo;
	}

	public InetAddress getIp() {
		return ip;
	}
	
	public int getPort() {
		return port;
	}
	
	public static void setSelf(String id, String pseudo, String host, int port) throws UnknownHostException {
		if(Utilisateur.self == null) {
			Utilisateur.self = new Utilisateur(id, pseudo, InetAddress.getByName(host), port);
		}
	}
	
	public static Utilisateur getSelf() {
		return Utilisateur.self;
	}
}
