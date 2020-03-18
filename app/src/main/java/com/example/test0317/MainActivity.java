package com.example.test0317;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import android.Manifest;
import android.app.Activity;
import android.app.AlertDialog;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.os.SystemClock;
import android.view.View;
import android.widget.Button;
import android.widget.SimpleAdapter;
import android.widget.TextView;
import android.widget.Toast;

import org.w3c.dom.Text;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.math.MathContext;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

public class MainActivity extends AppCompatActivity {

    private BluetoothAdapter bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
    private BluetoothSocket mBluetoothSocket;
    private BluetoothDevice mBluetoothDevice;

    Set<BluetoothDevice> pairedDevices = bluetoothAdapter.getBondedDevices();
    List<Map<String,String>> dataDevice;
    AlertDialog.Builder alertBuilder;
    SimpleAdapter adapterDevice;
    List<BluetoothDevice> bluetoothDevices;

    Button btnConnect;
    TextView textName;
    TextView textAddress;

    Handler mBluetoothHandler;
    ConnectedBluetoothThread mThreadConnectedBluetooth;

    private SharedPreferences savedData;
    private SharedPreferences.Editor editor = savedData.edit();


    private static final int REQUEST_ENABLE_BT = 1;

    final static int BT_MESSAGE_READ = 8;

    final static UUID BT_UUID = UUID.fromString("00001101-0000-1000-8000-00805f9b34fb");
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        ActivityCompat.requestPermissions(this,
                new String[]{Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION},1);

        if(bluetoothAdapter == null){
            Toast.makeText(getApplicationContext(),"블루투스를 지원하지 않는 기기입니다",Toast.LENGTH_SHORT).show();
            onDestroy();
        }

        //Button
        btnConnect = findViewById(R.id.btnConnect);
        btnConnect.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                BluetoothOn();
            }
        });

        //TextView
        textName = findViewById(R.id.textName);
        textAddress = findViewById(R.id.textAddress);

        IntentFilter filter = new IntentFilter(BluetoothDevice.ACTION_FOUND);
        registerReceiver(receiver,filter);

        savedData = getSharedPreferences("MiniDB",MODE_PRIVATE);

        //Show searched devices
        dataDevice = new ArrayList<>();
        adapterDevice = new SimpleAdapter(this, dataDevice,android.R.layout.simple_expandable_list_item_2, new String[]{"name","address"}, new int[]{android.R.id.text1,android.R.id.text2});
        alertBuilder = new AlertDialog.Builder(this);
        alertBuilder.setTitle("검색된 디바이스");
        alertBuilder.setAdapter(adapterDevice, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int id) {
                String strName = adapterDevice.getItem(id).toString();
                String resultAdd = strName.substring(strName.indexOf("address:")+8, strName.indexOf("name:")-2);
                ConnectSelectDevice(resultAdd);
            }
        });
        alertBuilder.setCancelable(false);

    }

    private void BluetoothOn(){
        if (!bluetoothAdapter.isEnabled()){
            Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivityForResult(enableBtIntent, REQUEST_ENABLE_BT);

        }
    }

    //Get paired devices
    private void GetPairedDevice(){
        if(pairedDevices.size() > 0){
            for(BluetoothDevice device : pairedDevices){
                String deviceName = device.getName();
                String deviceHardwareAddress = device.getAddress();
            }
        }
    }

    //search devices
    private final BroadcastReceiver receiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            switch (action){
                case BluetoothAdapter.ACTION_DISCOVERY_STARTED:
                    dataDevice.clear();
                    Toast.makeText(MainActivity.this, "블루투스 검색 시작", Toast.LENGTH_SHORT).show();
                    break;

                case  BluetoothDevice.ACTION_FOUND:
                    BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
                    Map map = new HashMap();
                    map.put("name", device.getName());
                    map.put("address",device.getAddress());
                    dataDevice.add(map);
                    adapterDevice.notifyDataSetChanged();
                    bluetoothDevices.add(device);
                    if(device.getAddress().equals(savedData.getString("address","nope")) && pairedDevices.size()>0){
                        bluetoothAdapter.cancelDiscovery();
                    }
                    break;

                case BluetoothAdapter.ACTION_DISCOVERY_FINISHED:

                    break;
            }
        }

    };

    void ConnectSelectDevice(String address){
        mBluetoothDevice = bluetoothAdapter.getRemoteDevice(address);
        try{
            mBluetoothSocket = mBluetoothDevice.createRfcommSocketToServiceRecord(BT_UUID);
            mBluetoothSocket.connect();
            mThreadConnectedBluetooth = new ConnectedBluetoothThread(mBluetoothSocket);
            mThreadConnectedBluetooth.start();
            editor.putString("address",address);
            editor.putBoolean("IsAddress",true);
            editor.apply();
        }catch (IOException e){
            Toast.makeText(getApplicationContext(),"블루투스 연결 중 오류가 발생했어요",Toast.LENGTH_SHORT).show();
        }
    }

    private class ConnectedBluetoothThread extends Thread{
        private final BluetoothSocket mmSocket;
        private final InputStream mmInStream;
        private final OutputStream mmOutStream;

        public ConnectedBluetoothThread(BluetoothSocket socket){
            mmSocket = socket;
            InputStream tmpIn = null;
            OutputStream tmpOut = null;

            try{
                tmpIn = socket.getInputStream();
                tmpOut = socket.getOutputStream();
            }catch (IOException e){
                Toast.makeText(getApplicationContext(),"소켓 연결 중 오류가 발생했습니다", Toast.LENGTH_SHORT).show();
            }
            mmInStream = tmpIn;
            mmOutStream = tmpOut;
        }

        public void run(){
            byte[] buffer = new byte[1024];
            int bytes;

            while (true){
                try {
                    bytes = mmInStream.available();
                    if(bytes != 0){
                        SystemClock.sleep(100);
                        bytes = mmInStream.available();
                        bytes = mmInStream.read(buffer,0,bytes);
                        mBluetoothHandler.obtainMessage(BT_MESSAGE_READ,bytes,-1,buffer).sendToTarget();
                    }
                }catch (IOException e){
                    break;
                }
            }
        }

        public void write(String str){
            byte[] bytes =str.getBytes();
            try{
                mmOutStream.write(bytes);
            }catch (IOException e){
                Toast.makeText(getApplicationContext(),"데이터 전송 중 오류가 발생했습니다", Toast.LENGTH_SHORT).show();
            }
        }
        public void cancel(){
            try{
                mmSocket.close();
            }catch (IOException e){
                Toast.makeText(getApplicationContext(),"소켓 해제 중 오류가 발생했습니다.", Toast.LENGTH_SHORT).show();
            }
        }
    }



    @Override
    protected void onDestroy(){
        super.onDestroy();

        unregisterReceiver(receiver);
        mThreadConnectedBluetooth.cancel();
        bluetoothAdapter.disable();
    }
}
