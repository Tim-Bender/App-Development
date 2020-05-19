package com.example.main;

import android.content.res.Resources;
import android.os.Bundle;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.snackbar.Snackbar;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import java.io.InputStream;

public class Pindiagnostic extends AppCompatActivity {

    private vehicle myvehicle;
    private TextView textView;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_pindiagnostic);
        try {
            Toolbar toolbar = findViewById(R.id.toolbar);
            setSupportActionBar(toolbar);
            InputStream is = getResources().openRawResource(R.raw.parsedtest);
            this.myvehicle = getIntent().getParcelableExtra("myvehicle");
            assert this.myvehicle != null;
            this.myvehicle.setConnections(getIntent().<connection>getParcelableArrayListExtra("connections"));
            this.myvehicle.setIs(is);

            updateValues();
            FloatingActionButton fab = findViewById(R.id.fab);
            fab.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
                            .setAction("Action", null).show();
                }
            });
        } catch (Resources.NotFoundException e) {
            Toast.makeText(this, "File Not Found Exception", Toast.LENGTH_SHORT).show();
            e.printStackTrace();
        } catch(Exception e){
            Toast.makeText(this, "File Not Found Exception", Toast.LENGTH_SHORT).show();
            e.printStackTrace();
        }
    }

    public void updateValues(){
        this.textView = findViewById(R.id.direction);
        String temp = this.myvehicle.getUniqueConnections().get(this.myvehicle.getLoc());
        String s1 = temp.substring(0, 1).toUpperCase();
        this.textView.setText(s1 + temp.substring(1));
        //connectorinformation
        this.textView = findViewById(R.id.connectorinformation);
        this.textView.setText(this.myvehicle.getPinCount() + "p " + this.myvehicle.inout() + " Connector");


    }

    public void nextPin(){

    }

}
