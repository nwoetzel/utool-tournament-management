package utool.persistence;

import java.util.ArrayList;
import java.util.StringTokenizer;
import java.util.UUID;

import android.content.Intent;

import utool.core.AbstractTournament;
import utool.core.Core;
import utool.plugin.Player;

/**
 * This class is responsible for storing the configuration information to start a tournament
 * @author kreierj
 * @version 1/3/2013
 *
 */
public class TournamentConfiguration extends AbstractTournament implements Savable, Cloneable{

	/**
	 * The list of players participating in the tournament
	 */
	private SavablePlayerList players;
	
	/**
	 * The plugin the tournament is meant for
	 */
	private String pluginName;
	
	/**
	 * The plugin package
	 */
	private String pluginPackage;
	
	
	/**
	 * Constructor for making a tournament configuration object
	 * @param name The tournament name
	 * @param plugin The plugin name
	 * @param pluginPackage The plugin package name
	 * @param playerList The list of players participating in the tournament
	 */
	public TournamentConfiguration(String name, String plugin, String pluginPackage, SavablePlayerList playerList){
		tournamentName = name;
		pluginName = plugin;
		this.pluginPackage = pluginPackage;
		players = playerList;
		tournamentUUID = UUID.randomUUID();
		this.pluginPackage = pluginPackage;
	}
	
	/**
	 * Constructor to define all attributes of the configuration
	 * @param name The tournament name
	 * @param plugin The plugin name
	 * @param pluginPackage The plugin package name
	 * @param playerList The player list
	 * @param id The tournament id
	 */
	public TournamentConfiguration(String name, String plugin, String pluginPackage, SavablePlayerList playerList, UUID id){
		setTournamentLocation(AbstractTournament.TournamentLocationEnum.LocalFinished);
		tournamentName = name;
		pluginName = plugin;
		this.pluginPackage = pluginPackage;
		players = playerList;
		tournamentUUID = id;
		this.pluginPackage = pluginPackage;
	}
	
	/**
	 * Blank constructor
	 */
	public TournamentConfiguration(){
		setTournamentLocation(AbstractTournament.TournamentLocationEnum.LocalFinished);
		tournamentUUID = UUID.randomUUID();
		players = new SavablePlayerList();
		tournamentName = "";
		pluginName = "";
	}
	
	@Override
	public Object clone(){
		TournamentConfiguration c = new TournamentConfiguration(tournamentName, pluginName, pluginPackage, players, tournamentUUID);
		SavablePlayerList clonedPlayers = new SavablePlayerList();
		clonedPlayers.addAll(players);
		c.players = clonedPlayers;
		return c;
	}
	
	/**
	 * Returns the tournament name
	 * @return The tournament name
	 */
	public String getTournamentName(){
		return tournamentName;
	}
	
	/**
	 * Returns the plugin name
	 * @return The plugin name
	 */
	public String getPluginName(){
		return pluginName;
	}
	
	/**
	 * Returns the list of players
	 * @return The list of players
	 */
	public SavablePlayerList getPlayers(){
		return players;
	}
	
	/**
	 * Sets the players associated with this tournament configuration object
	 * @param newPlayers The new list of players
	 */
	public void setPlayers(SavablePlayerList newPlayers){
		players = newPlayers;
	}
	
	/**
	 * Sets the tournament name
	 * @param newName The new tournament name
	 */
	public void setTournamentName(String newName){
		tournamentName = newName;
	}
	
	/**
	 * Sets the plugin name
	 * @param newPlugin The new plugin name
	 */
	public void setPluginName(String newPlugin){
		pluginName = newPlugin;
	}
	
	/**
	 * Gets the plugin package
	 * @return The plugin package
	 */
	public String getPluginPackage(){
		return pluginPackage;
	}
	
	/**
	 * Sets the plugin package
	 * @param newPackage The plugin package
	 */
	public void setPluginPackage(String newPackage){
		pluginPackage = newPackage;
	}
	
	@Override
	public boolean equals(Object o){
		if (o instanceof TournamentConfiguration){
			return tournamentUUID.equals(((TournamentConfiguration) o).tournamentUUID);
		} else if (o instanceof AbstractTournament){
			return super.equals(o);
		} else {
			return false;
		}
	}
	
	@Override
	public String save() {
		String ret = "";
		ret += tournamentName+"\n";
		ret += pluginName+"\n";
		ret += pluginPackage+"\n";
		ret += tournamentUUID.toString()+"\n[";
		for (int i = 0; i < players.size(); i++){
			//save the players into the string
			if (i != players.size()-1){
				ret += players.get(i).save()+"\t";
			} else {
				ret += players.get(i).save();
			}
		}
		ret += "]";
		return ret;
	}

	@Override
	public Savable load(String value) {
		//tokenize the value, deliminated by a newline
		StringTokenizer token = new StringTokenizer(value, "\n");
		String tName = token.nextToken();
		String pName = token.nextToken();
		String pPackage = token.nextToken();
		UUID id = UUID.fromString(token.nextToken());
		
		//the remaining tokens are now for the players
		int remaining = token.countTokens();
		String players = "";
		for (int i = 0; i < remaining; i++){
			players += token.nextToken()+"\n";
		}
		//cut the bracket from the front and back of the loaded players
		players = players.substring(1, players.length()-2);
		
		//tokenize the remaining tokens, deliminated by a tab
		token = new StringTokenizer(players, "\t");
		SavablePlayerList loadedPlayers = new SavablePlayerList();
		
		//load all the players
		while (token.hasMoreTokens()){
			String playerToken = token.nextToken();
			SavablePlayer p = new SavablePlayer();
			p = (SavablePlayer)p.load(playerToken);
			loadedPlayers.add(p);
		}
		
		return new TournamentConfiguration(tName, pName, pPackage, loadedPlayers, id);
	}
	
	/**
	 * Creates a tournament core from this object
	 * @param deviceId The device id
	 * @return Reference to the created core object
	 */
	public Core createTournament(UUID deviceId){
		Core t = Core.getCoreInstance(this.tournamentUUID);
		ArrayList<Player> playerList = new ArrayList<Player>();
		for (int i = 0; i< this.players.size(); i++){
			Player p = players.get(i).toPlayer();
			playerList.add(p);
		}

		//create the plugin intent
		Intent intent = new Intent(Intent.ACTION_MAIN);
		intent.setClassName(this.getPluginPackage(), this.getPluginName());
		
		t.setPlayerList(playerList);
		t.setPluginIntent(intent);
		t.setTournamentName(this.getTournamentName());
		t.setTournamentLocation(AbstractTournament.TournamentLocationEnum.Local);
		t.setPermissionLevel(Player.HOST);

		t.setDeviceId(deviceId);
		
		return t;
	}

}
