package com.example.ict_project;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.bluetooth.BluetoothAdapter;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Paint;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.Toast;

import static android.content.Intent.*;


public class MainActivity extends AppCompatActivity {

    BluetoothAdapter mBluetoothAdapter = null;

    //String myPW = new String()
    ImageView[] iv_pos = new ImageView[12];


    final static int BT_REQUEST_ENABLE = 1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Shuffle();
        bluetoothOn();

        Button btnShuffle = (Button)findViewById(R.id.btn_shuffle);
        Button btnOn = (Button)findViewById(R.id.btn_on);

        btnShuffle.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Shuffle();
            }
        });
        btnOn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

            }
        });



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
                Toast.makeText(getApplicationContext(), "블루투스가 이미 활성화 되어 있습니다.", Toast.LENGTH_LONG).show();
            }
            else {
                Toast.makeText(getApplicationContext(), "블루투스가 활성화 되어 있지 않습니다.", Toast.LENGTH_LONG).show();
                Intent intentBluetoothEnable = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
                startActivityForResult(intentBluetoothEnable, BT_REQUEST_ENABLE);
            }
        }
    }


}
