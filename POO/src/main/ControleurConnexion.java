package main;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

public class ControleurConnexion implements ActionListener {

	private enum Etat {DEBUT, ID_OK};
	
	private VueConnexion vue;
	private Etat etat;
	
	public ControleurConnexion(VueConnexion vue) {
		this.vue = vue;
		this.etat = Etat.DEBUT;
	}
	
	@Override
	public void actionPerformed(ActionEvent e) {
		String input;
		boolean inputOK = false;
		if (this.etat == Etat.DEBUT) {
			input=vue.getValeurTextField();
			
			//Recherche dans la liste des utilisateurs enregistres, report sur inputOK
			if (input.contentEquals("idvalide")) inputOK=true;
			
			if (inputOK) {
				this.etat=Etat.ID_OK;

				//Envoi broadcast du message "JeSuisActif" et, attente du retour de la liste des utilisateurs actifs
				
				//Mise en place de la demande du pseudo
				vue.setTexteLabelInput("Veuillez entrer votre nom");
				vue.resetValeurTextField();
				inputOK=false;
			}
			else vue.setTexteLabelInput("Identifiant invalide, veuillez réessayer");
		}
		else {
			input=vue.getValeurTextField();
			
			//Recherche dans la liste locale des utilisateurs connectes, report sur inputOK
			if (input.contentEquals("nomvalide")) inputOK=true;
			
			if (inputOK) {
				//Creation de la vue principale
				
			}
			else vue.setTexteLabelInput("Ce nom est déjà utilisé, veuillez en choisir un autre");
		}
	}

}
