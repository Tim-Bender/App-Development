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

import android.annotation.SuppressLint;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.view.View;
import android.widget.Button;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.ToggleButton;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.Diagnostic.Spudnik.Bluetooth.BluetoothLeService;
import com.Diagnostic.Spudnik.Bluetooth.BroadcastActionConstants;
import com.Diagnostic.Spudnik.CustomObjects.Pin;
import com.Diagnostic.Spudnik.CustomObjects.Vehicle;
import com.google.android.material.snackbar.Snackbar;

import java.util.ArrayList;

/**
 * Allow diagnostics on a specific pin via bluetooth. Adjust voltages among other things.
 *
 * @author timothy.bender
 * @version dev 1.0.0
 * @see android.bluetooth.BluetoothGatt
 * @see Vehicle
 * @see android.bluetooth.BluetoothGattCharacteristic
 * @since dev 1.0.0
 */
public class PinTest extends AppCompatActivity {
    private SeekBar seekBar;
    private TextView pwmTextview;
    private Vehicle myvehicle;
    private ArrayList<Pin> pins;
    private Pin myPin;
    private int pwm = 0, loc;

    private BluetoothLeService mServer;
    private boolean bounded = false;
    private PinTest.BluetoothBroadcastReceiver receiver;
    private ToggleButton toggleButton;
    private boolean bluetoothToggle = false;
    private boolean onOffType = false;

