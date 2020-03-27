package com.example.test0317;

import android.Manifest;
import android.app.ActionBar;
import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.database.DataSetObserver;
import android.os.Bundle;
import android.os.Handler;
import android.os.SystemClock;
import android.text.LoginFilter;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Adapter;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.Toolbar;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.reflect.Array;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Stream;

public class MainActivity extends AppCompatActivity {


    //BluetoothAdapter
    BluetoothAdapter mBluetoothAdapter;
    BluetoothSocket mBluetoothSocket;

    //블루투스 요청 액티비티 코드
    final static int BLUETOOTH_REQUEST_CODE = 100;
    final static int BT_MESSAGE_READ = 8;

    private SharedPreferences savedData;
    private SharedPreferences.Editor editor;

    ConnectedBluetoothThread mThreadConnectedBluetooth;

    //UI
    Button btnSearch;
    Button btnShuffle;
    Button btnChangePW;
    ImageView[] iv_pos = new ImageView[12];


    //Adapter
    SimpleAdapter adapterPaired;
    SimpleAdapter adapterDevice;

    //list - Device 목록 저장
    List<Map<String,String>> dataPaired;
    List<Map<String,String>> dataDevice;
    List<BluetoothDevice> bluetoothDevices;

    AlertDialog.Builder alertBuilder1;
    int selectDevice;

    int indexStart, indexEnd = -1;


    String newPW = "10";
    int[] rndArray = new int[12];



    //입력받은 데이터가 저장될 버퍼
    byte[] receivedPW = new byte[32];



    final static UUID BT_UUID = UUID.fromString("00001101-0000-1000-8000-00805f9b34fb");

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        ActivityCompat.requestPermissions(this,
                new String[]{Manifest.permission.ACCESS_FINE_LOCATION,
                        Manifest.permission.ACCESS_COARSE_LOCATION}, 1);

        //UI
        btnSearch = (Button)findViewById(R.id.btnSearch);
        btnShuffle = findViewById(R.id.btn_shuffle);
        btnChangePW = findViewById(R.id.btnChangePW);


        //Adapter1
        dataPaired = new ArrayList<>();
        adapterPaired = new SimpleAdapter(this, dataPaired, android.R.layout.simple_list_item_2, new String[]{"name","address"}, new int[]{android.R.id.text1, android.R.id.text2});
        //Adapter2
        dataDevice = new ArrayList<>();
        adapterDevice = new SimpleAdapter(this, dataDevice, android.R.layout.simple_list_item_2, new String[]{"name","address"}, new int[]{android.R.id.text1, android.R.id.text2});

        //검색된 블루투스 디바이스 데이터
        bluetoothDevices = new ArrayList<>();
        //선택한 디바이스 없음
        selectDevice = -1;

        //블루투스 지원 유무 확인
        mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();

