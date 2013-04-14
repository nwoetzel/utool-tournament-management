package utool.plugin.dummy;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.StringReader;
import java.io.StringWriter;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;
import org.xmlpull.v1.XmlSerializer;

import utool.networking.XmlMessageTypeException;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Xml;

/**
 * DummyMessage is a packet class sent and received over the network for dummy plugin data
 * @author Cory
 *
 */
public class DummyMessage {
	/**
	 * XML root tag
	 */
	private static final String ROOT_TAG = "utool_dummy_message";
	
	/**
	 * XML package name attribute name
	 */
	private static final String TEXT_NAME_ATTRIB = "text_data";

	/**
	 * XML class name attribute name
	 */
	private static final String IMAGE_NAME_ATTRIB = "image_data";
	
	/**
	 * Text data for the message
	 */
	private String textData;
	
	/**
	 * Image data for the message
	 */
	private byte[] imageData;
	
	/**
	 * Construct a new DummyMessage for sending.
	 * @param text The text data to send. Set to null to exclude.
	 * @param image The image data to send. Set to null to exclude.
	 */
	public DummyMessage(String text, Bitmap image){
		this.textData = text;
		
		if (image != null){
			ByteArrayOutputStream stream = new ByteArrayOutputStream();
			image.compress(Bitmap.CompressFormat.JPEG, 50, stream);
			imageData = stream.toByteArray();
		}
	}
	
	/**
	 * Construct a DummyMessage from a received message
	 * @param message The received XML message.
	 * @throws XmlMessageTypeException Thrown on an incorrect message type
	 */
	public DummyMessage(String message) throws XmlMessageTypeException{
		decodeMessage(message);
	}
	
	/**
	 * Decode XML to this object
	 * @param xml String to read from
	 * @throws XmlMessageTypeException Thrown on an incorrect message type
	 */
	private void decodeMessage(String xml) throws XmlMessageTypeException {		
		StringReader input = new java.io.StringReader(xml);
		try{
			XmlPullParser parser = Xml.newPullParser();
			parser.setFeature(XmlPullParser.FEATURE_PROCESS_NAMESPACES, false);
			parser.setInput(input);
			parser.nextTag();
			
			if (!parser.getName().equals(ROOT_TAG)){
				throw new XmlMessageTypeException("Not a dummy message: " + parser.getName());
			}

			for (int i = 0; i < parser.getAttributeCount(); i++){
				String attrib = parser.getAttributeName(i);
				if (attrib.equalsIgnoreCase(TEXT_NAME_ATTRIB)){
					textData = parser.getAttributeValue(i);
				} else if (attrib.equalsIgnoreCase(IMAGE_NAME_ATTRIB)){
					imageData = android.util.Base64.decode(parser.getAttributeValue(i), android.util.Base64.DEFAULT);
				}
			}
		} catch (XmlPullParserException e) {
		} catch (IOException e) {
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
			if (textData != null) {
				xmlSerializer.attribute("", TEXT_NAME_ATTRIB, textData);
			}
			if (imageData != null) {
				xmlSerializer.attribute("", IMAGE_NAME_ATTRIB, android.util.Base64.encodeToString(imageData, android.util.Base64.DEFAULT));
			}
			//End the document
			xmlSerializer.endTag("", ROOT_TAG);
			xmlSerializer.endDocument();
		} catch (Exception e) {
		}
		//Write the document to xmlDoc
		return writer.toString();
	}
	
	/**
	 * Get the text data from the message
	 * @return String or null
	 */
	public String getTextData(){
		return textData;
	}
	
	/**
	 * Get the image data from the message
	 * @return Bitmap or null
	 */
	public Bitmap getImage(){
		if (imageData != null)
			return BitmapFactory.decodeByteArray(imageData, 0, imageData.length);
		return null;
	}
}
