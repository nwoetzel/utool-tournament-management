package utool.core;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.UUID;

import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.Intent;
import android.os.RemoteException;
import android.util.Log;

import utool.core.UTooLCoreService.UTooLServiceImplementation;
import utool.networking.BroadcastManager;
import utool.networking.packet.HostInformation;
import utool.networking.packet.PlayerMessage;
import utool.networking.packet.PluginStartMessage;
import utool.plugin.Player;
import utool.plugin.dummy.DummyMainActivity;

/**
 * Handles data for individual tournaments before plugin instantiation
 * @author Cory
 *
 */
public class Core extends AbstractTournament {
	
	/**
	 * Profile id being used on the device
	 */
	private UUID deviceID;
	
	/**
	 * Player's profile
	 */
	private Player profile;
	
	/**
	 * The tournament's id on the device
	 */
	private static long tournamentIdCounter = 0;
	
	/**
	 * HashMap storing core instances for all tournaments
	 */
	private static HashMap<Long, Core> coreInstances = new HashMap<Long, Core>();
	
	/**
	 * The list of players registered for the tournament
	 */
	private List<Player> players = new ArrayList<Player>();
	
	/**
	 * The tournament's id
	 */
	private long tournamentId;
	
	/**
	 * The number of times this tournament instance has been attempted to be terminated.
	 */
	private int terminateCount = 0;
	
	/**
	 * The intent used to start the plugin
	 */
	private Intent pluginIntent = null;
	
	/**
	 * Tracks whether the tournament has been started.
	 */
	private boolean tournamentStarted = false;
	
	/**
	 * Tracks if this Core is for a host instance
	 */
	private int permissionLevel = Player.PARTICIPANT;
	
	/**
	 * Tracks if the player list has been updated since the last time getPlayerList() was called.
	 */
	private boolean playerListUpdated = false;
	
	/**
	 * The message sent by the server to start the plugin.
	 */
	private PluginStartMessage pluginStartMessage = null;
	
	/**
	 * The configuration index. Used only if this Core was created for a local tournament.
	 */
	private int configIndex = -1;
	
	/**
	 * Set to true if the Core has been told to shut down by the network code
	 */
	private boolean shutdownCore;
	
	/**
	 * Create a Core for a new tournament
	 * @return Instance of a new Core
	 */
	public static Core getNewCoreInstance(){
		Core instance = new Core(tournamentIdCounter);
		coreInstances.put(tournamentIdCounter, instance);
		AbstractTournament.setTournament(instance.tournamentUUID, instance);
		tournamentIdCounter = tournamentIdCounter + 1;
		return instance;
	}
	
	/**
	 * Create a Core for a discovered tournament
	 * @param hostInformationPacket The HostInformation object used to connect to the tournament.
	 * @return Instance of a new Core
	 */
	public static Core getNewCoreInstance(HostInformation hostInformationPacket){
		Core instance = new Core(tournamentIdCounter, hostInformationPacket.tournamentUUID);
		instance.serverAddress = hostInformationPacket.serverAddress;
		instance.serverPort = hostInformationPacket.serverPort;
		instance.setTournamentLocation(TournamentLocationEnum.RemoteConnected);
		instance.setTournamentName(hostInformationPacket.getTournamentName());
		coreInstances.put(tournamentIdCounter, instance);
		if (instance.tournamentUUID != null){
			AbstractTournament.setTournament(instance.tournamentUUID, instance);
		}
		tournamentIdCounter = tournamentIdCounter + 1;
		return instance;
	}
	
	/**
	 * Set the tournament UUID and add to the tournament list. Only for use with Direct Connect.
	 * @param uuid The UUID to associate this tournament with
	 * @throws Exception Thrown when the tournament already has a UUID set.
	 */
	public void setUUID(UUID uuid) throws Exception{
		if (tournamentUUID != null){
			throw new Exception("Tournament UUID already set");
		} else {
			tournamentUUID = uuid;
			AbstractTournament.setTournament(tournamentUUID, this);
		}
	}
	
	/**
	 * Get the instance of Core for a specific tournament id
	 * @param tournamentId The unique tournament id
	 * @return The instance of Core for that tournament id
	 */
	public static Core getCoreInstance(long tournamentId){
		Core instance = coreInstances.get(tournamentId);
		if (instance == null){
			throw new NullPointerException("Instance not found, try using getNewCoreInstance()");
		}
		return instance;
	}
	
