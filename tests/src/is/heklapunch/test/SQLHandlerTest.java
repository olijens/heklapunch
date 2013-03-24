package is.heklapunch.test;

import is.heklapunch.SQLHandler;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.test.AndroidTestCase;
import android.test.RenamingDelegatingContext;

public class SQLHandlerTest extends AndroidTestCase {
	
	MockSQLHandlerTest mHandler;
	
	public class MockSQLHandlerTest extends SQLHandler {
		private static final String DATABASE_NAME = "hekladbTEST";
		private static final int DB_VERSION = 2;
		private static final String COMPETE_TABLE_NAME = "keppandi";
		private static final String STANDING_TABLE_NAME = "standing";
		private static final String RESULT_TABLE_NAME = "results";
		private static final String COMPETE_STATION_NAME = "stationname";
		private static final String COMPETE_STATION_TIME = "stationtime";
		private static final String COMPETE_QR_VALUE = "qrvalue";
		private static final String COMPETE_TIME_CHECK = "timecheck";
		private static final String COMPETE_GPS_LOCATION = "gps_location";
		private static final String ORGANIZE_TABLE_NAME = "courses";
		private static final String ORGANIZE_STATION_ID = "stationid";
		private static final String ORGANIZE_COURSE_NAME = "coursename";
		private static final String ORGANIZE_COURSE_ID = "coursenumber";
		private static final String ORGANIZE_STATION_NUMBER = "stationnumber";
		private static final String ORGANIZE_STATION_NAME = "stationname";
		private static final String ORGANIZE_QR_VALUE = "qrvalue";
		private static final String ORGANIZE_GPS_VALUE = "gpsvalue";
		private static final String RESULT_COMPETITOR_NAME = "nafn_keppanda";
		private static final String TOTAL_TIME = "total_time";
		
		// required constructor
		public MockSQLHandlerTest(Context context) {
			super(context);
		}
		
		public boolean checkCoarse() {
			SQLiteDatabase db = this.getWritableDatabase();
			try {
				Cursor cursor = db.rawQuery("SELECT * FROM " + ORGANIZE_TABLE_NAME + ";", null);
			} catch (Exception e) {
				return false;
			}
			return true;
		}
	}
	
	@Override
	protected void setUp() throws Exception {
		super.setUp();
		RenamingDelegatingContext context 
        	= new RenamingDelegatingContext(getContext(), "test_");
		mHandler = new MockSQLHandlerTest(context);

	}
	
	@Override
	protected void tearDown() throws Exception {
		// DELETE DB
	}
	
	public SQLHandlerTest() {
		
	}
	
	public void test_create_coarse() {
		this.assertTrue(mHandler.checkCoarse());
	}
	
}
