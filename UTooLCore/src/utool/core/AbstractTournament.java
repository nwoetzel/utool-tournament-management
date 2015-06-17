package utool.core;

import java.net.InetAddress;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.UUID;

import utool.networking.packet.HostInformation;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

/**
 * The unified tournament information and management class for the home screen
 * @author Cory
 *
 */
public class AbstractTournament extends BaseAdapter implements Comparable<AbstractTournament>{

	/**
	 * The view resource id provided by the home screen. Not used by extended classes.
	 */
	private final int textViewResourceId;
	
	/**
	 * The last time a broadcast was received about this tournament
	 */
	protected long lastSeen;
	
	/**
	 * The instance of AbstractTournament, used as a BaseAdapter, on the home screen.
	 */
	private static AbstractTournament homeScreenAbstractTournament;
	
	/**
	 * A reference to the home activity, for calling on the UI thread
	 */
	private static HomeActivity homeActivity;
	
	/**
	 * Blank placeholder bitmap
	 */
	private static Bitmap blank = Bitmap.createBitmap(1, 1, Bitmap.Config.ARGB_8888);
	
	/**
	 * Tournament object constructor used by extended data classes that don't need BaseAdapter functionality.
	 */
	public AbstractTournament(){
		textViewResourceId = -1;
	}
	
	/**
	 * BaseAdapter constructor for tournament list
	 * @param homeActivity The home activity to associate this list with
	 * @param textViewResourceId The resource id of the view to use
	 */
	public AbstractTournament(HomeActivity homeActivity, int textViewResourceId){
		AbstractTournament.homeActivity = homeActivity;
		this.textViewResourceId = textViewResourceId;
		AbstractTournament.homeScreenAbstractTournament = this;
	}
	
	/**
	 * The tournament's name
	 */
	protected String tournamentName = "";
	
	/**
	 * The port number of the tournament server instance. This field will be null when this object is created by the local device.
	 */
	protected int serverPort;

	/**
	 * The IP address of the server. This field will be null when this object is created by the local device,
	 * since it is unnecessary in that case.
	 */
	protected InetAddress serverAddress;

	
	/**
	 * HashMap storing data for all tournaments
	 */
	private static HashMap<UUID, AbstractTournament> tournamentInstances = new HashMap<UUID, AbstractTournament>();
	
	/**
	 * An indexed version of the above HashMap, for ListView
	 */
	private static List<AbstractTournament> tournamentInstancesIndexed = new LinkedList<AbstractTournament>();
	
	/**
	 * Search for existing tournament data for the UUID
	 * @param tournamentUUID The tournament UUID to lookup
	 * @return An AbstractTournament object, or null
	 */
	public static AbstractTournament getTournament(UUID tournamentUUID){
		AbstractTournament instance = tournamentInstances.get(tournamentUUID);
		return instance;
	}
	
	/**
	 * Insert or update data for a tournament UUID
	 * @param tournamentUUID The tournament UUID
	 * @param tournament The tournament data
	 */
	public static void setTournament(UUID tournamentUUID, AbstractTournament tournament){
		synchronized (tournamentInstances) {
			tournamentInstances.put(tournamentUUID, tournament);
		}
		processUiChanges(false);
	}
	
	/**
	 * Insert data for a tournament UUID. This method doesn't overwrite existing data.
	 * @param tournamentUUID The tournament UUID
	 * @param tournament The tournament data
	 * @return True if inserted, false if UUID already exists
	 */
	public static boolean addTournament(UUID tournamentUUID, AbstractTournament tournament){
		boolean result = false;
		if (tournamentUUID != null){
			synchronized (tournamentInstances) {
				AbstractTournament t = tournamentInstances.get(tournamentUUID);
				if (t == null){
					tournamentInstances.put(tournamentUUID, tournament);
					result = true;
					processUiChanges(false);
				} else {
					t.touch();
					t.setTournamentName(tournament.getTournamentName());
				}
			}
		}
		return result;
	}
	
