package com.example.main;

import android.annotation.SuppressLint;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.Intent;
import android.content.res.Resources;
import android.graphics.Color;
import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import android.view.Gravity;
import android.view.View;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.LinearLayout;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Set;

public class selectpin extends AppCompatActivity {
    private vehicle myvehicle;
    private TextView  textView;
    private ArrayList<connection> connections = new ArrayList<>();
    private Toolbar toolbar;
    private Switch toggle;
    private final int REQUEST_ENABLE_BT = 1;
    private BluetoothAdapter myBluetoothAdapter;
    private Set<BluetoothDevice> connectedDevices;
    private BluetoothDevice myDevice;
    @SuppressLint("SetTextI18n")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.content_selectpin);
        this.toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        toolbar.setTitleTextColor(Color.WHITE);
        setTitle("Pin Selection");
        this.toolbar.setTitleTextColor(Color.WHITE);

        try {
            InputStream is = getResources().openRawResource(R.raw.parsedtest);
            this.myvehicle = getIntent().getParcelableExtra("myvehicle");
            assert this.myvehicle != null;
            this.myvehicle.setConnections(getIntent().<connection>getParcelableArrayListExtra("connections"));
            this.myvehicle.setIs(is);
            this.myvehicle.sortConnections(vehicle.SORT_BY_S4);

            this.textView = findViewById(R.id.connectorid);
            this.toggle=findViewById(R.id.sortbytoggle);
            toggle.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                @Override
                public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                    if(isChecked){
                        if(myvehicle.getLastSorted() == vehicle.SORT_BY_S4) {
                            myvehicle.sortConnections(vehicle.SORT_BY_NAME);
                            myvehicle.setLastSorted(vehicle.SORT_BY_NAME);
                            updatevalues();
                        }
                    }
                    else{
                        if(myvehicle.getLastSorted() == vehicle.SORT_BY_NAME){
                            myvehicle.sortConnections(vehicle.SORT_BY_S4);
                            myvehicle.setLastSorted(vehicle.SORT_BY_S4);
                            updatevalues();
                        }

                    }
                }
            });
        } catch (Resources.NotFoundException e) {
            e.printStackTrace();
        }
        updatevalues();

    }

    public void updatevalues(){
            try{
            String temp = this.myvehicle.getUniqueConnections().get(this.myvehicle.getLoc());
            String s1 = temp.substring(0, 1).toUpperCase();
            this.textView.setText(s1 + temp.substring(1));

            LinearLayout layout = findViewById(R.id.pins);
            layout.removeAllViews();
            int counter = 0;
            if (!this.myvehicle.getConnections().isEmpty()) {
                for (connection c : this.myvehicle.getConnections()) {
                    if (c.getDirection().contains(temp.toLowerCase())) {
                        if (!this.myvehicle.getUniquePins().contains(c.getDirection().toLowerCase().trim())) {
                            this.myvehicle.addUniquePin(c.getDirection().toLowerCase().trim());
                            this.myvehicle.setPinCount(this.myvehicle.getPinCount() + 1);
                        }
                        this.connections.add(c);
                        final Button btn = new Button(this);
                        btn.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.MATCH_PARENT));
                        btn.setTextSize(18);
                        btn.setTextColor(Color.BLACK);
                        btn.setGravity(Gravity.BOTTOM);
                        btn.setTag(counter);
                        btn.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                pinSelected((Integer) v.getTag());
                            }
                        });
                        btn.setText("Pin" + c.getS4() + " Signal:  " + c.getName());
                        btn.setGravity(Gravity.START);
                        layout.addView(btn);
                        counter++;

                    }
                }
            } else {
                Toast.makeText(this, "No Pins Available", Toast.LENGTH_SHORT).show();
                TextView textView = new TextView(this);
                textView.setText("No pin information available");
                layout.addView(textView);
            }
            this.textView = findViewById(R.id.numberofpinstextfield);
            this.textView.setText(this.myvehicle.getMap(this.myvehicle.getUniqueConnections().get(this.myvehicle.getLoc())) + "p " + this.myvehicle.inout() + " Connector");


        } catch (Resources.NotFoundException e) {
            Toast.makeText(this, "File Not Found Error", Toast.LENGTH_SHORT).show();
            e.printStackTrace();
        }

    }

    public void pinSelected(int loc){
        //Toast.makeText(this, o.toString(), Toast.LENGTH_SHORT).show();
        Intent i = new Intent(getBaseContext(), Pindiagnostic.class);
        i.putExtra("myvehicle", this.myvehicle);
        i.putParcelableArrayListExtra("connections",this.myvehicle.getConnections());
        i.putExtra("loc",loc);
        i.putParcelableArrayListExtra("uniqueconnections",this.connections);
        startActivity(i);


    }


}
