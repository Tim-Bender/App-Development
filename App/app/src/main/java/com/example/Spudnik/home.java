package com.example.Spudnik;

import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.AsyncTask;
import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.preference.PreferenceManager;

import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import java.io.InputStream;

/**
 * Author: Timothy Bender
 * timothy.bender@spudnik.com
 * 530-414-6778
 * Please see README before updating anything
 */

public class home extends AppCompatActivity {
    private vehicle myvehicle;
    private SharedPreferences preferences;
    private InputStream is;
    private InputStream d;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.content_home);
        Toolbar myToolBar = findViewById(R.id.toolbar);
        setSupportActionBar(myToolBar);
        setTitle("Home");
        myToolBar.setTitleTextColor(Color.WHITE);
        this.myvehicle = new vehicle();
        Toast.makeText(this,"Welcome!",Toast.LENGTH_LONG).show();
        d = getResources().openRawResource(R.raw.dealerids);
        is= getResources().openRawResource(R.raw.parsedtest);

    }
    @Override
    protected void onStart() {
        super.onStart();
        preferences = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
        if(preferences.getBoolean("nightmode",false)){
            nightMode();
        }
        AsyncTask.execute(new Runnable() {
            @Override
            public void run() {
                myvehicle.buildDealers(d);
                myvehicle.buildVehicleIds(is);
            }
        });
    }
    @Override
    protected void onResume() {
        super.onResume();
        if(preferences.getBoolean("nightmode",false)){
            nightMode();
            return;
        }
       if(!preferences.getBoolean("nightmode",false)){
           dayMode();
       }
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

    @Override
    public boolean onOptionsItemSelected(MenuItem item){
        if(item.getItemId() == R.id.action_settings){
            Intent i = new Intent(getBaseContext(),settings.class);
            startActivity(i);
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    public boolean onCreateOptionsMenu(Menu menu){
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.toolbarbuttons,menu);
        return true;
    }

    public void nightMode(){
        try {
            ConstraintLayout constraintLayout = findViewById(R.id.homeconstraintlayout);
            constraintLayout.setBackgroundColor(Color.parseColor("#333333"));
            Button button = findViewById(R.id.rundiagtoolbutton);
            button.setBackgroundResource(R.drawable.nightmodebuttonselector);
            button.setTextColor(Color.WHITE);
            button = findViewById(R.id.updatesoftwarebutton);
            button.setBackgroundResource(R.drawable.nightmodebuttonselector);
            button.setTextColor(Color.WHITE);
            button = findViewById(R.id.logdatabutton);
            button.setBackgroundResource(R.drawable.nightmodebuttonselector);
            button.setTextColor(Color.WHITE);
            button = findViewById(R.id.settingshomebutton);
            button.setBackgroundResource(R.drawable.nightmodebuttonselector);
            button.setTextColor(Color.WHITE);
            TextView textView = findViewById(R.id.hometextview);
            textView.setTextColor(Color.WHITE);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void dayMode(){
        ConstraintLayout constraintLayout = findViewById(R.id.homeconstraintlayout);
        constraintLayout.setBackgroundColor(Color.WHITE);
        Button button = findViewById(R.id.rundiagtoolbutton);
        button.setBackgroundResource(R.drawable.daymodebuttonselector);
        button.setTextColor(Color.BLACK);
        button = findViewById(R.id.updatesoftwarebutton);
        button.setBackgroundResource(R.drawable.daymodebuttonselector);
        button.setTextColor(Color.BLACK);
        button = findViewById(R.id.logdatabutton);
        button.setBackgroundResource(R.drawable.daymodebuttonselector);
        button.setTextColor(Color.BLACK);
        button = findViewById(R.id.settingshomebutton);
        button.setBackgroundResource(R.drawable.daymodebuttonselector);
        button.setTextColor(Color.BLACK);
        TextView textView = findViewById(R.id.hometextview);
        textView.setTextColor(Color.BLACK);

    }

}
