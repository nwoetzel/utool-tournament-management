package utool.core;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.InetAddress;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import utool.core.AbstractTournament.TournamentLocationEnum;
import utool.networking.BroadcastManager;
import utool.networking.NetworkHelper;
import utool.networking.packet.HostInformation;
import utool.networking.packet.PlayerMessage;
import utool.networking.packet.PluginStartMessage;
import utool.networking.packet.PluginTerminationMessage;
import utool.persistence.Profile;
import utool.persistence.SavableConfigurationList;
import utool.persistence.SavableProfileList;
import utool.persistence.StorageManager;
import utool.persistence.TournamentConfiguration;
import utool.plugin.IUTooLCore;
import utool.plugin.Player;
import utool.plugin.dummy.DummyMainActivity;
import utool.remoteexceptions.ConnectionFailedRemoteException;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.RemoteException;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.ActivityNotFoundException;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.ServiceConnection;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemLongClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Toast;
import android.widget.AdapterView.OnItemClickListener;

/**
 * Activity for the Home screen
 */
@SuppressLint("NewApi")
public class HomeActivity extends Activity {
	/**
	 * AbstractTournament adapter
	 */
	private AbstractTournament tournaments;

	/**
	 * Request code for Connect As activity
	 */
	private static final int CONNECT_AS_ACTIVITY_REQUEST_CODE = 5;

	/**
	 * Request code for Tournament Configuration activity
	 */
	private static final int TOURNAMENT_CONFIGURATION_ACTIVITY_REQUEST_CODE = 6;

	/**
	 * Request code when editing a tournament using the Tournament Configuration activity
	 */
	private static final int TOURNAMENT_CONFIGURATION_EDIT_ACTIVITY_REQUEST_CODE = 7;

	/**
	 * Tag used for logging
	 */
	private static final String LOG_TAG = "utool.core.HomeActivity";

	/**
	 * Information about the server selected from the list
	 */
	private HostInformation server;

	/**
	 * Progress dialog for waiting on the host during connections
	 */
	private ProgressDialog progressDialog;

	/**
	 * UUID to connect as, when the Connect As activity has been used
	 */
	private Player connectAsPlayer;
	
	/**
	 * Boolean controlling whether BroadcastManager is broadcasting/receiving remote tournaments
	 */
	private boolean networkingEnabled = true;

	/**
	 * The player connection type last selected
	 */
	private PlayerConnectType playerConnectAsType = PlayerConnectType.ConnectAsSelf;

	/**
	 * Handler for cleaning the connection list
	 */
	private Handler listCleanupHandler = new Handler();

	/**
	 * Runnable for cleaning the connection list
	 */
	private Runnable listCleaner = new Runnable() {

		@Override
		public void run() {
			AbstractTournament.processUiChanges(true);
			listCleanupHandler.postDelayed(listCleaner, 1000);
		}
	};

	/**
	 * Runnable class for connection dialog thread
	 */
	private class TournamentWaitThread implements Runnable {
		/**
		 * The tournament Core for the connection
		 */
		private Core tournamentCore;

		/**
		 * Constructor
		 * @param tournamentCore The tournament Core for the connection
		 */
		public TournamentWaitThread(Core tournamentCore){
			this.tournamentCore = tournamentCore;
		}

		public void run() 
		{
			//Prepare to start plugin
			PluginStartMessage pluginStart = null;
			while (pluginStart == null){
				try {
					pluginStart = tournamentCore.getPluginStartMessage();
				} catch (IOException e1) {
					//Core is shutdown, stop the connection attempt
					progressDialog.dismiss();
					unbindService(mConnectionRemoteTournament);
				}
				if (pluginStart == null){
					try {
						Thread.sleep(10);
					} catch (InterruptedException e) {
					}
				}
			}
			Intent i = pluginStart.getIntent();
			tournamentCore.setPluginIntent(i);
			progressDialog.dismiss();
			unbindService(mConnectionRemoteTournament);
			Intent tournamentIntent = tournamentCore.getIntent();
			//Start plugin
			try
			{
				tournamentCore.startPlugin(HomeActivity.this, tournamentIntent);
			} 
			catch (ActivityNotFoundException e)
			{
				tournamentIntent.setClass(HomeActivity.this, DummyMainActivity.class);
				tournamentCore.startPlugin(HomeActivity.this, tournamentIntent);
			}
		}
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		//add action bar if honeycomb or higher
		if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB){
			setContentView(R.layout.activity_home);
			getActionBar().setDisplayShowHomeEnabled(false);
			getActionBar().setDisplayShowTitleEnabled(false);
		} else {
			this.requestWindowFeature(Window.FEATURE_NO_TITLE);
			setContentView(R.layout.activity_home);
		}

