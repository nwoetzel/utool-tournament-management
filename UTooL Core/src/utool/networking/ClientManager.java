package utool.networking;

import java.io.IOException;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.util.concurrent.ExecutionException;

import utool.core.Core;

import android.os.AsyncTask;

/**
 * A socket wrapper for UTooL messages on the client side
 * @author Cory
 *
 */
public class ClientManager extends SocketWrapper {
	
	/**
	 * The connection to the server
	 */
	private SocketInfo connection;
	
	/**
	 * Storage variable for connection exceptions
	 */
	private IOException connectionException = null;
	
	/**
	 * The address of the server
	 */
	private InetAddress address;
	
	/**
	 * The port number on the server
	 */
	private int port;
	
	/**
	 * Connect to a tournament server
	 * @param address The IP address of the server
	 * @param port The port number on the server
	 * @param tournamentCore The tournament core to use
	 * @throws IOException Thrown when the connection has failed
	 */
	public ClientManager(InetAddress address, int port, Core tournamentCore) throws IOException{
		super(tournamentCore, true);
		
		this.address = address;
		this.port = port;
		
		SocketConnect s = new SocketConnect();
		s.execute();
		try {
			s.get();
		} catch (InterruptedException e) {
		} catch (ExecutionException e) {
		}
		if (connectionException != null){
			throw connectionException;
		}
	}
	
	/**
	 * Send data to the socket
	 * @param data The data to send
	 * @throws IOException Thrown on socket error
	 */
	public synchronized void send(byte[] data) throws IOException{
		connection.send(data);
	}
	
	/**
	 * Close the socket
	 */
	public void close(){
		try {
			connection.close();
		} catch (IOException e) {
		}
	}
	
	/**
	 * Private class for connecting a socket on a new thread, required for Android 3.0+
	 * @author Cory
	 *
	 */
	private class SocketConnect extends AsyncTask<Void, Void, Void> {

		@Override
		protected Void doInBackground(Void... arg0) {
			try {
				Socket conn = new Socket();
				conn.connect(new InetSocketAddress(address, port), 5000);
				//Send initial message
				byte[] initMessage = new byte[5];
				initMessage[0] = 'U';
				initMessage[1] = 'T';
				initMessage[2] = 'o';
				initMessage[3] = 'o';
				initMessage[4] = 'L';
				conn.getOutputStream().write(initMessage);
				connection = new SocketInfo(conn);
				connection.startReceive();
			} catch (IOException e) {
				connectionException = e;
			}
			return null;
		}
	}
}
