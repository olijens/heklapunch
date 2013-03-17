package is.heklapunch;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import com.google.gson.GsonBuilder;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.widget.TextView;
import android.widget.Toast;

public class SendActivity extends BlueToothActivity {

	private static final boolean DEBUG = true;
	private static final String TAG = "SendActivity";

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_send);

		ensureDiscoverable();
		Intent serverIntent = new Intent(this, BtDeviceListActivity.class);
		startActivityForResult(serverIntent, REQUEST_CONNECT_DEVICE_SECURE);

		// Senda lista
		SQLHandler handler = new SQLHandler(this);
		ArrayList<ArrayList<String>> results = handler.getAllStations();
		Iterator<ArrayList<String>> i = results.iterator();
		Map<String, String> payload = new HashMap<String, String>();

		while (i.hasNext()) {
			ArrayList<?> entry = i.next();
			payload.put(entry.get(0).toString(), entry.get(1).toString());
		}

		String json = new GsonBuilder().create()
				.toJson(payload, Map.class);
		if (DEBUG) Toast.makeText(this, json, Toast.LENGTH_LONG).show();
		if (json.length() > 0) {
			// Get the message bytes and tell the BluetoothChatService to write
			byte[] send = json.getBytes();
			write(send);
		}

		TextView tv = (TextView) findViewById(R.id.sendresult);
		String err = getLastError();
		if (err != "") {
			tv.setText(err);
		} else {
			tv.setText("SUCCESS");
		}

		stop();
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
		}
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.activity_send, menu);
		return true;
	}

}
