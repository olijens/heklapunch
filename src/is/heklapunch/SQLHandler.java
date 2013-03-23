package is.heklapunch;

import java.util.ArrayList;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;
import android.widget.ArrayAdapter;
import android.widget.SimpleCursorAdapter;

//TODO: IMPORTANT! the station numbering scheme is not safe! it needs to have an appropriate indexing system, or a update its counts on delete/update! fix asap.

public class SQLHandler extends SQLiteOpenHelper {

	private static final int DB_VERSION = 2;
	private static final String DATABASE_NAME = "hekladb";
	private static final String COMPETE_TABLE_NAME = "keppandi";
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

	// required constructor
	public SQLHandler(Context context) {
		super(context, DATABASE_NAME, null, DB_VERSION);
	}

	// required overrides. creates a simple table with 2 string keys for the
	// setting name (primary) and its value, and a table with 3 string keys
	// for the course name(primary), station number and QR value
	@Override
	public void onCreate(SQLiteDatabase db) {
		String createCompetitorTable = "CREATE TABLE " + COMPETE_TABLE_NAME
				+ "(" + COMPETE_STATION_NAME + " TEXT NOT NULL,"
				+ COMPETE_STATION_TIME + " LONG NOT NULL," 
				+ COMPETE_TIME_CHECK + " INT NOT NULL," 
				+ COMPETE_QR_VALUE + " TEXT NOT NULL,"
				+ COMPETE_GPS_LOCATION + " TEXT NOT NULL)";

		String createCoursesTable = "CREATE TABLE " + ORGANIZE_TABLE_NAME
				+ " (" + ORGANIZE_STATION_ID
				+ " INTEGER PRIMARY KEY AUTOINCREMENT, "
				+ ORGANIZE_STATION_NAME + " TEXT, " + ORGANIZE_STATION_NUMBER
				+ " INTEGER, " + ORGANIZE_COURSE_ID + " INTEGER NOT NULL, "
				+ ORGANIZE_COURSE_NAME + " TEXT NOT NULL, " + ORGANIZE_QR_VALUE
				+ " TEXT NOT NULL, " + ORGANIZE_GPS_VALUE + " TEXT, "
				+ "UNIQUE (" + ORGANIZE_COURSE_ID + ", " + ORGANIZE_QR_VALUE
				+ " ), " + "UNIQUE (" + ORGANIZE_COURSE_ID + ", "
				+ ORGANIZE_STATION_NUMBER + " )" + " )";
		
		
		db.execSQL(createCompetitorTable);
		db.execSQL(createCoursesTable);
	}

