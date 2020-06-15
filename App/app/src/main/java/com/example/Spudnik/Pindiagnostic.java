package com.example.Spudnik;


import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.preference.PreferenceManager;

import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;
import java.util.ArrayList;
import java.util.Objects;

public class Pindiagnostic extends AppCompatActivity {
    private vehicle myvehicle;
    private TextView direction,pinnumber,pinname,connectorinformation;
    private connection myConnection;
    private ArrayList<connection> uniqueConnections;
    private int loc;
    private SharedPreferences preferences;

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
            Toolbar toolbar = findViewById(R.id.toolbar);
            setSupportActionBar(toolbar);
            toolbar.setTitleTextColor(Color.WHITE);
            getSupportActionBar().setIcon(R.mipmap.ic_launcher);
            direction = findViewById(R.id.direction);
            pinnumber = findViewById(R.id.pinnumber);
            pinname = findViewById(R.id.pinname);
            connectorinformation = findViewById(R.id.connectorinformation);
            preferences = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
            updateValues();
        }catch (Exception e){
            Toast.makeText(this, "ArrayList Parse Error", Toast.LENGTH_SHORT).show();
            e.printStackTrace();
        }
    }
    @Override
    protected void onStart(){
        super.onStart();
        if(preferences.getBoolean("nightmode",false)){
            nightMode();
        }


    }
    @Override
    protected void onResume() {
        super.onResume();
        if(preferences.getBoolean("nightmode",false)){
            nightMode();
            return;
        }
        if(!preferences.getBoolean("nightmode",false)){
            dayMode();
        }
    }

    @SuppressLint("SetTextI18n")
    public void updateValues(){
        try {
            this.myConnection = uniqueConnections.get(loc);
            String temp = this.myConnection.getDirection();
            String s1 = temp.substring(0, 1).toUpperCase();
            this.direction.setText(s1 + temp.substring(1));
            this.pinnumber.setText("Pin:" + myConnection.getS4());
            temp = this.myConnection.getName();
            s1 = temp.substring(0,1).toUpperCase();
            this.pinname.setText(s1 + temp.substring(1));
            this.connectorinformation.setText(this.myvehicle.getMap(this.myConnection.getDirection().toLowerCase()) + " " + this.myConnection.inout() + " Connector");
            setTitle("Viewing Pin:" + myConnection.getS4());
        } catch (Exception e) {
            Toast.makeText(this, "ArrayList Parse Error", Toast.LENGTH_SHORT).show();
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
            Toast.makeText(this, "Bounds Error", Toast.LENGTH_SHORT).show();
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
            Toast.makeText(this, "Bounds Error", Toast.LENGTH_SHORT).show();
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

    public void nightMode(){
        ConstraintLayout constraintLayout = findViewById(R.id.pindiagnosticconstraintlayout);
        constraintLayout.setBackgroundColor(Color.parseColor("#333333"));
        LinearLayout layout = findViewById(R.id.pindiagnosticlayout1);
        layout.setBackgroundResource(R.drawable.nightmodeback);
        layout = findViewById(R.id.pindiagnosticlayout2);
        layout.setBackgroundResource(R.drawable.nightmodeback);
        TextView textView = findViewById(R.id.direction);
        textView.setTextColor(Color.WHITE);
        textView = findViewById(R.id.connectorinformation);
        textView.setTextColor(Color.WHITE);
        textView = findViewById(R.id.pindiagnostictextview3);
        textView.setTextColor(Color.WHITE);
        textView = findViewById(R.id.pindiagnosticvoltage);
        textView.setTextColor(Color.WHITE);
        textView = findViewById(R.id.pinnumber);
        textView.setTextColor(Color.WHITE);
        textView = findViewById(R.id.pinname);
        textView.setTextColor(Color.WHITE);
        textView = findViewById(R.id.voltage);
        textView.setTextColor(Color.WHITE);
        Button button = findViewById(R.id.pindiagnosticbutton1);
        button.setBackgroundResource(R.drawable.nightmodebuttonselector);
        button.setTextColor(Color.WHITE);
        button = findViewById(R.id.nextpin);
        button.setBackgroundResource(R.drawable.nightmodebuttonselector);
        button.setTextColor(Color.WHITE);
        button = findViewById(R.id.prevpin);
        button.setBackgroundResource(R.drawable.nightmodebuttonselector);
        button.setTextColor(Color.WHITE);
    }

    public void dayMode(){
        ConstraintLayout constraintLayout = findViewById(R.id.pindiagnosticconstraintlayout);
        constraintLayout.setBackgroundColor(Color.WHITE);
        LinearLayout layout = findViewById(R.id.pindiagnosticlayout1);
        layout.setBackgroundResource(R.drawable.back);
        layout = findViewById(R.id.pindiagnosticlayout2);
        layout.setBackgroundResource(R.drawable.back);
        TextView textView = findViewById(R.id.direction);
        textView.setTextColor(Color.BLACK);
        textView = findViewById(R.id.connectorinformation);
        textView.setTextColor(Color.BLACK);
        textView = findViewById(R.id.pindiagnostictextview3);
        textView.setTextColor(Color.BLACK);
        textView = findViewById(R.id.pindiagnosticvoltage);
        textView.setTextColor(Color.BLACK);
        textView = findViewById(R.id.pinnumber);
        textView.setTextColor(Color.BLACK);
        textView = findViewById(R.id.pinname);
        textView.setTextColor(Color.BLACK);
        textView = findViewById(R.id.voltage);
        textView.setTextColor(Color.BLACK);
        Button button = findViewById(R.id.pindiagnosticbutton1);
        button.setBackgroundResource(android.R.drawable.btn_default);
        button.setTextColor(Color.BLACK);
        button = findViewById(R.id.nextpin);
        button.setBackgroundResource(android.R.drawable.btn_default);
        button.setTextColor(Color.BLACK);
        button = findViewById(R.id.prevpin);
        button.setBackgroundResource(android.R.drawable.btn_default);
        button.setTextColor(Color.BLACK);

    }

}
