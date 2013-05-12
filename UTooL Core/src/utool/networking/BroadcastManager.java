package utool.networking;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.util.HashMap;
import java.util.UUID;

import utool.core.AbstractTournament;
import utool.core.Core;
import utool.networking.packet.HostInformation;


import android.content.Context;
import android.net.DhcpInfo;
import android.net.wifi.WifiManager;
import android.net.wifi.WifiManager.WifiLock;
import android.os.PowerManager;
import android.os.PowerManager.WakeLock;

/**
 * This class handles sending and receiving broadcast tournament advertisement packets.
 * @author Cory
 *
 */
public class BroadcastManager {
	/**
	 * The port number that advertisements are sent to.
	 */
	public static final int DISCOVERY_PORT_NUMBER = 62573;

	/**
	 * The maximum size of advertisement packets
	 */
	public static final int DISCOVERY_PACKET_SIZE = 1024;

	/**
	 * The thread used for receiving advertisement packets
	 */
	private static Thread discoveryThread;

	/**
	 * The socket used for receiving advertisements
	 */
	private static java.net.DatagramSocket discoverySocket;

	/**
	 * The socket used for sending advertisements out on.
	 */
	private java.net.DatagramSocket advertisementSocket;

	/**
	 * The thread used for sending advertisements
	 */
	private Thread advertisementThread;

	/**
	 * The HostInformation for the tournament being advertised
	 */
	private HostInformation tournamentHostInformation;

	/**
	 * Context received from the Android class that instantiated this class.
	 */
	private Context context;
	
	/**
	 * WiFi lock for each broadcast manager instance
	 */
	private WifiLock wifiLock;
	
	/**
	 * HashMap of all BroadcastManagers
	 */
	private static HashMap<Long, BroadcastManager> broadcastManagers = new HashMap<Long, BroadcastManager>();
	
	/**
	 * Update the tournament name for a given Core.
	 * This does nothing if the core doesn't already have running a broadcast manager.
	 * @param tournament The tournament to update.
	 */
	public static void updateTournamentName(Core tournament){
		BroadcastManager manager = broadcastManagers.get(tournament.getTournamentId());
		if (manager != null && manager.tournamentHostInformation != null){
			manager.tournamentHostInformation.setTournamentName(tournament.getTournamentName());
		}
	}

	/**
	 * Create a BroadcastManager for sending advertisements.
	 * @param context Android context
	 * @param tournamentName Name of the tournament to advertise
	 * @param tournamentId The tournament's id
	 * @param tournamentPort Port number of the tournament server
	 * @param tournamentUUID UUID of the tournament to broadcast
	 */
	public BroadcastManager(Context context, String tournamentName, int tournamentPort, long tournamentId, UUID tournamentUUID){
		this.context = context;
		if (tournamentUUID == null){
			throw new NullPointerException("Tournament UUID must not be null.");
		}
		this.tournamentHostInformation = new HostInformation(tournamentName, tournamentPort, tournamentUUID);
		synchronized (broadcastManagers) {
			broadcastManagers.put(tournamentId, this);
		}
	}
	
	/**
	 * Start advertising on all existing broadcast managers
	 */
	public static void startAllBroadcastManagers(){
		synchronized (broadcastManagers) {
			for (BroadcastManager b: broadcastManagers.values()){
				try {
					b.startServerAdvertisement();
				} catch (SocketException e) {
				}
			}
		}
	}
	
	/**
	 * Stop advertising on all broadcast managers
	 */
	public static void stopAllBroadcastManagers(){
		synchronized (broadcastManagers) {
			for (BroadcastManager b: broadcastManagers.values()){
				b.stopServerAdvertisement();
			}
		}
	}
	
	/**
	 * Stop the broadcast manager for a tournament
	 * @param tournamentId The tournament id
	 */
	public static void stopBroadcastManager(long tournamentId){
		BroadcastManager b = broadcastManagers.get(tournamentId);
		if (b != null){
			b.stopServerAdvertisement();
		}
	}

