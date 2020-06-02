package com.example.main;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.graphics.Color;
import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.preference.PreferenceManager;
import android.view.KeyEvent;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;
import java.io.InputStream;
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
    private Toolbar toolbar;
    private Spinner mySpinner;
    private SharedPreferences preferences;

    @SuppressLint("SetTextI18n")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.content_connectorselect);
        this.toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        this.toolbar.setTitleTextColor(Color.WHITE);
        this.mySpinner = findViewById(R.id.myconnectorspinner);
        try {
            preferences = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
            InputStream is = getResources().openRawResource(R.raw.parsedtest);
            setTitle("Connector Selection");
            this.myvehicle = getIntent().getParcelableExtra("myvehicle");
            Objects.requireNonNull(this.myvehicle).setConnections(getIntent().<connection>getParcelableArrayListExtra("connections"));
            this.myvehicle.setIs(is);

            List<String> connections = this.myvehicle.getUniqueConnections();

            ArrayAdapter<String> dataAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, connections);
            dataAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
            this.mySpinner.setAdapter(dataAdapter);
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

            this.mySpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
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
        } catch (Resources.NotFoundException e) {
            Toast.makeText(this, "File Not Found Exception", Toast.LENGTH_SHORT).show();
            e.printStackTrace();
        }catch(Exception e){
            Toast.makeText(this, "Unidentified error", Toast.LENGTH_SHORT).show();
            e.printStackTrace();
        }

    }
    @Override
    protected void onStart(){
        super.onStart();
        if(preferences.getBoolean("nightmode",false)){
            nightMode();
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
        } catch (Exception e) {
            Toast.makeText(this, "Undefined Error", Toast.LENGTH_SHORT).show();
            e.printStackTrace();
        }

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
            imageButton.setBackgroundResource(R.drawable.toolbargradient);
            imageButton = findViewById(R.id.connectorselectbutton2);
            imageButton.setBackgroundResource(R.drawable.toolbargradient);
            Button button = findViewById(R.id.connectorselectbutton3);
            button.setBackgroundResource(R.drawable.toolbargradient);
            button.setTextColor(Color.WHITE);
        } catch (Exception e) {
            e.printStackTrace();
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
            imageButton.setBackgroundResource(android.R.drawable.btn_default);
            imageButton = findViewById(R.id.connectorselectbutton2);
            imageButton.setBackgroundResource(android.R.drawable.btn_default);
            Button button = findViewById(R.id.connectorselectbutton3);
            button.setBackgroundResource(android.R.drawable.btn_default);
            button.setTextColor(Color.BLACK);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

}
