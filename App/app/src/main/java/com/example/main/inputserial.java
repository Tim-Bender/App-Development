package com.example.main;

import androidx.appcompat.app.AppCompatActivity;
import android.content.Intent;
import android.content.res.Resources;
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

public class inputserial extends AppCompatActivity {
    public vehicle myvehicle;
    boolean empty = true;
    private EditText edittext;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_inputserial);
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
        });} catch (Exception e) {
            Toast.makeText(this, "Unidentified Error", Toast.LENGTH_SHORT).show();
            e.printStackTrace();
        }

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
                this.myvehicle = new vehicle(vehicleId);
                this.myvehicle.setIs(is);
                this.myvehicle.buildDataBase();
                if (!myvehicle.getConnections().isEmpty()) {
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
