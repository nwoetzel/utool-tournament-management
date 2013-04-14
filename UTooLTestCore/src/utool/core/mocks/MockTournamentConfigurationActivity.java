package utool.core.mocks;

import android.content.Intent;
import android.net.Uri;
import android.util.SparseArray;
import utool.core.Core;
import utool.core.TournamentConfigurationActivity;
import utool.persistence.Profile;
import utool.persistence.SavableConfigurationList;
import utool.persistence.SavablePlayer;
import utool.persistence.SavablePlayerList;
import utool.persistence.TournamentConfiguration;

/**
 * Testable version of tournament configuration activity
 * @author Justin Kreier
 * @version 1/20/2013
 *
 */
public class MockTournamentConfigurationActivity extends TournamentConfigurationActivity{
	
	/**
	 * Public accessor for the configuration list
	 * @return The configuration list
	 */
	public SavableConfigurationList getConfigurationList(){
		return this.configurationList;
	}
	
	/**
	 * Gets the tournament core
	 * @return The tournament core
	 */
	public Core getTournamentCore(){
		return this.tournamentCore;
	}
	
	/**
	 * Gets the configuration
	 * @return the configuration
	 */
	public TournamentConfiguration getConfiguration(){
		return this.configuration;
	}
	
	/**
	 * Gets the list of all players
	 * @return The list of players
	 */
	public SavablePlayerList getPlayers(){
		return this.players;
	}
	
	/**
	 * Gets the list of selected players
	 * @return The list of selected players
	 */
	public SavablePlayerList getSelectedPlayers(){
		return this.selectedPlayers;
	}
	
	/**
	 * Gets the plugin intent map
	 * @return The plugin intent map
	 */
	public SparseArray<Intent> getIntentMap(){
		return this.intentMap;
	}
	
	/**
	 * Gets the selected profile
	 * @return The selected profile
	 */
	public Profile getSelectedProfile(){
		return this.selectedProfile;
	}
	
	/**
	 * Gets the list of other detected local profiles
	 * @return The list of unselected local profiles
	 */
	public SavablePlayerList getOtherProfiles(){
		return this.otherProfiles;
	}
	
	/**
	 * Gets the player representation of the selected profile
	 * @return The player representation of the selected profile
	 */
	public SavablePlayer getProfile(){
		return this.profile;
	}
	
	/**
	 * Gets the list of recent players
	 * @return The list of recent players
	 */
	public SavablePlayerList getRecentPlayers(){
		return this.recentPlayers;
	}
	
	/**
	 * Gets the list of added players
	 * @return The list of added players
	 */
	public SavablePlayerList getAddedPlayers(){
		return this.addedPlayers;
	}
	
	/**
	 * Gets the image uri
	 * @return The image ru
	 */
	public Uri getImageUri(){
		return this.imageUri;
	}
	
	/**
	 * Gets which player is currently being edited
	 * @return The index of the player being edited, or -1 if no player is being edited
	 */
	public int getPlayerBeingEdited(){
		return this.playerBeingEdited;
	}

	@Override
	public void deletePressedFromContext(int position){
		super.deletePressedFromContext(position);
	}
	
	@Override
	public void addPlayerButtonPressed(){
		super.addPlayerButtonPressed();
	}
	
	@Override
	public void editPressedFromContext(int position){
		super.editPressedFromContext(position);
	}
	
	@Override
	public void checkPressedFromContext(int position){
		super.checkPressedFromContext(position);
	}
}
