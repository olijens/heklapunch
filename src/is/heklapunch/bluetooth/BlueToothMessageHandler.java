package is.heklapunch.bluetooth;

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
