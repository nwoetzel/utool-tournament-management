package utool.core.tests;

import java.util.UUID;

import utool.core.mocks.MockTournamentConfigurationActivity;
import utool.persistence.Profile;
import utool.persistence.SavablePlayer;
import android.content.Intent;
import android.os.Bundle;
import android.test.ActivityUnitTestCase;

/**
 * Tests proper functionality of the tournament configuration activity
 * @author Justin Kreier
 * @version 1/20/2013
 */
public class TestTournamentConfigurationActivity extends ActivityUnitTestCase<MockTournamentConfigurationActivity> {

	/**
	 * Required constructor
	 */
	public TestTournamentConfigurationActivity() {
		super(MockTournamentConfigurationActivity.class);
	}

	/**
	 * The activity under test
	 */
	private MockTournamentConfigurationActivity mActivity;

	@Override
	protected void setUp() throws Exception{
		super.setUp();	
		
		//clear application data
		AndroidTestHelperMethods.clearApplicationData(getInstrumentation().getTargetContext());

		//should not get to screen without a profile, so we're going to add one
		AndroidTestHelperMethods.addProfile(new Profile("Test Profile", "Test Filepath", UUID.randomUUID()), getInstrumentation().getTargetContext());

		//start the activity
		Intent intent = new Intent(getInstrumentation().getTargetContext(), MockTournamentConfigurationActivity.class);
		mActivity = startActivity(intent, (Bundle)null, (Object)null);
		mActivity.onResume();

	}

	@Override
	protected void tearDown() throws Exception{

		//clear application data
		AndroidTestHelperMethods.clearApplicationData(getInstrumentation().getTargetContext());
		
		super.tearDown();
	}

	/**
	 * Tests that all instance variables get initialized
	 */
	public void testInitialization(){	
		assertNotNull(mActivity.getConfigurationList());
		assertNull(mActivity.getTournamentCore());
		assertNotNull(mActivity.getConfiguration());
		assertNotNull(mActivity.getPlayers());
		assertNotNull(mActivity.getSelectedPlayers());
		assertNotNull(mActivity.getIntentMap());
		assertNotNull(mActivity.getSelectedProfile());
		assertNotNull(mActivity.getOtherProfiles());
		assertNotNull(mActivity.getProfile());
		assertNotNull(mActivity.getRecentPlayers());
		assertNotNull(mActivity.getAddedPlayers());
		assertNotNull(mActivity.getImageUri());
		assertEquals(-1, mActivity.getPlayerBeingEdited());

	}

	/**
	 * Tests that pressing delete from context removes the player everywhere
	 */
	public void testDeletePressedFromContext(){
		assertEquals(0, mActivity.getOtherProfiles().size());
		assertEquals(1, mActivity.getPlayers().size());
		
		//add 3 players through UI
		mActivity.addPlayerButtonPressed();
		mActivity.addPlayerButtonPressed();
		mActivity.addPlayerButtonPressed();

		//add a 4th player as a "recent player" (normally has to be done through loading)
		SavablePlayer p = new SavablePlayer();
		mActivity.getRecentPlayers().add(p);
		mActivity.getPlayers().add(p);

		assertEquals(5, mActivity.getPlayers().size());
		assertEquals(4, mActivity.getSelectedPlayers().size());
		assertEquals(1, mActivity.getRecentPlayers().size());
		assertEquals(3, mActivity.getAddedPlayers().size());

		//try to delete the profile
		mActivity.deletePressedFromContext(0);

		//verify that nothing was deleted (profiles are deleted elsewhere)
		assertEquals(5, mActivity.getPlayers().size());
		assertEquals(4, mActivity.getSelectedPlayers().size());
		assertEquals(1, mActivity.getRecentPlayers().size());
		assertEquals(3, mActivity.getAddedPlayers().size());

		//delete the last player (the one from recent players)
		mActivity.deletePressedFromContext(4);
		assertEquals(4, mActivity.getPlayers().size());
		assertEquals(4, mActivity.getSelectedPlayers().size());
		assertEquals(0, mActivity.getRecentPlayers().size());
		assertEquals(3, mActivity.getAddedPlayers().size());

		//delete the new last player (one of the added players)
		mActivity.deletePressedFromContext(3);
		assertEquals(3, mActivity.getPlayers().size());
		assertEquals(3, mActivity.getSelectedPlayers().size());
		assertEquals(0, mActivity.getRecentPlayers().size());
		assertEquals(2, mActivity.getAddedPlayers().size());
	}

