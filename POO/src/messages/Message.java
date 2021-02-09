package messages;

import java.io.Serializable;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public abstract class Message implements Serializable {

	public enum TypeMessage {JE_SUIS_CONNECTE, JE_SUIS_DECONNECTE, INFO_PSEUDO, TEXTE, IMAGE, FICHIER, FICHIER_INIT, FICHIER_ANSWER}
	protected TypeMessage type;
	private String dateMessage;
	private String sender;
	private static final long serialVersionUID = 1L;
	
	
	
	// ------- GETTERS ------ //
	
	/**
	 * Returns the current date and time as a string using DateTimeFormatter and LocalDateTime
	 * 
	 * @return date and time as a String
	 */
	public static String getDateAndTime() {
		DateTimeFormatter dtf = DateTimeFormatter.ofPattern("yyyy/MM/dd HH:mm:ss");
		LocalDateTime now = LocalDateTime.now();
		return dtf.format(now);
	}
	
	/**
	 * Returns the type of the message
	 * 
	 * @return message type as TypeMessage
	 */
	public TypeMessage getTypeMessage() {
		return this.type;
	}
	
	/**
	 * Returns the date and time to which the message was timestamped
	 * 
	 * @return date and time of timestamp as String
	 */
	public String getDateMessage() {
		return this.dateMessage;
	}
	
	/**
	 * Returns the sender of the message (used in the database)
	 * 
	 * @return sender of message as String
	 */
	public String getSender() {
		return this.sender ;
	}
	

	
	// ------ SETTERS ------ //
	
	/**
	 * Set the date of the message to a specific timestamp
	 * 
	 * @param timestamp as (formatted) String
	 */
	public void setDateMessage(String dateMessage) {
		this.dateMessage = dateMessage;
	}
	
	/**
	 * Set the sender of the message to a specified string
	 * 
	 * @param sender pseudo as String
	 */
	public void setSender(String sender) {
		this.sender = sender;
	}	
	
	
	// ----- MESSAGE-STRING CONVERSION METHODS -------//

	/**
	 * Returns a string representing the formatted list of attributes
	 * 
	 *@return attributes as a String
	 */
	protected abstract String attributsToString();

	/**
	 * Returns the message as a formatted string
	 * 
	 *@return message as a String
	 */
	public String toString() {
		return this.type+"###"+this.attributsToString();
	}	

	/**
	 * Static method. Returns a message obtainer by parsing a given string
	 * 
	 *@param String representing a message
	 *@return Message
	 */
	public static Message stringToMessage(String messageString) {
		try {
			String[] parts = messageString.split("###");
			switch (parts[0]) {
			case "JE_SUIS_CONNECTE" :
				return new MessageSysteme(TypeMessage.JE_SUIS_CONNECTE);
				
			case "JE_SUIS_DECONNECTE" :
				return new MessageSysteme(TypeMessage.JE_SUIS_DECONNECTE, parts[1], parts[2], Integer.parseInt(parts[3]) );
				
			case "INFO_PSEUDO" :
				return new MessageSysteme(TypeMessage.INFO_PSEUDO, parts[1], parts[2], Integer.parseInt(parts[3]) );
				
			case "TEXTE" :
				return new MessageTexte(TypeMessage.TEXTE, parts[1]);
				
			case "IMAGE" :
				return new MessageFichier(TypeMessage.IMAGE, parts[1], parts[2]);
				
			case "FICHIER" :
				return new MessageFichier(TypeMessage.FICHIER, parts[1], parts[2]);
			}
		} catch (MauvaisTypeMessageException e) {}
			return null;
	}

}
