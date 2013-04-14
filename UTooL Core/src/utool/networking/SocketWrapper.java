package utool.networking;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.net.Socket;
import java.nio.ByteBuffer;
import java.util.Arrays;
import java.util.UUID;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.zip.CRC32;

import android.util.Log;

import utool.core.AbstractTournament;
import utool.core.Core;
import utool.networking.packet.HostInformation;
import utool.networking.packet.IXmlMessage;
import utool.networking.packet.PlayerMessage;
import utool.networking.packet.PluginStartMessage;
import utool.plugin.Player;

/**
 * Abstract wrapper class for socket management
 * @author Cory
 *
 */
public abstract class SocketWrapper {
	/**
	 * Tag used for logging
	 */
	private static final String LOG_TAG = "utool.core.SocketWrapper";

	/**
	 * Tournament core associated with this socket
	 */
	protected Core tournamentCore;

	/**
	 * True if this is a client socket
	 */
	private boolean isClient;

	/**
	 * Constructor
	 * @param tournamentCore The tournament core to use with this socket
	 * @param isClient Set to true for client sockets. This changes the behavior of socket disconnections.
	 */
	public SocketWrapper(Core tournamentCore, boolean isClient){
		this.tournamentCore = tournamentCore;
		this.isClient = isClient;
	}

	/**
	 * The unhandled data received from the sockets
	 */
	protected BlockingQueue<byte[]> receivedData = new LinkedBlockingQueue<byte[]>();

	/**
	 * Maximum message size. 20MiB
	 */
	public static final int MAX_LENGTH = 20 * 1024 * 1024;

	/**
	 * Receive data from the socket received data queue
	 * @return The oldest received data
	 */
	public byte[] receive(){
		byte[] data = null;
		try {
			data = receivedData.take();
		} catch (InterruptedException e) {
		}
		return data;
	}

	/**
	 * Read a message off a socket
	 * @param conn The socket to read from
	 * @return A byte array containing the message
	 * @throws IOException Thrown on socket error
	 */
	protected static byte[] read(Socket conn) throws IOException{
		byte[] crc32Buffer = new byte[8];
		ByteBuffer crcBufferWrapper = ByteBuffer.wrap(crc32Buffer);
		InputStream in = conn.getInputStream();

		//Read length CRC32
		int readCount = in.read(crc32Buffer);
		long lengthCrc32 = crcBufferWrapper.getLong();
		CRC32 crc32 = new CRC32();

		//Read the message length
		byte[] lenBuffer = new byte[4];
		ByteBuffer lenBufferWrapper = ByteBuffer.wrap(lenBuffer);
		readCount = in.read(lenBuffer);
		int length = lenBufferWrapper.getInt();

		//check CRC32		
		crc32.update(length);

		Log.d(LOG_TAG, "Receiving " + length + " with CRC32=" + crc32.getValue());
		if (length > MAX_LENGTH){
			Log.d(LOG_TAG, "Socket error. Message too large to receive: " + length);
			conn.close();
		}

		if (lengthCrc32 != crc32.getValue()){
			Log.e(LOG_TAG, "Length CRC32 doesn't match. Received: " + lengthCrc32 + " Calculated: " + crc32.getValue());
		}

		//read data CRC32
		readCount = in.read(crc32Buffer);


		if (conn.isInputShutdown() || readCount == -1){
			Log.d(LOG_TAG, "Socket is closed");
			throw new IOException("Socket is closed");
		}

		//Read the message data		
		java.io.ByteArrayOutputStream dataO = new ByteArrayOutputStream(length);
		byte[] buffer = new byte[512];
		int remaining = length;
		while (remaining > 0){
			if (buffer.length <= remaining){
				readCount = in.read(buffer);
				dataO.write(buffer, 0, readCount);
			} else {
				readCount = in.read(buffer, 0, remaining);
				dataO.write(buffer, 0, readCount);
			}
			if (readCount != -1){
				remaining -= readCount;
			} else {
				throw new IOException("Socket is closed");
			}
		}
		byte[] data = dataO.toByteArray();
		dataO.close();

		crcBufferWrapper.rewind();
		long dataCrc32 = crcBufferWrapper.getLong();

		//check data crc32
		crc32.reset();
		crc32.update(data);

		if (dataCrc32 != crc32.getValue()){
			Log.e(LOG_TAG, "Data CRC32 doesn't match. Received: " + dataCrc32 + " Calculated: " + crc32.getValue());
		}
		return data;
	}

