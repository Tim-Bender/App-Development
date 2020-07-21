package com.Diagnostic.Spudnik;

import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;

import androidx.appcompat.app.AppCompatActivity;

import java.util.ArrayList;

public class warningscreen extends AppCompatActivity {
    private vehicle myvehicle;
    private ArrayList<connection> connections;
    private connection myconnection;
    private int loc;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_warningscreen);
        myvehicle = getIntent().getParcelableExtra("myvehicle");
        connections = getIntent().getParcelableArrayListExtra("connections");
        myconnection = getIntent().getParcelableExtra("myConnection");
        loc = getIntent().getIntExtra("loc",0);
        findViewById(R.id.warningscreenabortbutton).setOnClickListener(new View.OnClickListener() { //add onclick listener to the abort button.
            @Override
            public void onClick(View v) {
                finish(); //close the activity
            }
        });
    }


    public void accepted(View view){
        Intent i = new Intent(getApplicationContext(),pintest.class);
        i.putExtra("myvehicle", myvehicle);
        i.putParcelableArrayListExtra("connections",connections);
        i.putExtra("myConnection",myconnection);
        i.putExtra("loc",loc);
        startActivity(i);
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