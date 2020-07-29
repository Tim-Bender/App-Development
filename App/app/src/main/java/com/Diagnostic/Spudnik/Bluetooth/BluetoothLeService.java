
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

import java.util.ArrayList;
import java.util.Arrays;

public class BluetoothLeService {

    private Context context;
    private BluetoothAdapter adapter;
    private static OperationManager operationManager;
    public static BluetoothAdapter.LeScanCallback leScanCallback;
    private BluetoothGatt server;
    private boolean connecting = false;

    public final byte IDLETYPE = 0x00;
    public final byte ONOFFTYPE = 0x01;
    public final byte CURRENTTYPE = 0x02;
    public final byte VOLTAGETYPE = 0x03;
    public final byte FREQUENCYTYPE = 0x04;
    public final byte COUNTSTYPE = 0x05;
    public final byte PULSEWIDTHTYPE = 0x08;
    public Connection connection;

    private static boolean USER_DISCONNECT = false;
    private static boolean WRITTEN = false;

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

    public static boolean SCANNING = false;
    public synchronized void scanLeDevice(final boolean enable) {
        if (enable) {
            SCANNING = true;
            System.out.println(leScanCallback);
            adapter.startLeScan(leScanCallback);
            System.out.println("SCAN STARTED");
        } else {
            adapter.stopLeScan(leScanCallback);
            SCANNING = false;
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
                if (operationManager != null)
                    operationManager.operationCompleted();
            }
        }

