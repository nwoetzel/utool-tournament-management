package utool.core.tests;

import java.util.UUID;

import android.util.Log;

import utool.persistence.SavablePlayer;
import utool.persistence.SavablePlayerList;
import junit.framework.TestCase;

/**
 * Tests the save and load functionality of the savable player list
 * @author Justin Kreier
 * @version 1/12/2013
 */
public class TestSavablePlayerList extends TestCase{

	/**
	 * Tests that save works properly
	 */
	public void testSave(){
		SavablePlayerList list = new SavablePlayerList();
		UUID id1 = UUID.randomUUID();
		UUID id2 = UUID.randomUUID();
		list.add(new SavablePlayer(id1, "player name 1", false, -1, "filepath 1"));
		list.add(new SavablePlayer(id2, "player name 2", true, -1, "filepath 2"));
		
		String actual = list.save();
		
		String expected = id1+"\nplayer name 1\nfalse\n-1\nfilepath 1\\";
		expected += id2+"\nplayer name 2\ntrue\n-1\nfilepath 2";
		
		Log.d("Expected", expected);
		Log.d("Actual", actual);
		
		assertEquals(expected, actual);
	}
	
	/**
	 * Tests that load works properly
	 */
	public void testLoad(){
		UUID id1 = UUID.randomUUID();
		UUID id2 = UUID.randomUUID();
		String input = id1+"\nplayer name 1\nfalse\n-1\nfilepath 1\\";
		input += id2+"\nplayer name 2\ntrue\n-2\nfilepath 2";
		
		SavablePlayerList list = new SavablePlayerList();
		list = (SavablePlayerList) list.load(input);
		
		assertEquals(2, list.size());
		
		assertEquals(id1, list.get(0).getUUID());
		assertEquals(id2, list.get(1).getUUID());
		
		assertEquals("player name 1", list.get(0).getName());
		assertEquals("player name 2", list.get(1).getName());
		
		assertFalse(list.get(0).isGhost());
		assertTrue(list.get(1).isGhost());
		
		assertEquals(-1, list.get(0).getSeedValue());
		assertEquals(-2, list.get(1).getSeedValue());
		
		assertEquals("filepath 1", list.get(0).getPortraitFilepath());
		assertEquals("filepath 2", list.get(1).getPortraitFilepath());
	}
}
