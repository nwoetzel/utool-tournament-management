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
        
        //add action bar if honeycomb or higher
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB){
        	setContentView(R.layout.activity_connect_as);
        	getActionBar().setDisplayShowHomeEnabled(false);
        	getActionBar().setDisplayShowTitleEnabled(false);
        } else {
        	this.requestWindowFeature(Window.FEATURE_NO_TITLE);
        	setContentView(R.layout.activity_connect_as);
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
				Player connectAsPlayer = playerList.get((int) id);
				connectAsPlayer.setPermissionsLevel(Player.PARTICIPANT);
				i.putExtra("connectAsPlayer", connectAsPlayer);
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
