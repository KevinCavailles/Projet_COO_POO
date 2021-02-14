package messages;

public class MessageFichier extends Message {
	

	private static final long serialVersionUID = 1L;
	private String contenu;
	private String extension;

	/**
	 * Create a file message. These message are used for all interactions regarding file transfer.
	 * 
	 * The "FICHIER_INIT" messages are used to inform the recipient application that you wish to transfer them files.
	 * The "FICHIER_ANSWER" messages are answers to "FICHIER_INIT" messages. They indicate that you are ready to receive the file.
	 * The "contenu" argument then contains the port on which you wish to receive the files.
	 * 
	 * The "FICHIER" messages contains the files themselves.
	 * The "IMAGE" messages contains images files, which means the application will display a thumbnail for the image to the recipient.
	 * 
	 * @param TypeMessage type (must be FICHIER_INIT, FICHIER_ANSWER, FICHIER or IMAGE, else an error is raised)
	 * @param contenu : message content as String
	 * @param extension : file extension as string
	 * 
	 * @throws MauvaisTypeMessageException
	 */
	public MessageFichier(TypeMessage type, String contenu, String extension) throws MauvaisTypeMessageException{
		if ((type==TypeMessage.IMAGE)||(type==TypeMessage.FICHIER) ||(type==TypeMessage.FICHIER_INIT) || (type==TypeMessage.FICHIER_ANSWER) ) {
			this.type=type;
			this.contenu=contenu;
			this.extension=extension;
			this.setDateMessage(Message.getDateAndTime());
		}
		else throw new MauvaisTypeMessageException();
	}

	// ----- GETTERS ----- //
	
	
	/**
	 * Returns content of the message
	 * 
	 * @return content as String
	 */
	public String getContenu() {
		return this.contenu;
	}
	
	/**
	 * Returns extension of the file contained in the message (if the message contains a file)
	 * 
	 * @return extension as String
	 */
	public String getExtension() {
		return this.extension;
	}

	// ----- MESSAGE-STRING CONVERSION METHODS -------//
	
	/**
	 * Implements attributsToString method of Message
	 * 
	 * @return attributes as a String
	 */	
	@Override
	protected String attributsToString() {
		return this.contenu+"###"+this.extension;
	}
	
	public String toString() {
		if(this.type == TypeMessage.IMAGE) {
			return this.contenu;
		}else {
			String suffixe;
			if(this.getSender().equals("Moi")) {
				suffixe = "envoyé\n";
			}else {
				suffixe = "reçu\n";
			}
			return "<"+this.getDateMessage()+"> : "+this.contenu+" "+suffixe;
		}
	}
}