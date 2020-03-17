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



    //State
    public static final int STATE_NONE = 1;
    public static final int STATE_LISTEN = 2;
    public static final int STATE_CONNECTING = 3;
    public static final int STATE_CONNECTED = 4;
    public static final int STATE_FAIL = 7;

	// Debugging
	private static final String TAG = "BluetoothService";
	
	// Intent request code
	private static final int REQUEST_CONNECT_DEVICE = 1;
	private static final int REQUEST_ENABLE_BT = 2;
	
	public static BluetoothAdapter btAdapter;
	
	private Activity mActivity;
	private Handler mHandler;

	private static final UUID MY_UUID = UUID.fromString("6E400002-B5A3-F393-E0A9-E50E24DCCA32");

	private int mState;

	private ConnectThread mConnectThread;
	private ConnectedThread mConnectedThread;


	// Constructors
	public BluetoothService(Activity ac, Handler h) {
		mActivity = ac;
		mHandler = h;
		

		btAdapter = BluetoothAdapter.getDefaultAdapter();
	}
	



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
		//MAC address를 가져온다
	    String address = data.getExtras().getString(DeviceListActivity.EXTRA_DEVICE_ADDRESS);
        //BluetoothDevice object를 가져온다
	    BluetoothDevice device = btAdapter.getRemoteDevice(address);

        Log.d(TAG,"Get Device Info \n"+"address:"+address);
        connect(device);
    }




	//소켓과 쓰레드를 생성하여 기기사이의 connection을 가능하게 함
    private class ConnectThread extends Thread{
        private final BluetoothSocket mmSocket;
        private final BluetoothDevice mmDevice;

        public ConnectThread(BluetoothDevice device){
            mmDevice = device;
            BluetoothSocket tmp = null;

            //디바이스 정보를 얻어서 BluetoothSocket 생성
            try{
                tmp = device.createRfcommSocketToServiceRecord(MY_UUID);
				Log.i(TAG,"create socket");
			}catch (IOException e){
                Log.e(TAG,"create() failed",e);
            }
            mmSocket = tmp;
        }
        public void run(){
            Log.i(TAG,"BEGIN mConnectThread");
            setName("ConnectThread");

            //연결 시도하기 전에는 기기검색 중지
            btAdapter.cancelDiscovery();

            //블루투스 소켓 연결 시도
            try{
            	//연결시도에 대한 return 값은 success 또는 exception
                mmSocket.connect();
                Log.d(TAG,"Connect Success");
            }catch (IOException e){
                connectionFailed();
                Log.d(TAG,"connect Fail");

                //소켓을 닫는다
                try{
                    mmSocket.close();
                }catch (IOException e2){
                    Log.e(TAG,"unable to close() socket during connection failure",e2);
                }
                //연결 중 혹은 연결 대기상태인 메소드 호출
                BluetoothService.this.start();
                return;
            }
            //ConnectThread 클래스를 reset
            synchronized (BluetoothService.this){
                mConnectThread = null;
            }
            //ConnectThread 를 시작
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
	            	//InputStream 으로부터 값을 받는 읽는 부분(값을 받음)
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
	        	//값을 쓰는 부분(값을 보냄)
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



    //setState() : 블루투스 상태를 set한다.
    private synchronized void setState(int state){
	    Log.d(TAG,"setState()"+mState+"->"+state);
	    mState = state;

	    //핸들러를 통해 상태를 메인에 넘겨준다
	    mHandler.obtainMessage(MainActivity.MESSAGE_STATE_CHANGE,state,-1).sendToTarget();
    }


    //getState() : 블루투스 상태를 get한다.
    public synchronized int getState() {
        return mState;
    }


	//Thread관련 service를 시작하는 start메소드
    public synchronized void start(){
	    Log.d(TAG,"start");

	    if(mConnectThread == null){

        }else{
	        mConnectThread.cancel();
	        mConnectThread = null;
        }
    }


	//ConnectThread 초기화와 시작 device의 모든 연결 제거
    public synchronized void connect(BluetoothDevice device) {
		Log.d(TAG, "connect to:" + device);

		if (mState == STATE_CONNECTING) {

			if (mConnectThread != null) {

				mConnectThread.cancel();
				mConnectThread = null;
			}
		}

		if(mConnectedThread != null) {


			mConnectedThread.cancel();
			mConnectedThread = null;
		}

		mConnectThread = new ConnectThread(device);
		mConnectThread.start();

		setState(STATE_CONNECTING);
		Log.d(TAG,"mState" + mState);
	}


	//ConnectedThread 초기화
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

	//모든 Thread stop
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


    //쓰레드를 통해 값을 내보내는 부분
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