        @Override
        public void onConnectionStateChange(BluetoothGatt gatt, int status, int newState) {
            try {
                if (newState == BluetoothProfile.STATE_CONNECTED) {
                    server = gatt;
                    operationManager = new OperationManager(gatt);
                    broadcastUpdate(BroadcastActionConstants.ACTION_GATT_CONNECTED.getString());
                    System.out.println("CONNECTION STATE Connected");
                    operationManager.request(new Operation(null, Operation.REQUEST_MTU, null));
                } else if (newState != BluetoothProfile.STATE_CONNECTING) {
                    broadcastUpdate(BroadcastActionConstants.ACTION_GATT_DISCONNECTED.getString());
                    connecting = false;
                    if (!USER_DISCONNECT)
                        autoReconnect();
                    System.out.println("CONNECTION STATE Disconnected");
                }
            } catch (Exception e) {
                e.printStackTrace();
                autoReconnect();
            } finally {
                if (operationManager != null)
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
                    broadcastUpdate(BroadcastActionConstants.ACTION_GATT_SERVICES_DISCOVERED.getString());

                } else {
                    broadcastUpdate(BroadcastActionConstants.ACTION_GATT_SERVICES_DISCOVERED_FAILURE.getString());
                    autoReconnect();
                }
            } catch (Exception e) {
                e.printStackTrace();
                autoReconnect();
            } finally {
                if (operationManager != null)
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
                    broadcastUpdate(BroadcastActionConstants.ACTION_CHARACTERISTIC_READ.getString(), characteristic);
                } else {
                    System.out.println("CHARACTERISTIC READ FAILURE");
                    broadcastUpdate(BroadcastActionConstants.ACTION_CHARACTERISTIC_READ_FAILURE.getString());
                }
            } catch (Exception e) {
                e.printStackTrace();
                broadcastUpdate(BroadcastActionConstants.ACTION_CHARACTERISTIC_READ_FAILURE.getString());
                autoReconnect();
            } finally {
                if (operationManager != null)
                    operationManager.operationCompleted();
            }
        }

        @Override
        public void onCharacteristicWrite(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic, int status) {
            try {
                if (status == BluetoothGatt.GATT_SUCCESS) {
                    WRITTEN = true;
                    System.out.println("WRITE CHARACTERISTIC SUCCESS");
                    broadcastUpdate(BroadcastActionConstants.ACTION_CHARACTERISTIC_WRITE.getString());
                } else {
                    System.out.println("WRITE CHARACTERSTISTIC FAILURE");
                    broadcastUpdate(BroadcastActionConstants.ACTION_CHARACTERISTIC__WRITE_FAILURE.getString());
                }
            } catch (Exception e) {
                e.printStackTrace();
                autoReconnect();
            } finally {
                if (operationManager != null)
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

    public void requestConnectorVoltage(@NonNull Connection c) {
        //first write the type, then pull data
        Packet myPacket = new Packet(getDefaultPacket(c).getBytes());
        if (connection == null)
            connection = c;
        ArrayList<UUIDConstants> uuids = getUuidSet(c);
        myPacket.SetSensorType(getType(c));
        System.out.println("TYPE: " + getType(c));
        System.out.println("WRITE UUID: " + uuids.get(0));
        if (uuids.size() == 2)
            System.out.println("READ UUID: " + uuids.get(1));
        if (!WRITTEN)
            operationManager.request(new Operation(uuids.get(0).getFromString(), Operation.WRITE_CHARACTERISTIC, myPacket));
        operationManager.request(new Operation(uuids.get(1).getFromString(), Operation.READ_CHARACTERISTIC, null));
    }

    private PacketConstants getDefaultPacket(@NonNull Connection c) {
        if (c.inout().equals("Input"))
            return PacketConstants.WRITE_SENSOR_CONFIGURATION;
        else if (c.getConnectionNumber() < 4 || c.getDirection().trim().contains("exp"))
            return PacketConstants.WRITE_STANDARD_CONTROL_CONFIGURATION;
        else {
            return PacketConstants.WRITE_SPECIAL_CONTROL_CONFIGURATION;
        }
    }

    private byte getType(@NonNull Connection c) {
        if (c.inout().equals("Input")) {
            switch (c.getType().toLowerCase().trim()) {
                case "curr":
                    return CURRENTTYPE;
                case "freq":
                    return FREQUENCYTYPE;
                case "volt":
                    return VOLTAGETYPE;
                case "pulsea":
                case "pulseb":
                    return COUNTSTYPE;
                case "toggle":
                case "i/o":
                case "i\\o":
                    return ONOFFTYPE;
                default:
                    return IDLETYPE;
            }
        } else {
            switch (c.getType().toLowerCase().trim()) {
                case "toggle":
                    return ONOFFTYPE;
                case "pwm":
                    return PULSEWIDTHTYPE;
                default:
                    return IDLETYPE;
            }
        }
    }

    private ArrayList<UUIDConstants> getUuidSet(@NonNull final Connection c) {
        final int connectorNumber = c.getConnectionNumber();
        ArrayList<UUIDConstants> mySet = new ArrayList<>(2);
        if (c.inout().equals("Output")) {
            switch (connectorNumber) {
                case 1:
                case 2:
                case 3:
                    mySet.add(UUIDConstants.WRITE_STANDARD_CONTROL_CONFIGURATION);
                    mySet.add(UUIDConstants.READ_STANDARD_CONTROL_STATUS);
                    break;
                default:
                    mySet.add(UUIDConstants.WRITE_SPECIAL_CONTROL_CONFIGURATION);
                    mySet.add(UUIDConstants.READ_SPECIAL_CONTROL_STATUS);
            }
        } else {
            mySet.add(UUIDConstants.WRITE_SENSOR_CONFIGURATION);
            mySet.add(UUIDConstants.READ_SENSOR_STATUS);
        }
        return mySet;
    }


    public synchronized void disconnect(final boolean userDisconnected) {
        if (server != null) {
            server.disconnect();
            USER_DISCONNECT = userDisconnected;
        }
    }

    private synchronized void autoReconnect() {
        try {
            if (server != null)
                server.disconnect();
            if (operationManager != null) {
                operationManager.reset();
                operationManager = null;
            }
            adapter = null;
            server = null;
            connecting = false;
            Thread.sleep(500);
            checkBluetoothCompatible();
            setUpBluetooth();
            if(!SCANNING)
                scanLeDevice(true);
        } catch (Exception e) {
            e.printStackTrace();
            broadcastUpdate(BroadcastActionConstants.ACTION_AUTORECONNECT_FAILURE.toString());
        } finally {
            broadcastUpdate(BroadcastActionConstants.ACTION_AUTORECONNECT_COMPLETE.getString());
        }
    }

    public void resetKilledProcess(){
        USER_DISCONNECT = false;
    }

}
