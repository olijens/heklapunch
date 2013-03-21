package is.heklapunch;

import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

public class OrganizeReceiveActivity extends BlueToothServer {

	private static final boolean DEBUG = true;
	private static final String TAG = "OrganizeReceiveActivity";

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		if (DEBUG)
			Log.d(TAG, "+ START +");

		if (mBluetoothAdapter.getScanMode() != BluetoothAdapter.SCAN_MODE_CONNECTABLE_DISCOVERABLE) {

			Intent discoverableIntent = new Intent(
					BluetoothAdapter.ACTION_REQUEST_DISCOVERABLE);
			discoverableIntent.putExtra(
					BluetoothAdapter.EXTRA_DISCOVERABLE_DURATION,
					DISCOVERABLE_SECONDS);
			startActivity(discoverableIntent);
		} else {
			if (DEBUG)
				Log.d(TAG, "Device ID already in discovery mode");
		}
		
		setContentView(R.layout.activity_organize_receive);

		if (DEBUG)
			Log.d(TAG, "- AFTER -");
	}

	public void onActivityResult(int requestCode, int resultCode, Intent data) {
		if (DEBUG)
			Log.d(TAG, "onActivityResult " + resultCode);
		switch (requestCode) {
		case REQUEST_CONNECT_DEVICE_SECURE:
			// When DeviceListActivity returns with a device to connect
			if (resultCode == Activity.RESULT_OK) {
				connectDevice(data, true);
			}
			break;
		case REQUEST_CONNECT_DEVICE_INSECURE:
			// When DeviceListActivity returns with a device to connect
			if (resultCode == Activity.RESULT_OK) {
				connectDevice(data, false);
			}
			break;
		case REQUEST_ENABLE_BT:
			// When the request to enable Bluetooth returns
			if (resultCode == Activity.RESULT_OK) {
				if (DEBUG)
					Log.d(TAG, "Bluetooth enabled");
			} else {
				Log.d(TAG, "BT not enabled");
				Toast.makeText(this,
						"User did not enable Bluetooth or an error occured",
						Toast.LENGTH_SHORT).show();
				finish();
			}
		}
	}
}
