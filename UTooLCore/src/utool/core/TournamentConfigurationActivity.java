package utool.core;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import utool.core.AbstractTournament.TournamentLocationEnum;
import utool.persistence.Profile;
import utool.persistence.SavableConfigurationList;
import utool.persistence.SavablePlayer;
import utool.persistence.SavablePlayerList;
import utool.persistence.SavableProfileList;
import utool.persistence.StorageManager;
import utool.persistence.TournamentConfiguration;
import utool.plugin.ImageDecodeTask;
import utool.plugin.PictureLoader;
import utool.plugin.Player;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.drawable.BitmapDrawable;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.FragmentActivity;
import android.util.Log;
import android.util.SparseArray;
import android.view.ContextMenu;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.ContextMenu.ContextMenuInfo;
import android.view.View.OnClickListener;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.AdapterView.AdapterContextMenuInfo;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

/**
 * This class is responsible for displaying the tournament configuration screen and allowing the user to 
 * configure their tournament settings
 * @author kreierj
 * @version 1/3/2013
 */
@SuppressLint("NewApi")
public class TournamentConfigurationActivity extends FragmentActivity{

	/**
	 * Key to be used when saving and loading the player list
	 */
	public static final String SAVED_PLAYER_LIST = "SavedPlayers";

	/**
	 * Pick plugin action used in plugin discovery
	 * @since 10/2/2012
	 */
	private static final String ACTION_PICK_PLUGIN = "utool.plugin.intent.PICK_PLUGIN";

	/**
	 * Plugin Category used in plugin discovery
	 * @since 10/2/2012
	 */
	private static final String CATEGORY_PLUGIN = "utool.plugin.PLUGIN";

	/**
	 * Request code for the camera
	 */
	private static final int CAMERA_REQUEST_CODE = 1;

	/**
	 * Request code for the gallery
	 */
	private static final int GALLERY_REQUEST_CODE = 3;

	/**
	 * Request code for editing a player's portrait using the camera
	 */
	private static final int EDIT_CAMERA_REQUEST_CODE = 2;

	/**
	 * Request code for editing a player's portrait using the gallery
	 */
	private static final int EDIT_GALLERY_REQUEST_CODE = 4;

	/**
	 * List of all configurations
	 */
	protected SavableConfigurationList configurationList;

	/**
	 * Reference to the tournament core being modified
	 */
	protected Core tournamentCore;

	/**
	 * Reference to the savable tournament configuration
	 */
	protected TournamentConfiguration configuration;

	/**
	 * The list of all players
	 */
	protected SavablePlayerList players;

	/**
	 * The list of selected players
	 */
	protected SavablePlayerList selectedPlayers;

	/**
	 * Map of intents created during plugin loading
	 */
	protected SparseArray<Intent> intentMap;

	/**
	 * The currently selected profile
	 */
	protected Profile selectedProfile;

	/**
	 * List of non-selected profiles (which are used for color coding the list)
	 */
	protected SavablePlayerList otherProfiles;

	/**
	 * The selected profile as a player (used for color coding the list)
	 */
	protected SavablePlayer profile;

	/**
	 * List of players loaded
	 */
	protected SavablePlayerList recentPlayers;

	/**
	 * List of players added
	 */
	protected SavablePlayerList addedPlayers;

	/**
	 * The imageUri of the add player portrait
	 */
	protected Uri imageUri;

	/**
	 * Index of the player being edited
	 */
	protected int playerBeingEdited = -1;


