package utool.plugin.email;

import java.io.File;

import android.content.Context;
import android.content.ContextWrapper;
import android.database.DatabaseErrorHandler;
import android.database.sqlite.SQLiteDatabase;
import android.os.Environment;
/**
 * Based off of:
 * http://stackoverflow.com/questions/5332328/sqliteopenhelper-problem-with-fully-qualified-db-path-name/9168969#9168969
 * 
 * This class takes in a context, and overwrites what the path to the database is, making
 * it so the data is saved externally on the sdcard. Nothing else is altered
 * of the original context.
 * 
 * Path to Database: sdcard/utool/datadases/[name of database]
 * @author waltzm
 * @version 4/28/2013
 */
public class DatabaseContext extends ContextWrapper {

	/**
	 * Creates a database context wrapper
	 * @param base the base context
	 */
	public DatabaseContext(Context base) {
		super(base);
	}

	@Override
	public File getDatabasePath(String name) 
	{
		
		File sdcard = Environment.getExternalStorageDirectory();    
		//make sure a folder is there
		File folder = new File(Environment.getExternalStorageDirectory()+"/utool");
		if (!folder.exists()){
			folder.mkdir();
		}
		folder = new File(Environment.getExternalStorageDirectory()+"/utool/databases");
		if (!folder.exists()){
			folder.mkdir();
		}
		
		String dbfile = sdcard.getAbsolutePath() + "/utool/databases/" + name;
		if (!dbfile.endsWith(".db"))
		{
			dbfile += ".db" ;
		}

		File result = new File(dbfile);

		if (!result.getParentFile().exists())
		{
			result.getParentFile().mkdirs();
		}

		return result;
	}


	
	@Override
	public SQLiteDatabase openOrCreateDatabase(String name, int mode, SQLiteDatabase.CursorFactory factory, DatabaseErrorHandler e) 
	{
		SQLiteDatabase result = SQLiteDatabase.openOrCreateDatabase(getDatabasePath(name), null);
		// SQLiteDatabase result = super.openOrCreateDatabase(name, mode, factory);
		return result;
	}
	
	@Override
	public SQLiteDatabase openOrCreateDatabase(String name, int mode, SQLiteDatabase.CursorFactory factory) 
	{
		SQLiteDatabase result = SQLiteDatabase.openOrCreateDatabase(getDatabasePath(name), null);
		// SQLiteDatabase result = super.openOrCreateDatabase(name, mode, factory);
		
		return result;
	}
}