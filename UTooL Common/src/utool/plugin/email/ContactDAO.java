package utool.plugin.email;

import java.util.ArrayList;
import java.util.List;
import java.util.StringTokenizer;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

/**
 * Database Access Object for Contact information.
 * dao.open() and dao.close() MUST be called for proper operation
 * @author waltzm
 * @version 4/27/2013
 */
public class ContactDAO {

	/**
	 * Designates that the type is email addresses
	 */
	public static final int EMAIL_TYPE = 1;

	/**
	 * Designates that the type is phone numbers
	 */
	public static final int PHONE_TYPE = 2;

	/**
	 * the database
	 */
	private SQLiteDatabase db;

	/**
	 * the custom content  provider
	 */
	private ContactContentProvider ccp;

	/**
	 * holds all columns in db
	 */
	private String[] asterik = {ContactContentProvider.COLUMN_ID, ContactContentProvider.COLUMN_INFO, ContactContentProvider.COLUMN_TYPE};


	/**
	 * Creates the data access object
	 * @param c the context
	 */
	public ContactDAO(Context c)
	{
		ccp = new ContactContentProvider(c);

	}

	/**
	 * Opens the connection to the db
	 * @throws SQLException throws if needed
	 */
	public void open() throws SQLException
	{
		try{
			db = ccp.getWritableDatabase();
		} catch(SQLException e){
			Log.e("SQLException","Unable to open the database. Commonly caused by device not having an SD card to save the database on");
			throw e;
		}
	}

	/**
	 * closes the connection to the db
	 */
	public void close()
	{
		ccp.close();
	}

	/**
	 * Creates the row in the db
	 * @param value the list of contacts
	 * @param type the type of contact
	 * @return the ContactsMdel created or null if something went wrong
	 */
	private ContactsModel createContact(String value, int type)
	{
		ContentValues values = new ContentValues();

		values.put(ContactContentProvider.COLUMN_INFO, value);
		values.put(ContactContentProvider.COLUMN_TYPE, type);

		long id = db.insert(ContactContentProvider.TABLE_NAME, null, values);

		if(id==-1)
		{
			//something went wrong
			return null;
		}

		Cursor c = db.query(ContactContentProvider.TABLE_NAME, asterik, ContactContentProvider.COLUMN_ID +" = "+ id, null, null, null, null);

		c.moveToFirst();

		ContactsModel model = ContactsModel.cursorToContacts(c);

		c.close();

		return model;
	}

	/**
	 * Puts a list of contacts into the database
	 * @param list the list of contacts comma  delimited
	 * @param type the type of contact
	 * @return true if success
	 * @throws SQLException if ContactDAO.open() has not been called yet
	 */
	public boolean putContactList(String list, int type) throws SQLException
	{
		//make sure db is open
		if(db==null || (db.isOpen()==false))
		{
			throw new SQLException("DATABASE ERROR: ContactDAO.open() MUST be called before attempting to access the database");
		}

		return this.updateRow(list, type);
	}

	/**
	 * Puts a list of contacts into the database. Contacts can be of type phone number or type email address
	 * @param list the list of contacts
	 * @return true if success
	 * @throws SQLException if ContactDAO.open() has not been called yet
	 */
	public boolean putContactList(List<Contact> list) throws SQLException
	{
		//make sure db is open
		if(db==null || (db.isOpen()==false))
		{
			throw new SQLException("DATABASE ERROR: ContactDAO.open() MUST be called before attempting to access the database");
		}

		//convert list of contacts into two strings, one for phone# and 1 for email

		//save email addresses
		String ems = "";

		//save phone numbers
		String pn = "";

		for(int i=0;i<list.size();i++)
		{
			if(list.get(i).getType()==Contact.EMAIL_ADDRESS)
			{
				ems+=list.get(i)+",";
			}
			else
			{
				pn+=list.get(i)+",";
			}
		}

		boolean happened = this.updateRow(ems, EMAIL_TYPE);
		boolean happened2 = this.updateRow(pn, PHONE_TYPE);

		if(!happened|| !happened2)
		{
			return false;
		}
		return true;
	}

	/**
	 * Gets a list of contacts from the database
	 * @param type the type of contact wanted
	 * @return the list of contacts comma delimited, or an empty string
	 * @throws SQLException if ContactDAO.open() has not been called yet
	 */
	public String getContactList(int type) throws SQLException
	{
		//make sure db is open
		if(db==null || (db.isOpen()==false))
		{
			throw new SQLException("DATABASE ERROR: ContactDAO.open() MUST be called before attempting to access the database");
		}
		Cursor c = db.query(ContactContentProvider.TABLE_NAME, asterik, ContactContentProvider.COLUMN_TYPE +" = "+ type, null, null, null, null);

		if(c==null || c.getCount()==0)
		{
			//not in database so return blank string
			return "";
		}
		else
		{
			c.moveToFirst();
			ContactsModel model = ContactsModel.cursorToContacts(c);
			c.close();
			
			return model.getInfo();
		}
	}

	/**
	 * Gets the full list of contacts from the database with either
	 * phone number or email as their type. Does not guarantee uniqueness of contacts
	 * @return the list of contacts 
	 * @throws SQLException if ContactDAO.open() has not been called yet
	 */
	public List<Contact> getContactListArray() throws SQLException
	{
		String em = this.getContactList(ContactDAO.EMAIL_TYPE);
		String pn = this.getContactList(ContactDAO.PHONE_TYPE);

		ArrayList<Contact> contacts = new ArrayList<Contact>();
		//load email addresses from database and add to list if unique

		StringTokenizer e = new StringTokenizer(em, ",");
		while(e.hasMoreTokens())
		{
			contacts.add(new Contact(e.nextToken(), Contact.EMAIL_ADDRESS));
		}


		//load phone numbers	
		StringTokenizer p = new StringTokenizer(pn, ",");
		while(p.hasMoreTokens())
		{
			contacts.add(new Contact(p.nextToken(), Contact.PHONE_NUMBER));
		}

		return contacts;
	}

	/**
	 * Updates a row in the database
	 * @param value the list of contacts
	 * @param type the type of contact
	 * @return true if successful, false otherwise
	 * @throws SQLException if multiple rows with the same type are detected, error is thrown
	 */
	private boolean updateRow(String value, int type) throws SQLException
	{
		ContentValues values = new ContentValues();

		values.put(ContactContentProvider.COLUMN_INFO, value);

		int rows = db.update(ContactContentProvider.TABLE_NAME, values, ContactContentProvider.COLUMN_TYPE+" = "+type, null);

		if(rows==0)
		{
			//create a new row instead of update
			ContactsModel c = this.createContact(value, type);	
			if(c==null)
			{
				//error occured
				return false;
			}
		}
		if(rows>1)
		{
			throw new SQLException("DATABASE ERROR: Violation of schema occured. There are multiple rows with the same type. THIS BREAKS EVERYTHING! Row type:"+type);
		}

		//operation successful
		return true;
	}
}
