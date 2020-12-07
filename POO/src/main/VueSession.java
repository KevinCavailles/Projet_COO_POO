package main;

import java.awt.*;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.IOException;
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
import javax.swing.ListSelectionModel;
import javax.swing.ScrollPaneConstants;
import javax.swing.border.EmptyBorder;

import communication.TCPClient;

public class VueSession extends Vue {
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	
	
	private JButton envoyerMessage;
	private JTextArea chatWindow;
	private JTextField chatInput;
	private ControleurSession c;

	public VueSession(String title, TCPClient tcpClient) throws IOException {
		
		super(title);
		
		this.c = new ControleurSession(this, tcpClient);
		
		this.setBounds(100, 100, 600, 600);
		JPanel main = new JPanel();
		main.setBorder(new EmptyBorder(5, 5, 5, 5));
		this.add(main);
		main.setLayout(new BorderLayout(0, 0));
		
		JPanel bottom = new JPanel();
		main.add(bottom, BorderLayout.SOUTH);
		bottom.setLayout(new BorderLayout(0, 0));
		
		this.chatInput = new JTextField();
	
		//textField.setPreferredSize(new Dimension(300, 50));
		bottom.add(this.chatInput);
		this.chatInput.setColumns(10);
		
		this.envoyerMessage = new JButton("Envoyer");
		this.envoyerMessage.addActionListener(this.c);
		
		bottom.add(this.envoyerMessage, BorderLayout.EAST);
		
		this.chatWindow = new JTextArea();
		this.chatWindow.setEditable(false);
		
		ScrollPane chatScroll = new ScrollPane();
		
		chatScroll.add(this.chatWindow);
		
		main.add(chatScroll, BorderLayout.CENTER);
		
		this.getRootPane().setDefaultButton(this.envoyerMessage);
		
		this.setDefaultCloseOperation(EXIT_ON_CLOSE);
	
		this.setSize(600,600);
		this.setVisible(true);
		
	}
	
	
	protected JButton getButtonEnvoyer() {
		return this.envoyerMessage;
	}
	
	protected JTextField getZoneSaisie() {
		return this.chatInput;
	}
	
	protected void resetZoneSaisie() {
		this.chatInput.setText("");
	}
	
	protected void appendMessage(String message) {
		this.chatWindow.append(message);
	}
	
}
