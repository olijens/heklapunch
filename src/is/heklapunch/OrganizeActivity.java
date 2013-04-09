package is.heklapunch;

import java.sql.Date;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.Vector;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.text.format.DateFormat;
import android.util.Log;
import android.util.TypedValue;
import android.view.View;
import android.view.ViewGroup.LayoutParams;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Spinner;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;

public class OrganizeActivity extends Activity {

	SQLHandler handler;
	TableLayout station_table;
	int selectedCourseID = -1;
	String selectedCourseName = "";
	Spinner courseSpinner;
	Button editButton;
	Button deleteButton;
	Button adminButton;
	public CourseData[] courses;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_organize);
		editButton = (Button) findViewById(R.id.edit_button);
		deleteButton = (Button) findViewById(R.id.delete_button);
		adminButton = (Button) findViewById(R.id.admin_button);

		// create database object
		handler = new SQLHandler(this);
		// populate the spinner
		populateSpinner();
	}
	
	//sets up the spinner
	public void populateSpinner(){
		editButton.setEnabled(false);
		deleteButton.setEnabled(false);
		adminButton.setEnabled(false);
		courses = handler.getCourseIDs();
		if (courses != null) {
			courseSpinner = (Spinner) findViewById(R.id.spinner1);
			ArrayAdapter<CourseData> adapter = new ArrayAdapter<CourseData>(
					this, android.R.layout.simple_spinner_item,
					handler.getCourseIDs());
			adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
			courseSpinner.setAdapter(adapter);
			courseSpinner
					.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
						public void onItemSelected(AdapterView<?> parent,
								View view, int position, long id) {
							CourseData data = courses[position];
							selectedCourseID = Integer.valueOf(data.getValue());
							selectedCourseName = data.getSpinnerText();
						}

						public void onNothingSelected(AdapterView<?> parent) {
						}
					});
			if(courses.length > 0)
			{
				editButton.setEnabled(true);
				deleteButton.setEnabled(true);
				adminButton.setEnabled(true);
			}
		}
	}

	// Go to create mode
	public void create(View view) {
		Intent m = new Intent(this, OrganizeCreateActivity.class);
		startActivity(m);

		// create database object
		handler = new SQLHandler(this);
	}

	// Go to recieve mode
	public void administer(View view) {
		Intent r = new Intent(this, OrganizeReceiveActivity.class);
		startActivity(r);
	}

	// go to modifycourse activity with no course name
	public void createCourse(View view) {
		Intent intent = new Intent(this, OrganizeModifyActivity.class);
		Bundle b = new Bundle();
		b.putInt("courseID", -1);
		intent.putExtras(b);
		startActivity(intent);
		finish();
	}

	// opens eh modify course activity with the selected course id
	public void modifyCourse(View view) {
		Intent intent = new Intent(this, OrganizeModifyActivity.class);
		Bundle b = new Bundle();
		b.putInt("courseID", selectedCourseID);
		intent.putExtras(b);
		startActivity(intent);
		finish();
	}

	// deletes the course with the selected course id
	public void deleteCourse(View view) {
		new AlertDialog.Builder(this)
		.setIcon(android.R.drawable.ic_dialog_alert)
		.setTitle("Eyða braut?")
		.setMessage(
				"Eyða braut \"" + selectedCourseName + "\"?")
		.setPositiveButton("já", new DialogInterface.OnClickListener() {

			public void onClick(DialogInterface dialog, int which) {
				handler.removeCourseByID(selectedCourseID);
				populateSpinner();
			}

		}).setNegativeButton("Nei", null).show();
	}
	
	//Bedlow this is now depricated.

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

		// Find all competitors
		Vector competitors = new Vector();
		while (ii.hasNext()) {
			ArrayList<?> entry = ii.next();
			if (!competitors.contains(entry.get(5))) {
				competitors.add(entry.get(5));
			}
		}

		Log.d("Logatest", "Fjöldi keppanda: " + competitors.size());

		// Get total time for each competitor
		Iterator itr = competitors.iterator();

		while (itr.hasNext()) {
			// set correctes to true
			boolean isCorrect = true;
			// To check if order is correct
			ArrayList<ArrayList<String>> course = handler.getCoursebyID(1);
			Log.d("Logatest", "Stærð brautar: " + course.size());

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
					Log.d("Logatest", "entry er " + e);
					String e2 = entry2.get(3).toString();
					Log.d("Logatest", "entry2 er " + e2);
					if (!e.equalsIgnoreCase(e2)) {
						isCorrect = false;
					}

				}
			} else {
				isCorrect = false;
			}

			// Add to database
			if (isCorrect) {
				handler.addStanding(fentry.get(5).toString(), dateString);
			} else {
				handler.addStanding(fentry.get(5).toString(),
						"Villa hjá hlaupara");
			}
		}

		ArrayList<ArrayList<String>> standings = handler.getAllStandings();

		Iterator<ArrayList<String>> i = standings.iterator();

		while (i.hasNext()) {

			ArrayList<?> entry = i.next();

			row = new TableRow(this);

			t1 = new TextView(this);
			t2 = new TextView(this);
			t1.setText(entry.get(0).toString());
			t2.setText(entry.get(1).toString());

			t1.setTypeface(null, 1);
			t1.setWidth(150);
			t2.setTypeface(null, 1);
			t2.setWidth(400);

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
