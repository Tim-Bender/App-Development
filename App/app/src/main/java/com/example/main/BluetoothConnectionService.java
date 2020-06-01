package com.example.main;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothServerSocket;
import android.bluetooth.BluetoothSocket;
import android.content.Context;
import android.content.Intent;
import android.util.Log;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.charset.Charset;
import java.util.UUID;

public class BluetoothConnectionService {
    private static final String TAG = "BluetoothConnectionService";
    private static final String appName = "SpudnikDiagnosticTool";
    private static final UUID uuid = UUID.fromString("8ce255c0-200a-11e0-ac64-0800200c9a66");
    private final BluetoothAdapter mBluetoothAdapter;
    private Context mcontext;

    private AcceptThread mInsecureAcceptThread;
    private ConnectThread mConnectThread;
    private BluetoothDevice mmDevice;
    private UUID deviceUUID;
    private ConnectedThread mConnectedThread;

    public BluetoothConnectionService(Context context){
        mcontext = context;
        mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        start();
    }

    private class AcceptThread extends Thread{
        private final BluetoothServerSocket mmServerSocket;

        public AcceptThread() {
            BluetoothServerSocket temp = null;
            try {
                temp = mBluetoothAdapter.listenUsingInsecureRfcommWithServiceRecord(appName, uuid);
                Log.d(TAG,"AcceptThread: Setting up Server using: " + uuid);
            } catch (IOException e) {
                Log.e(TAG,"AcceptThread: IOException: " + e.getMessage());
            }
            mmServerSocket = temp;
        }
        public void run(){
            Log.d(TAG,"run: AcceptThread Running.");

            BluetoothSocket socket = null;

            try {
                socket = mmServerSocket.accept();
                Log.d(TAG, "run: RFCOM server socket accepted connection.");
            } catch (IOException e) {
                Log.e(TAG,"RunThread: IOException: " + e.getMessage());
            }
            if(socket != null){
                connected(socket,mmDevice);
            }

        }

        public void cancel(){
            try{
                mmServerSocket.close();
            } catch (IOException e) {
               Log.e(TAG,"cancel: Close of AcceptThread ServerSocket failed. " + e.getMessage());
            }
        }
    }


    private class ConnectThread extends Thread{
        private BluetoothSocket mmSocket;
        public ConnectThread(BluetoothDevice device,UUID uuid){
            mmDevice = device;
            deviceUUID = uuid;
        }

        public void run(){
            BluetoothSocket tmp = null;
            Log.i(TAG,"Run mConnectThread");

            try {
                tmp = mmDevice.createInsecureRfcommSocketToServiceRecord(deviceUUID);
            } catch (IOException e) {
                Log.e(TAG,"could not create insecureRFcommSocket" + e.getMessage());
            }
            mmSocket = tmp;
            mBluetoothAdapter.cancelDiscovery();

            try {
                mmSocket.connect();
                Log.d(TAG,"run: Socket connected");
            } catch (IOException e) {
               e.printStackTrace();
                try{
                    mmSocket.close();
                    Log.d(TAG,"run: Closed Socket");
                } catch (IOException ex) {
                    Log.e(TAG,"Socket Connection failed to close.");
                }
            }
            connected(mmSocket,mmDevice);
        }
        public void cancel(){
            try{
                Log.d(TAG,"cancel: Closing Client Socket");
                mmSocket.close();
            } catch (IOException e) {
                Log.e(TAG,"cancel: close() of mmSocket in ConnectThread failed. " + e.getMessage());
            }
        }
    }

    public synchronized void start(){
        Log.d(TAG,"start");

        if(mConnectThread != null){
            mConnectThread.cancel();
            mConnectThread = null;
        }
        if(mInsecureAcceptThread == null){
            mInsecureAcceptThread = new AcceptThread();
            mInsecureAcceptThread.start();
        }
    }

    public void startclient(BluetoothDevice device, UUID uuid){
        Log.d(TAG, "startclient: Started");
       Log.d(TAG,"Connecting Bluetooth");
        mConnectThread = new ConnectThread(device,uuid);
        mConnectThread.start();
    }

    private class ConnectedThread extends Thread {
        private final BluetoothSocket mmSocket;
        private final InputStream mmInStream;
        private final OutputStream mmOutStream;

        public ConnectedThread(BluetoothSocket socket) {
            Log.d(TAG, "ConnectedTrhead: Starting");
            mmSocket = socket;
            InputStream tmpIn = null;
            OutputStream tmpOut = null;
            Log.d(TAG,"Bluetooth Connected");
            try {
                tmpIn = mmSocket.getInputStream();
                tmpOut = mmSocket.getOutputStream();
            } catch (IOException e) {
                e.printStackTrace();
            }
            this.mmInStream = tmpIn;
            this.mmOutStream = tmpOut;
        }

        public void run(){
            byte[] buffer = new byte[1024];
            int bytes;

            while(true){
                try {
                    bytes = mmInStream.read(buffer);
                    String incomingMessage = new String(buffer, 0 ,bytes);
                    Log.d(TAG,"Input Stream" + incomingMessage);

                    Intent incomingMessageIntent = new Intent("incomingMessage");
                    incomingMessageIntent.putExtra("themessage",incomingMessage);
                    LocalBroadcastManager.getInstance(mcontext).sendBroadcast(incomingMessageIntent);
                } catch (IOException e) {
                    Log.e(TAG,"write: Error writing to inputstream" + e.getMessage());
                    break;
                }
            }
        }
        public void write(byte[] bytes){
            String text = new String(bytes, Charset.defaultCharset());
            try {
                mmOutStream.write(bytes);
            } catch (IOException e) {
                Log.e(TAG,"write: Error writing to outputstream" + e.getMessage());
            }
        }
        public void cancel(){
            try{
                mmSocket.close();
            } catch (IOException e) {
                Log.e(TAG,"Socket should not be closed");
            }
        }
    }

    private void connected(BluetoothSocket mmSocket, BluetoothDevice mmDevice){
        Log.d(TAG,"connected: Starting.");

        mConnectedThread = new ConnectedThread(mmSocket);
        mConnectedThread.start();
    }

    public void write(byte[] out){
        ConnectedThread r;
        mConnectedThread.write(out);
    }

}
