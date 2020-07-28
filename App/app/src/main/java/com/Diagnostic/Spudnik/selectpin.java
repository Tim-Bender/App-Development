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
import android.graphics.Color;
import android.os.AsyncTask;
import android.os.Bundle;
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

import java.util.ArrayList;
import java.util.Objects;

/**
 * Select a pin for which to view diagnostics for. Display pins in a vertical recyclerview.
 *
 * @author timothy.bender
 * @version dev 1.0.0
 * @see vehicle
 * @see ItemTouchHelper
 * @since dev 1.0.0
 */

public class selectpin extends AppCompatActivity {
    /**
     * vehicle object
     */
    private vehicle myvehicle;
    /**
     * Will be used to update textviews
     */
    private TextView textView;
    /**
     * An Arraylist of connection objects. Will be parsed, and then used by the recyclerview
     */
    private ArrayList<connection> connections = new ArrayList<>(24);
    /**
     * Custom adapter for our recyclerview
     */
    private ConnectionAdapter myAdapter;

    private BluetoothLeService bluetoothService;
    private BluetoothBroadcastReceiver receiver;

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
        Objects.requireNonNull(myvehicle).setConnections(getIntent().getParcelableArrayListExtra("connections"));
        textView = findViewById(R.id.connectorid);

        RecyclerView recyclerView = findViewById(R.id.selectpinrecyclerview); //setup our recyclerview
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        myAdapter = new ConnectionAdapter(this, connections, myvehicle); //we will be using our custom adapter for this recyclerview
        recyclerView.setAdapter(myAdapter); //set the adapter

        ItemTouchHelper helper = new ItemTouchHelper(new ItemTouchHelper.SimpleCallback(0, ItemTouchHelper.LEFT | ItemTouchHelper.RIGHT) { //setup our itemtouchhelper
            @Override
            public boolean onMove(@NonNull RecyclerView recyclerView, @NonNull RecyclerView.ViewHolder viewHolder, @NonNull RecyclerView.ViewHolder target) {
                return false;
            }

            @Override
            public void onSwiped(@NonNull RecyclerView.ViewHolder viewHolder, int direction) {
                if (direction == ItemTouchHelper.LEFT || direction == ItemTouchHelper.RIGHT) { //if you swipe right or left..
                    if (connections.size() != 1) { //and it isnt a 1 pin connection
                        connections.remove(viewHolder.getAdapterPosition()); //then we remove the item from the arraylist
                        myAdapter.notifyItemRemoved(viewHolder.getAdapterPosition()); //and we notify the adapter that there as been a change so it may animate it.
                    }
                }

            }
        });
        helper.attachToRecyclerView(recyclerView); //attach the helper above to our recyclerview
        IntentFilter filter = new IntentFilter();
        filter.addAction(BluetoothLeService.ACTION_CHARACTERISTIC_READ);
        filter.addAction(BluetoothLeService.ACTION_GATT_SERVICES_DISCOVERED);
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
            if (intent.getAction().equals(BluetoothLeService.ACTION_CHARACTERISTIC_READ)) {
                byte[] bytes = intent.getByteArrayExtra("bytes");
                if (bytes != null) {
                    updatevalues(bytes[0] * 0x100 + bytes[1]);
                    bluetoothService.requestConnectorVoltage(connections.get(0));
                }
            }
            else if (intent.getAction().equals(BluetoothLeService.ACTION_GATT_SERVICES_DISCOVERED)){
                AsyncTask.execute(() -> bluetoothService.requestConnectorVoltage(connections.get(0)));
            }
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
        if (connections.isEmpty()) //build them
            buildConnections();
        checkPermissions();
        bluetoothService = new BluetoothLeService(this);
    }

    /**
     * This method will parse the connections further and fill our arraylists.
     *
     * @since dev 1.0.0
     */
    private void buildConnections() {
        if (connections.isEmpty()) {
            String temp = myvehicle.getUniqueConnections().get(myvehicle.getLoc());
            int counter = 0;
            for (connection c : myvehicle.getConnections()) {
                if (c.getDirection().contains(temp.toLowerCase())) {
                    if (!myvehicle.getUniquePins().contains(c.getDirection())) {
                        myvehicle.addUniquePin(c.getDirection());
                        myvehicle.setPinCount(myvehicle.getPinCount() + 1);
                    }
                    if (counter > 0 && c.getS4().equals(connections.get(counter - 1).getS4()))
                        connections.get(counter - 1).setName(connections.get(counter - 1).getName() + " / " + c.getName());
                    else {
                        connections.add(c);
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
    private void updatevalues(int voltage) {
        String temp = myvehicle.getUniqueConnections().get(myvehicle.getLoc());
        String s1 = temp.substring(0, 1).toUpperCase(); //capitalize the first letter
        textView.setText(s1 + temp.substring(1));
        textView = findViewById(R.id.numberofpinstextfield);
        textView.setText(myvehicle.getMap(myvehicle.getUniqueConnections().get(myvehicle.getLoc())) + "p " + myvehicle.inout() + " Connector\n Connector Voltage\n=" + voltage);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.action_settings) {
            Intent i = new Intent(getBaseContext(), settings.class);
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
