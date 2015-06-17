package utool.plugin.activity;

import java.util.List;
import java.util.UUID;

import utool.plugin.Player;
import android.os.Bundle;
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
	 * Get the tournament's name
	 * @return Tournament's name
	 */
	public String getTournamentName(){
		return pluginHelper.getTournamentName();
	}
	
	/**
	 * Get the player list
	 * @return Player list
	 */
	public List<Player> getPlayerList(){
		return pluginHelper.getPlayerList();
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
		return pluginHelper.getPermissionLevel();
	}
	
	/**
	 * Get whether this plugin instance is being newly started or resumed.
	 * @return True if this is a new instance.
	 */
	public boolean isNewInstance(){
		return pluginHelper.isNewInstance();
	}
	
	/**
	 * Get the player's UUID
	 * @return Player's id
	 */
	public UUID getPid(){
		return pluginHelper.getPid();
	}
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		pluginHelper = new PluginMainActivityHelper(this, this);
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
