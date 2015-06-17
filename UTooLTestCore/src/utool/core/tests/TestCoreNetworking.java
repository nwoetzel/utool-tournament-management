package utool.core.tests;

import java.io.IOException;
import java.net.InetAddress;
import java.net.SocketException;
import java.util.LinkedList;
import java.util.List;
import java.util.UUID;

import utool.core.AbstractTournament;
import utool.core.Core;
import utool.networking.BroadcastManager;
import utool.networking.ClientManager;
import utool.networking.packet.CoreMessage;
import utool.networking.packet.PlayerMessage;
import utool.networking.packet.HostInformation;
import utool.networking.packet.PluginStartMessage;
import utool.plugin.Player;
import utool.plugin.dummy.DummyMessage;
import android.content.Intent;
import android.graphics.Bitmap;
import android.test.AndroidTestCase;

/**
 * Test class for networking package
 * @author Cory
 *
 */
public class TestCoreNetworking extends AndroidTestCase {

	/**
	 * Test the HostInformation message class
	 * @throws Exception When something bad happens
	 */
	public void testHostInformation() throws Exception {
		HostInformation hi1 = new HostInformation("Test Server", 55555, UUID.randomUUID());

		byte[] data = hi1.getPacketData();
		byte[] ipaddr = {127, 0, 0, 1};
		InetAddress serverAddress = InetAddress.getByAddress(ipaddr);
		HostInformation hi2 = new HostInformation(serverAddress, data, data.length);

		assertEquals(hi1.getTournamentName(), hi2.getTournamentName());
		assertEquals(hi1.getServerPort(), hi2.getServerPort());
		assertEquals(hi1.getTournamentUUID(), hi2.getTournamentUUID());
		assertEquals("Test Server@" + serverAddress, hi2.toString());
		assertEquals(serverAddress, hi2.getServerAddress());
		
		HostInformation hi3 = new HostInformation(hi1.getXml());
		assertEquals(hi1.getTournamentName(), hi3.getTournamentName());
		assertEquals(hi1.getServerPort(), hi3.getServerPort());
		assertEquals(hi1.getTournamentUUID(), hi3.getTournamentUUID());
	}

	/**
	 * Test the CoreMessage class
	 * @throws Exception When something bad happens
	 */
	public void testCoreMessage() throws Exception {
		HostInformation hi1 = new HostInformation("Test Server", 55555, UUID.randomUUID());
		String payload = hi1.getXml();

		CoreMessage cm1 = new CoreMessage(CoreMessage.MESSAGE_TYPE_CORE, payload);
		String message = cm1.getXml();

		CoreMessage cm2 = new CoreMessage(message);
		assertEquals(payload, cm2.getPayload());
		assertEquals(CoreMessage.MESSAGE_TYPE_CORE, cm2.getMessageType());

		CoreMessage cm3 = new CoreMessage(CoreMessage.MESSAGE_TYPE_PLUGIN, payload);
		String message2 = cm3.getXml();
		CoreMessage cm4 = new CoreMessage(message2);
		assertEquals(payload, cm4.getPayload());
		assertEquals(CoreMessage.MESSAGE_TYPE_PLUGIN, cm4.getMessageType());
	}

	/**
	 * Test the PluginStart message class
	 * @throws Exception When something bad happens
	 */
	public void testPluginStartMessage() throws Exception {
		String pkg = "utool.test";
		String cls = pkg + ".ClassName";
		PluginStartMessage message = new PluginStartMessage(pkg, cls);
		String xml = message.getXml();

		PluginStartMessage message2 = new PluginStartMessage(xml);
		String xml2 = message2.getXml();
		assertEquals(xml, xml2);

		Intent i = message.getIntent();
		assertEquals(pkg, i.getComponent().getPackageName());
		assertEquals(cls, i.getComponent().getClassName());
	}

	/**
	 * Test the DummyMessage message class
	 * @throws Exception When something bad happens
	 */
	public void testDummyMessage() throws Exception {
		String text = "abcd";
		Bitmap b = Bitmap.createBitmap(100, 200, Bitmap.Config.ARGB_8888);

		DummyMessage m1 = new DummyMessage(text, b);
		assertEquals(text, m1.getTextData());
		assertEquals(b.getHeight(), m1.getImage().getHeight());
		assertEquals(b.getWidth(), m1.getImage().getWidth());

		String xml = m1.getXml();

		DummyMessage m2 = new DummyMessage(xml);

		assertEquals(m1.getTextData(), m2.getTextData());
		assertEquals(m1.getImage().getHeight(), m2.getImage().getHeight());
		assertEquals(m1.getImage().getWidth(), m2.getImage().getWidth());
	}

	
	
//	TODO: This test was preventing me from running the test suite due to failures
//	--Justin
	
//	Can not reproduce problem -- Cory
//	
	/**
	 * Test host broadcast and discovery. If this test fails, your emulator is probably not configured correctly.
	 * @throws SocketException On socket errors
	 */
	public void testBroadcast() throws SocketException{
		BroadcastManager.startHostDiscovery();

		BroadcastManager bcastmanager = new BroadcastManager(this.getContext(), "", 1, 0, UUID.randomUUID());
		bcastmanager.startServerAdvertisement();
		try {
			Thread.sleep(1000);
		} catch (InterruptedException e) {
		}

		bcastmanager.stopServerAdvertisement();

		List<HostInformation> hosts = AbstractTournament.getDiscoveredHosts();
		BroadcastManager.stopHostDiscovery(); 

		assertTrue(hosts.size() >= 1);
	}

