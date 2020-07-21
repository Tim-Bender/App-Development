package com.Diagnostic.Spudnik;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCallback;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothManager;
import android.bluetooth.BluetoothProfile;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.AsyncTask;
import android.os.Handler;

import java.util.ArrayList;

class BluetoothLeServiceTest {
    private final Context context;
    private BluetoothAdapter adapter;
    private Handler handler;
    private ArrayList<BluetoothDevice> bluetoothDevices;
    private ArrayList<String> bluetoothDeviceNames;
    private BluetoothDevice device;
    private BluetoothAdapter.LeScanCallback leScanCallback;

    BluetoothLeServiceTest(Context context){
        this.context = context;
        handler = new Handler();
        setUpBluetooth();
        leScanCallback = new BluetoothAdapter.LeScanCallback() {
            @Override
            public void onLeScan(final BluetoothDevice device, int rssi, byte[] scanRecord) {
                AsyncTask.execute(new Runnable() {
                    @Override
                    public void run() {
                        if (device.getName() != null){// && !bluetoothDeviceNames.contains(device.getName())) {
                            bluetoothDevices.add(device);
                            bluetoothDeviceNames.add(device.getName());
                            System.out.println("BLUETOOTH DEVICE DISCOVERED: " + device.getName());
                        }
                    }
                });
            }
        };
        bluetoothDevices = new ArrayList<>();
        bluetoothDeviceNames = new ArrayList<>();
    }
    BluetoothLeServiceTest(Context context,BluetoothAdapter bluetoothAdapter,BluetoothDevice bluetoothDevice){
        this.context = context;
        handler = new Handler();
        leScanCallback = new BluetoothAdapter.LeScanCallback() {
            @Override
            public void onLeScan(final BluetoothDevice device, int rssi, byte[] scanRecord) {
                AsyncTask.execute(new Runnable() {
                    @Override
                    public void run() {
                        if (device.getName() != null) {
                            bluetoothDevices.add(device);
                            System.out.println("BLUETOOTH DEVICE DISCOVERED: " + device.getName());
                        }
                    }
                });
            }
        };
        bluetoothDevices = new ArrayList<>();
        bluetoothDeviceNames = new ArrayList<>();
        adapter = bluetoothAdapter;
        device = bluetoothDevice;
    }

     private void setUpBluetooth(){
         AsyncTask.execute(new Runnable() {
             @Override
             public void run() {
                 if(checkBluetoothCompatible()){
                     BluetoothManager manager = (BluetoothManager) context.getSystemService(Context.BLUETOOTH_SERVICE);
                     adapter = manager.getAdapter();
                 }
                 if(adapter == null || !adapter.isEnabled()){
                     Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
                     context.startActivity(enableBtIntent);
                 }
             }
         });
     }

     private boolean checkBluetoothCompatible(){
         return context.getPackageManager().hasSystemFeature(PackageManager.FEATURE_BLUETOOTH_LE);
     }
     private boolean scanning;
     private static final long SCAN_PERIOD = 100000;

    public void scanLeDevice(final boolean enable){
        if(enable){
            handler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    scanning = false;
                    adapter.stopLeScan(leScanCallback);
                }
            },SCAN_PERIOD);
            scanning = true;
            adapter.startLeScan(leScanCallback);
        }
        else{
            scanning = false;
            adapter.stopLeScan(leScanCallback);
        }
    }

     private String deviceAddress;
     private BluetoothGatt bluetoothGatt;
     private int connectionState = STATE_DISCONNECTED;

     private final static int STATE_DISCONNECTED = 0, STATE_CONNECTING = 1, STATE_CONNECTED = 2;

     public final static String ACTION_GATT_CONNECTED =
             "com.Diagnostic.Spudnik.le.ACTION_GATT_CONNECTED";
     public final static String ACTION_GATT_DISCONNECTED =
             "com.Diagnostic.Spudnik.le.ACTION_GATT_DISCONNECTED";
     public final static String ACTION_GATT_SERVICES_DISCOVERED =
             "com.Diagnostic.Spudnik.le.ACTION_GATT_SERVICES_DISCOVERED";
     public final static String ACTION_DATA_AVAILABLE =
             "com.Diagnostic.Spudnik.le.ACTION_DATA_AVAILABLE";
     public final static String EXTRA_DATA =
             "com.Diagnostic.Spudnik.le.EXTRA_DATA";

     //public final static UUID SPUDNIK_DIAGNOSTIC_UUID = UUID.fromString("MyDeviceName");

     private final BluetoothGattCallback gattCallback = new BluetoothGattCallback() {
         @Override
         public void onConnectionStateChange(BluetoothGatt gatt, int status, int newState) {
             String intentAction;
             if(newState == BluetoothProfile.STATE_CONNECTED){
                 intentAction = ACTION_GATT_CONNECTED;
                 connectionState = STATE_CONNECTED;
                 broadcastUpdate(intentAction);
             }
             else if(newState == BluetoothProfile.STATE_DISCONNECTED){
                 intentAction = ACTION_GATT_DISCONNECTED;
                 connectionState = STATE_DISCONNECTED;
                 broadcastUpdate(intentAction);
             }
         }

         @Override
         public void onServicesDiscovered(BluetoothGatt gatt, int status){
             if(status == BluetoothGatt.GATT_SUCCESS){
                 broadcastUpdate(ACTION_GATT_SERVICES_DISCOVERED);
             }
             else{
                 System.out.println("onServicesDiscovered received: " + status);
             }
         }

         @Override
         public void onCharacteristicRead(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic,int status){
             if(status == BluetoothGatt.GATT_SUCCESS){
                 //broadcastUpdate(ACTION_DATA_AVAILABLE,characteric);
             }
         }
     };

     private void broadcastUpdate(final String action){
         final Intent intent = new Intent(action);
         context.sendBroadcast(intent);
     }

     private void broadcastUpdate(final String action, final BluetoothGattCharacteristic characteristic){
         final Intent intent = new Intent(action);
        /*if(SPUDNIK_DIAGNOSTIC_UUID.equals(characteristic.getUuid())){
             //formatting here
         }
         else{
             final byte[] data = characteristic.getValue();
             if(data != null && data.length > 0){
                 final StringBuilder stringBuilder = new StringBuilder(data.length);
                 for(byte bit : data){
                     stringBuilder.append(String.format("%02X ", bit));

                 }
             }
         }*/
         context.sendBroadcast(intent);
     }



 }
