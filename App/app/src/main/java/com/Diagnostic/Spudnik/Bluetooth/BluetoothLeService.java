
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

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.Diagnostic.Spudnik.CustomObjects.Pin;

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
    }

    public void initiate() {
        if (checkBluetoothCompatible()) {
            setUpBluetooth();
            scanLeDevice(true);
        }
    }


    private Context context;
    private BluetoothAdapter adapter;
    private static OperationManager operationManager;
    private static BluetoothAdapter.LeScanCallback leScanCallback;
    private BluetoothGatt server;
    private boolean connecting = false;

    public final byte IDLETYPE = 0x00;
    public final byte ONOFFTYPE = 0x01;
    public final byte CURRENTTYPE = 0x02;
    public final byte VOLTAGETYPE = 0x03;
    public final byte FREQUENCYTYPE = 0x04;
    public final byte COUNTSTYPE = 0x05;
    public final byte PULSEWIDTHTYPE = 0x08;

    public final static int SEARCHING = 1;
    public final static int CONNECTED = 2;
    public final static int DISCONNECTED = 3;
    public static int STATUS;

    private static boolean USER_DISCONNECT = false;
    public static boolean WRITTEN = false;

    private static int WEAK_SIGNAL_BUFFER = 0;
    private final static int WEAK_SIGNAL_MAXIMUM_BUFFER = 6;
    private static int FAILED_PACKET_BUFFER = 0;
    private final static int FAILED_PACKET_MAXIMUM_BUFFER = 4;

    public final static int[][] pinRelations = new int[][]{
            {1, 1, 12, 8},
            {2, 3, 13, 9},
            {3, 4, 14, 10},
            {4, 6, 15, 3},
            {5, 7, 4, 11},
            {6, 9, 16, 4},
            {7, 10, 5, 12},
            {8, 12, 17, 5},
            {9, 13, 6, 13},
            {10, 15, 18, 14},
            {11, 16, 7},
            {12, 18, 19},
            {13, 19, 8},
            {14, 21, 20},
            {15, 22, 21},
            {16, 24, 22}};

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

    public void scanLeDevice(final boolean enable) {
        AsyncTask.execute(() -> {
            if (enable) {
                STATUS = SEARCHING;
                broadcastUpdate(BroadcastActionConstants.ACTION_SCANNING.getString());
                adapter.startLeScan(leScanCallback);
                System.out.println("SCAN STARTED");
            } else {
                adapter.stopLeScan(leScanCallback);
                STATUS = SEARCHING;
            }
        });

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
                    status = DISCONNECTED;
                    broadcastUpdate(BroadcastActionConstants.ACTION_GATT_DISCONNECTED.getString());
                    connecting = false;
                    System.out.println("CONNECTION STATE Disconnected");
                    autoReconnect();
                }
            } catch (Exception e) {
                e.printStackTrace();
                autoReconnect();
            }
        }

        @Override
        public void onServicesDiscovered(BluetoothGatt gatt, int status) {
            try {
                if (status == BluetoothGatt.GATT_SUCCESS && operationManager != null) {
                    for (BluetoothGattService s : gatt.getServices()) {
                        if (s.getUuid().toString().equals(UUIDConstants.SERVICE_UUID.getFromString().toString())) {
                            STATUS = CONNECTED;
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
                }
            }

        }

        @Override
        public void onCharacteristicRead(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic, int status) {
            try {
                if (status == BluetoothGatt.GATT_SUCCESS) {
                    System.out.println("CHARACTERISTIC READ SUCCESS");
                    FAILED_PACKET_BUFFER = 0;
                    System.out.println(Arrays.toString(characteristic.getValue()));
                    broadcastUpdate(BroadcastActionConstants.ACTION_CHARACTERISTIC_READ.getString(), characteristic);
                    operationManager.request(new Operation(null, Operation.READ_RSSI, null));
                } else {
                    FAILED_PACKET_BUFFER++;
                    if (FAILED_PACKET_BUFFER == FAILED_PACKET_MAXIMUM_BUFFER) {
                        System.out.println("CHARACTERISTIC READ FAILURE");
                        broadcastUpdate(BroadcastActionConstants.ACTION_CHARACTERISTIC_READ_FAILURE.getString());
                        autoReconnect();
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
                FAILED_PACKET_BUFFER++;
                if (FAILED_PACKET_BUFFER == FAILED_PACKET_MAXIMUM_BUFFER) {
                    broadcastUpdate(BroadcastActionConstants.ACTION_CHARACTERISTIC_READ_FAILURE.getString());
                    autoReconnect();
                }
            } finally {
                if (operationManager != null) {
                    operationManager.operationCompleted();
                }
            }
        }

        @Override
        public void onCharacteristicWrite(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic, int status) {
            try {
                if (status == BluetoothGatt.GATT_SUCCESS) {
                    WRITTEN = true;
                    FAILED_PACKET_BUFFER = 0;
                    System.out.println("WRITE CHARACTERISTIC SUCCESS");
                    System.out.println("CHARACTERISTIC WRITE NEW VALUE  " + Arrays.toString(characteristic.getValue()));
                    broadcastUpdate(BroadcastActionConstants.ACTION_CHARACTERISTIC_WRITE.getString());
                    operationManager.request(new Operation(null, Operation.READ_RSSI, null));
                } else {
                    FAILED_PACKET_BUFFER++;
                    if (FAILED_PACKET_BUFFER == FAILED_PACKET_MAXIMUM_BUFFER) {
                        System.out.println("WRITE CHARACTERSTISTIC FAILURE");
                        broadcastUpdate(BroadcastActionConstants.ACTION_CHARACTERISTIC__WRITE_FAILURE.getString());
                        autoReconnect();
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
                FAILED_PACKET_BUFFER++;
                if (FAILED_PACKET_BUFFER == FAILED_PACKET_MAXIMUM_BUFFER) {
                    autoReconnect();
                }
            } finally {
                if (operationManager != null) {
                    operationManager.operationCompleted();
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
                if (operationManager != null)
                    operationManager.operationCompleted();
            }
        }
    };


    private synchronized void broadcastUpdate(final String action) {
        broadcastUpdate(action, null);
    }

    private synchronized void broadcastUpdate(@NonNull final String action, @NonNull final BluetoothGattCharacteristic characteristic) {
        final Intent intent = new Intent(action);
        if (characteristic != null)
            intent.putExtra("bytes", characteristic.getValue());
        context.sendBroadcast(intent);
    }

    public void requestConnectorVoltage(@NonNull ArrayList<Pin> pins) {
        if (operationManager != null && server != null && STATUS == CONNECTED) {
            ArrayList<UUIDConstants> uuids = getUuidSet(pins.get(0));
            Packet myPacket;
            if (!WRITTEN) {
                myPacket = new Packet(buildPacket(pins));
                System.out.println(Arrays.toString(buildPacket(pins)));
                operationManager.request(new Operation(uuids.get(0).getFromString(), Operation.WRITE_CHARACTERISTIC, myPacket));
            }
            Handler handler = new Handler();
            if (!WRITTEN)
                handler.postDelayed(() -> operationManager.request(new Operation(uuids.get(1).getFromString(), Operation.READ_CHARACTERISTIC, null)), 2000);
            else
                operationManager.request(new Operation(uuids.get(1).getFromString(),Operation.READ_CHARACTERISTIC,null));
        }
    }

    public void writeConnectorVoltage(@NonNull ArrayList<Pin> pins, @NonNull Pin currentPin, @NonNull int insertionValue) {
        if (operationManager != null && server != null) {
            ArrayList<UUIDConstants> uuids = getUuidSet(pins.get(0));
            byte[] bytes = buildPacket(pins);
            System.out.println("WRITING PACKET: " + Arrays.toString(bytes));
            bytes[getWritePosition(currentPin)] = (byte) insertionValue;
            Packet myPacket = new Packet(bytes);
            operationManager.request(new Operation(uuids.get(0).getFromString(), Operation.WRITE_CHARACTERISTIC, myPacket));
        }
    }

    public int getWritePosition(@NonNull Pin p) {
        return (getPinRelation(p) * 2) - 1;
    }


    public byte[] buildPacket(@NonNull ArrayList<Pin> pins) {
        byte[] defaultPacket = getDefaultPacket(pins.get(0)).getBytes();
        for (Pin p : pins) {
            defaultPacket[getArrayPosition(p)] = getType(p);
            System.out.println("TYPE OF PIN: " + getType(p));
        }
        return defaultPacket;
    }

    public int getArrayPosition(@NonNull Pin pin) {
        return (pin.inout().equals("Input")) ? getPinRelation(pin) - 1 : getPinRelation(pin) * 2 - 2;
    }

    public int getPinRelation(@NonNull Pin p) {
        int pinCount = getPinCount(p), col;
        int s4 = Integer.parseInt(p.getS4());
        switch (pinCount) {
            case 24:
                col = 1;
                break;
            case 22:
                col = 2;
                break;
            default:
                col = 3;
        }
        for (int[] arr : pinRelations) {
            if (arr[col] == s4) {
                return arr[0];
            }
        }
        return 1;
    }

    public int getPinCount(Pin p) throws NullPointerException {
        int toReturn = 0;
        String direction = p.getDirection();
        if (!direction.contains("exp")) {
            int number = p.getConnectionNumber();
            if (direction.contains("in")) {
                if (number < 3)
                    toReturn = 14;
                else
                    toReturn = 22;
            } else if (direction.contains("out")) {
                if (number < 4)
                    toReturn = 24;
                else
                    toReturn = 2;
            }
        } else {
            if (direction.contains("in"))
                toReturn = 22;
            else
                toReturn = 24;
        }
        return toReturn;
    }


    private PacketConstants getDefaultPacket(@NonNull Pin c) {
        if (c.inout().equals("Input"))
            return PacketConstants.WRITE_SENSOR_CONFIGURATION;
        else if (c.getConnectionNumber() < 4 || c.getDirection().trim().contains("exp"))
            return PacketConstants.WRITE_STANDARD_CONTROL_CONFIGURATION;
        else {
            return PacketConstants.WRITE_SPECIAL_CONTROL_CONFIGURATION;
        }
    }

    public byte getType(@NonNull Pin c) {
        if (c.inout().equals("Input")) {
            switch (c.getUnits().toLowerCase().trim()) {
                case "curr":
                    return CURRENTTYPE;
                case "freq":
                case "freqa":
                case "freqb":
                    return FREQUENCYTYPE;
                case "volt":
                    return VOLTAGETYPE;
                case "pulsea":
                case "pulseb":
                    return COUNTSTYPE;
                case "toggle":
                case "i/o":
                case "i\\o":
                case "skipdet":
                    return ONOFFTYPE;
                default:
                    return IDLETYPE;
            }
        } else {
            switch (c.getUnits().toLowerCase().trim()) {
                case "toggle":
                    return ONOFFTYPE;
                case "pwm":
                    return PULSEWIDTHTYPE;
                case "freq":
                    if (!c.getDirection().contains("exp") && Integer.parseInt(String.valueOf(c.getDirection().charAt(c.getDirection().length() - 1))) > 3)
                        return FREQUENCYTYPE;
                default:
                    return IDLETYPE;
            }
        }
    }

    public String[] formatPwmOutput(@NonNull Pin myPin, @NonNull byte[] bytes){
        String dir = myPin.inout();
        String[] outputValue = new String[4];
        if (dir.equals("Output")) {
            float supplyV = 0, pwmFreq;
            if (bytes != null) {
                int[] ints = new int[4];
                for (int i = 0; i < 4; i++) {
                    ints[i] = (bytes[i] < 0) ? bytes[i] + 256 : bytes[i];
                }
                supplyV = ((ints[0] << 8) + ints[1]) / 100f;
                pwmFreq = (ints[2] << 8) + ints[3];
                outputValue[0] = "Supply Voltage = " + supplyV + " VDC\n";
                outputValue[1] = "PWM Frequency "+ pwmFreq + "Hz";
            }
            float pwmValue = 0;
            outputValue[2] = " 0 Pwm";
            if (bytes != null) {
                int readPosition = getReadPosition(myPin);
                int[] nonNegatives = new int[bytes.length];
                for (int i = 0; i < bytes.length; i++) {
                    nonNegatives[i] = (bytes[i] < 0) ? bytes[i] + 256 : bytes[i];
                }
                pwmValue = ((nonNegatives[readPosition] << 8) + nonNegatives[readPosition + 1]) / 10f;
                if (getType(myPin) == FREQUENCYTYPE)
                    outputValue[2] = pwmValue + " Hz";
                else
                    outputValue[2] = pwmValue + " PWM";
            }
            outputValue[3] = (supplyV * pwmValue + "VDC");
        } else {
            float f = 0;
            if (bytes != null) {
                f = ((bytes[0] << 8) + bytes[1]) / 100f;
            }
            outputValue[0] = "Connector Voltage:  " + f +"v";
            int type = getType(myPin);
            int[] nonNegativeArray = new int[bytes.length];
            for (int i = 0; i < bytes.length; i++) {
                nonNegativeArray[i] = (bytes[i] < 0) ? bytes[i] + 256 : bytes[i];
            }
            int loc1 = getPinRelation(myPin) * 2;
            int loc2 = loc1 + 1;
            float value = nonNegativeArray[loc1] + nonNegativeArray[loc2];
            switch (type) {
                case IDLETYPE:
                    outputValue[2] = "Off";
                case FREQUENCYTYPE:
                    if (value >= 0 && value <= 10000)
                        outputValue[2] = (int) value / 10 + " Hz";
                    else if (value >= 33768 && value <= 42768)
                        outputValue[2] = ((int) value - 32768) + " Hz";
                    break;
                case ONOFFTYPE:
                    if (value == 0 || value == 1)
                        outputValue[2] = Float.toString(value);
                    break;
                case COUNTSTYPE:
                    if (value >= 0 && value < 10000)
                        outputValue[2] = (int) value + " counts";
                    break;
                case CURRENTTYPE:
                    if (value >= 0 && value <= 3000)
                        outputValue[2] = value / 100.0f + " mA";
                    break;
                case VOLTAGETYPE:
                    if (value >= 0 && value <= 7300)
                        outputValue[2] = value / 1000.0f + " volts";
                    break;
            }
            outputValue[2] = "null";
        }
        return outputValue;
    }

    private int getReadPosition(@NonNull Pin myPin) {
        int sensorNumber = getPinRelation(myPin);
        if (myPin.inout().equals("Input")) {
            return sensorNumber * 2 + 1;
        } else {
            int connectorNumber;
            if (myPin.getDirection().contains("exp")) {
                connectorNumber = 24;
            } else
                connectorNumber = Integer.parseInt(String.valueOf(myPin.getDirection().charAt(myPin.getDirection().length() - 1)));
            if (connectorNumber < 4)
                return 2 * sensorNumber + 2;
            else if (connectorNumber % 2 == 0)
                return 2;
            else {
                return 6;
            }
        }
    }

    private ArrayList<UUIDConstants> getUuidSet(@NonNull final Pin c) {
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


    private synchronized void autoReconnect() {
        try {
            System.out.println("ATTEMPTING AUTO RECONNECT");
            if (!USER_DISCONNECT) {
                if (server != null) {
                    server.disconnect();
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
                initiate();
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
    private void refreshDeviceCache(@NonNull BluetoothGatt bluetoothGatt) {
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

    public void clearOperationManager() {
        if (operationManager != null)
            operationManager.reset();
    }

}
