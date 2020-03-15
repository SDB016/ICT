package com.example.ict0314;

import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.Intent;
import android.os.Handler;
import android.util.Log;

public class BluetoothService {
	// Debugging
	private static final String TAG = "BluetoothService";
	
	// Intent request code
	private static final int REQUEST_CONNECT_DEVICE = 1;
	private static final int REQUEST_ENABLE_BT = 2;
	
	public static BluetoothAdapter btAdapter;
	
	private Activity mActivity;
	private Handler mHandler;
	
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
        //connect(device);
    }
	
}
