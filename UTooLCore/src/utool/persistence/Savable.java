package utool.persistence;

/**
 * This interface indicates an object can be saved and loaded using the PreferencesManager.
 * @author kreierj
 *
 */
public interface Savable {

	/**
	 * Converts the savable object into a parsable String representation such that when
	 * load is called using the String, the Savable returned by load is equal to the Savable
	 * that created the String.
	 * @return String representation of the object
	 */
	public String save();
	
	/**
	 * Instantiates a Savable object from a parsable String representation of an equivalent Savable object
	 * @param value The parsable representation of this Savable object
	 * @return A new savable object from the parsed data
	 */
	public Savable load(String value);
}
