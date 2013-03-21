package is.heklapunch;

import is.heklapunch.bluetooth.BlueToothServer;
import android.app.Activity;
import android.os.Bundle;
import android.view.Window;
import android.widget.ListView;

public class OrganizeReceiveActivity extends Activity {

//	private static final boolean DEBUG = true;
//	private static final String TAG = "BTServerActivity";
	private BlueToothServer mServer;
	private ListView mPayloadsView;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		//
		if (mServer == null) {
			mServer = new BlueToothServer(this, null);
			mServer.listen();
		}
		
		if(!mServer.isDiscoverable()) {
			mServer.ensureDiscoverable();
		} 

		// Setup the window
		requestWindowFeature(Window.FEATURE_INDETERMINATE_PROGRESS);
		setContentView(R.layout.activity_organize_receive);

		// Set result CANCELED incase the user backs out
		setResult(Activity.RESULT_CANCELED);

		// Indicate scanning in the title
		setProgressBarIndeterminateVisibility(true);
		setTitle(R.string.scanning);
		
		// Initialize the array adapter for the conversation thread
		mPayloadsView = (ListView) findViewById(R.id.in);
		mPayloadsView.setAdapter(mServer.mPayloadsRecieved);
	}

	@Override
	public void onStart() {
		super.onStart();
		if (mServer == null) {
			mServer = new BlueToothServer(this, null);
			mServer.listen();
		}
	}

	@Override
	public synchronized void onResume() {
		super.onResume();

		// Performing this check in onResume() covers the case in which BT was
		// not enabled during onStart(), so we were paused to enable it...
		// onResume() will be called when ACTION_REQUEST_ENABLE activity
		// returns.
		if (mServer == null) {
			mServer = new BlueToothServer(this, null);
			mServer.listen();
		}
	}

	@Override
	public void onStop() {
		super.onStop();
		if (mServer != null) {
			mServer.stop();
			mServer = null;
		}
	}

	@Override
	public void onDestroy() {
		super.onDestroy();
		// Stop the Bluetooth services
		if (mServer != null) {
			mServer.stop();
			mServer = null;
		}
	}
}
