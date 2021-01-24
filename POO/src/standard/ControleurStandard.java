package standard;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.InetAddress;
import java.net.Socket;
import java.util.ArrayList;

import javax.swing.JButton;
import javax.swing.JList;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

import communication.tcp.TCPServer;
import communication.udp.CommunicationUDP;
import database.SQLiteManager;
import main.Utilisateur;
import observers.ObserverInputMessage;
import observers.ObserverSocketState;
import observers.ObserverUserList;
import session.VueSession;

public class ControleurStandard implements ActionListener, ListSelectionListener, WindowListener, ObserverInputMessage, ObserverUserList, ObserverSocketState {

	private enum ModifPseudo {
		TERMINE, EN_COURS
	}

	private ModifPseudo modifPseudo;
	private VueStandard vue;
	private CommunicationUDP commUDP;
	private String lastPseudo;
	private TCPServer tcpServ;
	private ArrayList<String> idsSessionEnCours;
	private SQLiteManager sqlManager;

	public ControleurStandard(VueStandard vue, int portClientUDP, int portServerUDP, int[] portsOther, int portServerTCP, SQLiteManager sqlManager) throws IOException {
		this.vue = vue;
		
		this.tcpServ = new TCPServer(portServerTCP);
		this.tcpServ.addObserver(this);
		this.tcpServ.start();
		
		this.commUDP = new CommunicationUDP(portClientUDP,portServerUDP, portsOther);
		this.commUDP.setObserver(this);
		this.commUDP.sendMessageConnecte();
		this.commUDP.sendMessageInfoPseudo();
		
		this.idsSessionEnCours = new ArrayList<String>();
		
		this.sqlManager = sqlManager;
		
		this.modifPseudo = ModifPseudo.TERMINE;
	}

	//---------- LISTSELECTION LISTENER OPERATIONS ----------//
	@Override
	public void valueChanged(ListSelectionEvent e) {
		
		int a = 5;
		
		//Case when a list element is selected
		if (this.vue.getActiveUsersList().isFocusOwner() && !e.getValueIsAdjusting() && this.vue.getActiveUsersList().getSelectedValue() != null) {
			
			JList<String> list = this.vue.getActiveUsersList();
			String pseudoOther = list.getSelectedValue();
			Utilisateur other = this.commUDP.getUserFromPseudo(pseudoOther);
			String idOther = other.getId();
			
			//Check if we are already asking for a session/chatting with the person selected
			//null condition because the list.clearSelection() generates an event
			if(!this.idsSessionEnCours.contains(idOther)) {
				
				int choix = this.vue.displayJOptionSessionCreation(pseudoOther);
				System.out.println("choix : "+choix);
				
				if(choix == 0) {
					int port = other.getPort();
					System.out.println("port = "+port);
					try {
						
						Socket socketComm = new Socket(InetAddress.getLocalHost(), port);
						this.sendMessage(socketComm, Utilisateur.getSelf().getPseudo());
						String reponse = this.readMessage(socketComm);
		
						System.out.println("reponse : " + reponse);
						
						if(reponse.equals("accepted")) {
							this.idsSessionEnCours.add(idOther);
							
							VueSession session = new VueSession(socketComm, idOther, pseudoOther, this.sqlManager);
							this.vue.addSession(pseudoOther, session);
							
							this.vue.displayJOptionResponse("acceptee");
							
						}else{
							this.vue.displayJOptionResponse("refusee");
							socketComm.close();
							System.out.println("refused");
						}
						
					} catch (IOException e1) {
						e1.printStackTrace();
					}
				}
				
			}
			
			list.clearSelection();
			System.out.println("pseudo de la personne a atteindre : " + pseudoOther);
			
		}
	}

	
	//---------- ACTION LISTENER OPERATIONS ----------//
	@Override
	public void actionPerformed(ActionEvent e) {
		
		//Cas Modifier Pseudo
		if ((JButton) e.getSource() == this.vue.getButtonModifierPseudo()) {
			JButton modifierPseudo = (JButton) e.getSource();

			if (this.modifPseudo == ModifPseudo.TERMINE) {
				this.lastPseudo = Utilisateur.getSelf().getPseudo();
				modifierPseudo.setText("OK");
				this.modifPseudo = ModifPseudo.EN_COURS;
			} else {

				if (this.vue.getDisplayedPseudo().length() >= 1 && !this.commUDP.containsUserFromPseudo(this.vue.getDisplayedPseudo().toLowerCase())) {
					
					Utilisateur.getSelf().setPseudo(this.vue.getDisplayedPseudo());

					try {
						this.commUDP.sendMessageInfoPseudo();
					} catch (IOException e1) {
						// TODO Auto-generated catch block
						e1.printStackTrace();
					}

				} else {
					this.vue.setDisplayedPseudo(this.lastPseudo);
				}

				modifierPseudo.setText("Modifier");
				this.modifPseudo = ModifPseudo.TERMINE;
			}

			this.vue.toggleEditPseudo();
		}
		
		
		
		//Cas deconnexion
		else if((JButton) e.getSource() == this.vue.getButtonDeconnexion() ) {
			try {
				this.commUDP.sendMessageDelete();
				this.commUDP.removeAll();
				this.vue.removeAllUsers();
				Utilisateur.getSelf().setPseudo("");
				//Ajouter code pour passer � la vue de connexion
				//
				//
				this.vue.toggleEnableButtonConnexion();
				this.vue.toggleEnableButtonDeconnexion();
			} catch (IOException e1) {
				
				e1.printStackTrace();
			}
		}
		
		
		//Cas connexion
		if((JButton) e.getSource() == this.vue.getButtonConnexion() ) {
			try {
				Utilisateur.getSelf().setPseudo(this.vue.getDisplayedPseudo());
				this.commUDP.sendMessageConnecte();
				this.commUDP.sendMessageInfoPseudo();
				
				this.vue.toggleEnableButtonConnexion();
				this.vue.toggleEnableButtonDeconnexion();
			} catch (IOException e1) {
				
				e1.printStackTrace();
			}
		}
		
		else if(this.vue.isButtonTab(e.getSource()) ){
			JButton button = (JButton) e.getSource();
			int index = this.vue.removeSession(button);
			this.idsSessionEnCours.remove(index);
		}
	}
	