	/**
	 * Write a message to the stream
	 * @param data The message to send
	 * @param conn The socket to write to
	 * @throws IOException Thrown on socket error
	 */
	protected static void write(byte[] data, Socket conn) throws IOException{
		if (data.length > MAX_LENGTH){
			Log.d(LOG_TAG, "Message too large to send: " + data.length);
			return;
		}
		//Get the length of the data as a byte array, so clients know how much data to receive
		ByteBuffer b = ByteBuffer.allocate(8 + 8 + 4 + data.length);

		//length CRC32 + length values
		CRC32 crc32 = new CRC32();
		crc32.update(data.length);
		b.putLong(crc32.getValue());
		b.putInt(data.length);

		//data CRC32 + data values
		crc32.reset();
		crc32.update(data);
		b.putLong(crc32.getValue());
		b.put(data);

		Log.d(LOG_TAG, "Sending " + data.length + " with CRC32=" + crc32.getValue());

		//Write the data
		OutputStream out = conn.getOutputStream();
		synchronized (conn) {
			//out.write(crc32buf.array());
			//out.write(length);
			//out.write(data);
			out.write(b.array());
			out.flush();
		}
	}

	/**
	 * Send data directly to the plugin instance running on this device
	 * @param data The data to send to the plugin
	 */
	public void sendToPlugin(byte[] data){
		try {
			receivedData.put(data);
		} catch (InterruptedException e) {
		}
	}

	/**
	 * Close the socket
	 */
	public abstract void close();

	/**
	 * Protected class for managing socket information
	 * @author Cory
	 *
	 */
	protected class SocketInfo{
		/**
		 * Reception thread
		 */
		private Thread inputThread;
		/**
		 * Wrapped socket
		 */
		protected Socket socket;
		/**
		 * Player UUID associated with this socket
		 */
		private UUID playerId;

		/**
		 * Constructor
		 * @param socket The socket to wrap
		 */
		public SocketInfo(Socket socket){
			this.socket = socket;
		}

		/**
		 * Start the reception thread. Does nothing on subsequent calls.
		 */
		public void startReceive(){
			if (inputThread == null){
				StreamInput input = new StreamInput(this);
				inputThread = new Thread(input);
				inputThread.start();
			}
		}

		/**
		 * Get the player id associated with this socket, if received.
		 * @return Player UUID
		 */
		public UUID getPlayerId(){
			return playerId;
		}

