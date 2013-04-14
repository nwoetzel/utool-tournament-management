package utool.plugin.activity;

import java.util.ArrayList;
import java.util.UUID;

import utool.plugin.Player;

import android.app.Activity;
import android.os.Bundle;

/**
 * Plugin activity helper for the entry point activity
 * @author Cory
 *
 */
public class PluginMainActivityHelper extends PluginServiceActivityHelper {
	
	/**
	 * The tournament's name
	 */
	@Deprecated
	String tournamentName = null;
	
	/**
	 * Get the tournament's name
	 * @return Tournament's name
	 */
	@Deprecated
	public String getTournamentName(){
		return tournamentName;
	}
	
	/**
	 * Set the tournament's name
	 * @param tournamentName Tournament's name
	 */
	@Deprecated
	public void setTournamentName(String tournamentName){
		this.tournamentName = tournamentName;
	}
	
	/**
	 * The player list received from the core
	 */
	@Deprecated
	ArrayList<Player> playerList;
	
	/**
	 * Get the player list
	 * @return Player list
	 */
	@Deprecated
	public ArrayList<Player> getPlayerList(){
		return playerList;
	}
	
	/**
	 * Set the player list
	 * @param playerList Player list
	 */
	@Deprecated
	public void setPlayerList(ArrayList<Player> playerList){
		this.playerList = playerList;
	}
	
	/**
	 * True if the plugin was started in host mode
	 */
	int permissionLevel = Player.PARTICIPANT;
	
	/**
	 * Get if this plugin was started as a host plugin
	 * @return True if a host
	 */
	public int getPermissionLevel()
	{
		return permissionLevel;
	}
	
	/**
	 * False if this plugin is being resumed
	 */
	private boolean isNewInstance = true;
	
	/**
	 * Get whether this plugin instance is being newly started or resumed.
	 * @return True if this is a new instance.
	 */
	public boolean isNewInstance(){
		return isNewInstance;
	}
	
	/**
	 * The player's unique id
	 */
	UUID pid = null;
	
	/**
	 * Get the player's UUID
	 * @return Player's id
	 */
	public UUID getPid(){
		return pid;
	}

	/**
	 * Constructor for PluginMainActivityHelper
	 * @param activity The main activity of the plugin
	 * @param iActivity The same activity as an IPluginServiceActivity
	 */
	public PluginMainActivityHelper(Activity activity, IPluginServiceActivity iActivity) {
		super(activity, iActivity);
		
		//Get extras passed from the core
		Bundle extras = activity.getIntent().getExtras();
		tournamentName = extras.getString("tournamentName");
		playerList = extras.getParcelableArrayList("playerList");
		permissionLevel = extras.getInt("permissionLevel");
		pid = UUID.fromString(extras.getString("pid"));
		isNewInstance = extras.getBoolean("isNewInstance", false);
	}

}
