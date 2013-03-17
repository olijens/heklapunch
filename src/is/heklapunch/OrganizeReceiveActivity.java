package is.heklapunch;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

public class OrganizeReceiveActivity extends BlueToothActivity {
	
	private static final boolean DEBUG = true;
	private static final String TAG = "OrganizeReceiveActivity";

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		ensureDiscoverable();
		Intent serverIntent = new Intent(this, BtDeviceListActivity.class);
//		startActivityForResult(serverIntent, REQUEST_CONNECT_DEVICE_SECURE);
		startActivityForResult(serverIntent, REQUEST_CONNECT_DEVICE_INSECURE);
		
		
		setContentView(R.layout.activity_organize_receive);
	}
	
	public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if(DEBUG) Log.d(TAG, "onActivityResult " + resultCode);
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
            	if(DEBUG) Log.d(TAG, "Bluetooth enabled");
            } else {
                Log.d(TAG, "BT not enabled");
                Toast.makeText(this, "User did not enable Bluetooth or an error occured", Toast.LENGTH_SHORT).show();
                finish();
            }
        }
    }
}
