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
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.Space;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Objects;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * @author timothy.bender
 * @version dev1.0.0
 *
 * Welcome to the pin selection activity.
 */

public class selectpin extends AppCompatActivity {
    private vehicle myvehicle;
    private TextView  textView;
    private CopyOnWriteArrayList<connection> connections = new CopyOnWriteArrayList<>();
    private Handler handler = new Handler();

    @SuppressLint("SetTextI18n")
    @Override
    protected void onDestroy(){
        super.onDestroy();
        try {
            unregisterReceiver(mReceiver2);
        } catch (Exception ignored) {}

    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
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

    BroadcastReceiver mReceiver2 = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            try {
                boolean bool = intent.getBooleanExtra("boolean", false);
                if (bool) {
                    sortConnections();
                    updatevalues();
                }
            } catch (Exception ignored) {}
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
            }
        }catch (Exception ignored){}
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
                    LinearLayout layout = findViewById(R.id.pins);
                    layout.removeAllViews();
                    int counter = 0;
                    for(connection c : connections){
                        layout.addView(buildButton(c,counter));
                        Space space = new Space(getApplicationContext());
                        space.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, 0, .1f));
                        layout.addView(space);
                        counter++;
                    }
                    textView = findViewById(R.id.numberofpinstextfield);
                    textView.setText(myvehicle.getMap(myvehicle.getUniqueConnections().get(myvehicle.getLoc())) + "p " + myvehicle.inout() + " Connector\n Connector Voltage\n=12.5VDC");
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });
    }

    @SuppressLint("SetTextI18n")
    public Button buildButton(connection c,int id){
        Button btn = new Button(getApplicationContext());
        LinearLayout.LayoutParams textParams = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, 0, 1.5f);
        textParams.setMarginStart(10);
        textParams.setMarginEnd(10);
        btn.setLayoutParams(textParams);
        btn.setTextSize(14);
        btn.setTextColor(Color.WHITE);
        btn.setBackgroundResource(R.drawable.nightmodebuttonselector);
        btn.setGravity(Gravity.START);
        btn.setGravity(Gravity.CENTER_VERTICAL);
        btn.setPadding(20,0,0,0);
        btn.setTag(id);
        btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                pinSelected((Integer) v.getTag());
            }
        });
        btn.setText("Pin" + c.getS4() +" " +  c.getName());
        return btn;
    }

    public void sortConnections(){
        AsyncTask.execute(new Runnable() {
            @Override
            public void run() {
                try{
                   Collections.sort(connections);
                }catch(Exception ignored){}
            }
        });
    }

    public void pinSelected(int loc){
        Intent i = new Intent(getBaseContext(), Pindiagnostic.class);
        i.putExtra("myvehicle", myvehicle);
        i.putParcelableArrayListExtra("connections",myvehicle.getConnections());
        i.putExtra("loc",loc);
        i.putParcelableArrayListExtra("uniqueconnections",new ArrayList<>(connections));
        startActivity(i);


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
