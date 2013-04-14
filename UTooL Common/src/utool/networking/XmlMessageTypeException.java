package utool.networking;

/**
 * Internal exception class for invalid message types
 * @author Cory
 *
 */
public class XmlMessageTypeException extends Exception {
	/**
	 * Generated serial uid
	 */
	private static final long serialVersionUID = -8241101238551655795L;

	/**
	 * Constructor
	 * @param message The error message
	 */
	public XmlMessageTypeException(String message) {
		super(message);
	}
}