package utool.plugin.activity;

import java.io.StringWriter;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import android.util.Log;
import utool.plugin.Player;

/**
 * When a part of the SE plugin wants to send a message to either the host or 
 * all participants, they call the corresponding method within OutgoingCommandHandler. 
 * OutgoingCommandHandler? will then turn the command into the accepted xml command, 
 * and finally send it to the host.
 * @author waltzm
 * @version 10/20/2012
 */
public abstract class AbstractOutgoingCommandHandler 
{
	/**
	 * The tournament associated with this OutgoingCommandHandler
	 */
	protected AbstractTournament tournament;

	/**
	 * Only hosts can send the command
	 */
	protected static final int hostOnly = 0;

	/**
	 * Only hosts or Moderators can send the command
	 */
	protected static final int modOnly = 1;

	/**
	 * Anyone can send the command
	 */
	protected static final int parOnly = 2;


	/**
	 * Constructor for the OutgoingCommandHandler
	 * @param tournamentLogic The tournament to associate this object with
	 */
	public AbstractOutgoingCommandHandler(AbstractTournament tournamentLogic)
	{
		this.tournament = tournamentLogic;

	}

	/**
	 * Sends a notification that a matchup in tournament id has changed
	 * All previous information with the previous matchup id is now 
	 * invalid, and should be replaced with the new one. 
	 * Table, and the team names can be null, and if so will not be sent. All of the rest must be non null
	 * @param id of the tournament
	 * @param matchId id of the match to alter
	 * @param team1Name name of team 1 if applicable
	 * @param team2Name name of team 2 if applicable
	 * @param team1 of the match
	 * @param team2 of the match
	 * @param round of the match
	 * @param table of the match
	 */
	public void handleChangeMatchup(long id, long matchId, String team1Name, String team2Name, String[] team1, String[] team2, int round, String table)
	{
		try
		{
			OutgoingCommand command = new OutgoingCommand();
			command.setCommandType( "changeMatchup" );
			command.addAttribute("id", id+"");
			command.addAttribute("Matchup", matchId+"");

			command.addSubCommand("Team1");
			if(team1Name!=null)
			{
				command.addSubAttribute("Name",team1Name);
			}
			//add team 1 players
			for(int i=0;i<team1.length;i++)
			{
				command.addSubAttribute("Player",team1[i]+"");
			}

			command.addSubCommand("Team2");
			if(team2Name!=null)
			{
				command.addSubAttribute("Name",team2Name);
			}
			//add team 2 players
			for(int i=0;i<team2.length;i++)
			{
				command.addSubAttribute("Player",team2[i]+"");
			}

			command.addAttribute("Round", round+"");
			if(table!=null)
			{
				command.addAttribute("Table", table+"");
			}

			sendCommand( command , hostOnly);
		}
		catch( ParserConfigurationException e )
		{
			Log.e( "OutgoingCommandHandler", "Error sending change matchup command", e );
		}
	}

	/**
	 * Notifies participants that a new round is beginning
	 * The round must be the next logical round
	 * @param id of the tournament
	 * @param round that is just now beginning
	 */
	public void handleSendBeginNewRound(long id, int round)
	{
		try
		{
			OutgoingCommand command = new OutgoingCommand();
			command.setCommandType( "sendBeginNewRound" );
			command.addAttribute("id", id+"");
			command.addAttribute("Round", round+"");

			sendCommand( command , hostOnly);
		}
		catch( ParserConfigurationException e )
		{
			Log.e( "OutgoingCommandHandler", "Error sending begin new round command", e );
		}
	}

	/**
	 * Notifies host/participants of an error
	 * Can be used to notify that incorrect information was received. For instance:
	 * * player sent in a matchup that is not in the list of players
	 * * round sent not correct
	 * * Incomplete or malformed message was received
	 * @param id of the tournament
	 * @param playerid of the device sending the error notification
	 * @param name of the error
	 * @param message of the error
	 */
	public void handleSendError(long id, String playerid, String name, String message)
	{
		try
		{
			OutgoingCommand command = new OutgoingCommand();
			command.setCommandType( "sendError" );
			command.addAttribute("id", id+"");
			command.addAttribute("PlayerId", playerid+"");
			command.addAttribute("ErrorName", name+"");
			command.addAttribute("ErrorMessage", message+"");
			sendCommand( command , parOnly);
		}
		catch( ParserConfigurationException e )
		{
			Log.e( "OutgoingCommandHandler", "Error sending error message command", e );
		}
	}

