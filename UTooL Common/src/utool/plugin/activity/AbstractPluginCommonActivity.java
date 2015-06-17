package utool.plugin.activity;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;

/**
 * This class provides a framework for developing plugin activities.
 * Extending this class allows for activities to more easily track the tournament id throughout the plugin.
 * When calling activities extending this class, use getNewIntent, or make sure to place the tournamentId extra in any externally created intents.
 * 
 * The main entry point activity should extend {@link AbstractPluginMainActivity}.
 * @author Cory
 *
 */
public abstract class AbstractPluginCommonActivity extends FragmentActivity {
	
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
	 * The tournament's id from the core
	 */
	private long tournamentId;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		//Get tournament id extra
		tournamentId = getIntent().getExtras().getLong("tournamentId");
	}
	
	/**
	 * Get the tournament id
	 * @return Tournament id
	 */
	public long getTournamentId(){
		return tournamentId;
	}
	
	/**
	 * Get an intent preconfigured for an AbstractPluginCommon activity. The extra tournamentId is set.
	 * @param packageContext Intent's packageContect
	 * @param cls Intent's cls
	 * @return Intent with extra tournamentId
	 * @see Intent
	 */
	protected Intent getNewIntent(Context packageContext, Class<?> cls){
		Intent i = new Intent(packageContext, cls);
		i.putExtra("tournamentId", getTournamentId());
		return i;
	}
	
	/**
	 * Get an intent for calling the tournament configuration screen
	 * @return Preconfigured intent
	 */
	protected Intent getTournamentConfigurationIntent(){
		Intent i = new Intent(UTOOL_TOURNAMENT_CONFIG_ACTION);
		i.setClassName(UTOOL_TOURNAMENT_CONFIG_PACKAGE, UTOOL_TOURNAMENT_CONFIG_CLASS);
		i.putExtra(UTOOL_TOURNAMENT_ID_EXTRA_NAME, tournamentId);
		return i;
	}
}
