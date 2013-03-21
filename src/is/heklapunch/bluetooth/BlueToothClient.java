package is.heklapunch.bluetooth;
/*
 * 
 * 
 * */
import java.util.Set;

import android.app.Activity;
import android.bluetooth.BluetoothDevice;
import android.content.Intent;
import android.os.Message;
import android.util.Log;
import android.widget.Toast;

public class BlueToothClient extends BlueToothBase {
	
	private static final String TAG = "BlueToothClient";
	public static String EXTRA_DEVICE_ADDRESS = "device_address";
	
	private String _payload;
	private boolean _started = false;
	
	/* */
	public BlueToothClient(Activity caller) {
		_caller = caller;
	}
	
	@Override
	public boolean start() {
		if(super.start()) {
			enable();
			_started = true;
		} else {
			if (DEBUG) Log.d(TAG, "Unable to start bluetooth");
		}
		return _started;
	}
	
	/* */
	public Set<BluetoothDevice> getPaired() {
		if(mBluetoothAdapter != null)
			return mBluetoothAdapter.getBondedDevices();
		
		return null;
	}
	
	/* */
	public void send(String payload) {
		_payload = payload;
	}
	
	/* */
	public void connectToDevice(Intent data, boolean secure) {
		if(_started) {
	    	if(DEBUG) Log.d(TAG, "connectDevice");
	    	
	        // Get the device MAC address
	        String address = data.getExtras()
	            .getString(EXTRA_DEVICE_ADDRESS);
	        
	        // Get the BLuetoothDevice object
	        BluetoothDevice device = mBluetoothAdapter.getRemoteDevice(address);
	        
	        // Attempt to connect to the device
	        connect(device, secure);
		}
    }
	
	@Override
    protected void onConnected() {
		if(_payload != null && mConnectedThread != null) {
			if (DEBUG) Log.d(TAG, "WRITE buffer ... ");
			mHandler.obtainMessage(BlueToothMessageHandler.MESSAGE_WRITE, -1, -1, _payload.getBytes())
             	.sendToTarget();
		}
    }
	
	@Override
    protected void onMessageWrite(Message msg) {
		if(_payload != null && mConnectedThread != null) {
			_payload = null;
			byte[] writeBuf = (byte[]) msg.obj;
			mConnectedThread.write(writeBuf);
			
			if(DEBUG) {
				String writeMessage = new String(writeBuf);
				Log.d(TAG, "WRITE: " + writeMessage);
				Toast.makeText(_caller.getApplicationContext(),
						"WRITE: " + writeMessage, Toast.LENGTH_LONG).show();
			}
			
			_caller.finish();
		}
    }
	
	/**
     * Start the ConnectThread to initiate a connection to a remote device.
     * @param device  The BluetoothDevice to connect
     * @param secure Socket Security type - Secure (true) , Insecure (false)
     */
    public synchronized void connect(BluetoothDevice device, boolean secure) {
        if (DEBUG) Log.d(TAG, "connect to: " + device);

        // Cancel any thread attempting to make a connection
        if (mState == STATE_CONNECTING) {
            if (mConnectThread != null) {mConnectThread.cancel(); mConnectThread = null;}
        }

        // Cancel any thread currently running a connection
        if (mConnectedThread != null) {mConnectedThread.cancel(); mConnectedThread = null;}

        // Start the thread to connect with the given device
        mConnectThread = new ConnectThread(device, secure);
        mConnectThread.start();
        setState(STATE_CONNECTING);
    }

    /**
     * Stop all threads
     */
    public synchronized void stop() {
        if (DEBUG) Log.d(TAG, "stop");

        if (mConnectThread != null) {
            mConnectThread.cancel();
            mConnectThread = null;
        }

        if (mConnectedThread != null) {
            mConnectedThread.cancel();
            mConnectedThread = null;
        }

        setState(STATE_NONE);
    }
	
}
