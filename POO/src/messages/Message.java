package messages;

import java.io.Serializable;

public class Message implements Serializable {
	
	protected enum TypeMessage {JE_SUIS_ACTIF, JE_SUIS_INACTIF, INFO_PSEUDO, TEXTE, IMAGE, FICHIER}
	protected TypeMessage type;
	protected static final long serialVersionUID = 1L;
	protected Exception
	
	public TypeMessage getTypeMessage() {
		return this.type;
	}
}