	/**
	 * Start listening and tracking broadcast server advertisements.
	 * @throws SocketException Thrown when socket binding fails, meaning the port is already in use.
	 */
	public static synchronized void startHostDiscovery() throws SocketException{
		//There should only be one discovery thread running
		if (discoveryThread == null || !discoveryThread.isAlive()){
			//discoveredHosts.clear();
			discoverySocket = new DatagramSocket(DISCOVERY_PORT_NUMBER);
			discoverySocket.setBroadcast(true);
			discoverySocket.setSoTimeout(0);
			//Runnable for listening on the socket
			Runnable discoveryRunnable = new Runnable() {

				public void run() {
					while (discoverySocket != null && discoverySocket.isBound()){
						byte[] buf = new byte[DISCOVERY_PACKET_SIZE];
						DatagramPacket packet = new DatagramPacket(buf, buf.length);
						try {
							discoverySocket.receive(packet);
							HostInformation hostInfo = new HostInformation(packet.getAddress(), packet.getData(), packet.getLength());
							addDiscoveredHost(hostInfo);
						} catch (Exception e) {
							//On error, just break the loop and stop the thread
							break;
						}
					}
				}
			};
			//Start the discovery thread
			discoveryThread = new Thread(discoveryRunnable);
			discoveryThread.start();
		}
	}

	/**
	 * Stop listening for broadcast server information
	 */
	public static synchronized void stopHostDiscovery(){
		//discoveryCleanupTimer.cancel();
		try{
			discoverySocket.close();
		} catch (Exception ex) {}
		discoverySocket = null;
	}

	/**
	 * Add a newly discovered host to the AbstractTournament tournament data list
	 * @param hostInfo The HostInformation object to add
	 */
	private static void addDiscoveredHost(HostInformation hostInfo){
		AbstractTournament.addTournament(hostInfo.getTournamentUUID(), hostInfo);
	}

	/**
	 * Start advertising a server. This will silently fail in the emulator.
	 * @throws SocketException When socket binding fails
	 */
	public void startServerAdvertisement() throws SocketException{
		if (advertisementThread == null || !advertisementThread.isAlive()){
			advertisementSocket = new DatagramSocket();
			advertisementSocket.setBroadcast(true);
			
			WifiManager wifi = (WifiManager) context.getApplicationContext().getSystemService(Context.WIFI_SERVICE);
			if (wifiLock != null && !wifiLock.isHeld()){
				wifiLock = wifi.createWifiLock(WifiManager.WIFI_MODE_FULL, "BroadcastWifiLock");
				wifiLock.acquire();
			}
			
			//Prepare advertisement runnable
			Runnable advertisementRunnable = new Runnable() {

				public void run() {
					try {
						while (advertisementSocket != null){
							byte[] advertisementData = tournamentHostInformation.getPacketData();
							InetAddress broadcastAddress = getBroadcastAddress();
							DatagramPacket packet = new DatagramPacket(advertisementData, advertisementData.length, broadcastAddress, DISCOVERY_PORT_NUMBER);
							advertisementSocket.send(packet);
							Thread.sleep(500, 0);
						}
						if (wifiLock != null && wifiLock.isHeld()){
							wifiLock.release();
						}
					} catch (Exception e) {
					}
				}
			};
			//Start thread
			advertisementThread = new Thread(advertisementRunnable);
			advertisementThread.start();
		}
	}

	/**
	 * Stop advertising a server
	 */
	public void stopServerAdvertisement(){
		try{
			advertisementSocket.close();
		} catch (Exception ex) {}
		advertisementSocket = null;
	}	

	/**
	 * Get a list of all currently discovered servers
	 * @return An ArrayList of discovered hosts
	 */
	/*public static List<HostInformation> getDiscoveredHosts(){
		synchronized (discoveredHosts) {
			for (Iterator<HostInformation> iter = discoveredHosts.iterator(); iter.hasNext(); ){
				HostInformation host = iter.next();
				if (!host.isRecent()){
					iter.remove();
				}
			}
			return discoveredHosts;
		}
	}*/

	/**
	 * Get the broadcast address for the Wi-Fi connection
	 * @return The Wi-Fi broadcast address
	 */
	private InetAddress getBroadcastAddress() {
		WifiManager wifi = (WifiManager) context.getSystemService(Context.WIFI_SERVICE);
		DhcpInfo dhcp = wifi.getDhcpInfo();
		// handle null somehow
		if (dhcp == null || dhcp.ipAddress == 0){
			byte[] b = new byte[4];
			//10.1.2.255
			b[0] = (byte)10;
			b[1] = (byte)1;
			b[2] = (byte)2;
			b[3] = (byte)255;
			try {
				return InetAddress.getByAddress(b);
			} catch (UnknownHostException e) {
				return null;
			}
		}

		int broadcast = (dhcp.ipAddress & dhcp.netmask) | ~dhcp.netmask;
		byte[] quads = new byte[4];
		for (int k = 0; k < 4; k++)
			quads[k] = (byte) ((broadcast >> k * 8) & 0xFF);
		InetAddress broadcastAddress = null;
		try{
			broadcastAddress = InetAddress.getByAddress(quads);
		} catch (IOException e) {}
		return broadcastAddress;
	}
}
