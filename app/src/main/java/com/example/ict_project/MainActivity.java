package com.example.ict_project;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;


public class MainActivity extends AppCompatActivity {


    ImageView[] iv_pos = new ImageView[12];
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);





        iv_pos[0] = (ImageView)findViewById(R.id.pos1);
        iv_pos[1] = (ImageView)findViewById(R.id.pos2);
        iv_pos[2] = (ImageView)findViewById(R.id.pos3);
        iv_pos[3] = (ImageView)findViewById(R.id.pos4);
        iv_pos[4] = (ImageView)findViewById(R.id.pos5);
        iv_pos[5] = (ImageView)findViewById(R.id.pos6);
        iv_pos[6] = (ImageView)findViewById(R.id.pos7);
        iv_pos[7] = (ImageView)findViewById(R.id.pos8);
        iv_pos[8] = (ImageView)findViewById(R.id.pos9);
        iv_pos[9] = (ImageView)findViewById(R.id.pos10);
        iv_pos[10] = (ImageView)findViewById(R.id.pos11);
        iv_pos[11] = (ImageView)findViewById(R.id.pos12);


        iv_pos[0].setImageResource(R.drawable.line_0);
        iv_pos[1].setImageResource(R.drawable.line_1);
        iv_pos[2].setImageResource(R.drawable.line_2);
        iv_pos[3].setImageResource(R.drawable.line_3);
        iv_pos[4].setImageResource(R.drawable.line_4);
        iv_pos[5].setImageResource(R.drawable.line_5);
        iv_pos[6].setImageResource(R.drawable.line_6);
        iv_pos[7].setImageResource(R.drawable.line_7);
        iv_pos[8].setImageResource(R.drawable.line_8);
        iv_pos[9].setImageResource(R.drawable.line_9);
        iv_pos[10].setImageResource(R.drawable.line_shap);
        iv_pos[11].setImageResource(R.drawable.line_star);


    }

}