	@Override
	public void onCreate(Bundle savedInstanceState){
		super.onCreate(savedInstanceState);

		//hide the title bar
		if(Build.VERSION.SDK_INT < Build.VERSION_CODES.HONEYCOMB){
			//hide the title bar
			this.requestWindowFeature(Window.FEATURE_NO_TITLE);

			setContentView(R.layout.activity_tournament_configuration);
		} else {
			//Show the action bar on newer devices, for the menu
			setContentView(R.layout.activity_tournament_configuration);
			getActionBar().setDisplayShowHomeEnabled(false);
			getActionBar().setDisplayShowTitleEnabled(false);
		}

		//initialize instance variables
		initializeInstanceVariables();

		//initialize listeners
		initializeListeners(); 

		this.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN | WindowManager.LayoutParams.SOFT_INPUT_ADJUST_PAN);
	}

	@Override
	public void onResume(){
		super.onResume();

		//reset instance variables that need to be reset
		players = new SavablePlayerList();
		otherProfiles = new SavablePlayerList();
		addedPlayers = new SavablePlayerList();

		//Load players and profiles
		loadProfiles();
		loadPlayers();

		reloadList();

		if (StorageManager.loadBoolean("ConfigFirstUse", this, true)){
			showHelp();
		}
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data){
		if (requestCode == CAMERA_REQUEST_CODE && resultCode == Activity.RESULT_OK){
			//get path to picture taken
			String filepath = imageUri.getPath();

			//get the camera ImageButton
			ImageView portrait = (ImageView)findViewById(R.id.playerPortrait);

			//set the button's image to the taken picture
			BitmapDrawable image = PictureLoader.loadAndOrientPicture(getResources(), filepath);
			portrait.setImageDrawable(image);
			
			//update gallery with new image
			sendBroadcast(new Intent(Intent.ACTION_MEDIA_MOUNTED, Uri.fromFile(Environment.getExternalStorageDirectory())));

		} else if (requestCode == EDIT_CAMERA_REQUEST_CODE && resultCode == Activity.RESULT_OK){
			//get path to picture taken
			String filepath = imageUri.getPath();

			SavablePlayer old = players.get(playerBeingEdited);
			SavablePlayer p = new SavablePlayer(old.getUUID(), old.getName(), old.isGhost(), old.getSeedValue(), filepath);
			players.set(playerBeingEdited, p);
			if (selectedPlayers.contains(old)){
				selectedPlayers.set(selectedPlayers.indexOf(old), p);
			}
			if (addedPlayers.contains(old)){
				addedPlayers.set(addedPlayers.indexOf(old), p);
			}
			if (recentPlayers.contains(old)){
				recentPlayers.set(recentPlayers.indexOf(old), p);
				StorageManager.saveSavable("SavedPlayers", recentPlayers, this);
			}
			
			//update gallery with new image
			sendBroadcast(new Intent(Intent.ACTION_MEDIA_MOUNTED, Uri.fromFile(Environment.getExternalStorageDirectory())));
		}
		else if (requestCode == GALLERY_REQUEST_CODE && resultCode == RESULT_OK && data != null) {
			//get path to picture taken
			//parse the data
			imageUri = Uri.parse(data.getDataString());

			//retrieve from database
			Cursor cursor = getContentResolver().query(imageUri, null, null, null, null);
			cursor.moveToFirst();
			int idx = cursor.getColumnIndex(MediaStore.Images.ImageColumns.DATA);

			//get the path to the real file
			String filepath = cursor.getString(idx);
			imageUri = Uri.fromFile(new File(filepath));

			//get the camera ImageButton
			ImageView portrait = (ImageView)findViewById(R.id.playerPortrait);

			//set the button's image to the taken picture
			BitmapDrawable image = PictureLoader.loadAndOrientPicture(getResources(), filepath);
			portrait.setImageDrawable(image);
		}
		else if (requestCode == EDIT_GALLERY_REQUEST_CODE && resultCode == Activity.RESULT_OK){
			//get path to picture taken
			imageUri = Uri.parse(data.getDataString());

			//retrieve from database
			Cursor cursor = getContentResolver().query(imageUri, null, null, null, null);
			cursor.moveToFirst();
			int idx = cursor.getColumnIndex(MediaStore.Images.ImageColumns.DATA);

			//get the path to the real file
			String filepath = cursor.getString(idx);
			imageUri = Uri.fromFile(new File(filepath));

			SavablePlayer old = players.get(playerBeingEdited);
			SavablePlayer p = new SavablePlayer(old.getUUID(), old.getName(), old.isGhost(), old.getSeedValue(), filepath);
			players.set(playerBeingEdited, p);
			if (selectedPlayers.contains(old)){
				selectedPlayers.set(selectedPlayers.indexOf(old), p);
			}
			if (addedPlayers.contains(old)){
				addedPlayers.set(addedPlayers.indexOf(old), p);
			}
			if (recentPlayers.contains(old)){
				recentPlayers.set(recentPlayers.indexOf(old), p);
				StorageManager.saveSavable("SavedPlayers", recentPlayers, this);
			}
		}
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu){
		MenuInflater inflater = getMenuInflater();
		inflater.inflate(R.menu.activity_configuration, menu);
		return true;
	}

	@Override
	public void onCreateContextMenu(ContextMenu menu, View v, ContextMenuInfo menuInfo) {
		super.onCreateContextMenu(menu, v, menuInfo);
		MenuInflater inflater = getMenuInflater();
		inflater.inflate(R.menu.activity_configuration_context, menu);

	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item){
		//handle item
		switch(item.getItemId()){
		case R.id.Clear:
			clearPressedFromMenu();
			return true;
		case R.id.Profiles:
			profilesPressedFromMenu();
			return true;
		case R.id.Help:
			showHelp();
			return true;
		}

		return super.onOptionsItemSelected(item);
	}

	@Override
	public void onPause(){
		super.onPause();

		//save added players with loaded players
		recentPlayers.addAll(addedPlayers);
		StorageManager.saveSavable(SAVED_PLAYER_LIST, recentPlayers, this);
	}

	/**
	 * Displays the help text
	 */
	private void showHelp() {

		// Create and show the help dialog.
		final Dialog dialog = new Dialog(TournamentConfigurationActivity.this);
		dialog.setContentView(R.layout.activity_config_help);
		dialog.setTitle("Tournament Configuration Help");
		dialog.setCancelable(true);
		Button closeButton = (Button) dialog.findViewById(R.id.help_close_button);
		closeButton.setOnClickListener(new Button.OnClickListener() {      
			public void onClick(View view) { 
				dialog.dismiss();     
			}
		});
		dialog.show();

		StorageManager.saveBoolean("ConfigFirstUse", false, this);
	}

	/**
	 * Invoked when profiles is pressed from menu. Displays profile screen.
	 */
	private void profilesPressedFromMenu() {
		Intent i = new Intent(this,ProfileActivity.class);
		startActivity(i);
	}

	/**
	 * Invoked when clear is pressed from menu. Displays an alert dialog, and clears the
	 * list of players (not profiles) if the user chooses yes
	 */
	private void clearPressedFromMenu() {
		// Create and show the warning dialog.
		DialogFragment warning = new ClearWarningFragment();
		warning.show(getSupportFragmentManager(), "Clear Warning");

	}

	@Override
	public boolean onContextItemSelected(MenuItem item){
		AdapterContextMenuInfo info = (AdapterContextMenuInfo) item.getMenuInfo();
		switch (item.getItemId()) {
		case R.id.Check:
			checkPressedFromContext(info.position);
			return true;
		case R.id.Edit:
			editPressedFromContext(info.position);
			return true;
		case R.id.Delete:
			deletePressedFromContext(info.position);
			return true;
		}
		return super.onContextItemSelected(item);
	}


	/**
	 * Called when delete is pressed from context. Removes the item from the list, does not work
	 * on profiles
	 * @param position The position of the item.
	 */
	protected void deletePressedFromContext(int position) {

		//if it is a profile, tell user that profiles can't be deleted here
		if (players.get(position).equals(profile)){
			showError("You can't delete profiles here.");
			return;
		} else if (otherProfiles.contains(players.get(position))){
			showError("You can't delete profiles here.");
			return;
		}

		if (position == playerBeingEdited){
			playerBeingEdited = -1;
		}

		//if it is selected, remove it from selected
		selectedPlayers.remove(players.get(position));

		//remove the player from the configuration object
		configuration.getPlayers().remove(players.get(position));

		//if it is in loaded players, remove it from loaded players
		recentPlayers.remove(players.get(position));
		StorageManager.saveSavable(SAVED_PLAYER_LIST, recentPlayers, this);

		//if it is in added players, remove it from added players
		addedPlayers.remove(players.get(position));

		//remove it from players
		players.remove(position);

		EditText playerEdit = (EditText)findViewById(R.id.playerNameField);
		//Make the player hint change based on the number of players
		playerEdit.setHint(this.getNextPlayerNameHint());

		reloadList();
	}

	/**
	 * Called when edit is pressed from context. Allows the name and picture to be edited.
	 * @param position The position of the item
	 */
	protected void editPressedFromContext(int position) {
		Player p = players.get(position);
		if (p.equals(profile) || otherProfiles.contains(p)){
			showError("You cannot edit profiles from here.");
		} else {
			playerBeingEdited = position;
			reloadList();
		}
	}

	/**
	 * Called when check is pressed from context. Selects or deselects the item.
	 * @param position The position of the item
	 */
	protected void checkPressedFromContext(int position) {
		SavablePlayer p = players.get(position);
		for (int i = 0; i < selectedPlayers.size(); i++){
			if (selectedPlayers.get(i).equals(p)){
				selectedPlayers.remove(i);
				reloadList();
				return;
			}
		}
		selectedPlayers.add(p);

		reloadList();
	}

	/**
	 * Loads all profiles on the device and adds them to the list of selectable players
	 */
	private void loadProfiles(){

		SavableProfileList loadedProfiles = null;
		try {
			loadedProfiles = (SavableProfileList)StorageManager.loadSavable(ProfileActivity.PROFILE_LIST_KEY, SavableProfileList.class, this, new SavableProfileList());
		} catch (InstantiationException e) {
			e.printStackTrace();
		} catch (IllegalAccessException e) {
			e.printStackTrace();
		}

		if (loadedProfiles.size() > 0){
			selectedProfile = loadedProfiles.getSelectedProfile();
			loadedProfiles.sort();
			SavablePlayer newPlayer = new SavablePlayer(selectedProfile.getId(), selectedProfile.getName(), false, -1, selectedProfile.getFile());
			players.add(newPlayer);
			profile = newPlayer;
		}


		for (int i = 0; i < loadedProfiles.size(); i++){
			Profile p = loadedProfiles.get(i);
			if (!p.equals(selectedProfile)){
				SavablePlayer newPlayer = new SavablePlayer(p.getId(), p.getName(), false, -1, p.getFile());
				players.add(newPlayer);
				otherProfiles.add(newPlayer);
			}
		}

		if (selectedProfile == null){
			showError("Please create a profile first.");
			finish();
		}

		//Update saved profile information in tournament configuration
		for (SavablePlayer p : selectedPlayers){
			if (p.equals(selectedProfile)){
				p.setName(selectedProfile.getName());
				p.setPortraitFilepath(selectedProfile.getPortraitFilepath());
				break;
			}
		}
	}

	/**
	 * Loads the recent player list and adds it to the list of selectable players
	 */
	private void loadPlayers(){
		//load players from storage
		recentPlayers = new SavablePlayerList();
		SavablePlayerList loadedPlayers = new SavablePlayerList();
		try {
			loadedPlayers = (SavablePlayerList) StorageManager.loadSavable(SAVED_PLAYER_LIST, SavablePlayerList.class, this, null);
		} catch (InstantiationException e) {
			e.printStackTrace();
		} catch (IllegalAccessException e) {
			e.printStackTrace();
		}

		if (loadedPlayers == null){
			loadedPlayers = new SavablePlayerList();
		}

		//if there's any players in the configuration object, add them to recentPlayers
		List<SavablePlayer> configPlayers = configuration.getPlayers();
		for (int i = 0; i < configPlayers.size(); i++){
			SavablePlayer p = configPlayers.get(i);
			if (!recentPlayers.contains(p) && !players.contains(p)){
				recentPlayers.add(p);
			}
		}

		//if there's any players in loaded players, add them to recent players
		for (int i = 0; i < loadedPlayers.size(); i++){
			SavablePlayer p = loadedPlayers.get(i);
			if (!recentPlayers.contains(p) && !players.contains(p)){
				recentPlayers.add(p);
			}
		}

		recentPlayers.sort();

		//resave the loaded players in case any collisions need to be fixed
		StorageManager.saveSavable(SAVED_PLAYER_LIST, recentPlayers, this);

		//add all the loaded players to players
		players.addAll(recentPlayers);

		EditText playerEdit = (EditText)findViewById(R.id.playerNameField);
		//Make the player hint change based on the number of players
		playerEdit.setHint(this.getNextPlayerNameHint());


		//if there is no one in the tournament, select the profile by default
		if (selectedPlayers.size() == 0){
			selectedPlayers.add(profile);
		}
	}

	/**
	 * Reloads the list UI
	 */
	private void reloadList(){
		ListView list = (ListView)findViewById(R.id.listView1);
		list.setAdapter(new IconicAdapter());
		list.setOnCreateContextMenuListener(this);
		registerForContextMenu(list);

		//update the number of players label
		TextView t = (TextView) this.findViewById(R.id.num_players);
		if(selectedPlayers!=null) {
			t.setText(selectedPlayers.size()+"");
		} else {
			t.setText("0");
		}
	}

	/**
	 * Responsible for initializing all instance variables of this class
	 */
	private void initializeInstanceVariables(){
		Bundle extra = getIntent().getExtras();

		//load the configuration list
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

		//if no extras, we need to make a new configuration
		if (extra == null){
			configuration = new TournamentConfiguration();
			selectedPlayers = new SavablePlayerList();
		} else { //else load an existing core
			long coreId = extra.getLong("tournamentId");
			tournamentCore = Core.getCoreInstance(coreId);
			configuration = configurationList.get(tournamentCore.getConfigIndex());

			//synchronize configuration with core
			List<Player> players = tournamentCore.getPlayerList();
			SavablePlayerList savablePlayers = new SavablePlayerList();
			for (Player p : players){
				savablePlayers.add(new SavablePlayer(p));
			}
			configuration.setPlayers(savablePlayers);
			selectedPlayers = configuration.getPlayers();

			Button saveButton = (Button)findViewById(R.id.saveButton);
			saveButton.setText("Apply Changes");

		} //NOTE: the core is not modified (or created if new) until the save button is pressed

		imageUri = Uri.parse("/");

	}

	/**
	 * Responsible for initializing all view listeners for this class
	 */
	private void initializeListeners(){
		EditText tournamentName = (EditText)findViewById(R.id.nameTextField);
		tournamentName.setText(configuration.getTournamentName());
		if (configuration.getTournamentName().equals("")){
			tournamentName.setHint("Tournament "+(configurationList.size()+1));
		}

		Spinner pluginSelector = (Spinner)findViewById(R.id.pluginSelectionSpinner);

		Button saveButton = (Button)findViewById(R.id.saveButton);
		saveButton.setOnClickListener(new SaveButtonListener(tournamentName, pluginSelector));

		ImageButton addButton = (ImageButton)findViewById(R.id.addButton);
		addButton.setOnClickListener(new OnClickListener(){

			@Override
			public void onClick(View v) {
				addPlayerButtonPressed();
			}
		});

		ImageButton cameraButton = (ImageButton)findViewById(R.id.cameraButton);
		cameraButton.setOnClickListener(new OnClickListener(){

			public void onClick(View v) {
				//make sure a folder is there
				File folder = new File(Environment.getExternalStorageDirectory()+"/utool");
				boolean success = true;
				if (!folder.exists()){
					success = folder.mkdir();
				}
				if (success){
					//start the camera stuff
					Intent i = new Intent("android.media.action.IMAGE_CAPTURE");

					File photo = new File(Environment.getExternalStorageDirectory(), 
							"utool/cameraPic"+System.currentTimeMillis()+".jpg");
					i.putExtra(MediaStore.EXTRA_OUTPUT, Uri.fromFile(photo));
					imageUri = Uri.fromFile(photo);
					startActivityForResult(i, CAMERA_REQUEST_CODE);
				} else {
					//Notify the user that the SD card was inaccessible
					showError("SD card is currently inaccessible.");
					Log.e("TournamentConfigActivity", "Unable to access folder: /utool");
				}
			}
		});

		//setup gallery button
		ImageButton galleryButton = (ImageButton)findViewById(R.id.galleryButton);
		galleryButton.setOnClickListener(new OnGalleryButtonPressed(false));

		ImageView portrait = (ImageView)findViewById(R.id.playerPortrait);
		portrait.setOnClickListener(new OnClickListener(){

			@Override
			public void onClick(View v) {
				ClearPortraitFragment f = new ClearPortraitFragment();
				f.show(getSupportFragmentManager(), "clear portrait");
			}

		});

		loadPluginList();
	}

	/**
	 * Handler for when the add player button is pressed
	 */
	protected void addPlayerButtonPressed(){
		//get the player name
		EditText playerEdit = (EditText)findViewById(R.id.playerNameField);
		String name = playerEdit.getText().toString();
		if (name.equals("")){
			name = playerEdit.getHint().toString();
		}

		//get the player filepath
		String filepath = imageUri.getPath();

		//add the player to the list
		SavablePlayer p = new SavablePlayer(UUID.randomUUID(), name, false, -1, filepath);
		players.add(p);
		addedPlayers.add(p);
		selectedPlayers.add(p);

		//clear the text box
		playerEdit.setText("");
		//Make the player hint change based on the number of players
		playerEdit.setHint(this.getNextPlayerNameHint()); 

		//reset the portrait and the filepath
		ImageView portrait = (ImageView)findViewById(R.id.playerPortrait);
		portrait.setImageResource(R.drawable.silhouette);
		imageUri = Uri.parse("/");

		reloadList();
	}

	/**
	 * Returns the next logical number for an added player. This will 
	 * return the next number larger than the total number of 
	 * players that is not in use. Ignores whitespace and case when
	 * determining equality. Ex: "	PlAYer 2 " and "Player 2" are 
	 * considered equal.
	 * @return player hint text
	 */
	private String getNextPlayerNameHint(){
		int index = (recentPlayers.size()+addedPlayers.size()+1);
		String ret = "Player "+index;
		//true if in list
		boolean inList=true;

		//loop until a uniquely numbered player name is found
		while(inList)
		{
			//determine if in list of players
			boolean hasBeenFound=false;
			//look through recent
			for(int i=0;i<this.recentPlayers.size();i++){
				//determine equality ignoring case and leading/trailing whitespace
				if(recentPlayers.get(i).getName().trim().equalsIgnoreCase(ret)) {
					hasBeenFound=true;
					break;
				}
			}

			//look through added players
			if(!hasBeenFound) {
				for(int i=0;i<this.addedPlayers.size();i++) {
					//determine equality ignoring case and leading/trailing whitespace
					if(addedPlayers.get(i).getName().trim().equalsIgnoreCase(ret)) {
						hasBeenFound=true;
						break;
					}
				}
			}		
			if(!hasBeenFound) {
				//name wasn't found therefore its safe to return it
				inList=false;
			} else {
				//increment index and stay in while loop
				index++;
				ret = "Player "+index;
			}
		}

		return ret;
	}

	/**
	 * Loads the plugin list and populates the spinner
	 */
	private void loadPluginList(){
		//Clean out existing plugin information, in case this is a reload
		ArrayList<String> discoveredPlugins = new ArrayList<String>();
		intentMap = new SparseArray<Intent>();

		//Get list of all available plugins
		PackageManager packageManager = getPackageManager();
		Intent baseIntent = new Intent( ACTION_PICK_PLUGIN );
		baseIntent.addCategory(CATEGORY_PLUGIN);
		baseIntent.setFlags( Intent.FLAG_DEBUG_LOG_RESOLUTION );

		List<ResolveInfo> list = packageManager.queryIntentActivities(baseIntent, PackageManager.GET_RESOLVED_FILTER );

		int pluginIndex = -1;

		//add plugins to list and map their index to a new intent
		for(int i = 0 ; i < list.size() ; i++) {
			ResolveInfo info = list.get( i );
			String plugin = info.activityInfo.applicationInfo.loadLabel(getPackageManager()).toString();
			discoveredPlugins.add(plugin);


			Intent intent = new Intent(Intent.ACTION_MAIN);
			intent.setClassName(info.activityInfo.packageName, info.activityInfo.name);
			intentMap.put(i, intent);

			if (configuration.getPluginName().equals(info.activityInfo.name) &&
					configuration.getPluginPackage().equals(info.activityInfo.packageName)){
				pluginIndex = i;
			}
		}

		setUpAdapter(pluginIndex, discoveredPlugins);
	}

	/**
	 * Responsible for setting up the spinner adapter to display the plugins
	 * @param pluginIndex The index of the plugin to show
	 * @param discoveredPlugins The list of discovered plugins
	 */
	private void setUpAdapter(int pluginIndex, ArrayList<String> discoveredPlugins){

		Spinner s = (Spinner)findViewById(R.id.pluginSelectionSpinner);

		ArrayAdapter<String> spinnerAdapter = new ArrayAdapter<String>(this, android.R.layout.simple_spinner_item, discoveredPlugins);
		spinnerAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
		s.setAdapter(spinnerAdapter);
		if (pluginIndex > -1){
			s.setSelection(pluginIndex);
		}
	}

	/**
	 * Displays error text to the user as a toast
	 * @param text The text to display
	 */
	private void showError(String text){
		Toast.makeText(this, text, Toast.LENGTH_LONG).show();
	}

	/**
	 * Listener for when the save button is pressed
	 * @author Justin Kreier
	 * @version 1/10/2013
	 */
	private class SaveButtonListener implements OnClickListener{

		/**
		 * Reference to the tournament name edit field
		 */
		private EditText tournamentName;

		/**
		 * Reference to the plugin selection spinner
		 */
		private Spinner pluginSelector;

		/**
		 * Constructor
		 * @param name The tournament name edit field
		 * @param plugin The plugin selection spinner
		 */
		public SaveButtonListener(EditText name, Spinner plugin){
			tournamentName = name;
			pluginSelector = plugin;
		}

		@Override
		public void onClick(View v) {

			String name = tournamentName.getText().toString();
			if (name.equals("")){
				EditText tName = (EditText)findViewById(R.id.nameTextField);
				name = tName.getHint().toString();
			}

			// find the selected plugin
			Intent intent = intentMap.get(pluginSelector.getSelectedItemPosition());

			// there is no intent
			if( intent == null) {
				Button button = (Button)v; 
				loadPluginList();
				if( intentMap.size() == 0) {
					button.setText("Need to install at least one UTool Tournament Plugin! Press to Reload Types..");
				} else {
					button.setText("Create Tournament");
				}
				
				return;
			}

			//apply changes to configuration
			configuration.setPluginName(intent.getComponent().getClassName());
			configuration.setPluginPackage(intent.getComponent().getPackageName());
			configuration.setTournamentName(name);
			configuration.setPlayers(selectedPlayers);

			//apply changes to tournamentCore
			ArrayList<Player> nonSavablePlayers = new ArrayList<Player>();
			for (int i = 0; i < selectedPlayers.size(); i++){
				nonSavablePlayers.add(selectedPlayers.get(i).toPlayer());
			}

			if (tournamentCore == null){
				tournamentCore = Core.getCoreInstance(configuration.getTournamentUUID());
				tournamentCore.setTournamentLocation(TournamentLocationEnum.Local);
				tournamentCore.setPermissionLevel(Player.HOST);
				tournamentCore.setConfigIndex(configurationList.size());
				configurationList.add(configuration);
			} else {
				configurationList.set(tournamentCore.getConfigIndex(), configuration);
			}
			tournamentCore.setPlayerList(nonSavablePlayers);
			tournamentCore.setPluginIntent(intent);
			tournamentCore.setTournamentName(name);

			Intent resultIntent = new Intent();
			resultIntent.putExtra("tournamentId", tournamentCore.getTournamentId());
			resultIntent.putExtra("playerList", tournamentCore.getPlayerList().toArray());
			resultIntent.putExtra("tournamentName", tournamentCore.getTournamentName());
			setResult(RESULT_OK, resultIntent);

			tournamentCore.setDeviceId(selectedProfile.getId());
			StorageManager.saveSavable("ConfigurationList", configurationList, TournamentConfigurationActivity.this);

			//send updated player list
			tournamentCore.sendPlayerList();

			finish();
		}
	}

	/**
	 * This class is responsible for setting up the list of players to display in the list view
	 * @author Justin Kreier
	 * @version 1/10/2013
	 */
	private class IconicAdapter extends ArrayAdapter<SavablePlayer>{

		/**
		 * Simple constructor to hide the annoying stuff
		 */
		public IconicAdapter(){
			super(TournamentConfigurationActivity.this, R.layout.row_player, players);
		}

		@Override
		public View getView(int position, View convertView, ViewGroup parent){
			if (playerBeingEdited != position){
				return nonEditLineInitialize(position, convertView, parent);
			} else {
				return editLineInitialize(position, convertView, parent);
			}
		}

		/**
		 * Method for inflating all the views in the list
		 * @param position The position
		 * @param convertView The old view
		 * @param parent The parent
		 * @return The inflated view
		 */
		private View nonEditLineInitialize(int position, View convertView, ViewGroup parent){
			LayoutInflater inflater = getLayoutInflater();

			ImageView portrait;
			if (convertView == null){
				convertView = inflater.inflate(R.layout.row_player, parent, false);
				portrait = (ImageView)convertView.findViewById(R.id.playerPortrait);
			} else {
				portrait = (ImageView)convertView.findViewById(R.id.playerPortrait);
				Object o = portrait.getTag();
				if (o != null && o instanceof ImageDecodeTask){
					ImageDecodeTask t = (ImageDecodeTask)o;
					t.cancel(true);
				}

				convertView = inflater.inflate(R.layout.row_player, parent, false);
				portrait = (ImageView)convertView.findViewById(R.id.playerPortrait);
			}


			//Async load the portrait
			if (players.get(position).hasPortraitChanged()){
				portrait.setImageResource(R.drawable.silhouette);
				ImageDecodeTask task = new ImageDecodeTask(portrait);
				portrait.setTag(task);
				task.execute(players.get(position));
			} else {
				Bitmap bm = players.get(position).getPortrait();
				if (bm != null && !bm.isRecycled()){
					portrait.setImageBitmap(bm);
				} else {
					portrait.setImageResource(R.drawable.silhouette);
				}
			}
			//end async load

			SavablePlayer p = players.get(position);

			//set the profile name
			TextView playerName = (TextView)convertView.findViewById(R.id.playerName);
			playerName.setText(p.getName());

			//set the colors of the names
			for (int i = 0; i < otherProfiles.size(); i++){
				if (otherProfiles.get(i).equals(players.get(position))){
					playerName.setTextColor(0xFFFF7F24/*light orange*/);
					break;
				}
			}
			if (profile.equals(players.get(position))){
				playerName.setTextColor(Color.CYAN);
			}
			for (int i = 0; i < recentPlayers.size(); i++){
				if (recentPlayers.get(i).equals(players.get(position))){
					playerName.setTextColor(Color.GREEN);
					break;
				}
			}
			for (int i = 0; i < addedPlayers.size(); i++){
				if (addedPlayers.get(i).equals(players.get(position))){
					playerName.setTextColor(Color.YELLOW);
					break;
				}
			}

			//set the listener for when the items are checked
			CheckBox box = (CheckBox)convertView.findViewById(R.id.checkBox);

			for (int i = 0; i < selectedPlayers.size(); i++){
				if (selectedPlayers.get(i).equals(players.get(position))){
					box.setChecked(true);
					break;
				}
			}

			final int finalPos = position;
			box.setOnCheckedChangeListener(new OnCheckedChangeListener(){

				@Override
				public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
					if (isChecked){
						//add to selected
						selectedPlayers.add(players.get(finalPos));


					} else {

						selectedPlayers.remove(players.get(finalPos));
					}

					//update the number of players label
					TextView t = (TextView) findViewById(R.id.num_players);
					if(selectedPlayers!=null) {
						t.setText(selectedPlayers.size()+"");
					} else {
						t.setText("0");
					}
				}
			});

			//if not the host or deviceless, then they're connected
			if (players.get(position).getPermissionsLevel() != Player.DEVICELESS && 
					players.get(position).getPermissionsLevel() != Player.HOST){

				//show the icon
				ImageView connectIcon = (ImageView)convertView.findViewById(R.id.connectionIcon);
				connectIcon.setBackgroundResource(R.drawable.wireless_connection_icon);
			}

			return convertView;
		}

		/**
		 * Method for inflating the edit line view
		 * @param position The position
		 * @param convertView The old view
		 * @param parent The parent
		 * @return The inflated view
		 */
		private View editLineInitialize(final int position, View convertView, ViewGroup parent){
			LayoutInflater inflater = getLayoutInflater();

			ImageView portrait;
			if (convertView == null){
				convertView = inflater.inflate(R.layout.row_player_edit, parent, false);
				portrait = (ImageView)convertView.findViewById(R.id.playerPortrait);
			} else {
				portrait = (ImageView)convertView.findViewById(R.id.playerPortrait);
				Object o = portrait.getTag();
				if (o != null && o instanceof ImageDecodeTask){
					ImageDecodeTask t = (ImageDecodeTask)o;
					t.cancel(true);
				}

				convertView = inflater.inflate(R.layout.row_player_edit, parent, false);
				portrait = (ImageView)convertView.findViewById(R.id.playerPortrait);
			}

			//Async load the portrait
			if (players.get(position).hasPortraitChanged()){
				portrait.setImageResource(R.drawable.silhouette);
				ImageDecodeTask task = new ImageDecodeTask(portrait);
				portrait.setTag(task);
				task.execute(players.get(position));
			} else {
				Bitmap bm = players.get(position).getPortrait();
				if (bm != null && !bm.isRecycled()){
					portrait.setImageBitmap(bm);
				} else {
					portrait.setImageResource(R.drawable.silhouette);
				}
			}
			//end async load

			SavablePlayer p = players.get(position);

			final EditText edit = (EditText)convertView.findViewById(R.id.playerName);
			edit.setText(p.getName());

			portrait.setOnClickListener(new OnClickListener(){
				@Override
				public void onClick(View v) {
					DeletePortraitFragment f = new DeletePortraitFragment();
					f.show(getSupportFragmentManager(), "delete portrait");
				}
			});

			ImageButton saveButton = (ImageButton)convertView.findViewById(R.id.saveButton);
			saveButton.setOnClickListener(new OnClickListener(){

				@Override
				public void onClick(View view) {
					String newName = edit.getText().toString();
					SavablePlayer old = players.get(position);
					SavablePlayer p = new SavablePlayer(old.getUUID(), newName, old.isGhost(), old.getSeedValue(), old.getPortraitFilepath());
					players.set(position, p);
					if (selectedPlayers.contains(old)){
						selectedPlayers.set(selectedPlayers.indexOf(old), p);
					}
					if (addedPlayers.contains(old)){
						addedPlayers.set(addedPlayers.indexOf(old), p);
					}
					if (recentPlayers.contains(old)){
						recentPlayers.set(recentPlayers.indexOf(old), p);
						StorageManager.saveSavable("SavedPlayers", recentPlayers, TournamentConfigurationActivity.this);
					}

					playerBeingEdited = -1;

					reloadList();
				}
			});

			//setup camera button
			ImageButton cameraButton = (ImageButton)convertView.findViewById(R.id.cameraButton);
			cameraButton.setOnClickListener(new OnClickListener(){

				public void onClick(View v) {
					//make sure a folder is there
					File folder = new File(Environment.getExternalStorageDirectory()+"/utool");
					boolean success = true;
					if (!folder.exists()){
						success = folder.mkdir();
					}
					if (success){
						//start the camera stuff
						Intent i = new Intent("android.media.action.IMAGE_CAPTURE");

						File photo = new File(Environment.getExternalStorageDirectory(), 
								"utool/cameraPic"+System.currentTimeMillis()+".jpg");
						i.putExtra(MediaStore.EXTRA_OUTPUT, Uri.fromFile(photo));
						imageUri = Uri.fromFile(photo);
						startActivityForResult(i, EDIT_CAMERA_REQUEST_CODE);
					} else {
						//Notify the user that the SD card was inaccessible
						showError("SD card is currently inaccessible.");
						Log.e("TournamentConfigActivity", "Unable to access folder: /utool");
					}
				}
			});

			//setup camera button
			ImageButton galleryButton = (ImageButton)convertView.findViewById(R.id.galleryButton);
			galleryButton.setOnClickListener(new OnGalleryButtonPressed(true));

			return convertView;
		}
	}

	/**
	 * Listener for when the Gallery button is pressed in the edit field
	 * @author kreierj
	 * @version 4/25/2012
	 *
	 */
	private class OnGalleryButtonPressed implements OnClickListener{

		/**
		 * holds whether this listener is an edit or add listener
		 */
		private boolean isEdit;
		/**
		 * constructor for a Gallery button pressed listener
		 * @param isEdit true if it is the edit gallery button, false if it is the add player gallery button
		 */
		protected OnGalleryButtonPressed(boolean isEdit)
		{
			this.isEdit=isEdit;
		}
		@Override
		public void onClick(View v) {
			//start an activity to retrieve a gallery photo from the file system
			Intent i = new Intent(Intent.ACTION_PICK,android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
			if(isEdit) {
				startActivityForResult(i, EDIT_GALLERY_REQUEST_CODE);
			} else {
				startActivityForResult(i, GALLERY_REQUEST_CODE);
			}
		}
	}

	/**
	 * Responsible for displaying a warning if the user presses clear
	 * @author Justin Kreier
	 * @version 1/12/2013
	 */
	@SuppressLint("ValidFragment")
	private class ClearWarningFragment extends DialogFragment {

		@Override
		public Dialog onCreateDialog(Bundle savedInstanceState){
			//Note: Android wants this class to be public and static, but then it also requires profiles to be static or final
			//which I did not want to do. It seems to work as is, so until I see a reason to listen to this warning, I'd prefer to ignore it
			AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
			builder.setMessage("Are you sure you want to clear all players?");

			builder.setNegativeButton("No", null);
			builder.setPositiveButton("Yes", new DialogInterface.OnClickListener(){

				@Override
				public void onClick(DialogInterface dialog, int which) {
					players.clear();
					addedPlayers.clear();
					recentPlayers.clear();

					//add the profiles back in because they're deleted elsewhere
					players.add(profile);
					players.addAll(otherProfiles);

					//save the player list
					StorageManager.saveSavable(SAVED_PLAYER_LIST, recentPlayers, TournamentConfigurationActivity.this);

					//remove all from selected players that are not profiles
					for (int i = 0; i < selectedPlayers.size(); ){
						if (selectedPlayers.get(i).equals(profile)){
							i++;
						} else if (otherProfiles.contains(selectedPlayers.get(i))){
							i++;
						} else {
							selectedPlayers.remove(i);
						}
					}

					EditText playerEdit = (EditText)findViewById(R.id.playerNameField);
					//Make the player hint change based on the number of players
					playerEdit.setHint(getNextPlayerNameHint());

					reloadList();
				}
			});

			return builder.create();
		}
	}

	/**
	 * Responsible for letting the user remove portraits from the edit line
	 * @author Justin Kreier
	 * @version 3/10/2013
	 */
	@SuppressLint("ValidFragment")
	private class DeletePortraitFragment extends DialogFragment {

		@Override
		public Dialog onCreateDialog(Bundle savedInstanceState){
			//Note: Android wants this class to be public and static, but then it also requires profiles to be static or final
			//which I did not want to do. It seems to work as is, so until I see a reason to listen to this warning, I'd prefer to ignore it
			AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
			builder.setMessage("Do you want to remove the portrait?");

			builder.setNegativeButton("No", null);
			builder.setPositiveButton("Yes", new DialogInterface.OnClickListener(){

				@Override
				public void onClick(DialogInterface dialog, int which) {
					SavablePlayer old = players.get(playerBeingEdited);
					SavablePlayer p = new SavablePlayer(old.getUUID(), old.getName(), old.isGhost(), old.getSeedValue(), "");
					players.set(playerBeingEdited, p);
					if (selectedPlayers.contains(old)){
						selectedPlayers.set(selectedPlayers.indexOf(old), p);
					}
					if (addedPlayers.contains(old)){
						addedPlayers.set(addedPlayers.indexOf(old), p);
					}
					if (recentPlayers.contains(old)){
						recentPlayers.set(recentPlayers.indexOf(old), p);
						StorageManager.saveSavable("SavedPlayers", recentPlayers, TournamentConfigurationActivity.this);
					}

					reloadList();
				}
			});

			return builder.create();
		}
	}

	/**
	 * Popup dialog for clearing a portrait
	 * @author Justin Kreier
	 * @version 3/17/2013
	 */
	@SuppressLint("ValidFragment")
	private class ClearPortraitFragment extends DialogFragment {

		@Override
		public Dialog onCreateDialog(Bundle savedInstanceState){
			//Note: Android wants this class to be public and static, but then it also requires profiles to be static or final
			//which I did not want to do. It seems to work as is, so until I see a reason to listen to this warning, I'd prefer to ignore it
			AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
			builder.setMessage("Do you want to remove the portrait?");

			builder.setNegativeButton("No", null);
			builder.setPositiveButton("Yes", new DialogInterface.OnClickListener(){

				@Override
				public void onClick(DialogInterface dialog, int which) {
					imageUri = Uri.fromFile(new File(""));

					//get the camera ImageButton
					ImageView portrait = (ImageView)findViewById(R.id.playerPortrait);

					portrait.setImageResource(R.drawable.silhouette);
				}

			});

			return builder.create();
		}
	}

	/**
	 * A notification fragment which will display text to a user with a clickable "Ok" box
	 * @author Justin Kreier
	 * @version 1/18/2013
	 */
	@SuppressLint("ValidFragment")
	public static class HelpDialog extends DialogFragment{

		/**
		 * The message to display
		 */
		private String message;

		/**
		 * Constructor
		 * @param message The message you want to display when show() is called
		 */
		public HelpDialog(String message){
			this.message = message;
		}

		@Override
		public Dialog onCreateDialog(Bundle savedInstanceState){
			//Note: Android wants this class to be public and static, but then it also requires profiles to be static or final
			//which I did not want to do. It seems to work as is, so until I see a reason to listen to this warning, I'd prefer to ignore it
			AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
			builder.setMessage(message);

			builder.setPositiveButton("Ok", null);

			return builder.create();
		}
	}

}