	/**
	 * Notifies the connect participants two things:
	 * 1. Tournament has ended
	 * 2. Final scores fore everyone
	 * The plugin should already know the final scores since it has been recieving
	 * score updates, however this serves as a checker for correct information being passed.
	 * If the plugin detected that a score is different than what it was expecting it will send back
	 * an error message.
	 * @param id of the tournament
	 * @param players list of player ids in the tournament 
	 * @param wins for each player overall
	 * @param losses for each player overall
	 * @param roundWins for each player overall
	 * @param roundLosses for each player overall
	 * @param standing for each player at the end of the tournament
	 */
	public void handleSendFinalStandings(long id, String[] players, Double[] wins, Double[] losses, Integer[] roundWins, Integer[] roundLosses, Integer[] standing)
	{
		try
		{
			OutgoingCommand command = new OutgoingCommand();
			command.setCommandType( "sendFinalStandings" );
			command.addAttribute("id", id+"");

			//add information for each player
			for(int i=0;i<players.length;i++)
			{
				command.addSubCommand("Player");
				command.addSubAttribute("PlayerId",""+players[i]);
				command.addSubAttribute("Wins",""+wins[i]);
				command.addSubAttribute("Losses",""+losses[i]);
				command.addSubAttribute("RoundWins",""+roundWins[i]);
				command.addSubAttribute("RoundLosses",""+roundLosses[i]);
				command.addSubAttribute("Standing",""+standing[i]);
			}

			sendCommand( command , hostOnly);
		}
		catch( ParserConfigurationException e )
		{
			Log.e( "OutgoingCommandHandler", "Error sending change matchup command", e );
		}
	}

	/**
	 * Informs participants of a match between team1 and team2 during round
	 * "round" and at table "table". The table can be null and if so will not be sent.
	 * Round should be the current round going on.
	 * @param id of the tournament
	 * @param matchId of the match being sent
	 * @param team1Name name of team 1 if applicable
	 * @param team2Name name of team 2 if applicable
	 * @param team1 of the match
	 * @param team2 of the match
	 * @param round of the match
	 * @param table where the match will occur
	 */
	public void handleSendMatchup(long id,long matchId, String team1Name, String team2Name, String[] team1, String[] team2, int round, String table)
	{
		try
		{
			OutgoingCommand command = new OutgoingCommand();
			command.setCommandType( "sendMatchup" );
			command.addAttribute("id", id+"");
			command.addAttribute("Matchup", matchId+"");

			command.addSubCommand("Team1");
			if(team1Name!=null)
			{
				command.addSubAttribute("Name",team1Name);
			}
			//add team 1 players
			for(int i=0;i<team1.length;i++)
			{
				command.addSubAttribute("Player",team1[i]+"");
			}

			command.addSubCommand("Team2");
			if(team2Name!=null)
			{
				command.addSubAttribute("Name",team2Name);
			}
			//add team 2 players
			for(int i=0;i<team2.length;i++)
			{
				command.addSubAttribute("Player",team2[i]+"");
			}

			command.addAttribute("Round", round+"");
			if(table!=null)
			{
				command.addAttribute("Table", table+"");
			}
			sendCommand( command , hostOnly);
		}
		catch( ParserConfigurationException e )
		{
			Log.e( "OutgoingCommandHandler", "Error sending send matchup command", e );
		}
	}

	/**
	 * Sends the full playerlist to the participants. This is generally used for 
	 * error checking to make sure everyone is in synch. If a player is added, this
	 * must be resent out containing the new full list to operate correctly.
	 * Ghost players can also be included in this list.
	 * @param id of the tournament
	 * @param players in the tournament
	 */
	public void handleSendPlayers(long id, Player[] players)
	{
		try
		{
			OutgoingCommand command = new OutgoingCommand();
			command.setCommandType( "sendPlayers" );
			command.addAttribute("id", id+"");

			//send players
			for(int i=0;i<players.length;i++)
			{
				command.addSubCommand("Player");
				Player p = players[i];
				command.addSubAttribute("PlayerId",p.getUUID()+"");
				command.addSubAttribute("Name",p.getName());
				command.addSubAttribute("isGhost", p.isGhost()+"");
				command.addSubAttribute("permissionLevel", p.getPermissionsLevel()+"");
				//check if seed is set, if so send
				if(p.getSeedValue()!=-1)
				{
					command.addSubAttribute("Seed", p.getSeedValue()+"");
				}

			}

			sendCommand( command , hostOnly);
		}
		catch( ParserConfigurationException e )
		{
			Log.e( "OutgoingCommandHandler", "Error sending send players command", e );
		}
	}

