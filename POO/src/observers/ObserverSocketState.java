package observers;

public interface ObserverSocketState {

	/**
	 * Method called when a TCP socket is closed/a communication is broken
	 * 
	 * @param o   : The observer to notify
	 * @param arg : An object
	 */
	public void updateSocketState(Object o, Object arg);

}
