package com.example.ict0314;

import android.app.Activity;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import androidx.appcompat.app.AlertDialog;

public class MainActivity extends Activity implements OnClickListener {


	// Debugging
	private static final String TAG = "Main";

	int lenPW=4;
	String newPW;
	
	// Intent request code
	private static final int REQUEST_CONNECT_DEVICE = 1;
	private static final int REQUEST_ENABLE_BT = 2;
	
	// Layout
	private Button btn_Connect;
	private Button btn_shuffle;
	ImageView[] iv_pos = new ImageView[12];
	
	private BluetoothService btService = null;
	
	
	private final Handler mHandler = new Handler() {

		@Override
		public void handleMessage(Message msg) {
			super.handleMessage(msg);
		}
		
	};

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		Log.e(TAG, "onCreate");
		setContentView(R.layout.activity_main);

		Shuffle();

		/** Main Layout **/
		btn_shuffle = findViewById(R.id.btn_shuffle);
		btn_shuffle.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				Shuffle();
			}
		});

		btn_Connect = findViewById(R.id.btn_connect);
		btn_Connect.setOnClickListener(this);
		

		if(btService == null) {
			btService = new BluetoothService(this, mHandler);
		}
	}

	@Override
	public void onClick(View v) {
		if(btService.getDeviceState()) {

			btService.enableBluetooth();
		} else {
			finish();
		}
	}
	
	public void onActivityResult(int requestCode, int resultCode, Intent data) {
        Log.d(TAG, "onActivityResult " + resultCode);
        
        switch (requestCode) {

            case REQUEST_CONNECT_DEVICE:
                if(resultCode == Activity.RESULT_OK){
                    btService.getDeviceInfo(data);
                }
                break;
            case REQUEST_ENABLE_BT:
                // When the request to enable Bluetooth returns
                if (resultCode == Activity.RESULT_OK) {

                    btService.scanDevice();
                } else {

                    Log.d(TAG, "Bluetooth is not enabled");
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
				newPW = et.getText().toString();
				lenPW = Integer.parseInt(newPW);
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

	@Override
	protected void onDestroy() {
		super.onDestroy();
		btService.btAdapter.disable();
	}
}
