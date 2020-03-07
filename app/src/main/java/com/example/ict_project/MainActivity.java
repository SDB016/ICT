package com.example.ict_project;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Paint;
import android.os.Bundle;
import android.os.Handler;
import android.os.SystemClock;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.Toast;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.UUID;


public class MainActivity extends AppCompatActivity {

    Handler mBluetoothHandler;
    Set<BluetoothDevice> mPairedDevices;
    List<String> mListpaireddevices;

    ConnectedBluetoothThread mThreadConnectedBluetooth;
    BluetoothDevice mBluetoothDevice;
    BluetoothSocket mBluetoothSocket;

    //String myPW = new String()
    ImageView[] miv_pos = new ImageView[12];
    BluetoothAdapter mBluetoothAdapter;

    final static int BT_REQUEST_ENABLE = 1;
    final static int BT_MESSAGE_READ = 2;
    final static int BT_CONNECTING_STATUS = 3;
    final static UUID BT_UUID = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB");

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        bluetoothOn();

        Shuffle();

        ImageButton mbtnShuffle = (ImageButton)findViewById(R.id.btn_shuffle);
        ImageButton mbtnConnect = (ImageButton)findViewById(R.id.btn_connect);

        mbtnConnect.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                listPairDevices();
            }
        });

        mbtnShuffle.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Shuffle();
            }
        });

        mBluetoothHandler = new Handler(){
            public  void handleMessage(android.os.Message msg){
                if(msg.what == BT_MESSAGE_READ){
                    String readMessage = null;
                    try {
                        readMessage = new String((byte[])msg.obj,"UTF-8");
                    }catch (UnsupportedEncodingException e){
                        e.printStackTrace(); // 블루투스 핸들러로 블루투스 연결 뒤 수신된 데이터를 readMessage에 저장
                    }
                }
            }
        };
    }

    public void bluetoothOn()
    {
        if(mBluetoothAdapter == null){
            Toast.makeText(getApplicationContext(),"블루투스를 지원하지 않는 기기입니다",Toast.LENGTH_LONG).show();
        }
        else{
            if(mBluetoothAdapter.isEnabled()){
                Toast.makeText(getApplicationContext(),"이미 활성화 되어 있습니다",Toast.LENGTH_LONG).show();
            }
            else{
                Toast.makeText(getApplicationContext(),"블루투스를 활성화합니다",Toast.LENGTH_LONG).show();
                Intent intentBluetoothEnable = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE); //활성화창을 띄움
                startActivityForResult(intentBluetoothEnable, BT_REQUEST_ENABLE);
            }
        }
    }
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data){
        switch (requestCode){
            case BT_REQUEST_ENABLE:
                if(requestCode == RESULT_OK) { // 블루투스 활성화를 확인 버튼을 눌렀다면
                    Toast.makeText(getApplicationContext(),"블루투스 활성화",Toast.LENGTH_LONG).show();
                }
                else if(resultCode == RESULT_CANCELED){ // 블루투스 활성화를 취소 버튼을 눌렀다면
                    Toast.makeText(getApplicationContext(),"취소",Toast.LENGTH_LONG).show();
                }
                break;
        }
        super.onActivityResult(requestCode,resultCode,data);
    }

    void listPairDevices(){
        if (mBluetoothAdapter.isEnabled()){ //활성화 되어있는지 확인
            if(mPairedDevices.size()>0){ //페어링 된 장치가 있다면
                AlertDialog.Builder builder = new AlertDialog.Builder(this); //새로운 알람창 객체를 생성하여
                builder.setTitle("장치 선택"); //"장치선택" 타이틀과

                mListpaireddevices = new ArrayList();
                for (BluetoothDevice device : mPairedDevices){
                    mListpaireddevices.add(device.getName()); //긱 페어링된 장치명을 추가해준다.
                }
                final CharSequence[] items = mListpaireddevices.toArray(new CharSequence[mListpaireddevices.size()]); //페어링된 장치수를 얻어와서
                mListpaireddevices.toArray(new CharSequence[mListpaireddevices.size()]);

                builder.setItems(items, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int item) { // 각 장치를 누르면 장치 명을 매개변수로 사용하여
                        connectSelectedDevice(items[item].toString()); //connectSelectDevice 매서드로 전달
                    }
                });
                AlertDialog alert = builder.create();
                alert.show(); //위에서 리스트로 추가된 알람 창을 실제로 띄워준다
            } else {
                Toast.makeText(getApplicationContext(),"페어링 된 장치가 없습니다",Toast.LENGTH_LONG).show();
            }
        }
        else {
            Toast.makeText(getApplicationContext(),"블루투스가 활성화 되어있지 않습니다",Toast.LENGTH_LONG).show();
        }
    }

    void connectSelectedDevice(String selectedDeviceName){ //실제로 블루투스 장치와 연결하는 부분, 장치이름을 매개변수로 넘겨받음
        for(BluetoothDevice tempDevice : mPairedDevices){ //장치의 주소로 for문으로 페어링 된 모든 장치를 거맥하면서
            if(selectedDeviceName.equals(tempDevice.getName())){ // 매개변수값과 같다면
                mBluetoothDevice = tempDevice; //그 장치의 주소값을 얻어옴
                break;
            }
        }
        try{
            mBluetoothSocket = mBluetoothDevice.createInsecureRfcommSocketToServiceRecord(BT_UUID);
            mBluetoothSocket.connect();
            mThreadConnectedBluetooth = new ConnectedBluetoothThread(mBluetoothSocket);
            mThreadConnectedBluetooth.start();
            mBluetoothHandler.obtainMessage(BT_CONNECTING_STATUS,1,-1).sendToTarget();
        }catch (IOException e){
            Toast.makeText(getApplicationContext(),"블루투스 연결 중 오류가 발생했습니다.",Toast.LENGTH_LONG).show();
        }
    }

    private class ConnectedBluetoothThread extends Thread{
        private final BluetoothSocket mmSocket;
        private final InputStream mmInStream;
        private  final OutputStream mmOutStream;

        public ConnectedBluetoothThread(BluetoothSocket socket){
            mmSocket = socket;
            InputStream tmpIn = null;
            OutputStream tmpOut = null;

            try{
                tmpIn = socket.getInputStream();
                tmpOut = socket.getOutputStream();
            }catch (IOException e){
                Toast.makeText(getApplicationContext(),"소켓 연결 중 오류가 발생했습니다",Toast.LENGTH_LONG).show();
            }

            mmInStream = tmpIn;
            mmOutStream = tmpOut;
        }
        public void run(){
            byte [] buffer = new byte[1024];
            int bytes;

            while(true){ //while로 데이터가 존재한다면 데이터를 읽어오는 작업
                try{
                    bytes = mmInStream.available();
                    if (bytes !=0 ){
                        SystemClock.sleep(100);
                        bytes = mmInStream.available();
                        bytes = mmInStream.read(buffer,0,bytes);
                        mBluetoothHandler.obtainMessage(BT_MESSAGE_READ,bytes , -1, buffer).sendToTarget();
                    }
                }catch (IOException e){
                    break;
                }
            }
        }
    }

    public void Shuffle()
    {
        boolean flag[] = new boolean[12];
        for(int i = 1; i < 13; i++)
        {
                while(true) {
                    int rnd = (int)(Math.random() * 12 ); //rnd는 0~11까지 랜덤 수
                    if (!flag[rnd]) {
                        int posId = getResources().getIdentifier("pos" + i, "id", getPackageName()); // @+id/pos1, @+id/pos2...를 posId에 넣는다.
                        int drawableId = getResources().getIdentifier("line_" + rnd, "drawable", getPackageName()); // drawable/line_0, drawable/line_2를 drawableId에 넣는다

                        miv_pos[i - 1] = (ImageView) findViewById(posId); //id 할당
                        miv_pos[i - 1].setImageResource(drawableId);  // 할당된 iv_pos[]에 그림 그리기
                        flag[rnd] = !flag[rnd];
                        break;
                    }
                }
        }
    }

    public void ChangePW(View view)
    {
        final EditText met = new EditText(this);
        final AlertDialog.Builder builder = new AlertDialog.Builder(this,R.style.ChangeMyPW);
        builder.setTitle("비밀번호 바꾸기").setMessage("변경할 비밀번호를 입력하세요");
        builder.setIcon(R.drawable.shuffle);

        builder.setView(met);

        builder.setPositiveButton("저장", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                String newPW = met.getText().toString();  //바뀐 비밀번호가 newPW에 string형으로 저장
            }
        });
        builder.setNegativeButton("취소", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
            }
        });
        AlertDialog alertDialog = builder.create();
        alertDialog.show();
    }




}



