package main;




import javax.swing.JFrame;
import javax.swing.JPanel;

public class Main extends JPanel{

	public static void main(String[] args) {
		
		Vue vc = new VueConnexion();
		JFrame fenetre = new JFrame("Connexion");
		
		fenetre.add(vc);
		fenetre.setSize(500, 500);
		fenetre.setVisible(true);
	}

}
