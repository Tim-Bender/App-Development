package com.example.Spudnik;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.preference.PreferenceManager;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
/**
 * Author: Timothy Bender
 * timothy.bender@spudnik.com
 * 530-414-6778
 * Please see README before updating anything
 *
 *
 * Welcome to the input serial activity. Here the user will input a dealer id, an implement serial number,
 * the validity of both will be checked, and then our all important machine object containing that data will be created.
 */

public class inputserial extends AppCompatActivity {
    public vehicle myvehicle;
    boolean empty = true;
    private EditText edittext,dealerText;
    public ImageView imageView;
    public TextView textView;
    public Switch toggle;
    private InputStreamReader is;
    private SharedPreferences preferences;
    private SharedPreferences.Editor editor;
    private CheckBox checkBox;
    private FirebaseUser user;
    private int currentMode = 0;
    private Handler handler = new Handler();

    /**
     * Oncreate will do its typical tasks, of assigning instance fields to values, and setting up the toolbar.
     * @param savedInstanceState Bundle
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_inputserial);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setIcon(R.mipmap.ic_launcher);
        setTitle("Input Serial Numer");
        getSupportActionBar().setIcon(R.mipmap.ic_launcher);
        toolbar.setTitleTextColor(Color.WHITE);

        imageView = findViewById(R.id.helpimage);
        imageView.setVisibility(View.GONE);
        textView = findViewById(R.id.helptextview);
        textView.setVisibility(View.GONE);
        checkBox = findViewById(R.id.rememberdealeridcheckbox);

        preferences = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
        editor = preferences.edit();
        user = FirebaseAuth.getInstance().getCurrentUser();

        //VEHICLE CREATION
        myvehicle = getIntent().getParcelableExtra("myvehicle");
        if(myvehicle.getVehicleIds() == null){
            myvehicle.preBuildVehicleObject(this);
        }

    }

    /**
     * Onstart is quite extensive. Here we will do preliminary database construction,
     * Which will construct the list of acceptable dealer ids, and machine ids, and store them appropriately
     * inside of the machine object. We will set event listeners to the two toggle buttons.
     * We will also handle the dealer id "remember" feature here.
     */
    @Override
    protected void onStart(){
        super.onStart();
        AsyncTask.execute(new Runnable() {
            @Override
            public void run() {
                try {
                    handler.post(new Runnable() {
                        @Override
                        public void run() {
                            //When a user makes a change to the inputid edit text view, then we will check if the new value is a valid id, and if it is, then we attempt to build our database of connections
                            edittext = findViewById(R.id.inputid);
                            edittext.setOnKeyListener(new View.OnKeyListener() {
                                @Override
                                public boolean onKey(View v, int keyCode, KeyEvent event) {
                                    //if they hit enter, then we will attempt to begin the next activity.
                                    if((event.getAction() == KeyEvent.ACTION_DOWN) && (keyCode == KeyEvent.KEYCODE_ENTER)){
                                        go(getCurrentFocus());
                                        return true;
                                    }
                                    //any other keystroke will lead to an attempt to build the database
                                    else {
                                        if(event.getAction() != KeyEvent.ACTION_DOWN)
                                            tryBuildDataBaseObject();
                                    }

                                    return false;
                                }
                            });

                            //Here we add a keystroke listener to the dealerText edittext field
                            dealerText = findViewById(R.id.dealeridtextview);
                            dealerText.setOnKeyListener(new View.OnKeyListener() {
                                @Override
                                public boolean onKey(View v, int keyCode, KeyEvent event) {
                                    //if the user presses enter, then they will be re-focused onto the input serial number edit text view
                                    if((event.getAction() == KeyEvent.ACTION_DOWN) && (keyCode == KeyEvent.KEYCODE_ENTER)){
                                        Toast.makeText(inputserial.this, "Enter A Serial Number", Toast.LENGTH_SHORT).show();
                                        return true;
                                    }
                                    return false;
                                }
                            });

                            //Attempt to load a saved dealer id from shared preferences.
                            if(!preferences.getString("dealerid", "").equals("")){
                                dealerText.setText(preferences.getString("dealerid",""));
                                checkBox.setChecked(true);
                            }

                            // Set the oncheckedchange listener for the  "remember" checkbox. If it is checked, then we put the contents
                            // of the dealertext edittext view into shared preferences.
                            checkBox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                                @Override
                                public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                                    //if checked, save the dealer id
                                    if(isChecked){
                                        editor.putString("dealerid", dealerText.getText().toString().trim());
                                    }
                                    //else we wipe it
                                    else{
                                        editor.putString("dealerid","");
                                    }
                                    editor.apply();
                                }
                            });

                            //If there is a dealer id in shared preferences, then we set the checkbox to checked.
                            if(!preferences.getString("dealerid", "").equals("")){
                                checkBox.setChecked(true);
                                dealerText.setText(preferences.getString("dealerid",""));
                            }

                            //Here's the toggle listener for the "Where's my serial number?" toggle
                            toggle = findViewById(R.id.helptoggle);
                            //heres our background image
                            final ImageView spudnikelectrical = findViewById(R.id.spudnikelectrical);
                            toggle.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                                @Override
                                public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                                    //if it is checked, we swap the views and display the helpful diagram and description
                                    if(isChecked){
                                        spudnikelectrical.setVisibility(View.GONE);
                                        imageView.setVisibility(View.VISIBLE);
                                        textView.setVisibility(View.VISIBLE);

                                    }
                                    else{
                                        //otherwise we show the decoration electrical background
                                        imageView.setVisibility(View.GONE);
                                        textView.setVisibility(View.GONE);
                                        spudnikelectrical.setVisibility(View.VISIBLE);

                                    }
                                }
                            });
                        }
                    });
                }catch(Exception ignored){}
            }
        });


    }

    /**
     * Another check onResume for nightmode or daymode toggles
     */
    @Override
    protected void onResume() {
        super.onResume();
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


    /**
     *
     * @param view view
     */
    @SuppressLint("SetTextI18n")
    public void go(View view) {
        try {
            empty = true;
            String vehicleId = edittext.getText().toString();
            if (!(edittext.getText().length() < 3)) {
                if (myvehicle.getConnections().isEmpty()) {
                    myvehicle = new vehicle(vehicleId);
                    myvehicle.setIs(is);
                    myvehicle.buildDataBase();
                }
                if (!myvehicle.getConnections().isEmpty() && myvehicle.checkDealer(dealerText.getText().toString().toLowerCase().trim())) {
                    if (checkBox.isChecked()) {
                        editor.putString("dealerid", dealerText.getText().toString().toLowerCase().trim());
                    }
                    TextView errorMessage = findViewById(R.id.inputserialerrorserialtextview);
                    if(errorMessage.getVisibility() == View.VISIBLE) {
                        errorMessage.setVisibility(View.INVISIBLE);
                    }
                    Intent i = new Intent(getBaseContext(), connectorselect.class);
                    i.putExtra("myvehicle", myvehicle);
                    i.putParcelableArrayListExtra("connections", myvehicle.getConnections());
                    startActivity(i);
                } else {
                    Toast.makeText(this, "Invalid", Toast.LENGTH_SHORT).show();
                }
            } else {
                if(!edittext.getText().toString().isEmpty()) {
                    TextView errorMessage = findViewById(R.id.inputserialerrorserialtextview);
                    errorMessage.setVisibility(View.VISIBLE);
                    errorMessage.setText("Not a valid serial number. Try Updating the Database");
                }
                Toast.makeText(this, "Invalid", Toast.LENGTH_SHORT).show();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void tryBuildDataBaseObject(){
        AsyncTask.execute(new Runnable() {
                @Override
                public void run() {
                    try {
                        final String vehicleId = edittext.getText().toString().toLowerCase().trim();
                        FileInputStream fis2;
                        if (vehicleId.length() > 2) {
                            final String determined = myvehicle.determineComparison(vehicleId);
                            System.out.println("DETERMINED: " + determined);
                            if (myvehicle.getVehicleIds().contains(determined)) {
                                if (user != null) {
                                    final File rootpath = new File(getFilesDir(), "database");
                                    File localFile = new File(rootpath, "_" + determined + ".csv");
                                    fis2 = new FileInputStream(localFile);
                                    is = new InputStreamReader(fis2, StandardCharsets.UTF_8);

                                    myvehicle.setIs(is);
                                    myvehicle.setVehicleId(vehicleId.toLowerCase().trim());
                                    myvehicle.buildDataBase();
                                }
                            }
                        }
                    } catch (Exception ignored) {
                    }
                }
            });
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
        handler.post(new Runnable() {
            @Override
            public void run() {
                ConstraintLayout constraintLayout = findViewById(R.id.inputserialconstraintlayout);
                constraintLayout.setBackgroundColor(Color.parseColor("#333333"));
                TextView view = findViewById(R.id.inputserialtextview1);
                view.setTextColor(Color.WHITE);
                view = findViewById(R.id.inputserialtextview2);
                view.setTextColor(Color.WHITE);
                view = findViewById(R.id.helptextview);
                view.setTextColor(Color.WHITE);
                EditText editText = findViewById(R.id.dealeridtextview);
                editText.setTextColor(Color.WHITE);
                editText = findViewById(R.id.inputid);
                editText.setTextColor(Color.WHITE);
                CheckBox checkBox = findViewById(R.id.rememberdealeridcheckbox);
                checkBox.setTextColor(Color.WHITE);
                Switch myswitch = findViewById(R.id.helptoggle);
                myswitch.setTextColor(Color.WHITE);
                Button button = findViewById(R.id.gobutton);
                button.setBackgroundResource(R.drawable.nightmodebuttonselector);
                button.setTextColor(Color.WHITE);
            }
        });
    }


    public void dayMode() {
        handler.post(new Runnable() {
            @Override
            public void run() {
                ConstraintLayout constraintLayout = findViewById(R.id.inputserialconstraintlayout);
                constraintLayout.setBackgroundColor(Color.WHITE);
                TextView view = findViewById(R.id.inputserialtextview1);
                view.setTextColor(Color.BLACK);
                view = findViewById(R.id.inputserialtextview2);
                view.setTextColor(Color.BLACK);
                view = findViewById(R.id.helptextview);
                view.setTextColor(Color.BLACK);
                EditText editText = findViewById(R.id.dealeridtextview);
                editText.setTextColor(Color.BLACK);
                editText = findViewById(R.id.inputid);
                editText.setTextColor(Color.BLACK);
                CheckBox checkBox = findViewById(R.id.rememberdealeridcheckbox);
                checkBox.setTextColor(Color.BLACK);
                Switch myswitch = findViewById(R.id.helptoggle);
                myswitch.setTextColor(Color.BLACK);
                Button button = findViewById(R.id.gobutton);
                button.setBackgroundResource(R.drawable.daymodebuttonselector);
                button.setTextColor(Color.BLACK);
            }
        });

    }

}
