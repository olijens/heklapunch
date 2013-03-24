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
import is.heklapunch.SQLHandler;
import is.heklapunch.TimeItemResult;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;

import com.google.gson.Gson;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothServerSocket;
import android.bluetooth.BluetoothSocket;
import android.content.Context;
import android.os.Bundle;
import android.os.Message;
import android.util.Log;
import android.widget.ArrayAdapter;
import android.widget.Toast;

public class BlueToothServer extends BlueToothBase {
	
	private static final String TAG = "BlueToothServer";
	
	// Threads for sockets
	private AcceptThread mSecureAcceptThread;
	private AcceptThread mInsecureAcceptThread;
    
    // Current connection state
	private boolean _listening = false; 
	public ArrayAdapter<String> mPayloadsRecieved;
	
	public BlueToothServer(Activity caller, ArrayAdapter<String> adapter) {
		_caller = caller;
		mPayloadsRecieved = adapter;
	}
	
	@Override
	protected void onMessageRead(Message msg) {
		byte[] readBuf = (byte[]) msg.obj;
		String readMessage = new String(readBuf, 0, msg.arg1);

		if (DEBUG) {
			Log.d(TAG, "READ: " + readMessage);
			// Toast.makeText(_caller.getApplicationContext(),"READ: " +
			// readMessage, Toast.LENGTH_LONG).show();

		}
		// Tengjast gagnasafni
		SQLHandler handler = new SQLHandler(_caller.getApplicationContext());
		
		//Taka við gögnum og umbreyta
		Gson gson = new Gson();
		ArrayList<ArrayList<String>> results = gson.fromJson(readMessage, ArrayList.class);
		//ArrayList<String> test = results.get(0);
		//Toast.makeText(_caller.getApplicationContext(),"Parsed: " + test.get(0).toString(), Toast.LENGTH_LONG).show();
		Iterator<ArrayList<String>> i = results.iterator();
		
		while(i.hasNext()) {
			//Skrifa í töflu 
			ArrayList<String> entry = i.next();
			handler.addResult(entry.get(0), Long.valueOf(entry.get(1)),entry.get(2).toString(), Boolean.valueOf(entry.get(3)), entry.get(4), entry.get(5));
		}
		
		ArrayList<ArrayList<String>> res =  handler.getAllResults();
		
		Toast.makeText(_caller.getApplicationContext(),"Parsed: " + res.toString(), Toast.LENGTH_LONG).show();
		_caller.finish();
	}
	
	/** 
	 * Start the server, listen to connected devices
	 * */
	public void listen() {
		if(!_listening) {
			if(start()) {
				enable();
				ensureDiscoverable();
				
				if (DEBUG) Log.d(TAG, "start listen");

		        // Cancel any thread currently running a connection
		        if (mConnectedThread != null) {mConnectedThread.cancel(); mConnectedThread = null;}

		        setState(STATE_LISTEN);

		        // Start the thread to listen on a BluetoothServerSocket
		        if (mSecureAcceptThread == null) {
		            mSecureAcceptThread = new AcceptThread(true);
		            mSecureAcceptThread.start();
		        }
		        if (mInsecureAcceptThread == null) {
		            mInsecureAcceptThread = new AcceptThread(false);
		            mInsecureAcceptThread.start();
		        }
		        _listening = true;
			}
		}
	}
	
	/**
     * Stop all threads
     */
    public synchronized void stop() {
    	if(_listening) {
	        if (DEBUG) Log.d(TAG, "stop listen");
	
	        if (mConnectedThread != null) {
	            mConnectedThread.cancel();
	            mConnectedThread = null;
	        }
	
	        if (mSecureAcceptThread != null) {
	            mSecureAcceptThread.cancel();
	            mSecureAcceptThread = null;
	        }
	
	        if (mInsecureAcceptThread != null) {
	            mInsecureAcceptThread.cancel();
	            mInsecureAcceptThread = null;
	        }
	        setState(STATE_NONE);
	        _listening = false;
    	}
    }
	
