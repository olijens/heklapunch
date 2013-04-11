package is.heklapunch;


import is.heklapunch.sendjson.SendJSON;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.widget.Toast;

public class SendJSONActivity extends Activity {

	private static final boolean DEBUG = true;
	private static final String TAG = "SendActivity";
	private String _url = null;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		SharedPreferences pref = this.getSharedPreferences("send_to_url", Context.MODE_PRIVATE);
        this._url = pref.getString("send_to_url", null);
        if(this._url == null) {
        	Toast.makeText(this, "Enginn viðtakandi er settur", Toast.LENGTH_LONG).show();
        	this.finish();
        } else {
        	SendJSON task = new SendJSON(this._url, this.get_payload(), this);
        	task.execute();
        }
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.activity_send, menu);
		return true;
	}
	
	public void SetResponce(String responce) {
		Toast.makeText(this, responce, Toast.LENGTH_LONG).show();
		this.finish();
	}

	/*
	 * gagnastreng til að senda á url
	 * */
	private HashMap<String, HashMap<String, String>> get_payload() {
		SQLHandler handler = new SQLHandler(this);
		ArrayList<ArrayList<String>> results = handler.getAllStations();
		//Gögn frá gagnasafni í ArrayLista
		Iterator<ArrayList<String>> i = results.iterator();
		
		//Hlutur sem verður sendur
		HashMap<String, HashMap<String, String>> payload = new HashMap<String, HashMap<String, String>>();
		HashMap<String, String> info = new HashMap<String, String>();
		HashMap<String, String> data = new HashMap<String, String>();
		
		//Ná í nafn keppanda
		SharedPreferences pref = this.getSharedPreferences("competitor_name", Context.MODE_PRIVATE);
        String restoredText = pref.getString("competitor_name", null);
        if(restoredText == null) restoredText = "NO USERNAME";
        info.put("username", restoredText);
        
        //Bæta við upplýsingum um stöðvar
		while (i.hasNext()) {
			ArrayList<String> entry = i.next();
			data.put(
				entry.get(3).toString(), 
				entry.get(1).toString()
			);
		}	
		
		payload.put("info", info);
		payload.put("data", data);
		
		return payload;
	}
	
	public void onActivityResult(int requestCode, int resultCode, Intent data) {
		if (DEBUG)
			Log.d(TAG, "onActivityResult " + resultCode);
		
	}
}
