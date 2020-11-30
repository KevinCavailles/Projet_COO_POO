package messages;

import messages.Message.TypeMessage;

public class MessageActivite extends Message {
	
	private String pseudo;

	public MessageSysteme(TypeMessage t){
		if (t!=TypeMessage.) {
			this.type=t;
		}
	}

}
