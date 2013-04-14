package utool.core.tests;

import java.util.UUID;

import android.util.Log;
import utool.persistence.SavableConfigurationList;
import utool.persistence.SavablePlayerList;
import utool.persistence.TournamentConfiguration;
import junit.framework.TestCase;

/**
 * Tests the save and load functionality of the SavabelConfigurationList
 * @author Justin Kreier
 * @version 1/12/2013
 */
public class TestSavableConfigurationList extends TestCase{

	/**
	 * Tests that save works properly
	 */
	public void testSave(){
		SavableConfigurationList list = new SavableConfigurationList();
		UUID id1 = UUID.randomUUID();
		UUID id2 = UUID.randomUUID();
		list.add(new TournamentConfiguration("Player 1", "Plugin 1", "Package 1", new SavablePlayerList(), id1));
		list.add(new TournamentConfiguration("Player 2", "Plugin 2", "Package 2", new SavablePlayerList(), id2));
		
		String expected = "Player 1\nPlugin 1\nPackage 1\n"+id1+"\n[]\\";
		expected += "Player 2\nPlugin 2\nPackage 2\n"+id2+"\n[]";
		
		String actual = list.save();
		
		Log.d("Expected", expected);
		Log.d("Actual", actual);
		
		assertEquals(expected, actual);
	}
	
	/**
	 * Tests that load works properly
	 */
	public void testLoad(){
		SavableConfigurationList list = new SavableConfigurationList();
		UUID id1 = UUID.randomUUID();
		UUID id2 = UUID.randomUUID();
		
		String input = "Player 1\nPlugin 1\nPackage 1\n"+id1+"\n[]\\";
		input += "Player 2\nPlugin 2\nPackage 2\n"+id2+"\n[]";
		
		list = (SavableConfigurationList)list.load(input);
		
		assertEquals(2, list.size());
		assertEquals("Player 1",list.get(0).getTournamentName());
		assertEquals("Player 2",list.get(1).getTournamentName());
		
		assertEquals("Plugin 1",list.get(0).getPluginName());
		assertEquals("Plugin 2",list.get(1).getPluginName());
		
		assertEquals("Package 1", list.get(0).getPluginPackage());
		assertEquals("Package 2", list.get(1).getPluginPackage());
		
		assertEquals(id1, list.get(0).getTournamentUUID());
		assertEquals(id2, list.get(1).getTournamentUUID());
	}
	
}
