package utool.core;

import java.util.ArrayList;
import utool.plugin.Player;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

/**
 * This class controls the Connect As activity
 * @author Cory
 *
 */
@SuppressLint("NewApi")
public class ConnectAsActivity extends Activity {
	/**
	 * Temporary player list variable for received players
	 */
	private ArrayList<Player> playerList;
	
	@SuppressWarnings("unchecked")
	@Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        //Custom title bar code needs to go around setContentView to work
        if(Build.VERSION.SDK_INT < Build.VERSION_CODES.HONEYCOMB){
        	requestWindowFeature(Window.FEATURE_CUSTOM_TITLE);
        	setContentView(R.layout.activity_connect_as);
        	getWindow().setFeatureInt(Window.FEATURE_CUSTOM_TITLE, R.layout.window_title);
        } else {
        	setContentView(R.layout.activity_connect_as);
        	getActionBar().setDisplayShowHomeEnabled(false);
        	getActionBar().setDisplayShowTitleEnabled(false);
        }

        //get player list data
        playerList = (ArrayList<Player>) getIntent().getExtras().getSerializable("playerList");
        
        ListView connectAsListView = (ListView) findViewById(R.id.connectAsListView);
        ArrayAdapter<Player> list = new ConnectAsListView();
        connectAsListView.setAdapter(list);

        connectAsListView.setOnItemClickListener(new OnItemClickListener() {

			@Override
			public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
				//Return selected player to calling activity
				Intent i = new Intent();
				i.putExtra("connectAsPlayer", playerList.get((int) id));
				setResult(RESULT_OK, i);
				finish();
			}
		});
	}
	
	
	/**
	 * Inner class for showing the list of players
	 * @author Cory
	 *
	 */
	private class ConnectAsListView extends ArrayAdapter<Player>{

		/**
		 * Constructor
		 */
		public ConnectAsListView() {
			super(ConnectAsActivity.this, R.layout.connect_as_player_list_item, playerList);
		}
		
		@Override
		public View getView(int position, View convertView, ViewGroup parent){
			LayoutInflater inflater = getLayoutInflater();
			View row = inflater.inflate(R.layout.connect_as_player_list_item, parent, false);

			//set the player name
			TextView profileName = (TextView)row.findViewById(R.id.player_name);
			profileName.setText(playerList.get(position).getName());

			//set the profile picture
			ImageView portrait = (ImageView)row.findViewById(R.id.player_list_image);
			portrait.setImageBitmap(playerList.get(position).getPortrait());

			return row;
		}
	}
}
