package com.Diagnostic.Spudnik;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

/**
 * Welcome to the homescreen activity. This activity's primary job is to direct the user where they want to go.
 * The middle two functions are not currently supported. Support will likely be added at a later date... maybe.
 *
 * @author timothy.bender
 * @version dev1.0.0
 * @since dev 1.0.0
 */

public class home extends AppCompatActivity {
    /**Vehicle object. Initialized in mainactivity, and usually pre-build there as well. */
    private vehicle myvehicle;
    /**
     * Nothing special in this onCreate. There is a check whether or not the vehicle object's id's have been constructed or not
     * @param savedInstanceState savedInstancestate
     * @since dev 1.0.0
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.content_home);

        Toolbar myToolBar = findViewById(R.id.topAppBar); //typical toolbar setup
        setSupportActionBar(myToolBar);
        setTitle("Home");
        myToolBar.setTitleTextColor(Color.WHITE);

        Snackbar.make(findViewById(R.id.homeconstraintlayout),"Welcome",Snackbar.LENGTH_SHORT).show();
        myvehicle = getIntent().getParcelableExtra("myvehicle"); //get out parcelabled vehicle object
        if(myvehicle.getVehicleIds().isEmpty())
            myvehicle.preBuildVehicleObject(this); //try again to prebuild the vehicle ids and dealer names.
    }

    /**
     * Button Redirect for the Diagnostic Tool button.
     * @param view view
     * @since dev 1.0.0
     */
    public void diagTool(View view){
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser(); //get the current firebase user
        if(user != null) { //authentication is required before one can access the diagnostic tool
            Intent i = new Intent(getBaseContext(), inputserial.class);
            i.putExtra("myvehicle",myvehicle); //add the myvehicle object as a parcelable extra in the intent
            startActivity(i);
        }
        else
            Snackbar.make(findViewById(R.id.homeconstraintlayout), "Please Sign In", Snackbar.LENGTH_SHORT).show();

    }

    /**
     * Update Button redirect, currently disabled.
     * @param view view
     * @since dev 1.0.0
     */
    public void update(View view){
        Snackbar.make(findViewById(R.id.homeconstraintlayout), "Function Not Supported", Snackbar.LENGTH_SHORT).show();
    }

    /**
     * Log Data button redirect, currently disabled
     * @param view view
     * @since dev 1.0.0
     */

    public void logData(View view){
        Snackbar.make(findViewById(R.id.homeconstraintlayout), "Function Not Supported", Snackbar.LENGTH_SHORT).show();
    }

    /**
     * Settings button redirect, will start the settings activity.
     * @param view view
     * @since dev 1.0.0
     */
    public void settings(View view){
        Intent i = new Intent(getApplicationContext(), settings.class);
        startActivity(i);
    }


    /**
     * The next two methods will create the toolbar menu item on the top right, this will be on every
     * activity that contains this shortcut.
     * @param item MenuItem
     * @return onOptionsItemSelected(item)
     * @since dev 1.0.0
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