	/**
	 * Test the PlayerMessage class for registration messages.
	 * @throws Exception When something bad happens
	 */
	public void testPlayerRegisterMessage() throws Exception {
		Player p = new Player(UUID.randomUUID(), "Test Player");
		//Bitmap b = Bitmap.createBitmap(50, 60, Bitmap.Config.ARGB_8888);

		p.setGhost(true);
		PlayerMessage message = new PlayerMessage(p);
		String xml = message.getXml();

		PlayerMessage message2 = new PlayerMessage(xml);
		Player p2 = message2.getPlayer();
		assertEquals(p.getName(), p2.getName());
		assertEquals(p.getUUID(), p2.getUUID());
		assertEquals(p.isGhost(), p2.isGhost());
	}
	
	/**
	 * Test PlayerMessage class for player list messages.
	 * @throws Exception When something bad happens
	 */
	public void testPlayerListMessage() throws Exception{
		PlayerMessage req = new PlayerMessage();
		PlayerMessage rec = new PlayerMessage(req.getXml());
		assertEquals(req.getMessageType(), rec.getMessageType());
		assertEquals(0, rec.getPlayerList().size());
		
		Player p1 = new Player(UUID.randomUUID(), "Test Player 1");
		Player p2 = new Player(UUID.randomUUID(), "Test Player 2");
		List<Player> players1 = new LinkedList<Player>();
		players1.add(p1);
		players1.add(p2);
		
		PlayerMessage message = new PlayerMessage(players1);
		String xml = message.getXml();
		PlayerMessage message2 = new PlayerMessage(xml);
		Player p1_rec = message2.getPlayerList().get(0);
		Player p2_rec = message2.getPlayerList().get(1);
		assertEquals(p1.getName(), p1_rec.getName());
		assertEquals(p1.getUUID(), p1_rec.getUUID());
		assertEquals(p1.isGhost(), p1_rec.isGhost());
		assertEquals(p2.getName(), p2_rec.getName());
		assertEquals(p2.getUUID(), p2_rec.getUUID());
		assertEquals(p2.isGhost(), p2_rec.isGhost());
	}

//	TODO: This test no longer works, since server sends data immediately on client connection
	/**
	 * Test the client/server connection classes
	 * @throws Exception When something bad happens
	 */
	/*public void testClientServer() throws Exception {
		Core serverCore = Core.getNewCoreInstance();
		Core clientCore = Core.getNewCoreInstance();
		ServerManager server = new ServerManager(serverCore);
		ClientManager client = new ClientManager(InetAddress.getLocalHost(), server.getPort(), clientCore);

		Random r = new Random();
		
		byte[] data = new byte[10000];
		r.nextBytes(data);*/
		/*for (int i = 0; i < data.length; i++){
			data[i] = (byte)i;
		}*/
/*
		server.send(data);
		byte[] data2 = client.receive();
		for (int i = 0; i < data2.length; i++){
			assertEquals(data[i], data2[i]);
		}

		byte[] data3 = new byte[10000];
		r.nextBytes(data3);*/
		/*for (int i = 0; i < data3.length; i++){
			data3[i] = (byte)i;
		}*//*
		client.send(data3);
		byte[] data4 = server.receive();
		for (int i = 0; i < data4.length; i++){
			assertEquals(data3[i], data4[i]);
		}

		client.close();
		server.close();
	}*/
	
	/**
	 * Test if client network connections fail properly
	 * @throws Exception When something bad happens
	 */
	public void testClientConnectionFailure() throws Exception{
		IOException exception = null;
		Core clientCore = Core.getNewCoreInstance();
		try {
			@SuppressWarnings("unused")
			ClientManager client = new ClientManager(InetAddress.getLocalHost(), 0, clientCore);
		} catch (IOException e) {
			exception = e;
		}
		assertNotNull(exception);
	}

}