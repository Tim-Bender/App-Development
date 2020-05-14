package com.example.main;

import android.content.Intent;
import android.os.Bundle;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.snackbar.Snackbar;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.view.View;
import android.widget.EditText;

/**
 * Author: Timothy Bender
 * timothy.bender@spudnik.com
 * 530-414-6778
 * Please see README before updating anything
 */

public class connectorselect extends AppCompatActivity {
    private vehicle myvehicle;
    private int loc = 0;
    EditText editText;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_connectorselect);
        this.myvehicle = getIntent().getParcelableExtra("vehicle");
        this.editText = (EditText)findViewById(R.id.connectorinput);
        this.editText.setText(this.myvehicle.getUniqueConnections().get(0));
    }


    public void up(View view){
        this.loc++;
        if(this.loc == myvehicle.getUniqueConnections().size()){
            this.loc = 0;
        }
        this.editText.setText(this.myvehicle.getUniqueConnections().get(this.loc));
    }

    public void down(View view){
        this.loc--;
        if(this.loc < 0){
            this.loc = myvehicle.getUniqueConnections().size()-1;
        }
        this.editText.setText(this.myvehicle.getUniqueConnections().get(this.loc));
    }

    public void back(View view){
        finish();
    }

    public void next(View view){
        Intent i = new Intent(getBaseContext(), selectpin.class);
        i.putExtra("vehicle",myvehicle);
        startActivity(i);
    }

}
