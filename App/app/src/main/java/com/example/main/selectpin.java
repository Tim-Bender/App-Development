package com.example.main;

import android.annotation.SuppressLint;
import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import org.w3c.dom.Text;

import java.io.InputStream;

public class selectpin extends AppCompatActivity {
    private vehicle myvehicle;
    private TextView  textView;
    @SuppressLint("SetTextI18n")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_selectpin);
        InputStream is = getResources().openRawResource(R.raw.parsedtest);
        this.myvehicle = getIntent().getParcelableExtra("myvehicle");
        this.myvehicle.setIs(is);

        this.textView = findViewById(R.id.connectorid);
        String temp = this.myvehicle.getUniqueConnections().get(this.myvehicle.getLoc());
        String s1 = temp.substring(0,1).toUpperCase();
        this.textView.setText(s1 + temp.substring(1));

        LinearLayout layout = findViewById(R.id.pins);
        if(this.myvehicle.getConnections().get(0) != null) {
            for (connection c : this.myvehicle.getConnections()) {
                if (c.getDirection().contains(temp.toLowerCase())) {
                    System.out.println("Adding button");
                    Button btn = new Button(this);
                    btn.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.MATCH_PARENT));
                    btn.setText("Pin" + c.getS4() + " Signal:  " + c.getName());
                    layout.addView(btn);
                }
            }
        }
        else{
            Toast.makeText(this, "No Pins Available", Toast.LENGTH_SHORT).show();
            TextView textView = new TextView(this);
            textView.setText("No pin information available");
            layout.addView(textView);
        }

    }

    public void Back(View view){
        finish();
    }

}
