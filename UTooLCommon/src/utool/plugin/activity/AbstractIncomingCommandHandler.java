package utool.plugin.activity;

import java.util.ArrayList;

import utool.plugin.Player;

import android.util.Log;

/**
 * When a message is received from the host, the message will be parsed and then will call the IncomingCommandHandler? method corresponding to the 
 * message received. TheIncomingCommandHandler? is responsible for notifying the appropriate part of the SE plugin that a message has been received.
 * 
 * All that this class will do is log the received message.
 * @author waltzm
 * @version 10/20/2012
 */
public abstract class AbstractIncomingCommandHandler 
{
	/**
	 * Holds the tournament to make changes to
	 */
	protected AbstractTournament t;
	
	/**
	 * Creates an incoming command handler to receive messages
	 * and modify tournament t
	 * @param t the tournament the messages are intended for
	 */
	public AbstractIncomingCommandHandler(AbstractTournament t)
	{
		this.t=t;
	}
	

	/**
	 * Notifies that a previously sent matchup has been changed.
	 * @param id of the tournament
	 * @param matchid of the match
	 * @param team1name name of the first team
	 * @param team2name name of the second team
	 * @param team1 of the matchup being sent
	 * @param team2 of the matchup being sent
	 * @param round of the matchup
	 * @param table of the matchup
	 */
	public void handleChangeMatchup(long id, long matchid, String team1name, String team2name, ArrayList<String> team1, ArrayList<String> team2, int round, String table)
	{
		String team1string="";
		for(int i=0;i<team1.size();i++)
		{
			team1string+=team1.get(i).toString();
		}

		String team2string="";
		for(int i=0;i<team2.size();i++)
		{
			team2string+=team2.get(i).toString();
		}
		Log.w("Incoming Command Handler", "Change Matchup:  id: "+id+", matchid: "+matchid+", team1name: "+team1.get(0)+", team2name: "+team2.get(0)+", team1: "+team1string+", team2: "+team2string+", round: "+round+", table: "+table);

//		CoreSEBridge.getInstance().sendMessageToScreen("Matchup Changed: "+team1.get(0)+", "+team2.get(0));
	}

	/**
	 * Notifies the current plugin the a new round is beginning
	 * @param id of the tournament
	 * @param round the round that is just starting
	 */
	public void handleReceiveBeginNewRound(long id, int round)
	{
		Log.w("Incoming Command Handler", "Begin New Round:  id: "+id+", round: "+round);

		//TODO remove
	//	t.bridge.sendMessageToScreen("New Round Begining: "+round);
	}

	/**
	 * Notifies the current plugin that an error occurred. The plugin can then decided how to handle it
	 * @param id of the tournament
	 * @param playerid of the player on the device who encountered an error
	 * @param name of the error
	 * @param message of the error
	 */
	public void handleReceiveError(long id, String playerid, String name, String message)
	{
		Log.w("Incoming Command Handler", "Error Recived:  id: "+id+", playerid: "+playerid+", Name: "+name+", message: "+message);

		//CoreSEBridge.getInstance().sendMessageToScreen("<ERROR>"+name+": "+message );
	}

	/**
	 * Tells the SE plugin that the tournament has finished
	 * Also tells the final standings that hopefully match the internally calculated standings
	 * @param id of the tournament
	 * @param players list of players in the tournament
	 * @param w of each player overall
	 * @param l of each player overall
	 * @param rw of each player overall
	 * @param rl of each player overall
	 * @param s of each player at the completion of the tournament
	 */
	public void handleReceiveFinalStandings(String id, ArrayList<String> players, ArrayList<Double> w, ArrayList<Double> l, ArrayList<Integer> rw, ArrayList<Integer> rl, ArrayList<Integer> s)
	{
		String playerz="";
		for(int i=0;i<players.size();i++)
		{
			playerz+=players.get(i).toString();
		}
		String winz="";
		for(int i=0;i<w.size();i++)
		{
			winz+=w.get(i).toString();
		}
		String lossez="";
		for(int i=0;i<l.size();i++)
		{
			lossez+=l.get(i).toString();
		}
		String roundWinz="";
		for(int i=0;i<rw.size();i++)
		{
			roundWinz+=rw.get(i).toString();
		}
		String roundLossez="";
		for(int i=0;i<rl.size();i++)
		{
			roundLossez+=rl.get(i).toString();
		}
		String standingz="";
		for(int i=0;i<s.size();i++)
		{
			standingz+=s.get(i).toString();
		}

		Log.w("Incoming Command Handler", "Final Standings:  id: "+id+", players: "+playerz+", wins: "+winz+", losses: "+lossez+", round wins: "+roundWinz+", round losses: "+roundLossez+", standing: "+standingz);
	//	CoreSEBridge.getInstance().sendMessageToScreen("Final Standings received");
	}

