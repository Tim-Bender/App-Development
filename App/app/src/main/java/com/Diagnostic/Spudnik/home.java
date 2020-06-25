package com.Diagnostic.Spudnik;

import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.preference.PreferenceManager;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import java.util.Objects;

/**
 * @author timothy.bender
 * @version dev1.0.0
 *
 * Welcome to the homescreen activity. This activity's primary job is to direct the user where they want to go.
 */

public class home extends AppCompatActivity {
    private SharedPreferences preferences;
    private int currentMode = 0;
    private Handler handler = new Handler();
    private vehicle myvehicle;
    /**
     * Nothing special in this onCreate. There is a check whether or not the vehicle object's id's have been constructed or not
     * @param savedInstanceState savedInstancestate
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.content_home);
        Toolbar myToolBar = findViewById(R.id.toolbar);
        setSupportActionBar(myToolBar);
        Objects.requireNonNull(getSupportActionBar()).setIcon(R.mipmap.ic_launcher);
        setTitle("Home");
        myToolBar.setTitleTextColor(Color.WHITE);
        Toast.makeText(this, "Welcome!", Toast.LENGTH_SHORT).show(); //Welcome the user
        preferences = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
        myvehicle = getIntent().getParcelableExtra("myvehicle"); //get out parcelabled vehicle object
        if(myvehicle.getVehicleIds() == null) {
            myvehicle.preBuildVehicleObject(this); //try again to prebuild the vehicle ids and dealer names.
        }
    }


    /**
     * Just another day and night toggle
     */
    @Override
    protected void onResume() {
        super.onResume();
        handler.post(new Runnable() {
            @Override
            public void run() {
                boolean nightmode = preferences.getBoolean("nightmode",false);
                int NIGHTMODE = 1, DAYMODE = 2;
                if(nightmode && currentMode != NIGHTMODE){
                    nightMode();
                    currentMode = NIGHTMODE;
                }
                else if(!nightmode && currentMode != DAYMODE){
                    dayMode();
                    currentMode = DAYMODE;
                }
            }
        });
    }

    /**
     * Button Redirect for the Diagnostic Tool button.
     * @param view view
     */
    public void diagTool(View view){
        try {
            FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser(); //get the current firebase user
            if(user != null) { //authentication is required before one can access the diagnostic tool
                Intent i = new Intent(getBaseContext(), inputserial.class);
                i.putExtra("myvehicle",myvehicle); //add the myvehicle object as a parcelable extra in the intent
                startActivity(i);
            }
            else{
                Toast.makeText(this, "Please Sign In", Toast.LENGTH_SHORT).show();
            }
        } catch (Exception ignored) {}

    }

    /**
     * Update Button redirect, currently disabled.
     * @param view view
     */
    public void update(View view){
        try {
            Toast.makeText(this, "Function Not Supported", Toast.LENGTH_SHORT).show();
            //Intent i = new Intent(getBaseContext(), inputserial.class);
            //startActivity(i);
        } catch (Exception ignored) {}

    }

    /**
     * Log Data button redirect, currently disabled
     * @param view view
     */

    public void logData(View view){
        try {
            Toast.makeText(this, "Function Not Supported", Toast.LENGTH_SHORT).show();
            //Intent i = new Intent(getBaseContext(), inputserial.class);
            //startActivity(i);
        } catch (Exception ignored) {}

    }

    /**
     * Settings button redirect, will start the settings activity.
     * @param view view
     */
    public void settings(View view){
        try{
            Intent i = new Intent(getBaseContext(), settings.class);
            startActivity(i);
        }catch(Exception ignored){}

    }


    /**
     * The next two methods will create the toolbar menu item on the top right, this will be on every
     * activity that contains this shortcut.
     * @param item MenuItem
     * @return onOptionsItemSelected(item)
     */
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

    /**
     * Nightmode Toggle
     */
    public void nightMode(){
        handler.post(new Runnable() {
            @Override
            public void run() {
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
            }
        });
    }

    /**
     * DayMode Toggle
     */

    public void dayMode(){
        handler.post(new Runnable() {
            @Override
            public void run() {
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
        });

    }


}
