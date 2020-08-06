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
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.IBinder;
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

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.preference.PreferenceManager;

import com.Diagnostic.Spudnik.Bluetooth.BluetoothLeService;
import com.Diagnostic.Spudnik.CustomObjects.Vehicle;
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

@SuppressWarnings("unused")
enum DealerIds {
    GENAG("GENAG"),
    HJVPEI("HJVPEI"),
    HJVNB("HJVNB"),
    AFE("AFE"),
    GROWERS("GROWERS"),
    RDO("RDO"),
    LENCOW("LENCOW"),
    SGRAF("SGRAF"),
    SHEYB("SHEYD"),
    SBLACK("SBLACK"),
    SPRESQ("SPRESQ"),
    SPUD("SPUD");
    public String dealer;

    DealerIds(String dealer) {
        this.dealer = dealer;
    }
}

public class InputSerial extends AppCompatActivity {
    /**
     * Vehicle object
     */
    public Vehicle myvehicle;
    private TextInputEditText serialNumberText;
    private TextInputEditText dealerText;
    private ImageView imageView;
    private TextView textView;
    private InputStreamReader is;
    private SharedPreferences preferences;
    private SharedPreferences.Editor editor;
    private CheckBox checkBox;
    private InputSerial.BuildVehicleBroadcastListener receiver;
    private boolean bounded = false;

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
        myvehicle = new Vehicle();
        IntentFilter filter = new IntentFilter();
        filter.addAction(Vehicle.buildDoneAction);
        receiver = new InputSerial.BuildVehicleBroadcastListener();
        registerReceiver(receiver, filter);
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
        //When a user makes a change to the inputid edit text view, then we will check if the new value is a valid id, and if it is, then we attempt to build our database of connections
        serialNumberText = findViewById(R.id.inputid);
        serialNumberText.setOnKeyListener((v, keyCode, event) -> {
            //if they hit enter, then we will attempt to begin the next activity.
            if ((event.getAction() == KeyEvent.ACTION_DOWN) && (keyCode == KeyEvent.KEYCODE_ENTER)) {
                go(getCurrentFocus());
                return true;
            }
            return false;
        });

