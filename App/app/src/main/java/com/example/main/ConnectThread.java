package com.example.main;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;

import java.util.UUID;

public class ConnectThread extends Thread{

    private final BluetoothSocket mmSocket;
    private final BluetoothDevice mmDevice;
    private final BluetoothAdapter mmAdapter;
    private final UUID MY_UUID = UUID.fromString("hello");
    private BluetoothDataTransferThread myTransferThread;
    public ConnectThread(BluetoothDevice device,BluetoothAdapter adapter) {
        this.mmDevice = device;
        this.mmAdapter = adapter;
        BluetoothSocket temp = null;
        try{
            temp = mmDevice.createRfcommSocketToServiceRecord(MY_UUID);
        } catch (Exception e) {
            System.out.println("Bluetooth Socket Constructor Failure");
        }
        mmSocket = temp;
    }

    public void run() {
        mmAdapter.cancelDiscovery();
        try {
            mmSocket.connect();
        } catch (Exception e) {
            System.out.println("Socket connection failure.");

            try {
                mmSocket.close();
            } catch (Exception t) {
                System.out.println("Socket closure failure");
            }
            return;
        }
        //thread connection successful! pass socket to datatransfer thread
        myTransferThread = new BluetoothDataTransferThread(this.mmSocket);

    }

    public void cancel(){
        try{
            mmSocket.close();
        } catch (Exception e) {
            System.out.println("Could not close socket");
        }
    }

}
