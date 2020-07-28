
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

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.Queue;
import java.util.UUID;

class BluetoothLeService {

    @SuppressWarnings("unused")
    private enum UUIDConstants {
        SERVICE_UUID(UUID.fromString("ea7f12aa-0fa1-658b-bd4a-78b9c758fdbb")), //main service uuid
        SEND_UPDATE_UUID(UUID.fromString("1d354544-8c53-2ba3-7646-2e4225d4596a")), //update software - disabled
        SYSTEM_INFORMATION(UUID.fromString("153629f4-23fc-35b3-ec40-047a7292ca5b")), //version...
        READ_SENSOR_STATUS(UUID.fromString("1ec79834-e66c-7fb0-8241-b41b94c8ba73")), //read in 1-4
        WRITE_SENSOR_CONFIGURATION(UUID.fromString("77fc6fe4-26a3-c1a0-694c-428f97bc3176")), //write config in 1-4
        READ_STANDARD_CONTROL_STATUS(UUID.fromString("a6949c00-d6b0-fc95-8943-5fdd489fa949")), //read out1,2,3
        READ_SPECIAL_CONTROL_STATUS(UUID.fromString("79723e9f-70fb-2391-0a44-5ab4fc922b96")), //read out 4-9
        WRITE_STANDARD_CONTROL_CONFIGURATION(UUID.fromString("adbf2537-ef53-0a9f-a443-ce46702af9b9")), //writes config out1,2,3
        WRITE_SPECIAL_CONTROL_CONFIGURATION(UUID.fromString("d0fede9c-f89a-0ab0-1644-f994afeadd03")); //write out 4-9

        private final UUID fromString;

        UUIDConstants(UUID fromString) {
            this.fromString = fromString;
        }

        public UUID getFromString() {
            return fromString;
        }
    }

    private Context context;
    private BluetoothAdapter adapter;
    private static OperationManager operationManager;
    public static BluetoothAdapter.LeScanCallback leScanCallback;
    private BluetoothGatt server;

    private boolean connecting = false;

