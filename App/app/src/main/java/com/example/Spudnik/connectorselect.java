package com.example.Spudnik;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.preference.PreferenceManager;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;
import java.util.List;
import java.util.Objects;

/**
 * Author: Timothy Bender
 * timothy.bender@spudnik.com
 * 530-414-6778
 * Please see README before updating anything
 */

public class connectorselect extends AppCompatActivity {
    private vehicle myvehicle;
    private EditText editText;
    private SharedPreferences preferences;

    @SuppressLint("SetTextI18n")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.content_connectorselect);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        toolbar.setTitleTextColor(Color.WHITE);
        Objects.requireNonNull(getSupportActionBar()).setIcon(R.mipmap.ic_launcher);
        preferences = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
    }
    @SuppressLint("SetTextI18n")
    @Override
    protected void onStart(){
        super.onStart();
        boolean nightMode = preferences.getBoolean("nightmode",false);
        if(nightMode){
            nightMode();
        }
        Spinner mySpinner = findViewById(R.id.myconnectorspinner);

        try {
            preferences = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
            setTitle("Connector Selection");
            this.myvehicle = getIntent().getParcelableExtra("myvehicle");
            Objects.requireNonNull(this.myvehicle).setConnections(getIntent().<connection>getParcelableArrayListExtra("connections"));

            List<String> connections = this.myvehicle.getUniqueConnections();
            ArrayAdapter<String> dataAdapter;
            dataAdapter = (nightMode) ? new ArrayAdapter<>(this, R.layout.spinner_item_night, connections) :
                    new ArrayAdapter<>(this, R.layout.spinner_item_day, connections);

            dataAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
            mySpinner.setAdapter(dataAdapter);
            this.editText = findViewById(R.id.connectorinput);
            this.editText.setOnKeyListener(new View.OnKeyListener() {
                @Override
                public boolean onKey(View v, int keyCode, KeyEvent event) {
                    if ((event.getAction() == KeyEvent.ACTION_DOWN) && (keyCode == KeyEvent.KEYCODE_ENTER)) {
                        next(getCurrentFocus());
                        return true;
                    }

                    return false;
                }
            });

            mySpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                @Override
                public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                    Object item = parent.getItemAtPosition(position);
                    String temp = item.toString();
                    String s1 = temp.substring(0, 1).toUpperCase();
                    editText.setText(s1 + temp.substring(1));
                    myvehicle.setLoc(position);
                }

                @Override
                public void onNothingSelected(AdapterView<?> parent) {

                }
            });
            if (!this.myvehicle.getUniqueConnections().isEmpty()) {
                String temp = this.myvehicle.getUniqueConnections().get(this.myvehicle.getLoc());
                String s1 = temp.substring(0, 1).toUpperCase();
                this.editText.setText(s1 + temp.substring(1));
            } else {
                Toast.makeText(this, "No Connections", Toast.LENGTH_SHORT).show();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }


    }
    @Override
    protected void onResume() {
        super.onResume();
        if(preferences.getBoolean("nightmode",false)){
            nightMode();
            return;
        }
        if(!preferences.getBoolean("nightmode",false)){
            dayMode();
        }
    }

    //circularly go through the list of unique connections.
    @SuppressLint("SetTextI18n")
    public void down(View view) {
        try {
            if (!this.myvehicle.getUniqueConnections().isEmpty()) {
                this.myvehicle.setLoc(this.myvehicle.getLoc() + 1);
                if (this.myvehicle.getLoc() == myvehicle.getUniqueConnections().size()) {
                    this.myvehicle.setLoc(0);
                }
                System.out.println("Location " + this.myvehicle.getLoc());
                String temp = this.myvehicle.getUniqueConnections().get(this.myvehicle.getLoc());
                String s1 = temp.substring(0, 1).toUpperCase();
                System.out.println(s1 + temp.substring(1));
                System.out.println("Unique connections len: " + this.myvehicle.getUniqueConnections().size());
                this.editText.setText(s1 + temp.substring(1));
            } else {
                Toast.makeText(this, "No Connections", Toast.LENGTH_SHORT).show();
            }
        } catch (Exception e) {
            Toast.makeText(this, "Arraylist Error", Toast.LENGTH_SHORT).show();
            e.printStackTrace();
        }
    }

    @SuppressLint("SetTextI18n")
    public void up(View view){
        try {
            if (!this.myvehicle.getUniqueConnections().isEmpty()) {
                this.myvehicle.setLoc(this.myvehicle.getLoc() - 1);
                if (this.myvehicle.getLoc() < 0) {
                    this.myvehicle.setLoc(this.myvehicle.getUniqueConnections().size() - 1);
                }
                System.out.println("Location: " + this.myvehicle.getLoc());
                String temp = this.myvehicle.getUniqueConnections().get(this.myvehicle.getLoc());
                String s1 = temp.substring(0, 1).toUpperCase();
                this.editText.setText(s1 + temp.substring(1));
            } else {
                Toast.makeText(this, "No Connections", Toast.LENGTH_SHORT).show();
            }
        } catch (Exception e) {
            Toast.makeText(this, "ArrayList Error", Toast.LENGTH_SHORT).show();
            e.printStackTrace();
        }
    }

    public void next(View view){
        try {
            String connector = this.editText.getText().toString();
            if (!connector.isEmpty()) {
                if (this.myvehicle.getUniqueConnections().contains(connector.toLowerCase())) {
                    this.myvehicle.setLoc(this.myvehicle.getUniqueConnections().indexOf(connector.toLowerCase()));
                    Intent i = new Intent(getBaseContext(), selectpin.class);
                    i.putParcelableArrayListExtra("connections",this.myvehicle.getConnections());
                    i.putExtra("myvehicle", this.myvehicle);
                    startActivity(i);
                } else {
                    Toast.makeText(this, "Invalid", Toast.LENGTH_SHORT).show();
                }
            } else {
                Toast.makeText(this, "Empty", Toast.LENGTH_SHORT).show();
            }
        } catch (Exception ignored) {

        }

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

    public void nightMode(){
        try {
            ConstraintLayout constraintLayout = findViewById(R.id.connectorselectcontraintlayout);
            constraintLayout.setBackgroundColor(Color.parseColor("#333333"));
            TextView textView = findViewById(R.id.connectorselecttextview1);
            textView.setTextColor(Color.WHITE);
            EditText editText = findViewById(R.id.connectorinput);
            editText.setTextColor(Color.WHITE);
            ImageButton imageButton = findViewById(R.id.connectorselectbutton1);
            imageButton.setBackgroundResource(R.drawable.nightmodebuttonselector);
            imageButton = findViewById(R.id.connectorselectbutton2);
            imageButton.setBackgroundResource(R.drawable.nightmodebuttonselector);
            Button button = findViewById(R.id.connectorselectbutton3);
            button.setBackgroundResource(R.drawable.nightmodebuttonselector);
            button.setTextColor(Color.WHITE);
        } catch (Exception ignored) {
        }

    }

    public void dayMode(){
        try {
            ConstraintLayout constraintLayout = findViewById(R.id.connectorselectcontraintlayout);
            constraintLayout.setBackgroundColor(Color.WHITE);
            TextView textView = findViewById(R.id.connectorselecttextview1);
            textView.setTextColor(Color.BLACK);
            EditText editText = findViewById(R.id.connectorinput);
            editText.setTextColor(Color.BLACK);
            ImageButton imageButton = findViewById(R.id.connectorselectbutton1);
            imageButton.setBackgroundResource(R.drawable.daymodebuttonselector);
            imageButton = findViewById(R.id.connectorselectbutton2);
            imageButton.setBackgroundResource(R.drawable.daymodebuttonselector);
            Button button = findViewById(R.id.connectorselectbutton3);
            button.setBackgroundResource(R.drawable.daymodebuttonselector);
            button.setTextColor(Color.BLACK);
        } catch (Exception ignored) {
        }
    }

}
