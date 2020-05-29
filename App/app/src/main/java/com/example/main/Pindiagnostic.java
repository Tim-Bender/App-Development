package com.example.main;


import android.annotation.SuppressLint;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;
import java.io.InputStream;
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
            this.myvehicle = getIntent().getParcelableExtra("myvehicle");
            Objects.requireNonNull(this.myvehicle).setConnections(getIntent().<connection>getParcelableArrayListExtra("connections"));
            this.loc = getIntent().getIntExtra("loc", 0);
            this.uniqueConnections = getIntent().getParcelableArrayListExtra("uniqueconnections");


            InputStream is = getResources().openRawResource(R.raw.parsedtest);
            this.myvehicle.setIs(is);
            this.myConnection = uniqueConnections.get(this.loc);
            Toolbar toolbar = findViewById(R.id.toolbar);
            setSupportActionBar(toolbar);
            toolbar.setTitleTextColor(Color.WHITE);
            this.direction = findViewById(R.id.direction);
            this.pinnumber = findViewById(R.id.pinnumber);
            this.pinname = findViewById(R.id.pinname);
            this.connectorinformation = findViewById(R.id.connectorinformation);
            System.out.println("This is my connection: " + this.myConnection);
            System.out.println("This is my location: " + this.loc);
            updateValues();
        }catch (Exception e){
            Toast.makeText(this, "ArrayList Parse Error", Toast.LENGTH_SHORT).show();
            e.printStackTrace();
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

}
