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
import android.os.Build;
import android.os.Bundle;
import android.os.IBinder;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.RecyclerView;
import androidx.recyclerview.widget.SnapHelper;

import com.Diagnostic.Spudnik.Bluetooth.BluetoothLeService;
import com.Diagnostic.Spudnik.Bluetooth.BroadcastActionConstants;
import com.Diagnostic.Spudnik.CustomObjects.Pin;
import com.Diagnostic.Spudnik.CustomObjects.Vehicle;
import com.google.android.material.snackbar.Snackbar;

import java.util.ArrayList;
import java.util.Objects;

/**
 * Display diagnostic information pertaining to the currently selected pin. Pins may be scrolled through horizontally.
 *
 * @author timothy.bender
 * @version dev 1.0.0
 * @see RecyclerView
 * @see SnapHelper
 * @since dev 1.0.0
 */
public class PinDiagnostic extends AppCompatActivity {
    /**
     * Vehicle object
     */
    private Vehicle myvehicle;
    /**
     * Our two textviews that will need to be kept updated
     */
    private TextView direction, connectorinformation;
    /**
     * the current connection that we are viewing
     */
    private Pin myPin;
    /**
     * Unique connections, scrollview will be filled by this
     */
    private ArrayList<Pin> uniquePins;
    /**
     * Current position users are in the scrollview
     */
    private int loc;

    private PinDiagnostic.BluetoothBroadcastReceiver receiver;

    private BluetoothLeService mServer;
    private boolean bounded = false;
    private boolean shouldReadCharacteristic = true;

