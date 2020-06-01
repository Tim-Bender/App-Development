package com.example.main;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.view.View;
import android.widget.Toast;

/**
 * Author: Timothy Bender
 * timothy.bender@spudnik.com
 * 530-414-6778
 * Please see README before updating anything
 */

public class home extends AppCompatActivity {
    private vehicle myvehicle;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.content_home);
        Toolbar myToolBar = findViewById(R.id.toolbar);
        setSupportActionBar(myToolBar);
        setTitle("Home");
        myToolBar.setTitleTextColor(Color.WHITE);
        this.myvehicle=getIntent().getParcelableExtra("myvehicle");

        Toast.makeText(this,"Welcome!",Toast.LENGTH_LONG).show();
    }
    public void diagTool(View view){
        try {
            Intent i = new Intent(getBaseContext(), inputserial.class);
            i.putExtra("myvehicle",myvehicle);
            startActivity(i);
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    public void update(View view){
        try {
            Toast.makeText(this, "Function Not Supported", Toast.LENGTH_SHORT).show();
            //Intent i = new Intent(getBaseContext(), inputserial.class);
            //i.putExtra("myvehicle",myvehicle);
            //startActivity(i);
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    public void logData(View view){
        try {
            Toast.makeText(this, "Function Not Supported", Toast.LENGTH_SHORT).show();
            //Intent i = new Intent(getBaseContext(), inputserial.class);
            //i.putExtra("myvehicle",myvehicle);
            //startActivity(i);
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    public void settings(View view){
        try{
            Intent i = new Intent(getBaseContext(), settings.class);
            startActivity(i);
        }catch(Exception e){
            e.printStackTrace();
        }

    }

}
