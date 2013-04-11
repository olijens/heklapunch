package is.heklapunch;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

public class MainActivity extends Activity {
	
	EditText editBox;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		
		// get name box
		editBox = (EditText) findViewById(R.id.saved_name);
	}

	//Go to compete mode
    public void keppa(View view) {
    	
    	if (!this.editBox.getText().toString().equals("")) {
			Intent o = new Intent(this, KeppaActivity.class);
			startActivity(o);
		} else {
			Toast.makeText(this, "Nafn keppanda er nauðsynlegt",
					Toast.LENGTH_SHORT).show();
		}
    }
    
  //Go to organize mode
    public void organize(View view) {
    	Intent o = new Intent(this, OrganizeActivity.class);
        startActivity(o);
    }
    
  //Go to organize mode
    public void test_qr(View view) {
    	Intent o = new Intent(this, QRActivity.class);
        startActivity(o);
    }
    
 // Initiating Menu XML file (menu.xml)
    @Override
    public boolean onCreateOptionsMenu(Menu menu)
    {
        MenuInflater menuInflater = getMenuInflater();
        //menuInflater.inflate(R.layout.menu, menu);
        return true;
    }
    
    protected void onResume() {
		super.onResume();
		SharedPreferences prefs = this.getSharedPreferences("competitor_name",
				Context.MODE_PRIVATE);

		String restoredText = prefs.getString("competitor_name", null);
		if (restoredText != null) {
			editBox.setText(restoredText, TextView.BufferType.EDITABLE);

			int selectionStart = prefs.getInt("selection-start", -1);
			int selectionEnd = prefs.getInt("selection-end", -1);
			if (selectionStart != -1 && selectionEnd != -1) {
				editBox.setSelection(selectionStart, selectionEnd);
			}
		}
	}

	protected void onPause() {
		super.onPause();
		SharedPreferences prefs = this.getSharedPreferences("competitor_name",
				Context.MODE_PRIVATE);
		SharedPreferences.Editor editor = prefs.edit();
		editor.putString("competitor_name", editBox.getText().toString());
		editor.putInt("selection-start", editBox.getSelectionStart());
		editor.putInt("selection-end", editBox.getSelectionEnd());
		editor.commit();
	}

}
