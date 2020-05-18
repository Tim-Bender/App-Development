package com.example.main;

import android.content.Intent;
import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;
import android.view.KeyEvent;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;
import java.io.InputStream;

/**
 * Author: Timothy Bender
 * timothy.bender@spudnik.com
 * 530-414-6778
 * Please see README before updating anything
 */

public class connectorselect extends AppCompatActivity {
    private vehicle myvehicle;
    EditText editText;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_connectorselect);
        InputStream is = getResources().openRawResource(R.raw.parsedtest);
        this.myvehicle = getIntent().getParcelableExtra("myvehicle");
        this.myvehicle.setIs(is);
        this.editText = findViewById(R.id.connectorinput);
        this.editText.setOnKeyListener(new View.OnKeyListener() {
            @Override
            public boolean onKey(View v, int keyCode, KeyEvent event) {
                if((event.getAction() == KeyEvent.ACTION_DOWN) && (keyCode == KeyEvent.KEYCODE_ENTER)){
                    next(getCurrentFocus());
                    return true;
                }

                return false;
            }
        });
        if(!this.myvehicle.getUniqueConnections().isEmpty()) {
            String temp = this.myvehicle.getUniqueConnections().get(this.myvehicle.getLoc());
            String s1 = temp.substring(0,1).toUpperCase();
            this.editText.setText(s1 + temp.substring(1));
        }
        else{
            Toast.makeText(this, "No Connections", Toast.LENGTH_SHORT).show();
        }
    }

    //circularly go through the list of unique connections.
    public void up(View view) {
        if (!this.myvehicle.getUniqueConnections().isEmpty()) {
            this.myvehicle.setLoc(this.myvehicle.getLoc() + 1);
            if (this.myvehicle.getLoc() == myvehicle.getUniqueConnections().size()) {
                this.myvehicle.setLoc(0);
            }
            System.out.println("Location " + this.myvehicle.getLoc());
            String temp = this.myvehicle.getUniqueConnections().get(this.myvehicle.getLoc());
            String s1 = temp.substring(0,1).toUpperCase();
            System.out.println(s1 + temp.substring(1));
            System.out.println("Unique connections len: " + this.myvehicle.getUniqueConnections().size());
            this.editText.setText(s1 + temp.substring(1));
        }
        else{
            Toast.makeText(this, "No Connections", Toast.LENGTH_SHORT).show();
        }
    }

    public void down(View view){
        if(!this.myvehicle.getUniqueConnections().isEmpty()) {
            this.myvehicle.setLoc(this.myvehicle.getLoc() - 1);
            if (this.myvehicle.getLoc() < 0) {
                this.myvehicle.setLoc(this.myvehicle.getUniqueConnections().size() - 1);
            }
            System.out.println("Location: " + this.myvehicle.getLoc());
            String temp = this.myvehicle.getUniqueConnections().get(this.myvehicle.getLoc());
            String s1 = temp.substring(0,1).toUpperCase();
            this.editText.setText(s1 + temp.substring(1));
        }
        else{
            Toast.makeText(this, "No Connections", Toast.LENGTH_SHORT).show();
        }
    }

    public void back(View view){
        finish();
    }

    public void next(View view){
        String connector = this.editText.getText().toString();
        if(!connector.isEmpty()) {
            if (this.myvehicle.getUniqueConnections().contains(connector.toLowerCase())) {
                this.myvehicle.setLoc(this.myvehicle.getUniqueConnections().indexOf(connector.toLowerCase()));
                Intent i = new Intent(getBaseContext(), selectpin.class);
                i.putExtra("myvehicle", this.myvehicle);
                startActivity(i);
            } else {
                Toast.makeText(this, "Invalid", Toast.LENGTH_SHORT).show();
            }
        }
        else{
            Toast.makeText(this, "Empty", Toast.LENGTH_SHORT).show();
        }

    }

}
