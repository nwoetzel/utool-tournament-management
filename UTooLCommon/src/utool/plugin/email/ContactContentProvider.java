package utool.plugin.email;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

/**
 * Creates the database for saving contacts
 * The table should look like this:
 * |id|info|type|
 * where id holds the primary key,
 * Info holds the string of contacts,
 * and Type holds a 1 for email addresses, and a 2 for phone numbers
 * @author waltzm
 * @version 4/27/2013
 */
public class ContactContentProvider extends SQLiteOpenHelper{

	/**
	 * Name of the database
	 */
	private static final String DATABASE_NAME = "UTooLContacts.dm";

	/**
	 * Name of the contacts table
	 */
	public static final String TABLE_NAME = "UTooLContactsTable";

	/**
	 * column id
	 */
	public static final String COLUMN_ID = "id";

	/**
	 * column info
	 */
	public static final String COLUMN_INFO = "info";
	
	/**
	 * column type
	 */
	public static final String COLUMN_TYPE = "type";
	
	/**
	 * Holds the database version
	 */
	private static final int DATABASE_VERSION = 3;

	/**
	 * Holds the string to create the database
	 */
	private static final String DATABASE_CREATE = "create table "+ 
			TABLE_NAME+ "("+COLUMN_ID+" integer primary key autoincrement, "
			+COLUMN_INFO+" text not null, "+COLUMN_TYPE+" integer not null);";


	/**
	 * Creates the database
	 * @param context app context
	 */
	public ContactContentProvider(Context context) 
	{
		super(new DatabaseContext(context), DATABASE_NAME, null, DATABASE_VERSION);
	}
	
	@Override
	public void onCreate(SQLiteDatabase db) {
		db.execSQL(DATABASE_CREATE);

	}

	@Override
	public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion)
	{
		db.execSQL("drop table if exists "+TABLE_NAME);
		this.onCreate(db);

	}





}
