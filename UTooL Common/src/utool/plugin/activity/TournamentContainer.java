package utool.plugin.activity;

import java.util.HashMap;

/**
 * This class stores the list of tournament instances for a plugin
 * @author Justin Kreier
 * @version 1/22/2013
 */
public final class TournamentContainer {

	/**
	 * HashMap storing instances for all tournaments
	 */
	protected static HashMap<Long, AbstractTournament> tournamentInstances = new HashMap<Long, AbstractTournament>();
	
	/**
	 * Singleton pattern constructor
	 */
	private TournamentContainer(){
		
	}
	
	/**
	 * Stores an instance of an abstract tournament
	 * @param value The abstract tournament
	 */
	public static final void putInstance(AbstractTournament value){
		tournamentInstances.put(value.getTournamentId(), value);
	}
	
	/**
	 * Retrieves an instance of an abstract tournament
	 * @param tournamentId The tournament id
	 * @return The instance of the abstract tournament or null if it doesn't exist
	 */
	public static final AbstractTournament getInstance(long tournamentId){
		return tournamentInstances.get(tournamentId);
	}
	
	/**
	 * Clears the singleton instance of this tournament
	 * @param tournamentId The tournament id of the instance
	 */
	public static final void clearInstance(long tournamentId)
	{
		tournamentInstances.remove(tournamentId);
	}
}