	/**
	 * Notifies participants of the time allowed per round if applicable.
	 * If this isn't sent it is assumed no time limit. Each round will 
	 * have the amount of time sent in it unless resent as a different amount.
	 * If any of the passed in number are negative it will be assumed that
	 * no round time limit is wanted.
	 * @param milliElapsedSoFar the time in milliseconds since the tournament was started
	 * @param hour hours allowed per round
	 * @param minute minutes allowed per round
	 * @param second seconds allowed per round
	 */
	public void handleSendRoundTimerAmount(long milliElapsedSoFar, int hour, int minute, int second)
	{
		try
		{
			OutgoingCommand command = new OutgoingCommand();
			command.setCommandType( "sendRoundTimerAmount" );
			command.addAttribute("id", milliElapsedSoFar+"");

			String time = hour+":"+minute+":"+second;
			command.addAttribute("Time", time);

			sendCommand( command , hostOnly);
		}
		catch( ParserConfigurationException e )
		{
			Log.e( "OutgoingCommandHandler", "Error sending send round time command", e );
		}
	}

	/**
	 * Notifies participants of the time allowed per round if applicable.
	 * If this isn't sent it is assumed no time limit. Each round will 
	 * have the amount of time sent in it unless resent as a different amount.
	 * If the passed in time is negative it will be assumed that
	 * no round time limit is wanted.
	 * @param milliElapsedSoFar the time in milliseconds since the tournament was started
	 * @param time time per round in "hh:mm:ss"
	 */
	public void handleSendRoundTimerAmount(long milliElapsedSoFar, String time)
	{
		try
		{
			OutgoingCommand command = new OutgoingCommand();
			command.setCommandType( "sendRoundTimerAmount" );
			command.addAttribute("id", milliElapsedSoFar+"");
			command.addAttribute("Time", time);

			sendCommand( command , hostOnly);
		}
		catch( ParserConfigurationException e )
		{
			Log.e( "OutgoingCommandHandler", "Error sending send round time command", e );
		}
	}

	/**
	 * Sends a score for the matchup to participants. The sending of team names
	 * is just for error checking to make sure everything is working as intended.
	 * 
	 * @param id of the tournament
	 * @param matchId of the match
	 * @param team1name name of the team or id of the single player
	 * @param team2name name of the team or id of the single player
	 * @param team1score score of team 1 for the round
	 * @param team2score score of team 2 for the round
	 * @param round of the match to send a score for
	 */
	public void handleSendScore(long id, long matchId, String team1name, String team2name, double team1score, double team2score, int round)
	{
		try
		{
			OutgoingCommand command = new OutgoingCommand();
			command.setCommandType( "sendScore" );
			command.addAttribute("id", id+"");
			command.addAttribute("Matchup", matchId+"");

			command.addSubCommand("Team1");
			command.addSubAttribute("Name",team1name);
			command.addSubAttribute("Score",team1score+"");

			command.addSubCommand("Team2");
			command.addSubAttribute("Name",team2name);
			command.addSubAttribute("Score",team2score+"");

			command.addAttribute("Round", round+"");

			sendCommand( command , modOnly);
		}
		catch( ParserConfigurationException e )
		{
			Log.e( "OutgoingCommandHandler", "Error sending send score command", e );
		}
	}

	/**
	 * Sends the name of the tournament for display on the UI
	 * @param id of the tournament
	 * @param name of the tournament
	 */
	public void handleSendTournamentName(long id, String name)
	{
		try
		{
			OutgoingCommand command = new OutgoingCommand();
			command.setCommandType( "sendTournamentName" );
			command.addAttribute("id", id+"");
			command.addAttribute("Name",name);


			sendCommand( command, hostOnly);
		}
		catch( ParserConfigurationException e )
		{
			Log.e( "OutgoingCommandHandler", "Error sending send tournament name command", e );
		}
	}



	/**
	 * Sends a notification to clear all tournament information
	 * Usually followed by the commands to rebuild the tournament
	 * @param id the tournament id
	 */
	public void handleSendClear(long id)
	{
		try
		{
			OutgoingCommand command = new OutgoingCommand();
			command.setCommandType( "sendClear" );
			command.addAttribute("id", id+"");
			sendCommand( command, hostOnly);
		}
		catch( ParserConfigurationException e )
		{
			Log.e( "OutgoingCommandHandler", "Error sending send clear command", e );
		}
	}


