package utool.core.tests;

import java.util.Random;
import java.util.UUID;

import utool.persistence.SavablePlayer;
import utool.persistence.SavablePlayerList;
import utool.persistence.TournamentConfiguration;
import junit.framework.TestCase;

/**
 * Tests that the tournament configuration object works properly
 * @author Justin
 * @version 1/6/2013
 */
public class TestTournamentConfiguration extends TestCase{

	
	/**
	 * Tests that the implemented save method works properly
	 */
	public void testSave(){
		String testName = "test name";
		String testPlugin = "test plugin";
		String testPluginPackage = "test package";
		SavablePlayerList players = new SavablePlayerList();
		
		UUID testID1 = UUID.randomUUID();
		String playerName1 = "test player 1";
		boolean ghost1 = false;
		Random r = new Random();
		int seedValue1 = r.nextInt();
		String filepath1 = "testFilepath1/derp/herp";
		
		SavablePlayer p1 = new SavablePlayer(testID1, playerName1, ghost1, seedValue1, filepath1);
		
		UUID testID2 = UUID.randomUUID();
		String playerName2 = "test player 1";
		boolean ghost2 = false;
		int seedValue2 = r.nextInt();
		String filepath2 = "testFilepath1/derp/herp";
		
		SavablePlayer p2 = new SavablePlayer(testID2, playerName2, ghost2, seedValue2, filepath2);
		
		players.add(p1);
		players.add(p2);
		
		TournamentConfiguration config = new TournamentConfiguration(testName, testPlugin, testPluginPackage, players);
		
		String output = config.save();
		
		String expected = testName+"\n";
		expected += testPlugin+"\n";
		expected += testPluginPackage+"\n";
		expected += config.getTournamentUUID()+"\n";
		expected += "[";
		expected += p1.save()+"\t";
		expected += p2.save();
		expected += "]";
		
		assertEquals(expected, output);
	}
	
	/**
	 * Tests that the implemented load method works properly
	 */
	public void testLoad(){
		
		String testName = "test name";
		String testPlugin = "test plugin";
		String testPluginPackage = "test package";
		UUID tournamentID = UUID.randomUUID();
		
		UUID testID1 = UUID.randomUUID();
		String playerName1 = "test player 1";
		boolean ghost1 = false;
		Random r = new Random();
		int seedValue1 = r.nextInt();
		String filepath1 = "testFilepath1/derp/herp";
		
		SavablePlayer p1 = new SavablePlayer(testID1, playerName1, ghost1, seedValue1, filepath1);
		
		UUID testID2 = UUID.randomUUID();
		String playerName2 = "test player 1";
		boolean ghost2 = false;
		int seedValue2 = r.nextInt();
		String filepath2 = "testFilepath1/derp/herp";
		
		SavablePlayer p2 = new SavablePlayer(testID2, playerName2, ghost2, seedValue2, filepath2);
		
		String toLoad = testName+"\n";
		toLoad += testPlugin+"\n";
		toLoad += testPluginPackage+"\n";
		toLoad += tournamentID+"\n";
		toLoad += "[";
		toLoad += p1.save()+"\t";
		toLoad += p2.save();
		toLoad += "]";
		
		
		TournamentConfiguration t = new TournamentConfiguration();
		t = (TournamentConfiguration)t.load(toLoad);
		
		assertEquals(testName, t.getTournamentName());
		assertEquals(testPlugin, t.getPluginName());
		assertEquals(tournamentID, t.getTournamentUUID());
		
		assertEquals(2, t.getPlayers().size());
	}
}
