package com.Diagnostic.Spudnik;

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
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.preference.PreferenceManager;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
/**
 * @author timothy.bender
 * @version dev1.0.0
 *
 * Welcome to the input serial activity. Here the user will input a dealer id, an implement serial number,
 * the validity of both will be checked, and then our all important machine object containing that data will be created.
 */

public class inputserial extends AppCompatActivity {
    public vehicle myvehicle;
    boolean empty = true;
    private EditText serialNumberText,dealerText;
    public ImageView imageView;
    public TextView textView;
    public Switch toggle;
    private InputStreamReader is;
    private SharedPreferences preferences;
    private SharedPreferences.Editor editor;
    private CheckBox checkBox;
    private FirebaseUser user;
    private Handler handler = new Handler();

    /**
     * Oncreate will do its typical tasks, of assigning instance fields to values, and setting up the toolbar.
     * @param savedInstanceState Bundle
     */
    @SuppressLint("CommitPrefEdits")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_inputserial);

        Toolbar toolbar = findViewById(R.id.topAppBar);
        setSupportActionBar(toolbar);
        setTitle("Input Serial Number");
        toolbar.setTitleTextColor(Color.parseColor("#ffffff"));
        imageView = findViewById(R.id.helpimage);
        imageView.setVisibility(View.GONE);
        textView = findViewById(R.id.helptextview);
        textView.setVisibility(View.GONE);
        checkBox = findViewById(R.id.rememberdealeridcheckbox);

        preferences = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
        editor = preferences.edit();
        user = FirebaseAuth.getInstance().getCurrentUser();

       //FINAL PRE-DATABASE CHECK ON THE VEHICLE OBJECT
        myvehicle = getIntent().getParcelableExtra("myvehicle");
            myvehicle.preBuildVehicleObject(this);


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
                            serialNumberText = findViewById(R.id.inputid);
                            serialNumberText.setOnKeyListener(new View.OnKeyListener() {
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
     * This method will navigate the user to the connector select screen. It will also ensure that all necessary database objects are constructed.
     * In addition the validity of user inputs is checked here using abstracted methods.
     * @param view view
     */
    @SuppressLint("SetTextI18n")
    public void go(View view) {
        try {
            empty = true;
            if (!(serialNumberText.getText().length() < 3)) {  //If the contents of the inputserial edittext have a length less than three we will not accept it.
                if (myvehicle.getConnections().isEmpty()) {    //If our vehicle object's connection database has not been built, we do it now
                    myvehicle.preBuildVehicleObject(getApplicationContext()); //Just in case the prebuilding failed, we try it again
                    myvehicle.setIs(is); //set the inputstream for building the database
                    myvehicle.buildDataBase(); //build it
                }
                if (!myvehicle.getConnections().isEmpty() && myvehicle.checkDealer(dealerText.getText().toString().toLowerCase().trim())) { //final check that connections exist and checks if dealer id is valid
                    if (checkBox.isChecked()) {                                                                    //if "Remember" toggle is enabled then we save the dealer id into sharedpreferences
                        editor.putString("dealerid", dealerText.getText().toString().toLowerCase().trim());   //Important to lowercase it and trim whitespace...
                    }
                    TextView errorMessage = findViewById(R.id.inputserialerrorserialtextview);   //Hides the errormessage if it has been displayed.
                    if(errorMessage.getVisibility() == View.VISIBLE) {  //This if statement is actually necessary to avoid a ui glitch.
                        errorMessage.setVisibility(View.INVISIBLE);
                    }
                    Intent i = new Intent(getBaseContext(), connectorselect.class);
                    i.putExtra("myvehicle", myvehicle);                            //add our vehicle object as a parcelable extra.
                    i.putParcelableArrayListExtra("connections", myvehicle.getConnections()); //add the list of connections as a parcelable extra
                    startActivity(i); //go to connectorselect.class
                } else {
                    Toast.makeText(this, "Invalid", Toast.LENGTH_SHORT).show();
                }
            } else {
                if(!serialNumberText.getText().toString().isEmpty()) {  //If necessary display the error message, but don't display it on an empty field error
                    TextView errorMessage = findViewById(R.id.inputserialerrorserialtextview);
                    errorMessage.setVisibility(View.VISIBLE);
                    errorMessage.setText("Not a valid serial number. Try Updating the Database");
                }
                Toast.makeText(this, "Invalid", Toast.LENGTH_SHORT).show();
            }
        } catch (Exception ignored) {}    //this method is fairly error prone due to the massive amount of multithreading, so we will just catch everything.
    }

    /**
     * This method will be called anytime a user edits the contents of the inputserial edittext field. The entire process is done asynchronously, with multiple
     * extraneous threads splitting off from the original.
     */
    public void tryBuildDataBaseObject(){
        AsyncTask.execute(new Runnable() { //Begin a new thread
                @Override
                public void run() {
                    try {
                        final String vehicleId = serialNumberText.getText().toString().toLowerCase().trim(); //get their inputted vehicle id
                        if (vehicleId.length() > 2) {          //It has to be at least 3 long for us to accept it
                            final String determined = myvehicle.determineComparison(vehicleId); //Determine the most likely vehicle id to match with. See myvehicle's documentation for this function.
                            if (myvehicle.getVehicleIds().contains(determined)) { //If the returned vehicleid from determineComparison() is valid, then we proceed
                                if (user != null) { //user must be authenticated
                                    final File rootpath = new File(getFilesDir(), "database"); //rootpath to the database folder
                                    File localFile = new File(rootpath, "_" + determined + ".csv"); //We need to re-add the _ and .csv to the name of the file. Pointer to file we will reading from
                                    FileInputStream fis2 = new FileInputStream(localFile);//our fileinputstream
                                    is = new InputStreamReader(fis2, StandardCharsets.UTF_8); //new inputstreamreader

                                    myvehicle.setIs(is);        //give the inputstreamreader to our vehicle object
                                    myvehicle.setVehicleId(vehicleId.toLowerCase().trim());     //set the vehicle id
                                    myvehicle.buildDataBase();      //initiate database construction
                                }
                            }
                        }
                    } catch (Exception ignored) {}
                }
            });
        }

    /**
     * This method is called whenever a menuitem is selected from the toolbar menu.
     * @param item MenuItem
     * @return boolean
     */
    @Override
    public boolean onOptionsItemSelected(MenuItem item){
        if(item.getItemId() == R.id.action_settings){       //if the settings button is pressed, we redirect them to the settings page.
            Intent i = new Intent(getBaseContext(),settings.class);
            startActivity(i);
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    /**
     * This just inflates the menu view.
     * @param menu Menu
     * @return boolean
     */
    public boolean onCreateOptionsMenu(Menu menu){
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.toolbarbuttons,menu);
        return true;
    }

}
