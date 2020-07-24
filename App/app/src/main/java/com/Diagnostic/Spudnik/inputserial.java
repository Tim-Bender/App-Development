/*
 *
 *  Copyright (c) 2020, Spudnik LLc <https://www.spudnik.com/>
 *  All rights reserved.
 *
 *  Redistribution and use in source and binary forms, with or without
 *  modification, are not permitted in any form.
 *
 *  DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS BE LIABLE FOR
 *  ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 *  (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
 *  LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION, DEATH, or SERIOUS INJURY or DAMAGE)
 *  HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY,
 *  OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 *  SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 *
 */

package com.Diagnostic.Spudnik;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.preference.PreferenceManager;

import com.google.android.material.textfield.TextInputEditText;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;

/**
 * Receive user input (machine id and dealer id) verify them, pull data about the selected machine and then pass to next activity.
 *
 * @author timothy.bender
 * @version dev 1.0.0
 * @since dev 1.0.0
 */

public class inputserial extends AppCompatActivity {
    /**
     * Vehicle object
     */
    public vehicle myvehicle;
    private TextInputEditText serialNumberText;
    private TextInputEditText dealerText;
    private ImageView imageView;
    private TextView textView;
    private InputStreamReader is;
    private SharedPreferences preferences;
    private SharedPreferences.Editor editor;
    private CheckBox checkBox;

