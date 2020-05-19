package com.example.main;

import android.annotation.SuppressLint;
import android.content.res.Resources;
import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;
import android.view.Gravity;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;
import java.io.InputStream;

public class selectpin extends AppCompatActivity {
    private vehicle myvehicle;
    private TextView  textView;
    @SuppressLint("SetTextI18n")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_selectpin);
        setTitle("Pin Selection");
        try {
            InputStream is = getResources().openRawResource(R.raw.parsedtest);
            this.myvehicle = getIntent().getParcelableExtra("myvehicle");
            assert this.myvehicle != null;
            this.myvehicle.setConnections(getIntent().<connection>getParcelableArrayListExtra("connections"));
            this.myvehicle.setIs(is);
            this.myvehicle.sortConnections(vehicle.SORT_BY_S4);

            this.textView = findViewById(R.id.connectorid);
            String temp = this.myvehicle.getUniqueConnections().get(this.myvehicle.getLoc());
            String s1 = temp.substring(0, 1).toUpperCase();
            this.textView.setText(s1 + temp.substring(1));

            LinearLayout layout = findViewById(R.id.pins);
            if (!this.myvehicle.getConnections().isEmpty()) {
                for (connection c : this.myvehicle.getConnections()) {
                    if (c.getDirection().contains(temp.toLowerCase())) {
                        if (!this.myvehicle.getUniquePins().contains(c.getS4().toLowerCase())) {
                            this.myvehicle.addUniquePin(c.getS4());
                            this.myvehicle.setPinCount(this.myvehicle.getPinCount() + 1);
                        }
                        Button btn = new Button(this);
                        btn.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.MATCH_PARENT));
                        btn.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                //null for now
                            }
                        });
                        btn.setText("Pin" + c.getS4() + " Signal:  " + c.getName());
                        btn.setGravity(Gravity.START);
                        layout.addView(btn);
                    }
                }
            } else {
                Toast.makeText(this, "No Pins Available", Toast.LENGTH_SHORT).show();
                TextView textView = new TextView(this);
                textView.setText("No pin information available");
                layout.addView(textView);
            }
            this.textView = findViewById(R.id.numberofpinstextfield);
            this.textView.setText(this.myvehicle.getPinCount() + "p " + this.myvehicle.inout() + " Connector");
        } catch (Resources.NotFoundException e) {
            Toast.makeText(this, "File Not Found Error", Toast.LENGTH_SHORT).show();
            e.printStackTrace();
        }
    }

}
