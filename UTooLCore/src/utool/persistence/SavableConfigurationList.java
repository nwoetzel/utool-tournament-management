package utool.persistence;

import java.util.ArrayList;
import java.util.StringTokenizer;
import java.util.UUID;

import android.content.Context;

import utool.core.Core;
import utool.core.ProfileActivity;

/**
 * A savable list of tournament configurations
 * @author Justin Kreier
 * @version 1/12/2013
 */
public class SavableConfigurationList extends ArrayList<TournamentConfiguration> implements Savable{

	/**
	 * Generated serialVersionUID
	 */
	private static final long serialVersionUID = -961314938385829265L;

	@Override
	public String save() {
		String ret = "";
		//save each list separated by a backslash
		for (int i = 0; i < this.size(); i++){
			if (i < this.size()-1){
				ret += this.get(i).save()+"\\";
			} else {
				ret += this.get(i).save();
			}
		}
		return ret;
	}

	@Override
	public Savable load(String value) {
		SavableConfigurationList list = new SavableConfigurationList();
		StringTokenizer tokens = new StringTokenizer(value, "\\");
		while (tokens.hasMoreTokens()){
			TournamentConfiguration c = new TournamentConfiguration();
			list.add((TournamentConfiguration)c.load(tokens.nextToken()));
		}
		return list;
	}
	
	/**
	 * Creates a list of tournaments from this list of configurations
	 * @param c The application context
	 * @param index The index of the tournament to create
	 * @return The list of tournament cores
	 * @throws InstantiationException If the profile list can't be instantiated
	 * @throws IllegalAccessException If the profile list can't be accessed
	 */
	public Core createTournamentsFromList(Context c, int index) throws InstantiationException, IllegalAccessException{
		//get device Id
		SavableProfileList list = (SavableProfileList) StorageManager.loadSavable(ProfileActivity.PROFILE_LIST_KEY, SavableProfileList.class, c, null);
		UUID deviceId = list.getSelectedProfile().getId();
		
		// return the list of tournaments
		return this.get(index).createTournament(deviceId);
	}

}
