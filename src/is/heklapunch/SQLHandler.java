package is.heklapunch;

import java.util.ArrayList;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class SQLHandler extends SQLiteOpenHelper {

	private static final int DB_VERSION = 2;
	private static final String DATABASE_NAME = "hekladb";
	private static final String SETTINGS_TABLE_NAME = "keppandi";
	private static final String KEY_SETTING_NAME = "setting";
	private static final String KEY_SETTING_VALUE = "value";

	// required constructor
	public SQLHandler(Context context) {
		super(context, DATABASE_NAME, null, DB_VERSION);
	}

	// required overrides. creates a simple table with 2 string keys for the
	// setting name (primary) and its value
	@Override
	public void onCreate(SQLiteDatabase db) {
		String createTable = "CREATE TABLE " + SETTINGS_TABLE_NAME + "("
				+ KEY_SETTING_NAME + " TEXT PRIMARY KEY," + KEY_SETTING_VALUE
				+ " TEXT" + ")";
		db.execSQL(createTable);
	}

	@Override
	public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
		db.execSQL("DROP TABLE IF EXISTS " + SETTINGS_TABLE_NAME);
		onCreate(db);
	}

	// CRUD
	// Add a new setting with name name and value value to the database.
	// do not try to add a value that already exists, logcat gives you errors if you do.
	// value can be null.
	// To update use updateSettingbyName(name, value)
	public void addSetting(String name, String value) throws SQLException {
		SQLiteDatabase db = this.getWritableDatabase();
		// generate and send command
		ContentValues values = new ContentValues();
		try {
			values.put(KEY_SETTING_NAME, name);
			values.put(KEY_SETTING_VALUE, value);
		} catch (Exception e) {

		}
		// if we want a throw use the line below, if not use the line after that
		// db.insertOrThrow(TABLE_NAME, null, values);
		db.insert(SETTINGS_TABLE_NAME, null, values);
		db.close();
	}

	// Get value for single setting from database.
	// returns string containing value of setting with name name.
	// if that setting does not exist or has a null value, returns null
	public String getSettingbyName(String name) throws SQLException {
		SQLiteDatabase db = this.getReadableDatabase();
		// generate and send our query
		Cursor cursor = db.query(SETTINGS_TABLE_NAME, new String[] { KEY_SETTING_NAME,
				KEY_SETTING_VALUE }, KEY_SETTING_NAME + "=?",
				new String[] { String.valueOf(name) }, null, null, null, null);
		if (cursor != null) {
			cursor.moveToFirst();
			if (cursor.getCount() > 0) {
				return cursor.getString(1);
			} else
				return null;
		} else
			return null;
	}

	// Update a setting value in database
	// updates the setting with name name to setting value
	// Will not add entries if they are missing.
	public int updateSettingbyName(String name, String value) {
		SQLiteDatabase db = this.getWritableDatabase();
		ContentValues values = new ContentValues();
		try {
			values.put(KEY_SETTING_NAME, name);
			values.put(KEY_SETTING_VALUE, value);
		} catch (Exception e) {

		}
		return db.update(SETTINGS_TABLE_NAME, values, KEY_SETTING_NAME + " = ?",
				new String[] { String.valueOf(name) });
	}

	// Deletes setting with name name from database
	//does nothing if setting does not exist, or name = null
	public void deleteSettingbyName(String name) {
		if(name != null){
			SQLiteDatabase db = this.getWritableDatabase();
			db.delete(SETTINGS_TABLE_NAME, KEY_SETTING_NAME + " = ? ",
					new String[] { String.valueOf(name) });
			db.close();
		}
	}
	//Gets the number of rows already in the table
	public int count(){
	    SQLiteDatabase db = this.getWritableDatabase();
	    String count = "SELECT * FROM " + this.SETTINGS_TABLE_NAME;
	    Cursor mcursor = db.rawQuery(count, null);
	    int icount = mcursor.getCount();
	    db.close();
	    return icount;
	}
	
	//Get all values from the table
	public ArrayList<ArrayList<String>> getAllStations()
	{
		
	   ArrayList<ArrayList<String>> results = new ArrayList<ArrayList<String>>();
	   SQLiteDatabase db = this.getWritableDatabase();
	   Cursor cursor = db.rawQuery("SELECT * FROM " + this.SETTINGS_TABLE_NAME + " ORDER BY " + this.KEY_SETTING_NAME , null);
	   if(cursor.moveToFirst())
	   {
	       do
	       {
	           ArrayList<String> station = new ArrayList<String>();
	           station.add(cursor.getString(0));
	           station.add(cursor.getString(1));
	           results.add(station);
	       }
	       while(cursor.moveToNext());
	       
	       if(cursor != null && !cursor.isClosed()){
	          cursor.close();
	       }
	   }
	   db.close();
	   return results;
	}
	
}