    /**
     * Typical onCreate, we do setup the recyclerview with its scrolllistener and snaphelper
     *
     * @param savedInstanceState Bundle
     * @since dev 1.0.0
     */
    @RequiresApi(api = Build.VERSION_CODES.O)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.content_pindiagnostic);
        //get all of our objects
        myvehicle = getIntent().getParcelableExtra("myvehicle");
        Objects.requireNonNull(this.myvehicle).setPins(getIntent().getParcelableArrayListExtra("connections"));
        loc = getIntent().getIntExtra("loc", 0);
        uniquePins = getIntent().getParcelableArrayListExtra("uniqueconnections");
        //setup the toolbar as usual
        myPin = uniquePins.get(this.loc);
        Toolbar toolbar = findViewById(R.id.topAppBar);
        setSupportActionBar(toolbar);
        toolbar.setTitleTextColor(Color.WHITE);
        getSupportActionBar().setIcon(R.drawable.bluetoothdisconnected);

        direction = findViewById(R.id.direction);
        connectorinformation = findViewById(R.id.connectorinformation);
        updateValues(null);
        setUpUi();
        findViewById(R.id.pindiagnosticnextpinbutton).setOnClickListener((view) -> {
            loc = (loc++ == uniquePins.size()-1) ? 0 : loc++; //ternary operator. Determine if we have overflowed list
            myPin = uniquePins.get(loc);
            updateValues(null);
        });
        findViewById(R.id.pindiagnosticprevpinbutton).setOnClickListener((view) -> {
            loc = (loc == 0) ? uniquePins.size()-1 : --loc; //ternary operator. Determine if we have underflowed list
            myPin = uniquePins.get(loc);
            updateValues(null);
        });
        IntentFilter filter = new IntentFilter();
        for (BroadcastActionConstants b : BroadcastActionConstants.values())
            filter.addAction(b.getString());
        receiver = new PinDiagnostic.BluetoothBroadcastReceiver();
        registerReceiver(receiver, filter);
        Intent mIntent = new Intent(this, BluetoothLeService.class);
        bindService(mIntent, mConnection, BIND_AUTO_CREATE);
    }

    @Override
    protected void onResume() {
        shouldReadCharacteristic = true;
        if (mServer != null)
            mServer.requestConnectorVoltage(uniquePins);
        super.onResume();
    }

    @Override
    protected void onPause() {
        shouldReadCharacteristic = false;
        super.onPause();
    }


    @Override
    protected void onDestroy() {
        unregisterReceiver(receiver);
        if (bounded)
            unbindService(mConnection);
        super.onDestroy();
    }

    private void setUpUi() {
        String dir = uniquePins.get(0).inout();
        if (dir.equals("Input")) {
            findViewById(R.id.pindiagnosticvdc).setVisibility(View.GONE);
            findViewById(R.id.pindiagnostictestmodebutton).setVisibility(View.GONE);

        }
    }

    /**
     * This method will update the textviews
     *
     * @since dev 1.0.0
     */
    @RequiresApi(api = Build.VERSION_CODES.O)
    @SuppressLint("SetTextI18n")
    private void updateValues(@Nullable byte[] bytes) {
        String dir = myPin.inout();
        if(dir.equals("Output")) {
            float supplyV = 0,pwmFreq = 0;
            if (bytes != null) {
                int[] ints = new int[4];
                for(int i = 0; i < 4; i++){
                    ints[i] = (bytes[i] < 0) ? bytes[i] + 256 : bytes[i];
                }
                supplyV = ((ints[0] << 8) + ints[1]) / 100f;
                pwmFreq = (ints[2] << 8) + ints[3];
            }
            myPin = uniquePins.get(loc);
            String temp = myPin.getDirection();
            String s1 = temp.substring(0, 1).toUpperCase(); //capitalize the first letter
            direction.setText(s1 + temp.substring(1));
            connectorinformation.setText(myvehicle.getPinCount(myPin.getDirection().toLowerCase()) + " " + myPin.inout() + " Connector\nSupply Voltage = " + supplyV + " VDC\n" + "PWM Frequency = "+pwmFreq +" HZ" );
            setTitle("Viewing Pin:" + myPin.getS4());
            TextView textView = findViewById(R.id.pindiagnosticpinnumber);
            textView.setText("Pin " + myPin.getS4());
            textView = findViewById(R.id.pindiagnosticpinname);
            textView.setText(myPin.getName());
        }
        else{
            float f = 0;
            if (bytes != null) {
                f = ((bytes[0] << 8) + bytes[1]) / 100f;
            }
            myPin = uniquePins.get(loc);
            String temp = myPin.getDirection();
            String s1 = temp.substring(0, 1).toUpperCase(); //capitalize the first letter
            direction.setText(s1 + temp.substring(1));
            connectorinformation.setText(myvehicle.getPinCount(myPin.getDirection().toLowerCase()) + " " + myPin.inout() + " Connector\nConnectorVoltage\n" + f + "v");
            setTitle("Viewing Pin:" + myPin.getS4());
            TextView textView = findViewById(R.id.pindiagnosticpinnumber);
            textView.setText("Pin " + myPin.getS4());
            textView = findViewById(R.id.pindiagnosticpinname);
            textView.setText(myPin.getName());
        }
    }

    private class BluetoothBroadcastReceiver extends BroadcastReceiver {
        @RequiresApi(api = Build.VERSION_CODES.O)
        @Override
        public void onReceive(Context context, Intent intent) {
            if (intent.getAction().equals(BroadcastActionConstants.ACTION_CHARACTERISTIC_READ.getString())) {
                byte[] bytes = intent.getByteArrayExtra("bytes");
                if (bytes != null) {
                    updateValues(bytes);
                    //do the formatty stuffs
                    if (shouldReadCharacteristic && mServer != null)
                        mServer.requestConnectorVoltage(uniquePins);
                }
                getSupportActionBar().setIcon(R.drawable.bluetoothsymbol);
            } else if (intent.getAction().equals(BroadcastActionConstants.ACTION_GATT_DISCONNECTED.getString())) {
                getSupportActionBar().setIcon(R.drawable.bluetoothdisconnected);
                Snackbar.make(findViewById(R.id.pindiagnosticconstraintlayout), "Bluetooth Disconnected", Snackbar.LENGTH_SHORT).show();
            } else if (intent.getAction().equals(BroadcastActionConstants.ACTION_SCANNING.getString())) {
                getSupportActionBar().setIcon(R.drawable.bluetoothsearching);
            } else if (intent.getAction().equals(BroadcastActionConstants.ACTION_WEAK_SIGNAL.getString()))
                Snackbar.make(findViewById(R.id.pindiagnosticconstraintlayout), "Weak Bluetooth Signal", Snackbar.LENGTH_SHORT).show();
            else if(intent.getAction().equals(BroadcastActionConstants.ACTION_GATT_SERVICES_DISCOVERED.getString()))
                mServer.requestConnectorVoltage(uniquePins);
        }
    }

    /**
     * Button redirect to send users to the pinlocation activity
     *
     * @param view View
     * @since dev 1.0.0
     */
    public void viewpinloc(View view) {
        Intent i = new Intent(getBaseContext(), PinLocation.class);
        i.putExtra("myvehicle", myvehicle);
        i.putParcelableArrayListExtra("connections", myvehicle.getPins());
        i.putExtra("myConnection", myPin);
        startActivity(i);
    }

    ServiceConnection mConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            System.out.println("SERVICE CONNECTED");
            BluetoothLeService.LocalBinder mLocalBinder = ((BluetoothLeService.LocalBinder) service);
            mServer = mLocalBinder.getServerInstance();
            mServer.requestConnectorVoltage(uniquePins);
            bounded = true;
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            System.out.println("SERVICE DISCONNECTED");
            mServer = null;
            bounded = false;
        }
    };

    /**
     * Button redirect to send users towards the pintestmode, will pass to warningscreen activity
     *
     * @param view View
     * @since dev 1.0.0
     */
    public void testMode(View view) {
        Intent i = new Intent(getApplicationContext(), WarningScreen.class);
        i.putExtra("myvehicle", myvehicle);
        i.putParcelableArrayListExtra("connections", myvehicle.getPins());
        i.putExtra("myConnection", myPin);
        startActivity(i);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.action_settings) {
            Intent i = new Intent(getBaseContext(), Settings.class);
            startActivity(i);
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.toolbarbuttons, menu);
        return true;
    }
}
