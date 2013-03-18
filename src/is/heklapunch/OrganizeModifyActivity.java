package is.heklapunch;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Iterator;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;

import com.google.zxing.integration.android.IntentIntegrator;
import com.google.zxing.integration.android.IntentResult;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.util.TypedValue;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewGroup.LayoutParams;
import android.widget.EditText;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;
import android.widget.Toast;

public class OrganizeModifyActivity extends Activity {
	TableLayout station_table;
	SQLHandler handler;
	EditText stationNameField;
	EditText courseNameField;
	String stationName = "";
	ArrayList<ArrayList<String>> stationList = new ArrayList<ArrayList<String>>();
	int stationNumber = 1;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_organize_create);
		// create database object
		handler = new SQLHandler(this);
		courseNameField = (EditText) findViewById(R.id.editTextCourseName);
		stationNameField = (EditText) findViewById(R.id.EditTextStationName);

		// make table
		station_table = (TableLayout) findViewById(R.id.Create_Station_Table);
		this.fillTable();
	}

	// fill table with content, we do NOT read from the database here! we only
	// work
	// with new data in the stationList object
	public void fillTable() {

		TableRow row;
		TextView t1, t2;
		// Converting to dip unit
		int dip = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP,
				(float) 1, getResources().getDisplayMetrics());

		stationName = stationNameField.getText().toString();

		Iterator<ArrayList<String>> i = stationList.iterator();

		while (i.hasNext()) {

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
	
	//Save list contents to database
	public void saveList(View view){
		String courseTitle = courseNameField.getText().toString();
		int courseID = handler.getMaxCourseID() + 1;
		Iterator<ArrayList<String>> i = stationList.iterator();
		while (i.hasNext()) {
			ArrayList<?> entry = i.next();
			int stationNumber = Integer.valueOf(entry.get(0).toString());
			String stationTitle = entry.get(1).toString();
			String QRValue = entry.get(2).toString();
			String GPSValue = entry.get(3).toString();
			handler.addStation(courseTitle, courseID, stationTitle, stationNumber, QRValue, GPSValue);
		}
	}

	// Go to QR mode
	public void read_qr(View view) {
		IntentIntegrator integrator = new IntentIntegrator(this);
		integrator.initiateScan();
	}

	// QR Scan result
	public void onActivityResult(int requestCode, int resultCode, Intent intent) {
		ArrayList<String> tempStation = new ArrayList<String>();

		IntentResult scanResult = IntentIntegrator.parseActivityResult(requestCode, resultCode, intent);		
		if (scanResult != null && scanResult.getContents().length() != 0) {
			// handle scan result
			Toast.makeText(this, scanResult.getContents(), Toast.LENGTH_SHORT).show();
			//write to db			
			if (stationNameField.getText().toString() != null) {
				//add station number
				tempStation.add(String.valueOf(stationNumber));
				//add station name
				tempStation.add(stationNameField.getText().toString());
				//add QR code
				tempStation.add(String.valueOf(scanResult.getContents()));
				//add GPS
				//TODO: add working gps!
				tempStation.add("12345");
				stationNumber++;
			}
			else{
				tempStation.add(String.valueOf(stationNumber));
				tempStation.add("Stöð nr. " + stationNumber);
				tempStation.add(String.valueOf(scanResult.getContents()));
				//unused field for GPS
				tempStation.add("");
				stationNumber++;
			}
			stationList.add(tempStation);
			TableLayout vg = (TableLayout) findViewById (R.id.Create_Station_Table);
			vg.removeAllViews();
			//redraw table
			this.fillTable();			
		} 
		else {
			Toast.makeText(this, "No scan", Toast.LENGTH_SHORT).show();
		}
	}
}
