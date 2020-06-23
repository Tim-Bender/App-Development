package com.Diagnostic.Spudnik;

import androidx.appcompat.app.AppCompatActivity;

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
}
