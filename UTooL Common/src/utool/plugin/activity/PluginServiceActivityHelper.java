package utool.plugin.activity;

import java.util.HashMap;

import utool.plugin.IUTooLCore;
import android.app.Activity;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.IBinder;
import android.os.RemoteException;

/**
 * Plugin activity helper for any activity that needs direct access to the UTooL Service
 * @author Cory
 *
 */
public class PluginServiceActivityHelper extends PluginCommonActivityHelper {
	
	/**
	 * Message for when the mICore service connection isn't ready yet
	 */
	public static final String SERVICE_UNAVAILABLE_EXCEPTION_MESSAGE = "Service connection unavailable. Do not call this method before runOnServiceConnection() has started executing.";
	
	/**
	 * IPluginServiceActivity reference to the activity
	 */
	private IPluginServiceActivity iActivity;
	
	/**
	 * HashMap for mICore instances
	 */
	private static HashMap<Long, IUTooLCore> mICoreConnections = new HashMap<Long, IUTooLCore>();
	
	/**
	 * HashMap for mConnection instances
	 */
	private static HashMap<Long, MConnection> mConnections = new HashMap<Long, PluginServiceActivityHelper.MConnection>();

	/**
	 * Constructor for PluginServiceActivityHelper
	 * @param activity The activity that wishes to establish a service connection
	 * @param iActivity The same activity as an IPluginServiceActivity
	 */
	public PluginServiceActivityHelper(Activity activity, IPluginServiceActivity iActivity) {
		super(activity);
		this.iActivity = iActivity;
		
		//Connect to the plugin core service
		MConnection conn = mConnections.get(this.getTournamentId());
		if (conn == null){
			Intent serviceBindIntent = new Intent(UTOOL_SERVICE_NAME);
			serviceBindIntent.putExtra("tournamentId", getTournamentId());
			mConnection = new MConnection();
			activity.bindService(serviceBindIntent, mConnection, Context.BIND_AUTO_CREATE);
		} else {
			mConnection = conn;
			mICore = mICoreConnections.get(this.getTournamentId());
		}
	}
	
	/**
	 * This is to be called when there is a message to send to the core
	 * @param s the message in xml format to send
	 * @return true if sent
	 */
	public boolean sendMessage(String s)
	{
		try 
		{
			mICore.send(s);
			return true;
		} catch (RemoteException e) 
		{
			e.printStackTrace();
			return false;
		}
	}
	
	/**
	 * Unbind the service connection. Run this when you are done with the service.
	 * Usually this is run in onDestroy()
	 */
	public void unbindService(){
		try{
			activity.unbindService(mConnection);
			mICoreConnections.remove(this.getTournamentId());
			mConnections.remove(this.getTournamentId());
		} catch (Exception e) {}
	}
	
	/**
	 * The reference to the UTooLCore Service instance for this plugin
	 */
	public volatile IUTooLCore mICore;
	
	/**
	 * Current mConnection instance
	 */
	private MConnection mConnection;
	
	private IUTooLCore mICore(){
		return mICore;
	}
	
	/**
	 * The connection to the UTooLCore Service instance for this plugin
	 */
	private class MConnection implements ServiceConnection 
	{
		//Called when the connection with the service is established
		public void onServiceConnected(ComponentName className, IBinder service) {
			//This gets an instance of the IUTooLCore interface, which we can use to call on the service
			mICore = IUTooLCore.Stub.asInterface(service);

			mConnections.put(PluginServiceActivityHelper.this.getTournamentId(), PluginServiceActivityHelper.this.mConnection);
			mICoreConnections.put(PluginServiceActivityHelper.this.getTournamentId(), mICore);
			
			//Run code provided by the extending class
			iActivity.runOnServiceConnected();
		}

		//Called when the connection with the service disconnects unexpectedly
		public void onServiceDisconnected(ComponentName className) {
			iActivity.runOnServiceDisconnected();
		}
	};

}
