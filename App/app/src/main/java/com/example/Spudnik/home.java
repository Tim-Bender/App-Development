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

import android.os.Handler;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import java.io.InputStreamReader;
import java.util.Objects;

/**
 * Author: Timothy Bender
 * timothy.bender@spudnik.com
 * 530-414-6778
 * Please see README before updating anything
 *
 *
 *
 * Welcome to the homescreen activity. This activity's primary job is to direct the user where they want to go.
 */

public class home extends AppCompatActivity {
    private SharedPreferences preferences;
    private int currentMode = 0;
    private Handler handler = new Handler();
    private vehicle myvehicle;
    private FirebaseUser user;
    private InputStreamReader d;
    /**
     *
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
        Toast.makeText(this, "Welcome!", Toast.LENGTH_SHORT).show();
        preferences = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
        user = FirebaseAuth.getInstance().getCurrentUser();
        myvehicle = getIntent().getParcelableExtra("myvehicle");
        if(myvehicle.getVehicleIds() == null) {
            myvehicle.preBuildVehicleObject(this);
        }
    }


    /**
     * Another night and daymode toggle will be checked in onResume
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
            //Users are required to be authenticated before they may proceed to the tool.
            FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
            if(user != null) {
                Intent i = new Intent(getBaseContext(), inputserial.class);
                i.putExtra("myvehicle",myvehicle);
                startActivity(i);
            }
            else{
                Toast.makeText(this, "Please Sign In", Toast.LENGTH_SHORT).show();
            }
        } catch (Exception ignored) {
        }

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
        } catch (Exception ignored) {
        }

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
        } catch (Exception ignored) {
        }

    }

    /**
     * Settings button redirect, will start the settings activity.
     * @param view view
     */
    public void settings(View view){
        try{
            Intent i = new Intent(getBaseContext(), settings.class);
            startActivity(i);
        }catch(Exception ignored){
        }

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
                try {
                    LinearLayout layout = findViewById(R.id.homelinearlayout);
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
