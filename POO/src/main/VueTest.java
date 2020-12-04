package main;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.EventQueue;

import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.border.EmptyBorder;
import java.awt.FlowLayout;
import javax.swing.JTextField;
import javax.swing.JButton;
import java.awt.ScrollPane;
import javax.swing.JTextPane;
import javax.swing.JTabbedPane;

public class VueTest extends JFrame {

	private JPanel contentPane;
	private JTextField txtEntrezUnMessage;

	/**
	 * Launch the application.
	 */
	public static void main(String[] args) {
		EventQueue.invokeLater(new Runnable() {
			public void run() {
				try {
					VueTest frame = new VueTest();
					frame.setVisible(true);
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		});
	}

	/**
	 * Create the frame.
	 */
	public VueTest() {
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		setBounds(100, 100, 600, 600);
		contentPane = new JPanel();
		contentPane.setBorder(new EmptyBorder(5, 5, 5, 5));
		setContentPane(contentPane);
		contentPane.setLayout(new BorderLayout(0, 0));
		
		JPanel panel = new JPanel();
		contentPane.add(panel, BorderLayout.SOUTH);
		panel.setLayout(new BorderLayout(0, 0));
		
		txtEntrezUnMessage = new JTextField();
		txtEntrezUnMessage.setText("Entrez un message");
		//textField.setPreferredSize(new Dimension(300, 50));
		panel.add(txtEntrezUnMessage);
		txtEntrezUnMessage.setColumns(10);
		
		JButton btnNewButton = new JButton("Envoyer");
		panel.add(btnNewButton, BorderLayout.EAST);
		
		JTextPane textPane = new JTextPane();
		
		ScrollPane scrollPane = new ScrollPane();
		contentPane.add(scrollPane, BorderLayout.CENTER);
		
		scrollPane.add(textPane);
	}

}
