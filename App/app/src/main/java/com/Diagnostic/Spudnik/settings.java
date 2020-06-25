package com.Diagnostic.Spudnik;

import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.Point;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.view.Display;
import android.view.View;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.LinearLayout;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.preference.PreferenceManager;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
/**
 * Author: Timothy Bender
 * timothy.bender@spudnik.com
 * 530-414-6778
 * Please see README before updating anything
 *
 *
 * Welcome to the Settings activity.
 */
public class settings extends AppCompatActivity {

    private Switch aSwitch;
    private boolean nightmode = false;
    private SharedPreferences preferences;
    private SharedPreferences.Editor editor;
    private FirebaseDatabase firebaseDatabase;
    private int currentMode = 0;
    private Handler handler = new Handler();

    /**
     * Only thing out of the ordinary here in onCreate would be the switch's OnCheckedChangeListener.
     * This will toggle night and day mode for the entire app by pushing a boolean value into permanent storage via
     * shared preferences
     * @param savedInstanceState Bundle
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        Toolbar myToolBar = findViewById(R.id.toolbar);
        setSupportActionBar(myToolBar);
        getSupportActionBar().setIcon(R.mipmap.ic_launcher);
        setTitle("Settings");
        myToolBar.setTitleTextColor(Color.WHITE);

        aSwitch = findViewById(R.id.settingsToggle);
        preferences = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
        editor = preferences.edit();
        firebaseDatabase = FirebaseDatabase.getInstance();
        nightmode = preferences.getBoolean("nightmode",false);
        //set a listener to the nightmode switch button.
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
        if(nightmode){
            aSwitch.setChecked(true);
        }

    }

    /**
     * Check if its in day or night mode.
     */

    @Override
    public void onResume(){
        super.onResume();
        handler.post(new Runnable() {
            @Override
            public void run() {
                nightmode = preferences.getBoolean("nightmode",false);
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
     * Report a bug button redirect, create an email with auto-filled fields
     * @param view view
     */

    public void reportBug(View view){
        try{
            DatabaseReference reference = firebaseDatabase.getReference("settings").child("reportemail");
            reference.addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                    String email = dataSnapshot.getValue(String.class);
                    //get screen size
                    Display display = getWindowManager().getDefaultDisplay();
                    Point size = new Point();
                    display.getSize(size);
                    int width = size.x;
                    int height = size.y;
                    //create an email intent and fill in necessary information
                    Intent emailIntent = new Intent(Intent.ACTION_SEND);
                    emailIntent.setType("message/rfc822");
                    emailIntent.putExtra(Intent.EXTRA_EMAIL, new String[]{email});
                    emailIntent.putExtra(Intent.EXTRA_SUBJECT,"Bug report Diagnostic Tool");
                    emailIntent.putExtra(Intent.EXTRA_TEXT, "Device: " + Build.DEVICE + " \nScreenSize:" + height +" x "+ width + "\nAndroid Version: " +
                            Build.VERSION.CODENAME + " " + Build.VERSION.RELEASE + "\n\nPlease describe the bug in detail:\n");
                    //start the intent and start an email
                    startActivity(Intent.createChooser(emailIntent,"Send mail..."));

                }

                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {

                }
            });

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * Its literally the same function as above. With a different email subject...
     * @param view view
     */

    public void submitFeedback(View view){
        try{
            DatabaseReference reference = firebaseDatabase.getReference("settings").child("reportemail");
            reference.addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                    String email = dataSnapshot.getValue(String.class);
                    Intent emailIntent = new Intent(Intent.ACTION_SEND);
                    emailIntent.setType("message/rfc822");

                    emailIntent.putExtra(Intent.EXTRA_EMAIL, new String[]{email});
                    emailIntent.putExtra(Intent.EXTRA_SUBJECT,"Feedback Diagnostic Tool");
                    emailIntent.putExtra(Intent.EXTRA_TEXT, "Device: " + Build.DEVICE + "\nAndroid Version: " +
                            Build.VERSION.CODENAME + " " + Build.VERSION.RELEASE + "\n\nComments: \n");
                    startActivity(Intent.createChooser(emailIntent,"Send mail..."));
                }

                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {

                }
            });
        } catch (Exception ignored) {
        }
    }

    /**
     * Update DataBase button redirect, for a description see MainActivity's comments, its the exact same function.
      * @param view view
     */

    public void updateDataBase(View view){
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if(user != null)
        new UpdateDatabase(this);
        else
            Toast.makeText(this, "Please LogIn", Toast.LENGTH_SHORT).show();
    }


    /**
     * Login button redirect
     * @param view view
     */
    public void login(View view){
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        //if the user hasn't been authenticated, then we pass them to the login activity
        if(user == null) {
            Intent i = new Intent(getBaseContext(), LoginActivity.class);
            i.putExtra("fromsettings", true);
            startActivity(i);
        }
        //otherwise we assume it was a mistake
        else{
            Toast.makeText(this, "Already Logged In", Toast.LENGTH_SHORT).show();
        }

    }

    /**
     * Log out button redirect
     * @param view view
     */
    public void logout(View view){
        //log the user out.
       FirebaseAuth auth = FirebaseAuth.getInstance();
       FirebaseUser user = auth.getCurrentUser();
       //if logged in, log out
       if(user != null) {
           auth.signOut();
           Toast.makeText(this, "Signed Out", Toast.LENGTH_SHORT).show();
       }
       //else we assume it was a mistake.
       else{
           Toast.makeText(this, "Already Signed Out", Toast.LENGTH_SHORT).show();
       }
    }

    /**
     * DEV MODE FEATURE, WILL BE REMOVED LATER
     * @param view view
     */
    public void testBluetooth(View view){
        try{
            Intent i = new Intent(getBaseContext(), BluetoothTestActivity.class);
            startActivity(i);
        }catch(Exception ignored){}
    }

    /**
     * NightMode Toggle
     */
    public void nightMode(){
        handler.post(new Runnable() {
            @Override
            public void run() {
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
                button = findViewById(R.id.settingsloginbutton);
                button.setBackgroundResource(R.drawable.nightmodebuttonselector);
                button.setTextColor(Color.WHITE);
                button = findViewById(R.id.settingslogoutbutton);
                button.setBackgroundResource(R.drawable.nightmodebuttonselector);
                button.setTextColor(Color.WHITE);
                aSwitch.setTextColor(Color.WHITE);
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
                button = findViewById(R.id.settingsloginbutton);
                button.setBackgroundResource(R.drawable.daymodebuttonselector);
                button.setTextColor(Color.BLACK);
                button = findViewById(R.id.settingslogoutbutton);
                button.setBackgroundResource(R.drawable.daymodebuttonselector);
                button.setTextColor(Color.BLACK);
                aSwitch.setTextColor(Color.BLACK);
            }
        });

    }



}
