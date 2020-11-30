package main;

//Importe les librairies
import java.awt.*;
import java.awt.event.*;
import javax.swing.*;

public class VueConnexion extends Vue {
	
	//Penser à regler la taille de la fenetre et a la centrer !
	
	//Elements vue
	private JFrame frame;
	private JPanel panel;
	private JButton boutonValider;
	private JTextField input;
	private JLabel labelInput;

	//Controleur
	ControleurConnexion controle;
	
	public VueConnexion() {
		super();
		controle = new ControleurConnexion(this);
		
		//Creation fenetre
		frame = new JFrame("Connexion");
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		frame.setSize(400, 100);
		frame.setLocationRelativeTo(null);
		
		//Creation panel
		panel = new JPanel(new GridLayout(3,1));
		
		//Ajout elements
		ajouterElements();
		
		//Regle le bouton par défaut
		frame.getRootPane().setDefaultButton(boutonValider);
		
		//Ajoute le panel a la fenetre
		frame.getContentPane().add(panel, BorderLayout.CENTER);
		
		//Affiche la fenetre
		frame.setVisible(true);
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
