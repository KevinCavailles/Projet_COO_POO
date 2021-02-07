package session;

import java.awt.*;
import java.awt.event.KeyEvent;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.net.Socket;
import java.util.ArrayList;

import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextPane;
import javax.swing.KeyStroke;
import javax.swing.ScrollPaneConstants;
import javax.swing.border.EmptyBorder;
import javax.swing.text.AbstractDocument;
import javax.swing.text.BadLocationException;
import javax.swing.text.BoxView;
import javax.swing.text.ComponentView;
import javax.swing.text.Document;
import javax.swing.text.Element;
import javax.swing.text.IconView;
import javax.swing.text.LabelView;
import javax.swing.text.ParagraphView;
import javax.swing.text.StyleConstants;
import javax.swing.text.StyledDocument;
import javax.swing.text.StyledEditorKit;
import javax.swing.text.View;
import javax.swing.text.ViewFactory;

import communication.filetransfer.FileTransferUtils;
import database.SQLiteManager;
import messages.Message;
import messages.Message.TypeMessage;

public class VueSession extends JPanel {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	private JButton sendMessage;
	private JButton importFile;
	private JTextPane chatWindow;
	private JTextArea chatInput;
	private ControleurSession c;

	public VueSession(Socket socketComm, String idOther, String pseudoOther, SQLiteManager sqlManager)
			throws IOException {

		this.c = new ControleurSession(this, socketComm, idOther, pseudoOther, sqlManager);

		this.setBorder(new EmptyBorder(5, 5, 5, 5));
		this.setLayout(new BorderLayout(0, 0));

		this.chatInput = new JTextArea();
		this.chatInput.setColumns(10);
		this.chatInput.setLineWrap(true);
		this.chatInput.setWrapStyleWord(true);
		this.chatInput.addKeyListener(this.c);

		this.chatWindow = new JTextPane();
		this.chatWindow.setEditable(false);
		this.chatWindow.setEditorKit(new WrapEditorKit());

		JScrollPane chatScroll = new JScrollPane(this.chatWindow);
		chatScroll.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED);

		JPanel bottom = new JPanel();
		bottom.setLayout(new BorderLayout(0, 0));

		// remap "ENTER" to "none" to avoid "\n" in the input area after sending message
		KeyStroke enter = KeyStroke.getKeyStroke("ENTER");
		this.chatInput.getInputMap().put(enter, "none");
		KeyStroke shiftEnter = KeyStroke.getKeyStroke(KeyEvent.VK_ENTER, KeyEvent.SHIFT_DOWN_MASK);
		this.chatInput.getInputMap().put(shiftEnter, "insert-break");

		this.importFile = new JButton("Importer..");
		this.importFile.addActionListener(this.c);

		JScrollPane inputScroll = new JScrollPane(this.chatInput);
		inputScroll.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);

		this.sendMessage = new JButton("Envoyer");
		this.sendMessage.addActionListener(this.c);

		bottom.add(this.importFile, BorderLayout.WEST);
		bottom.add(inputScroll, BorderLayout.CENTER);
		bottom.add(this.sendMessage, BorderLayout.EAST);

		this.add(chatScroll, BorderLayout.CENTER);
		this.add(bottom, BorderLayout.SOUTH);

		this.setPreferredSize(new Dimension(500, 500));

		this.displayHistorique();

	}

	protected JButton getButtonEnvoyer() {
		return this.sendMessage;
	}

	protected JButton getButtonImportFile() {
		return this.importFile;
	}

	protected String getInputedText() {
		return this.chatInput.getText();
	}

	protected void appendInputedText(String str) {
		this.chatInput.append(str);
	}


	protected void resetZoneSaisie() {
		this.chatInput.setText("");
		
	}

	private void setCaretToEnd() {
		this.chatWindow.setCaretPosition(this.chatWindow.getDocument().getLength());
	}
	
	protected void appendString(String str) {
		try {
			Document doc = this.chatWindow.getDocument();
			doc.insertString(doc.getLength(), str, null);
		} catch (BadLocationException e) {
			e.printStackTrace();
		}
	}

	protected void appendMessage(Message message) {

		try {
			StyledDocument sdoc = this.chatWindow.getStyledDocument();
			// sdoc.setParagraphAttributes(sdoc.getLength(), message.toString().length()-1,
			// style, false);
			sdoc.insertString(sdoc.getLength(), message.toString(), null);

		} catch (BadLocationException e) {
			e.printStackTrace();
		}
	}

	protected void appendImage(Message message) {
		this.setCaretToEnd();
		
		String imgString = message.toString();
		Icon ic;
		try {
			BufferedImage img = FileTransferUtils.decodeImage(imgString);
			ic = new ImageIcon(img);
			this.chatWindow.insertIcon(ic);
			this.appendString("\n");
			
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		
	}

	private void printLineSeparator() {
		this.appendString("------------------------------------------\n");
	}

	protected void endSession(String pseudoOther) {
		this.printLineSeparator();
		this.appendString(pseudoOther + " a mis fin à la session.");
		this.chatInput.setEnabled(false);
		this.chatInput.setFocusable(false);
		this.sendMessage.setEnabled(false);
		this.importFile.setEnabled(false);
	}

	private void displayHistorique() {
		ArrayList<Message> historique = this.c.getHistorique();

		for (Message m : historique) {
			if(m.getTypeMessage() == TypeMessage.IMAGE) {
				this.appendImage(m);
			}else {
				this.appendMessage(m);
			}
		}

		if (historique.size() > 0) {
			this.printLineSeparator();
		}
	}

	public void destroyAll() {
		if (this.c != null) {
			this.c.destroyAll();
		}
		this.c = null;
		this.chatInput = null;
		this.chatWindow = null;
		this.sendMessage = null;
	}

	// ------------- PRIVATE CLASS TO WRAP TEXT -------------//

	class WrapEditorKit extends StyledEditorKit {
	
		private static final long serialVersionUID = 1L;
		
		ViewFactory defaultFactory = new WrapColumnFactory();

		public ViewFactory getViewFactory() {
			return defaultFactory;
		}

	}

	class WrapColumnFactory implements ViewFactory {
		public View create(Element elem) {
			String kind = elem.getName();
			if (kind != null) {
				if (kind.equals(AbstractDocument.ContentElementName)) {
					return new WrapLabelView(elem);
				} else if (kind.equals(AbstractDocument.ParagraphElementName)) {
					return new ParagraphView(elem);
				} else if (kind.equals(AbstractDocument.SectionElementName)) {
					return new BoxView(elem, View.Y_AXIS);
				} else if (kind.equals(StyleConstants.ComponentElementName)) {
					return new ComponentView(elem);
				} else if (kind.equals(StyleConstants.IconElementName)) {
					return new IconView(elem);
				}
			}

			// default to text display
			return new LabelView(elem);
		}
	}

	class WrapLabelView extends LabelView {
		public WrapLabelView(Element elem) {
			super(elem);
		}

		public float getMinimumSpan(int axis) {
			switch (axis) {
			case View.X_AXIS:
				return 0;
			case View.Y_AXIS:
				return super.getMinimumSpan(axis);
			default:
				throw new IllegalArgumentException("Invalid axis: " + axis);
			}
		}

	}
}