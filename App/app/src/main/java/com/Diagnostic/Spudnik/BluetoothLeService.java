package com.Diagnostic.Spudnik;

import android.app.Service;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCallback;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattService;
import android.bluetooth.BluetoothManager;
import android.bluetooth.BluetoothProfile;
import android.content.Context;
import android.content.Intent;
import android.os.Binder;
import android.os.Handler;
import android.os.IBinder;
import android.util.Log;

import java.util.List;
import java.util.UUID;

public class BluetoothLeService extends Service {

    private final static String TAG = BluetoothLeService.class.getSimpleName();
    private BluetoothManager bluetoothManager;
    private BluetoothAdapter adapter;
    private String deviceAddress;
    private BluetoothGatt bluetoothGatt;
    private int connectedState = STATE_DISCONNECTED;

    private static final int STATE_DISCONNECTED = 0;
    private static final int STATE_CONNECTING = 1;
    private static final int STATE_CONNECTED = 2;

    public final static String ACTION_GATT_CONNECTED =
            "com.example.bluetooth.le.ACTION_GATT_CONNECTED";
    public final static String ACTION_GATT_DISCONNECTED =
            "com.example.bluetooth.le.ACTION_GATT_DISCONNECTED";
    public final static String ACTION_GATT_SERVICES_DISCOVERED =
            "com.example.bluetooth.le.ACTION_GATT_SERVICES_DISCOVERED";
    public final static String ACTION_DATA_AVAILABLE =
            "com.example.bluetooth.le.ACTION_DATA_AVAILABLE";
    public final static String EXTRA_DATA =
            "com.example.bluetooth.le.EXTRA_DATA";

    public final static UUID UUID_HEART_RATE_MEASUREMENT =
            UUID.fromString("123e4567-e89b-12d3-a456-426652340000");

    private final BluetoothGattCallback gattCallback = new BluetoothGattCallback() {
        @Override
        public void onConnectionStateChange(BluetoothGatt gatt, int status, int newState) {
            String intentAction;
            if(newState == BluetoothProfile.STATE_CONNECTED) {
                intentAction = ACTION_GATT_CONNECTED;
                connectedState = STATE_CONNECTED;
                broadcastUpdate(intentAction);
                Log.i(TAG, "Connected to GATT server.");
                Log.i(TAG, "Attempting to start service discovery:" +
                        bluetoothGatt.discoverServices());
            }
            else if(newState == BluetoothProfile.STATE_DISCONNECTED){
                intentAction = ACTION_GATT_DISCONNECTED;
                connectedState = STATE_DISCONNECTED;
                Log.i(TAG,"Disconnected from GATT server");
                broadcastUpdate(intentAction);
            }
        }

        private boolean mScanning;
        private Handler handler;

        private static final long SCAN_PERIOD = 10000;

        //implement scan for devices here!

        @Override
        public void onServicesDiscovered(BluetoothGatt gatt, int status){
            if(status == BluetoothGatt.GATT_SUCCESS){
                broadcastUpdate(ACTION_GATT_SERVICES_DISCOVERED);
            }
            else{
                Log.w(TAG,"onservicesDiscovered received: " + status);
            }
        }

        @Override
        public void onCharacteristicRead(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic,int status){
            if(status == BluetoothGatt.GATT_SUCCESS){
                broadcastUpdate(ACTION_DATA_AVAILABLE,characteristic);
            }
        }

        @Override
        public void onCharacteristicChanged(BluetoothGatt gatt,BluetoothGattCharacteristic characteristic){
            broadcastUpdate(ACTION_DATA_AVAILABLE,characteristic);
        }
    };

    private void broadcastUpdate(final String action){
        final Intent intent = new Intent(action);
        sendBroadcast(intent);
    }

    private void broadcastUpdate(final String action, final BluetoothGattCharacteristic characteristic){
        final Intent intent = new Intent(action);

        final byte[] data = characteristic.getValue();
        if(data != null && data.length > 0){
            final StringBuilder stringBuilder = new StringBuilder(data.length);
            for(byte byteChar : data){
                stringBuilder.append(String.format("%02X ",byteChar));
            }
            intent.putExtra(EXTRA_DATA,new String(data) + "\n" + stringBuilder.toString());
        }
        sendBroadcast(intent);
    }

    public class LocalBinder extends Binder{
        BluetoothLeService getService(){
            return BluetoothLeService.this;
        }

    }

    @Override
    public IBinder onBind(Intent intent){
        return mBinder;
    }

    @Override
    public boolean onUnbind(Intent intent){
        close();
        return super.onUnbind(intent);
    }

    private final IBinder mBinder = new LocalBinder();

    public boolean initialize(){
        if(bluetoothManager == null){
            bluetoothManager = (BluetoothManager)getSystemService(Context.BLUETOOTH_SERVICE);
            if(bluetoothManager == null){
                Log.e(TAG,"Unable to initialize BluetoothManager.");
                return false;
            }
        }

        adapter = bluetoothManager.getAdapter();
        if(adapter == null){
            Log.e(TAG,"Unable to optain BT Adapter");
            return false;
        }
        return true;
    }

    public boolean connect(final String address){

        if(adapter == null || address == null){
            Log.w(TAG,"BluetoothAdapter not initialized or unsepecified address");
            return false;
        }

        if(address.equals(deviceAddress) && bluetoothGatt != null){
            Log.d(TAG,"Tryingto use an existing Gatt for connection.");
            if(bluetoothGatt.connect()){
                connectedState = STATE_CONNECTING;
                return true;
            }
            else{
                return false;
            }
        }

        final BluetoothDevice device = adapter.getRemoteDevice(address);
        if(device == null){
            Log.w(TAG,"Device not found. Unable to connect.");
            return false;
        }

        bluetoothGatt = device.connectGatt(this,false,gattCallback);
        Log.w(TAG,"trying to create a new connection.");
        deviceAddress = address;
        connectedState = STATE_CONNECTING;
        return true;
    }

    public void disconnect(){
        if(adapter == null || bluetoothGatt == null) {
            Log.w(TAG, "BluetoothAdapter not initialized");
            return;
        }
        bluetoothGatt.disconnect();
    }

    public void close(){
        if(bluetoothGatt == null){
            return;
        }
        bluetoothGatt.close();
        bluetoothGatt = null;
    }

    public void readCharacteristic(BluetoothGattCharacteristic characteristic){
        if(adapter == null || bluetoothGatt == null){
            Log.w(TAG,"Bluetooth Adapter not initialized");
            return;
        }
        bluetoothGatt.readCharacteristic(characteristic);
    }

    public void setCharacteristicNotification(BluetoothGattCharacteristic characteristic,boolean enabled){
        if(adapter == null || bluetoothGatt == null){
            Log.w(TAG,"BluetoothAdapter not initialized");
            return;
        }
        bluetoothGatt.setCharacteristicNotification(characteristic,enabled);

    }

    public List<BluetoothGattService> getSupportedGattServices(){
        if(bluetoothGatt == null) return null;
        return bluetoothGatt.getServices();
    }

}