	/**
	 * Called internally to send commands.
	 * If the command is only supposed to be sent by hosts (everything except error) and you
	 * are not a host, the message will not be sent
	 * Sends a command out to the server (Core)
	 * @param command to be sent
	 * @param permissionLevel Corresponds to Player's permission levels
	 */
	protected void sendCommand(OutgoingCommand command, int permissionLevel)
	{
		String output = command.getOutput();

		Log.w("OutgoingCommandHandler","Command to be sent: "+output);

		//check if the plugin has permission to send the message
		if(permissionLevel==modOnly)
		{
			//check if plugin is a participant
			if(tournament.getPermissionLevel()==Player.PARTICIPANT)
			{
				//cannot send message since don't have permission
				Log.e( "OutgoingCommandHandler", "Plugin is not a host or moderator and therefore message coud not be sent: "+command.getOutput());
				return;
			}
		}
		if(permissionLevel==hostOnly)
		{
			//check if plugin is a host
			if(tournament.getPermissionLevel()!=Player.HOST)
			{
				//cannot send message since don't have permission
				Log.e( "OutgoingCommandHandler", "Plugin is not a host and therefore message coud not be sent");
				return;
			}
		}


		//send command to core to send out		
		boolean sent = tournament.getBridge().sendMessage(output);

		if(!sent)
		{
			Log.e( "OutgoingCommandHandler", "Error sending command to server. This means that the main activity was null when the command was sent");
		}

	}



	/**
	 * Inner class for holding the currently being created command
	 * Used to house xml information before being sent out to the core
	 * @author waltzm
	 * @version 1020/2012
	 */
	protected static class OutgoingCommand
	{
		/**
		 * The document created for the command
		 */
		private Document doc;

		/**
		 * root element of the xml
		 */
		private Element root;

		/**
		 * sub element of the xml
		 */
		private Element sub;

		/**
		 * Constructor for a command
		 * Creates the document and the root
		 * Creates the root element to be "command"
		 * @throws ParserConfigurationException if something goes wrong
		 */
		public OutgoingCommand() throws ParserConfigurationException
		{
			DocumentBuilderFactory dbfac = DocumentBuilderFactory.newInstance();
			DocumentBuilder docBuilder = dbfac.newDocumentBuilder();
			doc = docBuilder.newDocument();

			//create root node
			root = doc.createElement( "command" );
			doc.appendChild( root );
		}

		/**
		 * Adds an attribute to the root element
		 * @param attribName of the attribute to add
		 * @param value of the attribute
		 */
		public void addAttribute( String attribName, String value )
		{
			Element attrib = doc.createElement( attribName );
			attrib.appendChild( doc.createTextNode( value ) );
			root.appendChild( attrib );
		}

		/**
		 * Adds a sub attribute to the root element
		 * @param attribName of the sub attribute to add
		 * @param value of the sub attribute
		 */
		public void addSubAttribute( String attribName, String value )
		{
			Element attrib = doc.createElement( attribName );
			attrib.appendChild( doc.createTextNode( value ) );
			sub.appendChild( attrib );
		}

		/**
		 * Adds a sub command with just a name
		 * @param name fo the sub command
		 */
		public void addSubCommand( String name )
		{
			sub = doc.createElement( name );
			root.appendChild( sub );
		}

		/**
		 * Gets the xml in String form
		 * @return xml to send off
		 */
		public String getOutput()
		{
			String command = null;
			try
			{
				//set up a transformer
				TransformerFactory transfac = TransformerFactory.newInstance();
				Transformer trans = transfac.newTransformer();
				trans.setOutputProperty( OutputKeys.OMIT_XML_DECLARATION, "yes" );
				trans.setOutputProperty( OutputKeys.STANDALONE, "no" );
				trans.setOutputProperty( OutputKeys.INDENT, "yes" );

				//create string from xml tree
				StringWriter sw = new StringWriter();
				StreamResult result = new StreamResult( sw );
				DOMSource source = new DOMSource( doc );
				trans.transform( source, result );
				command = sw.toString();
			}
			catch (TransformerException e) {
				Log.e( "OutgoingCommand", "Command generator - Failed to generate output", e );

			}

			return command;
		}

		/**
		 * Sets the root command's type
		 * @param type of the root command
		 */
		public void setCommandType( String type )
		{
			root.setAttribute( "type", type );
		}
	}

}
