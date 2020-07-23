package com.Diagnostic.Spudnik;

import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;

import androidx.appcompat.app.AppCompatActivity;

import java.util.ArrayList;

/**
 * This is the warning screen activity. It will serve as a final warning before users enter the pin test mode
 *
 * @author timothy.bender
 * @version dev 1.0.0
 * @since dev 1.0.0
 */
public class warningscreen extends AppCompatActivity {
    /**
     * Same vehicle object
     */
    private vehicle myvehicle;
    /**
     * Arraylist of unique connections
     */
    private ArrayList<connection> connections;
    /**
     * The connection we are currently looking at
     */
    private connection myconnection;
    private int loc;

    /**
     * Nothing interesting going on in on create, we do add a on click listener for the abort button...
     *
     * @param savedInstanceState Bundle
     * @since dev 1.0.0
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_warningscreen);

        myvehicle = getIntent().getParcelableExtra("myvehicle");
        connections = getIntent().getParcelableArrayListExtra("connections");
        myconnection = getIntent().getParcelableExtra("myConnection");
        loc = getIntent().getIntExtra("loc", 0);

        findViewById(R.id.warningscreenabortbutton).setOnClickListener(new View.OnClickListener() { //add onclick listener to the abort button.
            @Override
            public void onClick(View v) {
                finish(); //close the activity
            }
        });
    }

    /**
     * This is the "Got It!" button redirect. Will send them to the
     *
     * @param view View
     * @since dev 1.0.0
     */
    public void accepted(View view) {
        Intent i = new Intent(getApplicationContext(), pintest.class);
        i.putExtra("myvehicle", myvehicle);
        i.putParcelableArrayListExtra("connections", connections);
        i.putExtra("myConnection", myconnection);
        i.putExtra("loc", loc);
        startActivity(i);
    }

    /**
     * The next two methods will create the toolbar menu item on the top right, this will be on every
     * activity that contains this shortcut.
     *
     * @param item MenuItem
     * @return onOptionsItemSelected(item)
     * @since dev 1.0.0
     */
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.action_settings) {
            Intent i = new Intent(getBaseContext(), settings.class);
            startActivity(i);
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.toolbarbuttons, menu);
        return true;
    }
}