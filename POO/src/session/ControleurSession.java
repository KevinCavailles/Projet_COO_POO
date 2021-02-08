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
	 * Create the controller for this session. It will manage all the objects used
	 * to send/receive messages and files as well as the ones interacting with the
	 * database. It will handle the actions performed on the view and call the
	 * appropriate methods to display messages and data on the view.
	 * 
	 * @param vue         		The corresponding view
	 * @param socketComm  		The socket used to send/receive messages
	 * @param idOther     		The other user's id
	 * @param pseudoOther 		The other user's pseudo
	 * @param sqlManager  		The SQLManager instance to retrieve/insert
	 *                   		users,conversations,messages from/into the database
	 * @throws IOException
	 */
	protected ControleurSession(VueSession vue, Socket socketComm, String idOther, String pseudoOther,
			SQLiteManager sqlManager) throws IOException {
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

	
	// ---------- ACTION LISTENER OPERATIONS ---------- //
	@Override
	public void actionPerformed(ActionEvent e) {

		// If the button "Envoyer" is pressed
		if ((JButton) e.getSource() == this.vue.getButtonEnvoyer()) {
			String messageContent = this.vue.getInputedText();
			System.out.println(messageContent);

			if (!this.files.isEmpty()) {
				this.processSelectedFiles(messageContent);
				if (!this.files.isEmpty()) {
					this.askFileTransfer();

					this.vue.resetZoneSaisie();
					messageContent = "";
				}
			}

			// If the text field is not empty
			if (!messageContent.equals("")) {

				// Retrieve the date and prepare the messages to send/display
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

		// If the button "Importer" is pressed
		if ((JButton) e.getSource() == this.vue.getButtonImportFile()) {

			// Display a file chooser to select one or several files
			JFileChooser fc = new JFileChooser();
			fc.setMultiSelectionEnabled(true);
			int returVal = fc.showDialog(this.vue, "Importer");

			// If the user clicked on "Importer",
			// Retrieve all the files he clicked on.
			// The files are stored in this.files
			// and their names are display in the ChatInput.
			if (returVal == JFileChooser.APPROVE_OPTION) {
				File[] files = fc.getSelectedFiles();
				Collections.addAll(this.files, files);
				for (File file : files) {
					this.vue.appendInputedText(file.getName());
					this.vue.appendInputedText(";");
				}
			}

		}

	}

	
	// ---------- KEY LISTENER METHODS ---------- //

	@Override
	public void keyTyped(KeyEvent e) {
	}

	
	@Override
	public void keyPressed(KeyEvent e) {
		if (e.getKeyCode() == KeyEvent.VK_ENTER) {
			if (!e.isShiftDown()) {
				this.vue.getButtonEnvoyer().doClick();
			}

		}
	}

	
	@Override
	public void keyReleased(KeyEvent e) {
	}

	
	// ---------- OTHERS ---------- //

	/**
	 * Create and send the message to ask for a file transfer.
	 */
	private void askFileTransfer() {
		try {
			MessageFichier messageOut = new MessageFichier(TypeMessage.FICHIER_INIT, "" + this.files.size(), "");
			this.tcpClient.sendMessage(messageOut);
		} catch (MauvaisTypeMessageException | IOException e1) {
			e1.printStackTrace();
		}
	}

	
	/**
	 * Create and send the answer with the port on which the FileTransferServer is
	 * listening.
	 * 
	 * @param port
	 */
	private void answerFileTransfer(int port) {
		try {
			MessageFichier messageOut = new MessageFichier(TypeMessage.FICHIER_ANSWER, "" + port, "");
			this.tcpClient.sendMessage(messageOut);
		} catch (MauvaisTypeMessageException | IOException e1) {
			e1.printStackTrace();
		}
	}

	
	/**
	 * Retrieve the files' names from the given input using ";" as a separator
	 * Removes the files whose names are missing.
	 * 
	 * This method is used to check if a file's name has been deleted/overwritten in
	 * the ChatInput. Indeed, the only way to cancel the import of a file is by
	 * deleting its name from the ChatInput.
	 * 
	 * @param input
	 */
	private void processSelectedFiles(String input) {
		String[] tmp = input.split(";");
		ArrayList<String> potentialFiles = new ArrayList<String>();
		Collections.addAll(potentialFiles, tmp);

		for (File file : this.files) {
			if (!potentialFiles.contains(file.getName())) {
				this.files.remove(file);
			}
		}
	}

	
	/**
	 * Retrieve the messages previously exchanged between the current user of the
	 * application and the other user of this session
	 * 
	 * @return The ArrayList of all previous messages
	 */
	protected ArrayList<Message> getHistorique() {
		try {
			ArrayList<Message> historique = this.sqlManager.getMessageRecord(idOther, pseudoOther);
			return historique;
		} catch (SQLException e) {
			e.printStackTrace();
			return new ArrayList<Message>();

		}
	}

	
	/**
	 * Method used when the session is over. Insert every message exchanged in the
	 * database, set all attributes' references to null, and call destroyAll() on
	 * the TCPClient.
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

	
	// ---------- OBSERVERS ---------- //

	// Method called when a message is received from the TCP socket
	@Override
	public void updateInput(Object o, Object arg) {
		Message message = (Message) arg;

		switch (message.getTypeMessage()) {

		// If it is a simple text message, display it
		case TEXTE:
			System.out.println(message.toString());
			this.vue.appendMessage(message);
			this.messagesIn.add(message);
			break;

		// If it is an image, display a thumbnail
		case IMAGE:
			this.vue.appendImage(message);

			if (message.getSender().equals("Moi")) {
				this.messagesOut.add(message);
			} else {
				this.messagesIn.add(message);
			}
			break;

		// If it is a file, display a message saying whether it has been sent/received.
		case FICHIER:
			this.vue.appendMessage(message);

			if (message.getSender().equals("Moi")) {
				this.messagesOut.add(message);
			} else {
				this.messagesIn.add(message);
			}
			break;

		// If it is a demand for a file transfer, create a new FileTransferServer, start
		// it
		// and answer back with "FICHIER_ANSWER" message containing the port of the
		// server.
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

		// If it is an answer for a file transfer, create a FilteTransferClient
		// with the port received and the path of the file(s) to send,
		// and send the files.
		case FICHIER_ANSWER:
			try {
				MessageFichier mFichier = (MessageFichier) arg;
				int port = Integer.parseInt(mFichier.getContenu());

				@SuppressWarnings("unchecked")
				FileTransferClient ftc = new FileTransferClient(port, (ArrayList<File>) this.files.clone(), this);

				ftc.sendFiles();
				this.files.clear();
			} catch (IOException | InterruptedException e) {
				e.printStackTrace();
			}

			break;

		// Do nothing
		default:
		}

	}

	// If the other user closes the session or the communication is broken
	// Disable the view (TextArea, Buttons..) and display a message
	@Override
	public void updateSocketState(Object o, Object arg) {
		this.vue.endSession(this.pseudoOther);
	}

}