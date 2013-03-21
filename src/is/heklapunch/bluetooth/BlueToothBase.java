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
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.UUID;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.Intent;
import android.os.Bundle;
import android.os.Message;
import android.util.Log;
import android.widget.Toast;

public abstract class BlueToothBase {

	protected boolean DEBUG = true;
	private static final String TAG = "BlueToothBase";

	// How long discoverable
	private static final int DEFAULT_DISCOVERABLE_SECONDS = 300;
	private int _discSec = 0;

	// BT adapter instance
	protected BluetoothAdapter mBluetoothAdapter;
	protected String _last_error = "";

	protected int mState;

	// Unique UUID for this application
	protected UUID _uuid_secure = UUID
			.fromString("1658f56d-2dd4-4faf-bd34-2f8e5716fe80");
	protected UUID _uuid_insecure = UUID
			.fromString("40dd0626-305a-4c9b-bc76-378c14b173e3");

	// Name for the SDP record when creating server socket
	protected String _name_secure = "BlueToothSecure";
	protected String _name_insecure = "BlueToothInSecure";

	// Intent request codes
	public static final int REQUEST_CONNECT_DEVICE_SECURE = 1;
	public static final int REQUEST_CONNECT_DEVICE_INSECURE = 2;
	public static final int REQUEST_ENABLE_BT = 3;
	public static final int REQUEST_MAKE_DISCOVERABLE = 4;

	// Constants that indicate the current connection state
	public static final int STATE_NONE = 0; // we're doing nothing
	public static final int STATE_LISTEN = 1; // now listening for incoming
												// connections
	public static final int STATE_CONNECTING = 2; // now initiating an outgoing
													// connection
	public static final int STATE_CONNECTED = 3; // now connected to a remote
													// device

	//
	protected Activity _caller;
	public BlueToothMessageHandler mHandler;
	protected String mConnectedDeviceName;
	public ConnectedThread mConnectedThread;
	public ConnectThread mConnectThread;
	protected boolean HTCWorkaround = true;

	/* */
	protected void onConnected() {
	}

	/* */
	protected void onConnecting() {
	}

	/* */
	protected void onNotConnected() {
	}

	/* */
	protected void onMessageWrite(Message msg) {
	}

	/* */
	protected void onMessageRead(Message msg) {
	}

	/* save the connected device's name */
	protected void onMessageDeviceName(Message msg) {
		mConnectedDeviceName = msg.getData().getString(
				BlueToothMessageHandler.DEVICE_NAME);
		Toast.makeText(_caller.getApplicationContext(),
				"Connected to " + mConnectedDeviceName, Toast.LENGTH_SHORT)
				.show();
		Log.d(TAG, "MESSAGE_DEVICE_NAME: " + "Connected to "
				+ mConnectedDeviceName);
	}

	/* */
	protected void onMessageToast(Message msg) {
		if (_caller != null) {
			Toast.makeText(_caller.getApplicationContext(),
					msg.getData().getString(BlueToothMessageHandler.TOAST),
					Toast.LENGTH_SHORT).show();
		}
		Log.d(TAG,
				"MESSAGE_TOAST: "
						+ msg.getData()
								.getString(BlueToothMessageHandler.TOAST));
	}

	/* */
	public void setDiscoverableTimeout(int Secs) {
		_discSec = Secs > 0 ? Secs : 0;
	}

	/* */
	public boolean start() {
		if (_caller == null) {
			_last_error = "Missing caller Activity";
			return false;
		}
		mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
		if (mBluetoothAdapter == null) {
			return false;
		}

		mHandler = new BlueToothMessageHandler(this);

		if (DEBUG)
			Log.d(TAG, "+ START +");

		// OK
		return true;
	}

	/* */
	public boolean isDiscoverable() {
		return (mBluetoothAdapter.getScanMode() == BluetoothAdapter.SCAN_MODE_CONNECTABLE_DISCOVERABLE);
	}