	/**
	 * Get the instance of Core for a specific tournament UUID.
	 * This can also be used to create a new Core with the given UUID.
	 * @param tournamentUUID The unique tournament UUID
	 * @return The instance of Core for that tournament UUID
	 */
	public static Core getCoreInstance(UUID tournamentUUID){
		AbstractTournament abstractInstance = AbstractTournament.getTournament(tournamentUUID);
		if (abstractInstance != null && abstractInstance instanceof Core){
			return (Core)abstractInstance;
		} else {
			Core instance = new Core(tournamentIdCounter, tournamentUUID);
			coreInstances.put(tournamentIdCounter, instance);
			AbstractTournament.setTournament(instance.tournamentUUID, instance);
			tournamentIdCounter = tournamentIdCounter + 1;
			return instance;
		}
	}
	
	/**
	 * Get all currently running tournaments
	 * @return The collection of created tournament cores
	 */
	public static Collection<Core> getAllCoreInstances(){
		return coreInstances.values();
	}
	
	/**
	 * Get the tournament's id
	 * @return Tournament's id
	 */
	public long getTournamentId(){
		return tournamentId;
	}
	
	/**
	 * Private constructor. Set up the core for a new tournament.
	 * @param tournamentId A unique tournament id
	 */
	private Core(long tournamentId){
		players = new ArrayList<Player>();
		this.tournamentId = tournamentId;
		this.tournamentUUID = UUID.randomUUID();
	};
	
	/**
	 * Private constructor. Set up the core for a remote tournament.
	 * @param tournamentId A unique tournament id
	 * @param tournamentUUID The received tournament UUID.
	 */
	private Core(long tournamentId, UUID tournamentUUID){
		players = new ArrayList<Player>();
		this.tournamentId = tournamentId;
		this.tournamentUUID = tournamentUUID;
	}
	
	/**
	 * Set the plugin intent for this tournament
	 * @param pluginIntent The intent returned by the plugin discovery code
	 */
	public void setPluginIntent(Intent pluginIntent){
		this.pluginIntent = pluginIntent;
	}
	
	/**
	 * Set the player list for this tournament, overwriting the existing player list.
	 * @param players List of players
	 */
	public void setPlayerList(List<Player> players){
		this.players = players;
		playerListUpdated = true;
	}
	
	/**
	 * Add a player to the tournament
	 * @param player The player to add.
	 */
	public void addPlayer(Player player){
		if (!players.contains(player)){
			players.add(player);
			playerListUpdated = true;
		} else {
			Log.e("utool.core.Core", "Attempted to add player: "+player+" but it already was added");
		}
	}
	
	/**
	 * Returns the list of players for this tournament
	 * @return The list of players
	 */
	public List<Player> getPlayerList(){
		playerListUpdated = false;
		return players;
	}
	
	/**
	 * Checks if the player list was updated since the last time getPlayerList() was called
	 * @return True if it has been updated
	 */
	public boolean getPlayerListUpdated(){
		return playerListUpdated;
	}
	
	/**
	 * Set the permission level of the plugin
	 * @param permissionLevel The permission level of the plugin
	 */
	public void setPermissionLevel(int permissionLevel) {
		this.permissionLevel = permissionLevel;
	}
	
	/**
	 * Get the intent for the plugin to start, with the player list preset.
	 * This does not mark the tournament as started. Use the startPlugin method, or set the isNewInstance extra to !hasStarted()
	 * @return Preconfigured intent for the desired plugin type
	 */
	public Intent getIntent(){
		if (pluginIntent != null){
			pluginIntent.putExtra("tournamentId", tournamentId);
			pluginIntent.putExtra("tournamentName", this.toString());
			ArrayList<Player> playersArray = new ArrayList<Player>(players);
			pluginIntent.putExtra("playerList", playersArray);
			pluginIntent.putExtra("permissionLevel", permissionLevel);
			pluginIntent.putExtra("pid", deviceID.toString());
			return pluginIntent;
		} else {
			Log.e("utool.core.Core", "Intent was null, no plugin selected");
			return null;
		}
	}
	
