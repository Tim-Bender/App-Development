package com.example.main;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.Charset;

/**
 * Author: Timothy Bender
 * timothy.bender@spudnik.com
 * 530-414-6778
 * Please see README before updating anything
 */

public class inputserial extends AppCompatActivity implements Runnable {
    public vehicle myvehicle;
    boolean empty = true;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_inputserial);
    }


    /*
     * This method will build the vehicle class, by pulling data from the database csv
     */
    public void go(View view){
        this.empty = true;
        EditText edittext = findViewById(R.id.inputid);
        String vehicleId = edittext.getText().toString();
        this.myvehicle = new vehicle(vehicleId);
        Toast.makeText(this, vehicleId, Toast.LENGTH_LONG).show();
        InputStream is = getResources().openRawResource(R.raw.parsedtest);
        BufferedReader reader = new BufferedReader(new InputStreamReader(is, Charset.forName("UTF-8")));
        String line = null;
        try{
            while((line = reader.readLine()) != null) {
                String[] tokens = line.split(",");
                if (testConnection(vehicleId,tokens[0])) {
                    this.empty = false;
                    connection newConnection = new connection(tokens[0], tokens[1], tokens[2], tokens[3], tokens[4], tokens[5]);
                    this.myvehicle.addConnection(newConnection);
                    if(!myvehicle.getUniqueConnections().contains(tokens[1])){
                        myvehicle.addUniqueconnection(tokens[1]);
                    }
                    //System.out.println(newConnection);
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        if(this.empty){
            Toast.makeText(this, "Not a valid code", Toast.LENGTH_SHORT).show();
        }
        else {
            Intent i = new Intent(getBaseContext(), connectorselect.class);
            i.putExtra("vehicle", myvehicle);
            startActivity(i);
        }

    }

    //if it runs into an X or a 55 then it will return true
    public boolean testConnection(String vehicleid, String s){
        for(int i = 0; i < s.length(); i++){
            if(s.charAt(i) == 'X'){
                return true;
            }
            if(i<s.length()-1){
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
