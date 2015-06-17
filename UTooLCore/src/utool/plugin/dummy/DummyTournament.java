package utool.plugin.dummy;

import java.util.HashMap;
import java.util.UUID;

/**
 * Tournament state object for the dummy plugin
 * @author Cory
 *
 */
public class DummyTournament {
	/**
	 * HashMap storing instances for all tournaments
	 */
	private static HashMap<Long, DummyTournament> tournamentInstances = new HashMap<Long, DummyTournament>();
	
	/**
	 * The tournament's id
	 */
	private long tournamentId;
	
	/**
	 * The player's uuid
	 */
	public UUID pid = new UUID(-1,-1);
	
	/**
	 * The tournament's name
	 */
	private String tournamentName = null;
	
	/**
	 * The last message received
	 */
	private DummyMessage lastMessage;
	
	/**
	 * Get a dummy tournament instance for this tournament id. A new instance is created if an unknown id is provided.
	 * @param tournamentId The tournament id
	 * @return A dummy tournament object
	 */
	public static DummyTournament getInstance(long tournamentId){
		DummyTournament t = tournamentInstances.get(tournamentId); 
		if (t == null){
			t = new DummyTournament();
			tournamentInstances.put(tournamentId, t);
		}
		return t;
	}
	
	/**
	 * Get the tournament's id
	 * @return Tournament id
	 */
	public long getTournamentId(){
		return tournamentId;
	}
	
	/**
	 * Get the tournament name.
	 * @return The tournament name
	 */
	public String getTournamentName(){
			return tournamentName;
	}
	
	/**
	 * Set the tournament's name
	 * @param name The tournament's name
	 */
	public void setTournamentName(String name){
		tournamentName = name;
	}
	
	/**
	 * Get the last message received from the network
	 * @return A DummyMessage or null
	 */
	public DummyMessage getLastMessage(){
		return lastMessage;
	}
	
	/**
	 * Set the last message received from the network
	 * @param lastMessage A DummyMessage
	 */
	public void setLastMessage(DummyMessage lastMessage){
		this.lastMessage = lastMessage;
	}

}
