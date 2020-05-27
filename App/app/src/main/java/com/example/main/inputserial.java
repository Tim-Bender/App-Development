package com.example.main;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import android.Manifest;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.res.Resources;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.View;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;
import java.io.InputStream;
import java.util.ArrayList;

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
    public BluetoothAdapter mBluetoothAdapter;
    public ArrayList<BluetoothDevice> mBTdevices = new ArrayList<>();



    //Create a broadcast receiver for action_found #1
    private final BroadcastReceiver mBroadcastReceiver1 = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if(action.equals(BluetoothAdapter.ACTION_STATE_CHANGED)){
                final int state = intent.getIntExtra(BluetoothAdapter.EXTRA_STATE,BluetoothAdapter.ERROR);
                switch(state){
                    case BluetoothAdapter.STATE_OFF:
                        System.out.println("onReceive: STATE OFF");
                        break;
                    case BluetoothAdapter.STATE_TURNING_OFF:
                        System.out.println("onReceive: STATE TURNING OFF");
                        break;
                    case BluetoothAdapter.STATE_ON:
                        System.out.println("onReceive: STATE ON");
                        break;
                    case BluetoothAdapter.STATE_TURNING_ON:
                        System.out.println("onReceive: STATE TURNING ON");
                        break;
                }

            }
        }
    };
    //Create a broadcast receiver for action_found #2
    private final BroadcastReceiver mBroadcastReceiver2 = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if(action.equals(BluetoothAdapter.ACTION_SCAN_MODE_CHANGED)){
                final int state = intent.getIntExtra(BluetoothAdapter.EXTRA_SCAN_MODE,BluetoothAdapter.ERROR);
                switch(state){
                    case BluetoothAdapter.SCAN_MODE_CONNECTABLE_DISCOVERABLE:
                        System.out.println("mBroadcastReceiver2: Discovery Enabled.");
                        break;
                    case BluetoothAdapter.SCAN_MODE_NONE:
                        System.out.println("mBroadCastReceiver2: Discoverability Disabled. Not able to receive connections.");
                        break;
                    case BluetoothAdapter.SCAN_MODE_CONNECTABLE:
                        System.out.println("mBroadCastReceiver2: Discovery Enabled. Able to receive communication");
                        break;
                    case BluetoothAdapter.STATE_CONNECTING:
                        System.out.println("mBroadCastReceiver2: Connecting...");
                        break;
                    case BluetoothAdapter.STATE_CONNECTED:
                        System.out.println("mBroadCastReceiver2: Connected");
                        break;
                }
            }
        }
    };

    private BroadcastReceiver mBroadcastReceiver3 = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            final String action = intent.getAction();

            if(action.equals(BluetoothDevice.ACTION_FOUND)){
                BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
                mBTdevices.add(device);
                System.out.println("onReceive: " + device.getName() + ": " + device.getAddress());
            }
        }
    };

    private BroadcastReceiver mBroadcastReceiver4 = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            final String action = intent.getAction();
            if(action.equals(BluetoothDevice.ACTION_BOND_STATE_CHANGED)){
                BluetoothDevice mDevice = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);

                if(mDevice.getBondState() == BluetoothDevice.BOND_BONDED){
                    System.out.println("BroadcastReceiver4: Bond bonded");
                }
                if(mDevice.getBondState() == BluetoothDevice.BOND_BONDING){
                    System.out.println("BroadcastReceiver4: Bond bonding");
                }
                if(mDevice.getBondState() == BluetoothDevice.BOND_NONE){
                    System.out.println("BroadcastReceiver4: Bond none");
                }
            }
        }
    };

    @Override
    protected void onDestroy() {
        super.onDestroy();
        unregisterReceiver(mBroadcastReceiver1);
        unregisterReceiver(mBroadcastReceiver2);
        unregisterReceiver(mBroadcastReceiver3);
        unregisterReceiver(mBroadcastReceiver4);
    }


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
        IntentFilter filter = new IntentFilter(BluetoothDevice.ACTION_BOND_STATE_CHANGED);
        registerReceiver(mBroadcastReceiver4,filter);
        try{
            mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
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

    public void deviceDiscover(){
        System.out.println("In discoverable method");
        if(mBluetoothAdapter.isDiscovering()){
            mBluetoothAdapter.cancelDiscovery();
            checkBTPermissions();
            mBluetoothAdapter.startDiscovery();
            IntentFilter discoverDevicesIntent = new IntentFilter(BluetoothDevice.ACTION_FOUND);
            registerReceiver(mBroadcastReceiver3,discoverDevicesIntent);
        }
        if(!mBluetoothAdapter.isDiscovering()){
            checkBTPermissions();
            mBluetoothAdapter.startDiscovery();
            IntentFilter discoverDevicesIntent = new IntentFilter(BluetoothDevice.ACTION_FOUND);
            registerReceiver(mBroadcastReceiver3,discoverDevicesIntent);
        }
    }
    private void checkBTPermissions(){
        if(Build.VERSION.SDK_INT > Build.VERSION_CODES.LOLLIPOP);{
            int permissionCheck = this.checkSelfPermission("Manifest.permission.ACCESS_FINE_LOCATION");
            permissionCheck += this.checkSelfPermission("Manifest.permission.ACCESS_COARSE_LOCATION");
            if(permissionCheck != 0){
                this.requestPermissions(new String[]{Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION},101);
            }
            else{
                System.out.println("checkBtPermissions: No need to check permissions.");
            }
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
                InputStream d = getResources().openRawResource(R.raw.dealerids);
                this.myvehicle = new vehicle(vehicleId);
                this.myvehicle.setIs(is);
                this.myvehicle.buildDataBase();
                this.myvehicle.buildDealers(d);

                if (!myvehicle.getConnections().isEmpty() && this.myvehicle.checkDealer(this.dealerText.getText().toString().toLowerCase().trim())) {
                    //Toast.makeText(this, vehicleId, Toast.LENGTH_LONG).show();
                    enableBluetooth();
                    enableDiscoverable();
                    createBond();
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

    public void enableBluetooth(){
        if(mBluetoothAdapter == null){
            System.out.println("enableBluetooth: Does not have BT capabilities.");
        }
        if(!mBluetoothAdapter.isEnabled()) {
            Intent enableBTIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivity(enableBTIntent);

            IntentFilter BTIntent = new IntentFilter(BluetoothAdapter.ACTION_CONNECTION_STATE_CHANGED);
            registerReceiver(mBroadcastReceiver1, BTIntent);
        }
    }

    public void enableDiscoverable(){
        Intent discoverableIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_DISCOVERABLE);
        discoverableIntent.putExtra(BluetoothAdapter.EXTRA_DISCOVERABLE_DURATION,300);
        startActivity(discoverableIntent);
        IntentFilter intentFilter = new IntentFilter(mBluetoothAdapter.ACTION_SCAN_MODE_CHANGED);
        registerReceiver(mBroadcastReceiver2,intentFilter);
    }

    public void createBond(){
        mBluetoothAdapter.cancelDiscovery();
        for(BluetoothDevice device : this.mBTdevices){
            if(device.getName() == "test_dev_1"){
                String deviceName = device.getName();
                String deviceAddress = device.getAddress();

                System.out.println("createBond: " + deviceName);
                System.out.println("Createbond: " + deviceAddress);

                if(Build.VERSION.SDK_INT > Build.VERSION_CODES.JELLY_BEAN_MR2){
                   device.createBond();
                }
                break;
            }
        }

    }
}
