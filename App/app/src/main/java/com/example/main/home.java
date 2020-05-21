package com.example.main;

import android.content.Context;
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
    Toolbar myToolBar;
    Context context;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        context = this;
        setContentView(R.layout.content_home);
        this.myToolBar = findViewById(R.id.toolbar);
        setSupportActionBar(myToolBar);
        setTitle("Home");
        this.myToolBar.setTitleTextColor(Color.WHITE);
        Toast.makeText(this,"Welcome!",Toast.LENGTH_LONG).show();
    }
    public void diagTool(View view){
        try {
            Intent i = new Intent(getBaseContext(), inputserial.class);
            startActivity(i);
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    public void update(View view){
        try {
            Intent i = new Intent(getBaseContext(), inputserial.class);
            startActivity(i);
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    public void logData(View view){
        try {
            Intent i = new Intent(getBaseContext(), inputserial.class);
            startActivity(i);
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    public void settings(View view){
        /*try{
            Intent i = new Intent(getBaseContext(), settings.class);
            startActivity(i);
        }catch(Exception e){
            e.printStackTrace();
        }*/

    }

}
