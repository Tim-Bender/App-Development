package com.Diagnostic.Spudnik;


import android.annotation.SuppressLint;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import java.util.ArrayList;
import java.util.Objects;

public class Pindiagnostic extends AppCompatActivity {
    private vehicle myvehicle;
    private TextView direction,pinnumber,pinname,connectorinformation;
    private connection myConnection;
    private ArrayList<connection> uniqueConnections;
    private int loc;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.content_pindiagnostic);
        try {
            myvehicle = getIntent().getParcelableExtra("myvehicle");
            Objects.requireNonNull(this.myvehicle).setConnections(getIntent().<connection>getParcelableArrayListExtra("connections"));
            loc = getIntent().getIntExtra("loc", 0);
            uniqueConnections = getIntent().getParcelableArrayListExtra("uniqueconnections");

            myConnection = uniqueConnections.get(this.loc);
            Toolbar toolbar = findViewById(R.id.topAppBar);
            setSupportActionBar(toolbar);
            toolbar.setTitleTextColor(Color.WHITE);
            direction = findViewById(R.id.direction);
            pinnumber = findViewById(R.id.pinnumber);
            pinname = findViewById(R.id.pinname);
            connectorinformation = findViewById(R.id.connectorinformation);
            updateValues();
        }catch (Exception e){
            e.printStackTrace();
        }
    }

    /*
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
    }*/

    @SuppressLint("SetTextI18n")
    public void updateValues(){
        try {
            myConnection = uniqueConnections.get(loc);
            String temp = myConnection.getDirection();
            String s1 = temp.substring(0, 1).toUpperCase();
            direction.setText(s1 + temp.substring(1));
            pinnumber.setText("Pin:" + myConnection.getS4());
            temp = myConnection.getName();
            s1 = temp.substring(0,1).toUpperCase();
            pinname.setText(s1 + temp.substring(1));
            connectorinformation.setText(myvehicle.getMap(myConnection.getDirection().toLowerCase()) + " " + myConnection.inout() + " Connector\nConnectorVoltage\nVoltage");
            setTitle("Viewing Pin:" + myConnection.getS4());
        } catch (Exception e) {
            e.printStackTrace();
        }
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

    public void nextPin(View view){
        try {
            this.loc++;
            if (this.loc == this.uniqueConnections.size()) {
                this.loc = 0;
            }
            updateValues();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void prevPin(View view){
        try {
            this.loc--;
            if (this.loc < 0) {
                this.loc = this.uniqueConnections.size() - 1;
            }
            updateValues();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void viewpinloc(View view){
        Intent i = new Intent(getBaseContext(), pinlocation.class);
        i.putExtra("myvehicle", this.myvehicle);
        i.putParcelableArrayListExtra("connections",this.myvehicle.getConnections());
        i.putExtra("myConnection",this.myConnection);
        startActivity(i);
    }

}
