package utool.persistence;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;


/**
 * An Array List of Profiles that can be saved in Preferences using the PreferencesManager.
 * @author kreierj
 * @version 1/6/2013
 */
public class SavableProfileList extends ArrayList<Profile> implements Savable{

	/**
	 * Generated serialVersionUID
	 */
	private static final long serialVersionUID = 3698574264006816802L;
	
	/**
	 * The currently selected profile
	 */
	private int selectedProfile = 0;

	/**
	 * Default constructor
	 */
	public SavableProfileList(){
		super();
	}
	
	/**
	 * Converts an ArrayList into a SavableProfileList
	 * @param fromArray the original list
	 */
	public SavableProfileList(ArrayList<Profile> fromArray){
		super(fromArray);
	}
	
	@Override
	public String save() {
		String ret = "";
		ret += "<"+selectedProfile+">,";
		for (int i = 0; i < this.size(); i++){
			ret += "[";
			Profile temp = this.get(i);
			ret += temp.save();
			ret += "],";
		}
		
		return ret;
	}

	@Override
	public Savable load(String value) {
		SavableProfileList newList = new SavableProfileList();
		
		ArrayList<String> loadableObjects = new ArrayList<String>();
		String temp = "";
		String selected = "";
		boolean copy = false;
		boolean copy2 = false;
		for (int i = 0; i < value.length(); i++){
			if (value.charAt(i) == '<'){
				copy2 = true;
			} else if (value.charAt(i)=='>'){
				copy2 = false;
			} else if (value.charAt(i) == '['){
				copy = true;
				temp = "";
			} else if (value.charAt(i) == ']'){
				copy = false;
				loadableObjects.add(temp);
			} else if (copy) {
				temp += value.charAt(i);
			} else if (copy2) {
				selected += value.charAt(i);
			}
		}
		
		Profile p = new Profile();
		
		for (int i = 0; i < loadableObjects.size(); i++){
			newList.add((Profile) p.load(loadableObjects.get(i)));
		}
		Integer s = 0;
		try{
			s = Integer.parseInt(selected);
		} catch (NumberFormatException e){
			//use default value
		}
		
		newList.setSelectedProfile(s);
		
		return newList;
	}

	/**
	 * Sets the profile to select
	 * @param s The index to select
	 */
	public void setSelectedProfile(int s){
		if (s > this.size()-1){
			selectedProfile = this.size()-1;
		} else if (s < 0){
			selectedProfile = 0;
		} else {
			selectedProfile = s;
		}
	}
	
	/**
	 * Returns the index of the selected profile
	 * @return index of the selected profile
	 */
	public int getSelectedProfileIndex(){
		return selectedProfile;
	}
	
	/**
	 * Returns the currently selected profile
	 * @return the currently selected profile
	 */
	public Profile getSelectedProfile(){
		if (selectedProfile < size()){
			return get(selectedProfile);
		} else {
			return null;
		}
	}
	
	/**
	 * Sorts this list by name
	 */
	public void sort(){
		
		Profile p = getSelectedProfile();
		
		Collections.sort(this, new Comparator<Profile>(){

			@Override
			public int compare(Profile profile1, Profile profile2) {
				return profile1.getName().compareToIgnoreCase(profile2.getName());
			}
			
		});
		
		setSelectedProfile(indexOf(p));
	}
}
