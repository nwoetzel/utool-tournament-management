package utool.plugin;
import utool.plugin.Player;

//http://developer.android.com/guide/components/aidl.html

interface IUTooLCore
{
	int startServer(in long tournamentId);
	void setTournamentInformation(String tournamentName);
	void setInitialConnectMessage(String message);
	int getConnectionCount();
	List<Player> getPlayerList();
	boolean playerListUpdated();
	void connectToServer(in byte[] serverAddress, in int port, in long tournamentId);
	boolean isClient();
	boolean isClosed();
	void send(String message);
	String receive();
	void close();
}