package is.heklapunch;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.View;
import android.widget.Toast;

import com.google.zxing.integration.android.IntentIntegrator;
import com.google.zxing.integration.android.IntentResult;

public class QRActivity extends Activity {

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_qr);
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.activity_qr, menu);
		return true;
	}
	
	// Go to organize mode
	public void read_qr(View view) {
		IntentIntegrator integrator = new IntentIntegrator(this);
		integrator.initiateScan();
	}

	// QR Scan result
	public void onActivityResult(int requestCode, int resultCode, Intent intent) {
		IntentResult scanResult = IntentIntegrator.parseActivityResult(
				requestCode, resultCode, intent
		);
		if (scanResult != null && scanResult.getContents().length() != 0) {
			// handle scan result
			Toast.makeText(this, scanResult.getContents(), Toast.LENGTH_SHORT).show();
		} else {
			Toast.makeText(this, "No scan", Toast.LENGTH_SHORT).show();
		}
	}

}