	@Override
	public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
		db.execSQL("DROP TABLE IF EXISTS " + COMPETE_TABLE_NAME);
		db.execSQL("DROP TABLE IF EXISTS " + ORGANIZE_TABLE_NAME);
		onCreate(db);
	}

	// CRUD
	// Add a new setting with name name and value value to the database.
	// do not try to add a value that already exists, logcat gives you errors if
	// you do.
	// value can be null.
	// To update use updateSettingbyName(name, value)

	// Since a course without a station is nonexistant, we only add in stations
	public void addStation(String courseName, int courseID, String stationName,
			int stationNumber, String QRValue, String GPSValue)
			throws SQLException {

		SQLiteDatabase db = this.getWritableDatabase();
		// generate and send command
		ContentValues values = new ContentValues();
		try {
			values.put(ORGANIZE_STATION_NAME, stationName);
			values.put(ORGANIZE_STATION_NUMBER, stationNumber);
			values.put(ORGANIZE_COURSE_ID, courseID);
			values.put(ORGANIZE_COURSE_NAME, courseName);
			values.put(ORGANIZE_QR_VALUE, QRValue);
			values.put(ORGANIZE_GPS_VALUE, GPSValue);
		} catch (Exception e) {

		}
		// if we want a throw use the line below, if not use the line after that
		// db.insertOrThrow(ORGANIZE_TABLE_NAME, null, values);
		db.insert(ORGANIZE_TABLE_NAME, null, values);
		db.close();
	}

	// Get single courses' stations' values, which is an arraylist of arraylists
	// strings
	public ArrayList<ArrayList<String>> getCoursebyID(int courseID)
			throws SQLException {

		ArrayList<ArrayList<String>> results = new ArrayList<ArrayList<String>>();
		SQLiteDatabase db = this.getWritableDatabase();
		try {
			Cursor cursor = db.rawQuery("SELECT * FROM " + ORGANIZE_TABLE_NAME
					+ " WHERE " + ORGANIZE_COURSE_ID + "=" + courseID
					+ " ORDER BY " + ORGANIZE_STATION_NUMBER, null);
			if (cursor.moveToFirst()) {
				do {
					ArrayList<String> station = new ArrayList<String>();
					//station ID
					station.add(cursor.getString(0));
					//station name
					station.add(cursor.getString(1));
					//station number
					station.add(cursor.getString(2));
					//course ID
					station.add(cursor.getString(3));
					//course name
					station.add(cursor.getString(4));
					//QR value
					station.add(cursor.getString(5));
					//GPS value
					station.add(cursor.getString(6));
					results.add(station);
				} while (cursor.moveToNext());

				if (cursor != null && !cursor.isClosed()) {
					cursor.close();
				}
			}
			db.close();
		} catch (Exception e) {

		}
		return results;
	}

	// get the highest station number value from a course name
	/*
	 * DEPRICATED public int getMaxStationbyCourse(String courseName) throws
	 * SQLException {
	 * 
	 * SQLiteDatabase db = this.getReadableDatabase(); // generate and send our
	 * query Cursor cursor = db.rawQuery("SELECT MAX(" + ORGANIZE_STATION_NUMBER
	 * + ") FROM " + ORGANIZE_TABLE_NAME + " WHERE " + ORGANIZE_COURSE_NAME +
	 * "=" + courseName, null); if (cursor != null) { cursor.moveToFirst(); if
	 * (cursor.getCount() > 0) { return cursor.getInt(1); } else return 0; }
	 * else return 0; }
	 */

	// update station with id stationID, currentStationNumber with
	// newstationnumber and QRvalue
	// TODO: this should be depricated soon
	public int updateStation(int stationID, String newCourseName,
			int newStationNumber, String newQRvalue, String newGPSValue) {
		SQLiteDatabase db = this.getWritableDatabase();
		ContentValues values = new ContentValues();
		try {
			values.put(ORGANIZE_COURSE_NAME, newCourseName);
			values.put(ORGANIZE_STATION_NUMBER, newStationNumber);
			values.put(ORGANIZE_QR_VALUE, newQRvalue);
			values.put(ORGANIZE_GPS_VALUE, newGPSValue);
		} catch (Exception e) {

		}
		return db.update(ORGANIZE_TABLE_NAME, values, ORGANIZE_STATION_ID
				+ " = ?", new String[] { String.valueOf(stationID) });
	}

	// Deletes station by name
	public void deleteStationbyID(int ID) {
		SQLiteDatabase db = this.getWritableDatabase();
		db.delete(ORGANIZE_TABLE_NAME, ORGANIZE_STATION_ID + " = ? ",
				new String[] { String.valueOf(ID) });
		db.close();
	}

	// Gets the number of rows already in the course
	public int stationCount(int courseID) {
		SQLiteDatabase db = this.getWritableDatabase();
		String count = "SELECT * FROM " + ORGANIZE_TABLE_NAME + "WHERE "
				+ ORGANIZE_TABLE_NAME + " = " + courseID;
		Cursor mcursor = db.rawQuery(count, null);
		int icount = mcursor.getCount();
		db.close();
		return icount;
	}
	
	// Gets the number of courses already in the table
	public int courseCount() {
		SQLiteDatabase db = this.getWritableDatabase();
		String count = "SELECT DISTINCT " + ORGANIZE_COURSE_ID + " FROM " + ORGANIZE_TABLE_NAME;
		Cursor c = db.rawQuery(count, null);
		int icount = c.getCount();
		db.close();
		return icount;
	}

	// returns highest course ID number
	public int getMaxCourseID() {
		SQLiteDatabase db = this.getWritableDatabase();
		String query = "SELECT MAX (" + ORGANIZE_COURSE_ID + ") FROM "
				+ ORGANIZE_TABLE_NAME;
		Cursor mcursor = db.rawQuery(query, null);
		mcursor.moveToFirst();
		int cCount = (int)mcursor.getInt(0);
		db.close();
		return cCount;
	}

	// removes course with course ID targetID
	public void removeCourseByID(int targetID) {
		SQLiteDatabase db = this.getWritableDatabase();
		db.delete(ORGANIZE_TABLE_NAME, ORGANIZE_COURSE_ID + " = ? ",
				new String[] { String.valueOf(targetID) });
		db.close();
	}

	// returns true iff the database contains a course with courseID target
	public boolean checkCoursebyID(int target) {
		SQLiteDatabase db = this.getWritableDatabase();
		Cursor c = db.rawQuery("SELECT * FROM " + ORGANIZE_TABLE_NAME + " WHERE " + ORGANIZE_COURSE_ID + "= " + String.valueOf(target), null);
		if (c.getCount() == 0) {
			return false;
		} 
		else {
			return true;
		}
	}
	
	// returns array containing the course names and corrisponding courseIDs
	public CourseData[] getCourseIDs(){
        String query = "SELECT " +ORGANIZE_COURSE_ID + ", " + ORGANIZE_COURSE_NAME + " FROM " + ORGANIZE_TABLE_NAME + " GROUP BY " + ORGANIZE_COURSE_ID ;
        SQLiteDatabase db = this.getReadableDatabase();
        
		CourseData[] data = null;        
        try {
        	Cursor cursor = db.rawQuery(query, null);
        	int q = cursor.getCount();
        	data = new CourseData[q];
			if (cursor.moveToFirst()) {
				int i = 0;
				do {
					data[i] = new CourseData(cursor.getString(1), cursor.getString(0));
					i++;
				} while (cursor.moveToNext());

				if (cursor != null && !cursor.isClosed()) {
					cursor.close();
				}
			}
			db.close();
		} catch (Exception e) {

		}
        return data;
	}

	/*
	 * 
	 * Logi notar þessu föll
	 */

	// add a staion
	public void addStation(String name, long time, String qr, boolean check,
			String gps) throws SQLException {
		SQLiteDatabase db = this.getWritableDatabase();
		// generate and send command
		ContentValues values = new ContentValues();
		try {
			values.put(COMPETE_STATION_NAME, name);
			values.put(COMPETE_STATION_TIME, time);
			values.put(COMPETE_QR_VALUE, qr);
			values.put(COMPETE_TIME_CHECK, check);
			values.put(COMPETE_GPS_LOCATION, gps);
		} catch (Exception e) {

		}
		// if we want a throw use the line below, if not use the line after that
		// db.insertOrThrow(ORGANIZE_TABLE_NAME, null, values);
		db.insert(COMPETE_TABLE_NAME, null, values);
		db.close();
	}

	// Delete all stations
	public void deleteAll() {
		SQLiteDatabase db = this.getWritableDatabase();
		db.delete(COMPETE_TABLE_NAME, null, null);
		db.close();
	}

	// Get all values from the table
	public ArrayList<ArrayList<String>> getAllStations() {

		ArrayList<ArrayList<String>> results = new ArrayList<ArrayList<String>>();
		SQLiteDatabase db = this.getWritableDatabase();
		Cursor cursor = db.rawQuery("SELECT * FROM " + COMPETE_TABLE_NAME
				+ " ORDER BY " + COMPETE_STATION_TIME, null);
		if (cursor.moveToFirst()) {
			do {
				ArrayList<String> station = new ArrayList<String>();
				station.add(cursor.getString(0));
				station.add(Long.toString(cursor.getLong(1)));
				station.add(cursor.getString(3));
				station.add(cursor.getString(4));
				results.add(station);
				Log.d("Loga test", cursor.getString(4));
			} while (cursor.moveToNext());

			if (cursor != null && !cursor.isClosed()) {
				cursor.close();
			}
		}
		db.close();
		return results;
	}

	// Gets the number of rows already in the table
	public int count() {
		SQLiteDatabase db = this.getWritableDatabase();
		String count = "SELECT * FROM " + COMPETE_TABLE_NAME;
		Cursor mcursor = db.rawQuery(count, null);
		int icount = mcursor.getCount();
		db.close();
		return icount;
	}

}
