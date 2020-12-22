package session;

import java.awt.event.ActionEvent;

import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.io.IOException;
import java.net.Socket;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

import javax.swing.JButton;
import javax.swing.JList;

import java.util.concurrent.*;

import communication.TCPClient;
import main.Observer;
import main.Utilisateur;
import messages.MauvaisTypeMessageException;
import messages.Message;
import messages.MessageTexte;

public class ControleurSession implements ActionListener, Observer, KeyListener {

	private VueSession vue;
	private TCPClient tcpClient;

	protected ControleurSession(VueSession vue, Socket socketComm) throws IOException {
		this.vue = vue;
		this.tcpClient = new TCPClient(socketComm);
		this.tcpClient.setObserverInputThread(this);
		this.tcpClient.startInputThread();
	}

	// ---------- ACTION LISTENER OPERATIONS ----------//
	@Override
	public void actionPerformed(ActionEvent e) {

		//Quand le bouton envoyer est presse
		if ((JButton) e.getSource() == this.vue.getButtonEnvoyer()) {
			String messageOut = this.vue.getZoneSaisie().getText();
			System.out.println(messageOut);
			
			//Si le texte field n'est pas vide
			if (!messageOut.equals("")) {
				
				//On recupere la date et on prepare les messages a afficher/envoyer
				String date = this.getDateAndTime();
				String messageToDisplay = date+" Moi : "+ messageOut;
				messageOut = date +" "+ Utilisateur.getSelf().getPseudo() + " : " + messageOut+"\n";
				
				try {
					this.tcpClient.sendMessage(messageOut);	
				} catch (MauvaisTypeMessageException | IOException e1) {
					// TODO Auto-generated catch block
					e1.printStackTrace();
				}
				
				this.vue.appendMessage(messageToDisplay + "\n");
				this.vue.resetZoneSaisie();
			}
		}

	}

	//Methode appelee quand l'inputStream de la socket de communication recoit des donnees
	@Override
	public void update(Object o, Object arg) {
		if(arg instanceof MessageTexte) {
			MessageTexte messageIn = (MessageTexte) arg;
			System.out.println(messageIn.getContenu());
			this.vue.appendMessage(messageIn.getContenu());
		}else {
		}
		
	}

	
	
	private String getDateAndTime() {
		DateTimeFormatter dtf = DateTimeFormatter.ofPattern("yyyy/MM/dd HH:mm:ss");
		LocalDateTime now = LocalDateTime.now();
		return "<"+dtf.format(now)+">";
	}

	@Override
	public void keyTyped(KeyEvent e) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void keyPressed(KeyEvent e) {
		if(e.getKeyCode() == KeyEvent.VK_ENTER) {
			if(!e.isShiftDown()) {
				this.vue.getButtonEnvoyer().doClick();
			}
			
		}	
	}

	@Override
	public void keyReleased(KeyEvent e) {
		// TODO Auto-generated method stub
		
	}

}