        //Here we add a keystroke listener to the dealerText edittext field
        dealerText = findViewById(R.id.dealeridtextview);
        dealerText.setOnKeyListener((v, keyCode, event) -> {
            //if the user presses enter, then they will be re-focused onto the input serial number edit text view
            if ((event.getAction() == KeyEvent.ACTION_DOWN) && (keyCode == KeyEvent.KEYCODE_ENTER)) {
                Toast.makeText(InputSerial.this, "Enter A Serial Number", Toast.LENGTH_SHORT).show();
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
        findViewById(R.id.inputserialnextbutton).setOnClickListener(this::go);
        Intent mIntent = new Intent(this, BluetoothLeService.class);
        bindService(mIntent, mConnection, BIND_AUTO_CREATE);
        super.onStart();
    }

    /**
     * This method will navigate the user to the connector select screen. It will also ensure that all necessary database objects are constructed.
     * In addition the validity of user inputs is checked here using abstracted methods.
     *
     * @since dev 1.0.0
     */
    @SuppressLint("SetTextI18n")
    public void go(View view) {
        tryBuildDataBaseObject();
    }

    /**
     * This method will be called anytime a user edits the contents of the inputserial edittext field. The entire process is done asynchronously, with multiple
     * extraneous threads splitting off from the original.
     *
     * @since dev 1.0.0
     */
    public synchronized void tryBuildDataBaseObject() {
        AsyncTask.execute(() -> {
            try {
                final String vehicleId = serialNumberText.getText().toString().toLowerCase().trim(); //get their inputted vehicle id
                if (vehicleId.length() > 2) {          //It has to be at least 3 long for us to accept it
                    final String determined = determineVehicleFile(vehicleId); //Determine the most likely vehicle id to match with. See myvehicle's documentation for this function.
                    File localFile = new File(new File(getFilesDir(), "database"), "_" + determined + ".csv"); //We need to re-add the _ and .csv to the name of the file. Pointer to file we will reading from
                    FileInputStream fis2 = new FileInputStream(localFile);//our fileinputstream
                    is = new InputStreamReader(fis2, StandardCharsets.UTF_8); //new inputstreamreader
                    myvehicle.setIs(is);        //give the inputstreamreader to our vehicle object
                    myvehicle.setVehicleId(vehicleId.toLowerCase().trim());     //set the vehicle id
                    myvehicle.buildDataBase(getApplicationContext());      //initiate database construction
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        });
    }

    ServiceConnection mConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            System.out.println("SERVICE CONNECTED");
            //BluetoothLeService.LocalBinder mLocalBinder = ((BluetoothLeService.LocalBinder) service);
            bounded = true;
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            System.out.println("SERVICE DISCONNECTED");
            bounded = false;
        }
    };

    @Override
    protected void onDestroy() {
        if (receiver != null)
            unregisterReceiver(receiver);
        if (bounded) {
            unbindService(mConnection);
            bounded = false;
        }
        super.onDestroy();
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
            Intent i = new Intent(getBaseContext(), Settings.class);
            startActivity(i);
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    public boolean checkDealer(@NonNull String dealerid) {
        for (DealerIds d : DealerIds.values()) {
            if (dealerid.equals(d.dealer.toLowerCase().trim()))
                return true;
        }
        return false;
    }

    private class BuildVehicleBroadcastListener extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (intent.getIntExtra("success", 0) == Vehicle.BUILD_SUCCESSFUL) {
                if (!(serialNumberText.getText().length() < 3)) {
                    if (checkDealer(dealerText.getText().toString().toLowerCase().trim())) {
                        if (checkBox.isChecked())                                                           //if "Remember" toggle is enabled then we save the dealer id into sharedpreferences
                            editor.putString("dealerid", dealerText.getText().toString().toLowerCase().trim());   //Important to lowercase it and trim whitespace...
                        startActivity(new Intent(getApplicationContext(), ConnectorSelect.class)
                                .putExtra("myvehicle", myvehicle)
                                .putParcelableArrayListExtra("connections", myvehicle.getPins()));
                    } else
                        dealerText.setError("Invalid");
                } else
                    serialNumberText.setError("Invalid");
            }
        }
    }


    /**
     * This method will determine the closest match vehicle id from the id that was entered. It will then return that id.
     *
     * @param machineid MachineId
     * @return String
     * @since dev 1.0.0
     */
    public String determineVehicleFile(@NonNull String machineid) {
        String[] vehicleIds= preferences.getString("machineIds", "").split(",");
        String toReturn = "null"; //default return if we dont match with anything
        int maximum = Integer.MIN_VALUE;
        if (machineid.length() > 0 && vehicleIds.length != 0) {
            for (String id : vehicleIds) { //we will iterate through the ids
                char[] storage = machineid.toCharArray(), //cast the two into char arrays
                        idCharArray = id.toCharArray();
                int points = 0; //higher points of comparison the better for the id
                if (idCharArray[0] != storage[0] || storage.length != idCharArray.length) //if the first letters are not the same, or they aren't the same length. Skip it
                    continue;
                for (int i = 0; i < idCharArray.length; i++) { //iterate through every character
                    if (i < storage.length) {
                        if (storage[i] == idCharArray[i]) //if theres a character match the id earns a point
                            points++;
                        else if (idCharArray[i] != 'x' && idCharArray[i] != 'X') //if they dont match, but we are comparing against an x or an X then they don't lose a point
                            points--; //take a point away.
                    }
                }
                if (maximum < points) { //if we have a new maximum match set the variables
                    maximum = points;
                    toReturn = id;
                }
            }
            if (maximum <= 0) //if we have 0 or fewer comparison points, then there was no match
                return toReturn;
        }
        return toReturn;
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
