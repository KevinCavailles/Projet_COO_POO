package messages;

public class MessageSysteme extends Message {
	
	private static final long serialVersionUID = 1L;
	private String pseudo;
	private String id;
	private int port;

	// ------ CONSTRUCTORS ------ //
	
	/**
	 * Create a system message. These message are used for all system interactions by the UDP channel.
	 * The "JE_SUIS_CONNECTE" messages are used to inform the network that you just joined.
	 * They are sent directly after an user log in, and await multiple "INFO_PSEUDO" messages as answers, to build the table of users logged in.
	 * 
	 * @param TypeMessage type (must be JE_SUIS_CONNECTE, else an error is raised)
	 * @throws MauvaisTypeMessageException
	 */
	public MessageSysteme(TypeMessage type) throws MauvaisTypeMessageException{
		if (type==TypeMessage.JE_SUIS_CONNECTE) {
			this.type=type;
			this.pseudo="";
			this.id="";
			this.port = -1;
		}
		else throw new MauvaisTypeMessageException();
	}
	
	/**
	 * Create a system message. These message are used for all system interactions by the UDP channel.
	 * The "JE_SUIS_DECONNECTE" messages are used to inform the network that you just quit it.
	 * 
	 * The "INFO_PSEUDO" are used to give informations about you to another user, much like an business card.
	 * They are used either as an answer to a "JE_SUIS_CONNECTE" message or to inform the network of a change of pseudo.
	 * 
	 * @param TypeMessage type (must be JE_SUIS_DECONNECTE or INFO_PSEUDO, else an error is raised)
	 * @param pseudo : user pseudo as String
	 * @param id : user id as String
	 * @param port : "server" UDP port used by the application (used when the application id in local mode)
	 * 
	 * @throws MauvaisTypeMessageException
	 */
	public MessageSysteme(TypeMessage type, String pseudo, String id, int port) throws MauvaisTypeMessageException {
		if (type==TypeMessage.INFO_PSEUDO ||(type==TypeMessage.JE_SUIS_DECONNECTE)) {
			this.type=type;
			this.pseudo=pseudo;
			this.id=id;
			this.port = port;
		}
		else throw new MauvaisTypeMessageException();
	}
	
	
	// ----- GETTERS ----- //
	
	/**
	 * Returns pseudo of the sender of the message (when type == INFO_PSEUDO)
	 * 
	 * @return user pseudo as String
	 */
	public String getPseudo() {
		return this.pseudo;
	}
	
	/**
	 * Returns id of the sender of the message (when type == INFO_PSEUDO)
	 * 
	 * @return user id as String
	 */
	public String getId() {
		return this.id;
	}

	/**
	 * Returns the "server" UDP port used by the sender of the message
	 * 
	 * @return port as integer
	 */
	public int getPort() {
		return this.port;
	}
	
	
	// ----- MESSAGE-STRING CONVERSION METHODS -------//
	
	/**
	 * Implements attributsToString method of Message
	 * 
	 * @return attributes as a String
	 */
	@Override
	protected String attributsToString() {
		return this.pseudo+"###"+this.id+"###"+this.port;
	}
}
