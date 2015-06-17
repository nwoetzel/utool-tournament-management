package utool.networking.packet;

/**
 * Interface for methods common to all XML messages
 * @author Cory
 *
 */
public interface IXmlMessage {
	/**
	 * Get the XML data for this message
	 * @return XML String
	 */
	public String getXml();
	
	/**
	 * Check if a message can be handled by this class.
	 * @param xml XML string data
	 * @return True if the message can be handled, otherwise false.
	 */
	public boolean isOfMessageType(String xml);
	
	/**
	 * Container class for returning decoded messages
	 * @author Cory
	 *
	 * @param <T> The IXmlMessage class type that this container will hold
	 */
	public class DecodedMessageContainer<T extends IXmlMessage>{
		/**
		 * The decoded message
		 */
		private T message;
		
		/**
		 * True if message decoded properly
		 */
		private boolean isOfMessageType = false;
		
		/**
		 * Set the message in this container
		 * @param message Message to set
		 */
		public void setMessage(T message){
			this.message = message;
			this.isOfMessageType = true;
		}
		
		/**
		 * Get the message in this container
		 * @return Message object
		 */
		public T getMessage(){
			return message;
		}
		
		/**
		 * Get whether this container contains a valid message
		 * @return True if it does, otherwise false
		 */
		public boolean isOfMessageType(){
			return isOfMessageType;
		}
	}
}
