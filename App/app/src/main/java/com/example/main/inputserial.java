package com.example.main;

import androidx.appcompat.app.AppCompatActivity;
import android.content.Intent;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;
import java.io.InputStream;

/**
 * Author: Timothy Bender
 * timothy.bender@spudnik.com
 * 530-414-6778
 * Please see README before updating anything
 */

public class inputserial extends AppCompatActivity implements Runnable {
    public vehicle myvehicle;
    boolean empty = true;
    private EditText edittext;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_inputserial);
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
    }


    /*
     * This method will build the vehicle class, by pulling data from the database csv
     */
    public void go(View view){
        this.empty = true;
        String vehicleId = edittext.getText().toString();
        if(!(edittext.getText().length() < 2)) {
            InputStream is = getResources().openRawResource(R.raw.parsedtest);
            this.myvehicle = new vehicle(vehicleId);
            this.myvehicle.setIs(is);
            if(!myvehicle.getConnections().isEmpty()) {
                Toast.makeText(this, vehicleId, Toast.LENGTH_LONG).show();
                Intent i = new Intent(getBaseContext(), connectorselect.class);
                i.putExtra("myvehicle", myvehicle);
                startActivity(i);
            }
            else{
                Toast.makeText(this, "Invalid", Toast.LENGTH_SHORT).show();
            }
        }
        else{
            Toast.makeText(this, "Invalid", Toast.LENGTH_SHORT).show();
        }


    }

    //if it runs into an X or a 55 then it will return true
    public boolean testConnection(String vehicleid, String s){
        for(int i = 0; i < s.length(); i++){
            if(s.charAt(i) == 'X'){
                return true;
            }
            if(i<s.length()-1 && i > 2){
                if(s.charAt(i) == '5' && s.charAt(i+1) == '5'){
                    return true;
                }
            }
            if(s.charAt(i) != vehicleid.charAt(i)){
                return false;
            }
        }
        return true;
    }

    @Override
    public void run() {

    }
}
