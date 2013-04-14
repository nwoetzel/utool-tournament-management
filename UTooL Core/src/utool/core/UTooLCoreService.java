package utool.core;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.InetAddress;
import java.util.HashMap;
import java.util.List;

import utool.networking.BroadcastManager;
import utool.networking.ClientManager;
import utool.networking.ServerManager;
import utool.networking.SocketWrapper;
import utool.networking.packet.PlayerMessage;
import utool.plugin.IUTooLCore;
import utool.plugin.Player;
import utool.remoteexceptions.ConnectionFailedRemoteException;
import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.os.RemoteException;
import android.util.Log;

/**
 * This class manages the UTooL Core Service.
 * This service handles all communications between plugins, the core, and other devices.
 * 
 * @author Cory Bryan
 *
 */
public class UTooLCoreService extends Service
{
	/**
	 * String used for core service discovery
	 * @since 10/4/2012
	 */
	static final String UTOOL_SERVICE_NAME = "utool.core.UTooLCoreService";

	/**
	 * HashMap for service instances
	 */
	private static HashMap<Long, IBinder> serviceManagers = new HashMap<Long, IBinder>();
	
	/**
	 * HashMap for SocketWrapper instances
	 */
	private static HashMap<Long, SocketWrapper> socketWrappers = new HashMap<Long, SocketWrapper>();
	
	/**
	 * Get the socket wrapper associated with the tournament id
	 * @param tournamentId The tournament id
	 * @return A SocketWrapper, or null if not available
	 */
	public static SocketWrapper getSocketWrapper(long tournamentId){
		return socketWrappers.get(tournamentId);
	}
	
	/**
	 * Forcefully close and remove a tournament service instance
	 * @param tournamentId The tournament ID of the instance to remove
	 */
	public static void closeServiceInstance(long tournamentId){		
		IUTooLCore mICore = IUTooLCore.Stub.asInterface(serviceManagers.get(tournamentId));
		try {
			mICore.close();
		} catch (RemoteException e) {
			e.printStackTrace();
		}
	}
	
	public static UTooLServiceImplementation getServiceForTournamentInstance(long tournamentId){
		return (UTooLServiceImplementation)serviceManagers.get(tournamentId);
	}
	
	@Override
	public void onCreate()
	{
		super.onCreate();
	}

	@Override
	public IBinder onBind(Intent intent)
	{
		long tournamentId = intent.getExtras().getLong("tournamentId", -1);
		if (tournamentId < 0){
			throw new IndexOutOfBoundsException();
		}

		//Return an existing instance of the service connection if it exists
		if (serviceManagers.containsKey(tournamentId)){
			return serviceManagers.get(tournamentId);
		}
		
		UTooLServiceImplementation mBinder = new UTooLServiceImplementation();
		mBinder.setTournamentCore(tournamentId);
		
		serviceManagers.put(tournamentId, mBinder);
		return mBinder;
	}
	
	/**
	 * Core-side implementation of the IUTooLCore interface 
	 */
	public class UTooLServiceImplementation extends IUTooLCore.Stub
	{
		/**
		 * True if this service is for a client connection
		 */
		private boolean isClient = false;
		/**
		 * Client manager. Only set when isClient is true
		 */
		private ClientManager clientManager;
		/**
		 * Server manager. Only set when isClient is false
		 */
		private ServerManager serverManager;
		/**
		 * True if the service is closed, meaning it should not be used
		 */
		private boolean isClosed = false;
		/**
		 * The tournament core associated with this service
		 */
		private Core tournamentCore;
		
		/**
		 * Set the tournament core using the tournament ID
		 * @param tournamentId The tournament ID of an existing tournament Core
		 */
		public void setTournamentCore(long tournamentId){
			this.tournamentCore = Core.getCoreInstance(tournamentId);
		}

		/**
		 * Configure this service connection for a specific tournament instance.
		 */
		public void setTournamentInformation(String tournamentName){
			if (tournamentName != null){
				this.tournamentCore.setTournamentName(tournamentName);
			}
		}

		/**
		 * Get whether this service instance is for a client connection.
		 * @return True if this service connection is a client connection, false if a server connection.
		 */
		public boolean isClient(){
			return isClient;
		}

		/**
		 * True if this service connection has been closed. Don't reuse a closed connection.
		 * @return True if closed
		 */
		public boolean isClosed(){
			return isClosed;
		}

		/**
		 * Get the connection count for this service instance. This will always be 1 for a client.
		 * @return The number of connected clients.
		 */
		public int getConnectionCount(){
			if (isClient){
				return 1;
			} else {
				return serverManager.getConnectionCount();
			}
		}

