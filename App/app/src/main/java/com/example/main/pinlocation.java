package com.example.main;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.content.res.Resources;
import android.graphics.Color;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;
import java.util.ArrayList;

public class pinlocation extends AppCompatActivity {

    private int loc;
    private ArrayList<connection> uniqueConnections;
    private connection myConnection;
    private vehicle myvehicle;
    private TextView textView;
    private Toolbar toolbar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_pinlocation);
        this.toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        toolbar.setTitleTextColor(Color.WHITE);
        setTitle("Pin Selection");
        this.toolbar.setTitleTextColor(Color.WHITE);

        try {
            this.myvehicle = getIntent().getParcelableExtra("myvehicle");
            assert this.myvehicle != null;
            this.myvehicle.setConnections(getIntent().<connection>getParcelableArrayListExtra("connections"));
            this.myvehicle.sortConnections(vehicle.SORT_BY_S4);

            this.myConnection = getIntent().getParcelableExtra("myconnection");

        } catch (Resources.NotFoundException e) {
            e.printStackTrace();
        }
        updateValues();
    }

    private void updateValues(){
        try {
            this.textView = findViewById(R.id.pinlocationdirection);
            String temp = this.myConnection.getDirection();
            String s1 = temp.substring(0, 1).toUpperCase();
            this.textView.setText(s1 + temp.substring(1));
            this.textView = findViewById(R.id.pinlocationconnectorinformation);
            this.textView.setText(this.myvehicle.getMap(this.myvehicle.getUniqueConnections().get(this.myvehicle.getLoc())) + "p " + this.myvehicle.inout() + " Connector");
        } catch (Exception e) {
            Toast.makeText(this, "Error", Toast.LENGTH_SHORT).show();
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
}
