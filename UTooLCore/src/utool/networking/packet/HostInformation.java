package utool.networking.packet;

import java.io.StringReader;
import java.io.StringWriter;
import java.net.InetAddress;
import java.util.UUID;
import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlSerializer;
import utool.core.AbstractTournament;
import utool.networking.XmlMessageTypeException;
import android.util.Xml;

/**
 * HostInformation is a packet class sent and received over the network for server discovery.
 * @author Cory
 *
 */
public class HostInformation extends AbstractTournament implements IXmlMessage{
	/**
	 * XML root tag
	 */
	private static final String ROOT_TAG = "HostInformation";

	/**
	 * XML tag for the server's name
	 */
	private static final String SERVER_NAME_TAG = "server_name";

	/**
	 * XML tag for the server's port
	 */
	private static final String SERVER_PORT_TAG = "server_port";
	
	/**
	 * XML tag for the tournament's UUID
	 */
	private static final String UUID_TAG = "tournament_uuid";

	/**
	 * Decode a HostInformation packet received from the network.
	 * @param serverAddress The address the packet was broadcast from. This is the server's address.
	 * @param data The packet data to decode. This data is XML formatted.
	 * @param length The length of the string data in data[]
	 */
	public HostInformation(InetAddress serverAddress, byte[] data, int length){
		this.serverAddress = serverAddress;
		//InputStream input = new ByteArrayInputStream(data);
		String xml = new String(data, 0, length);
		this.setTournamentLocation(TournamentLocationEnum.RemoteDiscovered);

		try {
			decodeMessage(xml);
		} catch (XmlMessageTypeException e) {
			//do nothing
		}
		lastSeen = System.nanoTime();
	}

	/**
	 * For use by the broadcasting server. Create an instance of HostInformation using the server's information.
	 * @param serverName The name of the server/tournament instance to advertise.
	 * @param tournamentPort The TCP port number for clients to connect to.
	 * @param tournamentUUID The UUID of the tournament
	 */
	public HostInformation(String serverName, int tournamentPort, UUID tournamentUUID){
		this.tournamentName = serverName;
		this.serverPort = tournamentPort;
		this.tournamentUUID = tournamentUUID;
	}
	
	/**
	 * Decode XML to this object
	 * @param xml String to read from
	 * @throws XmlMessageTypeException When not a HostInformation message
	 */
	private void decodeMessage(String xml) throws XmlMessageTypeException {		
		StringReader input = new java.io.StringReader(xml);
		try{
			XmlPullParser parser = Xml.newPullParser();
			parser.setFeature(XmlPullParser.FEATURE_PROCESS_NAMESPACES, false);
			parser.setInput(input);
			parser.nextTag();
			
			if (!parser.getName().equals(ROOT_TAG)){
				throw new XmlMessageTypeException("Not a host information message: " + parser.getName());
			}

			int next = parser.next();
			while (next != XmlPullParser.END_DOCUMENT) {
				String name = parser.getName();
				if (parser.getEventType() == XmlPullParser.START_TAG){
					if (name != null && name.equalsIgnoreCase(SERVER_NAME_TAG)){
						this.tournamentName = parser.nextText();
					} else if (name != null && name.equalsIgnoreCase(SERVER_PORT_TAG)){
						this.serverPort = Integer.parseInt(parser.nextText());
					} else if (name != null && name.equalsIgnoreCase(UUID_TAG)){
						this.tournamentUUID = UUID.fromString(parser.nextText());
					}
				}
				next = parser.next();
			}

		} catch (Exception e) {
			if (e instanceof XmlMessageTypeException){
				throw (XmlMessageTypeException) e;
			}
		} finally {
			try {
				input.close();
			} catch (Exception e) {
			}
		}
	}

	
	/**
	 * For use on connected server information update.
	 * @param xml The message received
	 * @throws XmlMessageTypeException When not a HostInformation message
	 */
	public HostInformation(String xml) throws XmlMessageTypeException{
		decodeMessage(xml);
	}
	
	/**
	 * For use by direct connect functionality
	 * @param serverName The name of the server
	 * @param serverAddress The server's address
	 * @param tournamentPort The tournament's port
	 */
	public HostInformation(String serverName, InetAddress serverAddress, int tournamentPort){
		this.tournamentName = serverName;
		this.serverAddress = serverAddress;
		this.serverPort = tournamentPort;
	}
	
	/**
	 * Get the tournament's UUID
	 * @return Tournament's UUID
	 */
	public UUID getTournamentUUID(){
		return tournamentUUID;
	}
	
	/**
	 * Has this host been seen recently? 
	 * @return True if seen in the last 1 second
	 */
	@Override
	public boolean isRecent(){
		long now = System.nanoTime();
		long timeout = now - lastSeen;
		double seconds = timeout / 1.0E09;
		
		if (seconds > 1){
			return false;
		} else {
			return true;
		}
	}

	@Override
	public String getXml(){
		XmlSerializer xmlSerializer = Xml.newSerializer();
		StringWriter writer = new StringWriter();

		//Use writer as the output
		try {
			xmlSerializer.setOutput(writer);

			//Start the document
			xmlSerializer.startDocument("UTF-8", true);
			xmlSerializer.startTag("", ROOT_TAG);

			//Write the server name
			xmlSerializer.startTag("", SERVER_NAME_TAG);
			xmlSerializer.text(this.tournamentName);
			xmlSerializer.endTag("", SERVER_NAME_TAG);

			//Write the server port
			xmlSerializer.startTag("", SERVER_PORT_TAG);
			xmlSerializer.text(String.valueOf(serverPort));
			xmlSerializer.endTag("", SERVER_PORT_TAG);

			//Write the tournament UUID
			xmlSerializer.startTag("", UUID_TAG);
			xmlSerializer.text(tournamentUUID.toString());
			xmlSerializer.endTag("", UUID_TAG);

			//End the document
			xmlSerializer.endTag("", ROOT_TAG);
			xmlSerializer.endDocument();

			//Write the document to xmlDoc
		} catch (Exception e){
		}
		return writer.toString();
	}

	/**
	 * Get a byte array containing the XML packet.
	 * @return A byte array to be sent over the network.
	 * @throws Exception Thrown on error in creating XML document 
	 */
	public byte[] getPacketData() throws Exception{
		String xml = getXml();
		return xml.getBytes("UTF-8");
	}
	
	@Override
	public String toString(){
		return this.tournamentName + "@" + this.serverAddress;
	}
	
	/**
	 * Attempt to decode the message using this class
	 * @param xml The message string
	 * @return A DecodedMessageContainer, possible containing a decoded message
	 */
	public static IXmlMessage.DecodedMessageContainer<HostInformation> isHostInformationMessage(String xml) {
		DecodedMessageContainer<HostInformation> c = new DecodedMessageContainer<HostInformation>();
		try {
			HostInformation h = new HostInformation(xml);
			c.setMessage(h);
		} catch (Exception e){
		}
		return c;
	}

	@Override
	public boolean isOfMessageType(String xml) {
		return isHostInformationMessage(xml).isOfMessageType();
	}
}
