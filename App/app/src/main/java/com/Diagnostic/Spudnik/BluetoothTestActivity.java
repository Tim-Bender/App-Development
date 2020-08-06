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

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.IBinder;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.Diagnostic.Spudnik.Bluetooth.BluetoothLeService;
import com.Diagnostic.Spudnik.Bluetooth.BroadcastActionConstants;
import com.google.android.material.snackbar.Snackbar;

public class BluetoothTestActivity extends AppCompatActivity {

    private BluetoothBroadcastReceiver receiver;
    private BluetoothLeService mServer;
    private boolean bounded = false;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_bluetooth_test);

        Toolbar myToolBar = findViewById(R.id.topAppBar); //typical toolbar setup
        setSupportActionBar(myToolBar);
        setTitle("BluetoothTest");
        myToolBar.setTitleTextColor(Color.WHITE);
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        getSupportActionBar().setDisplayShowTitleEnabled(true);
        getSupportActionBar().setIcon(R.drawable.bluetoothdisconnected);

        checkPermissions();
        IntentFilter filter = new IntentFilter();
        for(BroadcastActionConstants b : BroadcastActionConstants.values())
            filter.addAction(b.getString());
        receiver = new BluetoothTestActivity.BluetoothBroadcastReceiver();
        registerReceiver(receiver, filter);
    }

    public void checkPermissions() {
        if (!(checkSelfPermission(Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED)) {
            requestPermissions(new String[]{Manifest.permission.ACCESS_COARSE_LOCATION}, 1);
        }
        if (!(checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED)) {
            requestPermissions(new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, 1);
        }
        LocationManager locationManager = (LocationManager)getSystemService(Context.LOCATION_SERVICE);
        boolean gpsStatus = locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER);
        System.out.println("GPS STATUS: " + gpsStatus);
    }

    @Override
    protected void onDestroy() {
        unregisterReceiver(receiver);
        if(bounded)
            unbindService(mConnection);
        super.onDestroy();
    }

    @Override
    protected void onStart() {
        super.onStart();
        Intent mIntent = new Intent(this, BluetoothLeService.class);
        bindService(mIntent,mConnection,BIND_AUTO_CREATE);
    }

    private class BluetoothBroadcastReceiver extends BroadcastReceiver {
        @SuppressLint("SetTextI18n")
        @Override
        public void onReceive(Context context, Intent intent) {
            if (intent.getAction().equals(BroadcastActionConstants.ACTION_CHARACTERISTIC_READ.getString())) {
                byte[] bytes = intent.getByteArrayExtra("bytes");
                if (bytes != null && bytes.length > 1) {
                    TextView textView = findViewById(R.id.bluetoothtestdatatextview);
                    float f = ((bytes[0] << 8) + bytes[1]) / 100f;
                    textView.setText(f + " ");
                    getSupportActionBar().setIcon(R.drawable.bluetoothsymbol);
                }
            } else if (intent.getAction().equals(BroadcastActionConstants.ACTION_GATT_SERVICES_DISCOVERED.getString())) {
            } else if (intent.getAction().equals(BroadcastActionConstants.ACTION_GATT_DISCONNECTED.getString())){
                getSupportActionBar().setIcon(R.drawable.bluetoothdisconnected);
                Snackbar.make(findViewById(R.id.bluetoothtestconstraintlayout),"Bluetooth Disconnected",Snackbar.LENGTH_SHORT).show();
            } else if(intent.getAction().equals(BroadcastActionConstants.ACTION_SCANNING.getString())){
                getSupportActionBar().setIcon(R.drawable.bluetoothsearching);
            } else if(intent.getAction().equals(BroadcastActionConstants.ACTION_WEAK_SIGNAL.getString()))
                Snackbar.make(findViewById(R.id.bluetoothtestconstraintlayout),"Weak Bluetooth Signal",Snackbar.LENGTH_SHORT).show();
        }
    }
    ServiceConnection mConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            System.out.println("SERVICE CONNECTED");
            BluetoothLeService.LocalBinder mLocalBinder = ((BluetoothLeService.LocalBinder)service);
            mServer = mLocalBinder.getServerInstance();
            bounded = true;
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            System.out.println("SERVICE DISCONNECTED");
            mServer = null;
            bounded = false;
        }
    };
}