	/**
	 * Remove data for a tournament
	 * @param tournamentUUID The tournament UUID
	 */
	public static void removeTournament(UUID tournamentUUID){
		synchronized(tournamentInstances){
			tournamentInstances.remove(tournamentUUID);
		}
		if (homeActivity != null){
			homeActivity.loadConfigurationList();
		}
		processUiChanges(false);
	}
	
	/**
	 * Update the UI with the most recent tournament list.
	 * This depends on homeActivity being properly set.
	 * @param uiCalled Set to true if calling from the UI thread for cleaning stale tournaments. Otherwise, set to false.
	 */
	public static void processUiChanges(boolean uiCalled){
		//Sync tournamentInstances to tournamentInstancesIndexed
		boolean cleaned = cleanupStaleTournaments();
		if (uiCalled && cleaned){
			homeScreenAbstractTournament.notifyDataSetChanged();
		}
		if (homeActivity != null && homeScreenAbstractTournament != null && uiCalled == false){
			homeActivity.runOnUiThread(new Runnable() {
				@Override
				public void run() {
					//Clear existing indexed list
					tournamentInstancesIndexed.clear();
					synchronized (tournamentInstances) {
						//Copy tournaments to indexed list
						for (AbstractTournament t: tournamentInstances.values()){
							tournamentInstancesIndexed.add(t);
						}
					}
					//Sort indexed list
					Collections.sort(tournamentInstancesIndexed);
					//Let the home screen know to update
					homeScreenAbstractTournament.notifyDataSetChanged();
				}
			});
		}
	}
	
	/**
	 * Get all discovered hosts, excluding already connected tournaments
	 * @return A list of HostInformation
	 */
	public static List<HostInformation> getDiscoveredHosts(){
		List<HostInformation> l = new LinkedList<HostInformation>();
		synchronized (tournamentInstances) {
			for (AbstractTournament t : AbstractTournament.tournamentInstances.values()){
				if (t instanceof HostInformation){
					l.add((HostInformation)t);
				}
			}
		}
		return l;
	}
	
	/**
	 * The location and state of the tournament
	 */
	private TournamentLocationEnum tournamentLocation = TournamentLocationEnum.Local;
	
	/**
	 * The tournament's universally unique id
	 */
	protected UUID tournamentUUID;
	
	/**
	 * Get the tournament UUID
	 * @return Tournament UUID
	 */
	public UUID getTournamentUUID(){
		return tournamentUUID;
	}
	
	/**
	 * Get the tournament location/state.
	 * @return The tournament location
	 */
	public TournamentLocationEnum getTournamentLocation(){
		return tournamentLocation;
	}
	
	/**
	 * Set the tournament location/state.
	 * @param tournamentLocation The tournament location.
	 */
	public void setTournamentLocation(TournamentLocationEnum tournamentLocation){
		this.tournamentLocation = tournamentLocation;
		processUiChanges(false);
	}
	
	/**
	 * Get the tournament's name
	 * @return Tournament name
	 */
	public String getTournamentName(){
		return tournamentName;
	}
	
	/**
	 * Sets the tournament
	 * @param newName The name to set
	 */
	public void setTournamentName(String newName){
		tournamentName = newName;
		processUiChanges(false);
	}
	
	/**
	 * Get the server address this HostInformation was received from.
	 * @return InetAddress of the server.
	 */
	public InetAddress getServerAddress(){
		return serverAddress;
	}
	
	/**
	 * Get the port number of the server/tournament
	 * @return Integer containing the port number
	 */
	public int getServerPort(){
		return serverPort;
	}
	
	/**
	 * Recent flag for discovered, but not connected, tournaments.
	 * @return True if recent.
	 */
	public boolean isRecent(){
		return true;
	}
	
	
	/**
	 * Update this object's timestamp
	 */
	public void touch(){
		lastSeen = System.nanoTime();
	}
	