	/**
	 * Notifies the SE plugin that a new matchup has been decided
	 * @param id of the tournament
	 * @param matchid of the match
	 * @param team1name name of the first team
	 * @param team2name name of the second team
	 * @param team1 of the matchup being sent
	 * @param team2 of the matchup being sent
	 * @param round of the matchup
	 * @param table of the matchup
	 */
	public void handleReceiveMatchup(long id, long matchid, String team1name, String team2name, ArrayList<String> team1, ArrayList<String> team2, int round, String table)
	{
		String team1string="";
		for(int i=0;i<team1.size();i++)
		{
			team1string+=team1.get(i).toString();
		}

		String team2string="";
		for(int i=0;i<team2.size();i++)
		{
			team2string+=team2.get(i).toString();
		}
		Log.w("Incoming Command Handler", "Send Matchup:  id: "+id+", matchid: "+matchid+", team1name: "+team1.get(0)+", team2name: "+team2.get(0)+", team1: "+team1string+", team2: "+team2string+", round: "+round+", table: "+table);
		
		//t.bridge.sendMessageToScreen("Matchup Received: "+team1.get(0)+", "+team2.get(0));
		//CoreSEBridge.getInstance().sendMessageToScreen("Matchup Received: "+id);
	}

	/**
	 * Notifies the plugin the list of players
	 * @param id of the tournament
	 * @param players in the tournament
	 */
	public void handleReceivePlayers(String id, ArrayList<Player> players)
	{
		String playerz="";
		for(int i=0;i<players.size();i++)
		{
			playerz+=players.get(i).toString()+", ";
		}

		Log.w("Incoming Command Handler", "Send Players:  id: "+id+", players: "+playerz);
	//	t.bridge.sendMessageToScreen("Player list received: "+playerz);
	}

	/**
	 * Notifies the plugin of the time limit for each round, beginning at the next round message.
	 * If any of the times are negative, indicates no round timer.
	 * @param id of the tournament
	 * @param time the time in hh:mm:ss
	 */
	public void handleReceiveRoundTimerAmount(long id, String time)
	{

		Log.w("Incoming Command Handler", "Send Round Timer:  id: "+id+", time: "+time);

	}

	/**
	 * Notifies the SE plugin that a match has concluded and the scores are sent
	 * @param id of the tournament
	 * @param matchid of the match
	 * @param team1name name of the first team/id of the player
	 * @param team2name name of the first team/id of the player
	 * @param score1 score of the first team
	 * @param score2 score of the second team
	 * @param round of the match
	 */
	public void handleReceiveScore(long id, long matchid, String team1name, String team2name, String score1, String score2, int round)
	{
		String s ="Send score:  id: "+id+", matchid: "+matchid+", team1name: "+team1name+", team2name: "+team2name+", team1 score: "+score1+", team2 score: "+score2+", round: "+round;
		Log.w("Incoming Command Handler", s);

		//CoreSEBridge.getInstance().sendMessageToScreen("Score Update: "+team1name+": "+score1+", "+team2name+": "+score2);
	}

	/**
	 * Notifies the SE plugin that the tournament name is as passed
	 * @param id of the tournament
	 * @param name of the tournament
	 */
	public void handleReceiveTournamentName(long id, String name)
	{
		Log.w("Incoming Command Handler", "Send Tournament Name:  id: "+id+", name: "+name);
//		CoreSEBridge.getInstance().sendMessageToScreen("Name Received: "+name);

	}

	/**
	 * Notifies the SE plugin that the tournament is to be cleared
	 * @param tid the id of the tournament to clear
	 */
	public void handleReceiveClear(long tid) 
	{
		Log.w("Incoming Command Handler", "Send Clear:  id: "+tid);
		
		//CoreSEBridge.getInstance().sendMessageToScreen("Clear Received: "+tid);
		
	}

}
