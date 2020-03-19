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
import android.util.Log;
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
    private static final int REQUEST_ENABLE_BT = 1;

    ArrayList<String> pairedList;

    Button btnConnect;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        ActivityCompat.requestPermissions(this,
                new String[]{Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION}, 1);

        if (bluetoothAdapter == null) {
            Toast.makeText(getApplicationContext(), "블루투스를 지원하지 않는 기기입니다", Toast.LENGTH_SHORT).show();
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


        IntentFilter filter = new IntentFilter();
        filter.addAction(BluetoothAdapter.ACTION_STATE_CHANGED); //블루투스 상태 변화 액션
        filter.addAction(BluetoothDevice.ACTION_FOUND); //기기 검색됨
        filter.addAction(BluetoothAdapter.ACTION_DISCOVERY_STARTED); //기기 검색 시작
        filter.addAction(BluetoothAdapter.ACTION_DISCOVERY_FINISHED); //기기 검색 종료
        filter.addAction(BluetoothDevice.ACTION_ACL_CONNECTED); //연결확인
        filter.addAction(BluetoothDevice.ACTION_ACL_DISCONNECTED); //연결 끊김 확인
        registerReceiver(receiver, filter);




    }





    private void BluetoothOn(){
        if (!bluetoothAdapter.isEnabled()) {
            Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivityForResult(enableBtIntent, REQUEST_ENABLE_BT);
        }
    }


    private final BroadcastReceiver receiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            String name = null;
            Log.d("Bluetooth Action",action);
            final BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
            if(device != null) name = device.getName();
            switch (action){
                case BluetoothAdapter.ACTION_DISCOVERY_STARTED:
                    break;

                case BluetoothDevice.ACTION_FOUND:

                    String device_name = device.getName();
                    String device_Address = device.getAddress();
                    
                    break;

                case BluetoothAdapter.ACTION_DISCOVERY_FINISHED:
                    ConnectSelectedDevice();
                    break;
            }
        }
    };

    private void ConnectSelectedDevice(){
        if()
    }

    @Override
    protected void onPause(){
        super.onPause();
        unregisterReceiver(receiver); //앱이 비활성화일 때 채널 수신하지 않음
    }

    @Override
    protected void onDestroy(){
        super.onDestroy();


        unregisterReceiver(receiver);
    }















}
