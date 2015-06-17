package utool.remoteexceptions;

import android.os.RemoteException;

/**
 * RemoteException class for when a client device has failed to connect to a remote server.
 * @author Cory
 *
 */
public class ConnectionFailedRemoteException extends RemoteException {
	/**
	 * serialVersionUID
	 */
	private static final long serialVersionUID = 852659172832138621L;

	/**
	 * Constructor
	 */
	public ConnectionFailedRemoteException() {
		super();
	}
}
