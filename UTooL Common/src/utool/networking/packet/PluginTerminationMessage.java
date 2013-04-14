package utool.networking.packet;

import java.io.IOException;
import java.io.StringReader;
import java.io.StringWriter;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;
import org.xmlpull.v1.XmlSerializer;

import utool.networking.XmlMessageTypeException;
import android.util.Xml;

/**
 * This class handles encoding and decoding messages used to signal plugin termination
 * @author Cory
 *
 */
public class PluginTerminationMessage implements IXmlMessage {
	
	/**
	 * XML root tag
	 */
	private static final String ROOT_TAG = "utool_terminate_plugin";
		
	/**
	 * Read a received PluginTerminationMessage
	 * @param message The XML string of the message
	 * @throws XmlMessageTypeException On invalid message type
	 */
	public PluginTerminationMessage(String message) throws XmlMessageTypeException{
		decodeMessage(message);
	}
	
	/**
	 * Constructor for player list request message
	 */
	public PluginTerminationMessage(){
		
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

			//End the document
			xmlSerializer.endTag("", ROOT_TAG);
			xmlSerializer.endDocument();
			xml = writer.toString();
		} catch (Exception e) {	}
		return xml;
	}
	
	@Override
	public boolean isOfMessageType(String xml){
		StringReader input = new java.io.StringReader(xml);

		XmlPullParser parser = Xml.newPullParser();
		try {
			parser.setFeature(XmlPullParser.FEATURE_PROCESS_NAMESPACES, false);

			parser.setInput(input);
			parser.nextTag();

			if (parser.getName().equals(ROOT_TAG)){
				return true;
			}
		} catch (XmlPullParserException e) {
		} catch (IOException e) {
		}
		return false;
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
				throw new XmlMessageTypeException("Not a plugin termination message: " + parser.getName());
			}
			
		} catch (XmlPullParserException e) {
			throw new XmlMessageTypeException("Not a plugin termination message");
		} catch (IOException e) {
		} finally {
			try {
				input.close();
			} catch (Exception e) {
			}
		}
	}
	
	/**
	 * Determine if the message is a PluginTerminationMessage
	 * @param xml The message to check
	 * @return True if it is a PluginTerminationMessage, otherwise false
	 */
	public static boolean isPluginTerminationMessage(String xml){
		boolean result = false;
		try {
			@SuppressWarnings("unused")
			PluginTerminationMessage message = new PluginTerminationMessage(xml);
			result = true;
		} catch (XmlMessageTypeException e) {
			result = false;
		}
		return result;
	}
}
