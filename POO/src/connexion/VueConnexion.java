package connexion;

//Importe les librairies
import java.awt.*;
import java.awt.event.*;
import javax.swing.*;

import main.Vue;

public class VueConnexion extends Vue {
	
	//Elements vue
	private JPanel panel;
	private JButton boutonValider;
	private JTextField input;
	private JLabel labelInput;

	//Controleur
	ControleurConnexion controle;
	
	//penser à enlever le numtest
	public VueConnexion(int numtest) {
		super("Connexion");
		controle = new ControleurConnexion(this, numtest);
		
		//Creation fenetre
		this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		this.setSize(400, 100);
		this.setLocationRelativeTo(null);
		
		//Creation panel
		panel = new JPanel(new GridLayout(3,1));
		
		//Ajout elements
		ajouterElements();
		
		//Regle le bouton par défaut
		this.getRootPane().setDefaultButton(boutonValider);
		
		//Ajoute le panel a la fenetre
		this.getContentPane().add(panel, BorderLayout.CENTER);
		
		//Affiche la fenetre
		this.setVisible(true);
	}
	
	private void ajouterElements() {
		
		//Cree les elements
		input = new JTextField();
		labelInput = new JLabel("Veuillez entrer votre identifiant unique");
		boutonValider = new JButton("Valider");
		
		//Le controleur guette les evenements du bouton
		boutonValider.addActionListener(controle);
		
		//Ajoute les elements
		panel.add(labelInput);
		panel.add(input);
		panel.add(boutonValider);
		
		labelInput.setBorder(BorderFactory.createEmptyBorder(5,5,5,5));
	}
	
	
	//Getters et setters
	public void setTexteLabelInput(String text) {
		labelInput.setText(text);
	}
	
	public String getValeurTextField() {
		return input.getText();
	}
	
	public void resetValeurTextField() {
		input.setText("");
	}
}
