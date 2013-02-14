package is.heklapunch;

import java.util.ArrayList;
import java.util.Iterator;

import com.google.zxing.integration.android.IntentIntegrator;
import com.google.zxing.integration.android.IntentResult;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;
import android.widget.Toast;
import android.util.TypedValue;
import android.view.ViewGroup.LayoutParams;

public class OrganizeCreateActivity extends Activity {
	
	TableLayout station_table;
	SQLHandler handler;
	EditText nameField;
	String stationName = "";
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_organize_create);
		//create database object
		handler = new SQLHandler(this);
		nameField   = (EditText)findViewById(R.id.editText1);
		
		//make table
		station_table=(TableLayout)findViewById(R.id.station_table);
		this.fillTable();
	}
	
	//fill table with content
	public void fillTable() {		
	
		TableRow row;
		TextView t1, t2;
		//Converting to dip unit
		int dip = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP,
				(float) 1, getResources().getDisplayMetrics());
		
		stationName = nameField.getText().toString();
		
		ArrayList<ArrayList<String>> results =  handler.getCoursebyName(stationName);
		
		Iterator<ArrayList<String>> i = results.iterator();

		while(i.hasNext()) {
			
			ArrayList<?> entry = i.next();
			
			row = new TableRow(this);

			t1 = new TextView(this);
			t2 = new TextView(this);

			t1.setText(entry.get(0).toString());
			t2.setText(entry.get(1).toString());

			t1.setTypeface(null, 1);
			t2.setTypeface(null, 1);

			t1.setTextSize(15);
			t2.setTextSize(15);

			t1.setWidth(150 * dip);
			t2.setWidth(150 * dip);
			row.addView(t1);
			row.addView(t2);

			station_table.addView(row, new TableLayout.LayoutParams(
					LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT));

		}
	}

	// Go to organize mode
	public void read_qr(View view) {
		IntentIntegrator integrator = new IntentIntegrator(this);
		integrator.initiateScan();
	}

	// QR Scan result
	public void onActivityResult(int requestCode, int resultCode, Intent intent) {
		
		IntentResult scanResult = IntentIntegrator.parseActivityResult(requestCode, resultCode, intent);
		
		if (scanResult != null && scanResult.getContents().length() != 0) {
			// handle scan result
			Toast.makeText(this, scanResult.getContents(), Toast.LENGTH_SHORT).show();
			//write to db
			handler.addSetting("Stöð " + Integer.toString(handler.count()+1),scanResult.getContents());
			//redraw view
			ViewGroup vg = (ViewGroup) findViewById (R.id.station_table);
			vg.invalidate();
			
		} 
		else {
			Toast.makeText(this, "No scan", Toast.LENGTH_SHORT).show();
		}
	}
	
		
}
