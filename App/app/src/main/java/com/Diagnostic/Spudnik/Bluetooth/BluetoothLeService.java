
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

import android.app.Service;
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
import android.os.Binder;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.Diagnostic.Spudnik.CustomObjects.Connection;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;

public class BluetoothLeService extends Service {

    private final IBinder mBinder = new LocalBinder();

    @Nullable
    @Override
    public IBinder onBind(@NonNull Intent intent) {
        System.out.println("ON BIND");
        return mBinder;
    }

    public class LocalBinder extends Binder {
        public BluetoothLeService getServerInstance() {
            return BluetoothLeService.this;
        }
    }


    @Override
    public void onCreate() {
        System.out.println("ON CREATE SERVICE");
        context = this;
        USER_DISCONNECT = false;
        leScanCallback = (device, rssi, scanRecord) -> {
            if (device.getName() != null && !connecting) {
                String deviceName = device.getName().toLowerCase();
                if (deviceName.contains("s4") || deviceName.contains("spud") || deviceName.contains("diag")) {
                    System.out.println("RSSI " + rssi);
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

    @Override
    public void onDestroy() {
        AsyncTask.execute(() -> disconnect(true));
    }


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

    private static
    int WEAK_SIGNAL_BUFFER = 0;
    private final static int WEAK_SIGNAL_MAXIMUM_BUFFER = 4;

    public BluetoothLeService() {

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
            broadcastUpdate(BroadcastActionConstants.ACTION_SCANNING.getString());
            adapter.startLeScan(leScanCallback);
            System.out.println("SCAN STARTED");
        } else {
            adapter.stopLeScan(leScanCallback);
            SCANNING = false;
        }
    }

    private final BluetoothGattCallback gattCallback = new BluetoothGattCallback() {

        @Override
        public void onMtuChanged(BluetoothGatt gatt, int mtu, int status) {
            try {
                if (status == BluetoothGatt.GATT_SUCCESS && operationManager != null) {
                    System.out.println("MTU CHANGED TO: " + mtu);
                    refreshDeviceCache(gatt);
                    operationManager.request(new Operation(null, Operation.DISCOVER_SERVICES, null));
                } else {
                    System.out.println("MTU REQUEST FAILURE");
                    autoReconnect();
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
                    refreshDeviceCache(gatt);
                    operationManager.request(new Operation(null, Operation.REQUEST_MTU, null));
                } else if (newState != BluetoothProfile.STATE_CONNECTING) {
                    broadcastUpdate(BroadcastActionConstants.ACTION_GATT_DISCONNECTED.getString());
                    connecting = false;
                    System.out.println("CONNECTION STATE Disconnected");
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
                    System.out.println("GATT SERVICES DISCOVERED FAILURE");
                    broadcastUpdate(BroadcastActionConstants.ACTION_GATT_SERVICES_DISCOVERED_FAILURE.getString());
                    autoReconnect();
                }
            } catch (Exception e) {
                e.printStackTrace();
                autoReconnect();
            } finally {
                if (operationManager != null) {
                    operationManager.operationCompleted();
                    operationManager.request(new Operation(null, Operation.READ_RSSI, null));
                }
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
                    autoReconnect();
                }
            } catch (Exception e) {
                e.printStackTrace();
                broadcastUpdate(BroadcastActionConstants.ACTION_CHARACTERISTIC_READ_FAILURE.getString());
                autoReconnect();
            } finally {
                if (operationManager != null) {
                    operationManager.operationCompleted();
                    operationManager.request(new Operation(null, Operation.READ_RSSI, null));
                }
            }
        }

        @Override
        public void onCharacteristicWrite(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic, int status) {
            try {
                if (status == BluetoothGatt.GATT_SUCCESS) {
                    WRITTEN = true;
                    System.out.println("WRITE CHARACTERISTIC SUCCESS");
                    broadcastUpdate(BroadcastActionConstants.ACTION_CHARACTERISTIC_WRITE.getString());
                    operationManager.request(new Operation(null, Operation.READ_RSSI, null));
                } else {
                    System.out.println("WRITE CHARACTERSTISTIC FAILURE");
                    broadcastUpdate(BroadcastActionConstants.ACTION_CHARACTERISTIC__WRITE_FAILURE.getString());
                    autoReconnect();
                }
            } catch (Exception e) {
                e.printStackTrace();
                autoReconnect();
            } finally {
                if (operationManager != null) {
                    operationManager.operationCompleted();
                    operationManager.request(new Operation(null, Operation.READ_RSSI, null));
                }
            }
        }

        @Override
        public void onReadRemoteRssi(BluetoothGatt gatt, int rssi, int status) {
            try {
                if (gatt != null && status == BluetoothGatt.GATT_SUCCESS) {
                    if (rssi < -95) {
                        WEAK_SIGNAL_BUFFER++;
                        System.out.println("EXTREMELY WEAK SIGNAL");
                        if (WEAK_SIGNAL_BUFFER >= WEAK_SIGNAL_MAXIMUM_BUFFER) {
                            broadcastUpdate(BroadcastActionConstants.ACTION_WEAK_SIGNAL.getString());
                            autoReconnect();
                        }
                    } else if (rssi < -85) {
                        WEAK_SIGNAL_BUFFER++;
                        System.out.println("WEAK SIGNAL " + rssi);
                        if (WEAK_SIGNAL_BUFFER >= WEAK_SIGNAL_MAXIMUM_BUFFER)
                            broadcastUpdate(BroadcastActionConstants.ACTION_WEAK_SIGNAL.getString());
                    } else
                        WEAK_SIGNAL_BUFFER = 0;
                    System.out.println("GOOD SIGNAL " + rssi);
                } else
                    System.out.println("RSSI REQUEST ERROR");
            } catch (Exception e) {
                System.out.println("RSSI REQUEST ERROR");
                e.printStackTrace();
            } finally {
                operationManager.operationCompleted();
            }
        }
    };


    private void broadcastUpdate(final String action) {
        final Intent intent = new Intent(action);
        context.sendBroadcast(intent);
    }

    private void broadcastUpdate(@NonNull final String action, @NonNull final BluetoothGattCharacteristic characteristic) {
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
            if (!USER_DISCONNECT) {
                if (server != null) {
                    server.disconnect();
                    Looper.prepare();
                    Handler handler = new Handler();
                    handler.postDelayed(() -> {
                        if (server != null)
                            server.close();
                    }, 600);
                }
                if (operationManager != null) {
                    operationManager.reset();
                    operationManager = null;
                }
                adapter = null;
                server = null;
                connecting = false;
                WRITTEN = false;
                Thread.sleep(500);
                checkBluetoothCompatible();
                setUpBluetooth();
                if (!SCANNING)
                    scanLeDevice(true);
            } else {
                System.out.println("USER DISCONNECT: AUTO RECONNECT ABORT");
            }
        } catch (Exception e) {
            e.printStackTrace();
            broadcastUpdate(BroadcastActionConstants.ACTION_AUTORECONNECT_FAILURE.toString());
        } finally {
            broadcastUpdate(BroadcastActionConstants.ACTION_AUTORECONNECT_COMPLETE.getString());
        }
    }

    @SuppressWarnings("JavaReflectionMemberAccess")
    private void refreshDeviceCache(BluetoothGatt bluetoothGatt) {
        try {
            Method hiddenClearCacheMethod = bluetoothGatt.getClass().getMethod("refresh");

            if (hiddenClearCacheMethod != null) {
                Boolean succeeded = (Boolean) hiddenClearCacheMethod.invoke(bluetoothGatt);
                if (succeeded) {
                    System.out.println("CACHE WAS CLEARED");
                } else {
                    System.out.println("CACHE NOT CLEARED");
                }
            } else {
                System.out.println("CACHE NOT CLEARED, METHOD FAILURE");
            }
        } catch (NoSuchMethodException | InvocationTargetException | IllegalAccessException e) {
            e.printStackTrace();
            System.out.println("CACHE NOT CLEARED METHOD FAILURE");
        }
    }

}
