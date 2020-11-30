package main;

import java.io.IOException;



public class Main {

	private static int portServers[] = {1526,1501,1551,1561};
	private static String ids[] = {"Raijila", "titi33", "Semtexx", "Salam"};
	private static String pseudo[] = {"Raijila", "Mirasio", "Semtexx", "Xaegon"};
	public static void main(String[] args) {
		

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
			
		
		
		
//		VueStandard.userList.addElement("Mirasio");
//		
//		try {
//			Thread.sleep(2000);
//			VueStandard.userList.addElement("Semtexx");
//		} catch (InterruptedException e) {
//			// TODO Auto-generated catch block
//			e.printStackTrace();
//		}


	}
	
	private static void createApp(int i) {
		try {
			Utilisateur.setSelf(Main.ids[i], Main.pseudo[i], "localhost");
			new VueStandard("Application", Main.portServers[i]-1, Main.portServers[i], Main.portServers);	
		} catch (IOException e) {
			System.out.println(e.toString());	
		}
	}

}
