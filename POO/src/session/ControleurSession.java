package session;

import java.awt.event.ActionEvent;

import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.io.File;
import java.io.IOException;
import java.net.Socket;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collections;

import javax.swing.JButton;
import javax.swing.JFileChooser;

import communication.filetransfer.FileTransferClient;
import communication.filetransfer.FileTransferServer;
import communication.tcp.TCPClient;
import database.SQLiteManager;
import main.Utilisateur;
import messages.MauvaisTypeMessageException;
import messages.Message;
import messages.MessageFichier;
import messages.MessageTexte;
import messages.Message.TypeMessage;
import observers.ObserverInputMessage;
import observers.ObserverSocketState;

public class ControleurSession implements ActionListener, ObserverInputMessage, ObserverSocketState, KeyListener {

	private VueSession vue;
	private String idOther;
	private String pseudoOther;
	private TCPClient tcpClient;
	private ArrayList<Message> messagesIn;
	private ArrayList<Message> messagesOut;
	private SQLiteManager sqlManager;
	private ArrayList<File> files;
	
	
	/**
	 * 
	 * @param vue
	 * @param socketComm
	 * @param idOther
	 * @param pseudoOther
	 * @param sqlManager
	 * @throws IOException
	 */
	protected ControleurSession(VueSession vue, Socket socketComm, String idOther, String pseudoOther, SQLiteManager sqlManager) throws IOException {
		this.vue = vue;
		this.tcpClient = new TCPClient(socketComm);
		this.tcpClient.setObserverInputThread(this);
		this.tcpClient.setObserverSocketState(this);
		this.tcpClient.startInputThread();
		this.messagesIn = new ArrayList<Message>();
		this.messagesOut = new ArrayList<Message>();
		
		this.idOther = idOther;
		this.pseudoOther = pseudoOther;
		
		this.sqlManager = sqlManager;
		
		this.files = new ArrayList<File>();
	}

	// ---------- ACTION LISTENER OPERATIONS ----------//
	@Override
	public void actionPerformed(ActionEvent e) {

		//If the button "Envoyer" is pressed
		if ((JButton) e.getSource() == this.vue.getButtonEnvoyer()) {
			String messageContent = this.vue.getInputedText();
			System.out.println(messageContent);
			
			if(!this.files.isEmpty()) {
				this.processSelectedFiles(messageContent);
				if(!this.files.isEmpty()) {
					this.askFileTransfer();
					
					this.vue.resetZoneSaisie();
					messageContent = "";
				}
			}
			
			
			//If the text field is not empty
			if (!messageContent.equals("")) {
				
				//Retrieve the date and prepare the messages to send/display
				MessageTexte messageOut = null;
	
				try {
					messageOut = new MessageTexte(TypeMessage.TEXTE, messageContent);
					messageOut.setSender(Utilisateur.getSelf().getPseudo());
				} catch (MauvaisTypeMessageException e2) {
					e2.printStackTrace();
				}
				
				
				try {
					this.tcpClient.sendMessage(messageOut);	
				} catch (IOException e1) {
					e1.printStackTrace();
				}
				
				messageOut.setSender("Moi");
				this.vue.appendMessage(messageOut);
				this.vue.resetZoneSaisie();
				
				this.messagesOut.add(messageOut);
			}
		}
		
		//If the button "Importer" is pressed
		if((JButton) e.getSource() == this.vue.getButtonImportFile()) {
			//Display a file chooser to select one or several files
			JFileChooser fc = new JFileChooser();
			fc.setMultiSelectionEnabled(true);
			int returVal = fc.showDialog(this.vue, "Importer");
		
			
			if(returVal == JFileChooser.APPROVE_OPTION) {
				File[] files = fc.getSelectedFiles();
				Collections.addAll(this.files, files);
				for(File file : files) {
					this.vue.appendInputedText(file.getName());
					this.vue.appendInputedText(";");
				}
			}
			
		}

	}
	

	@Override
	public void keyTyped(KeyEvent e) {}