	//---------- WINDOW LISTENER OPERATIONS ----------//

	@Override
	public void windowClosing(WindowEvent e) {
		
		try {
			this.commUDP.sendMessageDelete();
		} catch (IOException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		
	}
	
	@Override
	public void windowOpened(WindowEvent e) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void windowClosed(WindowEvent e) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void windowIconified(WindowEvent e) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void windowDeiconified(WindowEvent e) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void windowActivated(WindowEvent e) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void windowDeactivated(WindowEvent e) {
		// TODO Auto-generated method stub
		
	}
	
	
	//------------SOCKET-------------//
	
	private void sendMessage(Socket sock, String message) throws IOException {
		PrintWriter output= new PrintWriter(sock.getOutputStream(), true);
			output.println(message);
	}
	
	private String readMessage(Socket sock) throws IOException {
		
		BufferedReader input = new BufferedReader(new InputStreamReader( sock.getInputStream() ));
		return input.readLine();
	}

	
	//------------OBSERVERS-------------//
	
	@Override
	public void update(Object o, Object arg) {
		
		if(o == this.tcpServ) {
			
			Socket sockAccept = (Socket) arg;
			
			try {
				
				String pseudoOther = this.readMessage(sockAccept);
				String idOther = this.commUDP.getUserFromPseudo(pseudoOther).getId();
				 
				int reponse;
						
				if(!this.idsSessionEnCours.contains(idOther)) {
					reponse = this.vue.displayJOptionAskForSession(pseudoOther);
					System.out.println("reponse : " + reponse);
				}else {
					reponse = 1;
				}
				
				if(reponse == 0) {
					
					this.idsSessionEnCours.add(idOther);
					this.sendMessage(sockAccept, "accepted");
					
					VueSession session = new VueSession(sockAccept, idOther, pseudoOther, this.sqlManager);
					this.vue.addSession(pseudoOther, session);
				}else {
					this.sendMessage(sockAccept, "refused");
					sockAccept.close();
				}
				
				
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}
	
	@Override
	public void updateList(Object o, ArrayList<Utilisateur> userList) {
		
		if(o == this.commUDP) {
			ArrayList<String> pseudos = new ArrayList<String>();
			for (Utilisateur u : userList) {
				pseudos.add(u.getPseudo());
			}
			this.vue.setActiveUsersList(pseudos);
		}
	}

	@Override
	public void updateSocketState(Object o, Object arg) {
		VueSession session = (VueSession) arg;
		int index = this.vue.removeSession(session);
		this.idsSessionEnCours.remove(index);
	}

}
