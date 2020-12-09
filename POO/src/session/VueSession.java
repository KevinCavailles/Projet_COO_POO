package session;

import java.awt.*;
import java.awt.event.KeyEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.IOException;
import java.net.Socket;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.List;
import java.util.Vector;

import javax.swing.BorderFactory;
import javax.swing.DefaultListModel;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTabbedPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.JTextPane;
import javax.swing.KeyStroke;
import javax.swing.ListSelectionModel;
import javax.swing.ScrollPaneConstants;
import javax.swing.border.EmptyBorder;

import communication.TCPClient;

public class VueSession extends JPanel{

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	private JButton envoyerMessage;
	private JTextArea chatWindow;
	private JTextArea chatInput;
	private ControleurSession c;

	public VueSession(Socket socketComm) throws IOException {

		this.c = new ControleurSession(this, socketComm);

		this.setBorder(new EmptyBorder(5, 5, 5, 5));
		this.setLayout(new BorderLayout(0, 0));

		JPanel bottom = new JPanel();
		bottom.setLayout(new BorderLayout(0, 0));

		this.chatInput = new JTextArea();
		this.chatInput.setColumns(10);
		this.chatInput.setLineWrap(true);
		this.chatInput.setWrapStyleWord(true);
		this.chatInput.addKeyListener(this.c);
		
		//remap enter to none to avoid \n after sending message
		KeyStroke enter = KeyStroke.getKeyStroke("ENTER");
		this.chatInput.getInputMap().put(enter, "none");
		KeyStroke shiftEnter = KeyStroke.getKeyStroke(KeyEvent.VK_ENTER, KeyEvent.SHIFT_DOWN_MASK);
		this.chatInput.getInputMap().put(shiftEnter, "insert-break");
		
		JScrollPane inputScroll = new JScrollPane(this.chatInput);
		inputScroll.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
		
		//inputScroll.add(this.chatInput, BorderLayout.CENTER);

		this.envoyerMessage = new JButton("Envoyer");
		this.envoyerMessage.addActionListener(this.c);

		bottom.add(inputScroll);
		bottom.add(this.envoyerMessage, BorderLayout.EAST);

		this.chatWindow = new JTextArea();
		this.chatWindow.setEditable(false);
		this.chatWindow.setLineWrap(true);
		this.chatWindow.setWrapStyleWord(true);

		JScrollPane chatScroll = new JScrollPane(this.chatWindow);

		this.add(chatScroll, BorderLayout.CENTER);
		this.add(bottom, BorderLayout.SOUTH);

		this.setPreferredSize(new Dimension(500, 500));
		// this.setVisible(true);

	}

	protected JButton getButtonEnvoyer() {
		return this.envoyerMessage;
	}

	protected JTextArea getZoneSaisie() {
		return this.chatInput;
	}

	protected void resetZoneSaisie() {
		this.chatInput.setText("");
	}

	protected void appendMessage(String message) {
		this.chatWindow.append(message);
	}

}
