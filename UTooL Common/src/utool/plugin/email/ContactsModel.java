package utool.plugin.email;

import android.database.Cursor;
/**
 * Class for holding a row of the database in memory form.
 * @author waltzm
 * @version 4/28/2013
 */
public class ContactsModel {
	
	/**
	 * info
	 */
	private String info;
	
	/**
	 * id
	 */
	private long id=0;
	
	/**
	 * type
	 */
	private int type;
	
	/**
	 * Creates a contact
	 * @param info the list of items comma delimited
	 * @param id the primary key fo the row
	 * @param type the type, I.E email(1) or phone (2)
	 */
	public ContactsModel(String info, long id, int type)
	{
		this.setInfo(info);
		this.setId(id);
		this.setType(type);
	}

	/**
	 * Getter for the info
	 * @return info
	 */
	public String getInfo() {
		return info;
	}

	/**
	 * Setter for the info
	 * @param info the comma delimated list of informations
	 */
	public void setInfo(String info) {
		this.info = info;
	}

	/**
	 * Getter for id
	 * @return id
	 */
	public long getId() {
		return id;
	}

	/**
	 * Setter for id
	 * @param id the primary key id
	 */
	public void setId(long id) {
		this.id = id;
	}
	
	/**
	 * Creates a ContactsModel from the curser to a row in the db
	 * @param cursor cursor pointing to a row
	 * @return the model
	 */
	public static ContactsModel cursorToContacts(Cursor cursor)
	{
		ContactsModel c = new ContactsModel(cursor.getString(1),cursor.getLong(0), cursor.getInt(2));
		
		return c;
	}

	/**
	 * Getter for type
	 * @return type
	 */
	public int getType() {
		return type;
	}

	/**
	 * Setter for type.
	 * 1=email
	 * 2=phone
	 * @param type of row
	 */
	public void setType(int type) {
		this.type = type;
	}

}
