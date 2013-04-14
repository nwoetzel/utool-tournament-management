package utool.networking.packet;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.StringReader;
import java.io.StringWriter;
import java.util.LinkedList;
import java.util.List;
import java.util.UUID;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;
import org.xmlpull.v1.XmlSerializer;

import utool.networking.XmlMessageTypeException;
import utool.plugin.Player;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Base64;
import android.util.Xml;

/**
 * This class handles encoding and decoding messages passed between cores for player registration
 * @author Cory
 *
 */
public class PlayerMessage implements IXmlMessage {
	
	/**
	 * XML root tag
	 */
	private static final String ROOT_TAG = "utool_player_data";
	
	/**
	 * Message type attribute
	 */
	private static final String MESSAGE_TYPE_ATTRIB = "type";
	
	/**
	 * XML player tag
	 */
	private static final String PLAYER_TAG = "player";
	
	/**
	 * XML player picture tag
	 */
	private static final String PICTURE_TAG = "picture_data";
	
	/**
	 * XML player attribute name
	 */
	private static final String PLAYER_NAME_ATTRIB = "player_name";
	
	/**
	 * XML player UUID attribute name
	 */
	private static final String PLAYER_UUID_ATTRIB = "player_uuid";
	
	/**
	 * XML player ghost attribute name
	 */
	private static final String PLAYER_IS_GHOST_ATTRIB = "player_is_ghost";
	
	/**
	 * The players for this message
	 */
	private List<Player> players = new LinkedList<Player>();
	
	/**
	 * The message type
	 */
	private MessageType messageType;
	
	/**
	 * Read a received PlayerRegisterMessage
	 * @param message The XML string of the message
	 * @throws XmlMessageTypeException On invalid message type
	 */
	public PlayerMessage(String message) throws XmlMessageTypeException{
		players = new LinkedList<Player>();
		decodeMessage(message);
	}
	
	/**
	 * Get the first player in this message
	 * @return Player object
	 */
	public Player getPlayer(){
		return players.get(0);
	}
	
	/**
	 * Get all players in the message
	 * @return The list of players in the message
	 */
	public List<Player> getPlayerList(){
		return players;
	}
	
	/**
	 * Get this message's type
	 * @return MessageType of the message
	 */
	public MessageType getMessageType(){
		return messageType;
	}

	/**
	 * Constructor for player registration message
	 * @param p The player to set
	 */
	public PlayerMessage(Player p) {
		this.messageType = MessageType.PlayerRegister;
		this.players = new LinkedList<Player>();
		players.add(p);
	}
	
	/**
	 * Constructor for player list message
	 * @param players The players to add
	 */
	public PlayerMessage(List<Player> players){
		this.messageType = MessageType.PlayerList;
		this.players = players;
	}
	
	/**
	 * Constructor for player list request message
	 */
	public PlayerMessage(){
		this.messageType = MessageType.PlayerList;
	}
	
	@Override
	public String getXml(){
		String xml = "";
		try{
			XmlSerializer xmlSerializer = Xml.newSerializer();
			StringWriter writer = new StringWriter();

			//Use writer as the output
			xmlSerializer.setOutput(writer);

			//Start the document
			xmlSerializer.startDocument("UTF-8", true);
			xmlSerializer.startTag("", ROOT_TAG);
			xmlSerializer.attribute("", MESSAGE_TYPE_ATTRIB, messageType.name());

			//Write the player(s)
			for (Player player: players){
				xmlSerializer.startTag("", PLAYER_TAG);
				xmlSerializer.attribute("", PLAYER_NAME_ATTRIB, player.getName());
				xmlSerializer.attribute("", PLAYER_UUID_ATTRIB, player.getUUID().toString());
				xmlSerializer.attribute("", PLAYER_IS_GHOST_ATTRIB, String.valueOf(player.isGhost()));

				//Write the picture data tag
				if (player.getPortrait() != null){
					xmlSerializer.startTag("", PICTURE_TAG);
					Bitmap image = player.getPortrait();
					ByteArrayOutputStream output = new ByteArrayOutputStream();
					Bitmap smallImage = Bitmap.createScaledBitmap(image, 120, 120, false);
					smallImage.compress(Bitmap.CompressFormat.JPEG, 80, output); //bm is the bitmap object
					if (smallImage != image){
						smallImage.recycle();
					}
					byte[] b = output.toByteArray();
					xmlSerializer.text(Base64.encodeToString(b, Base64.DEFAULT));
					xmlSerializer.endTag("", PICTURE_TAG);
				}

				xmlSerializer.endTag("", PLAYER_TAG);
			}


			//End the document
			xmlSerializer.endTag("", ROOT_TAG);
			xmlSerializer.endDocument();
			xml = writer.toString();
		} catch (Exception e) {	}
		return xml;
	}
	