    /**
     * Oncreate will do its typical tasks, of assigning instance fields to values, and setting up the toolbar.
     *
     * @param savedInstanceState Bundle
     */
    @SuppressLint("CommitPrefEdits")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_inputserial);

        Toolbar toolbar = findViewById(R.id.topAppBar); //typical toolbar setup
        setSupportActionBar(toolbar);
        setTitle("Input Serial Number");
        toolbar.setTitleTextColor(Color.parseColor("#ffffff"));
        imageView = findViewById(R.id.helpimage);
        imageView.setVisibility(View.GONE);
        textView = findViewById(R.id.helptextview);
        textView.setVisibility(View.GONE);
        preferences = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
        editor = preferences.edit();
        checkBox = findViewById(R.id.rememberdealeridcheckbox);
        //FINAL PRE-DATABASE CHECK ON THE VEHICLE OBJECT
        myvehicle = getIntent().getParcelableExtra("myvehicle");
        myvehicle.preBuildVehicleObject(this); //final prebuild attempt...
    }

    /**
     * Onstart is quite extensive. Here we will do preliminary database construction,
     * Which will construct the list of acceptable dealer ids, and machine ids, and store them appropriately
     * inside of the machine object. We will set event listeners to the two toggle buttons.
     * We will also handle the dealer id "remember" feature here.
     * The whole method will be asynchronous with UI updating aspects being handled by the handler.
     *
     * @since dev 1.0.0
     */
    @Override
    protected void onStart() {
        super.onStart();
        //When a user makes a change to the inputid edit text view, then we will check if the new value is a valid id, and if it is, then we attempt to build our database of connections
        serialNumberText = findViewById(R.id.inputid);
        serialNumberText.setOnKeyListener((v, keyCode, event) -> {
            //if they hit enter, then we will attempt to begin the next activity.
            if ((event.getAction() == KeyEvent.ACTION_DOWN) && (keyCode == KeyEvent.KEYCODE_ENTER)) {
                go(getCurrentFocus());
                return true;
            }
            //any other keystroke will lead to an attempt to build the database
            else if (event.getAction() != KeyEvent.ACTION_DOWN)
                tryBuildDataBaseObject();
            return false;
        });
        //Here we add a keystroke listener to the dealerText edittext field
        dealerText = findViewById(R.id.dealeridtextview);
        dealerText.setOnKeyListener((v, keyCode, event) -> {
            //if the user presses enter, then they will be re-focused onto the input serial number edit text view
            if ((event.getAction() == KeyEvent.ACTION_DOWN) && (keyCode == KeyEvent.KEYCODE_ENTER)) {
                Toast.makeText(inputserial.this, "Enter A Serial Number", Toast.LENGTH_SHORT).show();
                return true;
            }
            return false;
        });
        //Attempt to load a saved dealer id from shared preferences.
        if (!preferences.getString("dealerid", "").equals("")) {
            dealerText.setText(preferences.getString("dealerid", ""));
            checkBox.setChecked(true);
        }

        // Set the oncheckedchange listener for the  "remember" checkbox. If it is checked, then we put the contents
        // of the dealertext edittext view into shared preferences.
        checkBox.setOnCheckedChangeListener((buttonView, isChecked) -> {
            //if checked, save the dealer id
            if (isChecked)
                editor.putString("dealerid", dealerText.getText().toString().trim());
                //else we wipe it
            else
                editor.putString("dealerid", "");
            editor.apply();
        });

        //If there is a dealer id in shared preferences, then we set the checkbox to checked.
        if (!preferences.getString("dealerid", "").equals("")) {
            checkBox.setChecked(true);
            dealerText.setText(preferences.getString("dealerid", ""));
        }

        //Here's the toggle listener for the "Where's my serial number?" toggle
        Switch toggle = findViewById(R.id.helptoggle);
        //heres our background image
        final ImageView spudnikelectrical = findViewById(R.id.spudnikelectrical);
        toggle.setOnCheckedChangeListener((buttonView, isChecked) -> {
            //if it is checked, we swap the views and display the helpful diagram and description
            if (isChecked) {
                spudnikelectrical.setVisibility(View.GONE);
                imageView.setVisibility(View.VISIBLE);
                textView.setVisibility(View.VISIBLE);
            } else {
                //otherwise we show the decoration electrical background
                imageView.setVisibility(View.GONE);
                textView.setVisibility(View.GONE);
                spudnikelectrical.setVisibility(View.VISIBLE);
            }
        });
    }

    /**
     * This method will navigate the user to the connector select screen. It will also ensure that all necessary database objects are constructed.
     * In addition the validity of user inputs is checked here using abstracted methods.
     *
     * @param view view
     * @since dev 1.0.0
     */
    @SuppressLint("SetTextI18n")
    public void go(View view) {
        if (!(serialNumberText.getText().length() < 3) || myvehicle.checkDealer(dealerText.getText().toString().toLowerCase().trim())) {
            if (checkBox.isChecked())                                                           //if "Remember" toggle is enabled then we save the dealer id into sharedpreferences
                editor.putString("dealerid", dealerText.getText().toString().toLowerCase().trim());   //Important to lowercase it and trim whitespace...
            startActivity(new Intent(getApplicationContext(), connectorselect.class)
                    .putExtra("myvehicle", myvehicle)
                    .putParcelableArrayListExtra("connections", myvehicle.getConnections()));
        } else
            serialNumberText.setError("Invalid");
    }

    /**
     * This method will be called anytime a user edits the contents of the inputserial edittext field. The entire process is done asynchronously, with multiple
     * extraneous threads splitting off from the original.
     *
     * @since dev 1.0.0
     */
    public void tryBuildDataBaseObject() {
        AsyncTask.execute(() -> { //Begin a new thread
            try {
                final String vehicleId = serialNumberText.getText().toString().toLowerCase().trim(); //get their inputted vehicle id
                FileInputStream fis2 = new FileInputStream(new File(new File(getFilesDir(), "database"), "_"
                        + myvehicle.determineComparison(vehicleId) + ".csv"));//our fileinputstream
                is = new InputStreamReader(fis2, StandardCharsets.UTF_8); //new inputstreamreader
                myvehicle.setIs(is);        //give the inputstreamreader to our vehicle object
                myvehicle.setVehicleId(vehicleId.toLowerCase().trim());     //set the vehicle id
                myvehicle.buildDataBase();      //initiate database construction
            } catch (Exception ignored) {}
        });
    }

    /**
     * This method is called whenever a menuitem is selected from the toolbar menu.
     *
     * @param item MenuItem
     * @return boolean
     * @since dev 1.0.0
     */
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.action_settings) {       //if the settings button is pressed, we redirect them to the settings page.
            Intent i = new Intent(getBaseContext(), settings.class);
            startActivity(i);
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    /**
     * This just inflates the menu view.
     *
     * @param menu Menu
     * @return boolean
     * @since dev 1.0.0
     */
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.toolbarbuttons, menu);
        return true;
    }

}