    BluetoothLeService(Context context) {
        this.context = context;
        leScanCallback = (device, rssi, scanRecord) -> {
            if (device.getName() != null && !connecting) {
                if (device.getName().equals("s4_diag_tool")) {
                    System.out.println("Connecting to: " + device.getName());
                    scanLeDevice(false);
                    connecting = true;
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
            try {
                String intentAction;
                if (newState == BluetoothProfile.STATE_CONNECTED) {
                    intentAction = ACTION_GATT_CONNECTED;
                    server = gatt;
                    operationManager = new OperationManager(gatt);
                    operationManager.request(new Operation(null, Operation.DISCOVER_SERVICES, null));
                    broadcastUpdate(intentAction);
                    System.out.println("CONNECTION STATE Connected");
                } else if (newState != BluetoothProfile.STATE_CONNECTING) {
                    intentAction = ACTION_GATT_DISCONNECTED;
                    broadcastUpdate(intentAction);
                    connecting = false;
                    Thread.sleep(1000);
                    if (server != null)
                        server.close();
                    System.out.println("CONNECTION STATE Distconnected");
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        @Override
        public void onServicesDiscovered(BluetoothGatt gatt, int status) {
            try {
                if (status == BluetoothGatt.GATT_SUCCESS) {
                    for (BluetoothGattService s : gatt.getServices()) {
                        System.out.println("SERVICE: " + s.getUuid());
                        System.out.println("SERVICE UUID " + UUIDConstants.SERVICE_UUID.fromString);
                        if (s.getUuid().toString().equals(UUIDConstants.SERVICE_UUID.fromString.toString()))
                            operationManager.setService(s);
                    }

                } else {
                    operationManager.request(new Operation(null, Operation.DISCOVER_SERVICES, null));
                }
                broadcastUpdate(ACTION_GATT_SERVICES_DISCOVERED);
            } catch (Exception e) {
                e.printStackTrace();
            } finally {
                operationManager.operationCompleted();
            }

        }

        @Override
        public void onCharacteristicRead(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic, int status) {
            try {
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
            } catch (Exception e) {
                e.printStackTrace();
            } finally {
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
                }
            } catch (Exception e) {
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

    private void broadcastUpdate(@NonNull final String action,
                                 @SuppressWarnings("unused") @NonNull final BluetoothGattCharacteristic characteristic) {
        final Intent intent = new Intent(action);
        intent.putExtra("bytes", characteristic.getValue());
        context.sendBroadcast(intent);
    }

    public void requestConnectorVoltage(@NonNull @SuppressWarnings("unused") connection c) {
        //first write the type, then pull data
        Packet myPacket = new Packet(PacketConstants.WRITE_SENSOR_CONFIGURATION.getBytes());
        myPacket.SetSensorType(IDLETYPE);
        operationManager.request(new Operation(UUIDConstants.WRITE_SENSOR_CONFIGURATION.fromString, Operation.WRITE_CHARACTERISTIC, myPacket));
        operationManager.request(new Operation(UUIDConstants.READ_STANDARD_CONTROL_STATUS.fromString, Operation.READ_CHARACTERISTIC, null));
    }

    public void disconnect() {
        if (server != null) {
            server.disconnect();
        }
    }

    public static class OperationManager {

        private Queue<Operation> operations = new LinkedList<>();
        private Operation currentOp;
        private final BluetoothGatt server;
        private BluetoothGattService service;

        public OperationManager(@NonNull BluetoothGatt gatt) {
            server = gatt;
        }

        public synchronized void request(@NonNull Operation operation) {
            operations.add(operation);
            if (currentOp == null) {
                currentOp = operations.poll();
                performOperation();
            }
        }

        public synchronized void operationCompleted() {
            currentOp = null;
            if (operations.peek() != null) {
                currentOp = operations.poll();
                performOperation();
            }
        }

        private synchronized void performOperation() {
            System.out.println("Performing Operation");
            System.out.println("Operation " + currentOp.getOPERATION());
            System.out.println("Server " + server);
            System.out.println("service " + service);
            if (currentOp.getOPERATION() == Operation.DISCOVER_SERVICES) {
                server.discoverServices();
            } else if (currentOp.getOPERATION() == Operation.READ_CHARACTERISTIC && service != null && server != null) {
                System.out.println("GATT PERFORM OPERATION: " + server.getServices());
                System.out.println("CHARACTERISTIC ABOUT TO READ" + service.getCharacteristic(currentOp.getOperationUUID()).getUuid());
                System.out.println("SERVICE ABOUT TO USE" + service.getUuid());
                server.readCharacteristic(service.getCharacteristic(currentOp.getOperationUUID()));
            } else if (currentOp.getOPERATION() == Operation.WRITE_CHARACTERISTIC && service != null) {
                System.out.println("WRITING CHARACTERISTIC");
                BluetoothGattCharacteristic characteristic = service.getCharacteristic(currentOp.getOperationUUID());
                characteristic.setValue(currentOp.getWriteValue().getPacket());
                server.writeCharacteristic(characteristic);
            } else if (currentOp.getOPERATION() == Operation.DISCONNECT) {
                server.disconnect();
            } else if (currentOp.getOPERATION() == Operation.CLOSE_CONNECTION) {
                server.close();
            }
        }

        @SuppressWarnings("unused")
        public void setService(@NonNull BluetoothGattService service) {
            this.service = service;
        }
    }

    private static class Operation {

        private final UUID operationUUID;
        private final int OPERATION;
        private final Packet writeValue;
        public final static int DISCOVER_SERVICES = 0, READ_CHARACTERISTIC = 1, WRITE_CHARACTERISTIC = 2, DISCONNECT = 3, CLOSE_CONNECTION = 4;

        public Operation(@Nullable UUID uuid, @NonNull int operation, @Nullable Packet writeValue) {
            operationUUID = uuid;
            OPERATION = operation;
            this.writeValue = writeValue;
        }

        public UUID getOperationUUID() {
            return operationUUID;
        }

        public int getOPERATION() {
            return OPERATION;
        }

        public Packet getWriteValue() {
            return writeValue;
        }
    }

    @SuppressWarnings("unused")
    private enum PacketConstants {
        WRITE_SENSOR_CONFIGURATION(0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00,
                0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00),
        WRITE_STANDARD_CONTROL_CONFIGURATION(0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00,
                0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00,
                0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00,
                0x00, 0x00, 0x00, 0x00),
        WRITE_SPECIAL_CONTROL_CONFIGURATION(0x00, 0x00, 0x00, 0x00, 0x00, 0x00),
        SEND_UPDATE( 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00,
                0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00,
                0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00,
                0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00,
                0x00, 0x00);
        private final byte[] bytes;

        PacketConstants(@NonNull int... bytes) {
            ByteArrayOutputStream bos = new ByteArrayOutputStream(bytes.length * 4);
            DataOutputStream dos = new DataOutputStream(bos);
            try {
                for (int i : bytes)
                    dos.write(i);
            } catch (IOException e) {
                this.bytes = bos.toByteArray();
                return;
            }
            this.bytes = bos.toByteArray();
        }
        public byte[] getBytes() {
            return bytes;
        }
    }
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

    @SuppressWarnings("unused")
    private static class Packet {

        private final byte[] packet;

        @SuppressWarnings("unused")
        Packet(@NonNull byte[] PACKET) {
            packet = PACKET;
        }

        @SuppressWarnings("unused")
        public byte[] getPacket() {
            return packet;
        }

        @SuppressWarnings("unused")
        public void SetSensorType(@NonNull byte TYPE) {
            if (packet != null)
                packet[0] = TYPE;
        }
    }

}
