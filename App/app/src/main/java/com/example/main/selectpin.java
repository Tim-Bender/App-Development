package com.example.main;

import android.Manifest;
import android.annotation.SuppressLint;
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
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.LinearLayout;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;
import java.io.InputStream;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.UUID;

public class selectpin extends AppCompatActivity {
    private vehicle myvehicle;
    private TextView  textView;
    private ArrayList<connection> connections = new ArrayList<>();
    private Toolbar toolbar;
    private Switch toggle;
    public BluetoothAdapter mBluetoothAdapter;
    public ArrayList<BluetoothDevice> mBTdevices = new ArrayList<>();
    BluetoothConnectionService mBluetoothConnection;
    BluetoothDevice mBTDevice;
    private final String TAG = "InputSerial";
    private static final UUID uuid = UUID.fromString("8ce255c0-200a-11e0-ac64-0800200c9a66");
    TextView incomingMessage;
    StringBuilder messages;

    @SuppressLint("SetTextI18n")



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
                    mBTDevice = mDevice;
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
        try {
            unregisterReceiver(mBroadcastReceiver1);
            unregisterReceiver(mBroadcastReceiver2);
            unregisterReceiver(mBroadcastReceiver3);
            unregisterReceiver(mBroadcastReceiver4);
        } catch (Exception e) {
            Log.e(TAG,"Broadcast Receivers could not be killed, did not exist.");
        }
    }
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.content_selectpin);
        this.toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        toolbar.setTitleTextColor(Color.WHITE);
        setTitle("Pin Selection");
        this.toolbar.setTitleTextColor(Color.WHITE);

        try {
            messages = new StringBuilder();
            incomingMessage = findViewById(R.id.selectpinvoltage);
            mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
            LocalBroadcastManager.getInstance(this).registerReceiver(mReceiver,new IntentFilter("incomingMessage"));
            IntentFilter filter = new IntentFilter(BluetoothDevice.ACTION_BOND_STATE_CHANGED);
            registerReceiver(mBroadcastReceiver4,filter);
            InputStream is = getResources().openRawResource(R.raw.parsedtest);
            this.myvehicle = getIntent().getParcelableExtra("myvehicle");
            assert this.myvehicle != null;
            this.myvehicle.setConnections(getIntent().<connection>getParcelableArrayListExtra("connections"));
            this.myvehicle.setIs(is);

            this.textView = findViewById(R.id.connectorid);
            this.toggle=findViewById(R.id.sortbytoggle);
            toggle.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                @Override
                public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                    if(isChecked){
                        if(myvehicle.getLastSorted() == vehicle.SORT_BY_S4) {
                            myvehicle.sortConnections(vehicle.SORT_BY_NAME);
                            myvehicle.setLastSorted(vehicle.SORT_BY_NAME);
                            updatevalues();
                        }
                    }
                    else{
                        if(myvehicle.getLastSorted() == vehicle.SORT_BY_NAME){
                            myvehicle.sortConnections(vehicle.SORT_BY_S4);
                            myvehicle.setLastSorted(vehicle.SORT_BY_S4);
                            updatevalues();
                        }

                    }
                }
            });
        } catch (Resources.NotFoundException e) {
            e.printStackTrace();
        }
        try {
        } catch (Exception e) {
            Toast.makeText(this, "Bluetooth Error ", Toast.LENGTH_SHORT).show();
        }

    }

    @Override
    public void onStart(){
        super.onStart();
        try {
            this.myvehicle.sortConnections(vehicle.SORT_BY_S4);
            updatevalues();
            masterBTStart();
        } catch (Exception e) {
            Toast.makeText(this, "Bluetooth Error", Toast.LENGTH_SHORT).show();
        }
    }
    BroadcastReceiver mReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String text = intent.getStringExtra("theMessage");
            messages.append(text + "\n");
            Toast.makeText(context, text, Toast.LENGTH_SHORT).show();
            incomingMessage.setText(messages);
            send("R,In4,4,V");
        }
    };



    public void updatevalues(){
            try{
            String temp = this.myvehicle.getUniqueConnections().get(this.myvehicle.getLoc());
            String s1 = temp.substring(0, 1).toUpperCase();
            this.textView.setText(s1 + temp.substring(1));

            LinearLayout layout = findViewById(R.id.pins);
            layout.removeAllViews();
            int counter = 0;
            if (!this.myvehicle.getConnections().isEmpty()) {
                for (connection c : this.myvehicle.getConnections()) {
                    if (c.getDirection().contains(temp.toLowerCase())) {
                        if (!this.myvehicle.getUniquePins().contains(c.getDirection().toLowerCase().trim())) {
                            this.myvehicle.addUniquePin(c.getDirection().toLowerCase().trim());
                            this.myvehicle.setPinCount(this.myvehicle.getPinCount() + 1);
                        }
                        this.connections.add(c);
                        final Button btn = new Button(this);
                        btn.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.MATCH_PARENT));
                        btn.setTextSize(18);
                        btn.setTextColor(Color.BLACK);
                        btn.setGravity(Gravity.BOTTOM);
                        btn.setTag(counter);
                        btn.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                pinSelected((Integer) v.getTag());
                            }
                        });
                        btn.setText("Pin" + c.getS4() + " Signal:  " + c.getName());
                        btn.setGravity(Gravity.START);
                        layout.addView(btn);
                        counter++;

                    }
                }
            } else {
                Toast.makeText(this, "No Pins Available", Toast.LENGTH_SHORT).show();
                TextView textView = new TextView(this);
                textView.setText("No pin information available");
                layout.addView(textView);
            }
            this.textView = findViewById(R.id.numberofpinstextfield);
            this.textView.setText(this.myvehicle.getMap(this.myvehicle.getUniqueConnections().get(this.myvehicle.getLoc())) + "p " + this.myvehicle.inout() + " Connector");


        } catch (Resources.NotFoundException e) {
            Toast.makeText(this, "File Not Found Error", Toast.LENGTH_SHORT).show();
            e.printStackTrace();
        }

    }

    public void pinSelected(int loc){
        //Toast.makeText(this, o.toString(), Toast.LENGTH_SHORT).show();
        Intent i = new Intent(getBaseContext(), Pindiagnostic.class);
        i.putExtra("myvehicle", this.myvehicle);
        i.putParcelableArrayListExtra("connections",this.myvehicle.getConnections());
        i.putExtra("loc",loc);
        i.putParcelableArrayListExtra("uniqueconnections",this.connections);
        startActivity(i);


    }

    public void masterBTStart(){
        enableBluetooth();
        enableDiscoverable();
        createBond();
        startConnection();

    }
    public void startBTConnection(BluetoothDevice device, UUID uuid){
        Log.d(TAG,"startBtConnection: Initializing RFZOM bluetooth connection");
        mBluetoothConnection.startclient(this.mBTDevice,this.uuid);
    }

    public void startConnection(){
        startBTConnection(mBTDevice,uuid);
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
                    mBTDevice = device;
                    mBluetoothConnection = new BluetoothConnectionService(selectpin.this);
                }
                break;
            }
        }

    }

    public void send(String tosend){
        byte[] bytes = tosend.getBytes(Charset.defaultCharset());
        mBluetoothConnection.write(bytes);
    }


}
