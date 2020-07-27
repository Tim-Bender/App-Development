
/*
 *
 *  Copyright (c) 2020, Spudnik LLc <https://www.spudnik.com/>
 *  All rights reserved.
 *
 *  Redistribution and use in source and binary forms, with or without
 *  modification, are not permitted in any form.
 *
 *  DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS BE LIABLE FOR
 *  ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 *  (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
 *  LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION, DEATH, or SERIOUS INJURY or DAMAGE)
 *  HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY,
 *  OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 *  SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 *
 */

package com.Diagnostic.Spudnik;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCallback;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattService;
import android.bluetooth.BluetoothManager;
import android.bluetooth.BluetoothProfile;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.AsyncTask;

import androidx.annotation.NonNull;

import java.util.Arrays;
import java.util.UUID;

class BluetoothLeServiceTest {
    private Context context;
    private BluetoothAdapter adapter;
    private OperationManager operationManager;
    private BluetoothAdapter.LeScanCallback leScanCallback;
    @SuppressWarnings("unused")
    private final static UUID SYSTEMINFORMATIONUUID = UUID.fromString("0edc348b-399a-ba9c-8f48-4d3a594c4b0a");

    @SuppressWarnings("unused")
    private final static UUID WRITESERVICEUUID = UUID.fromString("30573506-7bda-3893-7c48-b6f8ebd963ad");
    @SuppressWarnings("unused")
    private final static UUID WRITECHARACTERISTICUUID = UUID.fromString("9cc2bcf8-ee9d-40b1-704a-0befaef5e9e9"); //write data

    @SuppressWarnings("unused")
    private final static UUID READSERVICEUUID = UUID.fromString("2940f3d6-7439-1bbc-4b4b-28bd6780d17d");
    @SuppressWarnings("unused")
    private final static UUID READINPUTDATACHARACTERISTICUUID = UUID.fromString("0edc348b-399a-ba9c-8f48-4d3a594c4b0a"); //read all input data
    @SuppressWarnings("unused")
    private final static UUID READOUTPUTDATACHARACTERISTICUUID = UUID.fromString("645fb84b-7b42-ea90-664c-1b7031eef5b2"); //all STD output data
    @SuppressWarnings("unused")
    private final static UUID READSETTINGSCHARACTERISTICUUID = UUID.fromString("5fa78df8-ed5c-59a5-d343-3a596c2dcfa5"); //read settings data
    @SuppressWarnings("unused")
    private final static UUID READSPOUTPUTCHARACTERISTICUUID = UUID.fromString("7e621674-082a-4aa3-b847-4ff71309b472"); //SP output connector data 4-9

    private boolean connecting = false;

    BluetoothLeServiceTest(Context context) {
        AsyncTask.execute(() ->{
            this.context = context;
            leScanCallback = (device, rssi, scanRecord) -> {
                if (device.getName() != null && !connecting) {
                    if (device.getName().equals("test_dev_1")) {
                        System.out.println("Connecting to: " + device.getName());
                        adapter.stopLeScan(leScanCallback);
                        connecting = true;
                        try {
                            Thread.sleep(1000);
                        } catch (InterruptedException ignored) {

                        }
                        device.connectGatt(context, false, gattCallback);
                    }
                }
            };
            if (checkBluetoothCompatible()) {
                setUpBluetooth();
                scanLeDevice(true);
            }
        });
    }


    private void setUpBluetooth() {
        if (checkBluetoothCompatible()) {
            BluetoothManager manager = (BluetoothManager) context.getSystemService(Context.BLUETOOTH_SERVICE);
            adapter = manager.getAdapter();
        }
        if (adapter == null || !adapter.isEnabled()) {
            Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            context.startActivity(enableBtIntent);
        }
    }

    private boolean checkBluetoothCompatible() {
        return context.getPackageManager().hasSystemFeature(PackageManager.FEATURE_BLUETOOTH_LE);
    }


    public void scanLeDevice(final boolean enable) {
        if (enable) {
            adapter.startLeScan(leScanCallback);
            System.out.println("SCAN STARTED");
        } else {
            adapter.stopLeScan(leScanCallback);
            System.out.println("SCAN STOPPEd");
        }
    }

    public final static String ACTION_GATT_CONNECTED =
            "com.Diagnostic.Spudnik.le.ACTION_GATT_CONNECTED";
    public final static String ACTION_GATT_DISCONNECTED =
            "com.Diagnostic.Spudnik.le.ACTION_GATT_DISCONNECTED";
    public final static String ACTION_GATT_SERVICES_DISCOVERED =
            "com.Diagnostic.Spudnik.le.ACTION_GATT_SERVICES_DISCOVERED";
    public final static String ACTION_CHARACTERISTIC_READ =
            "com.Diagnostic.Spudnik.le.ACTION_CHARACTERISTIC_READ";

