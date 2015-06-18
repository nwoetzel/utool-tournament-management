package utool.core.tests;

import java.util.Random;
import java.util.UUID;

import utool.persistence.SavablePlayer;

import junit.framework.TestCase;

/**
 * Tests the proper saving and loading of the savable player
 * @author Justin Kreier
 * @version 1/6/2013
 */
public class TestSavablePlayer extends TestCase{
	
	/**
	 * Tests that the implemented save method works properly
	 */
	public void testSave(){
		UUID testID = UUID.randomUUID();
		String testName = "test name";
		boolean ghost = false;
		Random r = new Random();
		int seedValue = r.nextInt();
		String filepath = "testFilepath/derp/herp";
		
		
		SavablePlayer p = new SavablePlayer(testID, testName, ghost, seedValue, filepath);
		
		String output = p.save();
		
		String expected = testID.toString()+"\n";
		expected += "=="+testName+"==\n";
		expected += "false\n";
		expected += seedValue+"\n";
		expected += filepath;
		
		assertEquals(expected, output);
		
		
		ghost = true;
		p = new SavablePlayer(testID, testName, ghost, seedValue, filepath);
		
		output = p.save();
		
		expected = testID.toString()+"\n";
		expected += "=="+testName+"==\n";
		expected += "true\n";
		expected += seedValue+"\n";
		expected += filepath;
		assertEquals(expected, output);
	}
	
	/**
	 * Tests that the implemented load method works properly
	 */
	public void testLoad(){
		UUID testID = UUID.randomUUID();
		String testName = "test name";
		boolean ghost = false;
		Random r = new Random();
		int seedValue = r.nextInt();
		String filepath = "testFilepath/derp/herp";
		
		String toLoad = testID.toString()+"\n";
		toLoad += "=="+testName+"==\n";
		toLoad += "false\n";
		toLoad += seedValue+"\n";
		toLoad += filepath;
		
		
		SavablePlayer p = new SavablePlayer();
		
		p = (SavablePlayer)p.load(toLoad);
		
		assertEquals(testID,p.getUUID());
		assertEquals(testName, p.getName());
		assertEquals(ghost, p.isGhost());
		assertEquals(seedValue, p.getSeedValue());
		assertEquals(filepath, p.getPortraitFilepath());
	}

}
