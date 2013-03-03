package is.heklapunch;

import java.util.ArrayList;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

//TODO: IMPORTANT! the station numbering scheme is not safe! it needs to have an appropriate indexing system, or a update its counts on delete/update! fix asap.

public class SQLHandler extends SQLiteOpenHelper {

	private static final int DB_VERSION = 2;
	private static final String DATABASE_NAME = "hekladb";
	private static final String COMPETE_TABLE_NAME = "keppandi";
	private static final String COMPETE_STATION_NAME = "stationname";
	private static final String COMPETE_STATION_TIME = "stationtime";
	private static final String COMPETE_QR_VALUE = "qrvalue";
	private static final String COMPETE_GPS = "igps";
	private static final String ORGANIZE_TABLE_NAME = "courses";
	private static final String ORGANIZE_COURSE_NAME = "coursename";
	private static final String ORGANIZE_STATION_NUMBER = "stationnumber";
	private static final String ORGANIZE_QR_VALUE = "qrvalue";

	// required constructor
	public SQLHandler(Context context) {
		super(context, DATABASE_NAME, null, DB_VERSION);
	}

	// required overrides. creates a simple table with 2 string keys for the
	// setting name (primary) and its value, and a table with 3 string keys
	// for the course name(primary), station number and QR value
	@Override
	public void onCreate(SQLiteDatabase db) {
		String createCompetitorTable = "CREATE TABLE " + COMPETE_TABLE_NAME + "("
				+ COMPETE_STATION_NAME +  " TEXT NOT NULL,"
				+ COMPETE_STATION_TIME + " LONG NOT NULL,"
				+ COMPETE_GPS + " INT NOT NULL,"
				+ COMPETE_QR_VALUE + " TEXT NOT NULL)";
		
		String createCoursesTable = "CREATE TABLE " + ORGANIZE_TABLE_NAME + "("
				+ ORGANIZE_COURSE_NAME + " TEXT NOT NULL,"
				+ ORGANIZE_STATION_NUMBER + " INTEGER NOT NULL,"
				+ ORGANIZE_QR_VALUE + " TEXT, " + "PRIMARY KEY ("
				+ ORGANIZE_COURSE_NAME + ", " + ORGANIZE_STATION_NUMBER + "))";
		db.execSQL(createCompetitorTable);
		db.execSQL(createCoursesTable);
	}

	@Override
	public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
		db.execSQL("DROP TABLE IF EXISTS " + COMPETE_TABLE_NAME);
		db.execSQL("DROP TABLE IF EXISTS " + ORGANIZE_TABLE_NAME);
		onCreate(db);
	}

	// TODO: i have uglified this code, but dont have time to refactor
	// everything and make general purpose
	// TODO: CRUD methods, so i just duplicated the existing ones. we can fix
	// this later.

	// CRUD
	// Add a new setting with name name and value value to the database.
	// do not try to add a value that already exists, logcat gives you errors if
	// you do.
	// value can be null.
	// To update use updateSettingbyName(name, value)
	

	// Since a course without a station is nonexistant, we only add in stations
	// TODO: add handling for start/end stations, fix station numbering
	public void addStation(String courseName, String QRvalue)
			throws SQLException {
		
		SQLiteDatabase db = this.getWritableDatabase();
		// generate and send command
		ContentValues values = new ContentValues();
		try {
			int stations = stationCount(courseName);
			values.put(ORGANIZE_COURSE_NAME, courseName);
			values.put(ORGANIZE_STATION_NUMBER, stations);
			values.put(ORGANIZE_QR_VALUE, QRvalue);
		} catch (Exception e) {

		}
		// if we want a throw use the line below, if not use the line after that
		// db.insertOrThrow(ORGANIZE_TABLE_NAME, null, values);
		db.insert(ORGANIZE_TABLE_NAME, null, values);
		db.close();
	}

	
	// Get single course values, which is an arraylist of strings
	public ArrayList<ArrayList<String>> getCoursebyName(String courseName)
			throws SQLException {

		ArrayList<ArrayList<String>> results = new ArrayList<ArrayList<String>>();
		SQLiteDatabase db = this.getWritableDatabase();
		try {
			Cursor cursor = db.rawQuery("SELECT * FROM " + ORGANIZE_TABLE_NAME
					+ " WHERE " + ORGANIZE_COURSE_NAME + "=" + courseName
					+ " ORDER BY " + ORGANIZE_STATION_NUMBER, null);
		if (cursor.moveToFirst()) {
			do {
				ArrayList<String> station = new ArrayList<String>();
				station.add(cursor.getString(0));
				station.add(cursor.getString(1));
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
	
	//get the highest station number value from a course name
	public int getMaxStationbyCourse(String courseName)
			throws SQLException {

		SQLiteDatabase db = this.getReadableDatabase();
		// generate and send our query
		Cursor cursor = db.rawQuery("SELECT MAX(" + ORGANIZE_STATION_NUMBER + ") FROM " + ORGANIZE_TABLE_NAME
				+ " WHERE " + ORGANIZE_COURSE_NAME + "=" + courseName, null);
		if (cursor != null) {
			cursor.moveToFirst();
			if (cursor.getCount() > 0) {
				return cursor.getInt(1);
			} else
				return 0;
		} else
			return 0;
	}


	// update station courseName, currentStationNumber with newstationnumber and QRvalue
	// TODO: make this work correctly.
	public int updateStationbyNameandNumber(String courseName,
			int currentStationNumber, int newStationNumber, String QRvalue) {
		SQLiteDatabase db = this.getWritableDatabase();
		ContentValues values = new ContentValues();
		try {
			values.put(ORGANIZE_COURSE_NAME, courseName);
			values.put(ORGANIZE_STATION_NUMBER, currentStationNumber);
			values.put(ORGANIZE_QR_VALUE, QRvalue);
		} catch (Exception e) {

		}
		return db.update(ORGANIZE_TABLE_NAME, values, ORGANIZE_STATION_NUMBER
				+ " = ?", new String[] { String.valueOf(QRvalue) });
	}


	// Deletes course by name
	// TODO: make station numbering safe
	// TODO: create "delete station by name" method
	public void deleteStationbyNameandNumber(String name, int number) {
		if (name != null) {
			SQLiteDatabase db = this.getWritableDatabase();
			db.delete(
					ORGANIZE_TABLE_NAME,
					ORGANIZE_COURSE_NAME + ORGANIZE_STATION_NUMBER + " = ? ",
					new String[] { String.valueOf(name)
							+ String.valueOf(number) });
			db.close();
		}
	}
	
	// Gets the number of rows already in the course
	public int stationCount(String courseName) {
		SQLiteDatabase db = this.getWritableDatabase();
		String count = "SELECT * FROM " + ORGANIZE_TABLE_NAME + "WHERE "
				+ ORGANIZE_TABLE_NAME + " = " + courseName;
		Cursor mcursor = db.rawQuery(count, null);
		int icount = mcursor.getCount();
		db.close();
		return icount;
	}

		
	/* 
	 * 
	 * Logi notar þessu föll 
	 * 
	 * 
	 * */

	//add a staion
	public void addStation(String name,  long time, String qr, boolean gps) throws SQLException {
		SQLiteDatabase db = this.getWritableDatabase();
		// generate and send command
		ContentValues values = new ContentValues();
		try {
			values.put(COMPETE_STATION_NAME, name);
			values.put(COMPETE_STATION_TIME, time);
			values.put(COMPETE_QR_VALUE, qr);
			values.put(COMPETE_GPS, gps);
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
				results.add(station);
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
