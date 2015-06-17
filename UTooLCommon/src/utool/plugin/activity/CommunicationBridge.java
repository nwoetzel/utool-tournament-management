package utool.plugin.activity;

import utool.plugin.activity.IPluginServiceActivity;


/**
 * This class is used to capture output of the outgoing command handler and send it to the main activity
 * if there is one registered
 * @author waltzm
 *
 */
public class CommunicationBridge 
{

	/**
	 * Holds a reference to the main activity that has the connection to the core
	 */
	protected IPluginServiceActivity mainActivity;

	/**
	 * Last XML to be sent out
	 */
	protected String lastXML="";
	
	/**
	 * Notifies this class when a new message is to be sent out to the server
	 * This class will notify the main activity if it isn't null
	 * @param message the xml to send
	 * @return true if send is successful
	 */
	public boolean sendMessage(String message) 
	{

		//send message to log
		//Log.i("Bridge", message);
		this.lastXML = message;

		if(mainActivity!=null)
		{
			mainActivity.sendMessage(message);

			return true;
		}


		return false;
	}

	/**
	 * Getter for the last xml sent
	 * @return the lastXML
	 */
	public String getLastXML() 
	{
		return lastXML;
	}

	/**
	 * Getter for the main activity with the core connection
	 * @return the mainActivity
	 */
	public IPluginServiceActivity getMainActivity() 
	{
		return mainActivity;
	}
	
	/**
	 * Sets the main activity
	 * @param newActivity The main activity
	 */
	public void setMainActivity(IPluginServiceActivity newActivity){
		mainActivity = newActivity;
	}

	/**
	 * Clears the last message. Sets it to null
	 */
	public void clearLastMessage() 
	{
		this.lastXML = null;
		
	}




}
