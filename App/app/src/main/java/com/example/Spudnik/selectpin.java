package com.example.Spudnik;

import android.Manifest;
import android.annotation.SuppressLint;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.graphics.Color;
import android.os.AsyncTask;
import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;
import androidx.preference.PreferenceManager;

import android.view.Gravity;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.LinearLayout;
import android.widget.Space;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.Objects;
import java.util.UUID;

public class selectpin extends AppCompatActivity {
    private vehicle myvehicle;
    private TextView  textView;
    private ArrayList<connection> connections = new ArrayList<>();
    private  BluetoothAdapter mBluetoothAdapter;
    private ArrayList<BluetoothDevice> mBTdevices = new ArrayList<>();
    private BluetoothDevice mBTDevice;
    private final String TAG = "SelectPin";
    private static final UUID uuid = UUID.fromString("8ce255c0-200a-11e0-ac64-0800200c9a66");
    private TextView incomingMessage;
    private StringBuilder messages;
    private SharedPreferences preferences;
    @SuppressLint("SetTextI18n")


    @Override
    protected void onDestroy(){
        super.onDestroy();
        try {
            unregisterReceiver(mBroadcastReceiver1);
            unregisterReceiver(mBroadcastReceiver2);
            unregisterReceiver(mBroadcastReceiver3);
            unregisterReceiver(mBroadcastReceiver4);
            unregisterReceiver(mReceiver);
            unregisterReceiver(mReceiver2);
        } catch (Exception ignored) {}

    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.content_selectpin);
        try{
            Toolbar toolbar = findViewById(R.id.toolbar);
            setSupportActionBar(toolbar);
            toolbar.setTitleTextColor(Color.WHITE);
            setTitle("Pin Selection");
            toolbar.setTitleTextColor(Color.WHITE);
            messages = new StringBuilder();
            incomingMessage = findViewById(R.id.selectpinvoltage);
            mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
            LocalBroadcastManager.getInstance(this).registerReceiver(mReceiver,new IntentFilter("incomingMessage"));
            LocalBroadcastManager.getInstance(this).registerReceiver(mReceiver2,new IntentFilter("incomingboolean"));
            InputStream is = getResources().openRawResource(R.raw.parsedtest);
            this.myvehicle = getIntent().getParcelableExtra("myvehicle");
            Objects.requireNonNull(this.myvehicle).setConnections(getIntent().<connection>getParcelableArrayListExtra("connections"));
            this.myvehicle.setIs(is);
            preferences = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
            this.textView = findViewById(R.id.connectorid);
            Switch toggle = findViewById(R.id.sortbytoggle);
            toggle.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                @Override
                public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                    /*if(isChecked){
                        if(myvehicle.getLastSorted() == vehicle.SORT_BY_S4) {
                            myvehicle.sortConnections(vehicle.SORT_BY_NAME,getApplicationContext());
                            myvehicle.setLastSorted(vehicle.SORT_BY_NAME);
                        }
                    }
                    else{
                        if(myvehicle.getLastSorted() == vehicle.SORT_BY_NAME){
                            myvehicle.sortConnections(vehicle.SORT_BY_S4,getApplicationContext());
                            myvehicle.setLastSorted(vehicle.SORT_BY_S4);
                        }

                    }*/
                }
            });
        } catch (Resources.NotFoundException e) {
            e.printStackTrace();
        }

    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item){
        if(item.getItemId() == R.id.action_settings){
            Intent i = new Intent(getBaseContext(),settings.class);
            startActivity(i);
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    public boolean onCreateOptionsMenu(Menu menu){
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.toolbarbuttons,menu);
        return true;
    }

    @Override
    public void onResume(){
        super.onResume();
        if(preferences.getBoolean("nightmode",false)){
            nightMode();
        }
        else{
            dayMode();
        }
    }
    @Override
    public void onStart(){
        super.onStart();
        try {
            AsyncTask.execute(new Runnable() {
                @Override
                public void run() {
                    myvehicle.sortConnections(vehicle.SORT_BY_S4,getApplicationContext());
                    if(preferences.getBoolean("nightmode",false)){
                        nightMode();
                    }
                }
            });
            //masterBTStart();

        } catch (Exception ignored) {
        }
    }

    BroadcastReceiver mReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String text = intent.getStringExtra("theMessage");
            messages.append(text).append("\n");
            Toast.makeText(context, text, Toast.LENGTH_SHORT).show();
            incomingMessage.setText(messages);
            //send("R,In4,4,V");
        }
    };
    BroadcastReceiver mReceiver2 = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            boolean bool = intent.getBooleanExtra("boolean",false);
            if(bool){
                buildConnections();
                updatevalues();
            }
        }
    };



    public void buildConnections(){
        String temp= myvehicle.getUniqueConnections().get(myvehicle.getLoc());
        int counter = 0;
        if (!myvehicle.getConnections().isEmpty()) {
            for (connection c : myvehicle.getConnections()) {
                if (c.getDirection().contains(temp.toLowerCase())) {
                    if (!myvehicle.getUniquePins().contains(c.getDirection())) {
                        myvehicle.addUniquePin(c.getDirection());
                        myvehicle.setPinCount(myvehicle.getPinCount() + 1);
                    }
                   if(counter > 0 && connections.get(counter).getS4().equals(connections.get(counter-1).getS4())){
                      connections.get(counter-1).s
                   }
                   else{
                       connections.add(c);
                       counter ++;
                   }
                }
            }
        }

    }
    @SuppressLint("SetTextI18n")
    public void updatevalues(){
        try{
            String temp = myvehicle.getUniqueConnections().get(myvehicle.getLoc());
            String s1 = temp.substring(0, 1).toUpperCase();
            textView.setText(s1 + temp.substring(1));

            LinearLayout layout = findViewById(R.id.pins);
            layout.removeAllViews();
            boolean nightMode = preferences.getBoolean("nightmode",false);
            int counter = 0;
            if (!myvehicle.getConnections().isEmpty()) {
                for (connection c : connections){
                    final Button btn = new Button(this);
                    LinearLayout.LayoutParams textParams = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, 0, 1.5f);
                    textParams.setMarginStart(10);
                    textParams.setMarginEnd(10);
                    btn.setLayoutParams(textParams);
                    btn.setTextSize(14);

                    if(nightMode) {
                        btn.setTextColor(Color.WHITE);
                        btn.setBackgroundResource(R.drawable.nightmodebuttonselector);
                    }
                    else {
                        btn.setTextColor(Color.BLACK);
                        btn.setBackgroundResource(R.drawable.daymodebuttonselector);
                    }
                    btn.setGravity(Gravity.CENTER);
                    btn.setTag(counter);
                    btn.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            pinSelected((Integer) v.getTag());
                        }
                    });
                    btn.setText("Pin" + c.getS4() + " Signal:  " + c.getName());
                    layout.addView(btn);
                    final Space space = new Space(this);
                    space.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, 0, .1f));
                    layout.addView(space);

                    counter++;

                }
            }
            textView = findViewById(R.id.numberofpinstextfield);
            textView.setText(myvehicle.getMap(myvehicle.getUniqueConnections().get(myvehicle.getLoc())) + "p " + myvehicle.inout() + " Connector");


        } catch (Resources.NotFoundException e) {
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

    /*public void masterBTStart(){
        enableBluetooth();
        //enableDiscoverable();
        IntentFilter filter = new IntentFilter(BluetoothDevice.ACTION_BOND_STATE_CHANGED);
        registerReceiver(mBroadcastReceiver4,filter);
        deviceDiscover();
        Log.d(TAG,"masterBTStart: Completed deviceDiscover");
        createBond();

    }
    /*public void startBTConnection(BluetoothDevice device, UUID uuid){
        Log.d(TAG,"startBtConnection: Initializing RFZOM bluetooth connection");
        mBluetoothConnection.startclient(this.mBTDevice, selectpin.uuid);
    }*/


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
        int permissionCheck = this.checkSelfPermission("Manifest.permission.ACCESS_FINE_LOCATION");
        permissionCheck += this.checkSelfPermission("Manifest.permission.ACCESS_COARSE_LOCATION");
        if(permissionCheck != 0){
            this.requestPermissions(new String[]{Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION},101);
        }
        else{
            System.out.println("checkBtPermissions: No need to check permissions.");
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
        if(!mBluetoothAdapter.isDiscovering()) {
            Intent discoverableIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_DISCOVERABLE);
            discoverableIntent.putExtra(BluetoothAdapter.EXTRA_DISCOVERABLE_DURATION, 60);
            startActivity(discoverableIntent);
            IntentFilter intentFilter = new IntentFilter(mBluetoothAdapter.ACTION_SCAN_MODE_CHANGED);
            registerReceiver(mBroadcastReceiver2, intentFilter);
        }
    }

    /*public void createBond(){
        mBluetoothAdapter.cancelDiscovery();
        for(BluetoothDevice device : this.mBTdevices){
            Log.d(TAG,device.getName() + " " + device.getAddress());
            Log.d(TAG,"I AM HERE");
            if(device.getAddress().equals("8C:F1:12:5C:D3:CC")){
                String deviceName = device.getName();
                String deviceAddress = device.getAddress();

                System.out.println("createBond: " + deviceName);
                System.out.println("Createbond: " + deviceAddress);

                device.createBond();
                mBTDevice = device;
                mBluetoothConnection = new BluetoothConnectionService(selectpin.this);
                startBTConnection(mBTDevice,uuid);
                break;
            }
        }

    }*/
    /*
    public void send(String tosend){
        byte[] bytes = tosend.getBytes(Charset.defaultCharset());
        mBluetoothConnection.write(bytes);
    }*/

    public void nightMode(){
        try {
            ConstraintLayout constraintLayout = findViewById(R.id.selectpinconstraintlayout);
            constraintLayout.setBackgroundColor(Color.parseColor("#333333"));
            TextView textView = findViewById(R.id.connectorid);
            textView.setTextColor(Color.WHITE);
            textView = findViewById(R.id.numberofpinstextfield);
            textView.setTextColor(Color.WHITE);
            textView = findViewById(R.id.selectpintextview3);
            textView.setTextColor(Color.WHITE);
            textView = findViewById(R.id.selectpinvoltage);
            textView.setTextColor(Color.WHITE);
            textView = findViewById(R.id.selectpintextview5);
            textView.setTextColor(Color.WHITE);
            LinearLayout linearLayout = findViewById(R.id.selectpinhorizontallayout1);
            linearLayout.setBackgroundResource(R.drawable.nightmodeback);
            textView = findViewById(R.id.connectorid);
            textView.setBackgroundResource(R.drawable.nightmodeback);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void dayMode(){
        try {
            ConstraintLayout constraintLayout = findViewById(R.id.selectpinconstraintlayout);
            constraintLayout.setBackgroundColor(Color.WHITE);
            TextView textView = findViewById(R.id.connectorid);
            textView.setTextColor(Color.BLACK);
            textView = findViewById(R.id.numberofpinstextfield);
            textView.setTextColor(Color.BLACK);
            textView = findViewById(R.id.selectpintextview3);
            textView.setTextColor(Color.BLACK);
            textView = findViewById(R.id.selectpinvoltage);
            textView.setTextColor(Color.BLACK);
            textView = findViewById(R.id.selectpintextview5);
            textView.setTextColor(Color.BLACK);
            LinearLayout linearLayout = findViewById(R.id.selectpinhorizontallayout1);
            linearLayout.setBackgroundResource(R.drawable.back);
            textView = findViewById(R.id.connectorid);
            textView.setBackgroundResource(R.drawable.back);
        } catch (Exception e) {
            e.printStackTrace();
        }
        //updatevalues();

    }

    //Create a broadcast receiver for action_found #1
    private final BroadcastReceiver mBroadcastReceiver1 = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if (action != null && action.equals(BluetoothAdapter.ACTION_STATE_CHANGED)) {
                final int state = intent.getIntExtra(BluetoothAdapter.EXTRA_STATE, BluetoothAdapter.ERROR);
                switch (state) {
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
            if (action != null && action.equals(BluetoothAdapter.ACTION_SCAN_MODE_CHANGED)) {
                final int state = intent.getIntExtra(BluetoothAdapter.EXTRA_SCAN_MODE, BluetoothAdapter.ERROR);
                switch (state) {
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

            if(action != null && action.equals(BluetoothDevice.ACTION_FOUND)){
                BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
                mBTdevices.add(device);
                if (device != null) {
                    System.out.println("onReceive: " + device.getName() + ": " + device.getAddress());
                }
            }
        }
    };

    private BroadcastReceiver mBroadcastReceiver4 = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            final String action = intent.getAction();
            if (action != null && action.equals(BluetoothDevice.ACTION_BOND_STATE_CHANGED)) {
                BluetoothDevice mDevice = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);

                if (mDevice != null && mDevice.getBondState() == BluetoothDevice.BOND_BONDED) {
                    System.out.println("BroadcastReceiver4: Bond bonded");
                    mBTDevice = mDevice;
                }
                if (Objects.requireNonNull(mDevice).getBondState() == BluetoothDevice.BOND_BONDING) {
                    System.out.println("BroadcastReceiver4: Bond bonding");
                }
                if (mDevice.getBondState() == BluetoothDevice.BOND_NONE) {
                    System.out.println("BroadcastReceiver4: Bond none");
                }
            }
        }
    };


}
