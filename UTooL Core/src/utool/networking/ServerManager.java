package utool.networking;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

import android.util.Log;

import utool.core.Core;
import utool.networking.packet.HostInformation;
import utool.plugin.activity.PluginCommonActivityHelper;

/**
 * This class listens for incoming connections, and handles sending and receiving data from established client connections.
 * @author Cory
 *
 */
public class ServerManager extends SocketWrapper {
	/**
	 * The listening socket
	 */
	private ServerSocket listenerSocket;

	/**
	 * The list of all open sockets for connected clients
	 */
	private List<SocketInfo> connections = Collections.synchronizedList(new ArrayList<SocketInfo>());

	/**
	 * The thread running the listener socket
	 */
	private Thread listenThread;

	/**
	 * The timer used for removing stale sockets from the connection list
	 */
	private Timer socketCleanupTimer;
	
	/**
	 * The message to send a client immediately after connection.
	 * This is for starting the plugin on the client when the host has already loaded their plugin.
	 */
	private String initialConnectMessage = null;

	/**
	 * Create a new ServerManager listening on the given port
	 * @param tournamentCore The tournament core to associate with this server manager
	 * @throws IOException Thrown on an error creating the socket
	 */
	public ServerManager(Core tournamentCore) throws IOException{
		super(tournamentCore, false);
		
		listenerSocket = new ServerSocket(0);
		Runnable listenRunnable = new Runnable() {

			public void run() {
				listen();
			}
		};
		listenThread = new Thread(listenRunnable);
		listenThread.start();

		socketCleanupTimer = new Timer();
		TimerTask socketCleanupTask = new TimerTask() {

			@Override
			public void run() {
				cleanConnectionList();
			}
		};
		socketCleanupTimer.scheduleAtFixedRate(socketCleanupTask, 0, 1000);
	}

	/**
	 * Get the port number of this server socket
	 * @return The port number
	 */
	public int getPort(){
		return listenerSocket.getLocalPort();
	}

	/**
	 * Get the number of connected devices
	 * @return Number of connections established
	 */
	public int getConnectionCount(){
		return connections.size();
	}

	/**
	 * Clean closed sockets from the connection list
	 */
	public void cleanConnectionList(){
		synchronized (connections) {
			for (Iterator<SocketInfo> itr = connections.iterator(); itr.hasNext(); ){
				SocketInfo s = itr.next();
				if (s == null || s.socket.isClosed()){
					itr.remove();
				}
			}
		}
	}

	/**
	 * Listen for new connections
	 */
	private void listen(){
		while(listenerSocket != null && !listenerSocket.isClosed()){
			try {
				Socket conn = listenerSocket.accept();
				//Perform initial communications
				byte[] initMessasge = new byte[5];
				conn.getInputStream().read(initMessasge);
				boolean isUtool = (initMessasge[0] == 'U' &&
						initMessasge[1] == 'T' &&
						initMessasge[2] == 'o' && 
						initMessasge[3] == 'o' &&
						initMessasge[4] == 'L');
				if (isUtool){
					//Client has been verified as UTooL client
					synchronized(connections){
						SocketInfo sInfo = new SocketInfo(conn);
						connections.add(sInfo);
						sInfo.startReceive();
					}
					//send basic tournament information
					HostInformation hostInfo = new HostInformation(tournamentCore.getTournamentName(), tournamentCore.getServerPort(), tournamentCore.getTournamentUUID());
					write(hostInfo.getXml().getBytes(), conn);
					
					//send initial connection message (plugin start message)
					if (initialConnectMessage != null){
						write(initialConnectMessage.getBytes(), conn);
					}
				} else {
					conn.close();
				}
			} catch (IOException e) {
				Log.e("ServerManager", e.getMessage());
			}
		}
	}

	/**
	 * Send data to all connected clients
	 * @param data The data to send
	 */
	public void send(byte[] data){
		//Start threads to send out on each open socket
		synchronized(connections){
			for (SocketInfo conn: connections){		
				conn.send(data);
			}
		}
	}

	/**
	 * Set the message to send to clients immediately after they connect.
	 * @param message The message to send
	 */
	public void setInitialConnectMessage(String message){
		this.initialConnectMessage = message;
	}

	/**
	 * Close all connections to this server
	 */
	public void close(){
		socketCleanupTimer.cancel();
		try {
			listenerSocket.close();
		} catch (IOException e) {
		}
		listenerSocket = null;
		try {
			receivedData.put(PluginCommonActivityHelper.UTOOL_SOCKET_CLOSED_MESSAGE.getBytes());
		} catch (InterruptedException e1) {
		}
		synchronized (connections) {
			for (SocketInfo conn:connections){
				try {
					conn.close();
				} catch (IOException e) {
				}
			}
		}
	}
}