    /**
     * Simple oncreate. Nothing special
     *
     * @param savedInstanceState Bundle
     * @since dev 1.0.0
     */
    @SuppressLint("SetTextI18n")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_pintest);

        Toolbar toolbar = findViewById(R.id.topAppBar);
        setSupportActionBar(toolbar);
        setTitle("Input Serial Number");
        toolbar.setTitleTextColor(Color.WHITE);
        getSupportActionBar().setIcon(R.drawable.bluetoothdisconnected);

        myvehicle = getIntent().getParcelableExtra("myvehicle");
        pins = getIntent().getParcelableArrayListExtra("connections");
        myPin = getIntent().getParcelableExtra("myConnection");
        loc = getIntent().getIntExtra("loc", 0);

        pwmTextview = findViewById(R.id.pintestpwmdisplay);
        seekBar = findViewById(R.id.pintestseekbar);
        toggleButton = findViewById(R.id.pintesttogglebutton);
        IntentFilter filter = new IntentFilter();
        for (BroadcastActionConstants b : BroadcastActionConstants.values())
            filter.addAction(b.getString());
        receiver = new PinTest.BluetoothBroadcastReceiver();
        registerReceiver(receiver, filter);
        if (BluetoothLeService.STATUS == BluetoothLeService.DISCONNECTED) {
            getSupportActionBar().setIcon(R.drawable.bluetoothdisconnected);
        } else if (BluetoothLeService.STATUS == BluetoothLeService.CONNECTED) {
            getSupportActionBar().setIcon(R.drawable.bluetoothsearching);
        } else if (BluetoothLeService.STATUS == BluetoothLeService.SEARCHING) {
            getSupportActionBar().setIcon(R.drawable.bluetoothsearching);
        }
    }

    /**
     * This method will set listeners to all the views which need one. It also contains the logic for the bar's +- buttons
     *
     * @since dev 1.0.0
     */
    @SuppressLint("SetTextI18n")
    @Override
    protected void onStart() {
        super.onStart();
        toggleButton.setOnCheckedChangeListener((buttonView, isChecked) -> {
            bluetoothToggle = isChecked;
            if (isChecked && mServer != null) {
                toggleButton.setBackgroundColor(getColor(R.color.offgreen));
                mServer.writeConnectorVoltage(pins, myPin, pwm);
            } else {
                toggleButton.setBackgroundColor(getColor(R.color.colorAccent));
            }
        });
        seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {  //set our listener for the scrollbar. When the user moves it, it will update the pwm textfield
            @SuppressLint("SetTextI18n")
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                pwm = progress;
                updatePwmStatus();
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });
        pwmTextview.setText(seekBar.getProgress() + " PWM");
        TextView pinnumber = findViewById(R.id.pintestpinnumber);
        pinnumber.setText("Pin " + myPin.getS4() + " Test");

        //the following assigns click listeners to all of our buttons and includes the logic. Lambda implementation
        findViewById(R.id.buttonplus5).setOnClickListener(v -> {
            pwm = (pwm > 4) ? pwm - 5 : 0;
            updatePwmStatus();
        });
        findViewById(R.id.buttonminus1).setOnClickListener(v -> {
            pwm = (pwm > 0 && !onOffType) ? --pwm : 0;
            updatePwmStatus();
        });
        findViewById(R.id.buttonplus1).setOnClickListener(v -> {
            if (!onOffType)
                pwm = (pwm < 100) ? ++pwm : 100;
            else
                pwm = 1;
            updatePwmStatus();
        });
        findViewById(R.id.buttonplus5).setOnClickListener(v -> {
            pwm = (pwm < 96) ? pwm + 5 : 100;
            updatePwmStatus();
        });
        Intent mIntent = new Intent(this, BluetoothLeService.class);
        bindService(mIntent, mConnection, BIND_AUTO_CREATE);
        updateTextFields();
    }

    @Override
    protected void onDestroy() {
        unregisterReceiver(receiver);
        if (bounded)
            unbindService(mConnection);
        super.onDestroy();
    }


    ServiceConnection mConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            System.out.println("SERVICE CONNECTED");
            BluetoothLeService.LocalBinder mLocalBinder = ((BluetoothLeService.LocalBinder) service);
            mServer = mLocalBinder.getServerInstance();
            updatePwmStatus();
            bounded = true;
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            System.out.println("SERVICE DISCONNECTED");
            mServer = null;
            bounded = false;
        }
    };

    private class BluetoothBroadcastReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (intent.getAction().equals(BroadcastActionConstants.ACTION_CHARACTERISTIC_WRITE.getString())) {
                getSupportActionBar().setIcon(R.drawable.bluetoothsymbol);
                if (mServer != null && bluetoothToggle) {
                    Handler handler = new Handler();
                    handler.postDelayed(() -> mServer.writeConnectorVoltage(pins, myPin, pwm), 400);
                }
            } else if (intent.getAction().equals(BroadcastActionConstants.ACTION_GATT_DISCONNECTED.getString())) {
                getSupportActionBar().setIcon(R.drawable.bluetoothdisconnected);
                mServer.clearOperationManager();
                Snackbar.make(findViewById(R.id.pintestconstraintlayout), "Bluetooth Disconnected", Snackbar.LENGTH_SHORT).show();
            } else if (intent.getAction().equals(BroadcastActionConstants.ACTION_SCANNING.getString())) {
                getSupportActionBar().setIcon(R.drawable.bluetoothsearching);
                Snackbar.make(findViewById(R.id.pintestconstraintlayout), "Bluetooth Disconnected", Snackbar.LENGTH_SHORT).show();
            } else if (intent.getAction().equals(BroadcastActionConstants.ACTION_WEAK_SIGNAL.getString()))
                Snackbar.make(findViewById(R.id.pintestconstraintlayout), "Weak Bluetooth Signal", Snackbar.LENGTH_SHORT).show();
        }
    }

    /**
     *
     */
    @SuppressLint("SetTextI18n")
    private void updatePwmStatus() {
        if (mServer.getType(myPin) == mServer.FREQUENCYTYPE) {
            pwmTextview.setText(pwm * 100 + "Hz");
            seekBar.setProgress(pwm);
        } else if (!onOffType) {
            pwmTextview.setText(pwm + " PWM");
            seekBar.setProgress(pwm);
        } else {
            String text = (pwm == 0) ? "Off" : "On";
            pwmTextview.setText(text);
        }
    }

    @SuppressLint("SetTextI18n")
    private void updateTextFields() {
        String temp = myPin.getDirection();
        String s1 = temp.substring(0, 1).toUpperCase();
        TextView direction = findViewById(R.id.connectorid);
        direction.setText(s1 + temp.substring(1));
        TextView pinDescription = findViewById(R.id.pintestpindescription);
        pinDescription.setText(myvehicle.getPinCount(myPin.getDirection()) + "p Analog\n" + myvehicle.inout() + " Connector");
        TextView pinNumber = findViewById(R.id.pintestpinnumber);
        pinNumber.setText("Pin " + myPin.getS4() + " Test");
        if (onOffType) {
            findViewById(R.id.buttonminus5).setVisibility(View.INVISIBLE);
            findViewById(R.id.buttonplus5).setVisibility(View.INVISIBLE);
            findViewById(R.id.pintestseekbar).setVisibility(View.INVISIBLE);
            Button button = findViewById(R.id.buttonminus1);
            button.setText("Off");
            button = findViewById(R.id.buttonplus1);
            button.setText("On");
            pwmTextview.setText("Off");
        } else {
            findViewById(R.id.buttonminus5).setVisibility(View.VISIBLE);
            findViewById(R.id.buttonplus5).setVisibility(View.VISIBLE);
            findViewById(R.id.pintestseekbar).setVisibility(View.VISIBLE);
            Button button = findViewById(R.id.buttonminus1);
            button.setText("-1");
            button = findViewById(R.id.buttonplus1);
            button.setText("+1");
            pwmTextview.setText(seekBar.getProgress() + " PWM");
        }
    }

    public void nextPin(View view) {
        bluetoothToggle = false;
        toggleButton.setChecked(false);
        loc = (loc == 0) ? pins.size() - 1 : --loc;
        myPin = pins.get(loc);
        onOffType = mServer.getType(myPin) == mServer.ONOFFTYPE;
        if (onOffType)
            pwm = 0;
        updateTextFields();
    }

    public void prevPin(View view) {
        bluetoothToggle = false;
        toggleButton.setChecked(false);
        loc = (loc++ == pins.size() - 1) ? 0 : loc++;
        myPin = pins.get(loc);
        onOffType = mServer.getType(myPin) == mServer.ONOFFTYPE;
        if (onOffType)
            pwm = 0;
        updateTextFields();
    }

}