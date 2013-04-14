package utool.plugin.dummy;

import utool.core.R;
import android.os.Bundle;
import android.os.RemoteException;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ScrollView;
import utool.plugin.activity.AbstractPluginMainActivity;
import utool.networking.XmlMessageTypeException;

/**
 * Dummy plugin main activity
 * @author Cory
 *
 */
public class DummyMainActivity extends AbstractPluginMainActivity {

	/**
	 * Tag used for logging
	 */
	private static final String LOG_TAG = "utool.plugin.dummy.DummyMainActivity";

	/**
	 * Tournament state object
	 */
	private DummyTournament tournament;

	/**
	 * Runnable used by thread to receive messages
	 */
	Runnable receiveRunnable = new Runnable() 
	{
		public void run() 
		{
			try {
				while (true)
				{
					String msg = pluginHelper.mICore.receive();
					if (msg.equals("-1")){
						return;
					}
					DummyMessage message;
					try {
						message = new DummyMessage(msg);
						tournament.setLastMessage(message);
						DummyMainActivity.this.runOnUiThread(new UiUpdater(message));
					} catch (XmlMessageTypeException e) {
					}
				}
			} catch (RemoteException e) {
				e.printStackTrace();
			}
		}
	};

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_dummy);
		android.os.Debug.waitForDebugger();
		tournament = DummyTournament.getInstance(getTournamentId());
		UiUpdater u = new UiUpdater(tournament.getLastMessage());
		runOnUiThread(u);
	}

	@Override
	public void runOnServiceConnected() {
		//create thread to get received messages
		Thread t  = new Thread(receiveRunnable);
		t.start();
	}

	@Override
	public void runOnServiceDisconnected() {
		Log.e(LOG_TAG, "Service has unexpectedly disconnected");
	}

	/**
	 * Class to update UI
	 * @author Cory
	 *
	 */
	private class UiUpdater implements Runnable{
		/**
		 * Message received from server
		 */
		DummyMessage message;

		/**
		 * Construct this class in preparation of updating the UI
		 * @param message A message received from the server
		 */
		public UiUpdater(DummyMessage message){
			this.message = message;
		}

		@Override
		public void run() {
			EditText dummyTextField = (EditText) findViewById(R.id.dummyTextField);
			ScrollView dummyTextScrollView = (ScrollView) findViewById(R.id.dummyTextScroll);
			
			ImageView dummyImageView = (ImageView) findViewById(R.id.dummyImageView);
			ScrollView dummyImageScrollView = (ScrollView) findViewById(R.id.dummyImageScroll);
			
			if (message != null){	
				if (message.getTextData() != null) {
					dummyTextScrollView.setVisibility(View.VISIBLE);
					dummyTextField.setText(message.getTextData());
				} else {
					dummyTextScrollView.setVisibility(View.GONE);
				}

				if (message.getImage() != null){
					dummyImageScrollView.setVisibility(View.VISIBLE);
					dummyImageView.setImageBitmap(message.getImage());
				} else {
					dummyImageScrollView.setVisibility(View.GONE);
				}
			}
		}
	}
}
