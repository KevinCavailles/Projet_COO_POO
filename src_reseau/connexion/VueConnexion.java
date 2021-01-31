package connexion;

//Importe les librairies
import java.awt.*;
import javax.swing.*;

import main.Vue;

public class VueConnexion extends Vue {
	
	private static final long serialVersionUID = 1L;
	//Elements vue
	private JButton boutonValider;
	private JTextField inputUsername;
	private JPasswordField inputPassword;
	private JLabel labelUsername;
	private JLabel labelPassword;
	private JLabel connexionInfo;
	private JPanel main;
	private JPanel panelPassword;

	//Controleur
	private ControleurConnexion controle;
	
	//penser à enlever le numtest
	public VueConnexion() {
		super("Connexion");
		controle = new ControleurConnexion(this);
		
		//Creation fenetre
		this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		this.setSize(400, 300);
		this.setLocationRelativeTo(null);
		
		//Ajout elements
		ajouterElements();
		
		//Regle le bouton par défaut
		this.getRootPane().setDefaultButton(boutonValider);
		
		//Affiche la fenetre
		this.setVisible(true);
	}
	
	private void ajouterElements() {
		
		//Creation panel
		main = new JPanel(new GridLayout(4,1));		
		JPanel panelUsername = new JPanel(new GridLayout(1, 2));
		this.panelPassword = new JPanel(new GridLayout(1, 2));
		
		//Cree les elements
		this.connexionInfo = new JLabel("");
		
		this.inputUsername = new JTextField();
		this.inputUsername.setPreferredSize(new Dimension(100, 50));
		
		this.labelUsername = new JLabel("Nom d'utilisateur");	
		this.labelUsername.setBorder(BorderFactory.createEmptyBorder(5,5,5,5));
		
		this.inputPassword = new JPasswordField();
		this.inputPassword.setPreferredSize(new Dimension(100, 50));
		
		this.labelPassword = new JLabel("Mot de passe :");
		this.labelPassword.setBorder(BorderFactory.createEmptyBorder(5,5,5,5));
		
		boutonValider = new JButton("Valider");
		
		//Le controleur guette les evenements du bouton
		boutonValider.addActionListener(controle);
		
		//Ajoute les elements
		panelUsername.add(this.inputUsername);
		panelUsername.add(this.labelUsername);
		
		this.panelPassword.add(this.inputPassword);
		this.panelPassword.add(this.labelPassword);
		
		
		main.add(connexionInfo);
		main.add(panelUsername);
		main.add(this.panelPassword);
		main.add(boutonValider);
		
		this.add(main);
	}
	
	
	//Getters et setters
	protected void setConnexionInfo(String text) {
		this.connexionInfo.setText(text);
	}
	
	protected void setTextUsernameField(String text) {
		this.labelUsername.setText(text);
	}
	
	protected String getUsernameValue() {
		return this.inputUsername.getText();
	}
	
	protected char[] getPasswordValue() {
		return this.inputPassword.getPassword();
	}
	
	
	protected void resetUsernameField() {
		this.inputUsername.setText("");
	}
	
	protected void removePasswordPanel() {
		this.main.remove(2);
	}
	
	protected void addPasswordPanel() {
		this.main.add(this.panelPassword, 2);
	}

	protected void resetPasswordField() {
		this.inputPassword.setText("");
	}
	
}