	/**
	 * Start the plugin. It is recommended to use this method over startActivity.
	 * @param context The calling context
	 * @param tournamentIntent The tournament intent returned from getIntent
	 */
	public void startPlugin(Context context, Intent tournamentIntent){
		pluginIntent.putExtra("isNewInstance", !tournamentStarted);
		tournamentStarted = true;
		try
		{
			context.startActivity(tournamentIntent);
		} 
		catch (ActivityNotFoundException e)
		{
			tournamentIntent.setClass(context, DummyMainActivity.class);
			context.startActivity(tournamentIntent);
		}
	}
	
	/**
	 * Get the plugin start message
	 * @return The plugin start message if received. Otherwise null.
	 * @throws IOException Thrown when the Core is shutdown
	 */
	public PluginStartMessage getPluginStartMessage() throws IOException{
		if (shutdownCore){
			throw new IOException("Core is shutdown");
		}
		return pluginStartMessage;
	}
	
	/**
	 * Set the plugin start message
	 * @param message The plugin start message
	 */
	public void setPluginStartMessage(PluginStartMessage message){
		this.pluginStartMessage = message;
	}
	
	@Override
	public void setTournamentName(String newName){
		super.setTournamentName(newName);
		if (this.getTournamentLocation() == TournamentLocationEnum.Local || this.getTournamentLocation() == TournamentLocationEnum.LocalFinished){
			BroadcastManager.updateTournamentName(this);
			HostInformation hostInformationMessage = new HostInformation(tournamentName, 0, tournamentUUID);
			try {
				UTooLServiceImplementation service = UTooLCoreService.getServiceForTournamentInstance(tournamentId);
				if (service != null){
					service.send(hostInformationMessage.getXml());
				}
			} catch (RemoteException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}
	
	/**
	 * Removes a player from the list by their ID
	 * @param id The id of the player to remove
	 * @return true if exists
	 */
	public boolean removeById(UUID id){
		for (int i = 0; i < players.size(); i++){
			if (players.get(i).getUUID().equals(id)){
				players.remove(i);
				playerListUpdated = true;
				return true;
			}
		}
		return false;
	}
	
	/**
	 * Get whether the tournament has been started
	 * @return True if the tournament intent has been retrieved 
	 */
	public boolean hasStarted(){
		return tournamentStarted;
	}
	
	/**
	 * Get the number of times this instance has been attempted to be terminated
	 * @return Termination count
	 */
	public int getTerminateCount(){
		return terminateCount;
	}
	
	/**
	 * Increment the termination count by 1
	 */
	public void incrementTerminateCount(){
		terminateCount += 1;
	}
	
	/**
	 * Remove this tournament from the core.
	 */
	public void removeTournament(){
		Core t = coreInstances.remove(tournamentId);
		if (t != null){
			removeTournament(t.tournamentUUID);
		}
	}
	
	/**
	 * Sets the device id 
	 * @param id The id to set
	 */
	public void setDeviceId(UUID id){
		deviceID = id;
	}
	
	/**
	 * Get the user profile used by this tournament
	 * @return User profile
	 */
	public Player getProfile(){
		return profile;
	}
	
	/**
	 * Sets the profile name
	 * @param newPlayer The name to set
	 */
	public void setProfile(Player newPlayer){
		profile = newPlayer;
	}
	
	/**
	 * Get the saved configuration index of this core. Only valid if this core for a local tournament.
	 * @return The configuration index.
	 */
	public int getConfigIndex(){
		return configIndex;
	}
	
	/**
	 * Set the saved configuration index for this core.
	 * @param configIndex The value of the configuration index.
	 */
	public void setConfigIndex(int configIndex){
		this.configIndex = configIndex;
	}
	
	/**
	 * Called when shutting down due to a network connection error
	 */
	public void shutdownCore(){
		this.shutdownCore = true;
	}
	
	@Override
	public String toString(){
		return tournamentName;
	}
	
	/**
	 * Send the entire player list to client devices
	 */
	public void sendPlayerList(){
		if (this.getTournamentLocation() == TournamentLocationEnum.Local || this.getTournamentLocation() == TournamentLocationEnum.LocalFinished){
			PlayerMessage playerMessage = new PlayerMessage(getPlayerList());
			String playerMessageXml = playerMessage.getXml();
			try {
				UTooLServiceImplementation service = UTooLCoreService.getServiceForTournamentInstance(tournamentId);
				if (service != null){
					service.send(playerMessageXml);
				}
			} catch (RemoteException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}

}
