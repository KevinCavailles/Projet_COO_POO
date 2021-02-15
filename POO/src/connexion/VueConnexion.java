package connexion;

//Import librairies
import java.awt.*;
import javax.swing.*;

import database.SQLiteManager;
import main.Vue;

public class VueConnexion extends Vue {
	
	private static final long serialVersionUID = 1L;
	
	//Graphical elements
	private JButton boutonValider;
	private JTextField inputUsername;
	private JPasswordField inputPassword;
	private JLabel labelUsername;
	private JLabel labelPassword;
	private JLabel connexionInfo;
	private JPanel main;
	private JPanel panelPassword;

	//Controller
	private ControleurConnexion controle;
	
	/**
	 * Create and initialize the view SWING window that will be used during the connection phase.
	 * Doing so, it also creates the controller that will monitor all changes during the connection phase.
	 * 
	 * @param numtest : to be passed down to the controller
	 * 
	 */
	public VueConnexion(int numtest) {
		super("Connexion");
		controle = new ControleurConnexion(this, numtest);
		
		//Window creation
		this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		this.setSize(400, 300);
		this.setLocationRelativeTo(null);
		
		//Adding graphical elements
		ajouterElements();
		
		//Setting default button
		this.getRootPane().setDefaultButton(boutonValider);
		
		this.inputUsername.setText(SQLiteManager.hardcodedNames[numtest]+numtest);
		this.inputPassword.setText("aze1$"+SQLiteManager.hardcodedNames[numtest].charAt(0)+numtest);
		
		//Display window
		this.setVisible(true);
	}
	
	
	// ----- ADDING ELEMENTS TO AND REMOVING ELEMENTS FROM THE MAIN WINDOW ----- //
	
	
	/**
	 * Add various graphical elements to the main window : used when initializing the window
	 */
	private void ajouterElements() {
		
		//Create a panel
		main = new JPanel(new GridLayout(4,1));		
		JPanel panelUsername = new JPanel(new GridLayout(1, 2));
		this.panelPassword = new JPanel(new GridLayout(1, 2));
		
		//Create various elements
		this.connexionInfo = new JLabel("");
		
		this.inputUsername = new JTextField();
		this.inputUsername.setPreferredSize(new Dimension(100, 50));
		
		this.labelUsername = new JLabel("Nom d'utilisateur :");	
		this.labelUsername.setBorder(BorderFactory.createEmptyBorder(5,5,5,5));
		
		this.inputPassword = new JPasswordField();
		this.inputPassword.setPreferredSize(new Dimension(100, 50));
		
		this.labelPassword = new JLabel("Mot de passe :");
		this.labelPassword.setBorder(BorderFactory.createEmptyBorder(5,5,5,5));
		
		boutonValider = new JButton("Valider");
		
		//Make it so the controller is monitoring the button
		boutonValider.addActionListener(controle);
		
		
		panelUsername.add(this.labelUsername);
		panelUsername.add(this.inputUsername);
		
		
		this.panelPassword.add(this.labelPassword);
		this.panelPassword.add(this.inputPassword);
		
		
		main.add(connexionInfo);
		main.add(panelUsername);
		main.add(this.panelPassword);
		main.add(boutonValider);
		
		this.add(main);
	}
	
	protected void removePasswordPanel() {
		this.main.remove(2);
	}
	
	protected void addPasswordPanel() {
		this.main.add(this.panelPassword, 2);
	}

	
	//----- GETTERS -----//
	
	/**
	 * Returns the current value of the field inputUsername
	 * 
	 * @return current value of the field inputUsername as String
	 */
	protected String getUsernameValue() {
		return this.inputUsername.getText();
	}
	
	/**
	 * Returns the current value of the field inputPassword
	 * 
	 * @return current value of the field inputPassword as String
	 */
	protected char[] getPasswordValue() {
		return this.inputPassword.getPassword();
	}
	
	
	//----- SETTERS -----//
	
	/**
	 * Set a displayed message that will give the user information (for example if they entered a wrong password)
	 * 
	 * @param text : message to display as String
	 */
	protected void setConnexionInfo(String text) {
		this.connexionInfo.setText(text);
	}
	
	/**
	 * Set the label for the inputUsername fiel
	 * 
	 * @param text : label to display as String
	 */
	protected void setTextUsernameField(String text) {
		this.labelUsername.setText(text);
	}
	
	/**
	 * Empty the inputUsername text field
	 */
	protected void resetUsernameField() {
		this.inputUsername.setText("");
	}
	
	/**
	 * Empty the inputPassword text field
	 */
	protected void resetPasswordField() {
		this.inputPassword.setText("");
	}
	
}