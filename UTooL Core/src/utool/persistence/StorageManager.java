package utool.persistence;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.preference.PreferenceManager;

/**
 * This class is responsible for saving and loading objects using shared preferences
 * @author kreierj
 *
 */
public class StorageManager {
	
	/**
	 * Saves a savable object to preferences
	 * @param key The key to store the savable object to
	 * @param s The savable object
	 * @param c The application context
	 */
	public static void saveSavable(String key, Savable s, Context c){
		SharedPreferences p = PreferenceManager.getDefaultSharedPreferences(c);
		Editor e = p.edit();
		e.putString(key, s.save());
		e.commit();
	}
	
	/**
	 * Loads a savable object from preferences, or returns null if there is no value
	 * @param key The key to retrieve the savable object from
	 * @param expectedClass The expected class to return
	 * @param c The application context
	 * @param defValue The default value to return if it isn't there
	 * @return A loaded savable object
	 * @throws InstantiationException Thrown if the expected class cannot be instantiated
	 * @throws IllegalAccessException Thrown if the expected class has restricted access
	 */
	public static Savable loadSavable(String key, Class<? extends Savable> expectedClass, Context c, Savable defValue) throws InstantiationException, IllegalAccessException{
		Savable s = expectedClass.newInstance();
		SharedPreferences p = PreferenceManager.getDefaultSharedPreferences(c);
		String value = p.getString(key, null);
		if (value == null){
			return defValue;
		}
		
		return s.load(value);
	}
	
	/**
	 * Gets an int from saved preferences
	 * @param key The key to retrieve from
	 * @param c The application context
	 * @param defValue The default value to return if there is no value
	 * @return The saved integer
	 */
	public static int loadInt(String key, Context c, int defValue){
		SharedPreferences p = PreferenceManager.getDefaultSharedPreferences(c);
		int value = p.getInt(key, defValue);
		return value;
	}
	
	/**
	 * Saves an integer to preferences
	 * @param key The key to store to
	 * @param i The integer to save
	 * @param c The application context
	 */
	public static void saveInt(String key, int i, Context c){
		SharedPreferences p = PreferenceManager.getDefaultSharedPreferences(c);
		Editor e = p.edit();
		e.putInt(key, i);
		e.commit();
	}
	
	/**
	 * Saves a boolean to saved preferences
	 * @param key The key to retrieve from
	 * @param b The boolean to save
	 * @param c The application context
	 */
	public static void saveBoolean(String key, boolean b, Context c){
		SharedPreferences p = PreferenceManager.getDefaultSharedPreferences(c);
		Editor e = p.edit();
		e.putBoolean(key, b);
		e.commit();
	}
	
	/**
	 * Gets a boolean from saved preferences
	 * @param key The key to retrieve from
	 * @param c The application context
	 * @param defValue The default value to return if there is no value
	 * @return The saved boolean
	 */
	public static boolean loadBoolean(String key, Context c, boolean defValue){
		SharedPreferences p = PreferenceManager.getDefaultSharedPreferences(c);
		boolean value = p.getBoolean(key, defValue);
		return value;
	}


}
