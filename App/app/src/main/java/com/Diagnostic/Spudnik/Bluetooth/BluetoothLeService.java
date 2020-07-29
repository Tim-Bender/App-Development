
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

package com.Diagnostic.Spudnik.Bluetooth;

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

import androidx.annotation.NonNull;

import com.Diagnostic.Spudnik.CustomObjects.Connection;

import java.util.Arrays;

public class BluetoothLeService {

    private Context context;
    private BluetoothAdapter adapter;
    private static OperationManager operationManager;
    public static BluetoothAdapter.LeScanCallback leScanCallback;
    private BluetoothGatt server;
    private boolean connecting = false;


    @SuppressWarnings("unused")
    public final byte IDLETYPE = 0x00;
    @SuppressWarnings("unused")
    public final byte ONOFFTYPE = 0x01;
    @SuppressWarnings("unused")
    public final byte CURRENTTYPE = 0x02;
    @SuppressWarnings("unused")
    public final byte VOLTAGETYPE = 0x03;
    @SuppressWarnings("unused")
    public final byte FREQUENCYTYPE = 0x04;
    @SuppressWarnings("unused")
    public final byte COUNTSTYPE = 0x05;
    public final static String ACTION_GATT_CONNECTED =
            "com.Diagnostic.Spudnik.le.ACTION_GATT_CONNECTED";
    public final static String ACTION_GATT_DISCONNECTED =
            "com.Diagnostic.Spudnik.le.ACTION_GATT_DISCONNECTED";
    public final static String ACTION_GATT_SERVICES_DISCOVERED =
            "com.Diagnostic.Spudnik.le.ACTION_GATT_SERVICES_DISCOVERED";
    @SuppressWarnings("unused")
    public final static String ACTION_GATT_SERVICES_DISCOVERED_FAILURE =
            "com.Diagnostic.Spudnik.le.ACTION_GATT_SERVICES_DISCOVERED_FAILURE";
    public final static String ACTION_CHARACTERISTIC_READ =
            "com.Diagnostic.Spudnik.le.ACTION_CHARACTERISTIC_READ";
    @SuppressWarnings("unused")
    private final static String ACTION_CHARACTERISTIC_READ_FAILURE =
            "com.Diagnostic.Spudnik.le.ACTION_CHARACTERISTIC_READ_FAILURE";
    private final static String ACTION_AUTORECONNECT_COMPLETE =
            "com.Diagnostic.Spudnik.le.ACTION_AUTORECONNECT_COMPLETE";