	/* */
	public void ensureDiscoverable() {
		if (mBluetoothAdapter.getScanMode() != BluetoothAdapter.SCAN_MODE_CONNECTABLE_DISCOVERABLE) {
			int discSec = _discSec <= 0 ? DEFAULT_DISCOVERABLE_SECONDS
					: _discSec;

			Intent discoverableIntent = new Intent(
					BluetoothAdapter.ACTION_REQUEST_DISCOVERABLE);
			discoverableIntent.putExtra(
					BluetoothAdapter.EXTRA_DISCOVERABLE_DURATION, discSec);
			_caller.startActivityForResult(discoverableIntent,
					REQUEST_MAKE_DISCOVERABLE);
		}

		if (DEBUG)
			Log.d(TAG, "+ ensureDiscoverable +");
	}

	/* */
	public void enable() {
		// Enable bluetooth
		if (!mBluetoothAdapter.isEnabled()) {
			Intent enableBtIntent = new Intent(
					BluetoothAdapter.ACTION_REQUEST_ENABLE);
			_caller.startActivityForResult(enableBtIntent, REQUEST_ENABLE_BT);
		}

		if (DEBUG)
			Log.d(TAG, "+ ENABLE +");
	}

	/**/
	public String getLastError() {
		return _last_error;
	}

	/**
	 * Set the current state of the chat connection
	 * 
	 * @param state
	 *            An integer defining the current connection state
	 */
	protected synchronized void setState(int state) {
		mState = state;

		// Give the new state to the Handler so the UI Activity can update
		mHandler.obtainMessage(BlueToothMessageHandler.MESSAGE_STATE_CHANGE,
				state, -1).sendToTarget();

		if (DEBUG)
			Log.d(TAG, "setState() " + mState + " -> " + state);
	}

	/**
	 * Indicate that the connection was lost and notify the UI Activity.
	 */
	protected void connectionLost() {
		// Send a failure message back to the Activity
		Message msg = mHandler
				.obtainMessage(BlueToothMessageHandler.MESSAGE_TOAST);
		Bundle bundle = new Bundle();
		bundle.putString(BlueToothMessageHandler.TOAST,
				"Device connection was lost");
		msg.setData(bundle);
		mHandler.sendMessage(msg);
	}

	/**
	 * Indicate that the connection attempt failed and notify the UI Activity.
	 */
	protected void connectionFailed() {
		// Send a failure message back to the Activity
		Message msg = mHandler
				.obtainMessage(BlueToothMessageHandler.MESSAGE_TOAST);
		Bundle bundle = new Bundle();
		bundle.putString(BlueToothMessageHandler.TOAST,
				"Unable to connect device");
		msg.setData(bundle);
		mHandler.sendMessage(msg);
	}

	/**
	 * Start the ConnectedThread to begin managing a Bluetooth connection
	 * 
	 * @param socket
	 *            The BluetoothSocket on which the connection was made
	 * @param device
	 *            The BluetoothDevice that has been connected
	 */
	public synchronized void connected(BluetoothSocket socket,
			BluetoothDevice device, final String socketType) {
		if (DEBUG)
			Log.d(TAG, "connected, Socket Type:" + socketType);

		// Cancel the thread that completed the connection
		if (mConnectThread != null) {
			mConnectThread.cancel();
			mConnectThread = null;
		}

		// Cancel any thread currently running a connection
		if (mConnectedThread != null) {
			mConnectedThread.cancel();
			mConnectedThread = null;
		}

		// Start the thread to manage the connection and perform transmissions
		mConnectedThread = new ConnectedThread(socket, socketType);
		mConnectedThread.start();

		// Send the name of the connected device back to the UI Activity
		Message msg = mHandler
				.obtainMessage(BlueToothMessageHandler.MESSAGE_DEVICE_NAME);
		Bundle bundle = new Bundle();
		bundle.putString(BlueToothMessageHandler.DEVICE_NAME, device.getName());
		msg.setData(bundle);
		mHandler.sendMessage(msg);

		if (DEBUG)
			Log.d(TAG, "Connected to " + device.getName());

		setState(STATE_CONNECTED);
	}

	/**
	 * This thread runs during a connection with a remote device. It handles all
	 * incoming and outgoing transmissions.
	 */
	class ConnectedThread extends Thread {
		private final BluetoothSocket mmSocket;
		private final InputStream mmInStream;
		private final OutputStream mmOutStream;

