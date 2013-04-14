package utool.networking.wifidirect;

import utool.core.R;
import utool.core.R.layout;
import utool.networking.WiFiDirectBroadcastReceiver;
import android.annotation.TargetApi;
import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.IntentFilter;
import android.net.wifi.WifiManager;
import android.net.wifi.p2p.WifiP2pConfig;
import android.net.wifi.p2p.WifiP2pManager;
import android.net.wifi.p2p.WifiP2pManager.ActionListener;
import android.net.wifi.p2p.WifiP2pManager.Channel;
import android.net.wifi.p2p.WifiP2pManager.ChannelListener;
import android.os.Bundle;
import android.os.Looper;
import android.widget.Toast;

@TargetApi(14)
public class WiFiDirectActivity extends Activity implements ChannelListener {
	WifiP2pManager manager;
	Channel channel;
	BroadcastReceiver receiver;
	IntentFilter mIntentFilter = new IntentFilter();
	
	@Override
	protected void onCreate(Bundle savedInstanceState){
		super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_wifi_direct);
        
		manager = (WifiP2pManager) this.getSystemService(Context.WIFI_P2P_SERVICE);
		channel = manager.initialize(this, getMainLooper(), null);
	    
	    mIntentFilter.addAction(WifiP2pManager.WIFI_P2P_STATE_CHANGED_ACTION);
	    mIntentFilter.addAction(WifiP2pManager.WIFI_P2P_PEERS_CHANGED_ACTION);
	    mIntentFilter.addAction(WifiP2pManager.WIFI_P2P_CONNECTION_CHANGED_ACTION);
	    mIntentFilter.addAction(WifiP2pManager.WIFI_P2P_THIS_DEVICE_CHANGED_ACTION);
	    
	    manager.discoverPeers(channel, new WifiP2pManager.ActionListener() {
	        
	        public void onSuccess() {
	        	Toast.makeText(WiFiDirectActivity.this, "Discovery Initiated",
                        Toast.LENGTH_SHORT).show();
	        }

	        
	        public void onFailure(int reasonCode) {
	        	Toast.makeText(WiFiDirectActivity.this, "Discovery Failed : " + reasonCode,
                        Toast.LENGTH_SHORT).show();
	        }
	    });
	}

	/* register the broadcast receiver with the intent values to be matched */
	@Override
	protected void onResume() {
	    super.onResume();
	    receiver = new WiFiDirectBroadcastReceiver(manager, channel, this);
	    registerReceiver(receiver, mIntentFilter);
	}
	
	/* unregister the broadcast receiver */
	@Override
	protected void onPause() {
	    super.onPause();
	    unregisterReceiver(receiver);
	}
	
	
    public void connect(WifiP2pConfig config) {
        manager.connect(channel, config, new ActionListener() {

            
            public void onSuccess() {
                // WiFiDirectBroadcastReceiver will notify us. Ignore for now.
            }

            
            public void onFailure(int reason) {
                Toast.makeText(WiFiDirectActivity.this, "Connect failed. Retry.",
                        Toast.LENGTH_SHORT).show();
            }
        });
    }

	
	public void onChannelDisconnected() {
		// TODO Auto-generated method stub
		
	}
}
