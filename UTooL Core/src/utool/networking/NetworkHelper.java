package utool.networking;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.concurrent.ExecutionException;

import android.os.AsyncTask;

/**
 * Network function helper class
 * @author Cory
 *
 */
public class NetworkHelper {
	/**
	 * Allow InetAddress.getByName(String host) easily from UI thread
	 * @param host the hostName to be resolved to an address or null. 
	 * @return the InetAddress instance representing the host.
	 * @throws UnknownHostException if the address lookup fails. 
	 */
	public static InetAddress getByName(String host) throws UnknownHostException{
		GetAddressByName s = new GetAddressByName();
		s.execute(host);
		try {
			InetAddress addr = s.get();
			if (s.exception != null){
				throw s.exception;
			}
			return addr;
		} catch (InterruptedException e) {
		} catch (ExecutionException e) {
		}
		
		return null;
	}
	
	/**
	 * Async class for InetAddress.getByName
	 */
	private static class GetAddressByName extends AsyncTask<String, Void, InetAddress> {
		/**
		 * Exception to return
		 */
		public UnknownHostException exception;
		
		@Override
		protected InetAddress doInBackground(String... arg0) {
			try {
				return InetAddress.getByName(arg0[0]);
			} catch (UnknownHostException e) {
				exception = e;
				return null;
			}
		}
	}
}
