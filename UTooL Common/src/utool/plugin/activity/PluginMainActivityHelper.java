package utool.plugin.activity;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import utool.plugin.Player;

import android.app.Activity;
import android.os.Bundle;
import android.os.RemoteException;

/**
 * Plugin activity helper for the entry point activity
 * @author Cory
 *
 */
public class PluginMainActivityHelper extends PluginServiceActivityHelper {
	
// 	/**
//	 * The tournament's name from the initial plugin instantiation
//	 */
//	private String tournamentName = null;
//	
//	/**
//	 * The player list received from the core
//	 */
//	@Deprecated
//	ArrayList<Player> playerList;

	/**
	 * Get the tournament's name
	 * @return Tournament's name
	 */
	public String getTournamentName(){
//		if (mICore == null){
//			//return initial version from core
//			return tournamentName;
//		}
		try {
			if (mICore == null){
				throw new RuntimeException(SERVICE_UNAVAILABLE_EXCEPTION_MESSAGE);
			} else {
				return mICore.getTournamentName();
			}
		} catch (RemoteException e) {
		}
		return "";
	}
	
	/**
	 * Get the player list
	 * @return Player list
	 */
	public List<Player> getPlayerList(){
//		if (mICore == null){
//			return playerList;
//		}
		try {
			if (mICore == null){
				throw new RuntimeException(SERVICE_UNAVAILABLE_EXCEPTION_MESSAGE);
			}
			return mICore.getPlayerList();
		} catch (RemoteException e) {
		}
		return new ArrayList<Player>();
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
//		tournamentName = extras.getString("tournamentName");
//		playerList = extras.getParcelableArrayList("playerList");
		permissionLevel = extras.getInt("permissionLevel");
		pid = UUID.fromString(extras.getString("pid"));
		isNewInstance = extras.getBoolean("isNewInstance", false);
	}

}
