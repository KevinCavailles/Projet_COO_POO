package main;

import java.io.IOException;

import communication.TCPServer;

public class MainServTCP {

	public static void main(String[] args) {
		try {
			Utilisateur.setSelf("id1", "toto", "localhost");
			new TCPServer(7001).run();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
}
