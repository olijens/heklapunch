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
		adminButton = (Button) findViewById(R.id.modify_add);

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
		Intent r = new Intent(this, GetDataActivity.class);
		Bundle b = new Bundle();
		b.putInt("courseID", selectedCourseID);
		r.putExtras(b);
		startActivity(r);
		finish();
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
	
}
