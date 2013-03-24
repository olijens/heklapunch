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
	
	/**
	 * @param	Activity caller 
	 * */
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
	
	/** 
	 * Return a list of paired devices
	 * @return Set
	 * */
	public Set<BluetoothDevice> getPaired() {
		if(mBluetoothAdapter != null)
			return mBluetoothAdapter.getBondedDevices();
		
		return null;
	}
	
	/**
	 * Set the sent payload
	 * @param String
	 * */
	public void send(String payload) {
		_payload = payload;
	}
	
	/** 
	 * Connect to device
	 * @param  Intent
	 * @param  boolean	Use secure connection
	 * */
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
