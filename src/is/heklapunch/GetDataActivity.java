package is.heklapunch;

import java.sql.Date;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.Vector;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.text.format.DateFormat;
import android.util.TypedValue;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup.LayoutParams;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;

public class GetDataActivity extends Activity {

	SQLHandler handler;
	TableLayout station_table;
	int courseID = -1;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_getdata);

		// create database object
		handler = new SQLHandler(this);

		// make table
		station_table = (TableLayout) findViewById(R.id.receive_table);
		this.populateTable();

		// grabs the courses DATABASE id from the extras bundle
		Bundle b = getIntent().getExtras();
		if (b.containsKey("courseID")) {
			courseID = b.getInt("courseID");
		}

		ArrayList<ArrayList<String>> braut = handler.getCoursebyID(courseID);

		// Setjum hvaða braut er active
		TextView brautTitle = (TextView) findViewById(R.id.getdata_string);
		brautTitle.setText("Valin braut: " + braut.get(0).get(4).toString());
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
		// redraw view
		TableLayout vg = (TableLayout) findViewById(R.id.receive_table);
		vg.removeAllViews();
		// redraw table
		this.populateTable();

	}

	// fill table with results
	@SuppressLint("NewApi")
	public void populateTable() {

		TableRow row;
		TextView t0, t1, t2;

		// Converting to dip unit
		int dip = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP,
				(float) 1, getResources().getDisplayMetrics());

		ArrayList<ArrayList<String>> results = handler.getAllResults();
		Iterator<ArrayList<String>> ii = results.iterator();

		// Find all competitors
		Vector competitors = new Vector();
		while (ii.hasNext()) {
			ArrayList<?> entry = ii.next();
			if (!competitors.contains(entry.get(5))) {
				competitors.add(entry.get(5));
			}
		}

		// Get total time for each competitor
		Iterator itr = competitors.iterator();

		while (itr.hasNext()) {
			// set correctes to true
			boolean isCorrect = true;
			// To check if order is correct
			ArrayList<ArrayList<String>> course = handler
					.getCoursebyID(courseID);

			// Get all entries for each competitor and calculate total time
			ArrayList<ArrayList<String>> indResults = handler
					.getResultsForCompetitor(itr.next().toString());
			ArrayList<?> fentry = indResults.get(0);
			ArrayList<?> lentry = indResults.get(indResults.size() - 1);

			String longStart = fentry.get(1).toString();
			long millisecondStart = Long.parseLong(longStart);

			String longEnd = lentry.get(1).toString();
			long millisecondEnd = Long.parseLong(longEnd);
			// Total time of run
			long total = millisecondEnd - millisecondStart;
			// Make proper date string
			String dateString = DateFormat.format("kk:mm:ss", new Date(total))
					.toString();

			// Compare to the actual course
			Iterator<ArrayList<String>> i = course.iterator();
			Iterator<ArrayList<String>> i2 = indResults.iterator();

			if (course.size() == indResults.size()) {
				while (i.hasNext()) {

					ArrayList<?> entry = i.next();
					ArrayList<?> entry2 = i2.next();

					String e = entry.get(5).toString();
					String e2 = entry2.get(3).toString();
					if (!e.equalsIgnoreCase(e2)) {
						isCorrect = false;
					}

				}
			} else {
				isCorrect = false;
			}

			// Add to database
			// Entry is correct
			if (isCorrect) {
				handler.addStanding(fentry.get(5).toString(), dateString, courseID);
			} else {
				if (course.size() != indResults.size()) {
					handler.addStanding(fentry.get(5).toString(),
							"Vitlaus fjöldi stöðva", courseID);
				} else {
					handler.addStanding(fentry.get(5).toString(),
							"Vitlaus röð stöðva", courseID);
				}
			}

		}

		ArrayList<ArrayList<String>> standings = handler.getAllStandings(this.courseID);

		Iterator<ArrayList<String>> i = standings.iterator();
		int saeti = 1;

		while (i.hasNext()) {

			ArrayList<?> entry = i.next();

			row = new TableRow(this);

			t0 = new TextView(this);
			t1 = new TextView(this);
			t2 = new TextView(this);
			t0.setText(Integer.toString(saeti));
			saeti++;
			t1.setText(entry.get(0).toString());
			t2.setText(entry.get(1).toString());

			t0.setTypeface(null, 1);
			t0.setWidth(50);
			t1.setTypeface(null, 1);
			t1.setWidth(150);
			t2.setTypeface(null, 1);
			t2.setWidth(400);

			t0.setTextSize(15);
			t1.setTextSize(15);
			t2.setTextSize(15);

			t0.setTextColor(Color.RED);
			t1.setTextColor(Color.GREEN);
			t2.setTextColor(Color.RED);

			row.addView(t0);
			row.addView(t1);
			row.addView(t2);

			station_table.addView(row, new TableLayout.LayoutParams(
					LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT));

		}
		//Then we clear results so that they will not be added many times
		handler.deleteResults();
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		getMenuInflater().inflate(R.menu.activity_getdata, menu);
		return true;
	}

	@Override
	// Handle menu clicks
	public boolean onOptionsItemSelected(MenuItem item) {
		// Handle item selection
		switch (item.getItemId()) {

		case R.id.getdata_henda:
			delete_results(this.getCurrentFocus());
			return true;

		default:
			return super.onOptionsItemSelected(item);
		}
	}

}
