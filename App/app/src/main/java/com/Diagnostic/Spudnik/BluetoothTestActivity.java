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
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.Diagnostic.Spudnik.Bluetooth.BluetoothLeService;
import com.Diagnostic.Spudnik.Bluetooth.BroadcastActionConstants;
import com.Diagnostic.Spudnik.CustomObjects.Connection;

public class BluetoothTestActivity extends AppCompatActivity {
    private BluetoothLeService bluetoothService;
    private BluetoothBroadcastReceiver receiver;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_bluetooth_test);
        checkPermissions();
        bluetoothService = new BluetoothLeService(this);
        findViewById(R.id.bluetoothteststartscanbutton).setOnClickListener(v -> bluetoothService.scanLeDevice(true));
        findViewById(R.id.bluetoothteststopscanbutton).setOnClickListener(v -> bluetoothService.scanLeDevice(false));
        IntentFilter filter = new IntentFilter();
        filter.addAction(BroadcastActionConstants.ACTION_CHARACTERISTIC_READ.getString());
        filter.addAction(BroadcastActionConstants.ACTION_GATT_SERVICES_DISCOVERED.getString());
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
    }

    @Override
    protected void onDestroy() {
        bluetoothService.scanLeDevice(false);
        unregisterReceiver(receiver);
        bluetoothService.disconnect(true);
        super.onDestroy();
    }

    @Override
    protected void onStart() {
        super.onStart();
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
                    bluetoothService.requestConnectorVoltage(new Connection("bacon", "1", "", "", "", "none"));
                }
            } else if (intent.getAction().equals(BroadcastActionConstants.ACTION_GATT_SERVICES_DISCOVERED.getString())) {
                bluetoothService.requestConnectorVoltage(new Connection("bacon", "1", "", "", "", "none"));
            }
        }
    }
}