		public ConnectedThread(BluetoothSocket socket, String socketType) {
			Log.d(TAG, "create ConnectedThread: " + socketType);
			mmSocket = socket;
			InputStream tmpIn = null;
			OutputStream tmpOut = null;

			// Get the BluetoothSocket input and output streams
			try {
				tmpIn = socket.getInputStream();
				tmpOut = socket.getOutputStream();
			} catch (IOException e) {
				Log.e(TAG, "temp sockets not created", e);
			}

			mmInStream = tmpIn;
			mmOutStream = tmpOut;
		}

		public void run() {
			Log.d(TAG, "BEGIN mConnectedThread");
			byte[] buffer = new byte[1024];
			int bytes;

			// Keep listening to the InputStream while connected
			while (true) {
				try {
					// Read from the InputStream
					bytes = mmInStream.read(buffer);

					Log.e(TAG, "ConnectedThread: Read: " + buffer.toString());

					// Send the obtained bytes to the UI Activity
					mHandler.obtainMessage(
							BlueToothMessageHandler.MESSAGE_READ, bytes, -1,
							buffer).sendToTarget();
				} catch (IOException e) {
					Log.e(TAG, "disconnected", e);
					// connectionLost();
					break;
				}
			}
		}

		/**
		 * Write to the connected OutStream.
		 * 
		 * @param buffer
		 *            The bytes to write
		 */
		public void write(byte[] buffer) {
			try {
				mmOutStream.write(buffer);
				Log.e(TAG, "ConnectedThread: write:" + buffer.toString());

				// Share the sent message back to the UI Activity
				mHandler.obtainMessage(BlueToothMessageHandler.MESSAGE_WRITE,
						-1, -1, buffer).sendToTarget();
			} catch (IOException e) {
				Log.e(TAG, "Exception during write", e);
			}
		}

		public void cancel() {
			try {
				mmSocket.close();
			} catch (IOException e) {
				Log.e(TAG, "close() of connect socket failed", e);
			}
		}
	}

	/**
	 * This thread runs while attempting to make an outgoing connection with a
	 * device. It runs straight through; the connection either succeeds or
	 * fails.
	 */
	protected class ConnectThread extends Thread {
		private BluetoothSocket mmSocket;
		private final BluetoothDevice mmDevice;
		private String mSocketType;

		@SuppressLint("NewApi")
		public ConnectThread(BluetoothDevice device, boolean secure) {
			mmDevice = device;
			BluetoothSocket btsock = null;
			mSocketType = secure ? "Secure" : "Insecure";

			// Get a BluetoothSocket for a connection with the given BluetoothDevice
            if (HTCWorkaround) {
            		Log.d(TAG, "Using HTCWorkaround");
				try {
					btsock = BlueToothInsecure.createRfcommSocketToServiceRecord(device, _uuid_insecure, true);
				} catch (Exception e) {
					Log.e(TAG, "Socket Type: " + mSocketType + " create() failed",
							e);
				}
			} else {
				try {
					btsock = device.createInsecureRfcommSocketToServiceRecord(_uuid_insecure);
				} catch (Exception e) {
					Log.e(TAG, "Socket Type: " + mSocketType + " create() failed",
							e);
				}
			}
            
            mmSocket = btsock;
		}

		public void run() {
			Log.i(TAG, "BEGIN mConnectThread SocketType:" + mSocketType);
//			setName("ConnectThread" + mSocketType);

			// Always cancel discovery because it will slow down a connection
            mBluetoothAdapter.cancelDiscovery();

            // Make a connection to the BluetoothSocket
            try {
                // This is a blocking call and will only return on a successful connection or an exception
                mmSocket.connect();
            } catch (IOException e) {
                // Close the socket
                try {
                    mmSocket.close();
                } catch (IOException e2) {
                	Log.e(TAG, "unable to close() socket during connection failure", e2);
                }
                Log.e(TAG, "socket failure " + mSocketType, e);
                return;
            }

			// Reset the ConnectThread because we're done
			synchronized (this) {
				mConnectThread = null;
			}

			// Start the connected thread
			connected(mmSocket, mmDevice, mSocketType);
		}

		public void cancel() {
			try {
				mmSocket.close();
			} catch (IOException e) {
				Log.e(TAG, "close() of connect " + mSocketType
						+ " socket failed", e);
			}
		}
	}
}
