package utool.networking.packet;

import java.io.StringReader;
import java.io.StringWriter;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlSerializer;

import utool.networking.XmlMessageTypeException;

import android.content.Intent;
import android.util.Xml;

/**
 * PluginStartMessage is a packet class sent and received over the network for starting a tournament plugin
 * @author Cory
 *
 */
public class PluginStartMessage implements IXmlMessage {
	/**
	 * XML root tag
	 */
	private static final String ROOT_TAG = "utool_plugin_start";

	/**
	 * XML package name attribute name
	 */
	private static final String PACKAGE_NAME_ATTRIB = "package_name";

	/**
	 * XML class name attribute name
	 */
	private static final String CLASS_NAME_ATTRIB = "class_name";

	/**
	 * Package name of the plugin
	 */
	private String packageName;
	
	/**
	 * Class name of the plugin activity
	 */
	private String className;

	/**
	 * Create a new PluginStartMessage
	 * @param packageName The package name of the plugin
	 * @param className The class name of the plugin activity
	 */
	public PluginStartMessage(String packageName, String className){
		this.packageName = packageName;
		this.className = className;
	}

	/**
	 * Read a PluginStartMessage from a string
	 * @param message The XML string containing the message
	 * @throws XmlMessageTypeException When not a PluginStartMessage
	 */
	public PluginStartMessage(String message) throws XmlMessageTypeException{
		decodeMessage(message);
	}
	
	/**
	 * Get an intent for this plugin
	 * @return Plugin's intent
	 */
	public Intent getIntent(){
		Intent intent = new Intent(Intent.ACTION_MAIN);
        intent.setClassName(packageName, className);
        
        return intent;
	}

	/**
	 * Decode XML to this object
	 * @param xml String to read from
	 * @throws XmlMessageTypeException When not a PluginStartMessage
	 */
	private void decodeMessage(String xml) throws XmlMessageTypeException {		
		StringReader input = new java.io.StringReader(xml);
		try{
			XmlPullParser parser = Xml.newPullParser();
			parser.setFeature(XmlPullParser.FEATURE_PROCESS_NAMESPACES, false);
			parser.setInput(input);
			parser.nextTag();
			
			if (!parser.getName().equals(ROOT_TAG)){
				throw new XmlMessageTypeException("Not a plugin start message: " + parser.getName());
			}

			for (int i = 0; i < parser.getAttributeCount(); i++){
				String attrib = parser.getAttributeName(i);
				if (attrib.equalsIgnoreCase(PACKAGE_NAME_ATTRIB)){
					packageName = parser.getAttributeValue(i);
				} else if (attrib.equalsIgnoreCase(CLASS_NAME_ATTRIB)){
					className = parser.getAttributeValue(i);
				}
			}

		} catch (Exception e) {
			if (e instanceof XmlMessageTypeException){
				throw (XmlMessageTypeException)e;
			}
		} finally {
			try {
				input.close();
			} catch (Exception e) {
			}
		}
	}

	/**
	 * Get the XML for this object
	 * @return XML string
	 */
	public String getXml() {
		XmlSerializer xmlSerializer = Xml.newSerializer();
		StringWriter writer = new StringWriter();

		//Use writer as the output
		try {
			xmlSerializer.setOutput(writer);


			//Start the document
			xmlSerializer.startDocument("UTF-8", true);
			xmlSerializer.startTag("", ROOT_TAG);

			//Write attributes
			xmlSerializer.attribute("", PACKAGE_NAME_ATTRIB, packageName);
			xmlSerializer.attribute("", CLASS_NAME_ATTRIB, className);

			//End the document
			xmlSerializer.endTag("", ROOT_TAG);
			xmlSerializer.endDocument();
		} catch (Exception e) {
		}
		//Write the document to xmlDoc
		return writer.toString();
	}

	@Override
	public boolean isOfMessageType(String xml) {
		return isPluginStartMessage(xml).isOfMessageType();
	}
	
	/**
	 * Attempt to decode the message using this class
	 * @param xml The message string
	 * @return A DecodedMessageContainer, possible containing a decoded message
	 */
	public static IXmlMessage.DecodedMessageContainer<PluginStartMessage> isPluginStartMessage(String xml) {
		DecodedMessageContainer<PluginStartMessage> c = new DecodedMessageContainer<PluginStartMessage>();
		try {
			PluginStartMessage msg = new PluginStartMessage(xml);
			c.setMessage(msg);
		} catch (Exception e){
		}
		return c;
	}

}
