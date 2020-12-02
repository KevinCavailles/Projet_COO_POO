package messages;

public class MessageSysteme extends Message {
	
	private static final long serialVersionUID = 1L;
	private String pseudo;

	public MessageSysteme(TypeMessage type) throws MauvaisTypeMessageException{
		if ((type==TypeMessage.JE_SUIS_CONNECTE)||(type==TypeMessage.JE_SUIS_DECONNECTE)||(type==TypeMessage.MESSAGE_NUL)) {
			this.type=type;
			this.pseudo="";
		}
		else throw new MauvaisTypeMessageException();
	}
	
	public MessageSysteme(TypeMessage type, String pseudo) throws MauvaisTypeMessageException {
		if (type==TypeMessage.INFO_PSEUDO) {
			this.type=type;
			this.pseudo=pseudo;
		}
		else throw new MauvaisTypeMessageException();
	}
	
	public String getPseudo() {
		return this.pseudo;
	}

	@Override
	protected String attributsToString() {
		return this.pseudo;
	}
}
