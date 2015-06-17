package utool.core;


import java.io.File;

import java.util.Collections;
import java.util.UUID;

import utool.persistence.Profile;
import utool.persistence.SavableProfileList;
import utool.persistence.StorageManager;
import utool.plugin.ImageDecodeTask;
import utool.plugin.PictureLoader;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
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
import android.view.ContextMenu;
import android.view.ContextMenu.ContextMenuInfo;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewTreeObserver;
import android.view.ViewTreeObserver.OnGlobalLayoutListener;
import android.view.WindowManager;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.AdapterView.AdapterContextMenuInfo;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.RadioButton;
import android.widget.TextView;
import android.widget.Toast;

/**
 * This class is responsible for allowing the user to create, select, and manage their profiles.
 * @author Justin Kreier
 * @version 1/6/2013
 */
@SuppressLint("NewApi")
public class ProfileActivity extends FragmentActivity{

	/**
	 * The key used for saving and retrieving profiles from preferences
	 */
	public static final String PROFILE_LIST_KEY = "Profile_Key";

	/**
	 * The key used for saving and retrieving uses from preferences
	 */
	public static final String PROFILE_USE_KEY = "Profile_Use_Key";

	/**
	 * Help text displayed when there are no profiles
	 */
	private static final String CREATION_HELP_TEXT= "Please enter your name and choose a picture for your profile. A profile is" +
			" required to use this application. This is how other users will see you.";

	/**
	 * Help text displayed to first time users
	 */
	private static final String ACTION_HELP_TEXT= "Click a profile to select it. Hold for options.";

	/**
	 * Request code for the camera
	 */
	private static final int CAMERA_REQUEST_CODE = 1;

	/**
	 * Request code for the gallery
	 */
	private static final int GALLERY_REQUEST_CODE = 2;

	/**
	 * Tag for logger output
	 */
	private static final String LOG_TAG = "utool.core.ProfileManagementActivity";

	/**
	 * List of profiles to display
	 */
	protected SavableProfileList profiles;

	/**
	 * The current profile being edited
	 */
	protected int indexToEdit;

	/**
	 * true if the edit pane is open
	 */
	protected boolean isEditOpen;

	/**
	 * Reference to an imageUri
	 */
	protected Uri imageUri;

	/**
	 * Integer flag specifying if this is a user's first time, second time, or any other time using this screen
	 * 0 - First use
	 * 1 - Second use
	 * 2 - Other
	 */
	protected int numUse;

	/**
	 * boolean indicating if the activity should create a home screen intent when finishing or just finish
	 */
	private boolean createOnSave;

