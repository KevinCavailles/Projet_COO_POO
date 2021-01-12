package messages;

public class MessageTexte extends Message {
	

	private static final long serialVersionUID = 1L;
	private String contenu;

	public MessageTexte(TypeMessage type, String contenu) throws MauvaisTypeMessageException{
		if (type==TypeMessage.TEXTE) {
			this.type=type;
			this.contenu=contenu;
		}
		else throw new MauvaisTypeMessageException();
	}

	public String getContenu() {
		return this.contenu;
	}

	@Override
	protected String attributsToString() {
		return this.contenu;
	}
}