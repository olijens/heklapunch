package is.heklapunch;

import java.sql.Date;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.TreeMap;
import java.util.Vector;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Bundle;
import android.text.format.DateFormat;
import android.util.TypedValue;
import android.view.View;
import android.view.ViewGroup.LayoutParams;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;

public class OrganizeActivity extends Activity {

	SQLHandler handler;
	TableLayout station_table;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_organize);
		
		//create database object
		handler = new SQLHandler(this);

		// make table
		station_table = (TableLayout) findViewById(R.id.receive_table);
		this.populateTable();
	}

	// Go to create mode
	public void create(View view) {
		Intent m = new Intent(this, OrganizeCreateActivity.class);
		startActivity(m);

		// create database object
		handler = new SQLHandler(this);
	}

	// Go to recieve mode
	public void receive(View view) {
		Intent r = new Intent(this, OrganizeReceiveActivity.class);
		startActivity(r);
	}

	// Delete all stations from the view and db
	public void delete_results(View view) {
		handler.deleteResults();
		// redraw view
		TableLayout vg = (TableLayout) findViewById(R.id.receive_table);
		vg.removeAllViews();
	}

	protected void onResume() {
		super.onResume();
		//redraw view
		TableLayout vg = (TableLayout) findViewById (R.id.receive_table);
		vg.removeAllViews();
		//redraw table
		this.populateTable();

	}

	// fill table with results
	@SuppressLint("NewApi")
	public void populateTable() {

		TableRow row;
		TextView t1, t2;
			
		// Converting to dip unit
		int dip = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP,
				(float) 1, getResources().getDisplayMetrics());

		ArrayList<ArrayList<String>> results = handler.getAllResults();
		Iterator<ArrayList<String>> ii = results.iterator();
		//Find all competitors
		Vector competitors = new Vector();
		while (ii.hasNext()) {
			ArrayList<?> entry = ii.next();
			if(!competitors.contains(entry.get(5))){
				competitors.add(entry.get(5));
			}
		}
		//Get total time for each competitor
	    Iterator itr = competitors.iterator();
		while(itr.hasNext()){
			//Get all entries for each competitor and calculate total time
			ArrayList<ArrayList<String>> indResults = handler.getResultsForCompetitor(itr.next().toString());
			ArrayList<?> fentry = indResults.get(0);
			ArrayList<?> lentry = indResults.get(indResults.size()-1);
			
			String longStart = fentry.get(1).toString();
			long millisecondStart = Long.parseLong(longStart);
			
			String longEnd = lentry.get(1).toString();
			long millisecondEnd = Long.parseLong(longEnd);
			//Total time of run
			long total = millisecondEnd - millisecondStart;
			//Make proper date string
			String dateString = DateFormat.format("kk:mm:ss", new Date(total)).toString();
			
			//Add to database
			handler.addStanding(fentry.get(5).toString(), dateString);
		}
		
		ArrayList<ArrayList<String>> standings =  handler.getAllStandings();
		
		Iterator<ArrayList<String>> i =  standings.iterator();

		while (i.hasNext()) {

			ArrayList<?> entry = i.next();

			row = new TableRow(this);

			t1 = new TextView(this);
			t2 = new TextView(this);
			t1.setText(entry.get(0).toString());		
			t2.setText(entry.get(1).toString());

			t1.setTypeface(null, 1);
			// t1.setWidth(130);
			t2.setTypeface(null, 1);
			t2.setWidth(146);

			t1.setTextSize(15);
			t2.setTextSize(15);

			t1.setTextColor(Color.BLUE);
			t2.setTextColor(Color.RED);

			row.addView(t1);
			row.addView(t2);

			station_table.addView(row, new TableLayout.LayoutParams(
					LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT));

		}
	}
}
