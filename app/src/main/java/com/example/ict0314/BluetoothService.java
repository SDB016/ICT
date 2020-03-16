package com.example.ict0314;

import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.Intent;
import android.os.Handler;
import android.print.PrinterId;
import android.text.LoginFilter;
import android.util.Log;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.UUID;

public class BluetoothService {

    private int mState;

    //State
    public static final int STATE_NONE = 0;
    public static final int STATE_LISTEN = 1;
    public static final int STATE_CONNECTING = 2;
    public static final int STATE_CONNECTED = 3;

	// Debugging
	private static final String TAG = "BluetoothService";
	
	// Intent request code
	private static final int REQUEST_CONNECT_DEVICE = 1;
	private static final int REQUEST_ENABLE_BT = 2;
	
	public static BluetoothAdapter btAdapter;
	
	private Activity mActivity;
	private Handler mHandler;

	private static final UUID MY_UUID = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB");

	private ConnectThread mConnectThread;
	private ConnectedThread mConnectedThread;


	// Constructors
	public BluetoothService(Activity ac, Handler h) {
		mActivity = ac;
		mHandler = h;
		

		btAdapter = BluetoothAdapter.getDefaultAdapter();
	}
	
	/**
	 * Check the Bluetooth support
	 * @return boolean
	 */
	public boolean getDeviceState() {
		Log.i(TAG, "Check the Bluetooth support");
		
		if(btAdapter == null) {
			Log.d(TAG, "Bluetooth is not available");
			
			return false;
			
		} else {
			Log.d(TAG, "Bluetooth is available");
			
			return true;
		}
	}
	
	/**
	 * Check the enabled Bluetooth
	 */
	public void enableBluetooth() {
		Log.i(TAG, "Check the enabled Bluetooth");
		
		
		if(btAdapter.isEnabled()) {		

			Log.d(TAG, "Bluetooth Enable Now");
			scanDevice();
			// Next Step
		} else {		

			Log.d(TAG, "Bluetooth Enable Request");
			
			Intent i = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
			mActivity.startActivityForResult(i, REQUEST_ENABLE_BT);
		}
	}

	public void scanDevice(){
	    Log.d(TAG, "Scan Device");

	    Intent serverIntent = new Intent(mActivity, DeviceListActivity.class);
	    mActivity.startActivityForResult(serverIntent,REQUEST_CONNECT_DEVICE);
    }

    public void getDeviceInfo(Intent data){
	    String address = data.getExtras().getString(DeviceListActivity.EXTRA_DEVICE_ADDRESS);
        BluetoothDevice device = btAdapter.getRemoteDevice(address);

        Log.d(TAG,"Get Device Info \n"+"address:"+address);
        connect(device);
    }

    private class ConnectThread extends Thread{
        private final BluetoothSocket mmSocket;
        private final BluetoothDevice mmDevice;

        public ConnectThread(BluetoothDevice device){
            mmDevice = device;
            BluetoothSocket tmp = null;

            try{
                tmp = device.createRfcommSocketToServiceRecord(MY_UUID);
            }catch (IOException e){
                Log.e(TAG,"create() failed",e);
            }
            mmSocket = tmp;
        }
        public void run(){
            Log.i(TAG,"BEGIN mConnectThread");
            setName("ConnectThread");

            btAdapter.cancelDiscovery();

            try{
                mmSocket.connect();
                Log.d(TAG,"Connect Success");
            }catch (IOException e){
                connectionFailed();
                Log.d(TAG,"connect Fail");

                try{
                    mmSocket.close();
                }catch (IOException e2){
                    Log.e(TAG,"unable to close() socket during connection failure",e2);
                }
                BluetoothService.this.start();
                return;
            }
            synchronized (BluetoothService.this){
                mConnectedThread = null;
            }
            connected(mmSocket, mmDevice);
        }
        public void cancel(){
            try {
                mmSocket.close();
            }catch (IOException e){
                Log.e(TAG,"close() of connect socket failed",e);
            }
        }
    }

    private class ConnectedThread extends Thread{
	    private final BluetoothSocket mmSocket;
	    private final InputStream mmInStream;
	    private final OutputStream mmOutputStream;

	    public ConnectedThread(BluetoothSocket socket){
	        Log.d(TAG,"create connectedThread");
	        mmSocket = socket;
	        InputStream tmpIn = null;
	        OutputStream tmpOut = null;

	        try {
	            tmpIn = socket.getInputStream();
	            tmpOut = socket.getOutputStream();
            }catch (IOException e){
	            Log.e(TAG, "temp sockets not created",e);
            }
	        mmInStream = tmpIn;
	        mmOutputStream = tmpOut;
        }

        public void run(){
	        Log.i(TAG,"BEGIN mConnectedThread");
	        byte[] buffer = new byte[1024];
	        int bytes;

	        while(true){
	            try {
	                bytes = mmInStream.read(buffer);
                }catch (IOException e){
	                Log.e(TAG,"disconnected",e);
	                connectionLost();
	                break;
                }
            }
	    }

	    public void write(byte[] buffer){
	        try {
	            mmOutputStream.write(buffer);
            }catch (IOException e){
	            Log.e(TAG,"close() of connect socket failed",e);
            }
        }
        public void cancel(){
	        try {
	            mmSocket.close();
            }catch (IOException e){
	            Log.e(TAG,"close() of connect socket failed",e);
            }
        }
    }

    private synchronized void setState(int state){
	    Log.d(TAG,"setState()"+mState+"->"+state);
	    mState = state;
    }

    public synchronized int getState() {
        return mState;
    }

    public synchronized void start(){
	    Log.d(TAG,"start");

	    if(mConnectThread == null){

        }else{
	        mConnectThread.cancel();
	        mConnectThread = null;
        }

	    if (mConnectedThread == null){

        }else {
	        mConnectedThread.cancel();
	        mConnectedThread = null;
        }
    }

    public synchronized void connect(BluetoothDevice device){
	    Log.d(TAG,"connect to:"+device);

	    if(mState == STATE_CONNECTING){

        }else{
	        mConnectThread.cancel();
	        mConnectThread = null;
        }

	    if (mConnectedThread == null){

        }else{
	        mConnectedThread.cancel();
	        mConnectedThread = null;
        }

	    mConnectThread.start();
	    setState(STATE_CONNECTING);
    }

    public synchronized void connected(BluetoothSocket socket,BluetoothDevice device){
	    Log.d(TAG,"connected");

	    if(mConnectThread == null){

        }else {
	        mConnectThread.cancel();
	        mConnectThread = null;
        }
        if (mConnectedThread == null){

        }else{
            mConnectedThread.cancel();
            mConnectedThread = null;
        }

        mConnectedThread = new ConnectedThread(socket);
        mConnectedThread.start();

        setState(STATE_CONNECTED);
    }

    public synchronized void stop(){
	    Log.d(TAG,"stop");

	    if(mConnectThread != null){
	        mConnectThread.cancel();
	        mConnectThread = null;
        }
	    if (mConnectedThread != null){
	        mConnectedThread.cancel();
	        mConnectedThread = null;
        }
	    setState(STATE_NONE);
    }
    public void write(byte[] out){
	    ConnectedThread r;
	    synchronized (this){
	        if(mState != STATE_CONNECTED)
	            return;
	        r = mConnectedThread;
        }
    }

    private void connectionFailed(){
	    setState(STATE_LISTEN);
    }

    private void connectionLost(){
	    setState(STATE_LISTEN);
    }


}