		/**
		 * Start this service connection in server mode.
		 * Plugins shouldn't need to run this method.
		 * @return The port the server was started on.
		 */
		public int startServer(long tournamentId) throws RemoteException {
			isClient = false;
			//this.tournamentCore = Core.getCoreInstance(tournamentId);
			try {
				serverManager = new ServerManager(tournamentCore);
				socketWrappers.put(tournamentId, serverManager);
				return serverManager.getPort();
			} catch (IOException e) {
				RemoteException r = new RemoteException();
				r.setStackTrace(e.getStackTrace());
				throw r;
			}
		}

		/**
		 * Start this service connection in client mode.
		 * Plugins shouldn't need to run this method.
		 * @param serverAddress The server address, in bytes, to connect to
		 * @param serverPort The port on the server to connect to
		 */
		public void connectToServer(byte[] serverAddress, int serverPort, long tournamentId) throws RemoteException {
			isClient = true;
			//this.tournamentCore = Core.getCoreInstance(tournamentId);
			InetAddress addr;
			try {
				addr = InetAddress.getByAddress(serverAddress);
				clientManager = new ClientManager(addr, serverPort, tournamentCore);
				socketWrappers.put(tournamentId, clientManager);
			} catch (IOException e){
				ConnectionFailedRemoteException r = new ConnectionFailedRemoteException();
				r.setStackTrace(e.getStackTrace());
				if (tournamentCore != null) {
					tournamentCore.shutdownCore();
				}
				throw r;
			} catch (Exception e) {
				Log.e(UTOOL_SERVICE_NAME, e.toString());
				RemoteException r = new RemoteException();
				r.setStackTrace(e.getStackTrace());
				throw r;
			}
		}

		/**
		 * Send messages to the socket(s)
		 * @param message The XML message to send
		 */
		public void send(String message) throws RemoteException
		{
			byte[] data;
			try {
				data = message.getBytes("UTF-8");
				//Choose class to send to, depending on client/server mode
				if (isClient && clientManager != null){
					clientManager.send(data);
				} else if (serverManager != null) {
					serverManager.send(data);
				}
			} catch (UnsupportedEncodingException e) {
			} catch (IOException e) {
				RemoteException r = new RemoteException();
				r.setStackTrace(e.getStackTrace());
				throw r;
			}
		}

		/**
		 * Receive messages from the socket(s).
		 * On error, a string of value "-1" is returned.
		 * @return Message string or "-1"
		 */
		public String receive() throws RemoteException
		{
			String message = null;
			byte[] data;
			try{
				if (isClient){
					data = clientManager.receive();
				} else {
					data = serverManager.receive();
				}
				message = new String(data, "UTF-8");
			} catch (Exception e){
				message = "-1";
				tournamentCore.shutdownCore();
			}
			return message;
		}

		/**
		 * Close all connections.
		 * This also removes this service instance, preventing it from binding again, and removes the tournament Core using Core.removeTournament().
		 */
		public void close(){
			if (!isClosed){
				if (isClient){
					clientManager.close();
				} else {
					serverManager.close();
					BroadcastManager.stopBroadcastManager(tournamentCore.getTournamentId());
				}
				socketWrappers.remove(tournamentCore.getTournamentId());
				isClosed = true;
			}
			serviceManagers.remove(tournamentCore.getTournamentId());
			tournamentCore.removeTournament();
			tournamentCore.shutdownCore();
		}

		/**
		 * Set the initial connection message when this service is in server mode.
		 */
		public void setInitialConnectMessage(String message){
			if (!isClient){
				serverManager.setInitialConnectMessage(message);
			}
		}

		/**
		 * Get all players in the tournament.
		 * If this is run on a client, the server is queried for the most recent list.
		 * @return A list of players
		 */
		public List<Player> getPlayerList() throws RemoteException {
			if (isClient){
				//request new player list
				PlayerMessage playerListRequest = new PlayerMessage();
				send(playerListRequest.getXml());
				//wait for player list to be received
				while (!playerListUpdated()){
					try {
						Thread.sleep(10);
					} catch (InterruptedException e) {
					}
				}
			}
			return this.tournamentCore.getPlayerList();
		}

		/**
		 * Determine whether the player list has been updated since the last time getPlayerList() was called.
		 * @return True if it has been updated.
		 */
		public boolean playerListUpdated() throws RemoteException {
			return this.tournamentCore.getPlayerListUpdated();
		}
	}
}
