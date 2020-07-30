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
import android.os.Bundle;
import android.os.IBinder;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.Diagnostic.Spudnik.Bluetooth.BluetoothLeService;
import com.Diagnostic.Spudnik.Bluetooth.BroadcastActionConstants;
import com.Diagnostic.Spudnik.CustomObjects.Pin;
import com.Diagnostic.Spudnik.CustomObjects.Vehicle;
import com.google.android.material.snackbar.Snackbar;

import java.util.ArrayList;
import java.util.Objects;

/**
 * Select a pin for which to view diagnostics for. Display pins in a vertical recyclerview.
 *
 * @author timothy.bender
 * @version dev 1.0.0
 * @see Vehicle
 * @see ItemTouchHelper
 * @since dev 1.0.0
 */

public class SelectPin extends AppCompatActivity {
    /**
     * vehicle object
     */
    private Vehicle myvehicle;
    /**
     * Will be used to update textviews
     */
    private TextView textView;
    /**
     * An Arraylist of connection objects. Will be parsed, and then used by the recyclerview
     */
    private ArrayList<Pin> pins = new ArrayList<>(24);
    /**
     * Custom adapter for our recyclerview
     */
    private ConnectionAdapter myAdapter;

    private BluetoothBroadcastReceiver receiver;
    private BluetoothLeService mServer;
    private boolean bounded = false;
    /**
     * Some interesting stuff going on in this onCreate. First we setup our recycler view. and then we setupon an itemtouchhelper which allows
     * for the "deleting" of elements by swiping them off the screen.
     *
     * @param savedInstanceState Bundle
     * @since dev 1.0.0
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.content_selectpin);

        Toolbar toolbar = findViewById(R.id.topAppBar); //typical toolbar setup
        setSupportActionBar(toolbar);
        setTitle("Pin Selection");
        toolbar.setTitleTextColor(Color.WHITE);

        myvehicle = getIntent().getParcelableExtra("myvehicle");
        Objects.requireNonNull(myvehicle).setPins(getIntent().getParcelableArrayListExtra("connections"));
        textView = findViewById(R.id.connectorid);

        RecyclerView recyclerView = findViewById(R.id.selectpinrecyclerview); //setup our recyclerview
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        myAdapter = new ConnectionAdapter(this, pins, myvehicle); //we will be using our custom adapter for this recyclerview
        recyclerView.setAdapter(myAdapter); //set the adapter

        ItemTouchHelper helper = new ItemTouchHelper(new ItemTouchHelper.SimpleCallback(0, ItemTouchHelper.LEFT | ItemTouchHelper.RIGHT) { //setup our itemtouchhelper
            @Override
            public boolean onMove(@NonNull RecyclerView recyclerView, @NonNull RecyclerView.ViewHolder viewHolder, @NonNull RecyclerView.ViewHolder target) {
                return false;
            }

            @Override
            public void onSwiped(@NonNull RecyclerView.ViewHolder viewHolder, int direction) {
                if (direction == ItemTouchHelper.LEFT || direction == ItemTouchHelper.RIGHT) { //if you swipe right or left..
                    if (pins.size() != 1) { //and it isnt a 1 pin connection
                        pins.remove(viewHolder.getAdapterPosition()); //then we remove the item from the arraylist
                        myAdapter.notifyItemRemoved(viewHolder.getAdapterPosition()); //and we notify the adapter that there as been a change so it may animate it.
                    }
                }

            }
        });
        helper.attachToRecyclerView(recyclerView); //attach the helper above to our recyclerview
        IntentFilter filter = new IntentFilter();
        for(BroadcastActionConstants b : BroadcastActionConstants.values())
            filter.addAction(b.getString());
        receiver = new BluetoothBroadcastReceiver();
        registerReceiver(receiver, filter);
    }

    public void checkPermissions() {
        if (!(checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED)) {
            requestPermissions(new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, 1);
        }
    }

    private class BluetoothBroadcastReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (intent.getAction().equals(BroadcastActionConstants.ACTION_CHARACTERISTIC_READ.getString())) {
                byte[] bytes = intent.getByteArrayExtra("bytes");
                if (bytes != null) {
                    updatevalues(((bytes[0] << 8) + bytes[1]) / 100f);
                    getSupportActionBar().setIcon(R.drawable.bluetoothsymbol);
                    mServer.requestConnectorVoltage(pins.get(0));
                }
            }
            else if (intent.getAction().equals(BroadcastActionConstants.ACTION_GATT_SERVICES_DISCOVERED.getString())){
                    mServer.requestConnectorVoltage(pins.get(0));
            }
            else if (intent.getAction().equals(BroadcastActionConstants.ACTION_GATT_DISCONNECTED.getString())){
                getSupportActionBar().setIcon(R.drawable.bluetoothdisconnected);
                Snackbar.make(findViewById(R.id.selectpinconstraintlayout),"Bluetooth Disconnected",Snackbar.LENGTH_SHORT).show();
            } else if(intent.getAction().equals(BroadcastActionConstants.ACTION_SCANNING.getString())){
                getSupportActionBar().setIcon(R.drawable.bluetoothsearching);
            }  else if(intent.getAction().equals(BroadcastActionConstants.ACTION_WEAK_SIGNAL.getString()))
                Snackbar.make(findViewById(R.id.selectpinconstraintlayout),"Weak Bluetooth Signal",Snackbar.LENGTH_SHORT).show();
        }
    }

    /**
     * We will use onResume to ensure that our recyclerview is up to date
     *
     * @since dev 1.0.0
     */
    @Override
    protected void onResume() {
        super.onResume();
        myAdapter.notifyDataSetChanged(); //notify that the dataset has changed
        updatevalues(0);
    }

