package com.example.ict_project;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Paint;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.SimpleAdapter;
import android.widget.Toast;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static android.content.Intent.*;


public class MainActivity extends AppCompatActivity {

    BluetoothAdapter mBluetoothAdapter = null;

    //String myPW = new String()

    //UI
    Button btnSearch;
    ImageView[] iv_pos = new ImageView[12];

    //Adapter
    SimpleAdapter adapterDevice;

    //list - Device 목록 저장
    List<Map<String,String>> dataDevice;

    final static int BT_REQUEST_ENABLE = 1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Shuffle();
        bluetoothOn();

        Button btnShuffle = (Button)findViewById(R.id.btn_shuffle);
        Button btnSearch = (Button)findViewById(R.id.btnSearch);

        btnShuffle.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Shuffle();
            }
        });
        btnSearch.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
            bluetoothSearch();
            }
        });


        //bluetoothOff();
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

                    iv_pos[i - 1] = (ImageView) findViewById(posId); //id 할당
                    iv_pos[i - 1].setImageResource(drawableId);  // 할당된 iv_pos[]에 그림 그리기
                    flag[rnd] = !flag[rnd];
                    break;
                }
            }
        }
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
                String newPW = et.getText().toString();
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
    public void bluetoothOn() {
        mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        if(mBluetoothAdapter == null) {
            Toast.makeText(getApplicationContext(), "블루투스를 지원하지 않는 기기입니다.", Toast.LENGTH_LONG).show();
        }
        else {
            if (mBluetoothAdapter.isEnabled()) {
                //Toast.makeText(getApplicationContext(), "블루투스가 이미 활성화 되어 있습니다.", Toast.LENGTH_LONG).show();
            }
            else {
                //Toast.makeText(getApplicationContext(), "블루투스가 활성화 되어 있지 않습니다.", Toast.LENGTH_LONG).show();
                Intent intentBluetoothEnable = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
                startActivityForResult(intentBluetoothEnable, BT_REQUEST_ENABLE);
            }
        }
    }
    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data){
        if(requestCode == BT_REQUEST_ENABLE){
            if (resultCode == Activity.RESULT_OK) { // 블루투스 활성화를 확인 클릭했다면
                Toast.makeText(getApplicationContext(), "활성화됨", Toast.LENGTH_LONG).show(); // 블루투스가 활성화 되었을 때 작업
            } else { // 블루투스 활성화를 취소했다면
                Toast.makeText(getApplicationContext(), "취소됨", Toast.LENGTH_LONG).show(); // 사용자가 활성화를 취소한 경우
            }
        }
    }
    public void bluetoothSearch() {
        final BroadcastReceiver mReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                String action = intent.getAction();
                switch (action) {
                    case BluetoothAdapter.ACTION_DISCOVERY_STARTED:
                        dataDevice.clear();
                        Toast.makeText(MainActivity.this, "블루투스 검색 시작", Toast.LENGTH_SHORT).show();
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
                        break;
                    //블루투스 디바이스 검색 종료
                    case BluetoothAdapter.ACTION_DISCOVERY_FINISHED:
                        Toast.makeText(MainActivity.this, "블루투스 검색 종료", Toast.LENGTH_SHORT).show();
                        btnSearch.setEnabled(true);
                        break;


                }
            }
        };
    }



    /*
    public void bluetoothOff(){
        if(mBluetoothAdapter.isEnabled()){
            mBluetoothAdapter.disable();
            Toast.makeText(getApplicationContext(),"블루투스가 비활성화 됨니다",Toast.LENGTH_LONG).show();
        }
        else{
            Toast.makeText(getApplicationContext(),"블루투스가 이미 비활성화 되어 있습니다",Toast.LENGTH_LONG).show();
        }
    }
    */


}