    private final BluetoothGattCallback gattCallback = new BluetoothGattCallback() {
        @Override
        public void onConnectionStateChange(BluetoothGatt gatt, int status, int newState) {
            String intentAction;
            if (newState == BluetoothProfile.STATE_CONNECTED) {
                intentAction = ACTION_GATT_CONNECTED;
                operationManager = new OperationManager(gatt);
                operationManager.request(new Operation(null, Operation.DISCOVER_SERVICES, -1));
                broadcastUpdate(intentAction);
                System.out.println("CONNECTION STATE Connected");
            } else {
                intentAction = ACTION_GATT_DISCONNECTED;
                broadcastUpdate(intentAction);
                connecting = false;
                System.out.println("CONNECTION STATE Distconnected");
            }
        }


        @Override
        public void onServicesDiscovered(BluetoothGatt gatt, int status) {
            if (status == BluetoothGatt.GATT_SUCCESS) {
                System.out.println("SERVICESDISCOVERED SUCCESS");
                for (BluetoothGattService s : gatt.getServices()) {
                    System.out.println("SERVICE UUID: " + s.getUuid());
                    if (s.getUuid().toString().equals(READSERVICEUUID.toString())) {
                        operationManager.setReadService(s);
                        /*for (BluetoothGattCharacteristic c : s.getCharacteristics()) {
                            System.out.println("CHARACTERISTIC: " + c.getUuid());
                        }*/
                    } else if (s.getUuid().toString().equals(WRITESERVICEUUID.toString())) {
                        operationManager.setWriteService(s);
                        /*for (BluetoothGattCharacteristic c : s.getCharacteristics()) {
                            System.out.println("CHARACTERISTIC: " + c.getUuid());
                            System.out.println("CHARACTERISTIC VALUE " + Arrays.toString(c.getValue()));
                        }*/
                    }
                }
                broadcastUpdate(ACTION_GATT_SERVICES_DISCOVERED);

            } else {
                System.out.println("SERFICESDISCOVERED FAILURE");
                operationManager.request(new Operation(null, Operation.DISCOVER_SERVICES, -1));
            }
            operationManager.operationCompleted();

        }

        @Override
        public void onCharacteristicRead(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic, int status) {
            if (status == BluetoothGatt.GATT_SUCCESS) {
                //format stuffs
                broadcastUpdate(ACTION_CHARACTERISTIC_READ, characteristic);
                System.out.println("CHARACTERISTIC READ SUCCESS");
            } else {
                System.out.println("CHARACTERISTIC READ FAILURE");
            }
            System.out.println("VALUE: " + Arrays.toString(characteristic.getValue()));
            System.out.println("STATUS: " + status);
            System.out.println("PROPERTIES " + characteristic.getProperties());
            operationManager.operationCompleted();
        }

        @Override
        public void onCharacteristicWrite(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic, int status) {
            if (status == BluetoothGatt.GATT_SUCCESS) {
                System.out.println("WRITE CHARACTERISTIC SUCCESS");
                broadcastUpdate(ACTION_CHARACTERISTIC_READ, characteristic);
            } else {
                System.out.println("WRITE CHARACTERSTISTIC FAILURE");
            }
            operationManager.operationCompleted();
        }
    };


    private void broadcastUpdate(final String action) {
        final Intent intent = new Intent(action);
        context.sendBroadcast(intent);
    }

    private void broadcastUpdate(@NonNull final String action, @SuppressWarnings("unused") @NonNull final BluetoothGattCharacteristic characteristic) {
        final Intent intent = new Intent(action);
        intent.putExtra("bytes", characteristic.getValue());
        context.sendBroadcast(intent);
    }

    public void requestConnectorVoltage(connection c) {
        if (c.inout().equals("Output")) {
            operationManager.request(new Operation(READOUTPUTDATACHARACTERISTICUUID, Operation.READ_CHARACTERISTIC, -1));
        } else {
            operationManager.request(new Operation(READINPUTDATACHARACTERISTICUUID, Operation.READ_CHARACTERISTIC, -1));
        }
    }

    public void disconnect() {
        scanLeDevice(false);
        if (operationManager != null) {
            operationManager.request(new Operation(null, Operation.CLOSE_CONNECTION, -1));
            operationManager.request(new Operation(null, Operation.DISCONNECT, -1));
        }
    }


}
