package com.example.Spudnik;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.preference.PreferenceManager;
import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;

import java.io.InputStream;

public class MainActivity extends AppCompatActivity {
    private ProgressBar progressBar;
    private TextView textView;
    private int progressStatus = 0;
    private Handler handler = new Handler();
    private vehicle myVehicle;
    private InputStream is;
    private InputStream d;

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
            ImageView image = findViewById(R.id.gifloadingscreen);
            Glide.with(this).load(R.drawable.heartbeatgiftransparent).into(image);
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
                            Thread.sleep(30);
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
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
        if(preferences.getBoolean("nightmode",false)){
            nightMode();
        }
        AsyncTask.execute(new Runnable() {
            @Override
            public void run() {
                myVehicle.buildDealers(d);
                myVehicle.buildVehicleIds(is);
            }
        });

        super.onStart();
    }

    public void nightMode(){
        ConstraintLayout constraints = findViewById(R.id.mainactivityconstraintlayout);
        constraints.setBackgroundColor(Color.parseColor("#333333"));
        TextView textView = findViewById(R.id.textView2);
        textView.setTextColor(Color.WHITE);
        textView = findViewById(R.id.textView3);
        textView.setTextColor(Color.WHITE);
    }
}