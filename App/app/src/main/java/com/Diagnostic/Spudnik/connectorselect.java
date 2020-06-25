package com.Diagnostic.Spudnik;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Spinner;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.preference.PreferenceManager;

import java.util.List;
import java.util.Objects;

/**
 * @author timothy.bender
 * @version dev1.0.0
 * Please see README before updating anything
 *
 *
 * Welcome to the conenctorselect class. This activity's primary job is to allow populate the list of connectors,
 * then allow the user to pick one to begin viewing diagnostics for.
 */
public class connectorselect extends AppCompatActivity {
    private vehicle myvehicle;
    private EditText connectorSelectionEdittext;
    private SharedPreferences preferences;
    private int currentMode = 0;
    private Handler handler = new Handler();

    /**
     * Just your normal onCreate.
     * @param savedInstanceState Bundle
     */
    @SuppressLint("SetTextI18n")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.content_connectorselect);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        toolbar.setTitleTextColor(Color.WHITE);
        setTitle("Connector Selection");
        Objects.requireNonNull(getSupportActionBar()).setIcon(R.mipmap.ic_launcher);
        preferences = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
        myvehicle = getIntent().getParcelableExtra("myvehicle"); //get our vehicle object as a parcelable extra
        Objects.requireNonNull(this.myvehicle).setConnections(getIntent().<connection>getParcelableArrayListExtra("connections")); //get our list of connections as a parcelable extra
    }


    /**
     * Quite an interesting onStart here. We begin by setting up an arrayadapter for our spinner, and then setting a keylistener to our edittext field.
     */
    @SuppressLint("SetTextI18n")
    @Override
    protected void onStart(){
        super.onStart();
        boolean nightMode = preferences.getBoolean("nightmode",false); //Determine whether or not we are in nightmode
        Spinner mySpinner = findViewById(R.id.myconnectorspinner); //get our spinner view
        try {
            List<String> connections = myvehicle.getUniqueConnections(); //unique connections is a list of all the unique connections on a machine. We will use it to populate the spinner
            ArrayAdapter<String> dataAdapter;
            dataAdapter = (nightMode) ? new ArrayAdapter<>(this, R.layout.spinner_item_night, connections) :
                    new ArrayAdapter<>(this, R.layout.spinner_item_day, connections); //This is quite complicated so here we go. There are two separate arrayadapter options we have depending on
                                                                                             //whether or not we are in nightmode.. There are two separate spinner item layouts that we can choose from.
                                                                                             //So we use a ternary operator to determine which item should be used.
            dataAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
            mySpinner.setAdapter(dataAdapter);          //assign our new adapter to the spinner
            connectorSelectionEdittext = findViewById(R.id.connectorinput);
            connectorSelectionEdittext.setOnKeyListener(new View.OnKeyListener() { //set an onkeylistener in our connectiorselect edittext. If we detect a down+enter then we move the current focus
                @Override
                public boolean onKey(View v, int keyCode, KeyEvent event) {
                    if ((event.getAction() == KeyEvent.ACTION_DOWN) && (keyCode == KeyEvent.KEYCODE_ENTER)) {
                        next(getCurrentFocus());
                        return true;
                    }

                    return false;
                }
            });

            mySpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() { //Set the item selected listener on the spinner.
                @Override
                public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                    Object item = parent.getItemAtPosition(position);
                    String temp = item.toString();
                    String s1 = temp.substring(0, 1).toUpperCase();
                    connectorSelectionEdittext.setText(s1 + temp.substring(1)); //update the connectorselect edittext field with the selected item
                    myvehicle.setLoc(position);     //This will be used to track which one we are looking at.
                }
                @Override
                public void onNothingSelected(AdapterView<?> parent) {}
            });
        } catch (Exception ignored) {}


    }

    /**
     * Just another day and nightmode toggle in onResume
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
     * Button redirect for the "down" button.
     * @param view View
     */
    @SuppressLint("SetTextI18n")
    public void down(View view) {
        try {
            if (!myvehicle.getUniqueConnections().isEmpty()) { //Check that unique connections is not empty
                myvehicle.setLoc(myvehicle.getLoc() + 1);   //add one to the location tracking variable
                if (myvehicle.getLoc() == myvehicle.getUniqueConnections().size()) { //if we've reached the end of the list, go to the front
                    myvehicle.setLoc(0);
                }
                String temp = myvehicle.getUniqueConnections().get(myvehicle.getLoc()); //capitalize the new connection's name
                String s1 = temp.substring(0, 1).toUpperCase();
                connectorSelectionEdittext.setText(s1 + temp.substring(1)); //update the edittext field
            }
        } catch (Exception ignored) {}
    }

    /**
     * Button redirect for the "up' button.
     * @param view View
     */
    @SuppressLint("SetTextI18n")
    public void up(View view){
        try {
            if (!myvehicle.getUniqueConnections().isEmpty()) { //check that unique connections is not empty
                myvehicle.setLoc(myvehicle.getLoc() - 1); //subtract one from the location tracking variable
                if (myvehicle.getLoc() < 0) { //if we have gone past the front of the list, go to the end
                    myvehicle.setLoc(myvehicle.getUniqueConnections().size() - 1);
                }
                String temp = myvehicle.getUniqueConnections().get(myvehicle.getLoc()); //capitalize the new connection
                String s1 = temp.substring(0, 1).toUpperCase();
                connectorSelectionEdittext.setText(s1 + temp.substring(1)); //update the edittext
            }
        } catch (Exception ignored) {}
    }

    /**
     * Button redirect for next, will send the user to the selectpin activity
     * @param view View
     */
    public void next(View view){
        try {
            String connector = connectorSelectionEdittext.getText().toString();
            if (!connector.isEmpty()) {
                if (myvehicle.getUniqueConnections().contains(connector.toLowerCase())) {
                    Intent i = new Intent(getBaseContext(), selectpin.class);
                    i.putParcelableArrayListExtra("connections", myvehicle.getConnections());
                    i.putExtra("myvehicle", myvehicle);
                    startActivity(i);
                }
            }
        } catch (Exception ignored) {}

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
     * Just another nightmode toggle
     */
    public void nightMode(){
        handler.post(new Runnable() {
            @Override
            public void run() {
                ConstraintLayout constraintLayout = findViewById(R.id.connectorselectcontraintlayout);
                constraintLayout.setBackgroundColor(Color.parseColor("#333333"));
                TextView textView = findViewById(R.id.connectorselecttextview1);
                textView.setTextColor(Color.WHITE);
                EditText editText = findViewById(R.id.connectorinput);
                editText.setTextColor(Color.WHITE);
                ImageButton imageButton = findViewById(R.id.connectorselectbutton1);
                imageButton.setBackgroundResource(R.drawable.nightmodebuttonselector);
                imageButton = findViewById(R.id.connectorselectbutton2);
                imageButton.setBackgroundResource(R.drawable.nightmodebuttonselector);
                Button button = findViewById(R.id.connectorselectbutton3);
                button.setBackgroundResource(R.drawable.nightmodebuttonselector);
                button.setTextColor(Color.WHITE);
            }
        });

    }

    /**
     * Yet another daymode toggle
     */
    public void dayMode() {
        handler.post(new Runnable() {
            @Override
            public void run() {
                ConstraintLayout constraintLayout = findViewById(R.id.connectorselectcontraintlayout);
                constraintLayout.setBackgroundColor(Color.WHITE);
                TextView textView = findViewById(R.id.connectorselecttextview1);
                textView.setTextColor(Color.BLACK);
                EditText editText = findViewById(R.id.connectorinput);
                editText.setTextColor(Color.BLACK);
                ImageButton imageButton = findViewById(R.id.connectorselectbutton1);
                imageButton.setBackgroundResource(R.drawable.daymodebuttonselector);
                imageButton = findViewById(R.id.connectorselectbutton2);
                imageButton.setBackgroundResource(R.drawable.daymodebuttonselector);
                Button button = findViewById(R.id.connectorselectbutton3);
                button.setBackgroundResource(R.drawable.daymodebuttonselector);
                button.setTextColor(Color.BLACK);
            }
        });
    }

}