	@Override
	public void keyPressed(KeyEvent e) {
		if(e.getKeyCode() == KeyEvent.VK_ENTER) {
			if(!e.isShiftDown()) {
				this.vue.getButtonEnvoyer().doClick();
			}
			
		}	
	}

	@Override
	public void keyReleased(KeyEvent e) {}
	
	
	protected ArrayList<Message> getHistorique(){
		try {
			ArrayList<Message> historique = this.sqlManager.getHistoriquesMessages(idOther, pseudoOther);
			return historique;
		} catch (SQLException e) {
			e.printStackTrace();
			return new ArrayList<Message>();
			
		}
	}
	
	private void processSelectedFiles(String input) {
			String[] tmp = input.split(";");
			ArrayList<String> potentialFiles = new ArrayList<String>();
			Collections.addAll(potentialFiles, tmp);
			
			for(File file: this.files) {
				if(!potentialFiles.contains(file.getName()) ) {
					this.files.remove(file);
				}
			}
	}
	
	private void askFileTransfer() {
		try {
			MessageFichier messageOut = new MessageFichier(TypeMessage.FICHIER_INIT, ""+this.files.size(), "");
			this.tcpClient.sendMessage(messageOut);
		} catch (MauvaisTypeMessageException | IOException e1) {
			e1.printStackTrace();
		}
	}
	
	private void answerFileTransfer(int port) {
		try {
			MessageFichier messageOut = new MessageFichier(TypeMessage.FICHIER_ANSWER, ""+port, "");
			this.tcpClient.sendMessage(messageOut);
		} catch (MauvaisTypeMessageException | IOException e1) {
			e1.printStackTrace();
		}
	}
	
	//Method called when a message is received from the TCP socket 
	@Override
	public void updateInput(Object o, Object arg) {
		Message message = (Message) arg;
		
		switch(message.getTypeMessage()) {
		case TEXTE:
			System.out.println(message.toString());
			this.vue.appendMessage(message);
			this.messagesIn.add(message);
			break;
		case IMAGE:
			this.vue.appendImage(message);
			
			if(message.getSender().equals("Moi")) {
				this.messagesOut.add(message);
			}else {
				this.messagesIn.add(message);
			}
			break;
		case FICHIER:
			this.vue.appendMessage(message);
			
			if(message.getSender().equals("Moi")) {
				this.messagesOut.add(message);
			}else {
				this.messagesIn.add(message);
			}
			break;
			
		case FICHIER_INIT:
			try {
				MessageFichier mFichier = (MessageFichier) arg;
				int nbFile = Integer.parseInt(mFichier.getContenu());
				FileTransferServer fts = new FileTransferServer(nbFile, this);
				int port = fts.getPort();
				fts.start();
				this.answerFileTransfer(port);
				
			} catch (IOException e) {
				e.printStackTrace();
			}
			break;
		case FICHIER_ANSWER:
			try {
				MessageFichier mFichier = (MessageFichier) arg;
				int port = Integer.parseInt(mFichier.getContenu());
				
				@SuppressWarnings("unchecked")
				FileTransferClient ftc = new FileTransferClient(port ,(ArrayList<File>) this.files.clone(), this);
				
				ftc.sendFiles();
				this.files.clear();
			} catch (IOException | InterruptedException e) {
				e.printStackTrace();
			}
			
			break;
			
		//Do nothing
		default:
		}
		
	}

	//If the other user closes the session or the communication is broken
	//Disable the view (TextArea, Buttons..) and display a message
	@Override
	public void updateSocketState(Object o, Object arg) {
		this.vue.endSession(this.pseudoOther);	
	}
	
	/**
	 * 
	 */
	protected void destroyAll() {
		String idSelf = Utilisateur.getSelf().getId();
		String idOther = this.idOther;
		
		try {
			this.sqlManager.insertAllMessages(messagesOut, idSelf, idOther);
			this.sqlManager.insertAllMessages(messagesIn, idOther, idSelf);
		} catch (SQLException e) {
			e.printStackTrace();
		}
		
		this.vue = null;
		this.tcpClient.destroyAll();
		this.tcpClient = null;
	}

}