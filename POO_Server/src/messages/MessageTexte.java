package messages;

public class MessageTexte extends Message {
	

	private static final long serialVersionUID = 1L;
	private String contenu;
	

	/**
	 * Create a text message. These message are used for basic text conversation via TCP.
	 * 
	 * @param TypeMessage type (must be TEXT, else an error is raised)
	 * @param contenu : message content as String
	 * 
	 * @throws MauvaisTypeMessageException
	 */
	public MessageTexte(TypeMessage type, String contenu) throws MauvaisTypeMessageException{
		if (type==TypeMessage.TEXTE) {
			this.type=type;
			this.contenu=contenu;
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
	

	// ----- MESSAGE-STRING CONVERSION METHODS -------//
	
	/**
	 * Implements attributsToString method of Message
	 * 
	 * @return attributes as a String
	 */	
	@Override
	protected String attributsToString() {
		return this.contenu;
	}
	
	@Override
	public String toString() {
		return "<"+this.getDateMessage()+"> "+this.getSender()+" : "+this.contenu+"\n";
	}
}