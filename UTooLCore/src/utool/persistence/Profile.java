package utool.persistence;


import java.util.StringTokenizer;
import java.util.UUID;

import utool.plugin.Player;

/**
 * Structure for holding all information about a player profile
 * @author kreierj
 * @version 1/6/2013
 */
public class Profile extends Player implements Comparable<Profile>, Savable{

	/**
	 * Constructor for profile
	 * @param name Profile name
	 * @param picFile Filepath to pic
	 * @param id Profile ID
	 */
	public Profile(String name, String picFile, UUID id){
		super(id, name, false, -1, picFile);
	}

	/**
	 * Default profile constructor
	 */
	public Profile(){
		super("");
	}

	@Override
	public int compareTo(Profile p) {
		return name.compareTo(p.name);
	}

	@Override
	public String save() {
		String ret = "";
		ret += "=="+name+"==\n";
		ret += uuid.toString()+"\n";
		ret += portraitFilepath;
		return ret;
	}

	@Override
	public Savable load(String value) {
		StringTokenizer t = new StringTokenizer(value,"\n");
		String name = t.nextToken();
		name = name.substring(2, name.length()-2);
		UUID id = UUID.fromString(t.nextToken());
		String file = t.nextToken();

		Profile p = new Profile(name, file, id);

		return p;
	}

	/**
	 * Gets the profile name
	 * @return The name of the profile
	 */
	public String getName(){
		return name;
	}

	/**
	 * Gets the filepath to the profile's picture
	 * @return The filepath to the profile's picture
	 */
	public String getFile(){
		return portraitFilepath;
	}

	/**
	 * Gets the profile's ID
	 * @return the profile's id
	 */
	public UUID getId(){
		return uuid;
	}

	/**
	 * Sets the profile name
	 * @param newName The new name
	 */
	public void setName(String newName){
		name = newName;
	}

	/**
	 * Sets the profile file
	 * @param newFile The new file
	 */
	public void setFile(String newFile){
		super.setPortraitFilepath(newFile);
	}

	@Override
	public boolean equals(Object o){
		if (o instanceof Profile){
			Profile p = (Profile)o;
			return uuid.equals(p.getId());
		} else {
			return super.equals(o);
		}

	}
}
