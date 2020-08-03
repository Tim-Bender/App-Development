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

import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattService;

import androidx.annotation.NonNull;

import java.util.LinkedList;
import java.util.Queue;

public class OperationManager {

    private final Queue<Operation> operations = new LinkedList<>();
    private Operation currentOp;
    private BluetoothGatt server;
    private BluetoothGattService service;
    private final static int GATT_MTU_SIZE = 64;

    public OperationManager(@NonNull BluetoothGatt gatt) {
        server = gatt;
    }

    public synchronized void request(@NonNull Operation operation) {
        insertIntoQueue(operation);
        if (currentOp == null) {
            currentOp = operations.poll();
            performOperation();
        }
    }

    private void insertIntoQueue(@NonNull Operation operation) {
        for (Operation o : operations) {
            if (operation.getOPERATION() == o.getOPERATION() && operation.getOperationUUID() == o.getOperationUUID()) {
                if(operation.getWriteValue() != null && o.getWriteValue() != null) {
                    if (operation.getWriteValue().getPacket().length == o.getWriteValue().getPacket().length) {
                        o.setWriteValue(operation.getWriteValue());
                        return;
                    }
                }
            }
        }
        operations.add(operation);
    }

    public synchronized void operationCompleted() {
        currentOp = null;
        if (operations.peek() != null) {
            currentOp = operations.poll();
            performOperation();
        }
    }

    private synchronized void performOperation() {
        if (currentOp.getOPERATION() == Operation.DISCOVER_SERVICES) {
            server.discoverServices();
        } else if (currentOp.getOPERATION() == Operation.READ_CHARACTERISTIC && service != null && server != null) {
            server.readCharacteristic(service.getCharacteristic(currentOp.getOperationUUID()));
        } else if (currentOp.getOPERATION() == Operation.WRITE_CHARACTERISTIC && service != null) {
            BluetoothGattCharacteristic characteristic = service.getCharacteristic(currentOp.getOperationUUID());
            characteristic.setValue(currentOp.getWriteValue().getPacket());
            server.writeCharacteristic(characteristic);
        } else if (currentOp.getOPERATION() == Operation.DISCONNECT) {
            server.disconnect();
        } else if (currentOp.getOPERATION() == Operation.CLOSE_CONNECTION) {
            server.close();
        } else if (currentOp.getOPERATION() == Operation.REQUEST_MTU) {
            server.requestMtu(GATT_MTU_SIZE);
        } else if (currentOp.getOPERATION() == Operation.READ_RSSI) {
            server.readRemoteRssi();
        }
    }

    @SuppressWarnings("unused")
    public void setService(@NonNull BluetoothGattService service) {
        this.service = service;
    }

    public void reset() {
        operations.clear();
        server = null;
        currentOp = null;
        server = null;

    }
}