	/**
	 * Tests that editPressedFromContext sets the proper player to be editable
	 */
	public void testEditPressedFromContext(){
		assertEquals(0, mActivity.getOtherProfiles().size());
		assertEquals(1, mActivity.getPlayers().size());
		
		//add 3 players through UI
		mActivity.addPlayerButtonPressed();
		mActivity.addPlayerButtonPressed();
		mActivity.addPlayerButtonPressed();

		assertEquals(4, mActivity.getPlayers().size());
		assertEquals(4, mActivity.getSelectedPlayers().size());
		assertEquals(0, mActivity.getRecentPlayers().size());
		assertEquals(3, mActivity.getAddedPlayers().size());

		//try to edit the profile
		mActivity.editPressedFromContext(0);
		assertEquals(-1,mActivity.getPlayerBeingEdited());

		//try to edit something else
		for (int i = 1; i < 4; i++){
			mActivity.editPressedFromContext(i);
			assertEquals(i, mActivity.getPlayerBeingEdited());
		}
	}
	
	/**
	 * Verifies that checking a player works properly
	 */
	public void checkPressedFromContext(){
		assertEquals(0, mActivity.getOtherProfiles().size());
		assertEquals(1, mActivity.getPlayers().size());
		
		//add 3 players through UI
		mActivity.addPlayerButtonPressed();
		mActivity.addPlayerButtonPressed();
		mActivity.addPlayerButtonPressed();

		assertEquals(4, mActivity.getPlayers().size());
		assertEquals(4, mActivity.getSelectedPlayers().size());
		assertEquals(0, mActivity.getRecentPlayers().size());
		assertEquals(3, mActivity.getAddedPlayers().size());
		
		
		//uncheck the profile
		mActivity.checkPressedFromContext(0);
		
		assertEquals(4, mActivity.getPlayers().size());
		assertEquals(3, mActivity.getSelectedPlayers().size());
		assertEquals(0, mActivity.getRecentPlayers().size());
		assertEquals(3, mActivity.getAddedPlayers().size());
		
		//uncheck the other 3 players
		for (int i = 1; i < 4; i++){
			mActivity.checkPressedFromContext(i);
			assertEquals(4, mActivity.getPlayers().size());
			assertEquals(3-i, mActivity.getSelectedPlayers().size());
			assertEquals(0, mActivity.getRecentPlayers().size());
			assertEquals(3, mActivity.getAddedPlayers().size());
		}
		
		//check all 4 players{
		for (int i = 3; i >= 0; i--){
			mActivity.checkPressedFromContext(i);
			assertEquals(4, mActivity.getPlayers().size());
			assertEquals(0+(4-i), mActivity.getSelectedPlayers().size());
			assertEquals(0, mActivity.getRecentPlayers().size());
			assertEquals(3, mActivity.getAddedPlayers().size());
		}
	}
	
	/**
	 * Tests that the add player button method adds players properly
	 */
	public void testAddPlayerButtonPressed(){
		assertEquals(1, mActivity.getPlayers().size());
		assertEquals(1, mActivity.getSelectedPlayers().size());
		assertEquals(0, mActivity.getRecentPlayers().size());
		assertEquals(0, mActivity.getAddedPlayers().size());
		
		for (int i = 0; i < 5; i++){
			mActivity.addPlayerButtonPressed();
			
			assertEquals(1+(1+i), mActivity.getPlayers().size());
			assertEquals(1+(1+i), mActivity.getSelectedPlayers().size());
			assertEquals(0, mActivity.getRecentPlayers().size());
			assertEquals((1+i), mActivity.getAddedPlayers().size());
		}
	}


}
