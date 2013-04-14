package utool.networking.packet;

import java.io.StringReader;
import java.io.StringWriter;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlSerializer;

import android.util.Xml;

public class CoreMessage {
        /**
         * XML root tag
         */
        private static final String ROOT_TAG = "utool_core";

        /**
         * XML package name attribute name
         */
        private static final String MESSAGE_TYPE_ATTRIB = "message_type";

        private int messageType;

        //message types
        public static final int MESSAGE_TYPE_CORE = 0;
        public static final int MESSAGE_TYPE_PLUGIN = 1;

        private String payload;

        public CoreMessage(String message){
                decodeMessage(message);
        }

        public CoreMessage(int messageType, String payload) {
                this.messageType = messageType;
                this.payload = payload;
        }

        public int getMessageType(){
                return messageType;
        }

        public String getPayload(){
                return payload;
        }

        /**
         * Decode XML to this object
         * @param xml String to read from
         */
        private void decodeMessage(String xml) {                
                StringReader input = new java.io.StringReader(xml);
                try{
                        XmlPullParser parser = Xml.newPullParser();
                        parser.setFeature(XmlPullParser.FEATURE_PROCESS_NAMESPACES, false);
                        parser.setInput(input);
                        parser.nextTag();

                        for (int i = 0; i < parser.getAttributeCount(); i++){
                                String attrib = parser.getAttributeName(i);
                                if (attrib.equalsIgnoreCase(MESSAGE_TYPE_ATTRIB)){
                                        messageType = Integer.parseInt(parser.getAttributeValue(i));
                                }
                        }

                        payload = parser.nextText();

                } catch (Exception e) {
                } finally {
                        try {
                                input.close();
                        } catch (Exception e) {
                        }
                }
        }

        /**
         * Get this message's XML
         * @return XML string
         */
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

                        //Set the message type
                        xmlSerializer.attribute("", MESSAGE_TYPE_ATTRIB, Integer.toString(this.messageType));

                        //Write the payload
                        xmlSerializer.text(payload);

                        //End the document
                        xmlSerializer.endTag("", ROOT_TAG);
                        xmlSerializer.endDocument();
                        xml = writer.toString();
                } catch (Exception e) { }
                return xml;
        }
}