	/**
     * Start the ConnectedThread to begin managing a Bluetooth connection
     * @param socket  The BluetoothSocket on which the connection was made
     * @param device  The BluetoothDevice that has been connected
     */
    public synchronized void connected(BluetoothSocket socket, BluetoothDevice
            device, final String socketType) {
        if (DEBUG) Log.d(TAG, "connected, Socket Type:" + socketType);

        // Cancel any thread currently running a connection
        if (mConnectedThread != null) {mConnectedThread.cancel(); mConnectedThread = null;}

        // Cancel the accept thread because we only want to connect to one device
        if (mSecureAcceptThread != null) {
            mSecureAcceptThread.cancel();
            mSecureAcceptThread = null;
        }
        if (mInsecureAcceptThread != null) {
            mInsecureAcceptThread.cancel();
            mInsecureAcceptThread = null;
        }

        // Start the thread to manage the connection and perform transmissions
        mConnectedThread = new ConnectedThread(socket, socketType);
        mConnectedThread.start();

        // Send the name of the connected device back to the UI Activity
        Message msg = mHandler.obtainMessage(BlueToothMessageHandler.MESSAGE_DEVICE_NAME);
        Bundle bundle = new Bundle();
        bundle.putString(BlueToothMessageHandler.DEVICE_NAME, device.getName());
        msg.setData(bundle);
        mHandler.sendMessage(msg);
        
        if(DEBUG) Log.d(TAG, "Connected to " + device.getName());

        setState(STATE_CONNECTED);
    }
	
	 /**
     * This thread runs while listening for incoming connections. It behaves
     * like a server-side client. It runs until a connection is accepted
     * (or until cancelled).
     */
    private class AcceptThread extends Thread {
        // The local server socket
        private final BluetoothServerSocket mmServerSocket;
        private String mSocketType;

        @SuppressLint("NewApi")
        public AcceptThread(boolean secure) {
            BluetoothServerSocket tmp = null;
            mSocketType = secure ? "Secure" : "Insecure";

            // Create a new listening server socket
            try {
                if (secure) {
                    tmp = mBluetoothAdapter.listenUsingRfcommWithServiceRecord(_name_secure,
                    		_uuid_secure);
                } else {
                    tmp = mBluetoothAdapter.listenUsingInsecureRfcommWithServiceRecord(
                    		_name_insecure, _uuid_insecure);
                }
            } catch (IOException e) {
            	_last_error = "Socket Type: " + mSocketType + "listen() failed";
            }
            mmServerSocket = tmp;
        }

        /* */
        public void run() {
            if (DEBUG) Log.d(TAG, "Socket Type: " + mSocketType +
                    "BEGIN mAcceptThread" + this);
            setName("AcceptThread" + mSocketType);

            BluetoothSocket socket = null;

            // Listen to the server socket if we're not connected
            while (mState != STATE_CONNECTED) {
                try {
                    // This is a blocking call and will only return on a
                    // successful connection or an exception
                    socket = mmServerSocket.accept();
                } catch (IOException e) {
                    Log.e(TAG, "Socket Type: " + mSocketType + "accept() failed", e);
                    break;
                }

                // If a connection was accepted
                if (socket != null) {
                    synchronized (this) {
                        switch (mState) {
                        case STATE_LISTEN:
                        case STATE_CONNECTING:
                            // Situation normal. Start the connected thread.
                            connected(socket, socket.getRemoteDevice(),
                                    mSocketType);
                            break;
                        case STATE_NONE:
                        case STATE_CONNECTED:
                            // Either not ready or already connected. Terminate new socket.
                            try {
                                socket.close();
                            } catch (IOException e) {
                                Log.e(TAG, "Could not close unwanted socket", e);
                            }
                            break;
                        }
                    }
                }
            }
            if (DEBUG) Log.i(TAG, "END mAcceptThread, socket Type: " + mSocketType);

        }
        
        /* */
        public void cancel() {
            if (DEBUG) Log.d(TAG, "Socket Type" + mSocketType + "cancel " + this);
            try {
                mmServerSocket.close();
            } catch (IOException e) {
                Log.e(TAG, "Socket Type" + mSocketType + "close() of server failed", e);
            }
        }
    }
}
