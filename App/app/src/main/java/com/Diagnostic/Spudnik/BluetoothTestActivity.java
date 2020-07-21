package com.Diagnostic.Spudnik;

import android.Manifest;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.view.View;

import androidx.appcompat.app.AppCompatActivity;

public class BluetoothTestActivity extends AppCompatActivity {
    private BluetoothLeServiceTest bluetoothService;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_bluetooth_test);
        bluetoothService = new BluetoothLeServiceTest(this);
        checkPermissions();
        findViewById(R.id.bluetoothteststartscanbutton).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                bluetoothService.scanLeDevice(true);
            }
        });
        findViewById(R.id.bluetoothteststopscanbutton).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                bluetoothService.scanLeDevice(false);
            }
        });
    }

    public void checkPermissions(){
        if(!(checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED)){
            requestPermissions(new String[]{Manifest.permission.ACCESS_FINE_LOCATION},1);
        }
    }

    @Override
    protected void onDestroy(){
        bluetoothService.scanLeDevice(false);
        super.onDestroy();
    }


}
