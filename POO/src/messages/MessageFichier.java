package messages;

public class MessageFichier extends Message {
	

	private static final long serialVersionUID = 1L;
	private String contenu;
	private String extension;

	public MessageFichier(TypeMessage type, String contenu, String extension) throws MauvaisTypeMessageException{
		if ((type==TypeMessage.IMAGE)||(type==TypeMessage.FICHIER) ||(type==TypeMessage.FICHIER_INIT) || (type==TypeMessage.FICHIER_ANSWER) ) {
			this.type=type;
			this.contenu=contenu;
			this.extension=extension;
			this.setDateMessage(Message.getDateAndTime());
		}
		else throw new MauvaisTypeMessageException();
	}

	public String getContenu() {
		return this.contenu;
	}
	
	public String getExtension() {
		return this.extension;
	}

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