package is.heklapunch.bluetooth;
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
import android.os.Handler;
import android.os.Message;
import android.util.Log;

public class BlueToothMessageHandler extends Handler {
	
	private static final boolean DEBUG = true;
	private static final String TAG = "BlueToothMessageHandler";
	
	public static final String DEVICE_NAME = "device_name";
    public static final String TOAST = "toast";
	
	// Message types sent from the Handler
	public static final int MESSAGE_STATE_CHANGE = 1;
	public static final int MESSAGE_READ = 2;
	public static final int MESSAGE_WRITE = 3;
	public static final int MESSAGE_DEVICE_NAME = 4;
	public static final int MESSAGE_TOAST = 5;
	public static final int MESSAGE_ERROR = 6;
	
	 // Constants that indicate the current connection state
    public static final int STATE_NONE = 0;       // we're doing nothing
    public static final int STATE_LISTEN = 1;     // now listening for incoming connections
    public static final int STATE_CONNECTING = 2; // now initiating an outgoing connection
    public static final int STATE_CONNECTED = 3;  // now connected to a remote device
    
    private BlueToothBase _adapter; 
    
    public BlueToothMessageHandler(BlueToothBase adapter) {
    	_adapter = adapter;
    }
    
    @Override
	public void handleMessage(Message msg) {
		switch (msg.what) {
		case MESSAGE_STATE_CHANGE:
			switch (msg.arg1) {
			case STATE_CONNECTED:
				if (DEBUG)
					Log.d(TAG, "MESSAGE_STATE_CHANGE: STATE_CONNECTED");
				_adapter.onConnected();
				break;
			case STATE_CONNECTING:
				if (DEBUG)
					Log.d(TAG, "MESSAGE_STATE_CHANGE: STATE_CONNECTING");
				_adapter.onConnecting();
				break;
			case STATE_LISTEN:
			case STATE_NONE:
				if (DEBUG)
					Log.d(TAG, "MESSAGE_STATE_CHANGE: not_connected");
				_adapter.onNotConnected();
				break;
			}
			break;
		case MESSAGE_WRITE:
			_adapter.onMessageWrite(msg);
			break;
		case MESSAGE_ERROR:
			_adapter.onMessageError(msg);
			break;
		case MESSAGE_READ:
			_adapter.onMessageRead(msg);
			break;
		case MESSAGE_DEVICE_NAME:
			_adapter.onMessageDeviceName(msg);
			break;
		case MESSAGE_TOAST:
			_adapter.onMessageToast(msg);
			break;
		}
	}
}