	@Override
	public void onCreate(Bundle savedInstanceState){
		super.onCreate(savedInstanceState);

		Bundle e = getIntent().getExtras();
		if (e != null){
			createOnSave = e.getBoolean("createOnSave", false);
		}
		if(Build.VERSION.SDK_INT < Build.VERSION_CODES.HONEYCOMB){
			//hide the title bar
			this.requestWindowFeature(Window.FEATURE_NO_TITLE);
			setContentView(R.layout.activity_profile_management);
		} else {
			//Show the action bar on newer devices, for the menu
			setContentView(R.layout.activity_profile_management);
			getActionBar().setDisplayShowHomeEnabled(false);
			getActionBar().setDisplayShowTitleEnabled(false);
		}
		//		TODO: The above code throws tons of errors under test circumstances, no
		//		idea how to fix

		//initialize instance variables
		initializeInstanceVariables();

		initializeListeners();

		//hide the edit fields
		toggleEditFields(false);
		
		
		//Code to resize the portrait image view properly
		final ImageView iv = (ImageView)findViewById(R.id.portraitImageView);
		ViewTreeObserver vto = iv.getViewTreeObserver();
		vto.addOnGlobalLayoutListener(new OnGlobalLayoutListener() {

		    @Override
		    public void onGlobalLayout() {
		        int height = iv.getMeasuredHeight();
		        if (height != 0){
		        	iv.setLayoutParams(new LinearLayout.LayoutParams(height,height));
		        	ViewTreeObserver obs = iv.getViewTreeObserver(); 
			        obs.removeGlobalOnLayoutListener(this);
		        }
		        
		    }

		});
		
		
		this.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN | WindowManager.LayoutParams.SOFT_INPUT_ADJUST_PAN);
	}

	@Override
	public void onResume(){
		super.onResume();

		//load all the profiles
		SavableProfileList loadedProfiles = null;
		try {
			loadedProfiles = (SavableProfileList)StorageManager.loadSavable(PROFILE_LIST_KEY, SavableProfileList.class, this, null);
		} catch (InstantiationException e) {
			//ignore exception, occurs when the text in the saves preferences is corrupt.
			//Leaving loaded profiles null will fix the problem when it resaves the profiles.
			Log.e(LOG_TAG, "Profiles failed to load, instantiation exception");
		} catch (IllegalAccessException e) {
			//ignore exception, should not ever happen...
			Log.e(LOG_TAG, "Profiles failed to load, instantiation exception");
		}

		if (loadedProfiles != null){
			profiles = loadedProfiles;
		}



		//load the onUse number
		numUse = StorageManager.loadInt(PROFILE_USE_KEY, this, 0);

		if (numUse == 0 || profiles.size() == 0){
			//set the save button to transition to home screen and increment numUse
			Button saveButton = (Button)findViewById(R.id.saveButton);
			saveButton.setOnClickListener(new OnClickListener(){ 
				@Override
				public void onClick(View v) {
					saveButtonPressed();

					if (numUse == 0){
						numUse = 1;
					}

					if (createOnSave){
						Intent i = new Intent(ProfileActivity.this, HomeActivity.class);
						startActivity(i);
					}

					finish();
				}
			});

		} else if (numUse == 1){
			helpPressedFromMenu();
			numUse = 2;
		}

		//select the correct profile
		selectProfile(profiles.getSelectedProfileIndex());
	}
	
	@Override
	public boolean onCreateOptionsMenu(Menu menu){
		MenuInflater inflater = getMenuInflater();
		inflater.inflate(R.menu.activity_profile_management, menu);
		return true;
	}

	@Override
	public void onPause(){
		super.onPause();

		//save the profiles
		StorageManager.saveSavable(PROFILE_LIST_KEY, profiles, this);

		//save the onUse number
		StorageManager.saveInt(PROFILE_USE_KEY, numUse, this);
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item){
		//handle item
		switch(item.getItemId()){
		case R.id.menu_add:
			addPressedFromMenu();
			return true;
		case R.id.menu_clear:
			clearPressedFromMenu();
			return true;
		case R.id.menu_help:
			helpPressedFromMenu();
			return true;
		}

		return super.onOptionsItemSelected(item);
	}

	@Override
	public void onCreateContextMenu(ContextMenu menu, View v, ContextMenuInfo menuInfo) {
		if (!isEditOpen){
			super.onCreateContextMenu(menu, v, menuInfo);
			MenuInflater inflater = getMenuInflater();
			inflater.inflate(R.menu.activity_profile_managment_context_menu, menu);
		}
	}

	@Override
	public boolean onContextItemSelected(MenuItem item){
		AdapterContextMenuInfo info = (AdapterContextMenuInfo) item.getMenuInfo();
		switch (item.getItemId()) {
		case R.id.context_select:
			selectPressedFromContext(info.position);
			return true;
		case R.id.context_delete:
			deletePressedFromContext(info.position);
			return true;
		case R.id.context_edit:
			editPressedFromContext(info.position);
			return true;
		}
		return super.onContextItemSelected(item);
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data){
		if (requestCode == CAMERA_REQUEST_CODE && resultCode == Activity.RESULT_OK){
			//get path to picture taken
			String filepath = imageUri.getPath();

			//get the camera ImageButton
			ImageView portrait = (ImageView)findViewById(R.id.portraitImageView);

			//set the button's image to the taken picture
			BitmapDrawable image = PictureLoader.loadAndOrientPicture(getResources(), filepath);
			portrait.setImageDrawable(image);

			//update gallery with new image
			sendBroadcast(new Intent(Intent.ACTION_MEDIA_MOUNTED, Uri.fromFile(Environment.getExternalStorageDirectory())));
		} else if (requestCode == GALLERY_REQUEST_CODE && resultCode == RESULT_OK && data != null) {
			//parse the data
			imageUri = Uri.parse(data.getDataString());

			//retrieve from database
			Cursor cursor = getContentResolver().query(imageUri, null, null, null, null);
			cursor.moveToFirst();
			int idx = cursor.getColumnIndex(MediaStore.Images.ImageColumns.DATA);

			//get the path to the real file
			String path = cursor.getString(idx);

			//save that path
			imageUri = Uri.fromFile(new File(path));

			//set the image to the new image
			ImageView portrait = (ImageView)findViewById(R.id.portraitImageView);
			BitmapDrawable d = PictureLoader.loadAndOrientPicture(getResources(),path);
			portrait.setImageDrawable(d);
		}
	}

	/**
	 * Called when delete is pressed from context. Deletes the item from the list.
	 * @param position The index of the list item to delete
	 */
	protected void deletePressedFromContext(int position){
		profiles.remove(position);
		if (profiles.size() == 0){
			//reset portrait back to silhouette
			ImageView portrait = (ImageView)findViewById(R.id.portraitImageView);
			portrait.setImageResource(R.drawable.silhouette);
		}
		reloadListUI();
	}

	/**
	 * Called when edit is pressed from context. Opens the profile in the edit pane.
	 * @param position The index of the list item to edit
	 */
	protected void editPressedFromContext(int position){
		//open the edit pane
		toggleEditFields(true);

		//request focus to the edit pane
		EditText nameField = (EditText)this.findViewById(R.id.nameTextField);
		nameField.requestFocus();


		//set up edit fields
		indexToEdit = position;
		imageUri = Uri.fromFile(new File(profiles.get(position).getFile()));

		nameField.setText(profiles.get(position).getName());

		ImageView portrait = (ImageView)this.findViewById(R.id.portraitImageView);
		Bitmap bm = profiles.get(position).getPortrait();
		if (bm != null){
			portrait.setImageBitmap(profiles.get(position).getPortrait());
		} else {
			portrait.setImageResource(R.drawable.silhouette);
		}
	}

	/**
	 * Called when select is pressed from context. Selects the profile.
	 * @param position The index of the list item to select
	 */
	protected void selectPressedFromContext(int position){
		ListView list = (ListView)findViewById(R.id.listView1);
		View v = list.getAdapter().getView(position, null, null);
		RadioButton r = (RadioButton)v.findViewById(R.id.radioButton);
		r.performClick();
	}

	/**
	 * Called when help is pressed from Menu. Re-displays the first-time-user help text
	 */
	private void helpPressedFromMenu(){
		if (!isEditOpen){ //if the editor is open

			//show the profile creation help text
			final TextView help = (TextView)findViewById(R.id.profile_creation_help_text);
			help.setText(ACTION_HELP_TEXT);
			help.setVisibility(View.VISIBLE);

			help.setOnClickListener(new OnClickListener(){

				@Override
				public void onClick(View v) {
					help.setVisibility(View.GONE);
					help.setOnClickListener(null);

				}

			});
		} else {

			//otherwise show the list help text
			final TextView help = (TextView)findViewById(R.id.profile_creation_help_text);
			help.setText(CREATION_HELP_TEXT);
			help.setVisibility(View.VISIBLE);

			help.setOnClickListener(new OnClickListener(){

				@Override
				public void onClick(View v) {
					help.setVisibility(View.GONE);
					help.setOnClickListener(null);

				}

			});
		}
	}

	/**
	 * Called when clear is pressed from menu. Clears the list of all profiles
	 */
	private void clearPressedFromMenu(){
		// Create and show the warning dialog.
		DialogFragment warning = new ClearWarningFragment();
		warning.show(getSupportFragmentManager(), "Clear Warning");
	}

	/**
	 * Called when add is pressed from menu. Opens the edit pane with a new profile.
	 */
	protected void addPressedFromMenu(){
		//open the edit pane
		toggleEditFields(true);

		//request focus to the edit pane
		EditText nameField = (EditText)this.findViewById(R.id.nameTextField);
		nameField.setText("");
		nameField.requestFocus();

		//reset portrait back to silhouette
		ImageView portrait = (ImageView)this.findViewById(R.id.portraitImageView);
		portrait.setImageResource(R.drawable.silhouette);

		imageUri = Uri.fromFile(new File(""));

		//set up profile to be edited (in this case, a new one)
		indexToEdit = profiles.size();
	}


	/**
	 * Called when the save button is pressed. Saves the profile in the list.
	 */
	protected void saveButtonPressed(){
		//get the text out of the edit text
		EditText nameField = (EditText)this.findViewById(R.id.nameTextField);
		String editName = nameField.getText().toString();
		nameField.setText("");//clear the edit field for next time

		//get the picture out of the portrait
		String filepath = imageUri.getPath();


		if (indexToEdit == profiles.size()){
			profiles.add(new Profile(editName, filepath, UUID.randomUUID()));
			selectProfile(indexToEdit); //select the newly added profile
		} else {
			//modify the item
			Profile p = profiles.get(indexToEdit);
			p.setName(editName);
			p.setFile(imageUri.getPath());
			reloadListUI();
		}

		//close the edit pane
		toggleEditFields(false);

		//close android keyboard (because they shouldn't be editing it anymore)
		getWindow().setSoftInputMode(
				WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);

	}

	/**
	 * Initializes all necessary instance variables
	 */
	private void initializeInstanceVariables(){
		profiles = new SavableProfileList();
		imageUri = Uri.fromFile(new File(""));
		numUse = 0;
		indexToEdit = 0;
	}

	/**
	 * Initializes all listeners for the screen items
	 */
	private void initializeListeners(){
		//save button listener
		Button save = (Button)this.findViewById(R.id.saveButton);
		save.setOnClickListener(new OnClickListener(){

			@Override
			public void onClick(View v) {
				saveButtonPressed();
			}

		});

		ImageButton cameraButton = (ImageButton)this.findViewById(R.id.cameraButton);
		cameraButton.setOnClickListener(new OnCameraButtonPressed());

		ImageButton galleryButton = (ImageButton)this.findViewById(R.id.galleryButton);
		galleryButton.setOnClickListener(new OnFileButtonPressed());

		ImageView portrait = (ImageView)this.findViewById(R.id.portraitImageView);
		portrait.setOnClickListener(new OnClickListener(){

			@Override
			public void onClick(View v) {

				ClearPortraitFragment f = new ClearPortraitFragment();
				f.show(getSupportFragmentManager(), "clear portrait");

			}

		});

	}

	/**
	 * Turns the edit field on or off
	 * @param on True if the edit field should be turned on
	 */
	protected void toggleEditFields(boolean on){

		//get all of the things we need to toggle
		TextView nameView = (TextView)this.findViewById(R.id.nameTextView);
		EditText nameField = (EditText)this.findViewById(R.id.nameTextField);
		Button saveButton = (Button)this.findViewById(R.id.saveButton);
		View pbar = this.findViewById(R.id.progressBar2);
		TextView ptext = (TextView)this.findViewById(R.id.portraitTextView);
		ImageView pimage = (ImageView)this.findViewById(R.id.portraitImageView);
		ImageButton camButton = (ImageButton)this.findViewById(R.id.cameraButton);
		ImageButton galButton = (ImageButton)this.findViewById(R.id.galleryButton);
		
		if(pbar==null)
		{
			pbar = this.findViewById(R.id.fakeprogressBar);
		}

		if (on){ //if we are toggling it on
			nameView.setVisibility(View.VISIBLE);
			nameField.setVisibility(View.VISIBLE);
			saveButton.setVisibility(View.VISIBLE);
			pbar.setVisibility(View.VISIBLE);
			ptext.setVisibility(View.VISIBLE);
			pimage.setVisibility(View.VISIBLE);
			camButton.setVisibility(View.VISIBLE);
			galButton.setVisibility(View.VISIBLE);
		} else { //if we are toggling it off
			nameView.setVisibility(View.GONE);
			nameField.setVisibility(View.GONE);
			saveButton.setVisibility(View.GONE);
			pbar.setVisibility(View.GONE);
			ptext.setVisibility(View.GONE);
			pimage.setVisibility(View.GONE);
			camButton.setVisibility(View.GONE);
			galButton.setVisibility(View.GONE);
		}		
		isEditOpen = on;
	}

	/**
	 * Selects a profile from the list by index. Note: calling this method will reload the list view
	 * @param i The index to select
	 */
	protected void selectProfile(int i){
		profiles.setSelectedProfile(i);

		//reload the list view
		reloadListUI();
	}

	/**
	 * Reloads the UI and reattaches the listeners to the new adapter
	 */
	private void reloadListUI(){
		//sort the list
		if (profiles.size() > 0){
			if (profiles.getSelectedProfileIndex() >= profiles.size()){ //the last item was both selected and deleted
				Profile p = profiles.get(profiles.size()-1);
				Collections.sort(profiles);
				profiles.setSelectedProfile(profiles.indexOf(p));
			} else {
				Profile p = profiles.getSelectedProfile();
				Collections.sort(profiles);
				profiles.setSelectedProfile(profiles.indexOf(p));
			}

			ListView list = (ListView)findViewById(R.id.listView1);
			list.setAdapter(new IconicAdapter());
			list.setOnCreateContextMenuListener(this);
			registerForContextMenu(list);


			//hide the help text
			TextView text = (TextView)findViewById(R.id.profile_creation_help_text);
			text.setVisibility(View.GONE);

		} else{
			//no profiles in the list, prompt the user to make a profile instead
			toggleEditFields(true);

			//tell the user to make a profile
			TextView text = (TextView)findViewById(R.id.profile_creation_help_text);
			text.setText(CREATION_HELP_TEXT);
			text.setVisibility(View.VISIBLE);
		}

	}

	@Override
	public void onBackPressed(){
		if (isEditOpen && profiles.size() > 0){
			toggleEditFields(false);
		} else {
			finish();
		}
	}

	/**
	 * This class is responsible for setting up the list of profiles to display in the list view
	 * @author Justin Kreier
	 * @version 12/24/2012
	 */
	private class IconicAdapter extends ArrayAdapter<Profile>{

		/**
		 * Simple constructor to hide the annoying stuff
		 */
		public IconicAdapter(){
			super(ProfileActivity.this, R.layout.row_profile, profiles);
		}

		@Override
		public View getView(int position, View convertView, ViewGroup parent){
			LayoutInflater inflater = getLayoutInflater();

			ImageView portrait;
			if (convertView == null){
				convertView = inflater.inflate(R.layout.row_profile, parent, false);
				portrait = (ImageView)convertView.findViewById(R.id.profilePortrait);
			} else {
				portrait = (ImageView)convertView.findViewById(R.id.profilePortrait);
				Object o = portrait.getTag();
				if (o != null && o instanceof ImageDecodeTask){
					ImageDecodeTask t = (ImageDecodeTask)o;
					t.cancel(true);
				}

				convertView = inflater.inflate(R.layout.row_profile, parent, false);
				portrait = (ImageView)convertView.findViewById(R.id.profilePortrait);
			}

			//set the profile name
			TextView profileName = (TextView)convertView.findViewById(R.id.profileName);
			profileName.setText(profiles.get(position).getName());

			//Async load the portrait
			if (profiles.get(position).hasPortraitChanged()){
				portrait.setImageResource(R.drawable.silhouette);
				ImageDecodeTask task = new ImageDecodeTask(portrait);
				portrait.setTag(task);
				task.execute(profiles.get(position));
			} else {
				Bitmap bm = profiles.get(position).getPortrait();
				if (bm != null && !bm.isRecycled()){
					portrait.setImageBitmap(bm);
				} else {
					portrait.setImageResource(R.drawable.silhouette);
				}
			}
			//end async load

			//set the radio button
			RadioButton radio = (RadioButton)convertView.findViewById(R.id.radioButton);
			radio.setChecked(false);

			//if it's the selected row, do special things
			if (position == profiles.getSelectedProfileIndex()){
				profileName.setTextColor(Color.CYAN);
				radio.setChecked(true);
			}

			RowSelectedListener listener = new RowSelectedListener(radio);
			profileName.setOnClickListener(listener);
			portrait.setOnClickListener(listener);
			convertView.setOnClickListener(listener);
			radio.setOnCheckedChangeListener(new RadioChangeListener(position));

			return convertView;
		}
	}

	/**
	 * Handles the case when a radio button is selected
	 * @author kreierj
	 *
	 */
	private class RadioChangeListener implements OnCheckedChangeListener{

		/**
		 * The saved position to select
		 */
		private int savedPos;

		/**
		 * Constructor that saves the position of the row
		 * @param position the row's position
		 */
		public RadioChangeListener(int position){
			savedPos = position;
		}

		@Override
		public void onCheckedChanged(CompoundButton b, boolean checked) {
			selectProfile(savedPos);
		}

	}

	/**
	 * Handles the case when a row is selected from the list view
	 * @author kreierj
	 * @version 12/26/2012
	 */
	private class RowSelectedListener implements OnClickListener{

		/**
		 * Stored radio button reference
		 */
		private RadioButton radio;

		/**
		 * Default constructor that takes the radio button that holds the real action to perform
		 * @param realAction The radio button click to call
		 */
		public RowSelectedListener(RadioButton realAction){
			radio = realAction;
		}

		@Override
		public void onClick(View v) {
			radio.performClick();
		}

	}

	/**
	 * Listener for when the camera button is pressed in the edit field
	 * @author Justin Kreier
	 * @version 12/26/2012
	 */
	private class OnCameraButtonPressed implements OnClickListener{

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
				Log.e(LOG_TAG, "Unable to access folder: /utool");
			}
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
	 * Listener for when the file button is pressed in the edit field
	 * @author kreierj
	 * @version 12/26/2012
	 *
	 */
	private class OnFileButtonPressed implements OnClickListener{

		@Override
		public void onClick(View v) {
			//start an activity to retrieve a file from the file system
			Intent i = new Intent(Intent.ACTION_PICK,android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
			startActivityForResult(i, GALLERY_REQUEST_CODE);
		}

	}

	/**
	 * Responsible for displaying a warning if the user presses clear
	 * @author kreierj
	 * @version 12/26/2012
	 */
	@SuppressLint("ValidFragment")
	private class ClearWarningFragment extends DialogFragment {

		@Override
		public Dialog onCreateDialog(Bundle savedInstanceState){
			//Note: Android wants this class to be public and static, but then it also requires profiles to be static or final
			//which I did not want to do. It seems to work as is, so until I see a reason to listen to this warning, I'd prefer to ignore it
			AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
			builder.setMessage("Are you sure you want to delete all profiles?");

			builder.setNegativeButton("No", null);
			builder.setPositiveButton("Yes", new DialogInterface.OnClickListener(){

				@Override
				public void onClick(DialogInterface dialog, int which) {
					profiles.clear();

					indexToEdit = 0;

					//reset portrait back to silhouette
					ImageView portrait = (ImageView)findViewById(R.id.portraitImageView);
					portrait.setImageResource(R.drawable.silhouette);

					reloadListUI();
				}

			});

			return builder.create();
		}
	}

	/**
	 * Popup dialog for clearing the portrait
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
					ImageView portrait = (ImageView)findViewById(R.id.portraitImageView);

					portrait.setImageResource(R.drawable.silhouette);
				}

			});

			return builder.create();
		}
	}


}
