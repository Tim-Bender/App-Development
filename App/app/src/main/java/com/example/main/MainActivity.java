package com.example.main;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;
import java.io.InputStream;

public class MainActivity extends AppCompatActivity {
    private ProgressBar progressBar;
    private TextView textView;
    private int progressStatus = 0;
    private Handler handler = new Handler();
    private vehicle myVehicle;
    InputStream is;
    InputStream d;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        setTitle("Loading");
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        toolbar.setTitleTextColor(Color.WHITE);
        myVehicle = new vehicle();
        d = getResources().openRawResource(R.raw.dealerids);
        is= getResources().openRawResource(R.raw.parsedtest);
        try {
            this.progressBar = findViewById(R.id.loadingbar);
            this.progressBar.getProgressDrawable().setColorFilter(Color.RED, PorterDuff.Mode.SRC_IN);
            this.textView = findViewById(R.id.loadingText);
            new Thread(new Runnable() {
                @Override
                public void run() {
                    while (progressStatus < 100) {
                        progressStatus += 1;
                        handler.post(new Runnable() {
                            @SuppressLint("SetTextI18n")
                            @Override
                            public void run() {
                                progressBar.setProgress(progressStatus);
                                textView.setText("Loading " + progressStatus + "%");
                            }
                        });
                        try {
                            Thread.sleep(20);
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                    }
                    Intent i = new Intent(getBaseContext(), home.class);
                    i.putExtra("myvehicle",myVehicle);
                    startActivity(i);
                    finish();
                }
            }).start();
        } catch (Exception e) {
            Toast.makeText(this, "Unidentified Error", Toast.LENGTH_SHORT).show();
            e.printStackTrace();
        }
    }
    @Override
    protected void onStart() {
        AsyncTask.execute(new Runnable() {
            @Override
            public void run() {
                myVehicle.buildDealers(d);
                myVehicle.buildVehicleIds(is);
            }
        });

        super.onStart();
    }
}