		/**
		 * Process messages received
		 * @param data Byte[] to process
		 */
		private void processReceivedData(byte[] data){
			try {
				//Process core messages here
				String message;
				boolean putData = true;
				try {
					message = new String(data, "UTF-8");
					//Process HostInformation messages
					IXmlMessage.DecodedMessageContainer<HostInformation> hostInfo = HostInformation.isHostInformationMessage(message);
					if (hostInfo.isOfMessageType() && isClient){
						tournamentCore.setTournamentName(hostInfo.getMessage().getTournamentName());
						try {
							tournamentCore.setUUID(hostInfo.getMessage().getTournamentUUID());
						} catch (Exception e) {
							//do nothing
						}
					}

					else{
						IXmlMessage.DecodedMessageContainer<PlayerMessage> playerMessageContainer = PlayerMessage.isPlayerMessage(message);
						if (playerMessageContainer.isOfMessageType()){
							//Process PlayerRegisterMessage
							try{
								PlayerMessage prm = playerMessageContainer.getMessage();
								if (prm.getMessageType() == PlayerMessage.MessageType.PlayerRegister){
									Player p = prm.getPlayer();
									playerId = p.getUUID();
									p.setPermissionsLevel(Player.PARTICIPANT);
									tournamentCore.addPlayer(p);
								} else if (prm.getMessageType() == PlayerMessage.MessageType.PlayerList && tournamentCore.getTournamentLocation() != AbstractTournament.TournamentLocationEnum.Local){
									//Not the host device, so OK to replace player list
									tournamentCore.setPlayerList(prm.getPlayerList());
									//putData = false;
								} else if (prm.getMessageType() == PlayerMessage.MessageType.PlayerList){
									//Send the player list
									PlayerMessage playerMessage = new PlayerMessage(tournamentCore.getPlayerList());
									String playerMessageXml = playerMessage.getXml();
									send(playerMessageXml.getBytes());
									putData = false;
								}
							} catch (Exception e){
								//Other exception occurred in XML, don't crash the networking code because of it
								Log.e(LOG_TAG, e.getMessage() + "\n" + Arrays.toString(e.getStackTrace()));
							}
						}
					}
					//Process PluginStartMessage if not yet received
					if (tournamentCore.getPluginStartMessage() == null){
						try {
							PluginStartMessage psm = new PluginStartMessage(message);
							tournamentCore.setPluginStartMessage(psm);
						} catch (XmlMessageTypeException e) {
							//do nothing
						} catch (Exception e){
							//Other exception occurred in XML, don't crash the networking code because of it
							Log.e(LOG_TAG, e.getMessage() + "\n" + Arrays.toString(e.getStackTrace()));
						}
					}
				} catch (UnsupportedEncodingException e1) {
				} catch (IOException e) {
				}
				if (putData){
					receivedData.put(data);
				}
			} catch (InterruptedException e) {
			}
		}

		/**
		 * Send raw data on the socket
		 * @param data Byte[] to send
		 */
		public void send(byte[] data){
			if (data.length < 1024*1024*20){
				StreamOutput out = new StreamOutput(data, socket);
				Thread t = new Thread(out);
				t.start();
			} else {
				Log.d(LOG_TAG, "Large message attempted:" + data.length);
			}
		}

		/**
		 * Close the socket
		 * @throws IOException On socket error
		 */
		public void close() throws IOException{
			socket.close();
		}
	}

	/**
	 * Private class for sending data on a separate thread
	 * @author Cory
	 *
	 */
	protected class StreamOutput implements Runnable{
		/**
		 * The data to send
		 */
		private byte[] data;

		/**
		 * The socket to send out on
		 */
		private Socket conn;

		/**
		 * Create a StreamOutput Runnable object
		 * @param data The data to send
		 * @param conn The socket to send out on
		 */
		public StreamOutput(byte[] data, Socket conn){
			this.data = data;
			this.conn = conn;
		}


		public void run() {
			try {
				synchronized (conn) {
					write(data, conn);
				}
			} catch (Exception e) {
				Log.e(LOG_TAG, e.getMessage() + "\n" + Arrays.toString(e.getStackTrace()));
			}
		}
	}

	/**
	 * Private class for receiving data on a separate thread
	 * @author Cory
	 *
	 */
	protected class StreamInput implements Runnable{
		/**
		 * The socket to receive in on
		 */
		private SocketInfo socketInfo;

		/**
		 * Create a StreamInput Runnable object
		 * @param socketInfo The socket to receive in on
		 */
		public StreamInput(SocketInfo socketInfo){
			this.socketInfo = socketInfo;
		}

		public void run() {
			try {
				//Receive data on the socket and process
				while (socketInfo.socket != null && !socketInfo.socket.isInputShutdown()){
					byte[] data = read(socketInfo.socket);
					socketInfo.processReceivedData(data);
				}
			} catch (Exception e) {
				try {
					socketInfo.close();
				} catch (IOException e1) {
					Log.d("SocketWrapperInput", e1.getMessage());
				}
				if (isClient){
					try {
						receivedData.put("-1".getBytes());
					} catch (InterruptedException e1) {
					}
				}
			}
		}
	}
}