		//Prepare tournament list
		tournaments = new AbstractTournament(this, R.layout.tournament_list_item);
		ListView homeTournamentsListView = (ListView) findViewById(R.id.homeTournamentsListView);
		homeTournamentsListView.setAdapter(tournaments);
		listCleaner.run();

		//Load saved tournaments
		loadConfigurationList();

		try {
			BroadcastManager.startHostDiscovery();
		} catch (SocketException e) {
			Toast t = Toast.makeText(this, "Error listening on UDP port " + BroadcastManager.DISCOVERY_PORT_NUMBER + ". Remote tournaments will not be detected.", Toast.LENGTH_LONG);
			t.show();
		}

		//This is the most annoying line of code in the history of lines of code
		//android.os.Debug.waitForDebugger();
		//check if there is a profile saved on the device
		SavableProfileList profiles = null;
		try {
			profiles = (SavableProfileList) StorageManager.loadSavable(ProfileActivity.PROFILE_LIST_KEY, SavableProfileList.class, this, null);
		} catch (InstantiationException e) {
			e.printStackTrace();
		} catch (IllegalAccessException e) {
			e.printStackTrace();
		}
		if (profiles == null || profiles.size() == 0){
			//if there is not, then send them to the profile screen
			Intent i = new Intent(this, ProfileActivity.class);
			i.putExtra("createOnSave", true);
			startActivity(i);
			finish();
		}


