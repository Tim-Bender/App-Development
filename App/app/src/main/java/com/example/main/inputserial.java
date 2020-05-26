package com.example.main;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.preference.PreferenceManager;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.graphics.Color;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.View;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ToggleButton;

import java.io.InputStream;

/**
 * Author: Timothy Bender
 * timothy.bender@spudnik.com
 * 530-414-6778
 * Please see README before updating anything
 */

public class inputserial extends AppCompatActivity {
    public vehicle myvehicle;
    boolean empty = true;
    private EditText edittext,dealerText;
    private Toolbar toolbar;
    public ImageView imageView;
    public TextView textView;
    public Switch toggle;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_inputserial);
        this.toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(this.toolbar);
        setTitle("Input Serial Numer");
        this.toolbar.setTitleTextColor(Color.WHITE);
        this.imageView = findViewById(R.id.helpimage);
        this.imageView.setVisibility(View.GONE);
        this.textView = findViewById(R.id.helptextview);
        this.textView.setVisibility(View.GONE);
        try{
        this.edittext = findViewById(R.id.inputid);
        this.edittext.setOnKeyListener(new View.OnKeyListener() {
            @Override
            public boolean onKey(View v, int keyCode, KeyEvent event) {
                if((event.getAction() == KeyEvent.ACTION_DOWN) && (keyCode == KeyEvent.KEYCODE_ENTER)){
                    go(getCurrentFocus());
                    return true;
                }

                return false;
            }
        });
        this.dealerText = findViewById(R.id.dealeridtextview);
        this.dealerText.setOnKeyListener(new View.OnKeyListener() {
            @Override
            public boolean onKey(View v, int keyCode, KeyEvent event) {
                if((event.getAction() == KeyEvent.ACTION_DOWN) && (keyCode == KeyEvent.KEYCODE_ENTER)){
                    Toast.makeText(inputserial.this, "Enter A Serial Number", Toast.LENGTH_SHORT).show();
                        return true;
                    }
                return false;
            }
        });
        } catch (Exception e) {
            Toast.makeText(this, "Unidentified Error", Toast.LENGTH_SHORT).show();
            e.printStackTrace();
        }


        toggle = findViewById(R.id.helptoggle);
        final ImageView spudnikelectrical = findViewById(R.id.spudnikelectrical);
        toggle.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if(isChecked){
                    spudnikelectrical.setVisibility(View.GONE);
                    imageView.setVisibility(View.VISIBLE);
                    textView.setVisibility(View.VISIBLE);

                }
                else{
                    imageView.setVisibility(View.GONE);
                    textView.setVisibility(View.GONE);
                    spudnikelectrical.setVisibility(View.VISIBLE);

                }
            }
        });



    }


    /*
     * This method will build the vehicle class, by pulling data from the database csv
     */
    public void go(View view) {
        try {
            this.empty = true;
            String vehicleId = edittext.getText().toString();
            if (!(edittext.getText().length() < 2)) {
                InputStream is = getResources().openRawResource(R.raw.parsedtest);
                InputStream d = getResources().openRawResource(R.raw.dealerids);
                this.myvehicle = new vehicle(vehicleId);
                this.myvehicle.setIs(is);
                this.myvehicle.buildDataBase();
                this.myvehicle.buildDealers(d);

                if (!myvehicle.getConnections().isEmpty() && this.myvehicle.checkDealer(this.dealerText.getText().toString().toLowerCase().trim())) {
                    //Toast.makeText(this, vehicleId, Toast.LENGTH_LONG).show();
                    Intent i = new Intent(getBaseContext(), connectorselect.class);
                    i.putExtra("myvehicle", myvehicle);
                    i.putParcelableArrayListExtra("connections",this.myvehicle.getConnections());
                    startActivity(i);
                } else {
                    Toast.makeText(this, "Invalid", Toast.LENGTH_SHORT).show();
                }
            } else {
                Toast.makeText(this, "Invalid", Toast.LENGTH_SHORT).show();
            }
        } catch (Resources.NotFoundException e) {
            Toast.makeText(this, "Resource Not Found", Toast.LENGTH_SHORT).show();
            e.printStackTrace();
        }catch(Exception e){
            Toast.makeText(this, "Unidentified Error", Toast.LENGTH_SHORT).show();
            e.printStackTrace();
        }


    }
}
