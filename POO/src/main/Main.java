package main;

import java.io.IOException;
import java.sql.SQLException;

import database.SQLiteManager;
import standard.VueStandard;

import javax.swing.UIManager;
import javax.swing.UIManager.*;



public class Main {

	private static int portServersUDP[] = {1526,1501,1551,1561};
	private static String ids[] = {"Raijila", "titi33", "Semtexx", "Salam"};
	private static String pseudo[] = {"Raijila", "Mirasio", "Semtexx", "Xaegon"};
	private static String pwd[] = {"azertyuiop","12345","abcde","toto"};
	private static int portServersTCP[] = {1625,1600,1650,1660};
	public static void main(String[] args) {
		

		

		try {
		    for (LookAndFeelInfo info : UIManager.getInstalledLookAndFeels()) {
		        if ("Nimbus".equals(info.getName())) {
		            UIManager.setLookAndFeel(info.getClassName());
		            break;
		        }
		    }
		} catch (Exception e) {
		    // If Nimbus is not available, you can set the GUI to another look and feel.
		}
		
		switch(args[0]) {
		case "0": 
			Main.createApp(0);
			break;
		case "1":
			Main.createApp(1);
			break;
		case "2":
			Main.createApp(2);
			break;
		default:
			Main.createApp(3);
		}

	}
	
	private static void createApp(int i) {
		try {
			Utilisateur.setSelf(Main.ids[i], Main.pseudo[i], "localhost", Main.portServersTCP[i]);
			SQLiteManager sqlManager = new SQLiteManager(i);
			try {
				sqlManager.createNewUserEncrypt(Main.ids[i], Main.pwd[i]);
				sqlManager.checkPwd(Main.ids[i], Main.pwd[i].toCharArray());
			} catch (SQLException e) {
				System.out.println("erreur recherche utilisateur");
				e.printStackTrace();
			}
			new VueStandard("Application", Main.portServersUDP[i]-1, Main.portServersUDP[i], Main.portServersUDP, Main.portServersTCP[i], sqlManager);	
		} catch (IOException e) {
			System.out.println(e.toString());	
		}
	}

}
