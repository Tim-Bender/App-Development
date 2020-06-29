package com.Diagnostic.Spudnik;

import android.bluetooth.BluetoothGattCharacteristic;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;
import android.util.Log;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import java.util.ArrayList;

public class BluetoothTestActivity extends AppCompatActivity {
    private final static String TAG = BluetoothTestActivity.class.getSimpleName();

    public static final String EXTRAS_DEVICE_NAME = "DEVICE_NAME";
    public static final String EXTRAS_DEVICE_ADDRESS = "DEVICE_ADDRESS";

    private TextView ConnectionState;
    private TextView DataField;
    private String DeviceName;
    private String DeviceAddress;
    private BluetoothLeService mBluetoothService;
    private ArrayList<ArrayList<BluetoothGattCharacteristic>> mGattCharacteristics = new ArrayList<>();
    private boolean mConnected = false;
    private BluetoothGattCharacteristic mNotifyCharacteristic;

    private final String LIST_NAME = "Name";
    private final String LIST_UUID = "UUID";
    /*private  BluetoothAdapter mBluetoothAdapter;
    private ArrayList<BluetoothDevice> mBTdevices = new ArrayList<>();
    private BluetoothDevice mBTDevice;
    private final String TAG = "SelectPin";
    private static final UUID uuid = UUID.fromString("8ce255c0-200a-11e0-ac64-0800200c9a66");
    private TextView incomingMessage;
    private StringBuilder messages;*/

    private final ServiceConnection mServiceConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            mBluetoothService = ((BluetoothLeService.LocalBinder)service).getService();
            if(!mBluetoothService.initialize()){
                Log.e(TAG,"Unable to initialize Bluetooth");
                finish();
            }
            mBluetoothService.connect(DeviceAddress);
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            mBluetoothService = null;
        }
    };

    private final BroadcastReceiver mGattUpdateReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            final String action = intent.getAction();
            if(BluetoothLeService.ACTION_GATT_CONNECTED.equals(action)){
                mConnected = true;
                updateConnectionState("CONNECTED");
            }
            else if(BluetoothLeService.ACTION_GATT_DISCONNECTED.equals(action)){
                mConnected = false;
                updateConnectionState("DISCONNECTED");
                clearUI();
            }
           else if(BluetoothLeService.ACTION_DATA_AVAILABLE.equals(action)){
               displayData(intent.getStringExtra(BluetoothLeService.EXTRA_DATA));
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_bluetooth_test);
        ConnectionState = findViewById(R.id.ConnectionState);
        DataField = findViewById(R.id.DataField);
    }

    @Override
    protected void onResume(){
        super.onResume();
        registerReceiver(mGattUpdateReceiver,makeGattUpdateIntentFilter());
        if(mBluetoothService != null){
            final boolean result = mBluetoothService.connect(DeviceAddress);
        }
    }

    @Override
    protected void onPause(){
        super.onPause();
        unregisterReceiver(mGattUpdateReceiver);
    }

    @Override
    protected void onDestroy(){
        super.onDestroy();
        try {
            unbindService(mServiceConnection);
            mBluetoothService = null;
        }catch(Exception ignored){}
    }


    public void updateConnectionState(String update){
        ConnectionState.setText(update);
    }

    public void clearUI(){

    }

    public void displayData(String data){
        DataField.setText(data);

    }

    private static IntentFilter makeGattUpdateIntentFilter(){
        final IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(BluetoothLeService.ACTION_GATT_CONNECTED);
        intentFilter.addAction(BluetoothLeService.ACTION_GATT_DISCONNECTED);
        intentFilter.addAction(BluetoothLeService.ACTION_GATT_SERVICES_DISCOVERED);
        intentFilter.addAction(BluetoothLeService.ACTION_DATA_AVAILABLE);
        return intentFilter;
    }


    /*

        @SuppressLint("SetTextI18n")
    @Override
    protected void onDestroy(){
        super.onDestroy();
        try {
            /*unregisterReceiver(mBroadcastReceiver1);
            unregisterReceiver(mBroadcastReceiver2);
            unregisterReceiver(mBroadcastReceiver3);
            unregisterReceiver(mBroadcastReceiver4);
            unregisterReceiver(mReceiver);
    unregisterReceiver(mReceiver2);
} catch (Exception ignored) {}

        }
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
    Create a broadcast receiver for action_found #2
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


     public void masterBTStart(){
        enableBluetooth();
        //enableDiscoverable();
        IntentFilter filter = new IntentFilter(BluetoothDevice.ACTION_BOND_STATE_CHANGED);
        registerReceiver(mBroadcastReceiver4,filter);
        deviceDiscover();
        Log.d(TAG,"masterBTStart: Completed deviceDiscover");
        createBond();

    }
    public void startBTConnection(BluetoothDevice device, UUID uuid){
        Log.d(TAG,"startBtConnection: Initializing RFZOM bluetooth connection");
        mBluetoothConnection.startclient(this.mBTDevice, selectpin.uuid);
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
            IntentFilter intentFilter = new IntentFilter(BluetoothAdapter.ACTION_SCAN_MODE_CHANGED);
            registerReceiver(mBroadcastReceiver2, intentFilter);
        }
    }

    public void createBond(){
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

    }

    public void send(String tosend){
        byte[] bytes = tosend.getBytes(Charset.defaultCharset());
        mBluetoothConnection.write(bytes);
    }
    /*private BroadcastReceiver mBroadcastReceiver3 = new BroadcastReceiver() {
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
     */
}