    public BluetoothLeService(Context context) {
        this.context = context;
        leScanCallback = (device, rssi, scanRecord) -> {
            if (device.getName() != null && !connecting) {
                if (device.getName().equals("s4_diag_tool")) {
                    connecting = true;
                    System.out.println("Connecting to: " + device.getName());
                    scanLeDevice(false);
                    device.connectGatt(context, false, gattCallback);
                }
            }
        };
        if (checkBluetoothCompatible()) {
            setUpBluetooth();
            scanLeDevice(true);
        }
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
            System.out.println(leScanCallback);
            adapter.startLeScan(leScanCallback);
            System.out.println("SCAN STARTED");
        } else {
            adapter.stopLeScan(leScanCallback);
            System.out.println("SCAN STOPPED");
        }
    }

    private final BluetoothGattCallback gattCallback = new BluetoothGattCallback() {

        @Override
        public void onMtuChanged(BluetoothGatt gatt, int mtu, int status) {
            try {
                if (status == BluetoothGatt.GATT_SUCCESS && operationManager != null) {
                    System.out.println("MTU CHANGED TO: " + mtu);
                    operationManager.request(new Operation(null, Operation.DISCOVER_SERVICES, null));
                }
            } catch (Exception e) {
                autoReconnect();
                e.printStackTrace();
            } finally {
                if(operationManager != null)
                operationManager.operationCompleted();
            }
        }

        @Override
        public void onConnectionStateChange(BluetoothGatt gatt, int status, int newState) {
            try {
                String intentAction;
                if (newState == BluetoothProfile.STATE_CONNECTED) {
                    server = gatt;
                    operationManager = new OperationManager(gatt);
                    broadcastUpdate(ACTION_GATT_CONNECTED);
                    System.out.println("CONNECTION STATE Connected");
                    operationManager.request(new Operation(null, Operation.REQUEST_MTU, null));
                } else if (newState != BluetoothProfile.STATE_CONNECTING) {
                    intentAction = ACTION_GATT_DISCONNECTED;
                    broadcastUpdate(intentAction);
                    connecting = false;
                    autoReconnect();
                    System.out.println("CONNECTION STATE Disconnected");
                }
            } catch (Exception e) {
                e.printStackTrace();
                autoReconnect();
            } finally {
                if(operationManager != null)
                operationManager.operationCompleted();
            }
        }

        @Override
        public void onServicesDiscovered(BluetoothGatt gatt, int status) {
            try {
                if (status == BluetoothGatt.GATT_SUCCESS && operationManager != null) {
                    for (BluetoothGattService s : gatt.getServices()) {
                        if (s.getUuid().toString().equals(UUIDConstants.SERVICE_UUID.getFromString().toString())) {
                            operationManager.setService(s);
                        }
                    }
                    broadcastUpdate(ACTION_GATT_SERVICES_DISCOVERED);

                } else {
                    broadcastUpdate(ACTION_GATT_SERVICES_DISCOVERED_FAILURE);
                    autoReconnect();
                }
            } catch (Exception e) {
                e.printStackTrace();
                autoReconnect();
            } finally {
                if(operationManager != null)
                operationManager.operationCompleted();
            }

        }

        @Override
        public void onCharacteristicRead(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic, int status) {
            try {
                if (status == BluetoothGatt.GATT_SUCCESS) {
                    System.out.println("CHARACTERISTIC READ SUCCESS");
                    long[] l = new long[characteristic.getValue().length];
                    for (int i = 0; i < characteristic.getValue().length; i++) {
                        l[i] = characteristic.getValue()[i] % 0xFFFFFFFFL;
                    }
                    System.out.println("VALUE: " + Arrays.toString(l));
                    broadcastUpdate(ACTION_CHARACTERISTIC_READ, characteristic);
                } else {
                    System.out.println("CHARACTERISTIC READ FAILURE");
                    broadcastUpdate(ACTION_CHARACTERISTIC_READ_FAILURE);
                }
            } catch (Exception e) {
                e.printStackTrace();
                broadcastUpdate(ACTION_CHARACTERISTIC_READ_FAILURE);
                autoReconnect();
            } finally {
                if(operationManager != null)
                operationManager.operationCompleted();
            }
        }

        @Override
        public void onCharacteristicWrite(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic, int status) {
            try {
                if (status == BluetoothGatt.GATT_SUCCESS) {
                    //if
                    System.out.println("WRITE CHARACTERISTIC SUCCESS");
                    broadcastUpdate(ACTION_CHARACTERISTIC_READ);
                } else {
                    System.out.println("WRITE CHARACTERSTISTIC FAILURE");
                    broadcastUpdate(ACTION_CHARACTERISTIC_READ_FAILURE);
                }
            } catch (Exception e) {
                e.printStackTrace();
                autoReconnect();
            } finally {
                if(operationManager != null)
                operationManager.operationCompleted();
            }
        }
    };


    private void broadcastUpdate(final String action) {
        final Intent intent = new Intent(action);
        context.sendBroadcast(intent);
    }

    private void broadcastUpdate(@NonNull final String action,
                                 @SuppressWarnings("unused") @NonNull final BluetoothGattCharacteristic characteristic) {
        final Intent intent = new Intent(action);
        intent.putExtra("bytes", characteristic.getValue());
        context.sendBroadcast(intent);
    }

    public void requestConnectorVoltage(@NonNull @SuppressWarnings("unused") Connection c) {
        //first write the type, then pull data
        Packet myPacket = new Packet(PacketConstants.WRITE_SENSOR_CONFIGURATION.getBytes());
        myPacket.SetSensorType(IDLETYPE);
        operationManager.request(new Operation(UUIDConstants.WRITE_SENSOR_CONFIGURATION.getFromString(), Operation.WRITE_CHARACTERISTIC, myPacket));
        operationManager.request(new Operation(UUIDConstants.READ_STANDARD_CONTROL_STATUS.getFromString(), Operation.READ_CHARACTERISTIC, null));
    }

    public void disconnect() {
        if (server != null) {
            server.disconnect();
        }
    }

    private void autoReconnect(){
        try {
            if(server != null)
            server.disconnect();
            if(operationManager != null) {
                operationManager.reset();
                operationManager = null;
            }
            adapter = null;
            server = null;
            connecting = false;
            checkBluetoothCompatible();
            setUpBluetooth();
            scanLeDevice(true);
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            broadcastUpdate(ACTION_AUTORECONNECT_COMPLETE);
        }
    }

}
