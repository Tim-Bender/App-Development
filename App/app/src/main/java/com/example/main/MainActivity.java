package com.example.main;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.webkit.WebView;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        setTitle("Loading");

        Thread background = new Thread() {
            public void run() {
                try {
                    sleep(3 * 1000);

                    Intent i = new Intent(getBaseContext(), home.class);
                    startActivity(i);
                    finish();
                } catch (Exception e) {
                }
            }
        };

        background.start();
    }
        public void goToHome(View view){
            Intent intent = new Intent(this, home.class);
            startActivity(intent);
        }
}