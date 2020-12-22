package main;


import javax.swing.JPanel;

import connexion.VueConnexion;

public class Main extends JPanel{

	
	public static void main(String[] args) {
		new VueConnexion(Integer.parseInt(args[0]));
	}

}
