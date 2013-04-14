package utool.core.tests;

import java.util.UUID;

import android.graphics.Bitmap;
import android.os.Parcel;
import android.test.AndroidTestCase;

import utool.plugin.Player;

/**
 * Tests for the Player class
 * @author Cory
 *
 */
public class TestPlayer extends AndroidTestCase {
	
	/**
	 * Test player parceling
	 * @throws Exception When something bad happens
	 */
	public void testPlayerParcel() throws Exception{
		Player p1 = new Player(UUID.randomUUID(), "Test Player 1");
		Bitmap b = Bitmap.createBitmap(50, 40, Bitmap.Config.ARGB_8888);
		p1.setPortrait(b);
		
		Parcel playerParcel = Parcel.obtain();
		p1.writeToParcel(playerParcel, 0);
		playerParcel.setDataPosition(0);
		Player p2 = new Player(playerParcel);
		assertEquals(p1.getName(), p2.getName());
		assertEquals(p1.getUUID(), p2.getUUID());
		assertEquals(p1.getPermissionsLevel(), p2.getPermissionsLevel());
		assertEquals(p1.getPortrait().getHeight(), p2.getPortrait().getHeight());
		assertEquals(p1.getPortrait().getWidth(), p2.getPortrait().getWidth());
	}

}
