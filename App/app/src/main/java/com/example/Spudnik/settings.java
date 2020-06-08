package com.example.Spudnik;

import androidx.appcompat.app.AppCompatActivity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.Point;
import android.os.Build;
import android.os.Bundle;
import android.view.Display;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.LinearLayout;
import android.widget.Switch;
import android.widget.TextView;
import androidx.appcompat.widget.Toolbar;
import androidx.core.content.ContextCompat;
import androidx.preference.PreferenceManager;

public class settings extends AppCompatActivity {
    private Switch aSwitch;
    boolean nightmode = false;
    private SharedPreferences.Editor editor;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);
        Toolbar myToolBar = findViewById(R.id.toolbar);
        setSupportActionBar(myToolBar);
        setTitle("Settings");
        myToolBar.setTitleTextColor(Color.WHITE);
        aSwitch = findViewById(R.id.settingsToggle);
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
        editor = sharedPreferences.edit();
        aSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if(isChecked && !nightmode){
                    nightmode = true;
                    nightMode();
                    editor.putBoolean("nightmode",true);
                    editor.apply();
                }
                if(!isChecked && nightmode){
                    nightmode = false;
                    dayMode();
                    editor.putBoolean("nightmode",false);
                    editor.commit();
                }
            }
        });
        if(sharedPreferences.getBoolean("nightmode",false)){
            nightMode();
            aSwitch.setChecked(true);
        }

    }

     @Override
     public void onStart(){
        super.onStart();
     }

    public void reportBug(View view){
        try{
            Display display = getWindowManager().getDefaultDisplay();
            Point size = new Point();
            display.getSize(size);
            int width = size.x;
            int height = size.y;

            Intent emailIntent = new Intent(Intent.ACTION_SEND);
            emailIntent.setType("message/rfc822");
            emailIntent.putExtra(Intent.EXTRA_EMAIL, new String[]{"timothy.bender@spudnik.com"});
            emailIntent.putExtra(Intent.EXTRA_SUBJECT,"Bug report Diagnostic Tool");
            emailIntent.putExtra(Intent.EXTRA_TEXT, "Device: " + Build.DEVICE + " \nScreenSize:" + height +" x "+ width + "\nAndroid Version: " +
                                                    Build.VERSION.CODENAME + " " + Build.VERSION.RELEASE + "\n\nPlease describe the bug in detail:\n");
            startActivity(Intent.createChooser(emailIntent,"Send mail..."));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void submitFeedback(View view){
        try{
            Display display = getWindowManager().getDefaultDisplay();
            Point size = new Point();
            display.getSize(size);
            int width = size.x;
            int height = size.y;

            Intent emailIntent = new Intent(Intent.ACTION_SEND);
            emailIntent.setType("message/rfc822");
            //emailIntent.setData(Uri.parse("mailto:"));
            emailIntent.putExtra(Intent.EXTRA_EMAIL, new String[]{"timothy.bender@spudnik.com"});
            emailIntent.putExtra(Intent.EXTRA_SUBJECT,"Feedback Diagnostic Tool");
            emailIntent.putExtra(Intent.EXTRA_TEXT, "Device: " + Build.DEVICE + " \nScreenSize:" + height +" x "+ width + "\nAndroid Version: " +
                    Build.VERSION.CODENAME + " " + Build.VERSION.RELEASE + "\n\nPlease describe the bug in detail:\n");
            startActivity(Intent.createChooser(emailIntent,"Send mail..."));
        } catch (Exception e) {
            e.printStackTrace();
        }}



    public void nightMode(){
        try {
            LinearLayout layout = findViewById(R.id.settingsbackground);
            layout.setBackgroundColor(Color.parseColor("#333333"));
            TextView textView = findViewById(R.id.welcometosettingstextview);
            textView.setTextColor(Color.WHITE);
            Button button = findViewById(R.id.updatedatabasebutton);
            button.setBackgroundResource(R.drawable.nightmodebuttonselector);
            button.setTextColor(Color.WHITE);
            button = findViewById(R.id.reportbugbutton);
            button.setBackgroundResource(R.drawable.nightmodebuttonselector);
            button.setTextColor(Color.WHITE);
            button = findViewById(R.id.reportfeedback);
            button.setBackgroundResource(R.drawable.nightmodebuttonselector);
            button.setTextColor(Color.WHITE);
            button = findViewById(R.id.settingsbluetoothtestbutton);
            button.setBackgroundResource(R.drawable.nightmodebuttonselector);
            button.setTextColor(Color.WHITE);
            aSwitch.setTextColor(Color.WHITE);
        } catch (Exception ignored) {}

    }

    public void dayMode(){
        try {
            LinearLayout layout = findViewById(R.id.settingsbackground);
            layout.setBackgroundColor(Color.WHITE);
            TextView textView = findViewById(R.id.welcometosettingstextview);
            textView.setTextColor(Color.BLACK);
            Button button = findViewById(R.id.updatedatabasebutton);
            button.setBackgroundResource(R.drawable.daymodebuttonselector);
            button.setTextColor(Color.BLACK);
            button = findViewById(R.id.reportbugbutton);
            button.setBackgroundResource(R.drawable.daymodebuttonselector);
            button.setTextColor(Color.BLACK);
            button = findViewById(R.id.reportfeedback);
            button.setBackgroundResource(R.drawable.daymodebuttonselector);
            button.setTextColor(Color.BLACK);
            button = findViewById(R.id.settingsbluetoothtestbutton);
            button.setBackgroundResource(R.drawable.daymodebuttonselector);
            button.setTextColor(Color.BLACK);
            aSwitch.setTextColor(Color.BLACK);
        }catch(Exception ignored){}

    }

    public void testBluetooth(View view){
        try{
            Intent i = new Intent(getBaseContext(), BluetoothTestActivity.class);
            startActivity(i);
        }catch(Exception ignored){}
    }
}
