package utool.core.tests;


import utool.core.ProfileActivity;
import utool.persistence.Profile;
import utool.persistence.SavableProfileList;
import utool.persistence.StorageManager;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.preference.PreferenceManager;

/**
 * Public helper methods for automated android tests
 * @author Justin Kreier
 * @version 1/20/2013
 */
public class AndroidTestHelperMethods {

	/**
	 * Adds a profile to the saved preferences
	 * @param p The profile to add
	 * @param c The target application context
	 */
	public static void addProfile(Profile p, Context c){
		SavableProfileList loadedProfiles = null;
		try {
			loadedProfiles = (SavableProfileList)StorageManager.loadSavable(ProfileActivity.PROFILE_LIST_KEY, SavableProfileList.class, c, new SavableProfileList());
		} catch (InstantiationException e) {
			e.printStackTrace();
		} catch (IllegalAccessException e) {
			e.printStackTrace();
		}
		loadedProfiles.add(p);
		loadedProfiles.setSelectedProfile(0);
		StorageManager.saveSavable(ProfileActivity.PROFILE_LIST_KEY, loadedProfiles, c);
	}

	/**
	 * Wipes the application data (Similar to pressing "Clear Data" from apps settings)
	 * @param c The application context
	 */
	public static void clearApplicationData(Context c) {
		SharedPreferences settings = PreferenceManager.getDefaultSharedPreferences(c);
		Editor e = settings.edit();
		e.clear();
		e.commit();
		
	}

}
