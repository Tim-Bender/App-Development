package com.example.main;

import androidx.appcompat.app.AppCompatActivity;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.Intent;
import android.content.res.Resources;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;
import java.io.InputStream;
import java.util.PriorityQueue;
import java.util.Set;

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
        try {
            BluetoothAdapter myAdapter = BluetoothAdapter.getDefaultAdapter();
            if (myAdapter == null) {
                System.out.println("Not supported");
            }
            if (!myAdapter.isEnabled()) {
                Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
                startActivityForResult(enableBtIntent, 1);
            }
            Set<BluetoothDevice> pairedDevices = myAdapter.getBondedDevices();
            if (pairedDevices.size() > 0) {
                for (BluetoothDevice device : pairedDevices) {
                    String deviceName = device.getName();
                    String hardWareAddress = device.getAddress();
                }
            }
        } catch (Exception e) {
            Toast.makeText(this, "Bluetooth Failed", Toast.LENGTH_SHORT).show();
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
