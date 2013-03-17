/*
 * Activity for competiton part
 * 
 * logip@hi.is
 */

package is.heklapunch;


import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.sql.Date;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;

import com.google.zxing.integration.android.IntentIntegrator;
import com.google.zxing.integration.android.IntentResult;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.location.Location;
import android.location.LocationManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.View;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;
import android.widget.Toast;
import android.text.format.DateFormat;
import android.util.Log;
import android.util.TypedValue;
import android.view.ViewGroup.LayoutParams;

import com.google.gson.Gson;


public class KeppaActivity extends Activity {
	
	TableLayout station_table;
	SQLHandler handler;
	long time;

	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_keppa);
		//Set time object
		time = System.currentTimeMillis();
		
		//create database object
		handler = new SQLHandler(this);
		
		//make table
		station_table=(TableLayout)findViewById(R.id.station_table);
		this.fillTable();
		
	}
	
	//fill table with content
	public void fillTable() {		
	
		TableRow row;
		TextView t1, t2, t3;
		//Converting to dip unit
		int dip = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP,
				(float) 1, getResources().getDisplayMetrics());
		
		ArrayList<ArrayList<String>> results =  handler.getAllStations();
		
		Iterator<ArrayList<String>> i = results.iterator();

		while(i.hasNext()) {
			
			ArrayList<?> entry = i.next();
			
			row = new TableRow(this);

			t1 = new TextView(this);
			t2 = new TextView(this);
			t3 = new TextView(this);

			t1.setText(entry.get(0).toString());
			
			//fix date
			String longV = entry.get(1).toString();
			long millisecond = Long.parseLong(longV);
			String dateString = DateFormat.format("kk:mm:ss", new Date(millisecond)).toString();
			t2.setText(dateString);
			
			t3.setText(entry.get(3).toString());

			t1.setTypeface(null, 1);
			t1.setWidth(130);
			t2.setTypeface(null, 1);
			t2.setWidth(146);
			t3.setTypeface(null, 1);

			t1.setTextSize(15);
			t2.setTextSize(15);
			t3.setTextSize(15);

			t1.setTextColor(Color.BLUE);
			t2.setTextColor(Color.RED);
			t3.setTextColor(Color.DKGRAY);
			row.addView(t1);
			row.addView(t2);
			row.addView(t3);

			station_table.addView(row, new TableLayout.LayoutParams(
					LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT));

		}
	}

	// Go to organize mode
	public void read_qr(View view) {
		IntentIntegrator integrator = new IntentIntegrator(this);
		integrator.initiateScan();
	}
	
	// Delete all stations from the view and db
	public void delete(View view) {
		handler.deleteAll();
		//redraw view
		TableLayout vg = (TableLayout) findViewById (R.id.station_table);
		vg.removeAllViews();
	}

	// QR Scan result
	public void onActivityResult(int requestCode, int resultCode, Intent intent) {
		
		boolean isTimeChecked = false;
		IntentResult scanResult = IntentIntegrator.parseActivityResult(requestCode, resultCode, intent);		
		if (scanResult != null && scanResult.getContents().length() != 0) {
			// handle scan result
			Toast.makeText(this, scanResult.getContents(), Toast.LENGTH_SHORT).show();
			//Try to get time from gps
			if(isOnline()){				
				
				//Use json service http://json-time.appspot.com/time.json?tz=GMT
				class GetTimeFromServer extends AsyncTask<String, Void, String> {

					public KeppaActivity activity;

					public GetTimeFromServer(KeppaActivity a) {
						activity = a;
					}

					@Override
					protected String doInBackground(String... urls) {
						String url = urls[0];
						String response = "";
						// Log.v("tester", "execute request");
						HttpClient client = new DefaultHttpClient();
						HttpResponse httpResponse;

						HttpGet getRequest = new HttpGet(url);

						try {
							httpResponse = client.execute(getRequest);
							HttpEntity entity = httpResponse.getEntity();
							// Log.v("entity test", entity.getContent().toString());

							if (entity != null) {
								// Log.v("tester", "execute entity er ekki núll");
								InputStream instream = entity.getContent();
								BufferedReader reader = new BufferedReader(
										new InputStreamReader(instream, "UTF-8"), 8);
								StringBuilder sb = new StringBuilder(100000);
								String line = null;
								try {
									while ((line = reader.readLine()) != null) {
										sb.append(line + "\n");
									}
									instream.close();
								} catch (IOException e) {
								}
								response = sb.toString();
								Log.v("tester", "hér er response size: " + response.length());
								instream.close();
							}
						} catch (Exception e) {
							Log.v("tester", "villa í execute request: " + e.toString());
						}

						return response;
					}

					@Override
					protected void onPostExecute(String result) {
						// Log.v("Logatest onPostExecute", result);
						activity.setTime(result);
					}
				}// inner class end

			
					GetTimeFromServer task = new GetTimeFromServer(this);
					//set the time object
					task.execute(new String[] { "http://date.jsontest.com/" });					
					isTimeChecked = true;
			}
			else{
				//We just use the time that is set in the init function
				isTimeChecked = false;
			}
			//write to db			
			handler.addStation("Stöð " + Integer.toString(handler.count()+30),time,scanResult.getContents(), isTimeChecked, this.getGPS());
			//redraw view
			TableLayout vg = (TableLayout) findViewById (R.id.station_table);
			vg.removeAllViews();
			//redraw table
			this.fillTable();			
		} 
		else {
			Toast.makeText(this, "No scan", Toast.LENGTH_SHORT).show();
		}
	}
	
	// get gps points from last known location
	private String getGPS() {

		LocationManager lm = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
		List<String> providers = lm.getProviders(true);

		Location l = null;

		for (int i = providers.size() - 1; i >= 0; i--) {
			l = lm.getLastKnownLocation(providers.get(i));
			if (l != null)
				break;
		}

		//double[] gps = new double[2];
		String loc = "null";
		if (l != null) {
			loc = "";
			//Log.d("Loga gps test", "Location not null");
			Log.d("Loga gps test", Double.toString(l.getLatitude()));
			loc = loc + Double.toString(l.getLatitude());
			loc = loc + Double.toString(l.getLongitude());
		}
		return loc;
	}

	/*
	 * Check if we are online
	 * <jtm@hi.is>
	 * */
	public boolean isOnline() {
	    ConnectivityManager cm =
	        (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
	    NetworkInfo netInfo = cm.getActiveNetworkInfo();
	    if (netInfo != null && netInfo.isConnectedOrConnecting()) {
	        return true;
	    }
	    return false;
	}
	
	/*
	 * Set correct time by using a string from json service
	 * <logip@hi.is>
	 * */
	public void setTime(String json) {
		Gson gson = new Gson();
		TimeItemResult jsonResult = gson.fromJson(json, TimeItemResult.class);
		Log.d("Loga time test", "Setting time with internet");
		//Calendar c = Calendar.getInstance();
		//Phone date used but time taken from server
		//c.set(c.get(Calendar.YEAR), c.get(Calendar.MONTH), c.get(Calendar.DAY_OF_MONTH), jsonResult.hour, jsonResult.minute, jsonResult.second);
		this.time =  jsonResult.milliseconds_since_epoch;  
	}
		
}
