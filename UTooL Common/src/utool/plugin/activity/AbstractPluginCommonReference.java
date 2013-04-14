package utool.plugin.activity;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;

/**
 * This class provides reference to developing plugin activities.
 * Extending this class allows for activities to more easily track the tournament id throughout the plugin.
 * When calling activities extending this class, use {@link #getNewIntent(Context, Class)}, or make sure to place the tournamentId extra in any externally created intents.
 * 
 * The main entry point activity should extend {@link AbstractPluginMainActivity}.
 * @author Cory
 *
 */
public abstract class AbstractPluginCommonReference extends Activity {
	
	/**
	 * The PluginCommonActivityHelper this activity is using for all special operations
	 */
	protected PluginCommonActivityHelper pluginHelper;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		pluginHelper = new PluginCommonActivityHelper(this);
	}
	
	/**
	 * Get the tournament id
	 * @return Tournament id
	 */
	public long getTournamentId(){
		return pluginHelper.getTournamentId();
	}
	
	/**
	 * Get an intent preconfigured for an AbstractPluginCommon activity. The extra tournamentId is set.
	 * @param packageContext Intent's packageContect
	 * @param cls Intent's cls
	 * @return Intent with extra tournamentId
	 * @see Intent
	 */
	protected Intent getNewIntent(Context packageContext, Class<?> cls){
		return pluginHelper.getNewIntent(packageContext, cls);
	}
	
	/**
	 * Get an intent for calling the tournament configuration screen
	 * @return Preconfigured intent
	 */
	protected Intent getTournamentConfigurationIntent(){
		return pluginHelper.getTournamentConfigurationIntent();
	}
}
