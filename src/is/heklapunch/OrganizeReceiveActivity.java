package is.heklapunch;

import android.content.Intent;
import android.os.Bundle;

public class OrganizeReceiveActivity extends BlueToothActivity {

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		ensureDiscoverable();
		Intent serverIntent = new Intent(this, BtDeviceListActivity.class);
//		startActivityForResult(serverIntent, REQUEST_CONNECT_DEVICE_SECURE);
		startActivityForResult(serverIntent, REQUEST_CONNECT_DEVICE_INSECURE);
		
		
		setContentView(R.layout.activity_organize_receive);
	}
}