	/**
	 * Remove tournament broadcasts with stale timestamps
	 * @return True if tournaments removed
	 */
	private static boolean cleanupStaleTournaments(){
		synchronized (tournamentInstances) {
			LinkedList<UUID> remove = new LinkedList<UUID>();
			for (AbstractTournament t : tournamentInstances.values()){
				if (!t.isRecent()){
					remove.add(t.tournamentUUID);
				}
			}
			for(UUID uuid : remove){
				AbstractTournament t = tournamentInstances.remove(uuid);
				tournamentInstancesIndexed.remove(t);
			}
			return remove.size() > 0;
		}
	}
	
	/**
	 * Enum for tournament location
	 * @author Cory
	 *
	 */
	public enum TournamentLocationEnum{
		/**
		 * The tournament was started on this device. Associated with Core.
		 */
		Local,
		/**
		 * The tournament was started on this device, and has finished.
		 */
		LocalFinished,
		/**
		 * The tournament has been detected on the local network, but not connected to. Associated with HostInformation.
		 */
		RemoteDiscovered,
		/**
		 * The tournament has been detected on the local network, and connected to. Associated with Core.
		 */
		RemoteConnected;
	}
	
	@Override
	public boolean equals(Object o){
		boolean result = false;
		if (o instanceof AbstractTournament){
			AbstractTournament t = (AbstractTournament)o;
			result = t.tournamentUUID.equals(tournamentUUID);
			if (result){
				//List.contains(x) calls x.equals(o) against each item in the list, where o is all the items in the list 
				t.touch();
			}
		}
		return result;
	}
	
	@Override
	public int hashCode(){
		return tournamentUUID.hashCode();
	}

	@Override
	public int getCount() {
		return tournamentInstancesIndexed.size();
	}

	@Override
	public AbstractTournament getItem(int position) {
		return tournamentInstancesIndexed.get(position);
	}

	@Override
	public long getItemId(int position) {
		return -1;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		View v = convertView;
		if (v == null) {
			LayoutInflater vi = (LayoutInflater)AbstractTournament.homeActivity.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
			v = vi.inflate(textViewResourceId, null);
		}

	    TextView textView = (TextView) v.findViewById(R.id.tournament_name);
	    ImageView imageView = (ImageView)v.findViewById(R.id.tournament_list_image);
	    //ImageView imageView = (ImageView) rowView.findViewById(R.id.icon);
	    AbstractTournament item = getItem(position);
	    textView.setText(item.tournamentName);
	    
	    switch (item.tournamentLocation){
	    	case Local:
	    		textView.setTextColor(Color.WHITE);
	    		imageView.setImageBitmap(blank);
	    		break;
	    	case LocalFinished:
	    		textView.setTextColor(Color.RED);
	    		imageView.setImageBitmap(blank);
	    		break;
	    	case RemoteConnected:
	    		textView.setTextColor(Color.argb(255, 0, 255, 0));
	    		imageView.setImageResource(R.drawable.wireless_icon);
	    		break;
	    	case RemoteDiscovered:
	    		textView.setTextColor(Color.argb(255, 0, 255, 255));
	    		imageView.setImageResource(R.drawable.wifi_discover);
	    		break;
	    }
	    v.invalidate();
	    return v;
	}

	@Override
	public int compareTo(AbstractTournament another) {
		TournamentLocationEnum thisLocation = this.getTournamentLocation();
		TournamentLocationEnum otherLocation = another.getTournamentLocation();
		int result = 0;
		if (thisLocation == otherLocation){
			//sort alphabetically
			result = this.getTournamentName().compareTo(another.getTournamentName());
		} else {
			switch (thisLocation){
				case Local:
					result = -1;
					break;
				case LocalFinished:
					result = 1;
					break;
				case RemoteConnected:
					switch (otherLocation){
						case Local:
							result = 1;
							break;
						case LocalFinished:
							result = -1;
							break;
						case RemoteDiscovered:
							result = -1;
							break;
						case RemoteConnected:
							break;
						default:
							break;
					}
					break;
				case RemoteDiscovered:
					switch (otherLocation){
						case Local:
							result = 1;
							break;
						case LocalFinished:
							result = -1;
							break;
						case RemoteDiscovered:
							result = 0;
							break;
						case RemoteConnected:
							result = 1;
							break;
						default:
							break;
					}
					break;
			}
		}
		return result;
	}
}
