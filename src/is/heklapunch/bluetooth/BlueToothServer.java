package is.heklapunch.bluetooth;
/*
 * 
 * 
 * */
import java.io.IOException;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothServerSocket;
import android.bluetooth.BluetoothSocket;
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
		
		if(DEBUG) {
			Log.d(TAG, "READ: " + readMessage);
			Toast.makeText(_caller.getApplicationContext(),
					"READ: " + readMessage, Toast.LENGTH_LONG).show();
		}
		
		if(mPayloadsRecieved != null) {
			mPayloadsRecieved.add(readMessage);
		}
		
		_caller.finish();
    }
	
	/* */
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
