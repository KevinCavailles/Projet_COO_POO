package main;

import java.io.IOException;
import java.net.InetAddress;
import java.net.UnknownHostException;

import communication.*;

public class Main2tests {

	public static void main(String[] args) {
		try {
			Utilisateur.setSelf("idrandom", "RandomPersonne", "localhost");
		} catch (UnknownHostException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		try {
			Communication comUDP = new CommunicationUDP(2906, 2905, new int[] {});
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		while (true) {}
		
	}
}
