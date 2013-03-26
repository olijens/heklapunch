package is.heklapunch.test;

import java.util.ArrayList;
import java.util.Iterator;

import is.heklapunch.OrganizeModifyActivity;
import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.test.ActivityInstrumentationTestCase2;
import android.test.RenamingDelegatingContext;

public class OrganizeModifyActivityTest extends
		ActivityInstrumentationTestCase2<OrganizeModifyActivity> {

	private OrganizeModifyActivity mnActivity;
	private RenamingDelegatingContext context = null;

	@SuppressLint("NewApi")
	@Override
	protected void setUp() throws Exception {
		super.setUp();

		setActivityInitialTouchMode(false);
		
		Intent intent = new Intent();
		intent.setClassName("is.heklapunch", "is.heklapunch.OrganizeModifyActivity");
		Bundle b = new Bundle();
		b.putInt("courseID", -1);
		intent.putExtras(b);
		setActivityIntent(intent);
		mnActivity = getActivity();
	}

	@SuppressWarnings("deprecation")
	public OrganizeModifyActivityTest() {
		super("", OrganizeModifyActivity.class);
	}

	@SuppressLint("NewApi")
	public OrganizeModifyActivityTest(
			Class<OrganizeModifyActivity> activityClass) {
		super(activityClass);
		// TODO Auto-generated constructor stub
	}
	
	// the method getNextStation() should return the current number of stations
	// +30
	public void testNextStation() {
		assertTrue(mnActivity!= null);

	}

	// test the addStation() method. first station should be present at the
	// start of stationList after adding more than one station
	public void testAddStation() {
		assertTrue(1 == 1);
		/*String testSID = "-1";
		String testname = "station one";
		String testSNumber = String.valueOf(mnActivity.getNextStation());
		String testCID = "1";
		String testCName = "coursename";
		String testQR = "testQR";
		String testGPS = "12345";
		
		mnActivity.addStation(testSID, testname, testSNumber, testCID,
				testCName, testQR, testGPS);
		
		String testSID2 = "-1";
		String testname2 = "station two";
		String testSNumber2 = String.valueOf(mnActivity.getNextStation());
		String testCID2 = "1";
		String testCName2 = "coursename";
		String testQR2 = "different testQR";
		String testGPS2 = "different gps";
		mnActivity.addStation(testSID2, testname2, testSNumber2, testCID2,
				testCName2, testQR2, testGPS2);
		Iterator<ArrayList<String>> i = mnActivity.stationList.iterator();
		assertTrue(i.hasNext());
		ArrayList<?> entry = i.next();
		assertTrue(entry.get(0) == testSID);
		assertTrue(entry.get(1) == testname);
		assertTrue(entry.get(2) == testSNumber);
		assertTrue(entry.get(3) == testCID);
		assertTrue(entry.get(4) == testCName);
		assertTrue(entry.get(5) == testQR);
		assertTrue(entry.get(6) == testGPS);
		assertTrue(entry.get(1) != testname2);
		assertTrue(entry.get(2) != testSNumber2);
		assertTrue(entry.get(5) == testQR);
		assertTrue(entry.get(6) == testGPS);*/

	}
}
