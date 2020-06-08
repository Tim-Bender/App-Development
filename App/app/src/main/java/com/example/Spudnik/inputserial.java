package com.example.Spudnik;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.preference.PreferenceManager;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;
import java.io.InputStream;

/**
 * Author: Timothy Bender
 * timothy.bender@spudnik.com
 * 530-414-6778
 * Please see README before updating anything
 */

public class inputserial extends AppCompatActivity {
    public vehicle myvehicle;
    boolean empty = true;
    private EditText edittext,dealerText;
    public ImageView imageView;
    public TextView textView;
    public Switch toggle;
    private InputStream is;
    private boolean built = false;
    private int POINTTO;
    private SharedPreferences preferences;
    private SharedPreferences.Editor editor;
    private CheckBox checkBox;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_inputserial);

        try{
            Toolbar toolbar = findViewById(R.id.toolbar);
            setSupportActionBar(toolbar);
            setTitle("Input Serial Numer");
            toolbar.setTitleTextColor(Color.WHITE);
            imageView = findViewById(R.id.helpimage);
            imageView.setVisibility(View.GONE);
            textView = findViewById(R.id.helptextview);
            textView.setVisibility(View.GONE);
            myvehicle = getIntent().getParcelableExtra("myvehicle");
            POINTTO = getIntent().getIntExtra("pointto",0);
            checkBox = findViewById(R.id.rememberdealeridcheckbox);
            is = getResources().openRawResource(R.raw.parsedtest);
            preferences = PreferenceManager.getDefaultSharedPreferences(getBaseContext());
            editor = preferences.edit();

            this.edittext = findViewById(R.id.inputid);
            this.edittext.setOnKeyListener(new View.OnKeyListener() {
            @Override
            public boolean onKey(View v, int keyCode, KeyEvent event) {
                if((event.getAction() == KeyEvent.ACTION_DOWN) && (keyCode == KeyEvent.KEYCODE_ENTER)){
                    go(getCurrentFocus());
                    return true;
                }
                else {
                    if (!built) {
                        tryBuildDataBase();
                    }
                }

                return false;
                    }
        });
            this.dealerText = findViewById(R.id.dealeridtextview);
            this.dealerText.setOnKeyListener(new View.OnKeyListener() {
            @Override
            public boolean onKey(View v, int keyCode, KeyEvent event) {
                if((event.getAction() == KeyEvent.ACTION_DOWN) && (keyCode == KeyEvent.KEYCODE_ENTER)){
                    Toast.makeText(inputserial.this, "Enter A Serial Number", Toast.LENGTH_SHORT).show();
                        return true;
                    }
                return false;
            }
        });

            checkBox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                @Override
                public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                 if(isChecked){
                     editor.putString("dealerid", dealerText.getText().toString().trim());
                     editor.commit();

                 }
                }
            });
            Log.d("inputserial",preferences.getString("dealerid",""));
            if(!preferences.getString("dealerid", "").equals("")){
                checkBox.setChecked(true);
                dealerText.setText(preferences.getString("dealerid",""));
            }
            toggle = findViewById(R.id.helptoggle);
            final ImageView spudnikelectrical = findViewById(R.id.spudnikelectrical);
            toggle.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                @Override
                public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                    if(isChecked){
                        spudnikelectrical.setVisibility(View.GONE);
                        imageView.setVisibility(View.VISIBLE);
                        textView.setVisibility(View.VISIBLE);

                    }
                    else{
                        imageView.setVisibility(View.GONE);
                        textView.setVisibility(View.GONE);
                        spudnikelectrical.setVisibility(View.VISIBLE);

                    }
                }
            });

        } catch (Exception e) {
            Toast.makeText(this, "Unidentified Error", Toast.LENGTH_SHORT).show();
            e.printStackTrace();
        }
    }

    @Override
    protected void onStart(){
        super.onStart();
        try {
            if (preferences.getBoolean("nightmode", false)) {
                nightMode();
            }
        }catch(Exception Ignored){}


    }
    @Override
    protected void onResume() {
        super.onResume();
        try {
            if (preferences.getBoolean("nightmode", false)) {
                nightMode();
                return;
            }
            if (!preferences.getBoolean("nightmode", false)) {
                dayMode();
            }
        }catch(Exception ignored){}
    }


    /*
     * This method will build the vehicle class, by pulling data from the database csv
     */
    public void go(View view) {
        try {
            this.empty = true;
            String vehicleId = edittext.getText().toString();
            if (!(edittext.getText().length() < 2)) {
                if (!this.built) {
                    this.myvehicle = new vehicle(vehicleId);
                    this.myvehicle.setIs(is);
                    this.myvehicle.buildDataBase();
                }
                if (!myvehicle.getConnections().isEmpty() && this.myvehicle.checkDealer(this.dealerText.getText().toString().toLowerCase().trim())) {
                    if (checkBox.isChecked()) {
                        editor.putString("dealerid", dealerText.getText().toString().trim());
                    }
                    Intent i = new Intent(getBaseContext(), connectorselect.class);
                    i.putExtra("myvehicle", myvehicle);
                    i.putParcelableArrayListExtra("connections", this.myvehicle.getConnections());
                    startActivity(i);
                } else {
                    Toast.makeText(this, "Invalid", Toast.LENGTH_SHORT).show();
                }
            } else {
                Toast.makeText(this, "Invalid", Toast.LENGTH_SHORT).show();
            }
        } catch (Exception e) {
            Toast.makeText(this, "Resource Not Found", Toast.LENGTH_SHORT).show();
            e.printStackTrace();
        }
    }
    public void tryBuildDataBase(){
        AsyncTask.execute(new Runnable() {
            @Override
            public void run() {
                try {
                    final String vehicleId = edittext.getText().toString().toLowerCase().trim();
                    if (myvehicle.getVehicleIds().contains(vehicleId) || myvehicle.getVehicleIds().contains(vehicleId + "xx")) {
                        System.out.println("BUILDING DATABASE ON SERIAL!");
                        built = true;
                        myvehicle.setIs(is);
                        myvehicle.setVehicleId(vehicleId.toLowerCase().trim());
                        myvehicle.buildDataBase();
                    }
                } catch (Exception ignored) {}
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

    public void nightMode(){
        try {
            ConstraintLayout constraintLayout = findViewById(R.id.inputserialconstraintlayout);
            constraintLayout.setBackgroundColor(Color.parseColor("#333333"));
            TextView view = findViewById(R.id.inputserialtextview1);
            view.setTextColor(Color.WHITE);
            view = findViewById(R.id.inputserialtextview2);
            view.setTextColor(Color.WHITE);
            view = findViewById(R.id.helptextview);
            view.setTextColor(Color.WHITE);
            EditText editText = findViewById(R.id.dealeridtextview);
            editText.setTextColor(Color.WHITE);
            editText = findViewById(R.id.inputid);
            editText.setTextColor(Color.WHITE);
            CheckBox checkBox = findViewById(R.id.rememberdealeridcheckbox);
            checkBox.setTextColor(Color.WHITE);
            Switch myswitch = findViewById(R.id.helptoggle);
            myswitch.setTextColor(Color.WHITE);
            Button button = findViewById(R.id.gobutton);
            button.setBackgroundResource(R.drawable.nightmodebuttonselector);
            button.setTextColor(Color.WHITE);
        }catch(Exception ignored){}
    }


    public void dayMode(){
        try {
            ConstraintLayout constraintLayout = findViewById(R.id.inputserialconstraintlayout);
            constraintLayout.setBackgroundColor(Color.WHITE);
            TextView view = findViewById(R.id.inputserialtextview1);
            view.setTextColor(Color.BLACK);
            view = findViewById(R.id.inputserialtextview2);
            view.setTextColor(Color.BLACK);
            view = findViewById(R.id.helptextview);
            view.setTextColor(Color.BLACK);
            EditText editText = findViewById(R.id.dealeridtextview);
            editText.setTextColor(Color.BLACK);
            editText = findViewById(R.id.inputid);
            editText.setTextColor(Color.BLACK);
            CheckBox checkBox = findViewById(R.id.rememberdealeridcheckbox);
            checkBox.setTextColor(Color.BLACK);
            Switch myswitch = findViewById(R.id.helptoggle);
            myswitch.setTextColor(Color.BLACK);
            Button button = findViewById(R.id.gobutton);
            button.setBackgroundResource(R.drawable.daymodebuttonselector);
            button.setTextColor(Color.BLACK);
        }
        catch(Exception Ignored){}
    }

}
