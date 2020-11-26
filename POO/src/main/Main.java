package main;

import java.net.UnknownHostException;

import javax.swing.JFrame;

public class Main {

	public static void main(String[] args) {
		JFrame frame = new JFrame("Application");
		VueStandard vueStd = new VueStandard();
		frame.add(vueStd);
		
		frame.setSize(300,300);
		frame.setVisible(true);
		
		try {
			Utilisateur.setSelf("Raijila", "Raijila", "localhost");
		} catch (UnknownHostException e1) {
			System.out.println("hote inexistant");
		}
		
		VueStandard.userList.addElement("Mirasio");
		
		try {
			Thread.sleep(2000);
			VueStandard.userList.addElement("Semtexx");
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}


	}

}
