package session;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextPane;
import javax.swing.text.BadLocationException;

public class FileComponent extends JPanel {
	
	private JTextPane container;
	JPanel self;
	
	public FileComponent(JTextPane container,String label) {
		super();
		this.self = this;
		this.container = container;
		JLabel l = new JLabel(label);
		l.setEnabled(false);
		JButton close = new JButton("X");
		close.addActionListener(new ActionListener() {
			
			@Override
			public void actionPerformed(ActionEvent e) {

				try {
					container.getDocument().remove(container.getDocument().getLength()-3, 1);
				} catch (BadLocationException e1) {
					// TODO Auto-generated catch block
					e1.printStackTrace();
				}
				container.repaint();
				
			}
		});
		this.add(l);
		this.add(close);
		
		
	}
}
