package com.example.main;

import android.app.ActionBar;
import android.os.Bundle;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.snackbar.Snackbar;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.view.View;
import android.widget.EditText;
import android.widget.ScrollView;
import android.widget.TextView;

public class selectpin extends AppCompatActivity {
    vehicle myvehicle;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_selectpin);
        this.myvehicle = getIntent().getParcelableExtra("vehicle");

        ScrollView scrollView = findViewById(R.id.pins);
        TextView b = new TextView(this);
        String tempdirection = "Out1";
        for(connection connection : myvehicle.getConnections()){
            if(connection.getDirection() == tempdirection){
                b.setWidth(ActionBar.LayoutParams.MATCH_PARENT);
                b.setHeight(ActionBar.LayoutParams.WRAP_CONTENT);
                b.setText("Pin " + connection.getS4() + ": " + connection.getName());
                scrollView.addView(b);
            }

        }
    }

}