    @Override
    protected void onDestroy(){
        unregisterReceiver(receiver);
        //AsyncTask.execute(() -> bluetoothService.disconnect(true));
        if(bounded){
            unbindService(mConnection);
            bounded = false;
        }
        super.onDestroy();
    }


    /**
     * We will sort our connections by s4 number and then pass them to buildconnections
     *
     * @since dev 1.0.0
     */
    @Override
    protected void onStart() {
        super.onStart();
        myvehicle.sortConnections(); //sort the connections
        if (pins.isEmpty()) //build them
            buildConnections();
        checkPermissions();
        Intent mIntent = new Intent(this, BluetoothLeService.class);
        bindService(mIntent,mConnection,BIND_AUTO_CREATE);
        //bluetoothService = new BluetoothLeService(this);
        //bluetoothService.resetKilledProcess();
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

    /**
     * This method will parse the connections further and fill our arraylists.
     *
     * @since dev 1.0.0
     */
    private void buildConnections() {
        if (pins.isEmpty()) {
            String temp = myvehicle.getUniqueConnections().get(myvehicle.getLoc());
            int counter = 0;
            for (Pin c : myvehicle.getPins()) {
                if (c.getDirection().contains(temp.toLowerCase())) {
                    if (!myvehicle.getUniquePins().contains(c.getDirection())) {
                        myvehicle.addUniquePin(c.getDirection());
                        myvehicle.setPinCount(myvehicle.getPinCount() + 1);
                    }
                    if (counter > 0 && c.getS4().equals(pins.get(counter - 1).getS4()))
                        pins.get(counter - 1).setName(pins.get(counter - 1).getName() + " / " + c.getName());
                    else {
                        pins.add(c);
                        counter++;
                    }
                }
            }
            myAdapter.notifyDataSetChanged(); //after we parse it, we notify the adapter that the dataset has changed
        }
    }

    /**
     * This method will update the textfields
     *
     * @since dev 1.0.0
     */
    @SuppressLint("SetTextI18n")
    private void updatevalues(float voltage) {
        String temp = myvehicle.getUniqueConnections().get(myvehicle.getLoc());
        String s1 = temp.substring(0, 1).toUpperCase(); //capitalize the first letter
        textView.setText(s1 + temp.substring(1));
        textView = findViewById(R.id.numberofpinstextfield);
        textView.setText(myvehicle.getMap(myvehicle.getUniqueConnections().get(myvehicle.getLoc())) + "p " + myvehicle.inout() + " Connector\n Connector Voltage\n=" + voltage+"v");
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
