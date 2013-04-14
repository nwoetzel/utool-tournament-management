package utool.plugin.activity;

/**
 * This interface provides a way for activities to hook into the service connection established
 * by {@link PluginServiceActivityHelper} and {@link PluginMainActivityHelper}
 * @author Cory
 *
 */
public interface IPluginServiceActivity {
	/**
	 * Code to run right after the service connects. Put any code that immediately depends on the service here,
	 * such as network code.
	 */
	public void runOnServiceConnected();
	
	/**
	 * Code to run if the service unexpectedly disconnects
	 */
	public void runOnServiceDisconnected();
	
	/**
	 * This is to be called when there is a message to send to the core
	 * @param message the message in xml format to send
	 * @return true if sent
	 */
	public boolean sendMessage(String message);

}
