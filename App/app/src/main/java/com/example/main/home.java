package com.example.main;

import android.content.Intent;
import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;

import android.view.View;

public class home extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);
        setTitle("Home");
    }
    public void diagTool(View view){
        Intent i = new Intent(getBaseContext(), inputserial.class);
        startActivity(i);
        finish();

    }

    public void update(View view){
        Intent i = new Intent(getBaseContext(), inputserial.class);
        startActivity(i);
        finish();
    }

    public void logData(View view){
        Intent i = new Intent(getBaseContext(), inputserial.class);
        startActivity(i);
        finish();
    }

    public void settings(View view){


    }

}
