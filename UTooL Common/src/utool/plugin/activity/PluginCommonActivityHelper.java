package utool.plugin.activity;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;

/**
 * Plugin activity helper for common UTooL functionality.
 * @author Cory
 *
 */
public class PluginCommonActivityHelper {
	/**
	 * String used for core service discovery
	 * @since 10/4/2012
	 */
	public static final String UTOOL_SERVICE_NAME = "utool.core.UTooLCoreService";
	
	/**
	 * Action for calling tournament configuration screen in core
	 */
	public static final String UTOOL_TOURNAMENT_CONFIG_ACTION = "utool.core.intent.TOURNAMENT_CONFIG";
	
	/**
	 * Package name tournament configuration screen is in
	 */
	public static final String UTOOL_TOURNAMENT_CONFIG_PACKAGE = "utool.core";
	
	/**
	 * Class name of tournament configuration screen
	 */
	public static final String UTOOL_TOURNAMENT_CONFIG_CLASS = "utool.core.TournamentConfigurationActivity";
	
	/**
	 * Extra name for tournament id
	 */
	public static final String UTOOL_TOURNAMENT_ID_EXTRA_NAME = "tournamentId";
	
	/**
	 * Message sent to client plugins when their data reception socket is closed.
	 */
	public static final String UTOOL_SOCKET_CLOSED_MESSAGE = "UTOOL_SOCKET_DISCONNECT";
	
	/**
	 * The tournament's id from the core
	 */
	private long tournamentId;
	
	/**
	 * The activity this class is associated with
	 */
	protected Activity activity;
	
	/**
	 * Constructor for PluginCommonActivityHelper
	 * @param activity The activity to assist
	 */
	public PluginCommonActivityHelper(Activity activity){
		this.activity = activity;
		
		//Get tournament id extra
		tournamentId = activity.getIntent().getExtras().getLong("tournamentId");
	}
	
	/**
	 * Get the tournament id
	 * @return Tournament id
	 */
	public long getTournamentId(){
		return tournamentId;
	}
	
	/**
	 * Get an intent preconfigured for an activity containing a plugin helper. The extra tournamentId is set.
	 * @param packageContext Intent's packageContect
	 * @param cls Intent's cls
	 * @return Intent with extra tournamentId
	 * @see Intent
	 */
	public Intent getNewIntent(Context packageContext, Class<?> cls){
		Intent i = new Intent(packageContext, cls);
		i.putExtra("tournamentId", getTournamentId());
		return i;
	}
	
	/**
	 * Get an intent for calling the tournament configuration screen
	 * @return Preconfigured intent
	 */
	public Intent getTournamentConfigurationIntent(){
		Intent i = new Intent(UTOOL_TOURNAMENT_CONFIG_ACTION);
		i.setClassName(UTOOL_TOURNAMENT_CONFIG_PACKAGE, UTOOL_TOURNAMENT_CONFIG_CLASS);
		i.putExtra(UTOOL_TOURNAMENT_ID_EXTRA_NAME, tournamentId);
		return i;
	}
}
