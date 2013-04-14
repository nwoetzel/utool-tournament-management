package utool.plugin.activity;

import java.util.ArrayList;
import java.util.UUID;

import utool.plugin.IUTooLCore;
import utool.plugin.Player;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;
import android.os.RemoteException;

/**
 * This class provides a framework for developing the main entry point for plugins.
 * All extras sent by the core are automatically retrieved, and the core service is bound.
 * 
 * Remember, don't try to use mICore before the service is connected. The code in {@link #runOnServiceConnected()} will
 * be executed when the connection is established.
 * 
 * @author Cory
 *
 */
public abstract class AbstractPluginMainActivity extends AbstractPluginCommonActivity implements IPluginServiceActivity {
	
	/**
	 * The PluginMainActivityHelper this activity is using for all special operations
	 */
	protected PluginMainActivityHelper pluginHelper;
	
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
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		pluginHelper = new PluginMainActivityHelper(this, this);
		
		//Get extras passed from the core
		tournamentName = getIntent().getExtras().getString("tournamentName");
		playerList = getIntent().getExtras().getParcelableArrayList("playerList");
		permissionLevel = getIntent().getExtras().getInt("permissionLevel");
		pid = UUID.fromString(getIntent().getExtras().getString("pid"));
		isNewInstance = getIntent().getExtras().getBoolean("isNewInstance", false);

	}
	
	@Override
	protected void onDestroy(){
		super.onDestroy();

		//This activity is being destroyed, so disconnect from the service
		pluginHelper.unbindService();
	}
	
	/**
	 * Code to run right after the service connects. Put any code that immediately depends on the service here,
	 * such as network code.
	 */
	public abstract void runOnServiceConnected();
	
	/**
	 * Code to run if the service unexpectedly disconnects
	 */
	public abstract void runOnServiceDisconnected();
	
	/**
	 * Get the tournament name. The host will append the tournament id, clients will take what the host gives them.
	 * @param includeIdIfHost Include the tournament id if this is a host device
	 * @return The tournament name, maybe with :id appended
	 */
	public String getTournamentName(boolean includeIdIfHost){
			return tournamentName;
	}
	
	/**
	 * This is to be called when there is a message to send to the core
	 * @param s the message in xml format to send
	 * @return true if sent
	 */
	public boolean sendMessage(String s)
	{
		try 
		{
			pluginHelper.mICore.send(s);
			return true;
		} catch (RemoteException e) 
		{
			e.printStackTrace();
			return false;
		}
	}
	
}
