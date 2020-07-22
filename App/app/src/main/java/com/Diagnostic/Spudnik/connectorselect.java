package com.Diagnostic.Spudnik;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Spinner;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.google.android.material.textfield.TextInputEditText;

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
    private TextInputEditText connectorSelectionEdittext;

    /**
     * Just your normal onCreate.
     * @param savedInstanceState Bundle
     */
    @SuppressLint("SetTextI18n")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.content_connectorselect);
        Toolbar toolbar = findViewById(R.id.topAppBar);
        setSupportActionBar(toolbar);
        toolbar.setTitleTextColor(Color.WHITE);
        setTitle("Connector Selection");
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
        // boolean nightMode = preferences.getBoolean("nightmode",false); //Determine whether or not we are in nightmode
        Spinner mySpinner = findViewById(R.id.myconnectorspinner); //get our spinner view
        List<String> connections = myvehicle.getUniqueConnections(); //unique connections is a list of all the unique connections on a machine. We will use it to populate the spinner
        ArrayAdapter<String> dataAdapter;
        dataAdapter = new ArrayAdapter<>(this, R.layout.spinner_item_night, connections);
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
    }

    /**
     * Button redirect for the "down" button.
     * @param view View
     */
    @SuppressLint("SetTextI18n")
    public void down(View view) {
        if (!myvehicle.getUniqueConnections().isEmpty()) { //Check that unique connections is not empty
            myvehicle.setLoc(myvehicle.getLoc() + 1);   //add one to the location tracking variable
            if (myvehicle.getLoc() == myvehicle.getUniqueConnections().size()) //if we've reached the end of the list, go to the front
                myvehicle.setLoc(0);
            String temp = myvehicle.getUniqueConnections().get(myvehicle.getLoc()); //capitalize the new connection's name
            String s1 = temp.substring(0, 1).toUpperCase();
            connectorSelectionEdittext.setText(s1 + temp.substring(1)); //update the edittext field
        }

    }

    /**
     * Button redirect for the "up' button.
     * @param view View
     */
    @SuppressLint("SetTextI18n")
    public void up(View view){
        if (!myvehicle.getUniqueConnections().isEmpty()) { //check that unique connections is not empty
            myvehicle.setLoc(myvehicle.getLoc() - 1); //subtract one from the location tracking variable
            if (myvehicle.getLoc() < 0) //if we have gone past the front of the list, go to the end
                myvehicle.setLoc(myvehicle.getUniqueConnections().size() - 1);
            String temp = myvehicle.getUniqueConnections().get(myvehicle.getLoc()); //capitalize the new connection
            String s1 = temp.substring(0, 1).toUpperCase();
            connectorSelectionEdittext.setText(s1 + temp.substring(1)); //update the edittext
        }
    }

    /**
     * Button redirect for next, will send the user to the selectpin activity
     * @param view View
     */
    public void next(View view){
        String connector = connectorSelectionEdittext.getText().toString();
        if (!connector.isEmpty()) {
            if (myvehicle.getUniqueConnections().contains(connector.toLowerCase())) {
                Intent i = new Intent(getBaseContext(), selectpin.class);
                i.putParcelableArrayListExtra("connections", myvehicle.getConnections());
                i.putExtra("myvehicle", myvehicle);
                startActivity(i);
            }
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

}