		Button newTournamentButton = (Button)findViewById(R.id.newTournamentButton);
		newTournamentButton.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View view) {
				newTournamentButton_onClick(view);
			}
		});

		//Create handler for long clicks on tournament items
		homeTournamentsListView.setOnItemLongClickListener(new OnItemLongClickListener() {
			@Override
			public boolean onItemLongClick(AdapterView<?> adapter, View view, int index, long id) {
				return homeTournamentsListView_onItemLongClick(adapter, view, index, id);
			}});

		//create handler to launch the plugin on item select
		homeTournamentsListView.setOnItemClickListener(new OnItemClickListener(){
			public void onItemClick(AdapterView<?> adapter, View view, int index, long id) {
				homeTournamentsListView_onItemClick(adapter, view, index, id);
			}
		});
	}

	/**
	 * OnItemLongClickListener for listed tournaments
	 * @param adapter Adapter
	 * @param view View
	 * @param index Index
	 * @param id Id
	 * @return Boolean
	 * @see OnItemLongClickListener
	 */
	private boolean homeTournamentsListView_onItemLongClick(AdapterView<?> adapter, View view, int index, long id) {
		final String details = "Details";
		final String edit = "Edit";
		final String options = "Options";
		final String terminate = "Terminate";
		final String restart = "Restart";
		final String delete = "Delete";
		AbstractTournament tournament = (AbstractTournament) adapter.getItemAtPosition(index);
		List<String> menuItems = new LinkedList<String>();
		//Details
		menuItems.add(details);
		AlertDialog.Builder builder = new AlertDialog.Builder(HomeActivity.this);
		builder.setTitle(tournament.getTournamentName());
		if (tournament instanceof Core && ((Core)tournament).getTournamentLocation() == AbstractTournament.TournamentLocationEnum.Local){
			menuItems.add(edit);
			//menuItems.add(options);
			menuItems.add(terminate);
			//menuItems.add(restart);
		} else if (tournament instanceof Core){
			menuItems.add(terminate);
		} else if (tournament instanceof HostInformation){

		} else if (tournament instanceof TournamentConfiguration){
			menuItems.add(delete);
		}
		String[] items = new String[0];
		items = menuItems.toArray(items);

		/**
		 * Inner class for handling the menu choice
		 */
		class MenuDialogOnClick implements DialogInterface.OnClickListener {
			private String[] items;
			private AbstractTournament tournament;

			public MenuDialogOnClick(String[] items, AbstractTournament tournament){
				this.items = items;
				this.tournament = tournament;
			}

			@Override
			public void onClick(DialogInterface dialog, int which) {
				String item = items[which];
				if (item.equals(details)){
					//Show advanced details
					AlertDialog.Builder d = new AlertDialog.Builder(HomeActivity.this);
					d.setTitle("Details");
					String message = "Tournament Name: " + tournament.getTournamentName() + "\n\n"
							+ "Tournament UUID: " + tournament.getTournamentUUID() + "\n\n"
							+ "Status: " + tournament.getTournamentLocation().name();
					if (tournament.getTournamentLocation() == TournamentLocationEnum.RemoteConnected || tournament.getTournamentLocation() == TournamentLocationEnum.RemoteDiscovered){
						message += "\n\n" + "Server: " + tournament.getServerAddress() + ":" + tournament.getServerPort();
					} else {
						message += "\n\n Port: " + tournament.getServerPort();
					}
					d.setMessage(message);
					d.show();
				} else if (item.equals(edit)){
					//Go to tournament configuration
					Intent i = new Intent(HomeActivity.this, TournamentConfigurationActivity.class);
					i.putExtra("tournamentId", ((Core)tournament).getTournamentId());
					startActivityForResult(i, TOURNAMENT_CONFIGURATION_EDIT_ACTIVITY_REQUEST_CODE);
				} else if (item.equals(options)){
					//Options screen listed on UI doc
				} else if (item.equals(terminate)){
					/**
					 * Inner class for the terminate tournament Yes/No prompt
					 */
					class TerminatePromptDialogOnClick implements DialogInterface.OnClickListener {
						private AbstractTournament tournament;
						public TerminatePromptDialogOnClick(AbstractTournament tournament){
							this.tournament = tournament;
						}
						@Override
						public void onClick(DialogInterface dialog, int which) {
							switch (which){
								case DialogInterface.BUTTON_POSITIVE:
									//Yes button clicked
									Core t = (Core)tournament;
									terminateTournament(t);
									break;

								case DialogInterface.BUTTON_NEGATIVE:
									//No button clicked
									break;
							}
						}
					}
					//Construct the Yes/No prompt dialog
					AlertDialog.Builder builder = new AlertDialog.Builder(HomeActivity.this);
					TerminatePromptDialogOnClick promptOnClick = new TerminatePromptDialogOnClick(tournament);
					builder.setPositiveButton("Yes", promptOnClick).setNegativeButton("No", promptOnClick);
					if (tournament.getTournamentLocation() == TournamentLocationEnum.Local || tournament.getTournamentLocation() == TournamentLocationEnum.LocalFinished){
						builder.setMessage("Are you sure you want to terminate " + tournament.getTournamentName() + "?");
					} else {
						builder.setMessage("Are you sure you want to disconnect from " + tournament.getTournamentName() + "?");
					}
					//Show the Yes/No prompt dialog
					builder.show();

				} else if (item.equals(restart)){
					//Restart the tournament
				} else if (item.equals(delete)){
					//Delete the saved tournament
					if (tournament instanceof TournamentConfiguration){
						deleteFromConfigurationList(tournament);
					}
				}
			}

		}
		builder.setItems(items, new MenuDialogOnClick(items, tournament));
		builder.show();
		return true;
	}

	/**
	 * Terminate/disconnect from a tournament instance. This signals the plugin to terminate if it is started.
	 * @param tournament The tournament to terminate/disconnect from
	 */
	private void terminateTournament(Core tournament){
		if (tournament.hasStarted() && tournament.getTerminateCount() == 0){
			//Notify the plugin to terminate
			byte[] data;
			String message = new PluginTerminationMessage().getXml();
			tournament.incrementTerminateCount();
			try {
				data = message.getBytes("UTF-8");
				UTooLCoreService.getSocketWrapper(((Core)tournament).getTournamentId()).sendToPlugin(data);
			} catch (UnsupportedEncodingException e) {
			}
		} else if (tournament.getTerminateCount() > 0){
			AlertDialog.Builder builder = new AlertDialog.Builder(HomeActivity.this);
			builder.setMessage("You have already attempted to terminate this tournament. The plugin may not be responding. Do you want to remove the tournament from the list? Doing so may leave the plugin in an improper state.");
			/**
			 * Inner class for the forcefully terminate tournament Yes/No prompt
			 */
			class TerminatePromptDialogOnClick implements DialogInterface.OnClickListener {
				private Core tournament;
				public TerminatePromptDialogOnClick(Core tournament){
					this.tournament = tournament;
				}
				@Override
				public void onClick(DialogInterface dialog, int which) {
					switch (which){
						case DialogInterface.BUTTON_POSITIVE:
							//Notify service to close and cleanup tournament core
							UTooLCoreService.closeServiceInstance(tournament.getTournamentId());
							break;
						case DialogInterface.BUTTON_NEGATIVE:
							//No button clicked
							break;
					}
				}
			}
			TerminatePromptDialogOnClick promptOnClick = new TerminatePromptDialogOnClick(tournament);
			builder.setPositiveButton("Yes", promptOnClick).setNegativeButton("No", promptOnClick);
			builder.show();
		} else {
			//Plugin hasn't been started, so just remove tournament from the list
			BroadcastManager.stopBroadcastManager(tournament.getTournamentId());
			tournament.removeTournament();
		}
	}

	/**
	 * OnItemClickListener for listed tournaments
	 * @param adapter Adapter
	 * @param view View
	 * @param index Index
	 * @param id Id
	 * @see OnItemClickListener
	 */
	private void homeTournamentsListView_onItemClick(AdapterView<?> adapter, View view, int index, long id) {
		AbstractTournament tournament = (AbstractTournament) adapter.getItemAtPosition(index);
		if (tournament instanceof Core){ //Start local tournament, or resume any tournament
			Intent tournamentIntent = ((Core)tournament).getIntent();
			((Core)tournament).startPlugin(HomeActivity.this, tournamentIntent);
		} else if (tournament instanceof TournamentConfiguration){
			//Create core
			prepareSavedTournament((TournamentConfiguration) tournament);
		} else if (tournament instanceof HostInformation){ //Connect to remote tournament
			server = (HostInformation) tournament;
			showConnectMenu();
		}
	}
	
	/**
	 * Display Connect, Connect As choice dialog
	 */
	private void showConnectMenu(){
		AlertDialog.Builder choiceDialog = new AlertDialog.Builder(this);
		choiceDialog.setTitle("Connection Option");
		String[] items = new String[]{"Connect", "Connect as...", "Observe"};
		choiceDialog.setItems(items, new DialogInterface.OnClickListener() {

			@Override
			public void onClick(DialogInterface dialog, int which) {
				if (which == 0){
					//Connect
					playerConnectAsType = PlayerConnectType.ConnectAsSelf;
					prepareClientConnection();
				} else if (which == 1){
					//Connect as
					playerConnectAsType = PlayerConnectType.ConnectAsOther;
					prepareClientConnection();
				} else if (which == 2){
					//Observe
					playerConnectAsType = PlayerConnectType.ConnectAsObserver;
					prepareClientConnection();
				}
			}
		});
		choiceDialog.show();
	}

	/**
	 * Prepare to start a tournament from a saved configuration
	 * @param tournament The TournamentConfiguration to load from
	 * @return The initialized Core
	 */
	private Core prepareSavedTournament(TournamentConfiguration tournament){
		//======Set the device ID and host name of the user=======================
		SavableProfileList profiles = null;
		try {
			profiles = (SavableProfileList)StorageManager.loadSavable(ProfileActivity.PROFILE_LIST_KEY, SavableProfileList.class, this, null);
		} catch (InstantiationException e) {
			e.printStackTrace();
		} catch (IllegalAccessException e) {
			e.printStackTrace();
		}
		if (profiles == null){
			//TODO: send the user to make a profile
		}
		Profile p = profiles.getSelectedProfile();
		Core tournamentCore = tournament.createTournament(p.getId());
		if (tournamentCore.getPlayerList().contains(p)){
			tournamentCore.addPlayer(p);
		}
		//determine config index
		SavableConfigurationList list = loadConfigurationList();
		int configIndex = list.indexOf(tournament);
		tournamentCore.setConfigIndex(configIndex);

		//============end core setup===============================================

		//Connect to core service
		Intent serviceBindIntent = new Intent(UTooLCoreService.UTOOL_SERVICE_NAME);
		serviceBindIntent.putExtra("tournamentId", tournamentCore.getTournamentId());
		mConnectionLocalTournament = new MConnectionLocalTournament(tournamentCore);
		bindService(serviceBindIntent, mConnectionLocalTournament, Context.BIND_AUTO_CREATE);

		return tournamentCore;
	}


	/**
	 * Prepare the tournament creation process for connecting to a remote device.
	 */
	private void prepareClientConnection(){
		Core tournamentCore = Core.getNewCoreInstance(server);

		//======Set the device ID and host name of the user=======================
		SavableProfileList profiles = null;
		try {
			profiles = (SavableProfileList)StorageManager.loadSavable(ProfileActivity.PROFILE_LIST_KEY, SavableProfileList.class, HomeActivity.this, null);
		} catch (InstantiationException e) {
			e.printStackTrace();
		} catch (IllegalAccessException e) {
			e.printStackTrace();
		}
		if (profiles == null){
			//TODO: send the user to make a profile
		}
		Profile p = profiles.getSelectedProfile();

		Player profile = new Player(p.getId(), p.getName());
		profile.setPortraitFilepath(p.getFile());
		tournamentCore.setDeviceId(p.getId());
		tournamentCore.setProfile(profile);
		//============end core setup===============================================

		//Connect to service
		Intent serviceBindIntent = new Intent(UTooLCoreService.UTOOL_SERVICE_NAME);
		serviceBindIntent.putExtra("tournamentId", tournamentCore.getTournamentId());
		mConnectionRemoteTournament = new MConnectionRemoteTournament(tournamentCore);
		bindService(serviceBindIntent, mConnectionRemoteTournament, Context.BIND_AUTO_CREATE);
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		MenuInflater inflater = getMenuInflater();
		inflater.inflate(R.menu.activity_home, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		// Handle item selection
		switch (item.getItemId()) {
			case R.id.menu_profiles:
				Intent i = new Intent(HomeActivity.this, ProfileActivity.class);
				startActivity(i);
				return true;
			case R.id.menu_help:
				showHelp();
				return true;
			case R.id.menu_direct_connect:
				showDirectConnect();
				return true;
			case R.id.menu_enable_networking:
				toggleNetworking(!item.isChecked());
				return true;
			default:
				return super.onOptionsItemSelected(item);
		}
	}

	@Override
	protected void onPause() {
		super.onPause();
		BroadcastManager.stopHostDiscovery();
	}

	@Override
	protected void onResume(){
		super.onResume();
		loadConfigurationList();
		try {
			BroadcastManager.startHostDiscovery();
		} catch (SocketException e) {
		}

		if (StorageManager.loadBoolean("HomeFirstUse", this, true)){
			StorageManager.saveBoolean("HomeFirstUse", false, this);
			
			AlertDialog.Builder b = new AlertDialog.Builder(HomeActivity.this);
			b.setTitle("Welcome to UTooL!");
			b.setMessage("If you ever need help, press Menu (or the vertical ellipsis button) then Help.");
			b.setNegativeButton("Close", new DialogInterface.OnClickListener() {
				
				@Override
				public void onClick(DialogInterface dialog, int which) {
					dialog.cancel();
				}
			});
			b.show();
		}
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
	}

	@Override 
	public void onActivityResult(int requestCode, int resultCode, Intent data) {     
		super.onActivityResult(requestCode, resultCode, data); 
		switch(requestCode) { 
			//Process returned activity data
			case (CONNECT_AS_ACTIVITY_REQUEST_CODE) : {
				//Process data from Connect As activity
				if (resultCode == Activity.RESULT_OK) { 
					connectAsPlayer = (Player) data.getParcelableExtra("connectAsPlayer");

					//Send player registration information
					if (connectAsPlayer != null){
						PlayerMessage player = new PlayerMessage(connectAsPlayer);
						try {
							mICoreRemoteTournament.send(player.getXml());
							mConnectionRemoteTournament.tournamentCore.setProfile(player.getPlayer());
							mConnectionRemoteTournament.tournamentCore.setDeviceId(player.getPlayer().getUUID());
							continueClientConnection(mConnectionRemoteTournament.tournamentCore);
						} catch (RemoteException e) {
							Log.e("Remote Exception", "yup", e);
						}
					} else {
						closeServiceConnection();
						return;
					}
				} else {
					closeServiceConnection();
					connectAsPlayer = null;
				}
				break;
			}
			case (TOURNAMENT_CONFIGURATION_ACTIVITY_REQUEST_CODE) : {
				if (resultCode == Activity.RESULT_OK){
					//Connect to core service
					Intent serviceBindIntent = new Intent(UTooLCoreService.UTOOL_SERVICE_NAME);
					Core tournamentCore = Core.getCoreInstance(data.getLongExtra("tournamentId", -1));
					serviceBindIntent.putExtra("tournamentId", tournamentCore.getTournamentId());
					mConnectionLocalTournament = new MConnectionLocalTournament(tournamentCore);
					bindService(serviceBindIntent, mConnectionLocalTournament, Context.BIND_AUTO_CREATE);
				}
			}
		}
	}

	/**
	 * Show the help dialog
	 */
	private void showHelp(){
		final Dialog dialog = new Dialog(HomeActivity.this);
		dialog.setContentView(R.layout.activity_home_help);
		dialog.setTitle("UTooL Help");
		dialog.setCancelable(true);
		Button closeButton = (Button) dialog.findViewById(R.id.home_help_close_button);
		closeButton.setOnClickListener(new Button.OnClickListener() {      
			public void onClick(View view) { 
				dialog.dismiss();     
			}
		});
		dialog.show();
	}
	
	@Override
	public boolean onPrepareOptionsMenu(Menu menu) {
		MenuItem networkingMenuItem = menu.findItem(R.id.menu_enable_networking);
		if (networkingEnabled){
			networkingMenuItem.setChecked(true);
		} else {
			networkingMenuItem.setChecked(false);
		}
	    return super.onPrepareOptionsMenu(menu);
	}
	
	/**
	 * Toggle networking support
	 * @param enable Set to true to enable, false to disable
	 */
	private void toggleNetworking(boolean enable){
		AlertDialog.Builder b = new AlertDialog.Builder(HomeActivity.this);
		String message = "Are you sure you want to %toggle networking? Currently connected clients will not be affected.";
		if (enable){
			message = message.replace("%toggle", "enable");
		} else {
			message = message.replace("%toggle", "disable");
		}
		b.setMessage(message);
		class ToggleNetworking implements DialogInterface.OnClickListener{
			private boolean enable;
			public ToggleNetworking(boolean enable){
				this.enable = enable;
			}
			@Override
			public void onClick(DialogInterface dialog, int which) {
				networkingEnabled = enable;
				if (!enable) {
					BroadcastManager.stopHostDiscovery();
					BroadcastManager.stopAllBroadcastManagers();
				} else {
					try {
						BroadcastManager.startHostDiscovery();
						BroadcastManager.startAllBroadcastManagers();
					} catch (SocketException e) {}
				}
				HomeActivity.this.invalidateOptionsMenu();
			}
		}
		b.setPositiveButton("Yes", new ToggleNetworking(enable));
		b.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
			@Override
			public void onClick(DialogInterface dialog, int which) {
				dialog.cancel();
			}
		});
		b.show();
	}
	
	/**
	 * Show the Direct Connect dialog
	 */
	private void showDirectConnect(){
		final Dialog dialog = new Dialog(HomeActivity.this);
		dialog.setContentView(R.layout.activity_home_direct_connect);
		dialog.setTitle("Direct Connect");
		dialog.setCancelable(true);
		Button closeButton = (Button) dialog.findViewById(R.id.home_direct_connect_cancel_button);
		closeButton.setOnClickListener(new Button.OnClickListener() {      
			public void onClick(View view) { 
				dialog.dismiss();     
			}
		});
		Button connectButton = (Button) dialog.findViewById(R.id.home_direct_connect_button);
		connectButton.setOnClickListener(new Button.OnClickListener() {      
			public void onClick(View view) { 
				//TODO: Connect to server
				EditText hostname = (EditText) dialog.findViewById(R.id.editTextHostName);
				EditText port = (EditText) dialog.findViewById(R.id.editTextPortNumber);
				InetAddress serverAddress;
				try {
					serverAddress = NetworkHelper.getByName(hostname.getText().toString());
					if (serverAddress == null){
						throw new UnknownHostException("Unknown host " + hostname.getText().toString());
					}
					int tournamentPort = Integer.parseInt(port.getText().toString());
					server = new HostInformation("Direct Connect Tournament", serverAddress, tournamentPort);
					showConnectMenu();
					dialog.dismiss();   
				} catch (UnknownHostException e) {
					AlertDialog.Builder b = new AlertDialog.Builder(HomeActivity.this);
					b.setMessage(e.getMessage());
					b.setNegativeButton("Close", new DialogInterface.OnClickListener() {
						
						@Override
						public void onClick(DialogInterface dialog, int which) {
							dialog.cancel();
						}
					});
					b.show();
				}
			}
		});
		
		dialog.show();
	}

	/**
	 * OnClick method for the New Tournament button
	 * @param view The clicked view
	 */
	private void newTournamentButton_onClick(View view){
		Intent i = new Intent(HomeActivity.this, TournamentConfigurationActivity.class);
		startActivityForResult(i, TOURNAMENT_CONFIGURATION_ACTIVITY_REQUEST_CODE);
	}

	/**
	 * Load the saved tournament configurations.
	 * @return The list of configurations
	 */
	public SavableConfigurationList loadConfigurationList(){
		//load the configuration list
		SavableConfigurationList configurationList = null;
		try {
			configurationList = (SavableConfigurationList) StorageManager.loadSavable("ConfigurationList", SavableConfigurationList.class, this, null);
		} catch (InstantiationException e) {
			e.printStackTrace();
		} catch (IllegalAccessException e) {
			e.printStackTrace();
		}

		//if there isn't an existing list, make one
		if (configurationList == null){
			configurationList = new SavableConfigurationList();
		}

		for (TournamentConfiguration config: configurationList){
			AbstractTournament.addTournament(config.getTournamentUUID(), config);
		}
		return configurationList;
	}

	/**
	 * Delete a saved tournament
	 * @param tournament The tournament to delete
	 */
	private void deleteFromConfigurationList(AbstractTournament tournament){
		SavableConfigurationList configurationList;
		try {
			//load the configuration list
			configurationList = (SavableConfigurationList) StorageManager.loadSavable("ConfigurationList", SavableConfigurationList.class, this, null);
			//remove the tournament
			for (TournamentConfiguration t : configurationList){
				if (t.getTournamentUUID().equals(tournament.getTournamentUUID())){
					//remove from configuration list
					configurationList.remove(t);

					//resave the list
					StorageManager.saveSavable("ConfigurationList", configurationList, HomeActivity.this);

					//remove from memory
					AbstractTournament.removeTournament(t.tournamentUUID);
					break;
				}
			}
		} catch (InstantiationException e) {
			e.printStackTrace();
		} catch (IllegalAccessException e) {
			e.printStackTrace();
		}
	}

	/**
	 * Close the temporary service connection used for establishing remote connections
	 */
	private void closeServiceConnection(){
		try {
			mICoreRemoteTournament.close();
			unbindService(mConnectionRemoteTournament);
		} catch (RemoteException e) {
		}
	}

	/**
	 * Continue the connection process for connecting to remote tournaments
	 * @param tournamentCore The tournament Core used in this connection
	 */
	private void continueClientConnection(Core tournamentCore){
		//Show wait dialog
		progressDialog = new ProgressDialog(HomeActivity.this);
		progressDialog.setTitle("Connection Established");
		progressDialog.setMessage("Waiting for tournament to start");
		progressDialog.setOnCancelListener(new Dialog.OnCancelListener() {

			public void onCancel(DialogInterface arg0) {
				closeServiceConnection();
			}
		});
		
		progressDialog.show();

		//Start up message reception thread
		Thread t = new Thread(new TournamentWaitThread(tournamentCore));
		t.start();
	}


	/**
	 * The reference to the UTooLCore Service instance for this activity
	 * @since 10/4/2012
	 */
	IUTooLCore mICoreRemoteTournament;

	/**
	 * The connection to the UTooLCore Service instance for this activity
	 * @since 10/4/2012
	 */
	private MConnectionRemoteTournament mConnectionRemoteTournament;

	/**
	 * Inner class for service setup for client connections
	 */
	private class MConnectionRemoteTournament implements ServiceConnection{
		/**
		 * The tournament Core for this connection
		 */
		private Core tournamentCore;

		/**
		 * Constructor
		 * @param tournamentCore The tournament Core to use when configuring the service
		 */
		public MConnectionRemoteTournament(Core tournamentCore){
			this.tournamentCore = tournamentCore;
		}

		//Called when the connection with the service is established
		public void onServiceConnected(ComponentName className, IBinder service) {
			//This gets an instance of the IUTooLCore interface, which we can use to call on the service
			mICoreRemoteTournament = IUTooLCore.Stub.asInterface(service);

			//Now we can use the service
			try {
				//Connect to server
				mICoreRemoteTournament.connectToServer(server.getServerAddress().getAddress(), server.getServerPort(), tournamentCore.getTournamentId());
				Log.i(LOG_TAG, "Connected to server " + server.toString());

				if (playerConnectAsType == PlayerConnectType.ConnectAsOther){
					PlayerMessage playerListRequest = new PlayerMessage();
					mICoreRemoteTournament.send(playerListRequest.getXml());
					//wait for player list to be received
					while (!mICoreRemoteTournament.playerListUpdated()){
						try {
							Thread.sleep(10);
						} catch (InterruptedException e) {
						}
					}
					//get received player list
					ArrayList<Player> playerList = new ArrayList<Player>(mICoreRemoteTournament.getPlayerList());
					//Show player list activity to choose who to connect as
					Intent i = new Intent(HomeActivity.this, ConnectAsActivity.class);
					i.putExtra("playerList", playerList);
					startActivityForResult(i, CONNECT_AS_ACTIVITY_REQUEST_CODE);

					//look at onActivityResult for result retrieval
				} else if (playerConnectAsType == PlayerConnectType.ConnectAsSelf) {
					//Send player registration information
					PlayerMessage player = new PlayerMessage(tournamentCore.getProfile());
					mICoreRemoteTournament.send(player.getXml());
					continueClientConnection(tournamentCore);
				} else {
					//Connect without sending player registration information, implying observer status
					continueClientConnection(tournamentCore);
				}
			} catch (ConnectionFailedRemoteException e){
				Log.i(LOG_TAG, "Connection to server " + server.toString() + " failed");
				tournamentCore.removeTournament();
				Log.d(LOG_TAG, e.toString());
				Toast t = Toast.makeText(HomeActivity.this, "Error connecting to remote server " + server.toString(), Toast.LENGTH_LONG);
				t.show();
				unbindService(this);
			} catch (RemoteException e) {
				tournamentCore.removeTournament();
				Log.e(LOG_TAG, e.toString());
				Toast t = Toast.makeText(HomeActivity.this, "An unknown error has occured in the UTooL Service", Toast.LENGTH_LONG);
				t.show();
				unbindService(this);
			}
		}

		//Called when the connection with the service disconnects unexpectedly
		public void onServiceDisconnected(ComponentName className) {
			Log.e(LOG_TAG, "Service has unexpectedly disconnected");
			mICoreRemoteTournament = null;
		}
	}

	/**
	 * The reference to the UTooLCore Service instance for this activity
	 * @since 10/4/2012
	 */
	IUTooLCore mICoreLocalTournament;

	/**
	 * The connection to the UTooLCore Service instance for this activity
	 * @since 10/4/2012
	 */
	private MConnectionLocalTournament mConnectionLocalTournament;

	/**
	 * Inner class for local tournament service setup
	 */
	private class MConnectionLocalTournament implements ServiceConnection {
		/**
		 * The tournament Core to configure the service with
		 */
		private Core tournamentCore;
		/**
		 * Constructor
		 * @param tournamentCore The tournament Core to configure the service with
		 */
		public MConnectionLocalTournament(Core tournamentCore){
			this.tournamentCore = tournamentCore;
		}

		//Called when the connection with the service is established
		public void onServiceConnected(ComponentName className, IBinder service) {
			//This gets an instance of the IUTooLCore interface, which we can use to call on the service
			mICoreLocalTournament = IUTooLCore.Stub.asInterface(service);

			//Now we can use the service
			try {
				int port = mICoreLocalTournament.startServer(tournamentCore.getTournamentId());
				tournamentCore.serverPort = port;
				BroadcastManager bcastmanager = new BroadcastManager(HomeActivity.this, tournamentCore.toString(), port, tournamentCore.getTournamentId(), tournamentCore.getTournamentUUID());
				try {
					if (networkingEnabled){
						bcastmanager.startServerAdvertisement();
					}

					Intent i = tournamentCore.getIntent();
					PluginStartMessage pluginStart = new PluginStartMessage(i.getComponent().getPackageName(), i.getComponent().getClassName());
					String pluginStartXml = pluginStart.getXml();
					mICoreLocalTournament.setInitialConnectMessage(pluginStartXml);
					mICoreLocalTournament.send(pluginStartXml);
					Log.d(LOG_TAG, "PluginStartMessage sent");

					tournamentCore = null;
					unbindService(this);
				} catch (SocketException e) {
					Log.e(LOG_TAG, e.getMessage() + "\n" + e.getStackTrace());
				}

			} catch (RemoteException e) {
				Log.e(LOG_TAG, e.toString());
				Toast t = new Toast(HomeActivity.this);
				t.setText("Error connecting to core service");
				t.show();
				unbindService(this);
			}
		}

		//Called when the connection with the service disconnects unexpectedly
		public void onServiceDisconnected(ComponentName className) {
			Log.e(LOG_TAG, "Service has unexpectedly disconnected");
			mICoreLocalTournament = null;
		}
	};


	/**
	 * Enum definining player connection types
	 *
	 */
	private enum PlayerConnectType{
		/**
		 * Player has selected to use their profile information
		 */
		ConnectAsSelf,
		/**
		 * Player has selected to connect as another person
		 */
		ConnectAsOther,
		/**
		 * Player has selected to connect as an observer
		 */
		ConnectAsObserver
	}
}
