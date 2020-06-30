package com.Diagnostic.Spudnik;

import android.annotation.SuppressLint;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Color;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Objects;
/**
 * @author timothy.bender
 * @version dev1.0.0
 *
 * Welcome to the pin selection activity.
 */

public class selectpin extends AppCompatActivity {
    private vehicle myvehicle;
    private TextView  textView;
    private ArrayList<connection> connections = new ArrayList<>();
    private Handler handler = new Handler();
    private ConnectionAdapter myAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        try {
            super.onCreate(savedInstanceState);
            setContentView(R.layout.content_selectpin);
            Toolbar toolbar = findViewById(R.id.topAppBar);
            setSupportActionBar(toolbar);
            setTitle("Pin Selection");
            toolbar.setTitleTextColor(Color.WHITE);
            LocalBroadcastManager.getInstance(this).registerReceiver(mReceiver2,new IntentFilter("incomingboolean"));
            myvehicle = getIntent().getParcelableExtra("myvehicle");
            Objects.requireNonNull(myvehicle).setConnections(getIntent().<connection>getParcelableArrayListExtra("connections"));
            textView = findViewById(R.id.connectorid);
            RecyclerView recyclerView = findViewById(R.id.selectpinrecyclerview);
            recyclerView.setLayoutManager(new LinearLayoutManager(this));
            myAdapter = new ConnectionAdapter(this,connections,myvehicle);
            recyclerView.setAdapter(myAdapter);
            ItemTouchHelper helper = new ItemTouchHelper(new ItemTouchHelper.SimpleCallback(0, ItemTouchHelper.LEFT | ItemTouchHelper.RIGHT) {
                @Override
                public boolean onMove(@NonNull RecyclerView recyclerView, @NonNull RecyclerView.ViewHolder viewHolder, @NonNull RecyclerView.ViewHolder target) {
                    return false;
                }

                @Override
                public void onSwiped(@NonNull RecyclerView.ViewHolder viewHolder, int direction) {
                    if(direction == ItemTouchHelper.LEFT || direction == ItemTouchHelper.RIGHT){
                        connections.remove(viewHolder.getAdapterPosition());
                        myAdapter.notifyItemRemoved(viewHolder.getAdapterPosition());
                    }

                }
            });
            helper.attachToRecyclerView(recyclerView);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onResume(){
        super.onResume();
        myAdapter.notifyDataSetChanged();
    }
    @Override
    public void onStart(){
        super.onStart();
        try {
            AsyncTask.execute(new Runnable() {
                @Override
                public void run() {
                    myvehicle.sortConnections(getApplicationContext());
                    if(connections.isEmpty()) {
                        buildConnections();
                    }
                }
            });
        } catch (Exception ignored) {}
    }
    @SuppressLint("SetTextI18n")
    @Override
    protected void onDestroy() {
        super.onDestroy();
        try {
            unregisterReceiver(mReceiver2);
        } catch (Exception ignored) {
        }
    }

    BroadcastReceiver mReceiver2 = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            try {
                boolean bool = intent.getBooleanExtra("boolean", false);
                if (bool) {
                    sortConnections();
                    updatevalues();
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    };



    public void buildConnections(){
        try {
            if(connections.isEmpty()) {
                String temp = myvehicle.getUniqueConnections().get(myvehicle.getLoc());
                int counter = 0;
                for (connection c : myvehicle.getConnections()) {
                    if (c.getDirection().contains(temp.toLowerCase())) {
                        if (!myvehicle.getUniquePins().contains(c.getDirection())) {
                            myvehicle.addUniquePin(c.getDirection());
                            myvehicle.setPinCount(myvehicle.getPinCount() + 1);
                        }
                        if (counter > 0 && c.getS4().equals(connections.get(counter - 1).getS4())) {
                            connections.get(counter - 1).setName(connections.get(counter - 1).getName() + " / " + c.getName());
                        } else {
                            connections.add(c);
                            counter++;
                        }
                    }
                }
                myAdapter.notifyDataSetChanged();
            }
        }catch (Exception e){
            e.printStackTrace();
        }
    }




    @SuppressLint("SetTextI18n")
    public void updatevalues(){
        handler.post(new Runnable() {
            @Override
            public void run() {
                try{
                    String temp = myvehicle.getUniqueConnections().get(myvehicle.getLoc());
                    String s1 = temp.substring(0, 1).toUpperCase();
                    textView.setText(s1 + temp.substring(1));
                    textView = findViewById(R.id.numberofpinstextfield);
                    textView.setText(myvehicle.getMap(myvehicle.getUniqueConnections().get(myvehicle.getLoc())) + "p " + myvehicle.inout() + " Connector\n Connector Voltage\n=12.5VDC");
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });
    }


    public void sortConnections(){
        AsyncTask.execute(new Runnable() {
            @Override
            public void run() {
                try{
                   Collections.sort(connections);
                }catch(Exception e){
                    e.printStackTrace();
                }
            }
        });
    }

    
    @Override
    public boolean onOptionsItemSelected(MenuItem item){
        if(item.getItemId() == R.id.action_settings){
            Intent i = new Intent(getBaseContext(),settings.class);
            startActivity(i);
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    public boolean onCreateOptionsMenu(Menu menu){
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.toolbarbuttons,menu);
        return true;
    }

}
