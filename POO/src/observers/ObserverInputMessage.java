package observers;

public interface ObserverInputMessage {

	/**
	 * Method called when data is received from a TCP socket
	 * 
	 * @param o   : The observer to notify
	 * @param arg : An object
	 */
	public void updateInput(Object o, Object arg);
}