	@Override
	public boolean isOfMessageType(String xml){
		return isPlayerMessage(xml).isOfMessageType();
	}
	
	/**
	 * Attempt to decode the message using this class
	 * @param xml The message string
	 * @return A DecodedMessageContainer, possible containing a decoded message
	 */
	public static IXmlMessage.DecodedMessageContainer<PlayerMessage> isPlayerMessage(String xml) {
		DecodedMessageContainer<PlayerMessage> c = new DecodedMessageContainer<PlayerMessage>();
		try {
			PlayerMessage msg = new PlayerMessage(xml);
			c.setMessage(msg);
		} catch (Exception e){
		}
		return c;
	}
	
	/**
	 * Decode an XML message to this object
	 * @param xml string to parse
	 * @throws XmlMessageTypeException On invalid message type
	 */
	private void decodeMessage(String xml) throws XmlMessageTypeException {

		StringReader input = new java.io.StringReader(xml);
		try{
			XmlPullParser parser = Xml.newPullParser();
			parser.setFeature(XmlPullParser.FEATURE_PROCESS_NAMESPACES, false);
			parser.setInput(input);
			parser.nextTag();

			if (!parser.getName().equals(ROOT_TAG)){
				throw new XmlMessageTypeException("Not a player message: " + parser.getName());
			}
			String messageType = parser.getAttributeValue("", MESSAGE_TYPE_ATTRIB);
			this.messageType = MessageType.valueOf(messageType);

			int next = parser.next();
			String playerName = "";
			UUID playerUUID = null;
			boolean playerIsGhost = false;
			Bitmap playerPicture = null;

			while (next != XmlPullParser.END_DOCUMENT) {
				String name = parser.getName();
				if (parser.getEventType() == XmlPullParser.START_TAG){
					if (name != null && name.equalsIgnoreCase(PLAYER_TAG)){
						for (int i = 0; i < parser.getAttributeCount(); i++){
							String attrib = parser.getAttributeName(i);
							if (attrib.equalsIgnoreCase(PLAYER_NAME_ATTRIB)){
								playerName = parser.getAttributeValue(i);
							} else if (attrib.equalsIgnoreCase(PLAYER_UUID_ATTRIB)){
								playerUUID = UUID.fromString(parser.getAttributeValue(i));
							} else if (attrib.equalsIgnoreCase(PLAYER_IS_GHOST_ATTRIB)){
								playerIsGhost = Boolean.valueOf(parser.getAttributeValue(i));
							}
						}
					} else if (name != null && name.equalsIgnoreCase(PICTURE_TAG)){
						byte[] image = Base64.decode(parser.nextText(), Base64.DEFAULT);
						playerPicture = BitmapFactory.decodeByteArray(image, 0, image.length);
					}
				} else if (parser.getEventType() == XmlPullParser.END_TAG){
					if (name.equalsIgnoreCase(PLAYER_TAG)){
						Player player = new Player(playerUUID, playerName);
						player.setGhost(playerIsGhost);
						player.setPortrait(playerPicture);
						players.add(player);
						playerPicture = null;
						playerUUID = null;
						playerName = "";
					}
				}
				next = parser.next();
			}

		} catch (XmlPullParserException e) {
			throw new XmlMessageTypeException("Not a player message");
		} catch (IOException e) {
		} finally {
			try {
				input.close();
			} catch (Exception e) {
			}
		}
	}
	
	/**
	 * Message type enum
	 *
	 */
	public enum MessageType{
		/**
		 * The message contains a player list
		 */
		PlayerList,
		/**
		 * The message is a player registration message
		 */
		PlayerRegister
	}
}
