package is.heklapunch;

import is.heklapunch.bluetooth.BlueToothServer;
import android.app.Activity;
import android.os.Bundle;
import android.util.Log;
import android.view.Window;
import android.widget.ListView;

public class OrganizeReceiveActivity extends Activity {

	private static final boolean DEBUG = true;
	private static final String TAG = "OrganizeReceiveActivity";
	
	private BlueToothServer mServer;
	private ListView mPayloadsView;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		//
		if (mServer == null) {
			mServer = new BlueToothServer(this, null);
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
		if (mServer != null) {
			mServer.listen();
		}
		if(DEBUG) Log.e(TAG, "++ ON START ++");
	}

	@Override
	public synchronized void onResume() {
		super.onResume();
		if(DEBUG) Log.e(TAG, "-- ON RESUME --");
	}

	@Override
	public void onStop() {
		super.onStop();
		if(DEBUG) Log.e(TAG, "-- ON STOP --");
	}

	@Override
	public void onDestroy() {
		super.onDestroy();
		// Stop the Bluetooth services
		if (mServer != null) {
			mServer.stop();
			mServer = null;
		}
		if(DEBUG) Log.e(TAG, "--- ON DESTROY ---");
	}
}
