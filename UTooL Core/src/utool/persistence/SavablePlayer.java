package utool.persistence;

import java.util.NoSuchElementException;
import java.util.StringTokenizer;
import java.util.UUID;

import utool.plugin.Player;

/**
 * Savable version of player
 * @author Justin Kreier
 * @version 1/6/2013
 *
 */
public class SavablePlayer extends Player implements Savable{
	
	/**
	 * Default constructor for a savable player
	 * @param id The ID
	 * @param name The name
	 * @param isGhost True if observer
	 * @param seedValue The player's seed value
	 * @param filepath The filepath to the player's portrait
	 */
	public SavablePlayer(UUID id, String name, boolean isGhost, int seedValue, String filepath) {
		super(id, name, isGhost, seedValue, filepath);
	}
	
	/**
	 * Blank player
	 */
	public SavablePlayer(){
		super("");
	}

	/**
	 * Converts a player into a savable player
	 * @param player The player to convert
	 */
	public SavablePlayer(Player player) {
		this(player.getUUID(), player.getName(), player.isGhost(), player.getSeedValue(), player.getPortraitFilepath());
		
		this.setPermissionsLevel(player.getPermissionsLevel());
	}

	@Override
	public String save() {
		String ret = "";
		ret += uuid.toString()+"\n";
		ret += name+"\n";
		ret += isGhost+"\n";
		ret += seedValue+"\n";
		ret += portraitFilepath;
		return ret;
	}

	@Override
	public Savable load(String value) {
		StringTokenizer token = new StringTokenizer(value, "\n");
		UUID id = UUID.fromString(token.nextToken());
		String n = token.nextToken();
		String ghost = token.nextToken();
		boolean bGhost = false;
		if (ghost.equalsIgnoreCase("true")){
			bGhost = true;
		}
		int seed = Integer.parseInt(token.nextToken());
		String filepath = "";
		try{
			filepath = token.nextToken();
		} catch (NoSuchElementException e){}
		return new SavablePlayer(id, n, bGhost, seed, filepath);
	}
	
	/**
	 * Creates a new player object from this savable object
	 * @return A non-savable player
	 */
	public Player toPlayer(){
		return new Player(uuid, name, isGhost, seedValue, portraitFilepath, this.getPortrait());
	}

}