        //블루투스를 지원하지 않으면 null을 리턴한다
        if(mBluetoothAdapter == null){
            Toast.makeText(this, "블루투스를 지원하지 않는 단말기 입니다.", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        savedData = getSharedPreferences("miniDB",MODE_PRIVATE);
        editor = savedData.edit();

        //블루투스 브로드캐스트 리시버 등록
        //리시버2
        IntentFilter searchFilter = new IntentFilter();
        searchFilter.addAction(BluetoothAdapter.ACTION_DISCOVERY_STARTED); //BluetoothAdapter.ACTION_DISCOVERY_STARTED : 블루투스 검색 시작
        searchFilter.addAction(BluetoothDevice.ACTION_FOUND); //BluetoothDevice.ACTION_FOUND : 블루투스 디바이스 찾음
        searchFilter.addAction(BluetoothAdapter.ACTION_DISCOVERY_FINISHED); //BluetoothAdapter.ACTION_DISCOVERY_FINISHED : 블루투스 검색 종료
        searchFilter.addAction(BluetoothDevice.ACTION_BOND_STATE_CHANGED);
        searchFilter.addAction(BluetoothDevice.ACTION_ACL_CONNECTED);
        registerReceiver(mBluetoothSearchReceiver, searchFilter);

        //1. 블루투스가 꺼져있으면 활성화
//        if(!mBluetoothAdapter.isEnabled()){
//            mBluetoothAdapter.enable(); //강제 활성화
//        }

        //2. 블루투스가 꺼져있으면 사용자에게 활성화 요청하기
        if(!mBluetoothAdapter.isEnabled()){
            Intent intent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivityForResult(intent, BLUETOOTH_REQUEST_CODE);
        }else{
            GetListPairedDevice();
        }


        Shuffle();

        btnShuffle.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Shuffle();
            }
        });


        alertBuilder1 = new AlertDialog.Builder(this);
        alertBuilder1.setTitle("검색된 디바이스");
        alertBuilder1.setAdapter(adapterDevice, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int id) {
                BluetoothDevice device;
                mBluetoothAdapter.cancelDiscovery();
                String strName = adapterDevice.getItem(id).toString();
                String address = strName.substring(strName.indexOf("address=")+8, strName.indexOf("name=")-2);
                device = mBluetoothAdapter.getRemoteDevice(address);

                try {
                    //선택한 디바이스 페어링 요청
                   // Method method = device.getClass().getMethod("createBond", (Class[]) null);
                   // method.invoke(device, (Object[]) null);
                    mBluetoothSocket = device.createInsecureRfcommSocketToServiceRecord(BT_UUID);
                    mBluetoothSocket.connect();
                    mThreadConnectedBluetooth = new ConnectedBluetoothThread(mBluetoothSocket);
                    mThreadConnectedBluetooth.start();
                    editor.putString("address",address);
                    editor.putBoolean("IsAddress",true);
                    editor.apply();
                } catch (Exception e) {
                    e.printStackTrace();
                    editor.putBoolean("IsAddress",false);
                }
            }
        });
        //alertBuilder1.setCancelable(false);
    }

    private class ConnectedBluetoothThread extends Thread{
        private final BluetoothSocket mmSocket;
        private final InputStream mmInStream;
        private final OutputStream mmOutStream;

        public ConnectedBluetoothThread(BluetoothSocket socket) {
            mmSocket = socket;
            InputStream tmpIn = null;
            OutputStream tmpOut = null;

            try {
                tmpIn = socket.getInputStream();
                tmpOut = socket.getOutputStream();
            } catch (IOException e) {
                Toast.makeText(getApplicationContext(), "소켓 연결 중 오류가 발생했습니다.", Toast.LENGTH_LONG).show();
            }
            mmInStream = tmpIn;
            mmOutStream = tmpOut;
        }

        public void run() {
            byte[] buffer = new byte[1024];
            int bytes;

            int checkBytes = 0;
            byte[] buffer_b = new byte[1024];

            while (true) {
                try {
                    // Read from the InputStream
                    bytes = mmInStream.read(buffer);

                    if(bytes == 1){ //통신 시 한자리만 오는 것을 예외처리
                        System.arraycopy(buffer, 0,buffer_b,0,1);
                        checkBytes = 1;
                    } else {
                        if(checkBytes == 1){
                            System.arraycopy(buffer,0,buffer_b,1,bytes);
                            checkBytes = 0;
                        }
                    }

                    // Send the obtained bytes to the UI Activity
                    //mHandler.obtainMessage(BebopActivity.MESSAGE_READ, bytes, -1, buffer).sendToTarget();
                    GetReceivedData(bytes+1, buffer_b);
                } catch (IOException e) {
                    Log.e("MainActivity", "connected(run) - message=" + e.getLocalizedMessage());
                    //connectionLost();
                    break;
                }

//                try {
//                    bytes = mmInStream.available();
//                    if (bytes != 0) {
//                        SystemClock.sleep(100);
//                        bytes = mmInStream.available(); // 현재 읽을 수 있는 바이트 수를 얻는다
//                        bytes = mmInStream.read(buffer, 0, bytes); // bytes만큼 읽어서 buffer[]의 0의 자리에 저장한다
//
//                        SearchStartEnd();
//                }
            }
        }

        public void GetReceivedData(int bytes, byte[] buffer){
            String RealMessage;
            char[] arrMessage;
            byte[] readBuf = buffer;
            if(bytes>=3) { //앞뒤로 < >가 들어가므로 최소 3자리
                String readMessage = new String(readBuf, 0, bytes);
                Log.e("MainActivity", "SearchStartEnd(run) - message=" + readMessage);
                RealMessage = readMessage.substring(1, bytes-1);  // RealMessage : 앞뒤 < >를 잘라낸 데이터
                Log.e("MainActivity","realMessage = " + RealMessage);
                /*arrMessage = RealMessage.toCharArray(); //arrMessage : 받아온 문자열 -> char[]로 변환한 데이터*/


                char[] charPW = new char[newPW.length()];
                for(int i = 0; i<charPW.length;i++) { //newPW의 길이만큼 실행
                    charPW[i] = (newPW.charAt(i)); //newPW를 char[]로 자르기
                }

                int[] tmp = new int[charPW.length];
                Log.d("main", "charPW = " + charPW[0]);

                for (int i = 0; i<charPW.length;i++) {
                    char k = charPW[i];
                    tmp[i] = rndArray[k-48]; //charPW(값)이 있는 위치값을 tmp에 저장한다
                }
                String str = Arrays.toString(tmp).replace("10","A").replace("11","B").replace(", ","").replace("[","").replace("]","");
                Log.d("main","strstr = "+str);

                if (RealMessage.equals(str)){
                    mThreadConnectedBluetooth.write1((byte)0x41);
                }
/*
                int[] tmp = new int[newPW.length()];
                int k =0;
                char[] charPw = new char[newPW.length()]; //newPW를 char형 배열로 변환
                String rndStr[]=new String[rndArray.length];
                for (int i =0; i<rndArray.length;i++) {
                     rndStr[i] = String.valueOf(rndArray[i]); //rndArray를 String으로 변환
                }
                for(int i = 0; i<charPw.length;i++) { //newPW의 길이만큼 실행
                    charPw[i] = (newPW.charAt(i)); //newPW를 char[]로 자르기
                    Log.d("main","charPW = "+ charPw[i]);
                    //rndStr[]
                   // tmp[k] = rndStr.indexOf(charPw[i]); //rndArray에서 저장된 pw의 index값을 tmp에 저장
                    Log.d("rndaa",rndStr[0]);
                    k++;
                }*/






                //if(Arrays.equals(tmparr,arrMessage)){
                //    mThreadConnectedBluetooth.write1((byte)0x41);
               // }
        }
            //readBuf = null;
        }

        /*
        public void write(String str) {
            byte[] bytes = str.getBytes();
            try {
                mmOutStream.write(bytes);
            } catch (IOException e) {
                Toast.makeText(getApplicationContext(), "데이터 전송 중 오류가 발생했습니다.", Toast.LENGTH_LONG).show();
            }
        }*/

        public void write1(byte str) {
            byte bytes = str;
            try {
                mmOutStream.write(bytes);
            } catch (IOException e) {
                Toast.makeText(getApplicationContext(), "데이터 전송 중 오류가 발생했습니다.", Toast.LENGTH_LONG).show();
            }
        }

        public void cancel() {
            try {
                mmSocket.close();
            } catch (IOException e) {
                Toast.makeText(getApplicationContext(), "소켓 해제 중 오류가 발생했습니다.", Toast.LENGTH_LONG).show();
            }
        }
    }



    //블루투스 상태변화 BroadcastReceiver
    BroadcastReceiver mBluetoothStateReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            //BluetoothAdapter.EXTRA_STATE : 블루투스의 현재상태 변화
            int state = intent.getIntExtra(BluetoothAdapter.EXTRA_STATE, -1);

            //블루투스 활성화
            if(state == BluetoothAdapter.STATE_ON){
                Toast.makeText(getApplicationContext(),"블루투스 활성화",Toast.LENGTH_LONG).show();
            }
            //블루투스 활성화 중
            else if(state == BluetoothAdapter.STATE_TURNING_ON){
                Toast.makeText(getApplicationContext(),"블루투스 활성화 중...",Toast.LENGTH_LONG).show();
            }
            //블루투스 비활성화
            else if(state == BluetoothAdapter.STATE_OFF){
                Toast.makeText(getApplicationContext(),"블루투스 비활성화",Toast.LENGTH_LONG).show();
            }
            //블루투스 비활성화 중
            else if(state == BluetoothAdapter.STATE_TURNING_OFF){
                Toast.makeText(getApplicationContext(),"블루투스 비활성화 중...",Toast.LENGTH_LONG).show();
            }
        }
    };

    //블루투스 검색결과 BroadcastReceiver
    BroadcastReceiver mBluetoothSearchReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            String savedAddress = savedData.getString("address",null);

            switch(action){


                //블루투스 디바이스 검색 종료
                case BluetoothAdapter.ACTION_DISCOVERY_STARTED:
                    dataDevice.clear();
                    bluetoothDevices.clear();
                    Toast.makeText(MainActivity.this, "블루투스 검색 시작", Toast.LENGTH_SHORT).show();
                    alertBuilder1.show();
                    break;


                //블루투스 디바이스 검색 종료
                case BluetoothAdapter.ACTION_DISCOVERY_FINISHED:
                    btnSearch.setEnabled(true);
                    break;


                //블루투스 디바이스 찾음
                case BluetoothDevice.ACTION_FOUND:
                    //검색한 블루투스 디바이스의 객체를 구한다
                    BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);

                    //데이터 저장
                    Map map = new HashMap();
                    map.put("name", device.getName()); //device.getName() : 블루투스 디바이스의 이름
                    map.put("address", device.getAddress()); //device.getAddress() : 블루투스 디바이스의 MAC 주소
                    dataDevice.add(map);
                    //리스트 목록갱신
                    adapterDevice.notifyDataSetChanged();
                    //블루투스 디바이스 저장

                    break;

                //블루투스 디바이스 페어링 상태 변화
                case BluetoothDevice.ACTION_BOND_STATE_CHANGED:
                    BluetoothDevice paired = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
                    if(paired.getBondState()==BluetoothDevice.BOND_BONDED){
                        //데이터 저장
                        Map map2 = new HashMap();
                        map2.put("name", paired.getName()); //device.getName() : 블루투스 디바이스의 이름
                        map2.put("address", paired.getAddress()); //device.getAddress() : 블루투스 디바이스의 MAC 주소
                        dataPaired.add(map2);
                        //리스트 목록갱신
                        adapterPaired.notifyDataSetChanged();

                        //검색된 목록
                        if(selectDevice != -1){
                            bluetoothDevices.remove(selectDevice);
                            dataDevice.remove(selectDevice);
                            adapterDevice.notifyDataSetChanged();
                            selectDevice = -1;
                        }
                    }
                    break;

                case BluetoothDevice.ACTION_ACL_CONNECTED:

                    break;
            }
        }
    };


    //블루투스 검색 버튼 클릭
    public void mOnBluetoothSearch(View v){
        //검색버튼 비활성화
        btnSearch.setEnabled(false);
        //mBluetoothAdapter.isDiscovering() : 블루투스 검색중인지 여부 확인
        //mBluetoothAdapter.cancelDiscovery() : 블루투스 검색 취소
        if(mBluetoothAdapter.isDiscovering()){
            mBluetoothAdapter.cancelDiscovery();
        }
        //mBluetoothAdapter.startDiscovery() : 블루투스 검색 시작
        mBluetoothAdapter.startDiscovery();
    }


    //이미 페어링된 목록 가져오기
    public void GetListPairedDevice(){
        Set<BluetoothDevice> pairedDevice = mBluetoothAdapter.getBondedDevices();

        dataPaired.clear();
        if(pairedDevice.size() > 0){
            for(BluetoothDevice device : pairedDevice){
                //데이터 저장
                Map map = new HashMap();
                map.put("name", device.getName()); //device.getName() : 블루투스 디바이스의 이름
                map.put("address", device.getAddress()); //device.getAddress() : 블루투스 디바이스의 MAC 주소
                dataPaired.add(map);
            }
        }
        //리스트 목록갱신
        adapterPaired.notifyDataSetChanged();
    }



    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        switch (requestCode) {
            case BLUETOOTH_REQUEST_CODE:
                //블루투스 활성화 승인
                if (resultCode == Activity.RESULT_OK) {
                    GetListPairedDevice();
                }
                //블루투스 활성화 거절
                else {
                    Toast.makeText(this, "블루투스를 활성화해야 합니다.", Toast.LENGTH_SHORT).show();
                    finish();
                    return;
                }
                break;
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
                    if(mBluetoothSocket!=null) {
                        if (rnd == 11) {
                            mThreadConnectedBluetooth.write1((byte) ((byte) (0x60) + (byte) (i - 1))); //*자리 전송
                        } else if (rnd == 10) {
                            mThreadConnectedBluetooth.write1((byte) ((byte) (0x70) + (byte) (i - 1))); //#자리 전송
                        }
                    }

                    int posId = getResources().getIdentifier("pos" + i, "id", getPackageName()); // @+id/pos1, @+id/pos2...를 posId에 넣는다.
                    int drawableId = getResources().getIdentifier("line_" + rnd, "drawable", getPackageName()); // drawable/line_0, drawable/line_2를 drawableId에 넣는다

                    iv_pos[i - 1] = (ImageView) findViewById(posId); //id 할당
                    iv_pos[i - 1].setImageResource(drawableId);  // 할당된 iv_pos[]에 그림 그리기
                    flag[rnd] = !flag[rnd];


                    rndArray[rnd] = i-1;


                    break;
                }
            }
        }



        /*
        char[] charMessage = new char[RealMessage.length()];
        int[] intArray;
        RealMessage.getChars(0, RealMessage.length(), charMessage, 0); //charMessage에 문자열을 문자배열로 복사
        intArray = new String(charMessage).chars().toArray();
        for(int k = 0; k<RealMessage.length(); k++) {
            intArray[k] = rndArray[charMessage[k]];

        }*/
        //RecievedPW = new String(charMessage);
    }



    public void ChangePW(View view)
    {
        final EditText et = new EditText(this);
        final AlertDialog.Builder builder = new AlertDialog.Builder(this,R.style.ChangeMyPW);
        builder.setTitle("비밀번호 바꾸기").setMessage("변경할 비밀번호를 입력하세요");;

        builder.setView(et);

        builder.setPositiveButton("저장", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                newPW = et.getText().toString();
                Toast.makeText(getApplicationContext(),"비밀번호가 " + newPW + "로 변경되었습니다",Toast.LENGTH_LONG).show();
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

    public void ComparePW(String str){
        if(newPW.equals(str)){
            mThreadConnectedBluetooth.write1((byte)0x41);
        }
    }

    @Override
    protected void onDestroy() {
        receivedPW = null;
        mThreadConnectedBluetooth.cancel();
        unregisterReceiver(mBluetoothStateReceiver);
        unregisterReceiver(mBluetoothSearchReceiver);
        super.onDestroy();
    }
}

