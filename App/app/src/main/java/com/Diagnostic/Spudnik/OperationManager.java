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


import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattService;

import androidx.annotation.NonNull;

import java.util.LinkedList;
import java.util.Queue;

public class OperationManager {

    private Queue<Operation> operations = new LinkedList<>();
    private Operation currentOp;
    private BluetoothGatt server;
    private BluetoothGattService readService;
    private BluetoothGattService writeService;

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

    public synchronized void performOperation() {
        if (currentOp.getOPERATION() == Operation.DISCOVER_SERVICES) {
            server.discoverServices();
        }
        else if (currentOp.getOPERATION() == Operation.READ_CHARACTERISTIC && readService != null) {
            System.out.println("CHARACTERISTIC ABOUT TO READ" + readService.getCharacteristic(currentOp.getOperationUUID()).getUuid());
            System.out.println("SERVICE ABOUT TO USE" + readService.getUuid());
            server.readCharacteristic(readService.getCharacteristic(currentOp.getOperationUUID()));
        }
        /*else if(currentOp.getOPERATION() == Operation.WRITE_CHARACTERISTIC && writeService != null){
            System.out.println("WRITING CHARACTERISTIC");
            BluetoothGattCharacteristic characteristic = writeService.getCharacteristic(currentOp.getOperationUUID());
            characteristic.setValue(currentOp.getWriteValue(),BluetoothGattCharacteristic.FORMAT_UINT16,0);
            server.writeCharacteristic(characteristic);
        }*/
        else if(currentOp.getOPERATION() == Operation.DISCONNECT){
            server.disconnect();
        }
        else if(currentOp.getOPERATION() == Operation.CLOSE_CONNECTION){
            server.close();
        }

    }

    public synchronized void setReadService(BluetoothGattService service) {
        this.readService = service;
    }

    public synchronized void setWriteService(BluetoothGattService writeService) {
        this.writeService = writeService;
    }
}
