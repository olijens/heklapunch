package is.heklapunch;

import java.util.ArrayList;
import java.util.Iterator;

import com.google.zxing.integration.android.IntentIntegrator;
import com.google.zxing.integration.android.IntentResult;

import android.app.Activity;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;
import android.widget.Toast;
import android.util.TypedValue;
import android.view.ViewGroup.LayoutParams;

public class OrganizeCreateActivity extends Activity {

	TableLayout station_table;
	SQLHandler handler;
	EditText stationNameField;
	EditText courseNameField;
	String stationName = "";
	ArrayList<ArrayList<String>> stationList = new ArrayList<ArrayList<String>>();
	int stationNumber = 1;
	Spinner courseSpinner;
	CourseData[] courses;
	int selectedCourseID = -1;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_organize_create);
		// create database object
		handler = new SQLHandler(this);
		courseNameField = (EditText) findViewById(R.id.editTextCourseName);
		stationNameField = (EditText) findViewById(R.id.EditTextStationName);

		// populate the spinner
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
						}

						public void onNothingSelected(AdapterView<?> parent) {
						}
					});
		}
	}

	public void createCourse(View view) {
		Intent intent = new Intent(this, OrganizeModifyActivity.class);
		Bundle b = new Bundle();
		b.putInt("courseID", -1);
		intent.putExtras(b);
		startActivity(intent);
		finish();
	}

	public void modifyCourse(View view) {
		Intent intent = new Intent(this, OrganizeModifyActivity.class);
		Bundle b = new Bundle();
		b.putInt("courseID", selectedCourseID);
		intent.putExtras(b);
		startActivity(intent);
		finish();
	}
}
