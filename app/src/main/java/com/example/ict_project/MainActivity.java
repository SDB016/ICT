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


        for (int i = 1; i < 13; i++) {
            int posId = getResources().getIdentifier("pos" + i, "id", getPackageName());
            iv_pos[i - 1] = (ImageView) findViewById(posId);

            int drawableId = getResources().getIdentifier("line_" + (i - 1), "drawable", getPackageName());
            iv_pos[i - 1].setImageResource(drawableId);


        }

    }
}
