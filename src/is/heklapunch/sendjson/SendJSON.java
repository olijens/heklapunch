package is.heklapunch.sendjson;
/*
 * Permission is hereby granted, free of charge, to any person obtaining a copy of this software and associated documentation files (the "Software"), 
 * to deal in the Software without restriction, including without limitation the rights to use, copy, modify, merge, publish, distribute, sublicense, 
 * and/or sell copies of the Software, and to permit persons to whom the Software is furnished to do so, subject to the following conditions:
 * 
 * 	The above copyright notice and this permission notice shall be included in all copies or substantial portions of the Software.
 * 
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, 
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER 
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS 
 * IN THE SOFTWARE.
 * */
import is.heklapunch.SendJSONActivity;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.HashMap;
import java.util.Map;

import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.protocol.HTTP;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.util.Log;
import android.widget.Toast;

import com.google.gson.GsonBuilder;
import com.google.gson.JsonSyntaxException;

public class SendJSON extends AsyncTask<String, Void, Integer> {
	
	private static final boolean DEBUG = true;
	private static final String TAG = "SendJSON";

	protected int _status_code;
	protected HashMap<String, HashMap<String, String>> _payload;
	protected Activity _caller;
	protected String _url;
	protected String _last_error;
	protected ProgressDialog mProgressDialog;

	@Override protected Integer doInBackground(String... request) {
		this._last_error = null;
		if(!this.isOnline()) {
			this._last_error = "Ekki tngdur netinu";
			return -1;
		}
		
		return this.send();
	}
	
	@Override protected void onPostExecute(Integer code) {
		mProgressDialog.dismiss();
		if(this._caller instanceof SendJSONActivity) {
			if(code == -1) {
				((SendJSONActivity)this._caller).SetResponce("Villa: " + this._last_error);
			} else {
				if(code == 200) {
					((SendJSONActivity)this._caller).SetResponce("Sending tókst");
				} else if(code == 500) {
					((SendJSONActivity)this._caller).SetResponce("Villa kom upp á vefþjóni");
				} else {
					((SendJSONActivity)this._caller).SetResponce("Ekki náðist samband við vefþjón");
				}
			}
		}
    }
	
	protected void report(int code) {
		if(code == -1) {
			Toast.makeText(this._caller, "Error: " + this._last_error, Toast.LENGTH_LONG).show();
		} else {
			Toast.makeText(this._caller, "HTTP STATUS CODE: " + code, Toast.LENGTH_LONG).show();
		}
		this._caller.finish();
	}
	
	/* 
	 * Check network status
	 * @returns bool
	 * */
	public boolean isOnline() {
	    ConnectivityManager cm =
	        (ConnectivityManager) this._caller.getSystemService(Context.CONNECTIVITY_SERVICE);
	    NetworkInfo netInfo = cm.getActiveNetworkInfo();
	    if (netInfo != null && netInfo.isConnectedOrConnecting()) {
	        return true;
	    }
	    return false;
	}
	
	
	@Override protected void onPreExecute() {
		mProgressDialog = ProgressDialog.show(this._caller, "Sending...", "Sending data...");
   }
	
	public SendJSON(String url, HashMap<String, HashMap<String, String>> payload, Activity caller) {
		this._url 			= url;
		this._caller 		= caller;
		this._payload 		= payload;
	}
	
	protected int send() {
		String json = "";
		StringEntity payload = null;
		try {
			json = new GsonBuilder().create().toJson(this._payload, Map.class);
		} catch (JsonSyntaxException e) {
			this._last_error = "JSON payload syntax error";
			return 500;
		}
		try {
			payload = new StringEntity(json, HTTP.UTF_8);
		} catch (UnsupportedEncodingException e) {
			this._last_error = "EncodingError in payload";
			return 500;
		}
		DefaultHttpClient client = new DefaultHttpClient();
		HttpPost request = new HttpPost(this._url);
		
		/* Tryggjum UTF-8 svo hægt sé að nota íslensku */
		request.setHeader("Content-Type", "application/json; charset=UTF-8");
		request.setHeader("Accept", "application/json");
		request.setEntity(payload);
		if (DEBUG) Log.d(TAG, json);
		try {
			HttpResponse getResponse = client.execute(request);
			this._status_code = getResponse.getStatusLine().getStatusCode();
			Log.d("RESPONCE", "" + this._status_code);
			if (this._status_code != HttpStatus.SC_OK) {
				
				// Vistum síðustu villu sem status-code
				this._last_error = "Error: " + this._status_code;
				return this._status_code;
			}
			return this._status_code;
		} catch (IOException e) {
			request.abort();
			this._last_error = e.getMessage();
			return 500;
		}
	}
}

