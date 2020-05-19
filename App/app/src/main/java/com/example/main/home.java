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
        this.setTitle("Home Page");
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
