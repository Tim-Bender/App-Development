package com.example.Spudnik;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.preference.PreferenceManager;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.Space;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class pinlocation extends AppCompatActivity {

    private int loc;
    private ArrayList<connection> uniqueConnections;
    private connection myConnection;
    private vehicle myvehicle;
    private TextView textView;
    private Toolbar toolbar;
    private SharedPreferences preferences;
    private Map<String,Boolean> orientations = new HashMap<>();
    private TableLayout tableLayout;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_pinlocation);
        this.toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        toolbar.setTitleTextColor(Color.WHITE);
        setTitle("Pin Location");
        this.toolbar.setTitleTextColor(Color.WHITE);

        try {
            myvehicle = getIntent().getParcelableExtra("myvehicle");
            myvehicle.setConnections(getIntent().<connection>getParcelableArrayListExtra("connections"));
            myvehicle.sortConnections(vehicle.SORT_BY_S4,this);
            myConnection = getIntent().getParcelableExtra("myConnection");
            loc = Integer.parseInt(myConnection.getS4());
            preferences = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
            tableLayout = findViewById(R.id.pinlocationtablelayout);
        } catch (Exception e) {
            e.printStackTrace();

        }
    }

    @Override
    protected void onStart(){
        super.onStart();
        fillHashMap();
        if(preferences.getBoolean("nightmode",false)){
            nightMode();
        }
        updateValues();

    }

    @Override
    public void onResume(){
        super.onResume();
        if(preferences.getBoolean("nightmode",false)){
            nightMode();
        }
        else{
            dayMode();
        }
    }

    private void updateValues(){
        try {
            this.textView = findViewById(R.id.pinlocationdirection);
            String temp = this.myConnection.getDirection();
            String s1 = temp.substring(0, 1).toUpperCase();
            this.textView.setText(s1 + temp.substring(1));
            this.textView = findViewById(R.id.pinlocationconnectorinformation);
            this.textView.setText(this.myvehicle.getMap(this.myvehicle.getUniqueConnections().get(this.myvehicle.getLoc())) + "p " + this.myvehicle.inout() + " Connector");
            buildTableLayout();
        } catch (Exception e) {
            e.printStackTrace();
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

    public void nextPin(View view){
        try {
            this.loc++;
            if (this.loc == this.uniqueConnections.size()) {
                this.loc = 0;
            }
            updateValues();
        } catch (Exception ignored) {
        }
    }

    public void prevPin(View view){
        try {
            this.loc--;
            if (this.loc < 0) {
                this.loc = this.uniqueConnections.size() - 1;
            }
            updateValues();
        } catch (Exception ignored) {

        }
    }

    public void nightMode(){
        try{
            ConstraintLayout layout = findViewById(R.id.pinlocationconstraintlayout);
            layout.setBackgroundColor(Color.parseColor("#333333"));
            TextView textView = findViewById(R.id.pinlocationdirection);
            textView.setTextColor(Color.WHITE);
            textView.setBackgroundResource(R.drawable.nightmodeback);
            LinearLayout layout1 = findViewById(R.id.pinlocationlinearlayout);
            layout1.setBackgroundResource(R.drawable.nightmodeback);
            textView = findViewById(R.id.pinlocationconnectorinformation);
            textView.setTextColor(Color.WHITE);
            textView = findViewById(R.id.pinlocationtextview3);
            textView.setTextColor(Color.WHITE);
            textView = findViewById(R.id.pinlocationvoltage);
            textView.setTextColor(Color.WHITE);
            Button button = findViewById(R.id.prevpin);
            button.setBackgroundResource(R.drawable.nightmodebuttonselector);
            button.setTextColor(Color.WHITE);
            button = findViewById(R.id.nextpin);
            button.setBackgroundResource(R.drawable.nightmodebuttonselector);
            button.setTextColor(Color.WHITE);
        }catch(Exception ignored){}

    }

    public void dayMode(){
        try{
            ConstraintLayout layout = findViewById(R.id.pinlocationconstraintlayout);
            layout.setBackgroundColor(Color.WHITE);
            TextView textView = findViewById(R.id.pinlocationdirection);
            textView.setTextColor(Color.BLACK);
            textView.setBackgroundResource(R.drawable.back);
            LinearLayout layout1 = findViewById(R.id.pinlocationlinearlayout);
            layout1.setBackgroundResource(R.drawable.back);
            textView = findViewById(R.id.pinlocationconnectorinformation);
            textView.setTextColor(Color.BLACK);
            textView = findViewById(R.id.pinlocationtextview3);
            textView.setTextColor(Color.BLACK);
            textView = findViewById(R.id.pinlocationvoltage);
            textView.setTextColor(Color.BLACK);
            Button button = findViewById(R.id.prevpin);
            button.setBackgroundResource(R.drawable.daymodebuttonselector);
            button.setTextColor(Color.BLACK);
            button = findViewById(R.id.nextpin);
            button.setBackgroundResource(R.drawable.daymodebuttonselector);
            button.setTextColor(Color.BLACK);
        }catch(Exception ignored){}

    }

    public void buildTableLayout(){
        TableRow row;
        TextView textView;
        Space newspace;
        int[] ids = getResources().getIntArray(R.array.pinlocationids);

    }

    public void fillHashMap(){
        //vertical orientation = true
        //horizontal orientation = false
        orientations.put("out1",true);
        orientations.put("out2",true);
        orientations.put("out3",true);
        orientations.put("out4",true);
        orientations.put("out5",true);
        orientations.put("out6",true);
        orientations.put("out7",true);
        orientations.put("out8",true);
        orientations.put("out9",true);

        orientations.put("in1",true);
        orientations.put("in2",true);

        orientations.put("in3",false);
        orientations.put("in4",false);

        orientations.put("exp11out",true);
        orientations.put("exp11in",false);
    }
}
