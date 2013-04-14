package utool.plugin.activity;

import java.util.ArrayList;
import java.util.UUID;

import utool.plugin.Player;

/**
 * This class stores all the basic information that is needed to run a tournament
 * @author Justin Kreier
 * @version 1/22/2013
 */
public abstract class AbstractTournament {

	/**
	 * Holds whether or not the user of the plugin is the host of the tournament or not.
	 */
	protected int permissionLevel = Player.PARTICIPANT;

	/**
	 * The tournament's id
	 */
	protected long tournamentId;

	/**
	 * List of players in the tournament
	 */
	protected ArrayList<Player> players;

	/**
	 * The tournament's name
	 */
	protected String tournamentName = null;

	/**
	 * The UUID of the current user's profile
	 */
	protected UUID pid = new UUID(0,-1);

	/**
	 * The communication system being used for sending messages back to the core
	 */
	protected CommunicationBridge bridge;


	/**
	 * Required constructor that initializes the abstract tournament
	 * @param tournamentId The tournament id
	 * @param playerList The player list
	 * @param tournamentName The tournament name
	 * @param profileId The profile id of the current device
	 */
	public AbstractTournament(long tournamentId, ArrayList<Player> playerList, String tournamentName, UUID profileId){
		this.tournamentId = tournamentId;
		this.players = playerList;
		this.tournamentName = tournamentName;
		this.pid = profileId;
		this.bridge = new CommunicationBridge();
		TournamentContainer.putInstance(this);
	}


	/**
	 * Get the tournament's id
	 * @return The tournament's id
	 */
	public long getTournamentId(){
		return tournamentId;
	}

	/**
	 * Get the tournament name. The host will append the tournament id, clients will take what the host gives them.
	 * @return The tournament name, maybe with :id appended
	 */
	public String getTournamentName(){
		return tournamentName;
	}


	/**
	 * @return ArrayList of Players in the tournament
	 */
	public ArrayList<Player> getPlayers(){
		return players;
	}

	/**
	 * Set the tournament's name
	 * @param name The tournament's name
	 */
	public void setTournamentName(String name){
		tournamentName = name;
	}

	/**
	 * Set tournament players to the given list
	 * @param players The players to add
	 */
	public void setPlayers(ArrayList<Player> players){
		this.players = players;
	}

	/**
	 * Returns the permission level
	 * @return The permission level
	 */
	public int getPermissionLevel(){
		return permissionLevel;
	}

	/**
	 * Sets the permission level
	 * @param newPermissionLevel The new permission level
	 */
	public void setPermissionLevel(int newPermissionLevel){
		permissionLevel = newPermissionLevel;
	}


	/**
	 * Getter for the Bridge
	 * @return the bridge
	 */
	public CommunicationBridge getBridge() {
		return bridge;
	}

	/**
	 * Setter for the bridge
	 * @param bridge the bridge
	 */
	public void setBridge(CommunicationBridge bridge) {
		this.bridge = bridge;
	}
	
	/**
	 * Getter for the PID fo the player on this device
	 * @return the UUID of the player on this device
	 */
	public UUID getPID()
	{
		return pid;
	}

}
