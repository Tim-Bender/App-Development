package com.example.main;

import android.content.Intent;
import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;

import android.view.View;
import android.widget.Toast;
/**
 * Author: Timothy Bender
 * timothy.bender@spudnik.com
 * 530-414-6778
 * Please see README before updating anything
 */

public class home extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);
        setTitle("Home");
        Toast.makeText(this,"Welcome!",Toast.LENGTH_LONG).show();
    }
    public void diagTool(View view){
        Intent i = new Intent(getBaseContext(), inputserial.class);
        startActivity(i);


    }

    public void update(View view){
        Intent i = new Intent(getBaseContext(), inputserial.class);
        startActivity(i);

    }

    public void logData(View view){
        Intent i = new Intent(getBaseContext(), inputserial.class);
        startActivity(i);

    }

    public void settings(View view){


    }

}
