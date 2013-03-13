//This class imitates the function of the QR reader activity.
//it is only here so we can test methods semi-authentically in the IDE.

package is.heklapunch;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.View;
import android.widget.Toast;
import java.util.Random;

import com.google.zxing.integration.android.IntentIntegrator;
import com.google.zxing.integration.android.IntentResult;

public class TestQRActivity extends Activity {

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_qr_test);
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.activity_qr_test, menu);
		return true;
	}

	// Go to organize mode
	public void genFakeQR(View view) {
		Random randomGenerator = new Random();
		String fakeQRString = String.valueOf(randomGenerator
				.nextInt(1000000000));
		Intent i = getIntent();
		i.putExtra("FakeQR", fakeQRString);
		setResult(RESULT_OK, i);
		finish();
	}

	// QR Scan result
	public void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
		if (data.getExtras().containsKey("FakeQR")) {
			Toast.makeText(this, data.getStringExtra("FakeQR"),
					Toast.LENGTH_SHORT).show();
		} else {
			Toast.makeText(this, "FAILURE!", Toast.LENGTH_SHORT).show();
		}
	}
}
